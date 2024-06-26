package com.chaosbuffalo.mkultra.data.registries;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.StaticPlacement;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class UltraStructureSets {

    public static final ResourceKey<StructureSet> INTRO_CASTLE = createKey("intro_castle");
    public static final ResourceKey<StructureSet> DESERT_TEMPLE_VILLAGE = createKey("desert_temple_village");
    public static final ResourceKey<StructureSet> NECROTIDE_ALTER = createKey("necrotide_alter");
    public static final ResourceKey<StructureSet> DEEPSLATE_OBELISK = createKey("deepslate_obelisk");
    public static final ResourceKey<StructureSet> HYBOREAN_CRYPT = createKey("hyborean_crypt");
    public static final ResourceKey<StructureSet> DECAYING_CHURCH = createKey("decaying_church");

    public static ResourceKey<StructureSet> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, new ResourceLocation(MKUltra.MODID, name));
    }

    public static void bootstrap(BootstapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        HolderGetter<StructureSet> sets = context.lookup(Registries.STRUCTURE_SET);

        context.register(INTRO_CASTLE,
                new StructureSet(structures.getOrThrow(UltraStructures.INTRO_CASTLE),
                        new StaticPlacement(0, 0,
                                new StructurePlacement.ExclusionZone(sets.getOrThrow(INTRO_CASTLE), 0))));


        context.register(DESERT_TEMPLE_VILLAGE,
                new StructureSet(structures.getOrThrow(UltraStructures.DESERT_TEMPLE_VILLAGE),
                        new RandomSpreadStructurePlacement(36, 8, RandomSpreadType.LINEAR, 14444012)));

        context.register(NECROTIDE_ALTER,
                new StructureSet(structures.getOrThrow(UltraStructures.NECROTIDE_ALTER),
                        new RandomSpreadStructurePlacement(50, 24, RandomSpreadType.LINEAR, 132321313)));

        context.register(DEEPSLATE_OBELISK,
                new StructureSet(structures.getOrThrow(UltraStructures.DEEPSLATE_OBELISK),
                        new RandomSpreadStructurePlacement(65, 35, RandomSpreadType.LINEAR, 111111111)));

        context.register(HYBOREAN_CRYPT,
                new StructureSet(structures.getOrThrow(UltraStructures.HYBOREAN_CRYPT),
                        new RandomSpreadStructurePlacement(75, 25, RandomSpreadType.LINEAR, 222222)));

        context.register(DECAYING_CHURCH,
                new StructureSet(structures.getOrThrow(UltraStructures.DECAYING_CHURCH),
                        new RandomSpreadStructurePlacement(75, 35, RandomSpreadType.LINEAR, 4)));
    }
}
