package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MKJigsawStructure extends MKStructure {

    public static final MapCodec<MKJigsawStructure> CODEC = RecordCodecBuilder.<MKJigsawStructure>mapCodec(builder ->
            builder.group(settingsCodec(builder),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
                            .forGetter(s -> Optional.ofNullable(s.startJigsawName)),
                    Codec.intRange(0, 20).fieldOf("size")
                            .forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack")
                            .forGetter(s -> s.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> Optional.ofNullable(s.projectStartToHeightmap)),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center")
                            .forGetter(s -> s.maxDistanceFromCenter),
                    Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(p_307187_ -> p_307187_.poolAliases),
                    DimensionPadding.CODEC
                            .optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING)
                            .forGetter(p_348455_ -> p_348455_.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(p_352036_ -> p_352036_.liquidSettings),
                    MKDungeonLayoutSettings.CODEC.optionalFieldOf("dungeon_layout")
                            .forGetter(s -> Optional.ofNullable(s.dungeonLayout)),
                    CompoundTag.CODEC.fieldOf("structure_events")
                            .forGetter(MKJigsawStructure::getNbt),
                    Codec.BOOL.fieldOf("fill_floor").forGetter(s -> s.fillFloor),
                    BlockState.CODEC.optionalFieldOf("fill_state").forGetter(s -> Optional.ofNullable(s.fillState))
            ).apply(builder, (settings, startPool, startJigsawName, maxDepth, startHeight, useExpansionHack,
                              projectStartToHeightmap, maxDistanceFromCenter, poolAliases, dimensionPadding,
                              liquidSettings, dungeonLayout, structureEvents, fillFloor, fillState) ->
                    new MKJigsawStructure(settings, startPool, startJigsawName.orElse(null), maxDepth, startHeight,
                            useExpansionHack, projectStartToHeightmap.orElse(null), maxDistanceFromCenter,
                            poolAliases, dimensionPadding, liquidSettings, dungeonLayout.orElse(null),
                            structureEvents, fillFloor, fillState))).flatXmap(verifyRange(), verifyRange());

    private static Function<MKJigsawStructure, DataResult<MKJigsawStructure>> verifyRange() {
        return structure -> {
            int i = switch (structure.terrainAdaptation()) {
                case NONE -> 0;
                case BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE -> 12;
            };
            return structure.maxDistanceFromCenter + i > 128 ?
                    DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128") :
                    DataResult.success(structure);
        };
    }

    private final Holder<StructureTemplatePool> startPool;
    @Nullable
    private final ResourceLocation startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    @Nullable
    private final Heightmap.Types projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;
    @Nullable
    private final MKDungeonLayoutSettings dungeonLayout;
    private final boolean fillFloor;
    @Nullable
    private final BlockState fillState;

    public MKJigsawStructure(StructureSettings pSettings, Holder<StructureTemplatePool> templatePool,
                             @Nullable ResourceLocation startJigsawName, int maxDepth, HeightProvider heightProvider,
                             boolean useExpansionHack, @Nullable Heightmap.Types heightmapTypes, int maxDistanceFromCenter,
                             List<PoolAliasBinding> poolAliases,
                             DimensionPadding dimensionPadding,
                             LiquidSettings liquidSettings,
                             @Nullable MKDungeonLayoutSettings dungeonLayout,
                             CompoundTag structureNbt,
                             boolean fillFloor,
                             Optional<BlockState> fillState) {
        super(pSettings, structureNbt);
        this.startPool = templatePool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = heightProvider;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = heightmapTypes;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
        this.dungeonLayout = dungeonLayout;
        this.fillFloor = fillFloor;
        this.fillState = fillState.orElse(null);
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer pieces) {
        if (fillFloor && fillState != null) {
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            int minHeight = level.getMinBuildHeight();
            BoundingBox boundingbox = pieces.calculateBoundingBox();
            int boundingMin = boundingbox.minY();

            for(int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
                for(int l = boundingBox.minZ(); l <= boundingBox.maxZ(); ++l) {
                    blockPos.set(k, boundingMin, l);
                    if (!level.isEmptyBlock(blockPos) && boundingbox.isInside(blockPos) && pieces.isInsidePiece(blockPos)) {
                        for(int i1 = boundingMin - 1; i1 > minHeight; --i1) {
                            blockPos.setY(i1);
                            if (!level.isEmptyBlock(blockPos) && !level.getBlockState(blockPos).liquid()) {
                                break;
                            }
                            level.setBlock(blockPos, fillState, 2);
                        }
                    }
                }
            }
        }
        applyFloorLinks(level, boundingBox, pieces, random);
        applyPieceFoundations(level, boundingBox, pieces);
    }

    private void applyFloorLinks(WorldGenLevel level, BoundingBox chunkBounds, PiecesContainer pieces,
                                 RandomSource random) {
        if (dungeonLayout == null) {
            return;
        }
        List<PlacedLinkEndpoint> endpoints = collectFloorLinkEndpoints(pieces);
        if (endpoints.size() < 2) {
            return;
        }
        Map<String, List<PlacedLinkEndpoint>> byTopologyGroup = new HashMap<>();
        for (PlacedLinkEndpoint endpoint : endpoints) {
            byTopologyGroup.computeIfAbsent(endpoint.topologyGroup(), key -> new ArrayList<>()).add(endpoint);
        }
        for (Map.Entry<String, List<PlacedLinkEndpoint>> entry : byTopologyGroup.entrySet()) {
            Optional<MKDungeonTopologyGroupRule> ruleOpt = dungeonLayout.topologyGroupRule(entry.getKey());
            if (ruleOpt.isEmpty() || !ruleOpt.get().linksEnabled()) {
                continue;
            }
            applyFloorLinksForGroup(level, chunkBounds, pieces, entry.getValue(), ruleOpt.get(), random);
        }
    }

    private List<PlacedLinkEndpoint> collectFloorLinkEndpoints(PiecesContainer pieces) {
        ArrayList<PlacedLinkEndpoint> endpoints = new ArrayList<>();
        int pieceIndex = 0;
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) {
                continue;
            }
            Optional<ResourceLocation> templateId = getTemplateId(poolPiece.getElement());
            if (templateId.isEmpty()) {
                continue;
            }
            Optional<MKJigsawPieceMetadata> metadataOpt = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadataOpt.isEmpty()) {
                continue;
            }
            MKJigsawPieceMetadata metadata = metadataOpt.get();
            if (metadata.topologyGroup().isBlank() || metadata.floorLinkCandidates().isEmpty()) {
                continue;
            }
            for (MKJigsawPieceMetadata.FloorLinkCandidate candidate : metadata.floorLinkCandidates()) {
                BlockPos relative = new BlockPos(candidate.x(), candidate.y(), candidate.z());
                BlockPos rotated = StructureTemplate.transform(relative, Mirror.NONE, poolPiece.getRotation(), BlockPos.ZERO);
                BlockPos worldPos = poolPiece.getPosition().offset(rotated);
                Direction facing = poolPiece.getRotation().rotate(candidate.facing());
                endpoints.add(new PlacedLinkEndpoint(pieceIndex, metadata.topologyGroup(), worldPos, facing,
                        candidate.openingWidth(), candidate.openingHeight(), poolPiece.getBoundingBox()));
            }
            pieceIndex++;
        }
        return List.copyOf(endpoints);
    }

    private void applyFloorLinksForGroup(WorldGenLevel level, BoundingBox chunkBounds, PiecesContainer pieces,
                                         List<PlacedLinkEndpoint> endpoints, MKDungeonTopologyGroupRule rule,
                                         RandomSource random) {
        ArrayList<LinkCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < endpoints.size(); i++) {
            for (int j = i + 1; j < endpoints.size(); j++) {
                PlacedLinkEndpoint a = endpoints.get(i);
                PlacedLinkEndpoint b = endpoints.get(j);
                if (a.pieceIndex() == b.pieceIndex()) {
                    continue;
                }
                Optional<LinkRoute> route = routeBetween(a, b, rule.maxLinkLength(), pieces);
                route.ifPresent(linkRoute -> candidates.add(new LinkCandidate(a, b, linkRoute)));
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));
        Map<Integer, Integer> linksByPiece = new HashMap<>();
        int placed = 0;
        for (LinkCandidate candidate : candidates) {
            if (placed >= rule.maxLinksPerFloor()) {
                return;
            }
            if (linksByPiece.getOrDefault(candidate.a().pieceIndex(), 0) >= rule.maxLinksPerRoom() ||
                    linksByPiece.getOrDefault(candidate.b().pieceIndex(), 0) >= rule.maxLinksPerRoom()) {
                continue;
            }
            if (random.nextFloat() > rule.linkDensity()) {
                continue;
            }
            carveLink(level, chunkBounds, candidate);
            linksByPiece.merge(candidate.a().pieceIndex(), 1, Integer::sum);
            linksByPiece.merge(candidate.b().pieceIndex(), 1, Integer::sum);
            placed++;
        }
    }

    private Optional<LinkRoute> routeBetween(PlacedLinkEndpoint a, PlacedLinkEndpoint b, int maxLength,
                                             PiecesContainer pieces) {
        if (a.pos().getY() != b.pos().getY()) {
            return Optional.empty();
        }
        BlockPos start = a.pos().relative(a.facing());
        BlockPos end = b.pos().relative(b.facing());
        ArrayList<List<BlockPos>> routes = new ArrayList<>();
        if (start.getX() == end.getX() || start.getZ() == end.getZ()) {
            routes.add(line(start, end));
        }
        routes.add(dogleg(start, end, true));
        routes.add(dogleg(start, end, false));
        for (List<BlockPos> route : routes) {
            if (route.isEmpty() || route.size() > maxLength) {
                continue;
            }
            if (!firstStepMatches(route, a.facing()) || !lastStepMatches(route, b.facing().getOpposite())) {
                continue;
            }
            if (routeIntersectsPieces(route, pieces, a, b)) {
                continue;
            }
            return Optional.of(new LinkRoute(route));
        }
        return Optional.empty();
    }

    private List<BlockPos> dogleg(BlockPos start, BlockPos end, boolean xThenZ) {
        BlockPos bend = xThenZ ? new BlockPos(end.getX(), start.getY(), start.getZ()) :
                new BlockPos(start.getX(), start.getY(), end.getZ());
        ArrayList<BlockPos> route = new ArrayList<>(line(start, bend));
        List<BlockPos> second = line(bend, end);
        if (!route.isEmpty() && !second.isEmpty()) {
            route.addAll(second.subList(1, second.size()));
        } else {
            route.addAll(second);
        }
        return List.copyOf(route);
    }

    private List<BlockPos> line(BlockPos start, BlockPos end) {
        if (start.getY() != end.getY()) {
            return List.of();
        }
        if (start.getX() != end.getX() && start.getZ() != end.getZ()) {
            return List.of();
        }
        ArrayList<BlockPos> route = new ArrayList<>();
        int dx = Integer.compare(end.getX(), start.getX());
        int dz = Integer.compare(end.getZ(), start.getZ());
        BlockPos current = start;
        route.add(current);
        while (!current.equals(end)) {
            current = current.offset(dx, 0, dz);
            route.add(current);
        }
        return List.copyOf(route);
    }

    private boolean firstStepMatches(List<BlockPos> route, Direction facing) {
        if (route.size() < 2) {
            return false;
        }
        return directionBetween(route.get(0), route.get(1)) == facing;
    }

    private boolean lastStepMatches(List<BlockPos> route, Direction facing) {
        if (route.size() < 2) {
            return false;
        }
        return directionBetween(route.get(route.size() - 2), route.getLast()) == facing;
    }

    private Direction directionBetween(BlockPos from, BlockPos to) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dz = Integer.compare(to.getZ(), from.getZ());
        if (dx > 0) {
            return Direction.EAST;
        } else if (dx < 0) {
            return Direction.WEST;
        } else if (dz > 0) {
            return Direction.SOUTH;
        }
        return Direction.NORTH;
    }

    private boolean routeIntersectsPieces(List<BlockPos> route, PiecesContainer pieces,
                                          PlacedLinkEndpoint a, PlacedLinkEndpoint b) {
        for (BlockPos pos : route) {
            for (StructurePiece piece : pieces.pieces()) {
                if (piece.getBoundingBox().equals(a.pieceBox()) || piece.getBoundingBox().equals(b.pieceBox())) {
                    continue;
                }
                if (piece.getBoundingBox().isInside(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void carveLink(WorldGenLevel level, BoundingBox chunkBounds, LinkCandidate candidate) {
        int height = Math.max(2, Math.min(candidate.a().openingHeight(), candidate.b().openingHeight()));
        openEndpoint(level, chunkBounds, candidate.a());
        openEndpoint(level, chunkBounds, candidate.b());
        for (BlockPos center : candidate.route().positions()) {
            carveCorridorCell(level, chunkBounds, center, height);
        }
    }

    private void openEndpoint(WorldGenLevel level, BoundingBox chunkBounds, PlacedLinkEndpoint endpoint) {
        int minAcross = -(endpoint.openingWidth() / 2);
        int maxAcross = minAcross + endpoint.openingWidth() - 1;
        Direction.Axis acrossAxis = endpoint.facing().getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int across = minAcross; across <= maxAcross; across++) {
            for (int y = 0; y < endpoint.openingHeight(); y++) {
                BlockPos pos = acrossAxis == Direction.Axis.X ?
                        endpoint.pos().offset(across, y, 0) :
                        endpoint.pos().offset(0, y, across);
                setIfInChunk(level, chunkBounds, pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void carveCorridorCell(WorldGenLevel level, BoundingBox chunkBounds, BlockPos center, int height) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floor = center.offset(dx, -1, dz);
                setIfInChunk(level, chunkBounds, floor, Blocks.STONE.defaultBlockState());
                for (int y = 0; y < height; y++) {
                    setIfInChunk(level, chunkBounds, center.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
                }
                setIfInChunk(level, chunkBounds, center.offset(dx, height, dz), Blocks.STONE.defaultBlockState());
            }
        }
    }

    private void setIfInChunk(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos, BlockState state) {
        if (chunkBounds.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private record PlacedLinkEndpoint(
            int pieceIndex,
            String topologyGroup,
            BlockPos pos,
            Direction facing,
            int openingWidth,
            int openingHeight,
            BoundingBox pieceBox
    ) {
    }

    private record LinkRoute(List<BlockPos> positions) {
    }

    private record LinkCandidate(PlacedLinkEndpoint a, PlacedLinkEndpoint b, LinkRoute route) {
    }

    private void applyPieceFoundations(WorldGenLevel level, BoundingBox chunkBounds, PiecesContainer pieces) {
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) {
                continue;
            }
            Optional<ResourceLocation> templateId = getTemplateId(poolPiece.getElement());
            if (templateId.isEmpty()) {
                continue;
            }
            Optional<MKJigsawPieceMetadata> metadataOpt = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadataOpt.isEmpty()) {
                continue;
            }
            MKWorkspaceFoundationPolicy policy = metadataOpt.get().foundationPolicy();
            if (!policy.enabled()) {
                continue;
            }
            fillPieceFoundation(level, chunkBounds, poolPiece.getBoundingBox(), policy);
        }
    }

    private void fillPieceFoundation(WorldGenLevel level, BoundingBox chunkBounds, BoundingBox pieceBox,
                                     MKWorkspaceFoundationPolicy policy) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        int minHeight = level.getMinBuildHeight();
        int minX = Math.max(chunkBounds.minX(), pieceBox.minX());
        int maxX = Math.min(chunkBounds.maxX(), pieceBox.maxX());
        int minZ = Math.max(chunkBounds.minZ(), pieceBox.minZ());
        int maxZ = Math.min(chunkBounds.maxZ(), pieceBox.maxZ());
        int bottomY = pieceBox.minY();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                blockPos.set(x, bottomY, z);
                BlockState bottomState = level.getBlockState(blockPos);
                if (level.isEmptyBlock(blockPos) || bottomState.liquid()) {
                    continue;
                }
                BlockState fillStateForColumn = resolveFoundationFillState(policy, bottomState).orElse(null);
                if (fillStateForColumn == null) {
                    continue;
                }
                for (int y = bottomY - 1; y >= minHeight; y--) {
                    blockPos.setY(y);
                    BlockState targetState = level.getBlockState(blockPos);
                    if (!level.isEmptyBlock(blockPos) && !targetState.liquid()) {
                        break;
                    }
                    level.setBlock(blockPos, fillStateForColumn, 2);
                }
            }
        }
    }

    private Optional<BlockState> resolveFoundationFillState(MKWorkspaceFoundationPolicy policy, BlockState bottomState) {
        return switch (policy.mode()) {
            case NONE -> Optional.empty();
            case UNIFORM_STATE -> policy.foundationBlockOpt()
                    .flatMap(blockId -> BuiltInRegistries.BLOCK.getOptional(blockId))
                    .map(block -> block.defaultBlockState());
            case EXTEND_BOTTOM_BLOCKS -> Optional.of(bottomState);
            case MASKED_EXTEND_BOTTOM_BLOCKS -> {
                ResourceLocation bottomBlockId = BuiltInRegistries.BLOCK.getKey(bottomState.getBlock());
                yield policy.maskBlocks().contains(bottomBlockId) ? Optional.of(bottomState) : Optional.empty();
            }
        };
    }

    private Optional<ResourceLocation> getTemplateId(StructurePoolElement element) {
        if (element instanceof MKSinglePoolElement mkSinglePoolElement) {
            return mkSinglePoolElement.getPieceEither().left();
        }
        return Optional.empty();
    }

    @Override
    public StructureType<?> type() {
        return MKNpcWorldGen.MK_STRUCTURE_TYPE.get();
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
        ChunkPos chunkpos = pContext.chunkPos();
        int startY = this.startHeight.sample(pContext.random(), new WorldGenerationContext(pContext.chunkGenerator(), pContext.heightAccessor()));
        BlockPos startPos = new BlockPos(chunkpos.getMinBlockX(), startY, chunkpos.getMinBlockZ());
        PoolAliasLookup aliasLookup = PoolAliasLookup.create(this.poolAliases, startPos, pContext.seed());
        Optional<ResourceLocation> startJigsawNameOpt = Optional.ofNullable(startJigsawName);
        Optional<Heightmap.Types> projectStartToHeightmapOpt = Optional.ofNullable(projectStartToHeightmap);
        if (dungeonLayout != null) {
            return MKJigsawPlacement.addPieces(pContext, startPool, startJigsawNameOpt, maxDepth, startPos,
                    useExpansionHack, projectStartToHeightmapOpt, maxDistanceFromCenter, aliasLookup,
                    this.dimensionPadding, this.liquidSettings, dungeonLayout);
        }
        return net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement.addPieces(pContext, startPool, startJigsawNameOpt, maxDepth, startPos,
                useExpansionHack, projectStartToHeightmapOpt, maxDistanceFromCenter, aliasLookup,
                this.dimensionPadding, this.liquidSettings);
    }
}
