package com.kpi.tuke.scql;

import javacard.framework.ISOException;

public class Cursor {

    private Performable obj;
    private byte[] columnIndexes;
    private Filter[] filters;

    private Data[] selectedData;
    private short dataCursor;

    public Cursor(Performable obj, byte[] columnIndexes, Filter[] filters) {
        this.obj = obj;
        this.columnIndexes = columnIndexes;
        this.filters = filters;

        this.dataCursor = -1;
        this.selectedData = null;
    }

    public void removeReference() {
        obj = null;

        if (selectedData != null) {
            for (short i = 0; i < selectedData.length; i++) {
                selectedData[i] = null;
            }
            dataCursor = -1;
            selectedData = null;
        }
    }

    public void update(byte[] data, byte[] columnName) {

        if (data == null || data.length == 0) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        if (columnName == null || columnName.length == 0) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        if (obj == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        // If cursor was not opened
        if (selectedData == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        // Getting the column index
        short columnIndex = obj.getColumnIndexByName(columnName);

        if (columnIndex == -1) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }


    }

    public void delete() {
        // If cursor was not opened
        if (selectedData == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        if (dataCursor >= selectedData.length) {
            ISOException.throwIt(SCQL_ISO7816.SW_END_OF_TABLE);
        }

        // Getting table index of the row
        short indexToDelete = selectedData[dataCursor].getIndex();
        obj.delete(indexToDelete);

        // Moving the cursor to the next logical position
        next();
    }

    public void open() {
        this.selectedData = obj.filter(filters);

        if (selectedData.length == 0) {
            ISOException.throwIt(SCQL_ISO7816.SW_END_OF_TABLE);
        }

        this.dataCursor = 0;
    }

    public void next() {

        // If cursor was not opened
        if (selectedData == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        dataCursor++;

        if (dataCursor > selectedData.length) {
            ISOException.throwIt(SCQL_ISO7816.SW_END_OF_TABLE);
        }
    }

    public byte[] fetch() {

        // If cursor was not opened
        if (selectedData == null || dataCursor == -1) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        if (dataCursor >= selectedData.length) {
            ISOException.throwIt(SCQL_ISO7816.SW_END_OF_TABLE);
        }

        Data data = selectedData[dataCursor];

        if (data == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        byte[] columns = data.getData();

        return DatabaseUtil.selectColumnsByIndexes(columns, columnIndexes);
    }

    public byte[] fetchNext() {
        byte[] result = fetch();
        next();
        return result;
    }

    public short getColumnsCount() {
        return (short) this.columnIndexes.length;
    }

    public Filterable getObj() {
        return obj;
    }

    public void setObj(Performable obj) {
        this.obj = obj;
    }

    public byte[] getColumnIndexes() {
        return columnIndexes;
    }

    public void setColumnIndexes(byte[] columnIndexes) {
        this.columnIndexes = columnIndexes;
    }

    public Filter[] getFilters() {
        return filters;
    }

    public void setFilters(Filter[] filters) {
        this.filters = filters;
    }

    public Data[] getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(Data[] selectedData) {
        this.selectedData = selectedData;
    }

    public short getDataCursor() {
        return dataCursor;
    }

    public void setDataCursor(short dataCursor) {
        this.dataCursor = dataCursor;
    }
}
