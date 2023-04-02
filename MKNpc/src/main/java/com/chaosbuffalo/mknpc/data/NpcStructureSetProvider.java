package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.io.IOException;

public class NpcStructureSetProvider extends StructureSetProvider{
    public NpcStructureSetProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void run(HashCache cache) throws IOException {
        writeSet(new StructureSetData(new ResourceLocation(MKNpc.MODID, "configured_test_jigsaw"),
                new RandomSpreadStructurePlacement(10, 5, RandomSpreadType.LINEAR, 32441244))
                .withStructure(new ResourceLocation(MKNpc.MODID, "configured_test_jigsaw"), 1), cache);
    }

}
