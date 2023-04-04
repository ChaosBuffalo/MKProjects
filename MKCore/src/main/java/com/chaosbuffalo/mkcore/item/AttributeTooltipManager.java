package com.chaosbuffalo.mkcore.item;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

public class AttributeTooltipManager {

    // Why are these protected in Item? Not sure if it's worth an AT
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    public interface ItemAttributeRenderer {
        void render(ItemStack stack, EquipmentSlot equipmentSlotType, Player player, Attribute attribute,
                    AttributeModifier modifier, Consumer<Component> output);
    }

    public static final DecimalFormat DECIMALFORMAT = Util.make(new DecimalFormat("#.##"), format -> {
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    private static final Map<Attribute, ItemAttributeRenderer> attributeRendererMap = new IdentityHashMap<>(6);

    public static void registerAttributeRenderer(Attribute attribute, ItemAttributeRenderer renderer) {
        attributeRendererMap.put(attribute, renderer);
    }

    static void renderAttribute(ItemStack stack, EquipmentSlot equipmentSlotType,
                                Player player, Attribute attribute,
                                AttributeModifier modifier,
                                Consumer<Component> output) {
        attributeRendererMap.getOrDefault(attribute, AttributeTooltipManager::defaultAttributeRender)
                .render(stack, equipmentSlotType, player, attribute, modifier, output);
    }

    static void defaultAttributeRender(ItemStack stack, EquipmentSlot equipmentSlotType, Player player,
                                       Attribute attribute, AttributeModifier modifier, Consumer<Component> output) {
        double amount = modifier.getAmount();
        boolean absolute = false;
        if (player != null) {
            if (modifier.getId().equals(BASE_ATTACK_DAMAGE_UUID)) {
                amount += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                amount += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
                absolute = true;
            } else if (modifier.getId().equals(BASE_ATTACK_SPEED_UUID)) {
                amount += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                absolute = true;
            }
        }

        double displayAmount;
        if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE)) {
                displayAmount = amount * 10.0D;
            } else {
                displayAmount = amount;
            }
        } else {
            displayAmount = amount * 100.0D;
        }

        Component line = absolute ?
                makeEqualsText(attribute, modifier, displayAmount) :
                makePlusOrTakeText(attribute, modifier, amount, displayAmount);

        output.accept(line);
    }

    @Nullable
    public static MutableComponent makePlusOrTakeText(Attribute attribute, AttributeModifier modifier,
                                                      double amount, double displayAmount) {
        return makePlusOrTakeText(attribute, modifier, amount, displayAmount, DECIMALFORMAT::format);
    }

    @Nullable
    public static MutableComponent makePlusOrTakeText(Attribute attribute, AttributeModifier modifier,
                                                      double amount, double displayAmount, DoubleFunction<String> formatter) {
        if (amount > 0.0D) {
            return makePlusText(attribute, modifier, displayAmount, formatter);
        } else if (amount < 0.0D) {
            return makeTakeText(attribute, modifier, displayAmount, formatter);
        }
        return null;
    }

    public static MutableComponent makeTakeText(Attribute attribute, AttributeModifier attributemodifier,
                                                double displayAmount) {
        return makeTakeText(attribute, attributemodifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeTakeText(Attribute attribute, AttributeModifier modifier,
                                                double displayAmount, DoubleFunction<String> formatter) {
        displayAmount = displayAmount * -1.0D;
        return Component.translatable("attribute.modifier.take." + modifier.getOperation().toValue(),
                formatter.apply(displayAmount),
                Component.translatable(attribute.getDescriptionId()))
                .withStyle(ChatFormatting.RED);
    }

    public static MutableComponent makePlusText(Attribute attribute, AttributeModifier modifier,
                                                double displayAmount) {
        return makePlusText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makePlusText(Attribute attribute, AttributeModifier modifier,
                                                double displayAmount, DoubleFunction<String> formatter) {
        return Component.translatable("attribute.modifier.plus." + modifier.getOperation().toValue(),
                formatter.apply(displayAmount),
                Component.translatable(attribute.getDescriptionId()))
                .withStyle(ChatFormatting.BLUE);
    }

    public static MutableComponent makeEqualsText(Attribute attribute, AttributeModifier modifier,
                                                  double displayAmount) {
        return makeEqualsText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeEqualsText(Attribute attribute, AttributeModifier modifier,
                                                  double displayAmount, DoubleFunction<String> formatter) {
        return Component.literal(" ")
                .append(Component.translatable(
                        "attribute.modifier.equals." + modifier.getOperation().toValue(),
                        formatter.apply(displayAmount),
                        Component.translatable(attribute.getDescriptionId()))
                )
                .withStyle(ChatFormatting.DARK_GREEN);
    }


    public static void renderTooltip(List<Component> list, Player player, ItemStack stack,
                                     EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = stack.getAttributeModifiers(equipmentSlot);
        if (!multimap.isEmpty()) {
            list.add(Component.literal(""));
            list.add(Component.translatable("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));

            Comparator<Map.Entry<Attribute, AttributeModifier>> comp = Comparator.comparing(attr -> attr.getKey().getDescriptionId());

            multimap.entries().stream().sorted(comp).forEach(entry -> {
                renderAttribute(stack, equipmentSlot, player, entry.getKey(), entry.getValue(), list::add);
            });
        }
    }
}
