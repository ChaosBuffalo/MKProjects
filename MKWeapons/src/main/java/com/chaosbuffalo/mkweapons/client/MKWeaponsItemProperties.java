package com.chaosbuffalo.mkweapons.client;

import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class MKWeaponsItemProperties {
    public static void registerItemProperties() {
        registerDefaultRangedWeaponItemProperties(MKWeaponsItems.BOWS);
        registerDefaultMeleeWeaponItemProperties(MKWeaponsItems.WEAPONS);
    }

    public static <TItem extends Item & IMKMeleeWeapon> void registerDefaultMeleeWeaponItemProperties(Collection<? extends TItem> weapons) {
        for (TItem weapon : weapons) {
            if (weapon.getWeaponType().canBlock()) {
                ItemProperties.register(weapon, ResourceLocation.withDefaultNamespace("blocking"), MKWeaponsItemProperties::defaultMeleeBlockingProperty);
            }
        }
    }

    public static <TItem extends Item & IMKRangedWeapon> void registerDefaultRangedWeaponItemProperties(Collection<? extends TItem> weapons) {
        for (TItem bow : weapons) {
            ItemProperties.register(bow, ResourceLocation.withDefaultNamespace("pull"), MKWeaponsItemProperties::defaultRangedPullProperty);
            ItemProperties.register(bow, ResourceLocation.withDefaultNamespace("pulling"), MKWeaponsItemProperties::defaultRangedPullingProperty);
        }
    }

    private static float defaultRangedPullProperty(ItemStack itemStack, ClientLevel world, LivingEntity entity, int seed) {
        if (entity == null) {
            return 0.0F;
        } else {
            return !(entity.getUseItem().getItem() instanceof MKBow mkBow) ? 0.0F :
                    (float) (itemStack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / mkBow.getDrawTime(itemStack, entity);
        }
    }

    private static float defaultRangedPullingProperty(ItemStack itemStack, ClientLevel world, LivingEntity entity, int seed) {
        return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
    }

    private static float defaultMeleeBlockingProperty(ItemStack itemStack, ClientLevel world, LivingEntity entity, int seed) {
        return entity != null && entity.isUsingItem()
                && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
    }
}
