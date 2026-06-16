package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKFloorLayoutSolver;
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
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
        closeUnconnectedFloorOpenings(level, boundingBox, pieces, Set.of());
        applyPieceFoundations(level, boundingBox, pieces);
        // Link carving must run last so generic opening closure and foundations cannot overwrite planned corridors.
        applySolverFloorLinks(level, boundingBox, pieces);
    }

    private Set<BlockPos> applySolverFloorLinks(WorldGenLevel level, BoundingBox chunkBounds,
                                                PiecesContainer pieces) {
        if (dungeonLayout == null) {
            return Set.of();
        }
        HashSet<BlockPos> linkedOpenings = new HashSet<>();
        for (MKDungeonTopologyGroupRule rule : dungeonLayout.topologyGroupRules()) {
            if (rule.floorTopologySettings().isEmpty() || !rule.linksEnabled()) {
                continue;
            }
            Optional<LockedRootPiece> rootOpt = lockedRootPiece(rule, pieces);
            if (rootOpt.isEmpty()) {
                if (MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("solver floor link skipped group={} reason=missing_root_metadata",
                            rule.topologyGroup());
                }
                continue;
            }
            LockedRootPiece root = rootOpt.orElseThrow();
            List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits = rootExits(root, rule.topologyGroup());
            if (rootExits.isEmpty()) {
                if (MKNpc.DEV_LOGGING) {
                    MKNpc.LOGGER.debug("solver floor link skipped group={} reason=missing_root_exits",
                            rule.topologyGroup());
                }
                continue;
            }
            MKWorkspaceFloorTopologySettings settings = rule.floorTopologySettings().orElseThrow();
            int rootWidth = root.piece().getBoundingBox().getXSpan();
            int rootLength = root.piece().getBoundingBox().getZSpan();
            int leadIn = effectiveHallwayLeadInPieces(settings, rootWidth, rootLength);
            long planSeed = MKJigsawPlacement.floorPlanSeed(rule, rule.topologyGroup(), root.piece().getPosition());
            MKFloorLayoutSolver.FloorLayoutResult plan = new MKFloorLayoutSolver().solve(settings, rootWidth,
                    rootLength, rootExits, leadIn, planSeed, maxDistanceFromCenter);
            Map<Integer, PlacedFloorSegment> placedSegments = lockedPlacedSegments(rule.topologyGroup(), root,
                    plan, pieces);
            int carvedLinks = 0;
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
                LinkCandidate candidate = new LinkCandidate(a.orElseThrow(), b.orElseThrow(), linkRoute);
                int carvedBlocks = carveLink(level, chunkBounds, candidate);
                linkedOpenings.add(a.orElseThrow().pos());
                linkedOpenings.add(b.orElseThrow().pos());
                if (carvedBlocks > 0) {
                    carvedLinks++;
                    if (MKNpc.DEV_LOGGING) {
                        MKNpc.LOGGER.debug("solver floor link carved group={} aSegment={} bSegment={} aPos={} aFacing={} bPos={} bFacing={} expectedRouteStart={} expectedRouteEnd={} routeStart={} routeEnd={} routeCells={} carvedBlocks={}",
                                rule.topologyGroup(), link.a().segmentIndex(), link.b().segmentIndex(),
                                a.orElseThrow().pos(), a.orElseThrow().facing(),
                                b.orElseThrow().pos(), b.orElseThrow().facing(),
                                a.orElseThrow().pos().relative(a.orElseThrow().facing()),
                                b.orElseThrow().pos().relative(b.orElseThrow().facing()),
                                linkRoute.positions().getFirst(), linkRoute.positions().getLast(),
                                linkRoute.positions().size(), carvedBlocks);
                    }
                }
            }
            if (MKNpc.DEV_LOGGING && (carvedLinks > 0 || missingEndpoints > 0 || missingRoutes > 0)) {
                long expectedSegments = plan.segments().stream()
                        .filter(segment -> segment.kind() != MKFloorLayoutSolver.SegmentKind.LINK_HALL)
                        .count();
                MKNpc.LOGGER.debug("solver floor link plan group={} expectedSegments={} mappedSegments={} links={} seed={} locked={}",
                        rule.topologyGroup(), expectedSegments, placedSegments.size(), plan.acceptedLinks().size(),
                        planSeed, rule.lockedLayoutSeed().isPresent());
                MKNpc.LOGGER.debug("solver floor link summary group={} carved={} endpointMissing={} routeMissing={}",
                        rule.topologyGroup(), carvedLinks, missingEndpoints, missingRoutes);
            }
        }
        return Set.copyOf(linkedOpenings);
    }

    private Optional<LockedRootPiece> lockedRootPiece(MKDungeonTopologyGroupRule rule, PiecesContainer pieces) {
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
                return Optional.of(new LockedRootPiece(poolPiece, metadata.orElseThrow()));
            }
        }
        return Optional.empty();
    }

    private List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits(LockedRootPiece root, String topologyGroup) {
        return root.metadata().floorRootExits().stream()
                .filter(exit -> topologyGroup.equals(exit.topologyGroup()))
                .map(exit -> new MKWorkspaceFamilyHorizontalExitDefinition(
                        root.piece().getRotation().rotate(exit.facing()),
                        MKWorkspaceHorizontalExitPathKind.fromSerializedName(exit.pathKind()),
                        exit.openingProfileId()
                ))
                .toList();
    }

    private Map<Integer, PlacedFloorSegment> lockedPlacedSegments(String topologyGroup, LockedRootPiece root,
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

    private int effectiveHallwayLeadInPieces(MKWorkspaceFloorTopologySettings settings, int rootWidth,
                                             int rootLength) {
        if (settings.hallwayLeadInMode() == MKWorkspaceHallwayLeadInMode.MANUAL) {
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

    private int carveLink(WorldGenLevel level, BoundingBox chunkBounds, LinkCandidate candidate) {
        int height = Math.max(2, Math.min(candidate.a().openingHeight(), candidate.b().openingHeight()));
        int carvedBlocks = openEndpoint(level, chunkBounds, candidate.a());
        carvedBlocks += openEndpoint(level, chunkBounds, candidate.b());
        for (BlockPos center : candidate.route().positions()) {
            carvedBlocks += carveCorridorCell(level, chunkBounds, center, height);
        }
        return carvedBlocks;
    }

    private int openEndpoint(WorldGenLevel level, BoundingBox chunkBounds, PlacedLinkEndpoint endpoint) {
        int carvedBlocks = 0;
        int minAcross = -(endpoint.openingWidth() / 2);
        int maxAcross = minAcross + endpoint.openingWidth() - 1;
        Direction.Axis acrossAxis = endpoint.facing().getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int across = minAcross; across <= maxAcross; across++) {
            for (int y = 0; y < endpoint.openingHeight(); y++) {
                BlockPos pos = acrossAxis == Direction.Axis.X ?
                        endpoint.pos().offset(across, y, 0) :
                        endpoint.pos().offset(0, y, across);
                if (setIfInChunk(level, chunkBounds, pos, Blocks.AIR.defaultBlockState())) {
                    carvedBlocks++;
                }
            }
        }
        return carvedBlocks;
    }

    private int carveCorridorCell(WorldGenLevel level, BoundingBox chunkBounds, BlockPos center, int height) {
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

    private record LinkCandidate(PlacedLinkEndpoint a, PlacedLinkEndpoint b, LinkRoute route) {
    }

    private record LockedRootPiece(PoolElementStructurePiece piece, MKJigsawPieceMetadata metadata) {
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
