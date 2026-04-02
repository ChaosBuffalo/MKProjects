package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyleClient;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.looks.ModelLookManager;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class BipedGroupRenderer<T extends MKEntity, M extends HumanoidModel<T>> extends LivingEntityRenderer<T, M> {
    private final Map<String, MKBipedRenderer<T, M>> renderers;
    private M currentModel;

    public BipedGroupRenderer(EntityRendererProvider.Context p_174289_) {
        super(p_174289_, null, 0.5f);
        this.renderers = new HashMap<>();
        currentModel = null;
    }

    protected BipedGroupRenderer(EntityRendererProvider.Context context,
                                 ResourceLocation entityType,
                                 Function<ModelStyle, MKBipedRenderer<T, M>> rendererProvider) {
        this(context);
        putRegisteredRenderers(entityType, rendererProvider);
    }

    protected void putRenderer(String key, MKBipedRenderer<T, M> renderer) {
        renderers.put(key, renderer);
        if (currentModel == null) {
            setCurrentModel(renderer.getModel());
        }
    }

    protected void putRegisteredRenderers(ResourceLocation entityType,
                                          Function<ModelStyle, MKBipedRenderer<T, M>> rendererProvider) {
        for (ModelStyle style : ModelStyleClient.getRegisteredStyles(entityType)) {
            putRenderer(style.getName(), rendererProvider.apply(style));
        }
    }

    @Nullable
    protected ModelLook getLookForEntity(T entityIn) {
        return ModelLookManager.getLook(entityIn.level().registryAccess(), entityIn.getType(), entityIn.getCurrentModelLook());
    }

    @Nullable
    protected MKBipedRenderer<T, M> getRenderer(T entityIn) {
        ModelLook look = getLookForEntity(entityIn);
        if (look == null) {
            return null;
        }
        MKBipedRenderer<T, M> renderer = renderers.get(look.getStyleName(
                !entityIn.getItemBySlot(EquipmentSlot.CHEST).isEmpty()));
        if (renderer != null) {
            renderer.setLook(look);
        }
        return renderer;
    }

    @Override
    public M getModel() {
        return currentModel;
    }

    protected void setCurrentModel(M newModel) {
        this.currentModel = newModel;
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int packedLightIn) {
        MKBipedRenderer<T, M> currentRenderer = getRenderer(entityIn);
        if (currentRenderer != null) {
            setCurrentModel(currentRenderer.getModel());
            currentRenderer.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            this.shadowRadius = currentRenderer.shadowRadius;
        } else {
            MKNpc.LOGGER.error("No renderer group named {} found for {}",
                    entityIn.getCurrentModelLook(), entityIn);
        }
    }

    @Nonnull
    public abstract ResourceLocation getBaseTexture(T entity);

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        MKBipedRenderer<T, M> renderer = getRenderer(entity);
        if (renderer != null) {
            return renderer.getTextureLocation(entity);
        } else {
            return getBaseTexture(entity);
        }
    }

}
