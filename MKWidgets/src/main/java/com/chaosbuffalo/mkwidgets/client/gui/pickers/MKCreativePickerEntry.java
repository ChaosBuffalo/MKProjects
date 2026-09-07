package com.chaosbuffalo.mkwidgets.client.gui.pickers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record MKCreativePickerEntry(ResourceLocation id, Component displayName, ItemStack displayStack,
                                    String searchText) {
}
