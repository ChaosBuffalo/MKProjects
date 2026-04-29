package com.chaosbuffalo.mkultra.abilities.druid;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.OnHitEffect;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
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

public class FlameBlade extends MKAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("flame_blade.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("flame_blade.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("flame_blade.damage.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("flame_blade.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("flame_blade.duration.per_level"));
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("flame_blade_casting");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("flame_blade_cast");
    public static final ResourceLocation EDGE_PARTICLES = MKUltra.id("flame_blade_particles");

    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 1.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 1.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(DURATION_BASE_PARAMETER, 10.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 5.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final ResourceLocationAttribute castParticles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute edgeParticles = new ResourceLocationAttribute("edge_particles", EDGE_PARTICLES);

    public FlameBlade() {
        super();
        setCooldownSeconds(18);
        setManaCost(6);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addSkillAttribute(MKAttributes.ENCHANTMENT);
        addAttributes(formulaParameters, damage, durationFormula, castParticles, edgeParticles);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
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

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_fire.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_fire_3.value();
    }

    public MKEffectBuilder<?> onHitEffect(OnHitEffect.OnHitCallbackData args) {
        return MKAbilityDamageEffect.from(args.entityData.getEntity(), CoreDamageTypes.FireDamage.get(),
                        damage.value(), formulaParameters.value())
                .skillLevel(args.instance.getSkillLevel())
                .ability(this);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ENCHANTMENT);
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.FireDamage.get(),
                damage.value(), formulaParameters.value(), level);
        float duration = convertDurationToSeconds(
                getBuffDuration(entityData, durationFormula.value(), formulaParameters.value(), level));
        return Component.translatable(getDescriptionTranslationKey(), duration, damageStr);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(entity, casterData, context);
        float level = context.getSkill(MKAttributes.ENCHANTMENT);
        int duration = getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), level);

        MKEffectBuilder<?> flameBlade = MKUEffects.FLAME_BLADE_APPLIER.get().builder(entity)
                .ability(this)
                .skillLevel(level)
                .timed(duration);
        MKEffectBuilder<?> particles = MKParticleEffect.from(entity, castParticles.getValue(), true, new Vec3(0.0, 1.0, 0.0))
                .ability(this);
        MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_fire_7.value(), entity.getSoundSource())
                .ability(this);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(flameBlade, getTargetContext())
                .effect(particles, getTargetContext())
                .effect(sound, getTargetContext())
                .disableParticle()
                .instant()
                .color(16737330)
                .radius(getDistance(entity), true)
                .spawn();
    }
}
