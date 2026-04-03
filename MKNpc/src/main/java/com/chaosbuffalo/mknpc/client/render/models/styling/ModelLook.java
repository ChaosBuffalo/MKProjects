package com.chaosbuffalo.mknpc.client.render.models.styling;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModelLook {
    public static final String DEFINITION_FOLDER = "mknpc/model_looks";
    public static final Codec<ModelLook> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType").forGetter(ModelLook::getEntityType),
            Codec.BOOL.optionalFieldOf("default", false).forGetter(ModelLook::isDefaultLook),
            ResourceLocation.CODEC.fieldOf("modelStyle").forGetter(ModelLook::getModelStyleId),
            ResourceLocation.CODEC.fieldOf("baseTexture").forGetter(ModelLook::getBaseTexture),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                    .optionalFieldOf("layerTextures", Map.of())
                    .forGetter(ModelLook::getLayerTextures)
    ).apply(builder, ModelLook::new));

    private final ResourceLocation baseTexture;
    @Nullable
    private ModelStyle baseStyle;
    private final ResourceLocation modelStyleId;
    private final EntityType<?> entityType;
    private final boolean defaultLook;
    private final Map<String, ResourceLocation> layerTexture;
    public static final ResourceLocation MISSING_TEXTURE = MKNpc.id("textures/entity/missing_texture.png");

    public ModelLook(ModelStyle baseStyle, ResourceLocation baseTexture, ResourceLocation... textureLayers) {
        this(baseStyle, null, false, baseTexture, textureLayers);
    }

    public ModelLook(ModelStyle baseStyle, @Nullable EntityType<?> entityType, boolean defaultLook,
                     ResourceLocation baseTexture, ResourceLocation... textureLayers) {
        this.baseTexture = baseTexture;
        this.baseStyle = baseStyle;
        ResourceLocation resolvedStyleId = ModelStyles.getId(baseStyle);
        this.modelStyleId = resolvedStyleId != null ? resolvedStyleId : MKNpc.id(baseStyle.getName());
        this.entityType = entityType;
        this.defaultLook = defaultLook;
        this.layerTexture = new HashMap<>();
        setTexturesForLayers(baseStyle, textureLayers);
    }

    private ModelLook(EntityType<?> entityType, boolean defaultLook, ResourceLocation modelStyleId,
                      ResourceLocation baseTexture, Map<String, ResourceLocation> layerTextures) {
        this.baseTexture = baseTexture;
        this.baseStyle = null;
        this.modelStyleId = modelStyleId;
        this.entityType = entityType;
        this.defaultLook = defaultLook;
        this.layerTexture = new HashMap<>(layerTextures);
    }

    public void setTexturesForLayers(ModelStyle style, ResourceLocation... textures) {
        int index = 0;
        for (LayerStyle layer : style.getAdditionalLayers()) {
            layerTexture.put(layer.getLayerName(), textures[index]);
            index++;
        }
    }

    @Nullable
    public ResourceLocation getLayerTexture(String layer) {
        return layerTexture.get(layer);
    }

    public ResourceLocation getBaseTexture() {
        return baseTexture;
    }

    public ModelStyle getBaseStyle() {
        if (baseStyle == null) {
            baseStyle = NpcRegistries.MODEL_STYLES.get(modelStyleId);
            if (baseStyle == null) {
                throw new IllegalStateException("Unknown model style id " + modelStyleId);
            }
        }
        return baseStyle;
    }

    public ResourceLocation getModelStyleId() {
        return modelStyleId;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public boolean isDefaultLook() {
        return defaultLook;
    }

    public Map<String, ResourceLocation> getLayerTextures() {
        return layerTexture;
    }

    public String getStyleName(boolean isWearingArmor) {
        ModelStyle style = getBaseStyle();
        return style.needsArmorVariant() && isWearingArmor ? "%s_armored".formatted(style.getName()) : style.getName();
    }
}
