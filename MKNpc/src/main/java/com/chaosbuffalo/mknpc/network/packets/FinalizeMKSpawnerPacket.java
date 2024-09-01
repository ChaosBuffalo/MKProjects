package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class FinalizeMKSpawnerPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FinalizeMKSpawnerPacket> TYPE = new CustomPacketPayload.Type<>(
            MKNpc.id("finalize_mk_spawner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FinalizeMKSpawnerPacket> STREAM_CODEC = StreamCodec.ofMember(
            FinalizeMKSpawnerPacket::toBytes, FinalizeMKSpawnerPacket::new
    );

    protected final BlockPos blockEntityPos;

    public FinalizeMKSpawnerPacket(MKSpawnerTileEntity entity) {
        blockEntityPos = entity.getBlockPos();
    }

    public FinalizeMKSpawnerPacket(FriendlyByteBuf buffer) {
        blockEntityPos = buffer.readBlockPos();
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockEntityPos);
    }

    public static void handle(final FinalizeMKSpawnerPacket packet, IPayloadContext context) {
        Player entity = context.player();
        if (!entity.isCreative()) {
            return;
        }
        BlockEntity blockEntity = entity.level().getBlockEntity(packet.blockEntityPos);
        if (blockEntity instanceof MKSpawnerTileEntity spawner) {
            BlockState dataState = Blocks.STRUCTURE_BLOCK.getStateForPlacement(null);
            if (dataState != null) {
                entity.level().setBlock(packet.blockEntityPos.above(), dataState, 3);
                BlockEntity other = entity.level().getBlockEntity(packet.blockEntityPos.above());
                if (other instanceof StructureBlockEntity structureBlock) {
                    structureBlock.setMetaData("mkspawner");
                }
            }
            spawner.clearSpawn();
        }
    }
}
