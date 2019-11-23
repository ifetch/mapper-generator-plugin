package com.ifetch.cq.model;


import java.io.Serializable;

/**
 * Created by Owen on 6/22/16.
 */
public class UITableColumnVO implements Serializable {

    private boolean checked = true;

    private String columnName;

    private String javaType;

    private String jdbcType;

    private String propertyName;

    private String typeHandle;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getTypeHandle() {
        return typeHandle;
    }

    public void setTypeHandle(String typeHandle) {
        this.typeHandle = typeHandle;
    }
}
