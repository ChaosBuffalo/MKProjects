package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.block_entities.MKWorkspaceDevBlockEntity;
import com.chaosbuffalo.mknpc.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteSwapSafety;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPaletteMerge;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologyMutationPreflightService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLayerStateService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportArchiveWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportResult;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestDiscovery;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspaceIdentityRenameService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspaceHallwayRegenerationPlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspaceMarginExpansionService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspacePieceRelayoutService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKStructureWorkspaceMutationService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspaceTemplateBindingDiffService;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import com.chaosbuffalo.mknpc.world.gen.workspace.stairs.MKWorkspaceStairBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MKStructureWorkspaceService {
    public record MKWorkspaceImportResponse(@Nullable MKStructureWorkspace workspace, List<String> validationErrors) {
        public static MKWorkspaceImportResponse success(MKStructureWorkspace workspace) {
            return new MKWorkspaceImportResponse(workspace, List.of());
        }

        public static MKWorkspaceImportResponse failed() {
            return new MKWorkspaceImportResponse(null, List.of());
        }

        public static MKWorkspaceImportResponse validationFailed(List<String> validationErrors) {
            return new MKWorkspaceImportResponse(null, List.copyOf(validationErrors));
        }

        public Optional<MKStructureWorkspace> workspaceOpt() {
            return Optional.ofNullable(workspace);
        }
    }

    private final MKWorkspacePlannerRegistry plannerRegistry = MKWorkspacePlannerRegistry.shared();
    private final MKWorkspaceScaffoldBuilder scaffoldBuilder = new MKWorkspaceScaffoldBuilder();
    private final MKWorkspaceStairBuilder stairBuilder = new MKWorkspaceStairBuilder();
    private final MKStructureWorkspaceImportService importService = new MKStructureWorkspaceImportService();
    private final MKWorkspaceBackupManifestDiscovery backupDiscovery = new MKWorkspaceBackupManifestDiscovery();
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspacePieceRelayoutService relayoutService = new MKWorkspacePieceRelayoutService();
    private final MKWorkspaceMarginExpansionService marginExpansionService = new MKWorkspaceMarginExpansionService();
    private final MKStructureWorkspaceMutationService mutationService = new MKStructureWorkspaceMutationService();
    private final MKWorkspaceIdentityRenameService identityRenameService = new MKWorkspaceIdentityRenameService();
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();
    private final MKWorkspaceFloorTopologyMutationPreflightService floorTopologyPreflightService =
            new MKWorkspaceFloorTopologyMutationPreflightService();
    private final MKWorkspaceLayerStateService layerStateService = new MKWorkspaceLayerStateService();
    private final MKWorkspaceHallwayRegenerationPlanner hallwayRegenerationPlanner =
            new MKWorkspaceHallwayRegenerationPlanner();
    private final MKWorkspaceTemplateBindingDiffService templateBindingDiffService =
            new MKWorkspaceTemplateBindingDiffService();

    public List<String> validateWorkspace(MKStructureWorkspace workspace) {
        return plannerRegistry.validate(workspace);
    }

    public Optional<MKStructureWorkspace> createOrUpdateWorkspace(ServerLevel level, MKStructureWorkspace workspace) {
        List<String> errors = validateWorkspace(workspace);
        if (!errors.isEmpty()) {
            return Optional.empty();
        }
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> existingOpt = data.getWorkspaceByAnchor(workspace.anchor());
        if (existingOpt.isPresent()) {
            MKStructureWorkspace existing = existingOpt.get();
            if (canRelayoutPreviewMarginOnly(existing, workspace)) {
                try {
                    return relayoutService.relayoutPreviewMargin(level, existing, workspace.previewMargin())
                            .map(MKWorkspacePieceRelayoutService.RelayoutResult::workspace);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before preview margin relayout", e);
                }
            }
            if (canSwapPaletteOnly(existing, workspace)) {
                try {
                    mutationService.swapMaterialPalettes(level, existing, workspace);
                    return data.getWorkspace(existing.id());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before palette swap", e);
                }
            }
            if (canRenameIdentityOnly(existing, workspace)) {
                try {
                    return Optional.of(identityRenameService.rename(level, existing,
                            workspace.namespace(), workspace.structureName()).workspace());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before identity rename", e);
                }
            }
            if (canExpandMarginsOnly(existing, workspace)) {
                try {
                    return marginExpansionService.expandMargins(level, existing,
                                    workspace.shellMargin(), workspace.exteriorAirMargin())
                            .map(MKWorkspaceMarginExpansionService.ExpansionResult::workspace);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before margin expansion", e);
                }
            }
            if (canRegenerateHallwayRoutingOnly(existing, workspace)) {
                return regenerateHallwayRouting(level, existing, workspace);
            }
            if (canRefreshLinkRenderingOnly(existing, workspace)) {
                return refreshLinkRenderingMetadata(level, existing, workspace);
            }
            MKStructureWorkspace updated = new MKStructureWorkspace(
                    existing.id(),
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
                    workspace.insertFamilies(),
                    existing.createdAt(),
                    System.currentTimeMillis(),
                    existing.pieces(),
                    existing.layerStates()
            );
            data.updateWorkspace(updated);
            syncBlockEntity(level, updated.anchor(), updated.id());
            return Optional.of(updated);
        }
        data.createWorkspace(workspace);
        syncBlockEntity(level, workspace.anchor(), workspace.id());
        return Optional.of(workspace);
    }

    public boolean canApplyPreviewMarginRelayout(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canRelayoutPreviewMarginOnly(existing, requested))
                .isPresent();
    }

    public boolean canApplyPaletteSwap(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canSwapPaletteOnly(existing, requested))
                .isPresent();
    }

    public boolean canApplyIdentityRename(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canRenameIdentityOnly(existing, requested))
                .isPresent();
    }

    public boolean canApplyMarginExpansion(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canExpandMarginsOnly(existing, requested))
                .isPresent();
    }

    public boolean canApplyHallwayRoutingRegeneration(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canRegenerateHallwayRoutingOnly(existing, requested))
                .isPresent();
    }

    public boolean canApplyLinkRenderingRefresh(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canRefreshLinkRenderingOnly(existing, requested))
                .isPresent();
    }

    public Optional<MKWorkspaceMutationPreflight> preflightWorkspaceUpdate(ServerLevel level,
                                                                           MKStructureWorkspace requested) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        return data.getWorkspaceByAnchor(requested.anchor())
                .map(existing -> preflightWorkspaceUpdate(existing, requested, System.currentTimeMillis()));
    }

    public MKWorkspaceMutationPreflight preflightWorkspaceUpdate(MKStructureWorkspace existing,
                                                                 MKStructureWorkspace requested,
                                                                 long nowEpochMillis) {
        List<MKWorkspaceInvalidationReport> floorReports = floorTopologyReports(existing, requested, nowEpochMillis);
        MKWorkspaceInvalidationReport report = mergeReports(floorReports);
        MKStructureWorkspace workspaceWithLayerStates = layerStateService.ensureLayerStates(existing, nowEpochMillis);
        MKStructureWorkspace workspaceWithDirtyLayers = layerStateService.applyInvalidation(
                workspaceWithLayerStates, report, nowEpochMillis);
        return new MKWorkspaceMutationPreflight(report, workspaceWithDirtyLayers);
    }

    public Optional<MKStructureWorkspace> setLayerLocked(ServerLevel level, BlockPos anchor,
                                                         MKWorkspaceGeneratedLayer layer, boolean locked) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace workspace = layerStateService.ensureLayerStates(workspaceOpt.get(),
                System.currentTimeMillis());
        MKStructureWorkspace updated = locked ?
                layerStateService.lockLayers(workspace, List.of(layer)) :
                layerStateService.unlockLayers(workspace, List.of(layer));
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public List<MKWorkspaceGeneratedLayer> lockedInvalidatedLayers(MKStructureWorkspace existing,
                                                                   MKWorkspaceInvalidationReport report) {
        return report.invalidatedLayers().stream()
                .filter(existing::layerLocked)
                .toList();
    }

    private List<MKWorkspaceInvalidationReport> floorTopologyReports(MKStructureWorkspace existing,
                                                                     MKStructureWorkspace requested,
                                                                     long nowEpochMillis) {
        List<MKWorkspaceInvalidationReport> reports = new ArrayList<>();
        List<MKPlannedPiece> requestedCanonicalPieces = plannerRegistry.plannerFor(requested)
                .createCanonicalPieces(requested);
        for (MKWorkspaceFloorTopologySettings requestedSettings :
                requested.topologyProfile().floorTopologySettings()) {
            MKWorkspaceFloorTopologySettings previousSettings = existing.topologyProfile()
                    .floorTopologySettings(requestedSettings.stackId(), requestedSettings.floorRole())
                    .orElseGet(() -> existing.topologyProfile().floorTopologySettingsOrDefault(
                            requestedSettings.stackId(), requestedSettings.floorRole()));
            MKWorkspaceMutationPreflight preflight = floorTopologyPreflightService.preflight(
                    existing,
                    floorPlannerId(requestedSettings),
                    previousSettings,
                    requestedSettings,
                    nowEpochMillis);
            MKWorkspaceInvalidationReport report = withConcreteTemplateBindings(existing, requestedCanonicalPieces,
                    previousSettings, requestedSettings, preflight.report());
            if (!report.invalidatedLayers().isEmpty()) {
                reports.add(report);
            }
        }
        return List.copyOf(reports);
    }

    private MKWorkspaceInvalidationReport withConcreteTemplateBindings(
            MKStructureWorkspace existing,
            List<MKPlannedPiece> requestedCanonicalPieces,
            MKWorkspaceFloorTopologySettings previousSettings,
            MKWorkspaceFloorTopologySettings requestedSettings,
            MKWorkspaceInvalidationReport report) {
        if (!report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS)) {
            return report;
        }
        MKWorkspaceTemplateBindingDiffService.TemplateBindingDiff bindingDiff =
                templateBindingDiffService.floorTopologyBindings(
                        existing,
                        requestedCanonicalPieces,
                        previousSettings.stackId(),
                        previousSettings.floorRole(),
                        requestedSettings.stackId(),
                        requestedSettings.floorRole());
        return report.withTemplateBindings(bindingDiff.preserved(), bindingDiff.orphaned())
                .withRemapSuggestions(templateBindingDiffService.suggestFloorTopologyRemaps(
                        existing,
                        requestedCanonicalPieces,
                        bindingDiff.orphaned(),
                        previousSettings.stackId(),
                        previousSettings.floorRole(),
                        requestedSettings.stackId(),
                        requestedSettings.floorRole()));
    }

    private MKWorkspacePlannerId floorPlannerId(MKWorkspaceFloorTopologySettings settings) {
        return MKWorkspacePlannerId.of(settings.stackId())
                .child("floor")
                .child(settings.floorRole())
                .child("floor_plan");
    }

    private MKWorkspaceInvalidationReport mergeReports(List<MKWorkspaceInvalidationReport> reports) {
        if (reports.isEmpty()) {
            return MKWorkspaceInvalidationReport.noChanges("Requested workspace settings do not invalidate generated layers.");
        }
        LinkedHashSet<MKWorkspaceGeneratedLayer> layers = new LinkedHashSet<>();
        LinkedHashSet<MKWorkspacePlannerId> affected = new LinkedHashSet<>();
        LinkedHashSet<MKWorkspacePlannerId> preserved = new LinkedHashSet<>();
        LinkedHashSet<MKWorkspacePlannerId> orphaned = new LinkedHashSet<>();
        ArrayList<String> warnings = new ArrayList<>();
        MKWorkspaceMutationSafety safety = MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE;
        LinkedHashSet<String> operations = new LinkedHashSet<>();
        for (MKWorkspaceInvalidationReport report : reports) {
            layers.addAll(report.invalidatedLayers());
            affected.addAll(report.affectedPlannerIds());
            preserved.addAll(report.preservedTemplateBindings());
            orphaned.addAll(report.orphanedTemplateBindings());
            warnings.addAll(report.warnings());
            safety = maxSafety(safety, report.safety());
            operations.add(report.recommendedOperation());
        }
        return new MKWorkspaceInvalidationReport(
                List.copyOf(layers),
                List.copyOf(affected),
                List.copyOf(preserved),
                List.copyOf(orphaned),
                safety,
                summaryForReports(reports, safety),
                operations.size() == 1 ? operations.getFirst() : "mixed_workspace_update",
                List.copyOf(warnings)
        );
    }

    private String summaryForReports(List<MKWorkspaceInvalidationReport> reports, MKWorkspaceMutationSafety safety) {
        if (reports.size() == 1) {
            return reports.getFirst().summary();
        }
        return "Workspace update affects " + reports.size() + " floor topology sections; highest safety is " +
                safety.getSerializedName() + ".";
    }

    private MKWorkspaceMutationSafety maxSafety(MKWorkspaceMutationSafety current,
                                                MKWorkspaceMutationSafety candidate) {
        return safetyRank(candidate) > safetyRank(current) ? candidate : current;
    }

    private int safetyRank(MKWorkspaceMutationSafety safety) {
        return switch (safety) {
            case SAFE_METADATA_UPDATE -> 0;
            case SAFE_BLOCK_SUBSTITUTION -> 1;
            case SAFE_EXPANSION -> 2;
            case SAFE_RELAYOUT -> 3;
            case CONDITIONALLY_SAFE_TOPOLOGY_PATCH -> 4;
            case DESTRUCTIVE_REGENERATE -> 5;
        };
    }

    public Optional<MKStructureWorkspace> generateWorkspace(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        if (!validateWorkspace(workspace).isEmpty()) {
            return Optional.empty();
        }
        List<MKPlannedPiece> templates = plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace).stream()
                .map(this::toTemplatePiece)
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(scaffoldBuilder.build(level, workspace, templates));
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> regenerateHallwayRouting(ServerLevel level, MKStructureWorkspace requested) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> existingOpt = data.getWorkspaceByAnchor(requested.anchor());
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace existing = existingOpt.get();
        if (!canRegenerateHallwayRoutingOnly(existing, requested)) {
            return Optional.empty();
        }
        return regenerateHallwayRouting(level, existing, requested);
    }

    private Optional<MKStructureWorkspace> regenerateHallwayRouting(ServerLevel level, MKStructureWorkspace existing,
                                                                   MKStructureWorkspace requested) {
        long now = System.currentTimeMillis();
        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(requested).createCanonicalPieces(requested);
        MKWorkspaceHallwayRegenerationPlanner.RegenerationPlan plan =
                hallwayRegenerationPlanner.plan(existing, canonicalPieces);
        if (!plan.hasWork()) {
            return Optional.empty();
        }

        writeBackupBeforeMutation(level, existing, "regenerate-hallway-routing", "hallway routing regeneration");
        MKStructureWorkspace workspaceForBuild = workspaceForUpdate(existing, requested, existing.pieces(), now);
        scaffoldBuilder.clearExistingPieces(level, plan.existingHallwayPieces(), existing.anchor());

        ArrayList<MKWorkspacePieceDefinition> generatedHallways = new ArrayList<>();
        for (MKPlannedPiece hallwayPiece : plan.hallwayPieces()) {
            generatedHallways.add(scaffoldBuilder.buildSingle(level, workspaceForBuild, hallwayPiece,
                    plan.layoutPieces()));
        }

        List<MKWorkspacePieceDefinition> mergedPieces =
                hallwayRegenerationPlanner.mergeGeneratedHallways(existing, generatedHallways);
        MKStructureWorkspace updated = workspaceForUpdate(existing, requested, mergedPieces, now);
        updated = layerStateService.refreshLayers(layerStateService.ensureLayerStates(updated, now),
                List.of(
                        MKWorkspaceGeneratedLayer.HALLWAY_ROUTING,
                        MKWorkspaceGeneratedLayer.HALLWAY_PIECES,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                settingsComparisonTag(requested, existing.id(), requested.previewMargin()).hashCode(),
                now);

        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, existing.anchor(), updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> addWorkspaceVariant(ServerLevel level, BlockPos anchor, String basePieceName) {
        return addWorkspaceVariant(level, anchor, basePieceName, null);
    }

    public Optional<MKStructureWorkspace> addWorkspaceVariant(ServerLevel level, BlockPos anchor,
                                                                  String basePieceName, String sourcePieceName) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().isEmpty()) {
            return Optional.empty();
        }

        String resolvedBasePieceName = basePieceName;
        MKWorkspacePieceDefinition sourcePiece = null;
        if (sourcePieceName != null && !sourcePieceName.isBlank()) {
            sourcePiece = workspace.pieces().stream()
                    .filter(piece -> sourcePieceName.equals(piece.pieceName()))
                    .findFirst()
                    .orElse(null);
            if (sourcePiece == null) {
                return Optional.empty();
            }
            resolvedBasePieceName = getBaseName(sourcePiece);
        }
        final String targetBasePieceName = resolvedBasePieceName;

        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace);
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPieces.stream()
                .collect(Collectors.toMap(MKPlannedPiece::pieceName, piece -> piece));
        MKPlannedPiece basePiece = canonicalByBaseName.get(targetBasePieceName);
        if (basePiece == null) {
            return Optional.empty();
        }

        int nextVariantIndex = workspace.pieces().stream()
                .filter(piece -> targetBasePieceName.equals(getBaseName(piece)))
                .mapToInt(MKWorkspacePieceDefinition::variantIndex)
                .max()
                .orElse(0) + 1;

        MKPlannedPiece variantPiece = toVariantPiece(basePiece, nextVariantIndex);
        List<MKPlannedPiece> layoutPieces = physicalVariantLayoutPieces(workspace, canonicalPieces, canonicalByBaseName,
                List.of(variantPiece));

        MKWorkspacePieceDefinition templatePiece = resolveVariantSourcePiece(workspace, targetBasePieceName,
                nextVariantIndex, sourcePiece);
        if (templatePiece == null) {
            return Optional.empty();
        }

        MKWorkspacePieceDefinition generatedPiece = scaffoldBuilder.cloneFromTemplate(level, workspace, templatePiece,
                variantPiece, layoutPieces);
        List<MKWorkspacePieceDefinition> updatedPieces = new java.util.ArrayList<>(workspace.pieces());
        updatedPieces.add(generatedPiece);
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> addWorkspaceVariantsForAll(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().isEmpty()) {
            return Optional.empty();
        }

        List<String> basePieceNames = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .map(this::getBaseName)
                .distinct()
                .toList();

        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace);
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPieces.stream()
                .collect(Collectors.toMap(MKPlannedPiece::pieceName, piece -> piece));

        List<MKWorkspacePieceDefinition> templatePieces = new ArrayList<>();
        List<MKPlannedPiece> variantPieces = new ArrayList<>();
        Integer rowVariantIndex = null;
        boolean sameVariantRow = true;
        for (String basePieceName : basePieceNames) {
            MKPlannedPiece basePiece = canonicalByBaseName.get(basePieceName);
            if (basePiece == null) {
                return Optional.empty();
            }
            int nextVariantIndex = workspace.pieces().stream()
                    .filter(piece -> basePieceName.equals(getBaseName(piece)))
                    .mapToInt(MKWorkspacePieceDefinition::variantIndex)
                    .max()
                    .orElse(0) + 1;
            if (rowVariantIndex == null) {
                rowVariantIndex = nextVariantIndex;
            } else if (rowVariantIndex != nextVariantIndex) {
                sameVariantRow = false;
            }
            MKPlannedPiece variantPiece = toVariantPiece(basePiece, nextVariantIndex);
            MKWorkspacePieceDefinition templatePiece = resolveVariantSourcePiece(workspace, basePieceName,
                    nextVariantIndex, null);
            if (templatePiece == null) {
                return Optional.empty();
            }
            templatePieces.add(templatePiece);
            variantPieces.add(variantPiece);
        }

        List<MKPlannedPiece> physicalVariantPieces = variantPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .toList();
        List<MKPlannedPiece> layoutPieces = physicalVariantLayoutPieces(workspace, canonicalPieces, canonicalByBaseName,
                physicalVariantPieces);
        if (sameVariantRow) {
            scaffoldBuilder.clearLayoutAreaForPieces(level, workspace, layoutPieces, physicalVariantPieces);
        }

        List<MKWorkspacePieceDefinition> generatedPieces = new ArrayList<>();
        for (int i = 0; i < variantPieces.size(); i++) {
            generatedPieces.add(scaffoldBuilder.cloneFromTemplate(level, workspace, templatePieces.get(i),
                    variantPieces.get(i), layoutPieces));
        }

        List<MKWorkspacePieceDefinition> updatedPieces = new ArrayList<>(workspace.pieces());
        updatedPieces.addAll(generatedPieces);
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    List<MKPlannedPiece> physicalVariantLayoutPieces(MKStructureWorkspace workspace,
                                                     List<MKPlannedPiece> canonicalPieces,
                                                     Map<String, MKPlannedPiece> canonicalByBaseName,
                                                     List<MKPlannedPiece> newVariantPieces) {
        ArrayList<MKPlannedPiece> layoutPieces = canonicalPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .map(this::toTemplatePiece)
                .collect(Collectors.toCollection(ArrayList::new));
        layoutPieces.addAll(workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() > 0)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(piece -> toExistingVariantPiece(piece, canonicalByBaseName))
                .toList());
        layoutPieces.addAll(newVariantPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .toList());
        return List.copyOf(layoutPieces);
    }

    public Optional<MKWorkspaceExportResult> exportWorkspacePieces(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        try {
            MKWorkspaceExportArchiveWriter writer = new MKWorkspaceExportArchiveWriter();
            var archive = writer.write(level, workspace);
            return Optional.of(new MKWorkspaceExportResult(archive.structurePieceCount(), archive.metadataCount(),
                    archive.path()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export workspace archive for " + workspace.namespace() + ":" +
                    workspace.structureName(), e);
        }
    }

    public boolean deleteWorkspace(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return false;
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        scaffoldBuilder.clearExistingWorkspaceArea(level, workspace, anchor);
        data.deleteWorkspace(workspace.id());
        level.setBlock(anchor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    public Optional<MKStructureWorkspace> generateWorkspaceStairs(ServerLevel level, BlockPos anchor, String pieceName,
                                                                       MKWorkspaceStairAuthoringConfig stairConfigOverride) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().stream().anyMatch(piece -> pieceName.equals(piece.pieceName()))) {
            writeBackupBeforeMutation(level, workspace, "generate-stairs", "stair generation");
        }
        List<MKWorkspacePieceDefinition> updatedPieces = workspace.pieces().stream()
                .map(piece -> pieceName.equals(piece.pieceName()) ?
                        stairBuilder.generateForPiece(level, workspace, piece, stairConfigOverride) : piece)
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> generateAllWorkspaceStairs(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().stream().anyMatch(piece -> MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()))) {
            writeBackupBeforeMutation(level, workspace, "generate-all-stairs", "stair generation");
        }
        List<MKWorkspacePieceDefinition> updatedPieces = workspace.pieces().stream()
                .map(piece -> MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) ?
                        stairBuilder.generateForPiece(level, workspace, piece) : piece)
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> clearWorkspaceStairs(ServerLevel level, BlockPos anchor, String pieceName) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().stream().anyMatch(piece -> pieceName.equals(piece.pieceName()))) {
            writeBackupBeforeMutation(level, workspace, "clear-stairs", "clearing stairs");
        }
        List<MKWorkspacePieceDefinition> updatedPieces = workspace.pieces().stream()
                .map(piece -> pieceName.equals(piece.pieceName()) ? stairBuilder.clearForPiece(level, piece) : piece)
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public boolean teleportToWorkspacePiece(ServerPlayer player, BlockPos anchor, UUID pieceId) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(player.serverLevel());
        Optional<MKWorkspacePieceDefinition> pieceOpt = data.getWorkspaceByAnchor(anchor)
                .flatMap(workspace -> workspace.pieces().stream()
                        .filter(piece -> piece.pieceId().equals(pieceId))
                        .findFirst());
        if (pieceOpt.isEmpty()) {
            return false;
        }

        BlockPos target = findSafeTeleportTarget(player.serverLevel(), pieceOpt.get().signPos().west());
        player.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        return true;
    }

    public boolean openWorkspaceScreenAtPlayer(ServerPlayer player) {
        Optional<MKStructureWorkspace> workspace = findWorkspaceContaining(player.serverLevel(), player.blockPosition());
        workspace.ifPresent(value -> openWorkspaceScreen(player, value.anchor()));
        return workspace.isPresent();
    }

    public Optional<MKStructureWorkspace> findWorkspaceContaining(ServerLevel level, BlockPos pos) {
        return IMKStructureWorkspaceData.get(level).getAllWorkspaces().stream()
                .filter(workspace -> containsWorkspacePosition(workspace, pos))
                .min((left, right) -> Integer.compare(
                        left.anchor().distManhattan(pos),
                        right.anchor().distManhattan(pos)));
    }

    private boolean containsWorkspacePosition(MKStructureWorkspace workspace, BlockPos pos) {
        if (workspace.anchor().distManhattan(pos) <= MKWorkspaceScaffoldBuilder.CLEAR_MARGIN) {
            return true;
        }
        BoundingBox workspaceBounds = null;
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            if (piece.previewBounds().isInside(pos) || piece.exportBounds().isInside(pos)) {
                return true;
            }
            workspaceBounds = mergeBounds(workspaceBounds, piece.previewBounds());
            workspaceBounds = mergeBounds(workspaceBounds, piece.exportBounds());
            workspaceBounds = mergeBounds(workspaceBounds, singleBlockBounds(piece.structureBlockPos()));
            workspaceBounds = mergeBounds(workspaceBounds, singleBlockBounds(piece.signPos()));
        }
        return workspaceBounds != null &&
                expandBounds(workspaceBounds, MKWorkspaceScaffoldBuilder.CLEAR_MARGIN).isInside(pos);
    }

    private BlockPos findSafeTeleportTarget(ServerLevel level, BlockPos preferred) {
        List<BlockPos> candidates = List.of(
                preferred,
                preferred.above(),
                preferred.west(),
                preferred.east(),
                preferred.north(),
                preferred.south(),
                preferred.west().above(),
                preferred.east().above(),
                preferred.north().above(),
                preferred.south().above()
        );
        for (BlockPos candidate : candidates) {
            if (canStandAt(level, candidate)) {
                return candidate;
            }
        }
        return preferred;
    }

    private boolean canStandAt(ServerLevel level, BlockPos pos) {
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        return feet.getCollisionShape(level, pos).isEmpty() &&
                head.getCollisionShape(level, pos.above()).isEmpty();
    }

    private BoundingBox singleBlockBounds(BlockPos pos) {
        return new BoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    private BoundingBox expandBounds(BoundingBox bounds, int margin) {
        return new BoundingBox(
                bounds.minX() - margin,
                bounds.minY() - margin,
                bounds.minZ() - margin,
                bounds.maxX() + margin,
                bounds.maxY() + margin,
                bounds.maxZ() + margin
        );
    }

    private BoundingBox mergeBounds(@Nullable BoundingBox left, BoundingBox right) {
        if (left == null) {
            return right;
        }
        return new BoundingBox(
                Math.min(left.minX(), right.minX()),
                Math.min(left.minY(), right.minY()),
                Math.min(left.minZ(), right.minZ()),
                Math.max(left.maxX(), right.maxX()),
                Math.max(left.maxY(), right.maxY()),
                Math.max(left.maxZ(), right.maxZ())
        );
    }

    public void openWorkspaceScreen(ServerPlayer player, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(player.serverLevel());
        MKStructureWorkspace workspace = data.getWorkspaceByAnchor(anchor).orElse(null);
        player.connection.send(new OpenWorkspaceScreenPacket(anchor, workspace,
                importService.discoverManifestIds(), discoverBackupFileNames(player, workspace)));
    }

    private List<String> discoverBackupFileNames(ServerPlayer player, MKStructureWorkspace workspace) {
        if (workspace == null) {
            return List.of();
        }
        return backupDiscovery.discoverBackups(player.server, workspace).stream()
                .map(MKWorkspaceBackupManifestDiscovery.BackupCandidate::fileName)
                .toList();
    }

    private void writeBackupBeforeMutation(ServerLevel level, MKStructureWorkspace workspace, String operation,
                                           String description) {
        try {
            backupWriter.writeBeforeMutation(level, workspace, operation);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write workspace backup before " + description, e);
        }
    }

    public Optional<MKStructureWorkspace> importWorkspaceFromManifest(ServerLevel level, BlockPos anchor,
                                                                     ResourceLocation manifestId) {
        return importWorkspaceFromManifestWithValidation(level, anchor, manifestId).workspaceOpt();
    }

    public MKWorkspaceImportResponse importWorkspaceFromManifestWithValidation(ServerLevel level, BlockPos anchor,
                                                                              ResourceLocation manifestId) {
        MKStructureWorkspaceImportService.MKWorkspaceImportOutcome outcome =
                importService.importWorkspaceAtAnchorDetailed(level, anchor, manifestId);
        if (!outcome.validationErrors().isEmpty()) {
            return MKWorkspaceImportResponse.validationFailed(outcome.validationErrors());
        }
        if (outcome.resultOpt().isEmpty()) {
            return MKWorkspaceImportResponse.failed();
        }
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> imported = data.getWorkspace(outcome.resultOpt().get().workspaceId());
        if (imported.isEmpty()) {
            return MKWorkspaceImportResponse.failed();
        }
        imported.ifPresent(workspace -> syncBlockEntity(level, anchor, workspace.id()));
        return MKWorkspaceImportResponse.success(imported.get());
    }

    private void syncBlockEntity(ServerLevel level, BlockPos anchor, java.util.UUID workspaceId) {
        BlockEntity blockEntity = level.getBlockEntity(anchor);
        if (blockEntity instanceof MKWorkspaceDevBlockEntity workspaceDevBlockEntity) {
            workspaceDevBlockEntity.setWorkspaceId(workspaceId);
        }
    }

    private MKPlannedPiece toTemplatePiece(MKPlannedPiece basePiece) {
        return new MKPlannedPiece(
                basePiece.roleId(),
                basePiece.pieceName() + "_template",
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, "template", 0),
                basePiece.plannerId()
        );
    }

    private MKPlannedPiece toVariantPiece(MKPlannedPiece basePiece, int variantIndex) {
        return new MKPlannedPiece(
                basePiece.roleId(),
                basePiece.pieceName() + "_" + variantIndex,
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, "instance", variantIndex),
                basePiece.plannerId().child("variant_" + variantIndex)
        );
    }

    private MKPlannedPiece toExistingVariantPiece(MKWorkspacePieceDefinition piece, Map<String, MKPlannedPiece> canonicalByBaseName) {
        String baseName = getBaseName(piece);
        MKPlannedPiece basePiece = canonicalByBaseName.get(baseName);
        if (basePiece == null) {
            throw new IllegalStateException("missing canonical piece for base name " + baseName);
        }
        return new MKPlannedPiece(
                basePiece.roleId(),
                piece.pieceName(),
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, "instance", piece.variantIndex()),
                piece.plannerId()
        );
    }

    private boolean usesPhysicalWorkspaceCell(MKPlannedPiece piece) {
        return usesPhysicalWorkspaceCell(piece.tags());
    }

    private boolean usesPhysicalWorkspaceCell(Map<String, String> tags) {
        return !MKWorkspaceTemplateReuseTags.isDerived(tags);
    }

    private Map<String, String> withWorkspaceTags(MKPlannedPiece basePiece, String pieceKind, int variantIndex) {
        java.util.LinkedHashMap<String, String> tags = new java.util.LinkedHashMap<>(basePiece.tags());
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, basePiece.pieceName());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put("workspace_piece_kind", pieceKind);
        return tags;
    }

    private MKWorkspacePieceDefinition resolveVariantSourcePiece(MKStructureWorkspace workspace, String targetBaseName,
                                                                 int targetVariantIndex,
                                                                 MKWorkspacePieceDefinition explicitSourcePiece) {
        if (explicitSourcePiece != null) {
            return explicitSourcePiece;
        }
        MKWorkspacePieceDefinition baseTemplate = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0 && targetBaseName.equals(getBaseName(piece)))
                .findFirst()
                .orElse(null);
        if (baseTemplate == null) {
            return null;
        }
        if (!MKWorkspaceTemplateReuseTags.isDerived(baseTemplate.tags())) {
            return baseTemplate;
        }
        String sourceId = MKWorkspaceTemplateReuseTags.sourceId(baseTemplate.tags());
        MKWorkspacePieceDefinition sameVariantSource = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == targetVariantIndex)
                .filter(piece -> sourceId.equals(getBaseName(piece)))
                .findFirst()
                .orElse(null);
        if (sameVariantSource != null) {
            return sameVariantSource;
        }
        return workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(piece -> sourceId.equals(getBaseName(piece)))
                .findFirst()
                .orElse(null);
    }

    private String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    private boolean canRelayoutPreviewMarginOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (existing.pieces().isEmpty() || existing.previewMargin() == requested.previewMargin()) {
            return false;
        }
        return settingsComparisonTag(existing, existing.id(), requested.previewMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin()));
    }

    private boolean canSwapPaletteOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (existing.pieces().isEmpty()) {
            return false;
        }
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
        for (MKWorkspacePieceDefinition piece : existing.pieces()) {
            var sourcePalette = paletteResolver.resolvePiece(existing, piece).orElse(existing.palette());
            var targetPalette = paletteResolver.resolvePiece(requested, piece).orElse(requested.palette());
            if (!MKWorkspacePaletteSwapSafety.canRepresentAsBlockReplacement(sourcePalette, targetPalette)) {
                return false;
            }
        }
        return true;
    }

    private boolean canRenameIdentityOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (existing.pieces().isEmpty()) {
            return false;
        }
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
        if (existing.pieces().isEmpty()) {
            return false;
        }
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

    private boolean canRegenerateHallwayRoutingOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (existing.pieces().isEmpty()) {
            return false;
        }
        MKWorkspaceMutationPreflight preflight = preflightWorkspaceUpdate(existing, requested,
                System.currentTimeMillis());
        return "regenerate_hallway_routing".equals(preflight.report().recommendedOperation()) &&
                lockedInvalidatedLayers(existing, preflight.report()).isEmpty();
    }

    boolean canRefreshLinkRenderingOnly(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (existing.pieces().isEmpty()) {
            return false;
        }
        MKWorkspaceMutationPreflight preflight = preflightWorkspaceUpdate(existing, requested,
                System.currentTimeMillis());
        return "refresh_link_rendering".equals(preflight.report().recommendedOperation()) &&
                preflight.report().safety() == MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE &&
                lockedInvalidatedLayers(existing, preflight.report()).isEmpty();
    }

    private Optional<MKStructureWorkspace> refreshLinkRenderingMetadata(ServerLevel level,
                                                                        MKStructureWorkspace existing,
                                                                        MKStructureWorkspace requested) {
        long nowEpochMillis = System.currentTimeMillis();
        MKWorkspaceMutationPreflight preflight = preflightWorkspaceUpdate(existing, requested, nowEpochMillis);
        if (!"refresh_link_rendering".equals(preflight.report().recommendedOperation()) ||
                preflight.report().safety() != MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE ||
                !lockedInvalidatedLayers(existing, preflight.report()).isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace updated = workspaceForUpdate(existing, requested, existing.pieces(), nowEpochMillis,
                preflight.workspaceWithDirtyLayers().layerStates());
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        data.updateWorkspace(updated);
        syncBlockEntity(level, updated.anchor(), updated.id());
        return Optional.of(updated);
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin) {
        return settingsComparisonTag(workspace, id, previewMargin, workspace.palette());
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin,
                                                                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return settingsComparisonTag(workspace, id, previewMargin, palette, workspace.namespace(), workspace.structureName());
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin,
                                                                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette palette,
                                                                String namespace, String structureName) {
        return settingsComparisonTag(workspace, id, previewMargin, palette, namespace, structureName,
                workspace.shellMargin(), workspace.exteriorAirMargin());
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin,
                                                                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette palette,
                                                                String namespace, String structureName,
                                                                int shellMargin, int exteriorAirMargin) {
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
                workspace.insertFamilies(),
                0,
                0,
                List.of(),
                List.of()
        ).toTag();
    }

    MKStructureWorkspace workspaceForUpdate(MKStructureWorkspace existing, MKStructureWorkspace requested,
                                            List<MKWorkspacePieceDefinition> pieces,
                                            long nowEpochMillis) {
        return workspaceForUpdate(existing, requested, pieces, nowEpochMillis, existing.layerStates());
    }

    MKStructureWorkspace workspaceForUpdate(MKStructureWorkspace existing, MKStructureWorkspace requested,
                                            List<MKWorkspacePieceDefinition> pieces,
                                            long nowEpochMillis,
                                            List<MKWorkspaceGeneratedLayerState> layerStates) {
        return new MKStructureWorkspace(
                existing.id(),
                requested.anchor(),
                requested.namespace(),
                requested.structureName(),
                requested.topologyProfile(),
                requested.dimensions(),
                requested.palette(),
                requested.stairConfig(),
                requested.verticalAccessPlacement(),
                requested.shellMargin(),
                requested.exteriorAirMargin(),
                requested.previewMargin(),
                requested.verticalAccessSpec(),
                requested.familyDefinitions(),
                requested.openingProfiles(),
                requested.linearRunFamilies(),
                requested.insertFamilies(),
                existing.createdAt(),
                nowEpochMillis,
                pieces,
                layerStates
        );
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
                                .map(requested -> {
                                    MKWorkspaceTopologySlotMetadata metadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
                                    return com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                                            family.baseName(),
                                            metadata,
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
                                            requested.paletteOverride());
                                })
                                .orElse(family))
                        .toList(),
                source.openingProfiles(),
                source.linearRunFamilies().stream()
                        .map(linearRun -> materialSource.linearRunFamilies().stream()
                                .filter(requested -> requested.linearRunId().equals(linearRun.linearRunId()))
                                .findFirst()
                                .map(requested -> new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition(
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
                                        linearRun.foundationPolicy(),
                                        requested.paletteOverride()))
                                .orElse(linearRun))
                        .toList(),
                source.insertFamilies(),
                source.createdAt(),
                source.updatedAt(),
                source.pieces(),
                source.layerStates()
        );
    }

    private com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig alignStairMaterials(
            com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
            com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.stairWidth()
        );
    }

    private com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec alignVerticalAccessMaterials(
            com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec spec,
            com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec(
                spec.shaftSize(),
                spec.placement(),
                alignStairMaterials(spec.stairConfig(), palette)
        );
    }
}
