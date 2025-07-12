package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class CoreDataMaps {

    public static final DataMapType<Item, Holder<ArmorClass>> ARMOR_CLASS_MAPPING = AdvancedDataMapType.builder(
                    MKCore.id("armor_class_mapping"),
                    Registries.ITEM,
                    ArmorClass.REFERENCE_CODEC)
            .synced(ArmorClass.REFERENCE_CODEC, false)
            .build();


    public static void register(IEventBus modBus) {
        modBus.addListener(CoreDataMaps::registerDataMapTypes);
    }

    private static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(ARMOR_CLASS_MAPPING);
    }
}
