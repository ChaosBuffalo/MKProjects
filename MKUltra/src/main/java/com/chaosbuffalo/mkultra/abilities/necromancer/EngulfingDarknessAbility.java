package com.chaosbuffalo.mkultra.abilities.necromancer;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.EntityTargetingAbility;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.EngulfingDarknessEffect;
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

public class EngulfingDarknessAbility extends EntityTargetingAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "shadow_bolt_casting");
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "engulfing_darkness_cast");
    public static final ResourceLocation TICK_PARTICLES = new ResourceLocation(MKUltra.MODID, "engulfing_darkness_tick");
    protected final FloatAttribute baseDot = new FloatAttribute("base_dot_damage", 2.0f);
    protected final FloatAttribute scaleDot = new FloatAttribute("scale_dot_damage", 2.0f);
    protected final IntAttribute baseDuration = new IntAttribute("base_duration", 10);
    protected final IntAttribute scaleDuration = new IntAttribute("scale_duration", 1);
    protected final FloatAttribute dotModifierScaling = new FloatAttribute("dot_modifier_scaling", 0.2f);
    protected final ResourceLocationAttribute castParticles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute dotCastParticles = new ResourceLocationAttribute("dot_cast_particles", TICK_PARTICLES);
    protected final IntAttribute shadowbringerDuration = new IntAttribute("shadowbringer_duration", GameConstants.TICKS_PER_SECOND * 10);
    protected final FloatAttribute shadowbringerChance = new FloatAttribute("shadowbringer_chance", 0.02f);


    public EngulfingDarknessAbility() {
        super();
        setCooldownSeconds(4);
        setManaCost(5);
        setCastTime((GameConstants.TICKS_PER_SECOND * 3) / 2);
        addAttributes(baseDuration, scaleDuration, baseDot, scaleDot, dotModifierScaling,
                castParticles, dotCastParticles, shadowbringerChance, shadowbringerDuration);
        addSkillAttribute(MKAttributes.CONJURATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    protected float getShadowbringerChance(IMKEntityData entityData) {
        float skill = getSkillLevel(entityData.getEntity(), MKAttributes.CONJURATION);
        return shadowbringerChance.value() + shadowbringerChance.value() * skill;

    }

    @Override
    public void buildDescription(IMKEntityData casterData, AbilityContext context, Consumer<Component> consumer) {
        super.buildDescription(casterData, context, consumer);
        AbilityDescriptions.getEffectModifiers(MKUEffects.ENGULFING_DARKNESS.get(), context, false, consumer);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        Component dotStr = getDamageDescription(entityData,
                CoreDamageTypes.ShadowDamage.get(), baseDot.value(), scaleDot.value(), level, dotModifierScaling.value());
        float dotDur = convertDurationToSeconds(getBuffDuration(entityData, level, baseDuration.value(), scaleDuration.value()));
        float shadowbringerDur = convertDurationToSeconds(shadowbringerDuration.value());
        return Component.translatable(getDescriptionTranslationKey(),
                dotStr, NUMBER_FORMATTER.format(convertDurationToSeconds(EngulfingDarknessEffect.DEFAULT_PERIOD)),
                NUMBER_FORMATTER.format(dotDur), PERCENT_FORMATTER.format(getShadowbringerChance(entityData)), NUMBER_FORMATTER.format(shadowbringerDur));
    }

    public MKEffectBuilder<?> getDotCast(IMKEntityData casterData, float level) {
        int dur = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value());
        return EngulfingDarknessEffect.from(casterData.getEntity(), baseDot.value(), scaleDot.value(),
                        dotModifierScaling.value(), getShadowbringerChance(casterData), shadowbringerDuration.value(),
                        dotCastParticles.getValue())
                .ability(this)
                .skillLevel(level)
                .timed(dur);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 25.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_shadow.get();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_9.get();
    }

    @Override
    public void castAtEntity(IMKEntityData casterData, LivingEntity target, AbilityContext context) {
        float level = context.getSkill(MKAttributes.CONJURATION);
        MKEffectBuilder<?> dot = getDotCast(casterData, level)
                .ability(this)
                .skillLevel(level);

        MKCore.getEntityData(target).ifPresent(targetData -> {
            targetData.getEffects().addEffect(dot);
        });
        SoundUtils.serverPlaySoundAtEntity(target, MKUSounds.spell_dark_7.get(), target.getSoundSource());
        MKParticles.spawn(target, new Vec3(0.0, 1.0, 0.0), castParticles.getValue());
    }
}
