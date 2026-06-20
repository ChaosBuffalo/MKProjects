package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKStructureWorkspaceInsertFamilyValidationTest {
    @Test
    void floorTopologyReportsMissingInsertFamily() {
        MKStructureWorkspace workspace = workspaceWithFloorInsertFamily("missing_family", List.of());

        assertTrue(workspace.validate().stream()
                .anyMatch(error -> error.contains("references missing insert family missing_family")));
    }

    @Test
    void floorTopologyAcceptsKnownInsertFamily() {
        MKStructureWorkspace workspace = workspaceWithFloorInsertFamily("hallway_lamps",
                List.of(MKWorkspaceInsertFamilyDefinition.floorLinkHallway("hallway_lamps", 5, 5, 3)));

        assertFalse(workspace.validate().stream()
                .anyMatch(error -> error.contains("references missing insert family hallway_lamps")));
    }

    private MKStructureWorkspace workspaceWithFloorInsertFamily(String familyId,
                                                               List<MKWorkspaceInsertFamilyDefinition> insertFamilies) {
        MKStructureWorkspace base = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceFloorTopologySettings floorSettings = base.topologyProfile().floorTopologySettings().getFirst()
                .withInsertFamily(Optional.of(familyId));
        MKWorkspaceTopologyProfile topologyProfile = base.topologyProfile().withFloorTopologySettings(floorSettings);
        return new MKStructureWorkspace(
                base.id(),
                base.anchor(),
                base.namespace(),
                base.structureName(),
                topologyProfile,
                base.dimensions(),
                base.palette(),
                base.stairConfig(),
                base.verticalAccessPlacement(),
                base.shellMargin(),
                base.exteriorAirMargin(),
                base.previewMargin(),
                base.verticalAccessSpec(),
                base.familyDefinitions(),
                base.openingProfiles(),
                base.linearRunFamilies(),
                insertFamilies,
                base.createdAt(),
                base.updatedAt(),
                base.pieces(),
                base.layerStates()
        );
    }
}
