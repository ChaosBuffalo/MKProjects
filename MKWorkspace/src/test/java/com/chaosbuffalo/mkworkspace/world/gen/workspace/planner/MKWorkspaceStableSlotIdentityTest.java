package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceStableSlotIdentityTest {
    @Test
    void towerCanonicalPhysicalPiecesHaveStableIdentities() {
        assertPhysicalCatalogHasStableIdentities(new MKTowerWorkspacePlanner(),
                workspaceFor(new MKTowerWorkspacePlanner()));
    }

    @Test
    void walledKeepCanonicalPhysicalPiecesHaveStableIdentities() {
        assertPhysicalCatalogHasStableIdentities(new MKWalledKeepWorkspacePlanner(),
                workspaceFor(new MKWalledKeepWorkspacePlanner(), MKWalledKeepWorkspacePlanner.defaultTopologyProfile(false)));
    }

    @Test
    void floorRoomStableIdentitySurvivesProfileInsertion() {
        MKTowerWorkspacePlanner planner = new MKTowerWorkspacePlanner();
        MKStructureWorkspace workspace = floorTopologyWorkspace(planner);
        MKPlannedPiece baseline = planner.createCanonicalPieces(workspace).stream()
                .filter(piece -> "floor_plan_room".equals(piece.tags().get("tower_piece_kind")))
                .filter(piece -> MKFloorRoomKind.MAIN_ROOM.getSerializedName()
                        .equals(piece.tags().get("workspace_floor_room_kind")))
                .findFirst()
                .orElseThrow();
        String profileId = baseline.tags().get("workspace_floor_room_profile_id");
        String beforeKey = MKWorkspaceStableSlotIdentity.key(baseline.tags());

        String stackId = baseline.tags().get("workspace_floor_topology_stack_id");
        String floorRole = baseline.tags().get("workspace_floor_topology_floor_role");
        MKFloorTopologySettings settings = workspace.topologyProfile()
                .floorTopologySettingsOrDefault(stackId, floorRole);
        ArrayList<MKFloorRoomProfile> mainProfiles = new ArrayList<>(settings.mainRoomProfiles());
        MKFloorRoomProfile inserted = mainProfiles.getFirst()
                .withIdentity("new_main_room", "New Main Room");
        mainProfiles.add(0, inserted);
        MKFloorTopologySettings updatedSettings = settings.withRoomProfiles(
                List.copyOf(mainProfiles),
                settings.branchRoomProfiles(),
                settings.branchCapProfiles(),
                settings.mainCapApproachProfiles(),
                settings.mainCapProfiles());
        MKStructureWorkspace updatedWorkspace = withTopology(workspace,
                workspace.topologyProfile().withFloorTopologySettings(updatedSettings));

        String afterKey = planner.createCanonicalPieces(updatedWorkspace).stream()
                .filter(piece -> profileId.equals(piece.tags().get("workspace_floor_room_profile_id")))
                .findFirst()
                .map(piece -> MKWorkspaceStableSlotIdentity.key(piece.tags()))
                .orElseThrow();

        assertEquals(beforeKey, afterKey);
    }

    @Test
    void registryValidationReportsMissingStableIdentity() {
        MKWorkspacePlannerRegistry registry = new MKWorkspacePlannerRegistry();
        MKWorkspacePlanner planner = new MissingIdentityPlanner();
        registry.register(planner);

        List<String> errors = registry.validate(workspaceFor(planner));

        assertTrue(errors.stream().anyMatch(error -> error.contains("missing stable template identity")),
                errors.toString());
    }

    @Test
    void registryValidationReportsDuplicateStableIdentity() {
        MKWorkspacePlannerRegistry registry = new MKWorkspacePlannerRegistry();
        MKWorkspacePlanner planner = new DuplicateIdentityPlanner();
        registry.register(planner);

        List<String> errors = registry.validate(workspaceFor(planner));

        assertTrue(errors.stream().anyMatch(error -> error.contains("is used by both")),
                errors.toString());
    }

    private static void assertPhysicalCatalogHasStableIdentities(MKWorkspacePlanner planner,
                                                                 MKStructureWorkspace workspace) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (MKPlannedPiece piece : planner.createCanonicalPieces(workspace)) {
            if (MKWorkspaceTemplateReuseTags.isDerived(piece.tags())) {
                continue;
            }
            assertTrue(MKWorkspaceStableSlotIdentity.hasStableIdentity(piece.tags()),
                    "missing stable identity for " + piece.pieceName());
            String key = MKWorkspaceStableSlotIdentity.key(piece.tags());
            assertTrue(keys.add(key), "duplicate stable identity " + key);
        }
        List<String> stableErrors = MKWorkspacePlannerRegistry.shared().validate(workspace).stream()
                .filter(error -> error.contains("stable template identity"))
                .toList();
        assertTrue(stableErrors.isEmpty(), stableErrors.toString());
    }

    private static MKStructureWorkspace workspaceFor(MKWorkspacePlanner planner) {
        return workspaceFor(planner, planner.createDefaultTopologyProfile());
    }

    private static MKStructureWorkspace workspaceFor(MKWorkspacePlanner planner,
                                                     MKWorkspaceTopologyProfile topologyProfile) {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceDimensions dimensions = draft.dimensions();
        MKWorkspaceMaterialPalette palette = draft.palette();
        return new MKStructureWorkspace(
                draft.id(),
                draft.anchor(),
                draft.namespace(),
                draft.structureName(),
                topologyProfile,
                dimensions,
                palette,
                draft.stairConfig(),
                draft.verticalAccessPlacement(),
                draft.shellMargin(),
                draft.exteriorAirMargin(),
                draft.previewMargin(),
                draft.verticalAccessSpec(),
                planner.createDefaultRoomFamilyDefinitions(dimensions),
                draft.openingProfiles(),
                planner.createDefaultLinearRunFamilyDefinitions(dimensions, palette),
                draft.insertFamilies(),
                draft.createdAt(),
                draft.updatedAt(),
                draft.pieces(),
                draft.layerStates());
    }

    private static MKStructureWorkspace floorTopologyWorkspace(MKTowerWorkspacePlanner planner) {
        MKStructureWorkspace draft = workspaceFor(planner);
        ArrayList<MKHorizontalOpeningProfile> openings = new ArrayList<>(draft.openingProfiles());
        openings.add(new MKHorizontalOpeningProfile("floor_main", 5, 4, true, false));
        openings.add(new MKHorizontalOpeningProfile("floor_branch", 3, 3, false, true));
        ArrayList<MKWorkspaceRoomFamilyDefinition> families = new ArrayList<>();
        for (MKWorkspaceRoomFamilyDefinition family : draft.familyDefinitions()) {
            if (family.baseName().equals("floor_main")) {
                families.add(withFloorTopologyExits(family));
            } else {
                families.add(family);
            }
        }
        return new MKStructureWorkspace(
                draft.id(),
                draft.anchor(),
                draft.namespace(),
                draft.structureName(),
                draft.topologyProfile(),
                draft.dimensions(),
                draft.palette(),
                draft.stairConfig(),
                draft.verticalAccessPlacement(),
                draft.shellMargin(),
                draft.exteriorAirMargin(),
                draft.previewMargin(),
                draft.verticalAccessSpec(),
                List.copyOf(families),
                List.copyOf(openings),
                draft.linearRunFamilies(),
                draft.insertFamilies(),
                draft.createdAt(),
                draft.updatedAt(),
                draft.pieces(),
                draft.layerStates());
    }

    private static MKWorkspaceRoomFamilyDefinition withFloorTopologyExits(MKWorkspaceRoomFamilyDefinition family) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                List.of(
                        new MKFamilyHorizontalExitDefinition(
                                Direction.NORTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                "floor_main",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN),
                        new MKFamilyHorizontalExitDefinition(
                                Direction.EAST,
                                MKHorizontalExitPathKind.BRANCH,
                                "floor_branch",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN)
                ),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.sourceTopologySlotId(),
                family.settingsTopologySlotId(),
                family.foundationPolicyOverride(),
                family.paletteOverride());
    }

    private static MKStructureWorkspace withTopology(MKStructureWorkspace workspace,
                                                     MKWorkspaceTopologyProfile topologyProfile) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.insertFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces(),
                workspace.layerStates());
    }

    private abstract static class TestPlanner implements MKWorkspacePlanner {
        private final ResourceLocation plannerId;

        private TestPlanner(String path) {
            this.plannerId = ResourceLocation.fromNamespaceAndPath("mknpc_test", path);
        }

        @Override
        public ResourceLocation plannerId() {
            return plannerId;
        }

        @Override
        public MKWorkspaceTopologySchema schema() {
            return new MKWorkspaceTopologySchema(plannerId, List.of(), List.of(), List.of(), List.of());
        }

        @Override
        public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
            return new MKWorkspaceTopologyProfile(plannerId, List.of(), TerrainAdjustment.BEARD_THIN);
        }

        @Override
        public List<MKWorkspaceRoomFamilyDefinition> createDefaultRoomFamilyDefinitions(
                MKWorkspaceDimensions dimensions) {
            return List.of();
        }

        @Override
        public List<MKWorkspaceLinearRunFamilyDefinition> createDefaultLinearRunFamilyDefinitions(
                MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
            return List.of();
        }
    }

    private static class MissingIdentityPlanner extends TestPlanner {
        private MissingIdentityPlanner() {
            super("missing_identity");
        }

        @Override
        public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
            return List.of(new MKPlannedPiece("test.room", "test_room", 3, 3, 3,
                    List.of(), Map.of()));
        }
    }

    private static class DuplicateIdentityPlanner extends TestPlanner {
        private DuplicateIdentityPlanner() {
            super("duplicate_identity");
        }

        @Override
        public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
            Map<String, String> firstTags = new java.util.LinkedHashMap<>();
            MKWorkspaceStableSlotIdentity.apply(firstTags, "test_room", "test.room");
            Map<String, String> secondTags = new java.util.LinkedHashMap<>();
            MKWorkspaceStableSlotIdentity.apply(secondTags, "test_room", "test.room");
            return List.of(
                    new MKPlannedPiece("test.room", "test_room_a", 3, 3, 3, List.of(), firstTags),
                    new MKPlannedPiece("test.room", "test_room_b", 3, 3, 3, List.of(), secondTags)
            );
        }
    }
}
