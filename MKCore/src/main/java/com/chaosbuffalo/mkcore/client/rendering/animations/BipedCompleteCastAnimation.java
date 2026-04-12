package com.chaosbuffalo.mkcore.client.rendering.animations;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public abstract class BipedCompleteCastAnimation<T extends LivingEntity> extends AdditionalBipedAnimation<T> {
    private static final float ANIM_TIME = 15.0f;

    public BipedCompleteCastAnimation(HumanoidModel<?> model) {
        super(model);
    }

    protected abstract int getCastAnimTimer(T entity);

    @Override
    public void apply(T entity) {
        HumanoidModel<?> model = getModel();
        int castTicks = getCastAnimTimer(entity);
        float progress = ANIM_TIME - castTicks / ANIM_TIME;
        float armZ = Mth.cos((float) (Math.PI / 2.0f + progress * (float) Math.PI)) * (float) Math.PI / 2.0f;
        if (!hasVisualMeleeAttackSequenceArm(entity, HumanoidArm.RIGHT)) {
            model.rightArm.yRot = 0.0F;
            model.rightArm.zRot = -armZ;
            model.rightArm.xRot = 0.0F;
        }
        if (!hasVisualMeleeAttackSequenceArm(entity, HumanoidArm.LEFT)) {
            model.leftArm.yRot = 0.0F;
            model.leftArm.zRot = armZ;
            model.leftArm.xRot = 0.0F;
        }

    }
}
