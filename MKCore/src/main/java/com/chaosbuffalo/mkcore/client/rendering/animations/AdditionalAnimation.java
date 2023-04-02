package com.chaosbuffalo.mkcore.client.rendering.animations;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.LivingEntity;

public abstract class AdditionalAnimation<T extends LivingEntity> {
    private final Model model;

    public AdditionalAnimation(Model model) {
        this.model = model;
    }

    public abstract void apply(T entity);

    public Model getModel() {
        return model;
    }
}
