package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FinalizeMKSpawnerPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FinalizeMKSpawnerPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKNpc.MODID, "finalize_mk_spawner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FinalizeMKSpawnerPacket> STREAM_CODEC = StreamCodec.ofMember(
            FinalizeMKSpawnerPacket::toBytes, FinalizeMKSpawnerPacket::new
    );

    protected final BlockPos tileEntityLoc;


    public FinalizeMKSpawnerPacket(MKSpawnerTileEntity entity) {
        tileEntityLoc = entity.getBlockPos();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(tileEntityLoc);
    }

    public FinalizeMKSpawnerPacket(FriendlyByteBuf buffer) {
        tileEntityLoc = buffer.readBlockPos();
    }


    public static void handle(final FinalizeMKSpawnerPacket packet, IPayloadContext context) {
        Player entity = context.player();
        if (entity == null || !entity.isCreative()) {
            return;
        }
        BlockEntity tileEntity = entity.level().getBlockEntity(packet.tileEntityLoc);
        if (tileEntity instanceof MKSpawnerTileEntity spawner) {
            BlockState dataState = Blocks.STRUCTURE_BLOCK.getStateForPlacement(null);
            if (dataState != null) {
                entity.level().setBlock(packet.tileEntityLoc.above(), dataState, 3);
                BlockEntity other = entity.level().getBlockEntity(packet.tileEntityLoc.above());
                if (other instanceof StructureBlockEntity structureBlock) {
                    structureBlock.setMetaData("mkspawner");
                }
            }
            spawner.clearSpawn();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
