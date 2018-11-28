package structure;

import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
public class MethodDoc {
    private String methodName;
    private String methodRequestMethod;
    private String methodRequestMapping;
    private String methodRequestReturn;
    private String methodAnnotation;
    private List<RequestParams> methodRequestParams;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodRequestMethod() {
        return methodRequestMethod;
    }

    public void setMethodRequestMethod(String methodRequestMethod) {
        this.methodRequestMethod = methodRequestMethod;
    }

    public String getMethodRequestMapping() {
        return methodRequestMapping;
    }

    public void setMethodRequestMapping(String methodRequestMapping) {
        this.methodRequestMapping = methodRequestMapping;
    }

    public String getMethodRequestReturn() {
        return methodRequestReturn;
    }

    public void setMethodRequestReturn(String methodRequestReturn) {
        this.methodRequestReturn = methodRequestReturn;
    }

    public String getMethodAnnotation() {
        return methodAnnotation;
    }

    public void setMethodAnnotation(String methodAnnotation) {
        this.methodAnnotation = methodAnnotation;
    }

    public List<RequestParams> getMethodRequestParams() {
        return methodRequestParams;
    }

    public void setMethodRequestParams(List<RequestParams> methodRequestParams) {
        this.methodRequestParams = methodRequestParams;
    }
}