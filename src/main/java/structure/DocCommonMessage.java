package structure;

import lombok.Data;

/**
 * @author wb
 * @date 2019/8/19
 */
@Data
@Deprecated
public class DocCommonMessage {
    private String author;
    private String date;
    private String name;
    private String mapping;
    private String comment;
    private Boolean deprecated = false;
}
