package com.chaosbuffalo.mkcore.core.player.events;

import com.chaosbuffalo.mkcore.core.persona.Persona;

import java.util.UUID;
import java.util.function.Consumer;

public final class PersonaEventRegistration<T extends PlayerEvent<?>> extends EventRegistration<T> {
    private final Persona persona;

    public PersonaEventRegistration(Persona persona, UUID id, Consumer<T> callback, int priority) {
        super(id, callback, priority);
        this.persona = persona;
    }

    @Override
    public boolean matches(EventRegistration<?> other) {
        if (other instanceof PersonaEventRegistration<?> record) {
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
