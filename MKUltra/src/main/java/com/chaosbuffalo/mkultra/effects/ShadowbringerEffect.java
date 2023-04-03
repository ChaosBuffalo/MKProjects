package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ShadowbringerEffect extends MKEffect {

    public static MKEffectBuilder<?> from(LivingEntity source, int duration) {
        return MKUEffects.SHADOWBRINGER.get().builder(source).timed(duration);
    }

    public ShadowbringerEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        targetData.getAbilityExecutor().setCooldown(MKUAbilities.SHADOW_BOLT.getId(), 0);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
