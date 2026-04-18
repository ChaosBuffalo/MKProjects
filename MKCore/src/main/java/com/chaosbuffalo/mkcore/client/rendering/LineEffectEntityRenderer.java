package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class LineEffectEntityRenderer extends EntityRenderer<LineEffectEntity> {
    private static final int CAPSULE_SEGMENTS = 12;

    public LineEffectEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LineEffectEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
            renderCapsule(entity, poseStack, consumer);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderCapsule(LineEffectEntity entity, PoseStack poseStack, VertexConsumer consumer) {
        double radius = entity.getGrowth();
        if (radius <= 0.0) {
            addLine(consumer, poseStack.last().pose(), poseStack.last(), entity.getStartPoint().subtract(entity.position()),
                    entity.getEndPoint().subtract(entity.position()), 0.0f, 1.0f, 0.0f, 1.0f);
            return;
        }

        var axis = entity.getEndPoint().subtract(entity.getStartPoint());
        if (axis.lengthSqr() < 1.0e-8) {
            return;
        }
        AABB localBounds = entity.getTraceBounds().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, consumer, localBounds, 0.0f, 1.0f, 0.0f, 1.0f);

        var forward = axis.normalize();
        var reference = Math.abs(forward.y()) > 0.99 ? new net.minecraft.world.phys.Vec3(1.0, 0.0, 0.0) : new net.minecraft.world.phys.Vec3(0.0, 1.0, 0.0);
        var right = forward.cross(reference).normalize();
        var up = right.cross(forward).normalize();
        Matrix4f matrix = poseStack.last().pose();
        var pose = poseStack.last();
        var start = entity.getStartPoint().subtract(entity.position());
        var end = entity.getEndPoint().subtract(entity.position());

        for (int i = 0; i < CAPSULE_SEGMENTS; i++) {
            double angle0 = (Math.PI * 2.0 * i) / CAPSULE_SEGMENTS;
            double angle1 = (Math.PI * 2.0 * (i + 1)) / CAPSULE_SEGMENTS;
            var offset0 = right.scale(Math.cos(angle0) * radius).add(up.scale(Math.sin(angle0) * radius));
            var offset1 = right.scale(Math.cos(angle1) * radius).add(up.scale(Math.sin(angle1) * radius));
            addLine(consumer, matrix, pose, start.add(offset0), start.add(offset1), 0.0f, 1.0f, 0.0f, 1.0f);
            addLine(consumer, matrix, pose, end.add(offset0), end.add(offset1), 0.0f, 1.0f, 0.0f, 1.0f);
            addLine(consumer, matrix, pose, start.add(offset0), end.add(offset0), 0.0f, 1.0f, 0.0f, 1.0f);
        }
        addLine(consumer, matrix, pose, start, end, 1.0f, 1.0f, 0.0f, 1.0f);
    }

    private void addLine(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose,
                         net.minecraft.world.phys.Vec3 start, net.minecraft.world.phys.Vec3 end,
                         float red, float green, float blue, float alpha) {
        net.minecraft.world.phys.Vec3 normal = end.subtract(start);
        if (normal.lengthSqr() < 1.0e-8) {
            normal = new net.minecraft.world.phys.Vec3(0.0, 1.0, 0.0);
        } else {
            normal = normal.normalize();
        }
        float nx = (float) normal.x();
        float ny = (float) normal.y();
        float nz = (float) normal.z();
        consumer.addVertex(matrix, (float) start.x(), (float) start.y(), (float) start.z())
                .setColor(red, green, blue, alpha)
                .setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix, (float) end.x(), (float) end.y(), (float) end.z())
                .setColor(red, green, blue, alpha)
                .setNormal(pose, nx, ny, nz);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(LineEffectEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(@Nonnull LineEffectEntity entity, @Nonnull Frustum clippingHelper, double x, double y, double z) {
        return true;
    }
}
