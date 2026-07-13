package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalledKeepDraftEditorTest {
    @Test
    void wallGeometryControlsKeepGatehouseMatchedToWallSegment() {
        RecordingDraftSession session = new RecordingDraftSession();
        WalledKeepDraftEditor editor = new WalledKeepDraftEditor(session);

        editor.wallHeight(10);
        editor.wallTopVoidMargin(3);
        editor.wallUnitSpan(15);
        editor.wallPassageWidth(5);

        MKWorkspaceLinearRunFamilyDefinition wall = session.draft().linearRunFamilies.stream()
                .filter(linearRun -> linearRun.topologySlotId().equals("keep.perimeter"))
                .findFirst()
                .orElseThrow();
        MKWorkspaceRoomFamilyDefinition gatehouse = session.draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals("keep.gate.main"))
                .findFirst()
                .orElseThrow();

        assertEquals(10, wall.interiorHeight());
        assertEquals(3, wall.topVoidMargin());
        assertEquals(15, gatehouse.roomWidth());
        assertEquals(5, gatehouse.roomLength());
        assertEquals(10, gatehouse.roomHeight());
        assertEquals(3, gatehouse.topVoidMargin());
    }

    private static final class RecordingDraftSession extends WorkspaceDraftSession {
        private final Draft draft = new Draft();

        private RecordingDraftSession() {
            super(null, -1, -1, -1, -1, -1);
            MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
            draft.topologyProfile = MKWalledKeepWorkspacePlanner.defaultTopologyProfile(false);
            draft.palette = MKWorkspaceMaterialPalette.defaultPalette();
            draft.familyDefinitions = MKWalledKeepWorkspacePlanner.defaultRoomFamilyDefinitions(dimensions);
            draft.linearRunFamilies = MKWalledKeepWorkspacePlanner.defaultLinearRunFamilyDefinitions(
                    dimensions, draft.palette);
        }

        @Override
        public Draft draft() {
            return draft;
        }

        @Override
        public MKWorkspaceRoomFamilyDefinition normalizeFamilyDefinition(MKWorkspaceRoomFamilyDefinition family) {
            return family;
        }
    }
}
