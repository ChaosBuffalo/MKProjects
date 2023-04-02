package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.chaosbuffalo.mknpc.spawn.SpawnList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetSpawnListPacket {
    protected final BlockPos tileEntityLoc;
    protected final SpawnList spawnList;
    protected final int spawnTime;
    protected final MKEntity.NonCombatMoveType moveType;

    public SetSpawnListPacket(MKSpawnerTileEntity entity){
        tileEntityLoc = entity.getBlockPos();
        spawnList = entity.getSpawnList();
        spawnTime = entity.getRespawnTime();
        moveType = entity.getMoveType();
    }

    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeBlockPos(tileEntityLoc);
        buffer.writeInt(spawnTime);
        buffer.writeEnum(moveType);
        buffer.writeNbt(spawnList.serializeNBT());
    }

    public SetSpawnListPacket(FriendlyByteBuf buffer){
        tileEntityLoc = buffer.readBlockPos();
        spawnTime = buffer.readInt();
        moveType = buffer.readEnum(MKEntity.NonCombatMoveType.class);
        spawnList = new SpawnList();
        CompoundTag tag = buffer.readNbt();
        if (tag != null){
            spawnList.deserializeNBT(tag);
        }
    }

    protected void setSpawnerFromPacket(MKSpawnerTileEntity spawner){
        spawner.setSpawnList(spawnList);
        spawner.setRespawnTime(spawnTime);
        spawner.setMoveType(moveType);
        spawner.clearSpawn();

    }

    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null || !entity.isCreative()) {
                return;
            }
            BlockEntity tileEntity = entity.getLevel().getBlockEntity(tileEntityLoc);
            if (tileEntity instanceof MKSpawnerTileEntity){
                MKSpawnerTileEntity spawner = (MKSpawnerTileEntity) tileEntity;
                setSpawnerFromPacket(spawner);
            }
        });
        ctx.setPacketHandled(true);
    }
}
