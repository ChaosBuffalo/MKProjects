package com.chaosbuffalo.mkultra.client.render.styling;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MKUSkeletons {

    public static final String ANCIENT_KING_NAME = "ancient_king";

    public static final String SORCERER_NAME = "sorcerer";

    public static final String BURNING_NAME = "burning_skeleton";

    public static final String SORCERER_QUEEN_NAME = "sorcerer_queen";

    public static final String HONOR_GUARD_NAME = "honor_guard";

    public static final String HYBOREAN_ARCHER_NAME = "hyborean_archer";

    public static final String HYBOREAN_WARRIOR_NAME = "hyborean_warrior";

    public static final String BASIC_NAME = "basic";

    public static final String SEAWOVEN_NAME = "seawoven";

    public static final String SEAWOVEN_WRTECH_NAME = "seawoven_wretch";

    public static final ResourceKey<ModelLook> DEFAULT_LOOK = lookKey("default");
    public static final ResourceKey<ModelLook> ANCIENT_KING_LOOK = lookKey(ANCIENT_KING_NAME);
    public static final ResourceKey<ModelLook> SORCERER_LOOK = lookKey(SORCERER_NAME);
    public static final ResourceKey<ModelLook> BURNING_LOOK = lookKey(BURNING_NAME);
    public static final ResourceKey<ModelLook> SORCERER_QUEEN_LOOK = lookKey(SORCERER_QUEEN_NAME);
    public static final ResourceKey<ModelLook> HONOR_GUARD_LOOK = lookKey(HONOR_GUARD_NAME);
    public static final ResourceKey<ModelLook> HYBOREAN_ARCHER_LOOK = lookKey(HYBOREAN_ARCHER_NAME);
    public static final ResourceKey<ModelLook> HYBOREAN_WARRIOR_LOOK = lookKey(HYBOREAN_WARRIOR_NAME);
    public static final ResourceKey<ModelLook> BASIC_LOOK = lookKey(BASIC_NAME);
    public static final ResourceKey<ModelLook> SEAWOVEN_LOOK = lookKey(SEAWOVEN_NAME);
    public static final ResourceKey<ModelLook> SEAWOVEN_WRETCH_LOOK = lookKey(SEAWOVEN_WRTECH_NAME);

    private static ResourceKey<ModelLook> lookKey(String lookName) {
        ResourceLocation entityId = MKUEntities.HYBOREAN_SKELETON_TYPE.getId();
        return ResourceKey.create(NpcRegistries.MODEL_LOOKS,
                ResourceLocation.fromNamespaceAndPath(entityId.getNamespace(), "%s/%s".formatted(entityId.getPath(), lookName)));
    }
}

