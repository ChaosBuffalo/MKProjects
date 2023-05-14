package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class DamageMultiplierMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    protected float damageMultiplier;

    public DamageMultiplierMeleeWeaponEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        damageMultiplier = dynamic.get("multiplier").asFloat(1.5f);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("multiplier"), ops.createFloat(damageMultiplier));
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
