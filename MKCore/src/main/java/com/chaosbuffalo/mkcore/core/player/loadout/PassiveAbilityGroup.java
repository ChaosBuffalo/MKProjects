package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerEvents;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.UUID;

public class PassiveAbilityGroup extends AbilityGroup {
    private static final UUID EV_ID = UUID.fromString("137dc36b-c68b-4ace-8627-78c4dc1b6b85");

    public PassiveAbilityGroup(MKPlayerData playerData) {
        super(playerData, "passive", AbilityGroupId.Passive);
        playerData.events().subscribe(PlayerEvents.SERVER_JOIN_WORLD, EV_ID, this::onJoinWorld);
    }

    @Override
    public boolean containsActiveAbilities() {
        return false;
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

    private void onJoinWorld(PlayerEvents.JoinWorldServerEvent event) {
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
                removePassive(id);
                activatePassive(id);
            }
        });
    }

    private void activatePassive(ResourceLocation abilityId) {
        MKAbilityInfo info = playerData.getAbilities().getKnownAbility(abilityId);
        if (info != null && info.getAbility() instanceof MKPassiveAbility) {
            info.getAbility().executeWithContext(playerData, AbilityContext.selfTarget(playerData), info);
        }
    }

    private void activateAllPassives(boolean willBeInWorld) {
        if (playerData.isClientSide())
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
        if (ability instanceof MKPassiveAbility passiveAbility) {
            MKEffect passiveEffect = passiveAbility.getPassiveEffect();
            if (playerData.getEffects().isEffectActive(passiveEffect)) {
                playerData.getEffects().removeEffect(passiveEffect);
            }
        }
    }

    private void removeAllPassiveTalents() {
        getAbilities().forEach(this::removePassive);
    }

}
