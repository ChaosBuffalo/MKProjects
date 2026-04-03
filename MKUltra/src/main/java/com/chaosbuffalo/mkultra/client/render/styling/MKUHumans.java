package com.chaosbuffalo.mkultra.client.render.styling;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MKUHumans {

    public static final ResourceLocation HUMAN_SKIN_1 = MKUltra.id("textures/entity/humans/human_skin_1.png");
    public static final ResourceLocation HUMAN_SKIN_2 = MKUltra.id("textures/entity/humans/human_skin_2.png");
    public static final ResourceLocation HUMAN_HAIR_1 = MKUltra.id("textures/entity/humans/human_hair_1.png");
    public static final ResourceLocation HUMAN_HAIR_2 = MKUltra.id("textures/entity/humans/human_hair_2.png");
    public static final ResourceLocation HUMAN_HAIR_3 = MKUltra.id("textures/entity/humans/human_hair_3.png");

    public static final ResourceLocation PALE_HUMAN_SKIN_1 = MKUltra.id("textures/entity/humans/pale_skin_1.png");

    public static final ResourceLocation GHOST_SKIN_1 = MKUltra.id("textures/entity/humans/ghost_skin_1.png");
    public static final ResourceLocation GHOST_HAIR_1 = MKUltra.id("textures/entity/humans/ghost_hair_1.png");
    public static final ResourceLocation GHOST_HAIR_2 = MKUltra.id("textures/entity/humans/ghost_hair_2.png");

    public static final String TWO_LAYER_CLOTHES_SHORT_HAIR_NAME = "two_layer_clothes_short_hair";

    public static final String TWO_LAYER_ARMOR_SHORT_HAIR_NAME = "two_layer_armor_short_hair";

    public static final String TWO_LAYER_ARMOR_NO_HAIR_NAME = "two_layer_armor_no_hair";

    public static final String GHOST_LONG_HAIR_NAME = "ghost_long_hair";
    public static final String GHOST_LONG_HAIR_ARMORED_NAME = "ghost_long_hair_armored";

    public static final String GHOST_LONG_HAIR_NO_CLOTHES_NAME = "ghost_long_hair_no_clothes";
    public static final String GHOST_LONG_HAIR_NO_CLOTHES_ARMORED_NAME = "ghost_long_hair_no_clothes_armored";

    public static final String GHOST_1_NAME = "ghost_1";

    public static final String GHOST_LOOK_CLEAN_NAME = "ghost_clean";

    public static final String GHOST_SHORT_HAIR_NO_CLOTHES_NAME = "ghost_short_hair_no_clothes";
    public static final String GHOST_SHORT_HAIR_NO_CLOTHES_ARMORED_NAME = "ghost_short_hair_no_clothes_armored";


    public static final String GHOST_LOOK_CLEAN_SHORT_NAME = "ghost_clean_short";

    public static final String CLERIC_1_NAME = "cleric_1";
    public static final String CLERIC_2_NAME = "cleric_2";
    public static final String DEFAULT_NAME = "default";
    public static final String NETHER_MAGE_1_NAME = "nether_mage_1";
    public static final String BANDIT_RAIDER_1_NAME = "bandit_raider_1";
    public static final String TEMPLE_GUARD_1_NAME = "temple_guard_1";
    public static final String TEMPLE_GUARD_2_NAME = "temple_guard_2";
    public static final String NECROTIDE_CULTIST_1_NAME = "necrotide_cultist_1";
    public static final String NECROTIDE_CULTIST_SKULL_1_NAME = "necrotide_cultist_skull_1";

    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), DEFAULT_NAME);
    public static final ResourceKey<ModelLook> CLERIC_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), CLERIC_1_NAME);
    public static final ResourceKey<ModelLook> CLERIC_2_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), CLERIC_2_NAME);
    public static final ResourceKey<ModelLook> GHOST_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), GHOST_1_NAME);
    public static final ResourceKey<ModelLook> GHOST_CLEAN_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), GHOST_LOOK_CLEAN_NAME);
    public static final ResourceKey<ModelLook> GHOST_CLEAN_SHORT_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), GHOST_LOOK_CLEAN_SHORT_NAME);
    public static final ResourceKey<ModelLook> NETHER_MAGE_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), NETHER_MAGE_1_NAME);
    public static final ResourceKey<ModelLook> BANDIT_RAIDER_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), BANDIT_RAIDER_1_NAME);
    public static final ResourceKey<ModelLook> TEMPLE_GUARD_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), TEMPLE_GUARD_1_NAME);
    public static final ResourceKey<ModelLook> TEMPLE_GUARD_2_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), TEMPLE_GUARD_2_NAME);
    public static final ResourceKey<ModelLook> NECROTIDE_CULTIST_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), NECROTIDE_CULTIST_1_NAME);
    public static final ResourceKey<ModelLook> NECROTIDE_CULTIST_SKULL_1_LOOK = lookKey(MKUEntities.HUMAN_TYPE.getId(), NECROTIDE_CULTIST_SKULL_1_NAME);

    public static final ResourceKey<ModelLook> GHOST_DEFAULT_LOOK = lookKey(MKUEntities.HUMAN_GHOST_TYPE.getId(), DEFAULT_NAME);
    public static final ResourceKey<ModelLook> GHOST_ENTITY_GHOST_1_LOOK = lookKey(MKUEntities.HUMAN_GHOST_TYPE.getId(), GHOST_1_NAME);
    public static final ResourceKey<ModelLook> GHOST_ENTITY_GHOST_CLEAN_LOOK = lookKey(MKUEntities.HUMAN_GHOST_TYPE.getId(), GHOST_LOOK_CLEAN_NAME);
    public static final ResourceKey<ModelLook> GHOST_ENTITY_GHOST_CLEAN_SHORT_LOOK = lookKey(MKUEntities.HUMAN_GHOST_TYPE.getId(), GHOST_LOOK_CLEAN_SHORT_NAME);

    private static ResourceKey<ModelLook> lookKey(ResourceLocation entityId, String lookName) {
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}

