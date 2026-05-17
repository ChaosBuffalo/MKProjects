package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MKWorkspaceExportArchiveWriter {
    public static final int SCHEMA_VERSION = 4;

    public record WrittenArchive(Path path, MKWorkspaceExportManifest manifest, int structurePieceCount,
                                 int metadataCount) {
    }

    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();

    public WrittenArchive write(ServerLevel level, MKStructureWorkspace workspace) throws IOException {
        Path path = pathResolver.getArchivePath(level.getServer(), workspace);
        Files.createDirectories(path.getParent());
        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, SCHEMA_VERSION,
                Instant.now().toString());
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(path))) {
            writeJson(output, manifestEntryName(manifest), MKWorkspaceExportManifest.CODEC
                    .encodeStart(JsonOps.INSTANCE, manifest)
                    .getOrThrow());
            int structurePieceCount = writeStructurePieces(output, level, manifest, workspace);
            int metadataCount = writePieceMetadata(output, manifest);
            return new WrittenArchive(path, manifest, structurePieceCount, metadataCount);
        }
    }

    private int writeStructurePieces(ZipOutputStream output, ServerLevel level, MKWorkspaceExportManifest manifest,
                                     MKStructureWorkspace workspace) throws IOException {
        int written = 0;
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            output.putNextEntry(new ZipEntry(structureEntryName(manifest, piece)));
            output.write(pieceNbtBytes(level, piece));
            output.closeEntry();
            written++;
        }
        return written;
    }

    private int writePieceMetadata(ZipOutputStream output, MKWorkspaceExportManifest manifest) throws IOException {
        Map<String, MKWorkspaceExportManifest.ExportRuntimeCategory> categoryByBaseName = new LinkedHashMap<>();
        for (MKWorkspaceExportManifest.ExportRuntimeCategory category : manifest.runtimeHints().categories()) {
            categoryByBaseName.put(category.baseName(), category);
        }

        int written = 0;
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
                    category.pieceMetadata().branchCap(),
                    category.pieceMetadata().foundationPolicy()
            );
            writeJson(output, metadataEntryName(manifest, piece), MKJigsawPieceMetadata.CODEC
                    .encodeStart(JsonOps.INSTANCE, metadata)
                    .getOrThrow());
            written++;
        }
        return written;
    }

    private void writeJson(ZipOutputStream output, String entryName, JsonElement json) throws IOException {
        output.putNextEntry(new ZipEntry(entryName));
        Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.write(json.toString());
        writer.flush();
        output.closeEntry();
    }

    private byte[] pieceNbtBytes(ServerLevel level, MKWorkspacePieceDefinition piece) throws IOException {
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, boundsMin(piece.exportBounds()), boundsSize(piece.exportBounds()), false, null);
        CompoundTag tag = template.save(new CompoundTag());
        ByteArrayOutputStream pieceBytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, pieceBytes);
        return pieceBytes.toByteArray();
    }

    private String manifestEntryName(MKWorkspaceExportManifest manifest) {
        return safeEntryName("data/" + manifest.namespace() + "/mk_workspace_exports/" +
                manifest.structureName() + ".json");
    }

    private String structureEntryName(MKWorkspaceExportManifest manifest, MKWorkspacePieceDefinition piece) {
        return safeEntryName("data/" + manifest.namespace() + "/structure/" + manifest.structureName() +
                "/" + piece.pieceName() + ".nbt");
    }

    private String metadataEntryName(MKWorkspaceExportManifest manifest, MKWorkspaceExportManifest.ExportPiece piece) {
        return safeEntryName("data/" + manifest.namespace() + "/mk_jigsaw_piece_meta/" +
                manifest.structureName() + "/" + piece.pieceName() + ".json");
    }

    private String safeEntryName(String entryName) {
        String normalized = entryName.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("../") || normalized.contains("/..")) {
            throw new IllegalArgumentException("unsafe workspace export archive entry: " + entryName);
        }
        return normalized;
    }

    private BlockPos boundsMin(BoundingBox bounds) {
        return new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());
    }

    private Vec3i boundsSize(BoundingBox bounds) {
        return new Vec3i(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan());
    }
}
