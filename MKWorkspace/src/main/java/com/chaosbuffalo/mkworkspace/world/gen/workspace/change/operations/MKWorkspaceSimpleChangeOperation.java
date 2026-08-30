package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceImportService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeApplyResult;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequest;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspacePreparedChange;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestDiscovery;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupRestoreService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKStructureWorkspaceMutationService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspacePieceRelayoutService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspaceContentSelectionMutationService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

public final class MKWorkspaceSimpleChangeOperation implements MKWorkspaceChangeOperation<MKWorkspaceSimpleChangePayload> {
    public enum Kind {
        GENERATE("generate", "Generate Workspace"),
        GENERATE_ALL_STAIRS("generate_all_stairs", "Generate All Workspace Stairs"),
        GENERATE_STAIRS("generate_stairs", "Generate Workspace Stairs"),
        CLEAR_STAIRS("clear_stairs", "Clear Workspace Stairs"),
        SWAP_BLOCKS("swap_blocks", "Swap Workspace Blocks"),
        PREVIEW_MARGIN("preview_margin", "Change Workspace Preview Margin"),
        NORMALIZE_INSERT_SLOTS("normalize_insert_slots", "Normalize Insert Slot Identities"),
        IMPORT("import", "Import Workspace"),
        RESTORE("restore", "Restore Workspace Backup"),
        DELETE("delete", "Delete Workspace");

        private final ResourceLocation id;
        private final String title;

        Kind(String path, String title) {
            this.id = MKWorkspace.id(path);
            this.title = title;
        }

        public ResourceLocation id() {
            return id;
        }
    }

    private final Kind kind;

    public MKWorkspaceSimpleChangeOperation(Kind kind) {
        this.kind = kind;
    }

    @Override
    public ResourceLocation id() {
        return kind.id;
    }

    @Override
    public Codec<MKWorkspaceSimpleChangePayload> codec() {
        return MKWorkspaceSimpleChangePayload.CODEC;
    }

    @Override
    public MKWorkspacePreparedChange prepare(ServerPlayer player, BlockPos anchor,
                                             MKWorkspaceChangeRequest request,
                                             MKWorkspaceSimpleChangePayload payload) {
        return kind == Kind.IMPORT ? prepareImport(player, anchor, request, payload) :
                kind == Kind.RESTORE && IMKStructureWorkspaceData.get(player.serverLevel())
                        .getWorkspaceByAnchor(anchor).isEmpty() ? prepareDeletedRestore(player, anchor, request, payload) :
                prepareExisting(player, anchor, request, payload);
    }

    private MKWorkspacePreparedChange prepareDeletedRestore(ServerPlayer player, BlockPos anchor,
                                                             MKWorkspaceChangeRequest request,
                                                             MKWorkspaceSimpleChangePayload payload) {
        MKWorkspaceBackupManifestDiscovery discovery = new MKWorkspaceBackupManifestDiscovery();
        MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate = discovery.discoverBackups(
                        player.serverLevel(), anchor).stream()
                .filter(value -> value.fileName().equals(payload.target())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Backup file was not found at this anchor: " +
                        payload.target()));
        MKWorkspaceExportManifest manifest = discovery.loadBackup(candidate.path(), anchor)
                .orElseThrow(() -> new IllegalArgumentException("Backup manifest does not match this anchor."));
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        effects.add(MKWorkspaceChangeEffect.workspace(MKWorkspaceChangeEffect.Action.RESTORE,
                manifest.workspaceId(), manifest.namespace() + ":" + manifest.structureName(),
                "Restore deleted workspace metadata at its original anchor"));
        manifest.pieces().forEach(piece -> effects.add(manifestPieceEffect(MKWorkspaceChangeEffect.Action.RESTORE,
                piece, "Restore deleted authored piece")));
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(kind.id, "Confirm " + kind.title,
                "Restore deleted workspace from " + payload.target() + ".",
                MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE, false, List.of(), List.of(), List.of(),
                List.of(), effects);
        String guard = candidate.path() + ":" + lastModified(candidate.path()) + ":" + manifest.hashCode();
        return new MKWorkspacePreparedChange(request, anchor, null, 0L, guard, summary, applyPlayer -> {
            var result = new MKWorkspaceBackupRestoreService().restoreByFileName(applyPlayer.serverLevel(), anchor,
                    payload.target());
            return result.workspaceOpt().isPresent() ? MKWorkspaceChangeApplyResult.success(anchor,
                    "Deleted workspace restored from backup.") : MKWorkspaceChangeApplyResult.failure(anchor,
                    "Workspace restore failed: " + String.join("; ", result.validationErrors()));
        });
    }

    private MKWorkspacePreparedChange prepareExisting(ServerPlayer player, BlockPos anchor,
                                                       MKWorkspaceChangeRequest request,
                                                       MKWorkspaceSimpleChangePayload payload) {
        MKStructureWorkspace workspace = IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(anchor)
                .orElseThrow(() -> new IllegalArgumentException("No workspace exists at this anchor."));
        ArrayList<String> blockers = new ArrayList<>();
        ArrayList<String> warnings = new ArrayList<>();
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        String stateGuard = kind.name();
        switch (kind) {
            case GENERATE -> workspace.pieces().forEach(piece -> effects.add(pieceEffect(
                    MKWorkspaceChangeEffect.Action.UPDATE, piece, "Generate authored workspace blocks")));
            case GENERATE_ALL_STAIRS -> workspace.pieces().stream()
                    .filter(piece -> MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()))
                    .forEach(piece -> effects.add(stairEffect(MKWorkspaceChangeEffect.Action.REBUILD, piece,
                            "Generate or clear derived stair blocks from the workspace stair profile")));
            case GENERATE_STAIRS -> {
                MKWorkspacePieceDefinition piece = findPiece(workspace, payload.target(), blockers);
                if (piece != null) {
                    effects.add(stairEffect(MKWorkspaceChangeEffect.Action.REBUILD, piece,
                            "Generate stairs using " + payload.secondary() + ", " + payload.tertiary() +
                                    ", width " + payload.amount()));
                }
            }
            case CLEAR_STAIRS -> {
                MKWorkspacePieceDefinition piece = findPiece(workspace, payload.target(), blockers);
                if (piece != null) {
                    effects.add(stairEffect(MKWorkspaceChangeEffect.Action.REMOVE, piece,
                            "Clear generated stair blocks and metadata"));
                }
            }
            case SWAP_BLOCKS -> {
                ResourceLocation source = parseBlock(payload.target(), "source", blockers);
                ResourceLocation target = parseBlock(payload.secondary(), "target", blockers);
                if (source != null && target != null && source.equals(target)) {
                    blockers.add("Source and target blocks must differ.");
                }
                if (source != null && target != null) {
                    SwapScan scan = scanSwap(player, workspace, source, target);
                    effects.addAll(scan.effects());
                    stateGuard += ":" + scan.worldFingerprint();
                    if (scan.replacementCount() == 0) {
                        warnings.add("No matching source blocks are currently present in authored piece bounds.");
                    }
                }
            }
            case PREVIEW_MARGIN -> workspace.pieces().forEach(piece -> effects.add(pieceEffect(
                    MKWorkspaceChangeEffect.Action.MOVE, piece,
                    "Relayout for preview margin " + workspace.previewMargin() + " -> " + payload.amount())));
            case NORMALIZE_INSERT_SLOTS -> new MKWorkspaceContentSelectionMutationService()
                    .piecesNeedingInsertSlotNormalization(workspace)
                    .forEach(piece -> effects.add(pieceEffect(MKWorkspaceChangeEffect.Action.UPDATE, piece,
                            "Replace legacy synthetic insert role with user-owned slot " +
                                    MKWorkspaceContentSelectionTags.topologySlotId(piece) +
                                    "; preserve blocks, UUID, catalog position, and content family")));
            case RESTORE -> {
                MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate =
                        new MKWorkspaceBackupManifestDiscovery().discoverBackups(player.serverLevel(), workspace).stream()
                                .filter(value -> value.fileName().equals(payload.target())).findFirst().orElse(null);
                if (candidate == null) {
                    blockers.add("Backup file was not found for this workspace: " + payload.target());
                } else {
                    Optional<MKWorkspaceExportManifest> manifestOpt =
                            new MKWorkspaceBackupManifestDiscovery().loadBackup(candidate.path(), workspace);
                    stateGuard += ":" + candidate.path() + ":" + lastModified(candidate.path()) + ":" +
                            manifestOpt.map(Object::hashCode).orElse(0);
                    if (manifestOpt.isEmpty()) {
                        blockers.add("Backup archive is invalid or belongs to another workspace: " +
                                candidate.fileName());
                    } else {
                        MKWorkspaceExportManifest manifest = manifestOpt.get();
                        effects.add(MKWorkspaceChangeEffect.workspace(MKWorkspaceChangeEffect.Action.RESTORE,
                                workspace.id(), manifest.namespace() + ":" + manifest.structureName(),
                                "Replace current workspace metadata from " + candidate.fileName()));
                        Set<java.util.UUID> restoredPieceIds = manifest.pieces().stream()
                                .map(MKWorkspaceExportManifest.ExportPiece::pieceId)
                                .collect(java.util.stream.Collectors.toSet());
                        workspace.pieces().stream()
                                .filter(piece -> !restoredPieceIds.contains(piece.pieceId()))
                                .forEach(piece -> effects.add(pieceEffect(MKWorkspaceChangeEffect.Action.REMOVE,
                                        piece, "Clear current piece because it is absent from the backup")));
                        manifest.pieces().forEach(piece -> effects.add(manifestPieceEffect(
                                MKWorkspaceChangeEffect.Action.RESTORE, piece,
                                "Restore this piece from " + candidate.fileName())));
                    }
                }
            }
            case DELETE -> {
                effects.add(MKWorkspaceChangeEffect.workspace(MKWorkspaceChangeEffect.Action.REMOVE, workspace.id(),
                        workspace.namespace() + ":" + workspace.structureName(), "Delete workspace metadata and anchor"));
                workspace.pieces().forEach(piece -> effects.add(pieceEffect(MKWorkspaceChangeEffect.Action.REMOVE,
                        piece, "Clear authored blocks and remove the piece")));
            }
            case IMPORT -> throw new IllegalStateException("Import preparation does not use an existing workspace");
        }
        if (effects.isEmpty() && blockers.isEmpty()) {
            warnings.add("No matching authored pieces currently require this operation.");
        }
        List<MKWorkspaceGeneratedLayer> invalidated = invalidatedLayers();
        List<MKWorkspaceGeneratedLayer> lockedLayers = workspace.layerStates().stream()
                .filter(state -> state.locked() && invalidated.contains(state.layer()))
                .map(state -> state.layer()).toList();
        if (!lockedLayers.isEmpty()) {
            blockers.add("Unlock invalidated layer(s): " + lockedLayers.stream()
                    .map(MKWorkspaceGeneratedLayer::getSerializedName)
                    .reduce((left, right) -> left + ", " + right).orElse(""));
        }
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(kind.id, "Confirm " + kind.title,
                summaryText(workspace, payload), safety(), true, invalidatedLayers(), warnings, blockers,
                List.of(), effects);
        String finalStateGuard = stateGuard;
        return new MKWorkspacePreparedChange(request, anchor, workspace.id(),
                MKWorkspaceChangeCoordinator.fingerprint(workspace), finalStateGuard, summary,
                applyPlayer -> applyExisting(applyPlayer, workspace, anchor, payload));
    }

    private MKWorkspacePreparedChange prepareImport(ServerPlayer player, BlockPos anchor,
                                                     MKWorkspaceChangeRequest request,
                                                     MKWorkspaceSimpleChangePayload payload) {
        if (IMKStructureWorkspaceData.get(player.serverLevel()).getWorkspaceByAnchor(anchor).isPresent()) {
            throw new IllegalArgumentException("Import requires an empty workspace anchor.");
        }
        ResourceLocation manifestId = ResourceLocation.tryParse(payload.target());
        if (manifestId == null) {
            throw new IllegalArgumentException("Invalid workspace manifest id: " + payload.target());
        }
        Optional<MKWorkspaceExportManifest> manifestOpt =
                new MKStructureWorkspaceImportService().loadManifest(manifestId);
        ArrayList<String> blockers = new ArrayList<>();
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        if (manifestOpt.isEmpty()) {
            blockers.add("Workspace manifest was not found: " + manifestId);
        } else {
            MKWorkspaceExportManifest manifest = manifestOpt.get();
            effects.add(MKWorkspaceChangeEffect.workspace(MKWorkspaceChangeEffect.Action.CREATE,
                    manifest.workspaceId(), manifest.namespace() + ":" + manifest.structureName(),
                    "Import workspace at " + anchor.toShortString()));
            manifest.pieces().forEach(piece -> effects.add(manifestPieceEffect(
                    MKWorkspaceChangeEffect.Action.CREATE, piece, "Import authored piece")));
        }
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(kind.id, "Confirm " + kind.title,
                "Import " + manifestId + " at " + anchor.toShortString() + ".",
                MKWorkspaceMutationSafety.SAFE_RELAYOUT, false, List.of(), List.of(), blockers,
                List.of(), effects);
        String stateGuard = manifestId + ":" + manifestOpt.map(Object::hashCode).orElse(0);
        return new MKWorkspacePreparedChange(request, anchor, null, 0L, stateGuard, summary, applyPlayer -> {
            MKStructureWorkspaceService service = new MKStructureWorkspaceService();
            MKStructureWorkspaceService.MKWorkspaceImportResponse response =
                    service.importWorkspaceFromManifestWithValidation(applyPlayer.serverLevel(), anchor, manifestId);
            if (!response.validationErrors().isEmpty()) {
                return MKWorkspaceChangeApplyResult.failure(anchor,
                        "Workspace import validation failed: " + String.join("; ", response.validationErrors()));
            }
            return response.workspaceOpt().isPresent() ? MKWorkspaceChangeApplyResult.success(anchor,
                    "Workspace imported.") : MKWorkspaceChangeApplyResult.failure(anchor, "Workspace import failed.");
        });
    }

    private MKWorkspaceChangeApplyResult applyExisting(ServerPlayer player, MKStructureWorkspace preparedWorkspace,
                                                        BlockPos anchor, MKWorkspaceSimpleChangePayload payload)
            throws Exception {
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        return switch (kind) {
            case GENERATE -> service.generateWorkspace(player.serverLevel(), anchor).isPresent() ?
                    MKWorkspaceChangeApplyResult.success(anchor, "Workspace generated.") :
                    MKWorkspaceChangeApplyResult.failure(anchor, "Workspace generation failed.");
            case GENERATE_ALL_STAIRS -> service.generateAllWorkspaceStairs(player.serverLevel(), anchor).isPresent() ?
                    MKWorkspaceChangeApplyResult.success(anchor, "Workspace stairs generated.") :
                    MKWorkspaceChangeApplyResult.failure(anchor, "Workspace stair generation failed.");
            case GENERATE_STAIRS -> service.generateWorkspaceStairs(player.serverLevel(), anchor, payload.target(),
                    new MKWorkspaceStairAuthoringConfig(MKWorkspaceStairMode.fromSerializedName(payload.secondary()),
                            MKWorkspaceStairRiseType.fromSerializedName(payload.tertiary()), payload.amount())).isPresent() ?
                    MKWorkspaceChangeApplyResult.success(anchor, "Workspace stairs generated.") :
                    MKWorkspaceChangeApplyResult.failure(anchor, "Workspace stair generation failed.");
            case CLEAR_STAIRS -> service.clearWorkspaceStairs(player.serverLevel(), anchor, payload.target()).isPresent() ?
                    MKWorkspaceChangeApplyResult.success(anchor, "Workspace stairs cleared.") :
                    MKWorkspaceChangeApplyResult.failure(anchor, "Clearing workspace stairs failed.");
            case SWAP_BLOCKS -> {
                ResourceLocation source = ResourceLocation.parse(payload.target());
                ResourceLocation target = ResourceLocation.parse(payload.secondary());
                var result = new MKStructureWorkspaceMutationService().swapBlocks(player.serverLevel(),
                        preparedWorkspace, Map.of(source, target));
                yield MKWorkspaceChangeApplyResult.success(anchor, "Swapped " + result.replacedCount() +
                        " blocks across " + result.pieceCount() + " pieces.");
            }
            case PREVIEW_MARGIN -> {
                var result = new MKWorkspacePieceRelayoutService().relayoutPreviewMargin(player.serverLevel(),
                        preparedWorkspace, payload.amount());
                yield result.isPresent() ? MKWorkspaceChangeApplyResult.success(anchor,
                        "Workspace preview margin changed to " + payload.amount() + ".") :
                        MKWorkspaceChangeApplyResult.failure(anchor, "Preview margin relayout was not applicable.");
            }
            case NORMALIZE_INSERT_SLOTS -> {
                MKStructureWorkspace updated = new MKWorkspaceContentSelectionMutationService()
                        .normalizeInsertSlotIdentities(preparedWorkspace);
                IMKStructureWorkspaceData.get(player.serverLevel()).updateWorkspace(updated);
                yield MKWorkspaceChangeApplyResult.success(anchor, "Insert slot identities normalized.");
            }
            case RESTORE -> {
                var result = new MKWorkspaceBackupRestoreService().restoreByFileName(player.serverLevel(),
                        preparedWorkspace, payload.target());
                if (!result.validationErrors().isEmpty() || result.workspaceOpt().isEmpty()) {
                    yield MKWorkspaceChangeApplyResult.failure(anchor, "Workspace restore failed: " +
                            String.join("; ", result.validationErrors()));
                }
                yield MKWorkspaceChangeApplyResult.success(anchor, "Workspace backup restored: " + payload.target());
            }
            case DELETE -> service.deleteWorkspace(player.serverLevel(), anchor) ?
                    MKWorkspaceChangeApplyResult.success(anchor, "Workspace deleted.") :
                    MKWorkspaceChangeApplyResult.failure(anchor, "Workspace deletion failed.");
            case IMPORT -> throw new IllegalStateException("Import does not apply to an existing workspace");
        };
    }

    private String summaryText(MKStructureWorkspace workspace, MKWorkspaceSimpleChangePayload payload) {
        return switch (kind) {
            case GENERATE -> "Generate all authored blocks for " + workspace.namespace() + ":" + workspace.structureName() + ".";
            case GENERATE_ALL_STAIRS -> "Regenerate stairs for every vertical-access piece.";
            case GENERATE_STAIRS -> "Regenerate stairs for " + payload.target() + ".";
            case CLEAR_STAIRS -> "Clear generated stairs for " + payload.target() + ".";
            case SWAP_BLOCKS -> "Replace " + payload.target() + " with " + payload.secondary() + " in every authored piece.";
            case PREVIEW_MARGIN -> "Relayout every authored piece for preview margin " + payload.amount() + ".";
            case NORMALIZE_INSERT_SLOTS -> "Normalize legacy insert metadata so every scaffold and content family " +
                    "belongs to its user-declared insert slot.";
            case RESTORE -> "Restore workspace metadata and authored blocks from " + payload.target() + ".";
            case DELETE -> "Delete the workspace and clear all of its authored blocks.";
            case IMPORT -> throw new IllegalStateException();
        };
    }

    private MKWorkspaceMutationSafety safety() {
        return switch (kind) {
            case SWAP_BLOCKS -> MKWorkspaceMutationSafety.SAFE_BLOCK_SUBSTITUTION;
            case GENERATE_STAIRS, GENERATE_ALL_STAIRS, CLEAR_STAIRS, PREVIEW_MARGIN, NORMALIZE_INSERT_SLOTS ->
                    MKWorkspaceMutationSafety.SAFE_RELAYOUT;
            case DELETE, RESTORE, GENERATE -> MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE;
            case IMPORT -> MKWorkspaceMutationSafety.SAFE_RELAYOUT;
        };
    }

    private List<MKWorkspaceGeneratedLayer> invalidatedLayers() {
        return switch (kind) {
            case GENERATE_STAIRS, GENERATE_ALL_STAIRS, CLEAR_STAIRS ->
                    List.of(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS);
            case SWAP_BLOCKS -> List.of();
            case PREVIEW_MARGIN -> List.of(MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT,
                    MKWorkspaceGeneratedLayer.SCAFFOLD_BLOCKS);
            case NORMALIZE_INSERT_SLOTS -> List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                    MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT, MKWorkspaceGeneratedLayer.RUNTIME_METADATA);
            case GENERATE, RESTORE, DELETE -> List.of(MKWorkspaceGeneratedLayer.values());
            case IMPORT -> List.of();
        };
    }

    private MKWorkspacePieceDefinition findPiece(MKStructureWorkspace workspace, String pieceName,
                                                  List<String> blockers) {
        MKWorkspacePieceDefinition piece = workspace.pieces().stream()
                .filter(candidate -> candidate.pieceName().equals(pieceName)).findFirst().orElse(null);
        if (piece == null) {
            blockers.add("Workspace piece was not found: " + pieceName);
        }
        return piece;
    }

    private ResourceLocation parseBlock(String value, String label, List<String> blockers) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null || BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) {
            blockers.add("Unknown " + label + " block: " + value);
            return null;
        }
        return id;
    }

    private static MKWorkspaceChangeEffect pieceEffect(MKWorkspaceChangeEffect.Action action,
                                                        MKWorkspacePieceDefinition piece, String detail) {
        boolean derived = MKWorkspaceTemplateReuseTags.isDerived(piece.tags());
        String baseName = piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
        return new MKWorkspaceChangeEffect(action,
                piece.variantIndex() > 0 ? MKWorkspaceChangeEffect.Subject.VARIANT :
                        MKWorkspaceChangeEffect.Subject.TEMPLATE,
                piece.pieceId().toString(), piece.pieceName(), baseName, piece.variantIndex(), true, derived, detail);
    }

    private static MKWorkspaceChangeEffect stairEffect(MKWorkspaceChangeEffect.Action action,
                                                        MKWorkspacePieceDefinition piece, String detail) {
        return new MKWorkspaceChangeEffect(action, MKWorkspaceChangeEffect.Subject.STAIR,
                piece.pieceId().toString(), piece.pieceName(),
                piece.tags().getOrDefault("workspace_base_name", piece.pieceName()), piece.variantIndex(),
                true, true, detail);
    }

    private static MKWorkspaceChangeEffect manifestPieceEffect(MKWorkspaceChangeEffect.Action action,
                                                               MKWorkspaceExportManifest.ExportPiece piece,
                                                               String detail) {
        return new MKWorkspaceChangeEffect(action,
                piece.variantIndex() > 0 ? MKWorkspaceChangeEffect.Subject.VARIANT :
                        MKWorkspaceChangeEffect.Subject.TEMPLATE,
                piece.pieceId().toString(), piece.pieceName(), piece.baseName(), piece.variantIndex(),
                true, false, detail);
    }

    private long lastModified(java.nio.file.Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private record SwapScan(List<MKWorkspaceChangeEffect> effects, int replacementCount, long worldFingerprint) {
    }

    private SwapScan scanSwap(ServerPlayer player, MKStructureWorkspace workspace, ResourceLocation source,
                              ResourceLocation target) {
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        Set<BlockPos> fingerprinted = new HashSet<>();
        long fingerprint = 1L;
        int total = 0;
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            int pieceCount = 0;
            BoundingBox bounds = piece.exportBounds();
            for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                    for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        var state = player.serverLevel().getBlockState(pos);
                        if (fingerprinted.add(pos)) {
                            fingerprint = 31L * fingerprint + pos.hashCode();
                            fingerprint = 31L * fingerprint + state.hashCode();
                        }
                        if (!state.is(Blocks.JIGSAW) && !state.is(Blocks.STRUCTURE_BLOCK) &&
                                BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(source)) {
                            pieceCount++;
                        }
                    }
                }
            }
            if (pieceCount > 0) {
                total += pieceCount;
                effects.add(pieceEffect(MKWorkspaceChangeEffect.Action.UPDATE, piece,
                        "Replace " + pieceCount + " block(s) of " + source + " with " + target));
            }
        }
        return new SwapScan(List.copyOf(effects), total, fingerprint);
    }
}
