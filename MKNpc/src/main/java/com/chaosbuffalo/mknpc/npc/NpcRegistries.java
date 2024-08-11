package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.option_entries.NpcOptionEntryType;
import com.chaosbuffalo.mknpc.npc.options.NpcOptionType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

public class NpcRegistries {
    public static final ResourceLocation NPC_OPTION_TYPE_NAME = new ResourceLocation(MKNpc.MODID, "npc_option_types");
    public static IForgeRegistry<NpcOptionType<?>> NPC_OPTION_TYPES = null;
    public static final ResourceLocation NPC_OPTION_ENTRY_TYPE_NAME = new ResourceLocation(MKNpc.MODID, "npc_option_entry_types");
    public static IForgeRegistry<NpcOptionEntryType<?>> NPC_OPTION_ENTRY_TYPES = null;

    public static ResourceKey<Registry<NpcDefinition>> NPC_DEFINITIONS = ResourceKey.createRegistryKey(new ResourceLocation(MKNpc.MODID, "mknpcs"));

    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<NpcOptionType<?>>()
                .setName(NPC_OPTION_TYPE_NAME), r -> NPC_OPTION_TYPES = r);
        event.create(new RegistryBuilder<NpcOptionEntryType<?>>()
                .setName(NPC_OPTION_ENTRY_TYPE_NAME), r -> NPC_OPTION_ENTRY_TYPES = r);
    }

    public static void createDataRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(NPC_DEFINITIONS, NpcDefinition.CODEC);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(NpcRegistries::createRegistries);
        modBus.addListener(NpcRegistries::createDataRegistries);
    }
}
