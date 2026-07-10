package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MKWorkspaceBlockSwapService {
    public record BlockSwapStats(ResourceLocation sourceBlock, ResourceLocation targetBlock, int replacedCount,
                                 Map<String, Integer> droppedProperties) {
    }

    public record BlockSwapResult(Map<ResourceLocation, BlockSwapStats> statsBySource) {
        public int totalReplaced() {
            return statsBySource.values().stream().mapToInt(BlockSwapStats::replacedCount).sum();
        }
    }

    private record Replacement(Block targetBlock, ResourceLocation targetId) {
    }

    private record BlockEntitySnapshot(BlockEntityType<?> type, CompoundTag tag) {
    }

    private final MKWorkspaceBlockStateMapper stateMapper = new MKWorkspaceBlockStateMapper();

    public BlockSwapResult swapBlocks(ServerLevel level, BoundingBox bounds,
                                      Map<ResourceLocation, ResourceLocation> replacements,
                                      Set<BlockPos> excludedPositions) {
        Map<ResourceLocation, Replacement> resolvedReplacements = resolveReplacements(replacements);
        Map<ResourceLocation, MutableStats> stats = new LinkedHashMap<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (excludedPositions.contains(pos)) {
                        continue;
                    }
                    BlockState sourceState = level.getBlockState(pos);
                    if (isWorkspaceControlBlock(sourceState)) {
                        continue;
                    }
                    ResourceLocation sourceId = BuiltInRegistries.BLOCK.getKey(sourceState.getBlock());
                    Replacement replacement = resolvedReplacements.get(sourceId);
                    if (replacement == null) {
                        continue;
                    }
                    MKWorkspaceBlockStateMapper.MappedState mappedState =
                            stateMapper.mapToBlock(sourceState, replacement.targetBlock());
                    Optional<BlockEntitySnapshot> blockEntitySnapshot = snapshotCompatibleBlockEntity(level, pos);
                    level.setBlock(pos, mappedState.state(), Block.UPDATE_ALL);
                    restoreCompatibleBlockEntity(level, pos, blockEntitySnapshot);
                    MutableStats mutableStats = stats.computeIfAbsent(sourceId,
                            ignored -> new MutableStats(sourceId, replacement.targetId()));
                    mutableStats.replacedCount++;
                    for (String droppedProperty : mappedState.droppedProperties()) {
                        mutableStats.droppedProperties.merge(droppedProperty, 1, Integer::sum);
                    }
                }
            }
        }
        Map<ResourceLocation, BlockSwapStats> immutableStats = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, MutableStats> entry : stats.entrySet()) {
            MutableStats value = entry.getValue();
            immutableStats.put(entry.getKey(), new BlockSwapStats(value.sourceBlock, value.targetBlock,
                    value.replacedCount, Map.copyOf(value.droppedProperties)));
        }
        return new BlockSwapResult(Map.copyOf(immutableStats));
    }

    private boolean isWorkspaceControlBlock(BlockState state) {
        return state.is(Blocks.JIGSAW) || state.is(Blocks.STRUCTURE_BLOCK);
    }

    private Map<ResourceLocation, Replacement> resolveReplacements(Map<ResourceLocation, ResourceLocation> replacements) {
        Map<ResourceLocation, Replacement> resolved = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : replacements.entrySet()) {
            BuiltInRegistries.BLOCK.getOptional(entry.getValue())
                    .ifPresent(block -> resolved.put(entry.getKey(), new Replacement(block, entry.getValue())));
        }
        return resolved;
    }

    private Optional<BlockEntitySnapshot> snapshotCompatibleBlockEntity(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return Optional.empty();
        }
        return Optional.of(new BlockEntitySnapshot(blockEntity.getType(),
                blockEntity.saveWithFullMetadata(level.registryAccess())));
    }

    private void restoreCompatibleBlockEntity(ServerLevel level, BlockPos pos,
                                              Optional<BlockEntitySnapshot> snapshotOpt) {
        if (snapshotOpt.isEmpty()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.getType() != snapshotOpt.get().type()) {
            return;
        }
        CompoundTag tag = snapshotOpt.get().tag().copy();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        blockEntity.loadWithComponents(tag, level.registryAccess());
        blockEntity.setChanged();
    }

    private static class MutableStats {
        private final ResourceLocation sourceBlock;
        private final ResourceLocation targetBlock;
        private int replacedCount;
        private final Map<String, Integer> droppedProperties = new LinkedHashMap<>();

        private MutableStats(ResourceLocation sourceBlock, ResourceLocation targetBlock) {
            this.sourceBlock = sourceBlock;
            this.targetBlock = targetBlock;
        }
    }
}
