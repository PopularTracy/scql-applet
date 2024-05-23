package com.kpi.tuke.scql;

import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Class table is implemented by the definition of the table in ISO 7816-7.
 * Table consist of table name, columns and {@code Data} row information.
 * Additionally, table performs the modifications to the rows
 */
public class Table implements Performable {

    private byte[] tableName;
    private byte columnsN;
    private byte[] columns;

    private Data[] rows;
    private short dataCursor = 0;

    public Table(byte[] tableName, byte columnsN, byte[] columns) {
        this.tableName = tableName;
        this.columnsN = columnsN;
        this.columns = columns;
        rows = new Data[SCQL_ISO7816.MAX_ROWS];
    }

    /**
     * Deletes the row, by index.
     * @param rowIndex table row index.
     */
    @Override
    public void delete(short rowIndex) {

        if (rowIndex < 0 || rowIndex >= dataCursor) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        rows[rowIndex] = null;
        dataCursor--;

        for (short i = rowIndex; i < dataCursor; i++) {
            rows[i] = rows[(short) (i + 1)];
        }

        rows[dataCursor] = null;
    }

    /**
     * Updates the column data to the pointed column index.
     * @param data new data to update.
     * @param columnIndex index of column.
     */
    @Override
    public void update(byte[] data, short columnIndex) {

        if (data == null || data.length == 0) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        if (columnIndex < 0 || columnIndex >= dataCursor) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        Data dataRow = rows[columnIndex];


    }

    /**
     * Filters the rows by specified filters.
     * @param filters filters to apply.
     * @return filtered {@code Data}.
     */
    @Override
    public Data[] filter(Filter[] filters) {

        Data[] buffer = new Data[dataCursor];

        // Filling the buffer
        for (short i = 0; i < dataCursor; i++) {
            buffer[i] = rows[i];
        }

        // No filters to apply
        if (filters == null) {
            return buffer;
        }

        // Filtering all the data in the buffer
        for (short i = 0; i < filters.length; i++) {
            Filter filter = filters[i];
            short columnIndex = getColumnIndexByName(filter.getColumnName());

            // Applying filters
            for (short j = 0; j < dataCursor; j++) {
                Data row = buffer[j];

                if (row == null) {
                    continue;
                }

                // If filter is NOT apply, then remove the row from buffer
                if (!row.isFilterApply(columnIndex, filter.getOperand(), filter.getValue())) {
                    buffer[j] = null;
                }
            }

            // Checking, if the buffer is empty after filtering
            boolean isEmpty = true;
            for (short j = 0; j < dataCursor; j++) {
                if (buffer[j] != null) {
                    isEmpty = false;
                    break;
                }
            }

            // If the buffer empty, there is no reason to continue filtering
            if (isEmpty) {
                return new Data[0];
            }
        }

        // Counting filtered rows
        short countFilteredData = 0;
        for (short i = 0; i < dataCursor; i++) {
            if (buffer[i] != null) {
                countFilteredData++;
            }
        }

        // Creating new array with filtered rows
        short index = 0;
        Data[] filteredData = new Data[countFilteredData];
        for (short i = 0; i < dataCursor; i++) {
            if (buffer[i] != null) {
                filteredData[index] = buffer[i];
                index++;
            }
        }

        return filteredData;
    }

    /**
     * Table name getter.
     * @return table name.
     */
    @Override
    public byte[] getName() {
        return tableName;
    }

    /**
     * Gets amount of columns in the table.
     * @return number of columns.
     */
    @Override
    public short getColumnN() {
        return columnsN;
    }

    /**
     * Drops the all instances in the table.
     */
    @Override
    public void drop() {
        for (short i = 0; i < dataCursor; i++) {
            rows[i] = null;
        }
    }

    /**
     * Adds a new {@code Data} row into the table array.
     * @param data new row to add.
     */
    public void addData(Data data) {

        if (data == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        if (dataCursor > SCQL_ISO7816.MAX_ROWS) {
            ISOException.throwIt(SCQL_ISO7816.SW_END_OF_TABLE);
        }

        this.rows[dataCursor] = data;
        this.rows[dataCursor].setIndex(dataCursor);
        dataCursor++;
    }

    /**
     * Gets array column index in the table, by the given name.
     * @param columnName column name.
     * @return index of the array column, if the column name exists in the table.
     * -1 - otherwise.
     */
    @Override
    public short getColumnIndexByName(byte[] columnName) {

        if (columnName == null || columns.length == 0) {
            ISOException.throwIt(SCQL_ISO7816.SW_WRONG_DATA);
        }

        short index = 0;
        for (short i = 0; i < columnsN; i++) {
            short length = columns[index];
            index++;

            if (length == columnName.length) {
                if (Util.arrayCompare(columns, index, columnName, (short) 0, length) == 0) {
                    return i;
                }
            }

            index += length;
        }

        return -1;
    }

    public byte[] getColumns() {
        return columns;
    }

    public void setColumns(byte[] columns) {
        this.columns = columns;
    }

    public byte getColumnsN() {
        return columnsN;
    }

    public void setColumnsN(byte columnsN) {
        this.columnsN = columnsN;
    }

    public byte[] getTableName() {
        return tableName;
    }

    public void setTableName(byte[] tableName) {
        this.tableName = tableName;
    }
}
