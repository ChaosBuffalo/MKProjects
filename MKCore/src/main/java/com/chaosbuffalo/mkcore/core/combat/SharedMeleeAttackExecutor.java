package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SharedMeleeAttackExecutor {
    private SharedMeleeAttackExecutor() {
    }

    public static float applyWeaponDamageBonus(ItemStack weaponStack, Entity target, DamageSource damageSource, float baseDamage) {
        return baseDamage + weaponStack.getItem().getAttackDamageBonus(target, baseDamage, damageSource);
    }

    public static void applyKnockbackAndSlowAttacker(LivingEntity attacker, Entity target, float knockback) {
        if (knockback <= 0.0F) {
            return;
        }
        if (target instanceof LivingEntity livingTarget) {
            livingTarget.knockback(
                    (double) (knockback * 0.5F),
                    (double) Mth.sin(attacker.getYRot() * (float) (Math.PI / 180.0)),
                    (double) (-Mth.cos(attacker.getYRot() * (float) (Math.PI / 180.0)))
            );
        } else {
            target.push(
                    (double) (-Mth.sin(attacker.getYRot() * (float) (Math.PI / 180.0)) * knockback * 0.5F),
                    0.1,
                    (double) (Mth.cos(attacker.getYRot() * (float) (Math.PI / 180.0)) * knockback * 0.5F)
            );
        }
        attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
    }

    public static void finalizeWeaponHit(LivingEntity attacker, InteractionHand hand, ItemStack weaponStack,
                                         Entity target, @SuppressWarnings("SameParameterValue") boolean invokeDestroyItemEventForPlayers) {
        attacker.setLastHurtMob(target);
        if (weaponStack.isEmpty() || !(target instanceof LivingEntity livingTarget)) {
            return;
        }
        ItemStack originalWeaponCopy = weaponStack.copy();
        boolean hurtEnemy = weaponStack.getItem().hurtEnemy(weaponStack, livingTarget, attacker);
        if (hurtEnemy) {
            weaponStack.getItem().postHurtEnemy(weaponStack, livingTarget, attacker);
        }
        if (weaponStack.isEmpty()) {
            if (invokeDestroyItemEventForPlayers && attacker instanceof Player player) {
                net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, originalWeaponCopy, hand);
            }
            attacker.setItemInHand(hand, ItemStack.EMPTY);
        }
    }
}
