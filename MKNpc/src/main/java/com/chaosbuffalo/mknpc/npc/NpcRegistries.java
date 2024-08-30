package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.option_entries.NpcOptionEntryType;
import com.chaosbuffalo.mknpc.npc.options.NpcOptionType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class NpcRegistries {
    public static final ResourceLocation NPC_OPTION_TYPE_NAME = MKNpc.id("npc_option_types");
    public static final ResourceKey<Registry<NpcOptionType<?>>> NPC_OPTION_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(NPC_OPTION_TYPE_NAME);
    public static final Registry<NpcOptionType<?>> NPC_OPTION_TYPES = new RegistryBuilder<>(NPC_OPTION_TYPE_REGISTRY_KEY)
            .create();
    public static final ResourceLocation NPC_OPTION_ENTRY_TYPE_NAME = MKNpc.id("npc_option_entry_types");
    public static final ResourceKey<Registry<NpcOptionEntryType<?>>> NPC_OPTION_ENTRY_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(NPC_OPTION_ENTRY_TYPE_NAME);
    public static final Registry<NpcOptionEntryType<?>> NPC_OPTION_ENTRY_TYPES = new RegistryBuilder<>(NPC_OPTION_ENTRY_TYPE_REGISTRY_KEY)
            .create();

    public static ResourceKey<Registry<NpcDefinition>> NPC_DEFINITIONS = ResourceKey.createRegistryKey(MKNpc.id("mknpcs"));

    public static void createRegistries(NewRegistryEvent event) {
        event.register(NPC_OPTION_TYPES);
        event.register(NPC_OPTION_ENTRY_TYPES);
    }

    public static void createDataRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NPC_DEFINITIONS, NpcDefinition.CODEC);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(NpcRegistries::createRegistries);
        modBus.addListener(NpcRegistries::createDataRegistries);
    }
}
