package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

public interface IRecordType<T extends IRecordTypeHandler<?>> {
    T createTypeHandler(MKPlayerData playerData);
}
