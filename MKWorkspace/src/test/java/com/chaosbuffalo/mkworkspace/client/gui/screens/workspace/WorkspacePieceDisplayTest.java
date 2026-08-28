package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkspacePieceDisplayTest {
    @Test
    void concreteAuthoringBaseGroupsTemplateAndVariantsWhenRoleIsShared() {
        MKWorkspacePieceDefinition template = piece("hub_spoke_corner_north_west", 0, Map.of(
                "workspace_topology_slot_id", "hub_spoke.corner.north_west"
        ));
        MKWorkspacePieceDefinition variant = piece("hub_spoke_corner_north_west_1", 1, Map.of(
                "workspace_base_name", "hub_spoke_corner_north_west",
                "workspace_topology_slot_id", "hub_spoke.corner.north_west"
        ));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(template, variant));

        Map<String, List<MKWorkspacePieceDefinition>> grouped =
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(workspace);

        assertEquals(1, grouped.size());
        assertEquals(List.of(template, variant), grouped.values().iterator().next());
        assertEquals("Hub Spoke Corner North West", WorkspacePieceDisplay.buildWorkspaceGroupLabel(template));
    }

    @Test
    void insertFamilyAuthoringPiecesGroupByInsertFamilyId() {
        MKWorkspacePieceDefinition template = piece("fire_shrine_platform_contents", 0, Map.of(
                "workspace_base_name", "fire_shrine_platform_contents",
                MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, "fire_shrine_platform_contents",
                MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND,
                MKWorkspaceInsertFamilyKind.INSERT_SOCKET.getSerializedName()
        ));
        MKWorkspacePieceDefinition variant = piece("fire_shrine_gazebo", 1, Map.of(
                "workspace_base_name", "fire_shrine_platform_contents",
                MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, "fire_shrine_platform_contents",
                MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND,
                MKWorkspaceInsertFamilyKind.INSERT_SOCKET.getSerializedName()
        ));
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(template, variant));

        Map<String, List<MKWorkspacePieceDefinition>> grouped =
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(workspace);

        assertEquals(1, grouped.size());
        assertEquals("insert_family:fire_shrine_platform_contents", grouped.keySet().iterator().next());
        assertEquals(List.of(template, variant), grouped.values().iterator().next());
        assertEquals("Insert Family / fire_shrine_platform_contents",
                WorkspacePieceDisplay.buildWorkspaceGroupLabel(template));
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, int variantIndex, Map<String, String> tags) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "hub_spoke.corner.shared",
                variantIndex,
                MKWorkspaceDimensions.defaultDimensions(),
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 0, 0, 0),
                new BoundingBox(0, 0, 0, 0, 0, 0),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }
}
