package com.chaosbuffalo.mkultra.abilities.structure;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.MeleeUseCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.StringAttribute;
import com.chaosbuffalo.mknpc.abilities.StructureAbility;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class NecrotideGolemBeam extends StructureAbility {

    private static final ResourceLocation PULSE_PARTICLES = new ResourceLocation(MKUltra.MODID, "wrath_beam_pulse");
    private static final ResourceLocation WAIT_PARTICLES = new ResourceLocation(MKUltra.MODID, "wrath_beam_wait");

    protected final ResourceLocationAttribute pulse_particles = new ResourceLocationAttribute("pulse_particles", PULSE_PARTICLES);
    protected final ResourceLocationAttribute wait_particles = new ResourceLocationAttribute("wait_particles", WAIT_PARTICLES);
    protected final StringAttribute poi_name = new StringAttribute("poi_name", "golem_lantern");
    protected final IntAttribute tickRate = new IntAttribute("tick_rate", GameConstants.TICKS_PER_SECOND / 2);
    protected final IntAttribute duration = new IntAttribute("duration", GameConstants.TICKS_PER_SECOND * 2);
    protected final IntAttribute charge_time = new IntAttribute("charge_time", GameConstants.TICKS_PER_SECOND * 2);
    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    public NecrotideGolemBeam() {
        super();
        setCooldownSeconds(45);
        setManaCost(8);
        setCastTime(GameConstants.TICKS_PER_SECOND * 3);
        addAttributes(pulse_particles, wait_particles, poi_name, tickRate, duration, charge_time);
//        addAttributes(base, scale, modifierScaling, baseDuration, scaleDuration, cast_particles);
        setUseCondition(new MeleeUseCondition(this));
        addSkillAttribute(MKAttributes.EVOCATION);
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
       getStructure(castingEntity).ifPresent(entry -> entry.getPoisWithTag("golem_lantern").forEach(
               poi -> {
                   var builder = EntityEffectBuilder.createBlockAnchoredEffect(castingEntity, Vec3.atCenterOf(poi.getLocation().pos()));
                   builder.setBlock(Blocks.SOUL_LANTERN);
                   MKEffectBuilder<?> sound = SoundEffect.from(castingEntity, MKUSounds.spell_fire_7.get(), castingEntity.getSoundSource())
                           .ability(this);
                   castingEntity.getLevel().setBlockAndUpdate(poi.getLocation().pos(), Blocks.SOUL_LANTERN.defaultBlockState());
                   builder.setRange(10.0f)
                           .setTargetContext(TargetingContexts.ENEMY)
                           .setParticles(new BaseEffectEntity.ParticleDisplay(pulse_particles.getValue(),
                                   tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                           .setWaitingParticles(new BaseEffectEntity.ParticleDisplay(wait_particles.getValue(),
                                   tickRate.value(), BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS))
                           .duration(getCooldown(casterData))
                           .effect(sound, TargetingContexts.ENEMY)
                           .waitTime(charge_time.value())
                           .tickRate(tickRate.value());
                   builder.spawn();
               })
       );
    }
}
