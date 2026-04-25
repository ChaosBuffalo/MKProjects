package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities2.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityConditionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleAbilityEngine implements AbilityEngine {
    @FunctionalInterface
    public interface AbilityEventEmitter {
        void emit(AbilityEventSnapshot event);
    }

    public interface ReactionController {
        ReactionController NOOP = new ReactionController() {
        };

        default void install(AbilityReactionOwner owner,
                             UUID ownerEntityId,
                             UUID casterEntityId,
                             String reactionId,
                             AbilityReactionDefinition definition) {
        }

        default void remove(AbilityReactionOwner owner, String reactionId) {
        }

        default void clearOwner(AbilityReactionOwner owner) {
        }
    }

    public interface DeliveryController {
        DeliveryController NOOP = new DeliveryController() {
        };

        default AbilityReactionOwner registerProjectile(AbilityInvocation invocation,
                                                       String deliveryId,
                                                       AbilityDeliveryDefinition definition,
                                                       AbilityProjectileEntity projectile) {
            return new AbilityReactionOwner(ReactionOwnerType.DELIVERY, projectile.getUUID(), projectile.getUUID(),
                    invocation.abilityId());
        }

        default AbilityReactionOwner registerRuntime(AbilityInvocation invocation,
                                                     String deliveryId,
                                                     AbilityDeliveryDefinition definition,
                                                     Vec3 point,
                                                     double radius,
                                                     int delayTicks,
                                                     int durationTicks,
                                                     int tickIntervalTicks) {
            UUID runtimeId = UUID.randomUUID();
            return new AbilityReactionOwner(ReactionOwnerType.DELIVERY, runtimeId, runtimeId, invocation.abilityId());
        }
    }

    public interface LifecycleListener {
        LifecycleListener NOOP = new LifecycleListener() {
        };

        default void onInvocationStarted(AbilityInvocation invocation, int castTicks) {
        }

        default void onInvocationCompleted(AbilityInvocation invocation, int castTicksSpent) {
        }

        default void onInvocationInterrupted(AbilityInvocation invocation,
                                             FailureReason failureReason,
                                             int castTicksSpent) {
        }
    }

    private final AbilityDefinitionResolver definitionResolver;
    private final AbilityPowerResolver powerResolver;
    private final AbilityStateStore stateStore;
    private final AbilityEventEmitter eventEmitter;
    private final ReactionController reactionController;
    private final DeliveryController deliveryController;
    private final LifecycleListener lifecycleListener;
    private final BiPredicate<ResourceLocation, ResourceLocation> tagMatcher;
    private final Map<UUID, List<PendingCast>> pendingCastsByCaster = new HashMap<>();
    private final Map<UUID, List<PendingChannel>> pendingChannelsByCaster = new HashMap<>();
    private final Map<UUID, Float> pendingDamageInterrupts = new HashMap<>();

    public SimpleAbilityEngine(AbilityDefinitionResolver definitionResolver,
                               AbilityPowerResolver powerResolver,
                               AbilityStateStore stateStore) {
        this(definitionResolver, powerResolver, stateStore, event -> {
        });
    }

    public SimpleAbilityEngine(AbilityDefinitionResolver definitionResolver,
                               AbilityPowerResolver powerResolver,
                               AbilityStateStore stateStore,
                               AbilityEventEmitter eventEmitter) {
        this(definitionResolver, powerResolver, stateStore, eventEmitter, ReactionController.NOOP);
    }

    public SimpleAbilityEngine(AbilityDefinitionResolver definitionResolver,
                               AbilityPowerResolver powerResolver,
                               AbilityStateStore stateStore,
                               AbilityEventEmitter eventEmitter,
                               ReactionController reactionController) {
        this(definitionResolver, powerResolver, stateStore, eventEmitter, reactionController, DeliveryController.NOOP);
    }

    public SimpleAbilityEngine(AbilityDefinitionResolver definitionResolver,
                               AbilityPowerResolver powerResolver,
                               AbilityStateStore stateStore,
                               AbilityEventEmitter eventEmitter,
                               ReactionController reactionController,
                               DeliveryController deliveryController) {
        this(definitionResolver, powerResolver, stateStore, eventEmitter, reactionController, deliveryController,
                LifecycleListener.NOOP);
    }

    public SimpleAbilityEngine(AbilityDefinitionResolver definitionResolver,
                               AbilityPowerResolver powerResolver,
                               AbilityStateStore stateStore,
                               AbilityEventEmitter eventEmitter,
                               ReactionController reactionController,
                               DeliveryController deliveryController,
                               LifecycleListener lifecycleListener) {
        this(definitionResolver, powerResolver, stateStore, eventEmitter, reactionController, deliveryController,
                lifecycleListener, ResourceLocation::equals);
    }

    public SimpleAbilityEngine(AbilityDefinitionResolver definitionResolver,
                               AbilityPowerResolver powerResolver,
                               AbilityStateStore stateStore,
                               AbilityEventEmitter eventEmitter,
                               ReactionController reactionController,
                               DeliveryController deliveryController,
                               LifecycleListener lifecycleListener,
                               BiPredicate<ResourceLocation, ResourceLocation> tagMatcher) {
        this.definitionResolver = Objects.requireNonNull(definitionResolver, "definitionResolver");
        this.powerResolver = Objects.requireNonNull(powerResolver, "powerResolver");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore");
        this.eventEmitter = Objects.requireNonNull(eventEmitter, "eventEmitter");
        this.reactionController = Objects.requireNonNull(reactionController, "reactionController");
        this.deliveryController = Objects.requireNonNull(deliveryController, "deliveryController");
        this.lifecycleListener = Objects.requireNonNull(lifecycleListener, "lifecycleListener");
        this.tagMatcher = Objects.requireNonNull(tagMatcher, "tagMatcher");
    }

    @Override
    public InvocationResult activate(ActivationRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            return startActivation(
                    request.ownerData(),
                    request.casterData(),
                    request.ability().abilityId(),
                    request.ability().grantId(),
                    request.activationId(),
                    request.sourceId(),
                    request.forcedTargets(),
                    request.eventSnapshot(),
                    request.ignoreCosts(),
                    request.ignoreCooldowns(),
                    ActivationReason.DIRECT_REQUEST,
                    true,
                    0,
                    null,
                    null,
                    null,
                    null,
                    Map.of(),
                    null,
                    true,
                    true
            );
        } catch (ActivationStartFailure failure) {
            return InvocationResult.failed(failure.failureReason());
        }
    }

    public @Nullable FailureReason previewActivationFailure(ActivationRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            PreparedActivation prepared = prepareActivation(
                    request.ownerData(),
                    request.casterData(),
                    request.ability().abilityId(),
                    request.ability().grantId(),
                    request.activationId(),
                    request.sourceId(),
                    request.forcedTargets(),
                    request.eventSnapshot(),
                    request.ignoreCosts(),
                    request.ignoreCooldowns(),
                    ActivationReason.DIRECT_REQUEST,
                    true
            );
            AbilityInvocation invocation = createInvocation(
                    prepared,
                    request.ownerData(),
                    request.casterData(),
                    request.ability().abilityId(),
                    request.ability().grantId(),
                    Map.of(),
                    ActivationReason.DIRECT_REQUEST,
                    0,
                    null,
                    null,
                    null,
                    request.eventSnapshot(),
                    null,
                    true,
                    true
            );
            AbilityActionContext context = createContext(invocation, Optional::empty);
            long gameTick = currentGameTick(request.casterData());
            if (!request.ignoreCooldowns()) {
                checkCooldowns(invocation, prepared.activation(), context, gameTick);
            }
            if (!request.ignoreCosts()) {
                checkCosts(prepared.activation().costs(), context);
            }
            return null;
        } catch (ActivationStartFailure failure) {
            return failure.failureReason();
        }
    }

    public InvocationResult activateInternal(InternalActivationRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            return startActivation(
                    request.ownerData(),
                    request.casterData(),
                    request.ability().abilityId(),
                    request.ability().grantId(),
                    request.activationId(),
                    request.sourceId(),
                    request.forcedTargets(),
                    request.eventSnapshot(),
                    request.ignoreCosts(),
                    request.ignoreCooldowns(),
                    request.reason(),
                    false,
                    request.chainDepth(),
                    request.parentInvocationId(),
                    request.inheritedRootInvocationId(),
                    request.entryPointOverride(),
                    request.targetingOverride(),
                    request.grantParameterOverrides(),
                    request.reactionOwner(),
                    request.clearReactionOwnerOnCompletion(),
                    request.clearReactionOwnerOnInterruption()
            );
        } catch (ActivationStartFailure failure) {
            return InvocationResult.failed(failure.failureReason());
        }
    }

    public InvocationResult activateReaction(RegisteredReaction reaction,
                                             IMKEntityData ownerData,
                                             IMKEntityData casterData,
                                             @Nullable UUID sourceId,
                                             AbilityEventSnapshot event) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(event, "event");
        if (reaction.owner().abilityId() == null) {
            return InvocationResult.failed(FailureReason.UNKNOWN_ABILITY);
        }
        try {
            return startActivation(
                    ownerData,
                    casterData,
                    reaction.owner().abilityId(),
                    null,
                    reaction.definition().activationId(),
                    sourceId != null ? sourceId : reaction.owner().stableSourceId(),
                    null,
                    event,
                    false,
                    false,
                    ActivationReason.REACTION,
                    false,
                    event.chainDepth() + 1,
                    event.invocationId(),
                    event.rootInvocationId(),
                    null,
                    null,
                    Map.of(),
                    null,
                    true,
                    true
            );
        } catch (ActivationStartFailure failure) {
            return InvocationResult.failed(failure.failureReason());
        }
    }

    public void tickEntity(IMKEntityData entityData) {
        UUID casterId = entityData.getEntity().getUUID();
        Float damageInterrupt = pendingDamageInterrupts.remove(casterId);
        if (damageInterrupt != null && damageInterrupt > 0.0f) {
            applyDamageInterrupt(entityData, damageInterrupt);
        }
        Vec3 currentPosition = entityData.getEntity().position();

        List<PendingCast> casts = pendingCastsByCaster.get(casterId);
        if (casts != null && !casts.isEmpty()) {
            Iterator<PendingCast> iterator = casts.iterator();
            while (iterator.hasNext()) {
                PendingCast pendingCast = iterator.next();
                if (!entityData.getEntity().isAlive() || entityData.getEntity().isRemoved()) {
                    iterator.remove();
                    finishInterruptedInvocation(pendingCast.invocation(), FailureReason.INTERRUPTED_BY_DEATH,
                            pendingCast.castTicksSpent());
                    continue;
                }
                if (shouldInterruptOnMove(pendingCast.invocation(), pendingCast.startPosition(), currentPosition)) {
                    iterator.remove();
                    finishInterruptedInvocation(pendingCast.invocation(), FailureReason.INTERRUPTED_BY_MOVE,
                            pendingCast.castTicksSpent());
                    continue;
                }

                pendingCast.tick();
                if (!pendingCast.isComplete()) {
                    continue;
                }

                iterator.remove();
                completeInvocation(pendingCast.invocation(), pendingCast.ignoreCosts(), pendingCast.castTicksSpent());
            }

            if (casts.isEmpty()) {
                pendingCastsByCaster.remove(casterId);
            }
        }

        List<PendingChannel> channels = pendingChannelsByCaster.get(casterId);
        if (channels == null || channels.isEmpty()) {
            return;
        }

        Iterator<PendingChannel> channelIterator = channels.iterator();
        while (channelIterator.hasNext()) {
            PendingChannel pendingChannel = channelIterator.next();
                if (!entityData.getEntity().isAlive() || entityData.getEntity().isRemoved()) {
                    channelIterator.remove();
                    finishInterruptedInvocation(pendingChannel.invocation(), FailureReason.INTERRUPTED_BY_DEATH,
                            pendingChannel.castTicksSpent());
                    continue;
                }
                if (shouldInterruptOnMove(pendingChannel.invocation(), pendingChannel.startPosition(), currentPosition)) {
                    channelIterator.remove();
                    finishInterruptedInvocation(pendingChannel.invocation(), FailureReason.INTERRUPTED_BY_MOVE,
                            pendingChannel.castTicksSpent());
                    continue;
                }

                pendingChannel.tick();
                if (!pendingChannel.readyForPulse()) {
                    continue;
                }

            try {
                runChannelPulse(pendingChannel);
                pendingChannel.resetPulseInterval();
            } catch (InvocationInterruptedException interrupted) {
                channelIterator.remove();
                finishInterruptedInvocation(pendingChannel.invocation(), interrupted.failureReason(),
                        pendingChannel.castTicksSpent());
            }
        }

        if (channels.isEmpty()) {
            pendingChannelsByCaster.remove(casterId);
        }
    }

    public void queueDamageInterrupt(IMKEntityData entityData, float damageAmount) {
        Objects.requireNonNull(entityData, "entityData");
        if (damageAmount <= 0.0f) {
            return;
        }
        pendingDamageInterrupts.merge(entityData.getEntity().getUUID(), damageAmount, Math::max);
    }

    public boolean hasPendingActivation(IMKEntityData entityData) {
        Objects.requireNonNull(entityData, "entityData");
        UUID casterId = entityData.getEntity().getUUID();
        List<PendingCast> casts = pendingCastsByCaster.get(casterId);
        if (casts != null && !casts.isEmpty()) {
            return true;
        }
        List<PendingChannel> channels = pendingChannelsByCaster.get(casterId);
        return channels != null && !channels.isEmpty();
    }

    public void interruptPendingActivations(IMKEntityData entityData) {
        Objects.requireNonNull(entityData, "entityData");
        interruptPendingActivations(entityData.getEntity().getUUID(), FailureReason.INTERRUPTED);
    }

    public void interruptPendingActivations(IMKEntityData entityData, FailureReason failureReason) {
        Objects.requireNonNull(entityData, "entityData");
        Objects.requireNonNull(failureReason, "failureReason");
        interruptPendingActivations(entityData.getEntity().getUUID(), failureReason);
    }

    public List<PersistedPendingAbilityActivation> snapshotOwnedActivations(UUID ownerEntityId) {
        Objects.requireNonNull(ownerEntityId, "ownerEntityId");
        List<PersistedPendingAbilityActivation> snapshot = new ArrayList<>();

        for (List<PendingCast> casts : pendingCastsByCaster.values()) {
            for (PendingCast pendingCast : casts) {
                if (!isOwnedBy(pendingCast.invocation(), ownerEntityId) || pendingCast.remainingTicks() <= 0) {
                    continue;
                }
                snapshot.add(new PersistedPendingAbilityActivation(
                        PersistedPendingAbilityActivation.PendingActivationType.CAST,
                        PersistedPendingAbilityActivation.InvocationEntry.fromInvocation(pendingCast.invocation()),
                        pendingCast.ignoreCosts(),
                        pendingCast.remainingTicks(),
                        pendingCast.castTicksSpent(),
                        null
                ));
            }
        }

        for (List<PendingChannel> channels : pendingChannelsByCaster.values()) {
            for (PendingChannel pendingChannel : channels) {
                if (!isOwnedBy(pendingChannel.invocation(), ownerEntityId)) {
                    continue;
                }
                snapshot.add(new PersistedPendingAbilityActivation(
                        PersistedPendingAbilityActivation.PendingActivationType.CHANNEL,
                        PersistedPendingAbilityActivation.InvocationEntry.fromInvocation(pendingChannel.invocation()),
                        pendingChannel.ignoreCosts(),
                        pendingChannel.remainingPulseTicks(),
                        pendingChannel.castTicksSpent(),
                        PersistedPendingAbilityActivation.TargetsEntry.fromTargets(pendingChannel.currentTargets())
                ));
            }
        }

        return List.copyOf(snapshot);
    }

    public void restoreOwnedActivations(IMKEntityData ownerData,
                                        List<PersistedPendingAbilityActivation> activations,
                                        Function<UUID, IMKEntityData> entityResolver) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(activations, "activations");
        Objects.requireNonNull(entityResolver, "entityResolver");
        UUID ownerEntityId = ownerData.getEntity().getUUID();

        for (PersistedPendingAbilityActivation activation : activations) {
            AbilityInvocation invocation = restoreInvocation(activation.invocation(), ownerData, entityResolver);
            if (invocation == null || !isOwnedBy(invocation, ownerEntityId)) {
                continue;
            }

            switch (activation.type()) {
                case CAST -> {
                    if (activation.remainingTicks() <= 0) {
                        continue;
                    }
                    pendingCastsByCaster.computeIfAbsent(invocation.casterData().getEntity().getUUID(),
                                    ignored -> new ArrayList<>())
                            .add(PendingCast.restore(invocation, activation.remainingTicks(),
                                    activation.castTicksSpent(), activation.ignoreCosts()));
                }
                case CHANNEL -> {
                    if (!(activationDefinition(invocation).behavior() instanceof ActivationBehavior.ChannelBehavior channelBehavior)) {
                        continue;
                    }
                    AbilityResolvedTargets currentTargets = activation.currentTargets() != null
                            ? activation.currentTargets().toTargets()
                            : invocation.targets();
                    pendingChannelsByCaster.computeIfAbsent(invocation.casterData().getEntity().getUUID(),
                                    ignored -> new ArrayList<>())
                            .add(PendingChannel.restore(invocation, channelBehavior, activation.ignoreCosts(),
                                    activation.castTicksSpent(), currentTargets, activation.remainingTicks()));
                }
            }
        }
    }

    public void interruptOwnedActivations(UUID ownerEntityId, FailureReason failureReason) {
        Objects.requireNonNull(ownerEntityId, "ownerEntityId");
        Objects.requireNonNull(failureReason, "failureReason");

        Iterator<Map.Entry<UUID, List<PendingCast>>> castLists = pendingCastsByCaster.entrySet().iterator();
        while (castLists.hasNext()) {
            Map.Entry<UUID, List<PendingCast>> entry = castLists.next();
            Iterator<PendingCast> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                PendingCast pendingCast = iterator.next();
                if (!isOwnedBy(pendingCast.invocation(), ownerEntityId)) {
                    continue;
                }
                iterator.remove();
                finishInterruptedInvocation(pendingCast.invocation(), failureReason, pendingCast.castTicksSpent());
            }
            if (entry.getValue().isEmpty()) {
                castLists.remove();
            }
        }

        Iterator<Map.Entry<UUID, List<PendingChannel>>> channelLists = pendingChannelsByCaster.entrySet().iterator();
        while (channelLists.hasNext()) {
            Map.Entry<UUID, List<PendingChannel>> entry = channelLists.next();
            Iterator<PendingChannel> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                PendingChannel pendingChannel = iterator.next();
                if (!isOwnedBy(pendingChannel.invocation(), ownerEntityId)) {
                    continue;
                }
                iterator.remove();
                finishInterruptedInvocation(pendingChannel.invocation(), failureReason,
                        pendingChannel.castTicksSpent());
            }
            if (entry.getValue().isEmpty()) {
                channelLists.remove();
            }
        }
    }

    private InvocationResult startActivation(IMKEntityData ownerData,
                                             IMKEntityData casterData,
                                             ResourceLocation abilityId,
                                             @Nullable UUID abilityInstanceId,
                                             String activationId,
                                             @Nullable UUID sourceId,
                                             @Nullable AbilityResolvedTargets forcedTargets,
                                             @Nullable AbilityEventSnapshot eventSnapshot,
                                             boolean ignoreCosts,
                                             boolean ignoreCooldowns,
                                             ActivationReason reason,
                                             boolean externalOnly,
                                             int chainDepth,
                                             @Nullable UUID parentInvocationId,
                                             @Nullable UUID inheritedRootInvocationId,
                                             @Nullable String entryPointOverride,
                                             @Nullable AbilityTargetResolverDefinition targetingOverride,
                                             Map<String, AbilityValue> grantParameterOverrides,
                                             @Nullable AbilityReactionOwner reactionOwnerOverride,
                                             boolean clearReactionOwnerOnCompletion,
                                             boolean clearReactionOwnerOnInterruption) {
        PreparedActivation prepared = prepareActivation(
                ownerData,
                casterData,
                abilityId,
                abilityInstanceId,
                activationId,
                sourceId,
                forcedTargets,
                eventSnapshot,
                ignoreCosts,
                ignoreCooldowns,
                reason,
                externalOnly,
                targetingOverride
        );
        AbilityInvocation invocation = createInvocation(
                prepared,
                ownerData,
                casterData,
                abilityId,
                abilityInstanceId,
                grantParameterOverrides,
                reason,
                chainDepth,
                parentInvocationId,
                inheritedRootInvocationId,
                entryPointOverride,
                eventSnapshot,
                reactionOwnerOverride,
                clearReactionOwnerOnCompletion,
                clearReactionOwnerOnInterruption
        );

        SimpleAbilityActionContext context = createContext(invocation, Optional::empty);
        long gameTick = currentGameTick(casterData);

        if (!ignoreCooldowns) {
            checkCooldowns(invocation, prepared.activation(), context, gameTick);
        }
        if (!ignoreCosts) {
            checkCosts(prepared.activation().costs(), context);
            consumeCosts(prepared.activation().costs(), context);
        }
        if (!ignoreCooldowns) {
            writeCooldowns(invocation, prepared.activation(), context, gameTick);
        }

        int castTicks = resolveCastTicks(prepared.activation(), invocation.invocationStats());
        emitInvocationStarted(invocation, castTicks);
        if (castTicks > 0) {
            pendingCastsByCaster.computeIfAbsent(invocation.casterData().getEntity().getUUID(), ignored -> new ArrayList<>())
                    .add(new PendingCast(invocation, castTicks, ignoreCosts));
        } else {
            completeInvocation(invocation, ignoreCosts, 0);
        }
        return InvocationResult.started(invocation.invocationId());
    }

    private PreparedActivation prepareActivation(IMKEntityData ownerData,
                                                 IMKEntityData casterData,
                                                 ResourceLocation abilityId,
                                                 @Nullable UUID abilityInstanceId,
                                                 String activationId,
                                                 @Nullable UUID sourceId,
                                                 @Nullable AbilityResolvedTargets forcedTargets,
                                                 @Nullable AbilityEventSnapshot eventSnapshot,
                                                 boolean ignoreCosts,
                                                 boolean ignoreCooldowns,
                                                 ActivationReason reason,
                                                 boolean externalOnly) {
        return prepareActivation(ownerData, casterData, abilityId, abilityInstanceId, activationId, sourceId,
                forcedTargets, eventSnapshot, ignoreCosts, ignoreCooldowns, reason, externalOnly, null);
    }

    private PreparedActivation prepareActivation(IMKEntityData ownerData,
                                                 IMKEntityData casterData,
                                                 ResourceLocation abilityId,
                                                 @Nullable UUID abilityInstanceId,
                                                 String activationId,
                                                 @Nullable UUID sourceId,
                                                 @Nullable AbilityResolvedTargets forcedTargets,
                                                 @Nullable AbilityEventSnapshot eventSnapshot,
                                                 boolean ignoreCosts,
                                                 boolean ignoreCooldowns,
                                                 ActivationReason reason,
                                                 boolean externalOnly,
                                                 @Nullable AbilityTargetResolverDefinition targetingOverride) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(abilityId);
        if (definition == null) {
            throw new ActivationStartFailure(FailureReason.UNKNOWN_ABILITY);
        }

        AbilityActivationDefinition activation = definition.definition().getActivation(activationId);
        if (activation == null) {
            throw new ActivationStartFailure(FailureReason.UNKNOWN_ACTIVATION);
        }

        if (!isActivationAllowed(activation, reason, externalOnly)) {
            throw new ActivationStartFailure(FailureReason.ACTIVATION_NOT_EXTERNALLY_CALLABLE);
        }
        if (!isBehaviorSupported(activation.behavior())) {
            throw new ActivationStartFailure(FailureReason.UNSUPPORTED_FEATURE);
        }
        if (reason == ActivationReason.DIRECT_REQUEST && isBusyForDirectRequest(casterData)) {
            throw new ActivationStartFailure(FailureReason.BUSY);
        }

        AbilityResolvedTargets targets = resolveActivationTargets(casterData, forcedTargets, eventSnapshot,
                targetingOverride != null ? targetingOverride : activation.targeting());
        UUID resolvedSourceId = sourceId != null ? sourceId : casterData.getEntity().getUUID();
        AbilityStatSnapshot invocationStats = powerResolver.captureInvocationStats(casterData);
        return new PreparedActivation(definition, activationId, activation, targets, resolvedSourceId, invocationStats);
    }

    private AbilityInvocation createInvocation(PreparedActivation prepared,
                                               IMKEntityData ownerData,
                                               IMKEntityData casterData,
                                               ResourceLocation abilityId,
                                               @Nullable UUID abilityInstanceId,
                                               Map<String, AbilityValue> grantParameterOverrides,
                                               ActivationReason reason,
                                               int chainDepth,
                                               @Nullable UUID parentInvocationId,
                                               @Nullable UUID inheritedRootInvocationId,
                                               @Nullable String entryPointOverride,
                                               @Nullable AbilityEventSnapshot eventSnapshot,
                                               @Nullable AbilityReactionOwner reactionOwnerOverride,
                                               boolean clearReactionOwnerOnCompletion,
                                               boolean clearReactionOwnerOnInterruption) {
        UUID invocationId = UUID.randomUUID();
        return new AbilityInvocation(
                invocationId,
                inheritedRootInvocationId != null ? inheritedRootInvocationId : invocationId,
                chainDepth,
                parentInvocationId,
                abilityId,
                abilityInstanceId,
                prepared.activationId(),
                entryPointOverride != null ? entryPointOverride : prepared.activation().entryPoint(),
                reason,
                ownerData,
                casterData,
                prepared.resolvedSourceId(),
                prepared.targets(),
                eventSnapshot,
                reactionOwnerOverride,
                clearReactionOwnerOnCompletion,
                clearReactionOwnerOnInterruption,
                prepared.definition(),
                grantParameterOverrides,
                prepared.invocationStats(),
                Map.of(),
                casterData.getEntity().getRandom()
        );
    }

    private boolean isActivationAllowed(AbilityActivationDefinition activation,
                                        ActivationReason reason,
                                        boolean externalOnly) {
        if (externalOnly) {
            return activation.kind() == ActivationKind.MANUAL || activation.kind() == ActivationKind.AI;
        }
        return switch (reason) {
            case REACTION -> activation.kind() == ActivationKind.PROC;
            case TOGGLE_LIFECYCLE -> activation.kind() == ActivationKind.TOGGLE_ENABLE
                    || activation.kind() == ActivationKind.TOGGLE_DISABLE;
            case AURA_PULSE -> activation.kind() == ActivationKind.TOGGLE_ENABLE
                    && activation.behavior() instanceof ActivationBehavior.AuraBehavior;
            case PASSIVE_LIFECYCLE -> activation.kind() == ActivationKind.PASSIVE_SETUP
                    || activation.kind() == ActivationKind.PASSIVE_TEARDOWN;
            case DIRECT_REQUEST, DELIVERY_CALLBACK, CHANNEL_TICK -> true;
        };
    }

    private boolean isBehaviorSupported(ActivationBehavior behavior) {
        return behavior instanceof ActivationBehavior.InstantBehavior
                || behavior instanceof ActivationBehavior.ChannelBehavior
                || behavior instanceof ActivationBehavior.AuraBehavior;
    }

    private void applyDamageInterrupt(IMKEntityData entityData, float damageAmount) {
        UUID casterId = entityData.getEntity().getUUID();

        List<PendingCast> casts = pendingCastsByCaster.get(casterId);
        if (casts != null && !casts.isEmpty()) {
            Iterator<PendingCast> iterator = casts.iterator();
            while (iterator.hasNext()) {
                PendingCast pendingCast = iterator.next();
                if (!shouldInterruptOnDamage(pendingCast.invocation(), damageAmount)) {
                    continue;
                }
                iterator.remove();
                finishInterruptedInvocation(pendingCast.invocation(), FailureReason.INTERRUPTED_BY_DAMAGE,
                        pendingCast.castTicksSpent());
            }
            if (casts.isEmpty()) {
                pendingCastsByCaster.remove(casterId);
            }
        }

        List<PendingChannel> channels = pendingChannelsByCaster.get(casterId);
        if (channels != null && !channels.isEmpty()) {
            Iterator<PendingChannel> iterator = channels.iterator();
            while (iterator.hasNext()) {
                PendingChannel pendingChannel = iterator.next();
                if (!shouldInterruptOnDamage(pendingChannel.invocation(), damageAmount)) {
                    continue;
                }
                iterator.remove();
                finishInterruptedInvocation(pendingChannel.invocation(), FailureReason.INTERRUPTED_BY_DAMAGE,
                        pendingChannel.castTicksSpent());
            }
            if (channels.isEmpty()) {
                pendingChannelsByCaster.remove(casterId);
            }
        }
    }

    private boolean shouldInterruptOnDamage(AbilityInvocation invocation, float damageAmount) {
        InterruptPolicy interruptPolicy = activationDefinition(invocation).interruptPolicy();
        return interruptPolicy.onDamage() && damageAmount >= interruptPolicy.minDamage();
    }

    private boolean shouldInterruptOnMove(AbilityInvocation invocation, Vec3 startPosition, Vec3 currentPosition) {
        InterruptPolicy interruptPolicy = activationDefinition(invocation).interruptPolicy();
        if (!interruptPolicy.onMove()) {
            return false;
        }
        double threshold = interruptPolicy.moveThresholdBlocks();
        return currentPosition.distanceToSqr(startPosition) > threshold * threshold;
    }

    private boolean isBusyForDirectRequest(IMKEntityData casterData) {
        return casterData.getEntity().isBlocking()
                || casterData.getAbilityExecutor().isCasting()
                || hasPendingActivation(casterData);
    }

    private void interruptPendingActivations(UUID casterId, FailureReason failureReason) {
        List<PendingCast> casts = pendingCastsByCaster.remove(casterId);
        if (casts != null) {
            for (PendingCast pendingCast : casts) {
                finishInterruptedInvocation(pendingCast.invocation(), failureReason, pendingCast.castTicksSpent());
            }
        }

        List<PendingChannel> channels = pendingChannelsByCaster.remove(casterId);
        if (channels != null) {
            for (PendingChannel pendingChannel : channels) {
                finishInterruptedInvocation(pendingChannel.invocation(), failureReason, pendingChannel.castTicksSpent());
            }
        }
    }

    private boolean isOwnedBy(AbilityInvocation invocation, UUID ownerEntityId) {
        return invocation.ownerData().getEntity().getUUID().equals(ownerEntityId);
    }

    private @Nullable AbilityInvocation restoreInvocation(PersistedPendingAbilityActivation.InvocationEntry entry,
                                                          IMKEntityData ownerData,
                                                          Function<UUID, IMKEntityData> entityResolver) {
        IMKEntityData casterData = entry.casterEntityId().equals(entry.ownerEntityId())
                ? ownerData
                : entityResolver.apply(entry.casterEntityId());
        if (ownerData == null || casterData == null) {
            return null;
        }
        if (!ownerData.getEntity().isAlive() || ownerData.getEntity().isRemoved()
                || !casterData.getEntity().isAlive() || casterData.getEntity().isRemoved()) {
            return null;
        }

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(entry.abilityId());
        if (definition == null || definition.definition().getActivation(entry.activationId()) == null) {
            return null;
        }

        AbilityInvocation invocation = new AbilityInvocation(
                entry.invocationId(),
                entry.rootInvocationId(),
                entry.chainDepth(),
                entry.parentInvocationId(),
                entry.abilityId(),
                entry.abilityInstanceId(),
                entry.activationId(),
                entry.entryPointId(),
                entry.reason(),
                ownerData,
                casterData,
                entry.sourceId(),
                entry.targets().toTargets(),
                entry.eventSnapshot() != null ? entry.eventSnapshot().toSnapshot() : null,
                entry.reactionOwner() != null ? entry.reactionOwner().toOwner() : null,
                entry.clearReactionOwnerOnCompletion(),
                entry.clearReactionOwnerOnInterruption(),
                definition,
                entry.grantParameterOverrides(),
                entry.invocationStats().toSnapshot(),
                entry.graphVars(),
                casterData.getEntity().getRandom()
        );
        if (entry.hasProducedGameplayEffect()) {
            invocation.markProducedGameplayEffect();
        }
        return invocation;
    }

    private AbilityResolvedTargets resolveActivationTargets(IMKEntityData casterData,
                                                           @Nullable AbilityResolvedTargets forcedTargets,
                                                           @Nullable AbilityEventSnapshot eventSnapshot,
                                                           AbilityTargetResolverDefinition targeting) {
        if (forcedTargets != null) {
            validateForcedTargets(casterData, eventSnapshot, targeting, forcedTargets);
            return forcedTargets;
        }

        return switch (targeting.type()) {
            case "none" -> new AbilityResolvedTargets(null, List.of(), null, null, null);
            case "self" -> {
                UUID selfId = casterData.getEntity().getUUID();
                yield new AbilityResolvedTargets(selfId, List.of(selfId), null, null, null);
            }
            case "event_target" -> {
                UUID targetId = eventSnapshot != null ? eventSnapshot.targetEntityId() : null;
                yield resolveSingleTarget(casterData, targetId, targeting);
            }
            case "event_actor" -> {
                UUID targetId = eventSnapshot != null ? eventSnapshot.actorEntityId() : null;
                yield resolveSingleTarget(casterData, targetId, targeting);
            }
            case "resolved" -> throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
            default -> throw new ActivationStartFailure(FailureReason.UNSUPPORTED_FEATURE);
        };
    }

    private AbilityResolvedTargets resolveSingleTarget(IMKEntityData casterData,
                                                       @Nullable UUID targetId,
                                                       AbilityTargetResolverDefinition targeting) {
        if (targetId == null) {
            throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
        }
        LivingEntity target = resolveLivingEntity(casterData.getEntity(), targetId);
        if (target == null || !target.isAlive()
                || !AbilityTargeting.isValidTarget(targeting, casterData.getEntity(), target)) {
            throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
        }
        return new AbilityResolvedTargets(targetId, List.of(targetId), null, null, null);
    }

    private void validateForcedTargets(IMKEntityData casterData,
                                       @Nullable AbilityEventSnapshot eventSnapshot,
                                       AbilityTargetResolverDefinition targeting,
                                       AbilityResolvedTargets targets) {
        validateTargetEntities(casterData, targets, null);
        switch (targeting.type()) {
            case "none" -> {
                if (targets.primaryEntityId() != null || !targets.entityIds().isEmpty()) {
                    throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
                }
            }
            case "self" -> {
                UUID casterId = casterData.getEntity().getUUID();
                if (!casterId.equals(targets.primaryEntityId()) || targets.entityIds().size() != 1
                        || !casterId.equals(targets.entityIds().getFirst())) {
                    throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
                }
            }
            case "event_target" -> {
                UUID expected = eventSnapshot != null ? eventSnapshot.targetEntityId() : null;
                if (!Objects.equals(expected, targets.primaryEntityId())) {
                    throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
                }
                validateTargetEntities(casterData, targets, targeting);
            }
            case "event_actor" -> {
                UUID expected = eventSnapshot != null ? eventSnapshot.actorEntityId() : null;
                if (!Objects.equals(expected, targets.primaryEntityId())) {
                    throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
                }
                validateTargetEntities(casterData, targets, targeting);
            }
            case "resolved" -> validateTargetEntities(casterData, targets, targeting);
            default -> throw new ActivationStartFailure(FailureReason.UNSUPPORTED_FEATURE);
        }
    }

    private void validateTargetEntities(IMKEntityData casterData,
                                        AbilityResolvedTargets targets,
                                        @Nullable AbilityTargetResolverDefinition targeting) {
        for (UUID entityId : targets.entityIds()) {
            LivingEntity entity = resolveLivingEntity(casterData.getEntity(), entityId);
            if (entity == null || !entity.isAlive()
                    || (targeting != null && !AbilityTargeting.isValidTarget(targeting, casterData.getEntity(), entity))) {
                throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
            }
        }
        if (targets.primaryEntityId() != null && !targets.entityIds().contains(targets.primaryEntityId())) {
            throw new ActivationStartFailure(FailureReason.INVALID_TARGETS);
        }
    }

    private void checkCooldowns(AbilityInvocation invocation,
                                AbilityActivationDefinition activation,
                                AbilityActionContext context,
                                long gameTick) {
        if (activation.gcdGroup() != null
                && stateStore.getGcdRemainingTicks(invocation.ownerData(), activation.gcdGroup(), gameTick) > 0) {
            throw new ActivationStartFailure(FailureReason.ON_COOLDOWN);
        }

        for (AbilityCooldownDefinition cooldown : activation.cooldowns()) {
            if (stateStore.getCooldownRemainingTicks(invocation, cooldown.scope(), cooldown.key(), gameTick) > 0) {
                throw new ActivationStartFailure(FailureReason.ON_COOLDOWN);
            }
        }
    }

    private void writeCooldowns(AbilityInvocation invocation,
                                AbilityActivationDefinition activation,
                                AbilityActionContext context,
                                long gameTick) {
        for (AbilityCooldownDefinition cooldown : activation.cooldowns()) {
            int duration = applyCooldownRate(resolveScalarTicks(cooldown.duration(), context), invocation.invocationStats());
            stateStore.setCooldown(invocation, cooldown.scope(), cooldown.key(), duration, gameTick);
        }
        if (activation.gcdGroup() != null) {
            stateStore.setGcd(invocation.ownerData(), activation.gcdGroup(), GameConstants.GLOBAL_COOLDOWN_TICKS, gameTick);
        }
    }

    private void checkCosts(List<AbilityCostDefinition> costs, AbilityActionContext context) {
        for (AbilityCostDefinition cost : costs) {
            if (!canAfford(cost, context)) {
                throw new ActivationStartFailure(FailureReason.NOT_ENOUGH_RESOURCE);
            }
        }
    }

    private void consumeCosts(List<AbilityCostDefinition> costs, AbilityActionContext context) {
        for (AbilityCostDefinition cost : costs) {
            if (!consumeCost(cost, context)) {
                throw new ActivationStartFailure(FailureReason.NOT_ENOUGH_RESOURCE);
            }
        }
    }

    private void refundActivationCostsIfEligible(AbilityInvocation invocation) {
        AbilityActivationDefinition activation = activationDefinition(invocation);
        if (activation.refundPolicy() != InterruptRefundPolicy.REFUND_COSTS_BEFORE_FIRST_EFFECT
                || invocation.hasProducedGameplayEffect()) {
            return;
        }
        refundActivationCosts(activation.costs(), createContext(invocation, Optional::empty));
    }

    private void refundActivationCosts(List<AbilityCostDefinition> costs, AbilityActionContext context) {
        for (AbilityCostDefinition cost : costs) {
            refundCost(cost, context);
        }
    }

    private boolean canAfford(AbilityCostDefinition cost, AbilityActionContext context) {
        double amount = resolveCostAmount(cost, context);
        IMKEntityData ownerData = context.ownerData();
        return switch (cost.kind()) {
            case MANA -> ownerData.getStats().getMana() >= amount;
            case HEALTH -> ownerData.getEntity().getHealth() > amount;
            case CUSTOM_RESOURCE -> throw new ActivationStartFailure(FailureReason.UNSUPPORTED_FEATURE);
        };
    }

    private boolean consumeCost(AbilityCostDefinition cost, AbilityActionContext context) {
        double amount = resolveCostAmount(cost, context);
        IMKEntityData ownerData = context.ownerData();
        return switch (cost.kind()) {
            case MANA -> ownerData.getStats().consumeMana((float) amount);
            case HEALTH -> {
                if (ownerData.getEntity().getHealth() <= amount) {
                    yield false;
                }
                ownerData.getEntity().setHealth(Math.max(0.0f, ownerData.getEntity().getHealth() - (float) amount));
                yield true;
            }
            case CUSTOM_RESOURCE -> throw new ActivationStartFailure(FailureReason.UNSUPPORTED_FEATURE);
        };
    }

    private void refundCost(AbilityCostDefinition cost, AbilityActionContext context) {
        double amount = resolveCostAmount(cost, context);
        IMKEntityData ownerData = context.ownerData();
        switch (cost.kind()) {
            case MANA -> ownerData.getStats().addMana((float) amount);
            case HEALTH -> {
                if (!ownerData.getEntity().isAlive()) {
                    return;
                }
                ownerData.getStats().setHealth(Math.min(ownerData.getStats().getMaxHealth(),
                        ownerData.getStats().getHealth() + (float) amount));
            }
            case CUSTOM_RESOURCE -> {
            }
        }
    }

    private double resolveCostAmount(AbilityCostDefinition cost, AbilityActionContext context) {
        double amount = powerResolver.resolve(cost.amount(), context);
        if (cost.kind() == CostKind.MANA) {
            amount *= context.stats(StatCapturePolicy.ON_INVOCATION).manaCostMultiplier();
        }
        return Math.max(0.0, amount);
    }

    private int resolveCastTicks(AbilityActivationDefinition activation, AbilityStatSnapshot stats) {
        if (activation.castTicks() <= 0) {
            return 0;
        }
        if (!activation.affectedByCastSpeed()) {
            return activation.castTicks();
        }
        double modifier = 2.0 - stats.castSpeed();
        return Math.max(0, (int) (modifier * activation.castTicks()));
    }

    private int applyCooldownRate(int baseDuration, AbilityStatSnapshot stats) {
        double modifier = 2.0 - stats.cooldownRate();
        return Math.max(0, (int) (modifier * baseDuration));
    }

    private int resolveScalarTicks(AbilityScalar scalar, AbilityActionContext context) {
        return Math.max(0, (int) Math.round(powerResolver.resolve(scalar, context)));
    }

    private void completeInvocation(AbilityInvocation invocation, boolean ignoreCosts, int castTicksSpent) {
        if (!hasValidExecutionTargets(invocation)) {
            finishInterruptedInvocation(invocation, FailureReason.TARGET_LOST, castTicksSpent);
            return;
        }
        try {
            executeEntryPoint(invocation, createContext(invocation, Optional::empty), invocation.entryPointId(), ignoreCosts);
            if (activationDefinition(invocation).behavior() instanceof ActivationBehavior.ChannelBehavior channelBehavior) {
                pendingChannelsByCaster.computeIfAbsent(invocation.casterData().getEntity().getUUID(),
                                ignored -> new ArrayList<>())
                        .add(new PendingChannel(invocation, channelBehavior, ignoreCosts, castTicksSpent));
            } else {
                emitInvocationCompleted(invocation, castTicksSpent);
                clearReactionOwnerOnCompletion(invocation);
            }
        } catch (InvocationInterruptedException interrupted) {
            finishInterruptedInvocation(invocation, interrupted.failureReason(), castTicksSpent);
        }
    }

    private void executeEntryPoint(AbilityInvocation invocation,
                                   AbilityActionContext context,
                                   String entryPointId,
                                   boolean ignoreCosts) {
        List<AbilityAction> actions = invocation.definition().definition().getEntryPoint(entryPointId);
        if (actions == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unknown entry point " + entryPointId);
        }
        executeActions(invocation, context, actions, ignoreCosts);
    }

    private void executeActions(AbilityInvocation invocation,
                                AbilityActionContext context,
                                List<AbilityAction> actions,
                                boolean ignoreCosts) {
        for (AbilityAction action : actions) {
            switch (action) {
                case AbilityAction.DamageAction damageAction -> executeDamage(invocation, context, damageAction);
                case AbilityAction.HealAction healAction -> executeHeal(invocation, context, healAction);
                case AbilityAction.ApplyEffectAction applyEffectAction ->
                        executeApplyEffect(invocation, context, applyEffectAction);
                case AbilityAction.ModifyStateAction modifyStateAction ->
                        executeModifyState(invocation, context, modifyStateAction);
                case AbilityAction.PayCostAction payCostAction -> executePayCost(context, payCostAction, ignoreCosts);
                case AbilityAction.SetVarAction setVarAction -> context.setVar(setVarAction.name(), setVarAction.value());
                case AbilityAction.BranchAction branchAction -> executeBranch(invocation, context, branchAction, ignoreCosts);
                case AbilityAction.ForEachTargetAction forEachTargetAction ->
                        executeForEachTarget(invocation, context, forEachTargetAction, ignoreCosts);
                case AbilityAction.StartEntryPointAction startEntryPointAction ->
                        executeEntryPoint(invocation, context, startEntryPointAction.entryPoint(), ignoreCosts);
                case AbilityAction.InstallReactionAction installReactionAction ->
                        executeInstallReaction(invocation, context, installReactionAction);
                case AbilityAction.RemoveReactionAction removeReactionAction ->
                        executeRemoveReaction(invocation, context, removeReactionAction);
                case AbilityAction.SpawnProjectileAction spawnProjectileAction ->
                        executeSpawnProjectile(invocation, context, spawnProjectileAction, ignoreCosts);
                case AbilityAction.StartDeliveryAction startDeliveryAction ->
                        executeStartDelivery(invocation, context, startDeliveryAction, ignoreCosts);
            }
        }
    }

    private void executeDamage(AbilityInvocation invocation,
                               AbilityActionContext context,
                               AbilityAction.DamageAction action) {
        LivingEntity target = resolveActionTarget(context, action.target()).orElse(null);
        if (target == null) {
            return;
        }
        if (action.school() == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Damage actions currently require a school");
        }

        MKDamageType damageType = MKCoreRegistry.getDamageType(action.school());
        if (damageType == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unknown damage type " + action.school());
        }

        float amount = (float) powerResolver.resolve(action.amount(), context);
        MKEffectBuilder<?> effect = CoreEffects.ABILITY_DAMAGE.get().builder(invocation.sourceId())
                .sourceEntity(invocation.casterData().getEntity())
                .ability(invocation.abilityId())
                .eventProvenance(AbilityEventProvenance.fromInvocation(invocation))
                .state(state -> {
                    state.setDamageType(damageType);
                    state.setScalingParameters(amount, 0.0f, 0.0f);
                });
        targetData(target).getEffects().addEffect(effect);
        emitSpellHit(invocation, target, amount, damageType, action.school());
        invocation.markProducedGameplayEffect();
    }

    private void executeHeal(AbilityInvocation invocation,
                             AbilityActionContext context,
                             AbilityAction.HealAction action) {
        LivingEntity target = resolveActionTarget(context, action.target()).orElse(null);
        if (target == null) {
            return;
        }

        float amount = (float) powerResolver.resolve(action.amount(), context);
        MKHealing.healEntityFrom(
                target,
                amount,
                MKHealSource.getHolyHeal(invocation.abilityId(), null, invocation.casterData().getEntity(), 0.0f)
        );
        invocation.markProducedGameplayEffect();
    }

    private void executeApplyEffect(AbilityInvocation invocation,
                                    AbilityActionContext context,
                                    AbilityAction.ApplyEffectAction action) {
        LivingEntity target = resolveActionTarget(context, action.target()).orElse(null);
        if (target == null) {
            return;
        }

        MKEffect effectType = MKCoreRegistry.EFFECTS.get(action.effect());
        if (effectType == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unknown effect " + action.effect());
        }

        EntityEffectHandler effectHandler = targetData(target).getEffects();
        MKEffectBuilder<?> effect = effectType.builder(invocation.sourceId())
                .sourceEntity(invocation.casterData().getEntity())
                .ability(invocation.abilityId())
                .eventProvenance(AbilityEventProvenance.fromInvocation(invocation))
                .setBaseStackCount(action.stackCount());

        if (action.duration() != null) {
            effect.timed(applyBuffDuration(resolveScalarTicks(action.duration(), context), context));
        } else {
            effect.instant();
        }

        effectHandler.addEffect(effect);
        emitEffectApplied(invocation, target, action.effect(), action.stackCount());
        invocation.markProducedGameplayEffect();
    }

    private void executeModifyState(AbilityInvocation invocation,
                                    AbilityActionContext context,
                                    AbilityAction.ModifyStateAction action) {
        AbilityValue updated = switch (action.operation()) {
            case SET_BOOL -> requireValueKind(action.value(), AbilityValueKind.BOOL, action.stateKey());
            case SET_INT -> requireValueKind(action.value(), AbilityValueKind.INT, action.stateKey());
            case ADD_INT -> {
                int delta = requireValueKind(action.value(), AbilityValueKind.INT, action.stateKey()).asInt(action.stateKey());
                int current = currentIntState(invocation, action.scope(), action.stateKey());
                yield new AbilityValue.IntValue(current + delta);
            }
            case SET_FLOAT -> requireValueKind(action.value(), AbilityValueKind.FLOAT, action.stateKey());
            case ADD_FLOAT -> {
                float delta = requireValueKind(action.value(), AbilityValueKind.FLOAT, action.stateKey()).asFloat(action.stateKey());
                float current = currentFloatState(invocation, action.scope(), action.stateKey());
                yield new AbilityValue.FloatValue(current + delta);
            }
            case CLEAR -> null;
        };
        stateStore.setState(invocation, action.scope(), action.stateKey(), updated);
        invocation.markProducedGameplayEffect();
    }

    private void executePayCost(AbilityActionContext context,
                                AbilityAction.PayCostAction action,
                                boolean ignoreCosts) {
        if (ignoreCosts) {
            return;
        }
        if (action.cost().kind() == CostKind.CUSTOM_RESOURCE) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Custom resources are not implemented yet");
        }
        if (!canAfford(action.cost(), context) || !consumeCost(action.cost(), context)) {
            throw new InvocationInterruptedException(FailureReason.NOT_ENOUGH_RESOURCE,
                    "Unable to pay cost " + action.cost().kind());
        }
        context.invocation().markProducedGameplayEffect();
    }

    private void executeBranch(AbilityInvocation invocation,
                               AbilityActionContext context,
                               AbilityAction.BranchAction action,
                               boolean ignoreCosts) {
        List<AbilityAction> branch = evaluateCondition(action.condition(), context) ? action.ifTrue() : action.ifFalse();
        executeActions(invocation, context, branch, ignoreCosts);
    }

    private void executeForEachTarget(AbilityInvocation invocation,
                                      AbilityActionContext context,
                                      AbilityAction.ForEachTargetAction action,
                                      boolean ignoreCosts) {
        if (action.targets() != AbilityAction.TargetSet.SELECTED) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unsupported target set " + action.targets());
        }

        for (UUID targetId : context.targets().entityIds()) {
            LivingEntity target = resolveLivingEntity(context.casterData().getEntity(), targetId);
            if (target == null || !target.isAlive()) {
                continue;
            }
            AbilityActionContext targetContext = createContext(invocation, () -> Optional.of(target), context.targets());
            executeActions(invocation, targetContext, action.actions(), ignoreCosts);
        }
    }

    private void executeInstallReaction(AbilityInvocation invocation,
                                        AbilityActionContext context,
                                        AbilityAction.InstallReactionAction action) {
        AbilityReactionOwner owner = context.reactionOwner().orElseThrow(() ->
                new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                        "install_reaction requires a reaction owner"));
        AbilityReactionDefinition definition = invocation.definition().definition().getReaction(action.reaction());
        if (definition == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unknown reaction " + action.reaction());
        }
        reactionController.install(
                owner,
                invocation.ownerData().getEntity().getUUID(),
                invocation.casterData().getEntity().getUUID(),
                action.reaction(),
                definition
        );
        invocation.markProducedGameplayEffect();
    }

    private void executeRemoveReaction(AbilityInvocation invocation,
                                       AbilityActionContext context,
                                       AbilityAction.RemoveReactionAction action) {
        AbilityReactionOwner owner = context.reactionOwner().orElseThrow(() ->
                new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                        "remove_reaction requires a reaction owner"));
        reactionController.remove(owner, action.reaction());
        invocation.markProducedGameplayEffect();
    }

    private void executeSpawnProjectile(AbilityInvocation invocation,
                                        AbilityActionContext context,
                                        AbilityAction.SpawnProjectileAction action,
                                        boolean ignoreCosts) {
        AbilityDeliveryDefinition delivery = invocation.definition().definition().getDelivery(action.delivery());
        if (delivery == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unknown delivery " + action.delivery());
        }
        if (delivery.kind() != DeliveryKind.PROJECTILE || delivery.entityType() == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unsupported delivery kind " + delivery.kind());
        }
        if (!(invocation.casterData().getEntity().level() instanceof ServerLevel serverLevel)) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Projectile deliveries require a server level");
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(delivery.entityType());
        Entity entity = entityType != null ? entityType.create(serverLevel) : null;
        if (!(entity instanceof AbilityProjectileEntity projectile)) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Delivery %s did not create an AbilityProjectileEntity".formatted(action.delivery()));
        }

        LivingEntity caster = invocation.casterData().getEntity();
        LivingEntity target = resolveActionTarget(context, action.target()).orElse(null);
        Vec3 spawnPos = caster.getEyePosition().add(caster.getViewVector(1.0f).scale(0.25));
        Vec3 aimPoint = resolveProjectileAimPoint(context, target, spawnPos);
        Vec3 aimVector = aimPoint.subtract(spawnPos);
        if (aimVector.lengthSqr() <= 1.0e-6) {
            aimVector = caster.getViewVector(1.0f);
        }

        projectile.setOwner(caster);
        projectile.moveTo(spawnPos.x(), spawnPos.y(), spawnPos.z(), caster.getYRot(), caster.getXRot());
        projectile.setAbilityId(invocation.abilityId());
        applyProjectileRenderItem(projectile, delivery);
        projectile.setEventProvenance(AbilityEventProvenance.fromInvocation(invocation).asChildSource(projectile.getUUID()));
        projectile.setDoAirProc(delivery.onAirTickActivationId() != null);
        if (delivery.onAirTickActivationId() != null) {
            projectile.setAirProcTime(1);
        }
        projectile.setDoGroundProc(delivery.onGroundTickActivationId() != null);
        if (delivery.onGroundTickActivationId() != null) {
            projectile.setGroundProcTime(1);
        }
        projectile.shoot(
                aimVector.x(),
                aimVector.y(),
                aimVector.z(),
                Math.max(0.0f, (float) powerResolver.resolve(action.speed(), context)),
                Math.max(0.0f, (float) powerResolver.resolve(action.inaccuracy(), context))
        );
        serverLevel.addFreshEntity(projectile);

        AbilityReactionOwner deliveryOwner = deliveryController.registerProjectile(invocation, action.delivery(), delivery,
                projectile);
        invocation.markProducedGameplayEffect();

        if (!delivery.onSpawn().isEmpty()) {
            AbilityResolvedTargets deliveryTargets = new AbilityResolvedTargets(
                    target != null ? target.getUUID() : null,
                    target != null ? List.of(target.getUUID()) : List.of(),
                    aimPoint,
                    null,
                    projectile.getUUID()
            );
            executeActions(
                    invocation,
                    createContext(invocation, () -> Optional.ofNullable(target), deliveryTargets, deliveryOwner),
                    delivery.onSpawn(),
                    ignoreCosts
            );
        }
    }

    private void executeStartDelivery(AbilityInvocation invocation,
                                      AbilityActionContext context,
                                      AbilityAction.StartDeliveryAction action,
                                      boolean ignoreCosts) {
        AbilityDeliveryDefinition delivery = invocation.definition().definition().getDelivery(action.delivery());
        if (delivery == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unknown delivery " + action.delivery());
        }
        if (delivery.kind() == DeliveryKind.PROJECTILE) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "start_delivery does not support projectile deliveries");
        }
        if (!(invocation.casterData().getEntity().level() instanceof ServerLevel)) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Deliveries require a server level");
        }

        LivingEntity target = resolveActionTarget(context, action.target()).orElse(null);
        Vec3 anchorPoint = resolveDeliveryAnchorPoint(context, target);
        int delayTicks = delivery.delayTicks() != null ? resolveScalarTicks(delivery.delayTicks(), context) : 0;
        int durationTicks = delivery.durationTicks() != null ? resolveScalarTicks(delivery.durationTicks(), context) : 0;
        int tickIntervalTicks = delivery.tickIntervalTicks() != null
                ? resolveScalarTicks(delivery.tickIntervalTicks(), context)
                : 0;
        double radius = delivery.radius() != null ? Math.max(0.0, powerResolver.resolve(delivery.radius(), context)) : 0.0;

        switch (delivery.kind()) {
            case DELAYED_GROUND_BURST -> {
                if (delivery.onImpactActivationId() == null) {
                    throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                            "Delayed ground burst delivery requires an impact callback");
                }
            }
            case AREA_CLOUD -> {
                if (delivery.onGroundTickActivationId() == null || durationTicks <= 0 || tickIntervalTicks <= 0
                        || radius <= 0.0) {
                    throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                            "Area cloud delivery requires positive radius/duration/tick interval and a ground callback");
                }
            }
            case PROJECTILE -> throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unsupported delivery kind " + delivery.kind());
        }

        AbilityReactionOwner deliveryOwner = deliveryController.registerRuntime(
                invocation,
                action.delivery(),
                delivery,
                anchorPoint,
                radius,
                delayTicks,
                durationTicks,
                tickIntervalTicks
        );
        invocation.markProducedGameplayEffect();

        if (!delivery.onSpawn().isEmpty()) {
            AbilityResolvedTargets deliveryTargets = new AbilityResolvedTargets(
                    target != null ? target.getUUID() : null,
                    target != null ? List.of(target.getUUID()) : List.of(),
                    anchorPoint,
                    null,
                    deliveryOwner.stableSourceId()
            );
            executeActions(
                    invocation,
                    createContext(invocation, () -> Optional.ofNullable(target), deliveryTargets, deliveryOwner),
                    delivery.onSpawn(),
                    ignoreCosts
            );
        }
    }

    private void applyProjectileRenderItem(AbilityProjectileEntity projectile, AbilityDeliveryDefinition delivery) {
        ResourceLocation renderItemId = delivery.renderItem();
        if (renderItemId == null) {
            projectile.setItem(ItemStack.EMPTY);
            return;
        }

        if (!BuiltInRegistries.ITEM.containsKey(renderItemId)) {
            MKCore.LOGGER.warn("abilities2 projectile delivery referenced unknown render item {}", renderItemId);
            projectile.setItem(ItemStack.EMPTY);
            return;
        }

        projectile.setItem(new ItemStack(BuiltInRegistries.ITEM.get(renderItemId)));
    }

    private boolean evaluateCondition(AbilityConditionDefinition condition, AbilityActionContext context) {
        return switch (condition.type()) {
            case "always" -> true;
            case "event_has_actor" -> context.eventSnapshot() != null && context.eventSnapshot().actorEntityId() != null;
            case "event_has_target" -> context.eventSnapshot() != null && context.eventSnapshot().targetEntityId() != null;
            case "event_source_tag" -> {
                AbilityEventSnapshot eventSnapshot = context.eventSnapshot();
                ResourceLocation sourceAbilityId = eventSnapshot != null ? eventSnapshot.sourceAbilityId() : null;
                yield sourceAbilityId != null && tagMatcher.test(sourceAbilityId,
                        requiredResourceLocation(condition, "tag"));
            }
            case "event_has_payload" -> eventPayloadValue(context, requiredString(condition, "key")) != null;
            case "has_current_target" -> context.currentTarget().isPresent();
            case "has_primary_target" -> context.targets().primaryEntityId() != null;
            case "event_payload_bool" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.BOOL, condition.type());
                yield payload != null && payload.asBool(key) == optionalBoolean(condition, "value", true);
            }
            case "event_payload_int" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.INT, condition.type());
                yield payload != null && compareInts(payload.asInt(key), conditionOperator(condition),
                        requiredInt(condition, "value"));
            }
            case "event_payload_float" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.FLOAT, condition.type());
                yield payload != null && compareFloats(payload.asFloat(key), conditionOperator(condition),
                        requiredFloat(condition, "value"));
            }
            case "event_payload_string" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.STRING, condition.type());
                yield payload != null && compareStrings(payload.asString(key), conditionOperator(condition),
                        requiredString(condition, "value"));
            }
            case "event_payload_resource_location" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.RESOURCE_LOCATION,
                        condition.type());
                yield payload != null && compareResourceLocations(payload.asResourceLocation(key),
                        conditionOperator(condition), requiredResourceLocation(condition, "value"));
            }
            case "event_payload_tag" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.RESOURCE_LOCATION,
                        condition.type());
                yield payload != null && tagMatcher.test(payload.asResourceLocation(key),
                        requiredResourceLocation(condition, "tag"));
            }
            case "event_payload_entity_ref" -> {
                String key = requiredString(condition, "key");
                AbilityValue payload = eventPayloadValueOfKind(context, key, AbilityValueKind.ENTITY_REF,
                        condition.type());
                yield payload != null && compareEntityRefs(payload.asEntityRef(key), conditionOperator(condition),
                        requiredEntityRef(condition, "value"));
            }
            case "param_bool" -> context.getBoolParam(requiredString(condition, "parameter")) == optionalBoolean(condition, "value", true);
            case "var_bool" -> context.getBoolVar(requiredString(condition, "name")) == optionalBoolean(condition, "value", true);
            case "param_int" -> compareInts(
                    context.getIntParam(requiredString(condition, "parameter")),
                    conditionOperator(condition),
                    requiredInt(condition, "value")
            );
            case "var_int" -> compareInts(
                    context.getIntVar(requiredString(condition, "name")),
                    conditionOperator(condition),
                    requiredInt(condition, "value")
            );
            case "state_bool" -> {
                StateScope scope = requiredStateScope(condition);
                String key = requiredString(condition, "state_key");
                boolean expected = optionalBoolean(condition, "value", true);
                AbilityValue current = context.stateStore().getState(context.invocation(), scope, key);
                boolean actual = current instanceof AbilityValue.BoolValue boolValue && boolValue.value();
                yield actual == expected;
            }
            case "state_int" -> compareInts(
                    currentIntState(context.invocation(), requiredStateScope(condition), requiredString(condition, "state_key")),
                    conditionOperator(condition),
                    requiredInt(condition, "value")
            );
            case "param_float" -> compareFloats(
                    context.getFloatParam(requiredString(condition, "parameter")),
                    conditionOperator(condition),
                    requiredFloat(condition, "value")
            );
            case "var_float" -> compareFloats(
                    context.getFloatVar(requiredString(condition, "name")),
                    conditionOperator(condition),
                    requiredFloat(condition, "value")
            );
            case "state_float" -> compareFloats(
                    currentFloatState(context.invocation(), requiredStateScope(condition), requiredString(condition, "state_key")),
                    conditionOperator(condition),
                    requiredFloat(condition, "value")
            );
            default -> throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Unsupported condition type " + condition.type());
        };
    }

    private int applyBuffDuration(int baseDuration, AbilityActionContext context) {
        if (baseDuration <= 0) {
            return 0;
        }
        ResourceLocation buffDurationId = BuiltInRegistries.ATTRIBUTE.getKey(MKAttributes.BUFF_DURATION.value());
        double modifier = buffDurationId != null
                ? context.stats(StatCapturePolicy.ON_INVOCATION).attributes().getOrDefault(buffDurationId, 1.0)
                : 1.0;
        return Math.max(0, (int) Math.round(baseDuration * modifier));
    }

    private AbilityValue requireValueKind(@Nullable AbilityValue value, AbilityValueKind kind, String label) {
        if (value == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Missing state value for " + label);
        }
        if (value.kind() != kind) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "State value " + label + " expected " + kind + " but was " + value.kind());
        }
        return value;
    }

    private int currentIntState(AbilityInvocation invocation, StateScope scope, String key) {
        AbilityValue current = stateStore.getState(invocation, scope, key);
        if (current == null) {
            return 0;
        }
        if (current.kind() != AbilityValueKind.INT) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "State " + key + " is not an int");
        }
        return current.asInt(key);
    }

    private float currentFloatState(AbilityInvocation invocation, StateScope scope, String key) {
        AbilityValue current = stateStore.getState(invocation, scope, key);
        if (current == null) {
            return 0.0f;
        }
        if (current.kind() != AbilityValueKind.FLOAT) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "State " + key + " is not a float");
        }
        return current.asFloat(key);
    }

    private StateScope requiredStateScope(AbilityConditionDefinition condition) {
        String scopeName = requiredString(condition, "scope");
        try {
            return StateScope.valueOf(scopeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " field scope has unknown value " + scopeName);
        }
    }

    private ConditionOperator conditionOperator(AbilityConditionDefinition condition) {
        return ConditionOperator.fromSerializedName(optionalString(condition, "operator",
                optionalString(condition, "op", "eq")));
    }

    private boolean compareInts(int actual, ConditionOperator operator, int expected) {
        return switch (operator) {
            case EQ -> actual == expected;
            case NE -> actual != expected;
            case GT -> actual > expected;
            case GTE -> actual >= expected;
            case LT -> actual < expected;
            case LTE -> actual <= expected;
        };
    }

    private boolean compareFloats(float actual, ConditionOperator operator, float expected) {
        int comparison = Float.compare(actual, expected);
        return switch (operator) {
            case EQ -> comparison == 0;
            case NE -> comparison != 0;
            case GT -> comparison > 0;
            case GTE -> comparison >= 0;
            case LT -> comparison < 0;
            case LTE -> comparison <= 0;
        };
    }

    private boolean compareStrings(String actual, ConditionOperator operator, String expected) {
        return switch (operator) {
            case EQ -> actual.equals(expected);
            case NE -> !actual.equals(expected);
            case GT, GTE, LT, LTE -> throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "String conditions only support eq/ne operators");
        };
    }

    private boolean compareResourceLocations(ResourceLocation actual,
                                             ConditionOperator operator,
                                             ResourceLocation expected) {
        return switch (operator) {
            case EQ -> actual.equals(expected);
            case NE -> !actual.equals(expected);
            case GT, GTE, LT, LTE -> throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Resource location conditions only support eq/ne operators");
        };
    }

    private boolean compareEntityRefs(UUID actual, ConditionOperator operator, UUID expected) {
        return switch (operator) {
            case EQ -> actual.equals(expected);
            case NE -> !actual.equals(expected);
            case GT, GTE, LT, LTE -> throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Entity ref conditions only support eq/ne operators");
        };
    }

    private @Nullable AbilityValue eventPayloadValue(AbilityActionContext context, String key) {
        AbilityEventSnapshot eventSnapshot = context.eventSnapshot();
        return eventSnapshot != null ? eventSnapshot.payload().get(key) : null;
    }

    private @Nullable AbilityValue eventPayloadValueOfKind(AbilityActionContext context,
                                                           String key,
                                                           AbilityValueKind kind,
                                                           String conditionType) {
        AbilityValue payload = eventPayloadValue(context, key);
        if (payload == null) {
            return null;
        }
        if (payload.kind() != kind) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + conditionType + " expected payload " + key + " to be " + kind
                            + " but was " + payload.kind());
        }
        return payload;
    }

    private SimpleAbilityActionContext createContext(AbilityInvocation invocation,
                                                     Supplier<Optional<LivingEntity>> currentTargetSupplier) {
        return createContext(invocation, currentTargetSupplier, invocation.targets(), null);
    }

    private SimpleAbilityActionContext createContext(AbilityInvocation invocation,
                                                     Supplier<Optional<LivingEntity>> currentTargetSupplier,
                                                     AbilityResolvedTargets targets) {
        return createContext(invocation, currentTargetSupplier, targets, null);
    }

    private SimpleAbilityActionContext createContext(AbilityInvocation invocation,
                                                     Supplier<Optional<LivingEntity>> currentTargetSupplier,
                                                     AbilityResolvedTargets targets,
                                                     @Nullable AbilityReactionOwner reactionOwnerOverride) {
        return new SimpleAbilityActionContext(invocation, powerResolver, stateStore, targets, currentTargetSupplier,
                reactionOwnerOverride != null ? reactionOwnerOverride : effectiveReactionOwner(invocation));
    }

    private Vec3 resolveProjectileAimPoint(AbilityActionContext context,
                                           @Nullable LivingEntity target,
                                           Vec3 spawnPos) {
        if (target != null) {
            return new Vec3(target.getX(), target.getY(0.9D), target.getZ());
        }
        if (context.targets().point() != null) {
            return context.targets().point();
        }
        return spawnPos.add(context.casterData().getEntity().getViewVector(1.0f));
    }

    private Vec3 resolveDeliveryAnchorPoint(AbilityActionContext context,
                                            @Nullable LivingEntity target) {
        if (target != null) {
            return target.position();
        }
        if (context.targets().point() != null) {
            return context.targets().point();
        }
        return context.casterData().getEntity().position();
    }

    private Optional<LivingEntity> resolveActionTarget(AbilityActionContext context, AbilityAction.ActionTarget target) {
        Optional<LivingEntity> resolved = switch (target) {
            case SELF -> Optional.of(context.casterData().getEntity());
            case PRIMARY_ENTITY -> Optional.ofNullable(resolveLivingEntity(context.casterData().getEntity(), context.targets().primaryEntityId()));
            case TARGET -> context.currentTarget();
            case EVENT_TARGET -> Optional.ofNullable(resolveLivingEntity(context.casterData().getEntity(),
                    context.eventSnapshot() != null ? context.eventSnapshot().targetEntityId() : null));
        };
        return resolved.filter(LivingEntity::isAlive);
    }

    private @Nullable LivingEntity resolveLivingEntity(LivingEntity referenceEntity, @Nullable UUID entityId) {
        if (entityId == null) {
            return null;
        }
        if (!(referenceEntity.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(entityId);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private IMKEntityData targetData(LivingEntity entity) {
        return com.chaosbuffalo.mkcore.MKCore.getEntityDataOrThrow(entity);
    }

    private long currentGameTick(IMKEntityData entityData) {
        return entityData.getEntity().level().getGameTime();
    }

    private boolean hasValidExecutionTargets(AbilityInvocation invocation) {
        if (invocation.targets().entityIds().isEmpty()) {
            return true;
        }
        if (invocation.targets().primaryEntityId() != null) {
            LivingEntity primaryTarget = resolveLivingEntity(invocation.casterData().getEntity(), invocation.targets().primaryEntityId());
            return primaryTarget != null && primaryTarget.isAlive();
        }
        return invocation.targets().entityIds().stream()
                .map(targetId -> resolveLivingEntity(invocation.casterData().getEntity(), targetId))
                .anyMatch(target -> target != null && target.isAlive());
    }

    private void runChannelPulse(PendingChannel pendingChannel) {
        AbilityInvocation invocation = pendingChannel.invocation();
        AbilityResolvedTargets pulseTargets = resolveChannelPulseTargets(invocation, pendingChannel);
        pendingChannel.updateTargets(pulseTargets);
        executeEntryPoint(
                invocation,
                createContext(invocation, Optional::empty, pulseTargets),
                pendingChannel.behavior().tickEntryPoint(),
                pendingChannel.ignoreCosts()
        );
    }

    private AbilityResolvedTargets resolveChannelPulseTargets(AbilityInvocation invocation,
                                                              PendingChannel pendingChannel) {
        ActivationBehavior.ChannelBehavior behavior = pendingChannel.behavior();
        if (behavior.preserveInitialTargets()) {
            return revalidatePreservedTargets(invocation, pendingChannel.currentTargets());
        }

        AbilityTargetResolverDefinition targeting = behavior.tickTargeting() != null
                ? behavior.tickTargeting()
                : activationDefinition(invocation).targeting();
        try {
            return resolveActivationTargets(invocation.casterData(), null, invocation.eventSnapshot(), targeting);
        } catch (ActivationStartFailure failure) {
            if (failure.failureReason() == FailureReason.INVALID_TARGETS) {
                throw new InvocationInterruptedException(FailureReason.TARGET_LOST,
                        "Channel targets could not be resolved for invocation " + invocation.invocationId());
            }
            throw new InvocationInterruptedException(failure.failureReason(),
                    "Channel targets could not be resolved for invocation " + invocation.invocationId());
        }
    }

    private AbilityResolvedTargets revalidatePreservedTargets(AbilityInvocation invocation,
                                                              AbilityResolvedTargets targets) {
        if (targets.entityIds().isEmpty()) {
            return targets;
        }

        List<UUID> validTargets = new ArrayList<>(targets.entityIds().size());
        for (UUID entityId : targets.entityIds()) {
            LivingEntity target = resolveLivingEntity(invocation.casterData().getEntity(), entityId);
            if (target != null && target.isAlive()) {
                validTargets.add(entityId);
            }
        }
        if (validTargets.isEmpty()) {
            throw new InvocationInterruptedException(FailureReason.TARGET_LOST,
                    "Channel lost all preserved targets for invocation " + invocation.invocationId());
        }

        UUID primaryTarget = targets.primaryEntityId() != null && validTargets.contains(targets.primaryEntityId())
                ? targets.primaryEntityId()
                : null;
        return new AbilityResolvedTargets(primaryTarget, validTargets, targets.point(), targets.hitResult(),
                targets.deliveryId());
    }

    private String requiredString(AbilityConditionDefinition condition, String key) {
        var value = condition.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " requires string field " + key);
        }
        return value.getAsString();
    }

    private String optionalString(AbilityConditionDefinition condition, String key, String defaultValue) {
        var value = condition.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " field " + key + " must be string");
        }
        return value.getAsString();
    }

    private boolean optionalBoolean(AbilityConditionDefinition condition, String key, boolean defaultValue) {
        var value = condition.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " field " + key + " must be boolean");
        }
        return value.getAsBoolean();
    }

    private int requiredInt(AbilityConditionDefinition condition, String key) {
        var value = condition.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " requires numeric field " + key);
        }
        return value.getAsInt();
    }

    private float requiredFloat(AbilityConditionDefinition condition, String key) {
        var value = condition.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " requires numeric field " + key);
        }
        return value.getAsFloat();
    }

    private ResourceLocation requiredResourceLocation(AbilityConditionDefinition condition, String key) {
        ResourceLocation value = ResourceLocation.tryParse(requiredString(condition, key));
        if (value == null) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " field " + key + " must be a valid resource location");
        }
        return value;
    }

    private UUID requiredEntityRef(AbilityConditionDefinition condition, String key) {
        try {
            return UUID.fromString(requiredString(condition, key));
        } catch (IllegalArgumentException ignored) {
            throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                    "Condition " + condition.type() + " field " + key + " must be a valid UUID");
        }
    }

    private void emitInvocationStarted(AbilityInvocation invocation) {
        emitInvocationStarted(invocation, 0);
    }

    private void emitInvocationStarted(AbilityInvocation invocation, int castTicks) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("activation_kind", eventKeywordValue(activationKind(invocation)));
        emitEvent(AbilityEventType.INVOCATION_STARTED, invocation, invocation.targets().primaryEntityId(), payload);
        lifecycleListener.onInvocationStarted(invocation, castTicks);
    }

    private void emitInvocationCompleted(AbilityInvocation invocation, int castTicksSpent) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("activation_kind", eventKeywordValue(activationKind(invocation)));
        payload.put("completion_reason", eventKeywordValue("completed"));
        payload.put("cast_ticks_spent", new AbilityValue.IntValue(castTicksSpent));
        emitEvent(AbilityEventType.INVOCATION_COMPLETED, invocation, invocation.targets().primaryEntityId(), payload);
        lifecycleListener.onInvocationCompleted(invocation, castTicksSpent);
    }

    private void emitInvocationInterrupted(AbilityInvocation invocation,
                                           FailureReason failureReason,
                                           int castTicksSpent) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("activation_kind", eventKeywordValue(activationKind(invocation)));
        payload.put("completion_reason", eventKeywordValue(failureReason));
        payload.put("cast_ticks_spent", new AbilityValue.IntValue(castTicksSpent));
        emitEvent(AbilityEventType.INVOCATION_INTERRUPTED, invocation, invocation.targets().primaryEntityId(), payload);
        lifecycleListener.onInvocationInterrupted(invocation, failureReason, castTicksSpent);
    }

    private void finishInterruptedInvocation(AbilityInvocation invocation,
                                             FailureReason failureReason,
                                             int castTicksSpent) {
        refundActivationCostsIfEligible(invocation);
        emitInvocationInterrupted(invocation, failureReason, castTicksSpent);
        clearReactionOwnerOnInterruption(invocation);
    }

    private void clearReactionOwnerOnCompletion(AbilityInvocation invocation) {
        if (invocation.clearReactionOwnerOnCompletion()) {
            reactionController.clearOwner(effectiveReactionOwner(invocation));
        }
    }

    private void clearReactionOwnerOnInterruption(AbilityInvocation invocation) {
        if (invocation.clearReactionOwnerOnInterruption()) {
            reactionController.clearOwner(effectiveReactionOwner(invocation));
        }
    }

    private AbilityReactionOwner effectiveReactionOwner(AbilityInvocation invocation) {
        return invocation.reactionOwner() != null ? invocation.reactionOwner() : invocationReactionOwner(invocation);
    }

    private AbilityReactionOwner invocationReactionOwner(AbilityInvocation invocation) {
        return new AbilityReactionOwner(
                ReactionOwnerType.INVOCATION,
                invocation.invocationId(),
                invocation.sourceId(),
                invocation.abilityId()
        );
    }

    private void emitSpellHit(AbilityInvocation invocation,
                              LivingEntity target,
                              float amount,
                              MKDamageType damageType,
                              ResourceLocation damageSchool) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("damage_amount", new AbilityValue.FloatValue(amount));
        payload.put("damage_type", new AbilityValue.ResourceLocationValue(damageType.getId()));
        payload.put("damage_school", new AbilityValue.ResourceLocationValue(damageSchool));
        emitEvent(AbilityEventType.SPELL_HIT, invocation, target.getUUID(), payload);
    }

    private void emitEffectApplied(AbilityInvocation invocation,
                                   LivingEntity target,
                                   ResourceLocation effectId,
                                   int stackCount) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("effect_id", new AbilityValue.ResourceLocationValue(effectId));
        payload.put("stack_count", new AbilityValue.IntValue(stackCount));
        emitEvent(AbilityEventType.EFFECT_APPLIED, invocation, target.getUUID(), payload);
    }

    private void emitEvent(AbilityEventType eventType,
                           AbilityInvocation invocation,
                           @Nullable UUID targetEntityId,
                           Map<String, AbilityValue> payload) {
        eventEmitter.emit(new AbilityEventSnapshot(
                eventType,
                invocation.invocationId(),
                invocation.rootInvocationId(),
                invocation.chainDepth(),
                invocation.sourceId(),
                invocation.abilityId(),
                invocation.activationId(),
                invocation.casterData().getEntity().getUUID(),
                targetEntityId,
                payload
        ));
    }

    private ActivationKind activationKind(AbilityInvocation invocation) {
        return activationDefinition(invocation).kind();
    }

    private AbilityActivationDefinition activationDefinition(AbilityInvocation invocation) {
        AbilityActivationDefinition activation = invocation.definition().definition().getActivation(invocation.activationId());
        if (activation == null) {
            throw new IllegalStateException("Invocation %s references unknown activation %s"
                    .formatted(invocation.invocationId(), invocation.activationId()));
        }
        return activation;
    }

    private AbilityValue.ResourceLocationValue eventKeywordValue(Enum<?> value) {
        return eventKeywordValue(value.name().toLowerCase(Locale.ROOT));
    }

    private AbilityValue.ResourceLocationValue eventKeywordValue(String value) {
        return new AbilityValue.ResourceLocationValue(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, value));
    }

    private enum ConditionOperator {
        EQ,
        NE,
        GT,
        GTE,
        LT,
        LTE;

        private static ConditionOperator fromSerializedName(String serializedName) {
            return switch (serializedName.toLowerCase(Locale.ROOT)) {
                case "eq", "==" -> EQ;
                case "ne", "!=" -> NE;
                case "gt", ">" -> GT;
                case "gte", ">=" -> GTE;
                case "lt", "<" -> LT;
                case "lte", "<=" -> LTE;
                default -> throw new InvocationInterruptedException(FailureReason.UNSUPPORTED_FEATURE,
                        "Unsupported condition operator " + serializedName);
            };
        }
    }

    private static final class PendingCast {
        private final AbilityInvocation invocation;
        private final int totalTicks;
        private final boolean ignoreCosts;
        private final Vec3 startPosition;
        private int remainingTicks;

        private PendingCast(AbilityInvocation invocation, int castTicks, boolean ignoreCosts) {
            this.invocation = invocation;
            this.totalTicks = castTicks;
            this.remainingTicks = castTicks;
            this.ignoreCosts = ignoreCosts;
            this.startPosition = invocation.casterData().getEntity().position();
        }

        private PendingCast(AbilityInvocation invocation,
                            int totalTicks,
                            int remainingTicks,
                            boolean ignoreCosts,
                            Vec3 startPosition) {
            this.invocation = Objects.requireNonNull(invocation, "invocation");
            this.totalTicks = Math.max(0, totalTicks);
            this.remainingTicks = Math.max(0, remainingTicks);
            this.ignoreCosts = ignoreCosts;
            this.startPosition = Objects.requireNonNull(startPosition, "startPosition");
        }

        private static PendingCast restore(AbilityInvocation invocation,
                                           int remainingTicks,
                                           int castTicksSpent,
                                           boolean ignoreCosts) {
            return new PendingCast(invocation, castTicksSpent + Math.max(0, remainingTicks), remainingTicks,
                    ignoreCosts, invocation.casterData().getEntity().position());
        }

        private AbilityInvocation invocation() {
            return invocation;
        }

        private boolean ignoreCosts() {
            return ignoreCosts;
        }

        private Vec3 startPosition() {
            return startPosition;
        }

        private int remainingTicks() {
            return Math.max(0, remainingTicks);
        }

        private void tick() {
            remainingTicks--;
        }

        private boolean isComplete() {
            return remainingTicks <= 0;
        }

        private int castTicksSpent() {
            return Math.max(0, totalTicks - Math.max(remainingTicks, 0));
        }
    }

    private static final class PendingChannel {
        private final AbilityInvocation invocation;
        private final ActivationBehavior.ChannelBehavior behavior;
        private final boolean ignoreCosts;
        private final int castTicksSpent;
        private final Vec3 startPosition;
        private AbilityResolvedTargets currentTargets;
        private int remainingPulseTicks;

        private PendingChannel(AbilityInvocation invocation,
                               ActivationBehavior.ChannelBehavior behavior,
                               boolean ignoreCosts,
                               int castTicksSpent) {
            this.invocation = invocation;
            this.behavior = behavior;
            this.ignoreCosts = ignoreCosts;
            this.castTicksSpent = castTicksSpent;
            this.startPosition = invocation.casterData().getEntity().position();
            this.currentTargets = invocation.targets();
            this.remainingPulseTicks = behavior.tickIntervalTicks();
        }

        private PendingChannel(AbilityInvocation invocation,
                               ActivationBehavior.ChannelBehavior behavior,
                               boolean ignoreCosts,
                               int castTicksSpent,
                               Vec3 startPosition,
                               AbilityResolvedTargets currentTargets,
                               int remainingPulseTicks) {
            this.invocation = Objects.requireNonNull(invocation, "invocation");
            this.behavior = Objects.requireNonNull(behavior, "behavior");
            this.ignoreCosts = ignoreCosts;
            this.castTicksSpent = Math.max(0, castTicksSpent);
            this.startPosition = Objects.requireNonNull(startPosition, "startPosition");
            this.currentTargets = Objects.requireNonNull(currentTargets, "currentTargets");
            this.remainingPulseTicks = Math.max(0, remainingPulseTicks);
        }

        private static PendingChannel restore(AbilityInvocation invocation,
                                              ActivationBehavior.ChannelBehavior behavior,
                                              boolean ignoreCosts,
                                              int castTicksSpent,
                                              AbilityResolvedTargets currentTargets,
                                              int remainingPulseTicks) {
            return new PendingChannel(invocation, behavior, ignoreCosts, castTicksSpent,
                    invocation.casterData().getEntity().position(), currentTargets,
                    remainingPulseTicks);
        }

        private AbilityInvocation invocation() {
            return invocation;
        }

        private ActivationBehavior.ChannelBehavior behavior() {
            return behavior;
        }

        private boolean ignoreCosts() {
            return ignoreCosts;
        }

        private int castTicksSpent() {
            return castTicksSpent;
        }

        private Vec3 startPosition() {
            return startPosition;
        }

        private AbilityResolvedTargets currentTargets() {
            return currentTargets;
        }

        private void updateTargets(AbilityResolvedTargets currentTargets) {
            this.currentTargets = Objects.requireNonNull(currentTargets, "currentTargets");
        }

        private int remainingPulseTicks() {
            return Math.max(0, remainingPulseTicks);
        }

        private void tick() {
            remainingPulseTicks--;
        }

        private boolean readyForPulse() {
            return remainingPulseTicks <= 0;
        }

        private void resetPulseInterval() {
            remainingPulseTicks = behavior.tickIntervalTicks();
        }
    }

    private record PreparedActivation(
            PatchedAbilityDefinition definition,
            String activationId,
            AbilityActivationDefinition activation,
            AbilityResolvedTargets targets,
            UUID resolvedSourceId,
            AbilityStatSnapshot invocationStats
    ) {
    }

    private static final class ActivationStartFailure extends RuntimeException {
        private final FailureReason failureReason;

        private ActivationStartFailure(FailureReason failureReason) {
            this.failureReason = failureReason;
        }

        private FailureReason failureReason() {
            return failureReason;
        }
    }

    private static final class InvocationInterruptedException extends RuntimeException {
        private final FailureReason failureReason;

        private InvocationInterruptedException(FailureReason failureReason, String message) {
            super(message);
            this.failureReason = failureReason;
        }

        private FailureReason failureReason() {
            return failureReason;
        }
    }
}
