package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.UUID;

public class PassiveAbilityGroup extends AbilityGroup {
    private static final UUID EV_ID = UUID.fromString("137dc36b-c68b-4ace-8627-78c4dc1b6b85");

    public PassiveAbilityGroup(Persona persona) {
        super(persona, "passive", AbilityGroupId.Passive);
        persona.subscribe(PlayerEvents.SKILL_LEVEL_CHANGE, EV_ID, this::onSkillChange);
    }

    @Override
    public boolean containsActiveAbilities() {
        return false;
    }

    private void onSkillChange(PlayerEvents.SkillEvent event) {
        Holder<Attribute> skill = event.getSkillAttributeInstance().getAttribute();
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
