package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class MKSinglePoolElement extends SinglePoolElement implements IMKPoolElement {
    private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));

    public static final Codec<MKSinglePoolElement> codec = RecordCodecBuilder.create((builder) ->
            builder.group(templateCodec(), processorsCodec(), projectionCodec(),
                            Codec.BOOL.fieldOf("bWaterLog").forGetter(MKSinglePoolElement::doWaterlog))
                    .apply(builder, MKSinglePoolElement::new));

    private final boolean bWaterlogBlocks;

    protected MKSinglePoolElement(Either<ResourceLocation, StructureTemplate> templateEither,
                                  Holder<StructureProcessorList> structureProcessor,
                                  StructureTemplatePool.Projection placementBehaviour, boolean waterlogBlocks) {
        super(templateEither, structureProcessor, placementBehaviour);
        bWaterlogBlocks = waterlogBlocks;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return MKNpcWorldGen.MK_SINGLE_JIGSAW_DESERIALIZER.get();
    }

    public boolean doWaterlog() {
        return bWaterlogBlocks;
    }


    public Either<ResourceLocation, StructureTemplate> getPieceEither() {
        return template;
    }


    private StructureTemplate getTemplate(StructureTemplateManager p_210433_) {
        return this.template.map(p_210433_::getOrCreate, Function.identity());
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation pRotation, BoundingBox pBoundingBox, boolean p_210423_) {
        StructurePlaceSettings settings = super.getSettings(pRotation, pBoundingBox, p_210423_);
        settings.keepLiquids = doWaterlog();
        return settings;
    }

    @Override
    public boolean place(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, BlockPos p_227306_, BlockPos p_227307_, Rotation pRotation, BoundingBox pBox, RandomSource pRandom, boolean p_227311_) {
        return super.place(pStructureTemplateManager, pLevel, pStructureManager, pGenerator, p_227306_, p_227307_, pRotation, pBox, pRandom, p_227311_);
    }

    @Override
    public void handleDataMarker(LevelAccessor pLevel, StructureTemplate.StructureBlockInfo pBlockInfo, BlockPos pPos, Rotation pRotation, RandomSource pRandom, BoundingBox pBox) {
        super.handleDataMarker(pLevel, pBlockInfo, pPos, pRotation, pRandom, pBox);
    }

    public static Function<StructureTemplatePool.Projection, StructurePoolElement> forTemplate(ResourceLocation pieceName, boolean doWaterlog) {
        return (placementBehaviour) -> new MKSinglePoolElement(Either.left(pieceName), EMPTY, placementBehaviour, doWaterlog);
    }

    @Override
    public boolean mkPlace(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel,
                           StructureManager pStructureManager, ChunkGenerator pGenerator,
                           BlockPos p_227340_, BlockPos p_227341_, Rotation pRotation, BoundingBox pBox,
                           RandomSource pRandom, boolean p_227345_, ResourceLocation name, UUID instanceId) {
        StructureTemplate structuretemplate = this.getTemplate(pStructureTemplateManager);
        StructurePlaceSettings structureplacesettings = this.getSettings(pRotation, pBox, p_227345_);
        if (!structuretemplate.placeInWorld(pLevel, p_227340_, p_227341_, structureplacesettings, pRandom, 18)) {
            return false;
        } else {
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : StructureTemplate.processBlockInfos(pLevel, p_227340_, p_227341_, structureplacesettings, this.getDataMarkers(pStructureTemplateManager, p_227340_, pRotation, false), structuretemplate)) {
                mkHandleDataMarker(pLevel, structureBlockInfo, p_227340_, pRotation, pRandom, pBox, name, instanceId);
            }

            return true;
        }
    }
}
