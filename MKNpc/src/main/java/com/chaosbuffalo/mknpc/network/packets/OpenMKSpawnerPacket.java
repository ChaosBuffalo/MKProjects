package com.chaosbuffalo.mknpc.network.packets;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.gui.screens.MKSpawnerScreen;
import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;


public class OpenMKSpawnerPacket extends SetSpawnListPacket {

    public static final CustomPacketPayload.Type<OpenMKSpawnerPacket> TYPE = new CustomPacketPayload.Type<>(
            MKNpc.id("open_mk_spawner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMKSpawnerPacket> STREAM_CODEC = StreamCodec.ofMember(
            OpenMKSpawnerPacket::toBytes, OpenMKSpawnerPacket::new
    );

    public OpenMKSpawnerPacket(MKSpawnerBlockEntity entity) {
        super(entity);
    }

    public OpenMKSpawnerPacket(RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final OpenMKSpawnerPacket packet, IPayloadContext context) {
        ClientHandler.handleInternal(packet, context.player());
    }

    static class ClientHandler {
        private static void handleInternal(OpenMKSpawnerPacket packet, @Nonnull Player player) {
            Level level = player.level();
            BlockEntity blockEntity = level.getBlockEntity(packet.blockEntityPos);
            if (blockEntity instanceof MKSpawnerBlockEntity spawner) {
                packet.setSpawnerFromPacket(spawner);
                Minecraft.getInstance().setScreen(new MKSpawnerScreen(spawner));
            }
        }
    }
}
