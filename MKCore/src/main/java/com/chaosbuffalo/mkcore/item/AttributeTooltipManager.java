package com.chaosbuffalo.mkcore.item;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "base_attack_damage_id");
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "base_attack_speed_id");

    Mob

    public interface ItemAttributeRenderer {
        void render(ItemStack stack, EquipmentSlot equipmentSlotType, Player player, Holder<Attribute> attribute,
                    AttributeModifier modifier, Consumer<Component> output);
    }

    public static final DecimalFormat DECIMALFORMAT = Util.make(new DecimalFormat("#.##"), format -> {
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    private static final Map<Holder<Attribute>, ItemAttributeRenderer> attributeRendererMap = new IdentityHashMap<>(6);

    public static void registerAttributeRenderer(Holder<Attribute> attribute, ItemAttributeRenderer renderer) {
        attributeRendererMap.put(attribute, renderer);
    }

    static void renderAttribute(ItemStack stack, EquipmentSlot equipmentSlotType,
                                Player player, Holder<Attribute> attribute,
                                AttributeModifier modifier,
                                Consumer<Component> output) {
        attributeRendererMap.getOrDefault(attribute, AttributeTooltipManager::defaultAttributeRender)
                .render(stack, equipmentSlotType, player, attribute, modifier, output);
    }

    static void defaultAttributeRender(ItemStack stack, EquipmentSlot equipmentSlotType, Player player,
                                       Holder<Attribute> attribute, AttributeModifier modifier, Consumer<Component> output) {
        double amount = modifier.amount();
        boolean absolute = false;
        if (player != null) {
            if (modifier.id().equals(BASE_ATTACK_DAMAGE_ID)) {
                amount += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                //FIXME: Figure out how to calculate a client side enchantment damage bonus
//                amount += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
                absolute = true;
            } else if (modifier.id().equals(BASE_ATTACK_SPEED_ID)) {
                amount += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                absolute = true;
            }
        }

        double displayAmount;
        if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
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

        if (line != null) {
            output.accept(line);
        }
    }

    @Nullable
    public static MutableComponent makePlusOrTakeText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                      double amount, double displayAmount) {
        return makePlusOrTakeText(attribute, modifier, amount, displayAmount, DECIMALFORMAT::format);
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
        return makeTakeText(attribute, attributemodifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeTakeText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                double displayAmount, DoubleFunction<String> formatter) {
        displayAmount = displayAmount * -1.0D;
        return Component.translatable("attribute.modifier.take." + modifier.operation().id(),
                        formatter.apply(displayAmount),
                        Component.translatable(attribute.value().getDescriptionId()))
                .withStyle(ChatFormatting.RED);
    }

    public static MutableComponent makePlusText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                double displayAmount) {
        return makePlusText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makePlusText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                double displayAmount, DoubleFunction<String> formatter) {
        return Component.translatable("attribute.modifier.plus." + modifier.operation().id(),
                        formatter.apply(displayAmount),
                        Component.translatable(attribute.value().getDescriptionId()))
                .withStyle(ChatFormatting.BLUE);
    }

    public static MutableComponent makeEqualsText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                  double displayAmount) {
        return makeEqualsText(attribute, modifier, displayAmount, DECIMALFORMAT::format);
    }

    @Nonnull
    public static MutableComponent makeEqualsText(Holder<Attribute> attribute, AttributeModifier modifier,
                                                  double displayAmount, DoubleFunction<String> formatter) {
        return Component.literal(" ")
                .append(Component.translatable(
                        "attribute.modifier.equals." + modifier.operation().id(),
                        formatter.apply(displayAmount),
                        Component.translatable(attribute.value().getDescriptionId()))
                )
                .withStyle(ChatFormatting.DARK_GREEN);
    }


    public static void renderTooltip(List<Component> list, Player player, ItemStack stack,
                                     EquipmentSlot equipmentSlot) {
        ItemAttributeModifiers multimap = stack.getAttributeModifiers();
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
