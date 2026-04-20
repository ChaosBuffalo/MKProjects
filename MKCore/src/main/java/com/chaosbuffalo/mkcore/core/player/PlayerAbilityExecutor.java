package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.resources.ResourceLocation;

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

        MKCore.getAbilityRuntimeService().executeLoadoutAbility(getPlayerData(), getPlayerData(), group, abilityId);
    }

    public boolean clientSimulateAbility(AbilityGroupId executingGroup, int slot) {
        AbilityGroup loadoutGroup = getPlayerData().getLoadout().getAbilityGroup(executingGroup);
        ResourceLocation abilityId = loadoutGroup.getSlot(slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            return false;
        }

        MKAbilityInfo info = loadoutGroup.getAbilityInfo(slot);
        if (info == null) {
            var ability = loadoutGroup.getExecutionAbilityReference(slot);
            if (ability == null) {
                return false;
            }
            if (MKCore.getAbilityRuntimeService().getLoadoutCooldownTicks(getPlayerData(), ability,
                    loadoutGroup.getExecutionSourceId(slot)) > 0) {
                return false;
            }
            return MKCore.getAbilityRuntimeService().canExecuteLoadoutAbility(executingGroup, abilityId);
        }

        MKAbility ability = info.getAbility();
        if (ability.meetsCastingRequirements(entityData, info)) {
            AbilityTargetSelector selector = ability.getTargetSelector();
            AbilityContext context = selector.createContext(entityData, info);
            if (context != null) {
                return selector.validateContext(entityData, context);
            } else {
                MKCore.LOGGER.warn("CLIENT Entity {} tried to execute ability {} with a null context!", entityData.getEntity(), ability.getAbilityId());
            }
        }
        return false;
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
