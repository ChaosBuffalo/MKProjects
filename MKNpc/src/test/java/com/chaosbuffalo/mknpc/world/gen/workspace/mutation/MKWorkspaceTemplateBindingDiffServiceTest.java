package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
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

    private static MKPlannedPiece planned(String baseName, MKWorkspacePlannerId plannerId) {
        return new MKPlannedPiece(
                "floor.plan.room",
                baseName,
                5,
                5,
                5,
                List.of(),
                floorTags(),
                plannerId
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName, MKWorkspacePlannerId plannerId) {
        java.util.LinkedHashMap<String, String> tags = new java.util.LinkedHashMap<>(floorTags());
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, baseName);
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
        tags.put("workspace_piece_kind", "template");
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan.room",
                plannerId,
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
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

    private static Map<String, String> floorTags() {
        return Map.of(
                "workspace_floor_topology_stack_id", "tower.primary",
                "workspace_floor_topology_floor_role", "main_01"
        );
    }
}
