package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

public class ItemUtils {
    public static final UUID OFFHAND_UUID = UUID.fromString("80df259e-29f8-4779-bd46-94a24e313c2d");
    private static final float DEFAULT_CRIT_RATE = .0f;
    private static final float DEFAULT_CRIT_MULTIPLIER = 1.5f;
    public static CriticalStats<Item> CRIT = new CriticalStats<>(DEFAULT_CRIT_RATE, DEFAULT_CRIT_MULTIPLIER);

    public static ArrayList<Function<Item, Boolean>> IS_TWO_HANDED_CALLBACKS = new ArrayList<>();

    public static void addCriticalStats(Class<? extends Item> itemIn, int priority, float criticalChance, float damageMultiplier) {
        CRIT.addCriticalStats(itemIn, priority, criticalChance, damageMultiplier);
    }

    public static boolean isSuitableOffhandWeapon(ItemStack item) {
        return !item.equals(ItemStack.EMPTY) && item.getItem() instanceof SwordItem; // we need to add is no shield back here
    }

    public static void addTwoHandedCallback(Function<Item, Boolean> func) {
        IS_TWO_HANDED_CALLBACKS.add(func);
    }

    public static boolean isTwoHandedWeapon(Item item) {
        for (Function<Item, Boolean> callback : IS_TWO_HANDED_CALLBACKS) {
            if (callback.apply(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRangedWeapon(ItemStack item) {
        return item.getItem() instanceof BowItem || item.getItem() instanceof CrossbowItem;
    }

    static {
        addCriticalStats(SwordItem.class, 0, .05f, 1.0f);
        addCriticalStats(AxeItem.class, 0, .15f, 1.0f);
        addCriticalStats(PickaxeItem.class, 0, .05f, 0.5f);
        addCriticalStats(ShovelItem.class, 0, .05f, 0.5f);
        addCriticalStats(HoeItem.class, 0, .05f, 0.5f);
    }


    public static float getCritChanceForItem(ItemStack itemInHand) {
        if (itemInHand.isEmpty()) {
            return DEFAULT_CRIT_RATE;
        }
        Item item = itemInHand.getItem();
        return CRIT.getChance(item);
    }

    public static float getCritMultiplierForItem(ItemStack itemInHand) {
        if (itemInHand.isEmpty()) {
            return DEFAULT_CRIT_MULTIPLIER;
        }
        Item item = itemInHand.getItem();
        return CRIT.getMultiplier(item);
    }

    public static Multimap<String, AttributeModifier> getOffhandModifiersForItem(ItemStack item) {
        SwordItem sword = (SwordItem) item.getItem();
        Multimap<Attribute, AttributeModifier> modifiers = sword.getAttributeModifiers(
                EquipmentSlot.MAINHAND, item);
        Multimap<String, AttributeModifier> newModifiers = HashMultimap.create();
        modifiers.forEach((key, modifier) -> {
            if (key.equals(Attributes.ATTACK_SPEED)) {
                double attacksPerSecond = 4.0 + modifier.getAmount();
                double ratio = attacksPerSecond / 8.0;
                MKCore.LOGGER.info("ratio is {}, {}", ratio, attacksPerSecond);
                newModifiers.put(key.getDescriptionId(), new AttributeModifier(OFFHAND_UUID,
                        "Weapon modifier offhand", -ratio, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else {
                AttributeModifier newMod = new AttributeModifier(OFFHAND_UUID,
                        "Weapon modifier offhand", modifier.getAmount(), modifier.getOperation());
                newModifiers.put(key.getDescriptionId(), newMod);
            }

        });
        return newModifiers;
    }

}
