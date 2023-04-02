package com.chaosbuffalo.mknpc.client.render.animations;

import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCompleteCastAnimation;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.HumanoidModel;

public class MKEntityCompleteCastAnimation extends BipedCompleteCastAnimation<MKEntity> {
    public MKEntityCompleteCastAnimation(HumanoidModel<?> model) {
        super(model);
    }

    @Override
    protected int getCastAnimTimer(MKEntity entity) {
        return entity.getCastAnimTimer();
    }
}
