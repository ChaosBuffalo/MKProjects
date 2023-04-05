package com.chaosbuffalo.mkweapons.items.randomization.slots;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RandomizationSlot implements IRandomizationSlot {
    private final ResourceLocation name;
    private final ChatFormatting textColor;
    private final boolean permanent;

    public RandomizationSlot(ResourceLocation name, ChatFormatting textColor, boolean permanent) {
        this.name = name;
        this.textColor = textColor;
        this.permanent = permanent;
    }

    @Override
    public boolean isPermanent() {
        return permanent;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    public ChatFormatting getTextColor() {
        return textColor;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(String.format("%s.randomization_slot.%s.name",
                getName().getNamespace(), getName().getPath())).withStyle(getTextColor());
    }
}
