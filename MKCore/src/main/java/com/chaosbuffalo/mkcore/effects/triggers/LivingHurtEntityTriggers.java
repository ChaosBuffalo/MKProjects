package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.network.CritMessagePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class LivingHurtEntityTriggers extends SpellTriggers.TriggerCollectionBase {

    @FunctionalInterface
    public interface Trigger {
        void apply(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                   IMKEntityData attackerData);
    }

    private static final String MELEE_TAG = "LIVING_HURT_ENTITY.melee";
    private static final String MAGIC_TAG = "LIVING_HURT_ENTITY.magic";
    private static final String POST_TAG = "LIVING_HURT_ENTITY.post";
    private static final String PROJECTILE_TAG = "LIVING_HURT_ENTITY.projectile";

    private static final String MELEE_EFFECT_TAG = "LIVING_HURT_ENTITY.melee_effect";
    private static final String MAGIC_EFFECT_TAG = "LIVING_HURT_ENTITY.magic_effect";
    private static final String POST_EFFECT_TAG = "LIVING_HURT_ENTITY.post_effect";
    private static final String PROJECTILE_EFFECT_TAG = "LIVING_HURT_ENTITY.projectile_effect";
    private static final List<Trigger> livingHurtEntityMeleeTriggers = new ArrayList<>();
    private static final List<Trigger> livingHurtEntityMagicTriggers = new ArrayList<>();
    private static final List<Trigger> livingHurtEntityPostTriggers = new ArrayList<>();
    private static final List<Trigger> livingHurtEntityProjectileTriggers = new ArrayList<>();

    private static final LivingHurtEntityEffectTriggers livingHurtEntityMeleeEffectTriggers = new LivingHurtEntityEffectTriggers(MELEE_EFFECT_TAG);

    private static final LivingHurtEntityEffectTriggers livingHurtEntityMagicEffectTriggers = new LivingHurtEntityEffectTriggers(MAGIC_EFFECT_TAG);

    private static final LivingHurtEntityEffectTriggers livingHurtEntityPostEffectTriggers = new LivingHurtEntityEffectTriggers(POST_EFFECT_TAG);

    private static final LivingHurtEntityEffectTriggers livingHurtEntityProjectileEffectTriggers = new LivingHurtEntityEffectTriggers(PROJECTILE_EFFECT_TAG);

    public static class LivingHurtEntityEffectTriggers extends SpellTriggers.EffectBasedTriggerCollection<LivingHurtEntityEffectTriggers.Trigger> {
        private final String tag;

        public LivingHurtEntityEffectTriggers(String tag) {
            this.tag = tag;
        }

        @FunctionalInterface
        public interface Trigger {
            void apply(LivingDamageEvent.Pre event, DamageSource source,
                       LivingEntity livingTarget, IMKEntityData sourceData, MKActiveEffect effect);
        }

        public void onLivingHurtEntity(LivingDamageEvent.Pre event, DamageSource source,
                                       LivingEntity livingTarget, IMKEntityData sourceData) {
            runTrigger(sourceData, tag, (trigger, instance) ->
                    trigger.apply(event, source, livingTarget, sourceData, instance));
        }
    }

    private boolean hasTriggers = false;

    @Override
    public boolean hasTriggers() {
        return hasTriggers;
    }

    public void registerMelee(Trigger trigger) {
        livingHurtEntityMeleeTriggers.add(trigger);
        hasTriggers = true;
    }

    public void registerMeleeEffect(MKEffect effect, LivingHurtEntityEffectTriggers.Trigger trigger) {
        livingHurtEntityMeleeEffectTriggers.register(effect, trigger);
        hasTriggers = true;
    }

    public void registerMagicEffect(MKEffect effect, LivingHurtEntityEffectTriggers.Trigger trigger) {
        livingHurtEntityMagicEffectTriggers.register(effect, trigger);
        hasTriggers = true;
    }

    public void registerProjectileEffect(MKEffect effect, LivingHurtEntityEffectTriggers.Trigger trigger) {
        livingHurtEntityProjectileEffectTriggers.register(effect, trigger);
        hasTriggers = true;
    }

    public void registerPostEffect(MKEffect effect, LivingHurtEntityEffectTriggers.Trigger trigger) {
        livingHurtEntityPostEffectTriggers.register(effect, trigger);
        hasTriggers = true;
    }

    public void registerMagic(Trigger trigger) {
        livingHurtEntityMagicTriggers.add(trigger);
        hasTriggers = true;
    }

    public void registerProjectile(Trigger trigger) {
        livingHurtEntityProjectileTriggers.add(trigger);
        hasTriggers = true;
    }

    public void registerPostHandler(Trigger trigger) {
        livingHurtEntityPostTriggers.add(trigger);
        hasTriggers = true;
    }

    public void onLivingHurtEntity(LivingDamageEvent.Pre event, DamageSource source,
                                   LivingEntity livingTarget, IMKEntityData sourceData) {
        // A blocked source can still have remaining damage; only zero-damage full blocks
        // should skip the attacker-side trigger pipeline entirely.
        if (DamageUtils.isFullyBlockedDamage(source, event.getNewDamage())) {
            return;
        }
        LivingEntity livingSource = sourceData.getEntity();
        if (source instanceof MKDamageSource mkSource) {
            if (mkSource.isMeleeDamage()) {
                handleMKMelee(event, mkSource, livingTarget, livingSource, sourceData);
            } else {
                handleMKDamage(event, mkSource, livingTarget, livingSource, sourceData);
            }
        }

        // If this is a weapon swing
        if (DamageUtils.isMinecraftPhysicalDamage(source)) {
            handleVanillaMelee(event, source, livingTarget, livingSource, sourceData);
        }

        if (DamageUtils.isProjectileDamage(source)) {
            handleProjectile(event, source, livingTarget, livingSource, sourceData);
        }
        if (livingHurtEntityPostEffectTriggers.hasTriggers()) {
            livingHurtEntityPostEffectTriggers.onLivingHurtEntity(event, source, livingTarget, sourceData);
        }
        if (livingHurtEntityPostTriggers.isEmpty() || startTrigger(sourceData, POST_TAG))
            return;
        livingHurtEntityPostTriggers.forEach(f -> f.apply(event, source, livingTarget, sourceData));
        endTrigger(sourceData, POST_TAG);
    }

    private void handleMKDamage(LivingDamageEvent.Pre event, MKDamageSource source, LivingEntity livingTarget,
                                LivingEntity livingSource,
                                IMKEntityData sourceData) {
        calculateMKDamage(event, livingTarget, livingSource, sourceData, source,
                MAGIC_TAG, livingHurtEntityMagicTriggers, livingHurtEntityMagicEffectTriggers);
    }

    private static boolean wasBlocked(DamageSource source) {
        return source instanceof IMKDamageSourceExtensions ext && ext.wasBlocked();
    }

    private void calculateMKDamage(LivingDamageEvent.Pre event, LivingEntity livingTarget,
                                   LivingEntity livingSource, IMKEntityData sourceData,
                                   MKDamageSource source, String typeTag,
                                   List<Trigger> playerHurtTriggers, LivingHurtEntityEffectTriggers effectTriggers) {
        Entity immediate = source.getDirectEntity() != null ? source.getDirectEntity() : livingSource;
        float newDamage = source.getMKDamageType().applyDamage(livingSource, livingTarget, immediate, event.getNewDamage(), source.getModifierScaling());
        boolean notBlocked = !wasBlocked(source);
        if (notBlocked && source.getMKDamageType().rollCrit(livingSource, livingTarget, immediate)) {
            newDamage = source.getMKDamageType().applyCritDamage(livingSource, livingTarget, immediate, newDamage);
            if (!source.isMeleeDamage()) {
                MKCore.getAbilityRuntimeService().emitSpellCrit(source, livingTarget, newDamage);
            }
            switch (source.getOrigination()) {
                case MK_ABILITY:
                    sendAbilityCrit(livingTarget, livingSource, source, newDamage);
                    break;
                case DAMAGE_TYPE:
                    sendEffectCrit(livingTarget, livingSource, source, newDamage);
                    break;
            }
        }
        event.setNewDamage(newDamage);
        if (!notBlocked) {
            return;
        }
        if (effectTriggers.hasTriggers()) {
            effectTriggers.onLivingHurtEntity(event, source, livingTarget, sourceData);
        }
        if (playerHurtTriggers.isEmpty() || startTrigger(sourceData, typeTag))
            return;
        playerHurtTriggers.forEach(f -> f.apply(event, source, livingTarget, sourceData));
        endTrigger(sourceData, typeTag);
    }

    private void sendEffectCrit(LivingEntity livingTarget, LivingEntity livingSource, MKDamageSource source,
                                float newDamage) {
        if (source instanceof MKDamageSource.EffectDamage effectDamage) {
            InteractionHand hand = source.getAttackHand() != null ? source.getAttackHand() : MKCore.getEntityData(livingSource)
                    .map(data -> data.getCombatExtension().getActiveAttackHand())
                    .orElse(InteractionHand.MAIN_HAND);
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage,
                            source.getMKDamageType(), effectDamage.getDamageTypeName(), hand));
        }
    }

    private void sendAbilityCrit(LivingEntity livingTarget, LivingEntity livingSource, MKDamageSource source,
                                 float newDamage) {
        if (source instanceof MKDamageSource.AbilityDamage abilityDamage) {
            MKAbility ability = MKCoreRegistry.getAbility(abilityDamage.getAbilityId());
            ResourceLocation abilityName;
            if (ability != null) {
                abilityName = ability.getAbilityId();
            } else {
                abilityName = MKCoreRegistry.INVALID_ABILITY;
            }
            InteractionHand hand = source.getAttackHand() != null ? source.getAttackHand() : MKCore.getEntityData(livingSource)
                    .map(data -> data.getCombatExtension().getActiveAttackHand())
                    .orElse(InteractionHand.MAIN_HAND);
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage,
                            abilityName, source.getMKDamageType(), hand));
        }
    }

    private void handleProjectile(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                  LivingEntity livingSource, IMKEntityData sourceData) {

        boolean blocked = wasBlocked(source);
        Entity projectile = source.getDirectEntity();
        float damage = event.getNewDamage();
        if (DamageUtils.isNonMKProjectileDamage(source)) {
            damage += (float) livingSource.getAttributeValue(MKAttributes.RANGED_DAMAGE);
        }
        boolean wasCrit = false;
        if (!blocked && projectile != null && CoreDamageTypes.RangedDamage.get().rollCrit(livingSource, livingTarget, projectile)) {
            damage = CoreDamageTypes.RangedDamage.get().applyCritDamage(livingSource, livingTarget, projectile, damage);
            wasCrit = true;
        }
        // Partial shield blocks still leave residual projectile damage that should
        // continue through the ranged resistance calculation.
        damage = (float) (damage * (1.0 - livingTarget.getAttributeValue(MKAttributes.RANGED_RESISTANCE)));
        event.setNewDamage(damage);
        if (wasCrit) {
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), damage,
                            projectile.getId()));
        }
        if (blocked)
            return;
        if (livingHurtEntityProjectileEffectTriggers.hasTriggers()) {
            livingHurtEntityProjectileEffectTriggers.onLivingHurtEntity(event, source, livingTarget, sourceData);
        }
        if (livingHurtEntityProjectileTriggers.isEmpty() || startTrigger(sourceData, PROJECTILE_TAG))
            return;
        livingHurtEntityProjectileTriggers.forEach(f -> f.apply(event, source, livingTarget, sourceData));
        endTrigger(sourceData, PROJECTILE_TAG);
    }

    private void handleMKMelee(LivingDamageEvent.Pre event, MKDamageSource source, LivingEntity livingTarget,
                               LivingEntity livingSource, IMKEntityData sourceData) {

        calculateMKDamage(event, livingTarget, livingSource, sourceData, source,
                MELEE_TAG, livingHurtEntityMeleeTriggers, livingHurtEntityMeleeEffectTriggers);
    }

    private void handleVanillaMelee(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                    LivingEntity livingSource, IMKEntityData sourceData) {
        boolean blocked = wasBlocked(source);
        // A blocked vanilla melee hit may still have residual damage after poise absorption.
        // That remainder should continue through vanilla damage resolution, but MK melee crits
        // and attacker-side melee triggers should not fire on shield-blocked hits.
        if (!blocked && sourceData instanceof MKPlayerData) {
            if (CoreDamageTypes.MeleeDamage.get().rollCrit(livingSource, livingTarget)) {
                float newDamage = CoreDamageTypes.MeleeDamage.get().applyCritDamage(livingSource, livingTarget, event.getNewDamage());
                event.setNewDamage(newDamage);
                var critHand = sourceData.getCombatExtension().getActiveAttackHand();
                sendCritPacket(livingTarget, livingSource,
                        new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage, critHand));
            }
        }
        if (blocked)
            return;
        if (livingHurtEntityMeleeEffectTriggers.hasTriggers()) {
            livingHurtEntityMeleeEffectTriggers.onLivingHurtEntity(event, source, livingTarget, sourceData);
        }
        if (livingHurtEntityMeleeTriggers.isEmpty() || startTrigger(sourceData, MELEE_TAG))
            return;
        livingHurtEntityMeleeTriggers.forEach(f -> f.apply(event, source, livingTarget, sourceData));
        endTrigger(sourceData, MELEE_TAG);
    }

    private static void sendCritPacket(LivingEntity livingTarget, LivingEntity livingSource,
                                       CritMessagePacket packet) {
        PacketHandler.sendToTrackingAndSelf(packet, livingSource);
        Vec3 lookVec = livingTarget.getLookAngle();
        PacketHandler.sendToTrackingAndSelf(new ParticleEffectSpawnPacket(
                ParticleTypes.ENCHANTED_HIT,
                ParticleEffects.SPHERE_MOTION, 12, 4,
                livingTarget.getX(), livingTarget.getY() + 1.0f,
                livingTarget.getZ(), .5f, .5f, .5f, 0.2,
                lookVec), livingTarget);
    }
}
