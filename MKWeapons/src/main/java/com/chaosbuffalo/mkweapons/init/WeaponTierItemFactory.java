package com.chaosbuffalo.mkweapons.init;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RapidFireRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public interface WeaponTierItemFactory {

    default ItemAttributeModifiers getMeleeAttributes(IMKTier tier, IMeleeWeaponType weaponType) {
        return MKMeleeWeapon.createAttributes(tier, weaponType);
    }

    default Item.Properties modifyMeleeProperties(Item.Properties properties) {
        return properties;
    }

    default MKMeleeWeapon createMeleeWeapon(IMKTier tier, IMeleeWeaponType weaponType) {
        var properties = new Item.Properties()
                .attributes(getMeleeAttributes(tier, weaponType));
        properties = modifyMeleeProperties(properties);
        MKMeleeWeapon weapon = new MKMeleeWeapon(tier, weaponType, properties);
        return weapon;
    }

    ResourceLocation getMeleeRegistryName(IMKTier tier, IMeleeWeaponType weaponType);

    default MKBow createRangedWeapon(IMKTier tier) {

        var attrBuilder = ItemAttributeModifiers.builder();
        modifyRangedAttributes(tier, attrBuilder);

        MKBow bow = new MKBow(
                new Item.Properties()
                        .durability(tier.getUses() * 3)
                        .attributes(attrBuilder.build()),
                tier,
                GameConstants.TICKS_PER_SECOND * 2.5f, 4.0f,
                getRangedEffects(tier).toArray(IRangedWeaponEffect[]::new)
        );
        return bow;
    }

    default void modifyRangedAttributes(IMKTier tier, ItemAttributeModifiers.Builder builder) {
        ResourceLocation modifierId = MKWeapons.id("base." + tier.getName());
        builder.add(
                MKAttributes.RANGED_CRIT,
                new AttributeModifier(
                        modifierId, 0.05, AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );
        builder.add(
                MKAttributes.RANGED_CRIT_MULTIPLIER,
                new AttributeModifier(
                        modifierId, 0.25, AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );
    }

    default List<IRangedWeaponEffect> getRangedEffects(IMKTier tier) {
        return List.of(
                new RapidFireRangedWeaponEffect(7, .10f)
        );
    }

    ResourceLocation getRangedRegistryName(IMKTier tier);
}