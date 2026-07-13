package com.chaosbuffalo.mkworkspace.world.gen.workspace.preview;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKWorkspaceSamplePreviewService {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    private static final int SAMPLE_GAP = 16;
    private static final int CLEAR_MARGIN = 2;
    private static final int MAX_PIECES = 80;
    private static final int MAX_DEPTH = 12;

    public record MKWorkspaceSamplePreviewResult(
            BlockPos origin,
            BoundingBox bounds,
            long seed,
            boolean seedLocked,
            int placedPieceCount,
            int templateFallbackCount,
            List<String> warnings
    ) {
    }

    private record PreviewCandidate(MKWorkspacePieceDefinition piece, boolean templateFallback) {
    }

    private record PlacedPiece(PreviewCandidate candidate, BlockPos localOrigin, BoundingBox localBounds, int depth) {
    }

    public Optional<MKWorkspaceSamplePreviewResult> generate(ServerLevel level, MKStructureWorkspace workspace,
                                                            boolean lockSeed, List<String> errors) {
        PreviewPool pool = PreviewPool.from(workspace);
        if (pool.candidates().isEmpty()) {
            errors.add("workspace has no physical pieces to preview");
            return Optional.empty();
        }
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKWorkspaceSamplePreviewState> previousState = data.getSamplePreviewState(workspace.id());
        long seed = resolveSeed(level, previousState, lockSeed);
        RandomSource random = RandomSource.create(seed);

        Optional<PreviewCandidate> start = pool.startCandidate(random);
        if (start.isEmpty()) {
            errors.add("workspace did not define a runtime start piece and no preview start fallback was available");
            return Optional.empty();
        }

        List<PlacedPiece> placed = assemble(pool, start.get(), random);
        if (placed.isEmpty()) {
            errors.add("preview assembler produced no pieces");
            return Optional.empty();
        }
        BoundingBox localBounds = unionPlacedBounds(placed);
        BoundingBox authoringBounds = authoringBounds(workspace);
        if (authoringBounds == null) {
            errors.add("workspace has no authoring bounds");
            return Optional.empty();
        }
        BlockPos sampleMin = sampleMinCorner(workspace.anchor(), localBounds);
        BlockPos offset = sampleMin.subtract(new BlockPos(localBounds.minX(), localBounds.minY(), localBounds.minZ()));
        BoundingBox finalBounds = move(localBounds, offset);
        if (intersects(expand(finalBounds, CLEAR_MARGIN), expand(authoringBounds, CLEAR_MARGIN))) {
            errors.add("computed sample bounds overlap the workspace authoring area");
            return Optional.empty();
        }
        if (finalBounds.minY() < level.getMinBuildHeight() || finalBounds.maxY() >= level.getMaxBuildHeight()) {
            errors.add("computed sample bounds are outside the world build height");
            return Optional.empty();
        }

        if (previousState.isPresent() && !clearPreviousPreview(level, workspace, previousState.get(), authoringBounds,
                errors)) {
            return Optional.empty();
        }
        clearBounds(level, expand(finalBounds, CLEAR_MARGIN));

        int fallbackCount = 0;
        for (PlacedPiece placedPiece : placed) {
            if (placedPiece.candidate().templateFallback()) {
                fallbackCount++;
            }
            copyPiece(level, placedPiece.candidate().piece(), placedPiece.localOrigin().offset(offset));
        }

        MKWorkspaceSamplePreviewState state = new MKWorkspaceSamplePreviewState(
                sampleMin,
                finalBounds,
                seed,
                lockSeed,
                System.currentTimeMillis(),
                placed.size(),
                fallbackCount
        );
        data.setSamplePreviewState(workspace.id(), state);
        return Optional.of(new MKWorkspaceSamplePreviewResult(sampleMin, finalBounds, seed, lockSeed, placed.size(),
                fallbackCount, List.of()));
    }

    private long resolveSeed(ServerLevel level, Optional<MKWorkspaceSamplePreviewState> previousState,
                             boolean lockSeed) {
        if (lockSeed && previousState.isPresent()) {
            return previousState.get().seed();
        }
        return level.random.nextLong();
    }

    private List<PlacedPiece> assemble(PreviewPool pool, PreviewCandidate start, RandomSource random) {
        ArrayList<PlacedPiece> placed = new ArrayList<>();
        ArrayDeque<PlacedPiece> pending = new ArrayDeque<>();
        PlacedPiece startPiece = placeAt(start, BlockPos.ZERO, 0);
        placed.add(startPiece);
        pending.add(startPiece);

        while (!pending.isEmpty() && placed.size() < MAX_PIECES) {
            PlacedPiece parent = pending.removeFirst();
            if (parent.depth() >= MAX_DEPTH) {
                continue;
            }
            List<MKWorkspaceConnectorDefinition> connectors = shuffled(parent.candidate().piece().connectors(), random);
            for (MKWorkspaceConnectorDefinition connector : connectors) {
                if (placed.size() >= MAX_PIECES || connector.targetPool().equals(EMPTY_POOL)) {
                    continue;
                }
                List<PreviewCandidate> candidates = pool.candidatesForPool(connector.targetPool());
                if (candidates.isEmpty()) {
                    continue;
                }
                for (PreviewCandidate candidate : shuffled(candidates, random)) {
                    Optional<MKWorkspaceConnectorDefinition> incoming = matchingIncoming(candidate.piece(), connector);
                    if (incoming.isEmpty()) {
                        continue;
                    }
                    BlockPos childOrigin = childOrigin(parent.localOrigin(), connector, incoming.get());
                    PlacedPiece child = placeAt(candidate, childOrigin, parent.depth() + 1);
                    if (placed.stream().noneMatch(existing -> intersects(existing.localBounds(), child.localBounds()))) {
                        placed.add(child);
                        pending.add(child);
                        break;
                    }
                }
            }
        }
        return List.copyOf(placed);
    }

    private PlacedPiece placeAt(PreviewCandidate candidate, BlockPos localOrigin, int depth) {
        BoundingBox source = candidate.piece().exportBounds();
        BoundingBox localBounds = new BoundingBox(
                localOrigin.getX(),
                localOrigin.getY(),
                localOrigin.getZ(),
                localOrigin.getX() + source.getXSpan() - 1,
                localOrigin.getY() + source.getYSpan() - 1,
                localOrigin.getZ() + source.getZSpan() - 1
        );
        return new PlacedPiece(candidate, localOrigin, localBounds, depth);
    }

    private Optional<MKWorkspaceConnectorDefinition> matchingIncoming(MKWorkspacePieceDefinition candidate,
                                                                      MKWorkspaceConnectorDefinition parentConnector) {
        return candidate.connectors().stream()
                .filter(connector -> connector.facing() == parentConnector.facing().getOpposite())
                .filter(connector -> parentConnector.targetPool().equals(connector.incomingPool()))
                .findFirst();
    }

    private BlockPos childOrigin(BlockPos parentOrigin, MKWorkspaceConnectorDefinition parentConnector,
                                 MKWorkspaceConnectorDefinition childConnector) {
        BlockPos parentConnectorPos = parentOrigin.offset(parentConnector.relativePos());
        BlockPos childConnectorTarget = parentConnectorPos.relative(parentConnector.facing());
        return childConnectorTarget.subtract(childConnector.relativePos());
    }

    private <T> List<T> shuffled(List<T> values, RandomSource random) {
        ArrayList<T> copy = new ArrayList<>(values);
        for (int i = copy.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T value = copy.get(i);
            copy.set(i, copy.get(j));
            copy.set(j, value);
        }
        return copy;
    }

    private boolean clearPreviousPreview(ServerLevel level, MKStructureWorkspace workspace,
                                         MKWorkspaceSamplePreviewState previousState,
                                         BoundingBox authoringBounds,
                                         List<String> errors) {
        BoundingBox clearBounds = expand(previousState.bounds(), CLEAR_MARGIN);
        if (intersects(clearBounds, expand(authoringBounds, CLEAR_MARGIN))) {
            errors.add("stored sample preview bounds overlap the workspace authoring area; refusing to clear");
            return false;
        }
        clearBounds(level, clearBounds);
        return true;
    }

    private void copyPiece(ServerLevel level, MKWorkspacePieceDefinition sourcePiece, BlockPos destinationOrigin) {
        BoundingBox sourceBounds = sourcePiece.exportBounds();
        for (int x = 0; x < sourceBounds.getXSpan(); x++) {
            for (int y = 0; y < sourceBounds.getYSpan(); y++) {
                for (int z = 0; z < sourceBounds.getZSpan(); z++) {
                    BlockPos sourcePos = new BlockPos(sourceBounds.minX() + x, sourceBounds.minY() + y,
                            sourceBounds.minZ() + z);
                    BlockPos destPos = destinationOrigin.offset(x, y, z);
                    BlockState state = level.getBlockState(sourcePos);
                    if (state.is(Blocks.JIGSAW) || state.is(Blocks.STRUCTURE_VOID) ||
                            state.is(Blocks.STRUCTURE_BLOCK)) {
                        level.setBlock(destPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        continue;
                    }
                    level.setBlock(destPos, state, Block.UPDATE_ALL);
                    copyBlockEntity(level, sourcePos, destPos, state);
                }
            }
        }
    }

    private void copyBlockEntity(ServerLevel level, BlockPos sourcePos, BlockPos destPos, BlockState state) {
        BlockEntity sourceEntity = level.getBlockEntity(sourcePos);
        if (sourceEntity == null) {
            return;
        }
        BlockEntity destEntity = level.getBlockEntity(destPos);
        if (destEntity == null) {
            return;
        }
        CompoundTag tag = sourceEntity.saveWithFullMetadata(level.registryAccess());
        tag.putInt("x", destPos.getX());
        tag.putInt("y", destPos.getY());
        tag.putInt("z", destPos.getZ());
        destEntity.loadWithComponents(tag, level.registryAccess());
        destEntity.setChanged();
        level.sendBlockUpdated(destPos, state, state, Block.UPDATE_ALL);
    }

    private void clearBounds(ServerLevel level, BoundingBox bounds) {
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int y = Math.max(bounds.minY(), level.getMinBuildHeight());
                 y <= Math.min(bounds.maxY(), level.getMaxBuildHeight() - 1); y++) {
                for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private BoundingBox authoringBounds(MKStructureWorkspace workspace) {
        BoundingBox bounds = null;
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            bounds = merge(bounds, piece.previewBounds());
            bounds = merge(bounds, piece.exportBounds());
        }
        return bounds;
    }

    private BoundingBox unionPlacedBounds(List<PlacedPiece> placed) {
        BoundingBox bounds = null;
        for (PlacedPiece placedPiece : placed) {
            bounds = merge(bounds, placedPiece.localBounds());
        }
        if (bounds == null) {
            throw new IllegalStateException("cannot compute bounds for empty preview");
        }
        return bounds;
    }

    private BlockPos sampleMinCorner(BlockPos anchor, BoundingBox localBounds) {
        return new BlockPos(
                anchor.getX() - SAMPLE_GAP - localBounds.getXSpan(),
                anchor.getY(),
                anchor.getZ() - SAMPLE_GAP - localBounds.getZSpan()
        );
    }

    private BoundingBox move(BoundingBox bounds, BlockPos offset) {
        return new BoundingBox(
                bounds.minX() + offset.getX(),
                bounds.minY() + offset.getY(),
                bounds.minZ() + offset.getZ(),
                bounds.maxX() + offset.getX(),
                bounds.maxY() + offset.getY(),
                bounds.maxZ() + offset.getZ()
        );
    }

    private BoundingBox expand(BoundingBox bounds, int margin) {
        return new BoundingBox(bounds.minX() - margin, bounds.minY() - margin, bounds.minZ() - margin,
                bounds.maxX() + margin, bounds.maxY() + margin, bounds.maxZ() + margin);
    }

    private BoundingBox merge(@Nullable BoundingBox left, BoundingBox right) {
        if (left == null) {
            return right;
        }
        return new BoundingBox(
                Math.min(left.minX(), right.minX()),
                Math.min(left.minY(), right.minY()),
                Math.min(left.minZ(), right.minZ()),
                Math.max(left.maxX(), right.maxX()),
                Math.max(left.maxY(), right.maxY()),
                Math.max(left.maxZ(), right.maxZ())
        );
    }

    private boolean intersects(BoundingBox left, BoundingBox right) {
        return left.minX() <= right.maxX() && left.maxX() >= right.minX() &&
                left.minY() <= right.maxY() && left.maxY() >= right.minY() &&
                left.minZ() <= right.maxZ() && left.maxZ() >= right.minZ();
    }

    private static String baseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault(MKWorkspaceGridLayout.TAG_BASE_NAME, piece.pieceName());
    }

    private record PreviewPool(List<PreviewCandidate> candidates,
                               Map<ResourceLocation, List<PreviewCandidate>> candidatesByPool) {
        static PreviewPool from(MKStructureWorkspace workspace) {
            LinkedHashMap<String, List<MKWorkspacePieceDefinition>> piecesByBase = new LinkedHashMap<>();
            workspace.pieces().stream()
                    .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                    .forEach(piece -> piecesByBase.computeIfAbsent(baseName(piece), ignored -> new ArrayList<>())
                            .add(piece));

            ArrayList<PreviewCandidate> candidates = new ArrayList<>();
            for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : piecesByBase.entrySet()) {
                List<MKWorkspacePieceDefinition> variants = entry.getValue().stream()
                        .filter(piece -> piece.variantIndex() > 0)
                        .sorted(Comparator.comparingInt(MKWorkspacePieceDefinition::variantIndex))
                        .toList();
                if (variants.isEmpty()) {
                    entry.getValue().stream()
                            .filter(piece -> piece.variantIndex() == 0)
                            .findFirst()
                            .ifPresent(piece -> candidates.add(new PreviewCandidate(piece, true)));
                } else {
                    variants.forEach(piece -> candidates.add(new PreviewCandidate(piece, false)));
                }
            }

            HashMap<ResourceLocation, List<PreviewCandidate>> candidatesByPool = new HashMap<>();
            for (PreviewCandidate candidate : candidates) {
                ResourceLocation basePool = ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                        workspace.structureName() + "/" + baseName(candidate.piece()));
                candidatesByPool.computeIfAbsent(basePool, ignored -> new ArrayList<>()).add(candidate);
                for (MKWorkspaceConnectorDefinition connector : candidate.piece().connectors()) {
                    if (!connector.incomingPool().equals(EMPTY_POOL)) {
                        candidatesByPool.computeIfAbsent(connector.incomingPool(), ignored -> new ArrayList<>())
                                .add(candidate);
                    }
                }
            }
            return new PreviewPool(List.copyOf(candidates), Map.copyOf(candidatesByPool));
        }

        Optional<PreviewCandidate> startCandidate(RandomSource random) {
            List<PreviewCandidate> starts = candidates.stream()
                    .filter(candidate -> MKWorkspaceRuntimePieceInfo.fromTags(candidate.piece().tags())
                            .map(MKWorkspaceRuntimePieceInfo::start)
                            .orElse(false))
                    .toList();
            if (!starts.isEmpty()) {
                return Optional.of(starts.get(random.nextInt(starts.size())));
            }
            return candidates.stream().findFirst();
        }

        List<PreviewCandidate> candidatesForPool(ResourceLocation pool) {
            return candidatesByPool.getOrDefault(pool, List.of());
        }
    }
}
