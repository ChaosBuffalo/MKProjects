package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class FireballAbility extends ProjectileAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("fireball.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("fireball.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("fireball.damage.modifier_scaling"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("fireball_casting");
    protected final FloatAttribute radius = new FloatAttribute("explosionRadius", 2.0f);
    public static final ResourceLocation DETONATE_PARTICLES = MKUltra.id("fireball_detonate");
    public static final ResourceLocation TRAIL_PARTICLES = MKUltra.id("fireball_trail");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 6.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 2.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));

    public FireballAbility() {
        super(MKAttributes.EVOCATION, false);
        setCooldownSeconds(4);
        setManaCost(5);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(radius, formulaParameters, damage);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
    }

    public float getExplosionRadius() {
        return radius.value();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float skillLevel = context.getSkill(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.FireDamage.get(), damage.value(),
                formulaParameters.value(), skillLevel);
        return Component.translatable(getDescriptionTranslationKey(), damageStr, getExplosionRadius(),
                (skillLevel + 1) * .1f * 100.0f, skillLevel + 1);
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_fire_4.value(), cat);
        MKParticles.spawnOffset(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(caster, CoreDamageTypes.FireDamage.get(),
                        damage.value(),
                        formulaParameters.value())
                .ability(this)
                .directEntity(projectile)
                .skillLevel(getSkillLevel(caster, skill))
                .amplify(amplifier);

        MKEffectBuilder<?> fireBreak = MKUEffects.BREAK_FIRE.get().builder(caster)
                .ability(this)
                .directEntity(projectile)
                .timed(Math.round((getSkillLevel(caster, skill) + 1) * GameConstants.TICKS_PER_SECOND))
                .skillLevel(getSkillLevel(caster, skill))
                .amplify(amplifier);

        AreaEffectBuilder.createOnEntity(caster, projectile)
                .effect(damageEffect, getTargetContext())
                .effect(fireBreak, getTargetContext())
                .instant()
                .color(16737330).radius(getExplosionRadius(), true)
                .disableParticle()
                .spawn();

        return true;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level());
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.fireballProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 5);
        return projectile;
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_fire_2.value();
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_fire.value();
    }

}
