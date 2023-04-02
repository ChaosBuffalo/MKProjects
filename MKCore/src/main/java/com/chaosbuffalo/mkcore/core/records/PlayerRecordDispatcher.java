package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlayerRecordDispatcher {
    private final MKPlayerData playerData;
    private final Supplier<Stream<? extends IRecordInstance>> recordSupplier;
    private final Map<IRecordType<?>, IRecordTypeHandler<?>> typeHandlerMap = new HashMap<>();

    public PlayerRecordDispatcher(MKPlayerData playerData, Supplier<Stream<? extends IRecordInstance>> recordSupplier) {
        this.playerData = playerData;
        this.recordSupplier = recordSupplier;
    }

    @SuppressWarnings("unchecked")
    private <T extends IRecordInstance> IRecordTypeHandler<T> getRecordHandler(IRecordInstance record) {
        return (IRecordTypeHandler<T>) getTypeHandler(record.getRecordType());
    }

    public <T extends IRecordInstance> void onRecordUpdated(T record) {
        getRecordHandler(record).onRecordUpdated(record);
    }

    @SuppressWarnings("unchecked")
    public <T extends IRecordTypeHandler<?>> T getTypeHandler(IRecordType<T> type) {
        return (T) typeHandlerMap.computeIfAbsent(type, t -> type.createTypeHandler(playerData));
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerRecordDispatcher.onPersonaActivated");
        typeHandlerMap.clear();

        recordSupplier.get().forEach(r -> {
            MKCore.LOGGER.debug("PlayerRecordDispatcher.onPersonaActivated.onRecordLoaded {}", r);
            getRecordHandler(r).onRecordLoaded(r);
        });

        typeHandlerMap.values().forEach(IRecordTypeHandler::onPersonaActivated);
    }

    public void onPersonaDeactivated() {
        MKCore.LOGGER.debug("PlayerRecordDispatcher.onPersonaDeactivated");
        typeHandlerMap.values().forEach(IRecordTypeHandler::onPersonaDeactivated);
    }

    public void onJoinWorld() {
        MKCore.LOGGER.debug("PlayerRecordDispatcher.onJoinWorld");
        typeHandlerMap.values().forEach(IRecordTypeHandler::onJoinWorld);
    }
}
