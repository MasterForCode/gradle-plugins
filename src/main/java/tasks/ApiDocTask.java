package tasks;

import common.Const;
import extensions.ApiDocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import structure.ControllerDoc;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wb on 2018/11/28.
 */
public class ApiDocTask extends DefaultTask {
    private final Project project = getProject();

    private final ApiDocExtension apiDocExtension = (ApiDocExtension) project.getExtensions().getByName(Const.EXTENSION_APIDOC_NAME);

    private final File rootDir = project.getRootDir();


    @TaskAction
    void apiDoc() {
        String controllerPath = apiDocExtension.getControllerPath();
        String filePath = this.getPath(controllerPath);
        List<File> fileList = getAllFiles(filePath);
        if (fileList != null) {
            List<ControllerDoc> controllerDocList = new ArrayList<>();
            for (File file :fileList) {
                generateControllerDoc(file, controllerDocList);
            }
            createFile(controllerDocList);
        } else {
            System.out.println("No file in the path");
        }
    }

    private void generateControllerDoc(File file, List<ControllerDoc> controllerDocList) {
        ControllerDoc controllerDoc = new ControllerDoc();
        String fileName = this.getFileName(file);
        controllerDoc.setControllerName(fileName);
        String stringFile = this.file2String(file);
        if (stringFile != null) {
            Pattern pattern;
            Matcher matcher;
            pattern = Pattern.compile(ControllerDoc.controllerRequestMethodPattern);
            matcher = pattern.matcher(stringFile);
            if (matcher.find()) {
                System.out.println("." + matcher.group());
            }
            pattern = Pattern.compile(ControllerDoc.controllerRequestMappingPattern);
            matcher = pattern.matcher(stringFile);
            if (matcher.find()){ // 取第一个匹配的
                System.out.println("--" + matcher.group());
            }
            pattern = Pattern.compile(ControllerDoc.controllerAnnotationPattern);
            matcher = pattern.matcher(stringFile);
            if (matcher.find()){ // 取第一个匹配的
                System.out.println("-" + matcher.group());
            }

        } else {
            System.out.println(fileName + "is empty");
        }
    }

    private void createFile(List<ControllerDoc> controllerDocList) {}

    private List<File> getAllFiles(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles(pathname -> pathname.getName().endsWith("Controller.java"));
                if (fileList != null) {
                    return Arrays.asList(fileList);
                } else {
                    System.out.println("Directory is empty");
                }
            } else {
                System.out.println("Only directory");
            }
        } else {
            System.out.println("Can not find file");
        }
        return null;
    }

    private String getPath(String controllerPath) {
        String[] stringList = controllerPath.split("\\.");
        StringBuilder path = new StringBuilder(rootDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "java");
        for (String aStringList : stringList) {
            path.append(File.separator).append(aStringList);
        }
        return path.toString();
    }

    private String getFileName(File file) {
        String fullFileName = file.getName();
        List<String> stringList = Arrays.asList(fullFileName.split("\\."));
        StringBuilder fileName = new StringBuilder();
        for (int i = 0; i < stringList.size() - 1; i++) {
            fileName.append(stringList.get(i));
        }
        return fileName.toString();
    }

    private String file2String(File file) {
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int length = bis.available();
            byte[] buffer = new byte[length];
            bis.read(buffer);
            bis.close();
            return new String(buffer, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}