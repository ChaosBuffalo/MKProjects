package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;


@EventBusSubscriber(modid = MKCore.MOD_ID)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        SpellTriggers.FALL.onLivingFall(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.level().isClientSide)
            return;

        DamageSource source = event.getSource();
        // Fully blocked hits should not reach any attacker/victim trigger pipeline.
        if (DamageUtils.isFullyBlockedDamage(source, event.getNewDamage())) {
            return;
        }
        Entity trueSource = source.getEntity();

        // Living is source
        if (trueSource instanceof LivingEntity livingSource) {
            var sourceData = MKCore.getEntityDataOrThrow(livingSource);
            SpellTriggers.LIVING_HURT_ENTITY.onLivingHurtEntity(event, source, livingTarget, sourceData);

            if (livingSource instanceof ServerPlayer serverPlayer && DamageUtils.isMeleeDamage(source) && livingSource.getMainHandItem().isEmpty()) {
                var playerData = MKCore.getPlayerOrThrow(serverPlayer);
                playerData.getSkills().tryScaledIncreaseSkill(MKAttributes.HAND_TO_HAND, 0.5);
            }
        }

        // Living is victim
        var targetData = MKCore.getEntityDataOrThrow(livingTarget);
        SpellTriggers.ENTITY_HURT.onEntityHurtLiving(event, source, targetData);
    }

    private static void playSound(LivingEntity target, Holder<SoundEvent> sound) {
        SoundUtils.serverPlaySoundAtEntity(target, sound.value(), target.getSoundSource());
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide)
            return;

        DamageSource dmgSource = event.getDamageSource();

        // The hurt() mixin only redirects vanilla's internal block check so MKCore can
        // replace the built-in shield result. This direct call still returns the normal
        // facing/shield eligibility and is used as the gate for MKCore's custom block logic.
        if (!target.isDamageSourceBlocked(dmgSource))
            return;

        IMKEntityData targetData = MKCore.getEntityDataOrThrow(target);
        float incomingDamage = event.getOriginalBlockedDamage();
        IMKEntityStats.BlockResult breakResult = targetData.getStats().tryPoiseBlock(incomingDamage);

        // NeoForge subtracts blockedDamage from the container before LivingDamageEvent.Pre.
        // Setting this to only the poise-absorbed portion preserves the remaining damage
        // for downstream armor/resistance processing without re-entering hurt().
        float poiseAbsorbed = incomingDamage - breakResult.damageLeft();
        event.setBlocked(true);
        event.setBlockedDamage(poiseAbsorbed);
        event.setShieldDamage(0);

        // Mark on the damage source so downstream code (triggers, knockback mixin) can detect blocking
        if (dmgSource instanceof IMKDamageSourceExtensions ext) {
            ext.setWasBlocked(true);
        }

        Entity source = dmgSource.getEntity();
        if (breakResult.poiseBroke()) {
            playSound(target, CoreSounds.block_break);
        } else {
            if (target.getTicksUsingItem() <= 6) {
                playSound(target, CoreSounds.parry);
                if (targetData instanceof MKPlayerData playerData) {
                    playerData.getSkills().tryIncreaseSkill(MKAttributes.BLOCK);
                }
            } else {
                if (targetData instanceof MKPlayerData playerData) {
                    playerData.getSkills().tryScaledIncreaseSkill(MKAttributes.BLOCK, 0.5);
                }
                if (dmgSource.getDirectEntity() instanceof AbstractArrow) {
                    playSound(target, CoreSounds.arrow_block);
                } else if (source instanceof LivingEntity attacker) {
                    ItemStack weapon = attacker.getMainHandItem();
                    playSound(target, weapon.getItem() instanceof SwordItem ?
                            CoreSounds.weapon_block :
                            CoreSounds.fist_block);
                } else {
                    playSound(target, CoreSounds.fist_block);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent arrowEvent) {
        Entity shooter = arrowEvent.getProjectile().getOwner(); // getShooter
        if (shooter instanceof LivingEntity livingShooter && arrowEvent.getProjectile() instanceof AbstractArrow) {
            IMKEntityData entityData = MKCore.getEntityDataOrThrow(livingShooter);
            if (arrowEvent.getRayTraceResult().getType() == HitResult.Type.BLOCK) {
                entityData.getCombatExtension().projectileMiss();
            } else {
                entityData.getCombatExtension().recordProjectileHit();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        var victimData = MKCore.getEntityDataOrThrow(event.getEntity());
        victimData.getAbilityExecutor().interruptCast(CastInterruptReason.Death);

        DamageSource source = event.getSource();
        if (source.getEntity() instanceof LivingEntity killer) {
            if (killer.level().isClientSide) {
                return;
            }

            var killerData = MKCore.getEntityDataOrThrow(killer);
            SpellTriggers.LIVING_KILL_ENTITY.onEntityDeath(event, source, killerData);
        }
    }
}
