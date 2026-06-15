package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
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
        return settings();
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
        return settings().width();
    }

    public void width(int value) {
        replace(settings().withWidth(makeOdd(Math.max(3, value))));
        session.applyTowerStackSettingsToFamilies();
    }

    public int length() {
        return settings().length();
    }

    public void length(int value) {
        replace(settings().withLength(makeOdd(Math.max(3, value))));
        session.applyTowerStackSettingsToFamilies();
    }

    public int height() {
        return settings().height();
    }

    public void height(int value) {
        replaceWithNormalizedFloorCounts(settings().withHeight(Math.max(3, value)));
        session.applyTowerStackSettingsToFamilies();
    }

    public int entryHeight() {
        return settings().entryHeight();
    }

    public void entryHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withEntryHeight(value));
        session.applyTowerStackSettingsToFamilies();
    }

    public int basementHeight() {
        return settings().basementHeight();
    }

    public void basementHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withBasementHeight(value));
        session.applyTowerStackSettingsToFamilies();
    }

    public int basementEntryHeight() {
        return settings().basementEntryHeight();
    }

    public void basementEntryHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withBasementEntryHeight(value));
        session.applyTowerStackSettingsToFamilies();
    }

    public int basementCapHeight() {
        return settings().basementCapHeight();
    }

    public void basementCapHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withBasementCapHeight(value));
        session.applyTowerStackSettingsToFamilies();
    }

    public int mainHeight() {
        return settings().mainHeight();
    }

    public void mainHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withMainHeight(value));
        session.applyTowerStackSettingsToFamilies();
    }

    public int mainCapHeight() {
        return settings().mainCapHeight();
    }

    public void mainCapHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withMainCapHeight(value));
        session.applyTowerStackSettingsToFamilies();
    }

    public int mainFloors() {
        return settings().mainFloors();
    }

    public void mainFloors(int value) {
        MKWorkspaceTowerStackSettings current = settings();
        int normalizedMain = session.normalizeTowerStackMainFloorCount(current, value, current.basementFloors());
        int normalizedBasement = session.normalizeTowerStackBasementFloorCount(current, current.basementFloors(),
                normalizedMain);
        replace(current.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public int minMainFloors() {
        return settings().minMainFloors();
    }

    public void minMainFloors(int value) {
        replace(settings().withMinMainFloors(value));
    }

    public int basementFloors() {
        return settings().basementFloors();
    }

    public void basementFloors(int value) {
        MKWorkspaceTowerStackSettings current = settings();
        int normalizedBasement = session.normalizeTowerStackBasementFloorCount(current, value, current.mainFloors());
        int normalizedMain = session.normalizeTowerStackMainFloorCount(current, current.mainFloors(),
                normalizedBasement);
        replace(current.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public int minBasementFloors() {
        return settings().minBasementFloors();
    }

    public void minBasementFloors(int value) {
        replace(settings().withMinBasementFloors(value));
    }

    public List<Integer> allowedMainFloorCounts() {
        MKWorkspaceTowerStackSettings current = settings();
        return session.allowedTowerStackMainFloorCounts(current, current.basementFloors());
    }

    public List<Integer> allowedBasementFloorCounts() {
        MKWorkspaceTowerStackSettings current = settings();
        return session.allowedTowerStackBasementFloorCounts(current, current.mainFloors());
    }

    public boolean topCapApproachEnabled() {
        return settings().topCapApproachEnabled();
    }

    public void topCapApproachEnabled(boolean value) {
        MKWorkspaceTowerStackSettings updated = settings().withTopCapApproachEnabled(value);
        int normalizedMain = session.normalizeTowerStackMainFloorCount(updated, updated.mainFloors(),
                updated.basementFloors());
        int normalizedBasement = session.normalizeTowerStackBasementFloorCount(updated, updated.basementFloors(),
                normalizedMain);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public boolean basementEntryEnabled() {
        return settings().basementEntryEnabled();
    }

    public void basementEntryEnabled(boolean value) {
        MKWorkspaceTowerStackSettings updated = settings().withBasementEntryEnabled(value);
        int normalizedBasement = session.normalizeTowerStackBasementFloorCount(updated, updated.basementFloors(),
                updated.mainFloors());
        int normalizedMain = session.normalizeTowerStackMainFloorCount(updated, updated.mainFloors(),
                normalizedBasement);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public boolean basementCapApproachEnabled() {
        return settings().basementCapApproachEnabled();
    }

    public void basementCapApproachEnabled(boolean value) {
        MKWorkspaceTowerStackSettings updated = settings().withBasementCapApproachEnabled(value);
        int normalizedBasement = session.normalizeTowerStackBasementFloorCount(updated, updated.basementFloors(),
                updated.mainFloors());
        int normalizedMain = session.normalizeTowerStackMainFloorCount(updated, updated.mainFloors(),
                normalizedBasement);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public int shaftSize() {
        return settings().shaftSize();
    }

    public void shaftSize(int value) {
        replace(settings().withShaftSize(value));
    }

    public List<Integer> allowedShaftSizes() {
        MKWorkspaceTowerStackSettings current = settings();
        return MKWorkspaceDimensions.getAllowedShaftSizes(current.width(), current.length());
    }

    public MKVerticalAccessPlacement verticalAccessPlacement() {
        return settings().verticalAccessPlacement();
    }

    public void verticalAccessPlacement(MKVerticalAccessPlacement value) {
        replace(settings().withVerticalAccessPlacement(value));
    }

    public MKWorkspaceStairMode stairMode() {
        return settings().stairConfig().mode();
    }

    public void stairMode(MKWorkspaceStairMode value) {
        MKWorkspaceStairAuthoringConfig config = settings().stairConfig();
        replaceStairConfig(new MKWorkspaceStairAuthoringConfig(value, config.riseType(), config.stairWidth()));
    }

    public MKWorkspaceStairRiseType stairRiseType() {
        return settings().stairConfig().riseType();
    }

    public void stairRiseType(MKWorkspaceStairRiseType value) {
        MKWorkspaceStairAuthoringConfig config = settings().stairConfig();
        replaceStairConfig(new MKWorkspaceStairAuthoringConfig(config.mode(), value, config.stairWidth()));
    }

    public int stairWidth() {
        return settings().stairConfig().stairWidth();
    }

    public void stairWidth(int value) {
        MKWorkspaceStairAuthoringConfig config = settings().stairConfig();
        replaceStairConfig(new MKWorkspaceStairAuthoringConfig(config.mode(), config.riseType(), value));
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
        return settings().foundationPolicy();
    }

    public void foundationPolicy(MKWorkspaceFoundationPolicy value) {
        replace(settings().withFoundationPolicy(value));
    }

    public MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode() {
        return settings().horizontalExtrusionMode();
    }

    public void horizontalExtrusionMode(MKWorkspaceHorizontalExtrusionMode value) {
        replace(settings().withHorizontalExtrusionMode(value));
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return settings().paletteOverrideOpt();
    }

    public void paletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        replace(settings().withPaletteOverride(value));
    }

    public MKWorkspaceMaterialPalette resolvedPalette() {
        return session.resolveTowerStackPalette(stackId);
    }

    public void resetDefaults() {
        replace(MKWorkspaceTowerStackSettings.defaults(stackId, 7));
        session.applyTowerStackSettingsToFamilies();
    }

    private MKWorkspaceTowerStackSettings settings() {
        return session.towerStackSettings(stackId);
    }

    private void replace(MKWorkspaceTowerStackSettings settings) {
        session.replaceTowerStackSettings(settings);
    }

    private void replaceWithNormalizedFloorCounts(MKWorkspaceTowerStackSettings settings) {
        session.replaceTowerStackSettingsWithNormalizedFloorCounts(settings);
    }

    private void replaceStairConfig(MKWorkspaceStairAuthoringConfig stairConfig) {
        MKWorkspaceTowerStackSettings updated = settings().withStairConfig(stairConfig);
        int normalizedMain = session.normalizeTowerStackMainFloorCount(updated, updated.mainFloors(),
                updated.basementFloors());
        int normalizedBasement = session.normalizeTowerStackBasementFloorCount(updated, updated.basementFloors(),
                normalizedMain);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    private int makeOdd(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }
}
