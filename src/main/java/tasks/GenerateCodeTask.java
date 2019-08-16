package tasks;

import common.Const;
import extensions.GenerateCoeExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import structure.Column;
import structure.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Created by wb on 2018/11/26.
 */
public class GenerateCodeTask extends DefaultTask {
    private static final String SEPARATOR = System.getProperty("line.separator");

    private final Project project = getProject();

    private final GenerateCoeExtension generateCoeExtension = (GenerateCoeExtension) project.getExtensions().getByName(Const.EXTENSION_GENERATECODE_NAME);


    @TaskAction
    void generateCode() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        System.out.println("开始执行......");
        String host = generateCoeExtension.getHost();
        String port = generateCoeExtension.getPort();
        String dataBase = generateCoeExtension.getDataBase();
        String user = generateCoeExtension.getUser();
        String password = generateCoeExtension.getPassword();
        String driverName = "com.mysql.cj.jdbc.Driver";
        Class.forName(driverName).newInstance();
        // 在jdbc连接的url后面加上serverTimezone=GMT即可解决问题，如果需要使用gmt+8时区，需要写成GMT%2B8
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dataBase + "?serverTimezone=GMT%2B8";
        Connection connection = DriverManager.getConnection(url, user, password);
        List<Table> tableList = this.getStructure(connection);
        System.out.println("获得所有表结构......");
        if (generateCoeExtension.getTableNames().size() > 0) {
            System.out.println("获得指定表结构......");
            tableList = tableList.stream().filter(each -> generateCoeExtension.getTableNames().contains(each.getTableName())).collect(Collectors.toList());
        }
        this.createFile(tableList);
        System.out.println("执行成功......");
    }

    private List<Table> getStructure(Connection connection) throws SQLException {
        String dataBase = generateCoeExtension.getDataBase();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet resultSet = databaseMetaData.getTables(dataBase, null, null, null);
        List<Table> tableList = new ArrayList<>();
        while (resultSet.next()) {
            Table table = new Table();
            String tableName = resultSet.getString(3);
            table.setTableName(tableName);
            String getTableCommentSql = "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = '" + dataBase + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(getTableCommentSql);
            ResultSet tableCommentResultSet = preparedStatement.executeQuery();
            while (tableCommentResultSet.next()) {
                table.setTableComment(tableCommentResultSet.getString(1));
            }
            String getColumnSql = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, EXTRA, COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = '" + dataBase + "'";
            preparedStatement = connection.prepareStatement(getColumnSql);
            ResultSet columnResultSet = preparedStatement.executeQuery();
            List<Column> columnList = new ArrayList<>();
            while (columnResultSet.next()) {
                Column column = new Column();
                String columnName = columnResultSet.getString(1);
                column.setColumnName(columnName);
                column.setColumnType(this.getType(columnResultSet.getString(2)));
                column.setNullAble(Boolean.parseBoolean(columnResultSet.getString(3)));
                String key = columnResultSet.getString(4);
                if (key.equalsIgnoreCase("PRI")) {
                    column.setPrimary(true);
                } else {
                    column.setPrimary(false);
                }
                column.setDefaultValue(columnResultSet.getString(5));
                column.setExtra(columnResultSet.getString(6));
                column.setColumnComment(columnResultSet.getString(7));
                columnList.add(column);
            }
            table.setColumnList(columnList);
            tableList.add(table);
        }
        return tableList;
    }

    private String getType(String stringType) {
        if (stringType.startsWith("int")) {
            return "Integer";
        } else if  (stringType.startsWith("bigint")) {
            return "Long";
        }else if (stringType.startsWith("varchar") || stringType.startsWith("text")) {
            return "String";
        } else if (stringType.startsWith("date") || stringType.startsWith("datetime")) {
            return "Date";
        } else {
            // TODO Fixme
            return null;
        }
    }

    private void createFile(List<Table> tableList) {
        String rootDirPath = project.getProjectDir().getPath();
        String entityPath = generateCoeExtension.getEntityPath();
        String controllerPath = generateCoeExtension.getControllerPath();
        String servicePath = generateCoeExtension.getServicePath();
        String serviceImplPath = generateCoeExtension.getServiceImplPath();
        String daoPath = generateCoeExtension.getDaoPath();
        System.out.println("准备生成文件......");
        CountDownLatch countDownLatch = new CountDownLatch(tableList.size());
        tableList.forEach(each -> new Thread(() -> {
            String tableName = this.getUpCaseTableName(each.getTableName());
            String entityContent = this.getEntityContent(each, entityPath);
            this.doAction(rootDirPath, entityPath, entityContent, tableName);
            String controllerContent = this.getControllerContent(each, controllerPath, entityPath, servicePath);
            this.doAction(rootDirPath, controllerPath, controllerContent, tableName + "Controller");
            String serviceContent = this.getServiceContent(each, servicePath, entityPath);
            this.doAction(rootDirPath, servicePath, serviceContent, tableName + "Service");
            String serviceImplContent = this.getServiceImplContent(each, serviceImplPath, daoPath, entityPath, servicePath);
            this.doAction(rootDirPath, serviceImplPath, serviceImplContent, tableName + "ServiceImpl");
            String daoContent = this.getDaoContent(each, daoPath, entityPath);
            this.doAction(rootDirPath, daoPath, daoContent, tableName + "Dao");
            countDownLatch.countDown();
        }).start());
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            System.out.println("多线程执行任务异常");
            e.printStackTrace();
        }
    }

    private String getEntityContent(Table table, String entityPath) {
        String tableName = table.getTableName();
        List<Column> columnList = table.getColumnList();

        String entityContent = "";
        // package
        entityContent += "package " + entityPath + ";" + SEPARATOR + SEPARATOR;
        // import
        entityContent += "import lombok.Builder;" + SEPARATOR;
        entityContent += "import lombok.Data;" + SEPARATOR + SEPARATOR;
        entityContent += "import javax.persistence.Entity;" + SEPARATOR;
        entityContent += "import javax.persistence.Table;" + SEPARATOR;
        entityContent += "import java.util.Date;" + SEPARATOR;
        boolean isAutoIncrement = false;
        boolean isId = false;
        for (Column column : columnList) {
            if (column.getExtra().equalsIgnoreCase("auto_increment")) {
                isAutoIncrement = true;
            }
            if (column.getPrimary()) {
                isId = true;
            }
        }
        if (isId) {
            entityContent += "import javax.persistence.Id;" + SEPARATOR;
        }
        if (isAutoIncrement) {
            entityContent += "import javax.persistence.GeneratedValue;" + SEPARATOR + SEPARATOR;
        }
        // 注释
        entityContent += "/**" + SEPARATOR;
        entityContent += "* " + table.getTableComment() + SEPARATOR;
        entityContent += "*/" + SEPARATOR;
        // 注解
        entityContent += "@Data" + SEPARATOR;
        entityContent += "@Entity" + SEPARATOR;
        entityContent += "@Table(name = \"" + tableName + "\")" + SEPARATOR + SEPARATOR;
        // class
        entityContent += "public class " + this.getUpCaseTableName(tableName) + " {" + SEPARATOR;
        // field
        entityContent += this.getEntityFiledContent(columnList);
        entityContent += "}";
        return entityContent;
    }

    private String getUpCaseTableName(String tableName) {
        List<String> stringList = Arrays.asList(tableName.split("_"));
        stringList = stringList.stream().map(this::first2UpCase).collect(Collectors.toList());
        StringBuilder resultTableName = new StringBuilder();
        for (String str : stringList) {
            resultTableName.append(str);
        }
        return resultTableName.toString();
    }


    private String getEntityFiledContent(List<Column> columnList) {
        StringBuilder fieldContent = new StringBuilder();
        for (Column column : columnList) {
            if (column.getExtra().equalsIgnoreCase("auto_increment")) {
                fieldContent.append("    ");
                fieldContent.append("@Id").append(SEPARATOR);
                fieldContent.append("    ");
                fieldContent.append("@GeneratedValue").append(SEPARATOR);
            }
            fieldContent.append("    ").append("private").append(" ").append(column.getColumnType()).append(" ").append(this.getJavaColumnName(column.getColumnName()));
            String defaultValue = column.getDefaultValue();
            if (defaultValue != null && !defaultValue.equals("")) {
                fieldContent.append(" = ").append(defaultValue);
            }
            fieldContent.append(";").append(SEPARATOR);
        }
        return fieldContent.toString();
    }

    /**
     * user_id => userId
     *
     * @param columnName 原始值
     * @return 字段值
     */
    private String getJavaColumnName(String columnName) {
        List<String> stringList = Arrays.asList(columnName.split("_"));
        StringBuilder resultColumnName = new StringBuilder();
        for (int i = 0; i < stringList.size(); i++) {
            if (i == 0) {
                resultColumnName.append(stringList.get(i));
            } else {
                resultColumnName.append(this.first2UpCase(stringList.get(i)));
            }
        }
        return resultColumnName.toString();
    }

    private String getControllerContent(Table table, String controllerPath, String entityPath, String servicePath) {
        String upCaseTableName = this.getUpCaseTableName(table.getTableName());
        String serviceName = upCaseTableName + "Service";
        String controllerName = upCaseTableName + "Controller";
        String javaPrimaryName = "";
        String primaryType = "";
        String controllerContent = "";
        for (Column column : table.getColumnList()) {
            if (column.getPrimary()) {
                javaPrimaryName = this.getJavaColumnName(column.getColumnName());
                primaryType = column.getColumnType();
            }
        }
        // package
        controllerContent += "package " + controllerPath + ";" + SEPARATOR + SEPARATOR;
        // import
        controllerContent += "import org.springframework.beans.factory.annotation.Autowired;" + SEPARATOR;
        controllerContent += "import org.springframework.web.bind.annotation.*;" + SEPARATOR;
        controllerContent += "import " + entityPath + "." + upCaseTableName + ";" + SEPARATOR;
        controllerContent += "import " + servicePath + "." + serviceName + ";" + SEPARATOR + SEPARATOR;
        controllerContent += "import java.util.List;" + SEPARATOR + SEPARATOR;
        // 注解
        controllerContent += "@RestController" + SEPARATOR;
        controllerContent += "@RequestMapping(value = \"/" + upCaseTableName + "\")" + SEPARATOR;
        // controller
        controllerContent += "public class " + controllerName + " {" + SEPARATOR;
        controllerContent += this.getControllerMethodContent(upCaseTableName, javaPrimaryName, primaryType, controllerName, serviceName);
        controllerContent += "}";
        return controllerContent;
    }

    private String getControllerMethodContent(String tableName, String javaPrimaryName, String primaryType, String controllerName, String serviceName) {
        String lowerCaseServiceName = this.first2LowerCase(serviceName);
        String lowerCaseTableName = this.first2LowerCase(tableName);
        String closeMethodContent = "    }" + SEPARATOR + SEPARATOR;
        String methodContent = "";
        // Construct
        methodContent += "    " + "private final " + serviceName + " " + lowerCaseServiceName + ";" + SEPARATOR + SEPARATOR;
        methodContent += "    " + "@Autowired" + SEPARATOR;
        methodContent += "    " + "public " + controllerName + "(" + serviceName + " " + lowerCaseServiceName + ") {" + SEPARATOR;
        methodContent += "        " + "this." + lowerCaseServiceName + " = " + lowerCaseServiceName + ";" + SEPARATOR;
        methodContent += closeMethodContent;
        // Get findAll
        methodContent += "    @GetMapping(value = \"/\")" + SEPARATOR;
        methodContent += "    public List<" + tableName + ">" + " findAll() {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseServiceName + ".findAll();" + SEPARATOR;
        methodContent += closeMethodContent;
        // Get findById
        methodContent += "    @GetMapping(value = \"/{" + javaPrimaryName + "}\")" + SEPARATOR;
        methodContent += "    public " + tableName + " findById(@PathVariable(name = \"" + javaPrimaryName + "\") " + primaryType + " " + javaPrimaryName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseServiceName + ".findById(" + javaPrimaryName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        // Post addOne
        methodContent += "    @PostMapping(value = \"/\")" + SEPARATOR;
        methodContent += "    public " + tableName + " addOne(@RequestBody " + tableName + " " + lowerCaseTableName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseServiceName + ".addOne(" + lowerCaseTableName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        // Put updateOne
        methodContent += "    @PutMapping(value = \"/\")" + SEPARATOR;
        methodContent += "    public " + tableName + "updateOne(@RequestBody " + tableName + " " + lowerCaseTableName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseServiceName + ".updateOne(" + lowerCaseTableName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        // Delete deleteOne
        methodContent += "    @DeleteMapping(value = \"/{" + javaPrimaryName + "}\")" + SEPARATOR;
        methodContent += "    public " + tableName + " deleteById(@PathVariable(name = \"" + javaPrimaryName + "\") " + primaryType + " " + javaPrimaryName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseServiceName + ".deleteById(" + javaPrimaryName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        return methodContent;
    }

    private String getServiceContent(Table table, String servicePath, String entityPath) {
        String tableName = this.getUpCaseTableName(table.getTableName());
        String primaryName = "";
        String primaryType = "";
        String serviceContent = "";
        for (Column column : table.getColumnList()) {
            if (column.getPrimary()) {
                primaryName = column.getColumnName();
                primaryType = column.getColumnType();
            }
        }
        // package
        serviceContent += "package " + servicePath + ";" + SEPARATOR + SEPARATOR;
        // import
        serviceContent += "import " + entityPath + "." + tableName + ";" + SEPARATOR + SEPARATOR;
        serviceContent += "import java.util.List;" + SEPARATOR + SEPARATOR;
        // service
        serviceContent += "public interface " + tableName + "Service {" + SEPARATOR;
        serviceContent += this.getServiceMethodContent(tableName, primaryName, primaryType);
        serviceContent += "}";

        return serviceContent;
    }

    private String getServiceMethodContent(String tableName, String primaryName, String primaryType) {
        String lowerCaseTableName = this.first2LowerCase(tableName);
        String methodContent = "";
        methodContent += "    List<" + tableName + "> findAll();" + SEPARATOR + SEPARATOR;
        methodContent += "    " + tableName + " findById(" + primaryType + " " + primaryName + ");" + SEPARATOR + SEPARATOR;
        methodContent += "    " + tableName + " addOne(" + tableName + " " + lowerCaseTableName + ");" + SEPARATOR + SEPARATOR;
        methodContent += "    " + tableName + " updateOne(" + tableName + " " + lowerCaseTableName + ");" + SEPARATOR + SEPARATOR;
        methodContent += "    void deleteById(" + primaryType + " " + primaryName + ");" + SEPARATOR;
        return methodContent;
    }

    private String getServiceImplContent(Table table, String serviceImplPath, String daoPath, String entityPath, String servicePath) {
        String tableName = this.getUpCaseTableName(table.getTableName());
        String primaryName = "";
        String primaryType = "";
        String serviceImplContent = "";
        for (Column column : table.getColumnList()) {
            if (column.getPrimary()) {
                primaryName = column.getColumnName();
                primaryType = column.getColumnType();
            }
        }
        // package
        serviceImplContent += "package " + serviceImplPath + ";" + SEPARATOR + SEPARATOR;
        // import
        serviceImplContent += "import org.springframework.beans.factory.annotation.Autowired;" + SEPARATOR;
        serviceImplContent += "import org.springframework.stereotype.Service;" + SEPARATOR;
        serviceImplContent += "import " + daoPath + "." + tableName + "Dao;" + SEPARATOR;
        serviceImplContent += "import " + entityPath + "." + tableName + ";" + SEPARATOR;
        serviceImplContent += "import " + servicePath + "." + tableName + "Service;" + SEPARATOR + SEPARATOR;
        serviceImplContent += " import java.util.List;" + SEPARATOR + SEPARATOR;
        // 注解
        serviceImplContent += "@Service" + SEPARATOR;
        // serviceImpl
        serviceImplContent += "public class " + tableName + "ServiceImpl implements " + tableName + "Service {" + SEPARATOR;
        serviceImplContent += "    private final " + tableName + "Dao " + this.first2LowerCase(tableName) + "Dao;" + SEPARATOR + SEPARATOR;
        serviceImplContent += "    @Autowired" + SEPARATOR;
        serviceImplContent += "    public " + tableName + "ServiceImpl(" + tableName + "Dao" + " " + this.first2LowerCase(tableName) + "Dao) {" + SEPARATOR;
        serviceImplContent += "        this." + this.first2LowerCase(tableName) + "Dao" + " = " + this.first2LowerCase(tableName) + "Dao;" + SEPARATOR;
        serviceImplContent += "    }" + SEPARATOR + SEPARATOR;
        serviceImplContent += this.getServiceImplMethodContent(tableName, primaryName, primaryType);
        serviceImplContent += "}";
        return serviceImplContent;
    }

    private String getServiceImplMethodContent(String tableName, String primaryName, String primaryType) {
        String daoName = tableName + "Dao";
        String lowerCaseDaoName = this.first2LowerCase(daoName);
        String lowerCaseTableName = this.first2LowerCase(tableName);
        String overrideContent = "    @Override" + SEPARATOR;
        String closeMethodContent = "    }" + SEPARATOR + SEPARATOR;
        String methodContent = "";
        // findAll
        methodContent += overrideContent;
        methodContent += "    public List<" + tableName + "> findAll() {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseDaoName + ".findAll();" + SEPARATOR;
        methodContent += closeMethodContent;
        // findById
        methodContent += overrideContent;
        methodContent += "    public List<" + tableName + "> findById(" + primaryType + " " + primaryName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseDaoName + ".findById(" + primaryName + ")" + ".orElse(null);" + SEPARATOR;
        methodContent += closeMethodContent;
        // addOne
        methodContent += overrideContent;
        methodContent += "    public " + tableName + " addOne(" + tableName + " " + lowerCaseTableName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseDaoName + ".save(" + lowerCaseTableName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        // updateOne
        methodContent += overrideContent;
        methodContent += "    public " + tableName + " updateOne(" + tableName + " " + lowerCaseTableName + ") {" + SEPARATOR;
        methodContent += "        return this." + lowerCaseDaoName + ".save(" + lowerCaseTableName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        // deleteById
        methodContent += overrideContent;
        methodContent += "    public void deleteById(" + primaryType + " " + primaryName + ") {" + SEPARATOR;
        methodContent += "        this." + lowerCaseDaoName + ".deleteById(" + primaryName + ");" + SEPARATOR;
        methodContent += closeMethodContent;
        return methodContent;
    }

    private String getDaoContent(Table table, String daoPath, String entityPath) {
        String tableName = this.getUpCaseTableName(table.getTableName());
        String primaryType = "";
        String daoContent = "";
        for (Column column : table.getColumnList()) {
            if (column.getPrimary()) {
                primaryType = column.getColumnType();
            }
        }
        // package
        daoContent += "package " + daoPath + ";" + SEPARATOR + SEPARATOR;
        // import
        daoContent += "import org.springframework.data.jpa.repository.JpaRepository;" + SEPARATOR;
        daoContent += "import " + entityPath + "." + tableName + ";" + SEPARATOR + SEPARATOR;
        // controller
        daoContent += "public interface " + tableName + "Dao" + " extends " + "JpaRepository<" + tableName + ", " + primaryType + "> {" + SEPARATOR;
        daoContent += "}";

        return daoContent;
    }

    private void doAction(String rootDirPath, String path, String content, String fileName) {
        StringBuilder fileDirectoryPath = new StringBuilder(rootDirPath + File.separator + "src" + File.separator + "main" + File.separator + "java");
        String[] stringList = path.split("\\.");
        for (String str : stringList) {
            fileDirectoryPath.append(File.separator).append(str);
        }
        File directory = new File(fileDirectoryPath.toString());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filePath = fileDirectoryPath.toString() + File.separator + fileName + ".java";
        File file = new File(filePath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String first2UpCase(String str) {
        List<String> stringList = Arrays.asList(str.split(""));
        StringBuilder resultStr = new StringBuilder();
        for (int i = 0; i < stringList.size(); i++) {
            if (i == 0) {
                resultStr.append(stringList.get(i).toUpperCase());
            } else {
                resultStr.append(stringList.get(i));
            }
        }
        return resultStr.toString();
    }

    private String first2LowerCase(String str) {
        List<String> stringList = Arrays.asList(str.split(""));
        StringBuilder resultStr = new StringBuilder();
        for (int i = 0; i < stringList.size(); i++) {
            if (i == 0) {
                resultStr.append(stringList.get(i).toLowerCase());
            } else {
                resultStr.append(stringList.get(i));
            }
        }
        return resultStr.toString();
    }
}
