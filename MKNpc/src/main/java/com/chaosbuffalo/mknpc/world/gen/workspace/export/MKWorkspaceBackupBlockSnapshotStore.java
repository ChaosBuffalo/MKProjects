package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class MKWorkspaceBackupBlockSnapshotStore {
    private static final int SCHEMA_VERSION = 1;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();

    public record RestoreStats(int restoredBlockCount, int clearedBlockCount) {
    }

    public void writeSnapshot(Path manifestPath, ServerLevel level, MKStructureWorkspace workspace, String capturedAt)
            throws IOException {
        Path snapshotPath = pathResolver.getBackupBlockSnapshotPath(manifestPath);
        Files.createDirectories(snapshotPath.getParent());

        JsonObject root = new JsonObject();
        root.addProperty("schema_version", SCHEMA_VERSION);
        root.addProperty("workspace_id", workspace.id().toString());
        root.addProperty("captured_at", capturedAt);
        JsonArray blocks = new JsonArray();
        for (BlockPos pos : collectSnapshotPositions(workspace)) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (state.isAir() && blockEntity == null) {
                continue;
            }
            blocks.add(encodeBlock(level, pos, state, blockEntity));
        }
        root.add("blocks", blocks);
        Files.writeString(snapshotPath, gson.toJson(root));
    }

    public Optional<RestoreStats> restoreSnapshot(Path manifestPath, ServerLevel level,
                                                  MKStructureWorkspace currentWorkspace,
                                                  MKStructureWorkspace restoredWorkspace) throws IOException {
        Path snapshotPath = pathResolver.getBackupBlockSnapshotPath(manifestPath);
        if (!Files.isRegularFile(snapshotPath)) {
            return Optional.empty();
        }

        JsonArray blocks;
        try (Reader reader = Files.newBufferedReader(snapshotPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            blocks = root.getAsJsonArray("blocks");
        }
        int clearedCount = clearWorkspaceBlocks(level, currentWorkspace, restoredWorkspace);
        int restoredCount = 0;
        for (JsonElement element : blocks) {
            restoreBlock(level, element.getAsJsonObject());
            restoredCount++;
        }
        return Optional.of(new RestoreStats(restoredCount, clearedCount));
    }

    private JsonObject encodeBlock(ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        JsonObject block = new JsonObject();
        block.addProperty("x", pos.getX());
        block.addProperty("y", pos.getY());
        block.addProperty("z", pos.getZ());
        block.add("state", BlockState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow());
        if (blockEntity != null) {
            block.addProperty("block_entity", blockEntity.saveWithFullMetadata(level.registryAccess()).toString());
        }
        return block;
    }

    private void restoreBlock(ServerLevel level, JsonObject block) {
        BlockPos pos = new BlockPos(block.get("x").getAsInt(), block.get("y").getAsInt(), block.get("z").getAsInt());
        BlockState state = BlockState.CODEC.parse(JsonOps.INSTANCE, block.get("state")).getOrThrow();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (block.has("block_entity")) {
            restoreBlockEntity(level, pos, state, block.get("block_entity").getAsString());
        }
    }

    private void restoreBlockEntity(ServerLevel level, BlockPos pos, BlockState state, String tagString) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }
        try {
            CompoundTag tag = TagParser.parseTag(tagString);
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            blockEntity.loadWithComponents(tag, level.registryAccess());
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        } catch (Exception ignored) {
        }
    }

    private int clearWorkspaceBlocks(ServerLevel level, MKStructureWorkspace currentWorkspace,
                                     MKStructureWorkspace restoredWorkspace) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        positions.addAll(collectSnapshotPositions(currentWorkspace));
        positions.addAll(collectSnapshotPositions(restoredWorkspace));
        for (BlockPos pos : positions) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return positions.size();
    }

    private Set<BlockPos> collectSnapshotPositions(MKStructureWorkspace workspace) {
        LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            addBoundsPositions(positions, piece.exportBounds());
            positions.add(piece.structureBlockPos());
            positions.add(piece.signPos());
            positions.addAll(piece.markerPositions());
        }
        return positions;
    }

    private void addBoundsPositions(Set<BlockPos> positions, BoundingBox bounds) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
    }
}
