package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;

public class PlayerEffectHandler extends EntityEffectHandler {

    public PlayerEffectHandler(MKPlayerData playerData) {
        super(playerData);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }
}
