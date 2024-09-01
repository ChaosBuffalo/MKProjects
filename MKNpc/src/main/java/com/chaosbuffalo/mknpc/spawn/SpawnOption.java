package com.chaosbuffalo.mknpc.spawn;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class SpawnOption implements INBTSerializable<CompoundTag> {
    private double weight;
    private ResourceLocation definitionName;

    public SpawnOption() {
        this.weight = 1.0;
    }

    public SpawnOption(double weight, ResourceLocation definition) {
        this.weight = weight;
        this.definitionName = definition;
    }

    public void setDefinition(ResourceLocation definition) {
        this.definitionName = definition;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public NpcDefinition getDefinition(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(NpcRegistries.NPC_DEFINITIONS).get(definitionName);
    }

    public NpcDefinitionClient getDefinitionClient() {
        return NpcDefinitionManager.CLIENT_DEFINITIONS.get(definitionName);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("definition", definitionName.toString());
        tag.putDouble("weight", getWeight());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ResourceLocation definitionName = ResourceLocation.parse(nbt.getString("definition"));
        setDefinition(definitionName);
        setWeight(nbt.getDouble("weight"));
    }
}
