package com.chaosbuffalo.mkweapons.init;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.IRangedWeaponType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface WeaponTierItemFactory {

    default ItemAttributeModifiers.Builder getMeleeAttributes(IMKTier tier, IMeleeWeaponType weaponType) {
        return MKMeleeWeapon.createAttributes(tier, weaponType);
    }

    default Item.Properties modifyMeleeProperties(Item.Properties properties) {
        return properties;
    }

    default MKMeleeWeapon createMeleeWeapon(IMKTier tier, IMeleeWeaponType weaponType) {
        var properties = new Item.Properties()
                .attributes(getMeleeAttributes(tier, weaponType).build());
        properties = modifyMeleeProperties(properties);
        MKMeleeWeapon weapon = new MKMeleeWeapon(tier, weaponType, properties);
        return weapon;
    }

    ResourceLocation getMeleeRegistryName(IMKTier tier, IMeleeWeaponType weaponType);

    default ItemAttributeModifiers.Builder getRangedAttributes(IMKTier tier, IRangedWeaponType weaponType) {
        return MKBow.createAttributes(tier, weaponType);
    }

    default Item.Properties modifyRangedProperties(Item.Properties properties) {
        return properties;
    }

    default MKBow createRangedWeapon(IMKTier tier, IRangedWeaponType rangedWeaponType) {

        var attrBuilder = getRangedAttributes(tier, rangedWeaponType).build();
        var properties = new Item.Properties()
                .durability(tier.getUses() * rangedWeaponType.getDurabilityMultiplier())
                .attributes(attrBuilder);
        properties = modifyRangedProperties(properties);

        MKBow bow = new MKBow(properties, tier, rangedWeaponType);
        return bow;
    }

    ResourceLocation getRangedRegistryName(IMKTier tier, IRangedWeaponType rangedWeaponType);
}