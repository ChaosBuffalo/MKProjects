package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MKWorkspaceBackupBlockSnapshotStore {
    private static final int SCHEMA_VERSION = 1;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final MKWorkspaceExportPathResolver pathResolver = new MKWorkspaceExportPathResolver();

    public record RestoreStats(int restoredBlockCount, int clearedBlockCount) {
    }

    private record Snapshot(int schemaVersion, UUID workspaceId, String capturedAt, List<BlockEntry> blocks) {
        private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
        private static final Codec<Snapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("schema_version").forGetter(Snapshot::schemaVersion),
                UUID_CODEC.fieldOf("workspace_id").forGetter(Snapshot::workspaceId),
                Codec.STRING.fieldOf("captured_at").forGetter(Snapshot::capturedAt),
                BlockEntry.CODEC.listOf().fieldOf("blocks").forGetter(Snapshot::blocks)
        ).apply(instance, Snapshot::new));
    }

    private record BlockEntry(int x, int y, int z, BlockState state, Optional<String> blockEntity) {
        private static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(BlockEntry::x),
                Codec.INT.fieldOf("y").forGetter(BlockEntry::y),
                Codec.INT.fieldOf("z").forGetter(BlockEntry::z),
                BlockState.CODEC.fieldOf("state").forGetter(BlockEntry::state),
                Codec.STRING.optionalFieldOf("block_entity").forGetter(BlockEntry::blockEntity)
        ).apply(instance, BlockEntry::new));

        private BlockPos pos() {
            return new BlockPos(x, y, z);
        }
    }

    public void writeSnapshot(Path manifestPath, ServerLevel level, MKStructureWorkspace workspace, String capturedAt)
            throws IOException {
        Path snapshotPath = pathResolver.getBackupBlockSnapshotPath(manifestPath);
        Files.createDirectories(snapshotPath.getParent());

        List<BlockEntry> blocks = new ArrayList<>();
        for (BlockPos pos : collectSnapshotPositions(workspace)) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (state.isAir() && blockEntity == null) {
                continue;
            }
            blocks.add(encodeBlock(level, pos, state, blockEntity));
        }
        Snapshot snapshot = new Snapshot(SCHEMA_VERSION, workspace.id(), capturedAt, blocks);
        JsonElement json = Snapshot.CODEC.encodeStart(JsonOps.INSTANCE, snapshot).getOrThrow();
        Files.writeString(snapshotPath, gson.toJson(json));
    }

    public Optional<RestoreStats> restoreSnapshot(Path manifestPath, ServerLevel level,
                                                  MKStructureWorkspace currentWorkspace,
                                                  MKStructureWorkspace restoredWorkspace) throws IOException {
        Path snapshotPath = pathResolver.getBackupBlockSnapshotPath(manifestPath);
        if (!Files.isRegularFile(snapshotPath)) {
            return Optional.empty();
        }

        Snapshot snapshot;
        try (Reader reader = Files.newBufferedReader(snapshotPath)) {
            JsonElement json = JsonParser.parseReader(reader);
            snapshot = Snapshot.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        }
        int clearedCount = clearWorkspaceBlocks(level, currentWorkspace, restoredWorkspace);
        int restoredCount = 0;
        for (BlockEntry block : snapshot.blocks()) {
            restoreBlock(level, block);
            restoredCount++;
        }
        return Optional.of(new RestoreStats(restoredCount, clearedCount));
    }

    private BlockEntry encodeBlock(ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        Optional<String> blockEntityTag = blockEntity == null ? Optional.empty() :
                Optional.of(blockEntity.saveWithFullMetadata(level.registryAccess()).toString());
        return new BlockEntry(pos.getX(), pos.getY(), pos.getZ(), state, blockEntityTag);
    }

    private void restoreBlock(ServerLevel level, BlockEntry block) {
        BlockPos pos = block.pos();
        level.setBlock(pos, block.state(), Block.UPDATE_ALL);
        block.blockEntity().ifPresent(tag -> restoreBlockEntity(level, pos, block.state(), tag));
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
