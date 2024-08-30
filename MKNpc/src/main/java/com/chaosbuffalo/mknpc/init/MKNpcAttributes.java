package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKNpcAttributes {

    private static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(Registries.ATTRIBUTE, MKNpc.MODID);

    public static final DeferredHolder<Attribute, Attribute> AGGRO_RANGE = REGISTRY.register("aggro_range",
            () -> new RangedAttribute("attribute.name.mk.aggro_range", 5, 0, 128).setSyncable(false));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
