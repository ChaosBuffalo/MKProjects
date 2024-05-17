package com.chaosbuffalo.mkcore.core.records;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.persona.Persona;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlayerRecordDispatcher<T extends IRecordInstance<T>> {
    private final Persona persona;
    private final Supplier<Stream<T>> recordSupplier;
    private final Map<IRecordType<T>, IRecordTypeHandler<T>> typeHandlerMap = new HashMap<>();

    public PlayerRecordDispatcher(Persona persona, Supplier<Stream<T>> recordSupplier) {
        this.persona = persona;
        this.recordSupplier = recordSupplier;
    }

    private IRecordTypeHandler<T> getRecordHandler(IRecordInstance<T> record) {
        return getTypeHandler(record.getRecordType());
    }

    public void onRecordUpdated(T record) {
        getRecordHandler(record).onRecordUpdated(record);
    }

    public IRecordTypeHandler<T> getTypeHandler(IRecordType<T> type) {
        return typeHandlerMap.computeIfAbsent(type, t -> t.createTypeHandler(persona));
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
