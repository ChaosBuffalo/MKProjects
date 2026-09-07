package com.chaosbuffalo.mkwidgets.client.gui.pickers;

import net.minecraft.client.Minecraft;

import java.util.List;

public interface MKCreativePickerSource {
    List<MKCreativePickerCategory> categories(Minecraft minecraft);

    List<MKCreativePickerEntry> entries(Minecraft minecraft, MKCreativePickerCategory category, String query);
}
