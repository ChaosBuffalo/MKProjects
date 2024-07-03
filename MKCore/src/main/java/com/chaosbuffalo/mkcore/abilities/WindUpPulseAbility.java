package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.serialization.attributes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public abstract class WindUpPulseAbility extends PositionTargetingAbility {
    private static final ResourceLocation PULSE_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "test_pulse_detonate");
    private static final ResourceLocation WAIT_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "test_pulse_wait");
    protected final ResourceLocationAttribute pulseParticles = new ResourceLocationAttribute("pulse_particles", PULSE_PARTICLES);
    protected final ResourceLocationAttribute waitParticles = new ResourceLocationAttribute("wait_particles", WAIT_PARTICLES);
    protected final IntAttribute tickRate = new IntAttribute("tick_rate", GameConstants.TICKS_PER_SECOND / 5);
    protected final IntAttribute duration = new IntAttribute("duration", GameConstants.TICKS_PER_SECOND);
    protected final IntAttribute waitTime = new IntAttribute("wait_time", 3 * GameConstants.TICKS_PER_SECOND / 5);
    protected final IntAttribute waitTickRate = new IntAttribute("wait_tick_rate", GameConstants.TICKS_PER_SECOND / 5);
    protected final FloatAttribute radius = new FloatAttribute("radius", 1.5f);
    protected final Vector3dAttribute offset = new Vector3dAttribute("offset", new Vec3(0.0, 0.5, 0.0));
    protected final BooleanAttribute setEndpointOnWait = new BooleanAttribute("set_endpoint", false);

    public WindUpPulseAbility() {
        super();
        addAttributes(pulseParticles, waitParticles, tickRate, duration, waitTime, radius, offset, waitTickRate);
    }

    public abstract void setupEntityEffect(EntityEffectBuilder.PointEffectBuilder builder, IMKEntityData casterData, Vec3 position, AbilityContext context);

    @Override
    public void castAtPosition(IMKEntityData casterData, Vec3 position, AbilityContext context) {
        LivingEntity castingEntity = casterData.getEntity();
        Vec3 pulsePos = position.add(offset.getValue());

        EntityEffectBuilder.PointEffectBuilder builder = EntityEffectBuilder.createPointEffect(castingEntity, pulsePos);

        builder.radius(radius.value())
                .setParticles(new BaseEffectEntity.ParticleDisplay(pulseParticles.getValue(),
                        tickRate.value(),
                        BaseEffectEntity.ParticleDisplay.DisplayType.ONCE))
                .setWaitingParticles(new BaseEffectEntity.ParticleDisplay(waitParticles.getValue(),
                        waitTickRate.value(),
                        BaseEffectEntity.ParticleDisplay.DisplayType.CONTINUOUS,
                        setEndpointOnWait.value(),
                        getEndpointOffset(pulsePos)))
                .duration(duration.value())
                .waitTime(waitTime.value())
                .tickRate(tickRate.value());
        setupEntityEffect(builder, casterData, pulsePos, context);
        builder.spawn();
    }

    public Vec3 getEndpointOffset(Vec3 pulsePos) {
        return Vec3.ZERO;
    }
}
