package com.chaosbuffalo.mkultra.abilities.nether_mage;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.entities.ConeAreaEffectEntity;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.SkullFlameBreathEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkullFlameBreathAbility extends MKAbility {
    private static final ResourceLocation FLAME_PARTICLES = MKUltra.id("fire_breath_cone");

    protected final FloatAttribute base = new FloatAttribute("base", 1.5f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 0.75f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 0.35f);
    protected final FloatAttribute range = new FloatAttribute("range", 5.0f);
    protected final FloatAttribute angle = new FloatAttribute("angle", 36.0f);
    protected final IntAttribute tickRate = new IntAttribute("tickRate", 4);
    protected final IntAttribute burnSeconds = new IntAttribute("burnSeconds", 2);
    protected final ResourceLocationAttribute breath_particles = new ResourceLocationAttribute("breath_particles", FLAME_PARTICLES);

    public SkullFlameBreathAbility() {
        super();
        setCooldownSeconds(10);
        setManaCost(5);
        setCastTime(GameConstants.TICKS_PER_SECOND * 6);
        addAttributes(base, scale, modifierScaling, range, angle, tickRate, burnSeconds, breath_particles);
        addSkillAttribute(MKAttributes.EVOCATION);
        setUseCondition(new MeleeUseCondition(this));
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.EVOCATION);
        Component damageStr = getDamageDescription(entityData, com.chaosbuffalo.mkcore.init.CoreDamageTypes.FireDamage.get(),
                base.value(), scale.value(), level, modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(),
                NUMBER_FORMATTER.format(convertDurationToSeconds(getBaseCastTime())),
                damageStr,
                NUMBER_FORMATTER.format(convertDurationToSeconds(tickRate.value())),
                INTEGER_FORMATTER.format(burnSeconds.value()));
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return range.value();
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PBAOE;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_fire.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }

    @Override
    public void startCast(IMKEntityData casterData, int castTime, AbilityContext context) {
        super.startCast(casterData, castTime, context);
        LivingEntity castingEntity = casterData.getEntity();

        float level = context.getSkill(MKAttributes.EVOCATION);
        MKEffectBuilder<?> damage = SkullFlameBreathEffect.from(castingEntity, base.value(), scale.value(),
                        modifierScaling.value(), burnSeconds.value())
                .ability(this)
                .skillLevel(level);

        Vec3 mouthOffset = new Vec3(0.0, castingEntity.getEyeHeight() * 0.75f, 0.0);
        EntityEffectBuilder.ConeEffectBuilder builder = EntityEffectBuilder.createConeEffect(castingEntity,
                castingEntity.position().add(mouthOffset));

        builder.range(range.value())
                .angleDegrees(angle.value())
                .useOwnerLook();

        builder.effect(damage, getTargetContext())
                .setParticles(new BaseEffectEntity.ParticleDisplay(breath_particles.getValue(), tickRate.value(),
                        BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                .infiniteDuration()
                .waitTime(0)
                .tickRate(tickRate.value())
                .tickSound(MKUSounds.spell_fire_1.value());
        ConeAreaEffectEntity activeEffect = builder.spawnEntity();
        casterData.getRiders().addRider(activeEffect, mouthOffset, true);
        List<BaseEffectEntity> currentEffects = new ArrayList<>(
                context.getMemory(MKAbilityMemories.CURRENT_AREA_EFFECTS).orElse(List.of()));
        currentEffects.add(activeEffect);
        context.setMemory(MKAbilityMemories.CURRENT_AREA_EFFECTS.get(), Optional.of(currentEffects));
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        removeActiveBreath(casterData, context);
    }

    @Override
    public void interruptCast(IMKEntityData casterData, CastInterruptReason reason, AbilityContext context) {
        super.interruptCast(casterData, reason, context);
        removeActiveBreath(casterData, context);
    }

    @Override
    public boolean maintainCastWithoutLineOfSight(IMKEntityData casterData) {
        return true;
    }

    @Override
    public boolean requiresLineOfSightToStart(IMKEntityData casterData, LivingEntity target) {
        return false;
    }

    @Override
    public boolean isInterruptedBy(IMKEntityData targetData, CastInterruptReason reason) {
        return reason != CastInterruptReason.Jump;
    }

    private void removeActiveBreath(IMKEntityData casterData, AbilityContext context) {
        context.getMemory(MKAbilityMemories.CURRENT_AREA_EFFECTS).ifPresent(effects -> {
            for (BaseEffectEntity effect : effects) {
                casterData.getRiders().removeRider(effect);
                if (effect.isAlive()) {
                    effect.remove(Entity.RemovalReason.KILLED);
                }
            }
        });
        context.setMemory(MKAbilityMemories.CURRENT_AREA_EFFECTS.get(), Optional.empty());
    }
}
