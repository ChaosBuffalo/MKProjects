package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class ScalableDouble {
    public static final MapCodec<ScalableDouble> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.DOUBLE.fieldOf("min").forGetter(ScalableDouble::getMin),
            Codec.DOUBLE.fieldOf("max").forGetter(ScalableDouble::getMax),
            Codec.DOUBLE.optionalFieldOf("value").forGetter(i -> i.isDefaultValue() ? Optional.empty() : Optional.of(i.value()))
    ).apply(builder, ScalableDouble::new));

    private final double min;
    private final double max;
    private final double currentValue;

    private ScalableDouble(double min, double max, Optional<Double> value) {
        this.min = min;
        this.max = max;
        this.currentValue = value.orElse(min);
    }

    public ScalableDouble(double min, double max) {
        this.currentValue = min;
        this.min = min;
        this.max = max;
    }

    public double value() {
        return currentValue;
    }

    public boolean isDefaultValue() {
        return currentValue == min;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public ScalableDouble copyScaled(double value) {
        double newValue = MathUtils.exLerpDouble(min, max, value);
        return new ScalableDouble(min, max, Optional.of(newValue));
    }
}
