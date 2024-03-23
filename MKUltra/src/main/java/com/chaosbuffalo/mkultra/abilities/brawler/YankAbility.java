package com.chaosbuffalo.mkultra.abilities.brawler;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeSpellInterruptUseCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.YankEffect;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class YankAbility extends MKAbility {
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "yank_cast");
    protected final FloatAttribute base = new FloatAttribute("base", 1.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 0.75f);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);


    public YankAbility() {
        super();
        setCooldownSeconds(5);
        setManaCost(4);
        addAttributes(base, scale, cast_particles);
        addSkillAttribute(MKAttributes.PANKRATION);
        setUseCondition(new MeleeSpellInterruptUseCondition(this));
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.PANKRATION);
        String value = NUMBER_FORMATTER.format(base.value() + scale.value() * level);
        return Component.translatable(getDescriptionTranslationKey(), value);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return getMeleeReach(entity) * 1.5f;
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
        return MKUSounds.spell_grab_2.get();
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.PANKRATION);
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity -> {
            Vec3 yankPos = new Vec3(entity.getX(), entity.getY(0.6), entity.getZ());
            MKEffectBuilder<?> yank = YankEffect.from(entity, base.value(), scale.value(), yankPos)
                    .ability(this)
                    .skillLevel(level);

            MKCore.getEntityData(targetEntity).ifPresent(targetData -> {
                targetData.getEffects().addEffect(yank);
            });

            Vec3 targetPos = new Vec3(targetEntity.getX(), targetEntity.getY(0.6f), targetEntity.getZ());
            MKParticles.spawn(entity, yankPos, CAST_PARTICLES, p -> p.addLoc(targetPos));
        });
    }
}