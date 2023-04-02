package com.chaosbuffalo.mkcore.core.records;

public interface IRecordTypeHandler<T extends IRecordInstance> {
    default void onJoinWorld() {

    }

    default void onPersonaActivated() {

    }

    default void onPersonaDeactivated() {

    }

    default void onRecordUpdated(T record) {

    }

    default void onRecordLoaded(T record) {

    }
}
