package com.chaosbuffalo.mkultra.init;


import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.StaticPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class MKUWorldGen {

    public static final DeferredRegister<Structure> STRUCTURE_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE, MKUltra.MODID);

    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE_PLACEMENT, MKUltra.MODID);

    public static final RegistryObject<StructurePlacementType<StaticPlacement>> STATIC_PLACEMENT =
            STRUCTURE_PLACEMENT_REGISTRY.register("static_placement",
                    () -> () -> StaticPlacement.CODEC);

//    public static final RegistryObject<StructureFeature<JigsawConfiguration>> NECROTIDE_ALTER = STRUCTURE_REGISTRY.register("necrotide_alter",
//            () -> new MKJigsawStructure(JigsawConfiguration.CODEC, 0, true, true, (piece) -> true, false)
//                    .addEvent("summon_golem", new SpawnNpcDefinitionEvent(new ResourceLocation(MKUltra.MODID, "necrotide_golem"),
//                            "golem_spawn", "golem_look", MKEntity.NonCombatMoveType.STATIONARY)
//                            .addNotableDeadCondition(new ResourceLocation(MKUltra.MODID, "skeletal_lock"), true)
//                            .addTrigger(StructureEvent.EventTrigger.ON_DEATH)));
//

    public static void register(IEventBus bus) {
        STRUCTURE_REGISTRY.register(bus);
        STRUCTURE_PLACEMENT_REGISTRY.register(bus);
    }


}
