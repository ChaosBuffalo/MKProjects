package com.chaosbuffalo.mkcore.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;


public class MKRangedAttribute extends RangedAttribute {
    private boolean additionIsPercentage;
    private ResourceLocation name;

    public MKRangedAttribute(String name, double defaultValue, double minimumValueIn, double maximumValueIn) {
        super(name, defaultValue, minimumValueIn, maximumValueIn);
    }

    public MKRangedAttribute setAdditionIsPercentage(boolean set) {
        additionIsPercentage = set;
        return this;
    }

    public MKRangedAttribute setName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    public ResourceLocation getName() {
        return name;
    }

    public boolean displayAdditionAsPercentage() {
        return additionIsPercentage;
    }
}
