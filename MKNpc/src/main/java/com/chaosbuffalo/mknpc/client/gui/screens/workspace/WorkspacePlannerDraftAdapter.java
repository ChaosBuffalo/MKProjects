package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

interface WorkspacePlannerDraftAdapter {
    ResourceLocation plannerId();

    MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session);

    String primaryDimensionStackId();

    void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight);

    void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions);

    void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions);

    boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId);

    Optional<String> towerStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId);

    default Optional<MKTowerWorkspaceFamilyDefinition> sharedFamilySource(WorkspaceDraftSession session,
                                                                          String topologySlotId) {
        return Optional.empty();
    }
}
