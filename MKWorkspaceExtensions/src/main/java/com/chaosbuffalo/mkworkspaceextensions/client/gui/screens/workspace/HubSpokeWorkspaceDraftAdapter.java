package com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceDraftSession;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerDraftAdapter;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HubSpokeWorkspaceDraftAdapter implements WorkspacePlannerDraftAdapter {
    @Override
    public ResourceLocation plannerId() {
        return HubSpokePlanner.PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
        return HubSpokePlanner.defaultTopologyProfile();
    }

    @Override
    public String primaryDimensionStackId() {
        return HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID;
    }

    @Override
    public boolean usesPrimaryDimensionStack() {
        return false;
    }

    @Override
    public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        session.draft().familyDefinitions = session.draft().familyDefinitions.stream()
                .map(family -> copyWithHeight(family, requestedHeight))
                .toList();
    }

    @Override
    public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        session.draft().topologyProfile = HubSpokePlanner.defaultTopologyProfile();
        session.draft().familyDefinitions = HubSpokePlanner.defaultRoomFamilyDefinitions(dimensions);
        session.draft().linearRunFamilies = List.of();
        session.draft().insertFamilies = List.of();
    }

    @Override
    public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        boolean hasHubSpokeFamilies = session.draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("hub_spoke."));
        if (!hasHubSpokeFamilies) {
            session.draft().familyDefinitions = HubSpokePlanner.defaultRoomFamilyDefinitions(dimensions);
        }
        session.draft().topologyProfile = HubSpokePlannerSettings.from(session.draft().topologyProfile)
                .applyTo(session.draft().topologyProfile);
        ensureCornerFamiliesForActiveSlots(session);
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .filter(linearRun -> !linearRun.topologySlotId().startsWith("hub_spoke."))
                .toList();
    }

    @Override
    public boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        if (HubSpokePlanner.CORNER_SLOT.equals(topologySlotId)) {
            return settings.anySharedCorner();
        }
        if (HubSpokePlanner.concreteCornerSlots().contains(topologySlotId)) {
            return settings.uniqueCorner(topologySlotId);
        }
        return true;
    }

    @Override
    public Optional<String> verticalStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        return Optional.empty();
    }

    @Override
    public List<String> templateBaseNamesForFamily(WorkspaceDraftSession session,
                                                   MKWorkspaceRoomFamilyDefinition family) {
        if (HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()) &&
                HubSpokePlanner.CORNER_BASE_NAME.equals(family.baseName())) {
            return List.of(family.baseName(), HubSpokePlanner.CORNER_BASE_NAME + "_north_west");
        }
        return WorkspacePlannerDraftAdapter.super.templateBaseNamesForFamily(session, family);
    }

    @Override
    public Optional<MKWorkspaceRoomFamilyDefinition> sharedFamilySource(WorkspaceDraftSession session,
                                                                        String topologySlotId) {
        if (!HubSpokePlanner.concreteCornerSlots().contains(topologySlotId)) {
            return Optional.empty();
        }
        return session.draft().familyDefinitions.stream()
                .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                .findFirst();
    }

    @Override
    public int maxRoomHeightForFamilyNormalization(WorkspaceDraftSession session, Optional<String> verticalStackId) {
        return verticalStackId
                .map(stackId -> session.draft().topologyProfile.verticalStackSettingsOrDefault(stackId).height())
                .orElse(HubSpokePlanner.MAX_PLATFORM_HEIGHT);
    }

    private MKWorkspaceRoomFamilyDefinition copyWithHeight(MKWorkspaceRoomFamilyDefinition family, int height) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                Math.max(HubSpokePlanner.MIN_PLATFORM_HEIGHT,
                        Math.min(HubSpokePlanner.MAX_PLATFORM_HEIGHT, height)),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private void ensureCornerFamiliesForActiveSlots(WorkspaceDraftSession session) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(session.draft().topologyProfile);
        ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new ArrayList<>(session.draft().familyDefinitions);
        if (settings.anySharedCorner()) {
            ensureCornerFamily(updated, HubSpokePlanner.CORNER_SLOT);
        }
        for (String slot : HubSpokePlanner.concreteCornerSlots()) {
            if (settings.uniqueCorner(slot)) {
                ensureCornerFamily(updated, slot);
            }
        }
        session.draft().familyDefinitions = List.copyOf(updated);
    }

    private void ensureCornerFamily(List<MKWorkspaceRoomFamilyDefinition> families, String topologySlotId) {
        if (families.stream().anyMatch(family -> topologySlotId.equals(family.topologySlotId()))) {
            return;
        }
        MKWorkspaceRoomFamilyDefinition shared = families.stream()
                .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                .findFirst()
                .orElseGet(() -> HubSpokePlanner.defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions())
                        .stream()
                        .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                        .findFirst()
                        .orElseThrow());
        families.add(MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                topologySlotId.replace('.', '_'),
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, "corner", "room", true),
                shared.verticalAccessGroupId(),
                shared.supportsVerticalAccess(),
                shared.roomWidth(),
                shared.roomLength(),
                shared.roomHeight(),
                shared.horizontalExtrusionMode(),
                shared.horizontalExits(),
                shared.topVoidMargin(),
                shared.bottomVoidMargin(),
                shared.foundationPolicyOverride(),
                shared.paletteOverride()
        ));
    }
}
