package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceImportService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKWorkspaceAnchor;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class MKWorkspaceBackupRestoreService {
    private final MKWorkspaceBackupManifestDiscovery discovery = new MKWorkspaceBackupManifestDiscovery();
    private final MKWorkspaceBackupManifestWriter backupWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspaceBackupArchiveStore archiveStore = new MKWorkspaceBackupArchiveStore();
    private final MKStructureWorkspaceImportService importService = new MKStructureWorkspaceImportService();
    private final MKWorkspacePlannerRegistry plannerRegistry = MKWorkspacePlannerRegistry.shared();

    public record RestoreResult(@Nullable MKStructureWorkspace workspace, @Nullable Path selectedBackupPath,
                                @Nullable Path beforeRestoreBackupPath,
                                @Nullable MKWorkspaceBackupArchiveStore.RestoreStats blockRestoreStats,
                                List<String> validationErrors) {
        public static RestoreResult failed() {
            return new RestoreResult(null, null, null, null, List.of());
        }

        public static RestoreResult validationFailed(Path selectedBackupPath, Path beforeRestoreBackupPath,
                                                     List<String> validationErrors) {
            return new RestoreResult(null, selectedBackupPath, beforeRestoreBackupPath, null,
                    List.copyOf(validationErrors));
        }

        public static RestoreResult success(MKStructureWorkspace workspace, Path selectedBackupPath,
                                            Path beforeRestoreBackupPath,
                                            Optional<MKWorkspaceBackupArchiveStore.RestoreStats> blockRestoreStats) {
            return new RestoreResult(workspace, selectedBackupPath, beforeRestoreBackupPath,
                    blockRestoreStats.orElse(null), List.of());
        }

        public Optional<MKStructureWorkspace> workspaceOpt() {
            return Optional.ofNullable(workspace);
        }

        public Optional<Path> selectedBackupPathOpt() {
            return Optional.ofNullable(selectedBackupPath);
        }

        public Optional<Path> beforeRestoreBackupPathOpt() {
            return Optional.ofNullable(beforeRestoreBackupPath);
        }

        public Optional<MKWorkspaceBackupArchiveStore.RestoreStats> blockRestoreStatsOpt() {
            return Optional.ofNullable(blockRestoreStats);
        }
    }

    public RestoreResult restoreLatest(ServerLevel level, MKStructureWorkspace current) throws IOException {
        MKWorkspaceBackupManifestWriter.requireTransaction("restore-workspace-backup");
        List<MKWorkspaceBackupManifestDiscovery.BackupCandidate> candidates =
                discovery.discoverBackups(level, current);
        if (candidates.isEmpty()) {
            return RestoreResult.failed();
        }
        return restoreFromCandidate(level, current, candidates.getFirst());
    }

    public RestoreResult restoreByFileName(ServerLevel level, MKStructureWorkspace current, String fileName)
            throws IOException {
        MKWorkspaceBackupManifestWriter.requireTransaction("restore-workspace-backup");
        for (MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate :
                discovery.discoverBackups(level, current)) {
            if (candidate.fileName().equals(fileName)) {
                return restoreFromCandidate(level, current, candidate);
            }
        }
        return RestoreResult.failed();
    }

    public RestoreResult restoreByFileName(ServerLevel level, net.minecraft.core.BlockPos anchor, String fileName)
            throws IOException {
        MKWorkspaceBackupManifestWriter.requireTransaction("restore-deleted-workspace-backup");
        for (MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate :
                discovery.discoverBackups(level, anchor)) {
            if (candidate.fileName().equals(fileName)) {
                return restoreFromCandidate(level, null, anchor, candidate);
            }
        }
        return RestoreResult.failed();
    }

    private RestoreResult restoreFromCandidate(ServerLevel level, MKStructureWorkspace current,
                                               MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate)
            throws IOException {
        return restoreFromCandidate(level, current, current.anchor(), candidate);
    }

    private RestoreResult restoreFromCandidate(ServerLevel level, @Nullable MKStructureWorkspace current,
                                               net.minecraft.core.BlockPos anchor,
                                               MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate)
            throws IOException {
        Optional<MKWorkspaceExportManifest> manifestOpt = current == null ?
                discovery.loadBackup(candidate.path(), anchor) : discovery.loadBackup(candidate.path(), current);
        if (manifestOpt.isEmpty()) {
            return RestoreResult.failed();
        }
        MKWorkspaceBackupManifestWriter.WrittenBackup beforeRestore = current == null ? null :
                backupWriter.writeBeforeMutation(level, current, "backup-restore");
        MKWorkspaceExportManifest manifest = manifestOpt.get();
        MKStructureWorkspace restoredBase = importService.workspaceFromManifest(
                current == null ? manifest.workspaceId() : current.id(), anchor,
                current == null ? manifest.createdAt() : current.createdAt(), manifest);
        MKStructureWorkspace restored = restoredBase.withPieces(
                importService.pieceDefinitionsFromManifest(restoredBase, manifest));
        List<String> validationErrors = plannerRegistry.validate(restored);
        if (!validationErrors.isEmpty()) {
            return RestoreResult.validationFailed(candidate.path(), beforeRestore == null ? null : beforeRestore.path(),
                    validationErrors);
        }

        Optional<MKWorkspaceBackupArchiveStore.RestoreStats> blockRestoreStats =
                archiveStore.restorePieces(candidate.path(), level, current, restored);
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        if (current == null) {
            data.createWorkspace(restored);
        } else {
            data.updateWorkspace(restored);
        }
        syncBlockEntity(level, restored);
        return RestoreResult.success(restored, candidate.path(), beforeRestore == null ? null : beforeRestore.path(),
                blockRestoreStats);
    }

    private void syncBlockEntity(ServerLevel level, MKStructureWorkspace workspace) {
        BlockEntity blockEntity = level.getBlockEntity(workspace.anchor());
        if (blockEntity instanceof MKWorkspaceAnchor workspaceAnchor) {
            workspaceAnchor.setWorkspaceId(workspace.id());
        }
    }
}
