package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.chaosbuffalo.mknpc.client.render.models.MKBlazeModel;
import com.chaosbuffalo.mknpc.client.render.skeleton.BlazeSkeleton;
import com.chaosbuffalo.mknpc.entity.MKBlazeEntity;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MKBlazeRenderer extends MobRenderer<MKBlazeEntity, MKBlazeModel<MKBlazeEntity>> {
    private static final ResourceLocation BLAZE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/blaze.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(MKNpcEntityTypes.BLAZE_TYPE.getId(), "base");
    private final BlazeSkeleton<MKBlazeEntity, MKBlazeModel<MKBlazeEntity>> skeleton;

    public MKBlazeRenderer(EntityRendererProvider.Context p_173933_) {
        super(p_173933_, new MKBlazeModel<>(p_173933_.bakeLayer(LAYER_LOCATION)), 0.5F);
        skeleton = new BlazeSkeleton<>(getModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MKBlazeEntity mkBlazeEntity) {
        return BLAZE_LOCATION;
    }

    protected int getBlockLightLevel(Blaze entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(MKBlazeEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entityIn, entityYaw, partialTicks, poseStack, buffer, packedLight);
        MKEntity.VisualCastState castState = entityIn.getVisualCastState();
        if (castState == MKEntity.VisualCastState.CASTING || castState == MKEntity.VisualCastState.RELEASE) {
            MKAbility ability = entityIn.getCastingAbility();
            if (ability != null) {
                if (ability.hasCastingParticles()) {
                    ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                    if (anim != null) {
                        float scale = MathUtils.lerp(.6f, 1.f, entityIn.getCastRatio());
                        Vec3 scaleVec = new Vec3(scale, scale, scale);
                        Optional<Vec3> leftPos = getHandPosition(partialTicks, entityIn, HumanoidArm.LEFT);
                        leftPos.ifPresent(pos -> anim.spawn(entityIn.getCommandSenderWorld(), pos, scaleVec, null));
                        Optional<Vec3> rightPos = getHandPosition(partialTicks, entityIn, HumanoidArm.RIGHT);
                        rightPos.ifPresent(pos -> anim.spawn(entityIn.getCommandSenderWorld(), pos, scaleVec, null));
                    }
                }
            }
        }
        entityIn.getParticleEffectTracker().getParticleInstances().forEach(instance -> {
            instance.update(entityIn, skeleton, partialTicks, getRenderOffset(entityIn, partialTicks));
        });
    }

    private Optional<Vec3> getHandPosition(float partialTicks, MKBlazeEntity entityIn, HumanoidArm handSide) {
        return MCBone.getPositionOfBoneInWorld(entityIn, skeleton, partialTicks,
                getRenderOffset(entityIn, partialTicks), handSide == HumanoidArm.LEFT ?
                        BlazeSkeleton.LEFT_HAND_BONE_NAME : BlazeSkeleton.RIGHT_HAND_BONE_NAME);
    }
}
