package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.events.EventType;
import com.chaosbuffalo.mkcore.core.player.events.PlayerEvent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class PlayerEvents {

    public static final EventType<PersonaEvent> PERSONA_ACTIVATE = EventType.serverSide();
    public static final EventType<PersonaEvent> PERSONA_DEACTIVATE = EventType.serverSide();
    public static final EventType<JoinWorldServerEvent> SERVER_JOIN_WORLD = EventType.serverSide();
    public static final EventType<AbilityLearnEvent> ABILITY_LEARNED = EventType.serverSide();
    public static final EventType<AbilityUnlearnEvent> ABILITY_UNLEARNED = EventType.serverSide();
    public static final EventType<SkillEvent> SKILL_LEVEL_CHANGE = EventType.serverSide();

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

    public static class SkillEvent extends PlayerEvent<MKPlayerData> {
        private final AttributeInstance attributeInstance;

        public SkillEvent(MKPlayerData playerData, AttributeInstance attributeInstance) {
            super(playerData);
            this.attributeInstance = attributeInstance;
        }

        public AttributeInstance getSkillAttributeInstance() {
            return attributeInstance;
        }
    }

    public static class AbilityLearnEvent extends PlayerEvent<MKPlayerData> {

        private final MKAbilityInfo abilityInfo;
        private final AbilitySource source;

        public AbilityLearnEvent(MKPlayerData playerData, MKAbilityInfo abilityInfo, AbilitySource source) {
            super(playerData);
            this.abilityInfo = abilityInfo;
            this.source = source;
        }

        public MKAbilityInfo getAbilityInfo() {
            return abilityInfo;
        }

        public AbilitySource getSource() {
            return source;
        }
    }

    public static class AbilityUnlearnEvent extends PlayerEvent<MKPlayerData> {

        private final MKAbilityInfo abilityInfo;

        public AbilityUnlearnEvent(MKPlayerData playerData, MKAbilityInfo abilityInfo) {
            super(playerData);
            this.abilityInfo = abilityInfo;
        }

        public MKAbilityInfo getAbilityInfo() {
            return abilityInfo;
        }
    }
}
