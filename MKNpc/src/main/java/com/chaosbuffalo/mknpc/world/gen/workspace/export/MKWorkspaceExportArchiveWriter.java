package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        Map<String, MKWorkspacePieceDefinition> sourcePieces = sourcePiecesByBaseNameAndVariant(workspace);
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            if (MKWorkspaceTemplateReuseTags.isDerived(piece.tags()) &&
                    "template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance"))) {
                continue;
            }
            output.putNextEntry(new ZipEntry(structureEntryName(manifest, piece)));
            output.write(pieceNbtBytes(level, piece, sourcePieces));
            output.closeEntry();
            written++;
        }
        return written;
    }

    private int writePieceMetadata(ZipOutputStream output, MKWorkspaceExportManifest manifest) throws IOException {
        Map<String, MKWorkspaceExportManifest.ExportRuntimeTemplateGroup> groupByBaseName = new LinkedHashMap<>();
        for (MKWorkspaceExportManifest.ExportRuntimeTemplateGroup templateGroup : manifest.runtimeHints().templateGroups()) {
            groupByBaseName.put(templateGroup.baseName(), templateGroup);
        }

        int written = 0;
        for (MKWorkspaceExportManifest.ExportPiece piece : manifest.pieces()) {
            if ("template".equals(piece.workspacePieceKind())) {
                continue;
            }
            MKWorkspaceExportManifest.ExportRuntimeTemplateGroup templateGroup = groupByBaseName.get(piece.baseName());
            if (templateGroup == null) {
                continue;
            }
            MKJigsawPieceMetadata metadata = new MKJigsawPieceMetadata(
                    templateGroup.pieceMetadata().role(),
                    templateGroup.pieceMetadata().progressionDelta(),
                    templateGroup.pieceMetadata().verticalLevelDelta(),
                    templateGroup.pieceMetadata().allowOnMainPath(),
                    templateGroup.pieceMetadata().allowOnBranchPath(),
                    templateGroup.pieceMetadata().terminal(),
                    templateGroup.pieceMetadata().topCapOnly(),
                    templateGroup.pieceMetadata().topologyGroup(),
                    templateGroup.pieceMetadata().mainPathEnding(),
                    templateGroup.pieceMetadata().branchCap(),
                    templateGroup.pieceMetadata().foundationPolicy()
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

    private byte[] pieceNbtBytes(ServerLevel level, MKWorkspacePieceDefinition piece,
                                 Map<String, MKWorkspacePieceDefinition> sourcePieces) throws IOException {
        CompoundTag tag;
        if (MKWorkspaceTemplateReuseTags.isDerived(piece.tags())) {
            MKWorkspacePieceDefinition sourcePiece = resolveSourcePiece(piece, sourcePieces);
            if (sourcePiece == null) {
                throw new IllegalStateException("derived export piece " + piece.pieceName() +
                        " references missing source " + MKWorkspaceTemplateReuseTags.sourceId(piece.tags()));
            }
            tag = rotatedDerivedPieceTag(level, sourcePiece, piece);
        } else {
            tag = pieceNbtTag(level, piece);
        }
        return compressedBytes(tag);
    }

    private CompoundTag pieceNbtTag(ServerLevel level, MKWorkspacePieceDefinition piece) {
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, boundsMin(piece.exportBounds()), boundsSize(piece.exportBounds()), false, null);
        return template.save(new CompoundTag());
    }

    private byte[] compressedBytes(CompoundTag tag) throws IOException {
        ByteArrayOutputStream pieceBytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(tag, pieceBytes);
        return pieceBytes.toByteArray();
    }

    private Map<String, MKWorkspacePieceDefinition> sourcePiecesByBaseNameAndVariant(MKStructureWorkspace workspace) {
        Map<String, MKWorkspacePieceDefinition> result = new LinkedHashMap<>();
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            String baseName = piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
            result.put(sourceKey(baseName, piece.variantIndex()), piece);
        }
        return result;
    }

    private MKWorkspacePieceDefinition resolveSourcePiece(MKWorkspacePieceDefinition piece,
                                                          Map<String, MKWorkspacePieceDefinition> sourcePieces) {
        String sourceId = MKWorkspaceTemplateReuseTags.sourceId(piece.tags());
        MKWorkspacePieceDefinition sameVariant = sourcePieces.get(sourceKey(sourceId, piece.variantIndex()));
        if (sameVariant != null && !MKWorkspaceTemplateReuseTags.isDerived(sameVariant.tags())) {
            return sameVariant;
        }
        MKWorkspacePieceDefinition template = sourcePieces.get(sourceKey(sourceId, 0));
        if (template != null && !MKWorkspaceTemplateReuseTags.isDerived(template.tags())) {
            return template;
        }
        return null;
    }

    private String sourceKey(String baseName, int variantIndex) {
        return baseName + "#" + variantIndex;
    }

    private CompoundTag rotatedDerivedPieceTag(ServerLevel level, MKWorkspacePieceDefinition sourcePiece,
                                               MKWorkspacePieceDefinition targetPiece) {
        CompoundTag sourceTag = pieceNbtTag(level, sourcePiece);
        Rotation rotation = MKWorkspaceTemplateReuseTags.rotation(targetPiece.tags());
        HolderGetter<Block> blockGetter = level.registryAccess().lookupOrThrow(Registries.BLOCK);
        ListTag sourceSize = sourceTag.getList("size", Tag.TAG_INT);
        int sourceWidth = sourceSize.getInt(0);
        int sourceHeight = sourceSize.getInt(1);
        int sourceLength = sourceSize.getInt(2);
        int targetWidth = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ?
                sourceLength : sourceWidth;
        int targetLength = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ?
                sourceWidth : sourceLength;
        Map<BlockPos, ExportBlock> blocksByPos = new LinkedHashMap<>();
        List<BlockState> sourcePalette = readPalette(blockGetter, sourceTag);
        ListTag sourceBlocks = sourceTag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < sourceBlocks.size(); i++) {
            CompoundTag sourceBlock = sourceBlocks.getCompound(i);
            BlockPos sourcePos = readBlockPos(sourceBlock.getList("pos", Tag.TAG_INT));
            BlockState state = sourcePalette.get(sourceBlock.getInt("state")).rotate(rotation);
            if (state.is(Blocks.JIGSAW)) {
                continue;
            }
            BlockPos targetPos = rotatePos(sourcePos, sourceWidth, sourceLength, rotation);
            CompoundTag blockNbt = sourceBlock.contains("nbt", Tag.TAG_COMPOUND) ?
                    sourceBlock.getCompound("nbt").copy() : null;
            if (blockNbt != null) {
                blockNbt.putInt("x", targetPos.getX());
                blockNbt.putInt("y", targetPos.getY());
                blockNbt.putInt("z", targetPos.getZ());
            }
            blocksByPos.put(targetPos, new ExportBlock(targetPos, state, blockNbt));
        }
        for (var connector : targetPiece.connectors()) {
            BlockPos pos = connector.relativePos();
            BlockState state = Blocks.JIGSAW.defaultBlockState()
                    .setValue(JigsawBlock.ORIENTATION, getJigsawOrientation(connector.facing()));
            CompoundTag jigsawNbt = new CompoundTag();
            jigsawNbt.putString("id", "minecraft:jigsaw");
            jigsawNbt.putString("name", connector.jigsawName().toString());
            jigsawNbt.putString("target", connector.jigsawTarget().toString());
            jigsawNbt.putString("pool", connector.targetPool().toString());
            jigsawNbt.putString("final_state", "minecraft:air");
            jigsawNbt.putString("joint", "aligned");
            jigsawNbt.putInt("x", pos.getX());
            jigsawNbt.putInt("y", pos.getY());
            jigsawNbt.putInt("z", pos.getZ());
            blocksByPos.put(pos, new ExportBlock(pos, state, jigsawNbt));
        }
        ExportCrop crop = MKWorkspaceTemplateReuseTags.cropsNonStructureVoid(targetPiece.tags()) ?
                detectNonStructureVoidCrop(blocksByPos.values(), targetWidth, targetLength) :
                ExportCrop.full(targetWidth, targetLength);
        List<ExportBlock> croppedBlocks = cropBlocks(blocksByPos.values(), crop);
        CompoundTag result = sourceTag.copy();
        result.put("size", intList(crop.width(), sourceHeight, crop.length()));
        result.remove("palettes");
        writePaletteAndBlocks(result, croppedBlocks);
        return result;
    }

    private ExportCrop detectNonStructureVoidCrop(Iterable<ExportBlock> blocks, int width, int length) {
        int minX = width;
        int minZ = length;
        int maxX = -1;
        int maxZ = -1;
        for (ExportBlock block : blocks) {
            if (block.state().is(Blocks.STRUCTURE_VOID)) {
                continue;
            }
            minX = Math.min(minX, block.pos().getX());
            minZ = Math.min(minZ, block.pos().getZ());
            maxX = Math.max(maxX, block.pos().getX());
            maxZ = Math.max(maxZ, block.pos().getZ());
        }
        if (maxX < minX || maxZ < minZ) {
            return ExportCrop.full(width, length);
        }
        AxisCrop xCrop = normalizeOddCrop(minX, maxX, width);
        AxisCrop zCrop = normalizeOddCrop(minZ, maxZ, length);
        return new ExportCrop(xCrop.min(), xCrop.max(), zCrop.min(), zCrop.max());
    }

    private AxisCrop normalizeOddCrop(int min, int max, int size) {
        if (((max - min + 1) & 1) == 1) {
            return new AxisCrop(min, max);
        }
        int center = (size - 1) / 2;
        int distanceToMin = Math.abs(center - min);
        int distanceToMax = Math.abs(center - max);
        if (distanceToMin <= distanceToMax && min > 0) {
            return new AxisCrop(min - 1, max);
        }
        if (max < size - 1) {
            return new AxisCrop(min, max + 1);
        }
        if (min > 0) {
            return new AxisCrop(min - 1, max);
        }
        return new AxisCrop(min, max);
    }

    private List<ExportBlock> cropBlocks(Iterable<ExportBlock> blocks, ExportCrop crop) {
        ArrayList<ExportBlock> result = new ArrayList<>();
        for (ExportBlock block : blocks) {
            BlockPos pos = block.pos();
            if (pos.getX() < crop.minX() || pos.getX() > crop.maxX() ||
                    pos.getZ() < crop.minZ() || pos.getZ() > crop.maxZ()) {
                continue;
            }
            BlockPos croppedPos = new BlockPos(pos.getX() - crop.minX(), pos.getY(),
                    pos.getZ() - crop.minZ());
            CompoundTag blockNbt = block.nbt() == null ? null : block.nbt().copy();
            if (blockNbt != null) {
                blockNbt.putInt("x", croppedPos.getX());
                blockNbt.putInt("y", croppedPos.getY());
                blockNbt.putInt("z", croppedPos.getZ());
            }
            result.add(new ExportBlock(croppedPos, block.state(), blockNbt));
        }
        return result;
    }

    private List<BlockState> readPalette(HolderGetter<Block> blockGetter, CompoundTag tag) {
        ListTag paletteTag = tag.contains("palette", Tag.TAG_LIST) ?
                tag.getList("palette", Tag.TAG_COMPOUND) :
                tag.getList("palettes", Tag.TAG_LIST).getList(0);
        List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(blockGetter, paletteTag.getCompound(i)));
        }
        return palette;
    }

    private void writePaletteAndBlocks(CompoundTag tag, List<ExportBlock> blocks) {
        Map<BlockState, Integer> paletteIndexes = new LinkedHashMap<>();
        ListTag blockList = new ListTag();
        for (ExportBlock block : blocks) {
            int stateId = paletteIndexes.computeIfAbsent(block.state(), ignored -> paletteIndexes.size());
            CompoundTag blockTag = new CompoundTag();
            blockTag.put("pos", intList(block.pos().getX(), block.pos().getY(), block.pos().getZ()));
            blockTag.putInt("state", stateId);
            if (block.nbt() != null) {
                blockTag.put("nbt", block.nbt());
            }
            blockList.add(blockTag);
        }
        ListTag paletteTag = new ListTag();
        for (BlockState state : paletteIndexes.keySet()) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        tag.put("blocks", blockList);
        tag.put("palette", paletteTag);
    }

    private BlockPos rotatePos(BlockPos pos, int sourceWidth, int sourceLength, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new BlockPos(sourceLength - 1 - pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> new BlockPos(sourceWidth - 1 - pos.getX(), pos.getY(),
                    sourceLength - 1 - pos.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), sourceWidth - 1 - pos.getX());
            default -> pos;
        };
    }

    private BlockPos readBlockPos(ListTag list) {
        return new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2));
    }

    private ListTag intList(int x, int y, int z) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(x));
        list.add(IntTag.valueOf(y));
        list.add(IntTag.valueOf(z));
        return list;
    }

    private FrontAndTop getJigsawOrientation(net.minecraft.core.Direction facing) {
        if (facing == net.minecraft.core.Direction.UP || facing == net.minecraft.core.Direction.DOWN) {
            return FrontAndTop.fromFrontAndTop(facing, net.minecraft.core.Direction.NORTH);
        }
        return FrontAndTop.fromFrontAndTop(facing, net.minecraft.core.Direction.UP);
    }

    private record ExportBlock(BlockPos pos, BlockState state, CompoundTag nbt) {
    }

    private record AxisCrop(int min, int max) {
    }

    private record ExportCrop(int minX, int maxX, int minZ, int maxZ) {
        private static ExportCrop full(int width, int length) {
            return new ExportCrop(0, width - 1, 0, length - 1);
        }

        private int width() {
            return maxX - minX + 1;
        }

        private int length() {
            return maxZ - minZ + 1;
        }
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
