package com.chaosbuffalo.mknpc.npc.options.binding;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.npc.options.BindingNpcOption;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


public class BoundSpawnConfiguration {
    public static final Codec<BoundSpawnConfiguration> CODEC = Codec.unboundedMap(
                    ResourceLocation.CODEC, Codec.unboundedMap(ResourceLocation.CODEC, NpcDefinitionManager.ENTRY_CODEC))
            .xmap(BoundSpawnConfiguration::new, i -> i.definitionMap);


    private final Map<ResourceLocation, Map<ResourceLocation, IBoundNpcOptionValue>> definitionMap;

    private BoundSpawnConfiguration(Map<ResourceLocation, Map<ResourceLocation, IBoundNpcOptionValue>> map) {
        definitionMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, Map<ResourceLocation, IBoundNpcOptionValue>> entry : map.entrySet()) {
            definitionMap.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
    }

    public BoundSpawnConfiguration() {
        definitionMap = new HashMap<>();
    }

    public boolean hasBoundValue(ResourceLocation definitionName, ResourceLocation optionName) {
        return getBoundValue(definitionName, optionName) != null;
    }

    @Nullable
    public IBoundNpcOptionValue getBoundValue(ResourceLocation definitionName, ResourceLocation optionName) {
        Map<ResourceLocation, IBoundNpcOptionValue> defOpts = definitionMap.get(definitionName);
        if (defOpts != null) {
            return defOpts.get(optionName);
        }
        return null;
    }

    public void addBoundValue(ResourceLocation definitionName, BindingNpcOption option,
                              IBoundNpcOptionValue optionEntry) {
        definitionMap.computeIfAbsent(definitionName, n -> new HashMap<>())
                .put(option.getName(), optionEntry);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static <D> BoundSpawnConfiguration deserialize(DynamicOps<D> ops, D input) {
        return CODEC.parse(ops, input).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
