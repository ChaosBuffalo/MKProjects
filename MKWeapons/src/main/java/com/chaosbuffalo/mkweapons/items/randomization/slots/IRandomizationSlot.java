package com.chaosbuffalo.mkweapons.items.randomization.slots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public interface IRandomizationSlot {

    boolean isPermanent();

    ResourceLocation getName();

    Component getDisplayName();
}
