package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

final class TowerWorkspaceDraftAdapter implements WorkspacePlannerDraftAdapter {
    @Override
    public ResourceLocation plannerId() {
        return MKTowerWorkspacePlanner.PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
        return MKTowerWorkspacePlanner.defaultTopologyProfile();
    }

    @Override
    public String primaryDimensionStackId() {
        return WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID;
    }

    @Override
    public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        session.replaceVerticalStackSettingsWithNormalizedFloorCounts(
                session.verticalStackSettings(WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID).withHeight(requestedHeight));
        session.snapDraftVerticalAccess();
    }

    @Override
    public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        session.draft().familyDefinitions = MKWorkspaceRoomFamilyDefinition.createDefaults(dimensions);
        session.draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions,
                session.draft().palette);
        session.draft().topologyProfile = MKTowerWorkspacePlanner.defaultTopologyProfile()
                .withVerticalStackSettings(primaryVerticalStackSettingsFromDraft(session));
    }

    @Override
    public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        boolean hasTowerFamilies = session.draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("tower."));
        if (!hasTowerFamilies) {
            session.draft().familyDefinitions = MKWorkspaceRoomFamilyDefinition.createDefaults(dimensions);
        }
        session.verticalStackSettings(WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID);
        boolean hasTowerLinearRuns = session.draft().linearRunFamilies.stream()
                .anyMatch(linearRun -> linearRun.topologySlotId().startsWith("tower."));
        if (!hasTowerLinearRuns) {
            session.draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions,
                    session.draft().palette);
        }
    }

    @Override
    public boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        return true;
    }

    @Override
    public Optional<String> verticalStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        return MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(topologySlotId)
                .filter(stackId -> WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID.equals(stackId) || "tower".equals(stackId))
                .map(stackId -> WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID);
    }

    @Override
    public MKWorkspaceVerticalStackSettings defaultVerticalStackSettings(WorkspaceDraftSession session,
                                                                         String stackId) {
        if (!WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID.equals(stackId)) {
            return WorkspacePlannerDraftAdapter.super.defaultVerticalStackSettings(session, stackId);
        }
        return primaryVerticalStackSettingsFromDraft(session);
    }

    @Override
    public void syncDraftVerticalAccessFromStack(WorkspaceDraftSession session,
                                                 MKWorkspaceVerticalStackSettings settings) {
        if (!WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID.equals(settings.stackId())) {
            return;
        }
        session.draft().shaftSize = settings.shaftSize();
        session.draft().verticalAccessPlacement = settings.verticalAccessPlacement();
        session.draft().stairMode = settings.stairConfig().mode();
        session.draft().stairRiseType = settings.stairConfig().riseType();
        session.draft().stairWidth = settings.stairConfig().stairWidth();
    }

    private MKWorkspaceVerticalStackSettings primaryVerticalStackSettingsFromDraft(WorkspaceDraftSession session) {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        return MKWorkspaceVerticalStackSettings.defaults(WorkspaceVerticalStackDraftEditor.PRIMARY_STACK_ID, dimensions.roomHeight())
                .withShaftSize(session.draft().shaftSize)
                .withVerticalAccessPlacement(session.draft().verticalAccessPlacement)
                .withStairConfig(new MKWorkspaceStairAuthoringConfig(
                        session.draft().stairMode,
                        session.draft().stairRiseType,
                        session.draft().stairWidth
                ));
    }
}
