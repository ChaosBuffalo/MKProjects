package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorLinkGenerationMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceLayerStateService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceGeometry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepPlannerSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVariantAddition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceTopologySchema;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final ResourceLocation INSERT_FAMILY_CATALOG_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc_test", "insert_family_catalog");
    private static final ResourceLocation CONNECTOR_POOL_CATALOG_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc_test", "connector_pool_catalog");
    private static final ResourceLocation FLAT_PLATFORM_CATALOG_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc_test", "flat_platform_catalog");
    private final MKStructureWorkspaceService service = new MKStructureWorkspaceService();

    static {
        MKWorkspacePlannerRegistry.registerShared(new RenamedBaseCatalogPlanner());
        MKWorkspacePlannerRegistry.registerShared(new InsertFamilyCatalogPlanner());
        MKWorkspacePlannerRegistry.registerShared(new ConnectorPoolCatalogPlanner());
        MKWorkspacePlannerRegistry.registerShared(new FlatPlatformCatalogPlanner());
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
    void catalogRelayoutPreservesLegacyInsertSocketPiecesWhenInsertFamiliesChange() {
        MKWorkspaceInsertFamilyDefinition platformContents =
                MKWorkspaceInsertFamilyDefinition.insertSocket("fire_shrine_platform_contents", 5, 5, 3);
        MKWorkspaceInsertFamilyDefinition pillars =
                MKWorkspaceInsertFamilyDefinition.insertSocket("fire_shrine_pillars", 5, 5, 3);
        MKStructureWorkspace existing = withTopologyProfile(MKStructureWorkspace.createDraft(BlockPos.ZERO),
                insertFamilyCatalogProfile());
        existing = withInsertFamilies(existing, List.of(platformContents));
        existing = existing.withPieces(List.of(
                legacyInsertFamilyPiece("fire_shrine_platform_contents", platformContents, 0, "template"),
                legacyInsertFamilyPiece("fire_shrine_gazebo", platformContents, 1, "instance")
        ));
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(platformContents, pillars));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation());
        assertFalse(preflight.report().relayoutImpacts().stream()
                        .filter(impact -> platformContents.familyId().equals(impact.baseName()))
                        .anyMatch(impact -> "new".equals(impact.outcome()) ||
                                "removed".equals(impact.outcome()) ||
                                "rebuild".equals(impact.outcome())),
                () -> "existing insert socket family should not churn: " +
                        preflight.report().relayoutImpacts());
        assertEquals(2, preflight.report().relayoutImpacts().stream()
                        .filter(impact -> platformContents.familyId().equals(impact.baseName()))
                        .filter(impact -> "preserved".equals(impact.outcome()) ||
                                "moved".equals(impact.outcome()) ||
                                "expanded".equals(impact.outcome()))
                        .count(),
                () -> "expected template and variant to be preserved by insert socket identity: " +
                        preflight.report().relayoutImpacts());
    }

    @Test
    void catalogRelayoutDoesNotRebuildForConnectorPoolOnlyChanges() {
        ConnectorPoolCatalogPlanner planner = new ConnectorPoolCatalogPlanner();
        MKStructureWorkspace existing = withTopologyProfile(MKStructureWorkspace.createDraft(BlockPos.ZERO),
                connectorPoolCatalogProfile());
        MKPlannedPiece existingPlan = planner.createCanonicalPieces(existing).getFirst();
        existing = existing.withPieces(List.of(plannedTemplatePieceWithTargetBounds(existing, existingPlan,
                List.of(
                        connectorPoolCatalogConnector(existing, "old_route"),
                        authoredInsertConnector(existing, "route_target")
                ))));
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.insertSocket("route_target", 5, 5, 3)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation());
        assertFalse(preflight.report().relayoutImpacts().stream()
                        .filter(impact -> existingPlan.pieceName().equals(impact.baseName()))
                        .anyMatch(impact -> "rebuild".equals(impact.outcome())),
                () -> "pool-only connector route changes should preserve authored blocks: " +
                        preflight.report().relayoutImpacts());
        assertTrue(preflight.report().relayoutImpacts().stream()
                        .filter(impact -> existingPlan.pieceName().equals(impact.baseName()))
                        .anyMatch(impact -> "preserved".equals(impact.outcome()) ||
                                "moved".equals(impact.outcome()) ||
                                "expanded".equals(impact.outcome())),
                () -> "expected pool-only connector route change to be preserved: " +
                        preflight.report().relayoutImpacts());
    }

    @Test
    void catalogRelayoutDoesNotExpandExactBoundsFlatPlatformWhenAddingFamily() {
        FlatPlatformCatalogPlanner planner = new FlatPlatformCatalogPlanner();
        MKStructureWorkspace existing = withTopologyProfile(MKStructureWorkspace.createDraft(BlockPos.ZERO),
                flatPlatformCatalogProfile());
        MKPlannedPiece flatPlatform = planner.createCanonicalPieces(existing).getFirst();
        existing = existing.withPieces(List.of(plannedPiece(flatPlatform, flatPlatform.pieceName() + "_template",
                0, flatPlatform.plannerId(), "template", flatPlatform.interiorWidth(),
                flatPlatform.interiorLength(), flatPlatform.interiorHeight())));
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.insertSocket("added_insert", 5, 5, 3)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation());
        assertFalse(preflight.report().relayoutImpacts().stream()
                        .filter(impact -> flatPlatform.pieceName().equals(impact.baseName()))
                        .anyMatch(impact -> "expanded".equals(impact.outcome()) ||
                                "rebuild".equals(impact.outcome())),
                () -> "flat platform exact bounds should not expand to shell defaults: " +
                        preflight.report().relayoutImpacts());
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
    void variantDeletionPreflightReportsStagedWorkspaceMutation() {
        MKWorkspacePieceDefinition template = variantAwarePiece("room_template", "room", 0,
                MKWorkspacePlannerId.of("utility.room"));
        MKWorkspacePieceDefinition variant = variantAwarePiece("room_1", "room", 1,
                MKWorkspacePlannerId.of("utility.room").child("variant_1"));
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(template, variant));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, existing, 123L,
                List.of(), List.of(variant.pieceId()));

        assertEquals("relayout_workspace_variants", preflight.report().recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.SAFE_RELAYOUT, preflight.report().safety());
        assertFalse(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT));
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS));
        assertEquals(List.of(), preflight.report().orphanedTemplateBindings());
        assertTrue(service.isTerminalVariantMutationReport(preflight.report()));
        assertEquals(1, preflight.report().relayoutImpacts().stream()
                .filter(impact -> "removed".equals(impact.outcome()) &&
                        variant.pieceName().equals(impact.pieceName()))
                .count());
    }

    @Test
    void variantOnlyPreflightIgnoresRequestedSettingsDrift() {
        MKWorkspacePieceDefinition template = variantAwarePiece("room_template", "room", 0,
                MKWorkspacePlannerId.of("utility.room"));
        MKWorkspacePieceDefinition variant = variantAwarePiece("room_1", "room", 1,
                MKWorkspacePlannerId.of("utility.room").child("variant_1"));
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(template, variant));
        MKStructureWorkspace requestedWithDrift = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("unexpected_insert_family", 5, 5, 3)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requestedWithDrift, 123L,
                List.of(), List.of(), List.of(variant.pieceId()), false);

        assertEquals("relayout_workspace_variants", preflight.report().recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.SAFE_RELAYOUT, preflight.report().safety());
        assertFalse(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        assertEquals(1, preflight.report().relayoutImpacts().size());
        assertEquals(variant.pieceName(), preflight.report().relayoutImpacts().getFirst().pieceName());
    }

    @Test
    void variantAdditionPreflightReportsRelayoutOnly() {
        MKWalledKeepWorkspacePlanner planner = new MKWalledKeepWorkspacePlanner();
        MKStructureWorkspace existing = walledKeepWorkspace(planner);
        MKPlannedPiece plannedPiece = planner.createCanonicalPieces(existing).stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .findFirst()
                .orElseThrow();
        String baseName = plannedPiece.pieceName();
        existing = existing.withPieces(List.of(plannedTemplatePiece(plannedPiece)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, existing, 123L,
                List.of(), List.of(new MKWorkspaceVariantAddition(baseName, null)), List.of());

        assertEquals("relayout_workspace_variants", preflight.report().recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.SAFE_RELAYOUT, preflight.report().safety());
        assertFalse(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT));
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS));
        assertTrue(service.isTerminalVariantMutationReport(preflight.report()));
        assertEquals(1, preflight.report().relayoutImpacts().stream()
                .filter(impact -> "new".equals(impact.outcome()) &&
                        (baseName + "_1").equals(impact.pieceName()))
                .count());
    }

    @Test
    void catalogAffectingSettingsChangeIgnoresDraftIdentityPiecesAndLayerStates() {
        MKWorkspacePieceDefinition template = variantAwarePiece("room_template", "room", 0,
                MKWorkspacePlannerId.of("utility.room"));
        MKWorkspacePieceDefinition variant = variantAwarePiece("room_1", "room", 1,
                MKWorkspacePlannerId.of("utility.room").child("variant_1"));
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(template, variant));
        MKStructureWorkspace requestedDraft = service.workspaceForUpdate(existing, existing, List.of(), 456L,
                List.of());

        assertFalse(service.hasCatalogAffectingSettingsChange(existing, requestedDraft));
    }

    @Test
    void catalogAffectingSettingsChangeDetectsInsertFamilies() {
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("new_insert", 5, 5, 3)));

        assertTrue(service.hasCatalogAffectingSettingsChange(existing, requested));
    }

    @Test
    void variantMutationCombinedWithCatalogChangeIsNotTerminalVariantOnly() {
        MKStructureWorkspace existing = withTopologyProfile(MKStructureWorkspace.createDraft(BlockPos.ZERO),
                renamedBaseProfile());
        MKWorkspacePieceDefinition variant = variantAwarePiece("old_floor_room_1", "old_floor_room", 1,
                MKWorkspacePlannerId.of("renamed.base.old_floor_room").child("variant_1"));
        existing = existing.withPieces(List.of(
                variantAwarePiece("old_floor_room_template", "old_floor_room", 0,
                        MKWorkspacePlannerId.of("renamed.base.old_floor_room")),
                variant
        ));
        MKStructureWorkspace requested = withInsertFamilies(existing, List.of(
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("trigger_new_base", 5, 5, 3)));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L,
                List.of(), List.of(variant.pieceId()));

        assertEquals("mixed_workspace_update", preflight.report().recommendedOperation());
        assertFalse(service.isTerminalVariantMutationReport(preflight.report()));
        assertTrue(preflight.report().relayoutImpacts().stream()
                .anyMatch(impact -> "removed".equals(impact.outcome()) &&
                        variant.pieceName().equals(impact.pieceName())));
    }

    @Test
    void walledKeepAddingFloorRoomUsesCatalogRelayoutInsteadOfFullRegenerate() {
        MKWalledKeepWorkspacePlanner planner = new MKWalledKeepWorkspacePlanner();
        MKStructureWorkspace existing = walledKeepWorkspace(planner);
        existing = withMainFloorExit(existing);
        List<MKWorkspacePieceDefinition> pieces = planner.createCanonicalPieces(existing).stream()
                .filter(piece -> !com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags
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

    @Test
    void loadedTestKeepAddingMainRoomUsesCatalogRelayoutInsteadOfFullRegenerate() throws Exception {
        MKWorkspaceExportManifest manifest = loadTestKeepManifest();
        MKStructureWorkspaceImportService importService = new MKStructureWorkspaceImportService();
        MKStructureWorkspace imported = importService.workspaceFromManifest(
                UUID.randomUUID(), BlockPos.ZERO, 123L, manifest);
        imported = imported.withPieces(importService.pieceDefinitionsFromManifest(imported, manifest));
        imported = withMainFloorExit(imported);
        MKFloorTopologySettings floorSettings = imported.topologyProfile()
                .floorTopologySettingsOrDefault("keep.center", "main_floor");
        MKFloorRoomProfile added = floorSettings.mainRoomProfiles().getFirst()
                .withIdentity("extra_main_room", "Extra Main Room");
        MKFloorTopologySettings updatedFloorSettings = floorSettings.withAddedRoomProfile(
                MKFloorRoomKind.MAIN_ROOM, added);
        MKStructureWorkspace requested = withTopologyProfile(imported,
                imported.topologyProfile().withFloorTopologySettings(updatedFloorSettings));

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(imported, requested, 123L);

        assertEquals("preserve_catalog_relayout", preflight.report().recommendedOperation(),
                () -> "warnings=" + preflight.report().warnings() +
                        " impacts=" + preflight.report().relayoutImpacts());
        assertTrue(preflight.report().relayoutImpacts().stream()
                        .anyMatch(impact -> "new".equals(impact.outcome()) &&
                                impact.stableSlotKey().contains("extra_main_room")),
                () -> "expected only the added main room to be scaffolded: " +
                        preflight.report().relayoutImpacts());
        assertFalse(preflight.report().warnings().stream()
                .anyMatch(warning -> warning.contains("full regeneration will clear")));
    }

    @Test
    void walledKeepRampartAccessOnlyPreflightPatchesCornerEntryScaffold() {
        MKWalledKeepWorkspacePlanner planner = new MKWalledKeepWorkspacePlanner();
        MKStructureWorkspace existing = walledKeepWorkspace(planner);
        MKWorkspaceTopologyProfile tallEntryProfile = existing.topologyProfile()
                .withVerticalStackSettings(existing.topologyProfile().verticalStackSettings("keep.corner.shared")
                        .orElseThrow()
                        .withEntryHeight(11));
        existing = withTopologyProfile(existing, tallEntryProfile);
        existing = existing.withPieces(planner.createCanonicalPieces(existing).stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKStructureWorkspaceServicePreflightTest::plannedTemplatePiece)
                .toList());
        MKWorkspaceTopologyProfile rampartProfile = MKWalledKeepPlannerSettings.from(existing.topologyProfile())
                .withRampartAccessEnabled(true)
                .applyTo(existing.topologyProfile());
        MKStructureWorkspace requested = withTopologyProfile(existing, rampartProfile);

        MKWorkspaceMutationPreflight preflight = service.preflightWorkspaceUpdate(existing, requested, 123L);

        assertEquals("patch_rampart_access_openings", preflight.report().recommendedOperation());
        assertEquals(MKWorkspaceMutationSafety.SAFE_BLOCK_SUBSTITUTION, preflight.report().safety());
        assertTrue(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS));
        assertFalse(preflight.report().invalidatedLayers().contains(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS));
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

    private static MKWorkspacePieceDefinition plannedTemplatePieceWithTargetBounds(MKStructureWorkspace workspace,
                                                                                   MKPlannedPiece plannedPiece) {
        return plannedTemplatePieceWithTargetBounds(workspace, plannedPiece, List.of());
    }

    private static MKWorkspacePieceDefinition plannedTemplatePieceWithTargetBounds(MKStructureWorkspace workspace,
                                                                                   MKPlannedPiece plannedPiece,
                                                                                   List<MKWorkspaceConnectorDefinition> connectors) {
        return plannedPiece(plannedPiece, plannedPiece.pieceName() + "_template", 0,
                plannedPiece.plannerId(), "template",
                plannedPiece.interiorWidth() + (2 * (workspace.shellMargin() + workspace.exteriorAirMargin())),
                plannedPiece.interiorLength() + (2 * (workspace.shellMargin() + workspace.exteriorAirMargin())),
                plannedPiece.interiorHeight() + (2 * workspace.verticalShellMargin()),
                connectors);
    }

    private static MKWorkspacePieceDefinition plannedVariantPiece(MKPlannedPiece plannedPiece) {
        return plannedPiece(plannedPiece, plannedPiece.pieceName() + "_1", 1,
                plannedPiece.plannerId().child("variant_1"), "instance");
    }

    private static MKWorkspacePieceDefinition plannedPiece(MKPlannedPiece plannedPiece, String pieceName,
                                                          int variantIndex, MKWorkspacePlannerId plannerId,
                                                          String pieceKind) {
        return plannedPiece(plannedPiece, pieceName, variantIndex, plannerId, pieceKind,
                plannedPiece.interiorWidth() + 2,
                plannedPiece.interiorLength() + 2,
                plannedPiece.interiorHeight(),
                List.of());
    }

    private static MKWorkspacePieceDefinition plannedPiece(MKPlannedPiece plannedPiece, String pieceName,
                                                          int variantIndex, MKWorkspacePlannerId plannerId,
                                                          String pieceKind, int exportWidth, int exportLength,
                                                          int exportHeight) {
        return plannedPiece(plannedPiece, pieceName, variantIndex, plannerId, pieceKind, exportWidth, exportLength,
                exportHeight, List.of());
    }

    private static MKWorkspacePieceDefinition plannedPiece(MKPlannedPiece plannedPiece, String pieceName,
                                                          int variantIndex, MKWorkspacePlannerId plannerId,
                                                          String pieceKind, int exportWidth, int exportLength,
                                                          int exportHeight,
                                                          List<MKWorkspaceConnectorDefinition> connectors) {
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
                connectors,
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, exportWidth - 1, exportHeight - 1, exportLength - 1),
                new BoundingBox(0, 0, 0, exportWidth - 1, exportHeight - 1, exportLength - 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspaceConnectorDefinition connectorPoolCatalogConnector(MKStructureWorkspace workspace,
                                                                                String targetPoolName) {
        ResourceLocation targetPool = ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                workspace.structureName() + "/" + targetPoolName);
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.MAIN_FORWARD,
                Direction.NORTH,
                BlockPos.ZERO,
                3,
                2,
                0,
                0,
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                        MKConnectorRole.MAIN_FORWARD.getSerializedName()),
                targetPool,
                targetPool,
                ResourceLocation.parse("minecraft:empty")
        );
    }

    private static MKWorkspaceConnectorDefinition authoredInsertConnector(MKStructureWorkspace workspace,
                                                                          String insertFamilyId) {
        ResourceLocation targetPool = MKWorkspaceInsertFamilyDefinition.poolId(workspace.namespace(),
                workspace.structureName(), insertFamilyId);
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.LINK_CANDIDATE,
                Direction.UP,
                BlockPos.ZERO,
                1,
                1,
                0,
                0,
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(), "base"),
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(), "attach"),
                targetPool,
                ResourceLocation.parse("minecraft:empty")
        );
    }

    private static MKWorkspacePieceDefinition legacyInsertFamilyPiece(String pieceName,
                                                                      MKWorkspaceInsertFamilyDefinition family,
                                                                      int variantIndex,
                                                                      String pieceKind) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, family.familyId());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put("workspace_piece_kind", pieceKind);
        tags.put(MKWorkspacePieceGeometry.TAG_TOWER_PIECE_KIND,
                family.kind() == MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY ?
                        MKWorkspacePieceGeometry.TOWER_PIECE_KIND_FLOOR_LINK_INSERT :
                        "insert_socket_template");
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, family.familyId());
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND, family.kind().getSerializedName());
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "workspace.insert_family." + family.kind().getSerializedName(),
                MKWorkspacePlannerId.of("legacy.insert_family." + family.familyId())
                        .child(variantIndex == 0 ? "template" : "variant_" + variantIndex),
                variantIndex,
                new MKWorkspaceDimensions(family.width(), family.depth(), family.height(), family.height(),
                        family.height(), 3, 3, 3),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, family.width() - 1, family.height() - 1, family.depth() - 1),
                new BoundingBox(0, 0, 0, family.width() - 1, family.height() - 1, family.depth() - 1),
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

    private static MKWorkspaceExportManifest loadTestKeepManifest() throws Exception {
        Path path = Path.of("../MKNpc/src/main/resources/data/mknpc/mk_workspace_exports/test_keep.json");
        if (!Files.exists(path)) {
            path = Path.of("MKNpc/src/main/resources/data/mknpc/mk_workspace_exports/test_keep.json");
        }
        JsonElement json = JsonParser.parseString(Files.readString(path));
        return MKWorkspaceExportManifest.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    private static MKWorkspaceTopologyProfile renamedBaseProfile() {
        return new MKWorkspaceTopologyProfile(RENAMED_BASE_PLANNER_ID, List.of(), TerrainAdjustment.BEARD_THIN);
    }

    private static MKWorkspaceTopologyProfile insertFamilyCatalogProfile() {
        return new MKWorkspaceTopologyProfile(INSERT_FAMILY_CATALOG_PLANNER_ID, List.of(), TerrainAdjustment.BEARD_THIN);
    }

    private static MKWorkspaceTopologyProfile connectorPoolCatalogProfile() {
        return new MKWorkspaceTopologyProfile(CONNECTOR_POOL_CATALOG_PLANNER_ID, List.of(),
                TerrainAdjustment.BEARD_THIN);
    }

    private static MKWorkspaceTopologyProfile flatPlatformCatalogProfile() {
        return new MKWorkspaceTopologyProfile(FLAT_PLATFORM_CATALOG_PLANNER_ID, List.of(),
                TerrainAdjustment.BEARD_THIN);
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

    private static final class InsertFamilyCatalogPlanner implements MKWorkspacePlanner {
        @Override
        public ResourceLocation plannerId() {
            return INSERT_FAMILY_CATALOG_PLANNER_ID;
        }

        @Override
        public MKWorkspaceTopologySchema schema() {
            return new MKWorkspaceTopologySchema(INSERT_FAMILY_CATALOG_PLANNER_ID, List.of(), List.of(), List.of(),
                    List.of());
        }

        @Override
        public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
            return insertFamilyCatalogProfile();
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
            return MKWorkspacePlanner.createCommonInsertFamilyTemplatePieces(workspace);
        }
    }

    private static final class ConnectorPoolCatalogPlanner implements MKWorkspacePlanner {
        @Override
        public ResourceLocation plannerId() {
            return CONNECTOR_POOL_CATALOG_PLANNER_ID;
        }

        @Override
        public MKWorkspaceTopologySchema schema() {
            return new MKWorkspaceTopologySchema(CONNECTOR_POOL_CATALOG_PLANNER_ID, List.of(), List.of(), List.of(),
                    List.of());
        }

        @Override
        public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
            return connectorPoolCatalogProfile();
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
            String targetPool = workspace.insertFamilies().isEmpty() ? "old_route" : "new_route";
            LinkedHashMap<String, String> tags = new LinkedHashMap<>();
            tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, "pool_sensitive_room");
            tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
            tags.put("workspace_piece_kind", "template");
            MKWorkspaceStableSlotIdentity.apply(tags, "test_room", "stable.pool_sensitive_room");
            return List.of(new MKPlannedPiece(
                    "test.pool_sensitive_room",
                    "pool_sensitive_room",
                    MKWorkspaceDimensions.defaultDimensions().roomWidth(),
                    MKWorkspaceDimensions.defaultDimensions().roomLength(),
                    MKWorkspaceDimensions.defaultDimensions().roomHeight(),
                    List.of(new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH, 3, 2,
                            targetPool, null)),
                    tags,
                    MKWorkspacePlannerId.of("connector.pool_sensitive_room")
            ));
        }
    }

    private static final class FlatPlatformCatalogPlanner implements MKWorkspacePlanner {
        @Override
        public ResourceLocation plannerId() {
            return FLAT_PLATFORM_CATALOG_PLANNER_ID;
        }

        @Override
        public MKWorkspaceTopologySchema schema() {
            return new MKWorkspaceTopologySchema(FLAT_PLATFORM_CATALOG_PLANNER_ID, List.of(), List.of(), List.of(),
                    List.of());
        }

        @Override
        public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
            return flatPlatformCatalogProfile();
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
            LinkedHashMap<String, String> tags = new LinkedHashMap<>();
            tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, "flat_platform_room");
            tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
            tags.put("workspace_piece_kind", "template");
            tags.put("workspace_flat_platform_kind", "spoke");
            MKWorkspaceStableSlotIdentity.apply(tags, "test_room", "stable.flat_platform_room");
            java.util.ArrayList<MKPlannedPiece> pieces = new java.util.ArrayList<>();
            pieces.add(new MKPlannedPiece(
                    "test.flat_platform_room",
                    "flat_platform_room",
                    15,
                    11,
                    30,
                    List.of(),
                    tags,
                    MKWorkspacePlannerId.of("flat.platform_room")
            ));
            pieces.addAll(MKWorkspacePlanner.createCommonInsertFamilyTemplatePieces(workspace));
            return List.copyOf(pieces);
        }
    }
}
