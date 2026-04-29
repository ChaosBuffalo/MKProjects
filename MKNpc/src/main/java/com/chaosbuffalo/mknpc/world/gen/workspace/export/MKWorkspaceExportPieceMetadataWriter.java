package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class MKWorkspaceExportPieceMetadataWriter {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final MKWorkspaceExportMetadataPathResolver pathResolver = new MKWorkspaceExportMetadataPathResolver();

    public Path writeAll(MinecraftServer server, MKWorkspaceExportManifest manifest) throws IOException {
        Path metadataDir = pathResolver.getMetadataDirectory(server, manifest.namespace(), manifest.structureName());
        Files.createDirectories(metadataDir);

        Map<String, MKWorkspaceExportManifest.ExportRuntimeCategory> categoryByBaseName = new LinkedHashMap<>();
        for (MKWorkspaceExportManifest.ExportRuntimeCategory category : manifest.runtimeHints().categories()) {
            categoryByBaseName.put(category.baseName(), category);
        }

        for (MKWorkspaceExportManifest.ExportPiece piece : manifest.pieces()) {
            if ("template".equals(piece.workspacePieceKind())) {
                continue;
            }
            MKWorkspaceExportManifest.ExportRuntimeCategory category = categoryByBaseName.get(piece.baseName());
            if (category == null) {
                continue;
            }
            MKJigsawPieceMetadata metadata = new MKJigsawPieceMetadata(
                    category.pieceMetadata().role(),
                    category.pieceMetadata().progressionDelta(),
                    category.pieceMetadata().verticalLevelDelta(),
                    category.pieceMetadata().allowOnMainPath(),
                    category.pieceMetadata().allowOnBranchPath(),
                    category.pieceMetadata().terminal(),
                    category.pieceMetadata().topCapOnly(),
                    category.pieceMetadata().category(),
                    category.pieceMetadata().mainPathEnding(),
                    category.pieceMetadata().branchCap()
            );
            JsonElement json = MKJigsawPieceMetadata.CODEC.encodeStart(JsonOps.INSTANCE, metadata).getOrThrow();
            Files.writeString(metadataDir.resolve(piece.pieceName() + ".json"), gson.toJson(json));
        }
        return metadataDir;
    }
}

