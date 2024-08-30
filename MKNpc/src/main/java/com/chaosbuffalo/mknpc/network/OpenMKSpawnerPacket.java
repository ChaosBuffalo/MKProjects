package com.chaosbuffalo.mknpc.network;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.gui.screens.MKSpawnerScreen;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class OpenMKSpawnerPacket extends SetSpawnListPacket {

    public static final CustomPacketPayload.Type<OpenMKSpawnerPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKNpc.MODID, "open_mk_spawner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenMKSpawnerPacket> STREAM_CODEC = StreamCodec.ofMember(
            OpenMKSpawnerPacket::toBytes, OpenMKSpawnerPacket::new
    );

    public OpenMKSpawnerPacket(MKSpawnerTileEntity entity) {
        super(entity);
    }

    public OpenMKSpawnerPacket(RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final OpenMKSpawnerPacket packet, IPayloadContext context) {
        ClientHandler.handleInternal(packet);
    }

    static class ClientHandler {
        private static void handleInternal(OpenMKSpawnerPacket packet) {
            if (Minecraft.getInstance().player != null) {
                Level world = Minecraft.getInstance().player.getCommandSenderWorld();
                BlockEntity tileEntity = world.getBlockEntity(packet.tileEntityLoc);
                if (tileEntity instanceof MKSpawnerTileEntity spawner) {
                    packet.setSpawnerFromPacket(spawner);
                    Minecraft.getInstance().setScreen(new MKSpawnerScreen(spawner));
                }
            }
        }
    }
}
