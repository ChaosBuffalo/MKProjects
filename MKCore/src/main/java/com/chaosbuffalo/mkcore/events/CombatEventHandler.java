package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLeftClickEmptyPacket;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntity();
        if (livingTarget.level.isClientSide)
            return;

        DamageSource source = event.getSource();
        Entity trueSource = source.getEntity();
        if (source.is(DamageTypes.FALL)) { // TODO: maybe just use LivingFallEvent?
            SpellTriggers.FALL.onLivingFall(event, source, livingTarget);
        }

        // Living is source
        if (trueSource instanceof LivingEntity) {
            MKCore.getEntityData(trueSource).ifPresent(sourceData ->
                    SpellTriggers.LIVING_HURT_ENTITY.onLivingHurtEntity(event, source, livingTarget, sourceData));
        }

        // Living is victim
        MKCore.getEntityData(livingTarget).ifPresent(targetData ->
                SpellTriggers.ENTITY_HURT.onEntityHurtLiving(event, source, targetData));
    }

    @SubscribeEvent
    public static void onAttackEntityEvent(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level.isClientSide)
            return;
        Entity target = event.getTarget();

        SpellTriggers.PLAYER_ATTACK_ENTITY.onAttackEntity(player, target);
        if (target.is)
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity().level.isClientSide) {
            // Only send this spammy packet if someone will listen to it
            if (SpellTriggers.EMPTY_LEFT_CLICK.hasTriggers()) {
                PacketHandler.sendMessageToServer(new PlayerLeftClickEmptyPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmptyServer(ServerSideLeftClickEmpty event) {
        if (!event.getEntity().level.isClientSide) {
            SpellTriggers.EMPTY_LEFT_CLICK.onEmptyLeftClick(event.getEntity(), event);
        }
    }

    private static boolean canBlock(DamageSource source, LivingEntity entity) {
        if (DamageUtils.wasAlreadyPartiallyBlocked(source)) {
            return false;
        }

        return entity.isDamageSourceBlocked(source);
    }

    private static void playSound(LivingEntity target, Supplier<SoundEvent> sound) {
        SoundUtils.serverPlaySoundAtEntity(target, sound.get(), target.getSoundSource());
    }

    @SubscribeEvent
    public static void onLivingAttackEvent(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level.isClientSide)
            return;

        IMKEntityData targetData = MKCore.getEntityDataOrNull(target);
        if (targetData == null)
            return;

        DamageSource dmgSource = event.getSource();
        Entity source = dmgSource.getEntity();

        if (canBlock(dmgSource, target)) {
            IMKEntityStats.BlockResult breakResult = targetData.getStats().tryPoiseBlock(event.getAmount());
            float left = breakResult.damageLeft();
            if (!(dmgSource instanceof MKDamageSource)) {
                // correct for if we're a vanilla damage source and we're going to bypass armor so pre-apply armor
                if (DamageUtils.isProjectileDamage(dmgSource)) {
                    left = CoreDamageTypes.RangedDamage.get().applyResistance(target, left);
                } else {
                    left = CoreDamageTypes.MeleeDamage.get().applyResistance(target, left);
                }
            }
            // need to stop remainder damage from being blockable
            event.setCanceled(true);
            if (left > 0) {
                if (dmgSource instanceof IMKDamageSourceExtensions mkSrc) {
                    mkSrc.setCanBlock(false);
                }
                if (dmgSource instanceof MKDamageSource mk) {
                    mk.setSuppressTriggers(true);
                }
                target.hurt(dmgSource, left);
            }
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

        if (dmgSource instanceof MKDamageSource mkDamageSource) {
            if (mkDamageSource.shouldSuppressTriggers())
                return;
        }
        if (source instanceof LivingEntity attacker) {
            SpellTriggers.LIVING_ATTACKED.onAttacked(attacker, target);
        }
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent arrowEvent) {
        Entity shooter = arrowEvent.getProjectile().getOwner(); // getShooter
        if (shooter != null && arrowEvent.getProjectile() instanceof AbstractArrow) {
            MKCore.getEntityData(shooter).ifPresent(cap -> {
                if (arrowEvent.getRayTraceResult().getType() == HitResult.Type.BLOCK) {
                    cap.getCombatExtension().projectileMiss();
                } else {
                    cap.getCombatExtension().recordProjectileHit();
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(entityData ->
                entityData.getAbilityExecutor().interruptCast(CastInterruptReason.Death));

        DamageSource source = event.getSource();
        if (source.getEntity() instanceof LivingEntity killer) {
            if (killer.level.isClientSide) {
                return;
            }
            MKCore.getEntityData(killer).ifPresent(killerData -> {
                SpellTriggers.LIVING_KILL_ENTITY.onEntityDeath(event, source, killerData);
            });
        }

        SpellTriggers.LIVING_DEATH.onEntityDeath(event, source, event.getEntity());
    }
}
