package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mknpc.client.render.models.styling.LayerStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKUModelStyles {
    public static final DeferredRegister<ModelStyle> REGISTRY = DeferredRegister.create(NpcRegistries.MODEL_STYLE_REGISTRY_KEY, MKUltra.MODID);

    public static final DeferredHolder<ModelStyle, ModelStyle> TWO_LAYER_CLOTHES_SHORT_HAIR = REGISTRY.register(
            MKUHumans.TWO_LAYER_CLOTHES_SHORT_HAIR_NAME,
            () -> new ModelStyle(MKUHumans.TWO_LAYER_CLOTHES_SHORT_HAIR_NAME,
                    true, false, new LayerStyle("hair_1", 0.25F),
                    new LayerStyle("clothes_1", 0.3F), new LayerStyle("clothes_2", 0.5f)));
    public static final DeferredHolder<ModelStyle, ModelStyle> TWO_LAYER_ARMOR_SHORT_HAIR = REGISTRY.register(
            MKUHumans.TWO_LAYER_ARMOR_SHORT_HAIR_NAME,
            () -> new ModelStyle(MKUHumans.TWO_LAYER_ARMOR_SHORT_HAIR_NAME,
                    false, false, new LayerStyle("hair_1", 0.25F),
                    new LayerStyle("armor_lower", 0.5F), new LayerStyle("armor_upper", 1.0f)));
    public static final DeferredHolder<ModelStyle, ModelStyle> TWO_LAYER_ARMOR_NO_HAIR = REGISTRY.register(
            MKUHumans.TWO_LAYER_ARMOR_NO_HAIR_NAME,
            () -> new ModelStyle(MKUHumans.TWO_LAYER_ARMOR_NO_HAIR_NAME,
                    false, false, new LayerStyle("armor_lower", 0.5F), new LayerStyle("armor_upper", 1.0f)));
    public static final DeferredHolder<ModelStyle, ModelStyle> GHOST_LONG_HAIR = REGISTRY.register(
            MKUHumans.GHOST_LONG_HAIR_NAME,
            () -> new ModelStyle(MKUHumans.GHOST_LONG_HAIR_NAME,
                    true, true,
                    new LayerStyle("hair_1", 0.25F, true),
                    new LayerStyle("clothes_1", 0.4F, true),
                    new LayerStyle("hair_2", 0.45F, true)));
    public static final DeferredHolder<ModelStyle, ModelStyle> GHOST_LONG_HAIR_ARMORED = REGISTRY.register(
            MKUHumans.GHOST_LONG_HAIR_ARMORED_NAME,
            () -> new ModelStyle(MKUHumans.GHOST_LONG_HAIR_ARMORED_NAME,
                    true, false,
                    new LayerStyle("hair_1", 0.25F, true),
                    new LayerStyle("clothes_1", 0.4F, true),
                    new LayerStyle("hair_2", 1.15F, true)));
    public static final DeferredHolder<ModelStyle, ModelStyle> GHOST_LONG_HAIR_NO_CLOTHES = REGISTRY.register(
            MKUHumans.GHOST_LONG_HAIR_NO_CLOTHES_NAME,
            () -> new ModelStyle(MKUHumans.GHOST_LONG_HAIR_NO_CLOTHES_NAME,
                    true, true,
                    new LayerStyle("hair_1", 0.25F, true),
                    new LayerStyle("hair_2", 0.45F, true)));
    public static final DeferredHolder<ModelStyle, ModelStyle> GHOST_LONG_HAIR_NO_CLOTHES_ARMORED = REGISTRY.register(
            MKUHumans.GHOST_LONG_HAIR_NO_CLOTHES_ARMORED_NAME,
            () -> new ModelStyle(MKUHumans.GHOST_LONG_HAIR_NO_CLOTHES_ARMORED_NAME,
                    true, false,
                    new LayerStyle("hair_1", 0.25F, true),
                    new LayerStyle("hair_2", 1.15F, true)));
    public static final DeferredHolder<ModelStyle, ModelStyle> GHOST_SHORT_HAIR_NO_CLOTHES = REGISTRY.register(
            MKUHumans.GHOST_SHORT_HAIR_NO_CLOTHES_NAME,
            () -> new ModelStyle(MKUHumans.GHOST_SHORT_HAIR_NO_CLOTHES_NAME,
                    true, true,
                    new LayerStyle("hair_1", 0.25F, true)));
    public static final DeferredHolder<ModelStyle, ModelStyle> GHOST_SHORT_HAIR_NO_CLOTHES_ARMORED = REGISTRY.register(
            MKUHumans.GHOST_SHORT_HAIR_NO_CLOTHES_ARMORED_NAME,
            () -> new ModelStyle(MKUHumans.GHOST_SHORT_HAIR_NO_CLOTHES_ARMORED_NAME,
                    true, false,
                    new LayerStyle("hair_1", 0.25F, true)));
    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
