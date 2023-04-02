package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MKEffectBuilder<T extends MKEffectState> {

    private final MKEffect effect;
    private final UUID sourceId;
    private final MKEffectBehaviour behaviour;
    private final Supplier<T> stateFactory;
    private int baseStackCount;
    private float skillLevel;
    @Nullable
    private LivingEntity sourceEntity;
    @Nullable
    private Entity directEntity;
    private Consumer<T> configureState;
    private ResourceLocation abilityId = MKCoreRegistry.INVALID_ABILITY;

    public MKEffectBuilder(MKEffect effect, UUID sourceId, Supplier<T> stateFactory) {
        this.effect = Objects.requireNonNull(effect);
        this.sourceId = Objects.requireNonNull(sourceId);
        baseStackCount = 1;
        behaviour = new MKEffectBehaviour();
        this.stateFactory = stateFactory;
    }

    public MKEffectBuilder(MKEffect effect, LivingEntity sourceEntity, Supplier<T> stateFactory) {
        this(effect, sourceEntity.getUUID(), stateFactory);
        this.sourceEntity = sourceEntity;
    }

    public MKEffect getEffect() {
        return effect;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public MKEffectBehaviour getBehaviour() {
        return behaviour;
    }

    public int getInitialStackCount() {
        return baseStackCount;
    }

    public float getSkillLevel() {
        return skillLevel;
    }

    public MKEffectBuilder<T> ability(MKAbility ability) {
        if (ability != null) {
            this.abilityId = ability.getAbilityId();
        }
        return this;
    }

    public MKEffectBuilder<T> ability(ResourceLocation abilityId) {
        this.abilityId = abilityId;
        return this;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    public MKEffectBuilder<T> amplify(int level) {
        baseStackCount += level;
        return this;
    }

    public MKEffectBuilder<T> skillLevel(float skillLevel) {
        this.skillLevel = skillLevel;
        return this;
    }

    public MKEffectBuilder<T> timed(int duration) {
        behaviour.setDuration(duration);
        return this;
    }

    public MKEffectBuilder<T> periodic(int period) {
        behaviour.setPeriod(period);
        return this;
    }

    public MKEffectBuilder<T> infinite() {
        behaviour.setInfinite(true);
        return this;
    }

    public MKEffectBuilder<T> instant() {
        behaviour.setDuration(0);
        behaviour.setInfinite(false);
        return this;
    }

    public MKEffectBuilder<T> temporary() {
        behaviour.setTemporary();
        return this;
    }

    public MKEffectBuilder<T> sourceEntity(LivingEntity entity) {
        sourceEntity = entity;
        return this;
    }

    public MKEffectBuilder<T> directEntity(Entity entity) {
        directEntity = entity;
        return this;
    }

    @Nullable
    public Entity getDirectEntity() {
        return directEntity;
    }

    @Nullable
    public LivingEntity getSourceEntity() {
        return sourceEntity;
    }

    public MKEffectBuilder<T> state(Consumer<T> configure) {
        this.configureState = configure;
        return this;
    }

    public MKActiveEffect createApplication() {
        T state = stateFactory.get();
        if (configureState != null) {
            configureState.accept(state);
        }
        return new MKActiveEffect(this, state);
    }

    @Override
    public String toString() {
        return "MKEffectBuilder{" +
                "effect=" + effect +
                ", sourceId=" + sourceId +
                ", baseStackCount=" + baseStackCount +
                '}';
    }
}
