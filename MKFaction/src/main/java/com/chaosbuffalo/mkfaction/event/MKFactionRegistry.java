package com.chaosbuffalo.mkfaction.event;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = MKFactionMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MKFactionRegistry {
    public static final ResourceKey<Registry<MKFaction>> FACTION_REGISTRY_KEY = ResourceKey.createRegistryKey(MKFactionMod.id("factions"));

    public static final Registry<MKFaction> FACTION_REGISTRY = new RegistryBuilder<>(FACTION_REGISTRY_KEY).create();

    @Nullable
    public static MKFaction getFaction(ResourceLocation name) {
        return FACTION_REGISTRY.get(name);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent event) {
        event.register(FACTION_REGISTRY);
    }
}
