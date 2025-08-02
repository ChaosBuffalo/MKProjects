package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MKJigsawStructure extends MKStructure {

    public static final MapCodec<MKJigsawStructure> CODEC = RecordCodecBuilder.<MKJigsawStructure>mapCodec(builder ->
            builder.group(settingsCodec(builder),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
                            .forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, 7).fieldOf("size")
                            .forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack")
                            .forGetter(s -> s.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center")
                            .forGetter(s -> s.maxDistanceFromCenter),
                    Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(p_307187_ -> p_307187_.poolAliases),
                    DimensionPadding.CODEC
                            .optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING)
                            .forGetter(p_348455_ -> p_348455_.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(p_352036_ -> p_352036_.liquidSettings),
                    CompoundTag.CODEC.fieldOf("structure_events")
                            .forGetter(MKJigsawStructure::getNbt),
                    Codec.BOOL.fieldOf("fill_floor").forGetter(s -> s.fillFloor),
                    BlockState.CODEC.optionalFieldOf("fill_state").forGetter(s -> Optional.ofNullable(s.fillState))
            ).apply(builder, MKJigsawStructure::new)).flatXmap(verifyRange(), verifyRange());

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
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;
    private final boolean fillFloor;
    @Nullable
    private final BlockState fillState;

    public MKJigsawStructure(StructureSettings pSettings, Holder<StructureTemplatePool> templatePool,
                             Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider heightProvider,
                             boolean useExpansionHack, Optional<Heightmap.Types> heightmapTypes, int maxDistanceFromCenter,
                             List<PoolAliasBinding> poolAliases,
                             DimensionPadding dimensionPadding,
                             LiquidSettings liquidSettings,
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
        return JigsawPlacement.addPieces(pContext, startPool, startJigsawName, maxDepth, startPos,
                useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter,
                PoolAliasLookup.create(this.poolAliases, startPos, pContext.seed()),
                this.dimensionPadding,
                this.liquidSettings);
    }
}
