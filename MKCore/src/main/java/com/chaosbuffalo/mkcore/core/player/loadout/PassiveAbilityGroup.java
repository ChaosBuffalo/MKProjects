package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerEvents;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.UUID;

public class PassiveAbilityGroup extends AbilityGroup {
    private static final UUID EV_ID = UUID.fromString("137dc36b-c68b-4ace-8627-78c4dc1b6b85");

    public PassiveAbilityGroup(Persona persona) {
        super(persona, "passive", AbilityGroupId.Passive);
        persona.subscribe(PlayerEvents.SERVER_JOIN_LEVEL, EV_ID, this::onJoinLevel);
        persona.subscribe(PlayerEvents.SKILL_LEVEL_CHANGE, EV_ID, this::onSkillChange);
    }

    @Override
    public boolean containsActiveAbilities() {
        return false;
    }

    @Override
    protected void onAbilityAdded(MKAbilityInfo abilityInfo) {
        super.onAbilityAdded(abilityInfo);
        activatePassive(abilityInfo);
    }

    @Override
    protected void onAbilityRemoved(MKAbilityInfo abilityInfo) {
        super.onAbilityRemoved(abilityInfo);
        removePassive(abilityInfo);
    }

    private void onJoinLevel(PlayerEvents.JoinLevelServerEvent event) {
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

    private void onSkillChange(PlayerEvents.SkillEvent event) {
        Attribute skill = event.getSkillAttributeInstance().getAttribute();
        getAbilityInfoStream()
                .filter(info -> info.getAbility().getSkillAttributes().contains(skill))
                .forEach(info -> {
                    removePassive(info);
                    activatePassive(info);
                });
    }

    private void activatePassive(MKAbilityInfo info) {
        if (info != null && info.getAbility() instanceof MKPassiveAbility) {
            info.getAbility().executeWithContext(playerData, AbilityContext.selfTarget(playerData, info), info);
        }
    }

    private void activateAllPassives(boolean willBeInWorld) {
        if (playerData.isClientSide())
            return;

        // We come here during deserialization of the active persona, and it tries to apply effects which will crash the client because it's too early
        // Active persona passives should be caught by onJoinWorld
        // Persona switching while in-game should not go inside this branch
        if (willBeInWorld || playerData.getEntity().isAddedToWorld()) {
            getAbilityInfoStream().forEach(this::activatePassive);
        }
    }

    private void removePassive(MKAbilityInfo abilityInfo) {
        if (abilityInfo.getAbility() instanceof MKPassiveAbility passiveAbility) {
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
