package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WorkspaceVerticalStackDraftEditor {
    static final String PRIMARY_STACK_ID = "tower.primary";

    private final WorkspaceDraftSession session;
    private final String stackId;

    WorkspaceVerticalStackDraftEditor(WorkspaceDraftSession session, String stackId) {
        this.session = session;
        this.stackId = stackId;
    }

    public String stackId() {
        return stackId;
    }

    public MKWorkspaceVerticalStackSettings settingsForUi() {
        return settings();
    }

    public List<MKWorkspaceRoomFamilyDefinition> familiesForUi() {
        return session.draft().familyDefinitions.stream()
                .filter(family -> session.verticalStackIdForTopologySlot(family.topologySlotId())
                        .filter(stackId::equals)
                        .isPresent())
                .map(session::normalizeFamilyDefinition)
                .toList();
    }

    public String previewSelection() {
        return session.viewState.verticalStackPreviewSelections.getOrDefault(stackId, "entry");
    }

    public void previewSelection(String sectionKey) {
        session.viewState.verticalStackPreviewSelections.put(stackId,
                sectionKey == null || sectionKey.isBlank() ? "entry" : sectionKey);
    }

    public int width() {
        return settings().width();
    }

    public void width(int value) {
        replace(settings().withWidth(makeOdd(Math.max(3, value))));
    }

    public int length() {
        return settings().length();
    }

    public void length(int value) {
        replace(settings().withLength(makeOdd(Math.max(3, value))));
    }

    public int height() {
        return settings().height();
    }

    public void height(int value) {
        replaceWithNormalizedFloorCounts(settings().withHeight(Math.max(3, value)));
    }

    public int entryHeight() {
        return settings().entryHeight();
    }

    public void entryHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withEntryHeight(value));
    }

    public int basementHeight() {
        return settings().basementHeight();
    }

    public void basementHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withBasementHeight(value));
    }

    public int basementEntryHeight() {
        return settings().basementEntryHeight();
    }

    public void basementEntryHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withBasementEntryHeight(value));
    }

    public int basementCapHeight() {
        return settings().basementCapHeight();
    }

    public void basementCapHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withBasementCapHeight(value));
    }

    public int mainHeight() {
        return settings().mainHeight();
    }

    public void mainHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withMainHeight(value));
    }

    public int mainCapHeight() {
        return settings().mainCapHeight();
    }

    public void mainCapHeight(int value) {
        replaceWithNormalizedFloorCounts(settings().withMainCapHeight(value));
    }

    public int mainFloors() {
        return settings().mainFloors();
    }

    public void mainFloors(int value) {
        MKWorkspaceVerticalStackSettings current = settings();
        int normalizedMain = session.normalizeVerticalStackMainFloorCount(current, value, current.basementFloors());
        int normalizedBasement = session.normalizeVerticalStackBasementFloorCount(current, current.basementFloors(),
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
        MKWorkspaceVerticalStackSettings current = settings();
        int normalizedBasement = session.normalizeVerticalStackBasementFloorCount(current, value, current.mainFloors());
        int normalizedMain = session.normalizeVerticalStackMainFloorCount(current, current.mainFloors(),
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
        MKWorkspaceVerticalStackSettings current = settings();
        return session.allowedVerticalStackMainFloorCounts(current, current.basementFloors());
    }

    public List<Integer> allowedBasementFloorCounts() {
        MKWorkspaceVerticalStackSettings current = settings();
        return session.allowedVerticalStackBasementFloorCounts(current, current.mainFloors());
    }

    public boolean topCapApproachEnabled() {
        return settings().topCapApproachEnabled();
    }

    public void topCapApproachEnabled(boolean value) {
        MKWorkspaceVerticalStackSettings updated = settings().withTopCapApproachEnabled(value);
        int normalizedMain = session.normalizeVerticalStackMainFloorCount(updated, updated.mainFloors(),
                updated.basementFloors());
        int normalizedBasement = session.normalizeVerticalStackBasementFloorCount(updated, updated.basementFloors(),
                normalizedMain);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public boolean basementEntryEnabled() {
        return settings().basementEntryEnabled();
    }

    public void basementEntryEnabled(boolean value) {
        MKWorkspaceVerticalStackSettings updated = settings().withBasementEntryEnabled(value);
        int normalizedBasement = session.normalizeVerticalStackBasementFloorCount(updated, updated.basementFloors(),
                updated.mainFloors());
        int normalizedMain = session.normalizeVerticalStackMainFloorCount(updated, updated.mainFloors(),
                normalizedBasement);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public boolean basementCapApproachEnabled() {
        return settings().basementCapApproachEnabled();
    }

    public void basementCapApproachEnabled(boolean value) {
        MKWorkspaceVerticalStackSettings updated = settings().withBasementCapApproachEnabled(value);
        int normalizedBasement = session.normalizeVerticalStackBasementFloorCount(updated, updated.basementFloors(),
                updated.mainFloors());
        int normalizedMain = session.normalizeVerticalStackMainFloorCount(updated, updated.mainFloors(),
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
        MKWorkspaceVerticalStackSettings current = settings();
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
        return verticalStackFamily(MKWorkspaceVerticalStackSlot.TOP_CAP)
                .map(MKWorkspaceRoomFamilyDefinition::topVoidMargin)
                .orElse(0);
    }

    public void topCapUpperVoidMargin(int value) {
        int maxMargin = Math.max(0, settings().mainCapHeight() - MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
        replaceVerticalStackFamilyVoidMargins(MKWorkspaceVerticalStackSlot.TOP_CAP, clamp(value, 0, maxMargin), 0);
    }

    public int bottomCapLowerVoidMargin() {
        return verticalStackFamily(MKWorkspaceVerticalStackSlot.BASEMENT_CAP)
                .map(MKWorkspaceRoomFamilyDefinition::bottomVoidMargin)
                .orElse(0);
    }

    public void bottomCapLowerVoidMargin(int value) {
        int maxMargin = Math.max(0, settings().basementCapHeight() - MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
        replaceVerticalStackFamilyVoidMargins(MKWorkspaceVerticalStackSlot.BASEMENT_CAP, 0, clamp(value, 0, maxMargin));
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
        return session.topologyGroupPaletteOverride(stackId);
    }

    public void paletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        session.topologyGroupPaletteOverride(stackId, value);
    }

    public MKWorkspaceMaterialPalette resolvedPalette() {
        return session.resolveTopologyGroupPalette(stackId);
    }

    public void resetDefaults() {
        replace(MKWorkspaceVerticalStackSettings.defaults(stackId, 7));
    }

    private MKWorkspaceVerticalStackSettings settings() {
        return session.verticalStackSettings(stackId);
    }

    private void replace(MKWorkspaceVerticalStackSettings settings) {
        session.replaceVerticalStackSettings(settings);
    }

    private void replaceWithNormalizedFloorCounts(MKWorkspaceVerticalStackSettings settings) {
        session.replaceVerticalStackSettingsWithNormalizedFloorCounts(settings);
    }

    private void replaceStairConfig(MKWorkspaceStairAuthoringConfig stairConfig) {
        MKWorkspaceVerticalStackSettings updated = settings().withStairConfig(stairConfig);
        int normalizedMain = session.normalizeVerticalStackMainFloorCount(updated, updated.mainFloors(),
                updated.basementFloors());
        int normalizedBasement = session.normalizeVerticalStackBasementFloorCount(updated, updated.basementFloors(),
                normalizedMain);
        replace(updated.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    private Optional<MKWorkspaceRoomFamilyDefinition> verticalStackFamily(MKWorkspaceVerticalStackSlot slot) {
        String slotId = slot.slotId(stackId);
        return session.draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals(slotId))
                .findFirst();
    }

    private void replaceVerticalStackFamilyVoidMargins(MKWorkspaceVerticalStackSlot slot,
                                                    int topVoidMargin, int bottomVoidMargin) {
        String slotId = slot.slotId(stackId);
        Optional<MKWorkspaceRoomFamilyDefinition> source = verticalStackFamily(slot)
                .or(() -> session.topologySlot(slotId).map(session::defaultFamilyForTopologySlot));
        if (source.isEmpty()) {
            return;
        }
        MKWorkspaceRoomFamilyDefinition updated = session.normalizeFamilyDefinition(copyFamilyWithVoidMargins(
                source.get(), topVoidMargin, bottomVoidMargin));
        ArrayList<MKWorkspaceRoomFamilyDefinition> families = new ArrayList<>(session.draft().familyDefinitions);
        boolean replaced = false;
        for (int index = 0; index < families.size(); index++) {
            if (families.get(index).topologySlotId().equals(slotId)) {
                families.set(index, updated);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            families.add(updated);
        }
        session.draft().familyDefinitions = List.copyOf(families);
    }

    private MKWorkspaceRoomFamilyDefinition copyFamilyWithVoidMargins(MKWorkspaceRoomFamilyDefinition family,
                                                                       int topVoidMargin, int bottomVoidMargin) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                topVoidMargin,
                bottomVoidMargin,
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int makeOdd(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }
}
