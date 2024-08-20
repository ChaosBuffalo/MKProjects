package com.chaosbuffalo.mkcore.network.packets;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.entities.ISyncControllerProvider;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumSet;

public record EntityDataUpdatePacket(int entityId, CompoundTag updateTag, EnumSet<SyncVisibility> visibility) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EntityDataUpdatePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "entity_data_update"));

    public static final StreamCodec<ByteBuf, EnumSet<SyncVisibility>> ENUM_SET_STREAM_CODEC = StreamCodec.of((bytes, set) -> {
        bytes.writeInt(set.size());
        for (SyncVisibility visibility : set) {
          bytes.writeByte(visibility.ordinal());
      }
    }, (bytes) -> {
        EnumSet<SyncVisibility> visibility = EnumSet.noneOf(SyncVisibility.class);
        int count = bytes.readInt();
        for (int i = 0; i < count; i++) {
            visibility.add(SyncVisibility.values()[bytes.readByte()]);
        }
        return visibility;
    });

    public static final StreamCodec<ByteBuf, EntityDataUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            EntityDataUpdatePacket::entityId,
            ByteBufCodecs.COMPOUND_TAG,
            EntityDataUpdatePacket::updateTag,
            ENUM_SET_STREAM_CODEC,
            EntityDataUpdatePacket::visibility,
            EntityDataUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handlePacket(final EntityDataUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handleClient(packet))
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("mkcore.networking.failed", e.getMessage()));
                    return null;
                });
    }

    static class ClientHandler {
        public static void handleClient(final EntityDataUpdatePacket packet) {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            Entity target = level.getEntity(packet.entityId);
            if (target == null) {
                return;
            }

            if (target instanceof Player) {
                MKPlayerData data = MKCore.getPlayerOrNull(target);
                if (data != null) {
                    data.getSyncController().deserializeUpdate(target.registryAccess(), packet.updateTag, packet.visibility);
                }
            } else if (target instanceof ISyncControllerProvider provider) {
                provider.getSyncController().deserializeUpdate(target.registryAccess(), packet.updateTag, packet.visibility);
            }
        }
    }
}

