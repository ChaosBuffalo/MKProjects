package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkultra.entities.projectiles.AbilityProjectileEntity;
import com.chaosbuffalo.mkultra.entities.projectiles.FireballProjectileEntity;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.HitResult;

public abstract class ProjectileAbility extends MKAbility {
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 6.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 2.0f);
    protected final FloatAttribute projectileSpeed = new FloatAttribute("projectileSpeed", 1.25f);
    protected final FloatAttribute projectileInaccuracy = new FloatAttribute("projectileInaccuracy", 0.2f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final Attribute skill;

    public ProjectileAbility(Attribute skillAttribute) {
        super();
        addSkillAttribute(skillAttribute);
        skill = skillAttribute;
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy, modifierScaling);
    }

    public float getBaseDamage() {
        return baseDamage.value();
    }

    public float getScaleDamage() {
        return scaleDamage.value();
    }

    public float getModifierScaling() {
        return modifierScaling.value();
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 50.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PROJECTILE;
    }

    public abstract boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier);

    public abstract AbilityProjectileEntity makeProjectile(LivingEntity entity, IMKEntityData data, AbilityContext context);

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(skill);
        AbilityProjectileEntity proj = makeProjectile(entity, data, context);
        proj.setOwner(entity);
        proj.setSkillLevel(level);
        shootProjectile(proj, projectileSpeed.value(), projectileInaccuracy.value(), entity, context);
        entity.level.addFreshEntity(proj);
    }
}
