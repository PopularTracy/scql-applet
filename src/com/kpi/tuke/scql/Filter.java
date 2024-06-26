package com.kpi.tuke.scql;

/**
 * The class holds information about a filter definition, such as the column name to be applied,
 * the operand to filter, and the value to filter the column.
 */
public class Filter {

    private byte[] columnName;
    private byte operand;
    private byte[] value;

    public Filter() {
    }

    public Filter(byte[] columnName, byte operand, byte[] value) {
        this.columnName = columnName;
        this.operand = operand;
        this.value = value;
    }

    public byte[] getColumnName() {
        return columnName;
    }

    public void setColumnName(byte[] columnName) {
        this.columnName = columnName;
    }

    public byte getOperand() {
        return operand;
    }

    public void setOperand(byte operand) {
        this.operand = operand;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
