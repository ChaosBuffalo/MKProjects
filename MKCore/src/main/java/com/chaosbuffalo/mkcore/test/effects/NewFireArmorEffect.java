package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.world.effect.MobEffectCategory;

public class NewFireArmorEffect extends MKEffect {

    public NewFireArmorEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
