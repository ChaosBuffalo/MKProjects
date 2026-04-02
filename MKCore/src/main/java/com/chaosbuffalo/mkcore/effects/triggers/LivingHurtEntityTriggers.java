package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.combat.damage.MKDamageContext;
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
        applyDamageBonuses(event, source, livingTarget, sourceData);
        applyCrits(event, source, livingTarget, sourceData);
        dispatchTriggers(event, source, livingTarget, sourceData);
    }

    public void applyDamageAdjustments(LivingDamageEvent.Pre event, DamageSource source,
                                       LivingEntity livingTarget, IMKEntityData sourceData) {
        applyDamageBonuses(event, source, livingTarget, sourceData);
        applyCrits(event, source, livingTarget, sourceData);
    }

    public void applyDamageBonuses(LivingDamageEvent.Pre event, DamageSource source,
                                   LivingEntity livingTarget, IMKEntityData sourceData) {
        // A blocked source can still have remaining damage; only zero-damage full blocks
        // should skip the attacker-side trigger pipeline entirely.
        if (DamageUtils.isFullyBlockedDamage(source, event.getNewDamage())) {
            return;
        }
        LivingEntity livingSource = sourceData.getEntity();
        if (source instanceof MKDamageSource mkSource) {
            applyMKDamageBonus(event, mkSource, livingTarget, livingSource);
        }

        if (DamageUtils.isProjectileDamage(source)) {
            applyProjectileDamageBonus(event, source, livingSource);
        }
    }

    public void applyDamageBonuses(MKDamageContext context) {
        if (context.isFullyBlocked() || context.getAttackerData() == null) {
            return;
        }
        DamageSource source = context.getSource();
        LivingEntity livingTarget = context.getTarget();
        LivingEntity livingSource = context.getAttacker();
        if (livingSource == null) {
            return;
        }
        if (source instanceof MKDamageSource mkSource) {
            applyMKDamageBonus(context, mkSource, livingTarget, livingSource);
        }
        if (DamageUtils.isProjectileDamage(source)) {
            applyProjectileDamageBonus(context, source, livingSource);
        }
    }

    public void applyCrits(LivingDamageEvent.Pre event, DamageSource source,
                           LivingEntity livingTarget, IMKEntityData sourceData) {
        if (DamageUtils.isFullyBlockedDamage(source, event.getNewDamage())) {
            return;
        }
        LivingEntity livingSource = sourceData.getEntity();
        if (source instanceof MKDamageSource mkSource) {
            applyMKCrit(event, mkSource, livingTarget, livingSource);
        }

        if (DamageUtils.isMinecraftPhysicalDamage(source)) {
            applyVanillaMeleeCrit(event, source, livingTarget, livingSource, sourceData);
        }

        if (DamageUtils.isProjectileDamage(source)) {
            applyProjectileCrit(event, source, livingTarget, livingSource);
        }
    }

    public void applyCrits(MKDamageContext context) {
        if (context.isFullyBlocked() || context.getAttackerData() == null) {
            return;
        }
        DamageSource source = context.getSource();
        LivingEntity livingTarget = context.getTarget();
        LivingEntity livingSource = context.getAttacker();
        if (livingSource == null) {
            return;
        }
        if (source instanceof MKDamageSource mkSource) {
            applyMKCrit(context, mkSource, livingTarget, livingSource);
        }
        if (DamageUtils.isMinecraftPhysicalDamage(source)) {
            applyVanillaMeleeCrit(context, source, livingTarget, livingSource, context.getAttackerData());
        }
        if (DamageUtils.isProjectileDamage(source)) {
            applyProjectileCrit(context, source, livingTarget, livingSource);
        }
    }

    public void dispatchTriggers(LivingDamageEvent.Pre event, DamageSource source,
                                 LivingEntity livingTarget, IMKEntityData sourceData) {
        if (DamageUtils.isFullyBlockedDamage(source, event.getNewDamage())) {
            return;
        }
        if (source instanceof MKDamageSource mkSource) {
            if (mkSource.isMeleeDamage()) {
                dispatchMeleeTriggers(event, source, livingTarget, sourceData);
            } else {
                dispatchMagicTriggers(event, source, livingTarget, sourceData);
            }
        }

        if (DamageUtils.isMinecraftPhysicalDamage(source)) {
            dispatchMeleeTriggers(event, source, livingTarget, sourceData);
        }

        if (DamageUtils.isProjectileDamage(source)) {
            dispatchProjectileTriggers(event, source, livingTarget, sourceData);
        }
        if (livingHurtEntityPostEffectTriggers.hasTriggers()) {
            livingHurtEntityPostEffectTriggers.onLivingHurtEntity(event, source, livingTarget, sourceData);
        }
        if (livingHurtEntityPostTriggers.isEmpty() || startTrigger(sourceData, POST_TAG))
            return;
        livingHurtEntityPostTriggers.forEach(f -> f.apply(event, source, livingTarget, sourceData));
        endTrigger(sourceData, POST_TAG);
    }

    public void dispatchTriggers(MKDamageContext context) {
        if (context.isFullyBlocked() || context.getAttackerData() == null || context.getAttacker() == null) {
            return;
        }
        context.runLegacyEventMutation("mkcore:attacker_triggers", event ->
                dispatchTriggers(event, context.getSource(), context.getTarget(), context.getAttackerData()));
    }

    private static boolean wasBlocked(DamageSource source) {
        return source instanceof IMKDamageSourceExtensions ext && ext.wasBlocked();
    }

    private void applyMKDamageBonus(LivingDamageEvent.Pre event, MKDamageSource source,
                                    LivingEntity livingTarget, LivingEntity livingSource) {
        Entity immediate = source.getDirectEntity() != null ? source.getDirectEntity() : livingSource;
        event.setNewDamage(source.getMKDamageType().applyDamage(
                livingSource, livingTarget, immediate, event.getNewDamage(), source.getModifierScaling()));
    }

    private void applyMKDamageBonus(MKDamageContext context, MKDamageSource source,
                                    LivingEntity livingTarget, LivingEntity livingSource) {
        Entity immediate = source.getDirectEntity() != null ? source.getDirectEntity() : livingSource;
        float newDamage = source.getMKDamageType().applyDamage(
                livingSource, livingTarget, immediate, context.getWorkingDamage(), source.getModifierScaling());
        context.setWorkingDamage(newDamage, "mkcore:attacker_damage_bonus");
    }

    private void applyMKCrit(LivingDamageEvent.Pre event, MKDamageSource source,
                             LivingEntity livingTarget, LivingEntity livingSource) {
        Entity immediate = source.getDirectEntity() != null ? source.getDirectEntity() : livingSource;
        if (!wasBlocked(source) && source.getMKDamageType().rollCrit(livingSource, livingTarget, immediate)) {
            float newDamage = source.getMKDamageType().applyCritDamage(livingSource, livingTarget, immediate, event.getNewDamage());
            event.setNewDamage(newDamage);
            switch (source.getOrigination()) {
                case MK_ABILITY:
                    sendAbilityCrit(livingTarget, livingSource, source, newDamage);
                    break;
                case DAMAGE_TYPE:
                    sendEffectCrit(livingTarget, livingSource, source, newDamage);
                    break;
            }
        }
    }

    private void applyMKCrit(MKDamageContext context, MKDamageSource source,
                             LivingEntity livingTarget, LivingEntity livingSource) {
        Entity immediate = source.getDirectEntity() != null ? source.getDirectEntity() : livingSource;
        if (!wasBlocked(source) && source.getMKDamageType().rollCrit(livingSource, livingTarget, immediate)) {
            float newDamage = source.getMKDamageType().applyCritDamage(
                    livingSource, livingTarget, immediate, context.getWorkingDamage());
            context.setWorkingDamage(newDamage, "mkcore:attacker_crit");
            switch (source.getOrigination()) {
                case MK_ABILITY:
                    sendAbilityCrit(livingTarget, livingSource, source, newDamage);
                    break;
                case DAMAGE_TYPE:
                    sendEffectCrit(livingTarget, livingSource, source, newDamage);
                    break;
            }
        }
    }

    private void sendEffectCrit(LivingEntity livingTarget, LivingEntity livingSource, MKDamageSource source,
                                float newDamage) {
        if (source instanceof MKDamageSource.EffectDamage effectDamage) {
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage,
                            source.getMKDamageType(), effectDamage.getDamageTypeName()));
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
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage,
                            abilityName, source.getMKDamageType()));
        }
    }

    private void applyProjectileDamageBonus(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingSource) {
        if (DamageUtils.isNonMKProjectileDamage(source)) {
            event.setNewDamage(event.getNewDamage() + (float) livingSource.getAttributeValue(MKAttributes.RANGED_DAMAGE));
        }
    }

    private void applyProjectileDamageBonus(MKDamageContext context, DamageSource source, LivingEntity livingSource) {
        if (DamageUtils.isNonMKProjectileDamage(source)) {
            context.setWorkingDamage(context.getWorkingDamage() + (float) livingSource.getAttributeValue(MKAttributes.RANGED_DAMAGE),
                    "mkcore:projectile_damage_bonus");
        }
    }

    private void applyProjectileCrit(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                     LivingEntity livingSource) {
        boolean blocked = wasBlocked(source);
        Entity projectile = source.getDirectEntity();
        if (!blocked && projectile != null && CoreDamageTypes.RangedDamage.get().rollCrit(livingSource, livingTarget, projectile)) {
            float damage = CoreDamageTypes.RangedDamage.get().applyCritDamage(livingSource, livingTarget, projectile, event.getNewDamage());
            event.setNewDamage(damage);
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), damage, projectile.getId()));
        }
    }

    private void applyProjectileCrit(MKDamageContext context, DamageSource source, LivingEntity livingTarget,
                                     LivingEntity livingSource) {
        boolean blocked = wasBlocked(source);
        Entity projectile = source.getDirectEntity();
        if (!blocked && projectile != null && CoreDamageTypes.RangedDamage.get().rollCrit(livingSource, livingTarget, projectile)) {
            float damage = CoreDamageTypes.RangedDamage.get().applyCritDamage(
                    livingSource, livingTarget, projectile, context.getWorkingDamage());
            context.setWorkingDamage(damage, "mkcore:projectile_crit");
            sendCritPacket(livingTarget, livingSource,
                    new CritMessagePacket(livingTarget.getId(), livingSource.getId(), damage, projectile.getId()));
        }
    }

    private void applyVanillaMeleeCrit(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                       LivingEntity livingSource, IMKEntityData sourceData) {
        boolean blocked = wasBlocked(source);
        // A blocked vanilla melee hit may still have residual damage after poise absorption.
        // That remainder should continue through vanilla damage resolution, but MK melee crits
        // and attacker-side melee triggers should not fire on shield-blocked hits.
        if (!blocked && sourceData instanceof MKPlayerData) {
            if (CoreDamageTypes.MeleeDamage.get().rollCrit(livingSource, livingTarget)) {
                float newDamage = CoreDamageTypes.MeleeDamage.get().applyCritDamage(livingSource, livingTarget, event.getNewDamage());
                event.setNewDamage(newDamage);
                sendCritPacket(livingTarget, livingSource,
                        new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage));
            }
        }
    }

    private void applyVanillaMeleeCrit(MKDamageContext context, DamageSource source, LivingEntity livingTarget,
                                       LivingEntity livingSource, IMKEntityData sourceData) {
        boolean blocked = wasBlocked(source);
        if (!blocked && sourceData instanceof MKPlayerData) {
            if (CoreDamageTypes.MeleeDamage.get().rollCrit(livingSource, livingTarget)) {
                float newDamage = CoreDamageTypes.MeleeDamage.get().applyCritDamage(
                        livingSource, livingTarget, context.getWorkingDamage());
                context.setWorkingDamage(newDamage, "mkcore:melee_crit");
                sendCritPacket(livingTarget, livingSource,
                        new CritMessagePacket(livingTarget.getId(), livingSource.getId(), newDamage));
            }
        }
    }

    private void dispatchMagicTriggers(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                       IMKEntityData sourceData) {
        if (wasBlocked(source)) {
            return;
        }
        dispatchTypedTriggers(event, source, livingTarget, sourceData, MAGIC_TAG,
                livingHurtEntityMagicTriggers, livingHurtEntityMagicEffectTriggers);
    }

    private void dispatchProjectileTriggers(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                            IMKEntityData sourceData) {
        if (wasBlocked(source)) {
            return;
        }
        dispatchTypedTriggers(event, source, livingTarget, sourceData, PROJECTILE_TAG,
                livingHurtEntityProjectileTriggers, livingHurtEntityProjectileEffectTriggers);
    }

    private void dispatchMeleeTriggers(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                       IMKEntityData sourceData) {
        if (wasBlocked(source))
            return;
        dispatchTypedTriggers(event, source, livingTarget, sourceData, MELEE_TAG,
                livingHurtEntityMeleeTriggers, livingHurtEntityMeleeEffectTriggers);
    }

    private void dispatchTypedTriggers(LivingDamageEvent.Pre event, DamageSource source, LivingEntity livingTarget,
                                       IMKEntityData sourceData, String typeTag, List<Trigger> playerHurtTriggers,
                                       LivingHurtEntityEffectTriggers effectTriggers) {
        if (effectTriggers.hasTriggers()) {
            effectTriggers.onLivingHurtEntity(event, source, livingTarget, sourceData);
        }
        if (playerHurtTriggers.isEmpty() || startTrigger(sourceData, typeTag))
            return;
        playerHurtTriggers.forEach(f -> f.apply(event, source, livingTarget, sourceData));
        endTrigger(sourceData, typeTag);
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
