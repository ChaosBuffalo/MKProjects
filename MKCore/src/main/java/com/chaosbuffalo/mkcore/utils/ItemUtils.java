package com.chaosbuffalo.mkcore.utils;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

import java.util.Objects;
import java.util.Set;

public class ItemUtils {
    private static final float DEFAULT_CRIT_RATE = .0f;
    private static final float DEFAULT_CRIT_MULTIPLIER = 1.5f;
    public static CriticalStats<Item> CRIT = new CriticalStats<>(DEFAULT_CRIT_RATE, DEFAULT_CRIT_MULTIPLIER);

    public static void addCriticalStats(Class<? extends Item> itemIn, int priority, float criticalChance, float damageMultiplier) {
        CRIT.addCriticalStats(itemIn, priority, criticalChance, damageMultiplier);
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


    public static boolean compareItemsWithBlacklist(ItemStack stack, ItemStack other, Set<DataComponentType<?>> blackList) {
        if (!stack.is(other.getItem())) {
            return false;
        } else {
            return stack.isEmpty() && other.isEmpty() || stack.getComponents().stream().allMatch(
                    comp -> blackList.contains(comp.type()) || Objects.equals(comp, other.getComponents().get(comp.type())));
        }
    }

    private static Set<DataComponentType<?>> noDurability = Set.of(DataComponents.DAMAGE, DataComponents.MAX_DAMAGE);

    public static boolean isEqualNoDurability(ItemStack stack1, ItemStack stack2) {
        return compareItemsWithBlacklist(stack1, stack1, noDurability);
    }

    public static EquipmentSlot getGenericEquipmentSlotForItem(ItemStack stack) {
        EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot != null) {
            return slot;
        } else {
            Equipable equipable = Equipable.get(stack);
            if (equipable != null) {
                return equipable.getEquipmentSlot();
            }
        }
        return EquipmentSlot.MAINHAND;
        }

}
