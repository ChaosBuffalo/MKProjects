package com.chaosbuffalo.mkultra.abilities.nether_mage;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Function;

public class FireArmorAbility extends MKAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "fire_armor_casting");
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "fire_armor_cast");
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 60);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 15);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public FireArmorAbility() {
        super();
        setCooldownSeconds(150);
        setManaCost(12);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addSkillAttribute(MKAttributes.ABJURATION);
        addAttributes(baseDuration, scaleDuration, cast_particles);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 10.0f;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ABJURATION);
        float amount = MKUEffects.FIRE_ARMOR.get().getPerLevel() * (level + 1) * 100.0f;
        int duration = getBuffDuration(entityData, level, baseDuration.value(), scaleDuration.value()) / GameConstants.TICKS_PER_SECOND;
        return Component.translatable(getDescriptionTranslationKey(), amount,
                CoreDamageTypes.FireDamage.get().getFormattedDisplayName(), duration);
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_buff_5.get();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.ABJURATION);
        int duration = getBuffDuration(data, level, baseDuration.value(), scaleDuration.value());

        int oldAmp = Math.round(level);
        MobEffectInstance fireResist = new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, oldAmp, false, false, true);
        MobEffectInstance absorb = new MobEffectInstance(MobEffects.ABSORPTION, duration, oldAmp, false, false, true);

        MKEffectBuilder<?> particles = MKParticleEffect.from(entity, cast_particles.getValue(), true, new Vec3(0.0, 1.0, 0.0))
                .ability(this);

        MKEffectBuilder<?> sound = SoundEffect.from(entity, MKUSounds.spell_fire_2.get(), entity.getSoundSource())
                .ability(this);

        MKEffectBuilder<?> fireArmor = MKUEffects.FIRE_ARMOR.get().builder(entity)
                .ability(this)
                .timed(duration)
                .skillLevel(level);

        AreaEffectBuilder.createOnCaster(entity)
                .effect(fireResist, getTargetContext())
                .effect(absorb, getTargetContext())
                .effect(sound, getTargetContext())
                .effect(fireArmor, getTargetContext())
                .effect(particles, getTargetContext())
                .disableParticle()
                .instant()
                .color(16762905)
                .radius(getDistance(entity), true)
                .spawn();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.FRIENDLY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }
}
