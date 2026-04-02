package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SkeletonStyles {
    public static final ResourceLocation SKELETON_TEXTURES = ResourceLocation.withDefaultNamespace(
            "textures/entity/skeleton/skeleton.png");
    public static final ResourceLocation STRAY_SKELETON_TEXTURES = ResourceLocation.withDefaultNamespace(
            "textures/entity/skeleton/stray.png");
    public static final ResourceLocation WITHER_SKELETON_TEXTURES = ResourceLocation.withDefaultNamespace(
            "textures/entity/skeleton/wither_skeleton.png");

    public static final ResourceLocation STRAY_CLOTHES_TEXTURES = ResourceLocation.withDefaultNamespace(
            "textures/entity/skeleton/stray_overlay.png");

    public static ResourceKey<ModelLook> lookKey(EntityType<?> entityType, String lookName) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}

