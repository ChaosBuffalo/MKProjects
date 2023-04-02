package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.Tier;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IMeleeWeaponType {

    float getDamageMultiplier();

    float getAttackSpeed();

    float getCritMultiplier();

    float getCritChance();

    float getReach();

    boolean isTwoHanded();

    float getBlockEfficiency();

    float getMaxPoise();

    List<IMeleeWeaponEffect> getWeaponEffects();

    <D> D serialize(DynamicOps<D> ops);

    <D> void deserialize(Dynamic<D> dynamic);

    ResourceLocation getName();

    default float getDamageForTier(Tier tier){
        return (tier.getAttackDamageBonus() + 3) * getDamageMultiplier();
    }

}
