package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKFloorConnectorPatchTest {
    @Test
    void northSouthPatchUsesExactOddWidthFootprint() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.NORTH, new BlockPos(10, 3, 2), 3, 2);

        List<BlockPos> positions = MKFloorConnectorPatch.closedConnectorPatchPositions(piece);

        assertEquals(6, positions.size());
        assertEquals(Set.of(
                new BlockPos(9, 3, 2),
                new BlockPos(10, 3, 2),
                new BlockPos(11, 3, 2),
                new BlockPos(9, 4, 2),
                new BlockPos(10, 4, 2),
                new BlockPos(11, 4, 2)
        ), Set.copyOf(positions));
    }

    @Test
    void eastWestPatchUsesExactEvenWidthFootprint() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.EAST, new BlockPos(4, 1, 8), 4, 3);

        List<BlockPos> positions = MKFloorConnectorPatch.closedConnectorPatchPositions(piece);

        assertEquals(12, positions.size());
        assertEquals(Set.of(
                new BlockPos(4, 1, 7),
                new BlockPos(4, 1, 8),
                new BlockPos(4, 1, 9),
                new BlockPos(4, 1, 10),
                new BlockPos(4, 2, 7),
                new BlockPos(4, 2, 8),
                new BlockPos(4, 2, 9),
                new BlockPos(4, 2, 10),
                new BlockPos(4, 3, 7),
                new BlockPos(4, 3, 8),
                new BlockPos(4, 3, 9),
                new BlockPos(4, 3, 10)
        ), Set.copyOf(positions));
    }

    @Test
    void nonHorizontalClosedConnectorDoesNotPatch() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.UP, new BlockPos(4, 1, 8), 3, 3);

        assertTrue(MKFloorConnectorPatch.closedConnectorPatchPositions(piece).isEmpty());
    }

    @Test
    void closedConnectorPatchHonorsClosureDepth() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.WEST, new BlockPos(2, 1, 8), 3, 2, 3);

        List<BlockPos> positions = MKFloorConnectorPatch.closedConnectorPatchPositions(piece);

        assertEquals(18, positions.size());
        assertTrue(positions.contains(new BlockPos(2, 1, 8)));
        assertTrue(positions.contains(new BlockPos(3, 1, 8)));
        assertTrue(positions.contains(new BlockPos(4, 1, 8)));
        assertTrue(positions.contains(new BlockPos(4, 2, 9)));
    }

    @Test
    void closureDepthExtendsOuterConnectorToInnerShellWall() {
        MKWorkspacePieceDefinition piece = closureDepthPiece(new BoundingBox(0, 0, 0, 22, 8, 22));

        assertEquals(3, MKFloorConnectorPatch.closureDepth(piece,
                connector(MKConnectorRole.BRANCH, Direction.WEST, new BlockPos(0, 1, 11))));
        assertEquals(3, MKFloorConnectorPatch.closureDepth(piece,
                connector(MKConnectorRole.BRANCH, Direction.NORTH, new BlockPos(11, 1, 0))));
        assertEquals(3, MKFloorConnectorPatch.closureDepth(piece,
                connector(MKConnectorRole.BRANCH, Direction.EAST, new BlockPos(22, 1, 11))));
        assertEquals(3, MKFloorConnectorPatch.closureDepth(piece,
                connector(MKConnectorRole.MAIN_BACK, Direction.SOUTH, new BlockPos(11, 1, 22))));
    }

    @Test
    void randomizedMainExitExportPromotesSelectedBranchConnectorToMain() {
        MKWorkspacePieceDefinition source = randomizedMainExitTemplate();
        MKWorkspacePieceDefinition runtimeSource = runtimeContentVariant(source);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(source, runtimeSource));

        List<MKWorkspacePieceDefinition> exported = MKFloorMaskVariantExporter.exportPieces(workspace, true);
        List<MKWorkspacePieceDefinition> variants = exported.stream()
                .filter(piece -> "instance".equals(piece.tags().get("workspace_piece_kind")))
                .filter(piece -> piece.tags().containsKey(MKFloorMaskVariantExporter.FLOOR_MASK_TAG))
                .toList();
        MKWorkspacePieceDefinition eastMainAllBranches = variants.stream()
                .filter(piece -> "east".equals(piece.tags().get(MKFloorMaskVariantExporter.FLOOR_SELECTED_MAIN_EXIT_TAG)))
                .filter(piece -> "nw".equals(piece.tags().get(MKFloorMaskVariantExporter.FLOOR_MASK_TAG)))
                .findFirst()
                .orElseThrow();

        assertEquals(12, variants.size());
        assertTrue(eastMainAllBranches.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.MAIN_BACK && connector.facing() == Direction.EAST));
        assertTrue(eastMainAllBranches.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH && connector.facing() == Direction.NORTH));
        assertTrue(eastMainAllBranches.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.BRANCH && connector.facing() == Direction.WEST));
    }

    @Test
    void randomizedMainExitRuntimeMetadataClosesFormerMainExit() {
        MKWorkspacePieceDefinition source = linkCandidateTemplate(true);
        MKWorkspacePieceDefinition runtimeSource = runtimeContentVariant(source);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(runtimeStartPiece(), source, runtimeSource));

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = manifest.runtimeHints().templateGroups().stream()
                .filter(templateGroup -> "floor_main_room_link_1_main_w_mask_none".equals(templateGroup.baseName()))
                .findFirst()
                .orElseThrow();

        assertTrue(group.pieceMetadata().floorLinkCandidates().stream().anyMatch(candidate ->
                candidate.facing() == Direction.NORTH));
    }

    @Test
    void linkCandidateExportIsPatchedClosedByDefault() {
        MKWorkspacePieceDefinition source = linkCandidateTemplate(false);
        MKWorkspacePieceDefinition runtimeSource = runtimeContentVariant(source);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(source, runtimeSource));

        MKWorkspacePieceDefinition variant = MKFloorMaskVariantExporter.exportPieces(workspace, true).stream()
                .filter(piece -> "instance".equals(piece.tags().get("workspace_piece_kind")))
                .filter(piece -> piece.tags().containsKey(MKFloorMaskVariantExporter.FLOOR_MASK_TAG))
                .findFirst()
                .orElseThrow();

        assertFalse(variant.connectors().stream().anyMatch(connector ->
                connector.role() == MKConnectorRole.LINK_CANDIDATE));
        assertEquals("1", variant.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_COUNT_TAG));
        assertEquals("link_candidate", variant.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + "0_role"));
        assertEquals("east", variant.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + "0_facing"));
        assertEquals("0", variant.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + "0_lateral_offset"));
        assertEquals("0", variant.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + "0_vertical_offset"));
    }

    @Test
    void exportRuntimeMetadataIncludesLinkCandidateEndpoints() {
        MKWorkspacePieceDefinition source = linkCandidateTemplate(false);
        MKWorkspacePieceDefinition runtimeSource = runtimeContentVariant(source);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(runtimeStartPiece(), source, runtimeSource));

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = manifest.runtimeHints().templateGroups().stream()
                .filter(templateGroup -> "floor_main_room_link_1_mask_none".equals(templateGroup.baseName()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, group.pieceMetadata().floorLinkCandidates().size());
        assertEquals(Direction.EAST, group.pieceMetadata().floorLinkCandidates().getFirst().facing());
        assertEquals(4, group.pieceMetadata().floorLinkCandidates().getFirst().x());
    }

    @Test
    void mainPathForwardConnectorDoesNotExportClosableOpening() {
        MKWorkspacePieceDefinition mainPathRoom = mainPathPiece(false);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(runtimeStartPiece(), mainPathRoom));

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = manifest.runtimeHints().templateGroups().stream()
                .filter(templateGroup -> "floor_main_cap".equals(templateGroup.baseName()))
                .findFirst()
                .orElseThrow();

        assertTrue(group.pieceMetadata().floorClosableOpenings().isEmpty());
    }

    @Test
    void nonFloorRuntimePieceDoesNotExportClosableOpenings() {
        MKWorkspacePieceDefinition courtyardPath = runtimePiece(
                "keep_courtyard_path_t_east",
                "keep.courtyard.path.east",
                Map.of(
                        "tower_piece_kind", "linear_run",
                        "workspace_topology_slot_id", "keep.courtyard.path.east",
                        "workspace_linear_run_path_kind", "courtyard_path"
                ),
                List.of(connector(MKConnectorRole.BRANCH, Direction.EAST, new BlockPos(30, 1, 15))));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(runtimeStartPiece(), courtyardPath));

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = manifest.runtimeHints().templateGroups().stream()
                .filter(templateGroup -> "keep_courtyard_path_t_east".equals(templateGroup.baseName()))
                .findFirst()
                .orElseThrow();

        assertTrue(group.pieceMetadata().floorClosableOpenings().isEmpty());
        assertTrue(group.pieceMetadata().floorRootExits().isEmpty());
    }

    @Test
    void floorTopologyRootExportsClosableOpeningsAndRootExits() {
        MKWorkspacePieceDefinition floorRoot = runtimePiece(
                "keep_center_basement_floor",
                "keep.center.basement_floor",
                Map.of(
                        "tower_piece_kind", "room",
                        "workspace_topology_slot_id", "keep.center.basement_floor",
                        "workspace_vertical_stack_slot", "basement_floor"
                ),
                List.of(floorRootConnector(MKConnectorRole.BRANCH, Direction.EAST, "branch")));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(runtimeStartPiece(), floorRoot));

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = manifest.runtimeHints().templateGroups().stream()
                .filter(templateGroup -> "keep_center_basement_floor".equals(templateGroup.baseName()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, group.pieceMetadata().floorClosableOpenings().size());
        assertEquals(1, group.pieceMetadata().floorRootExits().size());
        assertEquals("keep.center.basement_floor", group.pieceMetadata().floorRootExits().getFirst().topologyGroup());
    }

    @Test
    void floorPlanLinearRunExportsClosableOpenings() {
        MKWorkspacePieceDefinition hallway = runtimePiece(
                "floor_branch_hallway",
                "tower.floor_plan.linear_run.branch",
                Map.of(
                        "tower_piece_kind", "floor_plan_linear_run",
                        "workspace_topology_slot_id", "tower.floor_plan.linear_run.branch",
                        "workspace_topology_group", "keep.center.basement_floor"
                ),
                List.of(connector(MKConnectorRole.BRANCH, Direction.WEST, new BlockPos(0, 1, 4))));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(runtimeStartPiece(), hallway));

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4, "test");
        MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = manifest.runtimeHints().templateGroups().stream()
                .filter(templateGroup -> "floor_branch_hallway".equals(templateGroup.baseName()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, group.pieceMetadata().floorClosableOpenings().size());
    }

    @Test
    void randomizedMainExitExportPatchesLinkCandidatesClosed() {
        MKWorkspacePieceDefinition source = linkCandidateTemplate(true);
        MKWorkspacePieceDefinition runtimeSource = runtimeContentVariant(source);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(source, runtimeSource));

        List<MKWorkspacePieceDefinition> variants = MKFloorMaskVariantExporter.exportPieces(workspace, true).stream()
                .filter(piece -> "instance".equals(piece.tags().get("workspace_piece_kind")))
                .filter(piece -> piece.tags().containsKey(MKFloorMaskVariantExporter.FLOOR_MASK_TAG))
                .toList();

        assertFalse(variants.isEmpty());
        assertTrue(variants.stream().allMatch(variant ->
                variant.connectors().stream().noneMatch(connector ->
                        connector.role() == MKConnectorRole.LINK_CANDIDATE)));
        assertTrue(variants.stream().allMatch(variant ->
                hasClosedConnectorFacing(variant, Direction.EAST)));
    }

    private static MKWorkspacePieceDefinition closedConnectorPiece(Direction facing, BlockPos pos, int openingWidth,
                                                                   int openingHeight) {
        return closedConnectorPiece(facing, pos, openingWidth, openingHeight, 0);
    }

    private static MKWorkspacePieceDefinition closedConnectorPiece(Direction facing, BlockPos pos, int openingWidth,
                                                                   int openingHeight, int closureDepth) {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_COUNT_TAG, "1");
        String prefix = MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + "0_";
        tags.put(prefix + "facing", facing.getSerializedName());
        tags.put(prefix + "x", Integer.toString(pos.getX()));
        tags.put(prefix + "y", Integer.toString(pos.getY()));
        tags.put(prefix + "z", Integer.toString(pos.getZ()));
        tags.put(prefix + "opening_width", Integer.toString(openingWidth));
        tags.put(prefix + "opening_height", Integer.toString(openingHeight));
        if (closureDepth > 0) {
            tags.put(prefix + "closure_depth", Integer.toString(closureDepth));
        }
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "floor_room_mask_none",
                "tower.primary.main_floor",
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(new MKWorkspaceConnectorDefinition(
                        MKConnectorRole.BRANCH,
                        Direction.SOUTH,
                        BlockPos.ZERO,
                        3,
                        3,
                        0,
                        0,
                        ResourceLocation.parse("mkdev:branch"),
                        ResourceLocation.parse("mkdev:target"),
                        ResourceLocation.parse("minecraft:empty"),
                        ResourceLocation.parse("minecraft:empty")
                )),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition closureDepthPiece(BoundingBox exportBounds) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "floor_room",
                "tower.primary.main_floor",
                0,
                new MKWorkspaceDimensions(17, 17, 9, 9, 9, 3, 3, 3),
                1,
                List.of(),
                BlockPos.ZERO,
                exportBounds,
                exportBounds,
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of()
        );
    }

    private static MKWorkspacePieceDefinition randomizedMainExitTemplate() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tower_piece_kind", "floor_plan_room");
        tags.put("workspace_piece_kind", "template");
        tags.put("workspace_floor_room_kind", "main_room");
        tags.put(MKFloorMaskVariantExporter.FLOOR_RANDOMIZE_MAIN_EXIT_TAG, "true");
        tags.put("workspace_base_name", "floor_main_room");
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "floor_main_room",
                "tower.primary.main_floor",
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(
                        connector(MKConnectorRole.MAIN_FORWARD, Direction.SOUTH),
                        connector(MKConnectorRole.MAIN_BACK, Direction.NORTH),
                        connector(MKConnectorRole.BRANCH, Direction.EAST),
                        connector(MKConnectorRole.BRANCH, Direction.WEST)
                ),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 8, 8, 8),
                new BoundingBox(0, 0, 0, 8, 8, 8),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition linkCandidateTemplate(boolean randomizeMainExit) {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("tower_piece_kind", "floor_plan_room");
        tags.put("workspace_piece_kind", "template");
        tags.put("workspace_floor_room_kind", "main_room");
        tags.put(MKFloorMaskVariantExporter.FLOOR_RANDOMIZE_MAIN_EXIT_TAG, Boolean.toString(randomizeMainExit));
        tags.put("workspace_base_name", "floor_main_room_link");
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, false, false, "tower.primary.main_floor", false)
                .applyToTags(tags);
        List<MKWorkspaceConnectorDefinition> connectors = new java.util.ArrayList<>();
        connectors.add(connector(MKConnectorRole.MAIN_FORWARD, Direction.SOUTH));
        connectors.add(connector(MKConnectorRole.MAIN_BACK, Direction.NORTH));
        if (randomizeMainExit) {
            connectors.add(connector(MKConnectorRole.BRANCH, Direction.WEST));
        }
        connectors.add(connector(MKConnectorRole.LINK_CANDIDATE, Direction.EAST));
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "floor_main_room_link",
                "tower.primary.main_floor",
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.copyOf(connectors),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 8, 8, 8),
                new BoundingBox(0, 0, 0, 8, 8, 8),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition runtimeStartPiece() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_base_name", "start");
        new MKWorkspaceRuntimePieceInfo(true, MKJigsawPieceRole.ROOM, 0, 0,
                true, false, false, false, "", false)
                .applyToTags(tags);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "start",
                "start",
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 8, 8, 8),
                new BoundingBox(0, 0, 0, 8, 8, 8),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition mainPathPiece(boolean mainPathEnding) {
        Map<String, String> tags = new LinkedHashMap<>();
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, false, mainPathEnding, false, "tower.primary.main_floor", mainPathEnding)
                .applyToTags(tags);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "floor_main_cap",
                "tower.primary.main_floor",
                0,
                new MKWorkspaceDimensions(17, 17, 9, 9, 9, 3, 3, 3),
                1,
                List.of(connector(MKConnectorRole.MAIN_FORWARD, Direction.SOUTH, new BlockPos(11, 1, 22))),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 22, 8, 22),
                new BoundingBox(0, 0, 0, 22, 8, 22),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition runtimeContentVariant(MKWorkspacePieceDefinition source) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(source.tags());
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_base_name", source.tags().getOrDefault("workspace_base_name", source.pieceName()));
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                source.workspaceId(),
                source.pieceName() + "_1",
                source.roleId(),
                1,
                source.effectiveDimensions(),
                source.shellMargin(),
                source.connectors(),
                source.worldOrigin(),
                source.exportBounds(),
                source.previewBounds(),
                source.structureBlockPos(),
                source.signPos(),
                source.markerPositions(),
                source.generatedStairPositions(),
                tags
        );
    }

    private static MKWorkspacePieceDefinition runtimePiece(String pieceName, String roleId, Map<String, String> extraTags,
                                                           List<MKWorkspaceConnectorDefinition> connectors) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(extraTags);
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_base_name", pieceName);
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, false, false,
                tags.getOrDefault("workspace_topology_group", ""), false)
                .applyToTags(tags);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                roleId,
                1,
                new MKWorkspaceDimensions(17, 17, 9, 9, 9, 3, 3, 3),
                1,
                connectors,
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 30, 8, 30),
                new BoundingBox(0, 0, 0, 30, 8, 30),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static boolean hasClosedConnectorFacing(MKWorkspacePieceDefinition piece, Direction direction) {
        int count = Integer.parseInt(piece.tags().getOrDefault(
                MKFloorMaskVariantExporter.CLOSED_CONNECTOR_COUNT_TAG, "0"));
        for (int i = 0; i < count; i++) {
            String facing = piece.tags().get(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + i + "_facing");
            if (direction.getSerializedName().equals(facing)) {
                return true;
            }
        }
        return false;
    }

    private static MKWorkspaceConnectorDefinition connector(MKConnectorRole role, Direction facing) {
        return connector(role, facing, BlockPos.ZERO.relative(facing, 4));
    }

    private static MKWorkspaceConnectorDefinition connector(MKConnectorRole role, Direction facing, BlockPos pos) {
        String name = role.getSerializedName();
        return new MKWorkspaceConnectorDefinition(
                role,
                facing,
                pos,
                3,
                3,
                0,
                0,
                ResourceLocation.parse("mkdev:" + name),
                ResourceLocation.parse("mkdev:" + name + "_target"),
                ResourceLocation.parse("mkdev:" + name + "_pool"),
                ResourceLocation.parse("mkdev:" + name + "_incoming")
        );
    }

    private static MKWorkspaceConnectorDefinition floorRootConnector(MKConnectorRole role, Direction facing,
                                                                     String pathKind) {
        String name = role.getSerializedName();
        return new MKWorkspaceConnectorDefinition(
                role,
                facing,
                BlockPos.ZERO.relative(facing, 4),
                3,
                3,
                0,
                0,
                ResourceLocation.parse("mkdev:" + name),
                ResourceLocation.parse("mkdev:" + name + "_target"),
                ResourceLocation.parse("mkdev:test_keep/floor_plan/keep.center.basement_floor/rooms/" +
                        pathKind + "/" + pathKind + "_opening"),
                ResourceLocation.parse("mkdev:" + name + "_incoming")
        );
    }
}
