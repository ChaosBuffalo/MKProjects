package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceTemplateBindingDiffServiceTest {
    private final MKWorkspaceTemplateBindingDiffService service = new MKWorkspaceTemplateBindingDiffService();

    @Test
    void floorTopologyBindingsPreserveByBaseNameAndOrphanRemovedBases() {
        MKWorkspacePlannerId preservedId = MKWorkspacePlannerId.of("keep.main.floor_plan.room.main_00");
        MKWorkspacePlannerId orphanedId = MKWorkspacePlannerId.of("keep.main.floor_plan.room.old_00");
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(
                        piece("main_00_template", "main_00", preservedId),
                        piece("old_00_template", "old_00", orphanedId)
                ));

        MKWorkspaceTemplateBindingDiffService.TemplateBindingDiff diff = service.floorTopologyBindings(
                workspace,
                List.of(planned("main_00", preservedId)),
                "tower.primary",
                "main_01",
                "tower.primary",
                "main_01");

        assertEquals(List.of(preservedId), diff.preserved());
        assertEquals(List.of(orphanedId), diff.orphaned());
    }

    @Test
    void suggestFloorTopologyRemapsRequiresCompatibleShapeAndConnectors() {
        MKWorkspacePlannerId orphanedId = MKWorkspacePlannerId.of("keep.main.floor_plan.room.old_00");
        MKWorkspacePlannerId compatibleTargetId = MKWorkspacePlannerId.of("keep.main.floor_plan.room.new_00");
        MKWorkspacePlannerId incompatibleTargetId = MKWorkspacePlannerId.of("keep.main.floor_plan.room.other_00");
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(piece("old_00_template", "old_00", orphanedId,
                        List.of(connector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH, 3, 3)))));

        List<MKWorkspaceTemplateRemapSuggestion> suggestions = service.suggestFloorTopologyRemaps(
                workspace,
                List.of(
                        planned("new_00", compatibleTargetId,
                                List.of(plannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH, 3, 3))),
                        planned("other_00", incompatibleTargetId,
                                List.of(plannedConnector(MKConnectorRole.BRANCH, Direction.NORTH, 3, 3)))
                ),
                List.of(orphanedId),
                "tower.primary",
                "main_01",
                "tower.primary",
                "main_01");

        assertEquals(List.of(new MKWorkspaceTemplateRemapSuggestion(
                orphanedId,
                compatibleTargetId,
                100,
                "same floor piece kind, dimensions, and connector signature"
        )), suggestions);
    }

    private static MKPlannedPiece planned(String baseName, MKWorkspacePlannerId plannerId) {
        return planned(baseName, plannerId, List.of());
    }

    private static MKPlannedPiece planned(String baseName, MKWorkspacePlannerId plannerId,
                                          List<MKPlannedConnector> connectors) {
        return new MKPlannedPiece(
                "floor.plan.room",
                baseName,
                5,
                5,
                5,
                connectors,
                floorTagsWithKind(),
                plannerId
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName, MKWorkspacePlannerId plannerId) {
        return piece(pieceName, baseName, plannerId, List.of());
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName, MKWorkspacePlannerId plannerId,
                                                   List<MKWorkspaceConnectorDefinition> connectors) {
        java.util.LinkedHashMap<String, String> tags = new java.util.LinkedHashMap<>(floorTags());
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, baseName);
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
        tags.put("workspace_piece_kind", "template");
        tags.put("tower_piece_kind", "floor_plan_room");
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan.room",
                plannerId,
                0,
                new MKWorkspaceDimensions(5, 5, 5, 5, 5, 3, 3, 3),
                1,
                connectors,
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

    private static MKWorkspaceConnectorDefinition connector(MKConnectorRole role, Direction facing, int width,
                                                           int height) {
        ResourceLocation empty = ResourceLocation.parse("minecraft:empty");
        return new MKWorkspaceConnectorDefinition(role, facing, BlockPos.ZERO, width, height, 0, 0, empty, empty,
                empty, empty);
    }

    private static MKPlannedConnector plannedConnector(MKConnectorRole role, Direction facing, int width, int height) {
        return new MKPlannedConnector(role, facing, width, height);
    }

    private static Map<String, String> floorTags() {
        return Map.of(
                "workspace_floor_topology_stack_id", "tower.primary",
                "workspace_floor_topology_floor_role", "main_01"
        );
    }

    private static Map<String, String> floorTagsWithKind() {
        return Map.of(
                "workspace_floor_topology_stack_id", "tower.primary",
                "workspace_floor_topology_floor_role", "main_01",
                "tower_piece_kind", "floor_plan_room"
        );
    }
}
