package com.chaosbuffalo.mknpc.npc;


import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class NpcAttributeEntry {
    private double value;
    private Attribute attribute;

    public NpcAttributeEntry(){

    }

    public NpcAttributeEntry(Attribute attribute, double value){
        this.attribute = attribute;
        this.value = value;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        this.attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(dynamic.get("attributeName")
                .asString("mknpc:invalid_attribute")));
        this.value = dynamic.get("value").asDouble(attribute != null ? attribute.getDefaultValue() : 1.0);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createMap(ImmutableMap.of(
           ops.createString("attributeName"), ops.createString(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString()),
           ops.createString("value"), ops.createDouble(getValue())
        ));
    }

    public double getValue() {
        return value;
    }

    public Attribute getAttribute(){
        return attribute;
    }

    public void setAttribute(Attribute attribute){
        this.attribute = attribute;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
