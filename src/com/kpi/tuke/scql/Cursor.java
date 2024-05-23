package com.kpi.tuke.scql;

import javacard.framework.ISOException;

/**
 * Cursor class used for selecting, deleting and updating data rows inside the {@code Performable} object
 * instances.
 * Cursor is defined by ISO7816-7, containing selecting column names and filters to be applied for filtering.
 */
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

    /**
     * Used for clearing the object instance and resets the structure.
     */
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

    /**
     * Updates the data row inside {@code Performable} object instance.
     * @param data columns data to be updated.
     * @param columnName column names to be updated.
     */
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

    /**
     * Deletes the row to which the cursor points to.
     */
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

    /**
     * Opens the cursor.
     */
    public void open() {
        this.selectedData = obj.filter(filters);

        if (selectedData.length == 0) {
            ISOException.throwIt(SCQL_ISO7816.SW_END_OF_TABLE);
        }

        this.dataCursor = 0;
    }

    /**
     * Moves cursor to the next data row.
     */
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

    /**
     * Selects the row data, which cursor points to.
     * @return pointed {@code Data} cursor row.
     */
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

    /**
     * Moves the cursor to the next row and returns the selected data.
     * @return selected data of the next row.
     */
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
