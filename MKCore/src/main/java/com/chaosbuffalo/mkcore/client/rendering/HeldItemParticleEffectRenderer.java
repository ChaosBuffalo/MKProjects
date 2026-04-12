package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.fx.particles.effect_instances.HeldItemParticleEffectInstance;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.IItemParticleAttachmentProvider;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ItemParticleAttachment;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ItemParticleAttachmentProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

public final class HeldItemParticleEffectRenderer {
    private HeldItemParticleEffectRenderer() {
    }

    public static void spawnForRenderPose(HeldItemParticleEffectInstance instance, LivingEntity entity, InteractionHand hand,
                                          PoseStack renderPoseStack, ItemStack stack, ItemDisplayContext displayContext,
                                          boolean leftHand, boolean includeCameraTranslation,
                                          boolean includeCameraRotation) {
        if (instance.getHand() != hand) {
            return;
        }

        ItemParticleAttachmentProfile profile = getProfile(stack);
        if (profile == null || profile.attachments().isEmpty()) {
            return;
        }

        Matrix4f transform = createWorldTransform(renderPoseStack, stack, leftHand, displayContext, entity,
                includeCameraTranslation, includeCameraRotation);
        if (transform == null) {
            return;
        }

        spawnProfile(instance, entity, profile, transform);
    }

    public static void spawnForFirstPerson(HeldItemParticleEffectInstance instance, LocalPlayer player, InteractionHand hand,
                                           PoseStack poseStack, ItemStack stack, float partialTicks) {
        boolean leftHand = resolveArm(player, hand) == net.minecraft.world.entity.HumanoidArm.LEFT;
        spawnForRenderPose(instance, player, hand, poseStack, stack,
                leftHand ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                leftHand, true, false);
    }

    private static void spawnProfile(HeldItemParticleEffectInstance instance, net.minecraft.world.entity.Entity entity,
                                     ItemParticleAttachmentProfile profile, Matrix4f transform) {
        instance.getAnimation().ifPresent(anim -> {
            for (ItemParticleAttachment attachment : profile.attachments()) {
                Vec3 start = transformPoint(transform, attachment.start());
                Vec3 end = transformPoint(transform, attachment.end());
                anim.spawn(entity.getCommandSenderWorld(), start, new Vec3(1.0, 1.0, 1.0), List.of(end));
            }
        });
    }

    private static ItemParticleAttachmentProfile getProfile(ItemStack stack) {
        if (stack.getItem() instanceof IItemParticleAttachmentProvider provider) {
            return provider.getParticleAttachmentProfile(stack);
        }
        return null;
    }

    private static Matrix4f createWorldTransform(PoseStack renderPoseStack, ItemStack stack, boolean leftHand,
                                                 ItemDisplayContext displayContext, LivingEntity entity,
                                                 boolean includeCameraTranslation, boolean includeCameraRotation) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer != null ? mc.gameRenderer.getMainCamera() : null;
        if (camera == null) {
            return null;
        }

        PoseStack poseStack = new PoseStack();
        if (includeCameraTranslation) {
            Vec3 cameraPos = camera.getPosition();
            poseStack.translate(cameraPos.x, cameraPos.y, cameraPos.z);
        }
        if (includeCameraRotation) {
            poseStack.mulPose(camera.rotation());
        }
        poseStack.last().pose().mul(new Matrix4f(renderPoseStack.last().pose()));
        applyModelTransform(stack, poseStack, leftHand, displayContext, entity);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        return new Matrix4f(poseStack.last().pose());
    }

    private static void applyModelTransform(ItemStack stack, PoseStack poseStack, boolean leftHand,
                                            ItemDisplayContext displayContext, LivingEntity entity) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity, 0);
        model.applyTransform(displayContext, poseStack, leftHand);
    }

    private static Vec3 transformPoint(Matrix4f transform, Vec3 point) {
        Vector4f position = new Vector4f(
                (float) point.x / 16.0F,
                (float) point.y / 16.0F,
                (float) point.z / 16.0F,
                1.0F);
        transform.transform(position);
        return new Vec3(position.x(), position.y(), position.z());
    }

    private static net.minecraft.world.entity.HumanoidArm resolveArm(LivingEntity entity, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
    }
}
