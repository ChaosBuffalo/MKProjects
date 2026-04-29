package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.PositionTargetingAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SeafuryAbility extends PositionTargetingAbility {
    private static final FormulaParameterKey BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("seafury.damage.base"));
    private static final FormulaParameterKey PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("seafury.damage.per_level"));
    private static final FormulaParameterKey MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("seafury.damage.modifier_scaling"));
    private static final ResourceLocation WAIT_PARTICLES = MKUltra.id("seafury_wait");
    private static final ResourceLocation PULSE_PARTICLES = MKUltra.id("seafury_pulse");
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("seafury_casting");

    protected final ResourceLocationAttribute pulse_particles = new ResourceLocationAttribute("pulse_particles", PULSE_PARTICLES);
    protected final ResourceLocationAttribute wait_particles = new ResourceLocationAttribute("wait_particles", WAIT_PARTICLES);
    protected final DoubleAttribute step = new DoubleAttribute("step", 1.5);
    protected final IntAttribute iterations = new IntAttribute("iterations", 3);
    protected final IntAttribute wait_time = new IntAttribute("wait_time", 3 * GameConstants.TICKS_PER_SECOND / 5);
    protected final IntAttribute step_delay = new IntAttribute("step_delay", GameConstants.TICKS_PER_SECOND);
    protected final FloatAttribute radius = new FloatAttribute("radius", 1.0f);
    protected final IntAttribute duration = new IntAttribute("duration", GameConstants.TICKS_PER_SECOND);
    protected final IntAttribute tickRate = new IntAttribute("tickRate", GameConstants.TICKS_PER_SECOND / 2);
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(BASE_PARAMETER, 2.0f)
                    .with(PER_LEVEL_PARAMETER, 1.0f)
                    .with(MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final FormulaAttribute damageFormula = new FormulaAttribute("damageFormula",
            AbilityFormula.bonusScaledLinear(BASE_PARAMETER, PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, MODIFIER_SCALING_PARAMETER));

    public SeafuryAbility() {
        super();
        setCastTime(GameConstants.TICKS_PER_SECOND * 2);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        setCooldownSeconds(15);
        setManaCost(6);
        addAttributes(pulse_particles, wait_particles, wait_time, step, step_delay, iterations, radius,
                formulaParameters, damageFormula);
        addSkillAttribute(MKAttributes.EVOCATION);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.NatureDamage.get(),
                damageFormula.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(),
                NUMBER_FORMATTER.format(iterations.value()),
                NUMBER_FORMATTER.format(step.value()),
                damageStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                NUMBER_FORMATTER.format(radius.value())
        );
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public void castAtPosition(IMKEntityData casterData, Vec3 position, AbilityContext context) {
        LivingEntity castingEntity = casterData.getEntity();
        Vec3 dir = position.subtract(castingEntity.position()).normalize();
        float level = context.getSkill(MKAttributes.EVOCATION);
        for (int i = 0; i < iterations.value(); i++) {
            int delay = step_delay.value() * i;
            Vec3 pos = position.add(dir.scale(step.value() * i));

            EntityEffectBuilder.PointEffectBuilder builder = EntityEffectBuilder.createPointEffect(castingEntity, pos);

            MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(castingEntity, CoreDamageTypes.NatureDamage.get(),
                            damageFormula.value(), formulaParameters.value())
                    .ability(this)
                    .skillLevel(level);


            builder.radius(radius.value())
                    .effect(damage, getTargetContext())
                    .setParticles(new BaseEffectEntity.ParticleDisplay(pulse_particles.getValue(), tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.ONCE))
                    .setWaitingParticles(new BaseEffectEntity.ParticleDisplay(wait_particles.getValue(), GameConstants.TICKS_PER_SECOND / 5, BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                    .setPreDelay(delay)
                    .instant()
                    .waitTime(wait_time.value())
                    .tickSound(MKUSounds.spell_water_1.value())
                    .tickRate(tickRate.value());
            SoundUtils.serverPlaySoundFromEntity(position.x(), position.y(), position.z(),
                    MKUSounds.spell_water_2.value(),
                    castingEntity.getSoundSource(), 1.0f, 1.0f, castingEntity);
            builder.spawn();

        }


    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_shadow.value();
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }
}
