package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.combat.MeleeAttackContext;
import com.chaosbuffalo.mkcore.core.combat.SharedMeleeAttackExecutor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;

public final class PlayerMeleeAttackExecutor {
    private PlayerMeleeAttackExecutor() {
    }

    public static void executeAttack(PlayerCombatExtensionModule combat, MeleeAttackContext context) {
        combat.executeWithAttackHand(context.hand(), () -> {
            Player player = combat.getPlayerData().getEntity();
            if (!net.neoforged.neoforge.common.CommonHooks.onPlayerAttackTarget(player, context.target())) {
                return;
            }
            Entity target = context.target();
            if (!target.isAttackable() || target.skipAttackInteraction(player)) {
                return;
            }

            InteractionHand hand = context.hand();
            ItemStack weaponStack = player.getItemInHand(hand);
            DamageSource damageSource = player.damageSources().playerAttack(player);
            float baseDamage = PlayerMeleeHandStatsResolver.resolveAttackDamage(combat, hand);
            float enchantDamage = player.level() instanceof ServerLevel serverLevel
                    ? EnchantmentHelper.modifyDamage(serverLevel, weaponStack, target, damageSource, baseDamage) - baseDamage
                    : 0.0F;
            float attackStrength = combat.getAttackStrengthScale(hand, 0.5F);
            baseDamage *= 0.2F + attackStrength * attackStrength * 0.8F;
            enchantDamage *= attackStrength;
            if (target.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                    && target instanceof Projectile projectile
                    && projectile.deflect(ProjectileDeflection.AIM_DEFLECT, player, player, true)) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource());
                return;
            }

            if (baseDamage <= 0.0F && enchantDamage <= 0.0F) {
                combat.onServerAttackCommitted(target, hand, combat.isExecutingMultiAttack(hand));
                return;
            }

            boolean fullyCharged = attackStrength > 0.9F;
            boolean sprintKnockback = false;
            if (player.isSprinting() && fullyCharged) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
                sprintKnockback = true;
            }

            baseDamage = SharedMeleeAttackExecutor.applyWeaponDamageBonus(weaponStack, target, damageSource, baseDamage);
            boolean critical = fullyCharged
                    && player.fallDistance > 0.0F
                    && !player.onGround()
                    && !player.onClimbable()
                    && !player.isInWater()
                    && !player.hasEffect(MobEffects.BLINDNESS)
                    && !player.isPassenger()
                    && target instanceof LivingEntity
                    && !player.isSprinting();
            var critEvent = net.neoforged.neoforge.common.CommonHooks.fireCriticalHit(player, target, critical, critical ? 1.5F : 1.0F);
            critical = critEvent.isCriticalHit();
            if (critical) {
                baseDamage *= critEvent.getDamageMultiplier();
            }

            float totalDamage = baseDamage + enchantDamage;
            double walkDelta = (double) (player.walkDist - player.walkDistO);
            boolean critBlocksSweep = critEvent.isCriticalHit() && critEvent.disableSweep();
            boolean sweeping = fullyCharged && !critBlocksSweep && !sprintKnockback && player.onGround() && walkDelta < (double) player.getSpeed()
                    && weaponStack.canPerformAction(ItemAbilities.SWORD_SWEEP);
            var sweepEvent = net.neoforged.neoforge.common.CommonHooks.fireSweepAttack(player, target, sweeping);
            sweeping = sweepEvent.isSweeping();

            float previousHealth = target instanceof LivingEntity living ? living.getHealth() : 0.0F;
            Vec3 previousMotion = target.getDeltaMovement();
            boolean hurt = target.hurt(damageSource, totalDamage);
            if (hurt) {
                float knockback = resolveKnockback(combat, player, hand, target, damageSource) + (sprintKnockback ? 1.0F : 0.0F);
                if (knockback > 0.0F) {
                    SharedMeleeAttackExecutor.applyKnockbackAndSlowAttacker(player, target, knockback);
                    player.setSprinting(false);
                }

                if (sweeping) {
                    float sweepDamage = 1.0F + (float) player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * baseDamage;
                    for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, weaponStack.getSweepHitBox(player, target))) {
                        double entityReachSq = Mth.square(player.entityInteractionRange());
                        if (nearby != player
                                && nearby != target
                                && !player.isAlliedTo(nearby)
                                && (!(nearby instanceof ArmorStand armorStand) || !armorStand.isMarker())
                                && player.distanceToSqr(nearby) < entityReachSq) {
                            float sweepEnchantedDamage = player.level() instanceof ServerLevel serverLevel
                                    ? EnchantmentHelper.modifyDamage(serverLevel, weaponStack, nearby, damageSource, sweepDamage) * attackStrength
                                    : sweepDamage * attackStrength;
                            nearby.knockback(
                                    0.4F,
                                    (double) Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)),
                                    (double) (-Mth.cos(player.getYRot() * (float) (Math.PI / 180.0)))
                            );
                            nearby.hurt(damageSource, sweepEnchantedDamage);
                            if (player.level() instanceof ServerLevel serverLevel) {
                                EnchantmentHelper.doPostAttackEffects(serverLevel, nearby, damageSource);
                            }
                        }
                    }
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                    player.sweepAttack();
                }

                if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
                    target.hurtMarked = false;
                    target.setDeltaMovement(previousMotion);
                }

                if (critical) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
                    player.crit(target);
                }

                if (!critical && !sweeping) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            fullyCharged ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_WEAK,
                            player.getSoundSource(), 1.0F, 1.0F);
                }

                if (enchantDamage > 0.0F) {
                    player.magicCrit(target);
                }

                if (player.level() instanceof ServerLevel serverLevel) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
                }
                Entity actualTarget = target instanceof net.neoforged.neoforge.entity.PartEntity<?> partEntity ? partEntity.getParent() : target;
                SharedMeleeAttackExecutor.finalizeWeaponHit(player, hand, weaponStack, actualTarget, true);

                if (target instanceof LivingEntity living) {
                    float damageDealt = previousHealth - living.getHealth();
                    player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                    if (player.level() instanceof ServerLevel serverLevel && damageDealt > 2.0F) {
                        int particleCount = (int) ((double) damageDealt * 0.5);
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), particleCount, 0.1, 0.0, 0.1, 0.2);
                    }
                }

                player.causeFoodExhaustion(0.1F);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
            }

            combat.onServerAttackCommitted(target, hand, combat.isExecutingMultiAttack(hand));
        });
    }

    private static float resolveKnockback(PlayerCombatExtensionModule combat, Player player, InteractionHand hand,
                                          Entity target, DamageSource damageSource) {
        float knockback = PlayerMeleeHandStatsResolver.resolveAttackKnockback(combat, hand);
        return player.level() instanceof ServerLevel serverLevel
                ? EnchantmentHelper.modifyKnockback(serverLevel, player.getItemInHand(hand), target, damageSource, knockback)
                : knockback;
    }
}
