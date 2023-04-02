package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.StructureFeatureManager;

import java.util.List;
import java.util.Random;
import java.util.function.Function;


import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class MKSingleJigsawPiece extends SinglePoolElement implements IMKJigsawPiece{

    public static final Codec<MKSingleJigsawPiece> codec = RecordCodecBuilder.create((builder) ->
            builder.group(templateCodec(), processorsCodec(), projectionCodec(), Codec.BOOL.fieldOf("bWaterLog")
                    .forGetter(MKSingleJigsawPiece::doWaterlog))
                    .apply(builder, MKSingleJigsawPiece::new));

    private boolean bWaterlogBlocks;

    protected MKSingleJigsawPiece(Either<ResourceLocation, StructureTemplate> templateEither,
                                  Holder<StructureProcessorList> structureProcessor,
                                  StructureTemplatePool.Projection placementBehaviour, boolean waterlogBlocks) {
        super(templateEither, structureProcessor, placementBehaviour);
        bWaterlogBlocks = waterlogBlocks;
    }

    public MKSingleJigsawPiece(StructureTemplate template) {
        super(template);
    }

    public boolean doWaterlog(){
        return bWaterlogBlocks;
    }



    public Either<ResourceLocation, StructureTemplate> getPieceEither(){
        return template;
    }


    private StructureTemplate getTemplate(StructureManager p_210433_) {
        return this.template.map(p_210433_::getOrCreate, Function.identity());
    }

    @Override
    public boolean mkPlace(StructureManager templateManager, WorldGenLevel seedReader, StructureFeatureManager structureManager,
                           ChunkGenerator chunkGenerator, BlockPos structurePos, BlockPos blockPos, Rotation rot,
                           BoundingBox boundingBox, Random rand, boolean keepJigsaw, MKPoolElementPiece parent) {
        StructureTemplate template = this.getTemplate(templateManager);
        StructurePlaceSettings placementsettings = this.getSettings(rot, boundingBox, keepJigsaw);
        placementsettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
//        placementsettings.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
        placementsettings.keepLiquids = doWaterlog();
        if (!template.placeInWorld(seedReader, structurePos, blockPos, placementsettings, rand, 18)) {
            return false;
        } else {
            List<StructureTemplate.StructureBlockInfo> dataMarkers = this.getDataMarkers(templateManager, structurePos, rot, false);
            StructurePlaceSettings processSettings = placementsettings.copy();
//            processSettings.removeProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
            for(StructureTemplate.StructureBlockInfo blockinfo : StructureTemplate.processBlockInfos(
                    seedReader, structurePos, blockPos, processSettings,
                    dataMarkers, template)) {
                if (boundingBox.isInside(blockinfo.pos)){
                    mkHandleDataMarker(seedReader, blockinfo, blockinfo.pos, rot, rand, boundingBox, parent);
                }
            }
            return true;
        }
    }


    @Override
    public StructurePoolElementType<?> getType() {
        return MKNpcWorldGen.MK_SINGLE_JIGSAW_DESERIALIZER.get();
    }

    public static Function<StructureTemplatePool.Projection, MKSingleJigsawPiece> getMKSingleJigsaw(ResourceLocation pieceName, boolean doWaterlog) {
        return (placementBehaviour) -> new MKSingleJigsawPiece(Either.left(pieceName), ProcessorLists.EMPTY, placementBehaviour, doWaterlog);
    }
}
