package com.chaosbuffalo.mkcore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

public class AttributeTooltipManager {

    public interface ItemAttributeRenderer {
        void render(ItemStack stack, Player player, Holder<Attribute> attribute,
                    AttributeModifier modifier, Consumer<Component> output);
    }

    private static final Map<Holder<Attribute>, ItemAttributeRenderer> attributeRendererMap = new IdentityHashMap<>(6);

    public static void registerAttributeRenderer(Holder<Attribute> attribute, ItemAttributeRenderer renderer) {
        attributeRendererMap.put(attribute, renderer);
    }

    @Nullable
    public static MutableComponent makePlusOrTakeText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                      double amount, double displayAmount) {
        return makePlusOrTakeText(attribute, modifier, amount, displayAmount, ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT::format);
    }

    @Nullable
    public static MutableComponent makePlusOrTakeText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                      double amount, double displayAmount, DoubleFunction<String> formatter) {
        if (amount > 0.0D) {
            return makePlusText(attribute, modifier, displayAmount, formatter);
        } else if (amount < 0.0D) {
            return makeTakeText(attribute, modifier, displayAmount, formatter);
        }
        return null;
    }

    public static MutableComponent makeTakeText(Holder<Attribute> attribute, AttributeModifier attributemodifier,
                                                double displayAmount) {
        return makeTakeText(attribute, attributemodifier, displayAmount, ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeTakeText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                double displayAmount, DoubleFunction<String> formatter) {
        return Component.translatable("attribute.modifier.take." + modifier.operation().id(),
                        formatter.apply(-displayAmount),
                        Component.translatable(attribute.value().getDescriptionId()))
                .withStyle(attribute.value().getStyle(false));
    }

    public static MutableComponent makePlusText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                double displayAmount) {
        return makePlusText(attribute, modifier, displayAmount, ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT::format);
    }

    @Nonnull
    public static MutableComponent makePlusText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                double displayAmount, DoubleFunction<String> formatter) {
        return Component.translatable("attribute.modifier.plus." + modifier.operation().id(),
                        formatter.apply(displayAmount),
                        Component.translatable(attribute.value().getDescriptionId()))
                .withStyle(attribute.value().getStyle(true));
    }

    @Nonnull
    public static MutableComponent makeEqualsText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                  double displayAmount, DoubleFunction<String> formatter) {
        return CommonComponents.space()
                .append(Component.translatable(
                        "attribute.modifier.equals." + modifier.operation().id(),
                        formatter.apply(displayAmount),
                        Component.translatable(attribute.value().getDescriptionId()))
                )
                .withStyle(ChatFormatting.DARK_GREEN);
    }

    public static boolean renderModifier(@Nullable Player player, ItemStack stack, Holder<Attribute> attribute,
                                         AttributeModifier modifier, Consumer<Component> tooltipAdder) {
        ItemAttributeRenderer renderer = attributeRendererMap.get(attribute);
        if (renderer != null) {
            renderer.render(stack, player, attribute, modifier, tooltipAdder);
            return true;
        }

        return false;
    }
}
