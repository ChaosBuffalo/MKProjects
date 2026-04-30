package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MKWorkspaceExportPathResolver {
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmssSSS'Z'")
            .withLocale(Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    public Path getManifestPath(MinecraftServer server, MKStructureWorkspace workspace) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("generated")
                .resolve(workspace.namespace())
                .resolve("mk_workspace_exports")
                .resolve(workspace.structureName() + ".json");
    }

    public Path getBackupManifestPath(MinecraftServer server, MKStructureWorkspace workspace, String operation,
                                      Instant timestamp) {
        String safeOperation = sanitizePathSegment(operation);
        String fileName = BACKUP_TIMESTAMP_FORMAT.format(timestamp) + "-before-" + safeOperation + ".zip";
        return getBackupManifestDirectory(server, workspace)
                .resolve(fileName);
    }

    public Path getBackupManifestDirectory(MinecraftServer server, MKStructureWorkspace workspace) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("generated")
                .resolve(workspace.namespace())
                .resolve("mk_workspace_exports")
                .resolve("backups")
                .resolve(workspace.structureName());
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
