package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MKNpcAttributes {

    private static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MKNpc.MODID);

    public static final RegistryObject<Attribute> AGGRO_RANGE = REGISTRY.register("aggro_range",
            () -> new RangedAttribute("attribute.name.mk.aggro_range", 5, 0, 128).setSyncable(false));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
