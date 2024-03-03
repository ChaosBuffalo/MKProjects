package com.chaosbuffalo.mkcore.core.records;

public interface IRecordInstance<T extends IRecordInstance<T>> {
    IRecordType<T> getRecordType();
}
