package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PiglinStyles {

    public static final ResourceLocation VANILLA_ZOMBIFIED_PIGLIN_TEXTURE = new ResourceLocation(
            "textures/entity/piglin/zombified_piglin.png");

    private static final Map<String, ResourceLocation> TEXTURE_VARIANTS = new HashMap<>();
    private static final Map<String, ResourceLocation> CLOTHING_VARIANTS = new HashMap<>();
    public static final Map<String, ModelLook> ZOMBIFIED_PIGLIN_LOOKS = new HashMap<>();

    public static final ModelLook DEFAULT_ZOMBIE_LOOK = new ModelLook(ModelStyles.BASIC_STYLE, VANILLA_ZOMBIFIED_PIGLIN_TEXTURE);

    public static void putZombifiedLook(String name, ModelLook style){
        ZOMBIFIED_PIGLIN_LOOKS.put(name, style);
    }

    static {
        TEXTURE_VARIANTS.put("default", VANILLA_ZOMBIFIED_PIGLIN_TEXTURE);


        for (Map.Entry<String, ResourceLocation> textureVariant : TEXTURE_VARIANTS.entrySet()){
            for (Map.Entry<String, ResourceLocation> clothingVariant : CLOTHING_VARIANTS.entrySet()){
                putZombifiedLook(String.format("%s_%s", textureVariant.getKey(), clothingVariant.getKey()),
                        new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE, textureVariant.getValue(), clothingVariant.getValue()));
            }
            putZombifiedLook(textureVariant.getKey(), new ModelLook(ModelStyles.BASIC_STYLE, textureVariant.getValue()));
        }

    }
}
