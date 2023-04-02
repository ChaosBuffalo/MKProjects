package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.entity.MKZombifiedPiglinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;

public class ZombifiedPiglinGroupRenderer extends BipedGroupRenderer<MKZombifiedPiglinEntity, MKPiglinModel<MKZombifiedPiglinEntity>> {

    public ZombifiedPiglinGroupRenderer(EntityRendererProvider.Context context, Map<String, ModelLook> styles, ResourceLocation entityType) {
        super(context);
        putRenderer(ModelStyles.BASIC_NAME, new ZombifiedPiglinRenderer(context, ModelStyles.BASIC_STYLE, entityType));
        putRenderer(ModelStyles.CLOTHES_ONLY_NAME, new ZombifiedPiglinRenderer(context, ModelStyles.CLOTHES_ONLY_STYLE, entityType));
        putRenderer(ModelStyles.CLOTHES_ARMOR_NAME, new ZombifiedPiglinRenderer(context, ModelStyles.CLOTHES_ARMOR_STYLE, entityType));
        putRenderer(ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_NAME, new ZombifiedPiglinRenderer(context, ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE, entityType));

        for (Map.Entry<String, ModelLook> entry : styles.entrySet()) {
            putLook(entry.getKey(), entry.getValue());
        }
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(MKZombifiedPiglinEntity entity) {
        return PiglinStyles.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE;
    }
}
