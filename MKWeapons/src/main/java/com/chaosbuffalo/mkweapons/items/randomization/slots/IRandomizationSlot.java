package com.chaosbuffalo.mkweapons.items.randomization.slots;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface IRandomizationSlot {
    Codec<IRandomizationSlot> CODEC = ResourceLocation.CODEC.comapFlatMap(slotName -> {
        IRandomizationSlot slot = RandomizationSlotManager.getSlotFromName(slotName);
        if (slot != null) {
            return DataResult.success(slot);
        }
        return DataResult.error(() -> "Randomization slot " + slotName + " not registered with RandomizationSlotManager");
    }, IRandomizationSlot::getName);


    boolean isPermanent();

    ResourceLocation getName();

    Component getDisplayName();
}
