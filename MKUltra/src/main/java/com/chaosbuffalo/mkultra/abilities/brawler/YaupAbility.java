package com.chaosbuffalo.mkultra.abilities.brawler;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.NeedsBuffCondition;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
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
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "yaup_cast");
    public static final ResourceLocation TICK_PARTICLES = new ResourceLocation(MKUltra.MODID, "yaup_tick");
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("tick_particles", TICK_PARTICLES);
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 15);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 5);


    public YaupAbility() {
        super();
        setCooldownSeconds(45);
        setManaCost(2);
        addAttributes(baseDuration, scaleDuration, tick_particles, cast_particles);
        addSkillAttribute(MKAttributes.ARETE);
        setUseCondition(new NeedsBuffCondition(MKUEffects.YAUP));
    }

    @Override
    protected Component getSkillDescription(IMKEntityData casterData, MKAbilityInfo abilityInfo) {
        float level = abilityInfo.getSkillValue(casterData, MKAttributes.ARETE);
        int duration = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value()) / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), INTEGER_FORMATTER.format(duration));
    }

    @Override
    public void buildDescription(IMKEntityData casterData, MKAbilityInfo abilityInfo, Consumer<Component> consumer) {
        super.buildDescription(casterData, abilityInfo, consumer);
        AbilityDescriptions.getEffectModifiers(MKUEffects.YAUP.get(), casterData, false,
                abilityInfo).forEach(consumer);
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
        return MKUSounds.spell_holy_2.get();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData casterData, AbilityContext context, MKAbilityInfo abilityInfo) {
        super.endCast(entity, casterData, context, abilityInfo);
        float level = abilityInfo.getSkillValue(casterData, MKAttributes.ARETE);
        MKEffectBuilder<?> yaup = YaupEffect.from(entity, level, getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value()));
        MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_buff_attack_4.get(), entity.getSoundSource())
                .ability(abilityInfo);
        MKEffectBuilder<?> particles = MKParticleEffect.from(entity, tick_particles.getValue(),
                        true, new Vec3(0.0, 1.0, 0.0))
                .ability(abilityInfo);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(yaup, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(particles, getTargetContext())
                .instant()
                .color(1048370)
                .radius(getDistance(entity, abilityInfo), true)
                .disableParticle()
                .spawn();

        MKParticles.spawn(entity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
    }
}