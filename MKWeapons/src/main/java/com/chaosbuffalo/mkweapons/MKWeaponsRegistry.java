package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkweapons.capabilities.WeaponsAttachments;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.init.MKWeaponEffects;
import com.chaosbuffalo.mkweapons.init.MKWeaponsCommands;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public class MKWeaponsRegistry {
    public static final ResourceKey<Registry<LootTier>> LOOT_TIER_REGISTRY_KEY = ResourceKey.createRegistryKey(MKWeapons.id("loot_tier"));

    public static void setup(IEventBus modBus) {
        MKWeaponsParticles.register(modBus);
        MKWeaponsItems.register(modBus);
        MKWeaponsCommands.register(modBus);
        MKWeaponEffects.register(modBus);
        WeaponsAttachments.register(modBus);
        WeaponsComponents.register(modBus);

        modBus.addListener(MKWeaponsRegistry::createDataPackRegistries);
    }

    public static void createDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(LOOT_TIER_REGISTRY_KEY, LootTier.CODEC);
    }
}
