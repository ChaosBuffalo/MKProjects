package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class PlayerAbilityExecutor extends AbilityExecutor {
    public static final UUID EV_ID = UUID.fromString("c14c2ba2-4a70-49a3-9999-7e8041d6e687");

    public PlayerAbilityExecutor(MKPlayerData playerData) {
        super(playerData);
        playerData.events().subscribe(PlayerEvents.ABILITY_UNLEARNED, EV_ID, this::onUnlearnAbility);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    private void onUnlearnAbility(PlayerEvents.AbilityUnlearnEvent event) {
        if (event.getAbilityInfo().getAbility() instanceof MKToggleAbility toggleAbility) {
            toggleAbility.removeEffect(entityData);
        }
    }

    public void executeHotBarAbility(AbilityGroupId group, int slot) {
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
        float manaCost = getPlayerData().getStats().getAbilityManaCost(abilityInfo.getAbility());
        getPlayerData().getStats().consumeMana(manaCost);
    }

    public float getCurrentAbilityCooldownPercent(ResourceLocation abilityId, float partialTicks) {
        return getPlayerData().getStats().getTimerPercent(abilityId, partialTicks);
    }

}
