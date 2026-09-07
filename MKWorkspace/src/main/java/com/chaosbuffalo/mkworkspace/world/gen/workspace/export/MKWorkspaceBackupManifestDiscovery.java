package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MKWorkspaceBackupManifestDiscovery {
    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();
    private final MKWorkspaceBackupArchiveStore archiveStore = new MKWorkspaceBackupArchiveStore();

    public record BackupCandidate(String fileName, String operation, Instant lastModified, int schemaVersion,
                                  String namespace, String structureName, int pieceCount, Path path) {
    }

    public List<BackupCandidate> discoverBackups(ServerLevel level, MKStructureWorkspace workspace) {
        LinkedHashSet<Path> paths = new LinkedHashSet<>();
        collectArchives(pathResolver.getBackupManifestDirectory(level, workspace), paths, 1);
        collectArchives(pathResolver.getBackupManifestDirectory(level.getServer(), workspace), paths, 1);
        collectLegacyArchives(level.getServer(), paths);
        return candidates(paths, manifest -> manifest.workspaceId().equals(workspace.id()));
    }

    /** Finds backups for a deleted workspace from an empty dev block at the original anchor. */
    public List<BackupCandidate> discoverBackups(ServerLevel level, BlockPos anchor) {
        LinkedHashSet<Path> paths = new LinkedHashSet<>();
        collectArchives(pathResolver.getBackupAnchorDirectory(level, anchor), paths, 3);
        collectLegacyArchives(level.getServer(), paths);
        return candidates(paths, manifest -> manifestAnchor(manifest).equals(anchor));
    }

    /** Legacy compatibility for callers without a level. New code should use the ServerLevel overload. */
    public List<BackupCandidate> discoverBackups(MinecraftServer server, MKStructureWorkspace workspace) {
        LinkedHashSet<Path> paths = new LinkedHashSet<>();
        collectArchives(pathResolver.getBackupManifestDirectory(server, workspace), paths, 1);
        collectLegacyArchives(server, paths);
        return candidates(paths, manifest -> manifest.workspaceId().equals(workspace.id()));
    }

    public Optional<MKWorkspaceExportManifest> loadBackup(Path path, MKStructureWorkspace workspace) {
        return readManifest(path).filter(manifest -> manifest.workspaceId().equals(workspace.id()));
    }

    public Optional<MKWorkspaceExportManifest> loadBackup(Path path, BlockPos anchor) {
        return readManifest(path).filter(manifest -> manifestAnchor(manifest).equals(anchor));
    }

    private List<BackupCandidate> candidates(LinkedHashSet<Path> paths,
                                             Predicate<MKWorkspaceExportManifest> predicate) {
        return paths.stream().map(path -> loadCandidate(path, predicate)).flatMap(Optional::stream)
                .sorted((left, right) -> right.lastModified().compareTo(left.lastModified())).toList();
    }

    private Optional<BackupCandidate> loadCandidate(Path path, Predicate<MKWorkspaceExportManifest> predicate) {
        return readManifest(path).filter(predicate).map(manifest -> new BackupCandidate(
                path.getFileName().toString(), operationFromFileName(path.getFileName().toString()),
                lastModified(path), manifest.schemaVersion(), manifest.namespace(), manifest.structureName(),
                manifest.pieces().size(), path));
    }

    private Optional<MKWorkspaceExportManifest> readManifest(Path path) {
        Optional<MKWorkspaceExportManifest> manifest = archiveStore.readManifest(path);
        if (manifest.isEmpty()) {
            MKWorkspace.LOGGER.warn("Failed to load workspace backup manifest {}", path);
        }
        return manifest;
    }

    private void collectArchives(Path directory, LinkedHashSet<Path> paths, int depth) {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(directory, depth)) {
            stream.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".zip"))
                    .map(path -> path.toAbsolutePath().normalize()).forEach(paths::add);
        } catch (Exception exception) {
            MKWorkspace.LOGGER.warn("Failed to scan workspace backups under {}", directory, exception);
        }
    }

    private void collectLegacyArchives(MinecraftServer server, LinkedHashSet<Path> paths) {
        Path root = pathResolver.getLegacyBackupRoot(server);
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> namespaces = Files.list(root)) {
            namespaces.filter(Files::isDirectory)
                    .map(path -> path.resolve("mk_workspace_exports").resolve("backups"))
                    .filter(Files::isDirectory)
                    .forEach(path -> collectArchives(path, paths, 3));
        } catch (Exception exception) {
            MKWorkspace.LOGGER.warn("Failed to scan legacy workspace backups under {}", root, exception);
        }
    }

    private BlockPos manifestAnchor(MKWorkspaceExportManifest manifest) {
        MKWorkspaceExportManifest.ExportBlockPos anchor = manifest.settings().anchor();
        return new BlockPos(anchor.x(), anchor.y(), anchor.z());
    }

    private Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (Exception exception) {
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
