package com.chaosbuffalo.mkcore.core.player.events;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

public class PlayerEvent<T extends MKPlayerData> {
    private final T playerData;

    public PlayerEvent(T playerData) {
        this.playerData = playerData;
    }

    public T getPlayerData() {
        return playerData;
    }
}
