package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityGrantSource;
import com.chaosbuffalo.mkcore.abilities2.runtime.GrantedAbility;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PassiveAbilityGroup extends AbilityGroup {
    private static final AbilityGrantSource PASSIVE_LOADOUT_SOURCE = new AbilityGrantSource(
            MKCore.makeRL("grant_source.loadout"),
            MKCore.makeRL("loadout_group.passive")
    );

    public PassiveAbilityGroup(Persona persona) {
        super(persona, AbilityGroupId.Passive);
        persona.getSkills().addSkillChangeObserver(this::onSkillChange);
    }

    @Override
    public boolean containsActiveAbilities() {
        return false;
    }

    private void onSkillChange(MKPlayerData playerData, AttributeInstance attribute) {
        Holder<Attribute> skill = attribute.getAttribute();
        getAbilityInfoStream()
                .filter(info -> info.getAbility().getSkillAttributes().contains(skill))
                .forEach(info -> {
                    if (info.getAbility() instanceof MKPassiveAbility passiveAbility) {
                        passiveAbility.deactivate(playerData, info);
                        passiveAbility.activate(playerData, info);
                    }
                });
        reapplyAbility2Passives();
    }

    @Override
    protected ResourceLocation getAbilityDefinitionSlotFamily() {
        return AbilityDatagenKeys.SLOT_FAMILY_PASSIVE;
    }

    @Override
    protected void onAbilityDefinitionAdded(int index, ResourceLocation abilityId) {
        refreshAbility2Passives();
    }

    @Override
    protected void onAbilityDefinitionRemoved(int index, ResourceLocation abilityId) {
        refreshAbility2Passives();
    }

    @Override
    public void onPersonaActivated() {
        super.onPersonaActivated();
        refreshAbility2Passives();
    }

    @Override
    public void onPersonaDeactivated() {
        clearAbility2Passives();
        super.onPersonaDeactivated();
    }

    private void refreshAbility2Passives() {
        MKCore.getAbilityRuntimeService().refreshPassives(
                playerData,
                playerData,
                PASSIVE_LOADOUT_SOURCE,
                collectAbilities2Passives()
        );
    }

    private void clearAbility2Passives() {
        MKCore.getAbilityRuntimeService().refreshPassives(
                playerData,
                playerData,
                PASSIVE_LOADOUT_SOURCE,
                List.of()
        );
    }

    private void reapplyAbility2Passives() {
        clearAbility2Passives();
        refreshAbility2Passives();
    }

    private List<GrantedAbility> collectAbilities2Passives() {
        List<GrantedAbility> desired = new ArrayList<>();
        for (int i = 0; i < getCurrentSlotCount(); i++) {
            ResourceLocation abilityId = getSlot(i);
            if (resolveAbilityDefinition(abilityId) == null) {
                continue;
            }
            desired.add(new GrantedAbility(
                    stablePassiveGrantId(abilityId),
                    abilityId,
                    Map.of(),
                    PASSIVE_LOADOUT_SOURCE
            ));
        }
        return desired;
    }

    private UUID stablePassiveGrantId(ResourceLocation abilityId) {
        String input = "loadout-passive:" + persona.getPersonaId() + ":" + abilityId;
        return UUID.nameUUIDFromBytes(input.getBytes(StandardCharsets.UTF_8));
    }
}
