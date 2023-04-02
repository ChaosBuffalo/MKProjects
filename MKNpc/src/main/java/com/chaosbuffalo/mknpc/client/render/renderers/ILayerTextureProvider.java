package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;

public interface ILayerTextureProvider<T extends MKEntity, M extends EntityModel<T>> extends RenderLayerParent<T, M> {

    ResourceLocation getLayerTexture(String layerName, T entity);
}
