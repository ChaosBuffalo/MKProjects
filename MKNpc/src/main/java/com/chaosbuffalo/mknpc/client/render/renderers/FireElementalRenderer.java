package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mkcore.core.EntityAnimationModule;
import com.chaosbuffalo.mknpc.client.render.models.MKFireElementalModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

public class FireElementalRenderer extends MKBipedRenderer<MKEntity, MKFireElementalModel<MKEntity>> {
    public FireElementalRenderer(EntityRendererProvider.Context context, ModelStyle style, ResourceLocation entityType) {
        super(context, style, 0.45f, MKFireElementalModel::new, entityType);
    }

    @Override
    protected float getVisualLungeAmount(MKEntity entity) {
        return 0.28f;
    }

    @Override
    protected float getVisualLungeOffset(MKEntity entity, float partialTicks) {
        if (entity.getCombatMoveType() != MKEntity.CombatMoveType.MELEE) {
            return 0.0F;
        }
        if (entity.getEntityDataCap().getAnimationModule().getVisualCastState() != EntityAnimationModule.VisualCastState.NONE) {
            return 0.0F;
        }
        float attackAnim = entity.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
        float impact = Mth.sin((1.0F - (1.0F - attackAnim) * (1.0F - attackAnim)) * Mth.PI);
        float settle = 1.0F - Mth.clamp((attackAnim - 0.5F) / 0.2F, 0.0F, 1.0F);
        settle = Mth.sin(settle * (Mth.PI / 2.0F));
        return impact * settle * getVisualLungeAmount(entity);
    }
}
