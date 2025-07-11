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
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record EntityDataUpdatePacket(int entityId, List<UpdateTag> updateTags) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EntityDataUpdatePacket> TYPE = new CustomPacketPayload.Type<>(MKCore.id("entity_data_update"));

    public static final StreamCodec<FriendlyByteBuf, EntityDataUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            EntityDataUpdatePacket::entityId,
            UpdateTag.STREAM_CODEC.apply(ByteBufCodecs.list()),
            EntityDataUpdatePacket::updateTags,
            EntityDataUpdatePacket::new
    );

    public record UpdateTag(SyncVisibility visibility, CompoundTag tag) {
        public static StreamCodec<FriendlyByteBuf, UpdateTag> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(SyncVisibility.class), UpdateTag::visibility,
                ByteBufCodecs.COMPOUND_TAG, UpdateTag::tag,
                UpdateTag::new
        );
    }

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
                for (var tag : packet.updateTags) {
                    data.getSyncController().deserializeUpdate(context, tag.tag, tag.visibility);
                }
            } else if (target instanceof ISyncControllerProvider provider) {
                for (var tag : packet.updateTags) {
                    provider.getSyncController().deserializeUpdate(context, tag.tag, tag.visibility);
                }
            }
        }
    }
}

