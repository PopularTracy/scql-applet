package com.kpi.tuke.scql;

public interface Performable extends Filterable {
	byte[] getName();
    short getColumnN();
    short getColumnIndexByName(byte[] columnName);
    void update(byte[] data, short columnIndex);
    void delete(short columnIndex);
    void drop();
}
