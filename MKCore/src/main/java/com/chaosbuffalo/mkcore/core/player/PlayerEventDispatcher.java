package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.events.EPriority;
import com.chaosbuffalo.mkcore.core.player.events.EventType;
import com.chaosbuffalo.mkcore.core.player.events.PlayerEvent;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.*;
import java.util.function.Consumer;

// Inspired by epicfightmod
public class PlayerEventDispatcher {
    private final MKPlayerData playerData;
    private final Multimap<EventType<?>, EventRecord<?>> eventMap;


    public PlayerEventDispatcher(MKPlayerData playerData) {
        this.playerData = playerData;
        eventMap = MultimapBuilder.hashKeys().treeSetValues().build();
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function) {
        subscribe(eventType, uuid, function, EPriority.LOWEST);
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function, EPriority priority) {
        if (!eventType.canFire(playerData.isClientSide())) {
            return;
        }

        unsubscribe(eventType, uuid);
        var triggerRecord = new EventRecord<>(uuid, function, priority);
        eventMap.put(eventType, triggerRecord);
    }

    public <T extends PlayerEvent<?>> void unsubscribe(EventType<T> eventType, UUID ownerId) {
        var typeList = eventMap.get(eventType);
        if (!typeList.isEmpty()) {
            typeList.removeIf(t -> t.matches(ownerId));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEvent<?>> void trigger(EventType<T> eventType, T event) {
        if (eventType.canFire(playerData.isClientSide())) {
            var typeList = eventMap.get(eventType);
            if (!typeList.isEmpty()) {
                for (EventRecord<?> eventRecord : typeList) {
                    ((EventRecord<T>) eventRecord).trigger(event);
                }
            }
        }
    }


    public static class EventRecord<T extends PlayerEvent<?>> implements Comparable<EventRecord<?>> {
        private final UUID uuid;
        private final Consumer<T> function;
        private final EPriority priority;

        public EventRecord(UUID ownerId, Consumer<T> function, EPriority priority) {
            this.uuid = ownerId;
            this.function = function;
            this.priority = priority;
        }

        public boolean matches(UUID uuid) {
            return this.uuid.equals(uuid);
        }

        public void trigger(T args) {
            function.accept(args);
        }

        @Override
        public int compareTo(EventRecord<?> o) {
            if (uuid == o.uuid) {
                return 0;
            } else {
                return priority.compareTo(o.priority);
            }
        }
    }
}
