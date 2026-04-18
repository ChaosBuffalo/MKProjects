package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class FireElementalStyles {
    public static final ResourceLocation DEFAULT_TEXTURE = MKNpc.id("textures/entity/fire_elemental.png");
    public static final ResourceLocation CHARGED_OVERLAY_TEXTURE = MKNpc.id("textures/entity/fire_elemental_overcharge.png");
    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey("default");
    public static final ResourceKey<ModelLook> CHARGED_LOOK = lookKey("charged");

    public static ResourceKey<ModelLook> lookKey(String lookName) {
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS, MKNpc.id("fire_elemental/" + lookName));
    }
}
