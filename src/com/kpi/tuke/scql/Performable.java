package com.kpi.tuke.scql;

/**
 * Interface, which can perform various operations on the data structure,
 * such as updating, deleting and dropping. In addition, extends {@code Filterable} interface,
 * so that, can filter a data.
 */
public interface Performable extends Filterable {
	byte[] getName();
    short getColumnN();
    short getColumnIndexByName(byte[] columnName);
    void update(byte[] data, short columnIndex);
    void delete(short columnIndex);
    void drop();
}
