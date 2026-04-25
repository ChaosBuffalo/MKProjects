package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities2.runtime.FailureReason;
import com.chaosbuffalo.mkcore.core.AbilityDisplayEntry;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PlayerAbilityExecutor extends AbilityExecutor {

    public PlayerAbilityExecutor(MKPlayerData playerData) {
        super(playerData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    public void executeLoadoutAbility(AbilityGroupId group, int slot) {
        getPlayerData().getLoadout().getAbilityGroup(group).executeSlot(slot);
    }

    public void executeLoadoutAbility(AbilityGroupId group, ResourceLocation abilityId) {
        MKAbilityInfo info = getPlayerData().getAbilities().getAbilityInfo(abilityId);
        if (info != null) {
            executeAbilityInfoWithContext(info, null);
            return;
        }

        var result = MKCore.getAbilityRuntimeService().executeLoadoutAbility(getPlayerData(), getPlayerData(), group, abilityId);
        if (!result.started()) {
            showLoadoutFailure(abilityId, result.failureReason());
        }
    }

    public boolean clientSimulateAbility(AbilityGroupId executingGroup, int slot) {
        ResourceLocation abilityId = getPlayerData().getLoadout().getAbilityGroup(executingGroup).getSlot(slot);
        return !abilityId.equals(MKCoreRegistry.INVALID_ABILITY)
                && previewLoadoutAbilityFailure(executingGroup, slot) == null;
    }

    public @javax.annotation.Nullable FailureReason previewLoadoutAbilityFailure(AbilityGroupId executingGroup, int slot) {
        AbilityGroup loadoutGroup = getPlayerData().getLoadout().getAbilityGroup(executingGroup);
        ResourceLocation abilityId = loadoutGroup.getSlot(slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            return null;
        }

        MKAbilityInfo info = loadoutGroup.getAbilityInfo(slot);
        if (info == null) {
            var ability = loadoutGroup.getExecutionAbilityReference(slot);
            if (ability == null) {
                return FailureReason.UNKNOWN_ABILITY;
            }
            return MKCore.getAbilityRuntimeService().previewLoadoutAbilityFailure(
                    getPlayerData(),
                    getPlayerData(),
                    executingGroup,
                    ability,
                    loadoutGroup.getExecutionSourceId(slot)
            );
        }

        if (isCasting() || entityData.getEntity().isBlocking() || MKCore.getAbilityRuntimeService().hasPendingActivation(entityData)) {
            return FailureReason.BUSY;
        }
        if (isOnGlobalCooldown() || getCurrentAbilityCooldown(info.getId()) > 0) {
            return FailureReason.ON_COOLDOWN;
        }
        if (getAbilityManaCost(info) > getPlayerData().getStats().getMana()) {
            return FailureReason.NOT_ENOUGH_RESOURCE;
        }

        MKAbility ability = info.getAbility();
        AbilityTargetSelector selector = ability.getTargetSelector();
        AbilityContext context = selector.createContext(entityData, info);
        if (context != null) {
            return selector.validateContext(entityData, context) ? null : FailureReason.INVALID_TARGETS;
        }
        MKCore.LOGGER.warn("CLIENT Entity {} tried to preview ability {} with a null context!", entityData.getEntity(), ability.getAbilityId());
        return FailureReason.INVALID_TARGETS;
    }

    public void showLoadoutFailure(ResourceLocation abilityId, @javax.annotation.Nullable FailureReason failureReason) {
        Player player = getPlayerData().getEntity();
        Component message = buildAbilityFeedbackMessage(abilityId, failureReason);
        if (message != null) {
            player.displayClientMessage(message, true);
        }
    }

    public void showStatusMessage(Component message) {
        getPlayerData().getEntity().displayClientMessage(message, true);
    }

    public @javax.annotation.Nullable Component buildAbilityFeedbackMessage(ResourceLocation abilityId,
                                                                           @javax.annotation.Nullable FailureReason failureReason) {
        if (failureReason == null) {
            return null;
        }

        Component abilityName = AbilityDisplayEntry.resolve(abilityId).displayName();
        return switch (failureReason) {
            case INVALID_TARGETS -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.invalid_target",
                    "No valid target for %s",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case NOT_ENOUGH_RESOURCE -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.not_enough_resource",
                    "Not enough mana for %s",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case ON_COOLDOWN -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.on_cooldown",
                    "%s is not ready yet",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case BUSY -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.busy",
                    "You are already casting"
            ).withStyle(ChatFormatting.RED);
            case TARGET_LOST -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.target_lost",
                    "%s lost its target",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted",
                    "%s was interrupted",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_MOVE -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted_move",
                    "%s was interrupted by movement",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_DAMAGE -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted_damage",
                    "%s was interrupted by damage",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_BLOCK -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted_block",
                    "%s was interrupted by blocking",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_JUMP -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted_jump",
                    "%s was interrupted by jumping",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_STUN -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted_stun",
                    "%s was interrupted by stun",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_DEATH -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.interrupted_death",
                    "%s was interrupted by death",
                    abilityName
            ).withStyle(ChatFormatting.RED);
            case INTERRUPTED_BY_LOGOUT, INTERRUPTED_BY_UNLOAD -> null;
            default -> Component.translatableWithFallback(
                    "mkcore.ability.feedback.unavailable",
                    "%s cannot be used right now",
                    abilityName
            ).withStyle(ChatFormatting.RED);
        };
    }

    public float getCurrentLoadoutAbilityCooldownPercent(AbilityGroup loadoutGroup, int slot, float partialTicks) {
        MKAbilityInfo info = loadoutGroup.getAbilityInfo(slot);
        if (info != null) {
            return getCurrentAbilityCooldownPercent(info.getId(), partialTicks);
        }

        var ability = loadoutGroup.getExecutionAbilityReference(slot);
        if (ability == null) {
            return 0.0f;
        }
        return MKCore.getAbilityRuntimeService().getLoadoutCooldownPercent(
                getPlayerData(),
                ability,
                loadoutGroup.getExecutionSourceId(slot),
                partialTicks
        );
    }

    @Override
    protected void consumeResource(MKAbilityInfo abilityInfo) {
        float manaCost = getAbilityManaCost(abilityInfo);
        getPlayerData().getStats().consumeMana(manaCost);
    }

    @Override
    public float getAbilityManaCost(MKAbilityInfo abilityInfo) {
        if (getPlayerData().getEntity().isCreative())
            return 0f;
        return super.getAbilityManaCost(abilityInfo);
    }

    @Override
    public int getAbilityCooldown(MKAbility ability) {
        if (getPlayerData().getEntity().isCreative())
            return 0;
        return super.getAbilityCooldown(ability);
    }

    public float getCurrentAbilityCooldownPercent(ResourceLocation abilityId, float partialTicks) {
        return getPlayerData().getStats().getTimerPercent(abilityId, partialTicks);
    }

}
