package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

public interface IRecordType<T extends IRecordInstance<T>> {
    IRecordTypeHandler<T> createTypeHandler(MKPlayerData playerData);
}
