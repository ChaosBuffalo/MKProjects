package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.block_entities.MKWorkspaceDevBlockEntity;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceImportService;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
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
        List<MKWorkspaceBackupManifestDiscovery.BackupCandidate> candidates =
                discovery.discoverBackups(level.getServer(), current);
        if (candidates.isEmpty()) {
            return RestoreResult.failed();
        }
        return restoreFromCandidate(level, current, candidates.getFirst());
    }

    public RestoreResult restoreByFileName(ServerLevel level, MKStructureWorkspace current, String fileName)
            throws IOException {
        for (MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate :
                discovery.discoverBackups(level.getServer(), current)) {
            if (candidate.fileName().equals(fileName)) {
                return restoreFromCandidate(level, current, candidate);
            }
        }
        return RestoreResult.failed();
    }

    private RestoreResult restoreFromCandidate(ServerLevel level, MKStructureWorkspace current,
                                               MKWorkspaceBackupManifestDiscovery.BackupCandidate candidate)
            throws IOException {
        Optional<MKWorkspaceExportManifest> manifestOpt = discovery.loadBackup(candidate.path(), current);
        if (manifestOpt.isEmpty()) {
            return RestoreResult.failed();
        }
        MKWorkspaceBackupManifestWriter.WrittenBackup beforeRestore =
                backupWriter.writeBeforeMutation(level, current, "backup-restore");
        MKWorkspaceExportManifest manifest = manifestOpt.get();
        MKStructureWorkspace restoredBase = importService.workspaceFromManifest(
                current.id(), current.anchor(), current.createdAt(), manifest);
        MKStructureWorkspace restored = restoredBase.withPieces(
                importService.pieceDefinitionsFromManifest(restoredBase, manifest));
        List<String> validationErrors = plannerRegistry.validate(restored);
        if (!validationErrors.isEmpty()) {
            return RestoreResult.validationFailed(candidate.path(), beforeRestore.path(), validationErrors);
        }

        Optional<MKWorkspaceBackupArchiveStore.RestoreStats> blockRestoreStats =
                archiveStore.restorePieces(candidate.path(), level, current, restored);
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        data.updateWorkspace(restored);
        syncBlockEntity(level, restored);
        return RestoreResult.success(restored, candidate.path(), beforeRestore.path(), blockRestoreStats);
    }

    private void syncBlockEntity(ServerLevel level, MKStructureWorkspace workspace) {
        BlockEntity blockEntity = level.getBlockEntity(workspace.anchor());
        if (blockEntity instanceof MKWorkspaceDevBlockEntity workspaceDevBlockEntity) {
            workspaceDevBlockEntity.setWorkspaceId(workspace.id());
        }
    }
}
