package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class BaseRandomizationOption implements IRandomizationOption {
    private final ResourceLocation typeName;
    private IRandomizationSlot slot;
    private double weight;

    public BaseRandomizationOption(ResourceLocation typeName, IRandomizationSlot slot) {
        this(typeName, slot, 1.0);
    }

    public BaseRandomizationOption(ResourceLocation typeName, IRandomizationSlot slot, double weight) {
        this(typeName);
        this.slot = slot;
        this.weight = weight;
    }

    public BaseRandomizationOption(ResourceLocation typeName) {
        this.typeName = typeName;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean isApplicableToItem(ItemStack stack) {
        return true;
    }


    @Override
    public IRandomizationSlot getSlot() {
        return slot;
    }

    @Override
    public ResourceLocation getName() {
        return typeName;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}
