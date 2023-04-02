package com.chaosbuffalo.mkcore.serialization.attributes;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationAttribute extends SimpleAttribute<ResourceLocation> {


    public ResourceLocationAttribute(String name, ResourceLocation defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createString(getValue().toString());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(new ResourceLocation(dynamic.asString(getDefaultValue().toString())));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(new ResourceLocation(stringValue));
    }

    @Override
    public boolean validateString(String stringValue) {
        return true;
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty();
    }

    @Override
    public String valueAsString() {
        return getValue().toString();
    }
}
