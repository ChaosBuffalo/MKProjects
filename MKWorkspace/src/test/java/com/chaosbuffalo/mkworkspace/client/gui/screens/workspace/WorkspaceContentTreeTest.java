package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceContentTreeTest {
    @Test
    void separatesSlotScaffoldFromFamiliesAndKeepsDeclaredEmptySlots() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        List<MKWorkspacePieceDefinition> pieces = List.of(
                piece(workspace, "platform_scaffold", "platform_contents", "legacy_platform",
                        MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD, 0, true),
                piece(workspace, "gazebo", "platform_contents", "gazebo_family",
                        MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0, true),
                piece(workspace, "gazebo_occupied", "platform_contents", "gazebo_family",
                        MKWorkspaceTemplatePurpose.FAMILY_VARIANT, 1, true),
                piece(workspace, "fountain", "platform_contents", "fountain_family",
                        MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0, true));

        WorkspaceContentTree tree = WorkspaceContentTree.build(pieces,
                Map.of("platform_contents", "Platform Contents", "unused_slot", "Unused Slot"));

        WorkspaceContentTree.SlotNode platform = tree.slots().stream()
                .filter(slot -> slot.slotId().equals("platform_contents")).findFirst().orElseThrow();
        assertEquals(1, platform.scaffolds().size());
        assertEquals(3, platform.families().size());
        assertTrue(tree.slots().stream().anyMatch(slot ->
                slot.slotId().equals("unused_slot") && slot.families().isEmpty()));

        WorkspaceContentTree.FamilyNode scaffoldFamily = tree.family("platform_contents", "legacy_platform");
        assertNotNull(scaffoldFamily);
        assertEquals(List.of("platform_scaffold"), scaffoldFamily.scaffolds().stream()
                .map(MKWorkspacePieceDefinition::pieceName).toList());
        assertEquals("empty scaffold", scaffoldFamily.status());

        WorkspaceContentTree.FamilyNode gazebo = tree.family("platform_contents", "gazebo_family");
        assertNotNull(gazebo);
        assertEquals("1 variant", gazebo.status());
        assertEquals("gazebo", gazebo.canonical().pieceName());
        assertEquals(List.of("gazebo_occupied"), gazebo.variants().stream()
                .map(MKWorkspacePieceDefinition::pieceName).toList());
        assertEquals("canonical fallback", tree.family("platform_contents", "fountain_family").status());
    }

    @Test
    void reportsDisabledFamilyAtTheFamilyLevel() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition canonical = piece(workspace, "disabled_room", "room_slot", "disabled_family",
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0, false);

        WorkspaceContentTree.FamilyNode family = WorkspaceContentTree.build(List.of(canonical), Map.of())
                .family("room_slot", "disabled_family");

        assertNotNull(family);
        assertEquals("disabled", family.status());
    }

    @Test
    void groupsScaffoldAndVariantIntoTheSameManageableFamily() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition scaffold = piece(workspace, "pillars_scaffold", "pillars", "pillars",
                MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD, 0, true);
        MKWorkspacePieceDefinition variant = piece(workspace, "fire_shrine_pillar", "pillars", "pillars",
                MKWorkspaceTemplatePurpose.FAMILY_VARIANT, 1, true);

        WorkspaceContentTree.FamilyNode family = WorkspaceContentTree.build(List.of(scaffold, variant), Map.of())
                .family("pillars", "pillars");

        assertNotNull(family);
        assertEquals(List.of(scaffold, variant), family.pieces());
        assertEquals(List.of(scaffold), family.scaffolds());
        assertEquals(List.of(variant), family.variants());
        assertEquals("1 variant", family.status());
    }

    @Test
    void legacyInsertPiecesAppearOnlyUnderTheirUserDeclaredSlot() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        Map<String, String> tags = new java.util.LinkedHashMap<>();
        tags.put("workspace_base_name", "fire_shrine_platform_contents");
        tags.put("workspace_piece_kind", "template");
        tags.put("workspace_insert_family_id", "fire_shrine_platform_contents");
        tags.put("workspace_topology_slot_id", "workspace.insert_family.insert_socket");
        tags.put(MKWorkspaceContentSelectionTags.TOPOLOGY_SLOT_ID, "workspace.insert_family.insert_socket");
        BlockPos origin = new BlockPos(0, 64, 0);
        BoundingBox bounds = new BoundingBox(0, 64, 0, 4, 67, 4);
        MKWorkspacePieceDefinition scaffold = new MKWorkspacePieceDefinition(UUID.randomUUID(), workspace.id(),
                "fire_shrine_platform_contents", "workspace.insert_family.insert_socket", 0,
                new MKWorkspaceDimensions(5, 4, 5, 4, 5, 4, 3, 3), List.of(), origin, bounds, bounds,
                origin.above(), origin.above(2), List.of(), List.of(), tags);

        WorkspaceContentTree tree = WorkspaceContentTree.build(List.of(scaffold),
                Map.of("fire_shrine_platform_contents", "Fire Shrine Platform Contents"));

        assertEquals(List.of("fire_shrine_platform_contents"), tree.slots().stream()
                .map(WorkspaceContentTree.SlotNode::slotId).toList());
        assertEquals(1, tree.slots().getFirst().scaffolds().size());
        assertEquals(List.of("fire_shrine_platform_contents"), tree.slots().getFirst().families().stream()
                .map(WorkspaceContentTree.FamilyNode::familyId).toList());
    }

    @Test
    void declaredTreeDoesNotExposeInactiveSlotsFromStalePieces() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition active = piece(workspace, "north_west_corner",
                "hub_spoke.corner.north_west", "north_west_family",
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0, true);
        MKWorkspacePieceDefinition inactive = piece(workspace, "shared_corner",
                "hub_spoke.corner.shared", "shared_family",
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0, true);

        WorkspaceContentTree tree = WorkspaceContentTree.buildDeclared(List.of(active, inactive),
                Map.of("hub_spoke.corner.north_west", "Hub Spoke Corner North West"));

        assertEquals(List.of("hub_spoke.corner.north_west"), tree.slots().stream()
                .map(WorkspaceContentTree.SlotNode::slotId).toList());
    }

    private MKWorkspacePieceDefinition piece(MKStructureWorkspace workspace, String name, String slot,
                                               String family, MKWorkspaceTemplatePurpose purpose, int variantIndex,
                                               boolean familyEnabled) {
        Map<String, String> tags = MKWorkspaceContentSelectionTags.applyTemplate(
                MKWorkspaceContentSelectionTags.applyFamily(Map.of("workspace_base_name", name), slot, family, 1,
                        familyEnabled), purpose, purpose.variant() ? name : "", 1, true);
        BlockPos origin = new BlockPos(variantIndex * 10, 64, 0);
        BoundingBox bounds = new BoundingBox(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + 4,
                origin.getY() + 3, origin.getZ() + 4);
        return new MKWorkspacePieceDefinition(UUID.randomUUID(), workspace.id(), name, slot, variantIndex,
                new MKWorkspaceDimensions(5, 4, 5, 4, 5, 4, 3, 3), List.of(), origin, bounds, bounds,
                origin.above(), origin.above(2), List.of(), List.of(), tags);
    }
}
