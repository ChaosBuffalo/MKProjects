package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlayerRecordDispatcher<T extends IRecordInstance<T>> {
    private final MKPlayerData playerData;
    private final Supplier<Stream<T>> recordSupplier;
    private final Map<IRecordType<T>, IRecordTypeHandler<T>> typeHandlerMap = new HashMap<>();

    public PlayerRecordDispatcher(MKPlayerData playerData, Supplier<Stream<T>> recordSupplier) {
        this.playerData = playerData;
        this.recordSupplier = recordSupplier;
    }

    private IRecordTypeHandler<T> getRecordHandler(IRecordInstance<T> record) {
        return getTypeHandler(record.getRecordType());
    }

    public void onRecordUpdated(T record) {
        getRecordHandler(record).onRecordUpdated(record);
    }

    public IRecordTypeHandler<T> getTypeHandler(IRecordType<T> type) {
        return typeHandlerMap.computeIfAbsent(type, t -> t.createTypeHandler(playerData));
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerRecordDispatcher.onPersonaActivated");
        typeHandlerMap.clear();

        recordSupplier.get().forEach(r -> {
            MKCore.LOGGER.debug("PlayerRecordDispatcher.onPersonaActivated.onRecordLoaded {}", r);
            getRecordHandler(r).onRecordLoaded(r);
        });
    }
}
