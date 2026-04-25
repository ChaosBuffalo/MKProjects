package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class MKWorkspaceExportMetadataPathResolver {
    public Path getMetadataDirectory(MinecraftServer server, String namespace, String structureName) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("generated")
                .resolve(namespace)
                .resolve("mk_jigsaw_piece_meta")
                .resolve(structureName);
    }
}
