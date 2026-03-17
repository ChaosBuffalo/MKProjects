package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.item.ItemCriticalStats;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
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

    public static final DataMapType<Level, Double> DIMENSION_DIFFICULTY_BONUSES = AdvancedDataMapType.builder(
                    MKCore.id("dimension_difficulty_bonuses"),
                    Registries.DIMENSION,
                    Codec.DOUBLE)
            .build();

    public static final DataMapType<Item, ItemCriticalStats> ITEM_CRITICAL_STATS = AdvancedDataMapType.builder(
                    MKCore.id("item_critical_stats"),
                    Registries.ITEM,
                    ItemCriticalStats.CODEC)
            .build();

    public static void register(IEventBus modBus) {
        modBus.addListener(CoreDataMaps::registerDataMapTypes);
    }

    private static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(ARMOR_CLASS_MAPPING);
        event.register(DIMENSION_DIFFICULTY_BONUSES);
        event.register(ITEM_CRITICAL_STATS);
    }
}
