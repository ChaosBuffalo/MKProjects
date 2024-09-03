package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class ScalableFloat {
    public static final MapCodec<ScalableFloat> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.FLOAT.fieldOf("min").forGetter(i -> i.min),
            Codec.FLOAT.fieldOf("max").forGetter(i -> i.max),
            Codec.FLOAT.optionalFieldOf("value").forGetter(i -> i.isDefaultValue() ? Optional.empty() : Optional.of(i.value()))
    ).apply(builder, ScalableFloat::new));

    private final float min;
    private final float max;
    private final float currentValue;

    private ScalableFloat(float min, float max, Optional<Float> value) {
        this.min = min;
        this.max = max;
        this.currentValue = value.orElse(min);
    }

    public ScalableFloat(float min, float max) {
        this.currentValue = min;
        this.min = min;
        this.max = max;
    }

    public float value() {
        return currentValue;
    }

    public boolean isDefaultValue() {
        return currentValue == min;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public ScalableFloat copyScaled(double value) {
        float newValue = MathUtils.exLerp(min, max, (float) value);
        return new ScalableFloat(min, max, Optional.of(newValue));
    }
}
