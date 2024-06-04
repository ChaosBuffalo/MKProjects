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
    private final Multimap<EventType<?>, EventSubscription<?>> eventSubscriptions;

    public PlayerEventDispatcher(MKPlayerData playerData) {
        this.playerData = playerData;
        eventSubscriptions = MultimapBuilder.hashKeys().treeSetValues().build();
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function) {
        subscribe(eventType, uuid, function, EventPriorities.CONSUMER);
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function, int priority) {
        subscribe(eventType, () -> new EventSubscription<>(uuid, function, priority));
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, Supplier<EventSubscription<T>> subscriptionSupplier) {
        if (!eventType.canFire(playerData.isClientSide())) {
            return;
        }

        var subscription = subscriptionSupplier.get();
        unsubscribe(eventType, subscription);
        eventSubscriptions.put(eventType, subscription);
    }

    private  <T extends PlayerEvent<?>> void unsubscribe(EventType<T> eventType, EventSubscription<T> subscription) {
        var subscriptions = eventSubscriptions.get(eventType);
        if (!subscriptions.isEmpty()) {
            subscriptions.removeIf(t -> t.matches(subscription));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEvent<?>> void trigger(EventType<T> eventType, T event) {
        if (eventType.canFire(playerData.isClientSide())) {
            var subscriptions = eventSubscriptions.get(eventType);
            if (!subscriptions.isEmpty()) {
                for (EventSubscription<?> subscription : subscriptions) {
                    ((EventSubscription<T>) subscription).trigger(event);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEvent<?>> void tryTrigger(EventType<T> eventType, Supplier<T> eventSupplier) {
        if (eventType.canFire(playerData.isClientSide())) {
            var subscriptions = eventSubscriptions.get(eventType);
            if (!subscriptions.isEmpty()) {
                T event = eventSupplier.get();
                for (EventSubscription<?> subscription : subscriptions) {
                    ((EventSubscription<T>) subscription).trigger(event);
                }
            }
        }
    }

    public <T extends PlayerEvent<?>> boolean hasSubscribers(EventType<T> eventType) {
        if (eventType.canFire(playerData.isClientSide())) {
            var subscriptions = eventSubscriptions.get(eventType);
            return !subscriptions.isEmpty();
        }
        return false;
    }

}
