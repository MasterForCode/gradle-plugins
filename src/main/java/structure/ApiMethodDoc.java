package structure;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
@Data
@Deprecated
public class ApiMethodDoc extends DocCommonMessage {
    private String returnDescription;
    private String returnType;
    private String accessType;
    private List<BeanDoc> methodParams = new ArrayList<>();
}
