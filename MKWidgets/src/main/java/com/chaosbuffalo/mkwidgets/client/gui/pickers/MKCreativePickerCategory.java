package com.chaosbuffalo.mkwidgets.client.gui.pickers;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record MKCreativePickerCategory(String id, Component displayName, ItemStack icon) {
}
