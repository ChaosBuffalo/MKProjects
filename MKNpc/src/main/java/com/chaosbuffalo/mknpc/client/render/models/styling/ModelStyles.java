package com.chaosbuffalo.mknpc.client.render.models.styling;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModelStyles {
    public static final DeferredRegister<ModelStyle> REGISTRY = DeferredRegister.create(NpcRegistries.MODEL_STYLE_REGISTRY_KEY, MKNpc.MODID);

    public static final String HAIR_1 = "hair_1";
    public static final String HAIR_2 = "hair_2";
    public static final String CLOTHES = "clothes_1";

    public static final String LONG_HAIR_NAME = "long_hair";
    public static final String LONG_HAIR_ARMORED_NAME = "long_hair_armored";
    public static final String SHORT_HAIR_NAME = "short_hair";
    public static final String CLOTHES_ONLY_NAME = "clothes_only";
    public static final String BASIC_NAME = "basic";
    public static final String BASIC_GOLEM_NAME = "basic_golem";
    public static final String CLOTHES_ARMOR_NAME = "clothes_armor";
    public static final String CLOTHES_ARMOR_TRANSLUCENT_NAME = "clothes_armor_translucent";

    public static final DeferredHolder<ModelStyle, ModelStyle> ARMORED_LONG_HAIR_STYLE = REGISTRY.register(
            LONG_HAIR_ARMORED_NAME,
            () -> new ModelStyle(
                    LONG_HAIR_ARMORED_NAME,
                    true,
                    false,
                    new LayerStyle(HAIR_1, 0.25f),
                    new LayerStyle(CLOTHES, 0.4f),
                    new LayerStyle(HAIR_2, 1.15f)));

    public static final DeferredHolder<ModelStyle, ModelStyle> LONG_HAIR_STYLE = REGISTRY.register(
            LONG_HAIR_NAME,
            () -> new ModelStyle(
                    LONG_HAIR_NAME,
                    true,
                    true,
                    new LayerStyle(HAIR_1, 0.25f),
                    new LayerStyle(CLOTHES, 0.4f),
                    new LayerStyle(HAIR_2, 0.45f)));

    public static final DeferredHolder<ModelStyle, ModelStyle> SHORT_HAIR_STYLE = REGISTRY.register(
            SHORT_HAIR_NAME,
            () -> new ModelStyle(
                    SHORT_HAIR_NAME,
                    true,
                    false,
                    new LayerStyle(HAIR_1, 0.25f),
                    new LayerStyle(CLOTHES, 0.4f)));

    public static final DeferredHolder<ModelStyle, ModelStyle> CLOTHES_ONLY_STYLE = REGISTRY.register(
            CLOTHES_ONLY_NAME,
            () -> new ModelStyle(
                    CLOTHES_ONLY_NAME,
                    true,
                    false,
                    new LayerStyle(CLOTHES, 0.25f)));

    public static final DeferredHolder<ModelStyle, ModelStyle> CLOTHES_ARMOR_STYLE = REGISTRY.register(
            CLOTHES_ARMOR_NAME,
            () -> new ModelStyle(
                    CLOTHES_ARMOR_NAME,
                    true,
                    false,
                    new LayerStyle(CLOTHES, 0.75f)));

    public static final DeferredHolder<ModelStyle, ModelStyle> CLOTHES_ARMOR_TRANSLUCENT_STYLE = REGISTRY.register(
            CLOTHES_ARMOR_TRANSLUCENT_NAME,
            () -> new ModelStyle(
                    CLOTHES_ARMOR_TRANSLUCENT_NAME,
                    true,
                    false,
                    new LayerStyle(CLOTHES, 0.75f, true)));

    public static final DeferredHolder<ModelStyle, ModelStyle> BASIC_STYLE = REGISTRY.register(
            BASIC_NAME,
            () -> new ModelStyle(
                    BASIC_NAME,
                    true,
                    false));

    public static final DeferredHolder<ModelStyle, ModelStyle> BASIC_GOLEM_STYLE = REGISTRY.register(
            BASIC_GOLEM_NAME,
            () -> new ModelStyle(
                    BASIC_GOLEM_NAME,
                    false,
                    false));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }

    public static ResourceLocation getId(ModelStyle style) {
        return NpcRegistries.MODEL_STYLES.getKey(style);
    }
}
