package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class PiglinStyles {

    public static final ResourceLocation VANILLA_ZOMBIFIED_PIGLIN_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/piglin/zombified_piglin.png");

    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey("default");

    private static ResourceKey<ModelLook> lookKey(String lookName) {
        ResourceLocation entityId = MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId();
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}

