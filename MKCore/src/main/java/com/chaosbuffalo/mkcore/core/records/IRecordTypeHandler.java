package com.chaosbuffalo.mkcore.core.records;

public interface IRecordTypeHandler<T extends IRecordInstance<T>> {

    default void onRecordUpdated(T record) {

    }

    default void onRecordLoaded(T record) {

    }
}
