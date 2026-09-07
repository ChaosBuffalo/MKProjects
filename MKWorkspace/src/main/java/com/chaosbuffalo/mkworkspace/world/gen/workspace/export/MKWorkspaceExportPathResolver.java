package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class MKWorkspaceExportPathResolver {
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmssSSS'Z'")
            .withLocale(Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    public Path getArchivePath(MinecraftServer server, MKStructureWorkspace workspace) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("generated")
                .resolve(workspace.namespace())
                .resolve("mk_workspace_exports")
                .resolve(workspace.structureName() + ".zip");
    }

    public Path getBackupManifestPath(ServerLevel level, MKStructureWorkspace workspace, String operation,
                                      Instant timestamp) {
        String safeOperation = sanitizePathSegment(operation);
        String fileName = BACKUP_TIMESTAMP_FORMAT.format(timestamp) + "-before-" + safeOperation + ".zip";
        return getBackupManifestDirectory(level, workspace)
                .resolve(fileName);
    }

    public Path getBackupManifestDirectory(ServerLevel level, MKStructureWorkspace workspace) {
        return getBackupManifestDirectory(level.getServer().getWorldPath(LevelResource.ROOT),
                level.dimension().location(), workspace.anchor(), workspace.id());
    }

    public Path getBackupAnchorDirectory(ServerLevel level, BlockPos anchor) {
        return getBackupAnchorDirectory(level.getServer().getWorldPath(LevelResource.ROOT),
                level.dimension().location(), anchor);
    }

    public Path getBackupManifestDirectory(Path worldRoot, ResourceLocation dimension, BlockPos anchor,
                                           UUID workspaceId) {
        return getBackupAnchorDirectory(worldRoot, dimension, anchor).resolve(workspaceId.toString());
    }

    public Path getBackupAnchorDirectory(Path worldRoot, ResourceLocation dimension, BlockPos anchor) {
        return worldRoot
                .resolve("generated")
                .resolve("mkworkspace")
                .resolve("backups")
                .resolve(sanitizePathSegment(dimension.getNamespace()))
                .resolve(sanitizePathSegment(dimension.getPath()))
                .resolve(anchor.getX() + "_" + anchor.getY() + "_" + anchor.getZ());
    }

    public Path getBackupManifestDirectory(MinecraftServer server, MKStructureWorkspace workspace) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("generated")
                .resolve(workspace.namespace())
                .resolve("mk_workspace_exports")
                .resolve("backups")
                .resolve(workspace.structureName());
    }

    public Path getLegacyBackupRoot(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("generated");
    }

    private String sanitizePathSegment(String value) {
        if (value == null || value.isBlank()) {
            return "workspace-mutation";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toLowerCase(value.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                builder.append(c);
            } else if (c == '-' || c == '_') {
                builder.append(c);
            } else if (Character.isWhitespace(c)) {
                builder.append('-');
            }
        }
        if (builder.isEmpty()) {
            return "workspace-mutation";
        }
        return builder.toString();
    }
}
