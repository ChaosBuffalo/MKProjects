package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface IRandomizationOption extends IDynamicMapTypedSerializer {

    ResourceLocation getName();

    void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty);

    boolean isApplicableToItem(ItemStack stack);

    double getWeight();

    void setWeight(double weight);

    void setSlot(IRandomizationSlot slot);

    IRandomizationSlot getSlot();
}
