package com.chaosbuffalo.mkcore.client.rendering.animations;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;

public class PlayerCompleteCastAnimation extends BipedCompleteCastAnimation<Player> {
    public PlayerCompleteCastAnimation(HumanoidModel<?> model) {
        super(model);
    }

    @Override
    protected int getCastAnimTimer(Player entity) {
        return MKCore.getPlayer(entity)
                .map(playerData -> playerData.getAnimationModule().getCastAnimTimer()).orElse(0);
    }
}
