package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWalledKeepCourtyardSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class WalledKeepDraftEditor {
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
    }

    public MKWalledKeepCourtyardSettings courtyardSettings() {
        return session.draft().topologyProfile.courtyardSettings();
    }

    public int courtyardContentTemplateSize() {
        return courtyardSettings().courtyardContentTemplateSize();
    }

    public int courtyardPathInnerMargin() {
        return courtyardSettings().courtyardPathInnerMargin();
    }

    public void courtyardPathInnerMargin(int value) {
        MKWalledKeepCourtyardSettings current = courtyardSettings();
        session.draft().topologyProfile = session.draft().topologyProfile.withCourtyardSettings(
                new MKWalledKeepCourtyardSettings(
                        current.courtyardContentEnabled(),
                        current.courtyardSocketGenerationEnabled(),
                        current.courtyardContentTemplateHeight(),
                        current.courtyardSocketClearance(),
                        current.courtyardWalkwayContinuationLength(),
                        value,
                        current.courtyardContentTemplateSize()
                ));
    }

    public void courtyardContentTemplateSize(int value) {
        MKWalledKeepCourtyardSettings current = courtyardSettings();
        session.draft().topologyProfile = session.draft().topologyProfile.withCourtyardSettings(
                new MKWalledKeepCourtyardSettings(
                        current.courtyardContentEnabled(),
                        current.courtyardSocketGenerationEnabled(),
                        current.courtyardContentTemplateHeight(),
                        current.courtyardSocketClearance(),
                        current.courtyardWalkwayContinuationLength(),
                        current.courtyardPathInnerMargin(),
                        value
                ));
    }

    public List<String> towerStackTabs() {
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID.equals(session.topologyPlannerId())) {
            return List.of();
        }
        java.util.ArrayList<String> tabs = new java.util.ArrayList<>();
        tabs.add("keep.center");
        if (session.draft().topologyProfile.anySharedCornerTower()) {
            tabs.add("keep.corner.shared");
        }
        for (String cornerSlot : KEEP_CORNER_STACK_IDS) {
            if (session.draft().topologyProfile.uniqueCornerTower(cornerSlot)) {
                tabs.add(cornerSlot);
            }
        }
        return List.copyOf(tabs);
    }

    public String towerStackTab() {
        normalizeTowerStackTab();
        return session.viewState.walledKeepTowerStackTab;
    }

    public void towerStackTab(String stackId) {
        List<String> tabs = towerStackTabs();
        session.viewState.walledKeepTowerStackTab = tabs.contains(stackId) ? stackId :
                (tabs.isEmpty() ? "keep.center" : tabs.getFirst());
    }

    void normalizeTowerStackTab() {
        List<String> tabs = towerStackTabs();
        if (tabs.isEmpty()) {
            session.viewState.walledKeepTowerStackTab = "keep.center";
            return;
        }
        if (!tabs.contains(session.viewState.walledKeepTowerStackTab)) {
            session.viewState.walledKeepTowerStackTab = tabs.getFirst();
        }
    }

    public boolean uniqueCornerTower(String topologySlotId) {
        return session.draft().topologyProfile.uniqueCornerTower(topologySlotId);
    }

    public void uniqueCornerTower(String topologySlotId, boolean value) {
        MKWorkspaceTopologyProfile current = session.draft().topologyProfile;
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID.equals(current.plannerId())) {
            return;
        }
        boolean northWest = "keep.corner.north_west".equals(topologySlotId) ? value : current.uniqueNorthWestCornerTower();
        boolean northEast = "keep.corner.north_east".equals(topologySlotId) ? value : current.uniqueNorthEastCornerTower();
        boolean southEast = "keep.corner.south_east".equals(topologySlotId) ? value : current.uniqueSouthEastCornerTower();
        boolean southWest = "keep.corner.south_west".equals(topologySlotId) ? value : current.uniqueSouthWestCornerTower();
        session.draft().topologyProfile = topologyProfileWithCornerModes(northWest, northEast, southEast, southWest);
        session.ensureFamiliesForActiveCornerSlots();
        normalizeTowerStackTab();
    }

    public int wallHeight() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorHeight)
                .orElse(7);
    }

    public void wallHeight(int value) {
        int height = Math.max(2, value);
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(), height,
                                Math.min(linearRun.topVoidMargin(), Math.max(0, height - 1))) :
                        linearRun)
                .toList();
    }

    public int wallUnitSpan() {
        return session.draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::length)
                .orElse(MKWorkspaceLinearRunFamilyDefinition.DEFAULT_WALLED_KEEP_WALL_SEGMENT_LENGTH);
    }

    public void wallUnitSpan(int value) {
        int span = makeOdd(Math.max(3, value));
        session.draft().linearRunFamilies = session.draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithLengthAndInteriorWidth(linearRun, span, linearRun.interiorWidth()) :
                        linearRun)
                .toList();
        syncGatehouseWallDimensions(span, wallPassageWidth());
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
    }

    public void resetPerimeterDefaults() {
        Map<String, MKWorkspaceLinearRunFamilyDefinition> defaultsBySlot =
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(MKWorkspaceDimensions.defaultDimensions(),
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
        ArrayList<MKWorkspaceTowerStackSettings> settings = new ArrayList<>(current.towerStackSettings());
        copyTowerStackSettingsIfMissing(settings, "keep.center", "keep.corner.shared");
        if (!northWest || !northEast || !southEast || !southWest) {
            copyTowerStackSettingsIfMissing(settings, "keep.corner.shared", firstCornerStackId(settings));
        }
        if (northWest) {
            copyTowerStackSettingsIfMissing(settings, "keep.corner.north_west", "keep.corner.shared");
        }
        if (northEast) {
            copyTowerStackSettingsIfMissing(settings, "keep.corner.north_east", "keep.corner.shared");
        }
        if (southEast) {
            copyTowerStackSettingsIfMissing(settings, "keep.corner.south_east", "keep.corner.shared");
        }
        if (southWest) {
            copyTowerStackSettingsIfMissing(settings, "keep.corner.south_west", "keep.corner.shared");
        }
        return new MKWorkspaceTopologyProfile(
                MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID,
                northWest && northEast && southEast && southWest,
                northWest,
                northEast,
                southEast,
                southWest,
                settings,
                current.floorTopologySettings(),
                current.pathSettings(),
                current.courtyardSettings(),
                current.terrainAdjustment()
        );
    }

    private void copyTowerStackSettingsIfMissing(List<MKWorkspaceTowerStackSettings> settings, String targetId,
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

    private String firstCornerStackId(List<MKWorkspaceTowerStackSettings> settings) {
        return settings.stream()
                .map(MKWorkspaceTowerStackSettings::stackId)
                .filter(KEEP_CORNER_STACK_IDS::contains)
                .findFirst()
                .orElse("keep.center");
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
