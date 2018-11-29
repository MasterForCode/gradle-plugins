package structure;

import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
public class ControllerDoc {
    private String controllerName;
    private String controllerRequestMethod;
    private String controllerRequestMapping;
    private String controllerAnnotation;
    private List<MethodDoc> methodDocList;
    public static final String controllerRequestMethodPattern = "@[a-zA-Z]*Controller";
    public static final String controllerRequestMappingPattern = "@RequestMapping(.*)";
    public static final String controllerAnnotationPattern = "/[\\s\\S]*/[\\s]+";

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getControllerRequestMethod() {
        return controllerRequestMethod;
    }

    public void setControllerRequestMethod(String controllerRequestMethod) {
        this.controllerRequestMethod = controllerRequestMethod;
    }

    public String getControllerRequestMapping() {
        return controllerRequestMapping;
    }

    public void setControllerRequestMapping(String controllerRequestMapping) {
        this.controllerRequestMapping = controllerRequestMapping;
    }

    public String getControllerAnnotation() {
        return controllerAnnotation;
    }

    public void setControllerAnnotation(String controllerAnnotation) {
        this.controllerAnnotation = controllerAnnotation;
    }

    public List<MethodDoc> getMethodDocList() {
        return methodDocList;
    }

    public void setMethodDocList(List<MethodDoc> methodDocList) {
        this.methodDocList = methodDocList;
    }
}