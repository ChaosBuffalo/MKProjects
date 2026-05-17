package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.block_entities.MKWorkspaceDevBlockEntity;
import com.chaosbuffalo.mknpc.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportArchiveWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportResult;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestDiscovery;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspaceIdentityRenameService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspaceMarginExpansionService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKWorkspacePieceRelayoutService;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKStructureWorkspaceMutationService;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import com.chaosbuffalo.mknpc.world.gen.workspace.stairs.MKWorkspaceStairBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private final MKWorkspacePlannerRegistry plannerRegistry = new MKWorkspacePlannerRegistry();
    private final MKWorkspaceScaffoldBuilder scaffoldBuilder = new MKWorkspaceScaffoldBuilder();
    private final MKWorkspaceStairBuilder stairBuilder = new MKWorkspaceStairBuilder();
    private final MKStructureWorkspaceImportService importService = new MKStructureWorkspaceImportService();
    private final MKWorkspaceBackupManifestDiscovery backupDiscovery = new MKWorkspaceBackupManifestDiscovery();
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspacePieceRelayoutService relayoutService = new MKWorkspacePieceRelayoutService();
    private final MKWorkspaceMarginExpansionService marginExpansionService = new MKWorkspaceMarginExpansionService();
    private final MKStructureWorkspaceMutationService mutationService = new MKStructureWorkspaceMutationService();
    private final MKWorkspaceIdentityRenameService identityRenameService = new MKWorkspaceIdentityRenameService();

    public Optional<MKStructureWorkspace> createOrUpdateTowerWorkspace(ServerLevel level, MKStructureWorkspace workspace) {
        List<String> errors = workspace.validate();
        if (!errors.isEmpty() || workspace.familyType() != MKStructureFamilyType.TOWER) {
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
            MKStructureWorkspace updated = new MKStructureWorkspace(
                    existing.id(),
                    workspace.anchor(),
                    workspace.namespace(),
                    workspace.structureName(),
                    workspace.familyType(),
                    workspace.dimensions(),
                    workspace.palette(),
                    workspace.stairConfig(),
                    workspace.verticalAccessPlacement(),
                    workspace.shellMargin(),
                    workspace.exteriorAirMargin(),
                    workspace.previewMargin(),
                    workspace.verticalAccessSpec(),
                    workspace.floorSettings(),
                    workspace.categoryProfiles(),
                    workspace.familyDefinitions(),
                    workspace.openingProfiles(),
                    workspace.linearRunFamilies(),
                    existing.createdAt(),
                    System.currentTimeMillis(),
                    existing.pieces()
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

    public Optional<MKStructureWorkspace> generateTowerWorkspace(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.familyType() != MKStructureFamilyType.TOWER) {
            return Optional.empty();
        }
        if (!workspace.validate().isEmpty()) {
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

    public Optional<MKStructureWorkspace> addTowerWorkspaceVariant(ServerLevel level, BlockPos anchor, String basePieceName) {
        return addTowerWorkspaceVariant(level, anchor, basePieceName, null);
    }

    public Optional<MKStructureWorkspace> addTowerWorkspaceVariant(ServerLevel level, BlockPos anchor,
                                                                  String basePieceName, String sourcePieceName) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }
        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.familyType() != MKStructureFamilyType.TOWER || workspace.pieces().isEmpty()) {
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

        List<MKPlannedPiece> layoutPieces = canonicalPieces.stream()
                .map(this::toTemplatePiece)
                .collect(Collectors.toList());
        layoutPieces.addAll(workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() > 0)
                .map(piece -> toExistingVariantPiece(piece, canonicalByBaseName))
                .toList());

        MKPlannedPiece variantPiece = toVariantPiece(basePiece, nextVariantIndex);
        layoutPieces.add(variantPiece);

        MKWorkspacePieceDefinition templatePiece = sourcePiece != null ? sourcePiece : workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0 && targetBasePieceName.equals(getBaseName(piece)))
                .findFirst()
                .orElse(null);
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

    public Optional<MKStructureWorkspace> addTowerWorkspaceVariantsForAll(ServerLevel level, BlockPos anchor) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKStructureWorkspace> workspaceOpt = data.getWorkspaceByAnchor(anchor);
        if (workspaceOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = workspaceOpt.get();
        if (workspace.familyType() != MKStructureFamilyType.TOWER || workspace.pieces().isEmpty()) {
            return Optional.empty();
        }

        List<String> basePieceNames = workspace.pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .map(this::getBaseName)
                .distinct()
                .toList();

        Optional<MKStructureWorkspace> current = Optional.of(workspace);
        for (String basePieceName : basePieceNames) {
            current = addTowerWorkspaceVariant(level, anchor, basePieceName);
            if (current.isEmpty()) {
                return Optional.empty();
            }
        }
        return current;
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

    public Optional<MKStructureWorkspace> generateTowerWorkspaceStairs(ServerLevel level, BlockPos anchor, String pieceName,
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

    public Optional<MKStructureWorkspace> generateAllTowerWorkspaceStairs(ServerLevel level, BlockPos anchor) {
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

    public Optional<MKStructureWorkspace> clearTowerWorkspaceStairs(ServerLevel level, BlockPos anchor, String pieceName) {
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
                basePiece.role(),
                basePiece.pieceName() + "_template",
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, "template", 0)
        );
    }

    private MKPlannedPiece toVariantPiece(MKPlannedPiece basePiece, int variantIndex) {
        return new MKPlannedPiece(
                basePiece.role(),
                basePiece.pieceName() + "_" + variantIndex,
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, "instance", variantIndex)
        );
    }

    private MKPlannedPiece toExistingVariantPiece(MKWorkspacePieceDefinition piece, Map<String, MKPlannedPiece> canonicalByBaseName) {
        String baseName = getBaseName(piece);
        MKPlannedPiece basePiece = canonicalByBaseName.get(baseName);
        if (basePiece == null) {
            throw new IllegalStateException("missing canonical piece for base name " + baseName);
        }
        return new MKPlannedPiece(
                basePiece.role(),
                piece.pieceName(),
                basePiece.interiorWidth(),
                basePiece.interiorLength(),
                basePiece.interiorHeight(),
                basePiece.connectors(),
                withWorkspaceTags(basePiece, "instance", piece.variantIndex())
        );
    }

    private Map<String, String> withWorkspaceTags(MKPlannedPiece basePiece, String pieceKind, int variantIndex) {
        java.util.LinkedHashMap<String, String> tags = new java.util.LinkedHashMap<>(basePiece.tags());
        tags.put(MKWorkspaceGridLayout.TAG_BASE_NAME, basePiece.pieceName());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, Integer.toString(variantIndex));
        tags.put("workspace_piece_kind", pieceKind);
        return tags;
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
        if (settingsComparisonTag(existing, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(existingWithRequestedMaterials, existing.id(), existing.previewMargin()))) {
            return false;
        }
        return settingsComparisonTag(existingWithRequestedMaterials, existing.id(), existing.previewMargin())
                .equals(settingsComparisonTag(requested, existing.id(), requested.previewMargin()));
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
                                .map(requested -> new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile(
                                        profile.category(),
                                        profile.roomWidth(),
                                        profile.roomLength(),
                                        profile.fullHeight(),
                                        profile.minMainPathPieces(),
                                        profile.maxMainPathPieces(),
                                        profile.maxBranchPiecesBeforeCap(),
                                        requested.paletteOverride()))
                                .orElse(profile))
                        .toList(),
                source.familyDefinitions().stream()
                        .map(family -> materialSource.familyDefinitions().stream()
                                .filter(requested -> requested.baseName().equals(family.baseName()))
                                .findFirst()
                                .map(requested -> new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition(
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
                                        requested.paletteOverride()))
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
                source.createdAt(),
                source.updatedAt(),
                source.pieces()
        );
    }

    private com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig alignStairMaterials(
            com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
            com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette palette) {
        return new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.stairWidth(),
                palette.stairBlock(),
                palette.slabBlock(),
                palette.ladderBlock()
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

