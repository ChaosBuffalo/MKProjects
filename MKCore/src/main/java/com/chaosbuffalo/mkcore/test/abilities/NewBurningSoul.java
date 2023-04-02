package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.test.MKTestEffects;

public class NewBurningSoul extends MKPassiveAbility {

    public NewBurningSoul() {
        super();
    }

    @Override
    public MKEffect getPassiveEffect() {
        return MKTestEffects.BURNING_SOUL.get();
    }
}
