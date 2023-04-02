package com.chaosbuffalo.mkweapons.event;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.ArmorDataProvider;
import com.chaosbuffalo.mkweapons.capabilities.ArrowDataProvider;
import com.chaosbuffalo.mkweapons.capabilities.WeaponDataProvider;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.armor.IMKArmor;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKWeapon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= MKWeapons.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class WeaponsCapabilityHandler {

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof AbstractArrow) {
            e.addCapability(WeaponsCapabilities.MK_ARROW_CAP_ID,
                    new ArrowDataProvider((AbstractArrow) e.getObject()));
        }
    }

    @SubscribeEvent
    public static void attachWeaponCapability(AttachCapabilitiesEvent<ItemStack> event){
        if (event.getObject().getItem() instanceof IMKWeapon){
            event.addCapability(WeaponsCapabilities.MK_WEAPON_CAP_ID,
                    new WeaponDataProvider(event.getObject()));
        }
        if (event.getObject().getItem() instanceof IMKArmor){
            event.addCapability(WeaponsCapabilities.MK_ARMOR_CAP_ID,
                    new ArmorDataProvider(event.getObject()));
        }
    }
}

