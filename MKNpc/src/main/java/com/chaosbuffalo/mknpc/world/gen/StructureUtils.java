package com.chaosbuffalo.mknpc.world.gen;

import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.init.MKNpcBlocks;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class StructureUtils {

    public static BlockPos getCorrectionForEvenRotation(Rotation rotation) {
        switch (rotation) {
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

    public static void handleMKDataMarker(String function, BlockPos pos, LevelAccessor worldIn, RandomSource rand, BoundingBox sbb,
                                          ResourceLocation structureName, UUID instanceId) {
        if (function.equals("mkspawner")) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos.below());
            if (blockEntity instanceof MKSpawnerTileEntity spawner) {
                worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                spawner.regenerateSpawnID();
                spawner.setStructureName(structureName);
                spawner.setStructureId(instanceId);
            }
        } else if (function.startsWith("mkcontainer")) {
            String[] names = function.split("#", 2);
            BlockEntity blockEntity = worldIn.getBlockEntity(pos.below());
            if (blockEntity instanceof ChestBlockEntity) {
                worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                blockEntity.getCapability(NpcCapabilities.CHEST_NPC_DATA_CAPABILITY).ifPresent(x -> {
                    x.setStructureId(instanceId);
                    x.setStructureName(structureName);
                    if (names.length == 2) {
                        String labels = names[1];
                        x.generateChestId(labels);
                    }
                });
            }
        } else if (function.startsWith("mkpoi")) {
            String[] names = function.split("#", 2);
            String tag = names[1];
            worldIn.destroyBlock(pos, false);
            worldIn.setBlock(pos, MKNpcBlocks.MK_POI_BLOCK.get().defaultBlockState(), 3);
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof MKPoiTileEntity poi) {
                poi.regenerateId();
                poi.setStructureId(instanceId);
                poi.setStructureName(structureName);
                poi.setPoiTag(tag);
            }
        }
    }

    public static List<StructureStart> getStructuresOverlaps(Entity entity) {
        if (entity.getLevel() instanceof ServerLevel serverLevel) {
            var manager = serverLevel.structureManager();
            return WorldStructureHandler.MK_STRUCTURE_INDEX.values().stream()
                    .map(x -> manager.getStructureAt(entity.blockPosition(), x))
                    .filter(StructureStart::isValid)
                    .toList();
        } else {
            return Collections.emptyList();
        }
    }
}
