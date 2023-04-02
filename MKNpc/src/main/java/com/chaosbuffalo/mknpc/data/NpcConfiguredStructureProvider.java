package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NpcConfiguredStructureProvider extends ConfiguredStructureProvider {

    public NpcConfiguredStructureProvider(DataGenerator generator) {
        super(generator);
    }


    @Override
    public void run(HashCache cache) throws IOException {
        List<MobSpawnSettings.SpawnerData> testSpawners = new ArrayList<>();
//        testSpawners.add(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 5, 1, 2));
//        testSpawners.add( new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 1, 1, 1));
        writeFeature(new ConfiguredStructureData(new ResourceLocation(MKNpc.MODID, "configured_test_jigsaw"),
                new ResourceLocation(MKNpc.MODID, "digger/diggercamp"), 7, BiomeTags.IS_FOREST, MKNpcWorldGen.TEST_JIGSAW.get()), cache);
//                .withSpawnOverride("monster", new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
//                        WeightedRandomList.create(testSpawners))
    }
}
