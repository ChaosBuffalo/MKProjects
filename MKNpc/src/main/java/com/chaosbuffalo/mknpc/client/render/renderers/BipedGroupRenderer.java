package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class BipedGroupRenderer<T extends MKEntity, M extends HumanoidModel<T>> extends LivingEntityRenderer<T, M> {

    private final Map<String, MKBipedRenderer<T, M>> renderers;
    private M currentModel;
    private final Map<String, ModelLook> looks;

    public BipedGroupRenderer(EntityRendererProvider.Context p_174289_) {
        super(p_174289_, null, 0.5f);
        this.renderers = new HashMap<>();
        this.looks = new HashMap<>();
        currentModel = null;
    }

    protected void putLook(String name, ModelLook look){
        if (!renderers.containsKey(look.getStyleName(false)) || !renderers.containsKey(look.getStyleName(true))){
            MKNpc.LOGGER.error("Tried to register look {} to {}, but renderer for style is missing.",
                    name, this);
            return;
        }
        this.looks.put(name, look);
    }

    protected void putRenderer(String key, MKBipedRenderer<T, M> renderer){
        renderers.put(key, renderer);
        if (currentModel == null){
            setCurrentModel(renderer.getModel());
        }
    }

    @Nullable
    protected ModelLook getLookForEntity(T entityIn){
        return looks.get(entityIn.getCurrentModelLook());
    }

    @Nullable
    protected MKBipedRenderer<T, M> getRenderer(T entityIn){
        ModelLook look = getLookForEntity(entityIn);
        if (look == null){
            return null;
        }
        MKBipedRenderer<T, M> renderer = renderers.get(look.getStyleName(
                !entityIn.getItemBySlot(EquipmentSlot.CHEST).isEmpty()));
        if (renderer != null){
            renderer.setLook(look);
        }
        return renderer;
    }

    @Override
    public M getModel() {
        return currentModel;
    }

    protected void setCurrentModel(M newModel){
        this.currentModel = newModel;
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int packedLightIn) {
        MKBipedRenderer<T, M> currentRenderer = getRenderer(entityIn);
        if (currentRenderer != null){
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
        if (renderer != null){
            return renderer.getTextureLocation(entity);
        } else {
            return getBaseTexture(entity);
        }
    }

}
