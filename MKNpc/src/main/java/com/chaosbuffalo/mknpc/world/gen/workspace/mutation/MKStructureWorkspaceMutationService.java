package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteSwapSafety;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
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
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();

    public WorkspaceBlockSwapResult swapBlocks(ServerLevel level, MKStructureWorkspace workspace,
                                               Map<ResourceLocation, ResourceLocation> replacements)
            throws IOException {
        return swapBlocks(level, workspace, replacements, "block-swap");
    }

    public WorkspaceBlockSwapResult swapPalette(ServerLevel level, MKStructureWorkspace workspace,
                                                MKWorkspaceMaterialPalette targetPalette)
            throws IOException {
        MKStructureWorkspace targetWorkspace = withPalette(workspace, targetPalette);
        return swapMaterialPalettes(level, workspace, targetWorkspace);
    }

    public WorkspaceBlockSwapResult swapMaterialPalettes(ServerLevel level, MKStructureWorkspace existing,
                                                         MKStructureWorkspace requested)
            throws IOException {
        MKStructureWorkspace requestedWithPieces = withPiecesAndIdentity(requested, existing);
        MKWorkspaceBackupManifestWriter.WrittenBackup backup = backupManifestWriter.writeBeforeMutation(
                level, existing, "palette-swap");
        Map<ResourceLocation, MutableStats> aggregateStats = new LinkedHashMap<>();
        int pieceCount = 0;
        int replacedCount = 0;
        for (MKWorkspacePieceDefinition piece : existing.pieces()) {
            MKWorkspaceMaterialPalette sourcePalette = paletteResolver.resolvePiece(existing, piece)
                    .orElse(existing.palette());
            MKWorkspaceMaterialPalette targetPalette = paletteResolver.resolvePiece(requestedWithPieces, piece)
                    .orElse(requestedWithPieces.palette());
            LinkedHashMap<ResourceLocation, ResourceLocation> replacements = replacementsFor(sourcePalette, targetPalette);
            if (replacements.isEmpty()) {
                continue;
            }
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
        IMKStructureWorkspaceData.get(level).updateWorkspace(requestedWithPieces);
        return new WorkspaceBlockSwapResult(backup.path(), pieceCount, replacedCount, freezeStats(aggregateStats));
    }

    private LinkedHashMap<ResourceLocation, ResourceLocation> replacementsFor(MKWorkspaceMaterialPalette sourcePalette,
                                                                              MKWorkspaceMaterialPalette targetPalette) {
        if (!MKWorkspacePaletteSwapSafety.canRepresentAsBlockReplacement(sourcePalette, targetPalette)) {
            throw new IllegalArgumentException("palette swap cannot be represented as a role-blind block replacement");
        }
        LinkedHashMap<ResourceLocation, ResourceLocation> replacements = new LinkedHashMap<>();
        addReplacement(replacements, sourcePalette.floorBlock(), targetPalette.floorBlock());
        addReplacement(replacements, sourcePalette.wallBlock(), targetPalette.wallBlock());
        addReplacement(replacements, sourcePalette.ceilingBlock(), targetPalette.ceilingBlock());
        addReplacement(replacements, sourcePalette.stairBlock(), targetPalette.stairBlock());
        addReplacement(replacements, sourcePalette.slabBlock(), targetPalette.slabBlock());
        addReplacement(replacements, sourcePalette.ladderBlock(), targetPalette.ladderBlock());
        return replacements;
    }

    private WorkspaceBlockSwapResult swapBlocks(ServerLevel level, MKStructureWorkspace workspace,
                                                Map<ResourceLocation, ResourceLocation> replacements,
                                                String operation)
            throws IOException {
        MKWorkspaceBackupManifestWriter.WrittenBackup backup = backupManifestWriter.writeBeforeMutation(
                level, workspace, operation);
        return swapBlocksWithoutBackup(level, workspace, replacements, backup.path());
    }

    private WorkspaceBlockSwapResult swapBlocksWithoutBackup(ServerLevel level, MKStructureWorkspace workspace,
                                                             Map<ResourceLocation, ResourceLocation> replacements,
                                                             Path backupPath) {
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
        return new WorkspaceBlockSwapResult(backupPath, pieceCount, replacedCount, freezeStats(aggregateStats));
    }

    private void addReplacement(Map<ResourceLocation, ResourceLocation> replacements, ResourceLocation source,
                                ResourceLocation target) {
        if (!source.equals(target)) {
            replacements.put(source, target);
        }
    }

    private MKStructureWorkspace withPalette(MKStructureWorkspace workspace, MKWorkspaceMaterialPalette palette) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                palette,
                alignStairMaterials(workspace.stairConfig(), palette),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                alignVerticalAccessMaterials(workspace.verticalAccessSpec(), palette),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.createdAt(),
                System.currentTimeMillis(),
                workspace.pieces()
        );
    }

    private MKStructureWorkspace withPiecesAndIdentity(MKStructureWorkspace requested, MKStructureWorkspace existing) {
        return new MKStructureWorkspace(
                existing.id(),
                existing.anchor(),
                requested.namespace(),
                requested.structureName(),
                requested.topologyProfile(),
                requested.dimensions(),
                requested.palette(),
                alignStairMaterials(requested.stairConfig(), requested.palette()),
                requested.verticalAccessPlacement(),
                requested.shellMargin(),
                requested.exteriorAirMargin(),
                requested.previewMargin(),
                alignVerticalAccessMaterials(requested.verticalAccessSpec(), requested.palette()),
                requested.familyDefinitions(),
                requested.openingProfiles(),
                requested.linearRunFamilies(),
                existing.createdAt(),
                System.currentTimeMillis(),
                existing.pieces()
        );
    }

    private MKWorkspaceStairAuthoringConfig alignStairMaterials(MKWorkspaceStairAuthoringConfig stairConfig,
                                                                MKWorkspaceMaterialPalette palette) {
        return new MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.stairWidth()
        );
    }

    private MKWorkspaceVerticalAccessSpec alignVerticalAccessMaterials(MKWorkspaceVerticalAccessSpec spec,
                                                                       MKWorkspaceMaterialPalette palette) {
        return new MKWorkspaceVerticalAccessSpec(
                spec.shaftSize(),
                spec.placement(),
                alignStairMaterials(spec.stairConfig(), palette)
        );
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
