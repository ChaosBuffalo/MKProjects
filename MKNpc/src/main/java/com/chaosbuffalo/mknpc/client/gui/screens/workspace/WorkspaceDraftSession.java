package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkspaceDraftSession {
    private final MKWorkspaceScreen screen;
    private Draft draft;
    private MKTowerWorkspaceCategory selectedFamilyCategory;
    private int selectedFamilyIndex;
    private int selectedFamilyExitIndex;
    private int selectedOpeningIndex;
    private int selectedHallwayIndex;

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
        draft.stairMode = workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO;
        draft.stairRiseType = workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.MIXED;
        draft.stairWidth = workspace != null ? workspace.stairConfig().stairWidth() : 1;
        draft.stairBlock = workspace != null ? workspace.palette().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs");
        draft.slabBlock = workspace != null ? workspace.palette().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab");
        draft.ladderBlock = workspace != null ? workspace.palette().ladderBlock() : ResourceLocation.parse("minecraft:ladder");
        draft.verticalAccessPlacement = workspace != null ? workspace.verticalAccessSpec().placement() : MKVerticalAccessPlacement.CENTER;
        draft.shellMargin = workspace != null ? workspace.shellMargin() : 1;
        draft.exteriorAirMargin = workspace != null ? workspace.exteriorAirMargin() : 2;
        draft.previewMargin = workspace != null ? workspace.previewMargin() : 4;
        draft.floorBlock = workspace != null ? workspace.palette().floorBlock() : ResourceLocation.parse("minecraft:smooth_stone");
        draft.wallBlock = workspace != null ? workspace.palette().wallBlock() : ResourceLocation.parse("minecraft:stone_bricks");
        draft.ceilingBlock = workspace != null ? workspace.palette().ceilingBlock() : ResourceLocation.parse("minecraft:smooth_stone");
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
        draft.hallwayFamilies = List.copyOf(workspace != null ? workspace.hallwayFamilies() :
                MKHallwayFamilyDefinition.createDefaults(MKWorkspaceDimensions.defaultDimensions(),
                        MKWorkspaceMaterialPalette.defaultPalette()));
        int requestedShaftSize = workspace != null ? workspace.verticalAccessSpec().shaftSize() :
                MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
        draft.shaftSize = requestedShaftSize;
        if (selectedFamilyCategory == null) {
            selectedFamilyCategory = MKTowerWorkspaceCategory.ENTRY;
        }
        snapDraftVerticalAccess();
    }

    public String summary() {
        Draft draft = draft();
        return draft.namespace + ":" + draft.structureName + "  |  " +
                "shaft " + draft.shaftSize +
                "  |  categories " + draft.categoryProfiles.size() +
                "  |  families " + draft.familyDefinitions.size() +
                "  |  openings " + draft.openingProfiles.size() +
                "  |  hallways " + draft.hallwayFamilies.size();
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

    public ResourceLocation floorBlock() {
        return draft().floorBlock;
    }

    public void floorBlock(ResourceLocation value) {
        draft().floorBlock = value;
    }

    public ResourceLocation wallBlock() {
        return draft().wallBlock;
    }

    public void wallBlock(ResourceLocation value) {
        draft().wallBlock = value;
    }

    public ResourceLocation ceilingBlock() {
        return draft().ceilingBlock;
    }

    public void ceilingBlock(ResourceLocation value) {
        draft().ceilingBlock = value;
    }

    public ResourceLocation stairBlock() {
        return draft().stairBlock;
    }

    public void stairBlock(ResourceLocation value) {
        draft().stairBlock = value;
    }

    public ResourceLocation slabBlock() {
        return draft().slabBlock;
    }

    public void slabBlock(ResourceLocation value) {
        draft().slabBlock = value;
    }

    public ResourceLocation ladderBlock() {
        return draft().ladderBlock;
    }

    public void ladderBlock(ResourceLocation value) {
        draft().ladderBlock = value;
    }

    public long familyCount(MKTowerWorkspaceCategory category) {
        return draft().familyDefinitions.stream()
                .filter(family -> family.category() == category)
                .count();
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

    public List<MKHallwayFamilyDefinition> hallwayFamilies() {
        return List.copyOf(draft().hallwayFamilies);
    }

    public void replaceHallwayFamily(int index, MKHallwayFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKHallwayFamilyDefinition> updated = new java.util.ArrayList<>(draft().hallwayFamilies);
        updated.set(index, updatedFamily);
        draft().hallwayFamilies = List.copyOf(updated);
    }

    public void removeHallwayFamily(int index) {
        java.util.ArrayList<MKHallwayFamilyDefinition> updated = new java.util.ArrayList<>(draft().hallwayFamilies);
        updated.remove(index);
        draft().hallwayFamilies = List.copyOf(updated);
    }

    public int addHallwayFamily() {
        String openingProfileId = firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind.BRANCH)
                .orElseGet(() -> draft().openingProfiles.isEmpty() ? "branch_opening" : draft().openingProfiles.getFirst().profileId());
        java.util.ArrayList<MKHallwayFamilyDefinition> updated = new java.util.ArrayList<>(draft().hallwayFamilies);
        updated.add(new MKHallwayFamilyDefinition(
                nextUniqueHallwayFamilyId(),
                openingProfileId,
                5,
                3,
                3,
                0,
                false,
                true,
                null
        ));
        draft().hallwayFamilies = List.copyOf(updated);
        return draft().hallwayFamilies.size() - 1;
    }

    public MKWorkspaceMaterialPalette basePalette() {
        Draft draft = draft();
        return new MKWorkspaceMaterialPalette(
                draft.floorBlock,
                draft.wallBlock,
                draft.ceilingBlock,
                draft.stairBlock,
                draft.slabBlock,
                draft.ladderBlock
        );
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
                draft().hallwayFamilies,
                now,
                now,
                List.of()
        );
    }

    public void snapDraftVerticalAccess() {
        int[] footprint = getDraftVerticalAccessFootprint();
        draft().shaftSize = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(footprint[0], footprint[1], draft().shaftSize);
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
        if (updatedExit.pathKind() != MKWorkspaceHorizontalExitPathKind.BRANCH) {
            for (int i = 0; i < exits.size(); i++) {
                if (i == exitIndex) {
                    continue;
                }
                MKWorkspaceFamilyHorizontalExitDefinition existingExit = exits.get(i);
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
                family.paletteOverride()
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
                family.paletteOverride()
        ));
    }

    public int addFamilyExitAtDirection(int familyIndex, Direction direction) {
        MKTowerWorkspaceFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
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
                family.paletteOverride()
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
                workspace.hallwayFamilies(),
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
                source.hallwayFamilies().stream()
                        .map(hallway -> materialSource.hallwayFamilies().stream()
                                .filter(requested -> requested.hallwayId().equals(hallway.hallwayId()))
                                .findFirst()
                                .map(requested -> copyHallwayFamily(hallway, requested.paletteOverrideOpt()))
                                .orElse(hallway))
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
                draft().stairBlock,
                draft().slabBlock,
                draft().ladderBlock
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
        int topVoidMargin = profile.category() == MKTowerWorkspaceCategory.TOP_CAP ? Math.max(0, profile.topVoidMargin()) : 0;
        int bottomVoidMargin = profile.category() == MKTowerWorkspaceCategory.BASEMENT_CAP ?
                Math.max(0, profile.bottomVoidMargin()) : 0;
        return new MKTowerWorkspaceCategoryProfile(
                profile.category(),
                roomWidth,
                roomLength,
                fullHeight,
                Math.max(0, profile.minMainPathPieces()),
                Math.max(Math.max(0, profile.minMainPathPieces()), profile.maxMainPathPieces()),
                Math.max(0, Math.min(MKTowerWorkspaceCategoryProfile.DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP,
                        profile.maxBranchPiecesBeforeCap())),
                topVoidMargin,
                bottomVoidMargin,
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
        MKTowerWorkspaceCategoryProfile profile = getCategoryProfile(family.category());
        int roomWidth = normalizeFamilyWidthForCategory(family.roomWidth(), family.supportsVerticalAccess(), profile);
        int roomLength = normalizeFamilyLengthForCategory(family.roomLength(), family.supportsVerticalAccess(), profile);
        int roomHeight = normalizeFamilyHeightForCategory(family.roomHeight(), family.supportsVerticalAccess(), profile);
        MKTowerWorkspaceFamilyDefinition normalizedGeometry = new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.supportsVerticalAccess(),
                roomWidth,
                roomLength,
                roomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.paletteOverride()
        );
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.supportsVerticalAccess(),
                roomWidth,
                roomLength,
                roomHeight,
                family.horizontalExtrusionMode(),
                family.horizontalExits().stream()
                        .map(exit -> new MKWorkspaceFamilyHorizontalExitDefinition(
                                exit.direction(),
                                exit.pathKind(),
                                exit.openingProfileId(),
                                exit.connectionMode(),
                                clampSideOffset(normalizedGeometry, exit.direction(), exit.openingProfileId(), exit.sideOffset()),
                                clampVerticalOffset(normalizedGeometry, exit.openingProfileId(), exit.verticalOffset())
                        ))
                        .toList(),
                family.paletteOverride()
        );
    }

    public int normalizeFamilyWidthForCategory(int requestedWidth, boolean supportsVerticalAccess,
                                               MKTowerWorkspaceCategoryProfile profile) {
        int width = Math.max(3, makeOdd(requestedWidth));
        return supportsVerticalAccess ? Math.max(width, draft().shaftSize) : width;
    }

    public int normalizeFamilyLengthForCategory(int requestedLength, boolean supportsVerticalAccess,
                                                MKTowerWorkspaceCategoryProfile profile) {
        int length = Math.max(3, makeOdd(requestedLength));
        return supportsVerticalAccess ? Math.max(length, draft().shaftSize) : length;
    }

    public int normalizeFamilyHeightForCategory(int requestedHeight, boolean supportsVerticalAccess,
                                                MKTowerWorkspaceCategoryProfile profile) {
        if (supportsVerticalAccess) {
            return profile.fullHeight();
        }
        return Math.max(MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT, Math.min(requestedHeight, profile.fullHeight()));
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

    private String nextUniqueHallwayFamilyId() {
        int index = 1;
        while (true) {
            String candidate = "hallway_" + index;
            boolean used = draft().hallwayFamilies.stream().anyMatch(hallway -> hallway.hallwayId().equals(candidate));
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
                profile.topVoidMargin(),
                profile.bottomVoidMargin(),
                paletteOverride.orElse(null)
        );
    }

    public MKTowerWorkspaceFamilyDefinition copyFamilyDefinition(MKTowerWorkspaceFamilyDefinition family,
                                                                 Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                paletteOverride.orElse(null)
        );
    }

    public MKHallwayFamilyDefinition copyHallwayFamily(MKHallwayFamilyDefinition hallway,
                                                       Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKHallwayFamilyDefinition(
                hallway.hallwayId(),
                hallway.openingProfileId(),
                hallway.length(),
                hallway.interiorWidth(),
                hallway.interiorHeight(),
                hallway.slopeDelta(),
                hallway.allowOnMainPath(),
                hallway.allowOnBranchPath(),
                paletteOverride.orElse(null)
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
        public int shaftSize;
        public MKVerticalAccessPlacement verticalAccessPlacement;
        public int shellMargin;
        public int exteriorAirMargin;
        public int previewMargin;
        public MKWorkspaceStairMode stairMode;
        public MKWorkspaceStairRiseType stairRiseType;
        public int stairWidth;
        public ResourceLocation floorBlock;
        public ResourceLocation wallBlock;
        public ResourceLocation ceilingBlock;
        public ResourceLocation stairBlock;
        public ResourceLocation slabBlock;
        public ResourceLocation ladderBlock;
        public int mainFloors;
        public int basementFloors;
        public boolean topCapApproachEnabled;
        public boolean basementCapApproachEnabled;
        public List<MKTowerWorkspaceCategoryProfile> categoryProfiles;
        public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions;
        public List<MKHorizontalOpeningProfile> openingProfiles;
        public List<MKHallwayFamilyDefinition> hallwayFamilies;
    }
}
