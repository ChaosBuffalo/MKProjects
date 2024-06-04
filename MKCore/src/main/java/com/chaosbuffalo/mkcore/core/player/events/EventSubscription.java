package com.chaosbuffalo.mkcore.core.player.events;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;

public class EventSubscription<T extends PlayerEvent<?>> implements Comparable<EventSubscription<?>> {
    protected final UUID id;
    protected final Consumer<T> callback;
    protected final int priority;

    public EventSubscription(UUID id, Consumer<T> callback, int priority) {
        this.id = id;
        this.callback = callback;
        this.priority = priority;
    }

    public boolean matches(EventSubscription<?> other) {
        return id.equals(other.id);
    }

    public void trigger(T args) {
        callback.accept(args);
    }

    @Override
    public int compareTo(@Nonnull EventSubscription<?> o) {
        if (matches(o)) {
            return 0;
        } else {
            return priority > o.priority ? 1 : -1;
        }
    }

    @Override
    public String toString() {
        return "EventRecord[" +
                "id=" + id + ", " +
                "callback=" + callback + ", " +
                "priority=" + priority + ']';
    }

}
