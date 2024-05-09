package com.kpi.tuke.scql;

import javacard.framework.ISOException;
import javacard.framework.Util;

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

    @Override
    public void delete(short columnIndex) {

        if (columnIndex < 0 || columnIndex >= dataCursor) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        rows[columnIndex] = null;
        dataCursor--;

        for (short i = columnIndex; i < dataCursor; i++) {
            rows[i] = rows[(short) (i + 1)];
        }

        rows[dataCursor] = null;
    }

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

    @Override
    public byte[] getName() {
        return tableName;
    }

    @Override
    public short getColumnN() {
        return columnsN;
    }

    @Override
    public void drop() {
        for (short i = 0; i < dataCursor; i++) {
            rows[i] = null;
        }
    }

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
