package com.chaosbuffalo.mkultra.abilities.nether_mage;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.MKUAbilityUtils;
import com.chaosbuffalo.mkultra.effects.IgniteEffect;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class IgniteAbility extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ignite.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ignite.damage.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("ignite.damage.modifier_scaling"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("ignite_casting");
    public static final ResourceLocation CAST_1_PARTICLES = MKUltra.id("ignite_cast_1");
    public static final ResourceLocation CAST_2_PARTICLES = MKUltra.id("ignite_cast_2");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 8.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 1.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));
    protected final FloatAttribute igniteDistance = new FloatAttribute("igniteDistance", 5.0f);
    protected final ResourceLocationAttribute cast_1_particles = new ResourceLocationAttribute("cast_1_particles", CAST_1_PARTICLES);
    protected final ResourceLocationAttribute cast_2_particles = new ResourceLocationAttribute("cast_2_particles", CAST_2_PARTICLES);

    public IgniteAbility() {
        super();
        setCooldownSeconds(12);
        setManaCost(6);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(formulaParameters, damage, cast_1_particles, cast_2_particles);
        addSkillAttribute(MKAttributes.EVOCATION);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component valueStr = getDamageDescription(entityData,
                CoreDamageTypes.FireDamage.get(), damage.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr, igniteDistance.value());
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 15.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_fire.value();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_13.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.EVOCATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(entity, CoreDamageTypes.FireDamage.get(),
                            damage.value(), formulaParameters.value())
                    .ability(this)
                    .skillLevel(level);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(damageEffect);

                SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_fire_4.value(), targetEntity.getSoundSource());

                if (MKUAbilityUtils.isBurning(targetData)) {
                    MKEffectBuilder<?> ignite = IgniteEffect.from(entity, damage.value(), formulaParameters.value())
                            .ability(this)
                            .skillLevel(level);
                    MKEffectBuilder<?> particle = MKParticleEffect.from(entity, cast_2_particles.getValue(),
                                    true, new Vec3(0.0, 1.0, 0.0))
                            .ability(this);
                    MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_fire_8.value(), entity.getSoundSource())
                            .ability(this);

                    AreaEffectBuilder.createOnEntity(entity, targetEntity)
                            .effect(particle, getTargetContext())
                            .effect(ignite, getTargetContext())
                            .effect(sound, getTargetContext())
                            .instant()
                            .color(16737305)
                            .radius(igniteDistance.value(), true)
                            .disableParticle()
                            .spawn();

                } else {
                    MKEffectBuilder<?> burn = MKUAbilities.EMBER.get().getBurnCast(data, level)
                            .ability(this);
                    targetData.getEffects().addEffect(burn);

                    MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_1_particles.getValue());
                }
            });
        });
    }
}
