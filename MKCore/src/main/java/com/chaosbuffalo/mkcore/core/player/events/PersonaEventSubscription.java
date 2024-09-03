package com.chaosbuffalo.mkcore.core.player.events;

import com.chaosbuffalo.mkcore.core.persona.Persona;

import java.util.UUID;
import java.util.function.Consumer;

public final class PersonaEventSubscription<T extends PlayerEvent<?>> extends EventSubscription<T> {
    private final Persona persona;

    public PersonaEventSubscription(Persona persona, UUID id, Consumer<T> callback, int priority) {
        super(id, callback, priority);
        this.persona = persona;
    }

    @Override
    public boolean matches(EventSubscription<?> other) {
        if (other instanceof PersonaEventSubscription<?> record) {
            return persona == record.persona && super.matches(other);
        }
        return false;
    }

    @Override
    public void trigger(T args) {
        if (persona.isActive()) {
            super.trigger(args);
        }
    }

    @Override
    public String toString() {
        return "PersonaEventRecord[" +
                "persona=" + persona + ", " +
                "callback=" + callback + ", " +
                "priority=" + priority + ']';
    }

}
