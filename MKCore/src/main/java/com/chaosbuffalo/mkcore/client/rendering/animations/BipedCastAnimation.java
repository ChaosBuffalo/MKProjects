package com.chaosbuffalo.mkcore.client.rendering.animations;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class BipedCastAnimation<T extends LivingEntity> extends AdditionalBipedAnimation<T> {

    public BipedCastAnimation(HumanoidModel<?> model) {
        super(model);
    }

    @Override
    public void apply(T entity) {
        HumanoidModel<?> model = getModel();
        MKCore.getEntityData(entity).ifPresent(mkEntityData -> {
            if (mkEntityData.getAbilityExecutor().isCasting()) {
                int castTicks = mkEntityData.getAbilityExecutor().getCastTicks();
                float castProgress = castTicks / 20.0f;
                float armZ = Mth.sin((float) (Math.PI / 2.0f + castProgress * (float) Math.PI / 2.f)) * 1.0f * (float) Math.PI / 4.0f;
                float angle = (float) ((float) (Math.PI / 2.0f) + Mth.sin((float) (castProgress * Math.PI)) * (Math.PI / 8.0f));
                if (!hasVisualMeleeAttackSequenceArm(entity, HumanoidArm.RIGHT)) {
                    model.rightArm.yRot = 0.0F;
                    model.rightArm.zRot = -armZ;
                    model.rightArm.xRot = -angle;
                }
                if (!hasVisualMeleeAttackSequenceArm(entity, HumanoidArm.LEFT)) {
                    model.leftArm.yRot = 0.0F;
                    model.leftArm.zRot = armZ;
                    model.leftArm.xRot = -angle;
                }
            }
        });
    }
}
