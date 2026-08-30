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
        assertEquals(2, platform.families().size());
        assertTrue(tree.slots().stream().anyMatch(slot ->
                slot.slotId().equals("unused_slot") && slot.families().isEmpty()));

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
