package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IRandomizationOption {
    Codec<IRandomizationOption> CODEC = RandomizationOptionManager.RANDOMIZATION_OPTION_CODEC;

    ResourceLocation getName();

    void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty);

    boolean isApplicableToItem(ItemStack stack);

    double getWeight();

    void setWeight(double weight);

    IRandomizationSlot getSlot();

    default <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
