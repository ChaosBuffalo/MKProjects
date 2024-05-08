package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class WorldPermanentSpawnConfiguration {
    public static final Codec<WorldPermanentSpawnConfiguration> CODEC = Codec.unboundedMap(
                    ResourceLocation.CODEC, Codec.unboundedMap(ResourceLocation.CODEC, NpcDefinitionManager.ENTRY_CODEC))
            .xmap(WorldPermanentSpawnConfiguration::new, i -> i.definitionMap);


    private final Map<ResourceLocation, Map<ResourceLocation, INpcOptionEntry>> definitionMap;

    private WorldPermanentSpawnConfiguration(Map<ResourceLocation, Map<ResourceLocation, INpcOptionEntry>> map) {
        definitionMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, Map<ResourceLocation, INpcOptionEntry>> entry : map.entrySet()) {
            definitionMap.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
    }

    public WorldPermanentSpawnConfiguration() {
        definitionMap = new HashMap<>();
    }

    public boolean hasDefinition(ResourceLocation definitionName) {
        return definitionMap.containsKey(definitionName);
    }

    public boolean hasAttributeEntry(ResourceLocation definitionName, ResourceLocation optionName) {
        return definitionMap.containsKey(definitionName) && definitionMap.get(definitionName).containsKey(optionName)
                && definitionMap.get(definitionName).get(optionName).isValid();
    }

    public INpcOptionEntry getOptionEntry(NpcDefinition definition, WorldPermanentOption option) {
        return definitionMap.get(definition.getDefinitionName()).get(option.getName());
    }

    public void addAttributeEntry(NpcDefinition definition, WorldPermanentOption option,
                                  INpcOptionEntry optionEntry) {
        if (!hasDefinition(definition.getDefinitionName())) {
            definitionMap.put(definition.getDefinitionName(), new HashMap<>());
        }
        definitionMap.get(definition.getDefinitionName()).put(option.getName(), optionEntry);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static <D> WorldPermanentSpawnConfiguration deserialize(DynamicOps<D> ops, D input) {
        return CODEC.parse(ops, input).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
