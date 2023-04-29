package com.chaosbuffalo.mkultra.abilities.necromancer;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.entities.projectiles.ShadowBoltProjectileEntity;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ShadowBoltAbility extends MKAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_bolt_casting");
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 8.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 4.0f);
    protected final FloatAttribute projectileSpeed = new FloatAttribute("projectileSpeed", 1.25f);
    protected final FloatAttribute projectileInaccuracy = new FloatAttribute("projectileInaccuracy", 0.2f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);

    public ShadowBoltAbility() {
        super();
        setCooldownSeconds(8);
        setManaCost(7);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy, modifierScaling);
        addSkillAttribute(MKAttributes.EVOCATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    public float getBaseDamage() {
        return baseDamage.value();
    }

    public float getScaleDamage() {
        return scaleDamage.value();
    }

    @Override
    public int getCastTime(IMKEntityData casterData) {
        return casterData.getEffects().isEffectActive(MKUEffects.SHADOWBRINGER.get()) ? 0 : super.getCastTime(casterData);
    }

    @Override
    public float getManaCost(IMKEntityData casterData) {
        float cost = super.getManaCost(casterData);
        return casterData.getEffects().isEffectActive(MKUEffects.SHADOWBRINGER.get()) ? cost / 2.0f : cost;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, Function<Attribute, Float> skillSupplier, MKAbilityInfo abilityInfo) {
        float skillLevel = skillSupplier.apply(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.ShadowDamage.get(), baseDamage.value(),
                scaleDamage.value(), skillLevel,
                modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 50.0f;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_3.get();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PROJECTILE;
    }

    public float getModifierScaling() {
        return modifierScaling.value();
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_shadow.get();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context, Function<Attribute, Float> skillSupplier) {
        super.endCast(entity, data, context, skillSupplier);
        float level = skillSupplier.apply(MKAttributes.EVOCATION);
        if (data.getEffects().isEffectActive(MKUEffects.SHADOWBRINGER.get())) {
            data.getEffects().removeEffect(MKUEffects.SHADOWBRINGER.get());
        }
        ShadowBoltProjectileEntity proj = new ShadowBoltProjectileEntity(MKUEntities.SHADOWBOLT_TYPE.get(), entity.level);
        proj.setOwner(entity);
        proj.setSkillLevel(level);
        shootProjectile(proj, projectileSpeed.value(), projectileInaccuracy.value(), entity, context);
        entity.level.addFreshEntity(proj);
    }
}
