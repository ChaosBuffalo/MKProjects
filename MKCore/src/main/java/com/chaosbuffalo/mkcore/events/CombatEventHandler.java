package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import com.chaosbuffalo.mkcore.effects.triggers.AttackerDamageTriggerContext;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.FallTriggerContext;
import com.chaosbuffalo.mkcore.effects.triggers.KillTriggerContext;
import com.chaosbuffalo.mkcore.effects.triggers.VictimDamageTriggerContext;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.network.MeleeCritMessagePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.ProjectileCritMessagePacket;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

        IMKEntityData livingData = MKCore.getEntityDataOrThrow(event.getEntity());
        livingData.getTriggers().dispatch(CoreTriggerTypes.FALL,
                new FallTriggerContext(event, livingData));
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
            var attackerContext = new AttackerDamageTriggerContext(event, source, livingTarget, sourceData);

            if (source instanceof MKDamageSource mkDamageSource) {
                handleMKDamage(event, mkDamageSource, livingTarget, livingSource, sourceData, attackerContext);
            } else {
                handleVanillaAndProjectileDamage(event, source, livingTarget, livingSource, sourceData, attackerContext);
            }

            if (livingSource instanceof ServerPlayer serverPlayer && DamageUtils.isMeleeDamage(source) && livingSource.getMainHandItem().isEmpty()) {
                var playerData = MKCore.getPlayerOrThrow(serverPlayer);
                playerData.getSkills().tryScaledIncreaseSkill(MKAttributes.HAND_TO_HAND, 0.5);
            }
        }

        // Living is victim
        var targetData = MKCore.getEntityDataOrThrow(livingTarget);
        var victimContext = new VictimDamageTriggerContext(event, source, targetData);
        targetData.getTriggers().dispatch(CoreTriggerTypes.VICTIM_INCOMING, victimContext);
        if (source instanceof MKDamageSource mkDamageSource && mkDamageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setNewDamage(mkDamageSource.getMKDamageType().applyResistance(targetData.getEntity(),
                    event.getNewDamage(), source));
        }
    }

    ///
    /// This fires after the damage calculations have been finalized and applied. Any triggers dispatched here can
    /// only react to the values, not modify them.
    @SubscribeEvent
    public static void onFinalDamageInflicted(LivingDamageEvent.Post event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.level().isClientSide) {
            return;
        }
        if (event.getNewDamage() <= 0.0f) {
            return;
        }

        DamageSource source = event.getSource();
        Entity trueSource = source.getEntity();

        if (trueSource instanceof LivingEntity livingSource) {
            var sourceData = MKCore.getEntityDataOrThrow(livingSource);
            var attackerContext = new AttackerDamageTriggerContext(event, source, livingTarget, sourceData);
            sourceData.getTriggers().dispatch(CoreTriggerTypes.ATTACKER_POST, attackerContext);
        }

        var targetData = MKCore.getEntityDataOrThrow(livingTarget);
        var victimContext = new VictimDamageTriggerContext(event, source, targetData);
        targetData.getTriggers().dispatch(CoreTriggerTypes.VICTIM_POST, victimContext);
    }

    private static void handleMKDamage(LivingDamageEvent.Pre event, MKDamageSource source, LivingEntity livingTarget,
                                       LivingEntity livingSource, IMKEntityData sourceData,
                                       AttackerDamageTriggerContext attackerContext) {
        Entity immediate = source.getDirectEntity() != null ? source.getDirectEntity() : livingSource;
        float newDamage = source.getMKDamageType().applyDamage(livingSource, livingTarget, immediate,
                event.getNewDamage(), source.getModifierScaling());
        boolean blocked = DamageUtils.wasAlreadyPartiallyBlocked(source);
        if (!blocked && source.getMKDamageType().rollCrit(livingSource, livingTarget, immediate)) {
            newDamage = source.getMKDamageType().applyCritDamage(livingSource, livingTarget, immediate, newDamage);
            CustomPacketPayload packet = source.createCritMessage(livingTarget.getId(), livingSource.getId(), newDamage);
            if (packet != null) {
                sendCritPacket(livingTarget, livingSource, packet);
            }
        }
        event.setNewDamage(newDamage);
        if (blocked) {
            return;
        }

        if (source.isMeleeDamage()) {
            sourceData.getTriggers().dispatch(CoreTriggerTypes.ATTACKER_MELEE, attackerContext);
        } else {
            sourceData.getTriggers().dispatch(CoreTriggerTypes.ATTACKER_MAGIC, attackerContext);
        }
    }

    private static void handleVanillaAndProjectileDamage(LivingDamageEvent.Pre event, DamageSource source,
                                                         LivingEntity livingTarget, LivingEntity livingSource,
                                                         IMKEntityData sourceData,
                                                         AttackerDamageTriggerContext attackerContext) {
        boolean blocked = DamageUtils.wasAlreadyPartiallyBlocked(source);
        if (DamageUtils.isVanillaMeleeDamage(source) && !blocked && sourceData instanceof MKPlayerData) {
            if (CoreDamageTypes.MeleeDamage.get().rollCrit(livingSource, livingTarget)) {
                float newDamage = CoreDamageTypes.MeleeDamage.get().applyCritDamage(livingSource, livingTarget,
                        event.getNewDamage());
                event.setNewDamage(newDamage);
                sendCritPacket(livingTarget, livingSource,
                        new MeleeCritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage));
            }
        }

        if (DamageUtils.isProjectileDamage(source)) {
            handleProjectileDamage(event, source, livingTarget, livingSource, blocked);
            if (!blocked) {
                sourceData.getTriggers().dispatch(CoreTriggerTypes.ATTACKER_PROJECTILE, attackerContext);
            }
        }

        if (!blocked && DamageUtils.isMeleeDamage(source)) {
            sourceData.getTriggers().dispatch(CoreTriggerTypes.ATTACKER_MELEE, attackerContext);
        } else if (!blocked && DamageUtils.isSpellDamage(source)) {
            sourceData.getTriggers().dispatch(CoreTriggerTypes.ATTACKER_MAGIC, attackerContext);
        }
    }

    private static void handleProjectileDamage(LivingDamageEvent.Pre event, DamageSource source,
                                               LivingEntity livingTarget, LivingEntity livingSource,
                                               boolean blocked) {
        Entity projectile = source.getDirectEntity();
        float damage = event.getNewDamage();
        if (DamageUtils.isVanillaProjectileDamage(source)) {
            damage += (float) livingSource.getAttributeValue(MKAttributes.RANGED_DAMAGE);
        }
        boolean wasCrit = false;
        if (!blocked && projectile != null && CoreDamageTypes.RangedDamage.get().rollCrit(livingSource, livingTarget, projectile)) {
            damage = CoreDamageTypes.RangedDamage.get().applyCritDamage(livingSource, livingTarget, projectile, damage);
            wasCrit = true;
        }
        damage = (float) (damage * (1.0 - livingTarget.getAttributeValue(MKAttributes.RANGED_RESISTANCE)));
        event.setNewDamage(damage);
        if (wasCrit && projectile != null) {
            sendCritPacket(livingTarget, livingSource,
                    new ProjectileCritMessagePacket(livingTarget.getId(), livingSource.getId(), damage, projectile.getId()));
        }
    }

    private static void sendCritPacket(LivingEntity livingTarget, LivingEntity livingSource,
                                       CustomPacketPayload packet) {
        PacketHandler.sendToTrackingAndSelf(packet, livingSource);
        Vec3 lookVec = livingTarget.getLookAngle();
        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.ENCHANTED_HIT,
                ParticleEffects.SPHERE_MOTION, 12, 4,
                livingTarget.getX(), livingTarget.getY() + 1.0f,
                livingTarget.getZ(), .5f, .5f, .5f, 0.2,
                lookVec), livingTarget);
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
            killerData.getTriggers().dispatch(CoreTriggerTypes.KILL,
                    new KillTriggerContext(event, source, killerData));
        }
    }
}
