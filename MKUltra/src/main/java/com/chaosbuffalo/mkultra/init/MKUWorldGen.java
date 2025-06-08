package com.chaosbuffalo.mkultra.init;


import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.world.gen.feature.structure.StaticPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MKUWorldGen {

    public static final DeferredRegister<Structure> STRUCTURE_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE, MKUltra.MODID);

    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_REGISTRY = DeferredRegister.create(
            Registries.STRUCTURE_PLACEMENT, MKUltra.MODID);

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<StaticPlacement>> STATIC_PLACEMENT =
            STRUCTURE_PLACEMENT_REGISTRY.register("static_placement",
                    () -> () -> StaticPlacement.CODEC);

    public static void register(IEventBus bus) {
        STRUCTURE_REGISTRY.register(bus);
        STRUCTURE_PLACEMENT_REGISTRY.register(bus);
    }
}
