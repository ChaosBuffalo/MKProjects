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
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
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
    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("fire_armor_casting");
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("fire_armor_cast");
    public static final ResourceLocation EDGE_PARTICLES = MKUltra.id("flame_blade_particles");

    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 10);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 5);
    protected final FloatAttribute base = new FloatAttribute("base", 1.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 1.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute castParticles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute edgeParticles = new ResourceLocationAttribute("edge_particles", EDGE_PARTICLES);

    public FlameBlade() {
        super();
        setCooldownSeconds(18);
        setManaCost(6);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addSkillAttribute(MKAttributes.ENCHANTMENT);
        addAttributes(baseDuration, scaleDuration, base, scale, modifierScaling, castParticles, edgeParticles);
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
                        base.value(), scale.value(), modifierScaling.value())
                .skillLevel(args.instance.getSkillLevel())
                .ability(this);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ENCHANTMENT);
        Component damage = getDamageDescription(entityData, CoreDamageTypes.FireDamage.get(),
                base.value(), scale.value(), level, modifierScaling.value());
        float duration = convertDurationToSeconds(getBuffDuration(entityData, level, baseDuration.value(), scaleDuration.value()));
        return Component.translatable(getDescriptionTranslationKey(), damage, duration);
    }

    @Override
    public void buildDescription(IMKEntityData casterData, AbilityContext context, Consumer<Component> consumer) {
        super.buildDescription(casterData, context, consumer);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(entity, casterData, context);
        float level = context.getSkill(MKAttributes.ENCHANTMENT);
        int duration = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value());

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
