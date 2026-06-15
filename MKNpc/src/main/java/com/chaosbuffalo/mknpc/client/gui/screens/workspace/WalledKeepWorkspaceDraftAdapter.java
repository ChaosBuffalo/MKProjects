package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class WalledKeepWorkspaceDraftAdapter implements WorkspacePlannerDraftAdapter {
    @Override
    public ResourceLocation plannerId() {
        return MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
        return session.walledKeepEditor().topologyProfileWithCornerModes(
                session.draft().topologyProfile.uniqueNorthWestCornerTower(),
                session.draft().topologyProfile.uniqueNorthEastCornerTower(),
                session.draft().topologyProfile.uniqueSouthEastCornerTower(),
                session.draft().topologyProfile.uniqueSouthWestCornerTower()
        );
    }

    @Override
    public String primaryDimensionStackId() {
        return "keep.center";
    }

    @Override
    public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        session.replaceTowerStackSettingsWithNormalizedFloorCounts(
                session.towerStackSettings("keep.center").withHeight(requestedHeight));
    }

    @Override
    public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        session.draft().topologyProfile = MKWorkspaceTopologyProfile.walledKeep(
                session.draft().topologyProfile.uniqueNorthWestCornerTower(),
                session.draft().topologyProfile.uniqueNorthEastCornerTower(),
                session.draft().topologyProfile.uniqueSouthEastCornerTower(),
                session.draft().topologyProfile.uniqueSouthWestCornerTower());
        session.draft().familyDefinitions = MKWorkspaceRoomFamilyDefinition.createWalledKeepDefaults(dimensions);
        session.draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions,
                session.draft().palette);
    }

    @Override
    public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        migratePerimeterLinearRuns(session);
        boolean hasKeepFamilies = session.draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("keep."));
        if (!hasKeepFamilies) {
            session.draft().familyDefinitions = MKWorkspaceRoomFamilyDefinition.createWalledKeepDefaults(dimensions);
        }
        ensureFamiliesForActiveCornerSlots(session);
        boolean hasKeepLinearRuns = session.draft().linearRunFamilies.stream()
                .anyMatch(linearRun -> linearRun.topologySlotId().startsWith("keep."));
        if (!hasKeepLinearRuns) {
            session.draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions,
                    session.draft().palette);
        }
    }

    @Override
    public boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isEmpty()) {
            return true;
        }
        if ("keep.corner.shared".equals(cornerStackId.get())) {
            return session.draft().topologyProfile.anySharedCornerTower();
        }
        return session.draft().topologyProfile.uniqueCornerTower(cornerStackId.get());
    }

    @Override
    public Optional<String> towerStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        if (topologySlotId.startsWith("keep.center.")) {
            return Optional.of("keep.center");
        }
        return cornerStackIdForSlot(topologySlotId);
    }

    @Override
    public Optional<MKWorkspaceRoomFamilyDefinition> sharedFamilySource(WorkspaceDraftSession session,
                                                                         String topologySlotId) {
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isEmpty() || "keep.corner.shared".equals(cornerStackId.get())) {
            return Optional.empty();
        }
        String suffix = topologySlotId.substring(cornerStackId.get().length());
        String sharedSlotId = "keep.corner.shared" + suffix;
        return session.draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals(sharedSlotId))
                .findFirst();
    }

    void ensureFamiliesForActiveCornerSlots(WorkspaceDraftSession session) {
        java.util.ArrayList<MKWorkspaceRoomFamilyDefinition> updated =
                new java.util.ArrayList<>(session.draft().familyDefinitions);
        if (session.draft().topologyProfile.anySharedCornerTower()) {
            session.ensureFamiliesForTowerStack(updated, "keep.corner.shared");
        }
        for (String cornerSlot : WalledKeepDraftEditor.KEEP_CORNER_STACK_IDS) {
            if (!session.draft().topologyProfile.uniqueCornerTower(cornerSlot)) {
                continue;
            }
            session.ensureFamiliesForTowerStack(updated, cornerSlot);
        }
        session.draft().familyDefinitions = List.copyOf(updated);
    }

    private void migratePerimeterLinearRuns(WorkspaceDraftSession session) {
        java.util.LinkedHashMap<String, MKWorkspaceLinearRunFamilyDefinition> convertedByPerimeterSlot =
                new java.util.LinkedHashMap<>();
        java.util.ArrayList<MKWorkspaceLinearRunFamilyDefinition> current = new java.util.ArrayList<>();
        java.util.HashSet<String> existingPerimeterSlots = new java.util.HashSet<>();
        for (MKWorkspaceLinearRunFamilyDefinition linearRun : session.draft().linearRunFamilies) {
            String perimeterSlot = legacyPerimeterSlot(linearRun.topologySlotId());
            if (perimeterSlot == null) {
                current.add(linearRun);
                if (isPerimeterTopologySlot(linearRun.topologySlotId())) {
                    existingPerimeterSlots.add(linearRun.topologySlotId());
                }
                continue;
            }
            MKWorkspaceLinearRunFamilyDefinition converted = copyPerimeterLinearRun(linearRun, perimeterSlot);
            convertedByPerimeterSlot.merge(perimeterSlot, converted, this::preferPerimeterLinearRun);
        }
        for (Map.Entry<String, MKWorkspaceLinearRunFamilyDefinition> entry : convertedByPerimeterSlot.entrySet()) {
            if (!existingPerimeterSlots.contains(entry.getKey())) {
                current.add(entry.getValue());
                existingPerimeterSlots.add(entry.getKey());
            }
        }
        session.draft().linearRunFamilies = List.copyOf(current);
    }

    private MKWorkspaceLinearRunFamilyDefinition preferPerimeterLinearRun(MKWorkspaceLinearRunFamilyDefinition existing,
                                                                          MKWorkspaceLinearRunFamilyDefinition candidate) {
        if (existing.kind() == MKWorkspaceLinearRunKind.DEFENSIVE_WALL) {
            return existing;
        }
        if (candidate.kind() == MKWorkspaceLinearRunKind.DEFENSIVE_WALL) {
            return candidate;
        }
        return existing;
    }

    private String legacyPerimeterSlot(String topologySlotId) {
        if (isPerimeterTopologySlot(topologySlotId)) {
            return "keep.perimeter";
        }
        if (topologySlotId.startsWith("keep.wall.")) {
            return "keep.perimeter";
        }
        if (topologySlotId.startsWith("keep.parapet.")) {
            return "keep.perimeter";
        }
        return null;
    }

    private boolean isPerimeterTopologySlot(String topologySlotId) {
        return topologySlotId.equals("keep.perimeter") || topologySlotId.startsWith("keep.perimeter.");
    }

    private MKWorkspaceLinearRunFamilyDefinition copyPerimeterLinearRun(MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                                        String topologySlotId) {
        return new MKWorkspaceLinearRunFamilyDefinition(
                "keep_wall_segment",
                topologySlotId,
                linearRun.kind(),
                linearRun.openingProfileId(),
                linearRun.length(),
                linearRun.interiorWidth(),
                linearRun.interiorHeight(),
                linearRun.slopeDelta(),
                linearRun.allowOnMainPath(),
                linearRun.allowOnBranchPath(),
                linearRun.projection(),
                linearRun.supportedShapes(),
                Math.min(linearRun.topVoidMargin(), Math.max(0, linearRun.interiorHeight() - 1)),
                linearRun.foundationPolicy(),
                linearRun.paletteOverride()
        );
    }

    private Optional<String> cornerStackIdForSlot(String topologySlotId) {
        if ("keep.corner.shared".equals(topologySlotId) || topologySlotId.startsWith("keep.corner.shared.")) {
            return Optional.of("keep.corner.shared");
        }
        return WalledKeepDraftEditor.KEEP_CORNER_STACK_IDS.stream()
                .filter(stackId -> topologySlotId.equals(stackId) || topologySlotId.startsWith(stackId + "."))
                .findFirst();
    }
}
