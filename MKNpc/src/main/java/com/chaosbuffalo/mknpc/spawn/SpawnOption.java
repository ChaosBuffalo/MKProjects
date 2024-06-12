package com.chaosbuffalo.mknpc.spawn;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class SpawnOption {
    public static final Codec<SpawnOption> CODEC = RecordCodecBuilder.<SpawnOption>mapCodec(builder -> {
        return builder.group(
                Codec.DOUBLE.fieldOf("weight").forGetter(SpawnOption::getWeight),
                ResourceLocation.CODEC.fieldOf("definition").forGetter(SpawnOption::getDefinitionName)
        ).apply(builder, SpawnOption::new);
    }).codec();

    private double weight;
    private ResourceLocation definitionName;

    public SpawnOption(double weight, ResourceLocation definition) {
        this.weight = weight;
        this.definitionName = definition;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setDefinition(ResourceLocation definition) {
        this.definitionName = definition;
    }

    public NpcDefinition getDefinition() {
        return NpcDefinitionManager.getDefinition(definitionName);
    }

    public ResourceLocation getDefinitionName() {
        return definitionName;
    }

    public NpcDefinitionClient getDefinitionClient() {
        return NpcDefinitionManager.CLIENT_DEFINITIONS.get(definitionName);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static SpawnOption deserialize(Tag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
