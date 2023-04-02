package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import com.chaosbuffalo.mkcore.entities.LineEffectEntity;
import com.chaosbuffalo.mkcore.entities.PointEffectEntity;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class EntityEffectBuilder<T extends BaseEffectEntity> {

    protected final T effect;

    private EntityEffectBuilder(LivingEntity caster, Entity center, Vec3 offset) {
        this(caster, center.position().add(offset));
    }

    private EntityEffectBuilder(LivingEntity caster, Vec3 position) {
        effect = createEntity(caster.getCommandSenderWorld(), position);
        effect.setOwner(caster);
    }

    protected abstract T createEntity(Level world, Vec3 pos);

    public EntityEffectBuilder<T> duration(int duration) {
        effect.setDuration(duration);
        return this;
    }

    public EntityEffectBuilder<T> instant() {
        return duration(6).waitTime(0);
    }


    public EntityEffectBuilder<T> waitTime(int waitTime) {
        effect.setWaitTime(waitTime);
        return this;
    }

    public EntityEffectBuilder<T> tickRate(int tickRate) {
        effect.setTickRate(tickRate);
        return this;
    }

    public EntityEffectBuilder<T> setParticles(ResourceLocation animation) {
        effect.setParticles(animation);
        return this;
    }

    public EntityEffectBuilder<T> setWaitingParticles(ResourceLocation animation) {
        effect.setWaitingParticles(animation);
        return this;
    }

    public EntityEffectBuilder<T> setParticles(BaseEffectEntity.ParticleDisplay display) {
        effect.setParticles(display);
        return this;
    }

    public EntityEffectBuilder<T> setWaitingParticles(BaseEffectEntity.ParticleDisplay display) {
        effect.setWaitingParticles(display);
        return this;
    }

    public EntityEffectBuilder<T> delayedEffect(MobEffectInstance effect, TargetingContext targetContext, int delayTicks) {
        this.effect.addDelayedEffect(effect, targetContext, delayTicks);
        return this;
    }

    public EntityEffectBuilder<T> delayedEffect(MKEffectBuilder<?> effect, TargetingContext targetContext, int delayTicks) {
        this.effect.addDelayedEffect(effect, targetContext, delayTicks);
        return this;
    }

    public EntityEffectBuilder<T> effect(MobEffectInstance effect, TargetingContext targetContext) {
        this.effect.addEffect(effect, targetContext);
        return this;
    }

    public EntityEffectBuilder<T> effect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        this.effect.addEffect(effect, targetContext);
        return this;
    }


    public void spawn() {
        if (effect.getOwner() != null) {
            effect.getOwner().level.addFreshEntity(effect);
        }
    }

    public static class LineEffectBuilder extends EntityEffectBuilder<LineEffectEntity> {

        private LineEffectBuilder(LivingEntity caster, Entity center, Vec3 startPoint, Vec3 endPoint) {
            super(caster, center, Vec3.ZERO);
            effect.setStartPoint(startPoint);
            effect.setEndPoint(endPoint);
        }

        private LineEffectBuilder(LivingEntity caster, Vec3 startPoint, Vec3 endPoint) {
            super(caster, startPoint);
            effect.setStartPoint(startPoint);
            effect.setEndPoint(endPoint);
        }

        @Override
        protected LineEffectEntity createEntity(Level world, Vec3 pos) {
            return new LineEffectEntity(world, pos.x(), pos.y(), pos.z());
        }
    }

    public static LineEffectBuilder createLineEffectOnEntity(LivingEntity caster, Entity center, Vec3 start, Vec3 end) {
        return new LineEffectBuilder(caster, center, start, end);
    }

    public static LineEffectBuilder createLineEffect(LivingEntity caster, Vec3 start, Vec3 end) {
        return new LineEffectBuilder(caster, start, end);
    }

    public static class PointEffectBuilder extends EntityEffectBuilder<PointEffectEntity> {

        private PointEffectBuilder(LivingEntity caster, Entity center, Vec3 offset) {
            super(caster, center, offset);
        }

        private PointEffectBuilder(LivingEntity caster, Vec3 position) {
            super(caster, position);
        }

        @Override
        protected PointEffectEntity createEntity(Level world, Vec3 pos) {
            return new PointEffectEntity(world, pos.x(), pos.y(), pos.z());
        }

        public PointEffectBuilder radius(float radius) {
            effect.setRadius(radius);
            return this;
        }

    }

    public static PointEffectBuilder createPointEffectOnEntity(LivingEntity caster, Entity center, Vec3 offset) {
        return new PointEffectBuilder(caster, center, offset);
    }

    public static PointEffectBuilder createPointEffect(LivingEntity caster, Vec3 position) {
        return new PointEffectBuilder(caster, position);
    }
}
