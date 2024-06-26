package com.chaosbuffalo.mkultra.abilities.cleric;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.ClericHealEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class HealAbility extends MKAbility {
    protected final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "heal_casting");
    protected final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "heal_cast");
    protected final FloatAttribute base = new FloatAttribute("base", 5.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 5.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public HealAbility() {
        super();
        setCooldownSeconds(6);
        setManaCost(4);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(base, scale, modifierScaling, cast_particles);
        addSkillAttribute(MKAttributes.RESTORATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.RESTORATION);
        Component valueStr = getHealDescription(entityData, base.value(),
                scale.value(), level, modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), valueStr);
    }

    public FloatAttribute getModifierScaling() {
        return modifierScaling;
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
        return AbilityTargeting.SINGLE_TARGET_OR_SELF;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_holy.get();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_holy_5.get();
    }

    @Override
    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return super.isValidTarget(caster, target) || MKHealing.wouldHealHurtUndead(caster, target);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        float level = context.getSkill(MKAttributes.RESTORATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {

            MKEffectBuilder<?> heal = ClericHealEffect.from(castingEntity, base.value(), scale.value(), modifierScaling.value())
                    .ability(this)
                    .skillLevel(level);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> targetData.getEffects().addEffect(heal));

            SoundUtils.serverPlaySoundAtEntity(targetEntity, MKUSounds.spell_heal_3.get(), targetEntity.getSoundSource());
            MKParticles.spawn(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }
}
