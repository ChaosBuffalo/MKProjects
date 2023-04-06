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
import java.util.function.Function;

public class MKSinglePoolElement extends SinglePoolElement implements IMKJigsawPiece {
    private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));

    public static final Codec<MKSinglePoolElement> codec = RecordCodecBuilder.create((builder) ->
            builder.group(templateCodec(), processorsCodec(), projectionCodec(),
                            Codec.BOOL.fieldOf("bWaterLog").forGetter(MKSinglePoolElement::doWaterlog))
                    .apply(builder, MKSinglePoolElement::new));

    private boolean bWaterlogBlocks;

    protected MKSinglePoolElement(Either<ResourceLocation, StructureTemplate> templateEither,
                                  Holder<StructureProcessorList> structureProcessor,
                                  StructureTemplatePool.Projection placementBehaviour, boolean waterlogBlocks) {
        super(templateEither, structureProcessor, placementBehaviour);
        bWaterlogBlocks = waterlogBlocks;
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

    //    @Override
//    public boolean mkPlace(StructureManager templateManager, WorldGenLevel seedReader, StructureFeatureManager structureManager,
//                           ChunkGenerator chunkGenerator, BlockPos structurePos, BlockPos blockPos, Rotation rot,
//                           BoundingBox boundingBox, Random rand, boolean keepJigsaw, MKPoolElementPiece parent) {
//        StructureTemplate template = this.getTemplate(templateManager);
//        StructurePlaceSettings placementsettings = this.getSettings(rot, boundingBox, keepJigsaw);
//        placementsettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
////        placementsettings.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
//        placementsettings.keepLiquids = doWaterlog();
//        if (!template.placeInWorld(seedReader, structurePos, blockPos, placementsettings, rand, 18)) {
//            return false;
//        } else {
//            List<StructureTemplate.StructureBlockInfo> dataMarkers = this.getDataMarkers(templateManager, structurePos, rot, false);
//            StructurePlaceSettings processSettings = placementsettings.copy();
////            processSettings.removeProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
//            for (StructureTemplate.StructureBlockInfo blockinfo : StructureTemplate.processBlockInfos(
//                    seedReader, structurePos, blockPos, processSettings,
//                    dataMarkers, template)) {
//                if (boundingBox.isInside(blockinfo.pos)) {
//                    mkHandleDataMarker(seedReader, blockinfo, blockinfo.pos, rot, rand, boundingBox, parent);
//                }
//            }
//            return true;
//        }
//    }


    @Override
    public StructurePoolElementType<?> getType() {
        return MKNpcWorldGen.MK_SINGLE_JIGSAW_DESERIALIZER.get();
    }

    public static Function<StructureTemplatePool.Projection, StructurePoolElement> getMKSingleJigsaw(ResourceLocation pieceName, boolean doWaterlog) {
        return (placementBehaviour) -> new MKSinglePoolElement(Either.left(pieceName), EMPTY, placementBehaviour, doWaterlog);
    }
}
