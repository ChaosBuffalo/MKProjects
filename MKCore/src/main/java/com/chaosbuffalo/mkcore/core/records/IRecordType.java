package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.core.persona.Persona;

public interface IRecordType<T extends IRecordInstance<T>> {
    IRecordTypeHandler<T> createTypeHandler(Persona persona);
}
