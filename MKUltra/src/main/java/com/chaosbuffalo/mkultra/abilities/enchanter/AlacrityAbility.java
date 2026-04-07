package com.chaosbuffalo.mkultra.abilities.enchanter;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
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

public class AlacrityAbility extends MKAbility {
    public static final ResourceLocation CAST_PARTICLES = MKUltra.id("inspire_cast");

    protected final FloatAttribute baseValue = new FloatAttribute("baseValue", 0.25f);
    protected final FloatAttribute scaleValue = new FloatAttribute("scaleValue", 0.03f);
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 600);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 60);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);

    public AlacrityAbility() {
        super();
        setCooldownSeconds(30);
        setManaCost(10);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(baseValue, scaleValue, baseDuration, scaleDuration, cast_particles);
        addSkillAttribute(MKAttributes.ENCHANTMENT);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.ENCHANTMENT);
        int duration = getBuffDuration(entityData, level, baseDuration.value(), scaleDuration.value()) / GameConstants.TICKS_PER_SECOND;
        float value = baseValue.value() + scaleValue.value() * level;
        return Component.translatable(getDescriptionTranslationKey(), PERCENT_FORMATTER.format(value), duration);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_cast_12.value();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            float level = context.getSkill(MKAttributes.ENCHANTMENT);
            int duration = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value());
            MKEffectBuilder<?> effect = MKUEffects.ATTACK_SPEED_HASTE.get().builder(castingEntity)
                    .ability(this)
                    .skillLevel(level)
                    .timed(duration);
            MKCore.getEntityData(targetEntity).ifPresent(targetData -> targetData.getEffects().addEffect(effect));
            SoundUtils.serverPlaySoundAtEntity(targetEntity, getSpellCompleteSoundEvent(), targetEntity.getSoundSource());
            MKParticles.spawnOffset(targetEntity, new Vec3(0.0, 1.0, 0.0), cast_particles.getValue());
        });
    }
}
