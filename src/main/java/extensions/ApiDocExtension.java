package extensions;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
@Data
@Deprecated
public class ApiDocExtension {
    private List<String> controllerPath = new ArrayList<>();

    private List<String> beanPath = new ArrayList<>();

    private List<String> controllerNameList = new ArrayList<>();
}
