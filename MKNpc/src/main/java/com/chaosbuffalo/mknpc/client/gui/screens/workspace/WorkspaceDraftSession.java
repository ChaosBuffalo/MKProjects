package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
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
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPaletteMerge;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPathSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyGroupSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceRegionSchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceRoleSchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceTopologySchema;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
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
    final WorkspaceDraftViewState viewState = new WorkspaceDraftViewState();

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
        draft.topologyProfile = workspace != null ? workspace.topologyProfile() :
                MKTowerWorkspacePlanner.defaultTopologyProfile();
        draft.stairMode = workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO;
        draft.stairRiseType = workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.MIXED;
        draft.stairWidth = workspace != null ? workspace.stairConfig().stairWidth() : 1;
        draft.palette = workspace != null ? workspace.palette() : MKWorkspaceMaterialPalette.defaultPalette();
        draft.verticalAccessPlacement = workspace != null ? workspace.verticalAccessSpec().placement() : MKVerticalAccessPlacement.CENTER;
        draft.shellMargin = workspace != null ? workspace.shellMargin() : 1;
        draft.exteriorAirMargin = workspace != null ? workspace.exteriorAirMargin() : 2;
        draft.previewMargin = workspace != null ? workspace.previewMargin() : 4;
        draft.familyDefinitions = List.copyOf(workspace != null ? workspace.familyDefinitions() :
                MKWorkspaceRoomFamilyDefinition.createDefaults());
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
                "  |  topology " + draft.topologyProfile.plannerId() +
                "  |  stacks " + draft.topologyProfile.verticalStackSettings().size() +
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

    public ResourceLocation topologyPlannerId() {
        return draft().topologyProfile.plannerId();
    }

    public void topologyPlannerId(ResourceLocation value) {
        draft().topologyProfile = plannerAdapterFor(value).profileForSwitch(this);
        seedDefaultsForTopology();
    }

    public void topologyDefaultHeight(int value) {
        plannerAdapter().applyDefaultHeight(this, Math.max(3, value));
    }

    public void resetCurrentTopologyDefaults() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceStairAuthoringConfig defaultStairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        draft().stairMode = defaultStairConfig.mode();
        draft().stairRiseType = defaultStairConfig.riseType();
        draft().stairWidth = defaultStairConfig.stairWidth();
        draft().verticalAccessPlacement = MKWorkspaceVerticalAccessSpec.defaultSpec().placement();
        draft().shaftSize = MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
        draft().openingProfiles = MKHorizontalOpeningProfile.createDefaults(dimensions);
        plannerAdapter().resetDefaults(this, dimensions);
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
        return plannerAdapter().isActiveTopologySlot(this, topologySlotId);
    }

    public List<Integer> familyIndexesForTopologySlot(String topologySlotId) {
        List<MKWorkspaceRoomFamilyDefinition> families = draft().familyDefinitions;
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

    public List<MKWorkspaceRoomFamilyDefinition> familyDefinitions() {
        return List.copyOf(draft().familyDefinitions);
    }

    public int addFamilyDefinition(MKWorkspaceSlotSchema slot) {
        Optional<MKWorkspaceRoomFamilyDefinition> source = draft().familyDefinitions.stream()
                .filter(family -> family.topologySlotId().equals(slot.slotId()))
                .findFirst()
                .or(() -> plannerAdapter().sharedFamilySource(this, slot.slotId()));
        MKWorkspaceRoomFamilyDefinition family = source
                .map(existing -> copyFamilyForTopologySlot(existing, slot.slotId()))
                .orElseGet(() -> defaultFamilyForTopologySlot(slot));
        java.util.ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
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

    public MKWorkspaceMaterialPalette resolveTopologyGroupPalette(String topologyGroupId) {
        MKWorkspaceMaterialPalette resolved = palette();
        for (String ancestorId : MKWorkspaceTopologyGroupSettings.hierarchy(topologyGroupId)) {
            Optional<MKWorkspacePaletteOverride> override = draft().topologyProfile.topologyGroupPaletteOverride(ancestorId);
            if (override.isPresent()) {
                resolved = override.get().resolve(resolved);
            }
        }
        return resolved;
    }

    public MKWorkspaceMaterialPalette resolveParentTopologyGroupPalette(String topologyGroupId) {
        List<String> hierarchy = MKWorkspaceTopologyGroupSettings.hierarchy(topologyGroupId);
        if (hierarchy.size() <= 1) {
            return palette();
        }
        return resolveTopologyGroupPalette(hierarchy.get(hierarchy.size() - 2));
    }

    public MKWorkspaceMaterialPalette resolveFamilyInheritedPalette(MKWorkspaceRoomFamilyDefinition family) {
        String topologyGroupId = plannerAdapter().topologyGroupIdForTopologySlot(this, family.topologySlotId())
                .orElse(family.slotMetadata().topologyGroupId());
        return resolveTopologyGroupPalette(topologyGroupId);
    }

    public Optional<MKWorkspacePaletteOverride> topologyGroupPaletteOverride(String topologyGroupId) {
        return draft().topologyProfile.topologyGroupPaletteOverride(topologyGroupId);
    }

    public void topologyGroupPaletteOverride(String topologyGroupId,
                                             Optional<MKWorkspacePaletteOverride> paletteOverride) {
        draft().topologyProfile = draft().topologyProfile.withTopologyGroupPaletteOverride(topologyGroupId,
                paletteOverride == null ? Optional.empty() : paletteOverride);
    }

    public MKWorkspaceFoundationPolicy resolveFamilyInheritedFoundation(MKWorkspaceRoomFamilyDefinition family) {
        String stackId = stackIdForFamily(family);
        if (stackId.isBlank()) {
            return MKWorkspaceFoundationPolicy.none();
        }
        return verticalStackSettings(stackId).foundationPolicy();
    }

    public boolean familyHasTopologyStack(MKWorkspaceRoomFamilyDefinition family) {
        return verticalStackIdForTopologySlot(family.topologySlotId()).isPresent();
    }

    public MKStructureWorkspace buildWorkspaceDraft() {
        snapDraftVerticalAccess();
        MKWorkspaceDimensions dimensions = legacyDimensionsFromTopologySettings();
        MKWorkspaceMaterialPalette palette = palette();
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
        draft().familyDefinitions = draft().familyDefinitions.stream()
                .map(this::normalizeFamilyDefinition)
                .toList();
    }

    MKWorkspaceFloorTopologySettings floorTopologySettings(String stackId, String floorRole) {
        return draft().topologyProfile.floorTopologySettingsOrDefault(stackId, floorRole);
    }

    void replaceFloorTopologySettings(MKWorkspaceFloorTopologySettings settings) {
        draft().topologyProfile = draft().topologyProfile.withFloorTopologySettings(settings);
        topologyGroupForFloorRole(settings.floorRole()).ifPresent(topologyGroupId ->
                draft().topologyProfile = draft().topologyProfile.withPathSettings(
                        new MKWorkspaceTopologyPathSettings(
                                topologyGroupId,
                                settings.minMainPathPieces(),
                                settings.maxMainPathPieces(),
                                settings.maxBranchPiecesBeforeCap())));
    }

    private Optional<String> topologyGroupForFloorRole(String floorRole) {
        return plannerAdapter().topologyGroupIdForFloorRole(this, floorRole);
    }

    public void replaceFamilyDefinition(int index, MKWorkspaceRoomFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.set(index, normalizeFamilyDefinition(preserveFamilyMetadata(updated.get(index), updatedFamily)));
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void replaceFamilyTopologySlotId(int index, String topologySlotId) {
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(), topologySlotMetadata(topologySlotId, family.slotMetadata()),
                family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                family.foundationPolicyOverride(), family.paletteOverride()));
    }

    public void replaceFamilyVerticalAccessGroupId(int index, String verticalAccessGroupId) {
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(index);
        replaceFamilyDefinitionExact(index, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(),
                family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(),
                foundationPolicy.orElse(null), family.paletteOverride()));
    }

    private void replaceFamilyDefinitionExact(int index, MKWorkspaceRoomFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.set(index, normalizeFamilyDefinition(updatedFamily));
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void removeFamilyDefinition(int index) {
        java.util.ArrayList<MKWorkspaceRoomFamilyDefinition> updated = new java.util.ArrayList<>(draft().familyDefinitions);
        updated.remove(index);
        draft().familyDefinitions = List.copyOf(updated);
    }

    public void replaceFamilyExit(int familyIndex, int exitIndex, MKWorkspaceFamilyHorizontalExitDefinition updatedExit) {
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
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
                            existingExit.verticalOffset(),
                            existingExit.horizontalExtrusionModeOverride()
                    ));
                }
            }
        }
        replaceFamilyDefinition(familyIndex, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
    }

    public void updateFamilyExitOffsets(int familyIndex, int exitIndex, Integer sideOffset, Integer verticalOffset) {
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        MKWorkspaceFamilyHorizontalExitDefinition currentExit = family.horizontalExits().get(exitIndex);
        int nextSideOffset = sideOffset == null ? currentExit.sideOffset() : sideOffset;
        int nextVerticalOffset = verticalOffset == null ? currentExit.verticalOffset() : verticalOffset;
        replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                currentExit.direction(),
                currentExit.pathKind(),
                currentExit.openingProfileId(),
                currentExit.connectionMode(),
                clampSideOffset(family, currentExit.direction(), currentExit.openingProfileId(), nextSideOffset),
                clampVerticalOffset(family, currentExit.openingProfileId(), nextVerticalOffset),
                currentExit.horizontalExtrusionModeOverride()
        ));
    }

    public void removeFamilyExit(int familyIndex, int exitIndex) {
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        exits.remove(exitIndex);
        replaceFamilyDefinition(familyIndex, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
    }

    public int addFamilyExitAtDirection(int familyIndex, Direction direction) {
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        if (direction.getAxis().isVertical()) {
            exits.add(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(direction));
            replaceFamilyDefinition(familyIndex, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
        replaceFamilyDefinition(familyIndex, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(), family.slotMetadata(), family.verticalAccessGroupId(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits,
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()
        ));
        return exits.size() - 1;
    }

    public int addFamilyBranchExitAtDirection(int familyIndex, Direction direction) {
        if (direction.getAxis().isVertical()) {
            return -1;
        }
        MKWorkspaceRoomFamilyDefinition family = draft().familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        MKWorkspaceHorizontalExitPathKind pathKind = MKWorkspaceHorizontalExitPathKind.BRANCH;
        exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                pathKind,
                firstCompatibleOpeningProfileId(pathKind)
                        .orElseGet(() -> draft().openingProfiles.isEmpty() ? "opening_1" : draft().openingProfiles.getFirst().profileId())
        ));
        replaceFamilyDefinition(familyIndex, MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
                MKWorkspaceTopologyPaletteMerge.preserveMaterialSettings(source.topologyProfile(),
                        materialSource.topologyProfile()),
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
                stairConfig.stairWidth()
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
                draft().stairWidth
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
        MKWorkspaceVerticalStackSettings primaryStack = primaryDimensionStackSettings();
        return new MKWorkspaceDimensions(
                primaryStack.width(),
                primaryStack.length(),
                primaryStack.entryHeight(),
                primaryStack.mainHeight(),
                primaryStack.basementHeight(),
                primaryStack.shaftSize(),
                deriveDoorwayWidth(),
                deriveDoorwayHeight()
        );
    }

    private MKWorkspaceVerticalStackSettings primaryDimensionStackSettings() {
        return verticalStackSettings(plannerAdapter().primaryDimensionStackId());
    }

    public Optional<MKHorizontalOpeningProfile> getOpeningProfile(String profileId) {
        return draft().openingProfiles.stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst();
    }

    private MKWorkspaceVerticalAccessSpec currentVerticalAccessSpec() {
        return new MKWorkspaceVerticalAccessSpec(draft().shaftSize, draft().verticalAccessPlacement, makeStairConfig());
    }

    public int[] verticalAccessFootprint() {
        MKWorkspaceVerticalStackSettings settings = primaryDimensionStackSettings();
        return new int[]{settings.width(), settings.length()};
    }

    private int[] getDraftVerticalAccessFootprint() {
        return verticalAccessFootprint();
    }

    MKWorkspaceVerticalStackSettings verticalStackSettings(String stackId) {
        MKWorkspaceVerticalStackSettings settings = draft().topologyProfile.verticalStackSettings(stackId)
                .orElseGet(() -> plannerAdapter().defaultVerticalStackSettings(this, stackId));
        if (draft().topologyProfile.verticalStackSettings(stackId).isEmpty()) {
            replaceVerticalStackSettings(settings);
        }
        return settings;
    }

    void replaceVerticalStackSettings(MKWorkspaceVerticalStackSettings settings) {
        draft().topologyProfile = draft().topologyProfile.withVerticalStackSettings(settings);
        plannerAdapter().syncDraftVerticalAccessFromStack(this, settings);
    }

    void replaceVerticalStackSettingsWithNormalizedFloorCounts(MKWorkspaceVerticalStackSettings settings) {
        int normalizedMain = normalizeVerticalStackMainFloorCount(settings, settings.mainFloors(),
                settings.basementFloors());
        int normalizedBasement = normalizeVerticalStackBasementFloorCount(settings, settings.basementFloors(),
                normalizedMain);
        replaceVerticalStackSettings(settings.withMainFloors(normalizedMain).withBasementFloors(normalizedBasement));
    }

    List<Integer> allowedVerticalStackMainFloorCounts(MKWorkspaceVerticalStackSettings settings, int basementFloors) {
        return plannerAdapter().allowedVerticalStackMainFloorCounts(this, settings, basementFloors);
    }

    List<Integer> allowedVerticalStackBasementFloorCounts(MKWorkspaceVerticalStackSettings settings, int mainFloors) {
        return plannerAdapter().allowedVerticalStackBasementFloorCounts(this, settings, mainFloors);
    }

    int normalizeVerticalStackMainFloorCount(MKWorkspaceVerticalStackSettings settings, int requestedCount,
                                          int basementFloors) {
        return allowedVerticalStackMainFloorCounts(settings, basementFloors).stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    int normalizeVerticalStackBasementFloorCount(MKWorkspaceVerticalStackSettings settings, int requestedCount,
                                              int mainFloors) {
        return allowedVerticalStackBasementFloorCounts(settings, mainFloors).stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    public MKWorkspaceRoomFamilyDefinition normalizeFamilyDefinition(MKWorkspaceRoomFamilyDefinition family) {
        Optional<String> verticalStackId = verticalStackIdForTopologySlot(family.topologySlotId());
        int maxRoomHeight = maxRoomHeightForFamilyNormalization(verticalStackId);
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = verticalAccessSpecForFamilyNormalization(verticalStackId);
        boolean stackBacked = verticalStackId.isPresent();
        int roomWidth = stackBacked && family.roomWidth() <= 0 ? 0 :
                normalizeFamilyWidth(family.roomWidth(), family.supportsVerticalAccess(), verticalAccessSpec);
        int roomLength = stackBacked && family.roomLength() <= 0 ? 0 :
                normalizeFamilyLength(family.roomLength(), family.supportsVerticalAccess(), verticalAccessSpec);
        int roomHeight = stackBacked && family.roomHeight() <= 0 ? 0 :
                normalizeFamilyHeight(family.roomHeight(), family.supportsVerticalAccess(), maxRoomHeight);
        int resolvedRoomWidth = roomWidth > 0 ? roomWidth :
                verticalStackId.map(stackId -> verticalStackSettings(stackId).width()).orElse(roomWidth);
        int resolvedRoomLength = roomLength > 0 ? roomLength :
                verticalStackId.map(stackId -> verticalStackSettings(stackId).length()).orElse(roomLength);
        int resolvedRoomHeight = roomHeight > 0 ? roomHeight :
                verticalStackId.map(stackId -> verticalStackSettings(stackId).heightForTopologySlot(family.topologySlotId()))
                        .orElse(roomHeight);
        int availableVoidMargin = Math.max(0, resolvedRoomHeight - MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
        int topVoidMargin = familyAllowsTopVoidMargin(family) ?
                clamp(family.topVoidMargin(), 0, availableVoidMargin) : 0;
        int bottomVoidMargin = familyAllowsBottomVoidMargin(family) ?
                clamp(family.bottomVoidMargin(), 0, availableVoidMargin - topVoidMargin) : 0;
        MKWorkspaceRoomFamilyDefinition normalizedGeometry = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
                                        clampVerticalOffset(normalizedGeometry, exit.openingProfileId(), exit.verticalOffset()),
                                        exit.horizontalExtrusionModeOverride()
                                ))
                        .toList(),
                topVoidMargin,
                bottomVoidMargin,
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private boolean familyAllowsTopVoidMargin(MKWorkspaceRoomFamilyDefinition family) {
        if (!family.supportsVerticalAccess()) {
            return true;
        }
        return plannerAdapter().verticalAccessFamilyAllowsTopVoidMargin(this, family);
    }

    private boolean familyAllowsBottomVoidMargin(MKWorkspaceRoomFamilyDefinition family) {
        if (!family.supportsVerticalAccess()) {
            return true;
        }
        return plannerAdapter().verticalAccessFamilyAllowsBottomVoidMargin(this, family);
    }

    private int maxRoomHeightForFamilyNormalization(Optional<String> verticalStackId) {
        if (verticalStackId.isEmpty()) {
            return primaryDimensionStackSettings().height();
        }
        return verticalStackSettings(verticalStackId.get()).height();
    }

    private MKWorkspaceVerticalAccessSpec verticalAccessSpecForFamilyNormalization(Optional<String> verticalStackId) {
        if (verticalStackId.isEmpty()) {
            return currentVerticalAccessSpec();
        }
        MKWorkspaceVerticalStackSettings settings = verticalStackSettings(verticalStackId.get());
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
        plannerAdapter().seedDefaults(this, MKWorkspaceDimensions.defaultDimensions());
    }

    void ensureRoomFamiliesForVerticalStack(List<MKWorkspaceRoomFamilyDefinition> updated, String stackId) {
        topologySchema().slots().stream()
                .filter(slot -> slot.slotId().startsWith(stackId + "."))
                .filter(slot -> updated.stream().noneMatch(family -> family.topologySlotId().equals(slot.slotId())))
                .map(slot -> plannerAdapter().sharedFamilySource(this, slot.slotId())
                        .map(existing -> copyFamilyForTopologySlot(existing, slot.slotId()))
                        .orElseGet(() -> defaultFamilyForTopologySlot(slot)))
                .forEach(updated::add);
    }

    private String stackIdForFamily(MKWorkspaceRoomFamilyDefinition family) {
        return verticalStackIdForTopologySlot(family.topologySlotId()).orElse("");
    }

    public int clampSideOffset(MKWorkspaceRoomFamilyDefinition family, Direction direction, String openingProfileId,
                               int sideOffset) {
        return clamp(sideOffset, minSideOffset(family, direction, openingProfileId),
                maxSideOffset(family, direction, openingProfileId));
    }

    public int clampVerticalOffset(MKWorkspaceRoomFamilyDefinition family, String openingProfileId,
                                   int verticalOffset) {
        return clamp(verticalOffset, 0, maxVerticalOffset(family, openingProfileId));
    }

    public int minSideOffset(MKWorkspaceRoomFamilyDefinition family, Direction direction, String openingProfileId) {
        int sideLength = getExitSideLength(family, direction);
        int halfOpening = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingWidth)
                .orElse(1) / 2;
        return halfOpening - (sideLength / 2);
    }

    public int maxSideOffset(MKWorkspaceRoomFamilyDefinition family, Direction direction, String openingProfileId) {
        int sideLength = getExitSideLength(family, direction);
        int halfOpening = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingWidth)
                .orElse(1) / 2;
        return (sideLength - 1 - halfOpening) - (sideLength / 2);
    }

    private int getExitSideLength(MKWorkspaceRoomFamilyDefinition family, Direction direction) {
        return direction == Direction.NORTH || direction == Direction.SOUTH ?
                resolvedFamilyRoomWidth(family) : resolvedFamilyRoomLength(family);
    }

    public int maxVerticalOffset(MKWorkspaceRoomFamilyDefinition family, String openingProfileId) {
        int openingHeight = getOpeningProfile(openingProfileId)
                .map(MKHorizontalOpeningProfile::openingHeight)
                .orElse(1);
        return Math.max(0, resolvedFamilyRoomHeight(family) - openingHeight);
    }

    public int resolvedFamilyRoomWidth(MKWorkspaceRoomFamilyDefinition family) {
        if (family.roomWidth() > 0) {
            return family.roomWidth();
        }
        return verticalStackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> verticalStackSettings(stackId).width())
                .orElse(family.roomWidth());
    }

    public int resolvedFamilyRoomLength(MKWorkspaceRoomFamilyDefinition family) {
        if (family.roomLength() > 0) {
            return family.roomLength();
        }
        return verticalStackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> verticalStackSettings(stackId).length())
                .orElse(family.roomLength());
    }

    public int resolvedFamilyRoomHeight(MKWorkspaceRoomFamilyDefinition family) {
        if (family.roomHeight() > 0) {
            return family.roomHeight();
        }
        return verticalStackIdForTopologySlot(family.topologySlotId())
                .map(stackId -> verticalStackSettings(stackId).heightForTopologySlot(family.topologySlotId()))
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

    private WorkspacePlannerDraftAdapter plannerAdapter() {
        return plannerAdapterFor(topologyPlannerId());
    }

    private WorkspacePlannerDraftAdapter plannerAdapterFor(ResourceLocation plannerId) {
        return WorkspacePlannerClientRegistry.getDraftAdapter(plannerId);
    }

    private MKWorkspaceTopologySchema topologySchema() {
        return MKWorkspacePlannerRegistry.withBuiltIns()
                .plannerFor(topologyPlannerId())
                .schema();
    }

    Optional<MKWorkspaceSlotSchema> topologySlot(String topologySlotId) {
        return topologySchema().slots().stream()
                .filter(slot -> slot.slotId().equals(topologySlotId))
                .findFirst();
    }

    Optional<String> verticalStackIdForTopologySlot(String topologySlotId) {
        return plannerAdapter().verticalStackIdForTopologySlot(this, topologySlotId);
    }

    private MKWorkspaceRoomFamilyDefinition copyFamilyForTopologySlot(MKWorkspaceRoomFamilyDefinition existing,
                                                                       String topologySlotId) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                nextUniqueFamilyBaseName(),
                topologySlotMetadata(topologySlotId, existing.slotMetadata()),
                existing.supportsVerticalAccess() ?
                        verticalStackIdForTopologySlot(topologySlotId)
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

    MKWorkspaceRoomFamilyDefinition defaultFamilyForTopologySlot(MKWorkspaceSlotSchema slot) {
        MKWorkspaceVerticalStackSettings settings = verticalStackIdForTopologySlot(slot.slotId())
                .map(this::verticalStackSettings)
                .orElseGet(this::primaryDimensionStackSettings);
        MKWorkspaceTopologySlotMetadata slotMetadata = topologySlotMetadata(slot);
        boolean supportsVerticalAccess = topologySlotSupportsVerticalAccess(slot);
        String verticalAccessGroupId = verticalStackIdForTopologySlot(slot.slotId()).orElse(slot.slotId());
        List<MKWorkspaceFamilyHorizontalExitDefinition> exits =
                verticalStackIdForTopologySlot(slot.slotId()).isPresent() ? List.of() : defaultHorizontalExitsForNewFamily();
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                nextUniqueFamilyBaseName(),
                slotMetadata,
                supportsVerticalAccess ? verticalAccessGroupId : "",
                supportsVerticalAccess,
                settings.width(),
                settings.length(),
                settings.heightForTopologySlot(slot.slotId()),
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
        Optional<MKWorkspaceTopologySlotMetadata> adapterMetadata = plannerAdapter().topologySlotMetadata(this, slot);
        if (adapterMetadata.isPresent()) {
            return adapterMetadata.get();
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
                .filter(profile -> isCompatibleOpeningProfile(pathKind, profile))
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
                .map(profile -> isCompatibleOpeningProfile(pathKind, profile))
                .orElse(false);
    }

    private boolean isCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind,
                                               MKHorizontalOpeningProfile profile) {
        if (pathKind == MKWorkspaceHorizontalExitPathKind.INGRESS) {
            return profile.allowOnMainPath();
        }
        return pathKind.usesMainPath() ? profile.allowOnMainPath() : profile.allowOnBranchPath();
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

    public MKWorkspaceRoomFamilyDefinition copyFamilyDefinition(MKWorkspaceRoomFamilyDefinition family,
                                                                 Optional<MKWorkspacePaletteOverride> paletteOverride) {
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

    private MKWorkspaceRoomFamilyDefinition preserveFamilyMetadata(MKWorkspaceRoomFamilyDefinition existing,
                                                                    MKWorkspaceRoomFamilyDefinition updated) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
        public List<MKWorkspaceRoomFamilyDefinition> familyDefinitions;
        public List<MKHorizontalOpeningProfile> openingProfiles;
        public List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies;
    }
}
