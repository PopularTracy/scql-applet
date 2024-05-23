package com.kpi.tuke.scql;

import javacard.framework.ISOException;

/**
 * Class View holds the information about the view structure in databases.
 * As defined in ISO7816-7, view contains a name, table to which it should refer,
 * column names from the referred table and select filters.
 */
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

    /**
     * Removes {@code Table} reference from the view.
     */
    public void removeTable() {
        table = null;
    }

    /**
     * Deletion of rows in the view is not supported.
     * @param columnIndex array column index in the table.
     */
    @Override
    public void delete(short columnIndex) {
        ISOException.throwIt(SCQL_ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    /**
     * Removes all object references from the view and prepare it for the
     * memory erase.
     */
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

    /**
     * Updates a column in the view's referred table.
     * @param data new column data.
     * @param columnIndex index of the column to update.
     */
    @Override
    public void update(byte[] data, short columnIndex) {

    }

    /**
     * Filters columns and rows by the specified {@code Filter} conditions inside view and additional of outside.
     * Returns new instances of {@code Data} from the referred table.
     * @param externalFilters additional filters to apply.
     * @return filtered {@code Data} objects
     */
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

    /**
     * Method filters the given rows to the specified amount of column names (indexes) in the view.
     * @param src rows to be filtered.
     * @return new filtered rows with restricted columns within the view.
     */
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

    /**
     * Method is searching the index of the array inside the referred view table.
     * @param columnName name of desired column.
     * @return array index if column name exists. -1 - otherwise.
     */
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

    /**
     * Gets the view name.
     * @return view name.
     */
    @Override
    public byte[] getName() {
        return viewName;
    }

    /**
     * Gets column amount.
     * @return column amount.
     */
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
