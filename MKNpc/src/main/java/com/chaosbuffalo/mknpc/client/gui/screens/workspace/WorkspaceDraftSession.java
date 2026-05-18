package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
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
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
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
    private MKTowerWorkspaceCategory selectedFamilyCategory;
    private int selectedFamilyIndex;
    private int selectedFamilyExitIndex;
    private int selectedOpeningIndex;
    private int selectedHallwayIndex;
    private static final List<String> KEEP_CORNER_STACK_IDS = List.of(
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west"
    );

    public WorkspaceDraftSession(MKWorkspaceScreen screen, MKTowerWorkspaceCategory selectedFamilyCategory,
                                 int selectedFamilyIndex, int selectedFamilyExitIndex, int selectedOpeningIndex,
                                 int selectedHallwayIndex) {
        this.screen = screen;
        this.selectedFamilyCategory = selectedFamilyCategory;
        this.selectedFamilyIndex = selectedFamilyIndex;
        this.selectedFamilyExitIndex = selectedFamilyExitIndex;
        this.selectedOpeningIndex = selectedOpeningIndex;
        this.selectedHallwayIndex = selectedHallwayIndex;
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
        draft.mainFloors = workspace != null ? workspace.floorSettings().mainFloors() : MKTowerWorkspaceFloorSettings.defaultSettings().mainFloors();
        draft.basementFloors = workspace != null ? workspace.floorSettings().basementFloors() : MKTowerWorkspaceFloorSettings.defaultSettings().basementFloors();
        draft.topCapApproachEnabled = workspace != null ? workspace.floorSettings().topCapApproachEnabled() :
                MKTowerWorkspaceFloorSettings.defaultSettings().topCapApproachEnabled();
        draft.basementCapApproachEnabled = workspace != null ? workspace.floorSettings().basementCapApproachEnabled() :
                MKTowerWorkspaceFloorSettings.defaultSettings().basementCapApproachEnabled();
        draft.categoryProfiles = List.copyOf(workspace != null ? workspace.categoryProfiles() :
                MKTowerWorkspaceCategoryProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()));
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
        if (selectedFamilyCategory == null) {
            selectedFamilyCategory = MKTowerWorkspaceCategory.ENTRY;
        }
        seedDefaultsForTopology();
        snapDraftVerticalAccess();
    }

    public String summary() {
        Draft draft = draft();
        return draft.namespace + ":" + draft.structureName + "  |  " +
                "shaft " + draft.shaftSize +
                "  |  topology " + draft.topologyProfile.profileType() +
                "  |  categories " + draft.categoryProfiles.size() +
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
                settings
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
        return towerStackSettings("keep.center").width();
    }

    public void walledKeepCenterWidth(int value) {
        int width = makeOdd(Math.max(3, value));
        replaceTowerStackSettings(towerStackSettings("keep.center").withWidth(width));
        applyTowerStackSettingsToFamilies();
    }

    public int walledKeepCenterLength() {
        return towerStackSettings("keep.center").length();
    }

    public void walledKeepCenterLength(int value) {
        int length = makeOdd(Math.max(3, value));
        replaceTowerStackSettings(towerStackSettings("keep.center").withLength(length));
        applyTowerStackSettingsToFamilies();
    }

    public int walledKeepCenterHeight() {
        return towerStackSettings("keep.center").height();
    }

    public void walledKeepCenterHeight(int value) {
        int height = Math.max(3, value);
        replaceTowerStackSettings(towerStackSettings("keep.center").withHeight(height));
        applyTowerStackSettingsToFamilies();
    }

    public void topologyDefaultHeight(int value) {
        int requestedHeight = Math.max(3, value);
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfileType())) {
            walledKeepCenterHeight(requestedHeight);
            return;
        }
        draft().categoryProfiles = draft().categoryProfiles.stream()
                .map(profile -> new MKTowerWorkspaceCategoryProfile(
                        profile.category(),
                        profile.roomWidth(),
                        profile.roomLength(),
                        requestedHeight,
                        profile.minMainPathPieces(),
                        profile.maxMainPathPieces(),
                        profile.maxBranchPiecesBeforeCap(),
                        profile.paletteOverride()))
                .toList();
        draft().familyDefinitions = draft().familyDefinitions.stream()
                .map(family -> family.topologySlotId().startsWith("keep.") ?
                        copyFamilyWithGeometry(family, family.roomWidth(), family.roomLength(), requestedHeight) :
                        family)
                .toList();
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
        return towerStackSettings(topologySlotId).width();
    }

    public void cornerTowerWidth(String topologySlotId, int value) {
        replaceTowerStackSettings(towerStackSettings(topologySlotId).withWidth(makeOdd(Math.max(3, value))));
        applyTowerStackSettingsToFamilies();
    }

    public int cornerTowerLength(String topologySlotId) {
        return towerStackSettings(topologySlotId).length();
    }

    public void cornerTowerLength(String topologySlotId, int value) {
        replaceTowerStackSettings(towerStackSettings(topologySlotId).withLength(makeOdd(Math.max(3, value))));
        applyTowerStackSettingsToFamilies();
    }

    public int cornerTowerHeight(String topologySlotId) {
        return towerStackSettings(topologySlotId).height();
    }

    public void cornerTowerHeight(String topologySlotId, int value) {
        replaceTowerStackSettings(towerStackSettings(topologySlotId).withHeight(value));
        applyTowerStackSettingsToFamilies();
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
                .filter(linearRun -> linearRun.topologySlotId().startsWith("keep.perimeter."))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorHeight)
                .orElse(7);
    }

    public void wallHeight(int value) {
        int height = Math.max(2, value);
        draft().linearRunFamilies = draft().linearRunFamilies.stream()
                .map(linearRun -> linearRun.topologySlotId().startsWith("keep.perimeter.") ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(), height,
                                Math.min(linearRun.topVoidMargin(), Math.max(0, height - 1))) :
                        linearRun)
                .toList();
    }

    public int wallTopVoidMargin() {
        return draft().linearRunFamilies.stream()
                .filter(linearRun -> linearRun.topologySlotId().startsWith("keep.perimeter."))
                .findFirst()
                .map(MKWorkspaceLinearRunFamilyDefinition::topVoidMargin)
                .orElse(0);
    }

    public void wallTopVoidMargin(int value) {
        int margin = Math.max(0, value);
        draft().linearRunFamilies = draft().linearRunFamilies.stream()
                .map(linearRun -> linearRun.topologySlotId().startsWith("keep.perimeter.") ?
                        copyLinearRunWithTopologyHeightAndTopVoid(linearRun, linearRun.topologySlotId(),
                                linearRun.interiorHeight(), Math.min(margin, Math.max(0, linearRun.interiorHeight() - 1))) :
                        linearRun)
                .toList();
    }

    public void resetCurrentTopologyDefaults() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig defaultStairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        MKTowerWorkspaceFloorSettings defaultFloorSettings = MKTowerWorkspaceFloorSettings.defaultSettings();
        draft().stairMode = defaultStairConfig.mode();
        draft().stairRiseType = defaultStairConfig.riseType();
        draft().stairWidth = defaultStairConfig.stairWidth();
        draft().verticalAccessPlacement = MKWorkspaceVerticalAccessSpec.defaultSpec().placement();
        draft().shaftSize = MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
        draft().mainFloors = defaultFloorSettings.mainFloors();
        draft().basementFloors = defaultFloorSettings.basementFloors();
        draft().topCapApproachEnabled = defaultFloorSettings.topCapApproachEnabled();
        draft().basementCapApproachEnabled = defaultFloorSettings.basementCapApproachEnabled();
        draft().openingProfiles = MKHorizontalOpeningProfile.createDefaults(dimensions);
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            draft().topologyProfile = MKWorkspaceTopologyProfile.walledKeep(
                    draft().topologyProfile.uniqueNorthWestCornerTower(),
                    draft().topologyProfile.uniqueNorthEastCornerTower(),
                    draft().topologyProfile.uniqueSouthEastCornerTower(),
                    draft().topologyProfile.uniqueSouthWestCornerTower());
            draft().categoryProfiles = MKTowerWorkspaceCategoryProfile.createWalledKeepDefaults(dimensions);
            draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createWalledKeepDefaults(dimensions);
            draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createWalledKeepDefaults(dimensions,
                    draft().palette);
        } else {
            draft().categoryProfiles = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions);
            draft().familyDefinitions = MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions);
            draft().linearRunFamilies = MKWorkspaceLinearRunFamilyDefinition.createDefaults(dimensions,
                    draft().palette);
        }
        selectedFamilyCategory = MKTowerWorkspaceCategory.ENTRY;
        selectedFamilyIndex = -1;
        selectedFamilyExitIndex = -1;
        selectedOpeningIndex = -1;
        selectedHallwayIndex = -1;
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

    public long familyCount(MKTowerWorkspaceCategory category) {
        return draft().familyDefinitions.stream()
                .filter(family -> family.category() == category)
                .count();
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

    public MKTowerWorkspaceCategory selectedFamilyCategory() {
        ensureInitialized();
        if (selectedFamilyCategory == null) {
            selectedFamilyCategory = MKTowerWorkspaceCategory.MAIN;
        }
        return selectedFamilyCategory;
    }

    public void selectedFamilyCategory(MKTowerWorkspaceCategory category) {
        selectedFamilyCategory = category;
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

    public int selectedHallwayIndex() {
        return selectedHallwayIndex;
    }

    public void selectedHallwayIndex(int index) {
        selectedHallwayIndex = index;
    }

    public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions() {
        return List.copyOf(draft().familyDefinitions);
    }

    public int addFamilyDefinition(MKTowerWorkspaceCategory category) {
        MKTowerWorkspaceCategoryProfile profile = getCategoryProfile(category);
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.add(new MKTowerWorkspaceFamilyDefinition(
                nextUniqueFamilyBaseName(),
                category,
                defaultRoleForCategory(category),
                true,
                profile.roomWidth(),
                profile.roomLength(),
                profile.fullHeight(),
                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                defaultHorizontalExitsForNewFamily()
        ));
        draft().familyDefinitions = List.copyOf(updated);
        return draft().familyDefinitions.size() - 1;
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

    public MKWorkspaceMaterialPalette resolveCategoryPalette(MKTowerWorkspaceCategory category) {
        MKWorkspaceMaterialPalette basePalette = draftBasePalette();
        return getCategoryProfile(category).paletteOverrideOpt()
                .map(override -> override.resolve(basePalette))
                .orElse(basePalette);
    }

    public MKStructureWorkspace buildWorkspaceDraft() {
        snapDraftVerticalAccess();
        MKTowerWorkspaceCategoryProfile entryProfile = getCategoryProfile(MKTowerWorkspaceCategory.ENTRY);
        MKTowerWorkspaceCategoryProfile mainProfile = getCategoryProfile(MKTowerWorkspaceCategory.MAIN);
        MKTowerWorkspaceCategoryProfile basementProfile = getCategoryProfile(MKTowerWorkspaceCategory.BASEMENT);
        MKWorkspaceDimensions dimensions = new MKWorkspaceDimensions(
                mainProfile.roomWidth(),
                mainProfile.roomLength(),
                entryProfile.fullHeight(),
                mainProfile.fullHeight(),
                basementProfile.fullHeight(),
                draft().shaftSize,
                deriveDoorwayWidth(),
                deriveDoorwayHeight()
        );
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
                MKStructureFamilyType.TOWER,
                draft().topologyProfile,
                dimensions,
                palette,
                stairConfig,
                draft().verticalAccessPlacement,
                draft().shellMargin,
                draft().exteriorAirMargin,
                draft().previewMargin,
                verticalAccessSpec,
                new MKTowerWorkspaceFloorSettings(draft().mainFloors, draft().basementFloors,
                        draft().topCapApproachEnabled, draft().basementCapApproachEnabled),
                draft().categoryProfiles,
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
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = currentVerticalAccessSpec();
        int normalizedMainHeight = normalizeCategoryFullHeight(MKTowerWorkspaceCategory.MAIN,
                getCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight(), verticalAccessSpec,
                getCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight());
        draft().categoryProfiles = draft().categoryProfiles.stream()
                .map(profile -> normalizeCategoryProfile(profile, verticalAccessSpec, normalizedMainHeight))
                .toList();
        draft().mainFloors = normalizeMainFloorCount(draft().mainFloors, draft().basementFloors);
        draft().basementFloors = normalizeBasementFloorCount(draft().basementFloors, draft().mainFloors);
        draft().mainFloors = normalizeMainFloorCount(draft().mainFloors, draft().basementFloors);
        draft().familyDefinitions = draft().familyDefinitions.stream()
                .map(this::normalizeFamilyDefinition)
                .toList();
        normalizeTowerStackSettingsFromFamilies();
    }

    public MKTowerWorkspaceCategoryProfile getCategoryProfile(MKTowerWorkspaceCategory category) {
        return draft().categoryProfiles.stream()
                .filter(profile -> profile.category() == category)
                .findFirst()
                .orElseThrow();
    }

    public void replaceCategoryProfile(MKTowerWorkspaceCategoryProfile updatedProfile) {
        draft().categoryProfiles = draft().categoryProfiles.stream()
                .map(profile -> profile.category() == updatedProfile.category() ? updatedProfile : profile)
                .toList();
        snapDraftVerticalAccess();
    }

    public void replaceFamilyDefinition(int index, MKTowerWorkspaceFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.set(index, normalizeFamilyDefinition(preserveFamilyMetadata(updated.get(index), updatedFamily)));
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void replaceFamilyTopologySlotId(int index, String topologySlotId) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), topologySlotId,
                family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                family.foundationPolicy(), family.paletteOverride()));
    }

    public void replaceFamilyVerticalAccessGroupId(int index, String verticalAccessGroupId) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.topologySlotId(),
                verticalAccessGroupId, family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                family.foundationPolicy(), family.paletteOverride()));
    }

    public void replaceFamilyFoundationPolicy(int index, MKWorkspaceFoundationPolicy foundationPolicy) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.topologySlotId(),
                family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                foundationPolicy, family.paletteOverride()));
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
        replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
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
        replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
    }

    public int addFamilyExitAtDirection(int familyIndex, Direction direction) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        if (direction.getAxis().isVertical()) {
            exits.add(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(direction));
            replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
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
        replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
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
        if (settingsComparisonTag(existing, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(existingWithRequestedMaterials, existing.id(), existing.previewMargin()))) {
            return false;
        }
        return settingsComparisonTag(existingWithRequestedMaterials, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin()));
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
                workspace.familyType(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                palette,
                alignStairMaterials(workspace.stairConfig(), palette),
                workspace.verticalAccessPlacement(),
                shellMargin,
                exteriorAirMargin,
                previewMargin,
                alignVerticalAccessMaterials(workspace.verticalAccessSpec(), palette),
                workspace.floorSettings(),
                workspace.categoryProfiles(),
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
                source.familyType(),
                source.topologyProfile(),
                source.dimensions(),
                materialSource.palette(),
                alignStairMaterials(source.stairConfig(), materialSource.palette()),
                source.verticalAccessPlacement(),
                source.shellMargin(),
                source.exteriorAirMargin(),
                source.previewMargin(),
                alignVerticalAccessMaterials(source.verticalAccessSpec(), materialSource.palette()),
                source.floorSettings(),
                source.categoryProfiles().stream()
                        .map(profile -> materialSource.categoryProfile(profile.category())
                                .map(requested -> copyCategoryProfile(profile, requested.paletteOverrideOpt()))
                                .orElse(profile))
                        .toList(),
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

    public Optional<MKHorizontalOpeningProfile> getOpeningProfile(String profileId) {
        return draft().openingProfiles.stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst();
    }

    private MKWorkspaceVerticalAccessSpec currentVerticalAccessSpec() {
        return new MKWorkspaceVerticalAccessSpec(draft().shaftSize, draft().verticalAccessPlacement, makeStairConfig());
    }

    private MKTowerWorkspaceCategoryProfile normalizeCategoryProfile(MKTowerWorkspaceCategoryProfile profile,
                                                                    MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                                                    int normalizedMainHeight) {
        int roomWidth = Math.max(3, makeOdd(profile.roomWidth()));
        int roomLength = Math.max(3, makeOdd(profile.roomLength()));
        int fullHeight = normalizeCategoryFullHeight(profile.category(), profile.fullHeight(), verticalAccessSpec, normalizedMainHeight);
        return new MKTowerWorkspaceCategoryProfile(
                profile.category(),
                roomWidth,
                roomLength,
                fullHeight,
                Math.max(0, profile.minMainPathPieces()),
                Math.max(Math.max(0, profile.minMainPathPieces()), profile.maxMainPathPieces()),
                Math.max(0, Math.min(MKTowerWorkspaceCategoryProfile.DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP,
                        profile.maxBranchPiecesBeforeCap())),
                profile.paletteOverride()
        );
    }

    public int[] verticalAccessFootprint() {
        int minWidth = draft().categoryProfiles.stream().mapToInt(MKTowerWorkspaceCategoryProfile::roomWidth).min().orElse(9);
        int minLength = draft().categoryProfiles.stream().mapToInt(MKTowerWorkspaceCategoryProfile::roomLength).min().orElse(9);
        return new int[]{minWidth, minLength};
    }

    private int[] getDraftVerticalAccessFootprint() {
        return verticalAccessFootprint();
    }

    public List<Integer> allowedFullHeightsForCategory(MKTowerWorkspaceCategory category) {
        if (category == MKTowerWorkspaceCategory.TOP_CAP && !draft().topCapApproachEnabled) {
            return integerRange(3, MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1);
        }
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = currentVerticalAccessSpec();
        int referenceHeight = getCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight();
        return MKWorkspaceDimensions.getAllowedBandHeights(
                verticalAccessSpec.stairConfig(),
                verticalAccessSpec.shaftSize(),
                referenceHeight,
                3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE
        );
    }

    public List<Integer> allowedMainFloorCounts(int basementFloors) {
        return MKTowerWorkspaceFloorSettings.allowedMainFloorCounts(draft().categoryProfiles, basementFloors,
                draft().topCapApproachEnabled, draft().basementCapApproachEnabled);
    }

    public List<Integer> allowedBasementFloorCounts(int mainFloors) {
        return MKTowerWorkspaceFloorSettings.allowedBasementFloorCounts(draft().categoryProfiles, mainFloors,
                draft().topCapApproachEnabled, draft().basementCapApproachEnabled);
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
        MKWorkspaceTowerStackSettings settings = draft().topologyProfile.towerStackSettingsOrDefault(stackId);
        if (draft().topologyProfile.towerStackSettings(stackId).isEmpty()) {
            replaceTowerStackSettings(settings);
        }
        return settings;
    }

    private void replaceTowerStackSettings(MKWorkspaceTowerStackSettings settings) {
        draft().topologyProfile = draft().topologyProfile.withTowerStackSettings(settings);
    }

    private void replaceTowerStackStairConfig(String stackId, MKWorkspaceStairAuthoringConfig stairConfig) {
        MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId).withStairConfig(stairConfig);
        int normalizedMain = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), settings.basementFloors());
        int normalizedBasement = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), normalizedMain);
        replaceTowerStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    private List<MKTowerWorkspaceCategoryProfile> categoryProfilesForTowerStack(MKWorkspaceTowerStackSettings settings) {
        int height = Math.max(3, settings.height());
        return draft().categoryProfiles.stream()
                .map(profile -> new MKTowerWorkspaceCategoryProfile(
                        profile.category(),
                        profile.roomWidth(),
                        profile.roomLength(),
                        height,
                        profile.minMainPathPieces(),
                        profile.maxMainPathPieces(),
                        profile.maxBranchPiecesBeforeCap(),
                        profile.paletteOverride()))
                .toList();
    }

    private List<Integer> allowedTowerStackMainFloorCounts(MKWorkspaceTowerStackSettings settings, int basementFloors) {
        return MKTowerWorkspaceFloorSettings.allowedMainFloorCounts(categoryProfilesForTowerStack(settings),
                basementFloors, settings.topCapApproachEnabled(), settings.basementCapApproachEnabled());
    }

    private List<Integer> allowedTowerStackBasementFloorCounts(MKWorkspaceTowerStackSettings settings, int mainFloors) {
        return MKTowerWorkspaceFloorSettings.allowedBasementFloorCounts(categoryProfilesForTowerStack(settings),
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

    private void normalizeTowerStackSettingsFromFamilies() {
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            return;
        }
        for (String stackId : activeTowerStackIds()) {
            int width = familyWidthForStack(stackId).orElseGet(() -> towerStackSettings(stackId).width());
            int length = familyLengthForStack(stackId).orElseGet(() -> towerStackSettings(stackId).length());
            int height = familyHeightForStack(stackId).orElseGet(() -> towerStackSettings(stackId).height());
            MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId)
                    .withWidth(width)
                    .withLength(length)
                    .withHeight(height);
            int mainFloors = normalizeTowerStackMainFloorCount(settings, settings.mainFloors(), settings.basementFloors());
            int basementFloors = normalizeTowerStackBasementFloorCount(settings, settings.basementFloors(), mainFloors);
            replaceTowerStackSettings(settings.withMainFloors(mainFloors).withBasementFloors(basementFloors));
        }
        applyTowerStackSettingsToFamilies();
    }

    private List<String> activeTowerStackIds() {
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfileType())) {
            return List.of();
        }
        java.util.ArrayList<String> stackIds = new java.util.ArrayList<>();
        stackIds.add("keep.center");
        stackIds.addAll(activeCornerTopologySlots());
        return List.copyOf(stackIds);
    }

    private Optional<Integer> familyHeightForStack(String stackId) {
        return draft().familyDefinitions.stream()
                .filter(family -> stackId.equals(stackIdForFamily(family)))
                .findFirst()
                .map(MKTowerWorkspaceFamilyDefinition::roomHeight);
    }

    private Optional<Integer> familyWidthForStack(String stackId) {
        return draft().familyDefinitions.stream()
                .filter(family -> stackId.equals(stackIdForFamily(family)))
                .findFirst()
                .map(MKTowerWorkspaceFamilyDefinition::roomWidth);
    }

    private Optional<Integer> familyLengthForStack(String stackId) {
        return draft().familyDefinitions.stream()
                .filter(family -> stackId.equals(stackIdForFamily(family)))
                .findFirst()
                .map(MKTowerWorkspaceFamilyDefinition::roomLength);
    }

    private int normalizeCategoryFullHeight(MKTowerWorkspaceCategory category, int requestedHeight,
                                            MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                            int mainReferenceHeight) {
        if (category == MKTowerWorkspaceCategory.TOP_CAP && !draft().topCapApproachEnabled) {
            return Math.max(3, Math.min(MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, requestedHeight));
        }
        return MKWorkspaceDimensions.snapToNearestAllowedBandHeight(
                verticalAccessSpec.stairConfig(),
                verticalAccessSpec.shaftSize(),
                mainReferenceHeight,
                requestedHeight,
                3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE
        );
    }

    public MKTowerWorkspaceFamilyDefinition normalizeFamilyDefinition(MKTowerWorkspaceFamilyDefinition family) {
        Optional<String> towerStackId = towerStackIdForTopologySlot(family.topologySlotId());
        MKTowerWorkspaceCategoryProfile profile = categoryProfileForFamilyNormalization(family, towerStackId);
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = verticalAccessSpecForFamilyNormalization(towerStackId);
        int roomWidth = normalizeFamilyWidth(family.roomWidth(), family.supportsVerticalAccess(), verticalAccessSpec);
        int roomLength = normalizeFamilyLength(family.roomLength(), family.supportsVerticalAccess(), verticalAccessSpec);
        int roomHeight = normalizeFamilyHeightForCategory(family.roomHeight(), family.supportsVerticalAccess(), profile);
        int availableVoidMargin = Math.max(0, roomHeight - MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT);
        int topVoidMargin = family.supportsVerticalAccess() ? 0 :
                clamp(family.topVoidMargin(), 0, availableVoidMargin);
        int bottomVoidMargin = family.supportsVerticalAccess() ? 0 :
                clamp(family.bottomVoidMargin(), 0, availableVoidMargin - topVoidMargin);
        MKTowerWorkspaceFamilyDefinition normalizedGeometry = new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.topologySlotId(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                roomWidth,
                roomLength,
                roomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                topVoidMargin,
                bottomVoidMargin,
                family.foundationPolicy(),
                family.paletteOverride()
        );
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.topologySlotId(),
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
                family.foundationPolicy(),
                family.paletteOverride()
        );
    }

    private MKTowerWorkspaceCategoryProfile categoryProfileForFamilyNormalization(MKTowerWorkspaceFamilyDefinition family,
                                                                                  Optional<String> towerStackId) {
        MKTowerWorkspaceCategoryProfile profile = getCategoryProfile(family.category());
        if (towerStackId.isEmpty()) {
            return profile;
        }
        MKWorkspaceTowerStackSettings settings = towerStackSettings(towerStackId.get());
        return new MKTowerWorkspaceCategoryProfile(
                profile.category(),
                profile.roomWidth(),
                profile.roomLength(),
                settings.height(),
                profile.minMainPathPieces(),
                profile.maxMainPathPieces(),
                profile.maxBranchPiecesBeforeCap(),
                profile.paletteOverride()
        );
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

    public int normalizeFamilyWidthForCategory(int requestedWidth, boolean supportsVerticalAccess,
                                               MKTowerWorkspaceCategoryProfile profile) {
        return normalizeFamilyWidth(requestedWidth, supportsVerticalAccess, currentVerticalAccessSpec());
    }

    public int normalizeFamilyLengthForCategory(int requestedLength, boolean supportsVerticalAccess,
                                                MKTowerWorkspaceCategoryProfile profile) {
        return normalizeFamilyLength(requestedLength, supportsVerticalAccess, currentVerticalAccessSpec());
    }

    public int normalizeFamilyHeightForCategory(int requestedHeight, boolean supportsVerticalAccess,
                                                MKTowerWorkspaceCategoryProfile profile) {
        if (supportsVerticalAccess) {
            return profile.fullHeight();
        }
        return Math.max(MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT, Math.min(requestedHeight, profile.fullHeight()));
    }

    private void seedDefaultsForTopology() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            migrateWalledKeepPerimeterLinearRuns();
            boolean hasKeepFamilies = draft().familyDefinitions.stream()
                    .anyMatch(family -> family.topologySlotId().startsWith("keep."));
            if (!hasKeepFamilies) {
                draft().categoryProfiles = MKTowerWorkspaceCategoryProfile.createWalledKeepDefaults(dimensions);
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
        if (!MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(draft().topologyProfile.profileType())) {
            return;
        }
        draft().familyDefinitions = draft().familyDefinitions.stream()
                .map(family -> {
                    String stackId = stackIdForFamily(family);
                    if (stackId.isBlank()) {
                        return family;
                    }
                    MKWorkspaceTowerStackSettings settings = towerStackSettings(stackId);
                    return copyFamilyWithGeometry(family, settings.width(), settings.length(), settings.height());
                })
                .toList();
    }

    private String stackIdForFamily(MKTowerWorkspaceFamilyDefinition family) {
        if (family.topologySlotId().startsWith("keep.center.")) {
            return "keep.center";
        }
        return cornerStackIdForSlot(family.topologySlotId()).orElse("");
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
                if (linearRun.topologySlotId().startsWith("keep.perimeter.")) {
                    existingPerimeterSlots.add(linearRun.topologySlotId());
                }
                continue;
            }
            MKWorkspaceLinearRunFamilyDefinition converted =
                    copyLinearRunWithTopologyAndHeight(linearRun, perimeterSlot, linearRun.interiorHeight());
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
        if (existing.kind() == MKWorkspaceLinearRunKind.SOLID_WALL) {
            return existing;
        }
        if (candidate.kind() == MKWorkspaceLinearRunKind.SOLID_WALL) {
            return candidate;
        }
        return existing;
    }

    private String legacyPerimeterSlot(String topologySlotId) {
        if (topologySlotId.startsWith("keep.wall.")) {
            return "keep.perimeter." + topologySlotId.substring("keep.wall.".length());
        }
        if (topologySlotId.startsWith("keep.parapet.")) {
            return "keep.perimeter." + topologySlotId.substring("keep.parapet.".length());
        }
        return null;
    }

    private MKTowerWorkspaceFamilyDefinition copyFamilyWithGeometry(MKTowerWorkspaceFamilyDefinition family,
                                                                    int roomWidth, int roomLength, int roomHeight) {
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.topologySlotId(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                roomWidth,
                roomLength,
                roomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicy(),
                family.paletteOverride()
        );
    }

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunWithTopologyAndHeight(
            MKWorkspaceLinearRunFamilyDefinition linearRun, String topologySlotId, int interiorHeight) {
        return copyLinearRunWithTopologyHeightAndTopVoid(linearRun, topologySlotId, interiorHeight,
                Math.min(linearRun.topVoidMargin(), Math.max(0, interiorHeight - 1)));
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
        return direction == Direction.NORTH || direction == Direction.SOUTH ? family.roomWidth() : family.roomLength();
    }

    public int maxVerticalOffset(MKTowerWorkspaceFamilyDefinition family, String openingProfileId) {
        int openingHeight = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingHeight)
                .orElse(1);
        return Math.max(0, family.roomHeight() - openingHeight);
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
        return new MKTowerWorkspaceFamilyDefinition(
                nextUniqueFamilyBaseName(),
                existing.category(),
                existing.pieceRole(),
                topologySlotId,
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
                existing.foundationPolicy(),
                null
        );
    }

    private MKTowerWorkspaceFamilyDefinition defaultFamilyForTopologySlot(MKWorkspaceSlotSchema slot) {
        MKTowerWorkspaceCategory category = defaultCategoryForTopologySlot(slot.slotId());
        MKTowerWorkspaceCategoryProfile profile = getCategoryProfile(category);
        MKWorkspacePieceRole role = defaultRoleForTopologySlot(slot.slotId(), category);
        boolean supportsVerticalAccess = topologySlotSupportsVerticalAccess(slot);
        String verticalAccessGroupId = towerStackIdForTopologySlot(slot.slotId()).orElse(slot.slotId());
        List<MKWorkspaceFamilyHorizontalExitDefinition> exits =
                towerStackIdForTopologySlot(slot.slotId()).isPresent() ? List.of() : defaultHorizontalExitsForNewFamily();
        return new MKTowerWorkspaceFamilyDefinition(
                nextUniqueFamilyBaseName(),
                category,
                role,
                slot.slotId(),
                supportsVerticalAccess ? verticalAccessGroupId : "",
                supportsVerticalAccess,
                profile.roomWidth(),
                profile.roomLength(),
                profile.fullHeight(),
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

    private MKTowerWorkspaceCategory defaultCategoryForTopologySlot(String topologySlotId) {
        if (topologySlotId.contains("basement_cap")) {
            return MKTowerWorkspaceCategory.BASEMENT_CAP;
        }
        if (topologySlotId.contains("basement")) {
            return MKTowerWorkspaceCategory.BASEMENT;
        }
        if (topologySlotId.contains("top_cap")) {
            return MKTowerWorkspaceCategory.TOP_CAP;
        }
        if (topologySlotId.contains("entry") || topologySlotId.contains("gate")) {
            return MKTowerWorkspaceCategory.ENTRY;
        }
        return MKTowerWorkspaceCategory.MAIN;
    }

    private MKWorkspacePieceRole defaultRoleForTopologySlot(String topologySlotId, MKTowerWorkspaceCategory category) {
        if (topologySlotId.contains("top_cap_approach")) {
            return MKWorkspacePieceRole.TOP_CAP_APPROACH;
        }
        if (topologySlotId.contains("top_cap")) {
            return MKWorkspacePieceRole.TOP_CAP;
        }
        if (topologySlotId.contains("basement_cap_approach")) {
            return MKWorkspacePieceRole.BASEMENT_CAP_APPROACH;
        }
        if (topologySlotId.contains("basement_cap")) {
            return MKWorkspacePieceRole.BASEMENT_CAP;
        }
        if (topologySlotId.contains("basement_entry")) {
            return MKWorkspacePieceRole.BASEMENT_ENTRY;
        }
        return defaultRoleForCategory(category);
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

    public MKTowerWorkspaceCategoryProfile copyCategoryProfile(MKTowerWorkspaceCategoryProfile profile,
                                                              Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKTowerWorkspaceCategoryProfile(
                profile.category(),
                profile.roomWidth(),
                profile.roomLength(),
                profile.fullHeight(),
                profile.minMainPathPieces(),
                profile.maxMainPathPieces(),
                profile.maxBranchPiecesBeforeCap(),
                paletteOverride.orElse(null)
        );
    }

    public MKTowerWorkspaceFamilyDefinition copyFamilyDefinition(MKTowerWorkspaceFamilyDefinition family,
                                                                 Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.topologySlotId(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicy(),
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
        return new MKTowerWorkspaceFamilyDefinition(
                updated.baseName(),
                updated.category(),
                updated.pieceRole(),
                existing.topologySlotId(),
                existing.verticalAccessGroupId(),
                updated.supportsVerticalAccess(),
                updated.roomWidth(),
                updated.roomLength(),
                updated.roomHeight(),
                updated.horizontalExtrusionMode(),
                updated.horizontalExits(),
                updated.topVoidMargin(),
                updated.bottomVoidMargin(),
                existing.foundationPolicy(),
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

    private MKWorkspacePieceRole defaultRoleForCategory(MKTowerWorkspaceCategory category) {
        return switch (category) {
            case ENTRY -> MKWorkspacePieceRole.ENTRY;
            case MAIN -> MKWorkspacePieceRole.FLOOR_MAIN;
            case BASEMENT -> MKWorkspacePieceRole.BASEMENT_MAIN;
            case TOP_CAP -> MKWorkspacePieceRole.TOP_CAP_APPROACH;
            case BASEMENT_CAP -> MKWorkspacePieceRole.BASEMENT_CAP;
        };
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
        public List<MKTowerWorkspaceCategoryProfile> categoryProfiles;
        public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions;
        public List<MKHorizontalOpeningProfile> openingProfiles;
        public List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies;
    }
}
