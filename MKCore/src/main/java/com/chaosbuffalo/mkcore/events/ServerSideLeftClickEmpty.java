package com.chaosbuffalo.mkcore.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ServerSideLeftClickEmpty extends PlayerEvent {
    private final InteractionHand hand;
    private final BlockPos pos;

    public ServerSideLeftClickEmpty(Player player) {
        super(player);
        this.hand = InteractionHand.MAIN_HAND;
        this.pos = player.blockPosition();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public BlockPos getPos() {
        return pos;
    }
}