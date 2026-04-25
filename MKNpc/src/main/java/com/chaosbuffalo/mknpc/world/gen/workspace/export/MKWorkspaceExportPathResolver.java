package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class MKWorkspaceExportPathResolver {
    public Path getManifestPath(MinecraftServer server, MKStructureWorkspace workspace) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("generated")
                .resolve(workspace.namespace())
                .resolve("mk_workspace_exports")
                .resolve(workspace.structureName() + ".json");
    }
}
