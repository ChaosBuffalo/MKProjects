package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKWorkspaceAnchor;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceGeometry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateCloneTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MKWorkspacePieceRelayoutService {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    private static final String FLAT_PLATFORM_KIND_TAG = "workspace_flat_platform_kind";
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspaceGridLayout gridLayout = new MKWorkspaceGridLayout();
    private final MKWorkspaceScaffoldBuilder scaffoldBuilder = new MKWorkspaceScaffoldBuilder();

    public record RelayoutResult(MKStructureWorkspace workspace, Path backupPath, int movedPieceCount,
                                 int movedBlockCount) {
    }

    private record PieceMove(MKWorkspacePieceDefinition original, MKWorkspacePieceDefinition moved, BlockPos delta) {
    }

    private record PieceExpansion(MKWorkspacePieceDefinition original, MKPlannedPiece targetPiece) {
    }

    private record PieceRebuild(MKWorkspacePieceDefinition original, MKPlannedPiece targetPiece, boolean remapped) {
    }

    private record BlockSnapshot(BlockState state, CompoundTag blockEntityTag) {
    }

    private record ConnectorSignature(
            MKConnectorRole role,
            Direction facing,
            int openingWidth,
            int openingHeight,
            int lateralOffset,
            int verticalOffset
    ) {
    }

    private record CatalogPlan(
            List<MKPlannedPiece> targetPieces,
            List<MKPlannedPiece> layoutPieces,
            List<PieceMove> moves,
            List<PieceExpansion> expansions,
            List<MKWorkspacePieceDefinition> removedPieces,
            List<PieceRebuild> rebuilds,
            List<MKPlannedPiece> newPieces
    ) {
        boolean hasWork() {
            return !moves.isEmpty() || !expansions.isEmpty() || !removedPieces.isEmpty() ||
                    !rebuilds.isEmpty() || !newPieces.isEmpty();
        }

        List<MKWorkspacePieceDefinition> rebuildSourcePieces() {
            return rebuilds.stream()
                    .map(PieceRebuild::original)
                    .toList();
        }

        List<MKPlannedPiece> buildPieces() {
            ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
            pieces.addAll(rebuilds.stream()
                    .map(PieceRebuild::targetPiece)
                    .toList());
            pieces.addAll(newPieces);
            return List.copyOf(pieces);
        }
    }

    private record WorkspaceCatalogTarget(List<MKPlannedPiece> targetPieces,
                                          List<MKPlannedPiece> layoutPieces,
                                          Map<String, String> sourceCatalogKeyByTargetKey) {
    }

    public record CatalogRelayoutSummary(int preservedCount, int movedCount, int expandedCount, int newCount,
                                         int removedCount, int rebuildRequiredCount,
                                         List<MKWorkspaceRelayoutImpact> impacts) {
        public boolean hasWork() {
            return movedCount > 0 || expandedCount > 0 || newCount > 0 || removedCount > 0 ||
                    rebuildRequiredCount > 0;
        }

        public List<String> warnings() {
            ArrayList<String> warnings = new ArrayList<>();
            if (preservedCount > 0) {
                warnings.add(preservedCount + " physical authored templates will be preserved.");
            }
            if (movedCount > 0) {
                warnings.add(movedCount + " preserved templates will move to new catalog positions.");
            }
            if (expandedCount > 0) {
                warnings.add(expandedCount + " templates will expand while keeping existing authored blocks.");
            }
            if (newCount > 0) {
                long clonedNewCount = impacts.stream()
                        .filter(impact -> "new".equals(impact.outcome()))
                        .filter(impact -> impact.reason().contains("will be cloned from "))
                        .count();
                long scaffoldedNewCount = newCount - clonedNewCount;
                if (scaffoldedNewCount > 0) {
                    warnings.add(scaffoldedNewCount + " new physical template slots will be scaffolded.");
                }
                if (clonedNewCount > 0) {
                    warnings.add(clonedNewCount + " new physical template slots will be cloned from selected source templates.");
                }
            }
            if (removedCount > 0) {
                warnings.add(removedCount + " removed physical template slots will be cleared.");
            }
            if (rebuildRequiredCount > 0) {
                warnings.add(rebuildRequiredCount + " resized or connector-changed templates will be cleared and rebuilt.");
            }
            return List.copyOf(warnings);
        }

        public CatalogRelayoutSummary {
            impacts = List.copyOf(impacts);
        }
    }

    public record CatalogRelayoutDiagnostics(List<String> reasons, int existingPhysicalCount,
                                             int targetPhysicalCount) {
        public CatalogRelayoutDiagnostics {
            reasons = List.copyOf(reasons);
        }

        public boolean eligible() {
            return reasons.isEmpty();
        }
    }

    public Optional<RelayoutResult> relayoutPreviewMargin(ServerLevel level, MKStructureWorkspace workspace,
                                                          int previewMargin) throws IOException {
        MKWorkspaceBackupManifestWriter.requireTransaction("relayout-preview-margin");
        if (previewMargin < 2 || workspace.pieces().isEmpty()) {
            return Optional.empty();
        }
        if (workspace.previewMargin() == previewMargin) {
            return Optional.of(new RelayoutResult(workspace, backupWriter
                    .writeBeforeMutation(level, workspace, "preview-margin-noop").path(), 0, 0));
        }

        MKWorkspaceBackupManifestWriter.WrittenBackup backup =
                backupWriter.writeBeforeMutation(level, workspace, "preview-margin-relayout");
        MKStructureWorkspace targetWorkspace = withPreviewMargin(workspace, previewMargin);
        List<PieceMove> moves = buildMoves(targetWorkspace, workspace.pieces());
        if (moves.isEmpty()) {
            return Optional.empty();
        }
        Map<BlockPos, BlockSnapshot> snapshots = snapshotSources(level, moves);
        Map<BlockPos, BlockSnapshot> destinationSnapshots = mapDestinations(moves, snapshots);
        clearSources(level, snapshots.keySet());
        placeDestinations(level, destinationSnapshots);

        MKStructureWorkspace updated = targetWorkspace.withPieces(moves.stream().map(PieceMove::moved).toList());
        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, updated);
        return Optional.of(new RelayoutResult(updated, backup.path(), moves.size(), snapshots.size()));
    }

    public boolean canRelayoutCatalog(MKStructureWorkspace existing, MKStructureWorkspace targetWorkspace,
                                      List<MKPlannedPiece> targetPieces, List<MKPlannedPiece> layoutPieces) {
        return canRelayoutCatalog(existing, targetWorkspace, targetPieces, layoutPieces, List.of());
    }

    public boolean canRelayoutCatalog(MKStructureWorkspace existing, MKStructureWorkspace targetWorkspace,
                                      List<MKPlannedPiece> targetPieces, List<MKPlannedPiece> layoutPieces,
                                      List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return planCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces, acceptedRemaps)
                .filter(CatalogPlan::hasWork)
                .isPresent();
    }

    public Optional<CatalogRelayoutSummary> summarizeCatalogRelayout(MKStructureWorkspace existing,
                                                                     MKStructureWorkspace targetWorkspace,
                                                                     List<MKPlannedPiece> targetPieces,
                                                                     List<MKPlannedPiece> layoutPieces) {
        return summarizeCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces, List.of());
    }

    public Optional<CatalogRelayoutSummary> summarizeCatalogRelayout(MKStructureWorkspace existing,
                                                                     MKStructureWorkspace targetWorkspace,
                                                                     List<MKPlannedPiece> targetPieces,
                                                                     List<MKPlannedPiece> layoutPieces,
                                                                     List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return planCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces, acceptedRemaps)
                .map(this::summarize);
    }

    /** Summarizes a catalog-only metadata projection while matching physical pieces by their stable UUIDs. */
    public Optional<CatalogRelayoutSummary> summarizeWorkspaceCatalogRelayout(MKStructureWorkspace existing,
                                                                               MKStructureWorkspace targetWorkspace) {
        WorkspaceCatalogTarget target = workspaceCatalogTarget(existing, targetWorkspace);
        return planCatalogRelayout(existing, targetWorkspace, target.targetPieces(), target.layoutPieces(),
                List.of(), target.sourceCatalogKeyByTargetKey()).map(this::summarize);
    }

    public CatalogRelayoutDiagnostics diagnoseCatalogRelayout(MKStructureWorkspace existing,
                                                              MKStructureWorkspace targetWorkspace,
                                                              List<MKPlannedPiece> targetPieces,
                                                              List<MKPlannedPiece> layoutPieces) {
        ArrayList<String> reasons = new ArrayList<>();
        if (existing.pieces().isEmpty()) {
            reasons.add("existing workspace has no pieces");
        }
        if (!existing.anchor().equals(targetWorkspace.anchor())) {
            reasons.add("workspace anchor changed from " + existing.anchor().toShortString() +
                    " to " + targetWorkspace.anchor().toShortString());
        }

        List<MKWorkspacePieceDefinition> existingPhysical = existing.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
        findExistingCatalogKeyProblems(existingPhysical, reasons);
        findTargetCatalogKeyProblems(layoutPieces, reasons);
        if (targetPieces.isEmpty()) {
            reasons.add("target planner produced no catalog pieces");
        }
        if (layoutPieces.isEmpty()) {
            reasons.add("target planner produced no physical catalog layout pieces");
        }
        return new CatalogRelayoutDiagnostics(List.copyOf(reasons), existingPhysical.size(), layoutPieces.size());
    }

    public Optional<RelayoutResult> relayoutCatalog(ServerLevel level, MKStructureWorkspace existing,
                                                    MKStructureWorkspace targetWorkspace,
                                                    List<MKPlannedPiece> targetPieces,
                                                    List<MKPlannedPiece> layoutPieces) throws IOException {
        return relayoutCatalog(level, existing, targetWorkspace, targetPieces, layoutPieces, List.of());
    }

    public Optional<RelayoutResult> relayoutCatalog(ServerLevel level, MKStructureWorkspace existing,
                                                    MKStructureWorkspace targetWorkspace,
                                                    List<MKPlannedPiece> targetPieces,
                                                    List<MKPlannedPiece> layoutPieces,
                                                    List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) throws IOException {
        return relayoutCatalog(level, existing, targetWorkspace, targetPieces, layoutPieces, acceptedRemaps,
                Map.of());
    }

    /** Applies a projected catalog metadata change without mistaking identity changes for delete/create work. */
    public Optional<RelayoutResult> relayoutWorkspaceCatalog(ServerLevel level, MKStructureWorkspace existing,
                                                             MKStructureWorkspace targetWorkspace) throws IOException {
        WorkspaceCatalogTarget target = workspaceCatalogTarget(existing, targetWorkspace);
        return relayoutCatalog(level, existing, targetWorkspace, target.targetPieces(), target.layoutPieces(),
                List.of(), target.sourceCatalogKeyByTargetKey());
    }

    private Optional<RelayoutResult> relayoutCatalog(ServerLevel level, MKStructureWorkspace existing,
                                                     MKStructureWorkspace targetWorkspace,
                                                     List<MKPlannedPiece> targetPieces,
                                                     List<MKPlannedPiece> layoutPieces,
                                                     List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                     Map<String, String> sourceCatalogKeyByTargetKey) throws IOException {
        MKWorkspaceBackupManifestWriter.requireTransaction("relayout-workspace-catalog");
        Optional<CatalogPlan> planOpt = planCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces,
                acceptedRemaps, sourceCatalogKeyByTargetKey);
        if (planOpt.isEmpty() || !planOpt.get().hasWork()) {
            return Optional.empty();
        }

        CatalogPlan plan = planOpt.get();
        logCatalogRelayoutPlan(targetWorkspace, plan);
        MKWorkspaceBackupManifestWriter.WrittenBackup backup =
                backupWriter.writeBeforeMutation(level, existing, "catalog-preserving-relayout");

        Map<MKPlannedPiece, MKWorkspacePieceDefinition> clonedNewByPlan =
                cloneNewPiecesFromSources(level, existing, targetWorkspace, plan.newPieces(), plan.layoutPieces());

        Map<BlockPos, BlockSnapshot> moveSnapshots = snapshotSources(level, plan.moves());
        Map<BlockPos, BlockSnapshot> destinationSnapshots = mapDestinations(plan.moves(), moveSnapshots);
        Map<PieceExpansion, Map<BlockPos, BlockSnapshot>> expansionSnapshots =
                snapshotExpansionSources(level, plan.expansions());
        clearCatalogSources(level, plan.moves(), plan.expansions(), plan.removedPieces(), plan.rebuildSourcePieces());

        Map<MKPlannedPiece, MKWorkspacePieceDefinition> physicalByPlan = new HashMap<>();
        ArrayList<MKPlannedPiece> piecesToBuild = new ArrayList<>();
        piecesToBuild.addAll(plan.expansions().stream()
                .map(PieceExpansion::targetPiece)
                .toList());
        piecesToBuild.addAll(plan.buildPieces());
        piecesToBuild.removeAll(clonedNewByPlan.keySet());
        Map<MKPlannedPiece, MKWorkspacePieceDefinition> generatedByPlan = scaffoldBuilder.buildSelected(
                level, targetWorkspace, piecesToBuild, plan.layoutPieces());
        physicalByPlan.putAll(clonedNewByPlan);
        placeDestinations(level, destinationSnapshots);
        for (PieceMove move : plan.moves()) {
            MKPlannedPiece targetPiece = matchingLayoutPiece(move.moved(), plan.layoutPieces());
            if (targetPiece != null) {
                physicalByPlan.put(targetPiece, move.moved());
            }
        }
        for (PieceExpansion expansion : plan.expansions()) {
            MKWorkspacePieceDefinition generated = generatedByPlan.get(expansion.targetPiece());
            if (generated == null) {
                throw new IllegalStateException("missing generated expansion workspace piece for " +
                        expansion.targetPiece().pieceName());
            }
            Map<BlockPos, BlockSnapshot> remappedSnapshots = remapExpansionSnapshots(
                    expansion.original(), generated, expansionSnapshots.getOrDefault(expansion, Map.of()));
            placeDestinations(level, remappedSnapshots);
            physicalByPlan.put(expansion.targetPiece(), withPreservedIdentity(generated, expansion.original()));
        }
        for (MKPlannedPiece buildPiece : plan.buildPieces()) {
            if (physicalByPlan.containsKey(buildPiece)) {
                continue;
            }
            MKWorkspacePieceDefinition generated = generatedByPlan.get(buildPiece);
            if (generated == null) {
                throw new IllegalStateException("missing generated workspace piece for " + buildPiece.pieceName());
            }
            physicalByPlan.put(buildPiece, generated);
        }
        for (PieceMove move : plan.moves()) {
            MKPlannedPiece targetPiece = matchingLayoutPiece(move.moved(), plan.layoutPieces());
            if (targetPiece != null) {
                refreshSidecarMetadata(level, targetWorkspace, move.moved(), targetPiece);
            }
        }

        Map<String, MKWorkspacePieceDefinition> authoringByBaseName = new HashMap<>();
        Map<String, MKWorkspacePieceDefinition> authoringByBaseNameAndVariant = new HashMap<>();
        for (Map.Entry<MKPlannedPiece, MKWorkspacePieceDefinition> entry : physicalByPlan.entrySet()) {
            authoringByBaseName.put(baseName(entry.getKey()), entry.getValue());
            authoringByBaseNameAndVariant.put(baseName(entry.getKey()) + ":" + variantIndex(entry.getKey().tags()),
                    entry.getValue());
        }

        List<MKWorkspacePieceDefinition> updatedPieces = new ArrayList<>();
        for (MKPlannedPiece targetPiece : plan.targetPieces()) {
            if (MKWorkspaceTemplateReuseTags.isDerived(targetPiece.tags())) {
                String sourceId = MKWorkspaceTemplateReuseTags.sourceId(targetPiece.tags());
                MKWorkspacePieceDefinition sourcePiece = authoringByBaseNameAndVariant.get(sourceId + ":" +
                        variantIndex(targetPiece.tags()));
                if (sourcePiece == null) {
                    sourcePiece = authoringByBaseName.get(sourceId);
                }
                if (sourcePiece == null) {
                    throw new IllegalStateException("derived workspace piece " + targetPiece.pieceName() +
                            " references missing authoring source " + sourceId);
                }
                updatedPieces.add(scaffoldBuilder.createDerivedLogicalPiece(targetWorkspace, targetPiece, sourcePiece));
            } else {
                MKWorkspacePieceDefinition physical = physicalByPlan.get(targetPiece);
                if (physical == null) {
                    throw new IllegalStateException("missing physical workspace piece for " + targetPiece.pieceName());
                }
                updatedPieces.add(physical);
            }
        }

        MKStructureWorkspace updated = targetWorkspace.withPieces(updatedPieces);
        IMKStructureWorkspaceData.get(level).updateWorkspace(updated);
        syncBlockEntity(level, updated);
        int movedBlocks = moveSnapshots.size() + expansionSnapshots.values().stream().mapToInt(Map::size).sum();
        return Optional.of(new RelayoutResult(updated, backup.path(),
                plan.moves().size() + plan.expansions().size(), movedBlocks));
    }

    private void logCatalogRelayoutPlan(MKStructureWorkspace targetWorkspace, CatalogPlan plan) {
        CatalogRelayoutSummary summary = summarize(plan);
        MKWorkspace.LOGGER.info("Workspace catalog relayout executing anchor={} workspace={}:{} targetPieces={} " +
                        "layoutPieces={} preserved={} moved={} expanded={} new={} removed={} rebuilds={}",
                targetWorkspace.anchor().toShortString(),
                targetWorkspace.namespace(),
                targetWorkspace.structureName(),
                plan.targetPieces().size(),
                plan.layoutPieces().size(),
                summary.preservedCount(),
                summary.movedCount(),
                summary.expandedCount(),
                summary.newCount(),
                summary.removedCount(),
                summary.rebuildRequiredCount());
        for (MKWorkspaceRelayoutImpact impact : summary.impacts()) {
            MKWorkspace.LOGGER.info("Workspace catalog relayout impact detail: outcome={} base={} piece={} variant={} " +
                            "plannerId={} slot={} reason={}",
                    impact.outcome(),
                    impact.baseName(),
                    impact.pieceName(),
                    impact.variantIndex(),
                    impact.plannerId(),
                    impact.stableSlotKey().isBlank() ? impact.plannerId() : impact.stableSlotKey(),
                    impact.reason());
        }
    }

    private Optional<CatalogPlan> planCatalogRelayout(MKStructureWorkspace existing,
                                                      MKStructureWorkspace targetWorkspace,
                                                      List<MKPlannedPiece> targetPieces,
                                                      List<MKPlannedPiece> layoutPieces) {
        return planCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces, List.of());
    }

    private Optional<CatalogPlan> planCatalogRelayout(MKStructureWorkspace existing,
                                                      MKStructureWorkspace targetWorkspace,
                                                      List<MKPlannedPiece> targetPieces,
                                                      List<MKPlannedPiece> layoutPieces,
                                                      List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return planCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces, acceptedRemaps, Map.of());
    }

    private Optional<CatalogPlan> planCatalogRelayout(MKStructureWorkspace existing,
                                                      MKStructureWorkspace targetWorkspace,
                                                      List<MKPlannedPiece> targetPieces,
                                                      List<MKPlannedPiece> layoutPieces,
                                                      List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                                      Map<String, String> sourceCatalogKeyByTargetKey) {
        if (existing.pieces().isEmpty() || !existing.anchor().equals(targetWorkspace.anchor())) {
            return Optional.empty();
        }
        List<MKWorkspacePieceDefinition> existingPhysical = existing.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
        Map<String, MKWorkspacePieceDefinition> existingByKey = new LinkedHashMap<>();
        Map<MKWorkspacePlannerId, MKWorkspacePieceDefinition> existingByPlannerId = new LinkedHashMap<>();
        HashSet<MKWorkspacePlannerId> ambiguousPlannerIds = new HashSet<>();
        for (MKWorkspacePieceDefinition piece : existingPhysical) {
            String key = catalogKey(piece);
            if (key.isBlank() || existingByKey.putIfAbsent(key, piece) != null) {
                return Optional.empty();
            }
            if (existingByPlannerId.putIfAbsent(piece.plannerId(), piece) != null) {
                ambiguousPlannerIds.add(piece.plannerId());
            }
        }
        for (MKWorkspacePlannerId ambiguousPlannerId : ambiguousPlannerIds) {
            existingByPlannerId.remove(ambiguousPlannerId);
        }
        Map<MKWorkspacePlannerId, MKWorkspacePlannerId> acceptedRemapTargets = acceptedRemapTargets(acceptedRemaps);

        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(
                targetWorkspace.anchor(),
                layoutPieces,
                targetWorkspace.shellMargin(),
                targetWorkspace.verticalShellMargin(),
                targetWorkspace.exteriorAirMargin(),
                targetWorkspace.previewMargin(),
                MKWorkspaceScaffoldBuilder.GRID_COLUMNS,
                MKWorkspaceScaffoldBuilder.CELL_PADDING
        );
        Map<MKPlannedPiece, MKWorkspaceGridLayout.Placement> placementByPlan = new HashMap<>();
        Map<String, MKPlannedPiece> targetByKey = new LinkedHashMap<>();
        for (int i = 0; i < layoutPieces.size(); i++) {
            MKPlannedPiece plannedPiece = layoutPieces.get(i);
            String key = catalogKey(plannedPiece);
            if (key.isBlank() || targetByKey.putIfAbsent(key, plannedPiece) != null) {
                return Optional.empty();
            }
            placementByPlan.put(plannedPiece, placements.get(i));
        }

        List<PieceMove> moves = new ArrayList<>();
        List<PieceExpansion> expansions = new ArrayList<>();
        List<PieceRebuild> rebuilds = new ArrayList<>();
        List<MKWorkspacePieceDefinition> removedPieces = new ArrayList<>();
        List<MKPlannedPiece> newPieces = new ArrayList<>();
        HashSet<String> consumedExistingKeys = new HashSet<>();
        HashSet<MKWorkspacePlannerId> consumedPlannerIds = new HashSet<>();
        for (MKPlannedPiece targetPiece : layoutPieces) {
            String key = catalogKey(targetPiece);
            MKWorkspacePieceDefinition existingPiece = existingByKey.get(key);
            boolean remapped = false;
            if (existingPiece == null) {
                String sourceKey = sourceCatalogKeyByTargetKey.get(key);
                existingPiece = sourceKey == null ? null : existingByKey.get(sourceKey);
                remapped = existingPiece != null;
            }
            if (existingPiece == null) {
                MKWorkspacePlannerId orphanedPlannerId = acceptedRemapTargets.get(targetPiece.plannerId());
                existingPiece = orphanedPlannerId == null ? null : existingByPlannerId.get(orphanedPlannerId);
                remapped = existingPiece != null && !consumedPlannerIds.contains(orphanedPlannerId);
            }
            if (existingPiece == null) {
                newPieces.add(targetPiece);
                continue;
            }
            consumedExistingKeys.add(catalogKey(existingPiece));
            consumedPlannerIds.add(existingPiece.plannerId());
            if (canPreserveAuthoredBlocks(existingPiece, targetPiece, targetWorkspace)) {
                moves.add(createCatalogMove(targetWorkspace, existingPiece, targetPiece, placementByPlan.get(targetPiece)));
            } else if (canExpandPreservingAuthoredBlocks(existingPiece, targetPiece, targetWorkspace)) {
                expansions.add(new PieceExpansion(existingPiece, targetPiece));
            } else {
                rebuilds.add(new PieceRebuild(existingPiece, targetPiece, remapped));
            }
        }
        for (Map.Entry<String, MKWorkspacePieceDefinition> entry : existingByKey.entrySet()) {
            if (!consumedExistingKeys.contains(entry.getKey())) {
                removedPieces.add(entry.getValue());
            }
        }
        return Optional.of(new CatalogPlan(List.copyOf(targetPieces), List.copyOf(layoutPieces), List.copyOf(moves),
                List.copyOf(expansions), List.copyOf(removedPieces), List.copyOf(rebuilds),
                List.copyOf(newPieces)));
    }

    private void findExistingCatalogKeyProblems(List<MKWorkspacePieceDefinition> existingPhysical,
                                                ArrayList<String> reasons) {
        Map<String, MKWorkspacePieceDefinition> existingByKey = new LinkedHashMap<>();
        for (MKWorkspacePieceDefinition piece : existingPhysical) {
            String key = catalogKey(piece);
            if (key.isBlank()) {
                reasons.add("existing physical piece has blank catalog key: " + piece.pieceName());
                continue;
            }
            MKWorkspacePieceDefinition previous = existingByKey.putIfAbsent(key, piece);
            if (previous != null) {
                reasons.add("duplicate existing physical catalog key " + key +
                        " used by " + previous.pieceName() + " and " + piece.pieceName());
            }
        }
    }

    private void findTargetCatalogKeyProblems(List<MKPlannedPiece> layoutPieces, ArrayList<String> reasons) {
        Map<String, MKPlannedPiece> targetByKey = new LinkedHashMap<>();
        for (MKPlannedPiece piece : layoutPieces) {
            String key = catalogKey(piece);
            if (key.isBlank()) {
                reasons.add("target physical layout piece has blank catalog key: " + piece.pieceName());
                continue;
            }
            MKPlannedPiece previous = targetByKey.putIfAbsent(key, piece);
            if (previous != null) {
                reasons.add("duplicate target physical catalog key " + key +
                        " used by " + previous.pieceName() + " and " + piece.pieceName());
            }
        }
    }

    private Map<MKWorkspacePlannerId, MKWorkspacePlannerId> acceptedRemapTargets(
            List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        LinkedHashMap<MKWorkspacePlannerId, MKWorkspacePlannerId> targets = new LinkedHashMap<>();
        for (MKWorkspaceTemplateRemapSuggestion remap : acceptedRemaps) {
            targets.putIfAbsent(remap.targetPlannerId(), remap.orphanedPlannerId());
        }
        return Map.copyOf(targets);
    }

    private CatalogRelayoutSummary summarize(CatalogPlan plan) {
        int movedCount = (int) plan.moves().stream()
                .filter(move -> !move.delta().equals(BlockPos.ZERO))
                .count();
        int preservedCount = plan.moves().size() + plan.expansions().size();
        return new CatalogRelayoutSummary(
                preservedCount,
                movedCount,
                plan.expansions().size(),
                plan.newPieces().size(),
                plan.removedPieces().size(),
                plan.rebuilds().size(),
                relayoutImpacts(plan)
        );
    }

    private List<MKWorkspaceRelayoutImpact> relayoutImpacts(CatalogPlan plan) {
        ArrayList<MKWorkspaceRelayoutImpact> impacts = new ArrayList<>();
        for (PieceMove move : plan.moves()) {
            boolean moved = !move.delta().equals(BlockPos.ZERO);
            impacts.add(impact(move.moved(),
                    moved ? "moved" : "preserved",
                    moved ? "authored blocks will move to the target catalog position" :
                            "authored blocks stay in the current catalog position"));
        }
        for (PieceExpansion expansion : plan.expansions()) {
            impacts.add(impact(expansion.targetPiece(), "expanded",
                    "scaffold will expand while existing authored blocks are preserved"));
        }
        for (MKPlannedPiece newPiece : plan.newPieces()) {
            String sourcePieceName = templateCloneSourcePieceName(newPiece.tags());
            impacts.add(impact(newPiece, "new", sourcePieceName.isBlank() ?
                    "new physical authored template slot will be scaffolded" :
                    "new physical authored template slot will be cloned from " + sourcePieceName));
        }
        for (PieceRebuild rebuild : plan.rebuilds()) {
            String reason = rebuild.remapped() ?
                    "accepted remap still requires clearing and rebuilding this target slot" :
                    "existing authored blocks will be cleared because dimensions or connectors changed";
            impacts.add(impact(rebuild.targetPiece(), "rebuild", reason));
        }
        for (MKWorkspacePieceDefinition removedPiece : plan.removedPieces()) {
            impacts.add(impact(removedPiece, "removed", "physical authored template slot is not present in the target catalog"));
        }
        return List.copyOf(impacts);
    }

    private MKWorkspaceRelayoutImpact impact(MKWorkspacePieceDefinition piece, String outcome, String reason) {
        String stableKey = stableCatalogKey(piece.tags(), piece.variantIndex());
        return new MKWorkspaceRelayoutImpact(
                outcome,
                piece.pieceName(),
                piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName()),
                piece.variantIndex(),
                piece.plannerId().toString(),
                stableKey.isBlank() ? catalogKey(piece) : stableKey,
                reason
        );
    }

    private MKWorkspaceRelayoutImpact impact(MKPlannedPiece piece, String outcome, String reason) {
        int variantIndex = variantIndex(piece.tags());
        String stableKey = stableCatalogKey(piece.tags(), variantIndex);
        return new MKWorkspaceRelayoutImpact(
                outcome,
                piece.pieceName(),
                baseName(piece),
                variantIndex,
                piece.plannerId().toString(),
                stableKey.isBlank() ? catalogKey(piece) : stableKey,
                reason
        );
    }

    private Map<MKPlannedPiece, MKWorkspacePieceDefinition> cloneNewPiecesFromSources(
            ServerLevel level,
            MKStructureWorkspace existing,
            MKStructureWorkspace targetWorkspace,
            List<MKPlannedPiece> newPieces,
            List<MKPlannedPiece> layoutPieces) {
        LinkedHashMap<MKPlannedPiece, MKWorkspacePieceDefinition> cloned = new LinkedHashMap<>();
        if (newPieces.isEmpty()) {
            return Map.of();
        }
        Map<String, MKWorkspacePieceDefinition> existingByPieceName = existing.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .collect(java.util.stream.Collectors.toMap(
                        MKWorkspacePieceDefinition::pieceName,
                        piece -> piece,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        for (MKPlannedPiece newPiece : newPieces) {
            String sourcePieceName = templateCloneSourcePieceName(newPiece.tags());
            if (sourcePieceName.isBlank()) {
                continue;
            }
            MKWorkspacePieceDefinition sourcePiece = existingByPieceName.get(sourcePieceName);
            if (sourcePiece == null) {
                throw new IllegalStateException("new workspace template " + newPiece.pieceName() +
                        " requested clone source " + sourcePieceName + " but no physical authored source exists");
            }
            cloned.put(newPiece, scaffoldBuilder.cloneFromTemplate(level, targetWorkspace, sourcePiece, newPiece,
                    layoutPieces));
        }
        return Map.copyOf(cloned);
    }

    private String templateCloneSourcePieceName(Map<String, String> tags) {
        return tags.getOrDefault(MKWorkspaceTemplateCloneTags.SOURCE_PIECE_NAME_TAG, "");
    }

    private PieceMove createCatalogMove(MKStructureWorkspace workspace, MKWorkspacePieceDefinition original,
                                        MKPlannedPiece targetPiece, MKWorkspaceGridLayout.Placement placement) {
        BlockPos newOrigin = placement.previewOrigin().offset(workspace.previewMargin(), 0, workspace.previewMargin());
        BlockPos delta = newOrigin.subtract(original.worldOrigin());
        return new PieceMove(original, movedCatalogPiece(workspace, original, targetPiece,
                placement.previewBounds(), delta), delta);
    }

    private MKWorkspacePieceDefinition movedCatalogPiece(MKStructureWorkspace workspace,
                                                         MKWorkspacePieceDefinition original,
                                                         MKPlannedPiece targetPiece,
                                                         BoundingBox previewBounds,
                                                         BlockPos delta) {
        Map<String, String> tags = new LinkedHashMap<>(targetPiece.tags());
        for (Map.Entry<String, String> entry : original.tags().entrySet()) {
            if (entry.getKey().startsWith("generated_stair_")) {
                tags.put(entry.getKey(), entry.getValue());
            }
        }
        return new MKWorkspacePieceDefinition(
                original.pieceId(),
                workspace.id(),
                targetPiece.pieceName(),
                targetPiece.roleId(),
                targetPiece.plannerId(),
                variantIndex(targetPiece.tags()),
                targetDimensions(targetPiece, original.effectiveDimensions()),
                retargetConnectors(workspace, original, targetPiece),
                original.worldOrigin().offset(delta),
                shift(original.exportBounds(), delta),
                previewBounds,
                original.structureBlockPos().offset(delta),
                original.signPos().offset(delta),
                original.markerPositions().stream().map(pos -> pos.offset(delta)).toList(),
                original.generatedStairPositions().stream().map(pos -> pos.offset(delta)).toList(),
                tags
        );
    }

    private List<MKWorkspaceConnectorDefinition> retargetConnectors(MKStructureWorkspace workspace,
                                                                    MKWorkspacePieceDefinition original,
                                                                    MKPlannedPiece targetPiece) {
        ArrayList<MKPlannedConnector> targetConnectors = new ArrayList<>(targetPiece.connectors().stream()
                .filter(MKPlannedConnector::placesJigsaw)
                .toList());
        ArrayList<MKWorkspaceConnectorDefinition> retargeted = new ArrayList<>();
        for (MKWorkspaceConnectorDefinition originalConnector : original.connectors()) {
            ConnectorSignature signature = connectorSignature(originalConnector);
            int targetIndex = firstMatchingConnectorIndex(targetConnectors, signature);
            if (targetIndex < 0) {
                retargeted.add(originalConnector);
                continue;
            }
            MKPlannedConnector targetConnector = targetConnectors.remove(targetIndex);
            retargeted.add(retargetConnector(workspace, targetPiece, originalConnector, targetConnector));
        }
        return List.copyOf(retargeted);
    }

    private int firstMatchingConnectorIndex(List<MKPlannedConnector> connectors, ConnectorSignature signature) {
        for (int i = 0; i < connectors.size(); i++) {
            if (signature.equals(connectorSignature(connectors.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private MKWorkspaceConnectorDefinition retargetConnector(MKStructureWorkspace workspace,
                                                             MKPlannedPiece targetPiece,
                                                             MKWorkspaceConnectorDefinition original,
                                                             MKPlannedConnector targetConnector) {
        ResourceLocation targetPool = ResourceLocation.parse(resolveTargetPool(workspace, targetPiece,
                targetConnector.targetPoolName()));
        ResourceLocation incomingPool = ResourceLocation.parse(resolveIncomingPool(workspace,
                targetConnector.incomingPoolName()));
        ResourceLocation jigsawName = isEmptyPool(incomingPool) ?
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(), targetConnector.role().getSerializedName()) :
                incomingPool;
        ResourceLocation jigsawTarget = isEmptyPool(targetPool) ?
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(), targetName(targetConnector.role())) :
                targetPool;
        return new MKWorkspaceConnectorDefinition(
                original.role(),
                original.facing(),
                original.relativePos(),
                original.openingWidth(),
                original.openingHeight(),
                original.lateralOffset(),
                original.verticalOffset(),
                jigsawName,
                jigsawTarget,
                targetPool,
                incomingPool,
                original.jigsawOrientation(),
                original.jigsawFinalState(),
                original.jigsawJoint()
        );
    }

    private boolean canPreserveAuthoredBlocks(MKWorkspacePieceDefinition existingPiece, MKPlannedPiece targetPiece,
                                              MKStructureWorkspace targetWorkspace) {
        return targetExportWidth(targetWorkspace, targetPiece) == existingPiece.exportBounds().getXSpan() &&
                targetExportLength(targetWorkspace, targetPiece) == existingPiece.exportBounds().getZSpan() &&
                targetExportHeight(targetWorkspace, targetPiece) == existingPiece.exportBounds().getYSpan() &&
                existingPiece.effectiveDimensions().roomWidth() == targetPiece.interiorWidth() &&
                existingPiece.effectiveDimensions().roomLength() == targetPiece.interiorLength() &&
                existingPiece.effectiveDimensions().roomHeight() == targetPiece.interiorHeight() &&
                connectorSignaturesCompatible(targetWorkspace, existingPiece, targetPiece);
    }

    private boolean canExpandPreservingAuthoredBlocks(MKWorkspacePieceDefinition existingPiece,
                                                      MKPlannedPiece targetPiece,
                                                      MKStructureWorkspace targetWorkspace) {
        int targetExportWidth = targetExportWidth(targetWorkspace, targetPiece);
        int targetExportLength = targetExportLength(targetWorkspace, targetPiece);
        int targetExportHeight = targetExportHeight(targetWorkspace, targetPiece);
        return targetExportWidth >= existingPiece.exportBounds().getXSpan() &&
                targetExportLength >= existingPiece.exportBounds().getZSpan() &&
                targetExportHeight >= existingPiece.exportBounds().getYSpan() &&
                targetPiece.interiorWidth() >= existingPiece.effectiveDimensions().roomWidth() &&
                targetPiece.interiorLength() >= existingPiece.effectiveDimensions().roomLength() &&
                targetPiece.interiorHeight() >= existingPiece.effectiveDimensions().roomHeight() &&
                (targetExportWidth > existingPiece.exportBounds().getXSpan() ||
                        targetExportLength > existingPiece.exportBounds().getZSpan() ||
                        targetExportHeight > existingPiece.exportBounds().getYSpan() ||
                        targetPiece.interiorWidth() > existingPiece.effectiveDimensions().roomWidth() ||
                        targetPiece.interiorLength() > existingPiece.effectiveDimensions().roomLength() ||
                        targetPiece.interiorHeight() > existingPiece.effectiveDimensions().roomHeight()) &&
                connectorSignaturesCompatible(targetWorkspace, existingPiece, targetPiece);
    }

    private int targetExportWidth(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        int horizontalPadding = isExactBoundsScaffold(piece) ? 0 :
                workspace.shellMargin() + workspace.exteriorAirMargin();
        return piece.interiorWidth() + (2 * horizontalPadding);
    }

    private int targetExportLength(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        int horizontalPadding = isExactBoundsScaffold(piece) ? 0 :
                workspace.shellMargin() + workspace.exteriorAirMargin();
        return piece.interiorLength() + (2 * horizontalPadding);
    }

    private int targetExportHeight(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        int verticalShellMargin = isExactBoundsScaffold(piece) ? 0 : workspace.verticalShellMargin();
        return piece.interiorHeight() + (2 * verticalShellMargin);
    }

    private boolean isExactBoundsScaffold(MKPlannedPiece piece) {
        return MKWorkspacePieceGeometry.isExactBoundsScaffold(piece.tags()) ||
                !piece.tags().getOrDefault(FLAT_PLATFORM_KIND_TAG, "").isBlank();
    }

    private List<ConnectorSignature> connectorSignatures(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .map(this::connectorSignature)
                .sorted(CONNECTOR_SIGNATURE_ORDER)
                .toList();
    }

    private List<ConnectorSignature> connectorSignatures(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        return piece.connectors().stream()
                .filter(MKPlannedConnector::placesJigsaw)
                .map(this::connectorSignature)
                .sorted(CONNECTOR_SIGNATURE_ORDER)
                .toList();
    }

    private boolean connectorSignaturesCompatible(MKStructureWorkspace workspace,
                                                  MKWorkspacePieceDefinition existingPiece,
                                                  MKPlannedPiece targetPiece) {
        ArrayList<ConnectorSignature> remainingExisting = existingPiece.connectors().stream()
                .map(this::connectorSignature)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        for (ConnectorSignature targetSignature : connectorSignatures(workspace, targetPiece)) {
            if (!remainingExisting.remove(targetSignature)) {
                return false;
            }
        }
        if (remainingExisting.isEmpty()) {
            return true;
        }
        return existingPiece.connectors().stream()
                .filter(connector -> remainingExisting.contains(connectorSignature(connector)))
                .allMatch(connector -> isPreservableAuthoredInsertConnector(workspace, connector));
    }

    private boolean isPreservableAuthoredInsertConnector(MKStructureWorkspace workspace,
                                                        MKWorkspaceConnectorDefinition connector) {
        if (connector.role() != MKConnectorRole.LINK_CANDIDATE) {
            return false;
        }
        Set<ResourceLocation> insertFamilyPools = workspace.insertFamilies().stream()
                .map(family -> MKWorkspaceInsertFamilyDefinition.poolId(workspace.namespace(),
                        workspace.structureName(), family.familyId()))
                .collect(java.util.stream.Collectors.toSet());
        return insertFamilyPools.contains(connector.targetPool()) || insertFamilyPools.contains(connector.incomingPool());
    }

    private ConnectorSignature connectorSignature(MKWorkspaceConnectorDefinition connector) {
        return new ConnectorSignature(
                connector.role(),
                connector.facing(),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset());
    }

    private ConnectorSignature connectorSignature(MKPlannedConnector connector) {
        return new ConnectorSignature(
                connector.role(),
                connector.facing(),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset());
    }

    private static final Comparator<ConnectorSignature> CONNECTOR_SIGNATURE_ORDER = Comparator
            .comparing((ConnectorSignature signature) -> signature.role().name())
            .thenComparing(signature -> signature.facing().name())
            .thenComparingInt(ConnectorSignature::openingWidth)
            .thenComparingInt(ConnectorSignature::openingHeight)
            .thenComparingInt(ConnectorSignature::lateralOffset)
            .thenComparingInt(ConnectorSignature::verticalOffset);

    private String resolveTargetPool(MKStructureWorkspace workspace, MKPlannedPiece piece, String poolName) {
        String resolvedPoolName = poolName == null ? baseName(piece) : poolName;
        return parseConnectorPool(workspace, resolvedPoolName).toString();
    }

    private String resolveIncomingPool(MKStructureWorkspace workspace, String poolName) {
        if (poolName == null || poolName.isBlank()) {
            return "minecraft:empty";
        }
        return parseConnectorPool(workspace, poolName).toString();
    }

    private boolean isEmptyPool(ResourceLocation pool) {
        return EMPTY_POOL.equals(pool);
    }

    private String targetName(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD -> MKConnectorRole.MAIN_BACK.getSerializedName();
            case MAIN_BACK -> MKConnectorRole.MAIN_FORWARD.getSerializedName();
            case CONNECT_UP -> MKConnectorRole.CONNECT_DOWN.getSerializedName();
            case CONNECT_DOWN -> MKConnectorRole.CONNECT_UP.getSerializedName();
            case TOP_CAP_FORWARD -> MKConnectorRole.TOP_CAP_BACK.getSerializedName();
            case TOP_CAP_BACK -> MKConnectorRole.TOP_CAP_FORWARD.getSerializedName();
            case BRANCH -> MKConnectorRole.BRANCH.getSerializedName();
            case LINK_CANDIDATE -> MKConnectorRole.LINK_CANDIDATE.getSerializedName();
            default -> throw new IllegalStateException("Unsupported workspace connector role " + role);
        };
    }

    private ResourceLocation parseConnectorPool(MKStructureWorkspace workspace, String poolName) {
        if (poolName.contains(":")) {
            return ResourceLocation.parse(poolName);
        }
        return ResourceLocation.fromNamespaceAndPath(workspace.namespace(), workspace.structureName() + "/" + poolName);
    }

    private MKWorkspaceDimensions targetDimensions(MKPlannedPiece targetPiece, MKWorkspaceDimensions existing) {
        return new MKWorkspaceDimensions(
                targetPiece.interiorWidth(),
                targetPiece.interiorLength(),
                targetPiece.interiorHeight(),
                targetPiece.interiorHeight(),
                targetPiece.interiorHeight(),
                existing.shaftWidth(),
                existing.doorwayWidth(),
                existing.doorwayHeight()
        );
    }

    private MKPlannedPiece matchingLayoutPiece(MKWorkspacePieceDefinition piece, List<MKPlannedPiece> layoutPieces) {
        String key = catalogKey(piece);
        for (MKPlannedPiece plannedPiece : layoutPieces) {
            if (catalogKey(plannedPiece).equals(key)) {
                return plannedPiece;
            }
        }
        return null;
    }

    private void clearCatalogSources(ServerLevel level, List<PieceMove> moves, List<PieceExpansion> expansions,
                                     List<MKWorkspacePieceDefinition> removedPieces,
                                     List<MKWorkspacePieceDefinition> rebuildSourcePieces) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        for (PieceMove move : moves) {
            positions.addAll(collectMovedPositions(move.original()));
        }
        for (PieceExpansion expansion : expansions) {
            positions.addAll(collectMovedPositions(expansion.original()));
        }
        for (MKWorkspacePieceDefinition removedPiece : removedPieces) {
            positions.addAll(collectMovedPositions(removedPiece));
        }
        for (MKWorkspacePieceDefinition rebuildSourcePiece : rebuildSourcePieces) {
            positions.addAll(collectMovedPositions(rebuildSourcePiece));
        }
        clearSources(level, positions);
    }

    private Map<PieceExpansion, Map<BlockPos, BlockSnapshot>> snapshotExpansionSources(
            ServerLevel level, List<PieceExpansion> expansions) {
        LinkedHashMap<PieceExpansion, Map<BlockPos, BlockSnapshot>> snapshotsByExpansion = new LinkedHashMap<>();
        for (PieceExpansion expansion : expansions) {
            LinkedHashMap<BlockPos, BlockSnapshot> snapshots = new LinkedHashMap<>();
            for (BlockPos sourcePos : collectExportPositions(expansion.original())) {
                snapshots.put(sourcePos.immutable(), snapshot(level, sourcePos));
            }
            snapshotsByExpansion.put(expansion, snapshots);
        }
        return snapshotsByExpansion;
    }

    private Map<BlockPos, BlockSnapshot> remapExpansionSnapshots(MKWorkspacePieceDefinition original,
                                                                MKWorkspacePieceDefinition generated,
                                                                Map<BlockPos, BlockSnapshot> snapshots) {
        LinkedHashMap<BlockPos, BlockSnapshot> remapped = new LinkedHashMap<>();
        BlockPos delta = generated.worldOrigin().subtract(original.worldOrigin());
        for (Map.Entry<BlockPos, BlockSnapshot> entry : snapshots.entrySet()) {
            BlockPos destination = entry.getKey().offset(delta).immutable();
            if (contains(generated.exportBounds(), destination)) {
                remapped.put(destination, entry.getValue());
            }
        }
        return remapped;
    }

    private MKWorkspacePieceDefinition withPreservedIdentity(MKWorkspacePieceDefinition generated,
                                                            MKWorkspacePieceDefinition original) {
        return new MKWorkspacePieceDefinition(
                original.pieceId(),
                generated.workspaceId(),
                generated.pieceName(),
                generated.roleId(),
                generated.plannerId(),
                generated.variantIndex(),
                generated.effectiveDimensions(),
                generated.connectors(),
                generated.worldOrigin(),
                generated.exportBounds(),
                generated.previewBounds(),
                generated.structureBlockPos(),
                generated.signPos(),
                generated.markerPositions(),
                generated.generatedStairPositions(),
                generated.tags()
        );
    }

    private String catalogKey(MKWorkspacePieceDefinition piece) {
        return catalogKey(piece.tags(), piece.plannerId().toString(), piece.pieceName(), piece.variantIndex());
    }

    private String catalogKey(MKPlannedPiece piece) {
        return catalogKey(piece.tags(), piece.plannerId().toString(), piece.pieceName(),
                variantIndex(piece.tags()));
    }

    private String catalogKey(Map<String, String> tags, String plannerId, String pieceName, int variantIndex) {
        String contentCatalogMemberId = tags.getOrDefault(MKWorkspaceContentSelectionTags.CATALOG_MEMBER_ID, "");
        if (!contentCatalogMemberId.isBlank()) {
            String slotId = MKWorkspaceContentSelectionTags.topologySlotId(plannerId, tags);
            return "content-family:" + slotId + ":" + contentCatalogMemberId;
        }
        String stableKey = stableCatalogKey(tags, variantIndex);
        if (!stableKey.isBlank()) {
            return stableKey;
        }
        if (tags.containsKey("workspace_floor_room_profile_id")) {
            return "floor-room:" +
                    tags.getOrDefault("workspace_floor_topology_stack_id", "") + ":" +
                    tags.getOrDefault("workspace_floor_topology_floor_role", "") + ":" +
                    tags.getOrDefault("workspace_floor_room_kind", "") + ":" +
                    tags.get("workspace_floor_room_profile_id") + ":" +
                    variantIndex;
        }
        if (tags.containsKey("workspace_linear_run_family_id")) {
            return "linear-run:" +
                    tags.getOrDefault("workspace_floor_topology_stack_id", "") + ":" +
                    tags.getOrDefault("workspace_floor_topology_floor_role", "") + ":" +
                    tags.get("workspace_linear_run_family_id") + ":" +
                    tags.getOrDefault("workspace_linear_run_path_kind", "") + ":" +
                    variantIndex;
        }
        if (tags.containsKey(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID)) {
            return "insert-family:" + tags.get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID) + ":" +
                    variantIndex;
        }
        String baseName = tags.getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, pieceName);
        return plannerId + ":" + baseName + ":" + variantIndex;
    }

    private String stableCatalogKey(Map<String, String> tags, int variantIndex) {
        String key = MKWorkspaceStableSlotIdentity.key(tags);
        if (key.isBlank()) {
            key = inferredStableCatalogKey(tags);
        }
        return key.isBlank() ? "" : "stable:" + key + ":" + variantIndex;
    }

    private String inferredStableCatalogKey(Map<String, String> tags) {
        String legacyProfileId = legacyFloorRoomProfileId(tags);
        if (tags.containsKey("workspace_floor_room_profile_id") || legacyProfileId != null) {
            String profileId = tags.getOrDefault("workspace_floor_room_profile_id", legacyProfileId);
            return "floor_room:floor." +
                    tags.getOrDefault("workspace_floor_topology_stack_id", "") + "." +
                    tags.getOrDefault("workspace_floor_topology_floor_role", "") + "." +
                    tags.getOrDefault("workspace_floor_room_kind", "") + "." +
                    profileId;
        }
        if (tags.containsKey(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID)) {
            String familyId = tags.get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID);
            String kind = tags.getOrDefault(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND,
                    MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY.getSerializedName());
            if (MKWorkspaceInsertFamilyKind.INSERT_SOCKET.getSerializedName().equals(kind) ||
                    "courtyard_socket".equals(kind)) {
                return "insert_socket_family:insert_socket.insert_family." + familyId;
            }
            return "floor_insert_family:floor.insert_family." + familyId;
        }
        if (tags.containsKey("workspace_perimeter_source_slot_id") &&
                tags.containsKey("workspace_linear_run_family_id")) {
            return "keep_perimeter_linear_run:keep.perimeter." + tags.get("workspace_linear_run_family_id");
        }
        if ("courtyard_path".equals(tags.get("workspace_content_kind"))) {
            return "keep_courtyard_path:keep.courtyard.path." +
                    tags.getOrDefault("workspace_courtyard_path_shape", "");
        }
        if ("courtyard".equals(tags.get("workspace_content_kind"))) {
            return "keep_courtyard_content:keep.courtyard.content";
        }
        if (tags.containsKey("workspace_floor_topology_stack_id") &&
                tags.containsKey("workspace_linear_run_family_id")) {
            return "floor_linear_run:floor." +
                    tags.getOrDefault("workspace_floor_topology_stack_id", "") + "." +
                    tags.getOrDefault("workspace_floor_topology_floor_role", "") + "." +
                    tags.get("workspace_linear_run_family_id") + "." +
                    tags.getOrDefault("workspace_linear_run_path_kind", "");
        }
        if (tags.containsKey("workspace_linear_run_family_id")) {
            String topologySlotId = tags.getOrDefault("workspace_topology_slot_id", "");
            String familyId = tags.get("workspace_linear_run_family_id");
            String pathKind = tags.getOrDefault("workspace_linear_run_path_kind", "");
            if (topologySlotId.startsWith("tower.linear_run.")) {
                return "tower_linear_run:tower.linear_run." + familyId + "." + pathKind;
            }
            if (topologySlotId.startsWith("keep.")) {
                return "keep_linear_run:" + topologySlotId + "." + familyId;
            }
        }
        if ("room".equals(tags.get("tower_piece_kind"))) {
            String topologySlotId = tags.getOrDefault("workspace_source_topology_slot_id",
                    tags.getOrDefault("workspace_topology_slot_id", ""));
            if (topologySlotId.isBlank()) {
                return "";
            }
            if (topologySlotId.startsWith("keep.") && !tags.containsKey("workspace_vertical_stack_id")) {
                return "keep_room:" + topologySlotId;
            }
            return "vertical_stack_room:" + topologySlotId;
        }
        return "";
    }

    private String legacyFloorRoomProfileId(Map<String, String> tags) {
        if (!tags.containsKey("workspace_floor_topology_stack_id") ||
                !tags.containsKey("workspace_floor_topology_floor_role") ||
                !tags.containsKey("workspace_floor_room_kind")) {
            return null;
        }
        String roomKind = tags.get("workspace_floor_room_kind");
        return roomKind == null || roomKind.isBlank() ? null : roomKind;
    }

    private int variantIndex(Map<String, String> tags) {
        try {
            return Integer.parseInt(tags.getOrDefault(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0"));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String baseName(MKPlannedPiece piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    private void refreshSidecarMetadata(ServerLevel level, MKStructureWorkspace workspace,
                                        MKWorkspacePieceDefinition piece, MKPlannedPiece plannedPiece) {
        ensureStructureBlock(level, piece.structureBlockPos());
        BlockEntity structureEntity = level.getBlockEntity(piece.structureBlockPos());
        BlockState structureState = level.getBlockState(piece.structureBlockPos());
        if (structureEntity instanceof StructureBlockEntity structureBlock) {
            structureBlock.setMode(StructureMode.SAVE);
            structureBlock.setIgnoreEntities(true);
            structureBlock.setShowBoundingBox(true);
            structureBlock.setStructureName(ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                    workspace.structureName() + "/" + plannedPiece.pieceName()));
            structureBlock.setStructurePos(piece.worldOrigin().subtract(piece.structureBlockPos()));
            structureBlock.setStructureSize(new Vec3i(piece.exportBounds().getXSpan(),
                    piece.exportBounds().getYSpan(), piece.exportBounds().getZSpan()));
            structureBlock.setChanged();
            level.sendBlockUpdated(piece.structureBlockPos(), structureState, structureState, Block.UPDATE_ALL);
        }
        ensureSign(level, piece.signPos());
        BlockEntity signEntity = level.getBlockEntity(piece.signPos());
        BlockState signState = level.getBlockState(piece.signPos());
        if (signEntity instanceof SignBlockEntity sign) {
            SignText text = sign.getFrontText()
                    .setMessage(0, Component.literal(workspace.namespace()))
                    .setMessage(1, Component.literal(workspace.structureName()))
                    .setMessage(2, Component.literal(plannedPiece.roleId()))
                    .setMessage(3, Component.literal(plannedPiece.pieceName()));
            sign.setText(text, true);
            sign.setText(text, false);
            sign.setChanged();
            level.sendBlockUpdated(piece.signPos(), signState, signState, Block.UPDATE_ALL);
        }
        refreshConnectorMetadata(level, piece);
    }

    private void refreshConnectorMetadata(ServerLevel level, MKWorkspacePieceDefinition piece) {
        for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
            BlockPos connectorPos = piece.worldOrigin().offset(connector.relativePos());
            BlockEntity entity = level.getBlockEntity(connectorPos);
            if (!(entity instanceof JigsawBlockEntity jigsaw)) {
                continue;
            }
            BlockState state = level.getBlockState(connectorPos);
            jigsaw.setName(connector.jigsawName());
            jigsaw.setTarget(connector.jigsawTarget());
            jigsaw.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, connector.targetPool()));
            jigsaw.setFinalState(connector.jigsawFinalState());
            jigsaw.setChanged();
            level.sendBlockUpdated(connectorPos, state, state, Block.UPDATE_ALL);
        }
    }

    private void ensureStructureBlock(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof StructureBlockEntity)) {
            level.setBlock(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void ensureSign(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SignBlockEntity)) {
            level.setBlock(pos, Blocks.OAK_SIGN.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private List<PieceMove> buildMoves(MKStructureWorkspace targetWorkspace, List<MKWorkspacePieceDefinition> pieces) {
        List<MKPlannedPiece> plannedPieces = pieces.stream().map(this::toPlannedPiece).toList();
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(
                targetWorkspace.anchor(),
                plannedPieces,
                targetWorkspace.shellMargin(),
                targetWorkspace.verticalShellMargin(),
                targetWorkspace.exteriorAirMargin(),
                targetWorkspace.previewMargin(),
                MKWorkspaceScaffoldBuilder.GRID_COLUMNS,
                MKWorkspaceScaffoldBuilder.CELL_PADDING
        );
        List<PieceMove> moves = new ArrayList<>();
        for (int i = 0; i < pieces.size(); i++) {
            MKWorkspacePieceDefinition original = pieces.get(i);
            MKWorkspaceGridLayout.Placement placement = placements.get(i);
            BlockPos newOrigin = placement.previewOrigin()
                    .offset(targetWorkspace.previewMargin(), 0, targetWorkspace.previewMargin());
            BlockPos delta = newOrigin.subtract(original.worldOrigin());
            moves.add(new PieceMove(original, movedPiece(targetWorkspace, original, placement.previewBounds(), delta), delta));
        }
        return moves;
    }

    private MKWorkspacePieceDefinition movedPiece(MKStructureWorkspace workspace, MKWorkspacePieceDefinition original,
                                                  BoundingBox previewBounds, BlockPos delta) {
        return new MKWorkspacePieceDefinition(
                original.pieceId(),
                workspace.id(),
                original.pieceName(),
                original.roleId(),
                original.variantIndex(),
                original.effectiveDimensions(),
                original.connectors(),
                original.worldOrigin().offset(delta),
                shift(original.exportBounds(), delta),
                previewBounds,
                original.structureBlockPos().offset(delta),
                original.signPos().offset(delta),
                original.markerPositions().stream().map(pos -> pos.offset(delta)).toList(),
                original.generatedStairPositions().stream().map(pos -> pos.offset(delta)).toList(),
                new LinkedHashMap<>(original.tags())
        );
    }

    private MKPlannedPiece toPlannedPiece(MKWorkspacePieceDefinition piece) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        return new MKPlannedPiece(
                tags.getOrDefault("workspace_topology_slot_id", piece.roleId()),
                piece.pieceName(),
                piece.effectiveDimensions().roomWidth(),
                piece.effectiveDimensions().roomLength(),
                piece.effectiveDimensions().roomHeight(),
                piece.connectors().stream().map(this::toPlannedConnector).toList(),
                tags
        );
    }

    private MKPlannedConnector toPlannedConnector(MKWorkspaceConnectorDefinition connector) {
        return new MKPlannedConnector(
                connector.role(),
                connector.facing(),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                connector.targetPool().toString(),
                connector.incomingPool().toString()
        );
    }

    private Map<BlockPos, BlockSnapshot> snapshotSources(ServerLevel level, List<PieceMove> moves) {
        LinkedHashMap<BlockPos, BlockSnapshot> snapshots = new LinkedHashMap<>();
        for (PieceMove move : moves) {
            for (BlockPos sourcePos : collectMovedPositions(move.original())) {
                snapshots.computeIfAbsent(sourcePos.immutable(), pos -> snapshot(level, pos));
            }
        }
        return snapshots;
    }

    private Map<BlockPos, BlockSnapshot> mapDestinations(List<PieceMove> moves, Map<BlockPos, BlockSnapshot> snapshots) {
        LinkedHashMap<BlockPos, BlockSnapshot> destinations = new LinkedHashMap<>();
        for (PieceMove move : moves) {
            for (BlockPos sourcePos : collectMovedPositions(move.original())) {
                BlockSnapshot snapshot = snapshots.get(sourcePos);
                if (snapshot == null) {
                    continue;
                }
                BlockPos destination = sourcePos.offset(move.delta()).immutable();
                if (destinations.putIfAbsent(destination, snapshot) != null) {
                    throw new IllegalStateException("workspace relayout produced overlapping destination " + destination);
                }
            }
        }
        return destinations;
    }

    private List<BlockPos> collectMovedPositions(MKWorkspacePieceDefinition piece) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        positions.addAll(collectExportPositions(piece));
        positions.add(piece.structureBlockPos());
        positions.add(piece.signPos());
        positions.addAll(piece.markerPositions());
        positions.addAll(piece.generatedStairPositions());
        return List.copyOf(positions);
    }

    private List<BlockPos> collectExportPositions(MKWorkspacePieceDefinition piece) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        BoundingBox bounds = piece.exportBounds();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return List.copyOf(positions);
    }

    private BlockSnapshot snapshot(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = blockEntity == null ? null : blockEntity.saveWithFullMetadata(level.registryAccess());
        return new BlockSnapshot(state, tag);
    }

    private void clearSources(ServerLevel level, Iterable<BlockPos> sourcePositions) {
        for (BlockPos sourcePos : sourcePositions) {
            level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void placeDestinations(ServerLevel level, Map<BlockPos, BlockSnapshot> destinationSnapshots) {
        for (Map.Entry<BlockPos, BlockSnapshot> entry : destinationSnapshots.entrySet()) {
            BlockPos destination = entry.getKey();
            BlockSnapshot snapshot = entry.getValue();
            level.setBlock(destination, snapshot.state(), Block.UPDATE_ALL);
            restoreBlockEntity(level, destination, snapshot);
        }
    }

    private void restoreBlockEntity(ServerLevel level, BlockPos destination, BlockSnapshot snapshot) {
        if (snapshot.blockEntityTag() == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(destination);
        if (blockEntity == null) {
            return;
        }
        CompoundTag tag = snapshot.blockEntityTag().copy();
        tag.putInt("x", destination.getX());
        tag.putInt("y", destination.getY());
        tag.putInt("z", destination.getZ());
        blockEntity.loadWithComponents(tag, level.registryAccess());
        blockEntity.setChanged();
        level.sendBlockUpdated(destination, snapshot.state(), snapshot.state(), Block.UPDATE_ALL);
    }

    private BoundingBox shift(BoundingBox bounds, BlockPos delta) {
        return new BoundingBox(
                bounds.minX() + delta.getX(),
                bounds.minY() + delta.getY(),
                bounds.minZ() + delta.getZ(),
                bounds.maxX() + delta.getX(),
                bounds.maxY() + delta.getY(),
                bounds.maxZ() + delta.getZ()
        );
    }

    private WorkspaceCatalogTarget workspaceCatalogTarget(MKStructureWorkspace existing,
                                                           MKStructureWorkspace targetWorkspace) {
        List<MKPlannedPiece> targetPieces = targetWorkspace.pieces().stream().map(this::toPlannedPiece).toList();
        List<MKPlannedPiece> layoutPieces = targetWorkspace.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(this::toPlannedPiece)
                .toList();
        Map<UUID, MKWorkspacePieceDefinition> existingById = existing.pieces().stream()
                .collect(java.util.stream.Collectors.toMap(MKWorkspacePieceDefinition::pieceId, piece -> piece,
                        (first, ignored) -> first));
        LinkedHashMap<String, String> remaps = new LinkedHashMap<>();
        for (MKWorkspacePieceDefinition targetPiece : targetWorkspace.pieces()) {
            if (MKWorkspaceTemplateReuseTags.isDerived(targetPiece.tags())) continue;
            MKWorkspacePieceDefinition sourcePiece = existingById.get(targetPiece.pieceId());
            if (sourcePiece == null) continue;
            String targetKey = catalogKey(toPlannedPiece(targetPiece));
            String sourceKey = catalogKey(sourcePiece);
            if (!targetKey.equals(sourceKey)) remaps.put(targetKey, sourceKey);
        }
        return new WorkspaceCatalogTarget(targetPieces, layoutPieces, Map.copyOf(remaps));
    }

    private boolean contains(BoundingBox bounds, BlockPos pos) {
        return pos.getX() >= bounds.minX() && pos.getX() <= bounds.maxX() &&
                pos.getY() >= bounds.minY() && pos.getY() <= bounds.maxY() &&
                pos.getZ() >= bounds.minZ() && pos.getZ() <= bounds.maxZ();
    }

    private MKStructureWorkspace withPreviewMargin(MKStructureWorkspace workspace, int previewMargin) {
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
                workspace.verticalShellMargin(),
                workspace.exteriorAirMargin(),
                previewMargin,
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.insertFamilies(),
                workspace.createdAt(),
                System.currentTimeMillis(),
                workspace.pieces(),
                workspace.layerStates()
        );
    }

    private void syncBlockEntity(ServerLevel level, MKStructureWorkspace workspace) {
        BlockEntity blockEntity = level.getBlockEntity(workspace.anchor());
        if (blockEntity instanceof MKWorkspaceAnchor workspaceAnchor) {
            workspaceAnchor.setWorkspaceId(workspace.id());
        }
    }
}
