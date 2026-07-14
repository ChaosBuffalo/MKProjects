package com.chaosbuffalo.mkworkspace.world.gen.workspace.preview;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonConnectorSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonTopologyGroupRule;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceMetadataManager;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPlacement;
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
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKFloorMaskVariantExporter;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorMaskPools;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class MKWorkspaceSamplePreviewService {
    private static final ResourceLocation WALLED_KEEP_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc", "walled_keep");
    private static final ResourceLocation TOWER_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    private static final int SAMPLE_GAP = 16;
    private static final int CLEAR_MARGIN = 2;
    private static final int RUNTIME_SPREAD_RESERVE = 128;
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

    private record RuntimePreviewContext(PreviewPool pool,
                                         MKDungeonLayoutSettings layoutSettings,
                                         int maxDepth,
                                         int maxDistanceFromCenter) {
    }

    private record DynamicPreviewPools(Holder<StructureTemplatePool> startPool,
                                       Holder<StructureTemplatePool> emptyPool,
                                       Map<ResourceKey<StructureTemplatePool>, Holder<StructureTemplatePool>> pools,
                                       Map<ResourceLocation, PreviewCandidate> candidatesByTemplateId,
                                       Map<ResourceLocation, MKJigsawPieceMetadata> metadataOverrides) {
        Optional<? extends Holder<StructureTemplatePool>> getHolder(ResourceKey<StructureTemplatePool> poolKey) {
            if (Pools.EMPTY.equals(poolKey)) {
                return Optional.of(emptyPool);
            }
            return Optional.ofNullable(pools.get(poolKey));
        }
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

        DynamicPreviewPools dynamicPools = buildDynamicPools(level, workspace, runtime, errors).orElse(null);
        if (dynamicPools == null) {
            return Optional.empty();
        }

        List<String> warnings = new ArrayList<>();
        BlockPos sampleCenter = sampleCenter(workspace.anchor(), runtime.maxDistanceFromCenter());
        MKJigsawPieceMetadataManager.MetadataOverrideSnapshot metadataSnapshot =
                MKJigsawPieceMetadataManager.installTemporaryPreviewOverrides(dynamicPools.metadataOverrides());
        MKJigsawPlacement.PreviewPlan plan;
        try {
            plan = MKJigsawPlacement.planPreviewPieces(
                    level.getStructureManager(),
                    level,
                    level.getChunkSource().getGenerator(),
                    level.getChunkSource().randomState(),
                    random,
                    dynamicPools.startPool(),
                    Optional.empty(),
                    runtime.maxDepth(),
                    sampleCenter,
                    true,
                    Optional.empty(),
                    runtime.maxDistanceFromCenter(),
                    PoolAliasLookup.create(List.of(), sampleCenter, seed),
                    JigsawStructure.DEFAULT_DIMENSION_PADDING,
                    LiquidSettings.IGNORE_WATERLOGGING,
                    runtime.layoutSettings(),
                    dynamicPools::getHolder
            ).orElse(null);
        } finally {
            MKJigsawPieceMetadataManager.restoreTemporaryPreviewOverrides(metadataSnapshot);
        }
        if (plan == null || plan.pieces().isEmpty()) {
            errors.add("preview runtime planner produced no pieces");
            return Optional.empty();
        }
        BoundingBox finalBounds = unionPieceBounds(plan.pieces());
        int yOffset = sampleCenter.getY() - finalBounds.minY();
        if (yOffset != 0) {
            plan.pieces().forEach(piece -> piece.move(0, yOffset, 0));
            finalBounds = unionPieceBounds(plan.pieces());
        }
        BoundingBox authoringBounds = authoringBounds(workspace);
        if (authoringBounds == null) {
            errors.add("workspace has no authoring bounds");
            return Optional.empty();
        }
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
        for (PoolElementStructurePiece placedPiece : plan.pieces()) {
            Optional<PreviewCandidate> candidate = previewCandidate(placedPiece, dynamicPools);
            if (candidate.isEmpty()) {
                warnings.add("preview skipped piece with unknown template id " + placedPiece.getElement());
                continue;
            }
            if (candidate.orElseThrow().templateFallback()) {
                fallbackCount++;
            }
            copyPiece(level, candidate.orElseThrow().piece(), placedPiece.getPosition(),
                    placedPiece.getRotation());
        }
        ArrayList<StructurePiece> structurePieces = new ArrayList<>(plan.pieces());
        runAfterPlace(level, workspace, runtime, new PiecesContainer(structurePieces),
                finalBounds, random, dynamicPools.metadataOverrides());

        BlockPos origin = new BlockPos(finalBounds.minX(), finalBounds.minY(), finalBounds.minZ());
        MKWorkspaceSamplePreviewState state = new MKWorkspaceSamplePreviewState(
                origin,
                finalBounds,
                seed,
                lockSeed,
                System.currentTimeMillis(),
                plan.pieces().size(),
                fallbackCount
        );
        data.setSamplePreviewState(workspace.id(), state);
        return Optional.of(new MKWorkspaceSamplePreviewResult(origin, finalBounds, seed, lockSeed, plan.pieces().size(),
                fallbackCount, List.copyOf(warnings)));
    }

    private long resolveSeed(ServerLevel level, Optional<MKWorkspaceSamplePreviewState> previousState,
                             boolean lockSeed) {
        if (lockSeed && previousState.isPresent()) {
            return previousState.get().seed();
        }
        return level.random.nextLong();
    }

    private Optional<DynamicPreviewPools> buildDynamicPools(ServerLevel level, MKStructureWorkspace workspace,
                                                           RuntimePreviewContext runtime, List<String> errors) {
        Registry<StructureTemplatePool> poolRegistry = level.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> emptyPool = poolRegistry.getHolder(Pools.EMPTY).orElse(null);
        if (emptyPool == null) {
            errors.add("minecraft empty template pool was not available");
            return Optional.empty();
        }

        HashMap<ResourceLocation, StructureTemplate> templatesById = new HashMap<>();
        HashMap<ResourceLocation, PreviewCandidate> candidatesByTemplateId = new HashMap<>();
        HashMap<ResourceLocation, MKJigsawPieceMetadata> metadataOverrides = new HashMap<>();
        for (PreviewCandidate candidate : runtime.pool().candidates()) {
            templatesById.computeIfAbsent(candidate.templateId(), ignored -> captureTemplate(level,
                    candidate.piece()));
            candidatesByTemplateId.put(candidate.templateId(), candidate);
            metadataOverrides.put(candidate.templateId(), candidate.metadata());
        }

        HashMap<ResourceKey<StructureTemplatePool>, Holder<StructureTemplatePool>> pools = new HashMap<>();
        for (Map.Entry<ResourceLocation, List<PreviewCandidate>> entry : runtime.pool().candidatesByPool().entrySet()) {
            ResourceKey<StructureTemplatePool> key = ResourceKey.create(Registries.TEMPLATE_POOL, entry.getKey());
            pools.put(key, Holder.direct(rigidPool(emptyPool, entry.getValue(), templatesById)));
        }

        ResourceLocation startPoolId = ResourceLocation.fromNamespaceAndPath(workspace.namespace(),
                workspace.structureName() + "/start");
        ResourceKey<StructureTemplatePool> startPoolKey = ResourceKey.create(Registries.TEMPLATE_POOL, startPoolId);
        Holder<StructureTemplatePool> startPool = pools.get(startPoolKey);
        if (startPool == null || startPool.value().size() == 0) {
            errors.add("workspace did not define a runtime start piece and no preview start fallback was available");
            return Optional.empty();
        }

        return Optional.of(new DynamicPreviewPools(startPool, emptyPool, Map.copyOf(pools),
                Map.copyOf(candidatesByTemplateId), Map.copyOf(metadataOverrides)));
    }

    private StructureTemplate captureTemplate(ServerLevel level, MKWorkspacePieceDefinition piece) {
        StructureTemplate template = new StructureTemplate();
        BoundingBox bounds = piece.exportBounds();
        template.fillFromWorld(level, new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ()),
                new Vec3i(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan()), false, null);
        removeInactiveJigsaws(level, template, piece);
        return template;
    }

    private void removeInactiveJigsaws(ServerLevel level, StructureTemplate template,
                                       MKWorkspacePieceDefinition piece) {
        Set<BlockPos> activeConnectorPositions = new HashSet<>();
        for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
            activeConnectorPositions.add(connector.relativePos());
        }
        CompoundTag tag = template.save(new CompoundTag());
        ListTag blocks = tag.getList("blocks", 10);
        ListTag filtered = new ListTag();
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag block = blocks.getCompound(i);
            if (!inactiveJigsaw(block, activeConnectorPositions)) {
                filtered.add(block);
            }
        }
        if (filtered.size() == blocks.size()) {
            return;
        }
        tag.put("blocks", filtered);
        template.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
    }

    private boolean inactiveJigsaw(CompoundTag block, Set<BlockPos> activeConnectorPositions) {
        if (!block.contains("nbt")) {
            return false;
        }
        CompoundTag nbt = block.getCompound("nbt");
        if (!"minecraft:jigsaw".equals(nbt.getString("id"))) {
            return false;
        }
        ListTag pos = block.getList("pos", 3);
        BlockPos relativePos = new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2));
        return !activeConnectorPositions.contains(relativePos);
    }

    private StructureTemplatePool rigidPool(Holder<StructureTemplatePool> emptyPool,
                                            List<PreviewCandidate> candidates,
                                            Map<ResourceLocation, StructureTemplate> templatesById) {
        ArrayList<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> entries =
                new ArrayList<>();
        for (PreviewCandidate candidate : candidates) {
            StructureTemplate template = templatesById.get(candidate.templateId());
            if (template == null) {
                continue;
            }
            entries.add(Pair.of(MKSinglePoolElement.forTemplate(candidate.templateId(), template, false),
                    templateWeight(candidate.piece())));
        }
        if (entries.isEmpty()) {
            entries.add(Pair.of(StructurePoolElement.empty(), 1));
        }
        return new StructureTemplatePool(emptyPool, entries, StructureTemplatePool.Projection.RIGID);
    }

    private int templateWeight(MKWorkspacePieceDefinition piece) {
        String weight = piece.tags().get(MKFloorMaskPools.FLOOR_MASK_WEIGHT_TAG);
        if (weight == null || weight.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(weight));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private Optional<PreviewCandidate> previewCandidate(PoolElementStructurePiece piece,
                                                        DynamicPreviewPools dynamicPools) {
        return templateId(piece.getElement()).map(dynamicPools.candidatesByTemplateId()::get);
    }

    private Optional<ResourceLocation> templateId(StructurePoolElement element) {
        if (element instanceof MKSinglePoolElement mkSinglePoolElement) {
            return mkSinglePoolElement.getTemplateId();
        }
        return Optional.empty();
    }

    private BlockPos rotatedRelative(BlockPos relative, MKWorkspacePieceDefinition piece, Rotation rotation) {
        BoundingBox bounds = piece.exportBounds();
        int maxX = bounds.getXSpan() - 1;
        int maxZ = bounds.getZSpan() - 1;
        return switch (rotation) {
            case NONE -> relative;
            case CLOCKWISE_90 -> new BlockPos(maxZ - relative.getZ(), relative.getY(), relative.getX());
            case CLOCKWISE_180 -> new BlockPos(maxX - relative.getX(), relative.getY(), maxZ - relative.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(relative.getZ(), relative.getY(), maxX - relative.getX());
        };
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

    private void copyPiece(ServerLevel level, MKWorkspacePieceDefinition sourcePiece, BlockPos destinationOrigin,
                           Rotation rotation) {
        BoundingBox sourceBounds = sourcePiece.exportBounds();
        for (int x = 0; x < sourceBounds.getXSpan(); x++) {
            for (int y = 0; y < sourceBounds.getYSpan(); y++) {
                for (int z = 0; z < sourceBounds.getZSpan(); z++) {
                    BlockPos sourcePos = new BlockPos(sourceBounds.minX() + x, sourceBounds.minY() + y,
                            sourceBounds.minZ() + z);
                    BlockPos destPos = destinationOrigin.offset(rotatedRelative(new BlockPos(x, y, z), sourcePiece,
                            rotation));
                    BlockState state = level.getBlockState(sourcePos);
                    if (state.is(Blocks.JIGSAW) || state.is(Blocks.STRUCTURE_VOID) ||
                            state.is(Blocks.STRUCTURE_BLOCK)) {
                        level.setBlock(destPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        continue;
                    }
                    BlockState rotatedState = state.rotate(rotation);
                    level.setBlock(destPos, rotatedState, Block.UPDATE_ALL);
                    copyBlockEntity(level, sourcePos, destPos, rotatedState);
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
                               PiecesContainer pieces, BoundingBox finalBounds, RandomSource random,
                               Map<ResourceLocation, MKJigsawPieceMetadata> metadataOverrides) {
        MKJigsawPieceMetadataManager.MetadataOverrideSnapshot snapshot =
                MKJigsawPieceMetadataManager.installTemporaryPreviewOverrides(metadataOverrides);
        try {
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

    private BoundingBox unionPieceBounds(List<PoolElementStructurePiece> pieces) {
        BoundingBox bounds = null;
        for (PoolElementStructurePiece piece : pieces) {
            bounds = merge(bounds, piece.getBoundingBox());
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

    private record PreviewPool(List<PreviewCandidate> candidates,
                               Map<ResourceLocation, List<PreviewCandidate>> candidatesByPool) {
    }

    private static final class RuntimePreviewContextBuilder {
        private RuntimePreviewContextBuilder() {
        }

        static Optional<RuntimePreviewContext> from(MKStructureWorkspace workspace, List<String> errors) {
            List<MKWorkspacePieceDefinition> previewPieces = MKFloorMaskVariantExporter.exportPieces(workspace,
                    true);
            MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 4,
                    Instant.now().toString());
            MKWorkspaceExportManifest.ExportRuntimeHints previewHints =
                    MKWorkspaceExportManifest.ExportRuntimeHints.forWorkspacePreview(workspace, previewPieces);
            Optional<PreviewPool> pool = buildPool(workspace, previewPieces, previewHints, errors);
            if (pool.isEmpty()) {
                return Optional.empty();
            }
            List<MKDungeonTopologyGroupRule> floorRules = floorTopologyRules(manifest);
            MKDungeonLayoutSettings layoutSettings = layoutSettings(workspace, floorRules);
            return Optional.of(new RuntimePreviewContext(
                    pool.orElseThrow(),
                    layoutSettings,
                    maxDepth(workspace),
                    maxDistanceFromCenter(workspace)
            ));
        }

        private static Optional<PreviewPool> buildPool(MKStructureWorkspace workspace,
                                                       List<MKWorkspacePieceDefinition> previewPieces,
                                                       MKWorkspaceExportManifest.ExportRuntimeHints previewHints,
                                                       List<String> errors) {
            Map<String, MKWorkspaceExportManifest.ExportRuntimeTemplateGroup> templateGroupByBase =
                    new LinkedHashMap<>();
            for (MKWorkspaceExportManifest.ExportRuntimeTemplateGroup group : previewHints.templateGroups()) {
                templateGroupByBase.put(group.baseName(), group);
            }
            Map<String, List<MKWorkspacePieceDefinition>> piecesByBase = new LinkedHashMap<>();
            previewPieces.stream()
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
            candidatesByPool.put(startPool, candidatesByBaseName.getOrDefault(previewHints.startBaseName(),
                    List.of()));
            for (MKWorkspaceExportManifest.ExportRuntimePool pool : previewHints.pools()) {
                ArrayList<PreviewCandidate> poolCandidates = new ArrayList<>();
                for (String childBaseName : pool.childBaseNames()) {
                    poolCandidates.addAll(candidatesByBaseName.getOrDefault(childBaseName, List.of()));
                }
                candidatesByPool.put(pool.poolId(), List.copyOf(poolCandidates));
            }
            return Optional.of(new PreviewPool(List.copyOf(candidates), immutableListMap(candidatesByPool)));
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

}
