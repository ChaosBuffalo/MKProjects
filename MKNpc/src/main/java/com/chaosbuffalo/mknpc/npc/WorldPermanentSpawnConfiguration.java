package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;


public class WorldPermanentSpawnConfiguration implements INBTSerializable<CompoundTag> {

    private final HashMap<ResourceLocation, HashMap<ResourceLocation, INpcOptionEntry>> definitionMap;

    public WorldPermanentSpawnConfiguration(){
        definitionMap = new HashMap<>();
    }

    public boolean hasDefinition(ResourceLocation definitionName){
        return definitionMap.containsKey(definitionName);
    }

    public boolean hasAttributeEntry(ResourceLocation definitionName, ResourceLocation optionName){
        return definitionMap.containsKey(definitionName) && definitionMap.get(definitionName).containsKey(optionName);
    }

    public INpcOptionEntry getOptionEntry(NpcDefinition definition, WorldPermanentOption option){
        return definitionMap.get(definition.getDefinitionName()).get(option.getName());
    }

    public void addAttributeEntry(NpcDefinition definition, WorldPermanentOption option,
                                  INpcOptionEntry optionEntry){
        if (!hasDefinition(definition.getDefinitionName())){
            definitionMap.put(definition.getDefinitionName(), new HashMap<>());
        }
        definitionMap.get(definition.getDefinitionName()).put(option.getName(), optionEntry);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (ResourceLocation definitionName : definitionMap.keySet()){
            CompoundTag defTag = new CompoundTag();
            HashMap<ResourceLocation, INpcOptionEntry> optionMap = definitionMap.get(definitionName);
            for (ResourceLocation attributeName : optionMap.keySet()){
                INpcOptionEntry entry = optionMap.get(attributeName);
                defTag.put(attributeName.toString(), entry.serializeNBT());
            }
            tag.put(definitionName.toString(), defTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        definitionMap.clear();
        for (String defKey : nbt.getAllKeys()){
            ResourceLocation defLoc = new ResourceLocation(defKey);
            CompoundTag defTag = nbt.getCompound(defKey);
            HashMap<ResourceLocation, INpcOptionEntry> entryMap = new HashMap<>();
            for (String entryKey : defTag.getAllKeys()){
                ResourceLocation entryLoc = new ResourceLocation(entryKey);
                INpcOptionEntry entry = NpcDefinitionManager.getNpcOptionEntry(entryLoc);
                if (entry != null){
                    entry.deserializeNBT(defTag.getCompound(entryKey));
                    entryMap.put(entryLoc, entry);
                }
            }
            definitionMap.put(defLoc, entryMap);
        }
    }
}
