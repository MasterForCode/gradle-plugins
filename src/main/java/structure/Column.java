package structure;

/**
 * Created by wb on 2018/11/27.
 */
public class Column {
    private String columnName;
    private String columnType;
    private Boolean nullAble;
    private Boolean isPrimary;
    private String defaultValue;
    private String extra;
    private String columnComment;

    public Column() {
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public Boolean getNullAble() {
        return nullAble;
    }

    public void setNullAble(Boolean nullAble) {
        this.nullAble = nullAble;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    @Override
    public String toString() {
        return "Column{" +
                "columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", nullAble=" + nullAble +
                ", isPrimary=" + isPrimary +
                ", defaultValue='" + defaultValue + '\'' +
                ", extra='" + extra + '\'' +
                ", columnComment='" + columnComment + '\'' +
                '}';
    }
}