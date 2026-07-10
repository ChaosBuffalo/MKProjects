package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceVerticalStackSizingReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
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

    default Optional<String> topologyGroupIdForFloorRole(WorkspaceDraftSession session, String floorRole) {
        return WorkspaceVerticalStackSlotDraftSupport.topologyGroupIdForFloorRole(floorRole);
    }

    default Optional<String> topologyGroupIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        return WorkspaceVerticalStackSlotDraftSupport.topologyGroupIdForTopologySlot(topologySlotId);
    }

    default MKWorkspaceVerticalStackSettings defaultVerticalStackSettings(WorkspaceDraftSession session,
                                                                          String stackId) {
        return session.draft().topologyProfile.verticalStackSettingsOrDefault(stackId);
    }

    default void syncDraftVerticalAccessFromStack(WorkspaceDraftSession session,
                                                  MKWorkspaceVerticalStackSettings settings) {
    }

    default List<Integer> allowedVerticalStackMainFloorCounts(WorkspaceDraftSession session,
                                                              MKWorkspaceVerticalStackSettings settings,
                                                              int basementFloors) {
        return WorkspaceVerticalStackSlotDraftSupport.allowedMainFloorCounts(settings, basementFloors);
    }

    default List<Integer> allowedVerticalStackBasementFloorCounts(WorkspaceDraftSession session,
                                                                  MKWorkspaceVerticalStackSettings settings,
                                                                  int mainFloors) {
        return WorkspaceVerticalStackSlotDraftSupport.allowedBasementFloorCounts(settings, mainFloors);
    }

    default boolean verticalAccessFamilyAllowsTopVoidMargin(WorkspaceDraftSession session,
                                                            MKWorkspaceRoomFamilyDefinition family) {
        return WorkspaceVerticalStackSlotDraftSupport.isTopCapSlot(family.topologySlotId());
    }

    default boolean verticalAccessFamilyAllowsBottomVoidMargin(WorkspaceDraftSession session,
                                                               MKWorkspaceRoomFamilyDefinition family) {
        return WorkspaceVerticalStackSlotDraftSupport.isBasementCapSlot(family.topologySlotId());
    }

    default Optional<MKWorkspaceTopologySlotMetadata> topologySlotMetadata(WorkspaceDraftSession session,
                                                                           MKWorkspaceSlotSchema slot) {
        return WorkspaceVerticalStackSlotDraftSupport.topologySlotMetadata(slot);
    }

    default boolean showTopologySlotInTemplateFamilies(WorkspaceDraftSession session, MKWorkspaceSlotSchema slot,
                                                       String regionKind, String roleKind) {
        if ("linear_run".equals(regionKind) || "linear_run".equals(roleKind)) {
            return false;
        }
        Optional<MKWorkspaceVerticalStackSlot> verticalSlot =
                MKWorkspaceVerticalStackSlot.fromTopologySlotId(slot.slotId());
        if (verticalSlot.isEmpty()) {
            return true;
        }
        return verticalStackIdForTopologySlot(session, slot.slotId())
                .map(stackId -> WorkspaceVerticalStackSlotDraftSupport.isEnabledInStackSettings(
                        verticalSlot.get(),
                        defaultVerticalStackSettings(session, stackId)))
                .orElse(true);
    }

    default boolean showLinearRunInTemplateFamilies(WorkspaceDraftSession session,
                                                    MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return true;
    }

    default List<String> templateBaseNamesForFamily(WorkspaceDraftSession session,
                                                    MKWorkspaceRoomFamilyDefinition family) {
        return List.of(family.baseName());
    }

    default List<WorkspaceTemplateFamilyDisplay> extraTemplateFamilies(WorkspaceDraftSession session) {
        return List.of();
    }

    default Optional<MKWorkspaceRoomFamilyDefinition> sharedFamilySource(WorkspaceDraftSession session,
                                                                          String topologySlotId) {
        return Optional.empty();
    }

    default List<MKWorkspaceVerticalStackSizingReport.HorizontalExitInfo> previewFallbackEntryExits(
            WorkspaceDraftSession session, String stackId) {
        return List.of();
    }
}
