package com.chaosbuffalo.mknpc.world.gen;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.init.MKNpcBlocks;
import com.chaosbuffalo.mknpc.block_entities.MKPoiBlockEntity;
import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
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

    public static void handleMKDataMarker(String function, BlockPos pos, WorldGenLevel worldIn, RandomSource rand, BoundingBox sbb,
                                          ResourceLocation structureName, UUID instanceId) {
        if (function.equals("mkspawner")) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos.below());
            if (blockEntity instanceof MKSpawnerBlockEntity spawner) {
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
                IChestNpcData.get(blockEntity).ifPresent(x -> {
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
            worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (tag == null || tag.isEmpty()) {
                MKNpc.LOGGER.error("MKPOI with null or empty tag ({}) attempted to load for {}, skipping", function, structureName);
                return;
            }
            worldIn.getServer().tell(new TickTask(0, () -> {
                ContentDB.getPrimaryData().addPointOfInterest(new GlobalPos(worldIn.getLevel().dimension(), pos),
                        tag, instanceId, UUID.randomUUID(), structureName);
            }));
        }
    }

    public static List<StructureStart> getStructuresOverlaps(Entity entity) {
        if (entity.getCommandSenderWorld() instanceof ServerLevel serverLevel) {
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
