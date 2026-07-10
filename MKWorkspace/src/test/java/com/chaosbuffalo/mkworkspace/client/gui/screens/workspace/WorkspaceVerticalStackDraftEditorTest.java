package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class WorkspaceVerticalStackDraftEditorTest {
    @Test
    void resetDefaultsUsesPlannerAdapterStackDefaults() {
        MKWorkspaceVerticalStackSettings plannerDefaults =
                MKWorkspaceVerticalStackSettings.defaults("test.stack", 7)
                        .withWidth(17)
                        .withTopCapApproachEnabled(false);
        RecordingDraftSession session = new RecordingDraftSession(new RecordingAdapter(plannerDefaults));

        new WorkspaceVerticalStackDraftEditor(session, "test.stack").resetDefaults();

        assertSame(plannerDefaults, session.replacedSettings);
    }

    @Test
    void walledKeepStackDefaultsIgnoreCorruptedDraftStackSettings() {
        MKWorkspaceVerticalStackSettings corruptedSharedCorner =
                MKWorkspaceVerticalStackSettings.defaults("keep.corner.shared", 7);
        MKWorkspaceTopologyProfile corruptedProfile = MKWalledKeepWorkspacePlanner.defaultTopologyProfile(false)
                .withVerticalStackSettings(corruptedSharedCorner);
        RecordingDraftSession session = new RecordingDraftSession(new RecordingAdapter(corruptedSharedCorner));
        session.draft.topologyProfile = corruptedProfile;

        MKWorkspaceVerticalStackSettings resolved = new WalledKeepWorkspaceDraftAdapter()
                .defaultVerticalStackSettings(session, "keep.corner.shared");

        assertEquals("keep.corner.shared", resolved.stackId());
        assertEquals(0, resolved.mainFloors());
        assertEquals(0, resolved.basementFloors());
        assertFalse(resolved.topCapApproachEnabled());
        assertFalse(resolved.basementEntryEnabled());
        assertFalse(resolved.basementCapApproachEnabled());
    }

    private static final class RecordingDraftSession extends WorkspaceDraftSession {
        private final WorkspacePlannerDraftAdapter adapter;
        private final Draft draft = new Draft();
        private MKWorkspaceVerticalStackSettings replacedSettings;

        private RecordingDraftSession(WorkspacePlannerDraftAdapter adapter) {
            super(null, -1, -1, -1, -1, -1);
            this.adapter = adapter;
            draft.topologyProfile = MKWalledKeepWorkspacePlanner.defaultTopologyProfile(false);
        }

        @Override
        public Draft draft() {
            return draft;
        }

        @Override
        WorkspacePlannerDraftAdapter plannerAdapter() {
            return adapter;
        }

        @Override
        void replaceVerticalStackSettings(MKWorkspaceVerticalStackSettings settings) {
            replacedSettings = settings;
        }
    }

    private record RecordingAdapter(MKWorkspaceVerticalStackSettings defaults) implements WorkspacePlannerDraftAdapter {
        @Override
        public ResourceLocation plannerId() {
            return ResourceLocation.fromNamespaceAndPath("mknpc", "test_planner");
        }

        @Override
        public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
            return new MKWorkspaceTopologyProfile(plannerId(), List.of(), TerrainAdjustment.BEARD_THIN);
        }

        @Override
        public String primaryDimensionStackId() {
            return defaults.stackId();
        }

        @Override
        public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        }

        @Override
        public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        }

        @Override
        public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        }

        @Override
        public boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
            return true;
        }

        @Override
        public Optional<String> verticalStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
            return Optional.empty();
        }

        @Override
        public MKWorkspaceVerticalStackSettings defaultVerticalStackSettings(WorkspaceDraftSession session,
                                                                             String stackId) {
            return defaults;
        }

        @Override
        public List<String> templateBaseNamesForFamily(WorkspaceDraftSession session,
                                                       MKWorkspaceRoomFamilyDefinition family) {
            return List.of(family.baseName());
        }

        @Override
        public boolean showLinearRunInTemplateFamilies(WorkspaceDraftSession session,
                                                       MKWorkspaceLinearRunFamilyDefinition linearRun) {
            return true;
        }
    }
}
