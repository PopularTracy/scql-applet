package com.kpi.tuke.scql;

import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * The class holds the information about the rows in the database.
 * In addition, the class stores the index of the array (row number) in the table.
 */
public class Data {

    private byte[] data;
    private short index;

    public Data(byte[] data) {
        this.data = data;
        this.index = -1; // not defined
    }

    public Data(byte[] data, short index) {
        this.data = data;
        this.index = index;
    }

    /**
     * Method checks if the filter can be applied for the data row.
     * @param columnIndex column index in the data row to filter apply.
     * @param operand filter operand.
     * @param filterValue value to filter.
     * @return true - if the operand and values are applied. false - otherwise.
     */
    public boolean isFilterApply(short columnIndex, byte operand, byte[] filterValue) {

        short indexOffset = 0;

        // Offset
        for (short i = 0; i < columnIndex; i++) {
            indexOffset += data[indexOffset];
            indexOffset++;
        }

        short lp = data[indexOffset];
        indexOffset++;

        // Copying the column
        byte[] dataColumn = new byte[lp];
        Util.arrayCopy(data, indexOffset, dataColumn, (short) 0, lp);

        // Checking operand and applying it
        switch (operand) {
            case 0x3d:
                return DatabaseUtil.isEqual(dataColumn, filterValue);
            case 0x3c:
                return DatabaseUtil.isLessThan(dataColumn, filterValue);
            case 0x3e:
                return DatabaseUtil.isGreaterThan(dataColumn, filterValue);
            case 0x4c:
                return DatabaseUtil.isLessOrEqual(dataColumn, filterValue);
            case 0x47:
                return DatabaseUtil.isGreaterOrEqual(dataColumn, filterValue);
            case 0x23:
                return DatabaseUtil.isNotEqual(dataColumn, filterValue);
            default:
                ISOException.throwIt(SCQL_ISO7816.SW_WRONG_DATA);
        }

        return false;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public short getIndex() {
        return index;
    }

    public void setIndex(short index) {
        this.index = index;
    }
}
