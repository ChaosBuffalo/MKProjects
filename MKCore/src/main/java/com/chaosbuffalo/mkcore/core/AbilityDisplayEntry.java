package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public record AbilityDisplayEntry(
        ResourceLocation abilityId,
        Component displayName,
        @Nullable AbilityType abilityType,
        @Nullable ResourceLocation icon,
        boolean definitionBacked
) {
    public static AbilityDisplayEntry resolve(ResourceLocation abilityId) {
        MKAbility legacyAbility = MKCoreRegistry.getAbility(abilityId);
        if (legacyAbility != null) {
            return new AbilityDisplayEntry(
                    abilityId,
                    legacyAbility.getAbilityName(),
                    legacyAbility.getType(),
                    legacyAbility.getAbilityIcon(),
                    false
            );
        }

        AbilityDefinitionData definition = MKCore.getAbilityDefinitionService().getDefinition(abilityId);
        if (definition != null) {
            return new AbilityDisplayEntry(
                    abilityId,
                    Component.literal(definition.presentation().name()),
                    resolveAbilityType(definition.slotFamily()),
                    definition.presentation().icon(),
                    true
            );
        }

        return new AbilityDisplayEntry(abilityId, Component.literal(abilityId.toString()), null, null, false);
    }

    public boolean isLoadoutAbility() {
        return abilityType != null;
    }

    public boolean fitsLoadoutGroup(AbilityGroupId groupId) {
        return abilityType != null && groupId.fitsAbilityType(abilityType);
    }

    public static @Nullable AbilityGroupId resolveAbilityGroup(ResourceLocation slotFamily) {
        AbilityType abilityType = resolveAbilityType(slotFamily);
        if (abilityType == null) {
            return null;
        }

        return switch (abilityType) {
            case Basic -> AbilityGroupId.Basic;
            case Passive -> AbilityGroupId.Passive;
            case Ultimate -> AbilityGroupId.Ultimate;
            case Npc, Structure -> null;
        };
    }

    public static @Nullable AbilityType resolveAbilityType(ResourceLocation slotFamily) {
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
