package structure;

import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
public class RequestParams {
    private String paramType;
    private String paramName;
    private List<RequestParams> innerParam;

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public List<RequestParams> getInnerParam() {
        return innerParam;
    }

    public void setInnerParam(List<RequestParams> innerParam) {
        this.innerParam = innerParam;
    }
}