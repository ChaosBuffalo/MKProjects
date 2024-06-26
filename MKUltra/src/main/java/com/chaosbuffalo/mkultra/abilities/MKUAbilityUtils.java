package com.chaosbuffalo.mkultra.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.entity.LivingEntity;

public class MKUAbilityUtils {

    public static boolean isBurning(LivingEntity entity) {
        return MKCore.getEntityData(entity).map(MKUAbilityUtils::isBurning).orElse(false);
    }

    public static boolean isBurning(IMKEntityData entityData) {
        return entityData.getEffects().isEffectActive(MKUEffects.BURN.get());
    }
}
