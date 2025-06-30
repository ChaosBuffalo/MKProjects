package com.chaosbuffalo.mknpc.components;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NpcComponents {
    public static final DeferredRegister.DataComponents COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MKNpc.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpawnerDataComponent>> SPAWNER_DATA = COMPONENT_TYPES
            .registerComponentType("spawner_data", builder -> {
                return builder.persistent(SpawnerDataComponent.CODEC).networkSynchronized(SpawnerDataComponent.STREAM_CODEC);
            });


    public static void register(IEventBus modBus) {
        COMPONENT_TYPES.register(modBus);
    }

}
