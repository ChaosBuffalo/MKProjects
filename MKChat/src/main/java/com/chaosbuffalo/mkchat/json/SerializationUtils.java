package com.chaosbuffalo.mkchat.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.function.BiFunction;

public class SerializationUtils {

    public static <T> BiFunction<Gson, JsonObject, T> deserialize(Class<? extends T> tClass) {
        return (gson, obj) -> gson.fromJson(obj, tClass);
    }
}
