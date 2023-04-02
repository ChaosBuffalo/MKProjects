package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSingleJigsawPiece;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.io.IOException;

public class NpcPoolProvider extends TemplatePoolProvider{

    private static final ResourceLocation DIGGER_TENT_DBL_1 = new ResourceLocation(MKNpc.MODID, "digger/diggertentdbl1");
    private static final ResourceLocation DIGGER_TENT_SGL_1 = new ResourceLocation(MKNpc.MODID, "digger/diggertentsgl1");

    static final StructureTemplatePool DIGGER_POOL = new StructureTemplatePool(new ResourceLocation(MKNpc.MODID, "digger/diggercamp"),
            new ResourceLocation("empty"),
            ImmutableList.of(
                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(DIGGER_TENT_DBL_1, false), 1),
                    Pair.of(MKSingleJigsawPiece.getMKSingleJigsaw(DIGGER_TENT_SGL_1, false), 1),
                    Pair.of(StructurePoolElement.empty(), 2)
            ), StructureTemplatePool.Projection.RIGID);

    public NpcPoolProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void run(HashCache cache) throws IOException {
        writePool(DIGGER_POOL, cache);
    }
}
