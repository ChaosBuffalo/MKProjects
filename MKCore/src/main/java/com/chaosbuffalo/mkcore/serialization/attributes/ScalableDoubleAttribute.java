package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class ScalableDoubleAttribute extends DoubleAttribute implements IScalableAttribute {
    protected double min;
    protected double max;

    public ScalableDoubleAttribute(String name, double min, double max) {
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

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        dynamic.get("value").get().result().ifPresent(super::deserialize);
        this.min = dynamic.get("min").asDouble(0.0);
        this.max = dynamic.get("max").asDouble(0.0);
    }

    @Override
    public void scale(double value) {
        setValue(MathUtils.exLerpDouble(min, max, value));
    }
}
