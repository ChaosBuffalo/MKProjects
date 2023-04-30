package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.OnHitEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class FrozenGraspAbility extends MKAbility {
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 10);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 2);
    protected final IntAttribute maxStacks = new IntAttribute("maxStacks", 2);
    protected final IntAttribute selfDuration = new IntAttribute("selfDuration", 20);
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "frozen_grasp_cast");
    public static final ResourceLocation HIT_PARTICLES = new ResourceLocation(MKUltra.MODID, "frozen_grasp_hit");

    protected final ResourceLocationAttribute hitParticles = new ResourceLocationAttribute("hit_particles", HIT_PARTICLES);

    public FrozenGraspAbility() {
        super();
        setCooldownSeconds(30);
        setManaCost(4);
        addSkillAttribute(MKAttributes.NECROMANCY);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        casting_particles.setDefaultValue(CAST_PARTICLES);
        addAttributes(baseDuration, scaleDuration, selfDuration, hitParticles);
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
        return MKUSounds.casting_water.get();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_dark_4.get();
    }


    private final Vec3 YP = new Vec3(0.0, 1.0, 0.0);
    public MKEffectBuilder<?> onHitEffect(OnHitEffect.OnHitCallbackData args) {
        int dur = getBuffDuration(args.entityData, args.instance.getSkillLevel(),
                baseDuration.value(), scaleDuration.value());
        MKParticles.spawn(args.target, YP, hitParticles.getValue());
        return MKUEffects.FROZEN_GRASP.get().builder(args.entityData.getEntity())
                .skillLevel(args.instance.getSkillLevel()).timed(dur);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, Function<Attribute, Float> skillSupplier) {
        float level = skillSupplier.apply(MKAttributes.NECROMANCY);
        float dur = convertDurationToSeconds(getBuffDuration(entityData, level, baseDuration.value(), scaleDuration.value()));
        return Component.translatable(getDescriptionTranslationKey(), INTEGER_FORMATTER.format(maxStacks.value()), dur);
    }

    @Override
    public void buildDescription(IMKEntityData casterData, Consumer<Component> consumer) {
        super.buildDescription(casterData, consumer);
        AbilityDescriptions.getEffectModifiers(MKUEffects.FROZEN_GRASP.get(), casterData, false,
                attr -> MKAbility.getSkillLevel(casterData.getEntity(), attr)).forEach(consumer);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context,
                        Function<Attribute, Float> skillSupplier) {
        super.endCast(castingEntity, casterData, context, skillSupplier);
        float level = skillSupplier.apply(MKAttributes.NECROMANCY);
        casterData.getEffects().addEffect(FrozenGraspEffect.applierFrom(castingEntity,
                selfDuration.value() * GameConstants.TICKS_PER_SECOND, maxStacks.value()));
    }
}
