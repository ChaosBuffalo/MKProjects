package com.chaosbuffalo.mkultra.client.render.styling;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MKUOrcs {

    public static final ResourceLocation BLUE_ORC = MKUltra.id("textures/entity/orcs/blue_orc.png");
    public static final ResourceLocation GREEN_ORC = MKUltra.id("textures/entity/orcs/green_orc.png");
    public static final ResourceLocation RED_ORC = MKUltra.id("textures/entity/orcs/red_orc.png");

    public static final ResourceLocation GREEN_LADY_HAIR_1 = MKUltra.id("textures/entity/orcs/green_lady_hair_1.png");
    public static final ResourceLocation GREEN_LADY_HAIR_2 = MKUltra.id("textures/entity/orcs/green_lady_hair_2.png");

    public static final ResourceLocation ORC_HAIR_1 = MKUltra.id("textures/entity/orcs/orc_hair_1.png");
    public static final ResourceLocation ORC_HAIR_2 = MKUltra.id("textures/entity/orcs/orc_hair_2.png");
    public static final ResourceLocation ORC_LONG_HAIR_1_LAYER_2 = MKUltra.id("textures/entity/orcs/orc_long_hair_1_layer_2.png");
    public static final ResourceLocation ORC_LONG_HAIR_2_LAYER_2 = MKUltra.id("textures/entity/orcs/orc_long_hair_2_layer_2.png");
    public static final ResourceLocation ORC_LONG_HAIR_3_LAYER_2 = MKUltra.id("textures/entity/orcs/orc_long_hair_3_layer_2.png");

    public static final String DEFAULT_NAME = "default";
    public static final String GREEN_LADY_NAME = "green_lady";
    public static final String GREEN_LADY_GUARD_1_NAME = "green_lady_guard_1";
    public static final String GREEN_LADY_GUARD_2_NAME = "green_lady_guard_2";
    public static final String GREEN_SMITH_NAME = "green_smith";

    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey(DEFAULT_NAME);
    public static final ResourceKey<ModelLook> GREEN_LADY_LOOK = lookKey(GREEN_LADY_NAME);
    public static final ResourceKey<ModelLook> GREEN_LADY_GUARD_1_LOOK = lookKey(GREEN_LADY_GUARD_1_NAME);
    public static final ResourceKey<ModelLook> GREEN_LADY_GUARD_2_LOOK = lookKey(GREEN_LADY_GUARD_2_NAME);
    public static final ResourceKey<ModelLook> GREEN_SMITH_LOOK = lookKey(GREEN_SMITH_NAME);

    private static ResourceKey<ModelLook> lookKey(String lookName) {
        ResourceLocation entityId = MKUEntities.ORC_TYPE.getId();
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}

