package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record AbilityDisplayEntry(
        ResourceLocation abilityId,
        Component displayName,
        @Nullable AbilityType abilityType,
        boolean definitionBacked
) {
    public static AbilityDisplayEntry resolve(ResourceLocation abilityId) {
        MKAbility legacyAbility = MKCoreRegistry.getAbility(abilityId);
        if (legacyAbility != null) {
            return new AbilityDisplayEntry(
                    abilityId,
                    legacyAbility.getAbilityName(),
                    legacyAbility.getType(),
                    false
            );
        }

        AbilityDefinitionData definition = MKCore.getAbilityDefinitionService().getDefinition(abilityId);
        if (definition != null) {
            return new AbilityDisplayEntry(
                    abilityId,
                    Component.literal(definition.presentation().name()),
                    resolveAbilityType(definition.slotFamily()),
                    true
            );
        }

        return new AbilityDisplayEntry(abilityId, Component.literal(abilityId.toString()), null, false);
    }

    private static @Nullable AbilityType resolveAbilityType(ResourceLocation slotFamily) {
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
