package com.kpi.tuke.scql;

import javacard.framework.ISOException;

public class View implements Performable {

    private Table table;
    private byte[] viewName;
    private byte[] columnIndexes;
    private Filter[] filters;

    public View(Table table, byte[] viewName, byte[] columnIndexes, Filter[] filters) {
        this.table = table;
        this.viewName = viewName;
        this.columnIndexes = columnIndexes;
        this.filters = filters;
    }

    public void removeTable() {
        table = null;
    }

    @Override
    public void delete(short columnIndex) {
        ISOException.throwIt(SCQL_ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    @Override
    public void drop() {
        removeTable();

        if (filters != null) {
            for (short i = 0; i < filters.length; i++) {
                filters[i] = null;
            }
            filters = null;
        }
    }

    @Override
    public void update(byte[] data, short columnIndex) {

    }

    @Override
    public Data[] filter(Filter[] externalFilters) {

        if (table == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        Data[] result;

        if (this.filters == null && externalFilters == null) { // If no filter available
            result = table.filter(null);
        } else if (externalFilters == null) { // Only applying view filters
            result = table.filter(this.filters);
        } else if (this.filters == null) { // If view has no filters, applying external one
            result = table.filter(externalFilters);
        } else {
            // If the view and external filters are available, merge them
            Filter[] allFilters = new Filter[(short)(externalFilters.length + this.filters.length)];

            for (short i = 0; i < this.filters.length; i++) {
                allFilters[i] = this.filters[i];
            }

            short index = (short) this.filters.length;

            for (short i = 0; i < externalFilters.length; i++) {
                allFilters[(short)(index + i)] = externalFilters[i];
            }

            // Filter all data by view filters and external one
            result = table.filter(allFilters);
        }

        return filterByColumnIndex(result);
    }

    private Data[] filterByColumnIndex(Data[] src) {

        if (table == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        if (src == null) {
            return new Data[0];
        }

        if (src.length == 0) {
            return src;
        }

        // If selecting all columns, simply returning same data
        if (table.getColumnsN() == columnIndexes.length) {
            return src;
        }

        // Creating new data structure with new column sizes
        Data[] filteredData = new Data[src.length];
        for (short i = 0; i < src.length; i++) {
            Data data = src[i];

            byte[] filteredColumns = DatabaseUtil.selectColumnsByIndexes(data.getData(), this.columnIndexes);
            filteredData[i] = new Data(filteredColumns);
        }

        return filteredData;
    }

    @Override
    public short getColumnIndexByName(byte[] columnName) {

        if (table == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        short index = table.getColumnIndexByName(columnName);

        if (index != -1) {
            for (short i = 0; i < columnIndexes.length; i++) {
                if (columnIndexes[i] == index) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public byte[] getName() {
        return viewName;
    }

    @Override
    public short getColumnN() {
        return (short) columnIndexes.length;
    }

    public byte[] getViewName() {
        return viewName;
    }

    public void setViewName(byte[] viewName) {
        this.viewName = viewName;
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

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setFilters(Filter[] filters) {
        this.filters = filters;
    }
}
