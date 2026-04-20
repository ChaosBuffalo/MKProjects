package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.description.AbilityDefinitionDescriptions;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.core.AbilityType;
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
import net.minecraft.resources.ResourceLocation;

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
        if (ability != null) {
            consumer.accept(ability.getAbilityName());
            ability.buildDescription(entityData, AbilityContext.forCaster(entityData, ability), consumer);
            return;
        }

        AbilityDefinitionData definition = abilityNode.getAbilityDefinition();
        if (definition != null) {
            consumer.accept(Component.literal(definition.presentation().name()));
            PatchedAbilityDefinition patched = com.chaosbuffalo.mkcore.MKCore.getAbilityDefinitionService()
                    .getResolver().resolvePatched(definition.id());
            if (patched != null) {
                AbilityDefinitionDescriptions.buildDescription(null, patched, consumer);
            } else if (!definition.presentation().description().isBlank()) {
                consumer.accept(Component.literal(definition.presentation().description()));
            }
            return;
        }

        consumer.accept(Component.literal(abilityNode.getAbilityId().toString()));
    }

    @Override
    public MutableComponent getTalentNodeName(TalentRecord record) {
        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return Component.literal("bad talent type");
        }

        MKAbility ability = abilityNode.getAbility();
        if (ability != null) {
            return ability.getAbilityName();
        }

        AbilityDefinitionData definition = abilityNode.getAbilityDefinition();
        if (definition != null) {
            return Component.literal(definition.presentation().name());
        }

        return Component.literal(abilityNode.getAbilityId().toString());
    }

    @Override
    public MutableComponent getTypeDisplayName(TalentRecord record) {
        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return Component.literal("bad talent type");
        }

        AbilityType abilityType = resolveAbilityType(abilityNode);
        if (abilityType == null) {
            return Component.literal("Ability Talent");
        }

        return switch (abilityType) {
            case Basic ->
                    Component.translatableWithFallback("talent_type.mkcore.ability_grant.basic.name", "Basic Ability Talent");
            case Passive ->
                    Component.translatableWithFallback("talent_type.mkcore.ability_grant.passive.name", "Passive Ability Talent");
            case Ultimate ->
                    Component.translatableWithFallback("talent_type.mkcore.ability_grant.ultimate.name", "Ultimate Ability Talent");
            default -> Component.literal("Ability Talent");
        };
    }

    @Override
    public MutableComponent getTalentDescription(TalentRecord record) {
        if (!(record.getNode() instanceof AbilityGrantTalentNode abilityNode)) {
            return Component.literal("bad talent type");
        }

        MutableComponent displayName = getTalentNodeName(record);
        AbilityType abilityType = resolveAbilityType(abilityNode);
        if (abilityType == null) {
            return Component.literal("Grants the %s ability".formatted(displayName.getString()));
        }

        return Component.translatableWithFallback("talent_type.mkcore.ability_grant.description",
                "Grants the %s %s ability",
                displayName,
                Component.literal(abilityType.name().toLowerCase(Locale.ROOT)));
    }

    private AbilityType resolveAbilityType(AbilityGrantTalentNode abilityNode) {
        MKAbility ability = abilityNode.getAbility();
        if (ability != null) {
            return ability.getType();
        }

        AbilityDefinitionData definition = abilityNode.getAbilityDefinition();
        if (definition == null) {
            return null;
        }
        return resolveAbilityType(definition.slotFamily());
    }

    private AbilityType resolveAbilityType(ResourceLocation slotFamily) {
        if (slotFamily.equals(AbilityDatagenKeys.SLOT_FAMILY_BASIC)) {
            return AbilityType.Basic;
        }
        if (slotFamily.equals(AbilityDatagenKeys.SLOT_FAMILY_PASSIVE)) {
            return AbilityType.Passive;
        }
        if (slotFamily.equals(AbilityDatagenKeys.SLOT_FAMILY_ULTIMATE)) {
            return AbilityType.Ultimate;
        }
        return null;
    }
}
