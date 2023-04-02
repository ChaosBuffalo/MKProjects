package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.resources.ResourceLocation;

public class PlayerAbilityExecutor extends AbilityExecutor {

    public PlayerAbilityExecutor(MKPlayerData playerData) {
        super(playerData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
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
    protected void consumeResource(MKAbility ability) {
        float manaCost = getPlayerData().getStats().getAbilityManaCost(ability);
        getPlayerData().getStats().consumeMana(manaCost);
    }

    public void onPersonaActivated() {
        rebuildActiveToggleMap();
    }

    public void onPersonaDeactivated() {
        deactivateCurrentToggleAbilities();
    }

    public float getCurrentAbilityCooldownPercent(ResourceLocation abilityId, float partialTicks) {
        return getPlayerData().getStats().getTimerPercent(abilityId, partialTicks);
    }

    private void deactivateCurrentToggleAbilities() {
        PlayerAbilityLoadout abilityLoadout = getPlayerData().getLoadout();
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilityGroupId.Basic));
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilityGroupId.Ultimate));
        deactivateCurrentToggleAbilities(abilityLoadout.getAbilityGroup(AbilityGroupId.Item));
    }

    private void deactivateCurrentToggleAbilities(AbilityGroup group) {
        for (int i = 0; i < group.getMaximumSlotCount(); i++) {
            ResourceLocation abilityId = group.getSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbility) {
                MKToggleAbility toggle = (MKToggleAbility) ability;
                toggle.removeEffect(entityData.getEntity(), entityData);
            }
        }
    }

    private void rebuildActiveToggleMap() {
        PlayerAbilityLoadout abilityLoadout = getPlayerData().getLoadout();
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilityGroupId.Basic));
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilityGroupId.Ultimate));
        rebuildActiveToggleMap(abilityLoadout.getAbilityGroup(AbilityGroupId.Item));
    }

    private void rebuildActiveToggleMap(AbilityGroup group) {
        // Inspect the player's action bar and see if there are any toggle abilities slotted.
        // If there are, and the corresponding toggle effect is active on the player, set the toggle exclusive group
        for (int i = 0; i < group.getMaximumSlotCount(); i++) {
            ResourceLocation abilityId = group.getSlot(i);
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability instanceof MKToggleAbility) {
                MKToggleAbility toggle = (MKToggleAbility) ability;
                if (toggle.isEffectActive(entityData))
                    setToggleGroupAbility(toggle.getToggleGroupId(), toggle);
            }
        }
    }
}
