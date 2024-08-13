package com.chaosbuffalo.mkcore.client.rendering.animations;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class BipedStunAnimation<T extends LivingEntity> extends AdditionalBipedAnimation<T> {

    public BipedStunAnimation(HumanoidModel<?> model) {
        super(model);
    }

    @Override
    public void apply(T entity) {
        HumanoidModel<?> model = getModel();
        MKCore.getEntityData(entity).ifPresent(mkEntityData -> {
            long time = entity.getLevel().getGameTime();
            float progress = (time % 100) / 100f;
            float armZ = Mth.sin((float) (Math.PI / 2.0f + progress * (float) Math.PI / 2.f)) * 1.0f * (float) Math.PI / 4.0f;
            float angle = (float) ((float) (0) + Mth.sin((float) (progress * Math.PI)) * (Math.PI / 8.0f));
            model.rightArm.xRot = 0.0F;
            model.leftArm.xRot = 0.0F;
            model.rightArm.zRot = 0.0f;
            model.leftArm.zRot = 0.0f;
            model.rightArm.yRot = 0.0f;
            model.leftArm.yRot = 0.0f;
            model.body.xRot = .5f;
            model.body.y = 3.2f;
            model.leftArm.y = 5.2F;
            model.rightArm.y = 5.2F;
            model.rightLeg.z = 4.0F;
            model.leftLeg.z = 4.0F;
            model.rightLeg.y = 12.2F;
            model.leftLeg.y = 12.2F;
            model.head.y = 4.2F;
            model.head.zRot = angle;
        });
    }
}
