package com.chaosbuffalo.mkcore.serialization.attributes;

import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.Locale;

public class EnumAttribute<T extends Enum<T>> extends CodecAttribute<T> {
    public EnumAttribute(String name, T defaultValue, Class<T> enumClass) {
        super(name, defaultValue, createCodec(enumClass));
    }

    private static <T extends Enum<T>> Codec<T> createCodec(Class<T> enumClass) {
        return Codec.STRING.xmap(
                value -> Arrays.stream(enumClass.getEnumConstants())
                        .filter(entry -> entry.name().equalsIgnoreCase(value))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown enum value '%s' for %s".formatted(value, enumClass.getSimpleName()))),
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }
}
