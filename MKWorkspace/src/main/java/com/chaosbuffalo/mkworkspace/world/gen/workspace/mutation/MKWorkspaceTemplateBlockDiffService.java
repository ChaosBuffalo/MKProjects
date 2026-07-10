package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MKWorkspaceTemplateBlockDiffService {
    public MKWorkspaceTemplateBlockDiffReport comparePieceBounds(ServerLevel level,
                                                                 MKWorkspacePieceDefinition expectedGeneratedPiece,
                                                                 MKWorkspacePieceDefinition actualTemplatePiece) {
        Map<BlockPos, BlockSnapshot> expected = snapshotRelativeBlocks(level, expectedGeneratedPiece.exportBounds());
        Map<BlockPos, BlockSnapshot> actual = snapshotRelativeBlocks(level, actualTemplatePiece.exportBounds());
        Set<BlockPos> sidecars = generatedOwnedSidecarRelativePositions(expectedGeneratedPiece, actualTemplatePiece);
        return compare(expected, actual, sidecars);
    }

    public <T> MKWorkspaceTemplateBlockDiffReport compare(Map<BlockPos, T> expectedGeneratedBlocks,
                                                          Map<BlockPos, T> actualTemplateBlocks,
                                                          Set<BlockPos> generatedOwnedSidecarPositions) {
        Set<BlockPos> positions = new HashSet<>();
        positions.addAll(expectedGeneratedBlocks.keySet());
        positions.addAll(actualTemplateBlocks.keySet());
        positions.removeAll(generatedOwnedSidecarPositions);

        ArrayList<BlockPos> sortedPositions = new ArrayList<>(positions);
        sortedPositions.sort(Comparator
                .comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingInt(pos -> pos.getZ())
                .thenComparingInt(pos -> pos.getX()));

        ArrayList<BlockPos> changedPositions = new ArrayList<>();
        for (BlockPos pos : sortedPositions) {
            T expected = expectedGeneratedBlocks.get(pos);
            T actual = actualTemplateBlocks.get(pos);
            if (!Objects.equals(expected, actual)) {
                changedPositions.add(pos.immutable());
            }
        }
        return new MKWorkspaceTemplateBlockDiffReport(sortedPositions.size(), changedPositions.size(),
                changedPositions);
    }

    private Map<BlockPos, BlockSnapshot> snapshotRelativeBlocks(ServerLevel level, BoundingBox bounds) {
        HashMap<BlockPos, BlockSnapshot> blocks = new HashMap<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    BlockPos worldPos = new BlockPos(x, y, z);
                    BlockPos relativePos = relativePos(bounds, worldPos);
                    blocks.put(relativePos, snapshotBlock(level, worldPos));
                }
            }
        }
        return Map.copyOf(blocks);
    }

    private BlockSnapshot snapshotBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag blockEntityTag = null;
        if (blockEntity != null) {
            blockEntityTag = blockEntity.saveWithFullMetadata(level.registryAccess());
            blockEntityTag.remove("x");
            blockEntityTag.remove("y");
            blockEntityTag.remove("z");
        }
        return new BlockSnapshot(state, blockEntityTag);
    }

    private Set<BlockPos> generatedOwnedSidecarRelativePositions(MKWorkspacePieceDefinition expectedGeneratedPiece,
                                                                 MKWorkspacePieceDefinition actualTemplatePiece) {
        HashSet<BlockPos> positions = new HashSet<>();
        addSidecarRelativePositions(expectedGeneratedPiece, positions);
        addSidecarRelativePositions(actualTemplatePiece, positions);
        return Set.copyOf(positions);
    }

    private void addSidecarRelativePositions(MKWorkspacePieceDefinition piece, Set<BlockPos> positions) {
        addRelativeIfInside(piece.exportBounds(), piece.structureBlockPos(), positions);
        addRelativeIfInside(piece.exportBounds(), piece.signPos(), positions);
        for (BlockPos markerPos : piece.markerPositions()) {
            addRelativeIfInside(piece.exportBounds(), markerPos, positions);
        }
        for (BlockPos stairPos : piece.generatedStairPositions()) {
            addRelativeIfInside(piece.exportBounds(), stairPos, positions);
        }
    }

    private void addRelativeIfInside(BoundingBox bounds, BlockPos worldPos, Set<BlockPos> positions) {
        if (worldPos.getX() < bounds.minX() || worldPos.getX() > bounds.maxX() ||
                worldPos.getY() < bounds.minY() || worldPos.getY() > bounds.maxY() ||
                worldPos.getZ() < bounds.minZ() || worldPos.getZ() > bounds.maxZ()) {
            return;
        }
        positions.add(relativePos(bounds, worldPos));
    }

    private BlockPos relativePos(BoundingBox bounds, BlockPos worldPos) {
        return new BlockPos(
                worldPos.getX() - bounds.minX(),
                worldPos.getY() - bounds.minY(),
                worldPos.getZ() - bounds.minZ()
        );
    }

    private record BlockSnapshot(BlockState state, CompoundTag blockEntityTag) {
    }
}
