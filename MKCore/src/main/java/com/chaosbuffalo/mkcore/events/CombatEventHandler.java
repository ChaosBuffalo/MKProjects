package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLeftClickEmptyPacket;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingTarget = event.getEntityLiving();
        if (livingTarget.level.isClientSide)
            return;

        DamageSource source = event.getSource();
        Entity trueSource = source.getEntity();
        if (source == DamageSource.FALL) { // TODO: maybe just use LivingFallEvent?
            SpellTriggers.FALL.onLivingFall(event, source, livingTarget);
        }

        // Living is source
        if (trueSource instanceof LivingEntity) {
            MKCore.getEntityData(trueSource).ifPresent(sourceData ->
                    SpellTriggers.LIVING_HURT_ENTITY.onLivingHurtEntity(event, source, livingTarget, sourceData));
        }

        // Living is victim
        MKCore.getEntityData(livingTarget).ifPresent(targetData ->
                SpellTriggers.ENTITY_HURT.onEntityHurtLiving(event, source, livingTarget, targetData));
    }

    @SubscribeEvent
    public static void onAttackEntityEvent(AttackEntityEvent event) {
        Player player = event.getPlayer();
        if (player.level.isClientSide)
            return;
        Entity target = event.getTarget();

        SpellTriggers.PLAYER_ATTACK_ENTITY.onAttackEntity(player, target);
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getPlayer().level.isClientSide) {
            // Only send this spammy packet if someone will listen to it
            if (SpellTriggers.EMPTY_LEFT_CLICK.hasTriggers()) {
                PacketHandler.sendMessageToServer(new PlayerLeftClickEmptyPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmptyServer(ServerSideLeftClickEmpty event) {
        if (!event.getPlayer().level.isClientSide) {
            SpellTriggers.EMPTY_LEFT_CLICK.onEmptyLeftClick(event.getPlayer(), event);
        }
    }

    private static boolean canBlock(DamageSource source, LivingEntity entity) {

        Entity sourceEntity = source.getDirectEntity();
        boolean hasPiercing = false;
        if (sourceEntity instanceof AbstractArrow) {
            AbstractArrow abstractarrowentity = (AbstractArrow) sourceEntity;
            if (abstractarrowentity.getPierceLevel() > 0) {
                hasPiercing = true;
            }
        }

        if (!source.isBypassArmor() && entity.isBlocking() && !hasPiercing) {
            Vec3 damageLoc = source.getSourcePosition();
            if (damageLoc != null) {
                Vec3 lookVec = entity.getViewVector(1.0F);
                Vec3 damageDir = damageLoc.vectorTo(entity.position()).normalize();
                damageDir = new Vec3(damageDir.x, 0.0D, damageDir.z);
                if (damageDir.dot(lookVec) < 0.0D) {
                    return true;
                }
            }
        }
        return false;

    }

    @SubscribeEvent
    public static void onLivingAttackEvent(LivingAttackEvent event) {
        Entity target = event.getEntity();
        if (target.level.isClientSide)
            return;

        DamageSource dmgSource = event.getSource();
        Entity source = dmgSource.getEntity();

        if (canBlock(dmgSource, event.getEntityLiving())) {
            MKCore.getPlayer(target).ifPresent(playerData -> {
                Tuple<Float, Boolean> breakResult = playerData.getStats().handlePoiseDamage(event.getAmount());
                float left = breakResult.getA();
                if (!(dmgSource instanceof MKDamageSource)) {
                    // correct for if we're a vanilla damage source and we're going to bypass armor so pre-apply armor
                    if (DamageUtils.isProjectileDamage(dmgSource)) {
                        left = CoreDamageTypes.RangedDamage.applyResistance(event.getEntityLiving(), left);
                    } else {
                        left = CoreDamageTypes.MeleeDamage.applyResistance(event.getEntityLiving(), left);
                    }

                }
                event.setCanceled(true);
                if (left > 0) {
                    target.hurt(dmgSource instanceof MKDamageSource ? ((MKDamageSource) dmgSource)
                                    .setSuppressTriggers(true).bypassArmor() : dmgSource.bypassArmor(),
                            left);
                }
                if (breakResult.getB()) {
                    SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                            CoreSounds.block_break.get(), event.getEntityLiving().getSoundSource());
                } else {
                    if (event.getEntityLiving().getTicksUsingItem() <= 6) {
                        SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                CoreSounds.parry.get(), event.getEntityLiving().getSoundSource());
                        playerData.getSkills().tryIncreaseSkill(MKAttributes.BLOCK);
                    } else {
                        playerData.getSkills().tryScaledIncreaseSkill(MKAttributes.BLOCK, 0.5);
                        if (dmgSource.getDirectEntity() instanceof AbstractArrow) {
                            SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                    CoreSounds.arrow_block.get(), event.getEntityLiving().getSoundSource());
                        } else if (source instanceof LivingEntity) {
                            if (((LivingEntity) source).getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwordItem) {
                                SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                        CoreSounds.weapon_block.get(), event.getEntityLiving().getSoundSource());
                            } else {
                                SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                        CoreSounds.fist_block.get(), event.getEntityLiving().getSoundSource());
                            }
                        } else {
                            SoundUtils.serverPlaySoundAtEntity(event.getEntityLiving(),
                                    CoreSounds.fist_block.get(), event.getEntityLiving().getSoundSource());
                        }
                    }
                }
            });
        }
        if (dmgSource instanceof MKDamageSource) {
            if (((MKDamageSource) dmgSource).shouldSuppressTriggers())
                return;
        }
        if (source instanceof LivingEntity) {
            SpellTriggers.LIVING_ATTACKED.onAttacked((LivingEntity) source, target);
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
        MKCore.getEntityData(event.getEntityLiving()).ifPresent(entityData ->
                entityData.getAbilityExecutor().interruptCast(CastInterruptReason.Death));

        DamageSource source = event.getSource();
        if (source.getEntity() instanceof LivingEntity) {
            LivingEntity killer = (LivingEntity) source.getEntity();
            if (killer.level.isClientSide) {
                return;
            }
            SpellTriggers.LIVING_KILL_ENTITY.onEntityDeath(event, source, killer);
        }

        SpellTriggers.LIVING_DEATH.onEntityDeath(event, source, event.getEntityLiving());
    }


}
