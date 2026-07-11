package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MKWorkspaceBackupArchiveStore {
    private static final String MANIFEST_ENTRY = "manifest.json";
    private static final String PIECE_DIR = "pieces/";

    public record RestoreStats(int restoredPieceCount, int clearedBlockCount) {
    }

    public void writeArchive(Path archivePath, ServerLevel level, MKStructureWorkspace workspace,
                             MKWorkspaceExportManifest manifest) throws IOException {
        Files.createDirectories(archivePath.getParent());
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archivePath))) {
            writeManifest(output, manifest);
            for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
                writePiece(output, level, piece);
            }
        }
    }

    public Optional<MKWorkspaceExportManifest> readManifest(Path archivePath) {
        try (ZipFile zipFile = new ZipFile(archivePath.toFile())) {
            ZipEntry manifestEntry = zipFile.getEntry(MANIFEST_ENTRY);
            if (manifestEntry == null) {
                return Optional.empty();
            }
            try (Reader reader = new InputStreamReader(zipFile.getInputStream(manifestEntry), StandardCharsets.UTF_8)) {
                JsonElement json = com.google.gson.JsonParser.parseReader(reader);
                return Optional.of(MKWorkspaceExportManifest.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public Optional<RestoreStats> restorePieces(Path archivePath, ServerLevel level,
                                                MKStructureWorkspace currentWorkspace,
                                                MKStructureWorkspace restoredWorkspace) throws IOException {
        if (!Files.isRegularFile(archivePath)) {
            return Optional.empty();
        }

        int clearedCount = clearWorkspaceBlocks(level, currentWorkspace, restoredWorkspace);
        int restoredPieceCount = 0;
        try (ZipFile zipFile = new ZipFile(archivePath.toFile())) {
            for (MKWorkspacePieceDefinition piece : restoredWorkspace.pieces()) {
                ZipEntry entry = zipFile.getEntry(pieceEntryName(piece));
                if (entry == null) {
                    continue;
                }
                CompoundTag tag;
                try (InputStream input = zipFile.getInputStream(entry)) {
                    tag = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                }
                placePiece(level, piece, tag);
                restoredPieceCount++;
            }
        }
        return Optional.of(new RestoreStats(restoredPieceCount, clearedCount));
    }

    private void writeManifest(ZipOutputStream output, MKWorkspaceExportManifest manifest) throws IOException {
        output.putNextEntry(new ZipEntry(MANIFEST_ENTRY));
        JsonElement json = MKWorkspaceExportManifest.CODEC.encodeStart(JsonOps.INSTANCE, manifest).getOrThrow();
        Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.write(json.toString());
        writer.flush();
        output.closeEntry();
    }

    private void writePiece(ZipOutputStream output, ServerLevel level, MKWorkspacePieceDefinition piece) throws IOException {
        output.putNextEntry(new ZipEntry(pieceEntryName(piece)));
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, boundsMin(piece.exportBounds()), boundsSize(piece.exportBounds()), false, null);
        CompoundTag tag = template.save(new CompoundTag());
        ByteArrayOutputStream pieceBytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, pieceBytes);
        output.write(pieceBytes.toByteArray());
        output.closeEntry();
    }

    private void placePiece(ServerLevel level, MKWorkspacePieceDefinition piece, CompoundTag tag) {
        StructureTemplate template = new StructureTemplate();
        template.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
        template.placeInWorld(level, boundsMin(piece.exportBounds()), boundsMin(piece.exportBounds()),
                new StructurePlaceSettings(), RandomSource.create(), Block.UPDATE_ALL);
    }

    private String pieceEntryName(MKWorkspacePieceDefinition piece) {
        return PIECE_DIR + piece.pieceId() + "-" + sanitizePieceName(piece.pieceName()) + ".nbt";
    }

    private String sanitizePieceName(String pieceName) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pieceName.length(); i++) {
            char c = pieceName.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
                    c == '_' || c == '-' || c == '/') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    private BlockPos boundsMin(BoundingBox bounds) {
        return new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());
    }

    private Vec3i boundsSize(BoundingBox bounds) {
        return new Vec3i(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan());
    }

    private int clearWorkspaceBlocks(ServerLevel level, MKStructureWorkspace currentWorkspace,
                                     MKStructureWorkspace restoredWorkspace) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        positions.addAll(collectPieceBlockPositions(currentWorkspace));
        positions.addAll(collectPieceBlockPositions(restoredWorkspace));
        for (BlockPos pos : positions) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return positions.size();
    }

    private Set<BlockPos> collectPieceBlockPositions(MKStructureWorkspace workspace) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            BoundingBox bounds = piece.exportBounds();
            for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                    for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return positions;
    }
}
