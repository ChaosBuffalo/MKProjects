package com.chaosbuffalo.mkcore.client.rendering.animations;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

public abstract class AdditionalBipedAnimation<T extends LivingEntity> extends AdditionalAnimation<T> {

    public AdditionalBipedAnimation(HumanoidModel<?> model) {
        super(model);
    }

    public HumanoidModel<?> getModel() {
        return (HumanoidModel<?>) super.getModel();
    }
}
