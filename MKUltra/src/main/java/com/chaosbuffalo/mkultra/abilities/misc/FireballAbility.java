package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.entities.projectiles.FireballProjectileEntity;
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

public class FireballAbility extends MKAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "fireball_casting");
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 6.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 2.0f);
    protected final FloatAttribute projectileSpeed = new FloatAttribute("projectileSpeed", 1.25f);
    protected final FloatAttribute projectileInaccuracy = new FloatAttribute("projectileInaccuracy", 0.2f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final FloatAttribute radius = new FloatAttribute("explosionRadius", 2.0f);

    public FireballAbility() {
        super();
        setCooldownSeconds(4);
        setManaCost(5);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy,
                modifierScaling, radius);
        addSkillAttribute(MKAttributes.EVOCATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    public float getBaseDamage() {
        return baseDamage.value();
    }

    public float getScaleDamage() {
        return scaleDamage.value();
    }

    public float getExplosionRadius() {
        return radius.value();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, Function<Attribute, Float> skillSupplier) {
        float skillLevel = skillSupplier.apply(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.FireDamage.get(), baseDamage.value(),
                scaleDamage.value(), skillLevel,
                modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr, getExplosionRadius(),
                (skillLevel + 1) * .1f * 100.0f, skillLevel + 1);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 50.0f;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_fire_2.get();
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
        return MKUSounds.hostile_casting_fire.get();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context, Function<Attribute, Float> skillSupplier) {
        super.endCast(entity, data, context, skillSupplier);
        float level = skillSupplier.apply(MKAttributes.EVOCATION);
        FireballProjectileEntity proj = new FireballProjectileEntity(MKUEntities.FIREBALL_TYPE.get(), entity.level);
        proj.setOwner(entity);
        proj.setSkillLevel(level);
        shootProjectile(proj, projectileSpeed.value(), projectileInaccuracy.value(), entity, context);
        entity.level.addFreshEntity(proj);
    }
}
