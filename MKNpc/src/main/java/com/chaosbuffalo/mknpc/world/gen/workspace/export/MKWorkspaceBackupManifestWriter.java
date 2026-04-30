package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

public class MKWorkspaceBackupManifestWriter {
    public record WrittenBackup(Path path, MKWorkspaceExportManifest manifest, String operation) {
    }

    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();
    private final MKWorkspaceBackupArchiveStore archiveStore = new MKWorkspaceBackupArchiveStore();

    public WrittenBackup writeBeforeMutation(ServerLevel level, MKStructureWorkspace workspace,
                                             String operation) throws IOException {
        Instant timestamp = Instant.now();
        Path path = pathResolver.getBackupManifestPath(level.getServer(), workspace, operation, timestamp);
        MKWorkspaceExportManifest manifest = createManifest(workspace, timestamp);
        archiveStore.writeArchive(path, level, workspace, manifest);
        return new WrittenBackup(path, manifest, operation);
    }

    private MKWorkspaceExportManifest createManifest(MKStructureWorkspace workspace, Instant timestamp) {
        return MKWorkspaceExportManifest.fromWorkspace(
                workspace,
                MKWorkspaceExportManifestWriter.SCHEMA_VERSION,
                timestamp.toString()
        );
    }
}
