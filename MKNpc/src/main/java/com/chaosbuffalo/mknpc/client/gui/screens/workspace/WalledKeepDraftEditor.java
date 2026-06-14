package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWalledKeepCourtyardSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.List;

public final class WalledKeepDraftEditor {
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
        tabs.addAll(session.activeCornerTopologySlots());
        return List.copyOf(tabs);
    }

    public String towerStackTab() {
        normalizeTowerStackTab();
        return session.draft().walledKeepTowerStackTab;
    }

    public void towerStackTab(String stackId) {
        List<String> tabs = towerStackTabs();
        session.draft().walledKeepTowerStackTab = tabs.contains(stackId) ? stackId :
                (tabs.isEmpty() ? "keep.center" : tabs.getFirst());
    }

    void normalizeTowerStackTab() {
        List<String> tabs = towerStackTabs();
        if (tabs.isEmpty()) {
            session.draft().walledKeepTowerStackTab = "keep.center";
            return;
        }
        if (!tabs.contains(session.draft().walledKeepTowerStackTab)) {
            session.draft().walledKeepTowerStackTab = tabs.getFirst();
        }
    }

    public boolean uniqueCornerTower(String topologySlotId) {
        return session.uniqueCornerTower(topologySlotId);
    }

    public void uniqueCornerTower(String topologySlotId, boolean value) {
        session.uniqueCornerTower(topologySlotId, value);
    }

    public int wallHeight() {
        return session.wallHeight();
    }

    public void wallHeight(int value) {
        session.wallHeight(value);
    }

    public int wallUnitSpan() {
        return session.wallUnitSpan();
    }

    public void wallUnitSpan(int value) {
        session.wallUnitSpan(value);
    }

    public int wallPassageWidth() {
        return session.wallPassageWidth();
    }

    public void wallPassageWidth(int value) {
        session.wallPassageWidth(value);
    }

    public MKWorkspaceLinearRunKind perimeterRunKind() {
        return session.perimeterRunKind();
    }

    public void perimeterRunKind(MKWorkspaceLinearRunKind value) {
        session.perimeterRunKind(value);
    }

    public int wallTopVoidMargin() {
        return session.wallTopVoidMargin();
    }

    public void wallTopVoidMargin(int value) {
        session.wallTopVoidMargin(value);
    }

    public void resetPerimeterDefaults() {
        session.resetWalledKeepPerimeterDefaults();
    }
}
