package com.chaosbuffalo.mknpc.entity.combat;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.combat.SharedMeleeAttackExecutor;
import com.chaosbuffalo.mkcore.events.ModifyBaseMeleeDamageEvent;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;

public final class NpcMeleeAttackExecutor {
    private NpcMeleeAttackExecutor() {
    }

    public static boolean executeAttack(MKEntity entity, LivingEntity target, InteractionHand hand,
                                        CombatExtensionModule combat) {
        return combat.executeWithAttackHand(hand, () -> {
            return executeDirectAttack(entity, target, hand);
        });
    }

    private static boolean executeDirectAttack(MKEntity entity, LivingEntity target, InteractionHand hand) {
        ItemStack weaponStack = entity.getItemInHand(hand);
        DamageSource damageSource = entity.damageSources().mobAttack(entity);
        float baseDamage = (float) entity.getProjectedAttackDamage(hand);
        ModifyBaseMeleeDamageEvent baseDamageEvent = new ModifyBaseMeleeDamageEvent(entity.getEntityDataCap(), hand, weaponStack, baseDamage);
        NeoForge.EVENT_BUS.post(baseDamageEvent);
        baseDamage = baseDamageEvent.getDamage();
        float knockback = (float) entity.getProjectedAttackKnockback(hand);
        if (entity.level() instanceof ServerLevel serverLevel) {
            baseDamage = EnchantmentHelper.modifyDamage(serverLevel, weaponStack, target, damageSource, baseDamage);
            knockback = EnchantmentHelper.modifyKnockback(serverLevel, weaponStack, target, damageSource, knockback);
        }
        baseDamage = SharedMeleeAttackExecutor.applyWeaponDamageBonus(weaponStack, target, damageSource, baseDamage);

        boolean didAttack = target.hurt(damageSource, baseDamage);
        if (!didAttack) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE,
                    entity.getSoundSource(), 1.0F, 1.0F);
            entity.swing(hand, true);
            return false;
        }

        if (knockback > 0.0F) {
            SharedMeleeAttackExecutor.applyKnockbackAndSlowAttacker(entity, target, knockback);
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
        }
        SharedMeleeAttackExecutor.finalizeWeaponHit(entity, hand, weaponStack, target, false);
        entity.swing(hand, true);
        return true;
    }
}
