package com.kpi.tuke.scql;

import javacard.framework.ISOException;
import javacard.framework.Util;

public class DatabaseUtil {

    /**
     * The method compares 2 byte arrays
     * @param srcView array 1 to compare
     * @param destView array 2 to compare
     * @return true - if length and data in arrays are equal, false - otherwise
     */
    public static boolean compare(byte[] srcView, byte[] destView) {

        if (srcView == null || destView == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        if (srcView != null && srcView.length == destView.length) {
            short result = Util.arrayCompare(destView, (short) 0, srcView, (short) 0, (short) destView.length);

            return result == 0;
        }

        return false;
    }

    public static byte[] replace(byte[] src, byte[] value, short columnIndex) {
        return null;
    }

    public static boolean isObjExistsWithName(Performable[] objs, short length, byte[] name) {

        if (objs == null || name == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        for (short i = 0; i < length; i++) {
            Performable obj = objs[i];

            if (obj != null && compare(obj.getName(), name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isTableExists(Table[] tables, short length, byte[] tableName) {
        short index = getTableIndexByName(tables, length, tableName);
        return index != -1;
    }

    public static Performable getSearchableByName(Performable[] objs, short length, byte[] name) {

        if (objs == null || name == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        for (short i = 0; i < length; i++) {
            Performable obj = objs[i];

            if (obj != null && obj.getName().length == name.length && compare(obj.getName(), name)) {
                return obj;
            }

        }

        return null;
    }

    public static Table getTableByName(Table[] tables, short length, byte[] tableName) {
        short index = getTableIndexByName(tables, length, tableName);

        if (index != -1) {
            return tables[index];
        }

        return null;
    }

    public static short getTableIndexByName(Table[] tables, short length, byte[] tableName) {
        if (tables == null || tableName == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        for (short i = 0; i < length; i++) {
            Table table = tables[i];

            if (table != null && compare(table.getTableName(), tableName)) {
                return i;
            }

        }

        return -1;
    }

    public static short getViewIndexByName(View[] views, short length, byte[] viewName) {
        if (views == null || viewName == null) {
            ISOException.throwIt(SCQL_ISO7816.SW_DATA_INVALID);
        }

        for (short i = 0; i < length; i++) {
            View view = views[i];

            if (view != null && compare(view.getViewName(), viewName)) {
                return i;
            }

        }

        return -1;
    }

    public static boolean isEqual(byte[] data1, byte[] data2) {
        return compareArrays(data1, data2) == 0;
    }

    public static boolean isNotEqual(byte[] data1, byte[] data2) {
        return !isEqual(data1, data2);
    }

    public static boolean isLessThan(byte[] data1, byte[] data2) {
        return compareArrays(data1, data2) < 0;
    }

    public static boolean isGreaterThan(byte[] data1, byte[] data2) {
        return compareArrays(data1, data2) > 0;
    }

    public static boolean isLessOrEqual(byte[] data1, byte[] data2) {
        return compareArrays(data1, data2) <= 0;
    }

    public static boolean isGreaterOrEqual(byte[] data1, byte[] data2) {
        return compareArrays(data1, data2) >= 0;
    }

    public static short compareArrays(byte[] array1, byte[] array2) {
        short length1 = (short) array1.length;
        short length2 = (short) array2.length;
        short limit = length1;

        if (length1 > length2) {
            limit = length2;
        }

        for (short i = 0; i < limit; i++) {
            byte byte1 = (byte) (array1[i] & 0xFF); // Convert to unsigned
            byte byte2 = (byte) (array2[i] & 0xFF); // Convert to unsigned
            if (byte1 != byte2) {
                return (short) (byte1 - byte2); // Negative if array1 is smaller, positive if larger
            }
        }

        return (short) (length1 - length2); // Compare lengths if all corresponding bytes are equal
    }

    public static byte[] selectColumnsByIndexes(byte[] data, byte[] columnIndexes) {

        short lc = 0;

        // Counting Lc for all column indexes
        for (short i = 0; i < columnIndexes.length; i++) {
            short lpIndex = getOffset(data, columnIndexes[i]);
            lc += (short) (data[lpIndex] + 1);
        }

        byte[] shorterData = new byte[lc];
        short dataIndex = 0;

        for (short i = 0; i < columnIndexes.length; i++) {
            short lpIndex = getOffset(data, columnIndexes[i]);
            short lp = data[lpIndex];

            shorterData[dataIndex] = (byte) lp;
            dataIndex++;

            Util.arrayCopy(data, (short) (lpIndex + 1), shorterData, dataIndex, lp);

            dataIndex += lp;
        }

        return shorterData;
    }

    public static short getOffset(byte[] data, short columnIndex) {
        short indexOffset = 0;

        // Offset
        for (short i = 0; i < columnIndex; i++) {
            indexOffset += data[indexOffset];
            indexOffset++;
        }

        return indexOffset;
    }

}
