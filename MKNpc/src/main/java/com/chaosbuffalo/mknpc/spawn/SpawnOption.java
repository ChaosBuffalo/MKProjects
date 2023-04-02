package com.chaosbuffalo.mknpc.spawn;

import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public class SpawnOption implements INBTSerializable<CompoundTag> {
    private double weight;
    private ResourceLocation definitionName;

    public SpawnOption(){
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

    public NpcDefinition getDefinition() {
        return NpcDefinitionManager.getDefinition(definitionName);
    }

    public NpcDefinitionClient getDefinitionClient(){
        return NpcDefinitionManager.CLIENT_DEFINITIONS.get(definitionName);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("definition", getDefinition().getDefinitionName().toString());
        tag.putDouble("weight", getWeight());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ResourceLocation definitionName = new ResourceLocation(nbt.getString("definition"));
        setDefinition(definitionName);
        setWeight(nbt.getDouble("weight"));
    }
}
