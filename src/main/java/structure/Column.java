package structure;

import lombok.Data;

/**
 * Created by wb on 2018/11/27.
 */
@Data
public class Column {
    private String columnName;
    private String columnType;
    private Boolean nullAble;
    private Boolean primary;
    private String defaultValue;
    private String extra;
    private String columnComment;


    @Override
    public String toString() {
        return "Column{" +
                "columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", nullAble=" + nullAble +
                ", primary=" + primary +
                ", defaultValue='" + defaultValue + '\'' +
                ", extra='" + extra + '\'' +
                ", columnComment='" + columnComment + '\'' +
                '}';
    }
}
