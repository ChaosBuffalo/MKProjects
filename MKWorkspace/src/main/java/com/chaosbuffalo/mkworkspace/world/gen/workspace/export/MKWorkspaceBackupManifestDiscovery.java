package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MKWorkspaceBackupManifestDiscovery {
    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();
    private final MKWorkspaceBackupArchiveStore archiveStore = new MKWorkspaceBackupArchiveStore();

    public record BackupCandidate(String fileName, String operation, Instant lastModified, int schemaVersion,
                                  String namespace, String structureName, int pieceCount, Path path) {
    }

    public List<BackupCandidate> discoverBackups(MinecraftServer server, MKStructureWorkspace workspace) {
        Path backupDir = pathResolver.getBackupManifestDirectory(server, workspace);
        if (!Files.isDirectory(backupDir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(backupDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".zip"))
                    .map(path -> loadCandidate(path, workspace))
                    .flatMap(Optional::stream)
                    .sorted((left, right) -> right.lastModified().compareTo(left.lastModified()))
                    .toList();
        } catch (Exception e) {
            MKNpc.LOGGER.warn("Failed to scan workspace backup manifests under {}", backupDir, e);
            return List.of();
        }
    }

    public Optional<MKWorkspaceExportManifest> loadBackup(Path path, MKStructureWorkspace workspace) {
        return readManifest(path).filter(manifest -> matchesWorkspace(manifest, workspace, path));
    }

    private Optional<BackupCandidate> loadCandidate(Path path, MKStructureWorkspace workspace) {
        return readManifest(path)
                .filter(manifest -> matchesWorkspace(manifest, workspace, path))
                .map(manifest -> new BackupCandidate(
                        path.getFileName().toString(),
                        operationFromFileName(path.getFileName().toString()),
                        lastModified(path),
                        manifest.schemaVersion(),
                        manifest.namespace(),
                        manifest.structureName(),
                        manifest.pieces().size(),
                        path
                ));
    }

    private Optional<MKWorkspaceExportManifest> readManifest(Path path) {
        Optional<MKWorkspaceExportManifest> manifest = archiveStore.readManifest(path);
        if (manifest.isEmpty()) {
            MKNpc.LOGGER.warn("Failed to load workspace backup manifest {}", path);
        }
        return manifest;
    }

    private boolean matchesWorkspace(MKWorkspaceExportManifest manifest, MKStructureWorkspace workspace, Path path) {
        boolean matches = manifest.namespace().equals(workspace.namespace()) &&
                manifest.structureName().equals(workspace.structureName());
        if (!matches) {
            MKNpc.LOGGER.warn("Skipping workspace backup manifest {} because it describes {}:{} instead of {}:{}",
                    path, manifest.namespace(), manifest.structureName(), workspace.namespace(), workspace.structureName());
        }
        return matches;
    }

    private Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (Exception e) {
            return Instant.EPOCH;
        }
    }

    private String operationFromFileName(String fileName) {
        int beforeIndex = fileName.indexOf("-before-");
        int extensionIndex = fileName.endsWith(".zip") ? fileName.length() - ".zip".length() : fileName.length();
        if (beforeIndex < 0 || beforeIndex + "-before-".length() >= extensionIndex) {
            return "unknown";
        }
        return fileName.substring(beforeIndex + "-before-".length(), extensionIndex);
    }
}
