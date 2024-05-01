package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class ScalableFloat implements IScalableAttribute {
    public static final Codec<ScalableFloat> CODEC = RecordCodecBuilder.<ScalableFloat>mapCodec(builder -> {
        return builder.group(
                Codec.FLOAT.fieldOf("min").forGetter(i -> i.min),
                Codec.FLOAT.fieldOf("max").forGetter(i -> i.max),
                Codec.FLOAT.optionalFieldOf("value").forGetter(i -> i.isDefaultValue() ? Optional.empty() : Optional.of(i.value()))
        ).apply(builder, ScalableFloat::new);
    }).codec();

    private final float min;
    private final float max;
    private final float defaultValue;
    private float currentValue;

    private ScalableFloat(float min, float max, Optional<Float> value) {
        this.min = min;
        this.max = max;
        this.defaultValue = min;
        this.currentValue = value.orElse(defaultValue);
    }

    public ScalableFloat(float min, float max) {
        this.defaultValue = min;
        this.currentValue = min;
        this.min = min;
        this.max = max;
    }

    public float value() {
        return currentValue;
    }

    public void setValue(float value) {
        this.currentValue = value;
    }

    public boolean isDefaultValue() {
        return currentValue == defaultValue;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @Override
    public void scale(double value) {
        setValue(MathUtils.exLerp(min, max, (float) value));
    }
}
