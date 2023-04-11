package com.chaosbuffalo.mkultra.data.registries;

import com.chaosbuffalo.mknpc.data.MKJigsawBuilder;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.event.SpawnNpcDefinitionEvent;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.UltraTags;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.DesertTempleVillagePools;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.IntroCastlePools;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.NecrotideAlterPools;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Map;

public class UltraStructures {

    public static ResourceKey<Structure> INTRO_CASTLE = createKey("intro_castle");
    public static ResourceKey<Structure> DESERT_TEMPLE_VILLAGE = createKey("desert_temple_village");
    public static ResourceKey<Structure> NECROTIDE_ALTER = createKey("necrotide_alter");

    public static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(MKUltra.MODID, name));
    }

    public static void bootstrap(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> templates = context.lookup(Registries.TEMPLATE_POOL);

        context.register(INTRO_CASTLE,
                new MKJigsawBuilder(INTRO_CASTLE.location(),
                        emptySpawnsStructure(biomes.getOrThrow(UltraTags.Biomes.HAS_INTRO_CASTLE),
                                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                        templates.getOrThrow(IntroCastlePools.ISLAND_POOL)).build());

        context.register(DESERT_TEMPLE_VILLAGE,
                new MKJigsawBuilder(DESERT_TEMPLE_VILLAGE.location(),
                        emptySpawnsStructure(biomes.getOrThrow(UltraTags.Biomes.HAS_DESERT_TEMPLE_VILLAGE),
                                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                        templates.getOrThrow(DesertTempleVillagePools.DESERT_TEMPLE_VILLAGE_BASE)).build());

        context.register(NECROTIDE_ALTER,
                new MKJigsawBuilder(NECROTIDE_ALTER.location(),
                        emptySpawnsStructure(biomes.getOrThrow(UltraTags.Biomes.HAS_NECROTIDE_ALTER),
                                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
                        templates.getOrThrow(NecrotideAlterPools.BASE))
                        .addEvent("summon_golem", new SpawnNpcDefinitionEvent(
                                new ResourceLocation(MKUltra.MODID, "necrotide_golem"),
                                "golem_spawn", "golem_look", MKEntity.NonCombatMoveType.STATIONARY)
                                .addNotableDeadCondition(new ResourceLocation(MKUltra.MODID, "skeletal_lock"), true)
                                .addTrigger(StructureEvent.EventTrigger.ON_DEATH)).build());
    }

    public static Structure.StructureSettings structure(HolderSet<Biome> biomes, GenerationStep.Decoration step, TerrainAdjustment adjustment) {
        return structure(biomes, Map.of(), step, adjustment);
    }

    public static Structure.StructureSettings emptySpawnsStructure(HolderSet<Biome> biomes, GenerationStep.Decoration step, TerrainAdjustment adjustment) {
        return structure(biomes,
                Map.of(
                        MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedRandomList.create())
                ),
                step, adjustment);
    }

    public static Structure.StructureSettings structure(HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> mobs, GenerationStep.Decoration step, TerrainAdjustment adjustment) {
        return new Structure.StructureSettings(biomes, mobs, step, adjustment);
    }
}
