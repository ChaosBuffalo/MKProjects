package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.registries.NpcStructures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class NpcStructureSets {

    public static final ResourceKey<StructureSet> TEST_STRUCTURES = createKey("test_structures");

    private static ResourceKey<StructureSet> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(MKNpc.MODID, name));
    }

    public static void bootstrap(BootstapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(TEST_STRUCTURES,
                new StructureSet(structures.getOrThrow(NpcStructures.TEST_JIGSAW),
                        new RandomSpreadStructurePlacement(10, 5, RandomSpreadType.LINEAR, 32441244)));
    }
}
