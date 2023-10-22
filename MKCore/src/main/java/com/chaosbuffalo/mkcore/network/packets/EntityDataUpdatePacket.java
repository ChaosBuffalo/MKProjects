package com.chaosbuffalo.mkcore.network.packets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.entities.ISyncControllerProvider;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumSet;
import java.util.function.Supplier;

public class EntityDataUpdatePacket {

    private final int targetId;
    private final EnumSet<SyncVisibility> visibility;
    private final CompoundTag updateTag;

    public EntityDataUpdatePacket(Entity entity, CompoundTag updateTag, EnumSet<SyncVisibility> visibility) {
        this.targetId = entity.getId();
        this.visibility = visibility;
        this.updateTag = updateTag;
    }

    public EntityDataUpdatePacket(FriendlyByteBuf buffer) {
        targetId = buffer.readInt();
        visibility = buffer.readEnumSet(SyncVisibility.class);
        updateTag = buffer.readNbt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(targetId);
        buffer.writeEnumSet(visibility, SyncVisibility.class);
        buffer.writeNbt(updateTag);
    }

    public static void handleMainThread(EntityDataUpdatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        ClientHandler.handleClient(packet);
    }

    static class ClientHandler {
        public static void handleClient(EntityDataUpdatePacket packet) {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            Entity target = level.getEntity(packet.targetId);
            if (target == null) {
                return;
            }

            if (target instanceof Player) {
                MKPlayerData data = MKCore.getPlayerOrNull(target);
                if (data != null) {
                    data.getSyncController().deserializeUpdate(packet.updateTag, packet.visibility);
                }
            } else if (target instanceof ISyncControllerProvider provider) {
                provider.getSyncController().deserializeUpdate(packet.updateTag, packet.visibility);
            }
        }
    }

    public String toString() {
        return String.format("[tag: %s]", updateTag);
    }

}
