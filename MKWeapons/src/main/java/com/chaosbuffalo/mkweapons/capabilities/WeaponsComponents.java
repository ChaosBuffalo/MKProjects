package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class WeaponsComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPES = DeferredRegister.create(
            BuiltInRegistries.DATA_COMPONENT_TYPE, MKWeapons.MODID);


    public static void register(IEventBus modBus) {
        COMPONENT_TYPES.register(modBus);
    }
}
