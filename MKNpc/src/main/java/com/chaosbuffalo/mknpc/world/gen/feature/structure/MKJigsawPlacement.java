package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKFloorMaskVariantExporter;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MKJigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> FLOOR_MASKS = List.of("none", "n", "e", "w", "ne", "nw", "ew", "new");

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
                                layoutSettings, layoutController, rootState);
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
            MKDungeonPieceState rootState
    ) {
        Placer placer = new Placer(pools, maxDepth, chunkGenerator, structureTemplateManager, pieces, random, layoutSettings, layoutController);
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
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        private final MKDungeonLayoutSettings layoutSettings;
        private final MKDungeonLayoutController layoutController;
        final SequencedPriorityIterator<MKPieceState> placing = new SequencedPriorityIterator<>();

        Placer(
                Registry<StructureTemplatePool> pools,
                int maxDepth,
                ChunkGenerator chunkGenerator,
                StructureTemplateManager structureTemplateManager,
                List<? super PoolElementStructurePiece> pieces,
                RandomSource random,
                MKDungeonLayoutSettings layoutSettings,
                MKDungeonLayoutController layoutController
        ) {
            this.pools = pools;
            this.maxDepth = maxDepth;
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
                            MKFloorMaskVariantExporter.maskPool(basePoolKey.location(), mask)))
                    .map(aliasLookup::lookup)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
            selectedPools.add(basePoolKey);
            return selectedPools.stream().distinct().toList();
        }

        private boolean floorMaskPoolAvailable(ResourceLocation basePool, String mask, PoolAliasLookup aliasLookup) {
            ResourceKey<StructureTemplatePool> key = ResourceKey.create(Registries.TEMPLATE_POOL,
                    MKFloorMaskVariantExporter.maskPool(basePool, mask));
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

    private static long mixSeed(long seed, long value) {
        long mixed = seed ^ (value + 0x9E3779B97F4A7C15L + (seed << 6) + (seed >> 2));
        return mixed * 6364136223846793005L + 1442695040888963407L;
    }

    private static Optional<String> floorTopologyGroup(ResourceLocation pool) {
        String path = pool.getPath();
        if (!path.startsWith("floor_plan/") || path.contains("/masks/")) {
            return Optional.empty();
        }
        int roomsMarker = path.indexOf("/rooms/");
        int linearRunsMarker = path.indexOf("/linear_runs/");
        int topologyMarker;
        if (roomsMarker < 0) {
            topologyMarker = linearRunsMarker;
        } else if (linearRunsMarker < 0) {
            topologyMarker = roomsMarker;
        } else {
            topologyMarker = Math.min(roomsMarker, linearRunsMarker);
        }
        if (topologyMarker < 0) {
            return Optional.empty();
        }
        String topologyGroup = path.substring("floor_plan/".length(), topologyMarker);
        return topologyGroup.isBlank() ? Optional.empty() : Optional.of(topologyGroup);
    }

    private static Optional<ResourceLocation> getTemplateId(StructurePoolElement element) {
        if (element instanceof MKSinglePoolElement mkSinglePoolElement) {
            return mkSinglePoolElement.getPieceEither().left();
        }
        return Optional.empty();
    }
}
