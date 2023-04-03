package com.chaosbuffalo.mkweapons.items.randomization.slots;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface IRandomizationSlot {

    boolean isPermanent();

    ResourceLocation getName();

    Component getDisplayName();
}
