package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.events.EventType;
import com.chaosbuffalo.mkcore.core.player.events.PlayerEvent;

public class PlayerEvents {

    public static final EventType<PersonaEvent> PERSONA_ACTIVATE = EventType.serverSide();
    public static final EventType<PersonaEvent> PERSONA_DEACTIVATE = EventType.serverSide();
    public static final EventType<JoinWorldServerEvent> SERVER_JOIN_WORLD = EventType.serverSide();

    public static class PersonaEvent extends PlayerEvent<MKPlayerData> {

        private final Persona persona;

        public PersonaEvent(MKPlayerData playerData, Persona persona) {
            super(playerData);
            this.persona = persona;
        }

        public Persona getPersona() {
            return persona;
        }
    }

    public static final class JoinWorldServerEvent extends PlayerEvent<MKServerPlayerData> {

        public JoinWorldServerEvent(MKServerPlayerData playerData) {
            super(playerData);
        }
    }
}
