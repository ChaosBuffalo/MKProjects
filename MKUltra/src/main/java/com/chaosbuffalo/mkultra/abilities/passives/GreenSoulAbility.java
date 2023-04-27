package com.chaosbuffalo.mkultra.abilities.passives;

import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkultra.init.MKUEffects;

public class GreenSoulAbility extends MKPassiveAbility {

    public GreenSoulAbility() {
        super();
        addSkillAttribute(MKAttributes.RESTORATION);
    }

    @Override
    public MKEffect getPassiveEffect() {
        return MKUEffects.GREEN_SOUL.get();
    }
}
