package com.kpi.tuke.scql;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

/**
 *
 */
public class ScqlDatabase {


    private Table[] tables;
    private View[] views;
    private Cursor cursor = null;

    private byte tableCursor;
    private byte viewCursor;

    private short byteIndex = 0;

    public ScqlDatabase() {
        this.tables = new Table[SCQL_ISO7816.MAX_TABLES];
        this.views = new View[SCQL_ISO7816.MAX_VIEWS];
        this.tableCursor = 0;
        this.viewCursor = 0;
    }

    /**
     * The method creates new Table instance in the Database
     * @param apdu - command with necessary table data
     */
    public void createTable(APDU apdu) {

        if (tableCursor >= SCQL_ISO7816.MAX_TABLES) {
            ISOException.throwIt(SCQL_ISO7816.SW_FILE_FULL);
        }

        byte[] buffer = apdu.getBuffer();

        byte[] tableMetadata = startRead(apdu);
        byte lc = buffer[SCQL_ISO7816.OFFSET_LC];
        byteIndex = tableMetadata[0];

        // Checking table before creating instance
        byte[] tableName = new byte[byteIndex];
        Util.arrayCopy(tableMetadata, (short) 1, tableName, (short) 0, byteIndex);

        // Checking if table with the name already exist
        // If true, throw error

        if (DatabaseUtil.isObjExistsWithName(tables, tableCursor, tableName)) {
            ISOException.throwIt(SCQL_ISO7816.SW_OBJECT_EXIST);
        }

        // Checking table number of columns N
        // which should be 0 < N < MAX_COLUMNS
        byteIndex++;
        short columnsCount = tableMetadata[byteIndex];
        if (columnsCount < 1 || columnsCount > SCQL_ISO7816.MAX_COLUMNS) {
            ISOException.throwIt(SCQL_ISO7816.SW_WRONG_DATA);
        }

        // Copy columns
        byteIndex++;
        byte[] columns = new byte[(short) (lc - byteIndex)];
        Util.arrayCopy(tableMetadata, byteIndex, columns, (short) 0, (short) (lc - byteIndex));
 
        // Creating new instance of table object and setting the metadata
        Table table = new Table(tableName, (byte) columnsCount, columns);
        tables[tableCursor] = table;
 
        // Updating table cursor
        tableCursor++;
    }

    /**
     * The method creates new View instance in the Database.
     * View is referred to existing table.
     * @param apdu - command with necessary table data
     */
    public void createView(APDU apdu) {

        if (viewCursor >= SCQL_ISO7816.MAX_VIEWS) {
            ISOException.throwIt(SCQL_ISO7816.SW_FILE_FULL);
        }

        byte[] viewMetadata = startRead(apdu);
        byteIndex = 0;

        // Coping view name
        byte[] viewName = readNextBytesLp(viewMetadata);

        // Checking view name for uniqueness
        if (DatabaseUtil.isObjExistsWithName(views, viewCursor, viewName)) {
            ISOException.throwIt(SCQL_ISO7816.SW_OBJECT_EXIST);
        }

        // Check view name with tables
        if (DatabaseUtil.isObjExistsWithName(tables, tableCursor, viewName)) {
            ISOException.throwIt(SCQL_ISO7816.SW_OBJECT_EXIST);
        }

        // Coping the table name
        byte[] tableName = readNextBytesLp(viewMetadata);

        // Checking if table exists
        Table refferedTable = (Table) DatabaseUtil.getSearchableByName(tables, tableCursor, tableName);
        
        if (refferedTable == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        short viewColumnsCount = viewMetadata[byteIndex];
        byteIndex++;

        // Reading all columns as indexes
        byte[] columnIndexes = selectColumnIndexes(viewMetadata, viewColumnsCount, refferedTable);

        // Reading all filters (can be null)
        Filter[] filters = readFilters(viewMetadata);

        View view = new View(refferedTable, viewName, columnIndexes, filters);
        views[viewCursor] = view;
        viewCursor++;
    }

    /**
     * Method creates new instance of {@code Cursor} from the APDU command with specified columns and
     * filters.
     * @param apdu - command with necessary cursor data
     */
    public void declareCursor(APDU apdu) {

        byte[] data = startRead(apdu);

        // Coping the table name
        byte[] objName = readNextBytesLp(data);

        Performable obj = null;

        // If the obj name is the table or view - process the command
        // otherwise - throw an error
        if (DatabaseUtil.isObjExistsWithName(tables, tableCursor, objName)) {
            obj = DatabaseUtil.getSearchableByName(tables, tableCursor, objName);
        } else if (DatabaseUtil.isObjExistsWithName(views, viewCursor, objName)) {
            obj = DatabaseUtil.getSearchableByName(views, viewCursor, objName);
        } else {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        short columnsCount = data[byteIndex];
        byteIndex++;

        // Reading all columns as indexes
        byte[] columnIndexes = selectColumnIndexes(data, columnsCount, obj);

        // Reading all filters (can be null)
        Filter[] filters = readFilters(data);

        cursor = new Cursor(obj, columnIndexes, filters);
    }

    /**
     * Opens the declared cursor to retrieve the data in select and initialize cursor on the first
     * row.
     */
    public void open() {
        // If cursor was not declared
        if (cursor == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        cursor.open();
    }

    /**
     * Moves cursor to the next position
     */
    public void next() {
        // If cursor was not declared
        if (cursor == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        cursor.next();
    }

    /**
     * Fetches selected data from the cursor positioned on a row.
     * @param apdu - instance to make APDU response
     */
    public void fetch(APDU apdu) {

        // If cursor was not declared
        if (cursor == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        byte[] buffer = apdu.getBuffer();

        byte[] selectedData = cursor.fetch();
        short columns = cursor.getColumnsCount();

        // inform system that the applet has finished
        // processing the command and the system should
        // now prepare to construct a response APDU
        // which contains data field
        apdu.setOutgoing();

        // informs the CAD the actual number of bytes returned
        apdu.setOutgoingLength((short) (selectedData.length + 1));

        buffer[0] = (byte) columns;

        // Prepare response in buffer
        Util.arrayCopy(selectedData, (short) 0, buffer, (short) 1, (short) selectedData.length);

        apdu.sendBytes((short) 0, (short) (selectedData.length + 1));
    }

    /**
     * Performs fetching the data and moving the cursor
     * @param apdu
     */
    public void fetchNext(APDU apdu) {
        fetch(apdu);
        next();
    }

    /**
     * The method creates new {@code Data} instances from APDU command and stores
     * it in the {@code Table} instance.
     * @param apdu - command with insert data.
     */
    public void insertInto(APDU apdu) {

        byte[] buffer = apdu.getBuffer();

        byte[] data = startRead(apdu);
        byte lc = buffer[SCQL_ISO7816.OFFSET_LC];

        // Coping the table name
        byte[] tableName = readNextBytesLp(data);

        // Getting table instance
        Table table = DatabaseUtil.getTableByName(tables, tableCursor, tableName);

        // Throwing the error if the table wasn't found
        if (table == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        // Getting the amount of columns and checking
        // it with the table's columns amount
        short nData = data[byteIndex];
        byteIndex++;

        if (nData != table.getColumnsN()) {
            ISOException.throwIt(SCQL_ISO7816.SW_WRONG_LENGTH);
        }

        // Copying entire data chunk
        byte[] columns = new byte[(short) (lc - byteIndex)];
        Util.arrayCopy(data, byteIndex, columns, (short) 0, (short) (lc - byteIndex));

        // Reading Lp of data block and checking the length of it
        for (short i = 0; i < nData; i++) {
            byte dataLp = data[byteIndex];
            byteIndex += (short) (dataLp + 1);

            if (dataLp > SCQL_ISO7816.MAX_DATA_COLUMN_LENGTH) {
                ISOException.throwIt(SCQL_ISO7816.SW_WRONG_LENGTH);
            }
        }

        // Adding new data row to the table
        table.addData(new Data(columns));
    }

    /**
     * Method drops existing table and data inside of it. Additionally, method
     * assigning null references to all views and cursor, where the table was used
     * @param apdu - command with table name to drop.
     */
    public void dropTable(APDU apdu) {
    	
    	short memoryConsumption = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);

        byte[] data = startRead(apdu);
        // Coping the table name
        byte[] tableName = readNextBytesLp(data);

        short index = DatabaseUtil.getTableIndexByName(tables, tableCursor, tableName);

        if (index == -1) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        Table tableToDrop = tables[index];

        tableToDrop.drop();

        // Each view with referred table should delete the reference
        for (short i = 0; i < viewCursor; i++) {
            View view = views[i];

            if (view.getTable().equals(tableToDrop)) {
                view.removeTable();
            }
        }

        // Removing reference of table from cursor if available
        if (cursor != null && cursor.getObj() != null && cursor.getObj() instanceof Table) {
            Table cursorTable = (Table) cursor.getObj();
            if (cursorTable.equals(tableToDrop)) {
                cursor.removeReference();
            }
        }

        tables[index] = null;
        tableCursor--;

        // Rearranging instances
        for (short i = index; i < tableCursor; i++) {
            tables[i] = tables[(short) (i + 1)];
        }
        tables[tableCursor] = null;

        // Requesting deletion mechanism
        JCSystem.requestObjectDeletion();
    }

    /**
     * Method deletes instance of view by name. Additionally, method assigns null
     * reference to the {@code Cursor} if view used there.
     * @param apdu - command with view name to drop.
     */
    public void dropView(APDU apdu) {

        short memoryConsumption = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);

        byte[] data = startRead(apdu);

        // Coping the view name
        byte[] viewName = readNextBytesLp(data);
        short index = DatabaseUtil.getViewIndexByName(views, viewCursor, viewName);

        if (index == -1) {
            ISOException.throwIt(SCQL_ISO7816.SW_REFERENCED_OBJ_NOT_FOUND);
        }

        View viewToDrop = views[index];

        viewToDrop.drop();

        // Removing reference of view from cursor if available
        if (cursor != null && cursor.getObj() != null && cursor.getObj() instanceof View) {
            View cursorView = (View) cursor.getObj();
            if (cursorView.equals(viewToDrop)) {
                cursor.removeReference();
            }
        }

        views[index] = null;
        viewCursor--;

        // Rearranging instances
        for (short i = index; i < viewCursor; i++) {
            views[i] = views[(short) (i + 1)];
        }
        views[viewCursor] = null;

        // Requesting deletion mechanism
        JCSystem.requestObjectDeletion();
    }

    /**
     * Deletes the row in the table, to which cursor is pointing
     */
    public void delete() {
        // If cursor was not declared
        if (cursor == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        cursor.delete();
    }

    /**
     * Method selects column names from source array and check it with the {@code Searchable} object.
     * @param source - Source with columns block
     * @param columnsCount - Amount of columns
     * @param referredObj - Reference to {@code Searchable} object instance
     * @return - Array with column instances referred to {@code referredObj}
     */
    private byte[] selectColumnIndexes(byte[] source, short columnsCount, Performable referredObj) {

        byte[] columnIndexes = null;

        // Comparing view columns count and table count
        if (columnsCount > referredObj.getColumnN()) {
            ISOException.throwIt(SCQL_ISO7816.SW_WRONG_DATA);
        } else if (columnsCount > 0) {
            columnIndexes = new byte[columnsCount];

            // Getting all the columns
            for (short i = 0; i < columnsCount; i++) {

                // Getting column name
                byte[] columnName = readNextBytesLp(source);

                byte columnIndex = (byte) referredObj.getColumnIndexByName(columnName);

                // Checking if the column exists in the table.
                // If the column exists adding the index of the table's column to the array
                // otherwise throw an exception
                if (columnIndex >= 0) {
                    columnIndexes[i] = columnIndex;
                } else {
                    ISOException.throwIt(SCQL_ISO7816.SW_WRONG_DATA);
                }
            }
        } else { // N == 0, that means all the columns of the table
            columnIndexes = new byte[referredObj.getColumnN()];

            for (short i = 0; i < referredObj.getColumnN(); i++) {
                columnIndexes[i] = (byte) i;
            }
        }

        return columnIndexes;
    }

    /**
     * The method is used to read filters block of APDU command.
     * APDU filter structure: N columnName operand value
     * @param buffer - buffer with filters
     * @return Array of {@code Filter} instances. Null - if buffer don't have filters block
     */
    private Filter[] readFilters(byte[] buffer) {
    	
    	if (buffer.length <= byteIndex) {
    		return null;
    	}
    	
        short filtersCount = buffer[byteIndex];
        byteIndex++;

        if (filtersCount == 0) {
            return null;
        }

        Filter[] filters = new Filter[filtersCount];

        // Lp columnName Lp operator Lp value
        for (short i = 0; i < filtersCount; i++) {
            byte[] column = readNextBytesLp(buffer);

            byteIndex++;
            byte operator = buffer[byteIndex];
            byteIndex++;

            byte[] value = readNextBytesLp(buffer);

            filters[i] = new Filter(column, operator, value);
        }

        return filters;
    }

    /**
     * Reads block of bytes defined by Lp in APDU command.
     * @param source - array of bytes.
     * @return Read block of Lp bytes from the source array.
     */
    private byte[] readNextBytesLp(byte[] source) {
        short bytesToRead = source[byteIndex];
        byte[] obj = new byte[bytesToRead];
        Util.arrayCopy(source, (short) (byteIndex + 1), obj, (short) 0, bytesToRead);
        byteIndex += (short) (bytesToRead + 1);

        return obj;
    }

    private byte[] startRead(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byteIndex = 0;
        
        // setIncomingAndReceive gets as many bytes of
        // data as will fit into the APDU buffer,
        // and returns the number of bytes it reads.
        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        if (byteRead != buffer[SCQL_ISO7816.OFFSET_LC]) {
            ISOException.throwIt(SCQL_ISO7816.SW_WRONG_DATA);
        }

        // Copying data from APDU command
        byte lc = buffer[SCQL_ISO7816.OFFSET_LC];
        byte[] data = new byte[lc];
        Util.arrayCopy(buffer, SCQL_ISO7816.OFFSET_CDATA, data, (short) 0, lc);

        return data;
    }
}
