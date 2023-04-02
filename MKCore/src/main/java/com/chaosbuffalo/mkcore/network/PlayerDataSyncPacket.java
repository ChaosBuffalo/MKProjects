package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerDataSyncPacket {

    private final UUID targetUUID;
    private final boolean privateUpdate;
    private final CompoundTag updateTag;

    public PlayerDataSyncPacket(UUID targetUUID, CompoundTag updateTag, boolean privateUpdate) {
        this.targetUUID = targetUUID;
        this.privateUpdate = privateUpdate;
        this.updateTag = updateTag;
    }

    public PlayerDataSyncPacket(FriendlyByteBuf buffer) {
        targetUUID = buffer.readUUID();
        privateUpdate = buffer.readBoolean();
        updateTag = buffer.readNbt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(targetUUID);
        buffer.writeBoolean(privateUpdate);
        buffer.writeNbt(updateTag);
//        MKCore.LOGGER.info("sync toBytes priv:{} {}", privateUpdate, updateTag);
    }

    public static void handle(PlayerDataSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(PlayerDataSyncPacket packet) {
            Level world = Minecraft.getInstance().level;
            if (world == null) {
                return;
            }
            Player entity = world.getPlayerByUUID(packet.targetUUID);
            if (entity == null)
                return;

            MKCore.getPlayer(entity).ifPresent(cap ->
                    cap.getUpdateEngine().deserializeUpdate(packet.updateTag, packet.privateUpdate));
        }
    }

    public String toString() {
        return String.format("[priv: %b, tag: %s]", privateUpdate, updateTag);
    }
}
