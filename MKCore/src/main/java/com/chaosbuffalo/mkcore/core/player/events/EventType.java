package com.chaosbuffalo.mkcore.core.player.events;

import net.minecraftforge.fml.LogicalSide;

public class EventType<T extends PlayerEvent<?>> {

    LogicalSide side;

    EventType(LogicalSide side) {
        this.side = side;
    }

    public boolean canFire(boolean isRemote) {
        return this.side == null || this.side.isClient() == isRemote;
    }

    public static <TEvent extends PlayerEvent<?>> EventType<TEvent> serverSide() {
        return new EventType<>(LogicalSide.SERVER);
    }

    public static <TEvent extends PlayerEvent<?>> EventType<TEvent> clientSide() {
        return new EventType<>(LogicalSide.CLIENT);
    }

    public static <TEvent extends PlayerEvent<?>> EventType<TEvent> bothSides() {
        return new EventType<>(null);
    }
}
