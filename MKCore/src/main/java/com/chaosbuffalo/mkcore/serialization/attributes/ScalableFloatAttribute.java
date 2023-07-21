package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class ScalableFloatAttribute extends FloatAttribute implements IScalableAttribute{
    protected float min;
    protected float max;

    public ScalableFloatAttribute(String name, float min, float max) {
        super(name, min);
        this.min = min;
        this.max = max;
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createMap(ImmutableMap.of(
                ops.createString("value"), super.serialize(ops),
                ops.createString("min"), ops.createDouble(min),
                ops.createString("max"), ops.createDouble(max)
        ));
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        dynamic.get("value").get().result().ifPresent(super::deserialize);
        this.min = dynamic.get("min").asFloat(0.0f);
        this.max = dynamic.get("max").asFloat(0.0f);
    }

    @Override
    public void scale(double value) {
        setValue(MathUtils.lerp(min, max, (float) value));
    }

    public void setMax(float max) {
        this.max = max;
    }

    public void setMin(float min) {
        this.min = min;
    }
}
