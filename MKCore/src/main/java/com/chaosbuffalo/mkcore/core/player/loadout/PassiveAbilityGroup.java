package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.resources.ResourceLocation;


public class PassiveAbilityGroup extends AbilityGroup {

    public PassiveAbilityGroup(MKPlayerData playerData) {
        super(playerData, "passive", AbilityGroupId.Passive);
    }

    @Override
    protected void onAbilityAdded(ResourceLocation abilityId) {
        super.onAbilityAdded(abilityId);
        activatePassive(abilityId);
    }

    @Override
    protected void onAbilityRemoved(ResourceLocation abilityId) {
        super.onAbilityRemoved(abilityId);
        removePassive(abilityId);
    }

    @Override
    public void onJoinWorld() {
        super.onJoinWorld();
        activateAllPassives(true);
    }

    @Override
    public void onPersonaActivated() {
        super.onPersonaActivated();
        activateAllPassives(false);
    }

    @Override
    public void onPersonaDeactivated() {
        super.onPersonaDeactivated();
        removeAllPassiveTalents();
    }

    private void activatePassive(ResourceLocation abilityId) {
        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(abilityId);
        if (info != null && info.getAbility() instanceof MKPassiveAbility) {
            info.getAbility().executeWithContext(playerData, AbilityContext.selfTarget(playerData), info);
        }
    }

    private void activateAllPassives(boolean willBeInWorld) {
        if (!playerData.isServerSide())
            return;

        // We come here during deserialization of the active persona, and it tries to apply effects which will crash the client because it's too early
        // Active persona passives should be caught by onJoinWorld
        // Persona switching while in-game should not go inside this branch
        if (willBeInWorld || playerData.getEntity().isAddedToWorld()) {
            getAbilities().forEach(this::activatePassive);
        }
    }

    private void removePassive(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability instanceof MKPassiveAbility) {
            MKEffect passiveEffect = ((MKPassiveAbility) ability).getPassiveEffect();
            if (playerData.getEffects().isEffectActive(passiveEffect)) {
                playerData.getEffects().removeEffect(passiveEffect);
            }
        }
    }

    private void removeAllPassiveTalents() {
        getAbilities().forEach(this::removePassive);
    }

}
