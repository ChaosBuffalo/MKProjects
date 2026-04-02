package com.chaosbuffalo.mkultra.client.render.styling;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MKUPiglins {

    public static final ResourceLocation VANILLA_ZOMBIFIED_PIGLIN_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/piglin/zombified_piglin.png");

    public static final ResourceLocation SKELETAL_ZOMBIFIED_PIGLIN_TEXTURE = MKUltra.id(
            "textures/entity/piglin/zombified_piglin_skeletal_face.png");

    public static final ResourceLocation IMPERIAL_TROOPER_ARMOR = MKUltra.id(
            "textures/entity/piglin/imperial_armor.png"
    );

    public static final ResourceLocation IMPERIAL_TROOPER_ARMOR_NO_HELMET = MKUltra.id(
            "textures/entity/piglin/imperial_armor_no_helmet.png"
    );

    public static final ResourceLocation IMPERIAL_TROOPER_ARMOR_DAMAGED = MKUltra.id(
            "textures/entity/piglin/imperial_trooper_damaged.png"
    );

    public static final ResourceLocation IMPERIAL_MAGUS_ARMOR_NO_HELMET = MKUltra.id(
            "textures/entity/piglin/imperial_magus_armor.png"
    );


    public static final ResourceLocation IMPERIAL_MAGUS_ARMOR = MKUltra.id(
            "textures/entity/piglin/imperial_magus_armor_no_helmet.png"
    );

    public static final ResourceLocation IMPERIAL_MAGUS_ARMOR_DAMAGED = MKUltra.id(
            "textures/entity/piglin/imperial_magus_armor_damaged.png"
    );

    public static final String ZOMBIE_PIG_TROOPER_NAME = "zombie_pig_trooper";
    public static final String ZOMBIE_PIG_MAGUS_NAME = "zombie_pig_magus";
    public static final String ZOMBIE_PIG_NAME = "zombie_pig";
    public static final String SKELETAL_TROOPER_NAME = "skeletal_trooper";
    public static final String SKELETAL_MAGE_NAME = "skeletal_mage";
    public static final String DESTROYED_SKELETAL_MAGE_NAME = "destroyed_skeletal_mage";
    public static final String DESTROYED_SKELETAL_TROOPER_NAME = "destroyed_skeletal_trooper";

    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey("default");
    public static final ResourceKey<ModelLook> ZOMBIE_PIG_TROOPER_LOOK = lookKey(ZOMBIE_PIG_TROOPER_NAME);
    public static final ResourceKey<ModelLook> ZOMBIE_PIG_MAGUS_LOOK = lookKey(ZOMBIE_PIG_MAGUS_NAME);
    public static final ResourceKey<ModelLook> ZOMBIE_PIG_LOOK = lookKey(ZOMBIE_PIG_NAME);
    public static final ResourceKey<ModelLook> SKELETAL_TROOPER_LOOK = lookKey(SKELETAL_TROOPER_NAME);
    public static final ResourceKey<ModelLook> SKELETAL_MAGE_LOOK = lookKey(SKELETAL_MAGE_NAME);
    public static final ResourceKey<ModelLook> DESTROYED_SKELETAL_MAGE_LOOK = lookKey(DESTROYED_SKELETAL_MAGE_NAME);
    public static final ResourceKey<ModelLook> DESTROYED_SKELETAL_TROOPER_LOOK = lookKey(DESTROYED_SKELETAL_TROOPER_NAME);

    private static ResourceKey<ModelLook> lookKey(String lookName) {
        ResourceLocation entityId = MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId();
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}


