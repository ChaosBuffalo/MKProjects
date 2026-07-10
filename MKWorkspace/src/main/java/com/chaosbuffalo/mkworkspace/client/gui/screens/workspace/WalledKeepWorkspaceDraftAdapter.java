package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepPlannerSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceVerticalStackSizingReport;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class WalledKeepWorkspaceDraftAdapter implements WorkspacePlannerDraftAdapter {
    private static final String WALKWAY_WEST_ROOT_SLOT = "keep.walkway.west";
    private static final String WALKWAY_EAST_ROOT_SLOT = "keep.walkway.east";
    private static final String COURTYARD_CONTENT_BASE_NAME = "keep_courtyard_content";
    private static final String COURTYARD_PATH_LINEAR_RUN_ID = "keep_courtyard_path";

    @Override
    public ResourceLocation plannerId() {
        return MKWalledKeepWorkspacePlanner.PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologyProfile profileForSwitch(WorkspaceDraftSession session) {
        MKWalledKeepPlannerSettings settings = MKWalledKeepPlannerSettings.from(session.draft().topologyProfile);
        return keepEditor(session).topologyProfileWithCornerModes(
                settings.uniqueNorthWestCornerTower(),
                settings.uniqueNorthEastCornerTower(),
                settings.uniqueSouthEastCornerTower(),
                settings.uniqueSouthWestCornerTower()
        );
    }

    @Override
    public String primaryDimensionStackId() {
        return "keep.center";
    }

    @Override
    public void applyDefaultHeight(WorkspaceDraftSession session, int requestedHeight) {
        session.replaceVerticalStackSettingsWithNormalizedFloorCounts(
                session.verticalStackSettings("keep.center").withHeight(requestedHeight));
    }

    @Override
    public MKWorkspaceVerticalStackSettings defaultVerticalStackSettings(WorkspaceDraftSession session, String stackId) {
        MKWalledKeepPlannerSettings settings = MKWalledKeepPlannerSettings.from(session.draft().topologyProfile);
        return MKWalledKeepWorkspacePlanner.defaultVerticalStackSettings(
                        settings.uniqueNorthWestCornerTower(),
                        settings.uniqueNorthEastCornerTower(),
                        settings.uniqueSouthEastCornerTower(),
                        settings.uniqueSouthWestCornerTower())
                .stream()
                .filter(stackSettings -> stackSettings.stackId().equals(stackId))
                .findFirst()
                .orElseGet(() -> WorkspacePlannerDraftAdapter.super.defaultVerticalStackSettings(session, stackId));
    }

    @Override
    public void resetDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        MKWalledKeepPlannerSettings settings = MKWalledKeepPlannerSettings.from(session.draft().topologyProfile);
        session.draft().topologyProfile = MKWalledKeepWorkspacePlanner.defaultTopologyProfile(
                settings.uniqueNorthWestCornerTower(),
                settings.uniqueNorthEastCornerTower(),
                settings.uniqueSouthEastCornerTower(),
                settings.uniqueSouthWestCornerTower());
        session.draft().familyDefinitions = MKWalledKeepWorkspacePlanner.defaultRoomFamilyDefinitions(dimensions);
        session.draft().linearRunFamilies = MKWalledKeepWorkspacePlanner.defaultLinearRunFamilyDefinitions(dimensions,
                session.draft().palette);
    }

    @Override
    public void seedDefaults(WorkspaceDraftSession session, MKWorkspaceDimensions dimensions) {
        migratePerimeterLinearRuns(session);
        boolean hasKeepFamilies = session.draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("keep."));
        if (!hasKeepFamilies) {
            session.draft().familyDefinitions = MKWalledKeepWorkspacePlanner.defaultRoomFamilyDefinitions(dimensions);
        }
        keepEditor(session).ensureFamiliesForActiveCornerSlots();
        boolean hasKeepLinearRuns = session.draft().linearRunFamilies.stream()
                .anyMatch(linearRun -> linearRun.topologySlotId().startsWith("keep."));
        if (!hasKeepLinearRuns) {
            session.draft().linearRunFamilies = MKWalledKeepWorkspacePlanner.defaultLinearRunFamilyDefinitions(dimensions,
                    session.draft().palette);
        }
    }

    @Override
    public boolean isActiveTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isEmpty()) {
            return true;
        }
        MKWalledKeepPlannerSettings settings = MKWalledKeepPlannerSettings.from(session.draft().topologyProfile);
        if ("keep.corner.shared".equals(cornerStackId.get())) {
            return settings.anySharedCornerTower();
        }
        return settings.uniqueCornerTower(cornerStackId.get());
    }

    @Override
    public Optional<String> verticalStackIdForTopologySlot(WorkspaceDraftSession session, String topologySlotId) {
        if (topologySlotId.startsWith("keep.center.")) {
            return Optional.of("keep.center");
        }
        return cornerStackIdForSlot(topologySlotId);
    }

    @Override
    public boolean showTopologySlotInTemplateFamilies(WorkspaceDraftSession session, MKWorkspaceSlotSchema slot,
                                                      String regionKind, String roleKind) {
        return WorkspacePlannerDraftAdapter.super.showTopologySlotInTemplateFamilies(session, slot, regionKind, roleKind) &&
                !"path".equals(slot.slotKind()) &&
                !"content_socket".equals(slot.slotKind());
    }

    @Override
    public List<String> templateBaseNamesForFamily(WorkspaceDraftSession session,
                                                   MKWorkspaceRoomFamilyDefinition family) {
        if (family.baseName().startsWith("keep_corner_shared")) {
            return List.of(family.baseName(),
                    family.baseName().replace("keep_corner_shared", "keep_corner_north_west"));
        }
        return WorkspacePlannerDraftAdapter.super.templateBaseNamesForFamily(session, family);
    }

    @Override
    public boolean showLinearRunInTemplateFamilies(WorkspaceDraftSession session,
                                                   MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return !WALKWAY_WEST_ROOT_SLOT.equals(linearRun.topologySlotId()) &&
                !WALKWAY_EAST_ROOT_SLOT.equals(linearRun.topologySlotId());
    }

    @Override
    public List<WorkspaceTemplateFamilyDisplay> extraTemplateFamilies(WorkspaceDraftSession session) {
        java.util.ArrayList<WorkspaceTemplateFamilyDisplay> displays = new java.util.ArrayList<>();
        displays.add(WorkspaceTemplateFamilyDisplay.forBaseNames(
                "Courtyard Content",
                "Content  |  keep.courtyard.content  |  derives 7 courtyard sockets",
                List.of(COURTYARD_CONTENT_BASE_NAME)));
        boolean hasCourtyardPathLinearRun = session.linearRunFamilies().stream()
                .anyMatch(linearRun -> COURTYARD_PATH_LINEAR_RUN_ID.equals(linearRun.linearRunId()));
        if (!hasCourtyardPathLinearRun) {
            displays.add(WorkspaceTemplateFamilyDisplay.forLinearRun(
                    "Courtyard Path",
                    "Open Walkway  |  keep.courtyard.path  |  derives 7 path sockets",
                    COURTYARD_PATH_LINEAR_RUN_ID));
        }
        return List.copyOf(displays);
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

    @Override
    public List<MKWorkspaceVerticalStackSizingReport.HorizontalExitInfo> previewFallbackEntryExits(
            WorkspaceDraftSession session, String stackId) {
        if (!"keep.center".equals(stackId)) {
            return List.of();
        }
        return List.of(new MKWorkspaceVerticalStackSizingReport.HorizontalExitInfo("south", "ingress", 0, 0));
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

    private WalledKeepDraftEditor keepEditor(WorkspaceDraftSession session) {
        return new WalledKeepDraftEditor(session);
    }
}
