package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePieceRelayoutServiceTest {
    private final MKWorkspacePieceRelayoutService service = new MKWorkspacePieceRelayoutService();

    @Test
    void catalogSummaryCountsPreservedNewAndRemovedPhysicalPieces() {
        MKWorkspacePieceDefinition preserved = piece("room_a_template", "room_a", 5, 5, 5);
        MKWorkspacePieceDefinition removed = piece("room_old_template", "room_old", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(preserved, removed));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", preserved.plannerId(), 5, 5, 5),
                planned("room_b_template", "room_b", plannerId("room_b"), 5, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(1, summary.newCount());
        assertEquals(1, summary.removedCount());
        assertEquals(0, summary.rebuildRequiredCount());
        assertEquals(3, summary.impacts().size());
        assertEquals(1, countPreservedWorkImpacts(summary));
        assertEquals(1, countImpacts(summary, "new"));
        assertEquals(1, countImpacts(summary, "removed"));
        assertTrue(summary.warnings().stream().anyMatch(warning -> warning.contains("preserved")));
    }

    @Test
    void catalogSummaryTreatsExpansionAsPreservedWork() {
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", existingPiece.plannerId(), 7, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(1, summary.expandedCount());
        assertEquals(0, summary.rebuildRequiredCount());
        assertEquals(1, countImpacts(summary, "expanded"));
    }

    @Test
    void catalogSummaryTreatsShrinkAsAffectedRebuild() {
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", 7, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", existingPiece.plannerId(), 5, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(0, summary.preservedCount());
        assertEquals(1, summary.rebuildRequiredCount());
        assertEquals(0, summary.newCount());
        assertEquals(1, countImpacts(summary, "rebuild"));
    }

    @Test
    void catalogSummarySupportsRemovingAllPhysicalPieces() {
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));

        Optional<MKWorkspacePieceRelayoutService.CatalogRelayoutSummary> summary = service
                .summarizeCatalogRelayout(existing, existing, List.of(), List.of());

        assertTrue(summary.isPresent());
        assertEquals(1, summary.get().removedCount());
    }

    @Test
    void acceptedRemapPreservesCompatiblePhysicalPiece() {
        MKWorkspacePieceDefinition existingPiece = piece("old_room_template", "old_room", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));
        MKWorkspacePlannerId targetPlannerId = plannerId("new_room");
        List<MKPlannedPiece> targetPieces = List.of(
                planned("new_room_template", "new_room", targetPlannerId, 5, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary withoutRemap = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();
        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary withRemap = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces, List.of(
                        new MKWorkspaceTemplateRemapSuggestion(existingPiece.plannerId(), targetPlannerId, 100,
                                "same floor piece kind, dimensions, and connector signature")
                ))
                .orElseThrow();

        assertEquals(1, withoutRemap.newCount());
        assertEquals(1, withoutRemap.removedCount());
        assertEquals(1, withRemap.preservedCount());
        assertEquals(0, withRemap.newCount());
        assertEquals(0, withRemap.removedCount());
        assertEquals(1, countPreservedWorkImpacts(withRemap));
    }

    @Test
    void duplicateExistingPlannerIdsDoNotBlockStableCatalogRelayout() {
        MKWorkspacePlannerId duplicatePlannerId = plannerId("legacy_duplicate");
        MKWorkspacePieceDefinition first = piece("room_a_template", "room_a", duplicatePlannerId,
                5, 5, 5, List.of(), tags("room_a"));
        MKWorkspacePieceDefinition second = piece("room_b_template", "room_b", duplicatePlannerId,
                5, 5, 5, List.of(), tags("room_b"));
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(first, second));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", duplicatePlannerId, 5, 5, 5),
                planned("room_b_template", "room_b", duplicatePlannerId, 5, 5, 5),
                planned("room_c_template", "room_c", plannerId("room_c"), 5, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(2, summary.preservedCount());
        assertEquals(1, summary.newCount());
        assertEquals(0, summary.removedCount());
        assertEquals(2, countPreservedWorkImpacts(summary));
    }

    @Test
    void catalogSummaryPreservesLegacyFloorRoomTagsWithoutProfileId() {
        Map<String, String> existingTags = floorRoomTags(false);
        Map<String, String> targetTags = floorRoomTags(true);
        MKWorkspacePieceDefinition existingPiece = piece("main_room_template", "main_room",
                plannerId("main_room"), 9, 9, 7, List.of(), existingTags);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));
        List<MKPlannedPiece> targetPieces = List.of(planned("main_room_template", "main_room",
                existingPiece.plannerId(), 9, 9, 7, List.of(), targetTags));

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(0, summary.newCount());
        assertEquals(0, summary.removedCount());
        assertEquals(0, summary.rebuildRequiredCount());
        assertEquals(1, countPreservedWorkImpacts(summary));
    }

    @Test
    void catalogSummaryPreservesConnectorsWithResolvedWorkspacePools() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        String localPoolName = "rooms/main/floor_main";
        ResourceLocation targetPool = ResourceLocation.fromNamespaceAndPath(draft.namespace(),
                draft.structureName() + "/" + localPoolName);
        MKWorkspaceConnectorDefinition existingConnector = connector(MKConnectorRole.MAIN_BACK, Direction.NORTH,
                5, 4, 0, 0, targetPool, ResourceLocation.parse("minecraft:empty"));
        MKPlannedConnector plannedConnector = new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.NORTH,
                5, 4, 0, 0, localPoolName, null);
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", plannerId("room_a"),
                9, 9, 7, List.of(existingConnector), tags("room_a"));
        MKStructureWorkspace existing = draft.withPieces(List.of(existingPiece));
        List<MKPlannedPiece> targetPieces = List.of(planned("room_a_template", "room_a",
                existingPiece.plannerId(), 9, 9, 7, List.of(plannedConnector), tags("room_a")));

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(0, summary.rebuildRequiredCount());
    }

    @Test
    void catalogSummaryRebuildsOnlyFloorRoomWithEditedExits() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        ResourceLocation mainPool = ResourceLocation.fromNamespaceAndPath(draft.namespace(),
                draft.structureName() + "/floor_plan/keep/rooms/main/main_opening");
        ResourceLocation branchPool = ResourceLocation.fromNamespaceAndPath(draft.namespace(),
                draft.structureName() + "/floor_plan/keep/rooms/branch/branch_opening");
        ResourceLocation emptyPool = ResourceLocation.parse("minecraft:empty");
        MKWorkspaceConnectorDefinition mainExit = connector(MKConnectorRole.MAIN_BACK, Direction.NORTH,
                5, 4, 0, 0, mainPool, emptyPool);
        MKWorkspaceConnectorDefinition branchExit = connector(MKConnectorRole.BRANCH, Direction.EAST,
                3, 4, 0, 0, branchPool, emptyPool);
        MKWorkspaceConnectorDefinition addedBranchExit = connector(MKConnectorRole.BRANCH, Direction.WEST,
                3, 4, 0, 0, branchPool, emptyPool);
        MKWorkspacePieceDefinition editedRoom = piece("main_room_template", "main_room",
                plannerId("main_room"), 9, 9, 7, List.of(mainExit, branchExit), floorRoomTags("main_room"));
        MKWorkspacePieceDefinition unchangedRoom = piece("branch_room_template", "branch_room",
                plannerId("branch_room"), 9, 9, 7, List.of(mainExit, branchExit), floorRoomTags("branch_room"));
        MKStructureWorkspace existing = draft.withPieces(List.of(editedRoom, unchangedRoom));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("main_room_template", "main_room", editedRoom.plannerId(), 9, 9, 7,
                        List.of(
                                plannedConnector(mainExit, "floor_plan/keep/rooms/main/main_opening"),
                                plannedConnector(branchExit, "floor_plan/keep/rooms/branch/branch_opening"),
                                plannedConnector(addedBranchExit, "floor_plan/keep/rooms/branch/branch_opening")
                        ), floorRoomTags("main_room")),
                planned("branch_room_template", "branch_room", unchangedRoom.plannerId(), 9, 9, 7,
                        List.of(
                                plannedConnector(branchExit, "floor_plan/keep/rooms/branch/branch_opening"),
                                plannedConnector(mainExit, "floor_plan/keep/rooms/main/main_opening")
                        ), floorRoomTags("branch_room"))
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(1, summary.rebuildRequiredCount());
        assertEquals(0, summary.newCount());
        assertEquals(0, summary.removedCount());
        assertEquals(1, countImpacts(summary, "rebuild"));
        assertEquals(1, countPreservedWorkImpacts(summary));
    }

    private static long countImpacts(MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary, String outcome) {
        return summary.impacts().stream()
                .filter(impact -> outcome.equals(impact.outcome()))
                .count();
    }

    private static long countPreservedWorkImpacts(MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary) {
        return summary.impacts().stream()
                .filter(impact -> "preserved".equals(impact.outcome()) ||
                        "moved".equals(impact.outcome()) ||
                        "expanded".equals(impact.outcome()))
                .count();
    }

    private static MKPlannedPiece planned(String pieceName, String baseName, MKWorkspacePlannerId plannerId,
                                          int width, int length, int height) {
        return planned(pieceName, baseName, plannerId, width, length, height, List.of(), tags(baseName));
    }

    private static MKPlannedPiece planned(String pieceName, String baseName, MKWorkspacePlannerId plannerId,
                                          int width, int length, int height, List<MKPlannedConnector> connectors,
                                          Map<String, String> tags) {
        return new MKPlannedPiece(
                "floor.plan.room",
                pieceName,
                width,
                length,
                height,
                connectors,
                tags,
                plannerId
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName,
                                                   int width, int length, int height) {
        return piece(pieceName, baseName, plannerId(baseName), width, length, height, List.of(), tags(baseName));
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName, MKWorkspacePlannerId plannerId,
                                                   int width, int length, int height,
                                                   List<MKWorkspaceConnectorDefinition> connectors,
                                                   Map<String, String> tags) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan.room",
                plannerId,
                0,
                new MKWorkspaceDimensions(width, length, height, height, height, 3, 3, 3),
                1,
                connectors,
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, width - 1, height - 1, length - 1),
                new BoundingBox(0, 0, 0, width + 3, height - 1, length + 3),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspaceConnectorDefinition connector(MKConnectorRole role, Direction facing,
                                                            int width, int height,
                                                            int lateralOffset, int verticalOffset,
                                                            ResourceLocation targetPool,
                                                            ResourceLocation incomingPool) {
        return new MKWorkspaceConnectorDefinition(
                role,
                facing,
                BlockPos.ZERO,
                width,
                height,
                lateralOffset,
                verticalOffset,
                ResourceLocation.fromNamespaceAndPath("mknpc_test", role.getSerializedName()),
                targetPool,
                targetPool,
                incomingPool
        );
    }

    private static MKPlannedConnector plannedConnector(MKWorkspaceConnectorDefinition connector, String targetPool) {
        String incomingPool = connector.incomingPool().equals(ResourceLocation.parse("minecraft:empty")) ?
                null : connector.incomingPool().toString();
        return new MKPlannedConnector(
                connector.role(),
                connector.facing(),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                targetPool,
                incomingPool
        );
    }

    private static MKWorkspacePlannerId plannerId(String baseName) {
        return MKWorkspacePlannerId.of("floor").child("plan").child("room").child(baseName);
    }

    private static Map<String, String> tags(String baseName) {
        return Map.of(
                MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0",
                "workspace_piece_kind", "template"
        );
    }

    private static Map<String, String> floorRoomTags(boolean includeProfileId) {
        return floorRoomTags("main_room", includeProfileId);
    }

    private static Map<String, String> floorRoomTags(String roomKind) {
        return floorRoomTags(roomKind, true);
    }

    private static Map<String, String> floorRoomTags(String roomKind, boolean includeProfileId) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(tags(roomKind));
        tags.put("workspace_floor_topology_stack_id", "tower.primary");
        tags.put("workspace_floor_topology_floor_role", "main_floor");
        tags.put("workspace_floor_room_kind", roomKind);
        if (includeProfileId) {
            tags.put("workspace_floor_room_profile_id", roomKind);
        }
        return Map.copyOf(tags);
    }
}
