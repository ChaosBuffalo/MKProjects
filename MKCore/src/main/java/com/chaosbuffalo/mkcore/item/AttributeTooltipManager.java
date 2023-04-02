package com.chaosbuffalo.mkcore.item;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import java.util.function.DoubleFunction;

public class AttributeTooltipManager {

    // Why are these protected in Item? Not sure it's worth an AT
    protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    public interface ItemAttributeRenderer {
        List<Component> render(ItemStack stack, EquipmentSlot equipmentSlotType, Player player, Attribute attribute, AttributeModifier modifier);
    }

    public static final DecimalFormat DECIMALFORMAT = Util.make(new DecimalFormat("#.##"), format -> {
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    static Map<Attribute, ItemAttributeRenderer> attributeRendererMap = new IdentityHashMap<>(60);

    public static void registerAttributeRenderer(Attribute attribute, ItemAttributeRenderer renderer) {
        attributeRendererMap.put(attribute, renderer);
    }

    static List<Component> renderAttribute(ItemStack stack, EquipmentSlot equipmentSlotType,
                                           Player player, Attribute attribute,
                                           AttributeModifier modifier) {
        return attributeRendererMap.getOrDefault(attribute, AttributeTooltipManager::defaultAttributeRender)
                .render(stack, equipmentSlotType, player, attribute, modifier);
    }

    static List<Component> defaultAttributeRender(ItemStack stack, EquipmentSlot equipmentSlotType,
                                                  Player player, Attribute attribute,
                                                  AttributeModifier modifier) {
        double amount = modifier.getAmount();
        boolean absolute = false;
        if (player != null) {
            if (modifier.getId().equals(ATTACK_DAMAGE_MODIFIER)) {
                amount = amount + player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                amount = amount + EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
                absolute = true;
            } else if (modifier.getId().equals(ATTACK_SPEED_MODIFIER)) {
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

        if (absolute) {
            return Collections.singletonList(makeAbsoluteText(attribute, modifier, displayAmount));
        }

        Component line = makeBonusOrTakeText(attribute, modifier, amount, displayAmount);
        if (line != null) {
            return Collections.singletonList(line);
        }
        return Collections.emptyList();
    }

    @Nullable
    public static MutableComponent makeBonusOrTakeText(Attribute attribute, AttributeModifier modifier,
                                                       double amount, double displayAmount) {
        return makeBonusOrTakeText(attribute, modifier, amount, displayAmount, DECIMALFORMAT::format);
    }

    @Nullable
    public static MutableComponent makeBonusOrTakeText(Attribute attribute, AttributeModifier modifier,
                                                       double amount, double displayAmount, DoubleFunction<String> formatter) {
        if (amount > 0.0D) {
            return makeBonusText(attribute, modifier, displayAmount, formatter);
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
        return new TranslatableComponent("attribute.modifier.take." + modifier.getOperation().toValue(),
                formatter.apply(displayAmount),
                new TranslatableComponent(attribute.getDescriptionId()))
                .withStyle(ChatFormatting.RED);
    }

    public static MutableComponent makeBonusText(Attribute attribute, AttributeModifier modifier,
                                                 double displayAmount) {
        return makeBonusText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeBonusText(Attribute attribute, AttributeModifier modifier,
                                                 double displayAmount, DoubleFunction<String> formatter) {
        return new TranslatableComponent("attribute.modifier.plus." + modifier.getOperation().toValue(),
                formatter.apply(displayAmount),
                new TranslatableComponent(attribute.getDescriptionId()))
                .withStyle(ChatFormatting.BLUE);
    }

    public static MutableComponent makeAbsoluteText(Attribute attribute, AttributeModifier modifier,
                                                    double displayAmount) {
        return makeAbsoluteText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeAbsoluteText(Attribute attribute, AttributeModifier modifier,
                                                    double displayAmount, DoubleFunction<String> formatter) {
        return new TextComponent(" ")
                .append(new TranslatableComponent(
                        "attribute.modifier.equals." + modifier.getOperation().toValue(),
                        formatter.apply(displayAmount),
                        new TranslatableComponent(attribute.getDescriptionId()))
                )
                .withStyle(ChatFormatting.DARK_GREEN);
    }


    public static void renderTooltip(List<Component> list, Player player, ItemStack stack,
                                     EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> multimap = stack.getAttributeModifiers(equipmentSlot);
        if (!multimap.isEmpty()) {
            list.add(TextComponent.EMPTY);
            list.add(new TranslatableComponent("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));

            Comparator<Map.Entry<Attribute, AttributeModifier>> comp = Comparator.comparing(attr -> attr.getKey().getDescriptionId());

            multimap.entries().stream().sorted(comp).forEach(entry -> {
                list.addAll(renderAttribute(stack, equipmentSlot, player, entry.getKey(), entry.getValue()));
            });
        }
    }
}
