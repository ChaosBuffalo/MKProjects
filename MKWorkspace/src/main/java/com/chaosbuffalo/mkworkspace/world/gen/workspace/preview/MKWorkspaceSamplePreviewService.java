package com.chaosbuffalo.mkworkspace.world.gen.workspace.preview;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorInfo;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonConnectorSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutController;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonPieceState;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonTopologyGroupRule;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadataManager;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKVerticalProgressionMode;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologyPoolNames;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKWorkspaceSamplePreviewService {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    private static final ResourceLocation WALLED_KEEP_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc", "walled_keep");
    private static final ResourceLocation TOWER_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    private static final int SAMPLE_GAP = 16;
    private static final int CLEAR_MARGIN = 2;
    private static final int RUNTIME_SPREAD_RESERVE = 128;
    private static final int MAX_PIECES = 256;
    private static final int MAX_DEPTH = 24;

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

    private record PreviewCandidate(MKWorkspacePieceDefinition piece,
                                    boolean templateFallback,
                                    ResourceLocation templateId,
                                    MKJigsawPieceMetadata metadata) {
    }

    private record PlacedPiece(PreviewCandidate candidate,
                               BlockPos localOrigin,
                               BoundingBox localBounds,
                               int depth,
                               MKDungeonPieceState state) {
    }

    private record RuntimePreviewContext(PreviewPool pool,
                                         MKDungeonLayoutController layoutController,
                                         MKDungeonLayoutSettings layoutSettings,
                                         int maxDepth,
                                         int maxDistanceFromCenter) {
    }

    public Optional<MKWorkspaceSamplePreviewResult> generate(ServerLevel level, MKStructureWorkspace workspace,
                                                            boolean lockSeed, List<String> errors) {
        RuntimePreviewContext runtime = RuntimePreviewContextBuilder.from(workspace, errors).orElse(null);
        if (runtime == null) {
            return Optional.empty();
        }
        if (runtime.pool().candidates().isEmpty()) {
            errors.add("workspace has no physical pieces to preview");
            return Optional.empty();
        }

        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        Optional<MKWorkspaceSamplePreviewState> previousState = data.getSamplePreviewState(workspace.id());
        long seed = resolveSeed(level, previousState, lockSeed);
        RandomSource random = RandomSource.create(seed);

        Optional<PreviewCandidate> start = runtime.pool().startCandidate(random);
        if (start.isEmpty()) {
            errors.add("workspace did not define a runtime start piece and no preview start fallback was available");
            return Optional.empty();
        }

        List<String> warnings = new ArrayList<>();
        List<PlacedPiece> placed = assemble(runtime, start.get(), random, warnings);
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
        BlockPos sampleCenter = sampleCenter(workspace.anchor(), runtime.maxDistanceFromCenter());
        BlockPos offset = sampleCenter.subtract(center(localBounds));
        BoundingBox finalBounds = move(localBounds, offset);
        if (intersects(expand(finalBounds, CLEAR_MARGIN), expand(authoringBounds, CLEAR_MARGIN))) {
            errors.add("computed sample bounds overlap the workspace authoring area");
            return Optional.empty();
        }
        if (finalBounds.minY() < level.getMinBuildHeight() || finalBounds.maxY() >= level.getMaxBuildHeight()) {
            errors.add("computed sample bounds are outside the world build height");
            return Optional.empty();
        }

        if (previousState.isPresent() && !clearPreviousPreview(level, previousState.get(), authoringBounds, errors)) {
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
        runAfterPlace(level, workspace, runtime, placed, offset, finalBounds, random);

        BlockPos origin = new BlockPos(finalBounds.minX(), finalBounds.minY(), finalBounds.minZ());
        MKWorkspaceSamplePreviewState state = new MKWorkspaceSamplePreviewState(
                origin,
                finalBounds,
                seed,
                lockSeed,
                System.currentTimeMillis(),
                placed.size(),
                fallbackCount
        );
        data.setSamplePreviewState(workspace.id(), state);
        return Optional.of(new MKWorkspaceSamplePreviewResult(origin, finalBounds, seed, lockSeed, placed.size(),
                fallbackCount, List.copyOf(warnings)));
    }

    private long resolveSeed(ServerLevel level, Optional<MKWorkspaceSamplePreviewState> previousState,
                             boolean lockSeed) {
        if (lockSeed && previousState.isPresent()) {
            return previousState.get().seed();
        }
        return level.random.nextLong();
    }

    private List<PlacedPiece> assemble(RuntimePreviewContext runtime, PreviewCandidate start, RandomSource random,
                                       List<String> warnings) {
        ArrayList<PlacedPiece> placed = new ArrayList<>();
        ArrayDeque<PlacedPiece> pending = new ArrayDeque<>();
        int targetFloors = runtime.layoutController().chooseTargetFloors(random);
        MKDungeonPieceState baseRootState = new MKDungeonPieceState(0, 0, 1, 0, true, targetFloors);
        MKDungeonPieceState rootState = runtime.layoutController()
                .initialStateForStart(baseRootState, start.metadata(), random);
        PlacedPiece startPiece = placeAt(start, BlockPos.ZERO, 0, rootState);
        placed.add(startPiece);
        pending.add(startPiece);

        while (!pending.isEmpty() && placed.size() < MAX_PIECES) {
            PlacedPiece parent = pending.removeFirst();
            if (parent.depth() >= runtime.maxDepth()) {
                continue;
            }
            List<MKWorkspaceConnectorDefinition> connectors = shuffled(parent.candidate().piece().connectors(), random);
            for (MKWorkspaceConnectorDefinition connector : connectors) {
                if (placed.size() >= MAX_PIECES || connector.targetPool().equals(EMPTY_POOL)) {
                    continue;
                }
                MKConnectorInfo connectorInfo = connectorInfo(connector);
                List<PreviewCandidate> candidates = runtime.pool().candidatesForConnector(parent.state(), connectorInfo,
                        random, runtime.layoutController());
                if (candidates.isEmpty()) {
                    continue;
                }
                boolean branchCapsAvailable = runtime.pool().branchCapsAvailable(connector.targetPool());
                for (PreviewCandidate candidate : candidates) {
                    if (runtime.layoutController()
                            .getRejectionReason(parent.state(), connectorInfo, candidate.metadata(),
                                    branchCapsAvailable)
                            .isPresent()) {
                        continue;
                    }
                    Optional<MKWorkspaceConnectorDefinition> incoming = matchingIncoming(candidate.piece(), connector);
                    if (incoming.isEmpty()) {
                        continue;
                    }
                    BlockPos childOrigin = childOrigin(parent.localOrigin(), connector, incoming.get());
                    MKDungeonPieceState childState = runtime.layoutController()
                            .nextState(parent.state(), connectorInfo, candidate.metadata(), random);
                    PlacedPiece child = placeAt(candidate, childOrigin, parent.depth() + 1, childState);
                    if (!withinSpread(child.localBounds(), runtime.maxDistanceFromCenter())) {
                        continue;
                    }
                    if (placed.stream().noneMatch(existing -> intersects(existing.localBounds(), child.localBounds()))) {
                        placed.add(child);
                        pending.add(child);
                        break;
                    }
                }
            }
        }
        if (placed.size() >= MAX_PIECES) {
            warnings.add("preview reached the piece limit before all connectors were explored");
        }
        return List.copyOf(placed);
    }

    private MKConnectorInfo connectorInfo(MKWorkspaceConnectorDefinition connector) {
        return new MKConnectorInfo(
                connector.jigsawName(),
                connector.jigsawTarget(),
                ResourceKey.create(Registries.TEMPLATE_POOL, connector.targetPool()),
                connector.role()
        );
    }

    private PlacedPiece placeAt(PreviewCandidate candidate, BlockPos localOrigin, int depth,
                                MKDungeonPieceState state) {
        BoundingBox source = candidate.piece().exportBounds();
        BoundingBox localBounds = new BoundingBox(
                localOrigin.getX(),
                localOrigin.getY(),
                localOrigin.getZ(),
                localOrigin.getX() + source.getXSpan() - 1,
                localOrigin.getY() + source.getYSpan() - 1,
                localOrigin.getZ() + source.getZSpan() - 1
        );
        return new PlacedPiece(candidate, localOrigin, localBounds, depth, state);
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

    private boolean withinSpread(BoundingBox bounds, int spread) {
        BlockPos center = center(bounds);
        return Math.abs(center.getX()) <= spread && Math.abs(center.getZ()) <= spread;
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

    private boolean clearPreviousPreview(ServerLevel level,
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

    private void runAfterPlace(ServerLevel level, MKStructureWorkspace workspace, RuntimePreviewContext runtime,
                               List<PlacedPiece> placed, BlockPos offset, BoundingBox finalBounds,
                               RandomSource random) {
        Map<ResourceLocation, MKJigsawPieceMetadata> metadataOverrides = previewMetadataOverrides(placed);
        MKJigsawPieceMetadataManager.MetadataOverrideSnapshot snapshot =
                MKJigsawPieceMetadataManager.installTemporaryPreviewOverrides(metadataOverrides);
        try {
            PiecesContainer pieces = previewPieces(level, placed, offset);
            BlockPos origin = new BlockPos(finalBounds.minX(), finalBounds.minY(), finalBounds.minZ());
            MKJigsawStructure.runPreviewAfterPlace(
                    level,
                    level.structureManager(),
                    level.getChunkSource().getGenerator(),
                    random,
                    expand(finalBounds, CLEAR_MARGIN),
                    new ChunkPos(origin),
                    pieces,
                    runtime.layoutSettings(),
                    runtime.maxDistanceFromCenter(),
                    ResourceLocation.fromNamespaceAndPath(workspace.namespace(), workspace.structureName() + "/start")
            );
        } finally {
            MKJigsawPieceMetadataManager.restoreTemporaryPreviewOverrides(snapshot);
        }
    }

    private Map<ResourceLocation, MKJigsawPieceMetadata> previewMetadataOverrides(List<PlacedPiece> placed) {
        HashMap<ResourceLocation, MKJigsawPieceMetadata> overrides = new HashMap<>();
        for (PlacedPiece placedPiece : placed) {
            overrides.put(placedPiece.candidate().templateId(), placedPiece.candidate().metadata());
        }
        return Map.copyOf(overrides);
    }

    private PiecesContainer previewPieces(ServerLevel level, List<PlacedPiece> placed, BlockPos offset) {
        ArrayList<StructurePiece> pieces = new ArrayList<>();
        for (PlacedPiece placedPiece : placed) {
            StructurePoolElement element = MKSinglePoolElement
                    .forTemplate(placedPiece.candidate().templateId(), false)
                    .apply(StructureTemplatePool.Projection.RIGID);
            BlockPos pieceOrigin = placedPiece.localOrigin().offset(offset);
            BoundingBox pieceBounds = move(placedPiece.localBounds(), offset);
            pieces.add(new PoolElementStructurePiece(
                    level.getStructureManager(),
                    element,
                    pieceOrigin,
                    element.getGroundLevelDelta(),
                    Rotation.NONE,
                    pieceBounds,
                    LiquidSettings.IGNORE_WATERLOGGING
            ));
        }
        return new PiecesContainer(pieces);
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

    private BlockPos sampleCenter(BlockPos anchor, int maxDistanceFromCenter) {
        int distance = Math.max(RUNTIME_SPREAD_RESERVE, maxDistanceFromCenter) + SAMPLE_GAP;
        return new BlockPos(anchor.getX() - distance, anchor.getY(), anchor.getZ() - distance);
    }

    private BlockPos center(BoundingBox bounds) {
        return new BlockPos((bounds.minX() + bounds.maxX()) / 2,
                bounds.minY(),
                (bounds.minZ() + bounds.maxZ()) / 2);
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

    private static MKJigsawPieceMetadata jigsawMetadata(
            MKWorkspaceExportManifest.ExportRuntimePieceMetadata metadata) {
        return new MKJigsawPieceMetadata(
                metadata.role(),
                metadata.progressionDelta(),
                metadata.verticalLevelDelta(),
                metadata.allowOnMainPath(),
                metadata.allowOnBranchPath(),
                metadata.terminal(),
                metadata.topCapOnly(),
                metadata.topologyGroup(),
                metadata.mainPathEnding(),
                metadata.branchCap(),
                metadata.verticalStackId(),
                metadata.verticalStackSlot(),
                metadata.minMainFloors(),
                metadata.maxMainFloors(),
                metadata.minBasementFloors(),
                metadata.maxBasementFloors(),
                metadata.topCapApproachEnabled(),
                metadata.basementEntryEnabled(),
                metadata.basementCapApproachEnabled(),
                metadata.floorExitMask(),
                metadata.foundationPolicy(),
                metadata.floorBlock(),
                metadata.wallBlock(),
                metadata.ceilingBlock(),
                metadata.floorLinkCandidates(),
                metadata.floorClosableOpenings(),
                metadata.floorRootExits()
        );
    }

    private record PreviewPool(String startBaseName,
                               List<PreviewCandidate> candidates,
                               Map<String, List<PreviewCandidate>> candidatesByBaseName,
                               Map<ResourceLocation, List<PreviewCandidate>> candidatesByPool) {
        Optional<PreviewCandidate> startCandidate(RandomSource random) {
            List<PreviewCandidate> starts = candidatesByBaseName.getOrDefault(startBaseName, List.of());
            if (!starts.isEmpty()) {
                return Optional.of(starts.get(random.nextInt(starts.size())));
            }
            return candidates.stream()
                    .filter(candidate -> MKWorkspaceRuntimePieceInfo.fromTags(candidate.piece().tags())
                            .map(MKWorkspaceRuntimePieceInfo::start)
                            .orElse(false))
                    .findFirst()
                    .or(() -> candidates.stream().findFirst());
        }

        List<PreviewCandidate> candidatesForConnector(MKDungeonPieceState parentState,
                                                      MKConnectorInfo connectorInfo,
                                                      RandomSource random,
                                                      MKDungeonLayoutController layoutController) {
            LinkedHashSet<PreviewCandidate> result = new LinkedHashSet<>();
            layoutController.endingPoolForState(parentState, connectorInfo)
                    .ifPresent(pool -> result.addAll(candidatesByPool.getOrDefault(pool, List.of())));
            result.addAll(candidatesByPool.getOrDefault(connectorInfo.targetPool().location(), List.of()));
            return shuffleDistinct(result, random);
        }

        boolean branchCapsAvailable(ResourceLocation targetPool) {
            return branchCapPoolFor(targetPool)
                    .map(pool -> !candidatesByPool.getOrDefault(pool, List.of()).isEmpty())
                    .orElse(false);
        }

        private List<PreviewCandidate> shuffleDistinct(LinkedHashSet<PreviewCandidate> values, RandomSource random) {
            ArrayList<PreviewCandidate> copy = new ArrayList<>(values);
            for (int i = copy.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                PreviewCandidate value = copy.get(i);
                copy.set(i, copy.get(j));
                copy.set(j, value);
            }
            return List.copyOf(copy);
        }
    }

    private static final class RuntimePreviewContextBuilder {
        private RuntimePreviewContextBuilder() {
        }

        static Optional<RuntimePreviewContext> from(MKStructureWorkspace workspace, List<String> errors) {
            MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4,
                    Instant.now().toString());
            List<String> validationErrors = manifest.validateRuntimeStructureExport();
            if (!validationErrors.isEmpty()) {
                errors.addAll(validationErrors);
                return Optional.empty();
            }
            Optional<PreviewPool> pool = buildPool(workspace, manifest, errors);
            if (pool.isEmpty()) {
                return Optional.empty();
            }
            List<MKDungeonTopologyGroupRule> floorRules = floorTopologyRules(manifest);
            MKDungeonLayoutSettings layoutSettings = layoutSettings(workspace, floorRules);
            return Optional.of(new RuntimePreviewContext(
                    pool.orElseThrow(),
                    new MKDungeonLayoutController(layoutSettings),
                    layoutSettings,
                    maxDepth(workspace),
                    maxDistanceFromCenter(workspace)
            ));
        }

        private static Optional<PreviewPool> buildPool(MKStructureWorkspace workspace,
                                                       MKWorkspaceExportManifest manifest,
                                                       List<String> errors) {
            Map<String, MKWorkspaceExportManifest.ExportRuntimeTemplateGroup> templateGroupByBase =
                    new LinkedHashMap<>();
            for (MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group : manifest.runtimeHints().templateGroups()) {
                templateGroupByBase.put(group.baseName(), group);
            }
            Map<String, List<MKWorkspacePieceDefinition>> piecesByBase = new LinkedHashMap<>();
            workspace.pieces().stream()
                    .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                    .forEach(piece -> piecesByBase.computeIfAbsent(baseName(piece), ignored -> new ArrayList<>())
                            .add(piece));

            ArrayList<PreviewCandidate> candidates = new ArrayList<>();
            LinkedHashMap<String, List<PreviewCandidate>> candidatesByBaseName = new LinkedHashMap<>();
            for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : piecesByBase.entrySet()) {
                MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group = templateGroupByBase.get(entry.getKey());
                if (group == null) {
                    continue;
                }
                List<MKWorkspacePieceDefinition> variants = entry.getValue().stream()
                        .filter(piece -> piece.variantIndex() > 0)
                        .sorted(Comparator.comparingInt(MKWorkspacePieceDefinition::variantIndex))
                        .toList();
                List<MKWorkspacePieceDefinition> selected = variants.isEmpty() ?
                        entry.getValue().stream()
                                .filter(piece -> piece.variantIndex() == 0)
                                .findFirst()
                                .stream()
                                .toList() :
                        variants;
                boolean templateFallback = variants.isEmpty();
                for (MKWorkspacePieceDefinition piece : selected) {
                    PreviewCandidate candidate = new PreviewCandidate(piece, templateFallback,
                            templateId(workspace, piece),
                            jigsawMetadata(group.pieceMetadata().withPieceDerivedFloorMetadata(piece)));
                    candidates.add(candidate);
                    candidatesByBaseName.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(candidate);
                }
            }
            if (candidates.isEmpty()) {
                errors.add("workspace has no runtime preview candidates after applying export runtime hints");
                return Optional.empty();
            }

            HashMap<ResourceLocation, List<PreviewCandidate>> candidatesByPool = new HashMap<>();
            ResourceLocation startPool = ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                    workspace.structureName() + "/start");
            candidatesByPool.put(startPool, candidatesByBaseName.getOrDefault(manifest.runtimeHints().startBaseName(),
                    List.of()));
            for (MKWorkspaceExportManifest.ExportRuntimePool pool : manifest.runtimeHints().pools()) {
                ArrayList<PreviewCandidate> poolCandidates = new ArrayList<>();
                for (String childBaseName : pool.childBaseNames()) {
                    poolCandidates.addAll(candidatesByBaseName.getOrDefault(childBaseName, List.of()));
                }
                candidatesByPool.put(pool.poolId(), List.copyOf(poolCandidates));
            }
            return Optional.of(new PreviewPool(manifest.runtimeHints().startBaseName(), List.copyOf(candidates),
                    immutableListMap(candidatesByBaseName), immutableListMap(candidatesByPool)));
        }

        private static ResourceLocation templateId(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
            return ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                    workspace.structureName() + "/" + piece.pieceName());
        }

        private static <K> Map<K, List<PreviewCandidate>> immutableListMap(
                Map<K, ? extends List<PreviewCandidate>> source) {
            HashMap<K, List<PreviewCandidate>> result = new HashMap<>();
            source.forEach((key, value) -> result.put(key, List.copyOf(value)));
            return Map.copyOf(result);
        }

        private static MKDungeonLayoutSettings layoutSettings(MKStructureWorkspace workspace,
                                                              List<MKDungeonTopologyGroupRule> floorRules) {
            boolean walledKeep = WALLED_KEEP_PLANNER_ID.equals(workspace.topologyProfile().plannerId());
            int maxBranchDepth = walledKeep ? 64 : Math.max(0, floorRules.stream()
                    .mapToInt(MKDungeonTopologyGroupRule::maxBranchPiecesBeforeCap)
                    .max()
                    .orElse(0));
            return new MKDungeonLayoutSettings(
                    walledKeep ? 1 : 3,
                    walledKeep ? 1 : 3,
                    1,
                    walledKeep ? 64 : 2,
                    maxBranchDepth,
                    walledKeep,
                    MKVerticalProgressionMode.MIXED,
                    true,
                    false,
                    floorRules,
                    new MKDungeonConnectorSettings(
                            connector("main_forward"),
                            connector("main_back"),
                            connector("branch"),
                            connector("connect_down"),
                            connector("connect_up"),
                            connector("boss_forward"),
                            connector("boss_back")
                    )
            );
        }

        private static int maxDepth(MKStructureWorkspace workspace) {
            return WALLED_KEEP_PLANNER_ID.equals(workspace.topologyProfile().plannerId()) ? 20 : MAX_DEPTH;
        }

        private static int maxDistanceFromCenter(MKStructureWorkspace workspace) {
            return WALLED_KEEP_PLANNER_ID.equals(workspace.topologyProfile().plannerId()) ? 116 :
                    TOWER_PLANNER_ID.equals(workspace.topologyProfile().plannerId()) ? 96 :
                            RUNTIME_SPREAD_RESERVE;
        }

        private static List<MKDungeonTopologyGroupRule> floorTopologyRules(MKWorkspaceExportManifest manifest) {
            ArrayList<MKDungeonTopologyGroupRule> rules = new ArrayList<>();
            for (MKFloorTopologySettings settings : manifest.settings().topologyProfile().floorTopologySettings()) {
                MKFloorTopologySettings physicalSettings = physicalFloorTopologySettings(manifest, settings);
                String topologyGroupId = floorTopologyGroupId(settings.stackId(), settings.floorRole());
                String endingPool = settings.mainCapApproachEnabled() ?
                        MKFloorTopologyPoolNames.mainCapApproachPoolName(topologyGroupId) :
                        MKFloorTopologyPoolNames.mainCapPoolName(topologyGroupId);
                int horizontalPadding = horizontalPadding(manifest);
                int rootWidth = manifest.settings().topologyProfile()
                        .verticalStackSettingsOrDefault(settings.stackId()).width() + horizontalPadding;
                int rootLength = manifest.settings().topologyProfile()
                        .verticalStackSettingsOrDefault(settings.stackId()).length() + horizontalPadding;
                rules.add(new MKDungeonTopologyGroupRule(
                        topologyGroupId,
                        physicalSettings.minMainPathPieces(),
                        physicalSettings.maxMainPathPieces(),
                        physicalSettings.maxBranchPiecesBeforeCap(),
                        physicalSettings.sprawl(),
                        physicalSettings.linksEnabled(),
                        physicalSettings.linkDensity(),
                        physicalSettings.maxLinksPerFloor(),
                        physicalSettings.maxLinksPerRoom(),
                        physicalSettings.maxLinkLength(),
                        physicalSettings.lockedLayoutSeed(),
                        Optional.of(physicalSettings),
                        rootWidth,
                        rootLength,
                        true,
                        ResourceLocation.fromNamespaceAndPath(manifest.namespace(),
                                manifest.structureName() + "/" + endingPool)
                ));
            }
            return List.copyOf(rules);
        }

        private static MKFloorTopologySettings physicalFloorTopologySettings(
                MKWorkspaceExportManifest manifest,
                MKFloorTopologySettings settings) {
            int horizontalPadding = horizontalPadding(manifest);
            HallwayFootprint mainHallway = hallwayFootprint(manifest, settings, true, horizontalPadding);
            HallwayFootprint branchHallway = hallwayFootprint(manifest, settings, false, horizontalPadding);
            return settings.withRoomProfiles(
                            physicalRoomProfiles(manifest, settings, settings.mainRoomProfiles(), horizontalPadding),
                            physicalRoomProfiles(manifest, settings, settings.branchRoomProfiles(), horizontalPadding),
                            physicalRoomProfiles(manifest, settings, settings.branchCapProfiles(), horizontalPadding),
                            physicalRoomProfiles(manifest, settings, settings.mainCapApproachProfiles(),
                                    horizontalPadding),
                            physicalRoomProfiles(manifest, settings, settings.mainCapProfiles(), horizontalPadding))
                    .withLayoutHallwayFootprints(mainHallway.length(), mainHallway.width(),
                            branchHallway.length(), branchHallway.width());
        }

        private static int horizontalPadding(MKWorkspaceExportManifest manifest) {
            return 2 * (manifest.settings().shellMargin() + manifest.settings().exteriorAirMargin());
        }

        private static HallwayFootprint hallwayFootprint(MKWorkspaceExportManifest manifest,
                                                         MKFloorTopologySettings settings,
                                                         boolean mainPath,
                                                         int horizontalPadding) {
            Optional<HallwayFootprint> exportedFootprint = exportedHallwayFootprint(manifest, settings, mainPath);
            if (exportedFootprint.isPresent()) {
                return exportedFootprint.orElseThrow();
            }
            String openingProfileId = floorOpeningProfileId(manifest, settings, mainPath)
                    .orElseGet(() -> firstOpeningProfileId(manifest, mainPath).orElse(""));
            Optional<MKWorkspaceExportManifest.ExportLinearRunFamily> linearRun = manifest.settings()
                    .linearRunFamilies()
                    .stream()
                    .filter(candidate -> isFloorTopologyLinearRun(candidate, mainPath))
                    .filter(candidate -> openingProfileId.isBlank() ||
                            candidate.openingProfileId().equals(openingProfileId))
                    .findFirst();
            if (linearRun.isPresent()) {
                MKWorkspaceExportManifest.ExportLinearRunFamily run = linearRun.orElseThrow();
                return new HallwayFootprint(
                        run.length() + horizontalPadding,
                        Math.max(1, run.interiorWidth() + horizontalPadding)
                );
            }
            int leadIn = settings.hallwayLeadInMode() == MKHallwayLeadInMode.MANUAL ?
                    Math.max(1, settings.manualHallwayLeadInPieces()) :
                    Math.max(1, Math.ceilDiv(Math.max(
                            manifest.settings().topologyProfile()
                                    .verticalStackSettingsOrDefault(settings.stackId()).width(),
                            manifest.settings().topologyProfile()
                                    .verticalStackSettingsOrDefault(settings.stackId()).length()), 8));
            int openingWidth = openingProfile(manifest, openingProfileId)
                    .map(MKWorkspaceExportManifest.ExportOpeningProfile::openingWidth)
                    .orElse(3);
            return new HallwayFootprint(leadIn + horizontalPadding, openingWidth + horizontalPadding);
        }

        private static Optional<HallwayFootprint> exportedHallwayFootprint(MKWorkspaceExportManifest manifest,
                                                                           MKFloorTopologySettings settings,
                                                                           boolean mainPath) {
            String pathKind = mainPath ? "main" : "branch";
            return manifest.pieces().stream()
                    .filter(piece -> "floor_plan_linear_run".equals(piece.tags().get("tower_piece_kind")))
                    .filter(piece -> settings.stackId().equals(piece.tags().get("workspace_floor_topology_stack_id")))
                    .filter(piece -> settings.floorRole().equals(piece.tags()
                            .get("workspace_floor_topology_floor_role")))
                    .filter(piece -> pathKind.equals(piece.tags().get("workspace_linear_run_path_kind")))
                    .filter(RuntimePreviewContextBuilder::baseTemplatePiece)
                    .map(piece -> new HallwayFootprint(
                            piece.placement().exportBounds().sizeX(),
                            piece.placement().exportBounds().sizeZ()))
                    .findFirst();
        }

        private static List<MKFloorRoomProfile> physicalRoomProfiles(
                MKWorkspaceExportManifest manifest,
                MKFloorTopologySettings settings,
                List<MKFloorRoomProfile> profiles,
                int horizontalPadding) {
            return profiles.stream()
                    .map(profile -> physicalRoomProfile(manifest, settings, profile, horizontalPadding))
                    .toList();
        }

        private static MKFloorRoomProfile physicalRoomProfile(MKWorkspaceExportManifest manifest,
                                                              MKFloorTopologySettings settings,
                                                              MKFloorRoomProfile profile,
                                                              int horizontalPadding) {
            return exportedRoomFootprint(manifest, settings, profile)
                    .map(footprint -> profile.withWidth(footprint.width()).withLength(footprint.length()))
                    .orElseGet(() -> profile.withWidth(profile.width() + horizontalPadding)
                            .withLength(profile.length() + horizontalPadding));
        }

        private static Optional<RoomFootprint> exportedRoomFootprint(MKWorkspaceExportManifest manifest,
                                                                     MKFloorTopologySettings settings,
                                                                     MKFloorRoomProfile profile) {
            return manifest.pieces().stream()
                    .filter(piece -> "floor_plan_room".equals(piece.tags().get("tower_piece_kind")))
                    .filter(piece -> settings.stackId().equals(piece.tags().get("workspace_floor_topology_stack_id")))
                    .filter(piece -> settings.floorRole().equals(piece.tags()
                            .get("workspace_floor_topology_floor_role")))
                    .filter(piece -> profile.id().equals(piece.tags().get("workspace_floor_room_profile_id")))
                    .filter(piece -> profile.kind().getSerializedName()
                            .equals(piece.tags().get("workspace_floor_room_kind")))
                    .filter(RuntimePreviewContextBuilder::baseTemplatePiece)
                    .map(piece -> new RoomFootprint(
                            piece.placement().exportBounds().sizeX(),
                            piece.placement().exportBounds().sizeZ()))
                    .findFirst();
        }

        private static boolean baseTemplatePiece(MKWorkspaceExportManifest.ExportPiece piece) {
            return "template".equals(piece.workspacePieceKind()) && piece.pieceName().endsWith("_template");
        }

        private static boolean isFloorTopologyLinearRun(MKWorkspaceExportManifest.ExportLinearRunFamily linearRun,
                                                        boolean mainPath) {
            if (mainPath && !linearRun.allowOnMainPath()) {
                return false;
            }
            if (!mainPath && !linearRun.allowOnBranchPath()) {
                return false;
            }
            return !linearRun.topologySlotId().startsWith("keep.");
        }

        private static Optional<String> floorOpeningProfileId(MKWorkspaceExportManifest manifest,
                                                              MKFloorTopologySettings settings,
                                                              boolean mainPath) {
            String topologySlotId = settings.stackId() + "." + settings.floorRole();
            MKHorizontalExitPathKind pathKind = mainPath ?
                    MKHorizontalExitPathKind.MAIN_EXIT :
                    MKHorizontalExitPathKind.BRANCH;
            return manifest.settings().familyDefinitions().stream()
                    .filter(family -> family.topologySlotId().equals(topologySlotId))
                    .flatMap(family -> family.horizontalExits().stream())
                    .filter(exit -> exit.pathKind() == pathKind)
                    .map(MKWorkspaceExportManifest.ExportFamilyHorizontalExit::openingProfileId)
                    .filter(id -> !id.isBlank())
                    .findFirst();
        }

        private static Optional<String> firstOpeningProfileId(MKWorkspaceExportManifest manifest, boolean mainPath) {
            return manifest.settings().openingProfiles().stream()
                    .filter(profile -> mainPath ? profile.allowOnMainPath() : profile.allowOnBranchPath())
                    .map(MKWorkspaceExportManifest.ExportOpeningProfile::profileId)
                    .findFirst();
        }

        private static Optional<MKWorkspaceExportManifest.ExportOpeningProfile> openingProfile(
                MKWorkspaceExportManifest manifest,
                String profileId) {
            return manifest.settings().openingProfiles().stream()
                    .filter(profile -> profile.profileId().equals(profileId))
                    .findFirst();
        }

        private static String floorTopologyGroupId(String stackId, String floorRole) {
            return stackId + "." + floorRole;
        }

        private static ResourceLocation connector(String name) {
            return ResourceLocation.fromNamespaceAndPath("mknpc", name);
        }

        private record HallwayFootprint(int length, int width) {
        }

        private record RoomFootprint(int width, int length) {
        }
    }

    private static Optional<ResourceLocation> branchCapPoolFor(ResourceLocation targetPool) {
        String path = targetPool.getPath();
        Optional<String> openingProfile = branchOpeningProfile(path, "linear_runs/branch/")
                .or(() -> branchOpeningProfile(path, "rooms/branch/"));
        if (openingProfile.isEmpty()) {
            return Optional.empty();
        }
        int markerIndex = path.indexOf("linear_runs/branch/");
        if (markerIndex < 0) {
            markerIndex = path.indexOf("rooms/branch/");
        }
        String prefix = markerIndex <= 0 ? "" : path.substring(0, markerIndex);
        return Optional.of(ResourceLocation.fromNamespaceAndPath(targetPool.getNamespace(),
                prefix + "branch_caps/" + openingProfile.get()));
    }

    private static Optional<String> branchOpeningProfile(String path, String marker) {
        int markerIndex = path.indexOf(marker);
        if (markerIndex < 0) {
            return Optional.empty();
        }
        String openingProfile = path.substring(markerIndex + marker.length());
        int slashIndex = openingProfile.indexOf('/');
        if (slashIndex >= 0) {
            openingProfile = openingProfile.substring(0, slashIndex);
        }
        return openingProfile.isBlank() ? Optional.empty() : Optional.of(openingProfile);
    }
}
