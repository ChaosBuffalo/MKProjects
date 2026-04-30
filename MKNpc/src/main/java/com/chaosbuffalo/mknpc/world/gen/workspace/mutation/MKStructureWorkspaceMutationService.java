package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MKStructureWorkspaceMutationService {
    public record WorkspaceBlockSwapResult(Path backupPath, int pieceCount, int replacedCount,
                                           Map<ResourceLocation, MKWorkspaceBlockSwapService.BlockSwapStats> statsBySource) {
    }

    private final MKWorkspaceBackupManifestWriter backupManifestWriter = new MKWorkspaceBackupManifestWriter();
    private final MKWorkspaceBlockSwapService blockSwapService = new MKWorkspaceBlockSwapService();

    public WorkspaceBlockSwapResult swapBlocks(ServerLevel level, MKStructureWorkspace workspace,
                                               Map<ResourceLocation, ResourceLocation> replacements)
            throws IOException {
        MKWorkspaceBackupManifestWriter.WrittenBackup backup = backupManifestWriter.writeBeforeMutation(
                level.getServer(), workspace, "block-swap");
        Map<ResourceLocation, MutableStats> aggregateStats = new LinkedHashMap<>();
        int pieceCount = 0;
        int replacedCount = 0;
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            MKWorkspaceBlockSwapService.BlockSwapResult pieceResult = blockSwapService.swapBlocks(
                    level,
                    piece.exportBounds(),
                    replacements,
                    getExcludedPositions(piece)
            );
            if (pieceResult.totalReplaced() > 0) {
                pieceCount++;
                replacedCount += pieceResult.totalReplaced();
                mergeStats(aggregateStats, pieceResult.statsBySource());
            }
        }
        return new WorkspaceBlockSwapResult(backup.path(), pieceCount, replacedCount, freezeStats(aggregateStats));
    }

    private Set<BlockPos> getExcludedPositions(MKWorkspacePieceDefinition piece) {
        LinkedHashSet<BlockPos> excluded = new LinkedHashSet<>();
        for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
            excluded.add(piece.worldOrigin().offset(connector.relativePos()));
        }
        return excluded;
    }

    private void mergeStats(Map<ResourceLocation, MutableStats> aggregateStats,
                            Map<ResourceLocation, MKWorkspaceBlockSwapService.BlockSwapStats> pieceStats) {
        for (Map.Entry<ResourceLocation, MKWorkspaceBlockSwapService.BlockSwapStats> entry : pieceStats.entrySet()) {
            MKWorkspaceBlockSwapService.BlockSwapStats sourceStats = entry.getValue();
            MutableStats targetStats = aggregateStats.computeIfAbsent(entry.getKey(),
                    ignored -> new MutableStats(sourceStats.sourceBlock(), sourceStats.targetBlock()));
            targetStats.replacedCount += sourceStats.replacedCount();
            for (Map.Entry<String, Integer> droppedProperty : sourceStats.droppedProperties().entrySet()) {
                targetStats.droppedProperties.merge(droppedProperty.getKey(), droppedProperty.getValue(), Integer::sum);
            }
        }
    }

    private Map<ResourceLocation, MKWorkspaceBlockSwapService.BlockSwapStats> freezeStats(
            Map<ResourceLocation, MutableStats> aggregateStats) {
        LinkedHashMap<ResourceLocation, MKWorkspaceBlockSwapService.BlockSwapStats> frozen = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, MutableStats> entry : aggregateStats.entrySet()) {
            MutableStats stats = entry.getValue();
            frozen.put(entry.getKey(), new MKWorkspaceBlockSwapService.BlockSwapStats(
                    stats.sourceBlock,
                    stats.targetBlock,
                    stats.replacedCount,
                    Map.copyOf(stats.droppedProperties)
            ));
        }
        return Map.copyOf(frozen);
    }

    private static class MutableStats {
        private final ResourceLocation sourceBlock;
        private final ResourceLocation targetBlock;
        private int replacedCount;
        private final Map<String, Integer> droppedProperties = new LinkedHashMap<>();

        private MutableStats(ResourceLocation sourceBlock, ResourceLocation targetBlock) {
            this.sourceBlock = sourceBlock;
            this.targetBlock = targetBlock;
        }
    }
}
