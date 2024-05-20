package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.client.gui.screens.MKSpawnerScreen;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenMKSpawnerPacket extends SetSpawnListPacket {

    public OpenMKSpawnerPacket(MKSpawnerTileEntity entity) {
        super(entity);
    }

    public OpenMKSpawnerPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleInternal(this));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        private static void handleInternal(OpenMKSpawnerPacket packet) {
            if (Minecraft.getInstance().player != null) {
                Level level = Minecraft.getInstance().player.getLevel();
                BlockEntity tileEntity = level.getBlockEntity(packet.tileEntityLoc);
                if (tileEntity instanceof MKSpawnerTileEntity spawner) {
                    packet.setSpawnerFromPacket(spawner);
                    Minecraft.getInstance().setScreen(new MKSpawnerScreen(spawner));
                }
            }
        }
    }
}
