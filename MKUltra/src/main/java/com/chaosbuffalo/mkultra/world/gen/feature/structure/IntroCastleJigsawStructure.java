package com.chaosbuffalo.mkultra.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

public class IntroCastleJigsawStructure extends MKJigsawStructure {

    public static final ResourceLocation INTRO_CASTLE_SET_NAME = new ResourceLocation("configured_intro_castle");

    public static final ResourceLocation INTRO_CASTLE_STRUCTURE_NAME = new ResourceLocation("configured_intro_castle");

    private static final ResourceKey<StructureSet> SET_KEY = ResourceKey.create(Registries.STRUCTURE_SET, INTRO_CASTLE_SET_NAME);

    public IntroCastleJigsawStructure(StructureSettings pSettings, Holder<StructureTemplatePool> templatePool, Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider heightProvider, boolean useExpansionHack, Optional<Heightmap.Types> heightmapTypes, int maxDistanceFromCenter, ResourceLocation structureName) {
        super(pSettings, templatePool, startJigsawName, maxDepth, heightProvider, useExpansionHack, heightmapTypes, maxDistanceFromCenter, structureName);
    }

    private static boolean canGenerateInChunk(ChunkPos chunkPos) {
        return chunkPos.x == 0 && chunkPos.z == 0;
    }


    //    public IntroCastleJigsawStructure(Codec<JigsawConfiguration> codec, int groundLevel, boolean offsetVertical,
//                                      boolean offsetFromWorldSurface,
//                                      boolean allowSpawns) {
//        super(codec, groundLevel, offsetVertical, offsetFromWorldSurface, IntroCastleJigsawStructure::checkLocation, allowSpawns);
//    }

//    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
//        ChunkPos chunkPos = context.chunkPos();
//        return canGenerateInChunk(chunkPos.x, chunkPos.z) &&
//                !context.chunkGenerator().hasFeatureChunkInRange(SET_KEY, context.seed(), chunkPos.x, chunkPos.z, 10);
//    }
}
