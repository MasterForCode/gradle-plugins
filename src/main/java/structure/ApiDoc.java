package structure;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
@Data
@Deprecated
public class ApiDoc extends DocCommonMessage {
    private List<ApiMethodDoc> methodDocList = new ArrayList<>();
}
