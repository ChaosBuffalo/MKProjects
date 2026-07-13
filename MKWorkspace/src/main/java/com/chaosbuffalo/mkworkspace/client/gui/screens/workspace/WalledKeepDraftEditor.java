package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepCourtyardSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepPlannerSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class WalledKeepDraftEditor {
    private static final String VERTICAL_STACK_TAB_SELECTION = "mknpc:walled_keep.vertical_stack_tab";
    static final List<String> KEEP_CORNER_STACK_IDS = List.of(
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west"
    );
    private final WorkspaceDraftSession session;

    WalledKeepDraftEditor(WorkspaceDraftSession session) {
        this.session = session;
    }

    public TerrainAdjustment terrainAdjustment() {
        return session.draft().topologyProfile.terrainAdjustment();
    }

    public void terrainAdjustment(TerrainAdjustment value) {
        session.draft().topologyProfile = session.draft().topologyProfile.withTerrainAdjustment(value);
        session.markDirty();
    }

    public MKWalledKeepCourtyardSettings courtyardSettings() {
        return plannerSettings().courtyardSettings();
    }

    public int courtyardContentTemplateSize() {
        return courtyardSettings().courtyardContentTemplateSize();
    }

    public int courtyardPathInnerMargin() {
        return courtyardSettings().courtyardPathInnerMargin();
    }

    public void courtyardPathInnerMargin(int value) {
        MKWalledKeepCourtyardSettings current = courtyardSettings();
        plannerSettings(plannerSettings().withCourtyardSettings(new MKWalledKeepCourtyardSettings(
                        current.courtyardContentEnabled(),
                        current.courtyardSocketGenerationEnabled(),
                        current.courtyardContentTemplateHeight(),
                        current.courtyardSocketClearance(),
                        current.courtyardWalkwayContinuationLength(),
                        value,
                        current.courtyardContentTemplateSize()
                )));
    }

    public void courtyardContentTemplateSize(int value) {
        MKWalledKeepCourtyardSettings current = courtyardSettings();
        plannerSettings(plannerSettings().withCourtyardSettings(new MKWalledKeepCourtyardSettings(
                        current.courtyardContentEnabled(),
                        current.courtyardSocketGenerationEnabled(),
                        current.courtyardContentTemplateHeight(),
                        current.courtyardSocketClearance(),
                        current.courtyardWalkwayContinuationLength(),
                        current.courtyardPathInnerMargin(),
                        value
                )));
    }

    public List<String> verticalStackTabs() {
        java.util.ArrayList<String> tabs = new java.util.ArrayList<>();
        tabs.add("keep.center");
        MKWalledKeepPlannerSettings settings = plannerSettings();
        if (settings.anySharedCornerTower()) {
            tabs.add("keep.corner.shared");
        }
        for (String cornerSlot : KEEP_CORNER_STACK_IDS) {
            if (settings.uniqueCornerTower(cornerSlot)) {
                tabs.add(cornerSlot);
            }
        }
        return List.copyOf(tabs);
    }

    public String verticalStackTab() {
        normalizeVerticalStackTab();
        return session.viewState.plannerSelection(VERTICAL_STACK_TAB_SELECTION, "keep.center");
    }

    public void verticalStackTab(String stackId) {
        List<String> tabs = verticalStackTabs();
        session.viewState.setPlannerSelection(VERTICAL_STACK_TAB_SELECTION, tabs.contains(stackId) ? stackId :
                (tabs.isEmpty() ? "keep.center" : tabs.getFirst()));
    }

    void normalizeVerticalStackTab() {
        List<String> tabs = verticalStackTabs();
        if (tabs.isEmpty()) {
            session.viewState.setPlannerSelection(VERTICAL_STACK_TAB_SELECTION, "keep.center");
            return;
        }
        if (!tabs.contains(session.viewState.plannerSelection(VERTICAL_STACK_TAB_SELECTION, "keep.center"))) {
            session.viewState.setPlannerSelection(VERTICAL_STACK_TAB_SELECTION, tabs.getFirst());
        }
    }

    public boolean uniqueCornerTower(String topologySlotId) {
        return plannerSettings().uniqueCornerTower(topologySlotId);
    }

    public void uniqueCornerTower(String topologySlotId, boolean value) {
        MKWalledKeepPlannerSettings updatedSettings = plannerSettings().withCornerMode(topologySlotId, value);
        session.draft().topologyProfile = topologyProfileWithCornerModes(
                updatedSettings.uniqueNorthWestCornerTower(),
                updatedSettings.uniqueNorthEastCornerTower(),
                updatedSettings.uniqueSouthEastCornerTower(),
                updatedSettings.uniqueSouthWestCornerTower());
        ensureFamiliesForActiveCornerSlots();
        normalizeVerticalStackTab();
        session.markDirty();
    }

    public boolean rampartAccessEnabled() {
        return plannerSettings().rampartAccessEnabled();
    }

    public void rampartAccessEnabled(boolean value) {
        plannerSettings(plannerSettings().withRampartAccessEnabled(value));
    }

    public int rampartAccessOpeningHeight() {
        return session.getOpeningProfile("branch_opening")
                .or(() -> session.openingProfiles().stream().findFirst())
                .map(MKHorizontalOpeningProfile::openingHeight)
                .orElse(3);
    }

    public int requiredRampartAccessEntryHeight() {
        return rampartAccessBottom() + rampartAccessOpeningHeight();
    }

    public boolean rampartAccessNeedsEntryHeightFix() {
        return rampartAccessEnabled() && activeCornerStackIds().stream()
                .map(session::verticalStackSettings)
                .anyMatch(settings -> settings.entryHeight() < requiredRampartAccessEntryHeight());
    }

    public void fixRampartAccessEntryHeights() {
        int requiredHeight = requiredRampartAccessEntryHeight();
        for (String stackId : activeCornerStackIds()) {
            MKWorkspaceVerticalStackSettings settings = session.verticalStackSettings(stackId);
            if (settings.entryHeight() < requiredHeight) {
                session.replaceVerticalStackSettings(settings.withEntryHeight(requiredHeight));
            }
        }
    }

    public int wallHeight() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorHeight)
                .orElse(7);
    }

    public int rampartAccessBottom() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(this::rampartAccessBottom)
                .orElse(7);
    }

    private int rampartAccessBottom(MKWorkspaceLinearRunFamilyDefinition linearRun) {
        int height = Math.max(0, linearRun.interiorHeight());
        int verticalShellMargin = Math.max(0, session.verticalShellMargin());
        int topVoidMargin = Math.max(0, linearRun.topVoidMargin());
        return Math.max(0, height + verticalShellMargin - topVoidMargin);
    }

    public void wallHeight(int value) {
        int height = Math.max(2, value);
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(), height,
                                Math.min(linearRun.topVoidMargin(), Math.max(0, height - 1))) :
                        linearRun)
                .toList();
        session.markDirty();
    }

    public int wallUnitSpan() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::length)
                .orElse(MKWalledKeepWorkspacePlanner.DEFAULT_WALL_SEGMENT_LENGTH);
    }

    public void wallUnitSpan(int value) {
        int span = makeOdd(Math.max(3, value));
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithLengthAndInteriorWidth(linearRun, span, linearRun.interiorWidth()) :
                        linearRun)
                .toList();
        syncGatehouseWallDimensions(span, wallPassageWidth());
        session.markDirty();
    }

    public int wallPassageWidth() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorWidth)
                .orElse(3);
    }

    public void wallPassageWidth(int value) {
        int passageWidth = makeOdd(Math.max(3, value));
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithLengthAndInteriorWidth(linearRun, linearRun.length(), passageWidth) :
                        linearRun)
                .toList();
        syncGatehouseWallDimensions(wallUnitSpan(), passageWidth);
        session.markDirty();
    }

    public MKWorkspaceLinearRunKind perimeterRunKind() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::kind)
                .orElse(MKWorkspaceLinearRunKind.DEFENSIVE_WALL);
    }

    public void perimeterRunKind(MKWorkspaceLinearRunKind value) {
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithKind(linearRun, value) :
                        linearRun)
                .toList();
        session.markDirty();
    }

    public int wallTopVoidMargin() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::topVoidMargin)
                .orElse(0);
    }

    public void wallTopVoidMargin(int value) {
        int margin = Math.max(0, value);
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(),
                                linearRun.interiorHeight(), Math.min(margin, Math.max(0, linearRun.interiorHeight() - 1))) :
                        linearRun)
                .toList();
        session.markDirty();
    }

    public void resetPerimeterDefaults() {
        Map<String, MKWorkspaceLinearRunFamilyDefinition> defaultsBySlot =
                MKWalledKeepWorkspacePlanner.defaultLinearRunFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions(),
                                session.draft().palette)
                        .stream()
                        .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                        .collect(Collectors.toMap(
                                MKWorkspaceLinearRunFamilyDefinition::topologySlotId,
                                linearRun -> linearRun,
                                (first, second) -> first,
                                java.util.LinkedHashMap::new));
        ArrayList<MKWorkspaceLinearRunFamilyDefinition> updated = new ArrayList<>();
        LinkedHashSet<String> resetSlots = new LinkedHashSet<>();
        for (MKWorkspaceLinearRunFamilyDefinition linearRun : session.draft().linearRunFamilies) {
            MKWorkspaceLinearRunFamilyDefinition defaultRun = defaultsBySlot.get(linearRun.topologySlotId());
            if (defaultRun == null) {
                if (!isPerimeterTopologySlot(linearRun.topologySlotId())) {
                    updated.add(linearRun);
                }
                continue;
            }
            if (resetSlots.add(linearRun.topologySlotId())) {
                updated.add(defaultRun);
            }
        }
        for (MKWorkspaceLinearRunFamilyDefinition defaultRun : defaultsBySlot.values()) {
            if (resetSlots.add(defaultRun.topologySlotId())) {
                updated.add(defaultRun);
            }
        }
        session.draft().linearRunFamilies = List.copyOf(updated);
        syncGatehouseWallDimensions(wallUnitSpan(), wallPassageWidth());
        session.markDirty();
    }

    private boolean isPerimeterTopologySlot(String topologySlotId) {
        return topologySlotId.equals("keep.perimeter") || topologySlotId.startsWith("keep.perimeter.");
    }

    private void syncGatehouseWallDimensions(int wallUnitSpan, int wallPassageWidth) {
        session.draft().familyDefinitions = session.draft().familyDefinitions.stream()
                .map(family -> family.topologySlotId().equals("keep.gate.main") ?
                        session.normalizeFamilyDefinition(copyFamilyWithGeometry(family, wallUnitSpan, wallPassageWidth,
                                family.roomHeight())) :
                        family)
                .toList();
    }

    MKWorkspaceTopologyProfile topologyProfileWithCornerModes(boolean northWest, boolean northEast,
                                                              boolean southEast, boolean southWest) {
        MKWorkspaceTopologyProfile current = session.draft().topologyProfile;
        ArrayList<MKWorkspaceVerticalStackSettings> settings = new ArrayList<>(current.verticalStackSettings());
        copyVerticalStackSettingsIfMissing(settings, "keep.center", "keep.corner.shared");
        if (!northWest || !northEast || !southEast || !southWest) {
            copyVerticalStackSettingsIfMissing(settings, "keep.corner.shared", firstCornerStackId(settings));
        }
        if (northWest) {
            copyVerticalStackSettingsIfMissing(settings, "keep.corner.north_west", "keep.corner.shared");
        }
        if (northEast) {
            copyVerticalStackSettingsIfMissing(settings, "keep.corner.north_east", "keep.corner.shared");
        }
        if (southEast) {
            copyVerticalStackSettingsIfMissing(settings, "keep.corner.south_east", "keep.corner.shared");
        }
        if (southWest) {
            copyVerticalStackSettingsIfMissing(settings, "keep.corner.south_west", "keep.corner.shared");
        }
        MKWorkspaceTopologyProfile updatedProfile = new MKWorkspaceTopologyProfile(
                MKWalledKeepWorkspacePlanner.PLANNER_ID,
                current.plannerScopeSettings(),
                settings,
                current.floorTopologySettings(),
                current.pathSettings(),
                current.plannerSettings(),
                current.terrainAdjustment()
        );
        return plannerSettings().withCornerModes(northWest, northEast, southEast, southWest)
                .applyTo(updatedProfile);
    }

    void ensureFamiliesForActiveCornerSlots() {
        ArrayList<MKWorkspaceRoomFamilyDefinition> updated =
                new ArrayList<>(session.draft().familyDefinitions);
        MKWalledKeepPlannerSettings settings = plannerSettings();
        if (settings.anySharedCornerTower()) {
            session.ensureRoomFamiliesForVerticalStack(updated, "keep.corner.shared");
        }
        for (String cornerSlot : KEEP_CORNER_STACK_IDS) {
            if (!settings.uniqueCornerTower(cornerSlot)) {
                continue;
            }
            session.ensureRoomFamiliesForVerticalStack(updated, cornerSlot);
        }
        session.draft().familyDefinitions = List.copyOf(updated);
    }

    private List<String> activeCornerStackIds() {
        ArrayList<String> stackIds = new ArrayList<>();
        MKWalledKeepPlannerSettings settings = plannerSettings();
        if (settings.anySharedCornerTower()) {
            stackIds.add("keep.corner.shared");
        }
        for (String cornerSlot : KEEP_CORNER_STACK_IDS) {
            if (settings.uniqueCornerTower(cornerSlot)) {
                stackIds.add(cornerSlot);
            }
        }
        return List.copyOf(stackIds);
    }

    private void copyVerticalStackSettingsIfMissing(List<MKWorkspaceVerticalStackSettings> settings, String targetId,
                                                 String sourceId) {
        if (targetId == null || targetId.isBlank() ||
                settings.stream().anyMatch(existing -> existing.stackId().equals(targetId))) {
            return;
        }
        settings.stream()
                .filter(existing -> existing.stackId().equals(sourceId))
                .findFirst()
                .map(source -> source.withStackId(targetId))
                .ifPresent(settings::add);
    }

    private String firstCornerStackId(List<MKWorkspaceVerticalStackSettings> settings) {
        return settings.stream()
                .map(MKWorkspaceVerticalStackSettings::stackId)
                .filter(KEEP_CORNER_STACK_IDS::contains)
                .findFirst()
                .orElse("keep.center");
    }

    private MKWalledKeepPlannerSettings plannerSettings() {
        return MKWalledKeepPlannerSettings.from(session.draft().topologyProfile);
    }

    private void plannerSettings(MKWalledKeepPlannerSettings updatedSettings) {
        session.draft().topologyProfile = updatedSettings.applyTo(session.draft().topologyProfile);
        session.markDirty();
    }

    private MKWorkspaceRoomFamilyDefinition copyFamilyWithGeometry(MKWorkspaceRoomFamilyDefinition family,
                                                                    int roomWidth, int roomLength, int roomHeight) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                roomWidth,
                roomLength,
                roomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunWithLengthAndInteriorWidth(
            MKWorkspaceLinearRunFamilyDefinition linearRun, int length, int interiorWidth) {
        return new MKWorkspaceLinearRunFamilyDefinition(
                linearRun.linearRunId(),
                linearRun.topologySlotId(),
                linearRun.kind(),
                linearRun.openingProfileId(),
                Math.max(1, length),
                makeOdd(Math.max(1, interiorWidth)),
                linearRun.interiorHeight(),
                linearRun.slopeDelta(),
                linearRun.allowOnMainPath(),
                linearRun.allowOnBranchPath(),
                linearRun.projection(),
                linearRun.supportedShapes(),
                linearRun.topVoidMargin(),
                linearRun.foundationPolicy(),
                linearRun.paletteOverride()
        );
    }

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunWithKind(MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                                       MKWorkspaceLinearRunKind kind) {
        return new MKWorkspaceLinearRunFamilyDefinition(
                linearRun.linearRunId(),
                linearRun.topologySlotId(),
                kind,
                linearRun.openingProfileId(),
                linearRun.length(),
                linearRun.interiorWidth(),
                linearRun.interiorHeight(),
                linearRun.slopeDelta(),
                linearRun.allowOnMainPath(),
                linearRun.allowOnBranchPath(),
                linearRun.projection(),
                linearRun.supportedShapes(),
                linearRun.topVoidMargin(),
                linearRun.foundationPolicy(),
                linearRun.paletteOverride()
        );
    }

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunWithTopologyHeightAndTopVoid(
            MKWorkspaceLinearRunFamilyDefinition linearRun, String topologySlotId, int interiorHeight,
            int topVoidMargin) {
        return new MKWorkspaceLinearRunFamilyDefinition(
                linearRun.linearRunId(),
                topologySlotId,
                linearRun.kind(),
                linearRun.openingProfileId(),
                linearRun.length(),
                linearRun.interiorWidth(),
                interiorHeight,
                linearRun.slopeDelta(),
                linearRun.allowOnMainPath(),
                linearRun.allowOnBranchPath(),
                linearRun.projection(),
                linearRun.supportedShapes(),
                topVoidMargin,
                linearRun.foundationPolicy(),
                linearRun.paletteOverride()
        );
    }

    private int makeOdd(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }
}
