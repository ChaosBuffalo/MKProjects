package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MKWorkspaceBackupArchiveStore {
    private static final String MANIFEST_ENTRY = "manifest.json";
    private static final String PIECE_DIR = "pieces/";
    private static final String BACKUP_ORIGIN_OFFSET_TAG = "mkworkspace_backup_origin_offset";

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
                placePiece(level, restoredWorkspace, piece, tag);
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
        BoundingBox backupBounds = pieceBackupBounds(piece);
        template.fillFromWorld(level, boundsMin(backupBounds), boundsSize(backupBounds), false, null);
        CompoundTag tag = template.save(new CompoundTag());
        tag.put(BACKUP_ORIGIN_OFFSET_TAG, blockPosList(boundsMin(backupBounds).subtract(boundsMin(piece.exportBounds()))));
        ByteArrayOutputStream pieceBytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, pieceBytes);
        output.write(pieceBytes.toByteArray());
        output.closeEntry();
    }

    private void placePiece(ServerLevel level, MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece, CompoundTag tag) {
        CompoundTag templateTag = tag.copy();
        BlockPos exportOrigin = boundsMin(piece.exportBounds());
        BlockPos origin = readBackupOriginOffset(templateTag)
                .map(exportOrigin::offset)
                .orElse(exportOrigin);
        templateTag.remove(BACKUP_ORIGIN_OFFSET_TAG);
        StructureTemplate template = new StructureTemplate();
        template.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), templateTag);
        template.placeInWorld(level, origin, origin,
                new StructurePlaceSettings(), RandomSource.create(), Block.UPDATE_ALL);
        restoreTemplateControls(level, workspace, piece);
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

    private ListTag blockPosList(BlockPos pos) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(pos.getX()));
        list.add(IntTag.valueOf(pos.getY()));
        list.add(IntTag.valueOf(pos.getZ()));
        return list;
    }

    private Optional<BlockPos> readBackupOriginOffset(CompoundTag tag) {
        if (!tag.contains(BACKUP_ORIGIN_OFFSET_TAG, Tag.TAG_LIST)) {
            return Optional.empty();
        }
        ListTag list = tag.getList(BACKUP_ORIGIN_OFFSET_TAG, Tag.TAG_INT);
        if (list.size() < 3) {
            return Optional.empty();
        }
        return Optional.of(new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2)));
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
            BoundingBox bounds = pieceBackupBounds(piece);
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

    static BoundingBox pieceBackupBounds(MKWorkspacePieceDefinition piece) {
        BoundingBox bounds = merge(piece.previewBounds(), piece.exportBounds());
        if (!MKWorkspaceTemplateReuseTags.isDerived(piece.tags())) {
            bounds = include(bounds, piece.structureBlockPos());
            bounds = include(bounds, piece.signPos());
            for (BlockPos markerPos : piece.markerPositions()) {
                bounds = include(bounds, markerPos);
            }
        }
        for (BlockPos stairPos : piece.generatedStairPositions()) {
            bounds = include(bounds, stairPos);
        }
        return bounds;
    }

    private static BoundingBox merge(BoundingBox first, BoundingBox second) {
        return new BoundingBox(
                Math.min(first.minX(), second.minX()),
                Math.min(first.minY(), second.minY()),
                Math.min(first.minZ(), second.minZ()),
                Math.max(first.maxX(), second.maxX()),
                Math.max(first.maxY(), second.maxY()),
                Math.max(first.maxZ(), second.maxZ())
        );
    }

    private static BoundingBox include(BoundingBox bounds, BlockPos pos) {
        return new BoundingBox(
                Math.min(bounds.minX(), pos.getX()),
                Math.min(bounds.minY(), pos.getY()),
                Math.min(bounds.minZ(), pos.getZ()),
                Math.max(bounds.maxX(), pos.getX()),
                Math.max(bounds.maxY(), pos.getY()),
                Math.max(bounds.maxZ(), pos.getZ())
        );
    }

    private void restoreTemplateControls(ServerLevel level, MKStructureWorkspace workspace,
                                         MKWorkspacePieceDefinition piece) {
        if (MKWorkspaceTemplateReuseTags.isDerived(piece.tags())) {
            return;
        }
        restoreStructureBlock(level, workspace, piece);
        restoreSign(level, workspace, piece);
        restoreConnectorMarkers(level, piece);
    }

    private void restoreStructureBlock(ServerLevel level, MKStructureWorkspace workspace,
                                       MKWorkspacePieceDefinition piece) {
        BlockPos structurePos = piece.structureBlockPos();
        level.setBlock(structurePos, Blocks.STRUCTURE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(structurePos);
        if (entity instanceof StructureBlockEntity structureBlock) {
            structureBlock.setMode(StructureMode.SAVE);
            structureBlock.setIgnoreEntities(true);
            structureBlock.setShowBoundingBox(true);
            structureBlock.setStructureName(ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                    workspace.structureName() + "/" + piece.pieceName()));
            structureBlock.setStructurePos(piece.worldOrigin().subtract(structurePos));
            structureBlock.setStructureSize(boundsSize(piece.exportBounds()));
            structureBlock.setChanged();
        }
    }

    private void restoreSign(ServerLevel level, MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        BlockPos signPos = piece.signPos();
        BlockState signState = Blocks.OAK_SIGN.defaultBlockState();
        level.setBlock(signPos, signState, Block.UPDATE_ALL);
        BlockEntity entity = level.getBlockEntity(signPos);
        if (entity instanceof SignBlockEntity sign) {
            SignText text = sign.getFrontText()
                    .setMessage(0, Component.literal(workspace.namespace()))
                    .setMessage(1, Component.literal(workspace.structureName()))
                    .setMessage(2, Component.literal(piece.tags().getOrDefault("workspace_topology_slot_id",
                            piece.roleId())))
                    .setMessage(3, Component.literal(piece.pieceName()));
            sign.setText(text, true);
            sign.setText(text, false);
            sign.setChanged();
            level.sendBlockUpdated(signPos, signState, signState, Block.UPDATE_ALL);
        }
    }

    private void restoreConnectorMarkers(ServerLevel level, MKWorkspacePieceDefinition piece) {
        List<BlockPos> markerPositions = piece.markerPositions();
        List<MKWorkspaceConnectorDefinition> connectors = piece.connectors();
        for (int i = 0; i < markerPositions.size(); i++) {
            BlockState markerState = i < connectors.size() ?
                    markerStateForRole(connectors.get(i).role()) :
                    Blocks.WHITE_WOOL.defaultBlockState();
            level.setBlock(markerPositions.get(i), markerState, Block.UPDATE_ALL);
        }
    }

    private BlockState markerStateForRole(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD, MAIN_BACK -> Blocks.BLUE_WOOL.defaultBlockState();
            case CONNECT_UP, CONNECT_DOWN -> Blocks.ORANGE_WOOL.defaultBlockState();
            case TOP_CAP_FORWARD, TOP_CAP_BACK -> Blocks.RED_WOOL.defaultBlockState();
            case BRANCH -> Blocks.GREEN_WOOL.defaultBlockState();
            case LINK_CANDIDATE -> Blocks.LIME_WOOL.defaultBlockState();
            default -> Blocks.WHITE_WOOL.defaultBlockState();
        };
    }
}
