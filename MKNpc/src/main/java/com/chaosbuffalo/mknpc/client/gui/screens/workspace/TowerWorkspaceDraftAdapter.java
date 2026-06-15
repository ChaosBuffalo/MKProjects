package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

final class TowerWorkspaceDraftAdapter implements WorkspacePlannerDraftAdapter {
    @Override
    public ResourceLocation plannerId() {
        return MKWorkspaceTopologyProfile.TOWER_PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
        return MKWorkspaceTopologyProfile.tower();
    }

    @Override
    public String primaryDimensionStackId() {
        return TowerStackDraftEditor.PRIMARY_STACK_ID;
    }

    @Override
    public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        session.replaceTowerStackSettingsWithNormalizedFloorCounts(
                session.towerStackSettings(TowerStackDraftEditor.PRIMARY_STACK_ID).withHeight(requestedHeight));
        session.applyTowerStackSettingsToFamilies();
        session.snapDraftVerticalAccess();
    }

    @Override
    public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        session.draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions);
        session.draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions,
                session.draft().palette);
        session.draft().topologyProfile = MKWorkspaceTopologyProfile.tower()
                .withTowerStackSettings(session.towerPrimarySettingsFromDraft());
    }

    @Override
    public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        boolean hasTowerFamilies = session.draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("tower."));
        if (!hasTowerFamilies) {
            session.draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions);
        }
        session.towerStackSettings(TowerStackDraftEditor.PRIMARY_STACK_ID);
        session.applyTowerStackSettingsToFamilies();
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
    public Optional<String> towerStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        return MKTowerWorkspaceStackSlot.stackIdForTopologySlot(topologySlotId)
                .filter(stackId -> TowerStackDraftEditor.PRIMARY_STACK_ID.equals(stackId) || "tower".equals(stackId))
                .map(stackId -> TowerStackDraftEditor.PRIMARY_STACK_ID);
    }
}
