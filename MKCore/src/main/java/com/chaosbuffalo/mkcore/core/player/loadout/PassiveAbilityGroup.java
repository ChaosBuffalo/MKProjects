package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;

import javax.annotation.Nonnull;


public class PassiveAbilityGroup extends AbilityGroup {

    public PassiveAbilityGroup(MKPlayerData playerData) {
        super(playerData, "passive", AbilityGroupId.Passive);
    }

    @Override
    public boolean containsActiveAbilities() {
        return false;
    }

    @Override
    protected void onAbilityAdded(@Nonnull MKAbilityInfo abilityInfo) {
        super.onAbilityAdded(abilityInfo);
        activatePassive(abilityInfo);
    }

    @Override
    protected void onAbilityRemoved(@Nonnull MKAbilityInfo abilityInfo) {
        super.onAbilityRemoved(abilityInfo);
        removePassive(abilityInfo);
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

    public void onSkillUpdate(Attribute skill) {
        getAbilities().forEach(id -> {
            MKAbilityInfo info = playerData.getAbilities().getKnownAbility(id);
            if (info != null && info.getAbility().getSkillAttributes().contains(skill)) {
                removePassive(info);
                activatePassive(info);
            }
        });
    }

    private void activatePassive(MKAbilityInfo info) {
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
            getAbilityInfoStream().forEach(this::activatePassive);
        }
    }

    private void removePassive(MKAbilityInfo abilityInfo) {
        MKAbility ability = abilityInfo.getAbility();
        if (ability instanceof MKPassiveAbility passiveAbility) {
            MKEffect passiveEffect = passiveAbility.getPassiveEffect();
            if (playerData.getEffects().isEffectActive(passiveEffect)) {
                playerData.getEffects().removeEffect(passiveEffect);
            }
        }
    }

    private void removeAllPassiveTalents() {
        getAbilityInfoStream().forEach(this::removePassive);
    }
}
