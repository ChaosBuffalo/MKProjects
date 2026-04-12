package com.chaosbuffalo.mkcore.core.combat;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;

public final class AbilityMeleeAttackExecutor {
    private AbilityMeleeAttackExecutor() {
    }

    public static boolean executeAttack(AbilityMeleeAttackContext context) {
        CombatExtensionModule combat = context.attackerData().getCombatExtension();
        return combat.executeWithAttackHand(context.hand(), () -> {
            ItemStack weaponStack = context.attacker().getItemInHand(context.hand());
            boolean handledSwing = !weaponStack.isEmpty() && weaponStack.onEntitySwing(context.attacker());
            if (context.triggerVisualSequence() && !handledSwing) {
                MeleeAttackVisualHelper.startVisualAttack(context.attacker(), context.hand(),
                        context.swingStartTicks(), context.swingDurationTicks());
            }
            boolean hurt = context.attacker() instanceof Player player
                    ? executePlayerAttack(context, combat, player)
                    : executeLivingAttack(context);
            combat.recordSwingHit();
            NeoForge.EVENT_BUS.post(new PostAttackEvent(context.attackerData(), context.target(), false, context.hand()));
            return hurt;
        });
    }

    private static boolean executePlayerAttack(AbilityMeleeAttackContext context, CombatExtensionModule combat, Player player) {
        LivingEntity target = context.target();
        if (!net.neoforged.neoforge.common.CommonHooks.onPlayerAttackTarget(player, target)) {
            return false;
        }
        if (!target.isAttackable() || target.skipAttackInteraction(player)) {
            return false;
        }
        InteractionHand hand = context.hand();
        ItemStack weaponStack = player.getItemInHand(hand);
        DamageSource damageSource = MKDamageSource.causeMeleeDamage(player.level(), context.abilityId(),
                context.directEntity(), player).setAttackHand(hand);
        float baseDamage = MeleeHandStatsResolver.resolveAttackDamage(context.attackerData(), hand);
        float enchantDamage = player.level() instanceof ServerLevel serverLevel
                ? EnchantmentHelper.modifyDamage(serverLevel, weaponStack, target, damageSource, baseDamage) - baseDamage
                : 0.0F;
        baseDamage = SharedMeleeAttackExecutor.applyWeaponDamageBonus(weaponStack, target, damageSource, baseDamage);
        float scaledSwingDamage = (baseDamage + enchantDamage) * context.swingDamageScale();
        float totalDamage = Math.max(0.0F, scaledSwingDamage + context.bonusDamage());
        boolean hurt = target.hurt(damageSource, totalDamage);
        if (hurt) {
            float knockback = resolveKnockback(context.attacker(), hand, target, damageSource);
            if (knockback > 0.0F) {
                SharedMeleeAttackExecutor.applyKnockbackAndSlowAttacker(player, target, knockback);
            }
            if (player.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
            }
            SharedMeleeAttackExecutor.finalizeWeaponHit(player, hand, weaponStack, target, true);
        }
        return hurt;
    }

    private static boolean executeLivingAttack(AbilityMeleeAttackContext context) {
        LivingEntity attacker = context.attacker();
        LivingEntity target = context.target();
        InteractionHand hand = context.hand();
        ItemStack weaponStack = attacker.getItemInHand(hand);
        DamageSource damageSource = MKDamageSource.causeMeleeDamage(attacker.level(), context.abilityId(),
                context.directEntity(), attacker).setAttackHand(hand);
        float baseDamage = MeleeHandStatsResolver.resolveAttackDamage(context.attackerData(), hand);
        float knockback = MeleeHandStatsResolver.resolveAttackKnockback(attacker, hand);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            baseDamage = EnchantmentHelper.modifyDamage(serverLevel, weaponStack, target, damageSource, baseDamage);
            knockback = EnchantmentHelper.modifyKnockback(serverLevel, weaponStack, target, damageSource, knockback);
        }
        baseDamage = SharedMeleeAttackExecutor.applyWeaponDamageBonus(weaponStack, target, damageSource, baseDamage);
        float totalDamage = Math.max(0.0F, baseDamage * context.swingDamageScale() + context.bonusDamage());
        boolean hurt = target.hurt(damageSource, totalDamage);
        if (hurt) {
            if (knockback > 0.0F) {
                SharedMeleeAttackExecutor.applyKnockbackAndSlowAttacker(attacker, target, knockback);
            }
            if (attacker.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
            }
            SharedMeleeAttackExecutor.finalizeWeaponHit(attacker, hand, weaponStack, target, false);
        }
        return hurt;
    }

    private static float resolveKnockback(LivingEntity attacker, InteractionHand hand, LivingEntity target, DamageSource damageSource) {
        float knockback = MeleeHandStatsResolver.resolveAttackKnockback(attacker, hand);
        return attacker.level() instanceof ServerLevel serverLevel
                ? EnchantmentHelper.modifyKnockback(serverLevel, attacker.getItemInHand(hand), target, damageSource, knockback)
                : knockback;
    }
}
