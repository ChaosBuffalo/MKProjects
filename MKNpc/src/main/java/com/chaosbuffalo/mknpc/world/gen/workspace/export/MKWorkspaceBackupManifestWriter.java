package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class MKWorkspaceBackupManifestWriter {
    public record WrittenBackup(Path path, MKWorkspaceExportManifest manifest, String operation) {
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();

    public WrittenBackup writeBeforeMutation(MinecraftServer server, MKStructureWorkspace workspace,
                                             String operation) throws IOException {
        Instant timestamp = Instant.now();
        Path path = pathResolver.getBackupManifestPath(server, workspace, operation, timestamp);
        Files.createDirectories(path.getParent());
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(
                workspace,
                MKWorkspaceExportManifestWriter.SCHEMA_VERSION,
                timestamp.toString()
        );
        JsonElement json = MKWorkspaceExportManifest.CODEC.encodeStart(JsonOps.INSTANCE, manifest).getOrThrow();
        Files.writeString(path, gson.toJson(json));
        return new WrittenBackup(path, manifest, operation);
    }
}
