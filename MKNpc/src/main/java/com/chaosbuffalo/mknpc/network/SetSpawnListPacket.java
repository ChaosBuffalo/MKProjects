package com.chaosbuffalo.mknpc.network;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.spawn.SpawnList;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SetSpawnListPacket implements CustomPacketPayload {
    protected final BlockPos tileEntityLoc;
    protected final SpawnList spawnList;
    protected final int spawnTime;
    protected final MKEntity.NonCombatMoveType moveType;

    public static final CustomPacketPayload.Type<SetSpawnListPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKNpc.MODID, "set_spawn_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetSpawnListPacket> STREAM_CODEC = StreamCodec.ofMember(
            SetSpawnListPacket::toBytes, SetSpawnListPacket::new
    );

    public SetSpawnListPacket(MKSpawnerTileEntity entity) {
        tileEntityLoc = entity.getBlockPos();
        spawnList = entity.getSpawnList();
        spawnTime = entity.getRespawnTime();
        moveType = entity.getMoveType();
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(tileEntityLoc);
        buffer.writeInt(spawnTime);
        buffer.writeEnum(moveType);
        buffer.writeNbt(spawnList.serializeNBT(buffer.registryAccess()));
    }

    public SetSpawnListPacket(RegistryFriendlyByteBuf buffer) {
        tileEntityLoc = buffer.readBlockPos();
        spawnTime = buffer.readInt();
        moveType = buffer.readEnum(MKEntity.NonCombatMoveType.class);
        spawnList = new SpawnList();
        CompoundTag tag = buffer.readNbt();
        if (tag != null) {
            spawnList.deserializeNBT(buffer.registryAccess(), tag);
        }
    }

    protected void setSpawnerFromPacket(MKSpawnerTileEntity spawner) {
        spawner.setSpawnList(spawnList);
        spawner.setRespawnTime(spawnTime);
        spawner.setMoveType(moveType);
        spawner.clearSpawn();

    }

    public static void handle(final SetSpawnListPacket packet, IPayloadContext context) {
        Player entity = context.player();
        if (entity == null || !entity.isCreative()) {
            return;
        }
        BlockEntity tileEntity = entity.level().getBlockEntity(packet.tileEntityLoc);
        if (tileEntity instanceof MKSpawnerTileEntity spawner) {
            packet.setSpawnerFromPacket(spawner);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
