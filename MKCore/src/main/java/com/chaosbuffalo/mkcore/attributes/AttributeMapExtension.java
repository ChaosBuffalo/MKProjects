package com.chaosbuffalo.mkcore.attributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.function.Consumer;

public interface AttributeMapExtension {
    void mkcore$setAttributeModifiedHandler(Consumer<AttributeInstance> handler);

    static AttributeMapExtension of(AttributeMap instance) {
        return (AttributeMapExtension) instance;
    }

    static void setModificationHandler(LivingEntity livingEntity, Consumer<AttributeInstance> handler) {
        of(livingEntity.getAttributes()).mkcore$setAttributeModifiedHandler(handler);
    }
}
