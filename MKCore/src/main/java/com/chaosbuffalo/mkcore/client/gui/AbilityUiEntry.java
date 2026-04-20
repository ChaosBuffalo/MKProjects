package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.description.AbilityDefinitionDescriptions;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.PlayerKnownAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class AbilityUiEntry {
    private final ResourceLocation abilityId;
    private final AbilityType abilityType;
    private final Component displayName;
    private final Component description;
    @Nullable
    private final ResourceLocation icon;
    @Nullable
    private final MKAbility legacyAbility;
    @Nullable
    private final AbilityDefinitionData definition;

    private AbilityUiEntry(ResourceLocation abilityId,
                           AbilityType abilityType,
                           Component displayName,
                           Component description,
                           @Nullable ResourceLocation icon,
                           @Nullable MKAbility legacyAbility,
                           @Nullable AbilityDefinitionData definition) {
        this.abilityId = abilityId;
        this.abilityType = abilityType;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.legacyAbility = legacyAbility;
        this.definition = definition;
    }

    public static AbilityUiEntry fromAbility(MKAbility ability) {
        return new AbilityUiEntry(
                ability.getAbilityId(),
                ability.getType(),
                ability.getAbilityName(),
                Component.empty(),
                ability.getAbilityIcon(),
                ability,
                null
        );
    }

    public static @Nullable AbilityUiEntry fromDefinition(AbilityDefinitionData definition) {
        AbilityType abilityType = resolveAbilityType(definition.slotFamily());
        if (abilityType == null) {
            return null;
        }

        return new AbilityUiEntry(
                definition.id(),
                abilityType,
                Component.literal(definition.presentation().name()),
                Component.literal(definition.presentation().description()),
                definition.presentation().icon(),
                null,
                definition
        );
    }

    public static @Nullable AbilityUiEntry resolve(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            return fromAbility(ability);
        }

        AbilityDefinitionData definition = MKCore.getAbilityDefinitionService().getDefinition(abilityId);
        if (definition == null) {
            return null;
        }
        return fromDefinition(definition);
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public AbilityType getAbilityType() {
        return abilityType;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public Component getDescription() {
        return description;
    }

    public @Nullable ResourceLocation getIcon() {
        return icon;
    }

    public ResourceLocation getIconOrFallback() {
        return icon != null ? icon : MKAbility.POOL_SLOT_ICON;
    }

    public @Nullable MKAbility getLegacyAbility() {
        return legacyAbility;
    }

    public boolean isLegacyAbility() {
        return legacyAbility != null;
    }

    public void buildDescription(MKPlayerData playerData, Consumer<Component> consumer) {
        if (legacyAbility != null) {
            legacyAbility.buildDescription(playerData, AbilityContext.forCaster(playerData, legacyAbility), consumer);
            return;
        }

        PatchedAbilityDefinition patchedDefinition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);
        if (patchedDefinition != null) {
            PlayerKnownAbility knownAbility = playerData.getAbilities().getKnownAbility(abilityId);
            AbilityDefinitionDescriptions.buildDescription(knownAbility, patchedDefinition, consumer);
            return;
        }

        consumer.accept(description.copy().withStyle(ChatFormatting.GRAY));
        if (definition == null) {
            return;
        }

        consumer.accept(Component.literal("Slot: " + abilityType.name()).withStyle(ChatFormatting.DARK_GRAY));
        if (!definition.schools().isEmpty()) {
            consumer.accept(Component.literal("Schools: " + formatIds(definition.schools())).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!definition.tags().isEmpty()) {
            consumer.accept(Component.literal("Tags: " + formatIds(definition.tags())).withStyle(ChatFormatting.DARK_GRAY));
        }
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

    private static String formatIds(Collection<ResourceLocation> ids) {
        return ids.stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbilityUiEntry that)) return false;
        return abilityId.equals(that.abilityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(abilityId);
    }
}
