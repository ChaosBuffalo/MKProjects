package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class MKSinglePoolElement extends SinglePoolElement implements IMKPoolElement {
    private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));
    private static final BlockIgnoreProcessor STRUCTURE_VOID_IGNORE =
            new BlockIgnoreProcessor(List.of(Blocks.STRUCTURE_VOID));

    public static final MapCodec<MKSinglePoolElement> codec = RecordCodecBuilder.mapCodec((builder) -> builder.group(
            templateCodec(),
            processorsCodec(),
            projectionCodec(),
            overrideLiquidSettingsCodec(),
            Codec.BOOL.fieldOf("bWaterLog").forGetter(MKSinglePoolElement::doWaterlog)
    ).apply(builder, MKSinglePoolElement::new));

    // TODO: see if this is still needed now that overrideLiquidSettingsCodec exists
    private final boolean bWaterlogBlocks;

    protected MKSinglePoolElement(Either<ResourceLocation, StructureTemplate> template,
                                  Holder<StructureProcessorList> processors,
                                  StructureTemplatePool.Projection projection,
                                  Optional<LiquidSettings> overrideLiquidSettings,
                                  Boolean waterlogBlocks) {
        super(template, processors, projection, overrideLiquidSettings);
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


    private StructureTemplate getTemplate(StructureTemplateManager pStructureTemplateManager) {
        return this.template.map(pStructureTemplateManager::getOrCreate, Function.identity());
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean offset) {
        var settings = super.getSettings(rotation, boundingBox, liquidSettings, offset);
        settings.setLiquidSettings(doWaterlog() ? LiquidSettings.APPLY_WATERLOGGING : LiquidSettings.IGNORE_WATERLOGGING);
        return settings;
    }

    @Override
    public boolean place(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, BlockPos offset, BlockPos pos, Rotation rotation, BoundingBox box, RandomSource random, LiquidSettings liquidSettings, boolean keepJigsaws) {
        throw new IllegalStateException("Should not get here. Did the mixins fail?");
    }

    @Override
    public void handleDataMarker(LevelAccessor pLevel, StructureTemplate.StructureBlockInfo pBlockInfo, BlockPos pPos, Rotation pRotation, RandomSource pRandom, BoundingBox pBox) {
        throw new IllegalStateException("Should not get here. Did the mixins fail?");
    }

    public static Function<StructureTemplatePool.Projection, StructurePoolElement> forTemplate(ResourceLocation pieceName, boolean doWaterlog) {
        LiquidSettings liquidSettings = doWaterlog ? LiquidSettings.APPLY_WATERLOGGING : LiquidSettings.IGNORE_WATERLOGGING;
        return (placementBehaviour) -> new MKSinglePoolElement(Either.left(pieceName), EMPTY, placementBehaviour, Optional.of(liquidSettings), doWaterlog);
    }

    @Override
    public boolean mkPlace(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel,
                           StructureManager pStructureManager, ChunkGenerator pGenerator,
                           BlockPos piecePosition, BlockPos firstPieceBottomCenter, Rotation pRotation, BoundingBox pBox,
                           RandomSource pRandom, LiquidSettings liquidSettings, boolean pKeepJigsaws, ResourceLocation name, UUID instanceId) {
        StructureTemplate template = this.getTemplate(pStructureTemplateManager);
        StructurePlaceSettings settings = this.getSettings(pRotation, pBox, liquidSettings, pKeepJigsaws);
        settings.addProcessor(STRUCTURE_VOID_IGNORE);
        if (!template.placeInWorld(pLevel, piecePosition, firstPieceBottomCenter, settings, pRandom, 18)) {
            return false;
        } else {
            var dataMarkers = this.getDataMarkers(pStructureTemplateManager, piecePosition, pRotation, false);

            // Need to pop this so processBlockInfos doesn't filter all data structure blocks
            settings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
            var dataBlocks = StructureTemplate.processBlockInfos(pLevel, piecePosition, firstPieceBottomCenter, settings, dataMarkers, template);
            for (var markerBlock : dataBlocks) {
                if (pBox.isInside(markerBlock.pos())) {
                    mkHandleDataMarker(pLevel, markerBlock, piecePosition, pRotation, pRandom, pBox, name, instanceId);
                }
            }

            return true;
        }
    }

    @Override
    public String toString() {
        return "MKSingle[" + this.template + "]";
    }
}
