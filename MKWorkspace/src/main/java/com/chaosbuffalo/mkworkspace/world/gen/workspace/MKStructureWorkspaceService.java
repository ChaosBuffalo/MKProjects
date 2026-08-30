package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceImportService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKWorkspaceAnchor;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspacePaletteSwapSafety;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepPlannerSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTopologyPaletteMerge;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVariantAddition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceFloorTopologyMutationPreflightService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceLayerStateService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceExportArchiveWriter;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceExportResult;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestDiscovery;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspaceIdentityRenameService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspaceHallwayRegenerationPlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspaceMarginExpansionService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspacePieceRelayoutService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKStructureWorkspaceMutationService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspaceTemplateBindingDiffService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.stairs.MKWorkspaceStairBuilder;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MKStructureWorkspaceService {
    private static final int WORKSPACE_OPEN_VERTICAL_MARGIN = 8;

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

    private record CatalogRelayoutTargets(List<MKPlannedPiece> targetPieces, List<MKPlannedPiece> layoutPieces) {
    }

    public enum PreparedUpdateStrategy {
        CREATE,
        VARIANT_MUTATION,
        PREVIEW_MARGIN_RELAYOUT,
        PALETTE_SWAP,
        IDENTITY_RENAME,
        MARGIN_EXPANSION,
        HALLWAY_ROUTING_REGENERATION,
        LINK_RENDERING_REFRESH,
        RAMPART_ACCESS_PATCH,
        CATALOG_PRESERVING_RELAYOUT,
        METADATA_UPDATE,
        FULL_REGENERATE
    }

    public List<String> validateWorkspace(MKStructureWorkspace workspace) {
        return plannerRegistry.validate(workspace);
    }

    public PreparedUpdateStrategy classifyPreparedUpdate(MKStructureWorkspace existing,
                                                          MKStructureWorkspace requested,
                                                          List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                          List<MKWorkspaceVariantAddition> addedVariants,
                                                          List<UUID> deletedVariantPieceIds,
                                                          MKWorkspaceMutationPreflight preflight) {
        if (!addedVariants.isEmpty() || !deletedVariantPieceIds.isEmpty()) {
            return PreparedUpdateStrategy.VARIANT_MUTATION;
        }
        if (canRelayoutPreviewMarginOnly(existing, requested)) {
            return PreparedUpdateStrategy.PREVIEW_MARGIN_RELAYOUT;
        }
        if (canSwapPaletteOnly(existing, requested)) {
            return PreparedUpdateStrategy.PALETTE_SWAP;
        }
        if (canRenameIdentityOnly(existing, requested)) {
            return PreparedUpdateStrategy.IDENTITY_RENAME;
        }
        if (canExpandMarginsOnly(existing, requested)) {
            return PreparedUpdateStrategy.MARGIN_EXPANSION;
        }
        if (canRegenerateHallwayRoutingOnly(existing, requested)) {
            return PreparedUpdateStrategy.HALLWAY_ROUTING_REGENERATION;
        }
        if (canRefreshLinkRenderingOnly(existing, requested)) {
            return PreparedUpdateStrategy.LINK_RENDERING_REFRESH;
        }
        if (canApplyRampartAccessPatch(existing, requested)) {
            return PreparedUpdateStrategy.RAMPART_ACCESS_PATCH;
        }
        if (hasCatalogAffectingSettingsChange(existing, requested) &&
                canApplyCatalogRelayout(existing, requested, acceptedRemaps)) {
            return PreparedUpdateStrategy.CATALOG_PRESERVING_RELAYOUT;
        }
        boolean settingsEqual = settingsComparisonTag(existing, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin()));
        if (settingsEqual && preflight.report().recommendedOperation().equals("none")) {
            return PreparedUpdateStrategy.METADATA_UPDATE;
        }
        // A new planner setting that has not explicitly claimed its mutation is destructive by default.
        return switch (preflight.report().recommendedOperation()) {
            case "preserve_catalog_relayout" -> PreparedUpdateStrategy.CATALOG_PRESERVING_RELAYOUT;
            case "regenerate_hallway_routing" -> PreparedUpdateStrategy.HALLWAY_ROUTING_REGENERATION;
            case "refresh_link_rendering" -> PreparedUpdateStrategy.LINK_RENDERING_REFRESH;
            case "patch_rampart_access_openings" -> PreparedUpdateStrategy.RAMPART_ACCESS_PATCH;
            default -> PreparedUpdateStrategy.FULL_REGENERATE;
        };
    }

    public Optional<MKStructureWorkspace> applyPreparedUpdate(ServerLevel level,
                                                               MKStructureWorkspace requested,
                                                               List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                               List<MKWorkspaceVariantAddition> addedVariants,
                                                               List<UUID> deletedVariantPieceIds,
                                                               boolean includeRequestedSettingsChanges,
                                                               MKWorkspaceMutationPreflight preflight,
                                                               PreparedUpdateStrategy strategy) {
        MKWorkspaceBackupManifestWriter.requireTransaction("apply-prepared-update");
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        if (strategy == PreparedUpdateStrategy.CREATE) {
            data.createWorkspace(requested);
            syncBlockEntity(level, requested.anchor(), requested.id());
            return Optional.of(requested);
        }
        MKStructureWorkspace existing = data.getWorkspaceByAnchor(requested.anchor()).orElse(null);
        if (existing == null) {
            return Optional.empty();
        }
        return switch (strategy) {
            case CREATE -> throw new IllegalStateException("CREATE strategy cannot replace an existing workspace");
            case VARIANT_MUTATION -> {
                Optional<MKStructureWorkspace> mutated = applyWorkspaceVariantMutations(level, existing,
                        addedVariants, deletedVariantPieceIds);
                if (mutated.isEmpty() || isTerminalVariantMutationReport(preflight.report())) {
                    yield mutated;
                }
                MKStructureWorkspace continuation = workspaceForUpdate(mutated.get(), requested,
                        mutated.get().pieces(), System.currentTimeMillis(), mutated.get().layerStates());
                yield applyWorkspaceUpdateByPreflightOperation(level, mutated.get(), continuation, acceptedRemaps,
                        preflight.report());
            }
            case PREVIEW_MARGIN_RELAYOUT -> {
                try {
                    yield relayoutService.relayoutPreviewMargin(level, existing, requested.previewMargin())
                            .map(MKWorkspacePieceRelayoutService.RelayoutResult::workspace);
                } catch (IOException exception) {
                    throw new IllegalStateException("Preview margin relayout failed", exception);
                }
            }
            case PALETTE_SWAP -> {
                try {
                    mutationService.swapMaterialPalettes(level, existing, requested);
                    yield data.getWorkspace(existing.id());
                } catch (IOException exception) {
                    throw new IllegalStateException("Palette swap failed", exception);
                }
            }
            case IDENTITY_RENAME -> {
                try {
                    yield Optional.of(identityRenameService.rename(level, existing,
                            requested.namespace(), requested.structureName()).workspace());
                } catch (IOException exception) {
                    throw new IllegalStateException("Workspace rename failed", exception);
                }
            }
            case MARGIN_EXPANSION -> {
                try {
                    yield marginExpansionService.expandMargins(level, existing,
                                    requested.shellMargin(), requested.exteriorAirMargin())
                            .map(MKWorkspaceMarginExpansionService.ExpansionResult::workspace);
                } catch (IOException exception) {
                    throw new IllegalStateException("Workspace margin expansion failed", exception);
                }
            }
            case HALLWAY_ROUTING_REGENERATION -> regenerateHallwayRouting(level, existing, requested);
            case LINK_RENDERING_REFRESH -> refreshLinkRenderingMetadata(level, existing, requested);
            case RAMPART_ACCESS_PATCH -> patchRampartAccessOpenings(level, existing, requested);
            case CATALOG_PRESERVING_RELAYOUT ->
                    relayoutCatalogPreservingPieces(level, existing, requested, acceptedRemaps);
            case METADATA_UPDATE -> applyMetadataUpdate(level, existing, requested);
            case FULL_REGENERATE -> fullRegenerateWorkspace(level, requested);
        };
    }

    public Optional<MKStructureWorkspace> createOrUpdateWorkspace(ServerLevel level, MKStructureWorkspace workspace) {
        return createOrUpdateWorkspace(level, workspace, List.of());
    }

    public Optional<MKStructureWorkspace> createOrUpdateWorkspace(ServerLevel level, MKStructureWorkspace workspace,
                                                                  List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return createOrUpdateWorkspace(level, workspace, acceptedRemaps, List.of(), List.of());
    }

    public Optional<MKStructureWorkspace> createOrUpdateWorkspace(ServerLevel level, MKStructureWorkspace workspace,
                                                                  List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                  List<UUID> deletedVariantPieceIds) {
        return createOrUpdateWorkspace(level, workspace, acceptedRemaps, List.of(), deletedVariantPieceIds);
    }

    public Optional<MKStructureWorkspace> createOrUpdateWorkspace(ServerLevel level, MKStructureWorkspace workspace,
                                                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                 List<MKWorkspaceVariantAddition> addedVariants,
                                                                 List<UUID> deletedVariantPieceIds) {
        return createOrUpdateWorkspace(level, workspace, acceptedRemaps, addedVariants, deletedVariantPieceIds,
                true);
    }

    public Optional<MKStructureWorkspace> createOrUpdateWorkspace(ServerLevel level, MKStructureWorkspace workspace,
                                                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                 List<MKWorkspaceVariantAddition> addedVariants,
                                                                 List<UUID> deletedVariantPieceIds,
                                                                 boolean includeRequestedSettingsChanges) {
        MKWorkspaceBackupManifestWriter.requireTransaction("create-or-update-workspace");
        List<String> errors = validateWorkspace(workspace);
        if (!errors.isEmpty()) {
            return Optional.empty();
        }
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> existingOpt = data.getWorkspaceByAnchor(workspace.anchor());
        if (existingOpt.isPresent()) {
            MKStructureWorkspace existing = existingOpt.get();
            if (!addedVariants.isEmpty() || !deletedVariantPieceIds.isEmpty()) {
            long nowEpochMillis = System.currentTimeMillis();
            MKWorkspaceMutationPreflight mutationPreflight = preflightWorkspaceUpdate(existing, workspace,
                    nowEpochMillis, acceptedRemaps, addedVariants, deletedVariantPieceIds,
                    includeRequestedSettingsChanges);
            boolean terminalVariantMutation = isTerminalVariantMutationReport(mutationPreflight.report());
            logUpdateBranch("variant_mutations", existing, workspace,
                    "addedVariants=" + addedVariants.size() +
                            " deletedVariants=" + deletedVariantPieceIds.size() +
                            " settingsDirty=" + includeRequestedSettingsChanges +
                            " followupRelayout=" + !terminalVariantMutation +
                            " operation=" + mutationPreflight.report().recommendedOperation() +
                            " safety=" + mutationPreflight.report().safety().getSerializedName());
                Optional<MKStructureWorkspace> mutated = applyWorkspaceVariantMutations(level, existing,
                        addedVariants, deletedVariantPieceIds);
                if (mutated.isEmpty()) {
                    return Optional.empty();
                }
                if (terminalVariantMutation) {
                    return mutated;
                }
                MKStructureWorkspace continuation = workspaceForUpdate(mutated.get(), workspace,
                        mutated.get().pieces(), System.currentTimeMillis(), mutated.get().layerStates());
                return applyWorkspaceUpdateByPreflightOperation(level, mutated.get(), continuation, acceptedRemaps,
                        mutationPreflight.report());
            }
            if (canRelayoutPreviewMarginOnly(existing, workspace)) {
                logUpdateBranch("preview_margin_relayout", existing, workspace,
                        "existingPreviewMargin=" + existing.previewMargin() +
                                " requestedPreviewMargin=" + workspace.previewMargin());
                try {
                    return relayoutService.relayoutPreviewMargin(level, existing, workspace.previewMargin())
                            .map(MKWorkspacePieceRelayoutService.RelayoutResult::workspace);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before preview margin relayout", e);
                }
            }
            if (canSwapPaletteOnly(existing, workspace)) {
                logUpdateBranch("palette_swap", existing, workspace, "");
                try {
                    mutationService.swapMaterialPalettes(level, existing, workspace);
                    return data.getWorkspace(existing.id());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before palette swap", e);
                }
            }
            if (canRenameIdentityOnly(existing, workspace)) {
                logUpdateBranch("identity_rename", existing, workspace,
                        "requestedWorkspace=" + workspace.namespace() + ":" + workspace.structureName());
                try {
                    return Optional.of(identityRenameService.rename(level, existing,
                            workspace.namespace(), workspace.structureName()).workspace());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before identity rename", e);
                }
            }
            if (canExpandMarginsOnly(existing, workspace)) {
                logUpdateBranch("margin_expansion", existing, workspace,
                        "requestedShellMargin=" + workspace.shellMargin() +
                                " requestedExteriorAirMargin=" + workspace.exteriorAirMargin());
                try {
                    return marginExpansionService.expandMargins(level, existing,
                                    workspace.shellMargin(), workspace.exteriorAirMargin())
                            .map(MKWorkspaceMarginExpansionService.ExpansionResult::workspace);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to write workspace backup before margin expansion", e);
                }
            }
            if (canRegenerateHallwayRoutingOnly(existing, workspace)) {
                logUpdateBranch("hallway_routing_regeneration", existing, workspace, "");
                return regenerateHallwayRouting(level, existing, workspace);
            }
            if (canRefreshLinkRenderingOnly(existing, workspace)) {
                logUpdateBranch("link_rendering_refresh", existing, workspace, "");
                return refreshLinkRenderingMetadata(level, existing, workspace);
            }
            if (canApplyRampartAccessPatch(existing, workspace)) {
                logUpdateBranch("rampart_access_patch", existing, workspace, "");
                return patchRampartAccessOpenings(level, existing, workspace);
            }
            if (hasCatalogAffectingSettingsChange(existing, workspace) &&
                    canApplyCatalogRelayout(existing, workspace, acceptedRemaps)) {
                logUpdateBranch("catalog_preserving_relayout", existing, workspace,
                        "acceptedRemaps=" + acceptedRemaps.size());
                return relayoutCatalogPreservingPieces(level, existing, workspace, acceptedRemaps);
            }
            logUpdateBranch("metadata_update_only", existing, workspace, "");
            return applyMetadataUpdate(level, existing, workspace);
        }
        data.createWorkspace(workspace);
        MKWorkspace.LOGGER.info("Workspace update branch=create_new anchor={} workspace={}:{} pieces={}",
                workspace.anchor().toShortString(), workspace.namespace(), workspace.structureName(),
                workspace.pieces().size());
        syncBlockEntity(level, workspace.anchor(), workspace.id());
        return Optional.of(workspace);
    }

    private void logUpdateBranch(String branch, MKStructureWorkspace existing, MKStructureWorkspace requested,
                                 String detail) {
        MKWorkspace.LOGGER.info("Workspace update branch={} anchor={} workspace={}:{} existingPieces={} requestedPieces={} {}",
                branch,
                requested.anchor().toShortString(),
                requested.namespace(),
                requested.structureName(),
                existing.pieces().size(),
                requested.pieces().size(),
                detail);
    }

    private Optional<MKStructureWorkspace> applyWorkspaceUpdateByPreflightOperation(
            ServerLevel level,
            MKStructureWorkspace existing,
            MKStructureWorkspace requested,
            List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
            MKWorkspaceInvalidationReport report) {
        return switch (report.recommendedOperation()) {
            case "none" -> {
                logUpdateBranch("metadata_update_only", existing, requested, "sourcePreflight=true");
                yield applyMetadataUpdate(level, existing, requested);
            }
            case "preserve_catalog_relayout" -> {
                logUpdateBranch("catalog_preserving_relayout", existing, requested,
                        "acceptedRemaps=" + acceptedRemaps.size() + " sourcePreflight=true");
                yield relayoutCatalogPreservingPieces(level, existing, requested, acceptedRemaps);
            }
            case "regenerate_hallway_routing" -> {
                logUpdateBranch("hallway_routing_regeneration", existing, requested, "sourcePreflight=true");
                yield regenerateHallwayRouting(level, existing, requested);
            }
            case "refresh_link_rendering" -> {
                logUpdateBranch("link_rendering_refresh", existing, requested, "sourcePreflight=true");
                yield refreshLinkRenderingMetadata(level, existing, requested);
            }
            case "patch_rampart_access_openings" -> {
                logUpdateBranch("rampart_access_patch", existing, requested, "sourcePreflight=true");
                yield patchRampartAccessOpenings(level, existing, requested);
            }
            default -> {
                throw new IllegalStateException("Unsupported prepared workspace operation: " +
                        report.recommendedOperation());
            }
        };
    }

    private Optional<MKStructureWorkspace> applyMetadataUpdate(ServerLevel level, MKStructureWorkspace existing,
                                                               MKStructureWorkspace requested) {
        MKStructureWorkspace updated = new MKStructureWorkspace(
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
                requested.verticalShellMargin(),
                requested.exteriorAirMargin(),
                requested.previewMargin(),
                requested.verticalAccessSpec(),
                requested.familyDefinitions(),
                requested.openingProfiles(),
                requested.linearRunFamilies(),
                requested.insertFamilies(),
                existing.createdAt(),
                System.currentTimeMillis(),
                existing.pieces(),
                existing.layerStates()
        );
        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, updated.anchor(), updated.id());
        return Optional.of(updated);
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

    public boolean canApplyRampartAccessPatch(ServerLevel level, MKStructureWorkspace requested) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canApplyRampartAccessPatch(existing, requested))
                .isPresent();
    }

    public boolean canApplyCatalogRelayout(ServerLevel level, MKStructureWorkspace requested) {
        return canApplyCatalogRelayout(level, requested, List.of());
    }

    public boolean canApplyCatalogRelayout(ServerLevel level, MKStructureWorkspace requested,
                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return IMKStructureWorkspaceData.get(level)
                .getWorkspaceByAnchor(requested.anchor())
                .filter(existing -> canApplyCatalogRelayout(existing, requested, acceptedRemaps))
                .isPresent();
    }

    public Optional<MKWorkspaceMutationPreflight> preflightWorkspaceUpdate(ServerLevel level,
                                                                           MKStructureWorkspace requested) {
        return preflightWorkspaceUpdate(level, requested, List.of());
    }

    public Optional<MKWorkspaceMutationPreflight> preflightWorkspaceUpdate(ServerLevel level,
                                                                           MKStructureWorkspace requested,
                                                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return preflightWorkspaceUpdate(level, requested, acceptedRemaps, List.of(), List.of());
    }

    public Optional<MKWorkspaceMutationPreflight> preflightWorkspaceUpdate(ServerLevel level,
                                                                           MKStructureWorkspace requested,
                                                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                           List<UUID> deletedVariantPieceIds) {
        return preflightWorkspaceUpdate(level, requested, acceptedRemaps, List.of(), deletedVariantPieceIds);
    }

    public Optional<MKWorkspaceMutationPreflight> preflightWorkspaceUpdate(ServerLevel level,
                                                                           MKStructureWorkspace requested,
                                                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                           List<MKWorkspaceVariantAddition> addedVariants,
                                                                           List<UUID> deletedVariantPieceIds) {
        return preflightWorkspaceUpdate(level, requested, acceptedRemaps, addedVariants, deletedVariantPieceIds,
                true);
    }

    public Optional<MKWorkspaceMutationPreflight> preflightWorkspaceUpdate(ServerLevel level,
                                                                           MKStructureWorkspace requested,
                                                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                           List<MKWorkspaceVariantAddition> addedVariants,
                                                                           List<UUID> deletedVariantPieceIds,
                                                                           boolean includeRequestedSettingsChanges) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        return data.getWorkspaceByAnchor(requested.anchor())
                .map(existing -> preflightWorkspaceUpdate(existing, requested, System.currentTimeMillis(),
                        acceptedRemaps, addedVariants, deletedVariantPieceIds, includeRequestedSettingsChanges));
    }

    public MKWorkspaceMutationPreflight preflightWorkspaceUpdate(MKStructureWorkspace existing,
                                                                 MKStructureWorkspace requested,
                                                                 long nowEpochMillis) {
        return preflightWorkspaceUpdate(existing, requested, nowEpochMillis, List.of());
    }

    public MKWorkspaceMutationPreflight preflightWorkspaceUpdate(MKStructureWorkspace existing,
                                                                 MKStructureWorkspace requested,
                                                                 long nowEpochMillis,
                                                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return preflightWorkspaceUpdate(existing, requested, nowEpochMillis, acceptedRemaps, List.of(), List.of());
    }

    public MKWorkspaceMutationPreflight preflightWorkspaceUpdate(MKStructureWorkspace existing,
                                                                 MKStructureWorkspace requested,
                                                                 long nowEpochMillis,
                                                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                 List<UUID> deletedVariantPieceIds) {
        return preflightWorkspaceUpdate(existing, requested, nowEpochMillis, acceptedRemaps, List.of(),
                deletedVariantPieceIds);
    }

    public MKWorkspaceMutationPreflight preflightWorkspaceUpdate(MKStructureWorkspace existing,
                                                                 MKStructureWorkspace requested,
                                                                 long nowEpochMillis,
                                                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                 List<MKWorkspaceVariantAddition> addedVariants,
                                                                 List<UUID> deletedVariantPieceIds) {
        return preflightWorkspaceUpdate(existing, requested, nowEpochMillis, acceptedRemaps, addedVariants,
                deletedVariantPieceIds, true);
    }

    public MKWorkspaceMutationPreflight preflightWorkspaceUpdate(MKStructureWorkspace existing,
                                                                 MKStructureWorkspace requested,
                                                                 long nowEpochMillis,
                                                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                                 List<MKWorkspaceVariantAddition> addedVariants,
                                                                 List<UUID> deletedVariantPieceIds,
                                                                 boolean includeRequestedSettingsChanges) {
        MKStructureWorkspace effectiveRequested = includeRequestedSettingsChanges ? requested : existing;
        List<MKWorkspaceInvalidationReport> floorReports = floorTopologyReports(existing, effectiveRequested,
                nowEpochMillis);
        MKWorkspaceInvalidationReport floorReport = mergeReports(floorReports);
        Optional<MKWorkspaceInvalidationReport> rampartAccessReport = rampartAccessPatchReport(existing,
                effectiveRequested);
        if (rampartAccessReport.isPresent()) {
            return preflightForReport(existing, withVariantMutationReport(existing, rampartAccessReport.get(),
                    addedVariants, deletedVariantPieceIds), nowEpochMillis);
        }
        boolean hasVariantMutation = !addedVariants.isEmpty() || !deletedVariantPieceIds.isEmpty();
        if (canConsiderCatalogRelayout(floorReport) &&
                (floorReport.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS) ||
                        hasCatalogAffectingSettingsChange(existing, effectiveRequested))) {
            Optional<MKWorkspacePieceRelayoutService.CatalogRelayoutSummary> catalogSummary =
                    catalogRelayoutSummary(existing, effectiveRequested, nowEpochMillis, acceptedRemaps);
            if (catalogSummary.isPresent() && catalogSummary.get().hasWork()) {
                return preflightForReport(existing, withVariantMutationReport(existing,
                                catalogRelayoutReport(catalogSummary.get(), floorReport), addedVariants,
                                deletedVariantPieceIds),
                        nowEpochMillis);
            }
        }
        if (hasVariantMutation && isNoChangeReport(floorReport)) {
            return preflightForReport(existing, withVariantMutationReport(existing, floorReport,
                    addedVariants, deletedVariantPieceIds), nowEpochMillis);
        }
        MKWorkspaceInvalidationReport report = withDestructiveRegenerationImpacts(existing, effectiveRequested,
                floorReport, nowEpochMillis);
        return preflightForReport(existing, withVariantMutationReport(existing, report, addedVariants,
                deletedVariantPieceIds), nowEpochMillis);
    }

    private MKWorkspaceMutationPreflight preflightForReport(MKStructureWorkspace existing,
                                                            MKWorkspaceInvalidationReport report,
                                                            long nowEpochMillis) {
        MKStructureWorkspace workspaceWithLayerStates = layerStateService.ensureLayerStates(existing, nowEpochMillis);
        MKStructureWorkspace workspaceWithDirtyLayers = layerStateService.applyInvalidation(
                workspaceWithLayerStates, report, nowEpochMillis);
        return new MKWorkspaceMutationPreflight(report, workspaceWithDirtyLayers);
    }

    private boolean canConsiderCatalogRelayout(MKWorkspaceInvalidationReport floorReport) {
        return "none".equals(floorReport.recommendedOperation()) ||
                floorReport.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS);
    }

    boolean hasCatalogAffectingSettingsChange(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        return !Objects.equals(existing.topologyProfile(), requested.topologyProfile()) ||
                !Objects.equals(existing.dimensions(), requested.dimensions()) ||
                !Objects.equals(existing.verticalAccessPlacement(), requested.verticalAccessPlacement()) ||
                !Objects.equals(existing.verticalAccessSpec(), requested.verticalAccessSpec()) ||
                !Objects.equals(existing.familyDefinitions(), requested.familyDefinitions()) ||
                !Objects.equals(existing.openingProfiles(), requested.openingProfiles()) ||
                !Objects.equals(existing.linearRunFamilies(), requested.linearRunFamilies()) ||
                !Objects.equals(existing.insertFamilies(), requested.insertFamilies());
    }

    private MKWorkspaceInvalidationReport withDestructiveRegenerationImpacts(
            MKStructureWorkspace existing,
            MKStructureWorkspace requested,
            MKWorkspaceInvalidationReport report,
            long nowEpochMillis) {
        if (report.safety() != MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE ||
                !report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS)) {
            return report;
        }
        List<MKWorkspacePieceDefinition> physicalPieces = existing.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
        if (physicalPieces.isEmpty()) {
            return report;
        }
        long variantCount = physicalPieces.stream()
                .filter(piece -> piece.variantIndex() > 0)
                .count();
        ArrayList<String> warnings = new ArrayList<>(report.warnings());
        warnings.add("Catalog-preserving relayout is unavailable; full regeneration will clear " +
                physicalPieces.size() + " physical authored template slots" +
                (variantCount > 0 ? ", including " + variantCount + " variants." : "."));
        catalogRelayoutUnavailableReasons(existing, requested, nowEpochMillis).forEach(reason ->
                warnings.add("Catalog-preserving relayout unavailable reason: " + reason));
        ArrayList<MKWorkspaceRelayoutImpact> impacts = new ArrayList<>(report.relayoutImpacts());
        impacts.addAll(physicalPieces.stream()
                .map(this::destructiveRegenerationImpact)
                .toList());
        return new MKWorkspaceInvalidationReport(
                report.invalidatedLayers(),
                report.affectedPlannerIds(),
                report.preservedTemplateBindings(),
                report.orphanedTemplateBindings(),
                report.safety(),
                report.summary(),
                report.recommendedOperation(),
                List.copyOf(warnings),
                report.remapSuggestions(),
                List.copyOf(impacts)
        );
    }

    private MKWorkspaceRelayoutImpact destructiveRegenerationImpact(MKWorkspacePieceDefinition piece) {
        String stableKey = stableSlotKey(piece.tags());
        String catalogKey = stableKey.isBlank() ?
                piece.plannerId() + ":" + getBaseName(piece) + ":" + piece.variantIndex() :
                "stable:" + stableKey + ":" + piece.variantIndex();
        boolean variant = piece.variantIndex() > 0;
        return new MKWorkspaceRelayoutImpact(
                variant ? "removed" : "rebuild",
                piece.pieceName(),
                getBaseName(piece),
                piece.variantIndex(),
                piece.plannerId().toString(),
                catalogKey,
                variant ? "full workspace regeneration will remove this authored variant" :
                        "full workspace regeneration will clear and rebuild this authored template"
        );
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
        for (MKFloorTopologySettings requestedSettings :
                requested.topologyProfile().floorTopologySettings()) {
            MKFloorTopologySettings previousSettings = existing.topologyProfile()
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
            MKFloorTopologySettings previousSettings,
            MKFloorTopologySettings requestedSettings,
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

    private MKWorkspacePlannerId floorPlannerId(MKFloorTopologySettings settings) {
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
        ArrayList<MKWorkspaceRelayoutImpact> impacts = new ArrayList<>();
        MKWorkspaceMutationSafety safety = MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE;
        LinkedHashSet<String> operations = new LinkedHashSet<>();
        for (MKWorkspaceInvalidationReport report : reports) {
            layers.addAll(report.invalidatedLayers());
            affected.addAll(report.affectedPlannerIds());
            preserved.addAll(report.preservedTemplateBindings());
            orphaned.addAll(report.orphanedTemplateBindings());
            warnings.addAll(report.warnings());
            impacts.addAll(report.relayoutImpacts());
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
                List.copyOf(warnings),
                List.of(),
                List.copyOf(impacts)
        );
    }

    private String summaryForReports(List<MKWorkspaceInvalidationReport> reports, MKWorkspaceMutationSafety safety) {
        if (reports.size() == 1) {
            return reports.getFirst().summary();
        }
        return "Workspace update affects " + reports.size() + " mutation groups; highest safety is " +
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
        MKWorkspaceBackupManifestWriter.requireTransaction("generate-workspace");
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

    public Optional<MKStructureWorkspace> fullRegenerateWorkspace(ServerLevel level, MKStructureWorkspace requested) {
        MKWorkspaceBackupManifestWriter.requireTransaction("full-regenerate-workspace");
        if (!validateWorkspace(requested).isEmpty()) {
            return Optional.empty();
        }
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> existingOpt = data.getWorkspaceByAnchor(requested.anchor());
        long now = System.currentTimeMillis();

        MKStructureWorkspace workspaceForBuild;
        if (existingOpt.isPresent()) {
            MKStructureWorkspace existing = existingOpt.get();
            if (!existing.pieces().isEmpty()) {
                writeBackupBeforeMutation(level, existing, "full-regenerate", "full workspace regeneration");
            }
            workspaceForBuild = workspaceForUpdate(existing, requested, existing.pieces(), now, List.of());
        } else {
            workspaceForBuild = requested;
        }

        List<MKPlannedPiece> templates = plannerRegistry.plannerFor(workspaceForBuild).createCanonicalPieces(workspaceForBuild).stream()
                .map(this::toTemplatePiece)
                .toList();
        List<MKWorkspacePieceDefinition> generatedPieces = scaffoldBuilder.build(level, workspaceForBuild, templates);
        MKStructureWorkspace updated = workspaceForBuild.withPieces(generatedPieces).withLayerStates(List.of());
        updated = layerStateService.refreshLayers(
                layerStateService.ensureLayerStates(updated, now),
                java.util.Arrays.asList(MKWorkspaceGeneratedLayer.values()),
                settingsComparisonTag(updated, updated.id(), updated.previewMargin()).hashCode(),
                now);

        if (existingOpt.isPresent()) {
            data.updateWorkspace(updated);
        } else {
            data.createWorkspace(updated);
        }
        syncBlockEntity(level, requested.anchor(), updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> regenerateHallwayRouting(ServerLevel level, MKStructureWorkspace requested) {
        MKWorkspaceBackupManifestWriter.requireTransaction("regenerate-hallway-routing");
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
        MKWorkspaceBackupManifestWriter.requireTransaction("add-workspace-variant");
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
                    .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                    .findFirst()
                    .orElse(null);
            if (sourcePiece == null) {
                return Optional.empty();
            }
            resolvedBasePieceName = getBaseName(sourcePiece);
        }
        final String targetBasePieceName = resolvedBasePieceName;

        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace);
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPiecesByLookupName(canonicalPieces);
        MKPlannedPiece basePiece = canonicalByBaseName.get(targetBasePieceName);
        if (basePiece == null) {
            return Optional.empty();
        }

        int nextVariantIndex = nextPhysicalVariantIndex(workspace, targetBasePieceName);

        MKPlannedPiece variantPiece = toVariantPiece(basePiece, nextVariantIndex);
        if (sourcePiece != null) {
            variantPiece = bindVariantToSourceFamily(variantPiece, sourcePiece, nextVariantIndex);
        }
        List<MKPlannedPiece> layoutPieces = physicalVariantLayoutPieces(workspace, canonicalPieces, canonicalByBaseName,
                List.of(variantPiece));
        MKWorkspaceGridLayout.Placement placement = alignedVariantPlacement(workspace, targetBasePieceName,
                nextVariantIndex, variantPiece, layoutPieces);

        MKWorkspacePieceDefinition templatePiece = resolveVariantSourcePiece(workspace, targetBasePieceName,
                nextVariantIndex, sourcePiece);
        if (templatePiece == null) {
            return Optional.empty();
        }

        MKWorkspacePieceDefinition generatedPiece = scaffoldBuilder.cloneFromTemplate(level, workspace, templatePiece,
                variantPiece, placement);
        List<MKWorkspacePieceDefinition> updatedPieces = new java.util.ArrayList<>(workspace.pieces());
        updatedPieces.add(generatedPiece);
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> addWorkspaceVariantsForAll(ServerLevel level, BlockPos anchor) {
        MKWorkspaceBackupManifestWriter.requireTransaction("add-all-workspace-variants");
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().isEmpty()) {
            return Optional.empty();
        }

        return addWorkspaceVariantsForBaseNames(level, anchor, data, workspace,
                physicalTemplateBasePieceNames(workspace));
    }

    public Optional<MKStructureWorkspace> addMissingWorkspaceVariantsForAll(ServerLevel level, BlockPos anchor) {
        MKWorkspaceBackupManifestWriter.requireTransaction("add-missing-workspace-variants");
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().isEmpty()) {
            return Optional.empty();
        }

        return addWorkspaceVariantsForBaseNames(level, anchor, data, workspace,
                basePieceNamesWithoutPhysicalVariants(workspace));
    }

    private Optional<MKStructureWorkspace> addWorkspaceVariantsForBaseNames(ServerLevel level, BlockPos anchor,
                                                                           IMKStructureWorkspaceData data,
                                                                           MKStructureWorkspace workspace,
                                                                           List<String> basePieceNames) {
        if (basePieceNames.isEmpty()) {
            return Optional.of(workspace);
        }
        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace);
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPiecesByLookupName(canonicalPieces);

        List<MKWorkspacePieceDefinition> templatePieces = new ArrayList<>();
        List<MKPlannedPiece> variantPieces = new ArrayList<>();
        for (String basePieceName : basePieceNames) {
            MKPlannedPiece basePiece = canonicalByBaseName.get(basePieceName);
            if (basePiece == null) {
                return Optional.empty();
            }
            int nextVariantIndex = nextPhysicalVariantIndex(workspace, basePieceName);
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

        List<MKWorkspacePieceDefinition> generatedPieces = new ArrayList<>();
        for (int i = 0; i < variantPieces.size(); i++) {
            MKPlannedPiece variantPiece = variantPieces.get(i);
            MKWorkspaceGridLayout.Placement placement = alignedVariantPlacement(workspace, getBaseName(variantPiece),
                    getVariantIndex(variantPiece), variantPiece, layoutPieces);
            generatedPieces.add(scaffoldBuilder.cloneFromTemplate(level, workspace, templatePieces.get(i),
                    variantPiece, placement));
        }

        List<MKWorkspacePieceDefinition> updatedPieces = new ArrayList<>(workspace.pieces());
        updatedPieces.addAll(generatedPieces);
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> deleteWorkspaceVariant(ServerLevel level, BlockPos anchor, UUID pieceId) {
        MKWorkspaceBackupManifestWriter.requireTransaction("delete-workspace-variant");
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        Optional<MKWorkspacePieceDefinition> variantOpt = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(pieceId))
                .filter(piece -> MKWorkspaceContentSelectionTags.purpose(piece) ==
                        MKWorkspaceTemplatePurpose.FAMILY_VARIANT)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .findFirst();
        if (variantOpt.isEmpty()) {
            return Optional.empty();
        }

        writeBackupBeforeMutation(level, workspace, "delete-variant", "variant deletion");
        MKWorkspacePieceDefinition variant = variantOpt.get();
        scaffoldBuilder.clearExistingPieceContents(level, List.of(variant), workspace.anchor());
        List<MKWorkspacePieceDefinition> updatedPieces = workspace.pieces().stream()
                .filter(piece -> !piece.pieceId().equals(pieceId))
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    private Optional<MKStructureWorkspace> applyWorkspaceVariantMutations(ServerLevel level,
                                                                          MKStructureWorkspace existing,
                                                                          List<MKWorkspaceVariantAddition> addedVariants,
                                                                          List<UUID> deletedVariantPieceIds) {
        MKStructureWorkspace current = existing;
        if (!deletedVariantPieceIds.isEmpty()) {
            Optional<MKStructureWorkspace> deleted = applyWorkspaceVariantDeletions(level, current,
                    deletedVariantPieceIds);
            if (deleted.isEmpty()) {
                return Optional.empty();
            }
            current = deleted.get();
        }
        for (MKWorkspaceVariantAddition addition : addedVariants) {
            if (addition.basePieceName().isBlank()) {
                return Optional.empty();
            }
            Optional<MKStructureWorkspace> added = addWorkspaceVariant(level, current.anchor(),
                    addition.basePieceName(), addition.sourcePieceName());
            if (added.isEmpty()) {
                return Optional.empty();
            }
            current = added.get();
        }
        Optional<MKWorkspaceInvalidationReport> report = variantMutationReport(existing, addedVariants,
                deletedVariantPieceIds);
        if (report.isPresent()) {
            long nowEpochMillis = System.currentTimeMillis();
            MKStructureWorkspace workspaceWithDirtyLayers = preflightForReport(current, report.get(), nowEpochMillis)
                    .workspaceWithDirtyLayers();
            current = workspaceForUpdate(current, current, current.pieces(), nowEpochMillis,
                    workspaceWithDirtyLayers.layerStates());
            IMKStructureWorkspaceData.get(level).updateWorkspace(current);
            syncBlockEntity(level, current.anchor(), current.id());
        }
        return Optional.of(current);
    }

    private Optional<MKStructureWorkspace> applyWorkspaceVariantDeletions(ServerLevel level,
                                                                          MKStructureWorkspace existing,
                                                                          List<UUID> deletedVariantPieceIds) {
        List<MKWorkspacePieceDefinition> variants = workspaceVariantPiecesForDeletion(existing, deletedVariantPieceIds);
        if (variants.isEmpty()) {
            return Optional.empty();
        }
        LinkedHashSet<UUID> removedIds = variants.stream()
                .map(MKWorkspacePieceDefinition::pieceId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        scaffoldBuilder.clearExistingPieceContents(level, variants, existing.anchor());
        List<MKWorkspacePieceDefinition> remainingPieces = existing.pieces().stream()
                .filter(piece -> !removedIds.contains(piece.pieceId()))
                .toList();
        long nowEpochMillis = System.currentTimeMillis();
        MKWorkspaceInvalidationReport report = variantMutationReport(existing, List.of(), deletedVariantPieceIds)
                .orElseGet(() -> MKWorkspaceInvalidationReport.noChanges("No variant changes were staged."));
        MKStructureWorkspace workspaceWithDirtyLayers = preflightForReport(existing, report, nowEpochMillis)
                .workspaceWithDirtyLayers();
        MKStructureWorkspace updated = workspaceForUpdate(existing, existing, remainingPieces, nowEpochMillis,
                workspaceWithDirtyLayers.layerStates());
        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, existing.anchor(), updated.id());
        return Optional.of(updated);
    }

    private MKWorkspaceInvalidationReport withVariantMutationReport(MKStructureWorkspace existing,
                                                                    MKWorkspaceInvalidationReport report,
                                                                    List<MKWorkspaceVariantAddition> addedVariants,
                                                                    List<UUID> deletedVariantPieceIds) {
        Optional<MKWorkspaceInvalidationReport> variantReport = variantMutationReport(existing, addedVariants,
                deletedVariantPieceIds);
        if (variantReport.isEmpty()) {
            return report;
        }
        if (isNoChangeReport(report)) {
            return variantReport.get();
        }
        return mergeReports(List.of(report, variantReport.get()));
    }

    private Optional<MKWorkspaceInvalidationReport> variantMutationReport(MKStructureWorkspace existing,
                                                                          List<MKWorkspaceVariantAddition> addedVariants,
                                                                          List<UUID> deletedVariantPieceIds) {
        List<MKWorkspacePieceDefinition> deletedVariants = workspaceVariantPiecesForDeletion(existing,
                deletedVariantPieceIds);
        List<MKWorkspaceRelayoutImpact> additionImpacts = workspaceVariantAdditionImpacts(existing, addedVariants);
        if (deletedVariants.isEmpty() && additionImpacts.isEmpty()) {
            return Optional.empty();
        }
        ArrayList<MKWorkspaceRelayoutImpact> impacts = new ArrayList<>(additionImpacts);
        impacts.addAll(deletedVariants.stream()
                .map(piece -> new MKWorkspaceRelayoutImpact(
                        "removed",
                        piece.pieceName(),
                        getBaseName(piece),
                        piece.variantIndex(),
                        piece.plannerId().toString(),
                        stableSlotKey(piece.tags()),
                        "authored variant scaffold will be cleared from the workspace"
                ))
                .toList());
        int addedCount = additionImpacts.size();
        int deletedCount = deletedVariants.size();
        return Optional.of(new MKWorkspaceInvalidationReport(
                List.of(
                        MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT,
                        MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                impacts.stream()
                        .map(impact -> MKWorkspacePlannerId.of(impact.plannerId()))
                        .toList(),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.SAFE_RELAYOUT,
                variantMutationSummary(addedCount, deletedCount),
                "relayout_workspace_variants",
                List.of(),
                List.of(),
                impacts
        ));
    }

    private List<MKWorkspaceRelayoutImpact> workspaceVariantAdditionImpacts(MKStructureWorkspace workspace,
                                                                            List<MKWorkspaceVariantAddition> addedVariants) {
        if (addedVariants.isEmpty() || workspace.pieces().isEmpty()) {
            return List.of();
        }
        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace);
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPiecesByLookupName(canonicalPieces);
        Map<String, Integer> nextVariantIndexes = addedVariants.stream()
                .map(MKWorkspaceVariantAddition::basePieceName)
                .distinct()
                .collect(Collectors.toMap(baseName -> baseName,
                        baseName -> nextPhysicalVariantIndex(workspace, baseName),
                        (left, ignored) -> left));
        ArrayList<MKWorkspaceRelayoutImpact> impacts = new ArrayList<>();
        for (MKWorkspaceVariantAddition addition : addedVariants) {
            MKPlannedPiece basePiece = canonicalByBaseName.get(addition.basePieceName());
            if (basePiece == null || !usesPhysicalWorkspaceCell(basePiece)) {
                continue;
            }
            int variantIndex = nextVariantIndexes.getOrDefault(addition.basePieceName(), 1);
            nextVariantIndexes.put(addition.basePieceName(), variantIndex + 1);
            MKPlannedPiece variantPiece = toVariantPiece(basePiece, variantIndex);
            impacts.add(new MKWorkspaceRelayoutImpact(
                    "new",
                    variantPiece.pieceName(),
                    getBaseName(variantPiece),
                    variantIndex,
                    variantPiece.plannerId().toString(),
                    stableSlotKey(variantPiece.tags()),
                    addition.sourcePieceName() == null ?
                            "new authored variant scaffold will be cloned from the template" :
                            "new authored variant scaffold will be cloned from " + addition.sourcePieceName()
            ));
        }
        return List.copyOf(impacts);
    }

    private String variantMutationSummary(int addedCount, int deletedCount) {
        if (addedCount > 0 && deletedCount > 0) {
            return "Adding " + addedCount + " and deleting " + deletedCount +
                    " workspace variants will relayout authored scaffold only.";
        }
        if (addedCount > 0) {
            return addedCount == 1 ?
                    "Adding 1 workspace variant will place new authored scaffold in the workspace grid." :
                    "Adding " + addedCount + " workspace variants will place new authored scaffold in the workspace grid.";
        }
        return deletedCount == 1 ?
                "Deleting 1 workspace variant will clear authored scaffold only." :
                "Deleting " + deletedCount + " workspace variants will clear authored scaffold only.";
    }

    private List<MKWorkspacePieceDefinition> workspaceVariantPiecesForDeletion(MKStructureWorkspace workspace,
                                                                               List<UUID> deletedVariantPieceIds) {
        if (deletedVariantPieceIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<UUID> requestedIds = new LinkedHashSet<>(deletedVariantPieceIds);
        return workspace.pieces().stream()
                .filter(piece -> requestedIds.contains(piece.pieceId()))
                .filter(piece -> MKWorkspaceContentSelectionTags.purpose(piece) ==
                        MKWorkspaceTemplatePurpose.FAMILY_VARIANT)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .toList();
    }

    private boolean isNoChangeReport(MKWorkspaceInvalidationReport report) {
        return report.invalidatedLayers().isEmpty() && "none".equals(report.recommendedOperation());
    }

    boolean isTerminalVariantMutationReport(MKWorkspaceInvalidationReport report) {
        return "relayout_workspace_variants".equals(report.recommendedOperation()) &&
                report.safety() == MKWorkspaceMutationSafety.SAFE_RELAYOUT &&
                !report.hasInvalidatedLayer(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS) &&
                !report.relayoutImpacts().isEmpty() &&
                report.relayoutImpacts().stream()
                        .allMatch(impact -> "new".equals(impact.outcome()) ||
                                "removed".equals(impact.outcome()));
    }

    List<String> physicalTemplateBasePieceNames(MKStructureWorkspace workspace) {
        return workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(this::getBaseName)
                .distinct()
                .toList();
    }

    List<String> basePieceNamesWithoutPhysicalVariants(MKStructureWorkspace workspace) {
        LinkedHashSet<String> basesWithPhysicalVariants = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() > 0)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(this::getBaseName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(this::getBaseName)
                .distinct()
                .filter(basePieceName -> !basesWithPhysicalVariants.contains(basePieceName))
                .toList();
    }

    List<MKPlannedPiece> physicalVariantLayoutPieces(MKStructureWorkspace workspace,
                                                     List<MKPlannedPiece> canonicalPieces,
                                                     Map<String, MKPlannedPiece> canonicalByBaseName,
                                                     List<MKPlannedPiece> newVariantPieces) {
        ArrayList<MKPlannedPiece> layoutPieces = workspace.pieces().stream()
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .sorted(Comparator.comparingInt(MKWorkspacePieceDefinition::variantIndex)
                        .thenComparingInt(piece -> piece.previewBounds().minX())
                        .thenComparingInt(piece -> piece.previewBounds().minZ())
                        .thenComparing(MKWorkspacePieceDefinition::pieceName))
                .map(piece -> toExistingLayoutPiece(piece, canonicalByBaseName))
                .collect(Collectors.toCollection(ArrayList::new));
        LinkedHashSet<String> existingBaseNames = layoutPieces.stream()
                .map(this::getBaseName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        layoutPieces.addAll(canonicalPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .filter(piece -> existingBaseNames.add(getBaseName(piece)))
                .map(this::toTemplatePiece)
                .toList());
        layoutPieces.addAll(newVariantPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .toList());
        return List.copyOf(layoutPieces);
    }

    MKWorkspaceGridLayout.Placement alignedVariantPlacement(MKStructureWorkspace workspace, String targetBaseName,
                                                           int targetVariantIndex, MKPlannedPiece targetPiece,
                                                           List<MKPlannedPiece> layoutPieces) {
        MKWorkspaceGridLayout.Placement plannedPlacement = placementForLayoutPiece(workspace, targetPiece, layoutPieces);
        int columnX = workspace.pieces().stream()
                .filter(piece -> targetBaseName.equals(getBaseName(piece)))
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(piece -> piece.previewBounds().minX())
                .min(Integer::compareTo)
                .orElse(plannedPlacement.previewBounds().minX());
        int rowZ = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == targetVariantIndex)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(piece -> piece.previewBounds().minZ())
                .min(Integer::compareTo)
                .orElse(plannedPlacement.previewBounds().minZ());
        return movePlacementOrigin(plannedPlacement, columnX, rowZ);
    }

    private MKWorkspaceGridLayout.Placement placementForLayoutPiece(MKStructureWorkspace workspace,
                                                                    MKPlannedPiece targetPiece,
                                                                    List<MKPlannedPiece> layoutPieces) {
        List<MKWorkspaceGridLayout.Placement> placements = new MKWorkspaceGridLayout().assignPlacements(
                workspace.anchor(),
                layoutPieces,
                workspace.shellMargin(),
                workspace.verticalShellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                MKWorkspaceScaffoldBuilder.GRID_COLUMNS,
                MKWorkspaceScaffoldBuilder.CELL_PADDING
        );
        int index = layoutPieces.indexOf(targetPiece);
        if (index < 0) {
            throw new IllegalArgumentException("piece is not present in layout list");
        }
        return placements.get(index);
    }

    private MKWorkspaceGridLayout.Placement movePlacementOrigin(MKWorkspaceGridLayout.Placement placement,
                                                               int minX, int minZ) {
        BoundingBox bounds = placement.previewBounds();
        int dx = minX - bounds.minX();
        int dz = minZ - bounds.minZ();
        BlockPos origin = placement.previewOrigin().offset(dx, 0, dz);
        BoundingBox movedBounds = new BoundingBox(
                bounds.minX() + dx,
                bounds.minY(),
                bounds.minZ() + dz,
                bounds.maxX() + dx,
                bounds.maxY(),
                bounds.maxZ() + dz
        );
        return new MKWorkspaceGridLayout.Placement(origin, movedBounds);
    }

    private int nextPhysicalVariantIndex(MKStructureWorkspace workspace, String basePieceName) {
        return workspace.pieces().stream()
                .filter(piece -> basePieceName.equals(getBaseName(piece)))
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .mapToInt(MKWorkspacePieceDefinition::variantIndex)
                .max()
                .orElse(0) + 1;
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
        MKWorkspaceBackupManifestWriter.requireTransaction("delete-workspace");
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
        MKWorkspaceBackupManifestWriter.requireTransaction("generate-workspace-stairs");
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().stream().anyMatch(piece -> pieceName.equals(piece.pieceName()) &&
                shouldGenerateWorkspaceStairs(piece))) {
            writeBackupBeforeMutation(level, workspace, "generate-stairs", "stair generation");
        }
        List<MKWorkspacePieceDefinition> updatedPieces = workspace.pieces().stream()
                .map(piece -> pieceName.equals(piece.pieceName()) && shouldGenerateWorkspaceStairs(piece) ?
                        stairBuilder.generateForPiece(level, workspace, piece, stairConfigOverride) : piece)
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> generateAllWorkspaceStairs(ServerLevel level, BlockPos anchor) {
        MKWorkspaceBackupManifestWriter.requireTransaction("generate-all-workspace-stairs");
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.pieces().stream().anyMatch(piece -> shouldGenerateWorkspaceStairs(piece) ||
                hasDerivedGeneratedStairState(piece))) {
            writeBackupBeforeMutation(level, workspace, "generate-all-stairs", "stair generation");
        }
        List<MKWorkspacePieceDefinition> updatedPieces = workspace.pieces().stream()
                .map(piece -> {
                    if (shouldGenerateWorkspaceStairs(piece)) {
                        return stairBuilder.generateForPiece(level, workspace, piece);
                    }
                    if (hasDerivedGeneratedStairState(piece)) {
                        clearDerivedGeneratedStairBlocksOutsidePhysicalPieces(level, workspace, piece);
                        return clearGeneratedStairState(piece);
                    }
                    return piece;
                })
                .toList();
        MKStructureWorkspace updated = workspace.withPieces(updatedPieces);
        data.updateWorkspace(updated);
        syncBlockEntity(level, anchor, updated.id());
        return Optional.of(updated);
    }

    public Optional<MKStructureWorkspace> clearWorkspaceStairs(ServerLevel level, BlockPos anchor, String pieceName) {
        MKWorkspaceBackupManifestWriter.requireTransaction("clear-workspace-stairs");
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

    private boolean shouldGenerateWorkspaceStairs(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) &&
                usesPhysicalWorkspaceCell(piece.tags());
    }

    private boolean hasDerivedGeneratedStairState(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceTemplateReuseTags.isDerived(piece.tags()) &&
                (!piece.generatedStairPositions().isEmpty() ||
                        !"none".equals(piece.tags().getOrDefault("generated_stair_mode", "none")));
    }

    private MKWorkspacePieceDefinition clearGeneratedStairState(MKWorkspacePieceDefinition piece) {
        Map<String, String> tags = new java.util.LinkedHashMap<>(piece.tags());
        tags.put("generated_stair_mode", MKWorkspaceStairMode.NONE.getSerializedName());
        tags.put("generated_stair_revision", Long.toString(System.currentTimeMillis()));
        tags.keySet().removeIf(key -> key.startsWith("resolved_"));
        return piece.withGeneratedStairs(List.of(), tags);
    }

    private void clearDerivedGeneratedStairBlocksOutsidePhysicalPieces(ServerLevel level, MKStructureWorkspace workspace,
                                                                       MKWorkspacePieceDefinition derivedPiece) {
        for (BlockPos pos : derivedPiece.generatedStairPositions()) {
            if (!isInsidePhysicalPieceBounds(workspace, pos)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private boolean isInsidePhysicalPieceBounds(MKStructureWorkspace workspace, BlockPos pos) {
        return workspace.pieces().stream()
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .anyMatch(piece -> piece.previewBounds().isInside(pos) || piece.exportBounds().isInside(pos));
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
                expandBounds(workspaceBounds, MKWorkspaceScaffoldBuilder.CLEAR_MARGIN,
                        WORKSPACE_OPEN_VERTICAL_MARGIN).isInside(pos);
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

    private BoundingBox expandBounds(BoundingBox bounds, int horizontalMargin, int verticalMargin) {
        return new BoundingBox(
                bounds.minX() - horizontalMargin,
                bounds.minY() - verticalMargin,
                bounds.minZ() - horizontalMargin,
                bounds.maxX() + horizontalMargin,
                bounds.maxY() + verticalMargin,
                bounds.maxZ() + horizontalMargin
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
                workspace == null ? null : data.getSamplePreviewState(workspace.id()).orElse(null),
                importService.discoverManifestIds(), discoverBackupFileNames(player, anchor, workspace)));
    }

    private List<String> discoverBackupFileNames(ServerPlayer player, BlockPos anchor,
                                                 @Nullable MKStructureWorkspace workspace) {
        List<MKWorkspaceBackupManifestDiscovery.BackupCandidate> candidates = workspace == null ?
                backupDiscovery.discoverBackups(player.serverLevel(), anchor) :
                backupDiscovery.discoverBackups(player.serverLevel(), workspace);
        return candidates.stream()
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

    public void writeApplyChangesBackup(ServerLevel level, MKStructureWorkspace workspace) {
        writeBackupBeforeMutation(level, workspace, "apply-changes", "applying workspace changes");
    }

    public Optional<MKStructureWorkspace> importWorkspaceFromManifest(ServerLevel level, BlockPos anchor,
                                                                     ResourceLocation manifestId) {
        return importWorkspaceFromManifestWithValidation(level, anchor, manifestId).workspaceOpt();
    }

    public MKWorkspaceImportResponse importWorkspaceFromManifestWithValidation(ServerLevel level, BlockPos anchor,
                                                                              ResourceLocation manifestId) {
        MKWorkspaceBackupManifestWriter.requireTransaction("import-workspace");
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
        if (blockEntity instanceof MKWorkspaceAnchor workspaceAnchor) {
            workspaceAnchor.setWorkspaceId(workspaceId);
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
        return toExistingVariantPiece(piece, basePiece);
    }

    private MKPlannedPiece toExistingVariantPiece(MKWorkspacePieceDefinition piece, MKPlannedPiece basePiece) {
        return new MKPlannedPiece(
                basePiece.roleId(),
                piece.pieceName(),
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                MKWorkspaceContentSelectionTags.preserveExplicitMetadata(
                        withWorkspaceTags(basePiece, "instance", piece.variantIndex()), piece.tags()),
                piece.plannerId()
        );
    }

    private MKPlannedPiece bindVariantToSourceFamily(MKPlannedPiece variantPiece,
                                                      MKWorkspacePieceDefinition sourcePiece,
                                                      int variantIndex) {
        Map<String, String> tags = MKWorkspaceContentSelectionTags.preserveExplicitMetadata(
                variantPiece.tags(), sourcePiece.tags());
        tags = MKWorkspaceContentSelectionTags.applyFamily(tags,
                MKWorkspaceContentSelectionTags.topologySlotId(sourcePiece),
                MKWorkspaceContentSelectionTags.familyId(sourcePiece),
                MKWorkspaceContentSelectionTags.familyWeight(sourcePiece.tags()),
                MKWorkspaceContentSelectionTags.familyEnabled(sourcePiece.tags()));
        tags = MKWorkspaceContentSelectionTags.applyTemplate(tags, MKWorkspaceTemplatePurpose.FAMILY_VARIANT,
                variantPiece.pieceName(), 1, true);
        return new MKPlannedPiece(variantPiece.roleId(), variantPiece.pieceName(), variantPiece.interiorWidth(),
                variantPiece.interiorLength(), variantPiece.interiorHeight(), variantPiece.connectors(), tags,
                variantPiece.plannerId());
    }

    private MKPlannedPiece toExistingLayoutPiece(MKWorkspacePieceDefinition piece,
                                                 Map<String, MKPlannedPiece> canonicalByBaseName) {
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
                MKWorkspaceContentSelectionTags.preserveExplicitMetadata(
                        withWorkspaceTags(basePiece, piece.variantIndex() == 0 ? "template" : "instance",
                                piece.variantIndex()), piece.tags()),
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
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, getBaseName(basePiece));
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put("workspace_piece_kind", pieceKind);
        MKWorkspaceTemplatePurpose purpose = "template".equals(pieceKind) ?
                (tags.containsKey(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID) ?
                        MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD :
                        MKWorkspaceTemplatePurpose.FAMILY_CANONICAL) :
                MKWorkspaceTemplatePurpose.FAMILY_VARIANT;
        String topologySlotId = MKWorkspaceContentSelectionTags.topologySlotId(basePiece.roleId(), tags);
        if (purpose == MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD) {
            tags.putAll(MKWorkspaceContentSelectionTags.clearFamily(
                    MKWorkspaceContentSelectionTags.applySlot(tags, topologySlotId)));
        } else {
            String familyId = MKWorkspaceContentSelectionTags.familyId(basePiece.pieceName(), tags);
            tags.putAll(MKWorkspaceContentSelectionTags.applyFamily(tags, topologySlotId, familyId,
                    MKWorkspaceContentSelectionTags.familyWeight(tags),
                    MKWorkspaceContentSelectionTags.familyEnabled(tags)));
        }
        tags.putAll(MKWorkspaceContentSelectionTags.applyTemplate(tags, purpose,
                purpose.variant() ? basePiece.pieceName() + "." + variantIndex : "",
                MKWorkspaceContentSelectionTags.variantWeight(tags),
                MKWorkspaceContentSelectionTags.variantEnabled(tags)));
        return tags;
    }

    private Map<String, MKPlannedPiece> canonicalPiecesByLookupName(List<MKPlannedPiece> canonicalPieces) {
        java.util.LinkedHashMap<String, MKPlannedPiece> piecesByName = new java.util.LinkedHashMap<>();
        for (MKPlannedPiece piece : canonicalPieces) {
            piecesByName.putIfAbsent(piece.pieceName(), piece);
            piecesByName.putIfAbsent(getBaseName(piece), piece);
        }
        return Map.copyOf(piecesByName);
    }

    private MKWorkspacePieceDefinition resolveVariantSourcePiece(MKStructureWorkspace workspace, String targetBaseName,
                                                                 int targetVariantIndex,
                                                                 MKWorkspacePieceDefinition explicitSourcePiece) {
        if (explicitSourcePiece != null) {
            return explicitSourcePiece;
        }
        MKWorkspacePieceDefinition baseTemplate = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0 && targetBaseName.equals(getBaseName(piece)))
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
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
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .findFirst()
                .orElse(null);
        if (sameVariantSource != null) {
            return sameVariantSource;
        }
        return workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(piece -> sourceId.equals(getBaseName(piece)))
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .findFirst()
                .orElse(null);
    }

    private String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    private String getBaseName(MKPlannedPiece piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    private int getVariantIndex(MKPlannedPiece piece) {
        try {
            return Integer.parseInt(piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0"));
        } catch (NumberFormatException ignored) {
            return 0;
        }
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
                existing.namespace(), existing.structureName(), requested.shellMargin(), existing.verticalShellMargin(),
                requested.exteriorAirMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin(),
                        requested.palette(), requested.namespace(), requested.structureName(),
                        requested.shellMargin(), requested.verticalShellMargin(), requested.exteriorAirMargin()));
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

    private boolean canApplyRampartAccessPatch(MKStructureWorkspace existing, MKStructureWorkspace requested) {
        if (!MKWalledKeepPlannerSettings.PLANNER_ID.equals(existing.topologyProfile().plannerId()) ||
                !MKWalledKeepPlannerSettings.PLANNER_ID.equals(requested.topologyProfile().plannerId()) ||
                existing.pieces().isEmpty()) {
            return false;
        }
        MKWalledKeepPlannerSettings existingSettings = MKWalledKeepPlannerSettings.from(existing.topologyProfile());
        MKWalledKeepPlannerSettings requestedSettings = MKWalledKeepPlannerSettings.from(requested.topologyProfile());
        if (existingSettings.rampartAccessEnabled() || !requestedSettings.rampartAccessEnabled()) {
            return false;
        }
        MKWorkspaceTopologyProfile normalizedRequestedProfile = requestedSettings.withRampartAccessEnabled(false)
                .applyTo(requested.topologyProfile());
        MKStructureWorkspace normalizedRequested = withTopologyProfile(requested, normalizedRequestedProfile);
        if (!settingsComparisonTag(existing, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(normalizedRequested, existing.id(), requested.previewMargin()))) {
            return false;
        }
        return !rampartAccessPatchTargets(requested).isEmpty();
    }

    private Optional<MKWorkspaceInvalidationReport> rampartAccessPatchReport(MKStructureWorkspace existing,
                                                                             MKStructureWorkspace requested) {
        if (!canApplyRampartAccessPatch(existing, requested)) {
            return Optional.empty();
        }
        List<MKPlannedPiece> targets = rampartAccessPatchTargets(requested);
        List<MKWorkspaceRelayoutImpact> impacts = targets.stream()
                .map(piece -> new MKWorkspaceRelayoutImpact(
                        "scaffold_patch",
                        piece.pieceName(),
                        getBaseName(piece),
                        0,
                        piece.plannerId().toString(),
                        stableSlotKey(piece.tags()),
                        "rampart access openings will be carved into the existing corner entry scaffold"
                ))
                .toList();
        return Optional.of(new MKWorkspaceInvalidationReport(
                List.of(
                        MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                targets.stream().map(MKPlannedPiece::plannerId).toList(),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.SAFE_BLOCK_SUBSTITUTION,
                "Rampart access can be applied by carving openings into existing corner entry scaffold.",
                "patch_rampart_access_openings",
                List.of("Corner entry authored blocks are preserved except for the new rampart access openings."),
                List.of(),
                impacts
        ));
    }

    private Optional<MKStructureWorkspace> patchRampartAccessOpenings(ServerLevel level,
                                                                      MKStructureWorkspace existing,
                                                                      MKStructureWorkspace requested) {
        if (!canApplyRampartAccessPatch(existing, requested)) {
            return Optional.empty();
        }
        long nowEpochMillis = System.currentTimeMillis();
        MKStructureWorkspace updated = workspaceForUpdate(existing, requested, existing.pieces(), nowEpochMillis);
        return Optional.of(applyRampartAccessOpenings(level, updated, requested, nowEpochMillis));
    }

    private MKStructureWorkspace applyRampartAccessOpenings(ServerLevel level,
                                                            MKStructureWorkspace workspaceWithPieces,
                                                            MKStructureWorkspace requested,
                                                            long nowEpochMillis) {
        List<MKPlannedPiece> targets = rampartAccessPatchTargets(requested);
        if (targets.isEmpty()) {
            return workspaceWithPieces;
        }
        Map<String, MKWorkspacePieceDefinition> existingByBaseName = workspaceWithPieces.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .collect(Collectors.toMap(piece -> getBaseName(piece), piece -> piece, (first, ignored) -> first));
        boolean patched = false;
        for (MKPlannedPiece target : targets) {
            MKWorkspacePieceDefinition existingPiece = existingByBaseName.get(getBaseName(target));
            if (existingPiece == null) {
                continue;
            }
            List<MKPlannedConnector> openings = rampartAccessOpenings(requested, target);
            scaffoldBuilder.carveOpeningOnlyConnectors(level, requested, existingPiece, target, openings);
            patched = true;
        }
        if (!patched) {
            return workspaceWithPieces;
        }
        MKStructureWorkspace refreshed = layerStateService.refreshLayers(
                layerStateService.ensureLayerStates(workspaceWithPieces, nowEpochMillis),
                List.of(
                        MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                settingsComparisonTag(requested, workspaceWithPieces.id(), requested.previewMargin()).hashCode(),
                nowEpochMillis);
        IMKStructureWorkspaceData.get(level).updateWorkspace(refreshed);
        syncBlockEntity(level, refreshed.anchor(), refreshed.id());
        return refreshed;
    }

    private List<MKPlannedPiece> rampartAccessPatchTargets(MKStructureWorkspace workspace) {
        if (!MKWalledKeepPlannerSettings.from(workspace.topologyProfile()).rampartAccessEnabled()) {
            return List.of();
        }
        return plannerRegistry.plannerFor(workspace).createCanonicalPieces(workspace).stream()
                .map(this::toTemplatePiece)
                .filter(piece -> !rampartAccessOpenings(workspace, piece).isEmpty())
                .toList();
    }

    private List<MKPlannedConnector> rampartAccessOpenings(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        int rampartBottom = rampartAccessBottom(workspace);
        return piece.connectors().stream()
                .filter(connector -> !connector.placesJigsaw())
                .filter(connector -> !connector.facing().getAxis().isVertical())
                .filter(connector -> connector.verticalOffset() == rampartBottom)
                .toList();
    }

    private int rampartAccessBottom(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .filter(this::isPerimeterRunFamily)
                .findFirst()
                .map(linearRun -> rampartAccessBottom(workspace, linearRun))
                .orElse(7);
    }

    private int rampartAccessBottom(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition linearRun) {
        int height = Math.max(0, linearRun.interiorHeight());
        int verticalShellMargin = Math.max(0, workspace.verticalShellMargin());
        int topVoidMargin = Math.max(0, linearRun.topVoidMargin());
        return Math.max(0, height + verticalShellMargin - topVoidMargin);
    }

    private boolean isPerimeterRunFamily(MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return linearRun.topologySlotId().equals(MKWalledKeepPlannerSettings.SCOPE_ID + ".perimeter") ||
                linearRun.topologySlotId().startsWith(MKWalledKeepPlannerSettings.SCOPE_ID + ".perimeter.");
    }

    private boolean canApplyCatalogRelayout(MKStructureWorkspace existing, MKStructureWorkspace requested,
                                            List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        if (existing.pieces().isEmpty() || !canCatalogRelayoutSharePhysicalSettings(existing, requested)) {
            return false;
        }
        CatalogRelayoutTargets targets = catalogRelayoutTargets(existing, requested);
        MKStructureWorkspace targetWorkspace = workspaceForUpdate(existing, requested, existing.pieces(),
                System.currentTimeMillis());
        return relayoutService.canRelayoutCatalog(existing, targetWorkspace, targets.targetPieces(),
                targets.layoutPieces(), acceptedRemaps);
    }

    private Optional<MKWorkspacePieceRelayoutService.CatalogRelayoutSummary> catalogRelayoutSummary(
            MKStructureWorkspace existing, MKStructureWorkspace requested, long nowEpochMillis) {
        return catalogRelayoutSummary(existing, requested, nowEpochMillis, List.of());
    }

    private Optional<MKWorkspacePieceRelayoutService.CatalogRelayoutSummary> catalogRelayoutSummary(
            MKStructureWorkspace existing, MKStructureWorkspace requested, long nowEpochMillis,
            List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        if (existing.pieces().isEmpty() || !canCatalogRelayoutSharePhysicalSettings(existing, requested)) {
            return Optional.empty();
        }
        CatalogRelayoutTargets targets = catalogRelayoutTargets(existing, requested);
        MKStructureWorkspace targetWorkspace = workspaceForUpdate(existing, requested, existing.pieces(),
                nowEpochMillis);
        return relayoutService.summarizeCatalogRelayout(existing, targetWorkspace, targets.targetPieces(),
                targets.layoutPieces(), acceptedRemaps);
    }

    private MKWorkspaceInvalidationReport catalogRelayoutReport(
            MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary) {
        return catalogRelayoutReport(summary, MKWorkspaceInvalidationReport.noChanges(""));
    }

    private MKWorkspaceInvalidationReport catalogRelayoutReport(
            MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary,
            MKWorkspaceInvalidationReport baseReport) {
        LinkedHashSet<MKWorkspaceGeneratedLayer> layers = new LinkedHashSet<>();
        if (summary.movedCount() > 0 || summary.expandedCount() > 0) {
            layers.add(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT);
            layers.add(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS);
            layers.add(MKWorkspaceGeneratedLayer.RUNTIME_METADATA);
        }
        if (summary.newCount() > 0 || summary.removedCount() > 0 || summary.rebuildRequiredCount() > 0 ||
                summary.expandedCount() > 0) {
            layers.add(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS);
            layers.add(MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS);
            layers.add(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS);
            layers.add(MKWorkspaceGeneratedLayer.RUNTIME_METADATA);
        }
        MKWorkspaceMutationSafety safety = summary.rebuildRequiredCount() > 0 || summary.removedCount() > 0 ?
                MKWorkspaceMutationSafety.CONDITIONALLY_SAFE_TOPOLOGY_PATCH :
                MKWorkspaceMutationSafety.SAFE_RELAYOUT;
        ArrayList<String> warnings = new ArrayList<>(baseReport.warnings());
        warnings.addAll(summary.warnings());
        return new MKWorkspaceInvalidationReport(
                List.copyOf(layers),
                baseReport.affectedPlannerIds(),
                baseReport.preservedTemplateBindings(),
                baseReport.orphanedTemplateBindings(),
                safety,
                "Workspace catalog relayout will preserve matched physical authored templates.",
                "preserve_catalog_relayout",
                List.copyOf(warnings),
                baseReport.remapSuggestions(),
                summary.impacts()
        );
    }

    private Optional<MKStructureWorkspace> relayoutCatalogPreservingPieces(ServerLevel level,
                                                                          MKStructureWorkspace existing,
                                                                          MKStructureWorkspace requested,
                                                                          List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        long nowEpochMillis = System.currentTimeMillis();
        CatalogRelayoutTargets targets = catalogRelayoutTargets(existing, requested);
        MKStructureWorkspace targetWorkspace = workspaceForUpdate(existing, requested, existing.pieces(), nowEpochMillis);
        targetWorkspace = layerStateService.refreshLayers(layerStateService.ensureLayerStates(targetWorkspace,
                        nowEpochMillis),
                List.of(
                        MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                        MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS,
                        MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS,
                        MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT,
                        MKWorkspaceGeneratedLayer.RUNTIME_METADATA
                ),
                settingsComparisonTag(requested, existing.id(), requested.previewMargin()).hashCode(),
                nowEpochMillis);
        try {
            return relayoutService.relayoutCatalog(level, existing, targetWorkspace, targets.targetPieces(),
                            targets.layoutPieces(), acceptedRemaps)
                    .map(MKWorkspacePieceRelayoutService.RelayoutResult::workspace)
                    .map(updated -> applyRampartAccessOpenings(level, updated, requested, nowEpochMillis));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write workspace backup before catalog relayout", e);
        }
    }

    private boolean canCatalogRelayoutSharePhysicalSettings(MKStructureWorkspace existing,
                                                            MKStructureWorkspace requested) {
        return catalogRelayoutPhysicalSettingMismatches(existing, requested).isEmpty();
    }

    private List<String> catalogRelayoutUnavailableReasons(MKStructureWorkspace existing,
                                                           MKStructureWorkspace requested,
                                                           long nowEpochMillis) {
        ArrayList<String> reasons = new ArrayList<>();
        if (existing.pieces().isEmpty()) {
            reasons.add("existing workspace has no pieces");
            return List.copyOf(reasons);
        }
        reasons.addAll(catalogRelayoutPhysicalSettingMismatches(existing, requested));
        if (!reasons.isEmpty()) {
            return List.copyOf(reasons);
        }
        CatalogRelayoutTargets targets = catalogRelayoutTargets(existing, requested);
        MKStructureWorkspace targetWorkspace = workspaceForUpdate(existing, requested, existing.pieces(),
                nowEpochMillis);
        MKWorkspacePieceRelayoutService.CatalogRelayoutDiagnostics diagnostics =
                relayoutService.diagnoseCatalogRelayout(existing, targetWorkspace, targets.targetPieces(),
                        targets.layoutPieces());
        reasons.addAll(diagnostics.reasons());
        if (reasons.isEmpty()) {
            reasons.add("catalog key validation passed with " + diagnostics.existingPhysicalCount() +
                    " existing physical pieces and " + diagnostics.targetPhysicalCount() +
                    " target physical pieces, but relayout planner did not produce applicable work");
        }
        return List.copyOf(reasons);
    }

    private List<String> catalogRelayoutPhysicalSettingMismatches(MKStructureWorkspace existing,
                                                                  MKStructureWorkspace requested) {
        ArrayList<String> reasons = new ArrayList<>();
        addMismatchReason(reasons, "anchor", existing.anchor(), requested.anchor());
        addMismatchReason(reasons, "namespace", existing.namespace(), requested.namespace());
        addMismatchReason(reasons, "structure name", existing.structureName(), requested.structureName());
        addMismatchReason(reasons, "palette", existing.palette(), requested.palette());
        addMismatchReason(reasons, "stair config", existing.stairConfig(), requested.stairConfig());
        addMismatchReason(reasons, "vertical access placement", existing.verticalAccessPlacement(),
                requested.verticalAccessPlacement());
        addMismatchReason(reasons, "shell margin", existing.shellMargin(), requested.shellMargin());
        addMismatchReason(reasons, "vertical shell margin", existing.verticalShellMargin(),
                requested.verticalShellMargin());
        addMismatchReason(reasons, "exterior air margin", existing.exteriorAirMargin(),
                requested.exteriorAirMargin());
        addMismatchReason(reasons, "preview margin", existing.previewMargin(), requested.previewMargin());
        addMismatchReason(reasons, "vertical access spec", existing.verticalAccessSpec(),
                requested.verticalAccessSpec());
        return List.copyOf(reasons);
    }

    private void addMismatchReason(ArrayList<String> reasons, String field, Object existingValue,
                                   Object requestedValue) {
        if (!Objects.equals(existingValue, requestedValue)) {
            reasons.add("physical setting changed: " + field + " existing=" + existingValue +
                    " requested=" + requestedValue);
        }
    }

    private CatalogRelayoutTargets catalogRelayoutTargets(MKStructureWorkspace existing,
                                                          MKStructureWorkspace requested) {
        List<MKPlannedPiece> canonicalPieces = plannerRegistry.plannerFor(requested).createCanonicalPieces(requested);
        Map<String, MKPlannedPiece> canonicalByBaseName = canonicalPiecesByLookupName(canonicalPieces);
        Map<String, MKPlannedPiece> canonicalByStableSlot = canonicalPieces.stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .filter(piece -> !stableSlotKey(piece.tags()).isBlank())
                .collect(Collectors.toMap(piece -> stableSlotKey(piece.tags()), piece -> piece,
                        (first, ignored) -> first));
        ArrayList<MKPlannedPiece> targetPieces = canonicalPieces.stream()
                .map(this::toTemplatePiece)
                .collect(Collectors.toCollection(ArrayList::new));
        targetPieces.addAll(existing.pieces().stream()
                .filter(piece -> piece.variantIndex() > 0)
                .filter(piece -> usesPhysicalWorkspaceCell(piece.tags()))
                .map(piece -> toExistingVariantPiece(piece, canonicalByBaseName, canonicalByStableSlot))
                .flatMap(Optional::stream)
                .toList());
        List<MKPlannedPiece> layoutPieces = targetPieces.stream()
                .filter(this::usesPhysicalWorkspaceCell)
                .toList();
        return new CatalogRelayoutTargets(List.copyOf(targetPieces), List.copyOf(layoutPieces));
    }

    private Optional<MKPlannedPiece> toExistingVariantPiece(
            MKWorkspacePieceDefinition piece,
            Map<String, MKPlannedPiece> canonicalByBaseName,
            Map<String, MKPlannedPiece> canonicalByStableSlot) {
        MKPlannedPiece basePiece = canonicalByBaseName.get(getBaseName(piece));
        if (basePiece == null) {
            basePiece = canonicalByStableSlot.get(stableSlotKey(piece.tags()));
        }
        return basePiece == null ? Optional.empty() : Optional.of(toExistingVariantPiece(piece, basePiece));
    }

    private String stableSlotKey(Map<String, String> tags) {
        String key = MKWorkspaceStableSlotIdentity.key(tags);
        if (!key.isBlank()) {
            return key;
        }
        String legacyFloorRoomProfileId = legacyFloorRoomProfileId(tags);
        if (legacyFloorRoomProfileId != null) {
            return "floor_room:floor." +
                    tags.getOrDefault("workspace_floor_topology_stack_id", "") + "." +
                    tags.getOrDefault("workspace_floor_topology_floor_role", "") + "." +
                    tags.getOrDefault("workspace_floor_room_kind", "") + "." +
                    legacyFloorRoomProfileId;
        }
        return "";
    }

    private String legacyFloorRoomProfileId(Map<String, String> tags) {
        if (!tags.containsKey("workspace_floor_topology_stack_id") ||
                !tags.containsKey("workspace_floor_topology_floor_role") ||
                !tags.containsKey("workspace_floor_room_kind")) {
            return null;
        }
        String profileId = tags.get("workspace_floor_room_profile_id");
        if (profileId == null || profileId.isBlank()) {
            profileId = tags.get("workspace_floor_room_kind");
        }
        return profileId == null || profileId.isBlank() ? null : profileId;
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin) {
        return settingsComparisonTag(workspace, id, previewMargin, workspace.palette());
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin,
                                                                com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return settingsComparisonTag(workspace, id, previewMargin, palette, workspace.namespace(), workspace.structureName());
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin,
                                                                com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette palette,
                                                                String namespace, String structureName) {
        return settingsComparisonTag(workspace, id, previewMargin, palette, namespace, structureName,
                workspace.shellMargin(), workspace.verticalShellMargin(), workspace.exteriorAirMargin());
    }

    private net.minecraft.nbt.CompoundTag settingsComparisonTag(MKStructureWorkspace workspace, java.util.UUID id,
                                                                int previewMargin,
                                                                com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette palette,
                                                                String namespace, String structureName,
                                                                int shellMargin, int verticalShellMargin,
                                                                int exteriorAirMargin) {
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
                verticalShellMargin,
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

    private MKStructureWorkspace withTopologyProfile(MKStructureWorkspace workspace,
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
                workspace.verticalShellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
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
                requested.verticalShellMargin(),
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
                source.verticalShellMargin(),
                source.exteriorAirMargin(),
                source.previewMargin(),
                alignVerticalAccessMaterials(source.verticalAccessSpec(), materialSource.palette()),
                source.familyDefinitions().stream()
                        .map(family -> materialSource.familyDefinitions().stream()
                                .filter(requested -> requested.baseName().equals(family.baseName()))
                                .findFirst()
                                .map(requested -> {
                                    MKWorkspaceTopologySlotMetadata metadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
                                    return com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition.forTopologySlot(
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
                                .map(requested -> new com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition(
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

    private com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig alignStairMaterials(
            com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
            com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return new com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.stairWidth()
        );
    }

    private com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec alignVerticalAccessMaterials(
            com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec spec,
            com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return new com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec(
                spec.shaftSize(),
                spec.placement(),
                alignStairMaterials(spec.stairConfig(), palette)
        );
    }
}
