package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class DamageMultiplierMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    protected final float damageMultiplier;

    public DamageMultiplierMeleeWeaponEffect(ResourceLocation name, ChatFormatting color, float damageMultiplier) {
        super(name, color);
        this.damageMultiplier = damageMultiplier;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public abstract boolean isTargetSuitable(LivingEntity attacker, LivingEntity target, IMKMeleeWeapon weapon, ItemStack stack);

    @Override
    public float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isTargetSuitable(attacker, target, weapon, stack)) {
            return damage * damageMultiplier;
        } else {
            return damage;
        }
    }
}
