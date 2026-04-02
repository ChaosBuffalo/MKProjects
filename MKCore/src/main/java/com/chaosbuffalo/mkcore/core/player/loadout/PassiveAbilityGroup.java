package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class PassiveAbilityGroup extends AbilityGroup {

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
    }
}
