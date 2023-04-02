package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.events.ServerSideLeftClickEmpty;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerLeftClickEmptyPacket {

    public PlayerLeftClickEmptyPacket() {
    }


    public PlayerLeftClickEmptyPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null)
                return;
            MinecraftForge.EVENT_BUS.post(new ServerSideLeftClickEmpty(entity));
        });
        ctx.setPacketHandled(true);
    }
}
