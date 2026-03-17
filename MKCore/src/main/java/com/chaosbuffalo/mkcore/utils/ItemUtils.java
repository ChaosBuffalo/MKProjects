package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.item.ItemCriticalStats;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class ItemUtils {
    private static final float DEFAULT_CRIT_RATE = .0f;
    private static final float DEFAULT_CRIT_MULTIPLIER = 1.5f;
    private static final ItemCriticalStats DEFAULT_CRIT = new ItemCriticalStats(DEFAULT_CRIT_RATE, DEFAULT_CRIT_MULTIPLIER);

    public static boolean isRangedWeapon(ItemStack item) {
        return item.getItem() instanceof BowItem || item.getItem() instanceof CrossbowItem;
    }

    @Nonnull
    public static ItemCriticalStats getCriticalStats(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return DEFAULT_CRIT;
        }

        var stats = ItemCriticalStats.get(itemStack);
        return stats == null ? DEFAULT_CRIT : stats;
    }


    public static boolean compareItemsWithIgnoreList(ItemStack stack, ItemStack other, Set<DataComponentType<?>> ignoreList) {
        if (!stack.is(other.getItem())) {
            return false;
        } else {
            if (stack.isEmpty() && other.isEmpty()) {
                return true;
            }
            return stack.getComponents().stream()
                    .allMatch(comp -> ignoreList.contains(comp.type()) ||
                            Objects.equals(comp.value(), other.get(comp.type())));
        }
    }

    private static final Set<DataComponentType<?>> DURABILITY_COMPONENTS = Set.of(DataComponents.DAMAGE, DataComponents.MAX_DAMAGE);

    public static boolean isEqualNoDurability(ItemStack stack1, ItemStack stack2) {
        return compareItemsWithIgnoreList(stack1, stack2, DURABILITY_COMPONENTS);
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
