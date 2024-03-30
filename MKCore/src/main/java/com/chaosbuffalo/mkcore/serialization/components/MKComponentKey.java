package com.chaosbuffalo.mkcore.serialization.components;


public record MKComponentKey<T>(int id, String name, MKDataSerializer<T> serializer) {

}
