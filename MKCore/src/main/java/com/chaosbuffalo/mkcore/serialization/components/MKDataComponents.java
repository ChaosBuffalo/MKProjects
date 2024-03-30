package com.chaosbuffalo.mkcore.serialization.components;

public class MKDataComponents {

    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();


    public static <T> MKComponentKey<T> defineId(Class<? extends IMKDataMapProvider> class_, String name, MKDataSerializer<T> entityDataSerializer) {
        int i = ID_REGISTRY.define(class_);
        if (i > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        } else {
            return new MKComponentKey<>(i, name, entityDataSerializer);
        }
    }

    public static ComponentDefiner definer(Class<? extends IMKDataMapProvider> class_) {
        return new ComponentDefiner() {
            @Override
            public <T> MKComponentKey<T> define(String name, MKDataSerializer<T> serializer) {
                return MKDataComponents.defineId(class_, name, serializer);
            }
        };
    }

    public interface ComponentDefiner {
        <T> MKComponentKey<T> define(String name, MKDataSerializer<T> serializer);
    }


}
