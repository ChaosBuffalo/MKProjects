package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.entities.IUpdateEngineProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityDataSyncPacket {

    private final int targetID;
    private final CompoundTag updateTag;

    public EntityDataSyncPacket(int targetID, CompoundTag updateTag) {
        this.targetID = targetID;
        this.updateTag = updateTag;
    }

    public EntityDataSyncPacket(FriendlyByteBuf buffer) {
        targetID = buffer.readInt();
        updateTag = buffer.readNbt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetID);
        buffer.writeNbt(updateTag);
//        MKCore.LOGGER.info("sync toBytes priv:{} {}", privateUpdate, updateTag);
    }

    public static void handle(EntityDataSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(EntityDataSyncPacket packet) {
            Level world = Minecraft.getInstance().level;
            if (world == null) {
                return;
            }
            Entity target = world.getEntity(packet.targetID);
            if (target instanceof IUpdateEngineProvider) {
                ((IUpdateEngineProvider) target).getUpdateEngine().deserializeUpdate(packet.updateTag, false);
            }
        }
    }

    public String toString() {
        return String.format("[tag: %s]", updateTag);
    }

}
