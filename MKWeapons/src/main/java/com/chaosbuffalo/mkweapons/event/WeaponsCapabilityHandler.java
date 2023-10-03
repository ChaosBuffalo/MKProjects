package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkcore.utils.CapabilityUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.*;
import com.chaosbuffalo.mkweapons.items.armor.IMKArmor;
import com.chaosbuffalo.mkweapons.items.weapon.IMKWeapon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeaponsCapabilityHandler {

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof AbstractArrow arrow) {
            var provider = CapabilityUtils.provider(WeaponsCapabilities.ARROW_DATA_CAPABILITY,
                    ArrowDataHandler::new,
                    arrow);

            event.addCapability(WeaponsCapabilities.MK_ARROW_CAP_ID, provider);
        }
    }

    @SubscribeEvent
    public static void attachWeaponCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof IMKWeapon) {
            var provider = CapabilityUtils.provider(WeaponsCapabilities.WEAPON_DATA_CAPABILITY,
                    WeaponDataHandler::new,
                    event.getObject());

            event.addCapability(WeaponsCapabilities.MK_WEAPON_CAP_ID, provider);
        }
        if (event.getObject().getItem() instanceof IMKArmor) {
            var provider = CapabilityUtils.provider(WeaponsCapabilities.ARMOR_DATA_CAPABILITY,
                    ArmorDataHandler::new,
                    event.getObject());

            event.addCapability(WeaponsCapabilities.MK_ARMOR_CAP_ID, provider);
        }
    }
}

