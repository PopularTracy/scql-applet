package com.kpi.tuke.scql;

import javacard.framework.ISO7816;

/**
 * Interface extends the {@code ISO7816} for the SCQL ISO7816-7 constants and APDU statuses.
 */
public interface SCQL_ISO7816 extends ISO7816 {
    short MAX_TABLES = 8;
    short MAX_VIEWS = 5;
    short MAX_COLUMNS = 10;
    short MAX_COLUMN_NAME_LENGTH = 8;
    short MAX_DATA_COLUMN_LENGTH = 15;
    short MAX_ROWS = 25;

    short SW_OBJECT_EXIST = 0x6A89;
    short SW_REFERENCED_OBJ_NOT_FOUND = 0x6A88;
    short SW_END_OF_TABLE = 0x6282;
}
