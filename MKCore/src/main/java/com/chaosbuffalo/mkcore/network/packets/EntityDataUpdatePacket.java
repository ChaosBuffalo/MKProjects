package com.chaosbuffalo.mkcore.network.packets;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.entities.ISyncControllerProvider;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumSet;

public record EntityDataUpdatePacket(int entityId, CompoundTag updateTag, EnumSet<SyncVisibility> visibility) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EntityDataUpdatePacket> TYPE = new CustomPacketPayload.Type<>(MKCore.id("entity_data_update"));

    public static final StreamCodec<FriendlyByteBuf, EnumSet<SyncVisibility>> ENUM_SET_STREAM_CODEC = StreamCodec.of(
            (bytes, set) -> bytes.writeEnumSet(set, SyncVisibility.class),
            (bytes) -> bytes.readEnumSet(SyncVisibility.class)
    );

    public static final StreamCodec<FriendlyByteBuf, EntityDataUpdatePacket> STREAM_CODEC = StreamCodec.composite(
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
        ClientHandler.handleClient(packet);
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

            var context = new SyncContext(target.registryAccess());
            if (target instanceof Player player) {
                MKPlayerData data = MKCore.getPlayerOrThrow(player);
                data.getSyncController().deserializeUpdate(context, packet.updateTag, packet.visibility);
            } else if (target instanceof ISyncControllerProvider provider) {
                provider.getSyncController().deserializeUpdate(context, packet.updateTag, packet.visibility);
            }
        }
    }
}

