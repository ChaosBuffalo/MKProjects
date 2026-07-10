package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKWorkspaceAnchor;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVoidMarginTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MKWorkspaceMarginExpansionService {
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspaceGridLayout gridLayout = new MKWorkspaceGridLayout();

    public record ExpansionResult(MKStructureWorkspace workspace, Path backupPath, int movedPieceCount,
                                  int movedBlockCount, int filledBlockCount) {
    }

    private record PieceExpansion(MKWorkspacePieceDefinition original, MKWorkspacePieceDefinition expanded,
                                  BlockPos contentDelta) {
    }

    private record BlockSnapshot(BlockState state, CompoundTag blockEntityTag) {
    }

    public Optional<ExpansionResult> expandMargins(ServerLevel level, MKStructureWorkspace workspace,
                                                   int shellMargin, int exteriorAirMargin) throws IOException {
        if (workspace.pieces().isEmpty() || shellMargin < workspace.shellMargin() ||
                exteriorAirMargin < workspace.exteriorAirMargin()) {
            return Optional.empty();
        }
        if (shellMargin == workspace.shellMargin() && exteriorAirMargin == workspace.exteriorAirMargin()) {
            return Optional.of(new ExpansionResult(workspace, backupWriter
                    .writeBeforeMutation(level, workspace, "margin-expansion-noop").path(), 0, 0, 0));
        }

        MKStructureWorkspace targetWorkspace = withMargins(workspace, shellMargin, exteriorAirMargin);
        List<PieceExpansion> expansions = buildExpansions(targetWorkspace, workspace.pieces());
        Map<BlockPos, BlockSnapshot> snapshots = snapshotSources(level, expansions);
        Map<BlockPos, BlockSnapshot> destinations = mapDestinations(expansions, snapshots);
        preflightDestinations(level, collectSourcePositions(expansions), destinations.keySet(), expansions);

        MKWorkspaceBackupManifestWriter.WrittenBackup backup =
                backupWriter.writeBeforeMutation(level, workspace, "margin-expansion");
        clearSources(level, collectSourcePositions(expansions));
        placeDestinations(level, destinations);
        int filled = fillExpandedShells(level, targetWorkspace, expansions, destinations.keySet());
        placeControls(level, targetWorkspace, expansions);

        MKStructureWorkspace updated = targetWorkspace.withPieces(expansions.stream()
                .map(PieceExpansion::expanded)
                .toList());
        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, updated);
        return Optional.of(new ExpansionResult(updated, backup.path(), expansions.size(), destinations.size(), filled));
    }

    private List<PieceExpansion> buildExpansions(MKStructureWorkspace targetWorkspace,
                                                 List<MKWorkspacePieceDefinition> pieces) {
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
        List<PieceExpansion> expansions = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            MKWorkspacePieceDefinition original = pieces.get(i);
            MKPlannedPiece plannedPiece = plannedPieces.get(i);
            PieceContext context = createContext(targetWorkspace, plannedPiece, placements.get(i));
            BlockPos contentDelta = getInteriorOrigin(context.exportOrigin(), targetWorkspace.exteriorAirMargin(),
                    context.shellMargin(), context.verticalShellThickness(), getBottomVoidMargin(plannedPiece),
                    isEmptyScaffold(original))
                    .subtract(getInteriorOrigin(original.worldOrigin(), originalExteriorMargin(original),
                            original.shellMargin(), context.verticalShellThickness(), getBottomVoidMargin(original),
                            isEmptyScaffold(original)));
            if (isEmptyScaffold(original)) {
                contentDelta = context.exportOrigin().subtract(original.worldOrigin());
            }
            MKWorkspacePieceDefinition expanded = expandedPiece(targetWorkspace, original, context, contentDelta);
            expansions.add(new PieceExpansion(original, expanded, contentDelta));
        }
        return expansions;
    }

    private int originalExteriorMargin(MKWorkspacePieceDefinition piece) {
        return isEmptyScaffold(piece) ? 0 : Math.max(0,
                (piece.exportBounds().getXSpan() - piece.effectiveDimensions().roomWidth() - (2 * piece.shellMargin())) / 2);
    }

    private BlockPos getInteriorOrigin(BlockPos exportOrigin, int exteriorMargin, int shellMargin,
                                       int verticalShellThickness, int bottomVoidMargin, boolean emptyScaffold) {
        if (emptyScaffold) {
            return exportOrigin;
        }
        return exportOrigin.offset(exteriorMargin + shellMargin, bottomVoidMargin + verticalShellThickness,
                exteriorMargin + shellMargin);
    }

    private MKWorkspacePieceDefinition expandedPiece(MKStructureWorkspace workspace, MKWorkspacePieceDefinition original,
                                                     PieceContext context, BlockPos contentDelta) {
        List<MKWorkspaceConnectorDefinition> connectors = original.connectors().stream()
                .map(connector -> expandedConnector(original, context, contentDelta, connector))
                .toList();
        return new MKWorkspacePieceDefinition(
                original.pieceId(),
                workspace.id(),
                original.pieceName(),
                original.roleId(),
                original.variantIndex(),
                original.effectiveDimensions(),
                context.shellMargin(),
                connectors,
                context.exportOrigin(),
                context.exportBounds(),
                context.previewBounds(),
                context.structureBlockPos(),
                context.signPos(),
                connectors.stream().map(connector -> markerPos(connector, context.exportBounds())).toList(),
                original.generatedStairPositions().stream().map(pos -> pos.offset(contentDelta)).toList(),
                new LinkedHashMap<>(original.tags())
        );
    }

    private MKWorkspaceConnectorDefinition expandedConnector(MKWorkspacePieceDefinition original, PieceContext context,
                                                             BlockPos contentDelta,
                                                             MKWorkspaceConnectorDefinition connector) {
        BlockPos oldPos = original.worldOrigin().offset(connector.relativePos());
        BlockPos newPos = oldPos.offset(contentDelta);
        newPos = switch (connector.facing()) {
            case NORTH -> new BlockPos(newPos.getX(), newPos.getY(), context.exportBounds().minZ());
            case SOUTH -> new BlockPos(newPos.getX(), newPos.getY(), context.exportBounds().maxZ());
            case WEST -> new BlockPos(context.exportBounds().minX(), newPos.getY(), newPos.getZ());
            case EAST -> new BlockPos(context.exportBounds().maxX(), newPos.getY(), newPos.getZ());
            case UP -> new BlockPos(newPos.getX(), context.exportBounds().maxY(), newPos.getZ());
            case DOWN -> new BlockPos(newPos.getX(), context.exportBounds().minY(), newPos.getZ());
        };
        return new MKWorkspaceConnectorDefinition(
                connector.role(),
                connector.facing(),
                newPos.subtract(context.exportOrigin()),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                connector.jigsawName(),
                connector.jigsawTarget(),
                connector.targetPool(),
                connector.incomingPool()
        );
    }

    private Map<BlockPos, BlockSnapshot> snapshotSources(ServerLevel level, List<PieceExpansion> expansions) {
        LinkedHashMap<BlockPos, BlockSnapshot> snapshots = new LinkedHashMap<>();
        for (PieceExpansion expansion : expansions) {
            Set<BlockPos> excluded = connectorPositions(expansion.original());
            forEachPos(expansion.original().exportBounds(), pos -> {
                if (!excluded.contains(pos)) {
                    snapshots.put(pos.immutable(), snapshot(level, pos));
                }
            });
        }
        return snapshots;
    }

    private Map<BlockPos, BlockSnapshot> mapDestinations(List<PieceExpansion> expansions,
                                                         Map<BlockPos, BlockSnapshot> snapshots) {
        LinkedHashMap<BlockPos, BlockSnapshot> destinations = new LinkedHashMap<>();
        for (PieceExpansion expansion : expansions) {
            Set<BlockPos> excluded = connectorPositions(expansion.original());
            forEachPos(expansion.original().exportBounds(), sourcePos -> {
                if (excluded.contains(sourcePos)) {
                    return;
                }
                BlockSnapshot snapshot = snapshots.get(sourcePos);
                if (snapshot == null) {
                    return;
                }
                BlockPos destination = sourcePos.offset(expansion.contentDelta()).immutable();
                if (contains(expansion.expanded().exportBounds(), destination) &&
                        destinations.putIfAbsent(destination, snapshot) != null) {
                    throw new IllegalStateException("workspace margin expansion produced overlapping destination " +
                            destination);
                }
            });
        }
        return destinations;
    }

    private void preflightDestinations(ServerLevel level, Set<BlockPos> sourcePositions, Set<BlockPos> destinations,
                                       List<PieceExpansion> expansions) {
        LinkedHashSet<BlockPos> checked = new LinkedHashSet<>(destinations);
        for (PieceExpansion expansion : expansions) {
            forEachPos(expansion.expanded().exportBounds(), pos -> checked.add(pos.immutable()));
            checked.add(expansion.expanded().structureBlockPos());
            checked.add(expansion.expanded().signPos());
            checked.addAll(expansion.expanded().markerPositions());
        }
        for (BlockPos pos : checked) {
            if (sourcePositions.contains(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.is(Blocks.STRUCTURE_VOID)) {
                throw new IllegalStateException("workspace margin expansion destination is blocked at " + pos);
            }
        }
    }

    private Set<BlockPos> collectSourcePositions(List<PieceExpansion> expansions) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        for (PieceExpansion expansion : expansions) {
            forEachPos(expansion.original().exportBounds(), pos -> positions.add(pos.immutable()));
            positions.add(expansion.original().structureBlockPos());
            positions.add(expansion.original().signPos());
            positions.addAll(expansion.original().markerPositions());
        }
        return positions;
    }

    private void clearSources(ServerLevel level, Iterable<BlockPos> sourcePositions) {
        for (BlockPos sourcePos : sourcePositions) {
            level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void placeDestinations(ServerLevel level, Map<BlockPos, BlockSnapshot> destinations) {
        for (Map.Entry<BlockPos, BlockSnapshot> entry : destinations.entrySet()) {
            BlockPos destination = entry.getKey();
            BlockSnapshot snapshot = entry.getValue();
            level.setBlock(destination, snapshot.state(), Block.UPDATE_ALL);
            restoreBlockEntity(level, destination, snapshot);
        }
    }

    private int fillExpandedShells(ServerLevel level, MKStructureWorkspace workspace, List<PieceExpansion> expansions,
                                   Set<BlockPos> mappedPositions) {
        int filled = 0;
        for (PieceExpansion expansion : expansions) {
            PieceContext context = createContext(workspace, toPlannedPiece(expansion.expanded()), null,
                    expansion.expanded().previewBounds(), expansion.expanded().worldOrigin());
            BlockState floor = resolvePaletteState(workspace, expansion.expanded(), MKWorkspacePaletteTags.FLOOR_BLOCK_TAG,
                    workspace.palette().floorBlock(), Blocks.SMOOTH_STONE.defaultBlockState());
            BlockState wall = resolvePaletteState(workspace, expansion.expanded(), MKWorkspacePaletteTags.WALL_BLOCK_TAG,
                    workspace.palette().wallBlock(), Blocks.STONE_BRICKS.defaultBlockState());
            BlockState ceiling = resolvePaletteState(workspace, expansion.expanded(), MKWorkspacePaletteTags.CEILING_BLOCK_TAG,
                    workspace.palette().ceilingBlock(), Blocks.SMOOTH_STONE.defaultBlockState());
            forEachPos(expansion.expanded().exportBounds(), pos -> {
                if (!mappedPositions.contains(pos)) {
                    level.setBlock(pos, fillState(context, pos, floor, wall, ceiling), Block.UPDATE_ALL);
                }
            });
            carveConnectorOpenings(level, expansion.expanded());
            filled += expansion.expanded().exportBounds().getXSpan() * expansion.expanded().exportBounds().getYSpan() *
                    expansion.expanded().exportBounds().getZSpan();
        }
        return filled;
    }

    private BlockState fillState(PieceContext context, BlockPos pos, BlockState floor, BlockState wall,
                                 BlockState ceiling) {
        if (context.emptyScaffold()) {
            return Blocks.AIR.defaultBlockState();
        }
        if (!contains(context.geometryBounds(), pos)) {
            return Blocks.STRUCTURE_VOID.defaultBlockState();
        }
        boolean bottom = pos.getY() < context.geometryBounds().minY() + context.verticalShellThickness();
        boolean top = pos.getY() > context.geometryBounds().maxY() - context.verticalShellThickness();
        boolean sideWall = pos.getX() < context.geometryBounds().minX() + context.shellMargin() ||
                pos.getX() > context.geometryBounds().maxX() - context.shellMargin() ||
                pos.getZ() < context.geometryBounds().minZ() + context.shellMargin() ||
                pos.getZ() > context.geometryBounds().maxZ() - context.shellMargin();
        if (bottom) {
            return floor;
        }
        if (top) {
            return ceiling;
        }
        if (sideWall) {
            return wall;
        }
        return Blocks.AIR.defaultBlockState();
    }

    private void carveConnectorOpenings(ServerLevel level, MKWorkspacePieceDefinition piece) {
        int exteriorMargin = originalExteriorMargin(piece);
        int interiorMinX = piece.worldOrigin().getX() + exteriorMargin + piece.shellMargin();
        int interiorMaxX = interiorMinX + piece.effectiveDimensions().roomWidth() - 1;
        int interiorMinZ = piece.worldOrigin().getZ() + exteriorMargin + piece.shellMargin();
        int interiorMaxZ = interiorMinZ + piece.effectiveDimensions().roomLength() - 1;
        for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
            BlockPos pos = piece.worldOrigin().offset(connector.relativePos());
            int halfWidth = connector.openingWidth() / 2;
            if (connector.facing().getAxis().isHorizontal()) {
                for (int y = pos.getY(); y < pos.getY() + connector.openingHeight(); y++) {
                    if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
                        int startZ = connector.facing() == Direction.NORTH ? piece.exportBounds().minZ() : interiorMaxZ;
                        int endZ = connector.facing() == Direction.NORTH ? interiorMinZ : piece.exportBounds().maxZ();
                        for (int x = pos.getX() - halfWidth; x <= pos.getX() + halfWidth; x++) {
                            for (int z = startZ; z <= endZ; z++) {
                                level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    } else {
                        int startX = connector.facing() == Direction.WEST ? piece.exportBounds().minX() : interiorMaxX;
                        int endX = connector.facing() == Direction.WEST ? interiorMinX : piece.exportBounds().maxX();
                        for (int x = startX; x <= endX; x++) {
                            for (int z = pos.getZ() - halfWidth; z <= pos.getZ() + halfWidth; z++) {
                                level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
        }
    }

    private void placeControls(ServerLevel level, MKStructureWorkspace workspace, List<PieceExpansion> expansions) {
        for (PieceExpansion expansion : expansions) {
            for (MKWorkspaceConnectorDefinition connector : expansion.expanded().connectors()) {
                placeJigsaw(level, connector, expansion.expanded());
                level.setBlock(markerPos(connector, expansion.expanded().exportBounds()), getMarkerState(connector.role()),
                        Block.UPDATE_ALL);
            }
            placeStructureBlock(level, workspace, expansion.expanded());
            placeSign(level, workspace, expansion.expanded());
        }
    }

    private void placeJigsaw(ServerLevel level, MKWorkspaceConnectorDefinition connector,
                             MKWorkspacePieceDefinition piece) {
        BlockPos pos = piece.worldOrigin().offset(connector.relativePos());
        level.setBlock(pos, Blocks.JIGSAW.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof JigsawBlockEntity jigsaw) {
            jigsaw.setName(connector.jigsawName());
            jigsaw.setTarget(connector.jigsawTarget());
            jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, connector.targetPool()));
            jigsaw.setFinalState("minecraft:air");
            jigsaw.setJoint(JigsawBlockEntity.JointType.ALIGNED);
            jigsaw.setChanged();
        }
    }

    private void placeStructureBlock(ServerLevel level, MKStructureWorkspace workspace,
                                     MKWorkspacePieceDefinition piece) {
        level.setBlock(piece.structureBlockPos(), Blocks.STRUCTURE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(piece.structureBlockPos());
        if (entity instanceof StructureBlockEntity structureBlock) {
            structureBlock.setMode(StructureMode.SAVE);
            structureBlock.setIgnoreEntities(true);
            structureBlock.setShowBoundingBox(true);
            structureBlock.setStructureName(ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                    workspace.structureName() + "/" + piece.pieceName()));
            structureBlock.setStructurePos(piece.worldOrigin().subtract(piece.structureBlockPos()));
            structureBlock.setStructureSize(new Vec3i(piece.exportBounds().getXSpan(), piece.exportBounds().getYSpan(),
                    piece.exportBounds().getZSpan()));
            structureBlock.setChanged();
        }
    }

    private void placeSign(ServerLevel level, MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        level.setBlock(piece.signPos(), Blocks.OAK_SIGN.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(piece.signPos());
        if (entity instanceof SignBlockEntity sign) {
            SignText text = sign.getFrontText()
                    .setMessage(0, Component.literal(workspace.namespace()))
                    .setMessage(1, Component.literal(workspace.structureName()))
                    .setMessage(2, Component.literal(piece.roleId()))
                    .setMessage(3, Component.literal(piece.pieceName()));
            sign.setText(text, true);
            sign.setText(text, false);
            sign.setChanged();
        }
    }

    private MKPlannedPiece toPlannedPiece(MKWorkspacePieceDefinition piece) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        return new MKPlannedPiece(
                tags.getOrDefault("workspace_topology_slot_id", piece.roleId()),
                piece.pieceName(),
                piece.effectiveDimensions().roomWidth(),
                piece.effectiveDimensions().roomLength(),
                piece.effectiveDimensions().roomHeight(),
                piece.connectors().stream().map(this::toPlannedConnector).toList(),
                tags
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

    private PieceContext createContext(MKStructureWorkspace workspace, MKPlannedPiece piece,
                                       MKWorkspaceGridLayout.Placement placement) {
        return createContext(workspace, piece, placement, placement.previewBounds(),
                placement.previewOrigin().offset(workspace.previewMargin(), 0, workspace.previewMargin()));
    }

    private PieceContext createContext(MKStructureWorkspace workspace, MKPlannedPiece piece,
                                       MKWorkspaceGridLayout.Placement placement, BoundingBox previewBounds,
                                       BlockPos exportOrigin) {
        int shellMargin = getShellMargin(piece, workspace.shellMargin());
        int verticalShellThickness = getVerticalShellThickness(piece);
        boolean emptyScaffold = isEmptyScaffold(piece);
        int exteriorAirMargin = emptyScaffold ? 0 : workspace.exteriorAirMargin();
        int topVoidMargin = emptyScaffold ? 0 : getTopVoidMargin(piece);
        int bottomVoidMargin = emptyScaffold ? 0 : getBottomVoidMargin(piece);
        int exportWidth = piece.interiorWidth() + (2 * shellMargin) + (2 * exteriorAirMargin);
        int exportLength = piece.interiorLength() + (2 * shellMargin) + (2 * exteriorAirMargin);
        int bodyHeight = piece.interiorHeight() + (2 * verticalShellThickness);
        int exportHeight = bodyHeight;
        int geometryHeight = Math.max(1, bodyHeight - topVoidMargin - bottomVoidMargin);
        BlockPos geometryOrigin = exportOrigin.offset(exteriorAirMargin, bottomVoidMargin, exteriorAirMargin);
        BoundingBox exportBounds = new BoundingBox(
                exportOrigin.getX(),
                exportOrigin.getY(),
                exportOrigin.getZ(),
                exportOrigin.getX() + exportWidth - 1,
                exportOrigin.getY() + exportHeight - 1,
                exportOrigin.getZ() + exportLength - 1
        );
        BoundingBox geometryBounds = new BoundingBox(
                geometryOrigin.getX(),
                geometryOrigin.getY(),
                geometryOrigin.getZ(),
                geometryOrigin.getX() + piece.interiorWidth() + (2 * shellMargin) - 1,
                geometryOrigin.getY() + geometryHeight - 1,
                geometryOrigin.getZ() + piece.interiorLength() + (2 * shellMargin) - 1
        );
        BlockPos structureBlockPos = exportOrigin.offset(-2, 1, exportLength / 2);
        return new PieceContext(exportOrigin, exportBounds, geometryBounds, previewBounds, structureBlockPos,
                structureBlockPos.west(), shellMargin, verticalShellThickness, emptyScaffold);
    }

    private BlockState resolvePaletteState(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece,
                                           String tagName, ResourceLocation fallbackId, BlockState fallbackState) {
        String overrideId = piece.tags().get(tagName);
        ResourceLocation blockId = overrideId == null || overrideId.isBlank() ? fallbackId : ResourceLocation.parse(overrideId);
        return BuiltInRegistries.BLOCK.getOptional(blockId).orElse(fallbackState.getBlock()).defaultBlockState();
    }

    private BlockSnapshot snapshot(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = blockEntity == null ? null : blockEntity.saveWithFullMetadata(level.registryAccess());
        return new BlockSnapshot(state, tag);
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

    private Set<BlockPos> connectorPositions(MKWorkspacePieceDefinition piece) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
            positions.add(piece.worldOrigin().offset(connector.relativePos()));
        }
        return positions;
    }

    private BlockPos markerPos(MKWorkspaceConnectorDefinition connector, BoundingBox exportBounds) {
        BlockPos wallPos = new BlockPos(
                exportBounds.minX() + connector.relativePos().getX(),
                exportBounds.minY() + connector.relativePos().getY(),
                exportBounds.minZ() + connector.relativePos().getZ()
        );
        return switch (connector.facing()) {
            case NORTH -> new BlockPos(wallPos.getX(), wallPos.getY(), exportBounds.minZ() - 1);
            case SOUTH -> new BlockPos(wallPos.getX(), wallPos.getY(), exportBounds.maxZ() + 1);
            case WEST -> new BlockPos(exportBounds.minX() - 1, wallPos.getY(), wallPos.getZ());
            case EAST -> new BlockPos(exportBounds.maxX() + 1, wallPos.getY(), wallPos.getZ());
            case UP -> new BlockPos(wallPos.getX(), exportBounds.maxY() + 1, wallPos.getZ());
            case DOWN -> new BlockPos(wallPos.getX(), exportBounds.minY() - 1, wallPos.getZ());
        };
    }

    private BlockState getMarkerState(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD, MAIN_BACK -> Blocks.BLUE_WOOL.defaultBlockState();
            case CONNECT_UP, CONNECT_DOWN -> Blocks.ORANGE_WOOL.defaultBlockState();
            case TOP_CAP_FORWARD, TOP_CAP_BACK -> Blocks.RED_WOOL.defaultBlockState();
            case BRANCH -> Blocks.GREEN_WOOL.defaultBlockState();
            default -> Blocks.WHITE_WOOL.defaultBlockState();
        };
    }

    private void forEachPos(BoundingBox bounds, java.util.function.Consumer<BlockPos> consumer) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    consumer.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    private boolean contains(BoundingBox bounds, BlockPos pos) {
        return pos.getX() >= bounds.minX() && pos.getX() <= bounds.maxX() &&
                pos.getY() >= bounds.minY() && pos.getY() <= bounds.maxY() &&
                pos.getZ() >= bounds.minZ() && pos.getZ() <= bounds.maxZ();
    }

    private int getShellMargin(MKPlannedPiece piece, int shellMargin) {
        return isEmptyScaffold(piece) ? 0 : shellMargin;
    }

    private int getVerticalShellThickness(MKPlannedPiece piece) {
        return isEmptyScaffold(piece) ? 0 : 1;
    }

    private boolean isEmptyScaffold(MKPlannedPiece piece) {
        return "embedded_stair".equals(piece.tags().get("tower_piece_kind")) ||
                "floor_link_insert".equals(piece.tags().get("tower_piece_kind"));
    }

    private boolean isEmptyScaffold(MKWorkspacePieceDefinition piece) {
        return "embedded_stair".equals(piece.tags().get("tower_piece_kind")) ||
                "floor_link_insert".equals(piece.tags().get("tower_piece_kind"));
    }

    private int getTopVoidMargin(MKPlannedPiece piece) {
        if (MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) &&
                !MKWorkspaceVerticalAccessTags.isTopCap(piece.tags())) {
            return 0;
        }
        return Math.max(0, parseIntTag(piece.tags(), MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG, 0));
    }

    private int getBottomVoidMargin(MKPlannedPiece piece) {
        if (MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) &&
                !MKWorkspaceVerticalAccessTags.isBottomCap(piece.tags())) {
            return 0;
        }
        return Math.max(0, parseIntTag(piece.tags(), MKWorkspaceVoidMarginTags.BOTTOM_VOID_MARGIN_TAG, 0));
    }

    private int getBottomVoidMargin(MKWorkspacePieceDefinition piece) {
        if (MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) &&
                !MKWorkspaceVerticalAccessTags.isBottomCap(piece.tags())) {
            return 0;
        }
        return Math.max(0, parseIntTag(piece.tags(), MKWorkspaceVoidMarginTags.BOTTOM_VOID_MARGIN_TAG, 0));
    }

    private int parseIntTag(Map<String, String> tags, String tagName, int fallback) {
        try {
            return Integer.parseInt(tags.getOrDefault(tagName, Integer.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private MKStructureWorkspace withMargins(MKStructureWorkspace workspace, int shellMargin, int exteriorAirMargin) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                shellMargin,
                exteriorAirMargin,
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.insertFamilies(),
                workspace.createdAt(),
                System.currentTimeMillis(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private void syncBlockEntity(ServerLevel level, MKStructureWorkspace workspace) {
        BlockEntity blockEntity = level.getBlockEntity(workspace.anchor());
        if (blockEntity instanceof MKWorkspaceAnchor workspaceAnchor) {
            workspaceAnchor.setWorkspaceId(workspace.id());
        }
    }

    private record PieceContext(BlockPos exportOrigin, BoundingBox exportBounds, BoundingBox geometryBounds,
                                BoundingBox previewBounds, BlockPos structureBlockPos, BlockPos signPos,
                                int shellMargin, int verticalShellThickness, boolean emptyScaffold) {
    }
}
