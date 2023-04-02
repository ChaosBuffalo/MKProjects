package com.chaosbuffalo.mkcore.serialization.attributes;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.phys.Vec3;

public class Vector3dAttribute extends SimpleAttribute<Vec3> {


    public Vector3dAttribute(String name, Vec3 defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        Vec3 val = getValue();
        builder.put(ops.createString("x"), ops.createDouble(val.x()));
        builder.put(ops.createString("y"), ops.createDouble(val.y()));
        builder.put(ops.createString("z"), ops.createDouble(val.z()));
        return ops.createMap(builder.build());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        double x = dynamic.get("x").asDouble(0.0);
        double y = dynamic.get("y").asDouble(0.0);
        double z = dynamic.get("z").asDouble(0.0);
        setValue(new Vec3(x, y, z));
    }

    @Override
    public void setValueFromString(String stringValue) {

    }

    @Override
    public boolean validateString(String stringValue) {
        return false;
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return false;
    }

    @Override
    public String valueAsString() {
        return String.format("%f,%f,%f", getValue().x, getValue().y, getValue().z);
    }
}
