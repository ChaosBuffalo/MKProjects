package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorLinkGenerationMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorLayoutSolver;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

public class MKJigsawStructure extends MKStructure {
    private static final BlockState TEMP_LINK_MARKER_STATE = Blocks.YELLOW_WOOL.defaultBlockState();
    private static final BlockIgnoreProcessor STRUCTURE_VOID_IGNORE =
            new BlockIgnoreProcessor(List.of(Blocks.STRUCTURE_VOID));

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
        ResolvedFloorLinks solverFloorLinks = resolveSolverFloorLinks(pieces);
        closeUnconnectedFloorOpenings(level, boundingBox, pieces, solverFloorLinks.linkedOpenings());
        applyPieceFoundations(level, boundingBox, pieces);
        // Link carving must run last so generic opening closure and foundations cannot overwrite planned corridors.
        carveSolverFloorLinks(level, boundingBox, solverFloorLinks);
    }

    private ResolvedFloorLinks resolveSolverFloorLinks(PiecesContainer pieces) {
        if (dungeonLayout == null) {
            return ResolvedFloorLinks.EMPTY;
        }
        HashSet<BlockPos> linkedOpenings = new HashSet<>();
        ArrayList<LinkCandidate> candidates = new ArrayList<>();
        for (MKDungeonTopologyGroupRule rule : dungeonLayout.topologyGroupRules()) {
            if (rule.floorTopologySettings().isEmpty() || !rule.linksEnabled()) {
                continue;
            }
            Optional<FloorRootPiece> rootOpt = floorRootPiece(rule, pieces);
            if (rootOpt.isEmpty()) {
                if (MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("solver floor link skipped group={} reason=missing_root_metadata",
                            rule.topologyGroup());
                }
                continue;
            }
            FloorRootPiece root = rootOpt.orElseThrow();
            List<MKFamilyHorizontalExitDefinition> rootExits = rootExits(root, rule.topologyGroup());
            if (rootExits.isEmpty()) {
                if (MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("solver floor link skipped group={} reason=missing_root_exits",
                            rule.topologyGroup());
                }
                continue;
            }
            MKFloorTopologySettings settings = rule.floorTopologySettings().orElseThrow();
            int rootWidth = root.piece().getBoundingBox().getXSpan();
            int rootLength = root.piece().getBoundingBox().getZSpan();
            int leadIn = effectiveHallwayLeadInPieces(settings, rootWidth, rootLength);
            long planSeed = MKJigsawPlacement.floorPlanSeed(rule, rule.topologyGroup(), root.piece().getPosition());
            MKFloorLayoutSolver.FloorLayoutResult plan = new MKFloorLayoutSolver().solve(settings, rootWidth,
                    rootLength, rootExits, leadIn, planSeed, maxDistanceFromCenter);
            Map<Integer, PlacedFloorSegment> placedSegments = placedFloorSegments(rule.topologyGroup(), root,
                    plan, pieces);
            int resolvedLinks = 0;
            int missingEndpoints = 0;
            int missingRoutes = 0;
            for (MKFloorLayoutSolver.AcceptedLink link : plan.acceptedLinks()) {
                Optional<PlacedLinkEndpoint> a = lockedLinkEndpoint(link.a(), placedSegments);
                Optional<PlacedLinkEndpoint> b = lockedLinkEndpoint(link.b(), placedSegments);
                if (a.isEmpty() || b.isEmpty()) {
                    missingEndpoints++;
                    if (MKNpc.DEV_LOGGING) {
                        MKNpc.LOGGER.debug("solver floor link endpoint missing group={} a={} b={}",
                                rule.topologyGroup(), link.a(), link.b());
                    }
                    continue;
                }
                Optional<LinkRoute> route = solverRoute(link, a.orElseThrow(), b.orElseThrow());
                if (route.isEmpty()) {
                    missingRoutes++;
                    if (MKNpc.DEV_LOGGING) {
                        MKNpc.LOGGER.debug("solver floor link route missing group={} a={} b={}",
                                rule.topologyGroup(), link.a(), link.b());
                    }
                    continue;
                }
                LinkRoute linkRoute = route.orElseThrow();
                linkedOpenings.add(a.orElseThrow().pos());
                linkedOpenings.add(b.orElseThrow().pos());
                LinkPalette palette = linkPalette(settings, root.metadata());
                candidates.add(new LinkCandidate(rule.topologyGroup(), link.a().segmentIndex(),
                        link.b().segmentIndex(), a.orElseThrow(), b.orElseThrow(), linkRoute, settings, palette));
                resolvedLinks++;
            }
            if (MKNpc.DEV_LOGGING && (resolvedLinks > 0 || missingEndpoints > 0 || missingRoutes > 0)) {
                long expectedSegments = plan.segments().stream()
                        .filter(segment -> segment.kind() != MKFloorLayoutSolver.SegmentKind.LINK_HALL)
                        .count();
                MKNpc.LOGGER.debug("solver floor link plan group={} expectedSegments={} mappedSegments={} links={} seed={} locked={}",
                        rule.topologyGroup(), expectedSegments, placedSegments.size(), plan.acceptedLinks().size(),
                        planSeed, rule.lockedLayoutSeed().isPresent());
                MKNpc.LOGGER.debug("solver floor link resolve summary group={} resolved={} endpointMissing={} routeMissing={}",
                        rule.topologyGroup(), resolvedLinks, missingEndpoints, missingRoutes);
            }
        }
        return new ResolvedFloorLinks(Set.copyOf(linkedOpenings), List.copyOf(candidates));
    }

    private void carveSolverFloorLinks(WorldGenLevel level, BoundingBox chunkBounds, ResolvedFloorLinks links) {
        HashMap<String, Integer> carvedByGroup = new HashMap<>();
        for (LinkCandidate candidate : links.candidates()) {
            int carvedBlocks = carveLink(level, chunkBounds, candidate);
            if (carvedBlocks > 0) {
                carvedByGroup.merge(candidate.topologyGroup(), 1, Integer::sum);
                if (MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("solver floor link carved group={} aSegment={} bSegment={} aPos={} aFacing={} bPos={} bFacing={} expectedRouteStart={} expectedRouteEnd={} routeStart={} routeEnd={} routeCells={} carvedBlocks={}",
                            candidate.topologyGroup(), candidate.aSegmentIndex(), candidate.bSegmentIndex(),
                            candidate.a().pos(), candidate.a().facing(),
                            candidate.b().pos(), candidate.b().facing(),
                            candidate.a().pos().relative(candidate.a().facing()),
                            candidate.b().pos().relative(candidate.b().facing()),
                            candidate.route().positions().getFirst(), candidate.route().positions().getLast(),
                            candidate.route().positions().size(), carvedBlocks);
                }
            }
        }
        if (MKNpc.DEV_LOGGING) {
            for (Map.Entry<String, Integer> entry : carvedByGroup.entrySet()) {
                MKNpc.LOGGER.debug("solver floor link carve summary group={} carved={}",
                        entry.getKey(), entry.getValue());
            }
        }
    }

    private Optional<FloorRootPiece> floorRootPiece(MKDungeonTopologyGroupRule rule, PiecesContainer pieces) {
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) {
                continue;
            }
            Optional<ResourceLocation> templateId = getTemplateId(poolPiece.getElement());
            if (templateId.isEmpty()) {
                continue;
            }
            Optional<MKJigsawPieceMetadata> metadata = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadata.isEmpty()) {
                continue;
            }
            boolean hasRootExit = metadata.orElseThrow().floorRootExits().stream()
                    .anyMatch(exit -> rule.topologyGroup().equals(exit.topologyGroup()));
            if (hasRootExit) {
                return Optional.of(new FloorRootPiece(poolPiece, metadata.orElseThrow()));
            }
        }
        return Optional.empty();
    }

    private List<MKFamilyHorizontalExitDefinition> rootExits(FloorRootPiece root, String topologyGroup) {
        return root.metadata().floorRootExits().stream()
                .filter(exit -> topologyGroup.equals(exit.topologyGroup()))
                .map(exit -> new MKFamilyHorizontalExitDefinition(
                        root.piece().getRotation().rotate(exit.facing()),
                        MKHorizontalExitPathKind.fromSerializedName(exit.pathKind()),
                        exit.openingProfileId()
                ))
                .toList();
    }

    private Map<Integer, PlacedFloorSegment> placedFloorSegments(String topologyGroup, FloorRootPiece root,
                                                                 MKFloorLayoutSolver.FloorLayoutResult plan,
                                                                 PiecesContainer pieces) {
        HashMap<Integer, PlacedFloorSegment> placed = new HashMap<>();
        placed.put(0, new PlacedFloorSegment(root.piece(), root.metadata()));
        List<MKFloorLayoutSolver.LogicalSegment> placeableSegments = plan.segments().stream()
                .filter(segment -> segment.kind() != MKFloorLayoutSolver.SegmentKind.ROOT)
                .filter(segment -> segment.kind() != MKFloorLayoutSolver.SegmentKind.LINK_HALL)
                .toList();
        HashSet<Integer> usedSegments = new HashSet<>();
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) {
                continue;
            }
            if (poolPiece == root.piece()) {
                continue;
            }
            Optional<ResourceLocation> templateId = getTemplateId(poolPiece.getElement());
            if (templateId.isEmpty()) {
                continue;
            }
            Optional<MKJigsawPieceMetadata> metadata = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadata.isEmpty() || !topologyGroup.equals(metadata.orElseThrow().topologyGroup())) {
                continue;
            }
            OptionalInt markedSegmentIndex = lockedFloorPlanSegmentIndex(topologyGroup, poolPiece, placeableSegments);
            if (markedSegmentIndex.isPresent()) {
                int segmentIndex = markedSegmentIndex.getAsInt();
                if (!usedSegments.contains(segmentIndex)) {
                    placed.put(segmentIndex, new PlacedFloorSegment(poolPiece, metadata.orElseThrow()));
                    usedSegments.add(segmentIndex);
                    continue;
                }
            }
            Optional<Integer> segmentIndex = matchingSegmentIndex(root.piece(), poolPiece, placeableSegments,
                    usedSegments);
            if (segmentIndex.isEmpty()) {
                if (MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("solver floor segment mapping skipped group={} template={} center={} rootCenter={}",
                            topologyGroup, templateId.get(), pieceCenter(poolPiece.getBoundingBox()),
                            pieceCenter(root.piece().getBoundingBox()));
                }
                continue;
            }
            placed.put(segmentIndex.orElseThrow(), new PlacedFloorSegment(poolPiece, metadata.orElseThrow()));
            usedSegments.add(segmentIndex.orElseThrow());
        }
        return Map.copyOf(placed);
    }

    private OptionalInt lockedFloorPlanSegmentIndex(String topologyGroup,
                                                    PoolElementStructurePiece poolPiece,
                                                    List<MKFloorLayoutSolver.LogicalSegment> segments) {
        if (!(poolPiece instanceof IMKPoolPiece mkPiece)) {
            return OptionalInt.empty();
        }
        OptionalInt segmentIndex = mkPiece.getLockedFloorPlanSegment(topologyGroup);
        if (segmentIndex.isEmpty()) {
            return OptionalInt.empty();
        }
        boolean knownSegment = segments.stream()
                .anyMatch(segment -> segment.segmentIndex() == segmentIndex.getAsInt());
        return knownSegment ? segmentIndex : OptionalInt.empty();
    }

    private Optional<Integer> matchingSegmentIndex(PoolElementStructurePiece rootPiece,
                                                   PoolElementStructurePiece poolPiece,
                                                   List<MKFloorLayoutSolver.LogicalSegment> segments,
                                                   Set<Integer> usedSegments) {
        double rootCenterX = centerX(rootPiece.getBoundingBox());
        double rootCenterZ = centerZ(rootPiece.getBoundingBox());
        double logicalX = centerX(poolPiece.getBoundingBox()) - rootCenterX;
        double logicalZ = centerZ(poolPiece.getBoundingBox()) - rootCenterZ;
        double bestDistance = Double.MAX_VALUE;
        Integer bestSegment = null;
        for (MKFloorLayoutSolver.LogicalSegment segment : segments) {
            if (usedSegments.contains(segment.segmentIndex())) {
                continue;
            }
            double dx = Math.abs(segment.rect().centerX() - logicalX);
            double dz = Math.abs(segment.rect().centerY() - logicalZ);
            double distance = dx + dz;
            if (dx <= 1.0 && dz <= 1.0 && distance < bestDistance) {
                bestDistance = distance;
                bestSegment = segment.segmentIndex();
            }
        }
        return Optional.ofNullable(bestSegment);
    }

    private String pieceCenter(BoundingBox box) {
        return centerX(box) + "," + centerY(box) + "," + centerZ(box);
    }

    private double centerX(BoundingBox box) {
        return (box.minX() + box.maxX()) / 2.0;
    }

    private double centerY(BoundingBox box) {
        return (box.minY() + box.maxY()) / 2.0;
    }

    private double centerZ(BoundingBox box) {
        return (box.minZ() + box.maxZ()) / 2.0;
    }

    private Optional<PlacedLinkEndpoint> lockedLinkEndpoint(MKFloorLayoutSolver.LinkEndpoint endpoint,
                                                            Map<Integer, PlacedFloorSegment> placedSegments) {
        PlacedFloorSegment segment = placedSegments.get(endpoint.segmentIndex());
        if (segment == null) {
            return Optional.empty();
        }
        return segment.metadata().floorLinkCandidates().stream()
                .filter(candidate -> candidate.facing() == endpoint.localDirection())
                .findFirst()
                .or(() -> segment.metadata().floorLinkCandidates().stream()
                        .filter(candidate -> segment.piece().getRotation().rotate(candidate.facing()) ==
                                endpoint.worldDirection())
                        .findFirst())
                .map(candidate -> placedLinkEndpoint(segment.piece(), segment.metadata().topologyGroup(), candidate,
                        endpoint.segmentIndex()));
    }

    private PlacedLinkEndpoint placedLinkEndpoint(PoolElementStructurePiece piece, String topologyGroup,
                                                  MKJigsawPieceMetadata.FloorLinkCandidate candidate,
                                                  int pieceIndex) {
        BlockPos relative = new BlockPos(candidate.x(), candidate.y(), candidate.z());
        BlockPos rotated = StructureTemplate.transform(relative, Mirror.NONE, piece.getRotation(), BlockPos.ZERO);
        BlockPos worldPos = piece.getPosition().offset(rotated);
        Direction facing = piece.getRotation().rotate(candidate.facing());
        return new PlacedLinkEndpoint(pieceIndex, topologyGroup, worldPos, facing, candidate.openingWidth(),
                candidate.openingHeight(), candidate.closureDepth(), piece.getBoundingBox());
    }

    private int effectiveHallwayLeadInPieces(MKFloorTopologySettings settings, int rootWidth,
                                             int rootLength) {
        if (settings.hallwayLeadInMode() == MKHallwayLeadInMode.MANUAL) {
            return Math.max(1, settings.manualHallwayLeadInPieces());
        }
        return Math.max(1, Math.ceilDiv(Math.max(rootWidth, rootLength), 8));
    }

    private void closeUnconnectedFloorOpenings(WorldGenLevel level, BoundingBox chunkBounds,
                                               PiecesContainer pieces, Set<BlockPos> linkedFloorOpenings) {
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) {
                continue;
            }
            Optional<ResourceLocation> templateId = getTemplateId(poolPiece.getElement());
            if (templateId.isEmpty()) {
                continue;
            }
            Optional<MKJigsawPieceMetadata> metadataOpt = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadataOpt.isEmpty() ||
                    (metadataOpt.get().floorClosableOpenings().isEmpty() &&
                            metadataOpt.get().floorLinkCandidates().isEmpty())) {
                continue;
            }
            MKJigsawPieceMetadata metadata = metadataOpt.get();
            BlockState closeState = closeOpeningBlockState(metadata);
            for (MKJigsawPieceMetadata.FloorClosableOpening opening : metadataOpt.get().floorClosableOpenings()) {
                Direction facing = poolPiece.getRotation().rotate(opening.facing());
                BlockPos relative = new BlockPos(opening.x(), opening.y(), opening.z());
                BlockPos rotated = StructureTemplate.transform(relative, Mirror.NONE, poolPiece.getRotation(),
                        BlockPos.ZERO);
                BlockPos worldPos = poolPiece.getPosition().offset(rotated);
                if (linkedFloorOpenings.contains(worldPos)) {
                    continue;
                }
                if (hasJigsawConnection(poolPiece, worldPos.relative(facing))) {
                    continue;
                }
                Optional<String> topologyGroup = openingTopologyGroup(metadata, opening);
                int closedBlocks = closeFloorOpening(level, chunkBounds, worldPos, facing, opening.openingWidth(),
                        opening.openingHeight(), opening.closureDepth(), closeState);
                if (closedBlocks > 0 && MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("floor opening closed template={} topologyGroup={} worldPos={} localPos={} facing={} width={} height={} closureDepth={} closeBlock={} closedBlocks={}",
                            templateId.get(), topologyGroup.orElse("<unknown>"), worldPos, relative, facing,
                            opening.openingWidth(), opening.openingHeight(), opening.closureDepth(),
                            BuiltInRegistries.BLOCK.getKey(closeState.getBlock()), closedBlocks);
                }
            }
            for (MKJigsawPieceMetadata.FloorLinkCandidate candidate : metadata.floorLinkCandidates()) {
                Direction facing = poolPiece.getRotation().rotate(candidate.facing());
                BlockPos relative = new BlockPos(candidate.x(), candidate.y(), candidate.z());
                BlockPos rotated = StructureTemplate.transform(relative, Mirror.NONE, poolPiece.getRotation(),
                        BlockPos.ZERO);
                BlockPos worldPos = poolPiece.getPosition().offset(rotated);
                if (linkedFloorOpenings.contains(worldPos)) {
                    continue;
                }
                int closedBlocks = closeFloorOpening(level, chunkBounds, worldPos, facing, candidate.openingWidth(),
                        candidate.openingHeight(), candidate.closureDepth(), closeState);
                if (closedBlocks > 0 && MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("floor link candidate closed template={} topologyGroup={} worldPos={} localPos={} facing={} width={} height={} closureDepth={} closeBlock={} closedBlocks={}",
                            templateId.get(), metadata.topologyGroup().isBlank() ? "<unknown>" : metadata.topologyGroup(),
                            worldPos, relative, facing, candidate.openingWidth(), candidate.openingHeight(),
                            candidate.closureDepth(), BuiltInRegistries.BLOCK.getKey(closeState.getBlock()),
                            closedBlocks);
                }
            }
        }
    }

    private BlockState closeOpeningBlockState(MKJigsawPieceMetadata metadata) {
        return BuiltInRegistries.BLOCK.getOptional(metadata.wallBlock())
                .map(block -> block.defaultBlockState())
                .orElse(Blocks.STONE_BRICKS.defaultBlockState());
    }

    private LinkPalette linkPalette(MKFloorTopologySettings settings, MKJigsawPieceMetadata rootMetadata) {
        MKWorkspaceMaterialPalette defaults = MKWorkspaceMaterialPalette.defaultPalette();
        ResourceLocation floorBlock = BuiltInRegistries.BLOCK.getOptional(rootMetadata.floorBlock()).isPresent() ?
                rootMetadata.floorBlock() : defaults.floorBlock();
        ResourceLocation wallBlock = BuiltInRegistries.BLOCK.getOptional(rootMetadata.wallBlock()).isPresent() ?
                rootMetadata.wallBlock() : defaults.wallBlock();
        ResourceLocation ceilingBlock = BuiltInRegistries.BLOCK.getOptional(rootMetadata.ceilingBlock()).isPresent() ?
                rootMetadata.ceilingBlock() : defaults.ceilingBlock();
        MKWorkspaceMaterialPalette basePalette = new MKWorkspaceMaterialPalette(floorBlock, wallBlock,
                ceilingBlock, defaults.stairBlock(), defaults.slabBlock(), defaults.ladderBlock());
        MKWorkspaceMaterialPalette resolved = settings.paletteOverride()
                .map(override -> override.resolve(basePalette))
                .orElse(basePalette);
        return new LinkPalette(
                blockStateOrDefault(resolved.floorBlock(), Blocks.SMOOTH_STONE.defaultBlockState()),
                blockStateOrDefault(resolved.wallBlock(), Blocks.STONE_BRICKS.defaultBlockState()),
                blockStateOrDefault(resolved.ceilingBlock(), Blocks.SMOOTH_STONE.defaultBlockState())
        );
    }

    private BlockState blockStateOrDefault(ResourceLocation blockId, BlockState fallback) {
        return BuiltInRegistries.BLOCK.getOptional(blockId)
                .map(block -> block.defaultBlockState())
                .orElse(fallback);
    }

    private Optional<String> openingTopologyGroup(MKJigsawPieceMetadata metadata,
                                                  MKJigsawPieceMetadata.FloorClosableOpening opening) {
        Optional<String> rootExitGroup = metadata.floorRootExits().stream()
                .filter(exit -> exit.facing() == opening.facing())
                .map(MKJigsawPieceMetadata.FloorRootExit::topologyGroup)
                .filter(group -> !group.isBlank())
                .findFirst();
        if (rootExitGroup.isPresent()) {
            return rootExitGroup;
        }
        return metadata.topologyGroup().isBlank() ? Optional.empty() : Optional.of(metadata.topologyGroup());
    }

    private boolean hasJigsawConnection(PoolElementStructurePiece poolPiece, BlockPos junctionPos) {
        for (JigsawJunction junction : poolPiece.getJunctions()) {
            if (junction.getSourceX() == junctionPos.getX() &&
                    junction.getSourceZ() == junctionPos.getZ()) {
                return true;
            }
        }
        return false;
    }

    private int closeFloorOpening(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos, Direction facing,
                                  int openingWidth, int openingHeight, int closureDepth, BlockState closeState) {
        int width = Math.max(1, openingWidth);
        int height = Math.max(1, openingHeight);
        int depthCount = Math.max(1, closureDepth);
        int minAcross = -((width - 1) / 2);
        int maxAcross = width / 2;
        int closedBlocks = 0;
        Direction.Axis acrossAxis = facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int depth = 0; depth < depthCount; depth++) {
            BlockPos basePos = depth == 0 ? pos : pos.relative(facing.getOpposite(), depth);
            for (int across = minAcross; across <= maxAcross; across++) {
                for (int y = 0; y < height; y++) {
                    BlockPos patchPos = acrossAxis == Direction.Axis.X ?
                            basePos.offset(across, y, 0) :
                            basePos.offset(0, y, across);
                    if (setIfInChunk(level, chunkBounds, patchPos, closeState)) {
                        closedBlocks++;
                    }
                }
            }
        }
        return closedBlocks;
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

    private Optional<LinkRoute> solverRoute(MKFloorLayoutSolver.AcceptedLink link,
                                            PlacedLinkEndpoint a,
                                            PlacedLinkEndpoint b) {
        if (a.pos().getY() != b.pos().getY()) {
            return Optional.empty();
        }
        BlockPos start = a.pos().relative(a.facing());
        BlockPos end = b.pos().relative(b.facing());
        ArrayList<List<BlockPos>> routes = new ArrayList<>();
        if (link.route().size() <= 1 && (start.getX() == end.getX() || start.getZ() == end.getZ())) {
            routes.add(line(start, end));
        }
        boolean xThenZ = link.route().isEmpty() || link.route().getFirst().width() >= link.route().getFirst().height();
        routes.add(dogleg(start, end, xThenZ));
        routes.add(dogleg(start, end, !xThenZ));
        for (List<BlockPos> route : routes) {
            if (route.isEmpty()) {
                continue;
            }
            if (!firstStepMatches(route, a.facing()) || !lastStepMatches(route, b.facing().getOpposite())) {
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

    private static Direction directionBetween(BlockPos from, BlockPos to) {
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

    private int carveLink(WorldGenLevel level, BoundingBox chunkBounds, LinkCandidate candidate) {
        int height = Math.max(2, Math.min(candidate.a().openingHeight(), candidate.b().openingHeight()));
        int width = Math.max(1, Math.min(candidate.a().openingWidth(), candidate.b().openingWidth()));
        int carvedBlocks = openEndpoint(level, chunkBounds, candidate.a());
        carvedBlocks += openEndpoint(level, chunkBounds, candidate.b());
        List<BlockPos> positions = candidate.route().positions();
        if (candidate.settings().linkGenerationMode() == MKFloorLinkGenerationMode.DEBUG) {
            for (BlockPos center : positions) {
                carvedBlocks += carveDebugCorridorCell(level, chunkBounds, center, height);
            }
        } else {
            carvedBlocks += carveHallwayCorridorFootprint(level, chunkBounds, candidate, positions, width, height);
        }
        stampLinkInserts(level, chunkBounds, candidate);
        return carvedBlocks;
    }

    private void stampLinkInserts(WorldGenLevel level, BoundingBox chunkBounds, LinkCandidate candidate) {
        if (candidate.settings().linkGenerationMode() == MKFloorLinkGenerationMode.DEBUG ||
                candidate.settings().insertFamily().isEmpty() ||
                candidate.settings().insertSpacing() <= 0 ||
                candidate.settings().insertProbability() <= 0.0f) {
            return;
        }
        Optional<ExportedWorkspaceId> workspaceId = exportedWorkspaceId();
        if (workspaceId.isEmpty()) {
            if (MKNpc.DEV_LOGGING) {
                MKNpc.LOGGER.debug("solver floor link insert skipped group={} family={} reason=unknown_workspace_pool",
                        candidate.topologyGroup(), candidate.settings().insertFamily().orElseThrow());
            }
            return;
        }
        String familyId = candidate.settings().insertFamily().orElseThrow();
        ResourceLocation poolId = MKInsertFamilyPools.poolId(workspaceId.get().namespace(),
                workspaceId.get().structureName(), familyId);
        Registry<StructureTemplatePool> pools = level.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Optional<StructureTemplatePool> poolOpt = pools.getOptional(ResourceKey.create(Registries.TEMPLATE_POOL,
                poolId));
        if (poolOpt.isEmpty() || poolOpt.get().size() <= 0) {
            if (MKNpc.DEV_LOGGING) {
                MKNpc.LOGGER.debug("solver floor link insert skipped group={} family={} pool={} reason=missing_or_empty_pool",
                        candidate.topologyGroup(), familyId, poolId);
            }
            return;
        }

        StructureTemplateManager templateManager = level.getLevel().getStructureManager();
        List<InsertTemplate> inserts = insertTemplates(poolOpt.get(), templateManager, candidate, familyId);
        if (inserts.isEmpty()) {
            if (MKNpc.DEV_LOGGING) {
                MKNpc.LOGGER.debug("solver floor link insert skipped group={} family={} pool={} reason=no_single_templates",
                        candidate.topologyGroup(), familyId, poolId);
            }
            return;
        }

        List<BlockPos> positions = candidate.route().positions();
        int routeLength = positions.size();
        int endpointPadding = Math.max(1, candidate.settings().endpointIntactRadius());
        int spacing = Math.max(1, candidate.settings().insertSpacing());
        int configuredDepth = Math.max(1, candidate.settings().insertDepth());
        int placed = 0;
        for (int index = endpointPadding; index < routeLength - endpointPadding; index += spacing) {
            InsertTemplate insert = selectInsert(inserts, candidate, positions.get(index), familyId);
            int depth = Math.max(configuredDepth, insert.depth());
            if (index + depth > routeLength - endpointPadding) {
                continue;
            }
            if (!MKJigsawLinkInsertPlacement.spanClearsDoglegConnectorArea(positions, index, depth, insert.width())) {
                continue;
            }
            Optional<Direction> direction = straightSpanDirection(positions, index, depth);
            if (direction.isEmpty()) {
                continue;
            }
            int decayIndex = Math.min(routeLength - 1, index + depth / 2);
            float spanDecay = candidate.settings().linkGenerationMode() ==
                    MKFloorLinkGenerationMode.DECAYING_HALLWAY ?
                    candidate.settings().linkDecay() *
                            routeDecayFactor(candidate.settings(), decayIndex, routeLength) :
                    0.0f;
            if (spanDecay > candidate.settings().insertMaxDecay()) {
                continue;
            }
            if (deterministicNoise(candidate, positions.get(index), 71) > candidate.settings().insertProbability()) {
                continue;
            }
            if (placeInsert(level, chunkBounds, candidate, insert, positions.get(index), direction.get())) {
                placed++;
            }
        }
        if (placed > 0 && MKNpc.DEV_LOGGING) {
            MKNpc.LOGGER.debug("solver floor link inserts placed group={} family={} pool={} placed={}",
                    candidate.topologyGroup(), familyId, poolId, placed);
        }
    }

    private Optional<ExportedWorkspaceId> exportedWorkspaceId() {
        return startPool.unwrapKey()
                .map(key -> key.location())
                .filter(id -> id.getPath().endsWith("/start"))
                .map(id -> new ExportedWorkspaceId(id.getNamespace(),
                        id.getPath().substring(0, id.getPath().length() - "/start".length())));
    }

    private List<InsertTemplate> insertTemplates(StructureTemplatePool pool, StructureTemplateManager templateManager,
                                                 LinkCandidate candidate, String familyId) {
        ArrayList<InsertTemplate> inserts = new ArrayList<>();
        List<StructurePoolElement> elements = pool.getShuffledTemplates(RandomSource.create(
                deterministicSeed(candidate, candidate.route().positions().getFirst(), familyId.hashCode())));
        for (StructurePoolElement element : elements) {
            Optional<ResourceLocation> templateId = getTemplateId(element);
            if (templateId.isEmpty()) {
                continue;
            }
            StructureTemplate template = templateManager.getOrCreate(templateId.get());
            Vec3i size = template.getSize();
            if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
                continue;
            }
            inserts.add(new InsertTemplate(templateId.get(), template, size.getX(), size.getZ()));
        }
        return List.copyOf(inserts);
    }

    private InsertTemplate selectInsert(List<InsertTemplate> inserts, LinkCandidate candidate, BlockPos pos,
                                        String familyId) {
        if (inserts.size() == 1) {
            return inserts.getFirst();
        }
        long seed = deterministicSeed(candidate, pos, familyId.hashCode() ^ 0x51F15EED);
        int index = (int) Math.floorMod(seed, inserts.size());
        return inserts.get(index);
    }

    private Optional<Direction> straightSpanDirection(List<BlockPos> positions, int startIndex, int depth) {
        if (depth <= 1) {
            return Optional.of(routeDirection(positions, startIndex));
        }
        if (startIndex + depth > positions.size()) {
            return Optional.empty();
        }
        Direction direction = directionBetween(positions.get(startIndex), positions.get(startIndex + 1));
        for (int index = startIndex + 1; index < startIndex + depth - 1; index++) {
            if (directionBetween(positions.get(index), positions.get(index + 1)) != direction) {
                return Optional.empty();
            }
        }
        return Optional.of(direction);
    }

    private Direction routeDirection(List<BlockPos> positions, int index) {
        if (positions.size() <= 1) {
            return Direction.SOUTH;
        }
        if (index + 1 < positions.size()) {
            return directionBetween(positions.get(index), positions.get(index + 1));
        }
        return directionBetween(positions.get(index - 1), positions.get(index));
    }

    private boolean placeInsert(WorldGenLevel level, BoundingBox chunkBounds, LinkCandidate candidate,
                                InsertTemplate insert, BlockPos routeCenter,
                                Direction direction) {
        Rotation rotation = insertRotation(direction);
        BlockPos origin = insertOrigin(routeCenter, rotation, insert.width());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setBoundingBox(chunkBounds)
                .setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);
        settings.addProcessor(STRUCTURE_VOID_IGNORE);
        return insert.template().placeInWorld(level, origin, origin, settings,
                RandomSource.create(deterministicSeed(candidate, routeCenter, insert.templateId().hashCode())), 18);
    }

    private Rotation insertRotation(Direction direction) {
        return switch (direction) {
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private BlockPos insertOrigin(BlockPos routeCenter, Rotation rotation, int shellWidth) {
        BlockPos localCenter = new BlockPos(shellWidth / 2, 0, 0);
        BlockPos centerOffset = StructureTemplate.transform(localCenter, Mirror.NONE, rotation, BlockPos.ZERO);
        return routeCenter.below().subtract(centerOffset);
    }

    private int openEndpoint(WorldGenLevel level, BoundingBox chunkBounds, PlacedLinkEndpoint endpoint) {
        int carvedBlocks = 0;
        int width = Math.max(1, endpoint.openingWidth());
        int height = Math.max(1, endpoint.openingHeight());
        int depthCount = Math.max(1, endpoint.closureDepth());
        int minAcross = -((width - 1) / 2);
        int maxAcross = width / 2;
        Direction.Axis acrossAxis = endpoint.facing().getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int depth = 0; depth < depthCount; depth++) {
            BlockPos basePos = depth == 0 ? endpoint.pos() : endpoint.pos().relative(endpoint.facing().getOpposite(), depth);
            for (int across = minAcross; across <= maxAcross; across++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos = acrossAxis == Direction.Axis.X ?
                            basePos.offset(across, y, 0) :
                            basePos.offset(0, y, across);
                    if (setIfInChunk(level, chunkBounds, pos, Blocks.AIR.defaultBlockState())) {
                        carvedBlocks++;
                    }
                }
            }
        }
        return carvedBlocks;
    }

    private int carveDebugCorridorCell(WorldGenLevel level, BoundingBox chunkBounds, BlockPos center, int height) {
        int carvedBlocks = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floor = center.offset(dx, -1, dz);
                if (setIfInChunk(level, chunkBounds, floor, TEMP_LINK_MARKER_STATE)) {
                    carvedBlocks++;
                }
                for (int y = 0; y < height; y++) {
                    if (setIfInChunk(level, chunkBounds, center.offset(dx, y, dz), Blocks.AIR.defaultBlockState())) {
                        carvedBlocks++;
                    }
                }
                if (setIfInChunk(level, chunkBounds, center.offset(dx, height, dz), TEMP_LINK_MARKER_STATE)) {
                    carvedBlocks++;
                }
            }
        }
        return carvedBlocks;
    }

    private int carveHallwayCorridorFootprint(WorldGenLevel level, BoundingBox chunkBounds, LinkCandidate candidate,
                                              List<BlockPos> positions, int width, int height) {
        MKJigsawLinkFootprint.Footprint footprint = MKJigsawLinkFootprint.build(positions, width);
        int carvedBlocks = 0;
        int routeLength = positions.size();
        boolean decaying = candidate.settings().linkGenerationMode() ==
                MKFloorLinkGenerationMode.DECAYING_HALLWAY;
        for (Map.Entry<BlockPos, MKJigsawLinkFootprint.Cell> entry : footprint.interior().entrySet()) {
            BlockPos pos = entry.getKey();
            MKJigsawLinkFootprint.Cell cell = entry.getValue();
            int routeIndex = cell.routeIndex();
            if (!decaying || shouldPlaceShellBlock(candidate, pos.below(), routeIndex, routeLength,
                    shellDecayWeight(ShellBlockRole.FLOOR, 0, height, true))) {
                if (setIfInChunk(level, chunkBounds, pos.below(), candidate.palette().floor())) {
                    carvedBlocks++;
                }
            }
            for (int y = 0; y < height; y++) {
                BlockPos air = pos.above(y);
                if (!decaying || shouldCarveAir(candidate, air, routeIndex, routeLength,
                        airDecayWeight(cell.center(), y, height))) {
                    if (setIfInChunk(level, chunkBounds, air, Blocks.AIR.defaultBlockState())) {
                        carvedBlocks++;
                    }
                }
            }
            BlockPos topShell = pos.above(height);
            if (!decaying || shouldPlaceShellBlock(candidate, topShell, routeIndex, routeLength,
                    shellDecayWeight(ShellBlockRole.CEILING, height, height, true))) {
                if (setIfInChunk(level, chunkBounds, topShell, candidate.palette().wall())) {
                    carvedBlocks++;
                }
            }
        }
        for (Map.Entry<BlockPos, MKJigsawLinkFootprint.Cell> entry : footprint.boundary().entrySet()) {
            BlockPos pos = entry.getKey();
            int routeIndex = entry.getValue().routeIndex();
            if (!decaying || shouldPlaceShellBlock(candidate, pos.below(), routeIndex, routeLength,
                    shellDecayWeight(ShellBlockRole.FLOOR, 0, height, false))) {
                if (setIfInChunk(level, chunkBounds, pos.below(), candidate.palette().floor())) {
                    carvedBlocks++;
                }
            }
            for (int y = 0; y < height; y++) {
                BlockPos wall = pos.above(y);
                if (!decaying || shouldPlaceShellBlock(candidate, wall, routeIndex, routeLength,
                        shellDecayWeight(ShellBlockRole.WALL, y, height, false))) {
                    if (setIfInChunk(level, chunkBounds, wall, candidate.palette().wall())) {
                        carvedBlocks++;
                    }
                }
            }
            BlockPos topShell = pos.above(height);
            if (!decaying || shouldPlaceShellBlock(candidate, topShell, routeIndex, routeLength,
                    shellDecayWeight(ShellBlockRole.CEILING, height, height, false))) {
                if (setIfInChunk(level, chunkBounds, topShell, candidate.palette().wall())) {
                    carvedBlocks++;
                }
            }
        }
        return carvedBlocks;
    }

    private boolean shouldPlaceShellBlock(LinkCandidate candidate, BlockPos pos, int routeIndex, int routeLength,
                                          float verticalWeight) {
        float chance = candidate.settings().linkDecay() * routeDecayFactor(candidate.settings(), routeIndex,
                routeLength) * verticalWeight;
        return deterministicNoise(candidate, pos, 17) >= clamp01(chance);
    }

    private boolean shouldCarveAir(LinkCandidate candidate, BlockPos pos, int routeIndex, int routeLength,
                                   float verticalWeight) {
        float chance = candidate.settings().linkDecay() * routeDecayFactor(candidate.settings(), routeIndex,
                routeLength) * verticalWeight;
        return deterministicNoise(candidate, pos, 37) >= clamp01(chance);
    }

    private float routeDecayFactor(MKFloorTopologySettings settings, int routeIndex, int routeLength) {
        if (routeLength <= 1) {
            return 0.0f;
        }
        int edgeDistance = Math.min(routeIndex, routeLength - 1 - routeIndex);
        float endpointProgress = settings.endpointIntactRadius() <= 0 ? 1.0f :
                clamp01(edgeDistance / (float) settings.endpointIntactRadius());
        float middleProgress = (float) Math.sin(Math.PI * (routeIndex / (float) (routeLength - 1)));
        float middleBias = clamp01(settings.middleDecayBonus());
        return clamp01(endpointProgress * (1.0f - middleBias) + middleProgress * middleBias);
    }

    private float shellDecayWeight(ShellBlockRole role, int y, int height, boolean interior) {
        return switch (role) {
            case FLOOR -> interior ? 0.10f : 0.20f;
            case CEILING -> 1.0f;
            case WALL -> {
                float vertical = height <= 1 ? 1.0f : y / (float) (height - 1);
                yield 0.35f + vertical * 0.55f;
            }
        };
    }

    private float airDecayWeight(boolean center, int y, int height) {
        float vertical = height <= 1 ? 1.0f : y / (float) (height - 1);
        return center ? 0.08f + vertical * 0.16f : 0.12f + vertical * 0.18f;
    }

    private float deterministicNoise(LinkCandidate candidate, BlockPos pos, int salt) {
        long seed = deterministicSeed(candidate, pos, salt);
        return ((seed >>> 40) & 0xFFFFFF) / (float) 0x1000000;
    }

    private long deterministicSeed(LinkCandidate candidate, BlockPos pos, int salt) {
        long seed = 0x9E3779B97F4A7C15L;
        seed = mix(seed ^ candidate.topologyGroup().hashCode());
        seed = mix(seed ^ candidate.aSegmentIndex());
        seed = mix(seed ^ ((long) candidate.bSegmentIndex() << 32));
        seed = mix(seed ^ pos.asLong());
        seed = mix(seed ^ salt);
        return seed;
    }

    private long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private boolean setIfInChunk(WorldGenLevel level, BoundingBox chunkBounds, BlockPos pos, BlockState state) {
        if (chunkBounds.isInside(pos)) {
            level.setBlock(pos, state, 2);
            return true;
        }
        return false;
    }

    private record PlacedLinkEndpoint(
            int pieceIndex,
            String topologyGroup,
            BlockPos pos,
            Direction facing,
            int openingWidth,
            int openingHeight,
            int closureDepth,
            BoundingBox pieceBox
    ) {
    }

    private record LinkRoute(List<BlockPos> positions) {
    }

    private record LinkCandidate(String topologyGroup, int aSegmentIndex, int bSegmentIndex,
                                 PlacedLinkEndpoint a, PlacedLinkEndpoint b, LinkRoute route,
                                 MKFloorTopologySettings settings, LinkPalette palette) {
    }

    private record LinkPalette(BlockState floor, BlockState wall, BlockState ceiling) {
    }

    private record InsertTemplate(ResourceLocation templateId, StructureTemplate template, int width, int depth) {
    }

    private record ExportedWorkspaceId(String namespace, String structureName) {
    }

    private enum ShellBlockRole {
        FLOOR,
        WALL,
        CEILING
    }

    private record ResolvedFloorLinks(Set<BlockPos> linkedOpenings, List<LinkCandidate> candidates) {
        private static final ResolvedFloorLinks EMPTY = new ResolvedFloorLinks(Set.of(), List.of());
    }

    private record FloorRootPiece(PoolElementStructurePiece piece, MKJigsawPieceMetadata metadata) {
    }

    private record PlacedFloorSegment(PoolElementStructurePiece piece, MKJigsawPieceMetadata metadata) {
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
