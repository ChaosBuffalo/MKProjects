package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import net.minecraft.server.level.ServerPlayer;

public class PlayerEffectHandler extends EntityEffectHandler {

    public PlayerEffectHandler(MKPlayerData playerData) {
        super(playerData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    @Override
    public void onJoinWorld() {
        if (getPlayerData().isServerSide()) {
            sendAllEffectsToPlayer((ServerPlayer) entityData.getEntity());
        }
        super.onJoinWorld();
    }
}
