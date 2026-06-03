package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerStackBudget;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteSwapSafety;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPathSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackFloorCounts;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceRegionSchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceRoleSchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceTopologySchema;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

public class WorkspaceDraftSession {
    private final MKWorkspaceScreen screen;
    private Draft draft;
    private int selectedFamilyIndex;
    private int selectedFamilyExitIndex;
    private int selectedOpeningIndex;
    private int selectedLinearRunIndex;
    private static final List<String> KEEP_CORNER_STACK_IDS = List.of(
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west"
    );
    private static final String TOWER_PRIMARY_STACK_ID = "tower.primary";

    public WorkspaceDraftSession(MKWorkspaceScreen screen, int selectedFamilyIndex, int selectedFamilyExitIndex, int selectedOpeningIndex,
                                 int selectedLinearRunIndex) {
        this.screen = screen;
        this.selectedFamilyIndex = selectedFamilyIndex;
        this.selectedFamilyExitIndex = selectedFamilyExitIndex;
        this.selectedOpeningIndex = selectedOpeningIndex;
        this.selectedLinearRunIndex = selectedLinearRunIndex;
    }

    public Draft draft() {
        ensureInitialized();
        return draft;
    }

    public void ensureInitialized() {
        if (draft != null) {
            return;
        }
        MKStructureWorkspace workspace = screen.workspace();
        draft = new Draft();
        draft.namespace = valueOrDefault(workspace != null ? workspace.namespace() : null, "mkdev");
        draft.structureName = valueOrDefault(workspace != null ? workspace.structureName() : null, "tower_workspace");
        draft.topologyProfile = workspace != null ? workspace.topologyProfile() : MKWorkspaceTopologyProfile.tower();
        draft.stairMode = workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO;
        draft.stairRiseType = workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.MIXED;
        draft.stairWidth = workspace != null ? workspace.stairConfig().stairWidth() : 1;
        draft.palette = workspace != null ? workspace.palette() : MKWorkspaceMaterialPalette.defaultPalette();
        draft.verticalAccessPlacement = workspace != null ? workspace.verticalAccessSpec().placement() : MKVerticalAccessPlacement.CENTER;
        draft.shellMargin = workspace != null ? workspace.shellMargin() : 1;
        draft.exteriorAirMargin = workspace != null ? workspace.exteriorAirMargin() : 2;
        draft.previewMargin = workspace != null ? workspace.previewMargin() : 4;
        MKWorkspaceTowerStackSettings primaryStackSettings = primaryStackSettings(draft.topologyProfile);
        draft.mainFloors = primaryStackSettings.mainFloors();
        draft.basementFloors = primaryStackSettings.basementFloors();
        draft.topCapApproachEnabled = primaryStackSettings.topCapApproachEnabled();
        draft.basementCapApproachEnabled = primaryStackSettings.basementCapApproachEnabled();
        draft.familyDefinitions = List.copyOf(workspace != null ? workspace.familyDefinitions() :
                MKTowerWorkspaceFamilyDefinition.createDefaults());
        draft.openingProfiles = List.copyOf(workspace != null ? workspace.openingProfiles() :
                MKHorizontalOpeningProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()));
        draft.linearRunFamilies = List.copyOf(workspace != null ? workspace.linearRunFamilies() :
                MKWorkspaceLinearRunFamilyDefinition.createDefaults(MKWorkspaceDimensions.defaultDimensions(),
                        MKWorkspaceMaterialPalette.defaultPalette()));
        int requestedShaftSize = workspace != null ? workspace.verticalAccessSpec().shaftSize() :
                MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
        draft.shaftSize = requestedShaftSize;
        seedDefaultsForTopology();
        snapDraftVerticalAccess();
    }

    public String summary() {
        Draft draft = draft();
        return draft.namespace + ":" + draft.structureName + "  |  " +
                "shaft " + draft.shaftSize +
                "  |  topology " + draft.topologyProfile.profileType() +
                "  |  stacks " + draft.topologyProfile.towerStackSettings().size() +
                "  |  families " + draft.familyDefinitions.size() +
                "  |  openings " + draft.openingProfiles.size() +
                "  |  linear runs " + draft.linearRunFamilies.size();
    }

    public String workspaceId() {
        Draft draft = draft();
        return draft.namespace + ":" + draft.structureName;
    }

    public boolean hasExistingWorkspacePieces() {
        return screen.hasExistingWorkspacePieces();
    }

    public void submit() {
        snapDraftVerticalAccess();
        MKStructureWorkspace draft = buildWorkspaceDraft();
        if (requiresDestructiveRegenerateConfirmation(draft)) {
            screen.pushState("generate_confirm");
            screen.flagNeedSetup();
            return;
        }
        send(draft);
    }

    public void send() {
        send(buildWorkspaceDraft());
    }

    public String namespace() {
        return draft().namespace;
    }

    public void namespace(String value) {
        draft().namespace = value;
    }

    public String structureName() {
        return draft().structureName;
    }

    public void structureName(String value) {
        draft().structureName = value;
    }

    public int shellMargin() {
        return draft().shellMargin;
    }

    public void shellMargin(int value) {
        draft().shellMargin = value;
    }

    public int exteriorAirMargin() {
        return draft().exteriorAirMargin;
    }

    public void exteriorAirMargin(int value) {
        draft().exteriorAirMargin = value;
    }

    public int previewMargin() {
        return draft().previewMargin;
    }

    public void previewMargin(int value) {
        draft().previewMargin = value;
    }

    public String topologyProfileType() {
        return draft().topologyProfile.profileType();
    }

    public void topologyProfileType(String value) {
        draft().topologyProfile = MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(value) ?
                walledKeepTopologyProfile(
                        draft().topologyProfile.uniqueNorthWestCornerTower(),
                        draft().topologyProfile.uniqueNorthEastCornerTower(),
                        draft().topologyProfile.uniqueSouthEastCornerTower(),
                        draft().topologyProfile.uniqueSouthWestCornerTower()
                ) : MKWorkspaceTopologyProfile.tower();
        seedDefaultsForTopology();
    }

    public boolean uniqueCornerTowers() {
        return draft().topologyProfile.uniqueCornerTowers();
    }

    public void uniqueCornerTowers(boolean value) {
        draft().topologyProfile = MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType()) ?
                walledKeepTopologyProfile(value, value, value, value) : MKWorkspaceTopologyProfile.tower();
        ensureFamiliesForActiveCornerSlots();
    }

    public boolean uniqueCornerTower(String topologySlotId) {
        return draft().topologyProfile.uniqueCornerTower(topologySlotId);
    }

    private MKWorkspaceTopologyProfile walledKeepTopologyProfile(boolean northWest, boolean northEast,
                                                                 boolean southEast, boolean southWest) {
        MKWorkspaceTopologyProfile current = draft().topologyProfile;
        java.util.ArrayList<MKWorkspaceTowerStackSettings> settings = new java.util.ArrayList<>(current.towerStackSettings());
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
                MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE,
                northWest && northEast && southEast && southWest,
                northWest,
                northEast,
                southEast,
                southWest,
                settings,
                current.pathSettings(),
                current.courtyardSettings()
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
                .filter(this::isConcreteCornerSlot)
                .findFirst()
                .orElse("keep.center");
    }

    public void uniqueCornerTower(String topologySlotId, boolean value) {
        MKWorkspaceTopologyProfile current = draft().topologyProfile;
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(current.profileType())) {
            return;
        }
        boolean northWest = "keep.corner.north_west".equals(topologySlotId) ? value : current.uniqueNorthWestCornerTower();
        boolean northEast = "keep.corner.north_east".equals(topologySlotId) ? value : current.uniqueNorthEastCornerTower();
        boolean southEast = "keep.corner.south_east".equals(topologySlotId) ? value : current.uniqueSouthEastCornerTower();
        boolean southWest = "keep.corner.south_west".equals(topologySlotId) ? value : current.uniqueSouthWestCornerTower();
        draft().topologyProfile = walledKeepTopologyProfile(northWest, northEast, southEast, southWest);
        ensureFamiliesForActiveCornerSlots();
    }

    public int walledKeepCenterWidth() {
        return towerStackWidth("keep.center");
    }

    public void walledKeepCenterWidth(int value) {
        towerStackWidth("keep.center", value);
    }

    public int walledKeepCenterLength() {
        return towerStackLength("keep.center");
    }

    public void walledKeepCenterLength(int value) {
        towerStackLength("keep.center", value);
    }

    public int walledKeepCenterHeight() {
        return towerStackHeight("keep.center");
    }

    public void walledKeepCenterHeight(int value) {
        towerStackHeight("keep.center", value);
    }

    public int towerStackWidth(String stackId) {
        return towerStackSettings(stackId).width();
    }

    public void towerStackWidth(String stackId, int value) {
        int width = makeOdd(Math.max(3, value));
        replaceTowerStackSettings(towerStackSettings(stackId).withWidth(width));
        applyTowerStackSettingsToFamilies();
    }

    public int towerStackLength(String stackId) {
        return towerStackSettings(stackId).length();
    }

    public void towerStackLength(String stackId, int value) {
        int length = makeOdd(Math.max(3, value));
        replaceTowerStackSettings(towerStackSettings(stackId).withLength(length));
        applyTowerStackSettingsToFamilies();
    }

    public int towerStackHeight(String stackId) {
        return towerStackSettings(stackId).height();
    }

    public void towerStackHeight(String stackId, int value) {
        int height = Math.max(3, value);
        replaceTowerStackSettings(towerStackSettings(stackId).withHeight(height));
        applyTowerStackSettingsToFamilies();
    }

    public void topologyDefaultHeight(int value) {
        int requestedHeight = Math.max(3, value);
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfileType())) {
            walledKeepCenterHeight(requestedHeight);
            return;
        }
        replaceTowerStackSettings(towerStackSettings("tower.primary").withHeight(requestedHeight));
        applyTowerStackSettingsToFamilies();
        snapDraftVerticalAccess();
    }

    public List<String> activeCornerTopologySlots() {
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfileType())) {
            return List.of();
        }
        java.util.ArrayList<String> slots = new java.util.ArrayList<>();
        if (draft().topologyProfile.anySharedCornerTower()) {
            slots.add("keep.corner.shared");
        }
        for (String cornerSlot : List.of(
                "keep.corner.north_west",
                "keep.corner.north_east",
                "keep.corner.south_east",
                "keep.corner.south_west"
        )) {
            if (draft().topologyProfile.uniqueCornerTower(cornerSlot)) {
                slots.add(cornerSlot);
            }
        }
        return List.copyOf(slots);
    }

    public int cornerTowerWidth(String topologySlotId) {
        return towerStackWidth(topologySlotId);
    }

    public void cornerTowerWidth(String topologySlotId, int value) {
        towerStackWidth(topologySlotId, value);
    }

    public int cornerTowerLength(String topologySlotId) {
        return towerStackLength(topologySlotId);
    }

    public void cornerTowerLength(String topologySlotId, int value) {
        towerStackLength(topologySlotId, value);
    }

    public int cornerTowerHeight(String topologySlotId) {
        return towerStackHeight(topologySlotId);
    }

    public void cornerTowerHeight(String topologySlotId, int value) {
        towerStackHeight(topologySlotId, value);
    }

    public int towerStackMainFloors(String stackId) {
        return towerStackSettings(stackId).mainFloors();
    }

    public void towerStackMainFloors(String stackId, int value) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId);
        int normalizedMain = normalizeTowerStackMainFloorCount(settings, value, settings.basementFloors());
        int normalizedBasement = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), normalizedMain);
        replaceTowerStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public int towerStackBasementFloors(String stackId) {
        return towerStackSettings(stackId).basementFloors();
    }

    public void towerStackBasementFloors(String stackId, int value) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId);
        int normalizedBasement = normalizeTowerStackBasementFloorCount(settings, value, settings.mainFloors());
        int normalizedMain = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), normalizedBasement);
        replaceTowerStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public boolean towerStackTopCapApproachEnabled(String stackId) {
        return towerStackSettings(stackId).topCapApproachEnabled();
    }

    public void towerStackTopCapApproachEnabled(String stackId, boolean value) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId).withTopCapApproachEnabled(value);
        int normalizedMain = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), settings.basementFloors());
        int normalizedBasement = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), normalizedMain);
        replaceTowerStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public boolean towerStackBasementCapApproachEnabled(String stackId) {
        return towerStackSettings(stackId).basementCapApproachEnabled();
    }

    public void towerStackBasementCapApproachEnabled(String stackId, boolean value) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId).withBasementCapApproachEnabled(value);
        int normalizedBasement = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), settings.mainFloors());
        int normalizedMain = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), normalizedBasement);
        replaceTowerStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    public int towerStackShaftSize(String stackId) {
        return towerStackSettings(stackId).shaftSize();
    }

    public void towerStackShaftSize(String stackId, int value) {
        replaceTowerStackSettings(towerStackSettings(stackId).withShaftSize(value));
    }

    public List<Integer> allowedTowerStackShaftSizes(String stackId) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId);
        return MKWorkspaceDimensions.getAllowedShaftSizes(settings.width(), settings.length());
    }

    public MKVerticalAccessPlacement towerStackVerticalAccessPlacement(String stackId) {
        return towerStackSettings(stackId).verticalAccessPlacement();
    }

    public void towerStackVerticalAccessPlacement(String stackId, MKVerticalAccessPlacement value) {
        replaceTowerStackSettings(towerStackSettings(stackId).withVerticalAccessPlacement(value));
    }

    public MKWorkspaceStairMode towerStackStairMode(String stackId) {
        return towerStackSettings(stackId).stairConfig().mode();
    }

    public void towerStackStairMode(String stackId, MKWorkspaceStairMode value) {
        MKWorkspaceStairAuthoringConfig config = towerStackSettings(stackId).stairConfig();
        replaceTowerStackStairConfig(stackId, new MKWorkspaceStairAuthoringConfig(
                value, config.riseType(), config.stairWidth(), config.stairBlock(), config.slabBlock(),
                config.ladderBlock()));
    }

    public MKWorkspaceStairRiseType towerStackStairRiseType(String stackId) {
        return towerStackSettings(stackId).stairConfig().riseType();
    }

    public void towerStackStairRiseType(String stackId, MKWorkspaceStairRiseType value) {
        MKWorkspaceStairAuthoringConfig config = towerStackSettings(stackId).stairConfig();
        replaceTowerStackStairConfig(stackId, new MKWorkspaceStairAuthoringConfig(
                config.mode(), value, config.stairWidth(), config.stairBlock(), config.slabBlock(),
                config.ladderBlock()));
    }

    public int towerStackStairWidth(String stackId) {
        return towerStackSettings(stackId).stairConfig().stairWidth();
    }

    public void towerStackStairWidth(String stackId, int value) {
        MKWorkspaceStairAuthoringConfig config = towerStackSettings(stackId).stairConfig();
        replaceTowerStackStairConfig(stackId, new MKWorkspaceStairAuthoringConfig(
                config.mode(), config.riseType(), value, config.stairBlock(), config.slabBlock(),
                config.ladderBlock()));
    }

    public List<Integer> allowedTowerStackStairWidths(String stackId) {
        return MKWorkspaceDimensions.getAllowedStairWidths(towerStackShaftSize(stackId));
    }

    public ResourceLocation towerStackStairBlock(String stackId) {
        return towerStackSettings(stackId).stairConfig().stairBlock();
    }

    public void towerStackStairBlock(String stackId, ResourceLocation value) {
        MKWorkspaceStairAuthoringConfig config = towerStackSettings(stackId).stairConfig();
        replaceTowerStackStairConfig(stackId, new MKWorkspaceStairAuthoringConfig(
                config.mode(), config.riseType(), config.stairWidth(), value, config.slabBlock(),
                config.ladderBlock()));
    }

    public ResourceLocation towerStackSlabBlock(String stackId) {
        return towerStackSettings(stackId).stairConfig().slabBlock();
    }

    public void towerStackSlabBlock(String stackId, ResourceLocation value) {
        MKWorkspaceStairAuthoringConfig config = towerStackSettings(stackId).stairConfig();
        replaceTowerStackStairConfig(stackId, new MKWorkspaceStairAuthoringConfig(
                config.mode(), config.riseType(), config.stairWidth(), config.stairBlock(), value,
                config.ladderBlock()));
    }

    public ResourceLocation towerStackLadderBlock(String stackId) {
        return towerStackSettings(stackId).stairConfig().ladderBlock();
    }

    public void towerStackLadderBlock(String stackId, ResourceLocation value) {
        MKWorkspaceStairAuthoringConfig config = towerStackSettings(stackId).stairConfig();
        replaceTowerStackStairConfig(stackId, new MKWorkspaceStairAuthoringConfig(
                config.mode(), config.riseType(), config.stairWidth(), config.stairBlock(), config.slabBlock(),
                value));
    }

    public MKWorkspaceFoundationPolicy towerStackFoundationPolicy(String stackId) {
        return towerStackSettings(stackId).foundationPolicy();
    }

    public void towerStackFoundationPolicy(String stackId, MKWorkspaceFoundationPolicy value) {
        replaceTowerStackSettings(towerStackSettings(stackId).withFoundationPolicy(value));
    }

    public Optional<MKWorkspacePaletteOverride> towerStackPaletteOverrideOpt(String stackId) {
        return towerStackSettings(stackId).paletteOverrideOpt();
    }

    public void towerStackPaletteOverride(String stackId, Optional<MKWorkspacePaletteOverride> value) {
        replaceTowerStackSettings(towerStackSettings(stackId).withPaletteOverride(value));
    }

    public int nextAllowedTowerStackMainFloorCount(String stackId, boolean reverse) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId);
        List<Integer> allowed = allowedTowerStackMainFloorCounts(settings, settings.basementFloors());
        int normalized = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), settings.basementFloors());
        return cycleValue(allowed, normalized, reverse, settings.mainFloors());
    }

    public int nextAllowedTowerStackBasementFloorCount(String stackId, boolean reverse) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId);
        List<Integer> allowed = allowedTowerStackBasementFloorCounts(settings, settings.mainFloors());
        int normalized = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), settings.mainFloors());
        return cycleValue(allowed, normalized, reverse, settings.basementFloors());
    }

    public int wallHeight() {
        return draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorHeight)
                .orElse(7);
    }

    public MKWorkspaceLinearRunKind perimeterRunKind() {
        return draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::kind)
                .orElse(MKWorkspaceLinearRunKind.DEFENSIVE_WALL);
    }

    public void perimeterRunKind(MKWorkspaceLinearRunKind value) {
        draft().linearRunFamilies = draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithKind(linearRun, value) :
                        linearRun)
                .toList();
    }

    public void wallHeight(int value) {
        int height = Math.max(2, value);
        draft().linearRunFamilies = draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(), height,
                                Math.min(linearRun.topVoidMargin(), Math.max(0, height - 1))) :
                        linearRun)
                .toList();
    }

    public int wallTopVoidMargin() {
        return draft().linearRunFamilies.stream()
                .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::topVoidMargin)
                .orElse(0);
    }

    public void wallTopVoidMargin(int value) {
        int margin = Math.max(0, value);
        draft().linearRunFamilies = draft().linearRunFamilies.stream()
                .map(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()) ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(),
                                linearRun.interiorHeight(), Math.min(margin, Math.max(0, linearRun.interiorHeight() - 1))) :
                        linearRun)
                .toList();
    }

    public void resetTowerStackDefaults(String stackId) {
        replaceTowerStackSettings(MKWorkspaceTowerStackSettings.defaults(stackId, 7));
        applyTowerStackSettingsToFamilies();
    }

    public void resetTopologyPathDefaults(String topologyGroupId) {
        replaceTopologyPathSettings(MKWorkspaceTopologyPathSettings.defaultForTopologyGroup(topologyGroupId));
    }

    public void resetWalledKeepPerimeterDefaults() {
        Map<String, MKWorkspaceLinearRunFamilyDefinition> defaultsBySlot =
                MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(MKWorkspaceDimensions.defaultDimensions(),
                                draft().palette)
                        .stream()
                        .filter(linearRun -> isPerimeterTopologySlot(linearRun.topologySlotId()))
                        .collect(java.util.stream.Collectors.toMap(
                                MKWorkspaceLinearRunFamilyDefinition::topologySlotId,
                                linearRun -> linearRun,
                                (first, second) -> first,
                                java.util.LinkedHashMap::new));
        java.util.ArrayList<MKWorkspaceLinearRunFamilyDefinition> updated = new java.util.ArrayList<>();
        java.util.LinkedHashSet<String> resetSlots = new java.util.LinkedHashSet<>();
        for (MKWorkspaceLinearRunFamilyDefinition linearRun : draft().linearRunFamilies) {
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
        draft().linearRunFamilies = List.copyOf(updated);
    }

    public void resetCurrentTopologyDefaults() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig defaultStairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        draft().stairMode = defaultStairConfig.mode();
        draft().stairRiseType = defaultStairConfig.riseType();
        draft().stairWidth = defaultStairConfig.stairWidth();
        draft().verticalAccessPlacement = MKWorkspaceVerticalAccessSpec.defaultSpec().placement();
        draft().shaftSize = MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
        draft().mainFloors = MKWorkspaceTowerStackFloorCounts.DEFAULT_MAIN_FLOORS;
        draft().basementFloors = MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_FLOORS;
        draft().topCapApproachEnabled = MKWorkspaceTowerStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED;
        draft().basementCapApproachEnabled = MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED;
        draft().openingProfiles = MKHorizontalOpeningProfile.createDefaults(dimensions);
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            draft().topologyProfile = MKWorkspaceTopologyProfile.walledKeep(
                    draft().topologyProfile.uniqueNorthWestCornerTower(),
                    draft().topologyProfile.uniqueNorthEastCornerTower(),
                    draft().topologyProfile.uniqueSouthEastCornerTower(),
                    draft().topologyProfile.uniqueSouthWestCornerTower());
            draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions);
            draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions,
                    draft().palette);
        } else {
            draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions);
            draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions,
                    draft().palette);
            draft().topologyProfile = MKWorkspaceTopologyProfile.tower()
                    .withTowerStackSettings(towerPrimarySettingsFromDraft());
        }
        selectedFamilyIndex = -1;
        selectedFamilyExitIndex = -1;
        selectedOpeningIndex = -1;
        selectedLinearRunIndex = -1;
        snapDraftVerticalAccess();
    }

    public ResourceLocation floorBlock() {
        return draft().palette.floorBlock();
    }

    public void floorBlock(ResourceLocation value) {
        MKWorkspaceMaterialPalette palette = palette();
        palette(new MKWorkspaceMaterialPalette(value, palette.wallBlock(), palette.ceilingBlock(),
                palette.stairBlock(), palette.slabBlock(), palette.ladderBlock()));
    }

    public ResourceLocation wallBlock() {
        return draft().palette.wallBlock();
    }

    public void wallBlock(ResourceLocation value) {
        MKWorkspaceMaterialPalette palette = palette();
        palette(new MKWorkspaceMaterialPalette(palette.floorBlock(), value, palette.ceilingBlock(),
                palette.stairBlock(), palette.slabBlock(), palette.ladderBlock()));
    }

    public ResourceLocation ceilingBlock() {
        return draft().palette.ceilingBlock();
    }

    public void ceilingBlock(ResourceLocation value) {
        MKWorkspaceMaterialPalette palette = palette();
        palette(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(), value,
                palette.stairBlock(), palette.slabBlock(), palette.ladderBlock()));
    }

    public ResourceLocation stairBlock() {
        return draft().palette.stairBlock();
    }

    public void stairBlock(ResourceLocation value) {
        MKWorkspaceMaterialPalette palette = palette();
        palette(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                palette.ceilingBlock(), value, palette.slabBlock(), palette.ladderBlock()));
    }

    public ResourceLocation slabBlock() {
        return draft().palette.slabBlock();
    }

    public void slabBlock(ResourceLocation value) {
        MKWorkspaceMaterialPalette palette = palette();
        palette(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                palette.ceilingBlock(), palette.stairBlock(), value, palette.ladderBlock()));
    }

    public ResourceLocation ladderBlock() {
        return draft().palette.ladderBlock();
    }

    public void ladderBlock(ResourceLocation value) {
        MKWorkspaceMaterialPalette palette = palette();
        palette(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(), value));
    }

    public MKWorkspaceMaterialPalette palette() {
        return draft().palette;
    }

    public void palette(MKWorkspaceMaterialPalette value) {
        draft().palette = value;
    }

    public long familyCount(String topologySlotId) {
        return draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals(topologySlotId))
                .count();
    }

    public List<MKWorkspaceSlotSchema> roomTopologySlots() {
        MKWorkspaceTopologySchema schema = topologySchema();
        Map<String, String> regionKinds = schema.regions().stream()
                .collect(java.util.stream.Collectors.toMap(MKWorkspaceRegionSchema::regionId,
                        MKWorkspaceRegionSchema::regionKind));
        return schema.slots().stream()
                .filter(slot -> !"linear_run".equals(regionKinds.getOrDefault(slot.regionId(), "")))
                .filter(slot -> isActiveTopologySlot(slot.slotId()))
                .toList();
    }

    private boolean isActiveTopologySlot(String topologySlotId) {
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfileType())) {
            return true;
        }
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isEmpty()) {
            return true;
        }
        if ("keep.corner.shared".equals(cornerStackId.get())) {
            return draft().topologyProfile.anySharedCornerTower();
        }
        return draft().topologyProfile.uniqueCornerTower(cornerStackId.get());
    }

    public List<Integer> familyIndexesForTopologySlot(String topologySlotId) {
        List<MKTowerWorkspaceFamilyDefinition> families = draft().familyDefinitions;
        return IntStream.range(0, families.size())
                .filter(index -> families.get(index).topologySlotId().equals(topologySlotId))
                .boxed()
                .toList();
    }

    public int selectedFamilyIndex() {
        return selectedFamilyIndex;
    }

    public void selectedFamilyIndex(int index) {
        selectedFamilyIndex = index;
    }

    public int selectedFamilyExitIndex() {
        return selectedFamilyExitIndex;
    }

    public void selectedFamilyExitIndex(int index) {
        selectedFamilyExitIndex = index;
    }

    public int selectedOpeningIndex() {
        return selectedOpeningIndex;
    }

    public void selectedOpeningIndex(int index) {
        selectedOpeningIndex = index;
    }

    public int selectedLinearRunIndex() {
        return selectedLinearRunIndex;
    }

    public void selectedLinearRunIndex(int index) {
        selectedLinearRunIndex = index;
    }

    public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions() {
        return List.copyOf(draft().familyDefinitions);
    }

    public int addFamilyDefinition(MKWorkspaceSlotSchema slot) {
        Optional<MKTowerWorkspaceFamilyDefinition> source = draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals(slot.slotId()))
                .findFirst()
                .or(() -> sharedCornerSource(slot.slotId()));
        MKTowerWorkspaceFamilyDefinition family = source
                .map(existing -> copyFamilyForTopologySlot(existing, slot.slotId()))
                .orElseGet(() -> defaultFamilyForTopologySlot(slot));
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.add(family);
        draft().familyDefinitions = List.copyOf(updated);
        return draft().familyDefinitions.size() - 1;
    }

    public List<MKHorizontalOpeningProfile> openingProfiles() {
        return List.copyOf(draft().openingProfiles);
    }

    public void replaceOpeningProfile(int index, MKHorizontalOpeningProfile updatedProfile) {
        java.util.ArrayList<MKHorizontalOpeningProfile> updated = new java.util.ArrayList<>(draft().openingProfiles);
        updated.set(index, updatedProfile);
        draft().openingProfiles = List.copyOf(updated);
    }

    public void removeOpeningProfile(int index) {
        java.util.ArrayList<MKHorizontalOpeningProfile> updated = new java.util.ArrayList<>(draft().openingProfiles);
        updated.remove(index);
        draft().openingProfiles = List.copyOf(updated);
    }

    public int addOpeningProfile() {
        java.util.ArrayList<MKHorizontalOpeningProfile> updated = new java.util.ArrayList<>(draft().openingProfiles);
        updated.add(new MKHorizontalOpeningProfile(nextUniqueOpeningProfileId(), 3, 3, false, true));
        draft().openingProfiles = List.copyOf(updated);
        return draft().openingProfiles.size() - 1;
    }

    public List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies() {
        return List.copyOf(draft().linearRunFamilies);
    }

    public void replaceLinearRunFamily(int index, MKWorkspaceLinearRunFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKWorkspaceLinearRunFamilyDefinition> updated =
                new java.util.ArrayList<>(draft().linearRunFamilies);
        updated.set(index, updatedFamily);
        draft().linearRunFamilies = List.copyOf(updated);
    }

    public void removeLinearRunFamily(int index) {
        java.util.ArrayList<MKWorkspaceLinearRunFamilyDefinition> updated =
                new java.util.ArrayList<>(draft().linearRunFamilies);
        updated.remove(index);
        draft().linearRunFamilies = List.copyOf(updated);
    }

    public int addLinearRunFamily() {
        String openingProfileId = firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind.BRANCH)
                .orElseGet(() -> draft().openingProfiles.isEmpty() ? "branch_opening" : draft().openingProfiles.getFirst().profileId());
        java.util.ArrayList<MKWorkspaceLinearRunFamilyDefinition> updated =
                new java.util.ArrayList<>(draft().linearRunFamilies);
        updated.add(new MKWorkspaceLinearRunFamilyDefinition(
                nextUniqueLinearRunFamilyId(),
                MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                openingProfileId,
                5,
                3,
                3,
                0,
                false,
                true,
                MKWorkspaceLinearRunProjection.RIGID,
                List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                MKWorkspaceFoundationPolicy.none(),
                null
        ));
        draft().linearRunFamilies = List.copyOf(updated);
        return draft().linearRunFamilies.size() - 1;
    }

    public MKWorkspaceMaterialPalette basePalette() {
        return palette();
    }

    public MKWorkspaceMaterialPalette draftBasePalette() {
        return basePalette();
    }

    public MKWorkspaceMaterialPalette resolveTopologyGroupPalette(String topologyGroupId) {
        return draftBasePalette();
    }

    public MKWorkspaceMaterialPalette resolveFamilyInheritedPalette(MKTowerWorkspaceFamilyDefinition family) {
        MKWorkspaceMaterialPalette topologyGroupPalette = resolveTopologyGroupPalette(family.slotMetadata().topologyGroupId());
        String stackId = stackIdForFamily(family);
        if (stackId.isBlank()) {
            return topologyGroupPalette;
        }
        return towerStackSettings(stackId).paletteOverrideOpt()
                .map(override -> override.resolve(topologyGroupPalette))
                .orElse(topologyGroupPalette);
    }

    public MKWorkspaceFoundationPolicy resolveFamilyInheritedFoundation(MKTowerWorkspaceFamilyDefinition family) {
        String stackId = stackIdForFamily(family);
        if (stackId.isBlank()) {
            return MKWorkspaceFoundationPolicy.none();
        }
        return towerStackSettings(stackId).foundationPolicy();
    }

    public boolean familyHasTopologyStack(MKTowerWorkspaceFamilyDefinition family) {
        return towerStackIdForTopologySlot(family.topologySlotId()).isPresent();
    }

    public MKStructureWorkspace buildWorkspaceDraft() {
        snapDraftVerticalAccess();
        MKWorkspaceDimensions dimensions = legacyDimensionsFromTopologySettings();
        MKWorkspaceMaterialPalette palette = basePalette();
        MKWorkspaceStairAuthoringConfig stairConfig = makeStairConfig();
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = new MKWorkspaceVerticalAccessSpec(draft().shaftSize,
                draft().verticalAccessPlacement, stairConfig);
        long now = System.currentTimeMillis();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                screen.anchor(),
                draft().namespace.trim(),
                draft().structureName.trim(),
                draft().topologyProfile,
                dimensions,
                palette,
                stairConfig,
                draft().verticalAccessPlacement,
                draft().shellMargin,
                draft().exteriorAirMargin,
                draft().previewMargin,
                verticalAccessSpec,
                draft().familyDefinitions,
                draft().openingProfiles,
                draft().linearRunFamilies,
                now,
                now,
                List.of()
        );
    }

    public void snapDraftVerticalAccess() {
        int[] footprint = getDraftVerticalAccessFootprint();
        draft().shaftSize = MKWorkspaceDimensions.snapToNearestUsableShaftSize(makeStairConfig(),
                footprint[0], footprint[1], draft().shaftSize, 3);
        draft().stairWidth = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(draft().shaftSize, draft().stairWidth);
        draft().mainFloors = normalizeMainFloorCount(draft().mainFloors, draft().basementFloors);
        draft().basementFloors = normalizeBasementFloorCount(draft().basementFloors, draft().mainFloors);
        draft().mainFloors = normalizeMainFloorCount(draft().mainFloors, draft().basementFloors);
        draft().familyDefinitions = draft().familyDefinitions.stream()
                .map(this::normalizeFamilyDefinition)
                .toList();
        applyTowerStackSettingsToFamilies();
    }

    public MKWorkspaceTopologyPathSettings topologyPathSettings(String topologyGroupId) {
        return draft().topologyProfile.pathSettingsOrDefault(topologyGroupId);
    }

    public void topologyPathMinMainPathPieces(String topologyGroupId, int value) {
        replaceTopologyPathSettings(topologyPathSettings(topologyGroupId).withMinMainPathPieces(value));
    }

    public void topologyPathMaxMainPathPieces(String topologyGroupId, int value) {
        replaceTopologyPathSettings(topologyPathSettings(topologyGroupId).withMaxMainPathPieces(value));
    }

    public void topologyPathMaxBranchPiecesBeforeCap(String topologyGroupId, int value) {
        replaceTopologyPathSettings(topologyPathSettings(topologyGroupId).withMaxBranchPiecesBeforeCap(value));
    }

    private void replaceTopologyPathSettings(MKWorkspaceTopologyPathSettings settings) {
        draft().topologyProfile = draft().topologyProfile.withPathSettings(settings);
    }

    public void replaceFamilyDefinition(int index, MKTowerWorkspaceFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.set(index, normalizeFamilyDefinition(preserveFamilyMetadata(updated.get(index), updatedFamily)));
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void replaceFamilyTopologySlotId(int index, String topologySlotId) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(), topologySlotMetadata(topologySlotId, family.slotMetadata()),
                family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                family.foundationPolicyOverride(), family.paletteOverride()));
    }

    public void replaceFamilyVerticalAccessGroupId(int index, String verticalAccessGroupId) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(),
                verticalAccessGroupId, family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                family.foundationPolicyOverride(), family.paletteOverride()));
    }

    public void replaceFamilyFoundationPolicy(int index, MKWorkspaceFoundationPolicy foundationPolicy) {
        replaceFamilyFoundationPolicyOverride(index, Optional.of(foundationPolicy));
    }

    public void replaceFamilyFoundationPolicyOverride(int index, Optional<MKWorkspaceFoundationPolicy> foundationPolicy) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(),
                family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                foundationPolicy.orElse(null), family.paletteOverride()));
    }

    private void replaceFamilyDefinitionExact(int index, MKTowerWorkspaceFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.set(index, normalizeFamilyDefinition(updatedFamily));
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void removeFamilyDefinition(int index) {
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.remove(index);
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void replaceFamilyExit(int familyIndex, int exitIndex, MKWorkspaceFamilyHorizontalExitDefinition updatedExit) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        exits.set(exitIndex, updatedExit);
        if (!updatedExit.isVerticalAccess() && updatedExit.pathKind() != MKWorkspaceHorizontalExitPathKind.BRANCH) {
            for (int i = 0; i < exits.size(); i++) {
                if (i == exitIndex) {
                    continue;
                }
                MKWorkspaceFamilyHorizontalExitDefinition existingExit = exits.get(i);
                if (existingExit.isVerticalAccess()) {
                    continue;
                }
                if (existingExit.pathKind() == updatedExit.pathKind()) {
                    exits.set(i, new MKWorkspaceFamilyHorizontalExitDefinition(
                            existingExit.direction(),
                            MKWorkspaceHorizontalExitPathKind.BRANCH,
                            ensureCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind.BRANCH,
                                    existingExit.openingProfileId()),
                            existingExit.connectionMode(),
                            existingExit.sideOffset(),
                            existingExit.verticalOffset()
                    ));
                }
            }
        }
        replaceFamilyDefinition(familyIndex, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
    }

    public void updateFamilyExitOffsets(int familyIndex, int exitIndex, Integer sideOffset, Integer verticalOffset) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        MKWorkspaceFamilyHorizontalExitDefinition currentExit = family.horizontalExits().get(exitIndex);
        int nextSideOffset = sideOffset == null ? currentExit.sideOffset() : sideOffset;
        int nextVerticalOffset = verticalOffset == null ? currentExit.verticalOffset() : verticalOffset;
        replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                currentExit.direction(),
                currentExit.pathKind(),
                currentExit.openingProfileId(),
                currentExit.connectionMode(),
                clampSideOffset(family, currentExit.direction(), currentExit.openingProfileId(), nextSideOffset),
                clampVerticalOffset(family, currentExit.openingProfileId(), nextVerticalOffset)
        ));
    }

    public void removeFamilyExit(int familyIndex, int exitIndex) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        exits.remove(exitIndex);
        replaceFamilyDefinition(familyIndex, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
    }

    public int addFamilyExitAtDirection(int familyIndex, Direction direction) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        if (direction.getAxis().isVertical()) {
            exits.add(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(direction));
            replaceFamilyDefinition(familyIndex, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                    family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                    family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                    0, 0, family.paletteOverride()
            ));
            return exits.size() - 1;
        }
        MKWorkspaceHorizontalExitPathKind pathKind = !family.mainExit().isPresent() ?
                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT :
                !family.mainEntry().isPresent() ? MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY :
                        MKWorkspaceHorizontalExitPathKind.BRANCH;
        exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                pathKind,
                firstCompatibleOpeningProfileId(pathKind)
                        .orElseGet(() -> draft().openingProfiles.isEmpty() ? "opening_1" : draft().openingProfiles.getFirst().profileId())
        ));
        replaceFamilyDefinition(familyIndex, MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
        return exits.size() - 1;
    }

    public int findFamilyExitIndexByDirection(int familyIndex, Direction direction) {
        List<MKWorkspaceFamilyHorizontalExitDefinition> exits = draft().familyDefinitions.get(familyIndex).horizontalExits();
        for (int i = 0; i < exits.size(); i++) {
            if (exits.get(i).direction() == direction) {
                return i;
            }
        }
        return -1;
    }

    private void send(MKStructureWorkspace draft) {
        PacketDistributor.sendToServer(new CreateWorkspacePacket(draft, true));
    }

    private boolean requiresDestructiveRegenerateConfirmation(MKStructureWorkspace draft) {
        MKStructureWorkspace workspace = screen.workspace();
        if (workspace == null || workspace.pieces().isEmpty()) {
            return false;
        }
        return !canApplySafeLiveMutation(workspace, draft);
    }

    private boolean canApplySafeLiveMutation(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        return canRelayoutPreviewMarginOnly(existing, requested) ||
                canSwapPaletteOnly(existing, requested) ||
                canRenameIdentityOnly(existing, requested) ||
                canExpandMarginsOnly(existing, requested);
    }

    private boolean canRelayoutPreviewMarginOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (existing.previewMargin() == requested.previewMargin()) {
            return false;
        }
        return settingsComparisonTag(existing, existing.id(), requested.previewMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin()));
    }

    private boolean canSwapPaletteOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        MKStructureWorkspace existingWithRequestedMaterials = withMaterialSettings(existing, requested);
        if (!canSwapMaterialPalettesWithoutRoleAmbiguity(existing, existingWithRequestedMaterials)) {
            return false;
        }
        if (settingsComparisonTag(existing, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(existingWithRequestedMaterials, existing.id(), existing.previewMargin()))) {
            return false;
        }
        return settingsComparisonTag(existingWithRequestedMaterials, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin()));
    }

    private boolean canSwapMaterialPalettesWithoutRoleAmbiguity(MKStructureWorkspace existing,
                                                                MKStructureWorkspace requested) {
        MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();
        for (MKWorkspacePieceDefinition piece : existing.pieces()) {
            MKWorkspaceMaterialPalette sourcePalette = paletteResolver.resolvePiece(existing, piece)
                    .orElse(existing.palette());
            MKWorkspaceMaterialPalette targetPalette = paletteResolver.resolvePiece(requested, piece)
                    .orElse(requested.palette());
            if (!MKWorkspacePaletteSwapSafety.canRepresentAsBlockReplacement(sourcePalette, targetPalette)) {
                return false;
            }
        }
        return true;
    }

    private boolean canRenameIdentityOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        boolean identityChanged = !existing.namespace().equals(requested.namespace()) ||
                !existing.structureName().equals(requested.structureName());
        if (!identityChanged) {
            return false;
        }
        return settingsComparisonTag(existing, existing.id(), existing.previewMargin(), existing.palette(),
                requested.namespace(), requested.structureName())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin(),
                        requested.palette(), requested.namespace(), requested.structureName()));
    }

    private boolean canExpandMarginsOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        boolean marginChanged = existing.shellMargin() != requested.shellMargin() ||
                existing.exteriorAirMargin() != requested.exteriorAirMargin();
        if (!marginChanged || requested.shellMargin() < existing.shellMargin() ||
                requested.exteriorAirMargin() < existing.exteriorAirMargin()) {
            return false;
        }
        return settingsComparisonTag(existing, existing.id(), existing.previewMargin(), existing.palette(),
                existing.namespace(), existing.structureName(), requested.shellMargin(), requested.exteriorAirMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin(),
                        requested.palette(), requested.namespace(), requested.structureName(),
                        requested.shellMargin(), requested.exteriorAirMargin()));
    }

    private CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, UUID id, int previewMargin) {
        return settingsComparisonTag(workspace, id, previewMargin, workspace.palette());
    }

    private CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, UUID id, int previewMargin,
                                              MKWorkspaceMaterialPalette palette) {
        return settingsComparisonTag(workspace, id, previewMargin, palette, workspace.namespace(),
                workspace.structureName());
    }

    private CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, UUID id, int previewMargin,
                                              MKWorkspaceMaterialPalette palette, String namespace,
                                              String structureName) {
        return settingsComparisonTag(workspace, id, previewMargin, palette, namespace, structureName,
                workspace.shellMargin(), workspace.exteriorAirMargin());
    }

    private CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, UUID id, int previewMargin,
                                              MKWorkspaceMaterialPalette palette, String namespace,
                                              String structureName, int shellMargin, int exteriorAirMargin) {
        return new MKStructureWorkspace(
                id,
                workspace.anchor(),
                namespace,
                structureName,
                workspace.topologyProfile(),
                workspace.dimensions(),
                palette,
                alignStairMaterials(workspace.stairConfig(), palette),
                workspace.verticalAccessPlacement(),
                shellMargin,
                exteriorAirMargin,
                previewMargin,
                alignVerticalAccessMaterials(workspace.verticalAccessSpec(), palette),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                0,
                0,
                List.of()
        ).toTag();
    }

    private MKStructureWorkspace withMaterialSettings(MKStructureWorkspace source, MKStructureWorkspace materialSource) {
        return new MKStructureWorkspace(
                source.id(),
                source.anchor(),
                source.namespace(),
                source.structureName(),
                withMaterialStackSettings(source.topologyProfile(), materialSource.topologyProfile()),
                source.dimensions(),
                materialSource.palette(),
                alignStairMaterials(source.stairConfig(), materialSource.palette()),
                source.verticalAccessPlacement(),
                source.shellMargin(),
                source.exteriorAirMargin(),
                source.previewMargin(),
                alignVerticalAccessMaterials(source.verticalAccessSpec(), materialSource.palette()),
                source.familyDefinitions().stream()
                        .map(family -> materialSource.familyDefinitions().stream()
                                .filter(requested -> requested.baseName().equals(family.baseName()))
                                .findFirst()
                                .map(requested -> copyFamilyDefinition(family, requested.paletteOverrideOpt()))
                                .orElse(family))
                        .toList(),
                source.openingProfiles(),
                source.linearRunFamilies().stream()
                        .map(linearRun -> materialSource.linearRunFamilies().stream()
                                .filter(requested -> requested.linearRunId().equals(linearRun.linearRunId()))
                                .findFirst()
                                .map(requested -> copyLinearRunFamily(linearRun, requested.paletteOverrideOpt()))
                                .orElse(linearRun))
                        .toList(),
                source.createdAt(),
                source.updatedAt(),
                source.pieces()
        );
    }

    private MKWorkspaceStairAuthoringConfig alignStairMaterials(MKWorkspaceStairAuthoringConfig stairConfig,
                                                                MKWorkspaceMaterialPalette palette) {
        return new MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.stairWidth(),
                palette.stairBlock(),
                palette.slabBlock(),
                palette.ladderBlock()
        );
    }

    private MKWorkspaceVerticalAccessSpec alignVerticalAccessMaterials(MKWorkspaceVerticalAccessSpec spec,
                                                                       MKWorkspaceMaterialPalette palette) {
        return new MKWorkspaceVerticalAccessSpec(
                spec.shaftSize(),
                spec.placement(),
                alignStairMaterials(spec.stairConfig(), palette)
        );
    }

    private MKWorkspaceStairAuthoringConfig makeStairConfig() {
        return new MKWorkspaceStairAuthoringConfig(
                draft().stairMode,
                draft().stairRiseType,
                draft().stairWidth,
                draft().palette.stairBlock(),
                draft().palette.slabBlock(),
                draft().palette.ladderBlock()
        );
    }

    private int deriveDoorwayWidth() {
        return getOpeningProfile("main_opening")
                .or(() -> firstCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT))
                .map(MKHorizontalOpeningProfile::openingWidth)
                .orElse(screen.workspace() != null ? screen.workspace().dimensions().doorwayWidth() : 3);
    }

    private int deriveDoorwayHeight() {
        return getOpeningProfile("main_opening")
                .or(() -> firstCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT))
                .map(MKHorizontalOpeningProfile::openingHeight)
                .orElse(screen.workspace() != null ? screen.workspace().dimensions().doorwayHeight() : 3);
    }

    private MKWorkspaceDimensions legacyDimensionsFromTopologySettings() {
        MKWorkspaceTowerStackSettings primaryStack = primaryDimensionStackSettings();
        return new MKWorkspaceDimensions(
                primaryStack.width(),
                primaryStack.length(),
                primaryStack.height(),
                primaryStack.height(),
                primaryStack.height(),
                primaryStack.shaftSize(),
                deriveDoorwayWidth(),
                deriveDoorwayHeight()
        );
    }

    private MKWorkspaceTowerStackSettings primaryDimensionStackSettings() {
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfileType())) {
            return towerStackSettings("keep.center");
        }
        return towerStackSettings(TOWER_PRIMARY_STACK_ID);
    }

    public Optional<MKHorizontalOpeningProfile> getOpeningProfile(String profileId) {
        return draft().openingProfiles.stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst();
    }

    private MKWorkspaceVerticalAccessSpec currentVerticalAccessSpec() {
        return new MKWorkspaceVerticalAccessSpec(draft().shaftSize, draft().verticalAccessPlacement, makeStairConfig());
    }

    private static MKWorkspaceTowerStackSettings primaryStackSettings(MKWorkspaceTopologyProfile profile) {
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(profile.profileType())) {
            return profile.towerStackSettingsOrDefault("keep.center");
        }
        return profile.towerStackSettingsOrDefault(TOWER_PRIMARY_STACK_ID);
    }

    public int[] verticalAccessFootprint() {
        MKWorkspaceTowerStackSettings settings = primaryDimensionStackSettings();
        return new int[]{settings.width(), settings.length()};
    }

    private int[] getDraftVerticalAccessFootprint() {
        return verticalAccessFootprint();
    }

    public List<Integer> allowedMainFloorCounts(int basementFloors) {
        MKWorkspaceTowerStackSettings settings = primaryDimensionStackSettings()
                .withBasementFloors(basementFloors);
        return allowedTowerStackMainFloorCounts(settings, basementFloors);
    }

    public List<Integer> allowedBasementFloorCounts(int mainFloors) {
        MKWorkspaceTowerStackSettings settings = primaryDimensionStackSettings()
                .withMainFloors(mainFloors);
        return allowedTowerStackBasementFloorCounts(settings, mainFloors);
    }

    public int nextAllowedMainFloorCount(int currentCount, int basementFloors, boolean reverse) {
        List<Integer> allowedCounts = allowedMainFloorCounts(basementFloors);
        int normalized = normalizeMainFloorCount(currentCount, basementFloors);
        return cycleValue(allowedCounts, normalized, reverse, currentCount);
    }

    public int nextAllowedBasementFloorCount(int currentCount, int mainFloors, boolean reverse) {
        List<Integer> allowedCounts = allowedBasementFloorCounts(mainFloors);
        int normalized = normalizeBasementFloorCount(currentCount, mainFloors);
        return cycleValue(allowedCounts, normalized, reverse, currentCount);
    }

    public int normalizeMainFloorCount(int requestedCount, int basementFloors) {
        List<Integer> allowedCounts = allowedMainFloorCounts(basementFloors);
        return allowedCounts.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    public int normalizeBasementFloorCount(int requestedCount, int mainFloors) {
        List<Integer> allowedCounts = allowedBasementFloorCounts(mainFloors);
        return allowedCounts.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    private MKWorkspaceTowerStackSettings towerStackSettings(String stackId) {
        MKWorkspaceTowerStackSettings settings = TOWER_PRIMARY_STACK_ID.equals(stackId) ?
                draft().topologyProfile.towerStackSettings(stackId).orElseGet(this::towerPrimarySettingsFromDraft) :
                draft().topologyProfile.towerStackSettingsOrDefault(stackId);
        if (draft().topologyProfile.towerStackSettings(stackId).isEmpty()) {
            replaceTowerStackSettings(settings);
        }
        return settings;
    }

    private void replaceTowerStackSettings(MKWorkspaceTowerStackSettings settings) {
        draft().topologyProfile = draft().topologyProfile.withTowerStackSettings(settings);
        if (TOWER_PRIMARY_STACK_ID.equals(settings.stackId())) {
            draft().mainFloors = settings.mainFloors();
            draft().basementFloors = settings.basementFloors();
            draft().topCapApproachEnabled = settings.topCapApproachEnabled();
            draft().basementCapApproachEnabled = settings.basementCapApproachEnabled();
            draft().shaftSize = settings.shaftSize();
            draft().verticalAccessPlacement = settings.verticalAccessPlacement();
            draft().stairMode = settings.stairConfig().mode();
            draft().stairRiseType = settings.stairConfig().riseType();
            draft().stairWidth = settings.stairConfig().stairWidth();
        }
    }

    private MKWorkspaceTowerStackSettings towerPrimarySettingsFromDraft() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        return new MKWorkspaceTowerStackSettings(
                TOWER_PRIMARY_STACK_ID,
                draft().mainFloors,
                draft().basementFloors,
                dimensions.roomHeight(),
                dimensions.roomWidth(),
                dimensions.roomLength(),
                draft().shaftSize,
                draft().verticalAccessPlacement,
                makeStairConfig(),
                draft().topCapApproachEnabled,
                draft().basementCapApproachEnabled
        );
    }

    private void replaceTowerStackStairConfig(String stackId, MKWorkspaceStairAuthoringConfig stairConfig) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId).withStairConfig(stairConfig);
        int normalizedMain = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), settings.basementFloors());
        int normalizedBasement = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), normalizedMain);
        replaceTowerStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    private List<Integer> allowedTowerStackMainFloorCounts(MKWorkspaceTowerStackSettings settings, int basementFloors) {
        return MKWorkspaceTowerStackFloorCounts.allowedMainFloorCounts(MKTowerStackBudget.fromStackSettings(settings),
                basementFloors, settings.topCapApproachEnabled(), settings.basementCapApproachEnabled());
    }

    private List<Integer> allowedTowerStackBasementFloorCounts(MKWorkspaceTowerStackSettings settings, int mainFloors) {
        return MKWorkspaceTowerStackFloorCounts.allowedBasementFloorCounts(MKTowerStackBudget.fromStackSettings(settings),
                mainFloors, settings.topCapApproachEnabled(), settings.basementCapApproachEnabled());
    }

    private int normalizeTowerStackMainFloorCount(MKWorkspaceTowerStackSettings settings, int requestedCount,
                                                  int basementFloors) {
        return allowedTowerStackMainFloorCounts(settings, basementFloors).stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    private int normalizeTowerStackBasementFloorCount(MKWorkspaceTowerStackSettings settings, int requestedCount,
                                                      int mainFloors) {
        return allowedTowerStackBasementFloorCounts(settings, mainFloors).stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    public MKTowerWorkspaceFamilyDefinition normalizeFamilyDefinition(MKTowerWorkspaceFamilyDefinition family) {
        Optional<String> towerStackId = towerStackIdForTopologySlot(family.topologySlotId());
        int maxRoomHeight = maxRoomHeightForFamilyNormalization(towerStackId);
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = verticalAccessSpecForFamilyNormalization(towerStackId);
        boolean stackBacked = towerStackId.isPresent();
        int roomWidth = stackBacked && family.roomWidth() <= 0 ? 0 :
                normalizeFamilyWidth(family.roomWidth(), family.supportsVerticalAccess(), verticalAccessSpec);
        int roomLength = stackBacked && family.roomLength() <= 0 ? 0 :
                normalizeFamilyLength(family.roomLength(), family.supportsVerticalAccess(), verticalAccessSpec);
        int roomHeight = stackBacked && family.roomHeight() <= 0 ? 0 :
                normalizeFamilyHeight(family.roomHeight(), family.supportsVerticalAccess(), maxRoomHeight);
        int resolvedRoomWidth = roomWidth > 0 ? roomWidth :
                towerStackId.map(stackId -> towerStackSettings(stackId).width()).orElse(roomWidth);
        int resolvedRoomLength = roomLength > 0 ? roomLength :
                towerStackId.map(stackId -> towerStackSettings(stackId).length()).orElse(roomLength);
        int resolvedRoomHeight = roomHeight > 0 ? roomHeight :
                towerStackId.map(stackId -> towerStackSettings(stackId).height()).orElse(roomHeight);
        int availableVoidMargin = Math.max(0, resolvedRoomHeight - MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
        int topVoidMargin = family.supportsVerticalAccess() ? 0 :
                clamp(family.topVoidMargin(), 0, availableVoidMargin);
        int bottomVoidMargin = family.supportsVerticalAccess() ? 0 :
                clamp(family.bottomVoidMargin(), 0, availableVoidMargin - topVoidMargin);
        MKTowerWorkspaceFamilyDefinition normalizedGeometry = MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                resolvedRoomWidth,
                resolvedRoomLength,
                resolvedRoomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                topVoidMargin,
                bottomVoidMargin,
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                roomWidth,
                roomLength,
                roomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits().stream()
                        .map(exit -> exit.isVerticalAccess() ?
                                MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(exit.direction()) :
                                new MKWorkspaceFamilyHorizontalExitDefinition(
                                        exit.direction(),
                                        exit.pathKind(),
                                        exit.openingProfileId(),
                                        exit.connectionMode(),
                                        clampSideOffset(normalizedGeometry, exit.direction(), exit.openingProfileId(), exit.sideOffset()),
                                        clampVerticalOffset(normalizedGeometry, exit.openingProfileId(), exit.verticalOffset())
                                ))
                        .toList(),
                topVoidMargin,
                bottomVoidMargin,
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private int maxRoomHeightForFamilyNormalization(Optional<String> towerStackId) {
        if (towerStackId.isEmpty()) {
            return primaryDimensionStackSettings().height();
        }
        return towerStackSettings(towerStackId.get()).height();
    }

    private MKWorkspaceVerticalAccessSpec verticalAccessSpecForFamilyNormalization(Optional<String> towerStackId) {
        if (towerStackId.isEmpty()) {
            return currentVerticalAccessSpec();
        }
        MKWorkspaceTowerStackSettings settings = towerStackSettings(towerStackId.get());
        return new MKWorkspaceVerticalAccessSpec(settings.shaftSize(), settings.verticalAccessPlacement(),
                settings.stairConfig());
    }

    private int normalizeFamilyWidth(int requestedWidth, boolean supportsVerticalAccess,
                                     MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        int width = Math.max(3, makeOdd(requestedWidth));
        return supportsVerticalAccess ? Math.max(width, verticalAccessSpec.shaftSize()) : width;
    }

    private int normalizeFamilyLength(int requestedLength, boolean supportsVerticalAccess,
                                      MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        int length = Math.max(3, makeOdd(requestedLength));
        return supportsVerticalAccess ? Math.max(length, verticalAccessSpec.shaftSize()) : length;
    }

    public int normalizeFamilyWidthForTopologySlot(int requestedWidth, boolean supportsVerticalAccess) {
        return normalizeFamilyWidth(requestedWidth, supportsVerticalAccess, currentVerticalAccessSpec());
    }

    public int normalizeFamilyLengthForTopologySlot(int requestedLength, boolean supportsVerticalAccess) {
        return normalizeFamilyLength(requestedLength, supportsVerticalAccess, currentVerticalAccessSpec());
    }

    public int normalizeFamilyHeight(int requestedHeight, boolean supportsVerticalAccess, int maxRoomHeight) {
        if (supportsVerticalAccess) {
            return maxRoomHeight;
        }
        return Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, Math.min(requestedHeight, maxRoomHeight));
    }

    private void seedDefaultsForTopology() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            migrateWalledKeepPerimeterLinearRuns();
            boolean hasKeepFamilies = draft().familyDefinitions.stream()
                    .anyMatch(family -> family.topologySlotId().startsWith("keep."));
            if (!hasKeepFamilies) {
                draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions);
            }
            ensureFamiliesForActiveCornerSlots();
            boolean hasKeepLinearRuns = draft().linearRunFamilies.stream()
                    .anyMatch(linearRun -> linearRun.topologySlotId().startsWith("keep."));
            if (!hasKeepLinearRuns) {
                draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions,
                        draft().palette);
            }
            return;
        }
        boolean hasTowerFamilies = draft().familyDefinitions.stream()
                .anyMatch(family -> family.topologySlotId().startsWith("tower."));
        if (!hasTowerFamilies) {
            draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions);
        }
        towerStackSettings(TOWER_PRIMARY_STACK_ID);
        applyTowerStackSettingsToFamilies();
        boolean hasTowerLinearRuns = draft().linearRunFamilies.stream()
                .anyMatch(linearRun -> linearRun.topologySlotId().startsWith("tower."));
        if (!hasTowerLinearRuns) {
            draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions, draft().palette);
        }
    }

    private void ensureFamiliesForActiveCornerSlots() {
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            return;
        }
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated =
                new java.util.ArrayList<>(draft().familyDefinitions);
        if (draft().topologyProfile.anySharedCornerTower()) {
            ensureFamiliesForTowerStack(updated, "keep.corner.shared");
        }
        for (String cornerSlot : KEEP_CORNER_STACK_IDS) {
            if (!draft().topologyProfile.uniqueCornerTower(cornerSlot)) {
                continue;
            }
            ensureFamiliesForTowerStack(updated, cornerSlot);
        }
        draft().familyDefinitions = List.copyOf(updated);
        applyTowerStackSettingsToFamilies();
    }

    private void ensureFamiliesForTowerStack(List<MKTowerWorkspaceFamilyDefinition> updated, String stackId) {
        topologySchema().slots().stream()
                .filter(slot -> slot.slotId().startsWith(stackId + "."))
                .filter(slot -> updated.stream().noneMatch(family -> family.topologySlotId().equals(slot.slotId())))
                .map(slot -> sharedCornerSource(slot.slotId())
                        .map(existing -> copyFamilyForTopologySlot(existing, slot.slotId()))
                        .orElseGet(() -> defaultFamilyForTopologySlot(slot)))
                .forEach(updated::add);
    }

    private void applyTowerStackSettingsToFamilies() {
        // Stack-backed families inherit stack geometry at resolution time. Existing explicit overrides are preserved.
    }

    private boolean topologyHasTowerStacks() {
        return !draft().topologyProfile.towerStackSettings().isEmpty();
    }

    private String stackIdForFamily(MKTowerWorkspaceFamilyDefinition family) {
        return towerStackIdForTopologySlot(family.topologySlotId()).orElse("");
    }

    private void migrateWalledKeepPerimeterLinearRuns() {
        java.util.LinkedHashMap<String, MKWorkspaceLinearRunFamilyDefinition> convertedByPerimeterSlot =
                new java.util.LinkedHashMap<>();
        java.util.ArrayList<MKWorkspaceLinearRunFamilyDefinition> current = new java.util.ArrayList<>();
        java.util.HashSet<String> existingPerimeterSlots = new java.util.HashSet<>();
        for (MKWorkspaceLinearRunFamilyDefinition linearRun : draft().linearRunFamilies) {
            String perimeterSlot = legacyPerimeterSlot(linearRun.topologySlotId());
            if (perimeterSlot == null) {
                current.add(linearRun);
                if (isPerimeterTopologySlot(linearRun.topologySlotId())) {
                    existingPerimeterSlots.add(linearRun.topologySlotId());
                }
                continue;
            }
            MKWorkspaceLinearRunFamilyDefinition converted =
                    copyPerimeterLinearRun(linearRun, perimeterSlot);
            convertedByPerimeterSlot.merge(perimeterSlot, converted, this::preferPerimeterLinearRun);
        }
        for (Map.Entry<String, MKWorkspaceLinearRunFamilyDefinition> entry : convertedByPerimeterSlot.entrySet()) {
            if (!existingPerimeterSlots.contains(entry.getKey())) {
                current.add(entry.getValue());
                existingPerimeterSlots.add(entry.getKey());
            }
        }
        draft().linearRunFamilies = List.copyOf(current);
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

    private MKTowerWorkspaceFamilyDefinition copyFamilyWithGeometry(MKTowerWorkspaceFamilyDefinition family,
                                                                    int roomWidth, int roomLength, int roomHeight) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
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

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunWithTopologyAndHeight(
            MKWorkspaceLinearRunFamilyDefinition linearRun, String topologySlotId, int interiorHeight) {
        return copyLinearRunWithTopologyHeightAndTopVoid(linearRun, topologySlotId, interiorHeight,
                Math.min(linearRun.topVoidMargin(), Math.max(0, interiorHeight - 1)));
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

    public int clampSideOffset(MKTowerWorkspaceFamilyDefinition family, Direction direction, String openingProfileId,
                               int sideOffset) {
        return clamp(sideOffset, minSideOffset(family, direction, openingProfileId),
                maxSideOffset(family, direction, openingProfileId));
    }

    public int clampVerticalOffset(MKTowerWorkspaceFamilyDefinition family, String openingProfileId,
                                   int verticalOffset) {
        return clamp(verticalOffset, 0, maxVerticalOffset(family, openingProfileId));
    }

    public int minSideOffset(MKTowerWorkspaceFamilyDefinition family, Direction direction, String openingProfileId) {
        int sideLength = getExitSideLength(family, direction);
        int halfOpening = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingWidth)
                .orElse(1) / 2;
        return halfOpening - (sideLength / 2);
    }

    public int maxSideOffset(MKTowerWorkspaceFamilyDefinition family, Direction direction, String openingProfileId) {
        int sideLength = getExitSideLength(family, direction);
        int halfOpening = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingWidth)
                .orElse(1) / 2;
        return (sideLength - 1 - halfOpening) - (sideLength / 2);
    }

    private int getExitSideLength(MKTowerWorkspaceFamilyDefinition family, Direction direction) {
        return direction == Direction.NORTH || direction == Direction.SOUTH ?
                resolvedFamilyRoomWidth(family) : resolvedFamilyRoomLength(family);
    }

    public int maxVerticalOffset(MKTowerWorkspaceFamilyDefinition family, String openingProfileId) {
        int openingHeight = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingHeight)
                .orElse(1);
        return Math.max(0, resolvedFamilyRoomHeight(family) - openingHeight);
    }

    public int resolvedFamilyRoomWidth(MKTowerWorkspaceFamilyDefinition family) {
        if (family.roomWidth() > 0) {
            return family.roomWidth();
        }
        return towerStackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> towerStackSettings(stackId).width())
                .orElse(family.roomWidth());
    }

    public int resolvedFamilyRoomLength(MKTowerWorkspaceFamilyDefinition family) {
        if (family.roomLength() > 0) {
            return family.roomLength();
        }
        return towerStackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> towerStackSettings(stackId).length())
                .orElse(family.roomLength());
    }

    public int resolvedFamilyRoomHeight(MKTowerWorkspaceFamilyDefinition family) {
        if (family.roomHeight() > 0) {
            return family.roomHeight();
        }
        return towerStackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> towerStackSettings(stackId).height())
                .orElse(family.roomHeight());
    }

    private List<MKWorkspaceFamilyHorizontalExitDefinition> defaultHorizontalExitsForNewFamily() {
        String openingProfileId = firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .orElseGet(() -> draft().openingProfiles.isEmpty() ? "opening_1" : draft().openingProfiles.getFirst().profileId());
        return List.of(new MKWorkspaceFamilyHorizontalExitDefinition(
                Direction.SOUTH,
                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                openingProfileId
        ));
    }

    private MKWorkspaceTopologySchema topologySchema() {
        return new MKWorkspacePlannerRegistry()
                .plannerFor(topologyProfileType())
                .schema();
    }

    private Optional<MKWorkspaceSlotSchema> topologySlot(String topologySlotId) {
        return topologySchema().slots().stream()
                .filter(slot -> slot.slotId().equals(topologySlotId))
                .findFirst();
    }

    private Optional<MKTowerWorkspaceFamilyDefinition> sharedCornerSource(String topologySlotId) {
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isEmpty() || "keep.corner.shared".equals(cornerStackId.get())) {
            return Optional.empty();
        }
        String suffix = topologySlotId.substring(cornerStackId.get().length());
        String sharedSlotId = "keep.corner.shared" + suffix;
        return draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals(sharedSlotId))
                .findFirst();
    }

    private boolean isConcreteCornerSlot(String topologySlotId) {
        return KEEP_CORNER_STACK_IDS.contains(topologySlotId);
    }

    private Optional<String> towerStackIdForTopologySlot(String topologySlotId) {
        if (MKWorkspaceTopologyProfile.TOWER_PROFILE_TYPE.equals(topologyProfileType())) {
            return MKTowerWorkspaceStackSlot.stackIdForTopologySlot(topologySlotId)
                    .filter(stackId -> TOWER_PRIMARY_STACK_ID.equals(stackId) || "tower".equals(stackId))
                    .map(stackId -> TOWER_PRIMARY_STACK_ID);
        }
        if (topologySlotId.startsWith("keep.center.")) {
            return Optional.of("keep.center");
        }
        return cornerStackIdForSlot(topologySlotId);
    }

    private Optional<String> cornerStackIdForSlot(String topologySlotId) {
        if ("keep.corner.shared".equals(topologySlotId) || topologySlotId.startsWith("keep.corner.shared.")) {
            return Optional.of("keep.corner.shared");
        }
        return KEEP_CORNER_STACK_IDS.stream()
                .filter(stackId -> topologySlotId.equals(stackId) || topologySlotId.startsWith(stackId + "."))
                .findFirst();
    }

    private MKTowerWorkspaceFamilyDefinition copyFamilyForTopologySlot(MKTowerWorkspaceFamilyDefinition existing,
                                                                       String topologySlotId) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                nextUniqueFamilyBaseName(),
                topologySlotMetadata(topologySlotId, existing.slotMetadata()),
                existing.supportsVerticalAccess() ?
                        towerStackIdForTopologySlot(topologySlotId)
                                .orElseGet(() -> valueOrDefault(existing.verticalAccessGroupId(), topologySlotId)) : "",
                existing.supportsVerticalAccess(),
                existing.roomWidth(),
                existing.roomLength(),
                existing.roomHeight(),
                existing.horizontalExtrusionMode(),
                existing.horizontalExits(),
                existing.topVoidMargin(),
                existing.bottomVoidMargin(),
                existing.foundationPolicyOverride(),
                null
        );
    }

    private MKTowerWorkspaceFamilyDefinition defaultFamilyForTopologySlot(MKWorkspaceSlotSchema slot) {
        MKWorkspaceTowerStackSettings settings = towerStackIdForTopologySlot(slot.slotId())
                .map(this::towerStackSettings)
                .orElseGet(this::primaryDimensionStackSettings);
        MKWorkspaceTopologySlotMetadata slotMetadata = topologySlotMetadata(slot);
        boolean supportsVerticalAccess = topologySlotSupportsVerticalAccess(slot);
        String verticalAccessGroupId = towerStackIdForTopologySlot(slot.slotId()).orElse(slot.slotId());
        List<MKWorkspaceFamilyHorizontalExitDefinition> exits =
                towerStackIdForTopologySlot(slot.slotId()).isPresent() ? List.of() : defaultHorizontalExitsForNewFamily();
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                nextUniqueFamilyBaseName(),
                slotMetadata,
                supportsVerticalAccess ? verticalAccessGroupId : "",
                supportsVerticalAccess,
                settings.width(),
                settings.length(),
                settings.height(),
                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                exits,
                0,
                0,
                MKWorkspaceFoundationPolicy.none(),
                null
        );
    }

    private boolean topologySlotSupportsVerticalAccess(MKWorkspaceSlotSchema slot) {
        return topologySchema().roles().stream()
                .filter(role -> role.roleId().equals(slot.roleId()))
                .map(MKWorkspaceRoleSchema::tags)
                .anyMatch(tags -> tags.contains("vertical_access"));
    }

    private MKWorkspaceTopologySlotMetadata topologySlotMetadata(String topologySlotId,
                                                                 MKWorkspaceTopologySlotMetadata fallback) {
        return topologySlot(topologySlotId)
                .map(this::topologySlotMetadata)
                .orElseGet(() -> fallback.withTopologySlotId(topologySlotId));
    }

    private MKWorkspaceTopologySlotMetadata topologySlotMetadata(MKWorkspaceSlotSchema slot) {
        Optional<MKTowerWorkspaceStackSlot> stackSlot = MKTowerWorkspaceStackSlot.fromTopologySlotId(slot.slotId());
        if (stackSlot.isPresent()) {
            return MKWorkspaceTopologySlotMetadata.fromTopologySlotId(slot.slotId());
        }
        MKWorkspaceRoleSchema role = topologySchema().roles().stream()
                .filter(candidate -> candidate.roleId().equals(slot.roleId()))
                .findFirst()
                .orElse(new MKWorkspaceRoleSchema(slot.roleId(), slot.slotKind(), "room",
                        false, false, java.util.Set.of()));
        return MKWorkspaceTopologySlotMetadata.fromTopologyRole(
                slot.slotId(),
                role.roleKind(),
                role.runtimeRoleHint(),
                role.terminal());
    }

    public Optional<String> firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind pathKind) {
        return firstCompatibleOpeningProfile(pathKind).map(MKHorizontalOpeningProfile::profileId);
    }

    private Optional<MKHorizontalOpeningProfile> firstCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind) {
        return draft().openingProfiles.stream()
                .filter(profile -> pathKind.usesMainPath() ?
                        profile.allowOnMainPath() : profile.allowOnBranchPath())
                .findFirst();
    }

    public String ensureCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind, String currentProfileId) {
        return isCompatibleOpeningProfile(pathKind, currentProfileId) ? currentProfileId :
                firstCompatibleOpeningProfileId(pathKind).orElse(currentProfileId);
    }

    public String nextOpeningProfileId(MKWorkspaceHorizontalExitPathKind pathKind, String currentProfileId,
                                       boolean reverse) {
        List<String> compatibleProfiles = draft().openingProfiles.stream()
                .map(MKHorizontalOpeningProfile::profileId)
                .filter(profileId -> isCompatibleOpeningProfile(pathKind, profileId))
                .toList();
        return cycleValue(compatibleProfiles, currentProfileId, reverse, currentProfileId);
    }

    public boolean isCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind, String profileId) {
        return draft().openingProfiles.stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst()
                .map(profile -> pathKind.usesMainPath() ?
                        profile.allowOnMainPath() : profile.allowOnBranchPath())
                .orElse(false);
    }

    private String nextUniqueFamilyBaseName() {
        int index = 1;
        while (true) {
            String candidate = "family_" + index;
            boolean used = draft().familyDefinitions.stream().anyMatch(family -> family.baseName().equals(candidate));
            if (!used) {
                return candidate;
            }
            index++;
        }
    }

    private String nextUniqueOpeningProfileId() {
        int index = 1;
        while (true) {
            String candidate = "opening_" + index;
            boolean used = draft().openingProfiles.stream().anyMatch(profile -> profile.profileId().equals(candidate));
            if (!used) {
                return candidate;
            }
            index++;
        }
    }

    private String nextUniqueLinearRunFamilyId() {
        int index = 1;
        while (true) {
            String candidate = "linear_run_" + index;
            boolean used = draft().linearRunFamilies.stream().anyMatch(linearRun -> linearRun.linearRunId().equals(candidate));
            if (!used) {
                return candidate;
            }
            index++;
        }
    }

    private MKWorkspaceTopologyProfile withMaterialStackSettings(MKWorkspaceTopologyProfile source,
                                                                 MKWorkspaceTopologyProfile materialSource) {
        List<MKWorkspaceTowerStackSettings> stackSettings = source.towerStackSettings().stream()
                .map(settings -> materialSource.towerStackSettings(settings.stackId())
                        .map(requested -> settings.withPaletteOverride(requested.paletteOverrideOpt()))
                        .orElse(settings))
                .toList();
        return new MKWorkspaceTopologyProfile(
                source.profileType(),
                source.uniqueCornerTowers(),
                source.uniqueNorthWestCornerTower(),
                source.uniqueNorthEastCornerTower(),
                source.uniqueSouthEastCornerTower(),
                source.uniqueSouthWestCornerTower(),
                stackSettings,
                source.pathSettings(),
                source.courtyardSettings()
        );
    }

    public MKTowerWorkspaceFamilyDefinition copyFamilyDefinition(MKTowerWorkspaceFamilyDefinition family,
                                                                 Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                paletteOverride.orElse(null)
        );
    }

    public MKWorkspaceLinearRunFamilyDefinition copyLinearRunFamily(MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                                    Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKWorkspaceLinearRunFamilyDefinition(
                linearRun.linearRunId(),
                linearRun.topologySlotId(),
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
                linearRun.topVoidMargin(),
                linearRun.foundationPolicy(),
                paletteOverride.orElse(null)
        );
    }

    private MKTowerWorkspaceFamilyDefinition preserveFamilyMetadata(MKTowerWorkspaceFamilyDefinition existing,
                                                                    MKTowerWorkspaceFamilyDefinition updated) {
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                updated.baseName(),
                topologySlotMetadata(existing.topologySlotId(), updated.slotMetadata()),
                existing.verticalAccessGroupId(),
                updated.supportsVerticalAccess(),
                updated.roomWidth(),
                updated.roomLength(),
                updated.roomHeight(),
                updated.horizontalExtrusionMode(),
                updated.horizontalExits(),
                updated.topVoidMargin(),
                updated.bottomVoidMargin(),
                existing.foundationPolicyOverride(),
                updated.paletteOverride()
        );
    }

    private int makeOdd(int value) {
        int adjusted = Math.max(1, value);
        return adjusted % 2 == 0 ? adjusted + 1 : adjusted;
    }

    private int clamp(int value, int min, int max) {
        int orderedMin = Math.min(min, max);
        int orderedMax = Math.max(min, max);
        return Math.max(orderedMin, Math.min(orderedMax, value));
    }

    private <T> T cycleValue(List<T> values, T current, boolean reverse, T fallback) {
        if (values.isEmpty()) {
            return fallback;
        }
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        int nextIndex = Math.floorMod(index + (reverse ? -1 : 1), values.size());
        return values.get(nextIndex);
    }

    private List<Integer> integerRange(int minInclusive, int maxInclusive) {
        List<Integer> values = new java.util.ArrayList<>();
        for (int value = minInclusive; value <= maxInclusive; value++) {
            values.add(value);
        }
        return values;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public static class Draft {
        public String namespace;
        public String structureName;
        public MKWorkspaceTopologyProfile topologyProfile;
        public int shaftSize;
        public MKVerticalAccessPlacement verticalAccessPlacement;
        public int shellMargin;
        public int exteriorAirMargin;
        public int previewMargin;
        public MKWorkspaceStairMode stairMode;
        public MKWorkspaceStairRiseType stairRiseType;
        public int stairWidth;
        public MKWorkspaceMaterialPalette palette;
        public int mainFloors;
        public int basementFloors;
        public boolean topCapApproachEnabled;
        public boolean basementCapApproachEnabled;
        public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions;
        public List<MKHorizontalOpeningProfile> openingProfiles;
        public List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies;
    }
}
