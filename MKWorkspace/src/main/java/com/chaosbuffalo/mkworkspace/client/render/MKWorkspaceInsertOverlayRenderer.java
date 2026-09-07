package com.chaosbuffalo.mkworkspace.client.render;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspaceInsertOverlayPacket;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.List;

@EventBusSubscriber(modid = MKWorkspace.MODID, value = Dist.CLIENT)
public final class MKWorkspaceInsertOverlayRenderer {
    private static final int REQUEST_INTERVAL_TICKS = 20;
    private static final int CACHE_LIFETIME_TICKS = 40;
    private static final double FRONT_VECTOR_LENGTH = 1.35;
    private static final double TOP_VECTOR_LENGTH = 0.85;
    private static final double VERTICAL_FRONT_TOP_ARROW_OFFSET_RATIO = 0.5;
    private static final double ARROW_HEAD_LENGTH = 0.25;

    private static List<MKWorkspaceInsertOverlaySnapshot.Entry> entries = List.of();
    private static int lastRequestTick = -REQUEST_INTERVAL_TICKS;
    private static int lastUpdateTick = Integer.MIN_VALUE;

    private MKWorkspaceInsertOverlayRenderer() {
    }

    public static void update(List<MKWorkspaceInsertOverlaySnapshot.Entry> newEntries) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            entries = List.of();
            lastUpdateTick = Integer.MIN_VALUE;
            return;
        }
        entries = List.copyOf(newEntries);
        lastUpdateTick = player.tickCount;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isCreative()) {
            entries = List.of();
            return;
        }
        requestOverlay(player);
        if (entries.isEmpty() || player.tickCount - lastUpdateTick > CACHE_LIFETIME_TICKS) {
            return;
        }
        render(event);
    }

    private static void requestOverlay(Player player) {
        if (player.tickCount - lastRequestTick < REQUEST_INTERVAL_TICKS) {
            return;
        }
        lastRequestTick = player.tickCount;
        PacketDistributor.sendToServer(new RequestWorkspaceInsertOverlayPacket());
    }

    private static void render(RenderLevelStageEvent event) {
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        for (MKWorkspaceInsertOverlaySnapshot.Entry entry : entries) {
            LevelRenderer.renderLineBox(poseStack, consumer, boundsToAabb(entry.bounds()),
                    0.0f, 0.85f, 1.0f, 0.95f);
            AABB socketBox = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(entry.socketWorldPos()))
                    .inflate(0.04);
            LevelRenderer.renderLineBox(poseStack, consumer, socketBox, 1.0f, 0.95f, 0.1f, 1.0f);
            Vec3 socketCenter = Vec3.atCenterOf(entry.socketWorldPos());
            renderDirection(matrix, pose, consumer, socketCenter, entry.front(),
                    FRONT_VECTOR_LENGTH, 1.0f, 0.95f, 0.1f, 1.0f);
            renderDirection(matrix, pose, consumer, topArrowStart(socketCenter, entry.front()), entry.top(),
                    TOP_VECTOR_LENGTH, 0.2f, 1.0f, 0.35f, 1.0f);
        }
        poseStack.popPose();
        buffer.endBatch(RenderType.lines());
    }

    private static AABB boundsToAabb(BoundingBox bounds) {
        return new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                bounds.maxX() + 1.0, bounds.maxY() + 1.0, bounds.maxZ() + 1.0);
    }

    private static Vec3 topArrowStart(Vec3 socketCenter, Direction front) {
        if (!front.getAxis().isVertical()) {
            return socketCenter;
        }
        Vec3 frontAxis = Vec3.atLowerCornerOf(front.getNormal());
        return socketCenter.add(frontAxis.scale(FRONT_VECTOR_LENGTH * VERTICAL_FRONT_TOP_ARROW_OFFSET_RATIO));
    }

    private static void renderDirection(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer consumer, Vec3 start,
                                        Direction direction, double length, float red, float green, float blue,
                                        float alpha) {
        Vec3 axis = Vec3.atLowerCornerOf(direction.getNormal());
        Vec3 end = start.add(axis.scale(length));
        addLine(consumer, matrix, pose, start, end, red, green, blue, alpha);

        Vec3 crossA = perpendicular(axis);
        Vec3 crossB = axis.cross(crossA).normalize();
        Vec3 back = axis.scale(-ARROW_HEAD_LENGTH);
        addLine(consumer, matrix, pose, end, end.add(back).add(crossA.scale(ARROW_HEAD_LENGTH * 0.55)),
                red, green, blue, alpha);
        addLine(consumer, matrix, pose, end, end.add(back).add(crossA.scale(-ARROW_HEAD_LENGTH * 0.55)),
                red, green, blue, alpha);
        addLine(consumer, matrix, pose, end, end.add(back).add(crossB.scale(ARROW_HEAD_LENGTH * 0.55)),
                red, green, blue, alpha);
        addLine(consumer, matrix, pose, end, end.add(back).add(crossB.scale(-ARROW_HEAD_LENGTH * 0.55)),
                red, green, blue, alpha);
    }

    private static Vec3 perpendicular(Vec3 axis) {
        Vec3 reference = Math.abs(axis.y()) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        return axis.cross(reference).normalize();
    }

    private static void addLine(VertexConsumer consumer, Matrix4f matrix, PoseStack.Pose pose, Vec3 start, Vec3 end,
                                float red, float green, float blue, float alpha) {
        Vec3 normal = end.subtract(start);
        if (normal.lengthSqr() < 1.0e-8) {
            normal = new Vec3(0.0, 1.0, 0.0);
        } else {
            normal = normal.normalize();
        }
        consumer.addVertex(matrix, (float) start.x(), (float) start.y(), (float) start.z())
                .setColor(red, green, blue, alpha)
                .setNormal(pose, (float) normal.x(), (float) normal.y(), (float) normal.z());
        consumer.addVertex(matrix, (float) end.x(), (float) end.y(), (float) end.z())
                .setColor(red, green, blue, alpha)
                .setNormal(pose, (float) normal.x(), (float) normal.y(), (float) normal.z());
    }
}
