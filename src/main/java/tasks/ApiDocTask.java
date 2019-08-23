package tasks;

import com.sun.javadoc.*;
import common.Const;
import common.JavaDocReader;
import common.Utils;
import extensions.ApiDocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.web.bind.annotation.*;
import structure.ApiDoc;
import structure.ApiMethodDoc;
import structure.BeanDoc;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by wb on 2018/11/28.
 */
@Deprecated
public class ApiDocTask extends DefaultTask {

    private final Project project = getProject();
    private final ApiDocExtension apiDocExtension = (ApiDocExtension) project.getExtensions().getByName(Const.EXTENSION_APIDOC_NAME);
    private final File rootDir = project.getRootDir();
//
//    public static void main(String[] args) {
//        generateBeanDoc(new File("D:\\workspace-java\\my\\gradle-plugins\\src\\main\\java\\entity\\User.java"));
//        generateApiDoc(new File("D:\\workspace-java\\my\\gradle-plugins\\src\\main\\java\\controller\\UserController.java"));
//    }

    /**
     * 获得bean的文档
     *
     * @param fileList    bean文件
     * @param beanDocList 所有bean文档
     */
    private static void generateBeanDoc(List<File> fileList, List<BeanDoc> beanDocList) {
        fileList.forEach(file -> {
            String fullPath = file.getAbsolutePath();
            String[] exeParams = JavaDocReader.getExecuteParams(true, fullPath);
            JavaDocReader.readDoc(new JavaDocReader.CallBack() {
                @Override
                public String callback(RootDoc rootDoc, ClassDoc[] classDocs) throws ClassNotFoundException {
                    if (classDocs != null) {
                        // 如果classDocs.length>1，则包含内部类
                        for (ClassDoc classDoc : classDocs) {
                            BeanDoc rootBeanDoc = new BeanDoc();
                            rootBeanDoc.setType(classDoc.toString());
                            rootBeanDoc.setName(getLast(classDoc.toString().split("\\.")));
                            rootBeanDoc.setComment(classDoc.commentText());
                            FieldDoc[] fieldDocs = classDoc.fields();
                            for (FieldDoc field : fieldDocs) {
                                structure.FieldDoc fieldDoc = new structure.FieldDoc();
                                fieldDoc.setType(field.type().typeName());
                                fieldDoc.setName(field.name());
                                fieldDoc.setComment(field.commentText());
                                rootBeanDoc.getFields().add(fieldDoc);
//                                BeanDoc beanDoc = new BeanDoc();
//                                beanDoc.setName(field.name());
//                                beanDoc.setComment(field.commentText());
//                                beanDoc.setType(field.type().typeName());
//                                rootBeanDoc.getInnerBeanDoc().add(beanDoc);
                            }
                            beanDocList.add(rootBeanDoc);
                        }
                    }
                    return null;
                }

                @Override
                public void error(Exception e) {

                }
            }, exeParams);
        });
    }

    /**
     * 判断是否是文档注解是否满足条件
     *
     * @param annotations    文档注解
     * @param annotationName 条件
     * @return 是否满足
     */
    private static boolean isApiDoc(AnnotationDesc[] annotations, String annotationName) {
        return Arrays.stream(annotations).map(annotationDesc -> annotationDesc.annotationType().name()).anyMatch(annotationName::equalsIgnoreCase);
    }

    /**
     * 判断注解是否满足
     *
     * @param annotations
     * @param annotationName
     * @return
     */
    private static boolean isApi(Annotation[] annotations, String annotationName) {
        return Arrays.stream(annotations).map(annotation -> annotation.annotationType().getSimpleName()).anyMatch(annotationName::equalsIgnoreCase);
    }

    private static void generateApiDoc(String pathPrefix, File file, List<ApiDoc> apiDocList, List<BeanDoc> beanDocList) {
        String className = file.getPath().substring(pathPrefix.length() + 1, file.getPath().lastIndexOf(".")).replaceAll("\\\\", ".");
        String fullPath = file.getAbsolutePath();
        String[] exeParams = JavaDocReader.getExecuteParams(true, fullPath);
        JavaDocReader.readDoc(new JavaDocReader.CallBack() {

            private final String paramTag = "@param";
            private final String returnTag = "@return";
            private final String dateTag = "@date";
            private final String authorTag = "@author";
            private final String GET_MAPPING = "GetMapping";
            private final String POST_MAPPING = "PostMapping";
            private final String PUT_MAPPING = "PutMapping";
            private final String DELETE_MAPPING = "DeleteMapping";
            private final String CONTROLLER = "Controller";
            private final String REST_CONTROLLER = "RestController";
            private List<String> requestMethods = new ArrayList<>(5);
            private Map<String, String> methods = new HashMap<>(4);

            {
                requestMethods.add("GetMapping");
                requestMethods.add("PostMapping");
                requestMethods.add("PutMapping");
                requestMethods.add("DeleteMapping");
                requestMethods.add("RequestMapping");
            }

            {
                methods.put("GetMapping", "GET");
                methods.put("PostMapping", "POST");
                methods.put("PutMapping", "PUT");
                methods.put("DeleteMapping", "DELETE");
            }

            @Override
            public String callback(RootDoc rootDoc, ClassDoc[] classDocs) throws Exception {
                if (classDocs != null) {
                    ApiDoc apiDoc = new ApiDoc();
                    List<ClassDoc> apiClassDoc = Arrays.stream(classDocs)
                            .filter(each -> isApiDoc(each.annotations(), this.CONTROLLER) || isApiDoc(each.annotations(), this.REST_CONTROLLER))
                            .collect(Collectors.toList());
                    for (ClassDoc classDoc : apiClassDoc) {
                        apiDoc.setName(classDoc.name());
                        apiDoc.setComment(classDoc.commentText());
                        Tag[] tags = classDoc.tags();
                        Map<String, List<Tag>> classTagGroup = Arrays.stream(tags).collect(Collectors.groupingBy(Tag::name));
                        List<Tag> classAuthorTag = classTagGroup.get(this.authorTag);
                        List<Tag> classDateTag = classTagGroup.get(this.dateTag);
                        if (classAuthorTag != null) {
                            apiDoc.setAuthor(classAuthorTag.stream().map(Tag::text).collect(Collectors.joining("/")));
                        }
                        if (classDateTag != null) {
                            apiDoc.setDate(classDateTag.stream().map(Tag::text).collect(Collectors.joining("/")));
                        }
                        // 反射获取类的注解
                        Class clz = Class.forName(className);
                        Annotation deprecated = clz.getAnnotation(Deprecated.class);
                        if (deprecated != null) {
                            apiDoc.setDeprecated(true);
                        }
                        RequestMapping requestMapping = (RequestMapping) clz.getAnnotation(RequestMapping.class);
                        apiDoc.setMapping(String.join("/", requestMapping.value()));

                        // 处理方法
                        MethodDoc[] methodDocs = classDoc.methods();
                        List<MethodDoc> apiMethodDocList = Arrays.stream(methodDocs)
                                .filter(each -> isApiMethodDoc(each.annotations(), this.requestMethods))
                                .collect(Collectors.toList());
                        Method[] methods = clz.getDeclaredMethods();
                        List<Method> apiMethods = Arrays.stream(methods)
                                .filter(each -> isApiMethod(each.getAnnotations(), this.requestMethods))
                                .collect(Collectors.toList());
                        for (MethodDoc methodDoc : apiMethodDocList) {
                            ApiMethodDoc apiMethodDoc = new ApiMethodDoc();
                            apiMethodDoc.setAuthor(apiDoc.getAuthor());
                            apiMethodDoc.setDate(apiDoc.getDate());
                            apiMethodDoc.setName(methodDoc.name());
                            apiMethodDoc.setComment(methodDoc.commentText());
                            Tag[] methodTags = methodDoc.tags();
                            Map<String, List<Tag>> methodTagGroup = Arrays.stream(methodTags).collect(Collectors.groupingBy(Tag::name));
                            List<Tag> authorTags = methodTagGroup.get(this.authorTag);
                            if (authorTags != null) {
                                apiMethodDoc.setAuthor(authorTags.stream().map(Tag::text).collect(Collectors.joining("/")));
                            }
                            List<Tag> dateTags = methodTagGroup.get(this.dateTag);
                            if (dateTags != null) {
                                apiMethodDoc.setDate(dateTags.stream().map(Tag::text).collect(Collectors.joining("/")));
                            }
                            List<Tag> returnTags = methodTagGroup.get(this.returnTag);
                            if (returnTags != null) {
                                apiMethodDoc.setReturnDescription(returnTags.stream().map(Tag::text).collect(Collectors.joining("/")));
                            }
                            Method method = getMethod(methodDoc, apiMethods);
                            if (method.getReturnType() != null) {
                                apiMethodDoc.setReturnType(method.getReturnType().getSimpleName());
                            }
                            // 处理请求mapping和请求方式
                            List<String> mapping = handlerMapping(method);
                            apiMethodDoc.setMapping(mapping.get(0));
                            apiMethodDoc.setAccessType(mapping.get(1));
                            // 处理请求参数
                            List<Tag> docMethodList = methodTagGroup.get(this.returnTag);
                            java.lang.reflect.Type[] types = method.getGenericParameterTypes();
                            java.lang.reflect.Parameter[] parameters = method.getParameters();
                            getMethodParams(beanDocList, types, parameters);
//                            apiMethodDoc.setMethodParams(getMethodParams(beanDocList, types, parameters));
                            apiDoc.getMethodDocList().add(apiMethodDoc);
                        }
                        System.out.println();
                    }
                    apiDocList.add(apiDoc);
                }
                return null;
            }

            @Override
            public void error(Exception e) {
                e.printStackTrace();
            }
        }, exeParams);
    }

    private static Object[] getMethodParams(List<BeanDoc> beanDocList, java.lang.reflect.Type[] types, java.lang.reflect.Parameter[] parameters) {
        List<BeanDoc> beanDocs = new ArrayList<>();
        List<structure.FieldDoc> fieldDocs = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            java.lang.reflect.Type type = types[i];
            BeanDoc beanDoc = new BeanDoc();
            beanDoc.setSimpleName(parameters[i].getName());
            setChild(type, beanDocList, beanDoc, fieldDocs, parameters[i]);
            beanDocs.add(beanDoc);

        }
        return new Object[]{beanDocs, fieldDocs};
    }

    private static void setChild(java.lang.reflect.Type type, List<BeanDoc> beanDocList, BeanDoc beanDoc, List<structure.FieldDoc> fieldDocs, java.lang.reflect.Parameter parameter) {
        // 包含泛型参数
        if (type instanceof ParameterizedTypeImpl) {
            beanDoc.setType(((ParameterizedTypeImpl) type).getRawType().getTypeName());
            beanDoc.setName(((ParameterizedTypeImpl) type).getRawType().getSimpleName());
            for (java.lang.reflect.Type each : ((ParameterizedTypeImpl) type).getActualTypeArguments()) {
                BeanDoc beanDoc1 = new BeanDoc();
                beanDoc.getInnerBeanDoc().add(beanDoc1);
                setChild(each, beanDocList, beanDoc1, fieldDocs, parameter);
            }
        } else {
            // 不包含泛型参数
            // 获得自定义类型
            BeanDoc temp = getBeanDoc(beanDocList, type.getTypeName());
            if (temp != null) {
                BeanCopier.create(BeanDoc.class, BeanDoc.class, false).copy(temp, beanDoc, null);
            } else {
                structure.FieldDoc fieldDoc = new structure.FieldDoc();
                fieldDoc.setName(parameter.getName());
                fieldDoc.setType(type.getTypeName());
            }
        }
    }

    private static BeanDoc getBeanDoc(List<BeanDoc> beanDocList, String type) {
        return beanDocList.stream().filter(each -> type.equalsIgnoreCase(each.getType())).findFirst().orElse(null);
    }

    /**
     * 找到方法文档
     *
     * @param methodDocs    所有方法文档
     * @param methodDocName 方法文档名
     * @return 方法文档
     */
    private static MethodDoc getMethodDoc(MethodDoc[] methodDocs, String methodDocName) {
        for (MethodDoc methodDoc : methodDocs) {
            if (methodDocName.equalsIgnoreCase(methodDoc.name())) {
                return methodDoc;
            }
        }
        // 不会执行到
        return null;
    }

    private static String getMethodComment(MethodDoc[] methodDocs, String methodName) {
        MethodDoc methodDoc = getMethodDoc(methodDocs, methodName);
        if (methodDoc != null) {
            return methodDoc.commentText();
        }
        return "";
    }

    /**
     * 判断是否是接口方法文档
     *
     * @param annotations    接口方法所有注解描述
     * @param requestMethods 请求的几种方式
     * @return 是否是接口方法文档
     */
    private static Boolean isApiMethodDoc(AnnotationDesc[] annotations, List<String> requestMethods) {
        return requestMethods.stream().anyMatch(requestMethod -> isApiDoc(annotations, requestMethod));
    }

    /**
     * 判断是否是接口方法
     *
     * @param annotations    接口注解
     * @param requestMethods 一般接口方法可能包含的注解
     * @return 是否是接口方法
     */
    private static Boolean isApiMethod(Annotation[] annotations, List<String> requestMethods) {
        return requestMethods.stream().anyMatch(requestMethod -> isApi(annotations, requestMethod));
    }

    /**
     * 获得mapping和请求方式
     *
     * @param method 接口方法
     * @return 第一个为mapping，第二个为请求方式
     */
    private static List<String> handlerMapping(Method method) {
        Annotation[] annotations = method.getAnnotations();
        List<String> mapping = new ArrayList<>(2);
        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getSimpleName();
            switch (annotationName) {
                case "GetMapping":
                    mapping.add(String.join("/", ((GetMapping) annotation).value()));
                    mapping.add("Get");
                    break;
                case "PostMapping":
                    mapping.add(String.join("/", ((PostMapping) annotation).value()));
                    mapping.add("Post");
                    break;
                case "PutMapping":
                    mapping.add(String.join("/", ((PutMapping) annotation).value()));
                    mapping.add("Put");
                    break;
                case "DeleteMapping":
                    mapping.add(String.join("/", ((DeleteMapping) annotation).value()));
                    mapping.add("Delete");
                    break;
                case "RequestMapping":
                    mapping.add(String.join("/", ((RequestMapping) annotation).value()));
                    mapping.add(Arrays.stream(((RequestMapping) annotation).method()).map(Enum::name).collect(Collectors.joining("/")));
                    break;
                default:
                    break;
            }
        }
        return mapping;
    }

    /**
     * 找到对应的方法文档和接口方法
     *
     * @param methodDoc 方法文档
     * @param methods   所有的接口方法
     * @return 匹配的接口方法
     */
    private static Method getMethod(MethodDoc methodDoc, List<Method> methods) {
        String methodDocSignature = docMethod2String(methodDoc);
        for (Method method : methods) {
            if (getLast(apiMethod2String(method).split(" ")).equalsIgnoreCase(methodDocSignature)) {
                return method;
            }
        }
        // 不会执行到
        return null;
    }

    /**
     * 文档方法的2String
     *
     * @param method 文档方法
     * @return 2String
     */
    private static String docMethod2String(MethodDoc method) {
        String methodName = method.name();
        Parameter[] parameters = method.parameters();
        List<String> parameterName = new ArrayList<>();
        for (Parameter parameter : parameters) {
            parameterName.add(parameter.type().typeName());
        }
        return methodName + "(" + String.join(",", parameterName) + ")";
    }

    /**
     * 接口方法的2String
     *
     * @param method 接口方法
     * @return 2String
     */
    private static String apiMethod2String(Method method) {
        String methodName = method.getName();
        Class[] clzs = method.getParameterTypes();
        List<String> parameterName = new ArrayList<>();
        for (Class clz : clzs) {
            parameterName.add(clz.getSimpleName());
        }
        return methodName + "(" + String.join(",", parameterName) + ")";
    }

    private static String getLast(String[] strings) {
        return strings[strings.length - 1];
    }

    private static List<File> getAllFiles(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles(pathname -> pathname.getName().endsWith(".java"));
                if ((fileList != null ? fileList.length : 0) > 0) {
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

    @TaskAction
    void apiDoc() {
        final List<ApiDoc> apiDocList = new ArrayList<>();
        final List<BeanDoc> beanDocList = new ArrayList<>();
//        String beanPath = apiDocExtension.getBeanPath();
//        String controllerPath = apiDocExtension.getControllerPath();
//        System.out.println("开始构建entity信息");
//        doAction(beanPath, "entity", apiDocList, entityDocDist);
//        System.out.println("构建entity信息结束");
//        System.out.println("开始构建controller信息");
//        doAction(controllerPath, "controller", apiDocList, entityDocDist);
//        System.out.println("构建controller信息结束");
//        System.out.println(new Gson().toJson(apiDocList));

        List<String> beanPathList = apiDocExtension.getBeanPath();
        List<String> apiPathList = apiDocExtension.getControllerPath();
        String rootPath = getRootPath();

        List<File> beanFileList = new ArrayList<>();
        List<File> apiFileList = new ArrayList<>();
        if (beanPathList.size() == 0) {
            System.out.println("配置beanPath");
            System.exit(-1);
        }

        if (apiPathList.size() == 0) {
            System.out.println("配置controllerPath");
            System.exit(-1);
        }

        beanPathList.forEach(each -> {
            beanFileList.addAll(Utils.getAllFile(rootPath + File.separator + each));
        });
        apiPathList.forEach(each -> {
            apiFileList.addAll(Utils.getAllFile(rootPath + File.separator + each));
        });


        generateBeanDoc(beanFileList, beanDocList);

        beanDocList.forEach(each -> setChild(each, beanDocList));

        beanDocList.forEach(each -> each.setInnerBeanDoc(each.getInnerBeanDoc().stream().distinct().collect(Collectors.toList())));

        apiFileList.forEach(each -> generateApiDoc(rootPath, each, apiDocList, beanDocList));
        System.out.println();
    }


    private void setChild(BeanDoc parent, List<BeanDoc> list) {
        for (BeanDoc each : list) {
            if (!each.getType().equals(parent.getType())
                    && each.getType().startsWith(parent.getType() + ".")
                    && !each.getType().substring(parent.getType().length() + 1).contains(".")) {
                parent.getInnerBeanDoc().add(each);
                setChild(each, list);
            }
        }
    }

    private String getRootPath() {
        return rootDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "java";
    }

    private void createFile(List<ApiDoc> apiDocList) {
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
