package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.MKSkullModel;
import com.chaosbuffalo.mknpc.entity.MKFlyingSkullEntity;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MKFlyingSkullRenderer extends MobRenderer<MKFlyingSkullEntity, MKSkullModel<MKFlyingSkullEntity>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(MKNpcEntityTypes.FLYING_SKULL_TYPE.getId(), "base");
    private static final ResourceLocation TEXTURE_LOCATION = MKNpc.id("textures/entity/mk_skull_base.png");
    private static final ResourceLocation FIRE_TRAIL = MKNpc.id("skull_trail");

    public MKFlyingSkullRenderer(EntityRendererProvider.Context context) {
        super(context, new MKSkullModel<>(context.bakeLayer(LAYER_LOCATION)), 0.35f);
    }

    @Override
    public ResourceLocation getTextureLocation(MKFlyingSkullEntity entity) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void render(MKFlyingSkullEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float attackAnim = entity.getAttackAnim(partialTicks);
        float lungeAmount = Mth.sin(attackAnim * (float) Math.PI) * 0.5F;
        if (lungeAmount > 0.0F) {
            Vec3 forward = entity.getLookAngle().normalize().scale(lungeAmount);
            poseStack.pushPose();
            poseStack.translate(forward.x, forward.y, forward.z);
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
        ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(FIRE_TRAIL);
        if (anim == null) {
            return;
        }
        Vec3 backwards = entity.getLookAngle().normalize();
        Vec3 motion = entity.getDeltaMovement().normalize();
        Vec3 backwards_start = backwards.scale(-0.35);
        Vec3 backwards_end = motion.scale(-1.0);
        Vec3 spawnPos = entity.getEyePosition(partialTicks).add(0.0, -0.25, 0.0).add(backwards_start);
        Vec3 spawnEnd = spawnPos.add(backwards_end);
        anim.spawn(entity.level(), spawnPos, new Vec3(1.0, 1.0, 1.0), List.of(spawnEnd));
    }
}
