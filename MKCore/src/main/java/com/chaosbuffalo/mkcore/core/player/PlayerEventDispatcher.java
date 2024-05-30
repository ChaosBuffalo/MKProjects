package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.events.*;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Inspired by epicfightmod
public class PlayerEventDispatcher {
    private final MKPlayerData playerData;
    private final Multimap<EventType<?>, EventRegistration<?>> eventMap;

    public PlayerEventDispatcher(MKPlayerData playerData) {
        this.playerData = playerData;
        eventMap = MultimapBuilder.hashKeys().treeSetValues().build();
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function) {
        subscribe(eventType, uuid, function, EventPriorities.CONSUMER);
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function, int priority) {
        subscribe(eventType, () -> new EventRegistration<>(uuid, function, priority));
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, Supplier<EventRegistration<T>> recordSupplier) {
        if (!eventType.canFire(playerData.isClientSide())) {
            return;
        }

        var triggerRecord = recordSupplier.get();
        unsubscribe(eventType, triggerRecord);
        eventMap.put(eventType, triggerRecord);
    }

    private  <T extends PlayerEvent<?>> void unsubscribe(EventType<T> eventType, EventRegistration<T> record) {
        var typeList = eventMap.get(eventType);
        if (!typeList.isEmpty()) {
            typeList.removeIf(t -> t.matches(record));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEvent<?>> void trigger(EventType<T> eventType, T event) {
        if (eventType.canFire(playerData.isClientSide())) {
            var typeList = eventMap.get(eventType);
            if (!typeList.isEmpty()) {
                for (EventRegistration<?> eventRegistration : typeList) {
                    ((EventRegistration<T>) eventRegistration).trigger(event);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEvent<?>> void tryTrigger(EventType<T> eventType, Supplier<T> eventSupplier) {
        if (eventType.canFire(playerData.isClientSide())) {
            var typeList = eventMap.get(eventType);
            if (!typeList.isEmpty()) {
                T event = eventSupplier.get();
                for (EventRegistration<?> eventRegistration : typeList) {
                    ((EventRegistration<T>) eventRegistration).trigger(event);
                }
            }
        }
    }

    public <T extends PlayerEvent<?>> boolean hasSubscribers(EventType<T> eventType) {
        if (eventType.canFire(playerData.isClientSide())) {
            var typeList = eventMap.get(eventType);
            return !typeList.isEmpty();
        }
        return false;
    }

}
