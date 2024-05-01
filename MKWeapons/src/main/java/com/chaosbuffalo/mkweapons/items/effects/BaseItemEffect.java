package com.chaosbuffalo.mkweapons.items.effects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseItemEffect implements IItemEffect {
    private final ResourceLocation name;
    protected final ChatFormatting color;

    public BaseItemEffect(ResourceLocation name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public ResourceLocation getTypeName() {
        return name;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable(String.format("%s.%s.name",
                this.getTypeName().getNamespace(), this.getTypeName().getPath())).withStyle(color));
    }
}
