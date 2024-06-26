package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FinalizeMKSpawnerPacket {
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


    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null || !entity.isCreative()) {
                return;
            }
            BlockEntity tileEntity = entity.getLevel().getBlockEntity(tileEntityLoc);
            if (tileEntity instanceof MKSpawnerTileEntity spawner) {
                BlockState dataState = Blocks.STRUCTURE_BLOCK.getStateForPlacement(null);
                if (dataState != null) {
                    entity.getLevel().setBlock(tileEntityLoc.above(), dataState, 3);
                    BlockEntity other = entity.getLevel().getBlockEntity(tileEntityLoc.above());
                    if (other instanceof StructureBlockEntity structureBlock) {
                        structureBlock.setMetaData("mkspawner");
                    }
                }
                spawner.clearSpawn();
            }
        });
        ctx.setPacketHandled(true);
    }
}
