package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import net.minecraftforge.eventbus.api.Event;

public class PersonaEvent extends Event {
    private final Persona persona;

    public PersonaEvent(Persona persona) {
        this.persona = persona;
    }

    public Persona getPersona() {
        return persona;
    }

    public static class PersonaActivated extends PersonaEvent {

        public PersonaActivated(Persona persona) {
            super(persona);
        }
    }

    public static class PersonaDeactivated extends PersonaEvent {

        public PersonaDeactivated(Persona persona) {
            super(persona);
        }
    }
}
