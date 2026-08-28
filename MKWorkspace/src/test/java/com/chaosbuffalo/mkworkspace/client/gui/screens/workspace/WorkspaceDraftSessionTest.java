package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceDraftSessionTest {
    @Test
    void variantDeletionDoesNotDirtyWorkspaceSettingsDraft() {
        WorkspaceDraftSession session = new WorkspaceDraftSession(null, -1, -1, -1, -1, -1);

        session.stageVariantDeletion(UUID.randomUUID());

        assertTrue(session.dirty());
        assertFalse(session.workspaceSettingsDirty());
    }

    @Test
    void variantAdditionDoesNotDirtyWorkspaceSettingsDraft() {
        WorkspaceDraftSession session = new WorkspaceDraftSession(null, -1, -1, -1, -1, -1);

        session.stageVariantAddition("hub_spoke_corner_north_west_template");

        assertTrue(session.dirty());
        assertFalse(session.workspaceSettingsDirty());
    }

    @Test
    void directWorkspaceSettingEditDirtiesWorkspaceSettingsDraft() {
        WorkspaceDraftSession session = new WorkspaceDraftSession(null, -1, -1, -1, -1, -1);

        session.markDirty();

        assertTrue(session.dirty());
        assertTrue(session.workspaceSettingsDirty());
    }

    @Test
    void addThenRemoveFamilyReturnsDraftToCleanState() {
        WorkspacePlannerClientRegistry.registerBuiltIns();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, workspace, List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspaceSlotSchema slot = session.roomTopologySlots().getFirst();

        int addedIndex = session.addFamilyDefinition(slot);
        assertTrue(session.dirty());
        assertTrue(session.workspaceSettingsDirty());

        session.removeFamilyDefinition(addedIndex);

        assertFalse(session.dirty());
        assertFalse(session.workspaceSettingsDirty());
    }

    @Test
    void previewDraftBuildDoesNotMutateCleanDraftBeforeNoOpFamilyEdit() {
        WorkspacePlannerClientRegistry.registerBuiltIns();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, workspace, List.of());
        WorkspaceDraftSession session = screen.draftSession();
        ArrayList<MKWorkspaceRoomFamilyDefinition> families =
                new ArrayList<>(session.draft().familyDefinitions);
        MKWorkspaceRoomFamilyDefinition firstFamily = families.getFirst();
        families.set(0, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                firstFamily.baseName(),
                firstFamily.slotMetadata(),
                firstFamily.verticalAccessGroupId(),
                firstFamily.supportsVerticalAccess(),
                4,
                firstFamily.roomLength(),
                firstFamily.roomHeight(),
                firstFamily.horizontalExtrusionMode(),
                firstFamily.horizontalExits(),
                firstFamily.topVoidMargin(),
                firstFamily.bottomVoidMargin(),
                firstFamily.foundationPolicyOverride(),
                firstFamily.paletteOverride()
        ));
        session.draft().familyDefinitions = List.copyOf(families);
        session.clearDirty();
        List<MKWorkspaceRoomFamilyDefinition> cleanFamilies = session.draft().familyDefinitions;

        session.buildWorkspaceDraft();

        assertEquals(cleanFamilies, session.draft().familyDefinitions);
        MKWorkspaceSlotSchema slot = session.roomTopologySlots().getFirst();
        int addedIndex = session.addFamilyDefinition(slot);
        session.removeFamilyDefinition(addedIndex);

        assertFalse(session.dirty());
        assertFalse(session.workspaceSettingsDirty());
    }

    @Test
    void addFamilyDefinitionClonesExistingCategoryFamilyShape() {
        WorkspacePlannerClientRegistry.registerBuiltIns();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, workspace, List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspaceSlotSchema slot = session.roomTopologySlots().getFirst();
        ArrayList<MKWorkspaceRoomFamilyDefinition> families =
                new ArrayList<>(session.draft().familyDefinitions);
        int sourceIndex = session.familyIndexesForTopologySlot(slot.slotId()).getFirst();
        MKWorkspaceRoomFamilyDefinition source = families.get(sourceIndex);
        List<MKFamilyHorizontalExitDefinition> exits = source.supportsVerticalAccess() ?
                source.horizontalExits() :
                List.of(new MKFamilyHorizontalExitDefinition(
                        Direction.EAST,
                        MKHorizontalExitPathKind.BRANCH,
                        "branch_opening"
                ));
        MKWorkspaceRoomFamilyDefinition customized = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                source.baseName(),
                source.slotMetadata(),
                source.verticalAccessGroupId(),
                source.supportsVerticalAccess(),
                15,
                17,
                9,
                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                exits,
                1,
                2,
                source.foundationPolicyOverride(),
                source.paletteOverride()
        );
        families.set(sourceIndex, customized);
        session.draft().familyDefinitions = List.copyOf(families);
        session.clearDirty();

        int addedIndex = session.addFamilyDefinition(slot);
        MKWorkspaceRoomFamilyDefinition added = session.familyDefinitions().get(addedIndex);

        assertEquals(slot.slotId(), added.topologySlotId());
        assertFalse(customized.baseName().equals(added.baseName()));
        assertEquals(customized.roomWidth(), added.roomWidth());
        assertEquals(customized.roomLength(), added.roomLength());
        assertEquals(customized.roomHeight(), added.roomHeight());
        assertEquals(customized.horizontalExtrusionMode(), added.horizontalExtrusionMode());
        assertEquals(customized.horizontalExits(), added.horizontalExits());
        assertEquals(customized.topVoidMargin(), added.topVoidMargin());
        assertEquals(customized.bottomVoidMargin(), added.bottomVoidMargin());
    }

    @Test
    void addFamilyDefinitionFromPieceCopiesClickedPieceShapeAndCloneSource() {
        WorkspacePlannerClientRegistry.registerBuiltIns();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, workspace, List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspaceSlotSchema slot = session.roomTopologySlots().getFirst();
        MKWorkspaceRoomFamilyDefinition sourceFamily =
                session.familyDefinitions().get(session.familyIndexesForTopologySlot(slot.slotId()).getFirst());
        MKWorkspacePieceDefinition sourcePiece = authoredPiece(
                workspace.id(),
                "source_room_variant",
                slot.slotId(),
                sourceFamily.baseName(),
                1,
                13,
                15,
                7);

        WorkspaceDraftSession.TemplateFamilyCreationSelection selection =
                session.addFamilyDefinitionFromPiece(sourcePiece).orElseThrow();
        int addedIndex = Integer.parseInt(selection.editId().substring("family:".length()));
        MKWorkspaceRoomFamilyDefinition added = session.familyDefinitions().get(addedIndex);

        assertEquals("slot:" + slot.slotId(), selection.selectedFamilyId());
        assertEquals(slot.slotId(), added.topologySlotId());
        assertEquals(13, added.roomWidth());
        assertEquals(15, added.roomLength());
        assertEquals(7, added.roomHeight());
        assertEquals("source_room_variant", added.templateCloneSourcePieceName());
    }

    @Test
    void addFamilyDefinitionFromInsertPieceCopiesInsertShapeAndCloneSource() {
        WorkspacePlannerClientRegistry.registerBuiltIns();
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, workspace, List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspacePieceDefinition sourcePiece = insertAuthoredPiece(
                workspace.id(),
                "platform_contents_variant",
                "platform_contents",
                1,
                7,
                5,
                3);

        WorkspaceDraftSession.TemplateFamilyCreationSelection selection =
                session.addFamilyDefinitionFromPiece(sourcePiece).orElseThrow();
        int addedIndex = Integer.parseInt(selection.editId().substring("insert:".length()));
        MKWorkspaceInsertFamilyDefinition added = session.insertFamilies().get(addedIndex);

        assertEquals("insert:" + addedIndex, selection.selectedFamilyId());
        assertEquals(MKWorkspaceInsertFamilyKind.INSERT_SOCKET, added.kind());
        assertEquals(7, added.width());
        assertEquals(5, added.height());
        assertEquals(3, added.depth());
        assertEquals("platform_contents_variant", added.templateCloneSourcePieceName());
    }

    private MKWorkspacePieceDefinition authoredPiece(UUID workspaceId, String pieceName, String topologySlotId,
                                                     String familyId, int variantIndex, int width, int length,
                                                     int height) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspaceId,
                pieceName,
                topologySlotId,
                variantIndex,
                new com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions(
                        width, length, height, height, height, 3, 3, 2),
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, width - 1, height - 1, length - 1),
                new BoundingBox(0, 0, 0, width - 1, height - 1, length - 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of(
                        "workspace_piece_kind", "instance",
                        "workspace_topology_slot_id", topologySlotId,
                        "workspace_family_id", familyId,
                        "workspace_base_name", familyId
                )
        );
    }

    private MKWorkspacePieceDefinition insertAuthoredPiece(UUID workspaceId, String pieceName, String familyId,
                                                           int variantIndex, int width, int height, int depth) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspaceId,
                pieceName,
                "workspace.insert_family.insert_socket",
                variantIndex,
                new com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions(
                        width, depth, height, height, height, 3, 3, 2),
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, width - 1, height - 1, depth - 1),
                new BoundingBox(0, 0, 0, width - 1, height - 1, depth - 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of(
                        "workspace_piece_kind", "instance",
                        MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, familyId,
                        MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND,
                        MKWorkspaceInsertFamilyKind.INSERT_SOCKET.getSerializedName(),
                        "workspace_base_name", familyId
                )
        );
    }
}
