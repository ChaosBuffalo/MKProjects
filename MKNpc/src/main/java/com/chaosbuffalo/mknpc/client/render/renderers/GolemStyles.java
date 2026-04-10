package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class GolemStyles {
    public static final ResourceLocation VANILLA_IRON_GOLEM_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");
    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey("default");

    public static ResourceKey<ModelLook> lookKey(String lookName) {
        ResourceLocation entityId = MKNpcEntityTypes.GOLEM_TYPE.getId();
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}
