package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.OnHitEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.FrozenGraspEffect;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FrozenGraspAbility extends MKAbility {
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("frozen_grasp.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("frozen_grasp.duration.per_level"));
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DURATION_BASE_PARAMETER, 10.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 2.0f)
                    .build());
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final IntAttribute maxStacks = new IntAttribute("maxStacks", 2);
    protected final IntAttribute selfDuration = new IntAttribute("selfDuration", 20);
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("frozen_grasp_cast");
    public static final ResourceLocation HIT_PARTICLES = MKUltra.id("frozen_grasp_hit");

    protected final ResourceLocationAttribute hitParticles = new ResourceLocationAttribute("hit_particles", HIT_PARTICLES);

    public FrozenGraspAbility() {
        super();
        setCooldownSeconds(30);
        setManaCost(4);
        addSkillAttribute(MKAttributes.NECROMANCY);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        castingParticles.setDefaultValue(CAST_PARTICLES);
        addAttributes(formulaParameters, durationFormula, selfDuration, hitParticles);
        setUseCondition(new NeedsBuffCondition(this, MKUEffects.FROZEN_GRASP_APPLIER).setCombatOnly(true));
    }


    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SELF;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_water.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_4.value();
    }


    private final Vec3 YP = new Vec3(0.0, 1.0, 0.0);

    public MKEffectBuilder<?> onHitEffect(OnHitEffect.OnHitCallbackData args) {
        int dur = getBuffDuration(args.entityData, durationFormula.value(), formulaParameters.value(),
                args.instance.getSkillLevel());
        MKParticles.spawnOffset(args.target, YP, hitParticles.getValue());
        return MKUEffects.FROZEN_GRASP.get().builder(args.entityData.getEntity())
                .skillLevel(args.instance.getSkillLevel()).timed(dur);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.NECROMANCY);
        float dur = convertDurationToSeconds(getBuffDuration(entityData, durationFormula.value(),
                formulaParameters.value(), level));
        return Component.translatable(getDescriptionTranslationKey(), INTEGER_FORMATTER.format(maxStacks.value()), dur);
    }

    @Override
    public void buildDescription(IMKEntityData casterData, AbilityContext context, Consumer<Component> consumer) {
        super.buildDescription(casterData, context, consumer);
        AbilityDescriptions.getEffectModifiers(MKUEffects.FROZEN_GRASP.get(), context, false, consumer);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        casterData.getEffects().addEffect(FrozenGraspEffect.applierFrom(castingEntity,
                selfDuration.value() * GameConstants.TICKS_PER_SECOND, maxStacks.value()));
    }
}
