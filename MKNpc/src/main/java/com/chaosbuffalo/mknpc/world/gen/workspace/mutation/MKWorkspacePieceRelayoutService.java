package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.block_entities.MKWorkspaceDevBlockEntity;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKWorkspacePieceRelayoutService {
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspaceGridLayout gridLayout = new MKWorkspaceGridLayout();

    public record RelayoutResult(MKStructureWorkspace workspace, Path backupPath, int movedPieceCount,
                                 int movedBlockCount) {
    }

    private record PieceMove(MKWorkspacePieceDefinition original, MKWorkspacePieceDefinition moved, BlockPos delta) {
    }

    private record BlockSnapshot(BlockState state, CompoundTag blockEntityTag) {
    }

    public Optional<RelayoutResult> relayoutPreviewMargin(ServerLevel level, MKStructureWorkspace workspace,
                                                          int previewMargin) throws IOException {
        if (previewMargin < 2 || workspace.pieces().isEmpty()) {
            return Optional.empty();
        }
        if (workspace.previewMargin() == previewMargin) {
            return Optional.of(new RelayoutResult(workspace, backupWriter
                    .writeBeforeMutation(level, workspace, "preview-margin-noop").path(), 0, 0));
        }

        MKWorkspaceBackupManifestWriter.WrittenBackup backup =
                backupWriter.writeBeforeMutation(level, workspace, "preview-margin-relayout");
        MKStructureWorkspace targetWorkspace = withPreviewMargin(workspace, previewMargin);
        List<PieceMove> moves = buildMoves(targetWorkspace, workspace.pieces());
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        Map<BlockPos, BlockSnapshot> snapshots = snapshotSources(level, moves);
        Map<BlockPos, BlockSnapshot> destinationSnapshots = mapDestinations(moves, snapshots);
        clearSources(level, snapshots.keySet());
        placeDestinations(level, destinationSnapshots);

        MKStructureWorkspace updated = targetWorkspace.withPieces(moves.stream().map(PieceMove::moved).toList());
        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, updated);
        return Optional.of(new RelayoutResult(updated, backup.path(), moves.size(), snapshots.size()));
    }

    private List<PieceMove> buildMoves(MKStructureWorkspace targetWorkspace, List<MKWorkspacePieceDefinition> pieces) {
        List<MKPlannedPiece> plannedPieces = pieces.stream().map(this::toPlannedPiece).toList();
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(
                targetWorkspace.anchor(),
                plannedPieces,
                targetWorkspace.shellMargin(),
                targetWorkspace.exteriorAirMargin(),
                targetWorkspace.previewMargin(),
                MKWorkspaceScaffoldBuilder.GRID_COLUMNS,
                MKWorkspaceScaffoldBuilder.CELL_PADDING
        );
        List<PieceMove> moves = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            MKWorkspacePieceDefinition original = pieces.get(i);
            MKWorkspaceGridLayout.Placement placement = placements.get(i);
            BlockPos newOrigin = placement.previewOrigin()
                    .offset(targetWorkspace.previewMargin(), 0, targetWorkspace.previewMargin());
            BlockPos delta = newOrigin.subtract(original.worldOrigin());
            moves.add(new PieceMove(original, movedPiece(targetWorkspace, original, placement.previewBounds(), delta), delta));
        }
        return moves;
    }

    private MKWorkspacePieceDefinition movedPiece(MKStructureWorkspace workspace, MKWorkspacePieceDefinition original,
                                                  BoundingBox previewBounds, BlockPos delta) {
        return new MKWorkspacePieceDefinition(
                original.pieceId(),
                workspace.id(),
                original.pieceName(),
                original.role(),
                original.variantIndex(),
                original.effectiveDimensions(),
                original.shellMargin(),
                original.connectors(),
                original.worldOrigin().offset(delta),
                shift(original.exportBounds(), delta),
                previewBounds,
                original.structureBlockPos().offset(delta),
                original.signPos().offset(delta),
                original.markerPositions().stream().map(pos -> pos.offset(delta)).toList(),
                original.generatedStairPositions().stream().map(pos -> pos.offset(delta)).toList(),
                new LinkedHashMap<>(original.tags())
        );
    }

    private MKPlannedPiece toPlannedPiece(MKWorkspacePieceDefinition piece) {
        return new MKPlannedPiece(
                piece.role(),
                piece.pieceName(),
                piece.effectiveDimensions().roomWidth(),
                piece.effectiveDimensions().roomLength(),
                piece.effectiveDimensions().roomHeight(),
                piece.connectors().stream().map(this::toPlannedConnector).toList(),
                new LinkedHashMap<>(piece.tags())
        );
    }

    private MKPlannedConnector toPlannedConnector(MKWorkspaceConnectorDefinition connector) {
        return new MKPlannedConnector(
                connector.role(),
                connector.facing(),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                connector.targetPool().toString(),
                connector.incomingPool().toString()
        );
    }

    private Map<BlockPos, BlockSnapshot> snapshotSources(ServerLevel level, List<PieceMove> moves) {
        LinkedHashMap<BlockPos, BlockSnapshot> snapshots = new LinkedHashMap<>();
        for (PieceMove move : moves) {
            for (BlockPos sourcePos : collectMovedPositions(move.original())) {
                snapshots.computeIfAbsent(sourcePos.immutable(), pos -> snapshot(level, pos));
            }
        }
        return snapshots;
    }

    private Map<BlockPos, BlockSnapshot> mapDestinations(List<PieceMove> moves, Map<BlockPos, BlockSnapshot> snapshots) {
        LinkedHashMap<BlockPos, BlockSnapshot> destinations = new LinkedHashMap<>();
        for (PieceMove move : moves) {
            for (BlockPos sourcePos : collectMovedPositions(move.original())) {
                BlockSnapshot snapshot = snapshots.get(sourcePos);
                if (snapshot == null) {
                    continue;
                }
                BlockPos destination = sourcePos.offset(move.delta()).immutable();
                if (destinations.putIfAbsent(destination, snapshot) != null) {
                    throw new IllegalStateException("workspace relayout produced overlapping destination " + destination);
                }
            }
        }
        return destinations;
    }

    private List<BlockPos> collectMovedPositions(MKWorkspacePieceDefinition piece) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        BoundingBox bounds = piece.exportBounds();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        positions.add(piece.structureBlockPos());
        positions.add(piece.signPos());
        positions.addAll(piece.markerPositions());
        return List.copyOf(positions);
    }

    private BlockSnapshot snapshot(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = blockEntity == null ? null : blockEntity.saveWithFullMetadata(level.registryAccess());
        return new BlockSnapshot(state, tag);
    }

    private void clearSources(ServerLevel level, Iterable<BlockPos> sourcePositions) {
        for (BlockPos sourcePos : sourcePositions) {
            level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void placeDestinations(ServerLevel level, Map<BlockPos, BlockSnapshot> destinationSnapshots) {
        for (Map.Entry<BlockPos, BlockSnapshot> entry : destinationSnapshots.entrySet()) {
            BlockPos destination = entry.getKey();
            BlockSnapshot snapshot = entry.getValue();
            level.setBlock(destination, snapshot.state(), Block.UPDATE_ALL);
            restoreBlockEntity(level, destination, snapshot);
        }
    }

    private void restoreBlockEntity(ServerLevel level, BlockPos destination, BlockSnapshot snapshot) {
        if (snapshot.blockEntityTag() == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(destination);
        if (blockEntity == null) {
            return;
        }
        CompoundTag tag = snapshot.blockEntityTag().copy();
        tag.putInt("x", destination.getX());
        tag.putInt("y", destination.getY());
        tag.putInt("z", destination.getZ());
        blockEntity.loadWithComponents(tag, level.registryAccess());
        blockEntity.setChanged();
        level.sendBlockUpdated(destination, snapshot.state(), snapshot.state(), Block.UPDATE_ALL);
    }

    private BoundingBox shift(BoundingBox bounds, BlockPos delta) {
        return new BoundingBox(
                bounds.minX() + delta.getX(),
                bounds.minY() + delta.getY(),
                bounds.minZ() + delta.getZ(),
                bounds.maxX() + delta.getX(),
                bounds.maxY() + delta.getY(),
                bounds.maxZ() + delta.getZ()
        );
    }

    private MKStructureWorkspace withPreviewMargin(MKStructureWorkspace workspace, int previewMargin) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                previewMargin,
                workspace.verticalAccessSpec(),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                System.currentTimeMillis(),
                workspace.pieces()
        );
    }

    private void syncBlockEntity(ServerLevel level, MKStructureWorkspace workspace) {
        BlockEntity blockEntity = level.getBlockEntity(workspace.anchor());
        if (blockEntity instanceof MKWorkspaceDevBlockEntity workspaceDevBlockEntity) {
            workspaceDevBlockEntity.setWorkspaceId(workspace.id());
        }
    }
}
