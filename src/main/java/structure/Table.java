package structure;

import lombok.Data;

import java.util.List;

/**
 * Created by wb on 2018/11/27.
 */
@Data
public class Table {
    private String tableName;
    private List<Column> columnList;
    private String tableComment;

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", columnList=" + columnList +
                ", tableComment='" + tableComment + '\'' +
                '}';
    }
}
