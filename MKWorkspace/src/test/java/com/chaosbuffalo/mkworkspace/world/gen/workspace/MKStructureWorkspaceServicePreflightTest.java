package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorLinkGenerationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceLayerStateService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceTopologySchema;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKStructureWorkspaceServicePreflightTest {
    private static final ResourceLocation RENAMED_BASE_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc_test", "renamed_base_catalog");
    private final MKStructureWorkspaceService service = new MKStructureWorkspaceService();

    static {
        MKWorkspacePlannerRegistry.registerShared(new RenamedBaseCatalogPlanner());
    }

    @Test
    void preflightWorkspaceUpdateAggregatesFloorTopologyChanges() {
        MKFloorTopologySettings previous = settings("tower.primary", "main_floor");
        MKFloorTopologySettings updated = previous.withManualHallwayLeadInPieces(6);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of());
        existing = withTopologyProfile(existing, existing.topologyProfile().withFloorTopologySettings(previous));
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updated));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("regenerate_hallway_routing", preflight.report().recommendedOperation());
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.HALLWAY_PIECES));
        assertTrue(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING).orElseThrow().dirty());
    }

    @Test
    void unchangedDraftReturnsSerializableNoChangeReport() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(workspace, workspace, 123L);

        assertEquals("none", preflight.report().recommendedOperation());
        assertEquals("none", MKWorkspaceMutationPreflight.CODEC.parse(
                com.mojang.serialization.JsonOps.INSTANCE,
                MKWorkspaceMutationPreflight.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, preflight)
                        .getOrThrow()
        ).getOrThrow().report().recommendedOperation());
    }

    @Test
    void lockedInvalidatedLayersReportsBlockedHallwayLayer() {
        MKFloorTopologySettings previous = settings("tower.primary", "main_floor");
        MKFloorTopologySettings updated = previous.withManualHallwayLeadInPieces(6);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        existing = withTopologyProfile(existing, existing.topologyProfile().withFloorTopologySettings(previous));
        existing = new MKWorkspaceLayerStateService()
                .lockLayers(new MKWorkspaceLayerStateService().ensureLayerStates(existing, 100L),
                        List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING));
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updated));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals(List.of(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING),
                service.lockedInvalidatedLayers(existing, preflight.report()));
    }

    @Test
    void linkRenderingOnlyPreflightIsSafeMetadataUpdate() {
        MKFloorTopologySettings previous = settings("tower.primary", "main_floor");
        MKFloorTopologySettings updated = previous
                .withLinkGenerationMode(MKFloorLinkGenerationMode.DECAYING_HALLWAY)
                .withLinkDecay(0.65f);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(floorPiece("room_00_template", "room_00",
                        MKWorkspacePlannerId.of("keep.main.floor_plan.room.room_00"))));
        existing = withTopologyProfile(existing, existing.topologyProfile().withFloorTopologySettings(previous));
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updated));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("refresh_link_rendering", preflight.report().recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE, preflight.report().safety());
        assertTrue(service.canRefreshLinkRenderingOnly(existing, requested));
        assertTrue(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS).orElseThrow().dirty());
        assertTrue(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.RUNTIME_METADATA).orElseThrow().dirty());
        assertFalse(preflight.workspaceWithDirtyLayers()
                .layerState(MKWorkspaceGeneratedLayer.HALLWAY_ROUTING).orElseThrow().dirty());
    }

    @Test
    void broadTopologyPreflightReportsOrphanedTemplateBindings() {
        MKWorkspacePlannerId orphanedId = MKWorkspacePlannerId.of("keep.main.floor_plan.room.removed_00");
        MKFloorTopologySettings previous = settings("tower.primary", "main_floor");
        MKFloorTopologySettings updated = previous.withMaxMainPathPieces(3);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(floorPiece("removed_00_template", "removed_00", orphanedId)));
        existing = withTopologyProfile(existing, existing.topologyProfile().withFloorTopologySettings(previous));
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updated));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertTrue(preflight.report().hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        assertEquals(List.of(orphanedId), preflight.report().orphanedTemplateBindings());
    }

    @Test
    void workspaceForUpdatePreservesRequestedInsertFamilies() {
        List<MKWorkspaceInsertFamilyDefinition> insertFamilies = List.of(
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("walled_keep_main_link_insert", 5, 5, 3));
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(floorPiece("room_00_template", "room_00",
                        MKWorkspacePlannerId.of("keep.main.floor_plan.room.room_00"))));
        existing = new MKWorkspaceLayerStateService().ensureLayerStates(existing, 100L);
        MKStructureWorkspace requested = withInsertFamilies(existing, insertFamilies);

        MKStructureWorkspace updated = service.workspaceForUpdate(existing, requested, existing.pieces(), 456L);

        assertEquals(insertFamilies, updated.insertFamilies());
        assertEquals(existing.pieces(), updated.pieces());
        assertEquals(existing.layerStates(), updated.layerStates());
        assertEquals(existing.createdAt(), updated.createdAt());
        assertEquals(456L, updated.updatedAt());
    }

    @Test
    void catalogRelayoutPreservesVariantsWhenCanonicalBaseNameChanges() {
        MKStructureWorkspace existing = withTopologyProfile(MKStructureWorkspace.createDraft(BlockPos.ZERO),
                renamedBaseProfile());
        existing = existing.withPieces(List.of(
                variantAwarePiece("old_floor_room_template", "old_floor_room", 0,
                        MKWorkspacePlannerId.of("renamed.base.old_floor_room")),
                variantAwarePiece("old_floor_room_1", "old_floor_room", 1,
                        MKWorkspacePlannerId.of("renamed.base.old_floor_room").child("variant_1"))
        ));
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("trigger_new_base", 5, 5, 3)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation());
        assertTrue(preflight.report().warnings().contains("2 physical authored templates will be preserved."));
        assertEquals(2, preflight.report().relayoutImpacts().stream()
                .filter(impact -> "preserved".equals(impact.outcome()) ||
                        "moved".equals(impact.outcome()) ||
                        "expanded".equals(impact.outcome()))
                .count());
        assertFalse(preflight.report().warnings().stream()
                .anyMatch(warning -> warning.contains("removed physical template slots")));
    }

    @Test
    void catalogRelayoutDoesNotFallbackDestructiveForDuplicateLegacyVariantPlannerIds() {
        MKWorkspacePlannerId duplicatePlannerId = MKWorkspacePlannerId.of("legacy.duplicate.floor_room");
        MKStructureWorkspace existing = withTopologyProfile(MKStructureWorkspace.createDraft(BlockPos.ZERO),
                renamedBaseProfile());
        existing = existing.withPieces(List.of(
                variantAwarePiece("old_floor_room_template", "old_floor_room", 0, duplicatePlannerId),
                variantAwarePiece("old_floor_room_1", "old_floor_room", 1, duplicatePlannerId)
        ));
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("trigger_new_base", 5, 5, 3)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation());
        assertTrue(preflight.report().warnings().contains("2 physical authored templates will be preserved."));
        assertEquals(2, preflight.report().relayoutImpacts().stream()
                .filter(impact -> "preserved".equals(impact.outcome()) ||
                        "moved".equals(impact.outcome()) ||
                        "expanded".equals(impact.outcome()))
                .count());
    }

    @Test
    void variantUtilitiesTargetPhysicalTemplatesWithDistinctMissingBehavior() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(
                        variantAwarePiece("room_without_variant_template", "room_without_variant", 0,
                                MKWorkspacePlannerId.of("utility.room_without_variant")),
                        variantAwarePiece("room_with_variant_template", "room_with_variant", 0,
                                MKWorkspacePlannerId.of("utility.room_with_variant")),
                        variantAwarePiece("room_with_variant_1", "room_with_variant", 1,
                                MKWorkspacePlannerId.of("utility.room_with_variant").child("variant_1")),
                        reuseAwarePiece("rotated_authoring_template", "rotated_authoring", true),
                        reuseAwarePiece("derived_hidden_template", "derived_hidden", false)
                ));

        assertEquals(List.of("room_without_variant", "room_with_variant", "rotated_authoring"),
                service.physicalTemplateBasePieceNames(workspace));
        assertEquals(List.of("room_without_variant", "rotated_authoring"),
                service.basePieceNamesWithoutPhysicalVariants(workspace));
    }

    @Test
    void walledKeepAddingFloorRoomUsesCatalogRelayoutInsteadOfFullRegenerate() {
        MKWalledKeepWorkspacePlanner planner = new MKWalledKeepWorkspacePlanner();
        MKStructureWorkspace existing = walledKeepWorkspace(planner);
        existing = withMainFloorExit(existing);
        List<MKWorkspacePieceDefinition> pieces = planner.createCanonicalPieces(existing).stream()
                .filter(piece -> !com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags
                        .isDerived(piece.tags()))
                .flatMap(piece -> List.of(plannedTemplatePiece(piece), plannedVariantPiece(piece)).stream())
                .toList();
        existing = existing.withPieces(pieces);
        MKFloorTopologySettings floorSettings = existing.topologyProfile()
                .floorTopologySettingsOrDefault("keep.center", "main_floor");
        MKFloorRoomProfile added = floorSettings.mainRoomProfiles().getFirst()
                .withIdentity("extra_main_room", "Extra Main Room");
        MKFloorTopologySettings updatedFloorSettings = floorSettings.withAddedRoomProfile(
                MKFloorRoomKind.MAIN_ROOM, added);
        MKStructureWorkspace requested = withTopologyProfile(existing,
                existing.topologyProfile().withFloorTopologySettings(updatedFloorSettings));
        requested = withCopiedPhysicalValueObjects(requested);

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation());
        assertTrue(preflight.report().relayoutImpacts().stream()
                .anyMatch(impact -> "new".equals(impact.outcome()) &&
                        impact.stableSlotKey().contains("floor.keep.center.main_floor.main_room.extra_main_room")),
                () -> "expected added main-floor main room in relayout impacts: " +
                        preflight.report().relayoutImpacts());
        assertFalse(preflight.report().warnings().stream()
                .anyMatch(warning -> warning.contains("full regeneration will clear")));
    }

    private static MKFloorTopologySettings settings(String stackId, String floorRole) {
        return new MKFloorTopologySettings(
                stackId,
                floorRole,
                1,
                1,
                0,
                MKHallwayLeadInMode.AUTO,
                1,
                true,
                true,
                false,
                MKFloorTopologySettings.DEFAULT_SPRAWL,
                Optional.empty(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private static MKStructureWorkspace walledKeepWorkspace(MKWalledKeepWorkspacePlanner planner) {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        return new MKStructureWorkspace(
                draft.id(),
                draft.anchor(),
                draft.namespace(),
                draft.structureName(),
                MKWalledKeepWorkspacePlanner.defaultTopologyProfile(false),
                draft.dimensions(),
                draft.palette(),
                draft.stairConfig(),
                draft.verticalAccessPlacement(),
                draft.shellMargin(),
                draft.exteriorAirMargin(),
                draft.previewMargin(),
                draft.verticalAccessSpec(),
                planner.createDefaultRoomFamilyDefinitions(draft.dimensions()),
                draft.openingProfiles(),
                planner.createDefaultLinearRunFamilyDefinitions(draft.dimensions(), draft.palette()),
                draft.insertFamilies(),
                draft.createdAt(),
                draft.updatedAt(),
                draft.pieces(),
                draft.layerStates()
        );
    }

    private static MKStructureWorkspace withMainFloorExit(MKStructureWorkspace workspace) {
        List<MKWorkspaceRoomFamilyDefinition> familyDefinitions = workspace.familyDefinitions().stream()
                .map(family -> family.topologySlotId().equals("keep.center.main_floor") ?
                        copyFamilyWithExits(family, List.of(new MKFamilyHorizontalExitDefinition(
                                Direction.SOUTH,
                                MKHorizontalExitPathKind.MAIN_EXIT,
                                "main_opening"
                        ))) : family)
                .toList();
        return withFamilyDefinitions(workspace, familyDefinitions);
    }

    private static MKWorkspaceRoomFamilyDefinition copyFamilyWithExits(
            MKWorkspaceRoomFamilyDefinition family,
            List<MKFamilyHorizontalExitDefinition> exits) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                exits,
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private static MKWorkspacePieceDefinition plannedTemplatePiece(MKPlannedPiece plannedPiece) {
        return plannedPiece(plannedPiece, plannedPiece.pieceName() + "_template", 0,
                plannedPiece.plannerId(), "template");
    }

    private static MKWorkspacePieceDefinition plannedVariantPiece(MKPlannedPiece plannedPiece) {
        return plannedPiece(plannedPiece, plannedPiece.pieceName() + "_1", 1,
                plannedPiece.plannerId().child("variant_1"), "instance");
    }

    private static MKWorkspacePieceDefinition plannedPiece(MKPlannedPiece plannedPiece, String pieceName,
                                                          int variantIndex, MKWorkspacePlannerId plannerId,
                                                          String pieceKind) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(plannedPiece.tags());
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, plannedPiece.pieceName());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put("workspace_piece_kind", pieceKind);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                plannedPiece.roleId(),
                plannerId,
                variantIndex,
                new MKWorkspaceDimensions(plannedPiece.interiorWidth(), plannedPiece.interiorLength(),
                        plannedPiece.interiorHeight(), plannedPiece.interiorHeight(), plannedPiece.interiorHeight(),
                        3, 3, 3),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, plannedPiece.interiorWidth() - 1,
                        plannedPiece.interiorHeight() - 1, plannedPiece.interiorLength() - 1),
                new BoundingBox(0, 0, 0, plannedPiece.interiorWidth() + 1,
                        plannedPiece.interiorHeight() - 1, plannedPiece.interiorLength() + 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKStructureWorkspace withTopologyProfile(MKStructureWorkspace workspace,
                                                            MKWorkspaceTopologyProfile topologyProfile) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                topologyProfile,
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private static MKStructureWorkspace withFamilyDefinitions(MKStructureWorkspace workspace,
                                                              List<MKWorkspaceRoomFamilyDefinition> familyDefinitions) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                familyDefinitions,
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private static MKStructureWorkspace withInsertFamilies(MKStructureWorkspace workspace,
                                                           List<MKWorkspaceInsertFamilyDefinition> insertFamilies) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                insertFamilies,
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private static MKStructureWorkspace withCopiedPhysicalValueObjects(MKStructureWorkspace workspace) {
        MKWorkspaceMaterialPalette palette = workspace.palette();
        MKWorkspaceStairAuthoringConfig stairConfig = workspace.stairConfig();
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = workspace.verticalAccessSpec();
        MKWorkspaceStairAuthoringConfig specStairConfig = verticalAccessSpec.stairConfig();
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(), palette.ceilingBlock(),
                        palette.stairBlock(), palette.slabBlock(), palette.ladderBlock()),
                new MKWorkspaceStairAuthoringConfig(stairConfig.mode(), stairConfig.riseType(),
                        stairConfig.stairWidth()),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                new MKWorkspaceVerticalAccessSpec(verticalAccessSpec.shaftSize(), verticalAccessSpec.placement(),
                        new MKWorkspaceStairAuthoringConfig(specStairConfig.mode(), specStairConfig.riseType(),
                                specStairConfig.stairWidth())),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.insertFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private static MKWorkspacePieceDefinition floorPiece(String pieceName, String baseName,
                                                        MKWorkspacePlannerId plannerId) {
        return floorPiece(pieceName, baseName, plannerId, 0, Map.of(
                "workspace_floor_topology_stack_id", "tower.primary",
                "workspace_floor_topology_floor_role", "main_floor",
                MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0",
                "workspace_piece_kind", "template"
        ));
    }

    private static MKWorkspacePieceDefinition variantAwarePiece(String pieceName, String baseName, int variantIndex,
                                                               MKWorkspacePlannerId plannerId) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, baseName);
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put("workspace_piece_kind", variantIndex == 0 ? "template" : "instance");
        MKWorkspaceStableSlotIdentity.apply(tags, "test_room", "stable.floor.room");
        return floorPiece(pieceName, baseName, plannerId, variantIndex, tags);
    }

    private static MKWorkspacePieceDefinition reuseAwarePiece(String pieceName, String baseName,
                                                              boolean authoringPiece) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, baseName);
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
        tags.put("workspace_piece_kind", "template");
        tags.put(MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG,
                MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT);
        tags.put(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, Boolean.toString(authoringPiece));
        tags.put(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG, "source_" + baseName);
        return floorPiece(pieceName, baseName, MKWorkspacePlannerId.of("utility." + baseName), 0, tags);
    }

    private static MKWorkspacePieceDefinition floorPiece(String pieceName, String baseName,
                                                        MKWorkspacePlannerId plannerId, int variantIndex,
                                                        Map<String, String> tags) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan.room",
                plannerId,
                variantIndex,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspaceTopologyProfile renamedBaseProfile() {
        return new MKWorkspaceTopologyProfile(RENAMED_BASE_PLANNER_ID, List.of(), TerrainAdjustment.BEARD_THIN);
    }

    private static final class RenamedBaseCatalogPlanner implements MKWorkspacePlanner {
        @Override
        public ResourceLocation plannerId() {
            return RENAMED_BASE_PLANNER_ID;
        }

        @Override
        public MKWorkspaceTopologySchema schema() {
            return new MKWorkspaceTopologySchema(RENAMED_BASE_PLANNER_ID, List.of(), List.of(), List.of(), List.of());
        }

        @Override
        public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
            return renamedBaseProfile();
        }

        @Override
        public List<MKWorkspaceRoomFamilyDefinition> createDefaultRoomFamilyDefinitions(
                MKWorkspaceDimensions dimensions) {
            return List.of();
        }

        @Override
        public List<MKWorkspaceLinearRunFamilyDefinition> createDefaultLinearRunFamilyDefinitions(
                MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
            return List.of();
        }

        @Override
        public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
            String baseName = workspace.insertFamilies().isEmpty() ? "old_floor_room" : "new_floor_room";
            LinkedHashMap<String, String> tags = new LinkedHashMap<>();
            tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, baseName);
            tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
            tags.put("workspace_piece_kind", "template");
            MKWorkspaceStableSlotIdentity.apply(tags, "test_room", "stable.floor.room");
            return List.of(new MKPlannedPiece(
                    "test.room",
                    baseName,
                    MKWorkspaceDimensions.defaultDimensions().roomWidth(),
                    MKWorkspaceDimensions.defaultDimensions().roomLength(),
                    MKWorkspaceDimensions.defaultDimensions().roomHeight(),
                    List.of(),
                    tags,
                    MKWorkspacePlannerId.of("renamed.base").child(baseName)
            ));
        }
    }
}
