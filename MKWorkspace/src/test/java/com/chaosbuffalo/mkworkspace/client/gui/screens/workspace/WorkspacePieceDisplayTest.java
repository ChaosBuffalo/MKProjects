package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void manageableGroupsKeepSlotScaffoldWithItsLegacyFamily() {
        Map<String, String> scaffoldTags = MKWorkspaceContentSelectionTags.applyTemplate(
                MKWorkspaceContentSelectionTags.applyFamily(Map.of("workspace_base_name", "platform_contents"),
                        "platform_contents", "legacy_platform", 1, true),
                MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD, "", 1, true);
        Map<String, String> canonicalTags = MKWorkspaceContentSelectionTags.applyTemplate(
                MKWorkspaceContentSelectionTags.applyFamily(Map.of("workspace_base_name", "platform_contents"),
                        "platform_contents", "legacy_platform", 1, true),
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, "", 1, true);
        MKWorkspacePieceDefinition scaffold = piece("platform_contents", 0, scaffoldTags);
        MKWorkspacePieceDefinition canonical = piece("platform_contents_filled", 0, canonicalTags);
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(scaffold, canonical));

        Map<String, List<MKWorkspacePieceDefinition>> authored =
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(workspace);
        Map<String, List<MKWorkspacePieceDefinition>> manageable =
                WorkspacePieceDisplay.groupManageableTemplatePiecesByTopology(workspace);

        assertEquals(List.of(canonical), authored.values().iterator().next());
        assertEquals(1, manageable.size());
        assertTrue(manageable.containsKey("slot_family:platform_contents:legacy_platform"));
        assertEquals(List.of(scaffold, canonical), manageable.values().iterator().next());
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
