package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
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

    Optional<String> verticalStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId);

    default MKWorkspaceVerticalStackSettings defaultVerticalStackSettings(WorkspaceDraftSession session,
                                                                          String stackId) {
        return session.draft().topologyProfile.verticalStackSettingsOrDefault(stackId);
    }

    default void syncDraftVerticalAccessFromStack(WorkspaceDraftSession session,
                                                  MKWorkspaceVerticalStackSettings settings) {
    }

    default Optional<MKWorkspaceRoomFamilyDefinition> sharedFamilySource(WorkspaceDraftSession session,
                                                                          String topologySlotId) {
        return Optional.empty();
    }
}
