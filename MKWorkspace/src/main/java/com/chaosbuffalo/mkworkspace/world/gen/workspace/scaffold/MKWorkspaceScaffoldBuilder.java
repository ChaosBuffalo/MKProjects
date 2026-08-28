package com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceGeometry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVoidMarginTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MKWorkspaceScaffoldBuilder {
    public static final int GRID_COLUMNS = 4;
    public static final int CELL_PADDING = 2;
    public static final int CLEAR_MARGIN = 4;
    private static final String LINEAR_RUN_SLOPE_DELTA_TAG = "workspace_linear_run_slope_delta";
    private static final String LINEAR_RUN_KIND_TAG = "workspace_linear_run_kind";
    private static final String HORIZONTAL_EXTRUSION_MODE_TAG = "workspace_horizontal_extrusion_mode";
    private static final String CONNECTOR_STITCH_TAG = "workspace_connector_stitch";
    private static final String CONTENT_KIND_TAG = "workspace_content_kind";
    private static final String CONTENT_CONNECTOR_EDGE_TAG = "workspace_content_connector_edge";
    private static final String CONTENT_WALKWAY_CONTINUATION_LENGTH_TAG =
            "workspace_content_walkway_continuation_length";
    private static final String FLAT_PLATFORM_KIND_TAG = "workspace_flat_platform_kind";
    private static final String FLAT_PLATFORM_CORNER_TAG = "workspace_flat_platform_corner";
    private static final String FLAT_PLATFORM_CENTER_KIND = "center";
    private static final String FLAT_PLATFORM_SPOKE_KIND = "spoke";
    private static final String FLAT_PLATFORM_CORNER_CHAMFER_KIND = "corner_chamfer";
    private static final String COURTYARD_PATH_KIND = "courtyard_path";
    private static final String LINEAR_RUN_PIECE_KIND = "linear_run";
    private static final String FLOOR_PLAN_LINEAR_RUN_PIECE_KIND = "floor_plan_linear_run";
    private static final String FLOOR_PLAN_ROOM_PIECE_KIND = "floor_plan_room";
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");

    private final MKWorkspaceGridLayout gridLayout = new MKWorkspaceGridLayout();

    private record PieceBuildContext(
            BlockPos exportOrigin,
            BlockPos geometryOrigin,
            BoundingBox exportBounds,
            BoundingBox geometryBounds,
            BoundingBox clearedBounds,
            int exportWidth,
            int exportLength,
            int exportHeight,
            int geometryInteriorHeight
    ) {
    }

    enum LinearRunScaffoldStyle {
        ENCLOSED_CORRIDOR,
        OPEN_WALKWAY,
        DEFENSIVE_WALL,
        SOLID_WALL,
        PARAPET
    }

    enum ConnectorFaceLayer {
        FLOOR,
        WALL,
        CEILING
    }

    public List<MKWorkspacePieceDefinition> build(ServerLevel level, MKStructureWorkspace workspace,
                                                  List<MKPlannedPiece> plannedPieces) {
        List<MKPlannedPiece> authoringPieces = plannedPieces.stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(workspace.anchor(), authoringPieces,
                workspace.shellMargin(), workspace.verticalShellMargin(), workspace.exteriorAirMargin(),
                workspace.previewMargin(), GRID_COLUMNS, CELL_PADDING);
        clearWorkspaceArea(level, workspace, placements);
        Map<String, MKWorkspacePieceDefinition> authoringByBaseName = new HashMap<>();
        Map<MKPlannedPiece, MKWorkspacePieceDefinition> generatedByPlan = new HashMap<>();
        for (int i = 0; i < authoringPieces.size(); i++) {
            MKPlannedPiece plannedPiece = authoringPieces.get(i);
            MKWorkspacePieceDefinition generated = buildPiece(level, workspace, plannedPiece, placements.get(i), false);
            generatedByPlan.put(plannedPiece, generated);
            authoringByBaseName.put(plannedPiece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME,
                    plannedPiece.pieceName()), generated);
        }
        List<MKWorkspacePieceDefinition> generatedPieces = new ArrayList<>();
        for (MKPlannedPiece plannedPiece : plannedPieces) {
            if (MKWorkspaceTemplateReuseTags.isDerived(plannedPiece.tags())) {
                generatedPieces.add(createDerivedLogicalPiece(workspace, plannedPiece, authoringByBaseName));
            } else {
                generatedPieces.add(generatedByPlan.get(plannedPiece));
            }
        }
        return generatedPieces;
    }

    public MKWorkspacePieceDefinition buildSingle(ServerLevel level, MKStructureWorkspace workspace, MKPlannedPiece piece,
                                                  List<MKPlannedPiece> layoutPieces) {
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(workspace.anchor(), layoutPieces,
                workspace.shellMargin(), workspace.verticalShellMargin(), workspace.exteriorAirMargin(),
                workspace.previewMargin(), GRID_COLUMNS, CELL_PADDING);
        int index = layoutPieces.indexOf(piece);
        if (index < 0) {
            throw new IllegalArgumentException("piece is not present in layout list");
        }
        return buildPiece(level, workspace, piece, placements.get(index), true);
    }

    public Map<MKPlannedPiece, MKWorkspacePieceDefinition> buildSelected(ServerLevel level, MKStructureWorkspace workspace,
                                                                         List<MKPlannedPiece> pieces,
                                                                         List<MKPlannedPiece> layoutPieces) {
        if (pieces.isEmpty()) {
            return Map.of();
        }
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(workspace.anchor(), layoutPieces,
                workspace.shellMargin(), workspace.verticalShellMargin(), workspace.exteriorAirMargin(),
                workspace.previewMargin(), GRID_COLUMNS, CELL_PADDING);
        BoundingBox clearBounds = layoutClearBoundsForPieces(workspace, layoutPieces, pieces);
        if (clearBounds != null) {
            clearWorkspaceHeightBounds(level, clearBounds);
        }
        Map<MKPlannedPiece, MKWorkspacePieceDefinition> generated = new HashMap<>();
        for (MKPlannedPiece piece : pieces) {
            int index = layoutPieces.indexOf(piece);
            if (index < 0) {
                throw new IllegalArgumentException("piece is not present in layout list");
            }
            generated.put(piece, buildPiece(level, workspace, piece, placements.get(index), false));
        }
        return Map.copyOf(generated);
    }

    public void carveOpeningOnlyConnectors(ServerLevel level, MKStructureWorkspace workspace,
                                           MKWorkspacePieceDefinition existingPiece, MKPlannedPiece targetPiece,
                                           List<MKPlannedConnector> openingOnlyConnectors) {
        if (openingOnlyConnectors.isEmpty()) {
            return;
        }
        PieceBuildContext context = createBuildContext(workspace, targetPiece, placementFromSource(existingPiece));
        int effectiveShellMargin = getShellMargin(targetPiece, workspace.shellMargin());
        int verticalShellThickness = getVerticalShellMargin(targetPiece, workspace.verticalShellMargin());
        BlockState floorState = resolvePaletteState(workspace, targetPiece, MKWorkspacePaletteTags.FLOOR_BLOCK_TAG,
                workspace.palette().floorBlock(), Blocks.SMOOTH_STONE.defaultBlockState());
        BlockState wallState = resolvePaletteState(workspace, targetPiece, MKWorkspacePaletteTags.WALL_BLOCK_TAG,
                workspace.palette().wallBlock(), Blocks.STONE_BRICKS.defaultBlockState());
        BlockState ceilingState = resolvePaletteState(workspace, targetPiece, MKWorkspacePaletteTags.CEILING_BLOCK_TAG,
                workspace.palette().ceilingBlock(), Blocks.SMOOTH_STONE.defaultBlockState());
        for (MKPlannedConnector connector : openingOnlyConnectors) {
            extendHorizontalConnectorShell(level, context.exportBounds(), context.geometryOrigin(), targetPiece,
                    connector, effectiveShellMargin, verticalShellThickness, context.geometryBounds().getXSpan(),
                    context.geometryBounds().getZSpan(), context.geometryBounds().getYSpan(), floorState, wallState,
                    ceilingState);
        }
        for (MKPlannedConnector connector : openingOnlyConnectors) {
            carveConnectorOpening(level, context.exportBounds(), context.geometryOrigin(), targetPiece, connector,
                    effectiveShellMargin, verticalShellThickness, context.geometryBounds().getXSpan(),
                    context.geometryBounds().getZSpan(), context.geometryBounds().getYSpan());
        }
    }

    public MKWorkspacePieceDefinition cloneFromTemplate(ServerLevel level, MKStructureWorkspace workspace,
                                                        MKWorkspacePieceDefinition templatePiece, MKPlannedPiece targetPiece,
                                                        List<MKPlannedPiece> layoutPieces) {
        if (MKWorkspaceTemplateReuseTags.isDerived(targetPiece.tags())) {
            return createDerivedLogicalPiece(workspace, targetPiece, templatePiece);
        }
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(workspace.anchor(), layoutPieces,
                workspace.shellMargin(), workspace.verticalShellMargin(), workspace.exteriorAirMargin(),
                workspace.previewMargin(), GRID_COLUMNS, CELL_PADDING);
        int index = layoutPieces.indexOf(targetPiece);
        if (index < 0) {
            throw new IllegalArgumentException("piece is not present in layout list");
        }
        return cloneFromTemplate(level, workspace, templatePiece, targetPiece, placements.get(index));
    }

    public MKWorkspacePieceDefinition cloneFromTemplate(ServerLevel level, MKStructureWorkspace workspace,
                                                        MKWorkspacePieceDefinition templatePiece, MKPlannedPiece targetPiece,
                                                        MKWorkspaceGridLayout.Placement placement) {
        if (MKWorkspaceTemplateReuseTags.isDerived(targetPiece.tags())) {
            return createDerivedLogicalPiece(workspace, targetPiece, templatePiece);
        }
        PieceBuildContext context = createBuildContext(workspace, targetPiece, placement);
        clearWorkspaceHeightBounds(level, context.clearedBounds());
        copyTemplateContents(level, templatePiece.exportBounds(), context.exportBounds());
        List<MKWorkspaceConnectorDefinition> connectors = recreateConnectorsFromTemplate(level, workspace, targetPiece,
                templatePiece.connectors(), context.exportOrigin());
        List<BlockPos> markerPositions = new ArrayList<>();
        for (MKWorkspaceConnectorDefinition connector : connectors) {
            markerPositions.add(placeConnectorMarker(level, connector, context.exportBounds()));
        }
        List<BlockPos> generatedStairPositions = remapGeneratedStairPositions(templatePiece, context.exportOrigin());
        BlockPos structureBlockPos = placeStructureBlock(level, workspace, targetPiece, context.exportOrigin(),
                context.exportWidth(), context.exportHeight(), context.exportLength());
        BlockPos signPos = placeSign(level, workspace, targetPiece, structureBlockPos);
        Map<String, String> pieceTags = new HashMap<>(targetPiece.tags());
        copyGeneratedStairTags(templatePiece, pieceTags);
        return createPieceDefinition(workspace, targetPiece, placement, context, connectors,
                structureBlockPos, signPos, markerPositions, generatedStairPositions, pieceTags);
    }

    public MKWorkspacePieceDefinition createDerivedLogicalPiece(MKStructureWorkspace workspace, MKPlannedPiece targetPiece,
                                                                MKWorkspacePieceDefinition sourcePiece) {
        PieceBuildContext context = createBuildContext(workspace, targetPiece, placementFromSource(sourcePiece));
        List<MKWorkspaceConnectorDefinition> connectors = createLogicalConnectors(workspace, targetPiece, context);
        return createPieceDefinition(workspace, targetPiece, placementFromSource(sourcePiece), context, connectors,
                context.exportOrigin(), context.exportOrigin(), List.of(), List.of(), new HashMap<>(targetPiece.tags()));
    }

    private MKWorkspacePieceDefinition createDerivedLogicalPiece(MKStructureWorkspace workspace, MKPlannedPiece targetPiece,
                                                                 Map<String, MKWorkspacePieceDefinition> authoringByBaseName) {
        String sourceId = MKWorkspaceTemplateReuseTags.sourceId(targetPiece.tags());
        MKWorkspacePieceDefinition sourcePiece = authoringByBaseName.get(sourceId);
        if (sourcePiece == null) {
            throw new IllegalStateException("derived workspace piece " + targetPiece.pieceName() +
                    " references missing authoring source " + sourceId);
        }
        return createDerivedLogicalPiece(workspace, targetPiece, sourcePiece);
    }

    private MKWorkspaceGridLayout.Placement placementFromSource(MKWorkspacePieceDefinition sourcePiece) {
        return new MKWorkspaceGridLayout.Placement(
                new BlockPos(sourcePiece.previewBounds().minX(), sourcePiece.previewBounds().minY(),
                        sourcePiece.previewBounds().minZ()),
                sourcePiece.previewBounds()
        );
    }

    public void clearLayoutAreaForPieces(ServerLevel level, MKStructureWorkspace workspace,
                                         List<MKPlannedPiece> layoutPieces, List<MKPlannedPiece> piecesToClear) {
        BoundingBox bounds = layoutClearBoundsForPieces(workspace, layoutPieces, piecesToClear);
        if (bounds != null) {
            clearWorkspaceHeightBounds(level, bounds);
        }
    }

    public void clearExistingWorkspaceArea(ServerLevel level, MKStructureWorkspace workspace, BlockPos excludedPos) {
        BoundingBox bounds = existingWorkspaceClearBounds(workspace);
        if (bounds != null) {
            clearWorkspaceHeightBounds(level, bounds, excludedPos);
        }
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            clearBlock(level, piece.structureBlockPos(), excludedPos);
            clearBlock(level, piece.signPos(), excludedPos);
            for (BlockPos markerPos : piece.markerPositions()) {
                clearBlock(level, markerPos, excludedPos);
            }
            for (BlockPos stairPos : piece.generatedStairPositions()) {
                clearBlock(level, stairPos, excludedPos);
            }
        }
    }

    public void clearExistingPieces(ServerLevel level, List<MKWorkspacePieceDefinition> piecesToClear,
                                    BlockPos excludedPos) {
        BoundingBox bounds = null;
        for (MKWorkspacePieceDefinition piece : piecesToClear) {
            BoundingBox expanded = expandBounds(piece.previewBounds(), CLEAR_MARGIN);
            bounds = bounds == null ? expanded : mergeBounds(bounds, expanded);
        }
        if (bounds != null) {
            clearWorkspaceHeightBounds(level, bounds, excludedPos);
        }
        for (MKWorkspacePieceDefinition piece : piecesToClear) {
            clearBlock(level, piece.structureBlockPos(), excludedPos);
            clearBlock(level, piece.signPos(), excludedPos);
            for (BlockPos markerPos : piece.markerPositions()) {
                clearBlock(level, markerPos, excludedPos);
            }
            for (BlockPos stairPos : piece.generatedStairPositions()) {
                clearBlock(level, stairPos, excludedPos);
            }
        }
    }

    public void clearExistingPieceContents(ServerLevel level, List<MKWorkspacePieceDefinition> piecesToClear,
                                           BlockPos excludedPos) {
        for (BlockPos pos : collectExistingPieceContentPositions(piecesToClear)) {
            clearBlock(level, pos, excludedPos);
        }
    }

    Set<BlockPos> collectExistingPieceContentPositions(List<MKWorkspacePieceDefinition> piecesToClear) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        for (MKWorkspacePieceDefinition piece : piecesToClear) {
            BoundingBox previewBounds = piece.previewBounds();
            for (int x = previewBounds.minX(); x <= previewBounds.maxX(); x++) {
                for (int y = previewBounds.minY(); y <= previewBounds.maxY(); y++) {
                    for (int z = previewBounds.minZ(); z <= previewBounds.maxZ(); z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }
            positions.add(piece.structureBlockPos());
            positions.add(piece.signPos());
            positions.addAll(piece.markerPositions());
            positions.addAll(piece.generatedStairPositions());
        }
        return Set.copyOf(positions);
    }

    BoundingBox existingWorkspaceClearBounds(MKStructureWorkspace workspace) {
        BoundingBox workspaceBounds = null;
        for (MKWorkspacePieceDefinition existingPiece : workspace.pieces()) {
            BoundingBox expanded = expandBounds(existingPiece.previewBounds(), CLEAR_MARGIN);
            workspaceBounds = workspaceBounds == null ? expanded : mergeBounds(workspaceBounds, expanded);
        }
        return workspaceBounds;
    }

    BoundingBox layoutClearBoundsForPieces(MKStructureWorkspace workspace, List<MKPlannedPiece> layoutPieces,
                                           List<MKPlannedPiece> piecesToClear) {
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(workspace.anchor(), layoutPieces,
                workspace.shellMargin(), workspace.verticalShellMargin(), workspace.exteriorAirMargin(),
                workspace.previewMargin(), GRID_COLUMNS, CELL_PADDING);
        BoundingBox bounds = null;
        for (MKPlannedPiece piece : piecesToClear) {
            int index = layoutPieces.indexOf(piece);
            if (index < 0) {
                throw new IllegalArgumentException("piece is not present in layout list");
            }
            BoundingBox expanded = expandBounds(placements.get(index).previewBounds(), CLEAR_MARGIN);
            bounds = bounds == null ? expanded : mergeBounds(bounds, expanded);
        }
        return bounds;
    }

    private MKWorkspacePieceDefinition buildPiece(ServerLevel level, MKStructureWorkspace workspace, MKPlannedPiece plannedPiece,
                                                  MKWorkspaceGridLayout.Placement placement, boolean clearBeforeBuild) {
        PieceBuildContext context = createBuildContext(workspace, plannedPiece, placement);
        int effectiveShellMargin = getShellMargin(plannedPiece, workspace.shellMargin());
        int verticalShellThickness = getVerticalShellMargin(plannedPiece, workspace.verticalShellMargin());

        BlockState floorState = resolvePaletteState(workspace, plannedPiece, MKWorkspacePaletteTags.FLOOR_BLOCK_TAG,
                workspace.palette().floorBlock(), Blocks.SMOOTH_STONE.defaultBlockState());
        BlockState wallState = resolvePaletteState(workspace, plannedPiece, MKWorkspacePaletteTags.WALL_BLOCK_TAG,
                workspace.palette().wallBlock(), Blocks.STONE_BRICKS.defaultBlockState());
        BlockState ceilingState = resolvePaletteState(workspace, plannedPiece, MKWorkspacePaletteTags.CEILING_BLOCK_TAG,
                workspace.palette().ceilingBlock(), Blocks.SMOOTH_STONE.defaultBlockState());
        boolean emptyScaffold = isEmptyScaffold(plannedPiece);
        boolean floorLinkInsert = isFloorLinkInsert(plannedPiece);

        if (clearBeforeBuild) {
            clearWorkspaceHeightBounds(level, context.clearedBounds());
        }
        clearBounds(level, context.exportBounds());
        if (floorLinkInsert) {
            placeFloorLinkInsertScaffold(level, context.exportBounds(), floorState, wallState);
        } else if (!emptyScaffold) {
            placeExteriorMargin(level, context.exportBounds(), context.geometryBounds());
            placeScaffoldGeometry(level, context.geometryBounds(), context.geometryOrigin(), plannedPiece,
                    effectiveShellMargin, verticalShellThickness, context.geometryInteriorHeight(), floorState,
                    wallState, ceilingState);
        }

        List<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>();
        List<BlockPos> markerPositions = new ArrayList<>();
        List<MKPlannedConnector> plannedConnectors = plannedPiece.connectors();
        for (MKPlannedConnector plannedConnector : plannedConnectors) {
            MKWorkspaceConnectorDefinition connector = createLogicalConnector(workspace, plannedPiece, plannedConnector,
                    context.exportOrigin(), context.exportBounds(), context.geometryOrigin(), effectiveShellMargin,
                    verticalShellThickness, context.geometryBounds().getXSpan(), context.geometryBounds().getZSpan(),
                    context.geometryBounds().getYSpan(), context.geometryInteriorHeight());
            if (connector != null) {
                connectors.add(connector);
            }
        }
        for (MKPlannedConnector plannedConnector : plannedConnectors) {
            extendHorizontalConnectorShell(level, context.exportBounds(), context.geometryOrigin(), plannedPiece,
                    plannedConnector, effectiveShellMargin, verticalShellThickness, context.geometryBounds().getXSpan(),
                    context.geometryBounds().getZSpan(), context.geometryBounds().getYSpan(), floorState, wallState,
                    ceilingState);
        }
        for (MKPlannedConnector plannedConnector : plannedConnectors) {
            carveConnectorOpening(level, context.exportBounds(), context.geometryOrigin(), plannedPiece,
                    plannedConnector, effectiveShellMargin, verticalShellThickness, context.geometryBounds().getXSpan(),
                    context.geometryBounds().getZSpan(), context.geometryBounds().getYSpan());
        }
        for (MKWorkspaceConnectorDefinition connector : connectors) {
            placeConnectorJigsaw(level, connector, context.exportOrigin());
            markerPositions.add(placeConnectorMarker(level, connector, context.exportBounds()));
        }

        BlockPos structureBlockPos = placeStructureBlock(level, workspace, plannedPiece, context.exportOrigin(),
                context.exportWidth(), context.exportHeight(), context.exportLength());
        BlockPos signPos = placeSign(level, workspace, plannedPiece, structureBlockPos);
        return createPieceDefinition(workspace, plannedPiece, placement, context, connectors, structureBlockPos, signPos,
                markerPositions, List.of(), new HashMap<>(plannedPiece.tags()));
    }

    private MKWorkspacePieceDefinition createPieceDefinition(MKStructureWorkspace workspace, MKPlannedPiece plannedPiece,
                                                             MKWorkspaceGridLayout.Placement placement,
                                                             PieceBuildContext context,
                                                             List<MKWorkspaceConnectorDefinition> connectors,
                                                             BlockPos structureBlockPos, BlockPos signPos,
                                                             List<BlockPos> markerPositions,
                                                             List<BlockPos> generatedStairPositions,
                                                             Map<String, String> pieceTags) {
        MKWorkspaceDimensions effectiveDimensions = new MKWorkspaceDimensions(
                plannedPiece.interiorWidth(),
                plannedPiece.interiorLength(),
                plannedPiece.interiorHeight(),
                plannedPiece.interiorHeight(),
                plannedPiece.interiorHeight(),
                effectiveShaftWidth(workspace, connectors),
                effectiveDoorwayWidth(workspace, connectors),
                effectiveDoorwayHeight(workspace, connectors)
        );
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspace.id(),
                plannedPiece.pieceName(),
                plannedPiece.roleId(),
                plannedPiece.plannerId(),
                getVariantIndex(plannedPiece),
                effectiveDimensions,
                getShellMargin(plannedPiece, workspace.shellMargin()),
                getVerticalShellMargin(plannedPiece, workspace.verticalShellMargin()),
                connectors,
                context.exportOrigin(),
                context.exportBounds(),
                placement.previewBounds(),
                structureBlockPos,
                signPos,
                markerPositions,
                generatedStairPositions,
                pieceTags
        );
    }

    private int effectiveShaftWidth(MKStructureWorkspace workspace, List<MKWorkspaceConnectorDefinition> connectors) {
        return connectors.stream()
                .filter(connector -> connector.facing().getAxis().isVertical())
                .mapToInt(MKWorkspaceConnectorDefinition::openingWidth)
                .findFirst()
                .orElse(workspace.dimensions().shaftWidth());
    }

    private int effectiveDoorwayWidth(MKStructureWorkspace workspace, List<MKWorkspaceConnectorDefinition> connectors) {
        return connectors.stream()
                .filter(connector -> !connector.facing().getAxis().isVertical())
                .mapToInt(MKWorkspaceConnectorDefinition::openingWidth)
                .findFirst()
                .orElse(workspace.dimensions().doorwayWidth());
    }

    private int effectiveDoorwayHeight(MKStructureWorkspace workspace, List<MKWorkspaceConnectorDefinition> connectors) {
        return connectors.stream()
                .filter(connector -> !connector.facing().getAxis().isVertical())
                .mapToInt(MKWorkspaceConnectorDefinition::openingHeight)
                .findFirst()
                .orElse(workspace.dimensions().doorwayHeight());
    }

    private List<MKWorkspaceConnectorDefinition> createLogicalConnectors(MKStructureWorkspace workspace,
                                                                         MKPlannedPiece targetPiece,
                                                                         PieceBuildContext context) {
        List<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>();
        int shellMargin = getShellMargin(targetPiece, workspace.shellMargin());
        int verticalShellThickness = getVerticalShellMargin(targetPiece, workspace.verticalShellMargin());
        for (MKPlannedConnector plannedConnector : targetPiece.connectors()) {
            MKWorkspaceConnectorDefinition connector = createLogicalConnector(workspace, targetPiece, plannedConnector,
                    context.exportOrigin(), context.exportBounds(), context.geometryOrigin(), shellMargin,
                    verticalShellThickness, context.geometryBounds().getXSpan(), context.geometryBounds().getZSpan(),
                    context.geometryBounds().getYSpan(), context.geometryInteriorHeight());
            if (connector != null) {
                connectors.add(connector);
            }
        }
        return connectors;
    }

    private MKWorkspaceConnectorDefinition createLogicalConnector(MKStructureWorkspace workspace, MKPlannedPiece piece,
                                                                  MKPlannedConnector plannedConnector,
                                                                  BlockPos exportOrigin, BoundingBox exportBounds,
                                                                  BlockPos geometryOrigin, int shellMargin,
                                                                  int verticalShellThickness, int geometryWidth,
                                                                  int geometryLength, int geometryHeight,
                                                                  int geometryInteriorHeight) {
        Direction facing = plannedConnector.facing();
        int interiorCenterX = getConnectorCenterX(geometryOrigin, piece, shellMargin, plannedConnector);
        int interiorCenterZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, plannedConnector);
        int openingBaseY = getOpeningBaseY(geometryOrigin, verticalShellThickness, plannedConnector);
        validateConnectorBounds(piece, plannedConnector, shellMargin, openingBaseY - geometryOrigin.getY(),
                geometryInteriorHeight);
        if (!plannedConnector.placesJigsaw()) {
            return null;
        }
        BlockPos connectorPos = connectorPosition(facing, exportBounds, geometryOrigin, geometryWidth,
                geometryLength, geometryHeight, verticalShellThickness, interiorCenterX, interiorCenterZ, openingBaseY);
        ResourceLocation pool = getConnectorPool(workspace, plannedConnector.targetPoolName(), piece);
        ResourceLocation incomingPool = getIncomingConnectorPool(workspace, plannedConnector.incomingPoolName());
        ResourceLocation name = getJigsawName(workspace, plannedConnector, incomingPool);
        ResourceLocation target = getJigsawTarget(workspace, plannedConnector, pool);
        return new MKWorkspaceConnectorDefinition(
                plannedConnector.role(),
                facing,
                connectorPos.subtract(exportOrigin),
                plannedConnector.openingWidth(),
                plannedConnector.openingHeight(),
                plannedConnector.lateralOffset(),
                plannedConnector.verticalOffset(),
                name,
                target,
                pool,
                incomingPool
        );
    }

    private List<BlockPos> remapGeneratedStairPositions(MKWorkspacePieceDefinition templatePiece, BlockPos exportOrigin) {
        BlockPos templateOrigin = new BlockPos(templatePiece.exportBounds().minX(), templatePiece.exportBounds().minY(),
                templatePiece.exportBounds().minZ());
        List<BlockPos> remapped = new ArrayList<>();
        for (BlockPos pos : templatePiece.generatedStairPositions()) {
            remapped.add(exportOrigin.offset(pos.subtract(templateOrigin)));
        }
        return remapped;
    }

    private void copyGeneratedStairTags(MKWorkspacePieceDefinition templatePiece, Map<String, String> targetTags) {
        for (Map.Entry<String, String> entry : templatePiece.tags().entrySet()) {
            if (entry.getKey().startsWith("generated_stair_")) {
                targetTags.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private PieceBuildContext createBuildContext(MKStructureWorkspace workspace, MKPlannedPiece plannedPiece,
                                                 MKWorkspaceGridLayout.Placement placement) {
        int shellMargin = getShellMargin(plannedPiece, workspace.shellMargin());
        int verticalShellThickness = getVerticalShellMargin(plannedPiece, workspace.verticalShellMargin());
        boolean exactBoundsScaffold = isExactBoundsScaffold(plannedPiece);
        int exteriorAirMargin = exactBoundsScaffold ? 0 : workspace.exteriorAirMargin();
        int topVoidMargin = exactBoundsScaffold ? 0 : getTopVoidMargin(plannedPiece);
        int bottomVoidMargin = exactBoundsScaffold ? 0 : getBottomVoidMargin(plannedPiece);
        int exportWidth = plannedPiece.interiorWidth() + (2 * shellMargin) + (2 * exteriorAirMargin);
        int exportLength = plannedPiece.interiorLength() + (2 * shellMargin) + (2 * exteriorAirMargin);
        int bodyHeight = plannedPiece.interiorHeight() + (2 * verticalShellThickness);
        int exportHeight = bodyHeight;
        int geometryHeight = Math.max(1, bodyHeight - topVoidMargin - bottomVoidMargin);
        int geometryInteriorHeight = Math.max(0, geometryHeight - (2 * verticalShellThickness));
        BlockPos exportOrigin = placement.previewOrigin().offset(workspace.previewMargin(), 0, workspace.previewMargin());
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
                geometryOrigin.getX() + plannedPiece.interiorWidth() + (2 * shellMargin) - 1,
                geometryOrigin.getY() + geometryHeight - 1,
                geometryOrigin.getZ() + plannedPiece.interiorLength() + (2 * shellMargin) - 1
        );
        BoundingBox clearedBounds = new BoundingBox(
                exportBounds.minX() - CLEAR_MARGIN,
                exportBounds.minY() - CLEAR_MARGIN,
                exportBounds.minZ() - CLEAR_MARGIN,
                exportBounds.maxX() + CLEAR_MARGIN,
                exportBounds.maxY() + CLEAR_MARGIN,
                exportBounds.maxZ() + CLEAR_MARGIN
        );
        return new PieceBuildContext(exportOrigin, geometryOrigin, exportBounds, geometryBounds, clearedBounds,
                exportWidth, exportLength, exportHeight, geometryInteriorHeight);
    }

    private void copyTemplateContents(ServerLevel level, BoundingBox sourceBounds, BoundingBox destinationBounds) {
        for (int x = 0; x < sourceBounds.getXSpan(); x++) {
            for (int y = 0; y < sourceBounds.getYSpan(); y++) {
                for (int z = 0; z < sourceBounds.getZSpan(); z++) {
                    BlockPos sourcePos = new BlockPos(sourceBounds.minX() + x, sourceBounds.minY() + y, sourceBounds.minZ() + z);
                    BlockPos destPos = new BlockPos(destinationBounds.minX() + x, destinationBounds.minY() + y, destinationBounds.minZ() + z);
                    BlockState state = level.getBlockState(sourcePos);
                    level.setBlock(destPos, state, Block.UPDATE_ALL);
                    copyBlockEntity(level, sourcePos, destPos, state);
                }
            }
        }
    }

    private void placeFloorLinkInsertScaffold(ServerLevel level, BoundingBox bounds, BlockState floorState,
                                              BlockState wallState) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                level.setBlock(new BlockPos(x, bounds.minY(), z), floorState, Block.UPDATE_ALL);
                level.setBlock(new BlockPos(x, bounds.maxY(), z), wallState, Block.UPDATE_ALL);
            }
        }
        if (bounds.getYSpan() <= 2) {
            return;
        }
        for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
            for (int y = bounds.minY() + 1; y <= bounds.maxY() - 1; y++) {
                level.setBlock(new BlockPos(bounds.minX(), y, z), wallState, Block.UPDATE_ALL);
                level.setBlock(new BlockPos(bounds.maxX(), y, z), wallState, Block.UPDATE_ALL);
            }
        }
    }

    private void copyBlockEntity(ServerLevel level, BlockPos sourcePos, BlockPos destPos, BlockState state) {
        BlockEntity sourceEntity = level.getBlockEntity(sourcePos);
        if (sourceEntity == null) {
            return;
        }
        BlockEntity destEntity = level.getBlockEntity(destPos);
        if (destEntity == null) {
            return;
        }
        CompoundTag tag = sourceEntity.saveWithFullMetadata(level.registryAccess());
        tag.putInt("x", destPos.getX());
        tag.putInt("y", destPos.getY());
        tag.putInt("z", destPos.getZ());
        destEntity.loadWithComponents(tag, level.registryAccess());
        destEntity.setChanged();
        level.sendBlockUpdated(destPos, state, state, Block.UPDATE_ALL);
    }

    private List<MKWorkspaceConnectorDefinition> recreateConnectorsFromTemplate(ServerLevel level, MKStructureWorkspace workspace,
                                                                                MKPlannedPiece targetPiece,
                                                                                List<MKWorkspaceConnectorDefinition> sourceConnectors,
                                                                                BlockPos exportOrigin) {
        List<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>();
        for (MKWorkspaceConnectorDefinition sourceConnector : sourceConnectors) {
            BlockPos connectorPos = exportOrigin.offset(sourceConnector.relativePos());
            BlockEntity entity = level.getBlockEntity(connectorPos);
            ResourceLocation pool = sourceConnector.targetPool();
            if (entity instanceof JigsawBlockEntity jigsaw) {
                jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, pool));
                jigsaw.setChanged();
            }
            connectors.add(new MKWorkspaceConnectorDefinition(
                    sourceConnector.role(),
                    sourceConnector.facing(),
                    sourceConnector.relativePos(),
                    sourceConnector.openingWidth(),
                    sourceConnector.openingHeight(),
                    sourceConnector.lateralOffset(),
                    sourceConnector.verticalOffset(),
                    sourceConnector.jigsawName(),
                    sourceConnector.jigsawTarget(),
                    pool,
                    sourceConnector.incomingPool()
            ));
        }
        return connectors;
    }

    private void clearBounds(ServerLevel level, BoundingBox bounds) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void clearWorkspaceArea(ServerLevel level, MKStructureWorkspace workspace,
                                    List<MKWorkspaceGridLayout.Placement> placements) {
        BoundingBox workspaceBounds = existingWorkspaceClearBounds(workspace);
        for (MKWorkspaceGridLayout.Placement placement : placements) {
            BoundingBox expanded = expandBounds(placement.previewBounds(), CLEAR_MARGIN);
            workspaceBounds = workspaceBounds == null ? expanded : mergeBounds(workspaceBounds, expanded);
        }
        if (workspaceBounds != null) {
            clearWorkspaceHeightBounds(level, workspaceBounds);
        }
    }

    void clearWorkspaceHeightBounds(ServerLevel level, BoundingBox bounds) {
        clearBounds(level, extendToWorkspaceClearHeight(level, bounds));
    }

    private void clearWorkspaceHeightBounds(ServerLevel level, BoundingBox bounds, BlockPos excludedPos) {
        clearBounds(level, extendToWorkspaceClearHeight(level, bounds), excludedPos);
    }

    BoundingBox extendToWorkspaceClearHeight(ServerLevel level, BoundingBox bounds) {
        return extendToWorkspaceClearHeight(bounds, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
    }

    BoundingBox extendToWorkspaceClearHeight(BoundingBox bounds, int minBuildHeight, int maxBuildY) {
        return new BoundingBox(
                bounds.minX(),
                minBuildHeight,
                bounds.minZ(),
                bounds.maxX(),
                maxBuildY,
                bounds.maxZ()
        );
    }

    private void clearBounds(ServerLevel level, BoundingBox bounds, BlockPos excludedPos) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    clearBlock(level, new BlockPos(x, y, z), excludedPos);
                }
            }
        }
    }

    private void clearBlock(ServerLevel level, BlockPos pos, BlockPos excludedPos) {
        if (pos.equals(excludedPos)) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private BoundingBox expandBounds(BoundingBox bounds, int margin) {
        return new BoundingBox(
                bounds.minX() - margin,
                bounds.minY() - margin,
                bounds.minZ() - margin,
                bounds.maxX() + margin,
                bounds.maxY() + margin,
                bounds.maxZ() + margin
        );
    }

    private BoundingBox mergeBounds(BoundingBox left, BoundingBox right) {
        return new BoundingBox(
                Math.min(left.minX(), right.minX()),
                Math.min(left.minY(), right.minY()),
                Math.min(left.minZ(), right.minZ()),
                Math.max(left.maxX(), right.maxX()),
                Math.max(left.maxY(), right.maxY()),
                Math.max(left.maxZ(), right.maxZ())
        );
    }

    private boolean isEmptyScaffold(MKPlannedPiece piece) {
        return MKWorkspacePieceGeometry.isEmptyScaffold(piece.tags());
    }

    private boolean isExactBoundsScaffold(MKPlannedPiece piece) {
        return MKWorkspacePieceGeometry.isExactBoundsScaffold(piece.tags()) || isFlatPlatform(piece);
    }

    private boolean isFloorLinkInsert(MKPlannedPiece piece) {
        return MKWorkspacePieceGeometry.isFloorLinkInsert(piece.tags());
    }

    private int getShellMargin(MKPlannedPiece piece, int shellMargin) {
        return isExactBoundsScaffold(piece) ? 0 : shellMargin;
    }

    private int getVerticalShellMargin(MKPlannedPiece piece, int verticalShellMargin) {
        return isExactBoundsScaffold(piece) ? 0 : verticalShellMargin;
    }

    private int getVariantIndex(MKPlannedPiece piece) {
        try {
            return Integer.parseInt(piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0"));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void placeShell(ServerLevel level, BoundingBox bounds, int shellMargin, int verticalShellThickness,
                            BlockState floorState, BlockState wallState, BlockState ceilingState) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    boolean bottom = y < bounds.minY() + verticalShellThickness;
                    boolean top = y > bounds.maxY() - verticalShellThickness;
                    boolean wall = x < bounds.minX() + shellMargin || x > bounds.maxX() - shellMargin ||
                            z < bounds.minZ() + shellMargin || z > bounds.maxZ() - shellMargin;
                    if (bottom) {
                        level.setBlock(pos, floorState, Block.UPDATE_ALL);
                    } else if (top) {
                        level.setBlock(pos, ceilingState, Block.UPDATE_ALL);
                    } else if (wall) {
                        level.setBlock(pos, wallState, Block.UPDATE_ALL);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private void placeScaffoldGeometry(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                                       MKPlannedPiece piece, int shellMargin, int verticalShellThickness,
                                       int geometryInteriorHeight, BlockState floorState, BlockState wallState,
                                       BlockState ceilingState) {
        if (isFlatPlatform(piece)) {
            placeFlatPlatformTemplate(level, geometryBounds, geometryOrigin, piece, shellMargin,
                    verticalShellThickness, floorState);
            return;
        }
        if (isCourtyardContent(piece)) {
            placeCourtyardContentTemplate(level, geometryBounds, geometryOrigin, piece, shellMargin,
                    verticalShellThickness, floorState);
            return;
        }
        if (isCourtyardPath(piece)) {
            placeCourtyardPathTemplate(level, geometryBounds, geometryOrigin, piece, shellMargin,
                    verticalShellThickness, floorState);
            return;
        }
        if (isFloorPlanLinearRun(piece)) {
            placeFloorPlanLinearRun(level, geometryBounds, geometryOrigin, piece, shellMargin,
                    verticalShellThickness, floorState, wallState, ceilingState);
            return;
        }
        switch (linearRunScaffoldStyle(piece)) {
            case SOLID_WALL -> placeSolidWall(level, geometryBounds, wallState);
            case OPEN_WALKWAY -> {
                placeOpenWalkway(level, geometryBounds, geometryOrigin, piece, shellMargin, verticalShellThickness,
                        floorState);
                decoratePieceInterior(level, geometryOrigin, piece, shellMargin, verticalShellThickness,
                        geometryInteriorHeight, floorState);
            }
            case PARAPET -> placeParapet(level, geometryBounds, geometryOrigin, piece, shellMargin, wallState,
                    floorState);
            case DEFENSIVE_WALL, ENCLOSED_CORRIDOR -> {
                placeShell(level, geometryBounds, shellMargin, verticalShellThickness, floorState, wallState,
                        ceilingState);
                carveInterior(level, geometryOrigin, piece, shellMargin, verticalShellThickness,
                        geometryInteriorHeight);
                decoratePieceInterior(level, geometryOrigin, piece, shellMargin, verticalShellThickness,
                        geometryInteriorHeight, floorState);
            }
        }
    }

    private boolean isFlatPlatform(MKPlannedPiece piece) {
        return !piece.tags().getOrDefault(FLAT_PLATFORM_KIND_TAG, "").isBlank();
    }

    private void placeFlatPlatformTemplate(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                                           MKPlannedPiece piece, int shellMargin, int verticalShellThickness,
                                           BlockState floorState) {
        BlockState structureVoid = Blocks.STRUCTURE_VOID.defaultBlockState();
        for (int x = geometryBounds.minX(); x <= geometryBounds.maxX(); x++) {
            for (int y = geometryBounds.minY(); y <= geometryBounds.maxY(); y++) {
                for (int z = geometryBounds.minZ(); z <= geometryBounds.maxZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), structureVoid, Block.UPDATE_ALL);
                }
            }
        }
        int floorY = geometryOrigin.getY() + Math.max(0, verticalShellThickness - 1);
        String kind = piece.tags().getOrDefault(FLAT_PLATFORM_KIND_TAG, "");
        if (FLAT_PLATFORM_CENTER_KIND.equals(kind) || FLAT_PLATFORM_SPOKE_KIND.equals(kind)) {
            fillFlatPlatformRect(level, geometryOrigin, piece, shellMargin, floorY, floorState);
            return;
        }
        if (FLAT_PLATFORM_CORNER_CHAMFER_KIND.equals(kind)) {
            fillFlatPlatformChamfer(level, geometryOrigin, piece, shellMargin, floorY, floorState);
        }
    }

    private void fillFlatPlatformRect(ServerLevel level, BlockPos geometryOrigin, MKPlannedPiece piece,
                                      int shellMargin, int floorY, BlockState floorState) {
        int minX = geometryOrigin.getX() + shellMargin;
        int minZ = geometryOrigin.getZ() + shellMargin;
        for (int x = 0; x < piece.interiorWidth(); x++) {
            for (int z = 0; z < piece.interiorLength(); z++) {
                level.setBlock(new BlockPos(minX + x, floorY, minZ + z), floorState, Block.UPDATE_ALL);
            }
        }
    }

    private void fillFlatPlatformChamfer(ServerLevel level, BlockPos geometryOrigin, MKPlannedPiece piece,
                                         int shellMargin, int floorY, BlockState floorState) {
        int minX = geometryOrigin.getX() + shellMargin;
        int minZ = geometryOrigin.getZ() + shellMargin;
        String corner = piece.tags().getOrDefault(FLAT_PLATFORM_CORNER_TAG, "north_west");
        for (int x = 0; x < piece.interiorWidth(); x++) {
            for (int z = 0; z < piece.interiorLength(); z++) {
                if (isInsideFlatPlatformChamfer(corner, x, z, piece.interiorWidth(), piece.interiorLength())) {
                    level.setBlock(new BlockPos(minX + x, floorY, minZ + z), floorState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private boolean isInsideFlatPlatformChamfer(String corner, int x, int z, int width, int length) {
        int maxX = width - 1;
        int maxZ = length - 1;
        return switch (corner) {
            case "north_east" -> z >= x;
            case "south_east" -> x + z <= maxX;
            case "south_west" -> x >= z;
            default -> x + z >= maxX;
        };
    }

    private boolean isCourtyardContent(MKPlannedPiece piece) {
        return "courtyard".equals(piece.tags().getOrDefault(CONTENT_KIND_TAG, ""));
    }

    private boolean isCourtyardPath(MKPlannedPiece piece) {
        return COURTYARD_PATH_KIND.equals(piece.tags().getOrDefault(CONTENT_KIND_TAG, ""));
    }

    private void placeCourtyardContentTemplate(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                                               MKPlannedPiece piece, int shellMargin, int verticalShellThickness,
                                               BlockState floorState) {
        BlockState structureVoid = Blocks.STRUCTURE_VOID.defaultBlockState();
        for (int x = geometryBounds.minX(); x <= geometryBounds.maxX(); x++) {
            for (int y = geometryBounds.minY(); y <= geometryBounds.maxY(); y++) {
                for (int z = geometryBounds.minZ(); z <= geometryBounds.maxZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), structureVoid, Block.UPDATE_ALL);
                }
            }
        }
        Direction connectorFacing = Direction.byName(piece.tags().getOrDefault(CONTENT_CONNECTOR_EDGE_TAG, "south"));
        if (connectorFacing == null || connectorFacing.getAxis().isVertical()) {
            connectorFacing = Direction.SOUTH;
        }
        int continuationLength = Math.min(piece.interiorLength(), parseIntTag(piece.tags(),
                CONTENT_WALKWAY_CONTINUATION_LENGTH_TAG, 3));
        int walkwayWidth = piece.connectors().stream()
                .findFirst()
                .map(MKPlannedConnector::openingWidth)
                .orElse(3);
        int halfWidth = walkwayWidth / 2;
        int interiorMinX = geometryOrigin.getX() + shellMargin;
        int interiorMaxX = interiorMinX + piece.interiorWidth() - 1;
        int interiorMinZ = geometryOrigin.getZ() + shellMargin;
        int interiorMaxZ = interiorMinZ + piece.interiorLength() - 1;
        int centerX = (interiorMinX + interiorMaxX) / 2;
        int centerZ = (interiorMinZ + interiorMaxZ) / 2;
        int floorY = geometryOrigin.getY() + Math.max(0, verticalShellThickness - 1);
        for (int step = 0; step < continuationLength; step++) {
            switch (connectorFacing) {
                case NORTH -> placeCourtyardWalkwayRow(level, centerX, interiorMinZ + step, halfWidth, true, floorY,
                        floorState);
                case SOUTH -> placeCourtyardWalkwayRow(level, centerX, interiorMaxZ - step, halfWidth, true, floorY,
                        floorState);
                case EAST -> placeCourtyardWalkwayRow(level, interiorMaxX - step, centerZ, halfWidth, false, floorY,
                        floorState);
                case WEST -> placeCourtyardWalkwayRow(level, interiorMinX + step, centerZ, halfWidth, false, floorY,
                        floorState);
                default -> {
                }
            }
        }
    }

    private void placeCourtyardWalkwayRow(ServerLevel level, int xOrCenter, int zOrCenter, int halfWidth,
                                          boolean varyX, int y, BlockState floorState) {
        for (int offset = -halfWidth; offset <= halfWidth; offset++) {
            int x = varyX ? xOrCenter + offset : xOrCenter;
            int z = varyX ? zOrCenter : zOrCenter + offset;
            level.setBlock(new BlockPos(x, y, z), floorState, Block.UPDATE_ALL);
        }
    }

    private void placeCourtyardPathTemplate(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                                            MKPlannedPiece piece, int shellMargin, int verticalShellThickness,
                                            BlockState floorState) {
        BlockState structureVoid = Blocks.STRUCTURE_VOID.defaultBlockState();
        for (int x = geometryBounds.minX(); x <= geometryBounds.maxX(); x++) {
            for (int y = geometryBounds.minY(); y <= geometryBounds.maxY(); y++) {
                for (int z = geometryBounds.minZ(); z <= geometryBounds.maxZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), structureVoid, Block.UPDATE_ALL);
                }
            }
        }
        int walkwayWidth = piece.connectors().stream()
                .findFirst()
                .map(MKPlannedConnector::openingWidth)
                .orElse(3);
        int halfWidth = walkwayWidth / 2;
        int interiorMinX = geometryOrigin.getX() + shellMargin;
        int interiorMaxX = interiorMinX + piece.interiorWidth() - 1;
        int interiorMinZ = geometryOrigin.getZ() + shellMargin;
        int interiorMaxZ = interiorMinZ + piece.interiorLength() - 1;
        int centerX = (interiorMinX + interiorMaxX) / 2;
        int centerZ = (interiorMinZ + interiorMaxZ) / 2;
        int floorY = geometryOrigin.getY() + Math.max(0, verticalShellThickness - 1);
        placeCourtyardWalkwayRow(level, centerX, centerZ, halfWidth, true, floorY, floorState);
        placeCourtyardWalkwayRow(level, centerX, centerZ, halfWidth, false, floorY, floorState);
        for (MKPlannedConnector connector : piece.connectors()) {
            if (connector.facing().getAxis().isVertical()) {
                continue;
            }
            placeCourtyardPathConnectorLeg(level, geometryOrigin, piece, shellMargin, connector, centerX, centerZ,
                    interiorMinX, interiorMaxX, interiorMinZ, interiorMaxZ, halfWidth, floorY, floorState);
        }
    }

    private void placeCourtyardPathConnectorLeg(ServerLevel level, BlockPos geometryOrigin, MKPlannedPiece piece,
                                                int shellMargin, MKPlannedConnector connector, int centerX,
                                                int centerZ, int interiorMinX, int interiorMaxX, int interiorMinZ,
                                                int interiorMaxZ, int halfWidth, int floorY, BlockState floorState) {
        int connectorCenterX = getConnectorCenterX(geometryOrigin, piece, shellMargin, connector);
        int connectorCenterZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, connector);
        switch (connector.facing()) {
            case NORTH -> {
                placeCourtyardHorizontalWalkway(level, centerX, connectorCenterX, centerZ, halfWidth, floorY,
                        floorState);
                for (int z = interiorMinZ; z <= centerZ; z++) {
                    placeCourtyardWalkwayRow(level, connectorCenterX, z, halfWidth, true, floorY, floorState);
                }
            }
            case SOUTH -> {
                placeCourtyardHorizontalWalkway(level, centerX, connectorCenterX, centerZ, halfWidth, floorY,
                        floorState);
                for (int z = centerZ; z <= interiorMaxZ; z++) {
                    placeCourtyardWalkwayRow(level, connectorCenterX, z, halfWidth, true, floorY, floorState);
                }
            }
            case EAST -> {
                placeCourtyardVerticalWalkway(level, centerX, centerZ, connectorCenterZ, halfWidth, floorY,
                        floorState);
                for (int x = centerX; x <= interiorMaxX; x++) {
                    placeCourtyardWalkwayRow(level, x, connectorCenterZ, halfWidth, false, floorY, floorState);
                }
            }
            case WEST -> {
                placeCourtyardVerticalWalkway(level, centerX, centerZ, connectorCenterZ, halfWidth, floorY,
                        floorState);
                for (int x = interiorMinX; x <= centerX; x++) {
                    placeCourtyardWalkwayRow(level, x, connectorCenterZ, halfWidth, false, floorY, floorState);
                }
            }
            default -> {
            }
        }
    }

    private void placeCourtyardHorizontalWalkway(ServerLevel level, int startX, int endX, int z, int halfWidth,
                                                 int floorY, BlockState floorState) {
        for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); x++) {
            placeCourtyardWalkwayRow(level, x, z, halfWidth, false, floorY, floorState);
        }
    }

    private void placeCourtyardVerticalWalkway(ServerLevel level, int x, int startZ, int endZ, int halfWidth,
                                               int floorY, BlockState floorState) {
        for (int z = Math.min(startZ, endZ); z <= Math.max(startZ, endZ); z++) {
            placeCourtyardWalkwayRow(level, x, z, halfWidth, true, floorY, floorState);
        }
    }

    LinearRunScaffoldStyle linearRunScaffoldStyle(MKPlannedPiece piece) {
        if (!isLinearRun(piece)) {
            return LinearRunScaffoldStyle.ENCLOSED_CORRIDOR;
        }
        String kind = piece.tags().getOrDefault(LINEAR_RUN_KIND_TAG, "enclosed_corridor");
        return switch (kind) {
            case "defensive_wall" -> LinearRunScaffoldStyle.DEFENSIVE_WALL;
            case "solid_wall" -> LinearRunScaffoldStyle.SOLID_WALL;
            case "open_walkway" -> LinearRunScaffoldStyle.OPEN_WALKWAY;
            case "parapet" -> LinearRunScaffoldStyle.PARAPET;
            default -> LinearRunScaffoldStyle.ENCLOSED_CORRIDOR;
        };
    }

    private boolean isLinearRun(MKPlannedPiece piece) {
        String pieceKind = piece.tags().get("tower_piece_kind");
        return LINEAR_RUN_PIECE_KIND.equals(pieceKind) || FLOOR_PLAN_LINEAR_RUN_PIECE_KIND.equals(pieceKind);
    }

    private boolean isFloorPlanLinearRun(MKPlannedPiece piece) {
        return FLOOR_PLAN_LINEAR_RUN_PIECE_KIND.equals(piece.tags().get("tower_piece_kind"));
    }

    private void placeSolidWall(ServerLevel level, BoundingBox bounds, BlockState wallState) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), wallState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private void placeFloorPlanLinearRun(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                                         MKPlannedPiece piece, int shellMargin, int verticalShellThickness,
                                         BlockState floorState, BlockState wallState, BlockState ceilingState) {
        fillLayeredBox(level, geometryBounds.minX(), geometryBounds.minY(), geometryBounds.minZ(),
                geometryBounds.maxX(), geometryBounds.maxY(), geometryBounds.maxZ(), verticalShellThickness,
                floorState, wallState, ceilingState);
        carveFloorPlanLinearRunWalkspace(level, geometryOrigin, piece, shellMargin, verticalShellThickness);
    }

    private void carveFloorPlanLinearRunWalkspace(ServerLevel level, BlockPos geometryOrigin, MKPlannedPiece piece,
                                                  int shellMargin, int verticalShellThickness) {
        List<MKPlannedConnector> horizontalConnectors = piece.connectors().stream()
                .filter(connector -> !connector.facing().getAxis().isVertical())
                .toList();
        if (horizontalConnectors.isEmpty()) {
            return;
        }
        MKPlannedConnector connector = horizontalConnectors.getFirst();
        boolean pathAlongX = connector.facing().getAxis() == Direction.Axis.X;
        int halfWidth = connector.openingWidth() / 2;
        int slopeDelta = parseIntTag(piece.tags(), LINEAR_RUN_SLOPE_DELTA_TAG, 0);
        int interiorMinX = geometryOrigin.getX() + shellMargin;
        int interiorMinZ = geometryOrigin.getZ() + shellMargin;
        int centerX = interiorMinX + (piece.interiorWidth() / 2);
        int centerZ = interiorMinZ + (piece.interiorLength() / 2);
        int pathLength = pathAlongX ? piece.interiorWidth() : piece.interiorLength();
        for (int step = 0; step < pathLength; step++) {
            int rise = getLinearRunRiseForColumn(slopeDelta, step, pathLength);
            int baseY = geometryOrigin.getY() + verticalShellThickness + rise;
            for (int height = 0; height < connector.openingHeight(); height++) {
                for (int width = -halfWidth; width <= halfWidth; width++) {
                    BlockPos pos = pathAlongX ?
                            new BlockPos(interiorMinX + step, baseY + height, centerZ + width) :
                            new BlockPos(centerX + width, baseY + height, interiorMinZ + step);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void placeOpenWalkway(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                                  MKPlannedPiece piece, int shellMargin, int verticalShellThickness,
                                  BlockState floorState) {
        int deckMaxY = geometryOrigin.getY() + Math.max(0, verticalShellThickness - 1);
        for (int x = geometryOrigin.getX(); x <= geometryBounds.maxX(); x++) {
            for (int z = geometryOrigin.getZ(); z <= geometryBounds.maxZ(); z++) {
                for (int y = geometryOrigin.getY(); y <= deckMaxY; y++) {
                    level.setBlock(new BlockPos(x, y, z), floorState, Block.UPDATE_ALL);
                }
            }
        }
        clearInteriorAbove(level, geometryOrigin, piece, shellMargin, verticalShellThickness);
    }

    private void placeParapet(ServerLevel level, BoundingBox geometryBounds, BlockPos geometryOrigin,
                              MKPlannedPiece piece, int shellMargin, BlockState wallState, BlockState floorState) {
        placeSolidWall(level, geometryBounds, wallState);
        int walkY = Math.max(geometryBounds.minY(), geometryBounds.maxY() - 1);
        int interiorMinX = geometryOrigin.getX() + shellMargin;
        int interiorMaxX = interiorMinX + piece.interiorWidth() - 1;
        int interiorMinZ = geometryOrigin.getZ() + shellMargin;
        int interiorMaxZ = interiorMinZ + piece.interiorLength() - 1;
        for (int x = interiorMinX; x <= interiorMaxX; x++) {
            for (int z = interiorMinZ; z <= interiorMaxZ; z++) {
                level.setBlock(new BlockPos(x, walkY, z), floorState, Block.UPDATE_ALL);
                for (int y = walkY + 1; y <= geometryBounds.maxY(); y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void clearInteriorAbove(ServerLevel level, BlockPos geometryOrigin, MKPlannedPiece piece, int shellMargin,
                                    int verticalShellThickness) {
        BlockPos interiorMin = geometryOrigin.offset(shellMargin, verticalShellThickness, shellMargin);
        for (int x = 0; x < piece.interiorWidth(); x++) {
            for (int y = 0; y < piece.interiorHeight(); y++) {
                for (int z = 0; z < piece.interiorLength(); z++) {
                    level.setBlock(interiorMin.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void placeExteriorMargin(ServerLevel level, BoundingBox exportBounds, BoundingBox geometryBounds) {
        BlockState structureVoid = Blocks.STRUCTURE_VOID.defaultBlockState();
        for (int x = exportBounds.minX(); x <= exportBounds.maxX(); x++) {
            for (int y = exportBounds.minY(); y <= exportBounds.maxY(); y++) {
                for (int z = exportBounds.minZ(); z <= exportBounds.maxZ(); z++) {
                    if (x < geometryBounds.minX() || x > geometryBounds.maxX() ||
                            y < geometryBounds.minY() || y > geometryBounds.maxY() ||
                            z < geometryBounds.minZ() || z > geometryBounds.maxZ()) {
                        level.setBlock(new BlockPos(x, y, z), structureVoid, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private void carveInterior(ServerLevel level, BlockPos exportOrigin, MKPlannedPiece piece, int shellMargin,
                               int verticalShellThickness, int geometryInteriorHeight) {
        BlockPos interiorMin = exportOrigin.offset(shellMargin, verticalShellThickness, shellMargin);
        for (int x = 0; x < piece.interiorWidth(); x++) {
            for (int y = 0; y < geometryInteriorHeight; y++) {
                for (int z = 0; z < piece.interiorLength(); z++) {
                    level.setBlock(interiorMin.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void decoratePieceInterior(ServerLevel level, BlockPos geometryOrigin, MKPlannedPiece piece, int shellMargin,
                                       int verticalShellThickness, int geometryInteriorHeight, BlockState floorState) {
        if (!isLinearRun(piece)) {
            return;
        }
        int slopeDelta = parseIntTag(piece.tags(), LINEAR_RUN_SLOPE_DELTA_TAG, 0);
        if (slopeDelta == 0) {
            return;
        }
        int interiorMinX = geometryOrigin.getX() + shellMargin;
        int interiorMinZ = geometryOrigin.getZ() + shellMargin;
        for (int x = 0; x < piece.interiorWidth(); x++) {
            int rise = getLinearRunRiseForColumn(slopeDelta, x, piece.interiorWidth());
            for (int y = 1; y <= Math.min(rise, geometryInteriorHeight); y++) {
                for (int z = 0; z < piece.interiorLength(); z++) {
                    level.setBlock(new BlockPos(interiorMinX + x, geometryOrigin.getY() + verticalShellThickness - 1 + y,
                            interiorMinZ + z), floorState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private void placeConnectorJigsaw(ServerLevel level, MKWorkspaceConnectorDefinition connector, BlockPos exportOrigin) {
        BlockPos connectorPos = exportOrigin.offset(connector.relativePos());
        level.setBlock(connectorPos, Blocks.JIGSAW.defaultBlockState()
                .setValue(JigsawBlock.ORIENTATION, getJigsawOrientation(connector.facing())), Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(connectorPos);
        if (entity instanceof JigsawBlockEntity jigsaw) {
            jigsaw.setName(connector.jigsawName());
            jigsaw.setTarget(connector.jigsawTarget());
            jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, connector.targetPool()));
            jigsaw.setFinalState("minecraft:air");
            jigsaw.setJoint(JigsawBlockEntity.JointType.ALIGNED);
            jigsaw.setChanged();
        }
    }

    private BlockPos connectorPosition(Direction facing, BoundingBox exportBounds, BlockPos geometryOrigin,
                                       int geometryWidth, int geometryLength, int geometryHeight,
                                       int verticalShellThickness, int interiorCenterX, int interiorCenterZ,
                                       int openingBaseY) {
        if (facing == Direction.NORTH) {
            return new BlockPos(interiorCenterX, openingBaseY, exportBounds.minZ());
        } else if (facing == Direction.SOUTH) {
            return new BlockPos(interiorCenterX, openingBaseY, exportBounds.maxZ());
        } else if (facing == Direction.WEST) {
            return new BlockPos(exportBounds.minX(), openingBaseY, interiorCenterZ);
        } else if (facing == Direction.EAST) {
            return new BlockPos(exportBounds.maxX(), openingBaseY, interiorCenterZ);
        } else if (facing == Direction.UP) {
            return new BlockPos(interiorCenterX, geometryOrigin.getY() + geometryHeight - 1, interiorCenterZ);
        }
        return new BlockPos(interiorCenterX, geometryOrigin.getY(), interiorCenterZ);
    }

    private ResourceLocation getJigsawName(MKStructureWorkspace workspace, MKPlannedConnector connector,
                                           ResourceLocation incomingPool) {
        if (!isEmptyPool(incomingPool)) {
            return incomingPool;
        }
        return ResourceLocation.fromNamespaceAndPath(workspace.namespace(), connector.role().getSerializedName());
    }

    private ResourceLocation getJigsawTarget(MKStructureWorkspace workspace, MKPlannedConnector connector,
                                             ResourceLocation targetPool) {
        if (!isEmptyPool(targetPool)) {
            return targetPool;
        }
        return ResourceLocation.fromNamespaceAndPath(workspace.namespace(), getTargetName(connector.role()));
    }

    private boolean isEmptyPool(ResourceLocation pool) {
        return EMPTY_POOL.equals(pool);
    }

    private void extendHorizontalConnectorShell(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin,
                                                MKPlannedPiece piece, MKPlannedConnector connector, int shellMargin,
                                                int verticalShellThickness, int geometryWidth, int geometryLength,
                                                int geometryHeight, BlockState floorState, BlockState wallState,
                                                BlockState ceilingState) {
        if (connector.facing().getAxis().isVertical()) {
            return;
        }
        LinearRunScaffoldStyle style = linearRunScaffoldStyle(piece);
        if (style == LinearRunScaffoldStyle.OPEN_WALKWAY) {
            extendHorizontalConnectorDeck(level, exportBounds, geometryOrigin, piece, connector, shellMargin,
                    verticalShellThickness, floorState);
            return;
        }
        if (style == LinearRunScaffoldStyle.SOLID_WALL || style == LinearRunScaffoldStyle.PARAPET) {
            extendHorizontalConnectorSolid(level, exportBounds, geometryOrigin, piece, connector, shellMargin,
                    geometryHeight, wallState);
            return;
        }
        MKWorkspaceHorizontalExtrusionMode extrusionMode = getHorizontalExtrusionMode(piece, connector, style);
        switch (extrusionMode) {
            case NO_EXTRUSION -> {
                return;
            }
            case FLOOR_ONLY -> {
                extendHorizontalConnectorDeck(level, exportBounds, geometryOrigin, piece, connector, shellMargin,
                        verticalShellThickness, floorState);
                return;
            }
            case TUNNEL_ONLY -> {
                extendHorizontalConnectorTunnelShell(level, exportBounds, geometryOrigin, piece, connector, shellMargin,
                        verticalShellThickness, floorState, wallState, ceilingState);
                return;
            }
            case FULL_FACE -> {
                extendHorizontalConnectorFullFace(level, exportBounds, geometryOrigin, connector, geometryWidth,
                        geometryLength, geometryHeight, verticalShellThickness, floorState, wallState, ceilingState);
                return;
            }
            case FULL_BODY -> {
            }
        }
        int centerX = getConnectorCenterX(geometryOrigin, piece, shellMargin, connector);
        int centerZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, connector);
        int halfWidth = connector.openingWidth() / 2;
        int minY = geometryOrigin.getY();
        int maxY = geometryOrigin.getY() + geometryHeight - 1;

        if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
            int minX = centerX - halfWidth - shellMargin;
            int maxX = centerX + halfWidth + shellMargin;
            int startZ = connector.facing() == Direction.NORTH ? exportBounds.minZ() :
                    geometryOrigin.getZ() + shellMargin + piece.interiorLength();
            int endZ = connector.facing() == Direction.NORTH ? geometryOrigin.getZ() + shellMargin - 1 :
                    exportBounds.maxZ();
            for (int z = startZ; z <= endZ; z++) {
                fillConnectorShellColumn(level, minX, maxX, minY, maxY, z, true,
                        verticalShellThickness, floorState, wallState, ceilingState);
            }
            return;
        }

        int minZ = centerZ - halfWidth - shellMargin;
        int maxZ = centerZ + halfWidth + shellMargin;
        int startX = connector.facing() == Direction.WEST ? exportBounds.minX() :
                geometryOrigin.getX() + shellMargin + piece.interiorWidth();
        int endX = connector.facing() == Direction.WEST ? geometryOrigin.getX() + shellMargin - 1 :
                exportBounds.maxX();
        for (int x = startX; x <= endX; x++) {
            fillConnectorShellColumn(level, minZ, maxZ, minY, maxY, x, false,
                    verticalShellThickness, floorState, wallState, ceilingState);
        }
    }

    private void extendHorizontalConnectorFullFace(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin,
                                                   MKPlannedConnector connector, int geometryWidth,
                                                   int geometryLength, int geometryHeight, int verticalShellThickness,
                                                   BlockState floorState, BlockState wallState,
                                                   BlockState ceilingState) {
        fillConnectorFace(level, exportBounds, geometryOrigin, connector, geometryWidth, geometryLength,
                geometryHeight, verticalShellThickness, floorState, wallState, ceilingState);
    }

    private void fillConnectorFace(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin,
                                   MKPlannedConnector connector, int geometryWidth, int geometryLength,
                                   int geometryHeight, int verticalShellThickness, BlockState floorState,
                                   BlockState wallState, BlockState ceilingState) {
        int minY = geometryOrigin.getY();
        int maxY = geometryOrigin.getY() + geometryHeight - 1;
        int minX = geometryOrigin.getX();
        int maxX = geometryOrigin.getX() + geometryWidth - 1;
        int minZ = geometryOrigin.getZ();
        int maxZ = geometryOrigin.getZ() + geometryLength - 1;
        if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
            int startZ = connector.facing() == Direction.NORTH ? exportBounds.minZ() : maxZ + 1;
            int endZ = connector.facing() == Direction.NORTH ? minZ - 1 : exportBounds.maxZ();
            fillLayeredBox(level, minX, minY, startZ, maxX, maxY, endZ, verticalShellThickness,
                    floorState, wallState, ceilingState);
            return;
        }

        int startX = connector.facing() == Direction.WEST ? exportBounds.minX() : maxX + 1;
        int endX = connector.facing() == Direction.WEST ? minX - 1 : exportBounds.maxX();
        fillLayeredBox(level, startX, minY, minZ, endX, maxY, maxZ, verticalShellThickness,
                floorState, wallState, ceilingState);
    }

    private void fillLayeredBox(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                int verticalShellThickness, BlockState floorState, BlockState wallState,
                                BlockState ceilingState) {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                BlockState state = connectorFaceLayerState(y, minY, maxY, verticalShellThickness, floorState,
                        wallState, ceilingState);
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
                }
            }
        }
    }

    BlockState connectorFaceLayerState(int y, int minY, int maxY, int verticalShellThickness, BlockState floorState,
                                       BlockState wallState, BlockState ceilingState) {
        return switch (connectorFaceLayer(y, minY, maxY, verticalShellThickness)) {
            case FLOOR -> floorState;
            case CEILING -> ceilingState;
            case WALL -> wallState;
        };
    }

    ConnectorFaceLayer connectorFaceLayer(int y, int minY, int maxY, int verticalShellThickness) {
        if (y < minY + verticalShellThickness) {
            return ConnectorFaceLayer.FLOOR;
        }
        if (y > maxY - verticalShellThickness) {
            return ConnectorFaceLayer.CEILING;
        }
        return ConnectorFaceLayer.WALL;
    }

    private void fillBox(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                         BlockState state) {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
                }
            }
        }
    }

    private void extendHorizontalConnectorDeck(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin,
                                               MKPlannedPiece piece, MKPlannedConnector connector, int shellMargin,
                                               int verticalShellThickness, BlockState floorState) {
        int centerX = getConnectorCenterX(geometryOrigin, piece, shellMargin, connector);
        int centerZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, connector);
        int halfWidth = connector.openingWidth() / 2;
        int deckMinY = geometryOrigin.getY();
        int deckMaxY = geometryOrigin.getY() + Math.max(0, verticalShellThickness - 1);
        if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
            int minX = centerX - halfWidth - shellMargin;
            int maxX = centerX + halfWidth + shellMargin;
            int startZ = connector.facing() == Direction.NORTH ? exportBounds.minZ() :
                    geometryOrigin.getZ() + shellMargin + piece.interiorLength();
            int endZ = connector.facing() == Direction.NORTH ? geometryOrigin.getZ() + shellMargin - 1 :
                    exportBounds.maxZ();
            for (int z = startZ; z <= endZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = deckMinY; y <= deckMaxY; y++) {
                        level.setBlock(new BlockPos(x, y, z), floorState, Block.UPDATE_ALL);
                    }
                }
            }
            return;
        }

        int minZ = centerZ - halfWidth - shellMargin;
        int maxZ = centerZ + halfWidth + shellMargin;
        int startX = connector.facing() == Direction.WEST ? exportBounds.minX() :
                geometryOrigin.getX() + shellMargin + piece.interiorWidth();
        int endX = connector.facing() == Direction.WEST ? geometryOrigin.getX() + shellMargin - 1 :
                exportBounds.maxX();
        for (int x = startX; x <= endX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = deckMinY; y <= deckMaxY; y++) {
                    level.setBlock(new BlockPos(x, y, z), floorState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private void extendHorizontalConnectorSolid(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin,
                                                MKPlannedPiece piece, MKPlannedConnector connector, int shellMargin,
                                                int geometryHeight, BlockState wallState) {
        int centerX = getConnectorCenterX(geometryOrigin, piece, shellMargin, connector);
        int centerZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, connector);
        int halfWidth = connector.openingWidth() / 2;
        int minY = geometryOrigin.getY();
        int maxY = geometryOrigin.getY() + geometryHeight - 1;
        if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
            int minX = centerX - halfWidth - shellMargin;
            int maxX = centerX + halfWidth + shellMargin;
            int startZ = connector.facing() == Direction.NORTH ? exportBounds.minZ() :
                    geometryOrigin.getZ() + shellMargin + piece.interiorLength();
            int endZ = connector.facing() == Direction.NORTH ? geometryOrigin.getZ() + shellMargin - 1 :
                    exportBounds.maxZ();
            for (int z = startZ; z <= endZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        level.setBlock(new BlockPos(x, y, z), wallState, Block.UPDATE_ALL);
                    }
                }
            }
            return;
        }

        int minZ = centerZ - halfWidth - shellMargin;
        int maxZ = centerZ + halfWidth + shellMargin;
        int startX = connector.facing() == Direction.WEST ? exportBounds.minX() :
                geometryOrigin.getX() + shellMargin + piece.interiorWidth();
        int endX = connector.facing() == Direction.WEST ? geometryOrigin.getX() + shellMargin - 1 :
                exportBounds.maxX();
        for (int x = startX; x <= endX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    level.setBlock(new BlockPos(x, y, z), wallState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private void fillConnectorShellColumn(ServerLevel level, int minAcross, int maxAcross, int minY, int maxY,
                                     int fixedAxisValue, boolean fixedZ, int verticalShellThickness,
                                     BlockState floorState, BlockState wallState, BlockState ceilingState) {
        for (int across = minAcross; across <= maxAcross; across++) {
            for (int y = minY; y <= maxY; y++) {
                BlockState state;
                if (y < minY + verticalShellThickness) {
                    state = floorState;
                } else if (y > maxY - verticalShellThickness) {
                    state = ceilingState;
                } else {
                    state = wallState;
                }
                BlockPos pos = fixedZ ? new BlockPos(across, y, fixedAxisValue) :
                        new BlockPos(fixedAxisValue, y, across);
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        }
    }

    private void extendHorizontalConnectorTunnelShell(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin,
                                                      MKPlannedPiece piece, MKPlannedConnector connector, int shellMargin,
                                                      int verticalShellThickness, BlockState floorState,
                                                      BlockState wallState, BlockState ceilingState) {
        int centerX = getConnectorCenterX(geometryOrigin, piece, shellMargin, connector);
        int centerZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, connector);
        int baseY = getOpeningBaseY(geometryOrigin, verticalShellThickness, connector);
        int halfWidth = connector.openingWidth() / 2;
        int leftBound = -halfWidth - shellMargin;
        int rightBound = halfWidth + shellMargin;
        int floorTopY = Math.max(geometryOrigin.getY(), baseY - 1);
        int ceilingBottomY = baseY + connector.openingHeight();
        if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
            int startZ = connector.facing() == Direction.NORTH ? exportBounds.minZ() :
                    geometryOrigin.getZ() + shellMargin + piece.interiorLength();
            int endZ = connector.facing() == Direction.NORTH ? geometryOrigin.getZ() + shellMargin - 1 :
                    exportBounds.maxZ();
            for (int z = startZ; z <= endZ; z++) {
                for (int offset = leftBound; offset <= rightBound; offset++) {
                    int x = centerX + offset;
                    for (int thickness = 0; thickness < verticalShellThickness; thickness++) {
                        level.setBlock(new BlockPos(x, floorTopY - thickness, z), floorState, Block.UPDATE_ALL);
                        level.setBlock(new BlockPos(x, ceilingBottomY + thickness, z), wallState, Block.UPDATE_ALL);
                    }
                }
                fillTunnelSideWalls(level, centerX, z, true, baseY, connector.openingHeight(), halfWidth, shellMargin,
                        wallState);
            }
            return;
        }

        int startX = connector.facing() == Direction.WEST ? exportBounds.minX() :
                geometryOrigin.getX() + shellMargin + piece.interiorWidth();
        int endX = connector.facing() == Direction.WEST ? geometryOrigin.getX() + shellMargin - 1 :
                exportBounds.maxX();
        for (int x = startX; x <= endX; x++) {
            for (int offset = leftBound; offset <= rightBound; offset++) {
                int z = centerZ + offset;
                for (int thickness = 0; thickness < verticalShellThickness; thickness++) {
                    level.setBlock(new BlockPos(x, floorTopY - thickness, z), floorState, Block.UPDATE_ALL);
                    level.setBlock(new BlockPos(x, ceilingBottomY + thickness, z), wallState, Block.UPDATE_ALL);
                }
            }
            fillTunnelSideWalls(level, centerZ, x, false, baseY, connector.openingHeight(), halfWidth, shellMargin,
                    wallState);
        }
    }

    private void fillTunnelSideWalls(ServerLevel level, int centerAcross, int fixedAxisValue, boolean fixedZ, int baseY,
                                     int openingHeight, int halfWidth, int shellMargin, BlockState wallState) {
        int leftStart = centerAcross - halfWidth - shellMargin;
        int leftEnd = centerAcross - halfWidth - 1;
        int rightStart = centerAcross + halfWidth + 1;
        int rightEnd = centerAcross + halfWidth + shellMargin;
        fillTunnelWallSegment(level, leftStart, leftEnd, fixedAxisValue, fixedZ, baseY, openingHeight, wallState);
        fillTunnelWallSegment(level, rightStart, rightEnd, fixedAxisValue, fixedZ, baseY, openingHeight, wallState);
    }

    private void fillTunnelWallSegment(ServerLevel level, int startAcross, int endAcross, int fixedAxisValue,
                                       boolean fixedZ, int baseY, int openingHeight, BlockState wallState) {
        if (startAcross > endAcross) {
            return;
        }
        for (int across = startAcross; across <= endAcross; across++) {
            for (int y = baseY; y < baseY + openingHeight; y++) {
                BlockPos pos = fixedZ ? new BlockPos(across, y, fixedAxisValue) :
                        new BlockPos(fixedAxisValue, y, across);
                level.setBlock(pos, wallState, Block.UPDATE_ALL);
            }
        }
    }

    private void carveConnectorOpening(ServerLevel level, BoundingBox exportBounds, BlockPos geometryOrigin, MKPlannedPiece piece,
                                       MKPlannedConnector connector, int shellMargin, int verticalShellThickness,
                                       int geometryWidth,
                                       int geometryLength, int geometryHeight) {
        int baseY = getOpeningBaseY(geometryOrigin, verticalShellThickness, connector);
        int centerX;
        int centerZ;
        if (connector.facing() == Direction.UP || connector.facing() == Direction.DOWN) {
            centerX = getVerticalCenterX(geometryOrigin, piece, shellMargin);
            centerZ = getVerticalCenterZ(geometryOrigin, piece, shellMargin);
        } else {
            centerX = getConnectorCenterX(geometryOrigin, piece, shellMargin, connector);
            centerZ = getConnectorCenterZ(geometryOrigin, piece, shellMargin, connector);
        }
        int halfWidth = connector.openingWidth() / 2;
        int halfDepth = connector.openingHeight() / 2;
        if (connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH) {
            int startZ = connector.facing() == Direction.NORTH ? exportBounds.minZ() :
                    geometryOrigin.getZ() + shellMargin + piece.interiorLength();
            int endZ = connector.facing() == Direction.NORTH ? geometryOrigin.getZ() + shellMargin - 1 :
                    exportBounds.maxZ();
            for (int z = startZ; z <= endZ; z++) {
                for (int height = 0; height < connector.openingHeight(); height++) {
                    for (int width = -halfWidth; width <= halfWidth; width++) {
                        level.setBlock(new BlockPos(centerX + width, baseY + height, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        } else if (connector.facing() == Direction.WEST || connector.facing() == Direction.EAST) {
            int startX = connector.facing() == Direction.WEST ? exportBounds.minX() :
                    geometryOrigin.getX() + shellMargin + piece.interiorWidth();
            int endX = connector.facing() == Direction.WEST ? geometryOrigin.getX() + shellMargin - 1 :
                    exportBounds.maxX();
            for (int x = startX; x <= endX; x++) {
                for (int height = 0; height < connector.openingHeight(); height++) {
                    for (int width = -halfWidth; width <= halfWidth; width++) {
                        level.setBlock(new BlockPos(x, baseY + height, centerZ + width), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        } else {
            int carveThickness = verticalShellThickness;
            for (int thickness = 0; thickness < carveThickness; thickness++) {
                int y = connector.facing() == Direction.UP ? geometryOrigin.getY() + geometryHeight - 1 - thickness :
                        geometryOrigin.getY() + thickness;
                for (int xOffset = -halfWidth; xOffset <= halfWidth; xOffset++) {
                    for (int zOffset = -halfDepth; zOffset <= halfDepth; zOffset++) {
                        level.setBlock(new BlockPos(centerX + xOffset, y, centerZ + zOffset), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private FrontAndTop getJigsawOrientation(Direction facing) {
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return FrontAndTop.fromFrontAndTop(facing, Direction.NORTH);
        }
        return FrontAndTop.fromFrontAndTop(facing, Direction.UP);
    }

    MKWorkspaceHorizontalExtrusionMode getHorizontalExtrusionMode(MKPlannedPiece piece, MKPlannedConnector connector,
                                                                  LinearRunScaffoldStyle style) {
        if (connector.horizontalExtrusionModeOverride() != null) {
            return connector.horizontalExtrusionModeOverride();
        }
        if (style == LinearRunScaffoldStyle.DEFENSIVE_WALL) {
            return MKWorkspaceHorizontalExtrusionMode.FULL_FACE;
        }
        String legacyStitchMode = piece.tags().getOrDefault(CONNECTOR_STITCH_TAG, "");
        if ("full_face".equals(legacyStitchMode) || "wall_run".equals(legacyStitchMode)) {
            return MKWorkspaceHorizontalExtrusionMode.FULL_FACE;
        }
        return MKWorkspaceHorizontalExtrusionMode.fromSerializedName(piece.tags().getOrDefault(
                HORIZONTAL_EXTRUSION_MODE_TAG, MKWorkspaceHorizontalExtrusionMode.FULL_BODY.getSerializedName()));
    }

    private int getVerticalCenterX(BlockPos geometryOrigin, MKPlannedPiece piece, int shellMargin) {
        MKVerticalAccessPlacement placement = getTowerStairPlacement(piece);
        int interiorMinX = geometryOrigin.getX() + shellMargin;
        int interiorMaxX = interiorMinX + piece.interiorWidth() - 1;
        int shaftHalf = getShaftHalfWidth(piece);
        return switch (placement) {
            case WEST -> interiorMinX + shaftHalf;
            case EAST -> interiorMaxX - shaftHalf;
            default -> interiorMinX + (piece.interiorWidth() / 2);
        };
    }

    private int getVerticalCenterZ(BlockPos geometryOrigin, MKPlannedPiece piece, int shellMargin) {
        MKVerticalAccessPlacement placement = getTowerStairPlacement(piece);
        int interiorMinZ = geometryOrigin.getZ() + shellMargin;
        int interiorMaxZ = interiorMinZ + piece.interiorLength() - 1;
        int shaftHalf = getShaftHalfWidth(piece);
        return switch (placement) {
            case NORTH -> interiorMinZ + shaftHalf;
            case SOUTH -> interiorMaxZ - shaftHalf;
            default -> interiorMinZ + (piece.interiorLength() / 2);
        };
    }

    private int getShaftHalfWidth(MKPlannedPiece piece) {
        int openingWidth = piece.connectors().stream()
                .filter(connector -> connector.facing() == Direction.UP || connector.facing() == Direction.DOWN)
                .mapToInt(MKPlannedConnector::openingWidth)
                .findFirst()
                .orElse(1);
        return openingWidth / 2;
    }

    private MKVerticalAccessPlacement getTowerStairPlacement(MKPlannedPiece piece) {
        return MKVerticalAccessPlacement.fromSerializedName(
                MKWorkspaceVerticalAccessTags.placement(piece.tags(), MKVerticalAccessPlacement.CENTER.getSerializedName())
        );
    }

    private int getConnectorCenterX(BlockPos geometryOrigin, MKPlannedPiece piece, int shellMargin, MKPlannedConnector connector) {
        if (connector.facing() == Direction.UP || connector.facing() == Direction.DOWN) {
            return getVerticalCenterX(geometryOrigin, piece, shellMargin);
        }
        return geometryOrigin.getX() + shellMargin + (piece.interiorWidth() / 2) + connector.lateralOffset();
    }

    private int getConnectorCenterZ(BlockPos geometryOrigin, MKPlannedPiece piece, int shellMargin, MKPlannedConnector connector) {
        if (connector.facing() == Direction.UP || connector.facing() == Direction.DOWN) {
            return getVerticalCenterZ(geometryOrigin, piece, shellMargin);
        }
        return geometryOrigin.getZ() + shellMargin + (piece.interiorLength() / 2) + connector.lateralOffset();
    }

    private int getOpeningBaseY(BlockPos geometryOrigin, int verticalShellThickness, MKPlannedConnector connector) {
        return geometryOrigin.getY() + verticalShellThickness + connector.verticalOffset();
    }

    private void validateConnectorBounds(MKPlannedPiece piece, MKPlannedConnector connector, int shellMargin,
                                         int localBaseY, int geometryInteriorHeight) {
        if (connector.facing() == Direction.UP || connector.facing() == Direction.DOWN) {
            return;
        }
        int halfWidth = connector.openingWidth() / 2;
        int centeredHalfSpan = connector.facing() == Direction.NORTH || connector.facing() == Direction.SOUTH ?
                piece.interiorWidth() / 2 : piece.interiorLength() / 2;
        if (Math.abs(connector.lateralOffset()) + halfWidth > centeredHalfSpan) {
            throw new IllegalStateException("connector " + connector.role().getSerializedName() +
                    " opening exceeds the available wall span for piece " + piece.pieceName());
        }
        if (localBaseY < Math.max(1, 0 + 1)) {
            throw new IllegalStateException("connector " + connector.role().getSerializedName() +
                    " opening base is below the room interior for piece " + piece.pieceName());
        }
        if ((localBaseY - 1) + connector.openingHeight() > geometryInteriorHeight) {
            throw new IllegalStateException("connector " + connector.role().getSerializedName() +
                    " opening exceeds the room height for piece " + piece.pieceName());
        }
    }

    private BlockPos placeStructureBlock(ServerLevel level, MKStructureWorkspace workspace, MKPlannedPiece piece,
                                         BlockPos exportOrigin, int exportWidth, int exportHeight, int exportLength) {
        BlockPos structurePos = exportOrigin.offset(-2, 1, exportLength / 2);
        level.setBlock(structurePos, Blocks.STRUCTURE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(structurePos);
        if (entity instanceof StructureBlockEntity structureBlock) {
            structureBlock.setMode(StructureMode.SAVE);
            structureBlock.setIgnoreEntities(true);
            structureBlock.setShowBoundingBox(true);
            structureBlock.setStructureName(ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                    workspace.structureName() + "/" + piece.pieceName()));
            structureBlock.setStructurePos(exportOrigin.subtract(structurePos));
            structureBlock.setStructureSize(new Vec3i(exportWidth, exportHeight, exportLength));
            structureBlock.setChanged();
        }
        return structurePos;
    }

    private BlockPos placeSign(ServerLevel level, MKStructureWorkspace workspace, MKPlannedPiece piece, BlockPos structureBlockPos) {
        BlockPos signPos = structureBlockPos.west();
        BlockState signState = Blocks.OAK_SIGN.defaultBlockState();
        level.setBlock(signPos, signState, Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(signPos);
        if (entity instanceof SignBlockEntity sign) {
            SignText text = sign.getFrontText()
                    .setMessage(0, Component.literal(workspace.namespace()))
                    .setMessage(1, Component.literal(workspace.structureName()))
                    .setMessage(2, Component.literal(piece.tags().getOrDefault("workspace_topology_slot_id",
                            piece.roleId())))
                    .setMessage(3, Component.literal(piece.pieceName()));
            sign.setText(text, true);
            sign.setText(text, false);
            sign.setChanged();
            level.sendBlockUpdated(signPos, signState, signState, Block.UPDATE_ALL);
        }
        return signPos;
    }

    private BlockPos placeConnectorMarker(ServerLevel level, MKWorkspaceConnectorDefinition connector, BoundingBox exportBounds) {
        BlockPos wallPos = new BlockPos(
                exportBounds.minX() + connector.relativePos().getX(),
                exportBounds.minY() + connector.relativePos().getY(),
                exportBounds.minZ() + connector.relativePos().getZ()
        );
        BlockPos markerPos = switch (connector.facing()) {
            case NORTH -> new BlockPos(wallPos.getX(), wallPos.getY(), exportBounds.minZ() - 1);
            case SOUTH -> new BlockPos(wallPos.getX(), wallPos.getY(), exportBounds.maxZ() + 1);
            case WEST -> new BlockPos(exportBounds.minX() - 1, wallPos.getY(), wallPos.getZ());
            case EAST -> new BlockPos(exportBounds.maxX() + 1, wallPos.getY(), wallPos.getZ());
            case UP -> new BlockPos(wallPos.getX(), exportBounds.maxY() + 1, wallPos.getZ());
            case DOWN -> new BlockPos(wallPos.getX(), exportBounds.minY() - 1, wallPos.getZ());
        };
        level.setBlock(markerPos, getMarkerState(connector.role()), Block.UPDATE_ALL);
        return markerPos;
    }

    private ResourceLocation getConnectorPool(MKStructureWorkspace workspace, String explicitTargetBaseName, MKPlannedPiece piece) {
        if (explicitTargetBaseName != null) {
            return parseConnectorPool(workspace, explicitTargetBaseName);
        }
        return getConnectorPool(workspace, piece);
    }

    private ResourceLocation getConnectorPool(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        String baseName = piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, deriveBasePoolName(piece.pieceName()));
        return parseConnectorPool(workspace, baseName);
    }

    private ResourceLocation parseConnectorPool(MKStructureWorkspace workspace, String poolName) {
        if (poolName.contains(":")) {
            return ResourceLocation.parse(poolName);
        }
        return ResourceLocation.fromNamespaceAndPath(workspace.namespace(), workspace.structureName() + "/" + poolName);
    }

    private ResourceLocation getIncomingConnectorPool(MKStructureWorkspace workspace, String incomingPoolName) {
        if (incomingPoolName == null || incomingPoolName.isBlank()) {
            return ResourceLocation.parse("minecraft:empty");
        }
        return parseConnectorPool(workspace, incomingPoolName);
    }

    private String deriveBasePoolName(String pieceName) {
        if (pieceName.endsWith("_template")) {
            return pieceName.substring(0, pieceName.length() - "_template".length());
        }
        int suffixIndex = pieceName.lastIndexOf('_');
        if (suffixIndex > 0) {
            boolean numericSuffix = true;
            for (int i = suffixIndex + 1; i < pieceName.length(); i++) {
                if (!Character.isDigit(pieceName.charAt(i))) {
                    numericSuffix = false;
                    break;
                }
            }
            if (numericSuffix) {
                return pieceName.substring(0, suffixIndex);
            }
        }
        return pieceName;
    }

    private BlockState getMarkerState(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD, MAIN_BACK -> Blocks.BLUE_WOOL.defaultBlockState();
            case CONNECT_UP, CONNECT_DOWN -> Blocks.ORANGE_WOOL.defaultBlockState();
            case TOP_CAP_FORWARD, TOP_CAP_BACK -> Blocks.RED_WOOL.defaultBlockState();
            case BRANCH -> Blocks.GREEN_WOOL.defaultBlockState();
            case LINK_CANDIDATE -> Blocks.LIME_WOOL.defaultBlockState();
            default -> Blocks.WHITE_WOOL.defaultBlockState();
        };
    }

    private String getTargetName(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD -> MKConnectorRole.MAIN_BACK.getSerializedName();
            case MAIN_BACK -> MKConnectorRole.MAIN_FORWARD.getSerializedName();
            case CONNECT_UP -> MKConnectorRole.CONNECT_DOWN.getSerializedName();
            case CONNECT_DOWN -> MKConnectorRole.CONNECT_UP.getSerializedName();
            case TOP_CAP_FORWARD -> MKConnectorRole.TOP_CAP_BACK.getSerializedName();
            case TOP_CAP_BACK -> MKConnectorRole.TOP_CAP_FORWARD.getSerializedName();
            case BRANCH -> MKConnectorRole.BRANCH.getSerializedName();
            case LINK_CANDIDATE -> MKConnectorRole.LINK_CANDIDATE.getSerializedName();
            default -> throw new IllegalStateException("Unsupported workspace connector role " + role);
        };
    }

    private BlockState resolveBlockState(ResourceLocation id, BlockState fallback) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback.getBlock());
        return block.defaultBlockState();
    }

    private BlockState resolvePaletteState(MKStructureWorkspace workspace, MKPlannedPiece piece, String tagName,
                                           ResourceLocation fallbackId, BlockState fallbackState) {
        String overrideId = piece.tags().get(tagName);
        if (overrideId == null || overrideId.isBlank()) {
            return resolveBlockState(fallbackId, fallbackState);
        }
        return resolveBlockState(ResourceLocation.parse(overrideId), fallbackState);
    }

    private int parseIntTag(Map<String, String> tags, String tagName, int fallback) {
        try {
            return Integer.parseInt(tags.getOrDefault(tagName, Integer.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
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

    private int getLinearRunRiseForColumn(int slopeDelta, int columnIndex, int linearRunLength) {
        int absoluteSlope = Math.abs(slopeDelta);
        if (absoluteSlope == 0 || linearRunLength <= 1) {
            return Math.max(0, slopeDelta);
        }
        int rise = (columnIndex * absoluteSlope) / (linearRunLength - 1);
        if (slopeDelta < 0) {
            return absoluteSlope - rise;
        }
        return rise;
    }
}


