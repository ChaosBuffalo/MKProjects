package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import net.minecraft.world.effect.MobEffectCategory;

public abstract class OnStackEffect extends MKEffect {

    public OnStackEffect(MobEffectCategory Category) {
        super(Category);
    }

    protected abstract void Detonate(IMKEntityData targetData, MKActiveEffect instance);

    @Override
    public boolean onInstanceUpdated(IMKEntityData targetData, MKActiveEffect instance) {
        super.onInstanceUpdated(targetData, instance);
        if (instance.getStackCount() == instance.getState().getMaxStacks()) {
            Detonate(targetData, instance);
            return true;
        }
        return false;
    }
}