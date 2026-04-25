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

public class MKWorkspaceExportManifestWriter {
    public static final int SCHEMA_VERSION = 1;

    public record WrittenManifest(Path path, MKWorkspaceExportManifest manifest) {
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();

    public WrittenManifest write(MinecraftServer server, MKStructureWorkspace workspace) throws IOException {
        Path path = pathResolver.getManifestPath(server, workspace);
        Files.createDirectories(path.getParent());
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, SCHEMA_VERSION, Instant.now().toString());
        JsonElement json = MKWorkspaceExportManifest.CODEC.encodeStart(JsonOps.INSTANCE, manifest).getOrThrow();
        Files.writeString(path, gson.toJson(json));
        return new WrittenManifest(path, manifest);
    }
}
