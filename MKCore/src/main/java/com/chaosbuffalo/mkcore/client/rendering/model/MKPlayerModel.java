package com.chaosbuffalo.mkcore.client.rendering.model;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.PlayerCompleteCastAnimation;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

public class MKPlayerModel extends PlayerModel<AbstractClientPlayer> {
    private final BipedCastAnimation<Player> castAnimation = new BipedCastAnimation<>(this);
    private final PlayerCompleteCastAnimation completeCastAnimation = new PlayerCompleteCastAnimation(this);

    public MKPlayerModel(ModelPart p_170821_, boolean p_170822_) {
        super(p_170821_, p_170822_);
    }

    @Override
    public void setupAnim(AbstractClientPlayer entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        entityIn.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(mkEntityData -> {
            AdditionalBipedAnimation<Player> animation = getAdditionalAnimation(mkEntityData);
            if (animation != null) {
                animation.apply(entityIn);
            }
        });
    }


    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

    }

    public AdditionalBipedAnimation<Player> getAdditionalAnimation(MKPlayerData playerData) {
        switch (playerData.getAnimationModule().getPlayerVisualCastState()) {
            case CASTING:
                return castAnimation;
            case RELEASE:
                return completeCastAnimation;
            case NONE:
            default:
                return null;
        }
    }
}
