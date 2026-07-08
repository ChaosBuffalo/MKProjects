package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.block_entities.MKWorkspaceDevBlockEntity;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class MKWorkspacePieceRelayoutService {
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
            int verticalOffset,
            String targetPool,
            String incomingPool
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
                warnings.add(newCount + " new physical template slots will be scaffolded.");
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

    public Optional<RelayoutResult> relayoutPreviewMargin(ServerLevel level, MKStructureWorkspace workspace,
                                                          int previewMargin) throws IOException {
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
        Optional<CatalogPlan> planOpt = planCatalogRelayout(existing, targetWorkspace, targetPieces, layoutPieces,
                acceptedRemaps);
        if (planOpt.isEmpty() || !planOpt.get().hasWork()) {
            return Optional.empty();
        }

        CatalogPlan plan = planOpt.get();
        MKWorkspaceBackupManifestWriter.WrittenBackup backup =
                backupWriter.writeBeforeMutation(level, existing, "catalog-preserving-relayout");

        Map<BlockPos, BlockSnapshot> moveSnapshots = snapshotSources(level, plan.moves());
        Map<BlockPos, BlockSnapshot> destinationSnapshots = mapDestinations(plan.moves(), moveSnapshots);
        Map<PieceExpansion, Map<BlockPos, BlockSnapshot>> expansionSnapshots =
                snapshotExpansionSources(level, plan.expansions());
        clearCatalogSources(level, plan.moves(), plan.expansions(), plan.removedPieces(), plan.rebuildSourcePieces());
        placeDestinations(level, destinationSnapshots);

        Map<MKPlannedPiece, MKWorkspacePieceDefinition> physicalByPlan = new HashMap<>();
        for (PieceMove move : plan.moves()) {
            MKPlannedPiece targetPiece = matchingLayoutPiece(move.moved(), plan.layoutPieces());
            if (targetPiece != null) {
                physicalByPlan.put(targetPiece, move.moved());
                refreshSidecarMetadata(level, targetWorkspace, move.moved(), targetPiece);
            }
        }
        for (PieceExpansion expansion : plan.expansions()) {
            MKWorkspacePieceDefinition generated = scaffoldBuilder.buildSingle(level, targetWorkspace,
                    expansion.targetPiece(), plan.layoutPieces());
            Map<BlockPos, BlockSnapshot> remappedSnapshots = remapExpansionSnapshots(
                    expansion.original(), generated, expansionSnapshots.getOrDefault(expansion, Map.of()));
            placeDestinations(level, remappedSnapshots);
            physicalByPlan.put(expansion.targetPiece(), withPreservedIdentity(generated, expansion.original()));
        }
        for (MKPlannedPiece buildPiece : plan.buildPieces()) {
            MKWorkspacePieceDefinition generated = scaffoldBuilder.buildSingle(level, targetWorkspace, buildPiece,
                    plan.layoutPieces());
            physicalByPlan.put(buildPiece, generated);
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
            impacts.add(impact(newPiece, "new", "new physical authored template slot will be scaffolded"));
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
                original.variantIndex(),
                targetDimensions(targetPiece, original.effectiveDimensions()),
                original.shellMargin(),
                original.connectors(),
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

    private boolean canPreserveAuthoredBlocks(MKWorkspacePieceDefinition existingPiece, MKPlannedPiece targetPiece,
                                              MKStructureWorkspace targetWorkspace) {
        return existingPiece.shellMargin() == targetWorkspace.shellMargin() &&
                existingPiece.effectiveDimensions().roomWidth() == targetPiece.interiorWidth() &&
                existingPiece.effectiveDimensions().roomLength() == targetPiece.interiorLength() &&
                existingPiece.effectiveDimensions().roomHeight() == targetPiece.interiorHeight() &&
                Objects.equals(connectorSignatures(existingPiece),
                        connectorSignatures(targetWorkspace, targetPiece));
    }

    private boolean canExpandPreservingAuthoredBlocks(MKWorkspacePieceDefinition existingPiece,
                                                      MKPlannedPiece targetPiece,
                                                      MKStructureWorkspace targetWorkspace) {
        return existingPiece.shellMargin() == targetWorkspace.shellMargin() &&
                targetPiece.interiorWidth() >= existingPiece.effectiveDimensions().roomWidth() &&
                targetPiece.interiorLength() >= existingPiece.effectiveDimensions().roomLength() &&
                targetPiece.interiorHeight() >= existingPiece.effectiveDimensions().roomHeight() &&
                (targetPiece.interiorWidth() > existingPiece.effectiveDimensions().roomWidth() ||
                        targetPiece.interiorLength() > existingPiece.effectiveDimensions().roomLength() ||
                        targetPiece.interiorHeight() > existingPiece.effectiveDimensions().roomHeight()) &&
                Objects.equals(connectorSignatures(existingPiece),
                        connectorSignatures(targetWorkspace, targetPiece));
    }

    private List<ConnectorSignature> connectorSignatures(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .map(connector -> new ConnectorSignature(
                        connector.role(),
                        connector.facing(),
                        connector.openingWidth(),
                        connector.openingHeight(),
                        connector.lateralOffset(),
                        connector.verticalOffset(),
                        connector.targetPool().toString(),
                        connector.incomingPool().toString()))
                .toList();
    }

    private List<ConnectorSignature> connectorSignatures(MKStructureWorkspace workspace, MKPlannedPiece piece) {
        return piece.connectors().stream()
                .filter(MKPlannedConnector::placesJigsaw)
                .map(connector -> new ConnectorSignature(
                        connector.role(),
                        connector.facing(),
                        connector.openingWidth(),
                        connector.openingHeight(),
                        connector.lateralOffset(),
                        connector.verticalOffset(),
                        resolveTargetPool(workspace, piece, connector.targetPoolName()),
                        resolveIncomingPool(workspace, connector.incomingPoolName())))
                .toList();
    }

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
                generated.shellMargin(),
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
        if (tags.containsKey("workspace_insert_family_id")) {
            return "insert-family:" + tags.get("workspace_insert_family_id") + ":" + variantIndex;
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
        if (tags.containsKey("workspace_insert_family_id")) {
            return "floor_insert_family:floor.insert_family." + tags.get("workspace_insert_family_id");
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
    }

    private List<PieceMove> buildMoves(MKStructureWorkspace targetWorkspace, List<MKWorkspacePieceDefinition> pieces) {
        List<MKPlannedPiece> plannedPieces = pieces.stream().map(this::toPlannedPiece).toList();
        List<MKWorkspaceGridLayout.Placement> placements = gridLayout.assignPlacements(
                targetWorkspace.anchor(),
                plannedPieces,
                targetWorkspace.shellMargin(),
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
                original.shellMargin(),
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
        if (blockEntity instanceof MKWorkspaceDevBlockEntity workspaceDevBlockEntity) {
            workspaceDevBlockEntity.setWorkspaceId(workspace.id());
        }
    }
}
