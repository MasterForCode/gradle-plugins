package structure;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wb on 2018/11/28.
 */
@Data
@Deprecated
public class BeanDoc {
    /**
     * 全限定类名
     */
    private String type;
    /**
     * 类名
     */
    private String name;
    private String simpleName;
    /**
     * 类注释
     */
    private String comment;
    /**
     * 字段
     */
    private List<FieldDoc> fields = new ArrayList<>();
    /**
     * 内部类
     */
    private List<BeanDoc> innerBeanDoc = new ArrayList<>();
}
