package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class ScalableDouble implements IScalableAttribute {
    public static final Codec<ScalableDouble> CODEC = RecordCodecBuilder.<ScalableDouble>mapCodec(builder -> {
        return builder.group(
                Codec.DOUBLE.fieldOf("min").forGetter(ScalableDouble::getMin),
                Codec.DOUBLE.fieldOf("max").forGetter(ScalableDouble::getMax),
                Codec.DOUBLE.optionalFieldOf("value").forGetter(i -> i.isDefaultValue() ? Optional.empty() : Optional.of(i.value()))
        ).apply(builder, ScalableDouble::new);
    }).codec();

    private final double min;
    private final double max;
    private final double defaultValue;
    private double currentValue;

    private ScalableDouble(double min, double max, Optional<Double> value) {
        this.min = min;
        this.max = max;
        this.defaultValue = min;
        this.currentValue = value.orElse(defaultValue);
    }

    public ScalableDouble(double min, double max) {
        this.defaultValue = min;
        this.currentValue = min;
        this.min = min;
        this.max = max;
    }

    public double value() {
        return currentValue;
    }

    public void setValue(double value) {
        this.currentValue = value;
    }

    public boolean isDefaultValue() {
        return currentValue == defaultValue;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public void scale(double value) {
        setValue(MathUtils.exLerpDouble(min, max, value));
    }
}
