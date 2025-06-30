package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import javax.annotation.Nullable;
import java.util.Optional;

@EventBusSubscriber(modid = MKFactionMod.MODID)
public class MKFactionRegistry {
    public static final ResourceKey<Registry<MKFaction>> FACTION_REGISTRY_KEY = ResourceKey.createRegistryKey(MKFactionMod.id("factions"));

    @Nullable
    public static MKFaction getFaction(RegistryAccess registryAccess, ResourceLocation factionId) {
        return registryAccess.registryOrThrow(FACTION_REGISTRY_KEY).get(factionId);
    }

    public static Optional<Holder.Reference<MKFaction>> getFactionHolder(RegistryAccess registryAccess, ResourceLocation factionId) {
        return registryAccess.registryOrThrow(FACTION_REGISTRY_KEY).getHolder(factionId);
    }

    public static Optional<Holder.Reference<MKFaction>> getFactionHolder(RegistryAccess registryAccess, ResourceKey<MKFaction> factionId) {
        return registryAccess.registryOrThrow(FACTION_REGISTRY_KEY).getHolder(factionId);
    }

    @SubscribeEvent
    public static void createDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(FACTION_REGISTRY_KEY, MKFaction.DIRECT_CODEC, MKFaction.DIRECT_CODEC);
    }
}
