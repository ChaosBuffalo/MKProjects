package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class PlayerDataEvent extends Event {
    private final MKPlayerData data;

    protected PlayerDataEvent(MKPlayerData data) {
        this.data = data;
    }

    public Player getPlayer() {
        return getPlayerData().getEntity();
    }

    public MKPlayerData getPlayerData() {
        return data;
    }

    public static class Updated extends PlayerDataEvent {

        public Updated(MKPlayerData data) {
            super(data);
        }
    }
}
