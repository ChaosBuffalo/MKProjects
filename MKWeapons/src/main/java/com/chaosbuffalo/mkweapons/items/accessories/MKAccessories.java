package com.chaosbuffalo.mkweapons.items.accessories;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.function.BiConsumer;

public class MKAccessories {
    public static void iterateAccessories(LivingEntity entity, BiConsumer<ItemStack, IMKAccessory> consumer) {
        CuriosApi.getCuriosInventory(entity).map(ICuriosItemHandler::getEquippedCurios).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack curioStack = handler.getStackInSlot(i);
                if (!curioStack.isEmpty() && curioStack.getItem() instanceof IMKAccessory accessory) {
                    consumer.accept(curioStack, accessory);
                }
            }
        });
    }
}
