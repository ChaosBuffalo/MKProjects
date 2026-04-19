package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class SimpleAbilityActionContext implements AbilityActionContext {
    private final AbilityInvocation invocation;
    private final AbilityPowerResolver powerResolver;
    private final AbilityStateStore stateStore;
    private final AbilityResolvedTargets targets;
    private final Supplier<Optional<LivingEntity>> currentTargetSupplier;
    private final @Nullable AbilityReactionOwner reactionOwner;

    public SimpleAbilityActionContext(AbilityInvocation invocation,
                                      AbilityPowerResolver powerResolver) {
        this(invocation, powerResolver, NoopAbilityStateStore.INSTANCE, invocation.targets(), Optional::empty, null);
    }

    public SimpleAbilityActionContext(AbilityInvocation invocation,
                                      AbilityPowerResolver powerResolver,
                                      AbilityStateStore stateStore,
                                      AbilityResolvedTargets targets,
                                      Supplier<Optional<LivingEntity>> currentTargetSupplier,
                                      @Nullable AbilityReactionOwner reactionOwner) {
        this.invocation = Objects.requireNonNull(invocation, "invocation");
        this.powerResolver = Objects.requireNonNull(powerResolver, "powerResolver");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        this.targets = Objects.requireNonNull(targets, "targets");
        this.currentTargetSupplier = Objects.requireNonNull(currentTargetSupplier, "currentTargetSupplier");
        this.reactionOwner = reactionOwner;
    }

    @Override
    public AbilityInvocation invocation() {
        return invocation;
    }

    @Override
    public IMKEntityData ownerData() {
        return invocation.ownerData();
    }

    @Override
    public IMKEntityData casterData() {
        return invocation.casterData();
    }

    @Override
    public UUID sourceId() {
        return invocation.sourceId();
    }

    @Override
    public AbilityResolvedTargets targets() {
        return targets;
    }

    @Override
    public Optional<LivingEntity> currentTarget() {
        return currentTargetSupplier.get();
    }

    @Override
    public @Nullable AbilityEventSnapshot eventSnapshot() {
        return invocation.eventSnapshot();
    }

    @Override
    public Optional<AbilityReactionOwner> reactionOwner() {
        return Optional.ofNullable(reactionOwner);
    }

    @Override
    public AbilityValue getParam(String id) {
        return invocation.definition().resolveParameter(id, invocation.grantParameterOverrides());
    }

    @Override
    public AbilityValue getVar(String id) {
        AbilityValue value = invocation.getGraphVar(id);
        if (value == null) {
            throw new IllegalStateException("Ability graph var '%s' was not set on invocation %s"
                    .formatted(id, invocation.invocationId()));
        }
        return value;
    }

    @Override
    public void setVar(String id, AbilityValue value) {
        invocation.setGraphVar(id, value);
    }

    @Override
    public AbilityStatSnapshot stats(StatCapturePolicy policy) {
        if (policy == StatCapturePolicy.ON_INVOCATION) {
            return invocation.invocationStats();
        }
        return powerResolver.resolveStats(this, policy);
    }

    @Override
    public AbilityStateStore stateStore() {
        return stateStore;
    }
}
