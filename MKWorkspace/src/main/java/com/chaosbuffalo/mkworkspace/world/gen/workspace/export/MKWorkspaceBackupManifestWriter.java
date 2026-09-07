package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

public class MKWorkspaceBackupManifestWriter {
    private record TransactionContext(WrittenBackup backup) {
    }

    private static final ThreadLocal<TransactionContext> ACTIVE_TRANSACTION = new ThreadLocal<>();

    public record WrittenBackup(Path path, MKWorkspaceExportManifest manifest, String operation) {
    }

    public static final class TransactionScope implements AutoCloseable {
        private final TransactionContext previous;

        private TransactionScope(WrittenBackup backup) {
            previous = ACTIVE_TRANSACTION.get();
            ACTIVE_TRANSACTION.set(new TransactionContext(backup));
        }

        @Override
        public void close() {
            if (previous == null) {
                ACTIVE_TRANSACTION.remove();
            } else {
                ACTIVE_TRANSACTION.set(previous);
            }
        }
    }

    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();
    private final MKWorkspaceBackupArchiveStore archiveStore = new MKWorkspaceBackupArchiveStore();

    public WrittenBackup writeBeforeMutation(ServerLevel level, MKStructureWorkspace workspace,
                                             String operation) throws IOException {
        TransactionContext transaction = ACTIVE_TRANSACTION.get();
        if (transaction != null) {
            if (transaction.backup() == null) {
                throw new IOException("Prepared workspace transaction did not authorize a backup for " + operation);
            }
            return transaction.backup();
        }
        Instant timestamp = Instant.now();
        Path path = pathResolver.getBackupManifestPath(level, workspace, operation, timestamp);
        MKWorkspaceExportManifest manifest = createManifest(workspace, timestamp);
        archiveStore.writeArchive(path, level, workspace, manifest,
                new MKWorkspaceBackupArchiveStore.BackupMetadata(operation,
                        level.dimension().location().toString(), workspace.anchor(), workspace.id().toString(),
                        workspace.namespace() + ":" + workspace.structureName(), timestamp.toString()));
        return new WrittenBackup(path, manifest, operation);
    }

    public static TransactionScope enterTransaction(WrittenBackup backup) {
        return new TransactionScope(backup);
    }

    public static void requireTransaction(String operation) {
        if (ACTIVE_TRANSACTION.get() == null) {
            throw new IllegalStateException("Persistent workspace operation '" + operation +
                    "' must be applied through MKWorkspaceChangeCoordinator");
        }
    }

    private MKWorkspaceExportManifest createManifest(MKStructureWorkspace workspace, Instant timestamp) {
        return MKWorkspaceExportManifest.snapshotFromWorkspace(
                workspace,
                MKWorkspaceExportArchiveWriter.SCHEMA_VERSION,
                timestamp.toString()
        );
    }
}
