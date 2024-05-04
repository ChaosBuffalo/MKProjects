package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemModifierEffect extends BaseItemEffect {
    protected final List<AttributeOptionEntry> modifiers;

    public ItemModifierEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
        modifiers = new ArrayList<>();
    }


    public void addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier) {
        modifiers.add(new AttributeOptionEntry(attribute, attributeModifier, attributeModifier.getAmount(), attributeModifier.getAmount()));
    }

    public List<AttributeOptionEntry> getModifiers() {
        return modifiers;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {

    }
}
