package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;

public class PlayerEvents {

    public static final PlayerEventListener.EventType<PersonaEvent> PERSONA_ACTIVATE = PlayerEventListener.EventType.serverSide();
    public static final PlayerEventListener.EventType<PersonaEvent> PERSONA_DEACTIVATE = PlayerEventListener.EventType.serverSide();

    public static class PersonaEvent extends PlayerEventListener.PlayerEvent<MKPlayerData> {

        private final Persona persona;

        public PersonaEvent(MKPlayerData playerData, Persona persona) {
            super(playerData);
            this.persona = persona;
        }

        public Persona getPersona() {
            return persona;
        }
    }
}
