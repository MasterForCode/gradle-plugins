package structure;

import java.util.List;

/**
 * Created by wb on 2018/11/27.
 */
public class Table {
    private String tableName;
    private List<Column> columnList;
    private String tableComment;

    public Table() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", columnList=" + columnList +
                ", tableComment='" + tableComment + '\'' +
                '}';
    }
}