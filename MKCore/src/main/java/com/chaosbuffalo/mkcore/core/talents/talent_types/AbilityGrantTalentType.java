package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.AbilityGrantTalentHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.AbilityGrantTalentNode;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;
import java.util.function.Consumer;

public class AbilityGrantTalentType extends TalentType<AbilityGrantTalentNode> {

    @Override
    public TalentTypeHandler createTypeHandler(Persona persona) {
        return new AbilityGrantTalentHandler(persona);
    }

    @Override
    public MapCodec<AbilityGrantTalentNode> codec() {
        return AbilityGrantTalentNode.MAP_CODEC;
    }

    @Override
    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        super.describeTalent(entityData, record, consumer);

        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return;
        }

        MKAbility ability = abilityNode.getAbility();
        consumer.accept(ability.getAbilityName());
        ability.buildDescription(entityData, AbilityContext.forCaster(entityData, ability), consumer);
    }

    @Override
    public MutableComponent getTalentNodeName(TalentRecord record) {
        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return Component.literal("bad talent type");
        }

        return abilityNode.getAbility().getAbilityName();
    }

    @Override
    public MutableComponent getTypeDisplayName(TalentRecord record) {
        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return Component.literal("bad talent type");
        }

        MKAbility ability = abilityNode.getAbility();
        return switch (ability.getType()) {
            case Basic ->
                    Component.translatableWithFallback("talent_type.mkcore.ability_grant.basic.name", "Basic Ability Talent");
            case Passive ->
                    Component.translatableWithFallback("talent_type.mkcore.ability_grant.passive.name", "Passive Ability Talent");
            case Ultimate ->
                    Component.translatableWithFallback("talent_type.mkcore.ability_grant.ultimate.name", "Ultimate Ability Talent");
            default -> Component.literal("%s Ability Talent".formatted(ability.getType()));
        };
    }

    @Override
    public MutableComponent getTalentDescription(TalentRecord record) {
        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return Component.literal("bad talent type");
        }

        return Component.translatableWithFallback("talent_type.mkcore.ability_grant.description",
                "Grants the %s %s ability",
                abilityNode.getAbility().getAbilityName(),
                Component.literal(abilityNode.getAbility().getType().name().toLowerCase(Locale.ROOT)));
    }
}
