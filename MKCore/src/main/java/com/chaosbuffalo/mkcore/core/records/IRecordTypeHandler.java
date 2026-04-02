package com.chaosbuffalo.mkcore.core.records;

public interface IRecordTypeHandler<T extends IRecordInstance<T>> {

    default void onRecordUpdated(T record) {

    }

    default void onRecordLoaded(T record) {

    }

    /**
     * Will be called after all records are loaded
     */
    default void onPersonaActivated() {

    }

    default void onPersonaDeactivated() {

    }
}
