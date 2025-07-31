package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
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

    public boolean clientSimulateAbility(AbilityGroupId executingGroup, int slot) {
        MKAbilityInfo info = getPlayerData().getLoadout().getAbilityGroup(executingGroup).getAbilityInfo(slot);
        if (info == null) {
            return false;
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
