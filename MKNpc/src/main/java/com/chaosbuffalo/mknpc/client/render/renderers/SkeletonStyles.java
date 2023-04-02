package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SkeletonStyles {
    public static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation(
            "textures/entity/skeleton/skeleton.png");
    public static final ResourceLocation STRAY_SKELETON_TEXTURES = new ResourceLocation(
            "textures/entity/skeleton/stray.png");
    public static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation(
            "textures/entity/skeleton/wither_skeleton.png");

    public static final ResourceLocation STRAY_CLOTHES_TEXTURES = new ResourceLocation(
            "textures/entity/skeleton/stray_overlay.png");

    private static final Map<String, ResourceLocation> TEXTURE_VARIANTS = new HashMap<>();
    private static final Map<String, ResourceLocation> CLOTHING_VARIANTS = new HashMap<>();

    public static final Map<String, ModelLook> SKELETON_LOOKS = new HashMap<>();
    public static final ModelLook DEFAULT_LOOK = new ModelLook(ModelStyles.BASIC_STYLE, SKELETON_TEXTURES);

    public static void putLook(String name, ModelLook style){
        SKELETON_LOOKS.put(name, style);
    }

    static {
        TEXTURE_VARIANTS.put("wither", WITHER_SKELETON_TEXTURES);
        TEXTURE_VARIANTS.put("stray", STRAY_SKELETON_TEXTURES);
        TEXTURE_VARIANTS.put("default", SKELETON_TEXTURES);
        CLOTHING_VARIANTS.put("stray", STRAY_CLOTHES_TEXTURES);

        for (Map.Entry<String, ResourceLocation> textureVariant : TEXTURE_VARIANTS.entrySet()){
            for (Map.Entry<String, ResourceLocation> clothingVariant : CLOTHING_VARIANTS.entrySet()){
                putLook(String.format("%s_%s", textureVariant.getKey(), clothingVariant.getKey()),
                        new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE, textureVariant.getValue(), clothingVariant.getValue()));
            }
            putLook(textureVariant.getKey(), new ModelLook(ModelStyles.BASIC_STYLE, textureVariant.getValue()));
        }

    }
}
