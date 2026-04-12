package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKGolemModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.client.render.skeleton.GolemSkeleton;
import com.chaosbuffalo.mknpc.entity.MKGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

public class GolemRenderer extends MKBipedRenderer<MKGolemEntity, MKGolemModel<MKGolemEntity>> {
    public GolemRenderer(EntityRendererProvider.Context context, ModelStyle style, ResourceLocation entityType) {
        super(context, style, 1.25f, MKGolemModel::new, entityType);
        this.skeleton = new GolemSkeleton<>(getModel());
    }

    @Override
    protected float getVisualLungeAmount(MKGolemEntity entity) {
        return 0.45f;
    }

    @Override
    protected float getVisualLungeOffset(MKGolemEntity entity, float partialTicks) {
        if (entity.getCombatMoveType() != MKGolemEntity.CombatMoveType.MELEE) {
            return 0.0F;
        }
        if (entity.getVisualCastState() != MKGolemEntity.VisualCastState.NONE) {
            return 0.0F;
        }
        float attackAnim = entity.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
        float impact = Mth.sin((1.0F - (1.0F - attackAnim) * (1.0F - attackAnim)) * Mth.PI);
        float settle = 1.0F - Mth.clamp((attackAnim - 0.52F) / 0.22F, 0.0F, 1.0F);
        settle = Mth.sin(settle * (Mth.PI / 2.0F));
        return impact * settle * getVisualLungeAmount(entity);
    }
}
