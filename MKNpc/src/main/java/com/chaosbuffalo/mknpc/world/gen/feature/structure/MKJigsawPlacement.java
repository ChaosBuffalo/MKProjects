package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorMaskPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorLayoutSolver;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class MKJigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> FLOOR_MASKS = List.of("none", "n", "e", "w", "en", "nw", "ew", "enw");

    public static Optional<Structure.GenerationStub> addPieces(
            Structure.GenerationContext context,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            PoolAliasLookup aliasLookup,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings,
            MKDungeonLayoutSettings layoutSettings
    ) {
        RegistryAccess registryAccess = context.registryAccess();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
        LevelHeightAccessor levelHeightAccessor = context.heightAccessor();
        WorldgenRandom random = context.random();
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        Rotation rotation = Rotation.getRandom(random);
        StructureTemplatePool startTemplatePool = startPool.unwrapKey()
                .flatMap(key -> registry.getOptional(aliasLookup.lookup(key)))
                .orElse(startPool.value());
        StructurePoolElement startElement = startTemplatePool.getRandomTemplate(random);
        if (startElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }

        BlockPos startJigsawPos;
        if (startJigsawName.isPresent()) {
            Optional<BlockPos> optional = getRandomNamedJigsaw(startElement, startJigsawName.get(), pos, rotation, structureTemplateManager, random);
            if (optional.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", startJigsawName.get(),
                        startPool.unwrapKey().map(key -> key.location().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            startJigsawPos = optional.get();
        } else {
            startJigsawPos = pos;
        }

        Vec3i offset = startJigsawPos.subtract(pos);
        BlockPos piecePos = pos.subtract(offset);
        PoolElementStructurePiece startPiece = new PoolElementStructurePiece(
                structureTemplateManager,
                startElement,
                piecePos,
                startElement.getGroundLevelDelta(),
                rotation,
                startElement.getBoundingBox(structureTemplateManager, piecePos, rotation),
                liquidSettings
        );
        BoundingBox boundingBox = startPiece.getBoundingBox();
        int centerX = (boundingBox.maxX() + boundingBox.minX()) / 2;
        int centerZ = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
        int startY;
        if (projectStartToHeightmap.isPresent()) {
            startY = pos.getY() + chunkGenerator.getFirstFreeHeight(centerX, centerZ, projectStartToHeightmap.get(), levelHeightAccessor, context.randomState());
        } else {
            startY = piecePos.getY();
        }

        int pieceMinY = boundingBox.minY() + startPiece.getGroundLevelDelta();
        startPiece.move(0, startY - pieceMinY, 0);
        int centerY = startY + offset.getY();
        MKDungeonLayoutController layoutController = new MKDungeonLayoutController(layoutSettings);
        int targetFloors = layoutController.chooseTargetFloors(random);
        MKDungeonPieceState baseRootState = new MKDungeonPieceState(0, 0, 1, 0, true, targetFloors);
        Optional<ResourceLocation> startTemplateId = getTemplateId(startElement);
        MKDungeonPieceState rootState = startTemplateId
                .flatMap(MKJigsawPieceMetadataManager::get)
                .map(metadata -> layoutController.initialStateForStart(baseRootState, metadata, random))
                .orElse(baseRootState);
        if (MKNpc.DEV_LOGGING) {
            MKNpc.LOGGER.debug("mk_jigsaw targetFloors={} startPiece={}", targetFloors,
                    startTemplateId.orElse(MKNpcWorldGen.UNKNOWN_PIECE));
        }

        return Optional.of(new Structure.GenerationStub(
                new BlockPos(centerX, centerY, centerZ),
                builder -> {
                    List<PoolElementStructurePiece> pieces = Lists.newArrayList();
                    pieces.add(startPiece);
                    if (maxDepth > 0) {
                        AABB aabb = new AABB(
                                (double) (centerX - maxDistanceFromCenter),
                                (double) Math.max(centerY - maxDistanceFromCenter, levelHeightAccessor.getMinBuildHeight() + dimensionPadding.bottom()),
                                (double) (centerZ - maxDistanceFromCenter),
                                (double) (centerX + maxDistanceFromCenter + 1),
                                (double) Math.min(centerY + maxDistanceFromCenter + 1, levelHeightAccessor.getMaxBuildHeight() - dimensionPadding.top()),
                                (double) (centerZ + maxDistanceFromCenter + 1)
                        );
                        VoxelShape free = Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(startPiece.getBoundingBox())), BooleanOp.ONLY_FIRST);
                        addPieces(context.randomState(), maxDepth, useExpansionHack, chunkGenerator, structureTemplateManager,
                                levelHeightAccessor, random, registry, startPiece, pieces, free, aliasLookup, liquidSettings,
                                layoutSettings, layoutController, rootState, maxDistanceFromCenter);
                        pieces.forEach(builder::addPiece);
                    }
                }
        ));
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(
            StructurePoolElement element,
            ResourceLocation startJigsawName,
            BlockPos pos,
            Rotation rotation,
            StructureTemplateManager structureTemplateManager,
            WorldgenRandom random
    ) {
        List<StructureTemplate.StructureBlockInfo> list = element.getShuffledJigsawBlocks(structureTemplateManager, pos, rotation, random);
        for (StructureTemplate.StructureBlockInfo info : list) {
            ResourceLocation name = ResourceLocation.tryParse(Objects.requireNonNull(info.nbt(), () -> info + " nbt was null").getString("name"));
            if (startJigsawName.equals(name)) {
                return Optional.of(info.pos());
            }
        }
        return Optional.empty();
    }

    private static void addPieces(
            RandomState randomState,
            int maxDepth,
            boolean useExpansionHack,
            ChunkGenerator chunkGenerator,
            StructureTemplateManager structureTemplateManager,
            LevelHeightAccessor level,
            RandomSource random,
            Registry<StructureTemplatePool> pools,
            PoolElementStructurePiece startPiece,
            List<PoolElementStructurePiece> pieces,
            VoxelShape free,
            PoolAliasLookup aliasLookup,
            LiquidSettings liquidSettings,
            MKDungeonLayoutSettings layoutSettings,
            MKDungeonLayoutController layoutController,
            MKDungeonPieceState rootState,
            int maxDistanceFromCenter
    ) {
        Placer placer = new Placer(pools, maxDepth, maxDistanceFromCenter, chunkGenerator, structureTemplateManager,
                pieces, random, layoutSettings, layoutController);
        placer.tryPlacingChildren(startPiece, new MutableObject<>(free), 0, useExpansionHack, level, randomState, aliasLookup, liquidSettings, rootState);
        while (placer.placing.hasNext()) {
            MKPieceState pieceState = placer.placing.next();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, useExpansionHack, level, randomState, aliasLookup, liquidSettings, pieceState.dungeonState);
        }
    }

    private record MKPieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth, MKDungeonPieceState dungeonState) {
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final int maxDistanceFromCenter;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        private final MKDungeonLayoutSettings layoutSettings;
        private final MKDungeonLayoutController layoutController;
        private final Set<String> attemptedLockedFloorPlans = new HashSet<>();
        final SequencedPriorityIterator<MKPieceState> placing = new SequencedPriorityIterator<>();

        Placer(
                Registry<StructureTemplatePool> pools,
                int maxDepth,
                int maxDistanceFromCenter,
                ChunkGenerator chunkGenerator,
                StructureTemplateManager structureTemplateManager,
                List<? super PoolElementStructurePiece> pieces,
                RandomSource random,
                MKDungeonLayoutSettings layoutSettings,
                MKDungeonLayoutController layoutController
        ) {
            this.pools = pools;
            this.maxDepth = maxDepth;
            this.maxDistanceFromCenter = maxDistanceFromCenter;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.pieces = pieces;
            this.random = random;
            this.layoutSettings = layoutSettings;
            this.layoutController = layoutController;
        }

        void tryPlacingChildren(
                PoolElementStructurePiece piece,
                MutableObject<VoxelShape> free,
                int depth,
                boolean useExpansionHack,
                LevelHeightAccessor level,
                RandomState randomState,
                PoolAliasLookup aliasLookup,
                LiquidSettings liquidSettings,
                MKDungeonPieceState dungeonState
        ) {
            StructurePoolElement parentElement = piece.getElement();
            BlockPos piecePosition = piece.getPosition();
            Rotation rotation = piece.getRotation();
            StructureTemplatePool.Projection projection = parentElement.getProjection();
            boolean rigid = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> localFree = new MutableObject<>();
            BoundingBox pieceBox = piece.getBoundingBox();
            int pieceMinY = pieceBox.minY();

            label134:
            for (StructureTemplate.StructureBlockInfo parentJigsaw : prioritizedParentJigsaws(parentElement,
                    piecePosition, rotation, dungeonState)) {
                MKConnectorInfo connectorInfo = MKConnectorClassifier.resolve(parentJigsaw, layoutSettings);
                Direction direction = JigsawBlock.getFrontFacing(parentJigsaw.state());
                BlockPos parentJigsawPos = parentJigsaw.pos();
                BlockPos childAttachPos = parentJigsawPos.relative(direction);
                int parentJigsawY = parentJigsawPos.getY() - pieceMinY;
                int firstFreeHeight = -1;
                boolean insideParentBox = pieceBox.isInside(childAttachPos);
                MutableObject<VoxelShape> connectorFree;
                if (insideParentBox) {
                    connectorFree = localFree;
                    if (localFree.getValue() == null) {
                        localFree.setValue(Shapes.create(AABB.of(pieceBox)));
                    }
                } else {
                    connectorFree = free;
                }

                int placementPriority = parentJigsaw.nbt() != null ? parentJigsaw.nbt().getInt("placement_priority") : 0;
                ResourceKey<StructureTemplatePool> basePoolKey = readPoolKey(parentJigsaw, aliasLookup);
                Optional<String> solverFloorGroup = solverFloorTopologyGroup(basePoolKey.location());
                if (solverFloorGroup.isPresent()) {
                    String topologyGroup = solverFloorGroup.get();
                    String attemptKey = lockedFloorPlanAttemptKey(topologyGroup, piece);
                    if (!attemptedLockedFloorPlans.contains(attemptKey)) {
                        attemptedLockedFloorPlans.add(attemptKey);
                        placeLockedFloorPlan(topologyGroup, parentElement, piece, free, useExpansionHack, level,
                                randomState, aliasLookup, liquidSettings, dungeonState);
                    }
                    continue;
                }
                for (ResourceKey<StructureTemplatePool> poolKey : selectFloorMaskPools(basePoolKey, aliasLookup,
                        dungeonState, parentJigsawPos)) {
                    Optional<? extends Holder<StructureTemplatePool>> optional = this.pools.getHolder(poolKey);
                    if (optional.isEmpty()) {
                        LOGGER.warn("Empty or non-existent pool: {}", poolKey.location());
                        continue;
                    }

                    Holder<StructureTemplatePool> holder = optional.get();
                    if (holder.value().size() == 0 && !holder.is(Pools.EMPTY)) {
                        LOGGER.warn("Empty or non-existent pool: {}", poolKey.location());
                        continue;
                    }

                    Holder<StructureTemplatePool> fallback = holder.value().getFallback();
                    if (fallback.value().size() == 0 && !fallback.is(Pools.EMPTY)) {
                        LOGGER.warn("Empty or non-existent fallback pool: {}", fallback.unwrapKey().map(key -> key.location().toString()).orElse("<unregistered>"));
                        continue;
                    }

                    List<StructurePoolElement> candidates = Lists.newArrayList();
                    if (depth != this.maxDepth) {
                        candidates.addAll(holder.value().getShuffledTemplates(this.random));
                    }
                    addMainPathEndingCandidates(candidates, dungeonState, connectorInfo, aliasLookup);
                    boolean branchCapsAvailable = addBranchCapCandidates(candidates, connectorInfo, poolKey, aliasLookup);
                    candidates.addAll(fallback.value().getShuffledTemplates(this.random));

                    for (StructurePoolElement childElement : candidates) {
                        if (childElement == EmptyPoolElement.INSTANCE) {
                            break;
                        }

                        Optional<ResourceLocation> childTemplateId = getTemplateId(childElement);
                        if (childTemplateId.isEmpty()) {
                            logRejection("missing_template_id", connectorInfo, MKNpcWorldGen.UNKNOWN_PIECE, dungeonState);
                            continue;
                        }
                        Optional<MKJigsawPieceMetadata> childMetadataOptional = MKJigsawPieceMetadataManager.get(childTemplateId.get());
                        if (childMetadataOptional.isEmpty()) {
                            logRejection("missing_metadata", connectorInfo, childTemplateId.get(), dungeonState);
                            continue;
                        }
                        MKJigsawPieceMetadata childMetadata = childMetadataOptional.get();
                        Optional<String> rejectionReason = layoutController.getRejectionReason(dungeonState, connectorInfo,
                                childMetadata, branchCapsAvailable);
                        if (rejectionReason.isPresent()) {
                            logRejection(rejectionReason.get(), connectorInfo, childTemplateId.get(), dungeonState);
                            continue;
                        }

                        for (Rotation childRotation : Rotation.getShuffled(this.random)) {
                            List<StructureTemplate.StructureBlockInfo> childJigsaws = childElement.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO, childRotation, this.random);
                            BoundingBox childBoxAtOrigin = childElement.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, childRotation);
                            int expansionHackHeight;
                            if (useExpansionHack && childBoxAtOrigin.getYSpan() <= 16) {
                                expansionHackHeight = childJigsaws.stream()
                                        .mapToInt(info -> {
                                            if (!childBoxAtOrigin.isInside(info.pos().relative(JigsawBlock.getFrontFacing(info.state())))) {
                                                return 0;
                                            }
                                            ResourceKey<StructureTemplatePool> childPoolKey = readPoolKey(info, aliasLookup);
                                            Optional<? extends Holder<StructureTemplatePool>> childPool = this.pools.getHolder(childPoolKey);
                                            Optional<Holder<StructureTemplatePool>> childFallback = childPool.map(pool -> pool.value().getFallback());
                                            int poolSize = childPool.map(pool -> pool.value().getMaxSize(this.structureTemplateManager)).orElse(0);
                                            int fallbackSize = childFallback.map(pool -> pool.value().getMaxSize(this.structureTemplateManager)).orElse(0);
                                            return Math.max(poolSize, fallbackSize);
                                        }).max().orElse(0);
                            } else {
                                expansionHackHeight = 0;
                            }

                            for (StructureTemplate.StructureBlockInfo childJigsaw : childJigsaws) {
                                if (!JigsawBlock.canAttach(parentJigsaw, childJigsaw)) {
                                    continue;
                                }

                                BlockPos childJigsawPos = childJigsaw.pos();
                                BlockPos childPiecePos = childAttachPos.subtract(childJigsawPos);
                                BoundingBox childBox = childElement.getBoundingBox(this.structureTemplateManager, childPiecePos, childRotation);
                                int childBoxMinY = childBox.minY();
                                StructureTemplatePool.Projection childProjection = childElement.getProjection();
                                boolean childRigid = childProjection == StructureTemplatePool.Projection.RIGID;
                                int childJigsawY = childJigsawPos.getY();
                                int relativeY = parentJigsawY - childJigsawY + direction.getStepY();
                                int placementY;
                                if (rigid && childRigid) {
                                    placementY = pieceMinY + relativeY;
                                } else {
                                    if (firstFreeHeight == -1) {
                                        firstFreeHeight = this.chunkGenerator.getFirstFreeHeight(parentJigsawPos.getX(), parentJigsawPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                                    }
                                    placementY = firstFreeHeight - childJigsawY;
                                }

                                int childYOffset = placementY - childBoxMinY;
                                BoundingBox movedChildBox = childBox.moved(0, childYOffset, 0);
                                BlockPos movedChildPos = childPiecePos.offset(0, childYOffset, 0);
                                if (expansionHackHeight > 0) {
                                    int extraHeight = Math.max(expansionHackHeight + 1, movedChildBox.maxY() - movedChildBox.minY());
                                    movedChildBox.encapsulate(new BlockPos(movedChildBox.minX(), movedChildBox.minY() + extraHeight, movedChildBox.minZ()));
                                }

                                if (Shapes.joinIsNotEmpty(connectorFree.getValue(), Shapes.create(AABB.of(movedChildBox).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
                                    continue;
                                }

                                connectorFree.setValue(Shapes.joinUnoptimized(connectorFree.getValue(), Shapes.create(AABB.of(movedChildBox)), BooleanOp.ONLY_FIRST));
                                int groundLevelDelta = piece.getGroundLevelDelta();
                                int childGroundDelta = childRigid ? groundLevelDelta - relativeY : childElement.getGroundLevelDelta();
                                PoolElementStructurePiece placedChild = new PoolElementStructurePiece(this.structureTemplateManager, childElement, movedChildPos, childGroundDelta, childRotation, movedChildBox, liquidSettings);
                                int junctionY;
                                if (rigid) {
                                    junctionY = pieceMinY + parentJigsawY;
                                } else if (childRigid) {
                                    junctionY = placementY + childJigsawY;
                                } else {
                                    if (firstFreeHeight == -1) {
                                        firstFreeHeight = this.chunkGenerator.getFirstFreeHeight(parentJigsawPos.getX(), parentJigsawPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                                    }
                                    junctionY = firstFreeHeight + relativeY / 2;
                                }

                                piece.addJunction(new JigsawJunction(childAttachPos.getX(), junctionY - parentJigsawY + groundLevelDelta, childAttachPos.getZ(), relativeY, childProjection));
                                placedChild.addJunction(new JigsawJunction(parentJigsawPos.getX(), junctionY - childJigsawY + childGroundDelta, parentJigsawPos.getZ(), -relativeY, projection));
                                this.pieces.add(placedChild);
                                MKDungeonPieceState childState = layoutController.nextState(dungeonState, connectorInfo,
                                        childMetadata, this.random);
                                if (MKNpc.DEV_LOGGING) {
                                    MKNpc.LOGGER.debug("mk_jigsaw accept template={} connector={} floor={} vertical={} piecesOnFloor={} branchDepth={} mainPath={}",
                                            childTemplateId.get(), connectorInfo.role().getSerializedName(), childState.progressionFloorIndex(),
                                            childState.verticalLevelIndex(), childState.piecesOnFloor(), childState.branchDepth(), childState.onMainPath());
                                }
                                if (depth + 1 <= this.maxDepth) {
                                    this.placing.add(new MKPieceState(placedChild, connectorFree, depth + 1, childState), placementPriority);
                                }
                                continue label134;
                            }
                        }
                    }
                }
            }
        }

        private Optional<String> solverFloorTopologyGroup(ResourceLocation pool) {
            return floorTopologyGroup(pool)
                    .filter(group -> layoutSettings.topologyGroupRule(group)
                            .filter(rule -> rule.floorTopologySettings().isPresent())
                            .isPresent());
        }

        private String lockedFloorPlanAttemptKey(String topologyGroup, PoolElementStructurePiece rootPiece) {
            BlockPos position = rootPiece.getPosition();
            return topologyGroup + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
        }

        private void placeLockedFloorPlan(String topologyGroup,
                                          StructurePoolElement parentElement,
                                          PoolElementStructurePiece rootPiece,
                                          MutableObject<VoxelShape> free,
                                          boolean useExpansionHack,
                                          LevelHeightAccessor level,
                                          RandomState randomState,
                                          PoolAliasLookup aliasLookup,
                                          LiquidSettings liquidSettings,
                                          MKDungeonPieceState dungeonState) {
            Optional<MKDungeonTopologyGroupRule> ruleOpt = layoutSettings.topologyGroupRule(topologyGroup);
            if (ruleOpt.isEmpty() || ruleOpt.get().floorTopologySettings().isEmpty()) {
                return;
            }
            MKDungeonTopologyGroupRule rule = ruleOpt.get();
            long planSeed = floorPlanSeed(rule, topologyGroup, rootPiece.getPosition());
            Optional<RootFloorPlanContext> contextOpt = rootFloorPlanContext(parentElement, rootPiece, topologyGroup,
                    aliasLookup);
            if (contextOpt.isEmpty()) {
                logLockedFloorPlanFailure(topologyGroup, "missing root floor topology exits");
                return;
            }
            RootFloorPlanContext context = contextOpt.get();
            MKFloorTopologySettings settings = rule.floorTopologySettings().orElseThrow();
            int rootWidth = rootPiece.getBoundingBox().getXSpan();
            int rootLength = rootPiece.getBoundingBox().getZSpan();
            int leadIn = effectiveHallwayLeadInPieces(settings, rootWidth, rootLength);
            MKFloorLayoutSolver.FloorLayoutResult plan = new MKFloorLayoutSolver().solve(settings, rootWidth,
                    rootLength, context.rootExits(), leadIn, planSeed, maxDistanceFromCenter);
            if (plan.hasRequiredRejections() || !plan.fitsHardLimit()) {
                logLockedFloorPlanFailure(topologyGroup, "solver rejected floor plan");
                return;
            }

            ArrayList<LockedFloorPlacedSegment> placedSegments = new ArrayList<>();
            plan.segments().stream()
                    .filter(segment -> segment.kind() == MKFloorLayoutSolver.SegmentKind.ROOT)
                    .findFirst()
                    .ifPresent(rootSegment -> {
                        markLockedFloorPlanSegment(rootPiece, topologyGroup, rootSegment.segmentIndex());
                        placedSegments.add(new LockedFloorPlacedSegment(rootSegment,
                                parentElement, rootPiece, rootPiece.getRotation()));
                    });
            int placed = 0;
            for (MKFloorLayoutSolver.LogicalSegment segment : plan.segments()) {
                if (segment.kind() == MKFloorLayoutSolver.SegmentKind.ROOT ||
                        segment.kind() == MKFloorLayoutSolver.SegmentKind.LINK_HALL) {
                    continue;
                }
                Optional<LockedFloorPlacedSegment> placedSegment = placeLockedFloorSegment(topologyGroup, context,
                        planSeed, segment, placedSegments, free, aliasLookup, liquidSettings);
                if (placedSegment.isEmpty()) {
                    logLockedFloorPlanFailure(topologyGroup, "failed placing segment " + segment.label() +
                            " " + segment.kind());
                    return;
                }
                placedSegments.add(placedSegment.orElseThrow());
                placed++;
            }
            if (MKNpc.DEV_LOGGING) {
                MKNpc.LOGGER.debug("mk_jigsaw solver floor plan placed group={} segments={} seed={} locked={}",
                        topologyGroup, placed, planSeed, rule.lockedLayoutSeed().isPresent());
            }
        }

        private Optional<LockedFloorPlacedSegment> placeLockedFloorSegment(String topologyGroup,
                                                                           RootFloorPlanContext context,
                                                                           long planSeed,
                                                                           MKFloorLayoutSolver.LogicalSegment segment,
                                                                           List<LockedFloorPlacedSegment> placedSegments,
                                                                           MutableObject<VoxelShape> free,
                                                                           PoolAliasLookup aliasLookup,
                                                                           LiquidSettings liquidSettings) {
            Rotation childRotation = rotationForSegment(segment);
            long seed = lockedSegmentSeed(planSeed, segment);
            Optional<StructurePoolElement> childOpt = selectLockedFloorTemplate(context, segment, childRotation,
                    seed, aliasLookup);
            if (childOpt.isEmpty()) {
                return Optional.empty();
            }
            StructurePoolElement childElement = childOpt.get();
            Optional<LockedFloorPlacedSegment> parentOpt = lockedSegmentParent(placedSegments, segment);
            if (parentOpt.isEmpty()) {
                logLockedFloorPlanFailure(topologyGroup, "no placed parent for segment " + segment.label());
                return Optional.empty();
            }
            LockedFloorPlacedSegment parent = parentOpt.orElseThrow();
            Direction direction = segment.direction() == null ? Direction.NORTH : segment.direction();
            Optional<StructureTemplate.StructureBlockInfo> parentJigsaw = lockedFloorParentJigsaw(parent,
                    context.topologyGroup(), direction, parentOutgoingRole(segment), aliasLookup);
            if (parentJigsaw.isEmpty()) {
                logLockedFloorPlanFailure(topologyGroup, "no parent connector for segment " + segment.label() +
                        " facing " + direction.getSerializedName() + " parent=" +
                        lockedFloorSegmentSummary(parent) + " connectors=" +
                        lockedFloorConnectorSummary(parent, aliasLookup));
                return Optional.empty();
            }
            Optional<StructureTemplate.StructureBlockInfo> childJigsaw = lockedFloorChildJigsaw(
                    parentJigsaw.orElseThrow(), childElement, childRotation, direction.getOpposite());
            if (childJigsaw.isEmpty()) {
                logLockedFloorPlanFailure(topologyGroup, "no child connector for segment " + segment.label() +
                        " facing " + direction.getOpposite().getSerializedName());
                return Optional.empty();
            }
            BlockPos childAttachPos = parentJigsaw.orElseThrow().pos().relative(direction);
            BlockPos movedChildPos = childAttachPos.subtract(childJigsaw.orElseThrow().pos());
            BoundingBox movedChildBox = childElement.getBoundingBox(this.structureTemplateManager, movedChildPos,
                    childRotation);
            if (Shapes.joinIsNotEmpty(free.getValue(), Shapes.create(AABB.of(movedChildBox).deflate(0.25)),
                    BooleanOp.ONLY_SECOND)) {
                logLockedFloorPlanFailure(topologyGroup, "physical collision for segment " + segment.label() +
                        " " + lockedFloorLogicalSummary(segment) +
                        " parent=" + lockedFloorSegmentSummary(parent) +
                        " template=" + getTemplateId(childElement).map(ResourceLocation::toString)
                        .orElse("<unknown>") +
                        " parentJigsaw=" + parentJigsaw.orElseThrow().pos() +
                        " childJigsaw=" + childJigsaw.orElseThrow().pos() +
                        " attach=" + childAttachPos +
                        " box=" + movedChildBox +
                        " overlaps=" + lockedFloorCollisionSummary(movedChildBox));
                return Optional.empty();
            }
            free.setValue(Shapes.joinUnoptimized(free.getValue(), Shapes.create(AABB.of(movedChildBox)),
                    BooleanOp.ONLY_FIRST));
            PoolElementStructurePiece placedChild = new PoolElementStructurePiece(this.structureTemplateManager,
                    childElement, movedChildPos, parent.piece().getGroundLevelDelta(), childRotation, movedChildBox,
                    liquidSettings);
            addLockedFloorJunctions(parent, placedChild, parentJigsaw.orElseThrow(), childJigsaw.orElseThrow(),
                    childAttachPos, direction, childElement);
            markLockedFloorPlanSegment(placedChild, topologyGroup, segment.segmentIndex());
            this.pieces.add(placedChild);
            return Optional.of(new LockedFloorPlacedSegment(segment, childElement, placedChild, childRotation));
        }

        private void addLockedFloorJunctions(LockedFloorPlacedSegment parent,
                                             PoolElementStructurePiece child,
                                             StructureTemplate.StructureBlockInfo parentJigsaw,
                                             StructureTemplate.StructureBlockInfo childJigsaw,
                                             BlockPos childAttachPos,
                                             Direction direction,
                                             StructurePoolElement childElement) {
            int parentPieceMinY = parent.piece().getBoundingBox().minY();
            int parentJigsawY = parentJigsaw.pos().getY() - parentPieceMinY;
            int childJigsawY = childJigsaw.pos().getY();
            int relativeY = parentJigsawY - childJigsawY + direction.getStepY();
            int junctionY = parentPieceMinY + parentJigsawY;
            StructureTemplatePool.Projection parentProjection = parent.element().getProjection();
            StructureTemplatePool.Projection childProjection = childElement.getProjection();
            parent.piece().addJunction(new JigsawJunction(
                    childAttachPos.getX(),
                    junctionY - parentJigsawY + parent.piece().getGroundLevelDelta(),
                    childAttachPos.getZ(),
                    relativeY,
                    childProjection
            ));
            child.addJunction(new JigsawJunction(
                    parentJigsaw.pos().getX(),
                    junctionY - childJigsawY + child.getGroundLevelDelta(),
                    parentJigsaw.pos().getZ(),
                    -relativeY,
                    parentProjection
            ));
        }

        private void markLockedFloorPlanSegment(PoolElementStructurePiece piece, String topologyGroup,
                                                int segmentIndex) {
            if (piece instanceof IMKPoolPiece mkPiece) {
                mkPiece.setLockedFloorPlanSegment(topologyGroup, segmentIndex);
            }
        }

        private Optional<LockedFloorPlacedSegment> lockedSegmentParent(
                List<LockedFloorPlacedSegment> placedSegments,
                MKFloorLayoutSolver.LogicalSegment segment) {
            int parentSegmentIndex = segment.parentSegmentIndex();
            return placedSegments.stream()
                    .filter(candidate -> candidate.segment().segmentIndex() == parentSegmentIndex)
                    .findFirst();
        }

        private boolean logicalRectsAdjacent(MKFloorLayoutSolver.LogicalRect parent,
                                             MKFloorLayoutSolver.LogicalRect child,
                                             Direction direction) {
            float tolerance = 0.75f;
            return switch (direction) {
                case NORTH -> Math.abs(parent.top() - child.bottom() - 1.0f) <= tolerance &&
                        rangesOverlap(parent.left(), parent.right(), child.left(), child.right());
                case SOUTH -> Math.abs(child.top() - parent.bottom() - 1.0f) <= tolerance &&
                        rangesOverlap(parent.left(), parent.right(), child.left(), child.right());
                case EAST -> Math.abs(child.left() - parent.right() - 1.0f) <= tolerance &&
                        rangesOverlap(parent.top(), parent.bottom(), child.top(), child.bottom());
                case WEST -> Math.abs(parent.left() - child.right() - 1.0f) <= tolerance &&
                        rangesOverlap(parent.top(), parent.bottom(), child.top(), child.bottom());
                default -> false;
            };
        }

        private boolean rangesOverlap(float aMin, float aMax, float bMin, float bMax) {
            return aMin < bMax && aMax > bMin;
        }

        private Optional<StructureTemplate.StructureBlockInfo> lockedFloorParentJigsaw(
                LockedFloorPlacedSegment parent,
                String topologyGroup,
                Direction direction,
                MKConnectorRole role,
                PoolAliasLookup aliasLookup) {
            return parent.element().getShuffledJigsawBlocks(this.structureTemplateManager, parent.piece().getPosition(),
                            parent.rotation(), RandomSource.create(0L))
                    .stream()
                    .filter(info -> JigsawBlock.getFrontFacing(info.state()) == direction)
                    .filter(info -> {
                        MKConnectorRole actual = MKConnectorClassifier.resolve(info, layoutSettings).role();
                        return actual == role || actual == MKConnectorRole.UNKNOWN;
                    })
                    .filter(info -> floorTopologyGroup(readPoolKey(info, aliasLookup).location())
                            .map(topologyGroup::equals)
                            .orElse(false))
                    .findFirst();
        }

        private String lockedFloorSegmentSummary(LockedFloorPlacedSegment segment) {
            String template = getTemplateId(segment.element())
                    .map(ResourceLocation::toString)
                    .orElse("<unknown>");
            return segment.segment().label() + "/" + segment.segment().kind() +
                    "#" + segment.segment().segmentIndex() +
                    " mask=" + segment.segment().acceptedMask() +
                    " dir=" + (segment.segment().direction() == null ? "none" :
                    segment.segment().direction().getSerializedName()) +
                    " template=" + template;
        }

        private String lockedFloorLogicalSummary(MKFloorLayoutSolver.LogicalSegment segment) {
            MKFloorLayoutSolver.LogicalRect rect = segment.rect();
            return segment.kind() + "#" + segment.segmentIndex() +
                    " parent=" + segment.parentSegmentIndex() +
                    " dir=" + (segment.direction() == null ? "none" : segment.direction().getSerializedName()) +
                    " mask=" + segment.acceptedMask() +
                    " rect=[" + rect.left() + "," + rect.top() + " -> " +
                    rect.right() + "," + rect.bottom() + "]";
        }

        private String lockedFloorCollisionSummary(BoundingBox box) {
            ArrayList<String> collisions = new ArrayList<>();
            for (Object existing : this.pieces) {
                if (!(existing instanceof PoolElementStructurePiece piece)) {
                    continue;
                }
                BoundingBox existingBox = piece.getBoundingBox();
                if (!boxesIntersect(existingBox, box)) {
                    continue;
                }
                String template = getTemplateId(piece.getElement())
                        .map(ResourceLocation::toString)
                        .orElse("<unknown>");
                collisions.add(template + " " + existingBox);
                if (collisions.size() >= 5) {
                    break;
                }
            }
            return collisions.isEmpty() ? "[]" : collisions.toString();
        }

        private boolean boxesIntersect(BoundingBox left, BoundingBox right) {
            return left.minX() <= right.maxX() && left.maxX() >= right.minX() &&
                    left.minY() <= right.maxY() && left.maxY() >= right.minY() &&
                    left.minZ() <= right.maxZ() && left.maxZ() >= right.minZ();
        }

        private String lockedFloorConnectorSummary(LockedFloorPlacedSegment parent,
                                                   PoolAliasLookup aliasLookup) {
            return parent.element().getShuffledJigsawBlocks(this.structureTemplateManager,
                            parent.piece().getPosition(), parent.rotation(), RandomSource.create(0L))
                    .stream()
                    .map(info -> {
                        MKConnectorRole role = MKConnectorClassifier.resolve(info, layoutSettings).role();
                        ResourceLocation pool = readPoolKey(info, aliasLookup).location();
                        return JigsawBlock.getFrontFacing(info.state()).getSerializedName() +
                                ":" + role.name() + ":" + pool;
                    })
                    .toList()
                    .toString();
        }

        private Optional<StructureTemplate.StructureBlockInfo> lockedFloorChildJigsaw(
                StructureTemplate.StructureBlockInfo parentJigsaw,
                StructurePoolElement childElement,
                Rotation childRotation,
                Direction direction) {
            return childElement.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO,
                            childRotation, RandomSource.create(0L))
                    .stream()
                    .filter(info -> JigsawBlock.getFrontFacing(info.state()) == direction)
                    .filter(info -> JigsawBlock.canAttach(parentJigsaw, info))
                    .findFirst();
        }

        private MKConnectorRole parentOutgoingRole(MKFloorLayoutSolver.LogicalSegment segment) {
            return switch (segment.kind()) {
                case MAIN_HALL, MAIN_ROOM, MAIN_CAP -> MKConnectorRole.MAIN_BACK;
                case BRANCH_HALL, BRANCH_ROOM, BRANCH_CAP -> MKConnectorRole.BRANCH;
                default -> MKConnectorRole.BRANCH;
            };
        }

        private Optional<StructurePoolElement> selectLockedFloorTemplate(RootFloorPlanContext context,
                                                                         MKFloorLayoutSolver.LogicalSegment segment,
                                                                         Rotation rotation,
                                                                         long seed,
                                                                         PoolAliasLookup aliasLookup) {
            ArrayList<String> rejected = new ArrayList<>();
            for (ResourceLocation poolId : lockedSegmentPools(context, segment)) {
                ResourceKey<StructureTemplatePool> poolKey = aliasLookup.lookup(ResourceKey.create(
                        Registries.TEMPLATE_POOL, poolId));
                Optional<? extends Holder<StructureTemplatePool>> holderOpt = this.pools.getHolder(poolKey);
                if (holderOpt.isEmpty() || holderOpt.get().value().size() == 0) {
                    rejected.add(poolId + "=empty");
                    continue;
                }
                List<StructurePoolElement> candidates = holderOpt.get().value()
                        .getShuffledTemplates(RandomSource.create(seed));
                for (StructurePoolElement candidate : candidates) {
                    if (lockedTemplateMatchesSegment(candidate, segment, rotation)) {
                        return Optional.of(candidate);
                    }
                    rejected.add(lockedTemplateRejectSummary(candidate, segment, rotation));
                }
            }
            logLockedFloorPlanFailure(context.topologyGroup(), "no template candidates matched segment " +
                    segment.label() + " " + segment.kind() + " mask=" + segment.acceptedMask() +
                    " dir=" + (segment.direction() == null ? "none" : segment.direction().getSerializedName()) +
                    " rejected=" + rejected);
            return Optional.empty();
        }

        private List<ResourceLocation> lockedSegmentPools(RootFloorPlanContext context,
                                                          MKFloorLayoutSolver.LogicalSegment segment) {
            ArrayList<ResourceLocation> pools = new ArrayList<>();
            switch (segment.kind()) {
                case MAIN_HALL -> pools.add(context.pool("linear_runs/main/" + context.mainOpeningProfile()));
                case BRANCH_HALL -> pools.add(context.pool("linear_runs/branch/" + context.branchOpeningProfile()));
                case MAIN_ROOM -> addMaskedPool(pools, context.pool("rooms/main/" + context.mainOpeningProfile()),
                        segment.acceptedMask());
                case BRANCH_ROOM -> addMaskedPool(pools, context.pool("rooms/branch/" + context.branchOpeningProfile()),
                        segment.acceptedMask());
                case BRANCH_CAP -> {
                    addMaskedPool(pools, context.pool("rooms/branch/" + context.branchOpeningProfile()),
                            segment.acceptedMask());
                    addMaskedPool(pools, context.pool("branch_caps/" + context.branchOpeningProfile()),
                            segment.acceptedMask());
                }
                case MAIN_CAP -> {
                    addMaskedPool(pools, context.pool("rooms/main/" + context.mainOpeningProfile()),
                            segment.acceptedMask());
                    if (segment.profile() != null &&
                            segment.profile().kind() == MKFloorRoomKind.MAIN_CAP_APPROACH) {
                        pools.add(context.structurePool("main_cap_approaches/" + context.topologyGroup()));
                    } else {
                        pools.add(context.structurePool("main_caps/" + context.topologyGroup()));
                    }
                }
                default -> {
                }
            }
            return List.copyOf(pools);
        }

        private void addMaskedPool(List<ResourceLocation> pools, ResourceLocation basePool, String mask) {
            pools.add(MKFloorMaskPools.maskPool(basePool,
                    mask == null || mask.isBlank() ? "none" : mask));
            pools.add(basePool);
        }

        private boolean lockedTemplateMatchesSegment(StructurePoolElement element,
                                                     MKFloorLayoutSolver.LogicalSegment segment,
                                                     Rotation rotation) {
            Optional<ResourceLocation> templateId = getTemplateId(element);
            if (templateId.isEmpty()) {
                return false;
            }
            Optional<MKJigsawPieceMetadata> metadataOpt = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadataOpt.isEmpty()) {
                return false;
            }
            MKJigsawPieceMetadata metadata = metadataOpt.get();
            String path = templateId.get().getPath();
            boolean roleMatches = switch (segment.kind()) {
                case MAIN_HALL -> path.contains("_linear_run_") && path.contains("_main") &&
                        metadata.allowOnMainPath() && !metadata.mainPathEnding() && !metadata.branchCap();
                case BRANCH_HALL -> path.contains("_linear_run_") && path.contains("_branch") &&
                        metadata.allowOnBranchPath() && !metadata.branchCap();
                case MAIN_ROOM -> path.contains("_main_room_") && metadata.allowOnMainPath() &&
                        !metadata.mainPathEnding() && floorMaskMatches(metadata, segment) &&
                        mainExitMatches(element, segment, rotation);
                case BRANCH_ROOM -> path.contains("_branch_room_") && metadata.allowOnBranchPath() &&
                        !metadata.branchCap() && floorMaskMatches(metadata, segment);
                case BRANCH_CAP -> metadata.branchCap() && floorMaskMatches(metadata, segment);
                case MAIN_CAP -> metadata.mainPathEnding() && floorMaskMatches(metadata, segment);
                default -> false;
            };
            return roleMatches && lockedTemplateFootprintMatches(element, segment, rotation);
        }

        private String lockedTemplateRejectSummary(StructurePoolElement element,
                                                   MKFloorLayoutSolver.LogicalSegment segment,
                                                   Rotation rotation) {
            Optional<ResourceLocation> templateId = getTemplateId(element);
            if (templateId.isEmpty()) {
                return "<no-template-id>";
            }
            Optional<MKJigsawPieceMetadata> metadataOpt = MKJigsawPieceMetadataManager.get(templateId.get());
            if (metadataOpt.isEmpty()) {
                return templateId.get() + ":missing-meta";
            }
            MKJigsawPieceMetadata metadata = metadataOpt.get();
            return templateId.get() +
                    ":mask=" + metadata.floorExitMask() +
                    ":allowBranch=" + metadata.allowOnBranchPath() +
                    ":allowMain=" + metadata.allowOnMainPath() +
                    ":branchCap=" + metadata.branchCap() +
                    ":mainEnd=" + metadata.mainPathEnding() +
                    ":footprint=" + lockedTemplateFootprint(element, rotation) +
                    ":expectedFootprint=" + expectedLockedFootprint(segment) +
                    ":expectedMask=" + segment.acceptedMask();
        }

        private boolean lockedTemplateFootprintMatches(StructurePoolElement element,
                                                       MKFloorLayoutSolver.LogicalSegment segment,
                                                       Rotation rotation) {
            BoundingBox box = element.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation);
            return box.getXSpan() == Math.round(segment.rect().width()) &&
                    box.getZSpan() == Math.round(segment.rect().height());
        }

        private String lockedTemplateFootprint(StructurePoolElement element, Rotation rotation) {
            BoundingBox box = element.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation);
            return box.getXSpan() + "x" + box.getZSpan();
        }

        private String expectedLockedFootprint(MKFloorLayoutSolver.LogicalSegment segment) {
            return Math.round(segment.rect().width()) + "x" + Math.round(segment.rect().height());
        }

        private boolean floorMaskMatches(MKJigsawPieceMetadata metadata, MKFloorLayoutSolver.LogicalSegment segment) {
            String expected = segment.acceptedMask() == null || segment.acceptedMask().isBlank() ?
                    "none" : segment.acceptedMask();
            String actual = metadata.floorExitMask() == null || metadata.floorExitMask().isBlank() ?
                    "none" : metadata.floorExitMask();
            return expected.equals(actual) || "none".equals(expected) && actual.isBlank();
        }

        private boolean mainExitMatches(StructurePoolElement element, MKFloorLayoutSolver.LogicalSegment segment,
                                        Rotation rotation) {
            if (segment.profile() == null || segment.profile().mainExitDirection().isEmpty() ||
                    segment.direction() == null) {
                return true;
            }
            Direction expected = rotateRoomExit(segment.profile().mainExitDirection().orElseThrow(),
                    segment.direction());
            List<StructureTemplate.StructureBlockInfo> jigsaws = element.getShuffledJigsawBlocks(
                    this.structureTemplateManager, BlockPos.ZERO, rotation, RandomSource.create(0L));
            return jigsaws.stream().anyMatch(info ->
                    MKConnectorClassifier.resolve(info, layoutSettings).role() == MKConnectorRole.MAIN_BACK &&
                            JigsawBlock.getFrontFacing(info.state()) == expected);
        }

        private Optional<RootFloorPlanContext> rootFloorPlanContext(StructurePoolElement parentElement,
                                                                    PoolElementStructurePiece rootPiece,
                                                                    String topologyGroup,
                                                                    PoolAliasLookup aliasLookup) {
            List<StructureTemplate.StructureBlockInfo> jigsaws = parentElement.getShuffledJigsawBlocks(
                    this.structureTemplateManager, rootPiece.getPosition(), rootPiece.getRotation(),
                    RandomSource.create(0L));
            ArrayList<MKFamilyHorizontalExitDefinition> rootExits = new ArrayList<>();
            ResourceLocation samplePool = null;
            String mainOpening = "";
            String branchOpening = "";
            for (StructureTemplate.StructureBlockInfo jigsaw : jigsaws) {
                ResourceKey<StructureTemplatePool> poolKey = readPoolKey(jigsaw, aliasLookup);
                Optional<String> group = floorTopologyGroup(poolKey.location());
                if (group.isEmpty() || !group.get().equals(topologyGroup)) {
                    continue;
                }
                MKConnectorInfo connectorInfo = MKConnectorClassifier.resolve(jigsaw, layoutSettings);
                Direction facing = JigsawBlock.getFrontFacing(jigsaw.state());
                String opening = openingProfileFromPool(poolKey.location()).orElse("");
                if (connectorInfo.role() == MKConnectorRole.MAIN_BACK) {
                    rootExits.add(new MKFamilyHorizontalExitDefinition(facing,
                            MKHorizontalExitPathKind.MAIN_EXIT, opening));
                    mainOpening = opening;
                } else if (connectorInfo.role() == MKConnectorRole.BRANCH) {
                    rootExits.add(new MKFamilyHorizontalExitDefinition(facing,
                            MKHorizontalExitPathKind.BRANCH, opening));
                    branchOpening = opening;
                }
                samplePool = poolKey.location();
            }
            if (rootExits.isEmpty() || samplePool == null) {
                return Optional.empty();
            }
            if (mainOpening.isBlank()) {
                mainOpening = branchOpening;
            }
            if (branchOpening.isBlank()) {
                branchOpening = mainOpening;
            }
            return Optional.of(new RootFloorPlanContext(topologyGroup, poolPrefix(samplePool, topologyGroup),
                    samplePool.getNamespace(), mainOpening, branchOpening, List.copyOf(rootExits)));
        }

        private Optional<String> openingProfileFromPool(ResourceLocation pool) {
            String path = pool.getPath();
            int marker = path.lastIndexOf('/');
            if (marker < 0 || marker == path.length() - 1) {
                return Optional.empty();
            }
            String opening = path.substring(marker + 1);
            return opening.isBlank() ? Optional.empty() : Optional.of(opening);
        }

        private String poolPrefix(ResourceLocation pool, String topologyGroup) {
            String marker = "floor_plan/" + topologyGroup + "/";
            String path = pool.getPath();
            int index = path.indexOf(marker);
            return index <= 0 ? "" : path.substring(0, index);
        }

        private int effectiveHallwayLeadInPieces(MKFloorTopologySettings settings, int rootWidth,
                                                 int rootLength) {
            if (settings.hallwayLeadInMode() ==
                    com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHallwayLeadInMode.MANUAL) {
                return Math.max(1, settings.manualHallwayLeadInPieces());
            }
            return Math.max(1, Math.ceilDiv(Math.max(rootWidth, rootLength), 8));
        }

        private Rotation rotationForSegment(MKFloorLayoutSolver.LogicalSegment segment) {
            Direction direction = segment.direction() == null ? Direction.NORTH : segment.direction();
            if (segment.kind() == MKFloorLayoutSolver.SegmentKind.MAIN_HALL ||
                    segment.kind() == MKFloorLayoutSolver.SegmentKind.BRANCH_HALL) {
                return switch (direction) {
                    case EAST -> Rotation.NONE;
                    case SOUTH -> Rotation.CLOCKWISE_90;
                    case WEST -> Rotation.CLOCKWISE_180;
                    case NORTH -> Rotation.COUNTERCLOCKWISE_90;
                    default -> Rotation.NONE;
                };
            }
            return switch (direction) {
                case NORTH -> Rotation.NONE;
                case EAST -> Rotation.CLOCKWISE_90;
                case SOUTH -> Rotation.CLOCKWISE_180;
                case WEST -> Rotation.COUNTERCLOCKWISE_90;
                default -> Rotation.NONE;
            };
        }

        private Direction rotateRoomExit(Direction localDirection, Direction pathDirection) {
            if (pathDirection == Direction.NORTH) {
                return localDirection;
            }
            if (pathDirection == Direction.SOUTH) {
                return localDirection.getOpposite();
            }
            if (pathDirection == Direction.EAST) {
                return switch (localDirection) {
                    case NORTH -> Direction.EAST;
                    case EAST -> Direction.SOUTH;
                    case SOUTH -> Direction.WEST;
                    case WEST -> Direction.NORTH;
                    default -> localDirection;
                };
            }
            if (pathDirection == Direction.WEST) {
                return switch (localDirection) {
                    case NORTH -> Direction.WEST;
                    case EAST -> Direction.NORTH;
                    case SOUTH -> Direction.EAST;
                    case WEST -> Direction.SOUTH;
                    default -> localDirection;
                };
            }
            return localDirection;
        }

        private long lockedSegmentSeed(long seed, MKFloorLayoutSolver.LogicalSegment segment) {
            seed = mixSeed(seed, segment.segmentIndex());
            seed = mixSeed(seed, segment.parentSegmentIndex());
            seed = mixSeed(seed, segment.kind().ordinal());
            seed = mixSeed(seed, segment.label().hashCode());
            seed = mixSeed(seed, Float.floatToIntBits(segment.rect().centerX()));
            seed = mixSeed(seed, Float.floatToIntBits(segment.rect().centerY()));
            return seed;
        }

        private void logLockedFloorPlanFailure(String topologyGroup, String reason) {
            MKNpc.LOGGER.warn("solver floor plan generation failed for {}: {}", topologyGroup, reason);
        }

        private record RootFloorPlanContext(
                String topologyGroup,
                String poolPrefix,
                String namespace,
                String mainOpeningProfile,
                String branchOpeningProfile,
                List<MKFamilyHorizontalExitDefinition> rootExits
        ) {
            private ResourceLocation pool(String suffix) {
                return ResourceLocation.fromNamespaceAndPath(namespace, poolPrefix + "floor_plan/" +
                        topologyGroup + "/" + suffix);
            }

            private ResourceLocation structurePool(String suffix) {
                return ResourceLocation.fromNamespaceAndPath(namespace, poolPrefix + suffix);
            }
        }

        private record LockedFloorPlacedSegment(
                MKFloorLayoutSolver.LogicalSegment segment,
                StructurePoolElement element,
                PoolElementStructurePiece piece,
                Rotation rotation
        ) {
        }

        private List<StructureTemplate.StructureBlockInfo> prioritizedParentJigsaws(
                StructurePoolElement parentElement,
                BlockPos piecePosition,
                Rotation rotation,
                MKDungeonPieceState dungeonState
        ) {
            List<StructureTemplate.StructureBlockInfo> jigsaws = parentElement.getShuffledJigsawBlocks(
                    this.structureTemplateManager, piecePosition, rotation, this.random);
            if (!dungeonState.onMainPath()) {
                return jigsaws;
            }
            return jigsaws.stream()
                    .sorted(java.util.Comparator.comparingInt(info ->
                            connectorPriority(MKConnectorClassifier.resolve(info, layoutSettings))))
                    .toList();
        }

        private int connectorPriority(MKConnectorInfo connectorInfo) {
            return switch (connectorInfo.role()) {
                case BRANCH, LINK_CANDIDATE -> 2;
                case UNKNOWN -> 1;
                default -> 0;
            };
        }

        private void logRejection(String reason, MKConnectorInfo connectorInfo, ResourceLocation templateId, MKDungeonPieceState state) {
            if (MKNpc.DEV_LOGGING) {
                MKNpc.LOGGER.debug("mk_jigsaw reject reason={} template={} connector={} floor={} vertical={} piecesOnFloor={} branchDepth={} mainPath={}",
                        reason, templateId, connectorInfo.role().getSerializedName(), state.progressionFloorIndex(),
                        state.verticalLevelIndex(), state.piecesOnFloor(), state.branchDepth(), state.onMainPath());
            }
        }

        private static ResourceKey<StructureTemplatePool> readPoolKey(StructureTemplate.StructureBlockInfo blockInfo, PoolAliasLookup aliasLookup) {
            CompoundTag compoundTag = Objects.requireNonNull(blockInfo.nbt(), () -> blockInfo + " nbt was null");
            ResourceKey<StructureTemplatePool> resourceKey = Pools.parseKey(compoundTag.getString("pool"));
            return aliasLookup.lookup(resourceKey);
        }

        private void addMainPathEndingCandidates(List<StructurePoolElement> candidates,
                                                 MKDungeonPieceState dungeonState,
                                                 MKConnectorInfo connectorInfo,
                                                 PoolAliasLookup aliasLookup) {
            Optional<ResourceLocation> endingPool = layoutController.endingPoolForState(dungeonState, connectorInfo);
            if (endingPool.isEmpty()) {
                return;
            }
            ResourceKey<StructureTemplatePool> poolKey = ResourceKey.create(Registries.TEMPLATE_POOL, endingPool.get());
            ResourceKey<StructureTemplatePool> resolvedPoolKey = aliasLookup.lookup(poolKey);
            pools.getOptional(resolvedPoolKey)
                    .ifPresent(pool -> candidates.addAll(pool.getShuffledTemplates(this.random)));
        }

        private List<ResourceKey<StructureTemplatePool>> selectFloorMaskPools(ResourceKey<StructureTemplatePool> basePoolKey,
                                                                              PoolAliasLookup aliasLookup,
                                                                              MKDungeonPieceState dungeonState,
                                                                              BlockPos connectorPos) {
            Optional<String> topologyGroup = floorTopologyGroup(basePoolKey.location());
            if (topologyGroup.isEmpty()) {
                return List.of(basePoolKey);
            }
            Optional<MKDungeonTopologyGroupRule> rule = layoutSettings.topologyGroupRule(topologyGroup.get());
            float sprawl = rule.map(MKDungeonTopologyGroupRule::sprawl).orElse(0.5f);
            List<String> availableMasks = FLOOR_MASKS.stream()
                    .filter(mask -> floorMaskPoolAvailable(basePoolKey.location(), mask, aliasLookup))
                    .toList();
            RandomSource maskRandom = rule.flatMap(MKDungeonTopologyGroupRule::lockedLayoutSeed)
                    .map(seed -> RandomSource.create(floorMaskSelectionSeed(seed, basePoolKey.location(),
                            dungeonState, connectorPos)))
                    .orElse(random);
            List<ResourceKey<StructureTemplatePool>> selectedPools = chooseFloorMasks(sprawl, availableMasks, maskRandom).stream()
                    .map(mask -> ResourceKey.create(Registries.TEMPLATE_POOL,
                            MKFloorMaskPools.maskPool(basePoolKey.location(), mask)))
                    .map(aliasLookup::lookup)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
            selectedPools.add(basePoolKey);
            return selectedPools.stream().distinct().toList();
        }

        private boolean floorMaskPoolAvailable(ResourceLocation basePool, String mask, PoolAliasLookup aliasLookup) {
            ResourceKey<StructureTemplatePool> key = ResourceKey.create(Registries.TEMPLATE_POOL,
                    MKFloorMaskPools.maskPool(basePool, mask));
            return pools.getOptional(aliasLookup.lookup(key))
                    .filter(pool -> pool.size() > 0)
                    .isPresent();
        }

        private boolean addBranchCapCandidates(List<StructurePoolElement> candidates,
                                               MKConnectorInfo connectorInfo,
                                               ResourceKey<StructureTemplatePool> targetPoolKey,
                                               PoolAliasLookup aliasLookup) {
            if (connectorInfo.role() != MKConnectorRole.BRANCH) {
                return false;
            }
            Optional<StructureTemplatePool> poolOpt = branchCapPoolFor(targetPoolKey)
                    .map(pool -> ResourceKey.create(Registries.TEMPLATE_POOL, pool))
                    .map(aliasLookup::lookup)
                    .flatMap(pools::getOptional)
                    .filter(pool -> pool.size() > 0);
            poolOpt.ifPresent(pool -> candidates.addAll(pool.getShuffledTemplates(this.random)));
            return poolOpt.isPresent();
        }

        private Optional<ResourceLocation> branchCapPoolFor(ResourceKey<StructureTemplatePool> targetPoolKey) {
            ResourceLocation location = targetPoolKey.location();
            String path = location.getPath();
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
            return Optional.of(ResourceLocation.fromNamespaceAndPath(location.getNamespace(),
                    prefix + "branch_caps/" + openingProfile.get()));
        }

        private Optional<String> branchOpeningProfile(String path, String marker) {
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

    static Optional<String> chooseFloorMask(float sprawl, List<String> availableMasks, RandomSource random) {
        if (availableMasks == null || availableMasks.isEmpty()) {
            return Optional.empty();
        }
        if (sprawl <= 0.0f && availableMasks.contains("none")) {
            return Optional.of("none");
        }
        if (sprawl >= 1.0f) {
            return availableMasks.stream()
                    .max(java.util.Comparator.comparingInt(MKJigsawPlacement::activeMaskCount));
        }
        int totalWeight = availableMasks.stream()
                .mapToInt(mask -> floorMaskWeight(mask, sprawl))
                .sum();
        if (totalWeight <= 0) {
            return Optional.of(availableMasks.getFirst());
        }
        int roll = random == null ? 0 : random.nextInt(totalWeight);
        int cursor = 0;
        for (String mask : availableMasks) {
            cursor += floorMaskWeight(mask, sprawl);
            if (roll < cursor) {
                return Optional.of(mask);
            }
        }
        return Optional.of(availableMasks.getLast());
    }

    static List<String> chooseFloorMasks(float sprawl, List<String> availableMasks, RandomSource random) {
        Optional<String> preferred = chooseFloorMask(sprawl, availableMasks, random);
        if (preferred.isEmpty()) {
            return List.of();
        }
        List<String> ordered = Lists.newArrayList();
        ordered.add(preferred.get());
        availableMasks.stream()
                .filter(mask -> !mask.equals(preferred.get()))
                .sorted((left, right) -> compareFallbackMasks(left, right, sprawl))
                .forEach(ordered::add);
        return List.copyOf(ordered);
    }

    private static int compareFallbackMasks(String left, String right, float sprawl) {
        int leftCount = activeMaskCount(left);
        int rightCount = activeMaskCount(right);
        int countComparison = sprawl >= 0.5f ?
                Integer.compare(rightCount, leftCount) :
                Integer.compare(leftCount, rightCount);
        if (countComparison != 0) {
            return countComparison;
        }
        return Integer.compare(FLOOR_MASKS.indexOf(left), FLOOR_MASKS.indexOf(right));
    }

    private static int floorMaskWeight(String mask, float sprawl) {
        int activeCount = activeMaskCount(mask);
        if (activeCount == 0) {
            return Math.max(1, Math.round((1.0f - sprawl) * 8.0f) + 1);
        }
        return Math.max(1, Math.round(1.0f + sprawl * activeCount * 4.0f));
    }

    private static int activeMaskCount(String mask) {
        return "none".equals(mask) ? 0 : mask.length();
    }

    private static long floorMaskSelectionSeed(long lockedSeed, ResourceLocation pool, MKDungeonPieceState state,
                                               BlockPos connectorPos) {
        long seed = lockedSeed;
        seed = mixSeed(seed, pool.hashCode());
        seed = mixSeed(seed, state.mainPathPiecesInTopologyGroup());
        seed = mixSeed(seed, state.branchDepth());
        seed = mixSeed(seed, state.piecesOnFloor());
        seed = mixSeed(seed, connectorPos.asLong());
        return seed;
    }

    static long floorPlanSeed(MKDungeonTopologyGroupRule rule, String topologyGroup, BlockPos rootPos) {
        return rule.lockedLayoutSeed().orElseGet(() -> runtimeFloorPlanSeed(topologyGroup, rootPos));
    }

    static long runtimeFloorPlanSeed(String topologyGroup, BlockPos rootPos) {
        long seed = 0x6A09E667F3BCC909L;
        seed = mixSeed(seed, topologyGroup.hashCode());
        seed = mixSeed(seed, rootPos.asLong());
        return seed;
    }

    private static long mixSeed(long seed, long value) {
        long mixed = seed ^ (value + 0x9E3779B97F4A7C15L + (seed << 6) + (seed >> 2));
        return mixed * 6364136223846793005L + 1442695040888963407L;
    }

    private static Optional<String> floorTopologyGroup(ResourceLocation pool) {
        String path = pool.getPath();
        if (path.contains("/masks/")) {
            return Optional.empty();
        }
        int floorPlanMarker = path.indexOf("floor_plan/");
        if (floorPlanMarker < 0) {
            return Optional.empty();
        }
        int topologyStart = floorPlanMarker + "floor_plan/".length();
        int roomsMarker = path.indexOf("/rooms/", topologyStart);
        int linearRunsMarker = path.indexOf("/linear_runs/", topologyStart);
        int branchCapsMarker = path.indexOf("/branch_caps/", topologyStart);
        int mainCapsMarker = path.indexOf("/main_caps/", topologyStart);
        int mainCapApproachesMarker = path.indexOf("/main_cap_approaches/", topologyStart);
        int topologyMarker;
        topologyMarker = minPositive(roomsMarker, linearRunsMarker, branchCapsMarker, mainCapsMarker,
                mainCapApproachesMarker);
        if (topologyMarker < 0) {
            return Optional.empty();
        }
        String topologyGroup = path.substring(topologyStart, topologyMarker);
        return topologyGroup.isBlank() ? Optional.empty() : Optional.of(topologyGroup);
    }

    private static int minPositive(int... values) {
        int result = -1;
        for (int value : values) {
            if (value >= 0 && (result < 0 || value < result)) {
                result = value;
            }
        }
        return result;
    }

    private static Optional<ResourceLocation> getTemplateId(StructurePoolElement element) {
        if (element instanceof MKSinglePoolElement mkSinglePoolElement) {
            return mkSinglePoolElement.getPieceEither().left();
        }
        return Optional.empty();
    }
}
