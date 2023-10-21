package com.chaosbuffalo.mkcore.attributes;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;


public interface AttributeInstanceExtension {

    void mkcore$forceUpdate();

    default void recomputeValue() {
        mkcore$forceUpdate();
    }

    static AttributeInstanceExtension of(AttributeInstance attributeInstance) {
        return (AttributeInstanceExtension) (Object) attributeInstance;
    }

    static void recomputeValue(AttributeInstance attributeInstance) {
        of(attributeInstance).mkcore$forceUpdate();
    }
}
