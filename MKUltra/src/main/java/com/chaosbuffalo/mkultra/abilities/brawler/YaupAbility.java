package com.chaosbuffalo.mkultra.abilities.brawler;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.YaupEffect;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class YaupAbility extends MKAbility {
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("yaup.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("yaup.duration.per_level"));
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("yaup_cast");
    public static final ResourceLocation TICK_PARTICLES = MKUltra.id("yaup_tick");
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("tick_particles", TICK_PARTICLES);
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DURATION_BASE_PARAMETER, 15.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 5.0f)
                    .build());
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));


    public YaupAbility() {
        super();
        setCooldownSeconds(45);
        setManaCost(2);
        addAttributes(formulaParameters, durationFormula, tick_particles, cast_particles);
        addSkillAttribute(MKAttributes.ARETE);
        setUseCondition(new NeedsBuffCondition(this, MKUEffects.YAUP));
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ARETE);
        int duration = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level)
                / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), INTEGER_FORMATTER.format(duration));
    }

    @Override
    public void buildDescription(IMKEntityData casterData, AbilityContext context, Consumer<Component> consumer) {
        super.buildDescription(casterData, context, consumer);
        AbilityDescriptions.getEffectModifiers(MKUEffects.YAUP.get(), context, false, consumer);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_holy_2.value();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.ARETE);
        MKEffectBuilder<?> yaup = YaupEffect.from(entity, level,
                getBuffDuration(data, durationFormula.value(), formulaParameters.value(), level));
        MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_buff_attack_4.value(), entity.getSoundSource())
                .ability(this);
        MKEffectBuilder<?> particles = MKParticleEffect.from(entity, tick_particles.getValue(),
                        true, new Vec3(0.0, 1.0, 0.0))
                .ability(this);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(yaup, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(particles, getTargetContext())
                .instant()
                .color(1048370)
                .radius(getDistance(entity), true)
                .disableParticle()
                .spawn();

        MKParticles.spawnOffset(entity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
    }
}
