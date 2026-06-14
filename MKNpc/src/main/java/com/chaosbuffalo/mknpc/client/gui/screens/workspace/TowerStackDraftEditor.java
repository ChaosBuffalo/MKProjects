package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;

import java.util.List;
import java.util.Optional;

public final class TowerStackDraftEditor {
    private final WorkspaceDraftSession session;
    private final String stackId;

    TowerStackDraftEditor(WorkspaceDraftSession session, String stackId) {
        this.session = session;
        this.stackId = stackId;
    }

    public String stackId() {
        return stackId;
    }

    public MKWorkspaceTowerStackSettings settingsForUi() {
        return session.towerStackSettingsForUi(stackId);
    }

    public List<MKTowerWorkspaceFamilyDefinition> familiesForUi() {
        return session.towerStackFamiliesForUi(stackId);
    }

    public String previewSelection() {
        return session.towerStackPreviewSelection(stackId);
    }

    public void previewSelection(String sectionKey) {
        session.towerStackPreviewSelection(stackId, sectionKey);
    }

    public int width() {
        return session.towerStackWidth(stackId);
    }

    public void width(int value) {
        session.towerStackWidth(stackId, value);
    }

    public int length() {
        return session.towerStackLength(stackId);
    }

    public void length(int value) {
        session.towerStackLength(stackId, value);
    }

    public int height() {
        return session.towerStackHeight(stackId);
    }

    public void height(int value) {
        session.towerStackHeight(stackId, value);
    }

    public int entryHeight() {
        return session.towerStackEntryHeight(stackId);
    }

    public void entryHeight(int value) {
        session.towerStackEntryHeight(stackId, value);
    }

    public int basementHeight() {
        return session.towerStackBasementHeight(stackId);
    }

    public void basementHeight(int value) {
        session.towerStackBasementHeight(stackId, value);
    }

    public int basementEntryHeight() {
        return session.towerStackBasementEntryHeight(stackId);
    }

    public void basementEntryHeight(int value) {
        session.towerStackBasementEntryHeight(stackId, value);
    }

    public int basementCapHeight() {
        return session.towerStackBasementCapHeight(stackId);
    }

    public void basementCapHeight(int value) {
        session.towerStackBasementCapHeight(stackId, value);
    }

    public int mainHeight() {
        return session.towerStackMainHeight(stackId);
    }

    public void mainHeight(int value) {
        session.towerStackMainHeight(stackId, value);
    }

    public int mainCapHeight() {
        return session.towerStackMainCapHeight(stackId);
    }

    public void mainCapHeight(int value) {
        session.towerStackMainCapHeight(stackId, value);
    }

    public int mainFloors() {
        return session.towerStackMainFloors(stackId);
    }

    public void mainFloors(int value) {
        session.towerStackMainFloors(stackId, value);
    }

    public int minMainFloors() {
        return session.towerStackMinMainFloors(stackId);
    }

    public void minMainFloors(int value) {
        session.towerStackMinMainFloors(stackId, value);
    }

    public int basementFloors() {
        return session.towerStackBasementFloors(stackId);
    }

    public void basementFloors(int value) {
        session.towerStackBasementFloors(stackId, value);
    }

    public int minBasementFloors() {
        return session.towerStackMinBasementFloors(stackId);
    }

    public void minBasementFloors(int value) {
        session.towerStackMinBasementFloors(stackId, value);
    }

    public List<Integer> allowedMainFloorCounts() {
        return session.allowedTowerStackMainFloorCounts(stackId);
    }

    public List<Integer> allowedBasementFloorCounts() {
        return session.allowedTowerStackBasementFloorCounts(stackId);
    }

    public boolean topCapApproachEnabled() {
        return session.towerStackTopCapApproachEnabled(stackId);
    }

    public void topCapApproachEnabled(boolean value) {
        session.towerStackTopCapApproachEnabled(stackId, value);
    }

    public boolean basementEntryEnabled() {
        return session.towerStackBasementEntryEnabled(stackId);
    }

    public void basementEntryEnabled(boolean value) {
        session.towerStackBasementEntryEnabled(stackId, value);
    }

    public boolean basementCapApproachEnabled() {
        return session.towerStackBasementCapApproachEnabled(stackId);
    }

    public void basementCapApproachEnabled(boolean value) {
        session.towerStackBasementCapApproachEnabled(stackId, value);
    }

    public int shaftSize() {
        return session.towerStackShaftSize(stackId);
    }

    public void shaftSize(int value) {
        session.towerStackShaftSize(stackId, value);
    }

    public List<Integer> allowedShaftSizes() {
        return session.allowedTowerStackShaftSizes(stackId);
    }

    public MKVerticalAccessPlacement verticalAccessPlacement() {
        return session.towerStackVerticalAccessPlacement(stackId);
    }

    public void verticalAccessPlacement(MKVerticalAccessPlacement value) {
        session.towerStackVerticalAccessPlacement(stackId, value);
    }

    public MKWorkspaceStairMode stairMode() {
        return session.towerStackStairMode(stackId);
    }

    public void stairMode(MKWorkspaceStairMode value) {
        session.towerStackStairMode(stackId, value);
    }

    public MKWorkspaceStairRiseType stairRiseType() {
        return session.towerStackStairRiseType(stackId);
    }

    public void stairRiseType(MKWorkspaceStairRiseType value) {
        session.towerStackStairRiseType(stackId, value);
    }

    public int stairWidth() {
        return session.towerStackStairWidth(stackId);
    }

    public void stairWidth(int value) {
        session.towerStackStairWidth(stackId, value);
    }

    public int topCapUpperVoidMargin() {
        return session.towerStackTopCapUpperVoidMargin(stackId);
    }

    public void topCapUpperVoidMargin(int value) {
        session.towerStackTopCapUpperVoidMargin(stackId, value);
    }

    public int bottomCapLowerVoidMargin() {
        return session.towerStackBottomCapLowerVoidMargin(stackId);
    }

    public void bottomCapLowerVoidMargin(int value) {
        session.towerStackBottomCapLowerVoidMargin(stackId, value);
    }

    public MKWorkspaceFoundationPolicy foundationPolicy() {
        return session.towerStackFoundationPolicy(stackId);
    }

    public void foundationPolicy(MKWorkspaceFoundationPolicy value) {
        session.towerStackFoundationPolicy(stackId, value);
    }

    public MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode() {
        return session.towerStackHorizontalExtrusionMode(stackId);
    }

    public void horizontalExtrusionMode(MKWorkspaceHorizontalExtrusionMode value) {
        session.towerStackHorizontalExtrusionMode(stackId, value);
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return session.towerStackPaletteOverrideOpt(stackId);
    }

    public void paletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        session.towerStackPaletteOverride(stackId, value);
    }

    public MKWorkspaceMaterialPalette resolvedPalette() {
        return session.resolveTowerStackPalette(stackId);
    }

    public void resetDefaults() {
        session.resetTowerStackDefaults(stackId);
    }
}
