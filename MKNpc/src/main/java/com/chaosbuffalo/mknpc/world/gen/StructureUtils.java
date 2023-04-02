package com.chaosbuffalo.mknpc.world.gen;

import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.init.MKNpcBlocks;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class StructureUtils {

    public static BlockPos getCorrectionForEvenRotation(Rotation rotation){
        switch (rotation){
            case CLOCKWISE_90:
                return new BlockPos(-1, 0, 0);
            case COUNTERCLOCKWISE_90:
                return new BlockPos(0, 0, -1);
            case CLOCKWISE_180:
                return new BlockPos(-1, 0, -1);
            case NONE:
            default:
                return new BlockPos(0, 0, 0);
        }
    }

    public static void handleMKDataMarker(String function, BlockPos pos, LevelAccessor worldIn, Random rand, BoundingBox sbb,
                                          ResourceLocation structureName, UUID instanceId)
    {
        if (function.equals("mkspawner")) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos.below());
            if (tileentity instanceof MKSpawnerTileEntity) {
                worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                MKSpawnerTileEntity spawner = (MKSpawnerTileEntity) tileentity;
                spawner.regenerateSpawnID();
                spawner.setStructureName(structureName);
                spawner.setStructureId(instanceId);
            }
        } else if (function.startsWith("mkcontainer")){
            String[] names = function.split("#", 2);
            String labels = names[1];
            BlockEntity tileEntity = worldIn.getBlockEntity(pos.below());
            if (tileEntity instanceof ChestBlockEntity){
                worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                tileEntity.getCapability(NpcCapabilities.CHEST_NPC_DATA_CAPABILITY).ifPresent(x ->{
                    x.setStructureId(instanceId);
                    x.setStructureName(structureName);
                    x.generateChestId(labels);
                });
            }
        } else if (function.startsWith("mkpoi")) {
            String[] names = function.split("#", 2);
            String tag = names[1];
            worldIn.destroyBlock(pos, false);
            worldIn.setBlock(pos, MKNpcBlocks.MK_POI_BLOCK.get().defaultBlockState(), 3);
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if (tile instanceof MKPoiTileEntity) {
                MKPoiTileEntity poi = (MKPoiTileEntity) tile;
                poi.regenerateId();
                poi.setStructureId(instanceId);
                poi.setStructureName(structureName);
                poi.setPoiTag(tag);
            }
        }
    }

    public static Optional<List<StructureStart>> getStructuresOverlaps(Entity entity) {
        if (entity.getCommandSenderWorld() instanceof ServerLevel){
            StructureFeatureManager manager = ((ServerLevel) entity.getCommandSenderWorld()).structureFeatureManager();
            return Optional.of(WorldStructureHandler.MK_STRUCTURE_CACHE.stream().map(
                    x -> manager.getStructureAt(entity.blockPosition(), x)).filter(x -> x != StructureStart.INVALID_START)
                    .collect(Collectors.toList()));
        } else {
            return Optional.empty();
        }
    }
}
