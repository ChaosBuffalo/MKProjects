package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ParticipantRelation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.runtime.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.*;

public class AbilityRuntimeService {
    private static final AbilityTargetResolverDefinition RESOLVED_TARGETING =
            new AbilityTargetResolverDefinition("resolved");

    private final AbilityDefinitionResolver definitionResolver;
    private final AbilityStateStore stateStore;
    private final AbilityPowerResolver powerResolver;
    private final AbilityReactionBus reactionBus;
    private final SimpleAbilityEngine engine;
    private final Map<AbilityReactionOwner, ReactionOwnerRuntime> reactionOwnerRuntime = new HashMap<>();
    private final Map<AbilityReactionOwner, Map<String, AbilityReactionHandle>> installedReactionHandles = new HashMap<>();
    private final Map<ToggleKey, ToggleRuntime> activeToggles = new LinkedHashMap<>();
    private final Map<PassiveKey, PassiveRuntime> activePassives = new LinkedHashMap<>();
    private final Map<UUID, DeliveryRuntime> activeDeliveries = new LinkedHashMap<>();
    private long lastStateStoreTick = Long.MIN_VALUE;

    public AbilityRuntimeService(AbilityDefinitionResolver definitionResolver) {
        this.definitionResolver = definitionResolver;
        this.stateStore = new MemoryAbilityStateStore();
        this.powerResolver = new MKAbilityPowerResolver();
        this.reactionBus = new AbilityReactionBus(
                RandomSource.create(),
                this::currentGameTick,
                this::triggerReaction,
                this::evaluateRelation,
                this::matchesTag,
                this::resolveOwnerEntityId
        );
        this.engine = new SimpleAbilityEngine(
                definitionResolver,
                powerResolver,
                stateStore,
                reactionBus::emit,
                new EngineReactionController(),
                new EngineDeliveryController(),
                new EngineLifecycleListener()
        );
    }

    public AbilityStateStore getStateStore() {
        return stateStore;
    }

    public AbilityPowerResolver getPowerResolver() {
        return powerResolver;
    }

    public AbilityReactionBus getReactionBus() {
        return reactionBus;
    }

    public AbilityEngine getEngine() {
        return engine;
    }

    public InvocationResult requestToggle(IMKEntityData ownerData,
                                          IMKEntityData casterData,
                                          AbilityReference ability,
                                          @Nullable UUID sourceId) {
        return requestToggle(ownerData, casterData, ability, Map.of(), sourceId);
    }

    public InvocationResult requestToggle(IMKEntityData ownerData,
                                          IMKEntityData casterData,
                                          GrantedAbility ability,
                                          @Nullable UUID sourceId) {
        Objects.requireNonNull(ability, "ability");
        return requestToggle(
                ownerData,
                casterData,
                new AbilityReference(ability.abilityId(), ability.grantId()),
                ability.parameterOverrides(),
                sourceId
        );
    }

    public void refreshPassives(IMKEntityData ownerData,
                                IMKEntityData casterData,
                                AbilityGrantSource grantSource,
                                Collection<GrantedAbility> desiredPassives) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(grantSource, "grantSource");
        Objects.requireNonNull(desiredPassives, "desiredPassives");

        UUID ownerEntityId = ownerData.getEntity().getUUID();
        LinkedHashMap<PassiveKey, PassiveRuntime> desiredByKey = new LinkedHashMap<>();
        for (GrantedAbility grantedAbility : desiredPassives) {
            if (grantedAbility == null) {
                continue;
            }
            if (!grantSource.equals(grantedAbility.source())) {
                MKCore.LOGGER.warn("abilities2 passive refresh for owner {} ignored mismatched grant source {} on {}",
                        ownerEntityId, grantedAbility.source(), grantedAbility.abilityId());
                continue;
            }
            PassiveRuntime runtime = createPassiveRuntime(ownerData, casterData, grantedAbility);
            if (runtime == null) {
                continue;
            }
            desiredByKey.put(runtime.key(), runtime);
        }

        for (PassiveRuntime runtime : List.copyOf(activePassives.values())) {
            if (!runtime.ownerEntityId().equals(ownerEntityId)
                    || !runtime.source().equals(grantSource)
                    || desiredByKey.containsKey(runtime.key())) {
                continue;
            }
            requestPassiveTeardown(runtime);
        }

        for (PassiveRuntime runtime : desiredByKey.values()) {
            if (activePassives.containsKey(runtime.key())
                    ) {
                continue;
            }
            requestPassiveSetup(runtime, ownerData, casterData);
        }
    }

    private InvocationResult requestToggle(IMKEntityData ownerData,
                                           IMKEntityData casterData,
                                           AbilityReference ability,
                                           Map<String, AbilityValue> grantParameterOverrides,
                                           @Nullable UUID sourceId) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(grantParameterOverrides, "grantParameterOverrides");

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(ability.abilityId());
        if (definition == null) {
            return InvocationResult.failed(FailureReason.UNKNOWN_ABILITY);
        }

        UUID stableSourceId = sourceId != null
                ? sourceId
                : ability.grantId() != null ? ability.grantId() : casterData.getEntity().getUUID();
        ToggleKey key = new ToggleKey(ownerData.getEntity().getUUID(), ability.abilityId(), ability.grantId(), stableSourceId);
        ToggleRuntime activeToggle = activeToggles.get(key);
        if (activeToggle != null) {
            return requestToggleDisable(activeToggle);
        }

        String enableActivationId;
        String disableActivationId;
        try {
            enableActivationId = resolveSingleActivationId(definition, ActivationKind.TOGGLE_ENABLE);
            disableActivationId = resolveSingleActivationId(definition, ActivationKind.TOGGLE_DISABLE);
        } catch (IllegalStateException e) {
            MKCore.LOGGER.debug("abilities2 toggle request for {} is ambiguous: {}", ability.abilityId(), e.getMessage());
            return InvocationResult.failed(FailureReason.UNSUPPORTED_FEATURE);
        }
        if (enableActivationId == null) {
            return InvocationResult.failed(FailureReason.UNKNOWN_ACTIVATION);
        }

        AbilityActivationDefinition enableActivation = definition.definition().getActivation(enableActivationId);
        if (enableActivation == null) {
            return InvocationResult.failed(FailureReason.UNKNOWN_ACTIVATION);
        }

        ActivationBehavior.AuraBehavior auraBehavior = enableActivation.behavior() instanceof ActivationBehavior.AuraBehavior aura
                ? aura
                : null;
        ToggleRuntime runtime = new ToggleRuntime(
                key,
                new AbilityReactionOwner(ReactionOwnerType.TOGGLE_STATE, ownerData.getEntity().getUUID(), stableSourceId,
                        ability.abilityId()),
                ability,
                grantParameterOverrides,
                ownerData.getEntity().getUUID(),
                casterData.getEntity().getUUID(),
                enableActivationId,
                disableActivationId,
                auraBehavior
        );

        InvocationResult result = engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                ability,
                enableActivationId,
                stableSourceId,
                null,
                null,
                false,
                false,
                ActivationReason.TOGGLE_LIFECYCLE,
                0,
                null,
                null,
                null,
                null,
                runtime.grantParameterOverrides(),
                runtime.owner(),
                false,
                true
        ));
        if (result.started()) {
            activeToggles.put(runtime.key(), runtime);
            runtime.scheduleNextPulse(currentGameTick());
            if (runtime.hasAuraBehavior() && runtime.auraBehavior().pulseOnEnable()) {
                startAuraPulse(runtime, ownerData, casterData);
            }
        }
        return result;
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()) {
            engine.tickEntity(MKCore.getEntityDataOrThrow(living));
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || serverLevel.dimension() != Level.OVERWORLD) {
            return;
        }
        long gameTick = serverLevel.getGameTime();
        if (gameTick == lastStateStoreTick) {
            return;
        }
        lastStateStoreTick = gameTick;
        stateStore.tick(gameTick, this::emitCooldownFinished);
        tickActiveDeliveries();
        tickActiveToggles(gameTick);
    }

    @SubscribeEvent
    public void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide() || event.getNewDamage() <= 0.0f) {
            return;
        }
        if (shouldQueueDamageInterrupt(event.getSource(), event.getNewDamage())) {
            engine.queueDamageInterrupt(MKCore.getEntityDataOrThrow(event.getEntity()), event.getNewDamage());
        }
        emitDamageTaken(event.getSource(), event.getEntity(), event.getNewDamage());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        emitKill(event.getSource(), event.getEntity());
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide()) {
            return;
        }
        emitProjectileHit(projectile, event.getRayTraceResult() instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity livingTarget ? livingTarget : null);
    }

    private long currentGameTick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.overworld().getGameTime() : 0L;
    }

    private InvocationResult requestToggleDisable(ToggleRuntime runtime) {
        IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
        IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
        if (ownerData == null || casterData == null) {
            teardownToggle(runtime);
            return InvocationResult.failed(FailureReason.INVALID_TARGETS);
        }
        if (runtime.disableActivationId() == null) {
            return InvocationResult.failed(FailureReason.UNKNOWN_ACTIVATION);
        }

        InvocationResult result = engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                runtime.ability(),
                runtime.disableActivationId(),
                runtime.owner().stableSourceId(),
                null,
                null,
                false,
                false,
                ActivationReason.TOGGLE_LIFECYCLE,
                0,
                null,
                null,
                null,
                null,
                runtime.grantParameterOverrides(),
                runtime.owner(),
                true,
                true
        ));
        if (result.started()) {
            activeToggles.remove(runtime.key());
        }
        return result;
    }

    private @Nullable PassiveRuntime createPassiveRuntime(IMKEntityData ownerData,
                                                          IMKEntityData casterData,
                                                          GrantedAbility grantedAbility) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(grantedAbility.abilityId());
        if (definition == null) {
            MKCore.LOGGER.debug("abilities2 passive refresh skipped unknown ability {}", grantedAbility.abilityId());
            return null;
        }

        String setupActivationId;
        String teardownActivationId;
        try {
            setupActivationId = resolveSingleActivationId(definition, ActivationKind.PASSIVE_SETUP);
            teardownActivationId = resolveSingleActivationId(definition, ActivationKind.PASSIVE_TEARDOWN);
        } catch (IllegalStateException e) {
            MKCore.LOGGER.debug("abilities2 passive refresh for {} is ambiguous: {}",
                    grantedAbility.abilityId(), e.getMessage());
            return null;
        }

        return new PassiveRuntime(
                new PassiveKey(ownerData.getEntity().getUUID(), grantedAbility.source(),
                        grantedAbility.abilityId(), grantedAbility.grantId()),
                new AbilityReactionOwner(ReactionOwnerType.ENTITY_PASSIVE, ownerData.getEntity().getUUID(),
                        grantedAbility.grantId(), grantedAbility.abilityId()),
                new AbilityReference(grantedAbility.abilityId(), grantedAbility.grantId()),
                grantedAbility.source(),
                grantedAbility.parameterOverrides(),
                ownerData.getEntity().getUUID(),
                casterData.getEntity().getUUID(),
                setupActivationId,
                teardownActivationId
        );
    }

    private void requestPassiveSetup(PassiveRuntime runtime,
                                     IMKEntityData ownerData,
                                     IMKEntityData casterData) {
        if (runtime.setupActivationId() == null) {
            activePassives.put(runtime.key(), runtime);
            return;
        }

        InvocationResult result = engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                runtime.ability(),
                runtime.setupActivationId(),
                runtime.owner().stableSourceId(),
                null,
                null,
                false,
                false,
                ActivationReason.PASSIVE_LIFECYCLE,
                0,
                null,
                null,
                null,
                null,
                runtime.grantParameterOverrides(),
                runtime.owner(),
                false,
                true
        ));
        if (result.started()) {
            activePassives.put(runtime.key(), runtime);
        } else {
            MKCore.LOGGER.debug("abilities2 passive setup for {} did not start: {}",
                    runtime.ability().abilityId(), result.failureReason());
        }
    }

    private void requestPassiveTeardown(PassiveRuntime runtime) {
        if (runtime.teardownActivationId() == null) {
            activePassives.remove(runtime.key());
            clearReactionOwner(runtime.owner());
            return;
        }

        IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
        IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
        if (ownerData == null || casterData == null) {
            activePassives.remove(runtime.key());
            clearReactionOwner(runtime.owner());
            return;
        }

        InvocationResult result = engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                runtime.ability(),
                runtime.teardownActivationId(),
                runtime.owner().stableSourceId(),
                null,
                null,
                false,
                false,
                ActivationReason.PASSIVE_LIFECYCLE,
                0,
                null,
                null,
                null,
                null,
                runtime.grantParameterOverrides(),
                runtime.owner(),
                true,
                false
        ));
        if (result.started()) {
            activePassives.remove(runtime.key());
        } else {
            MKCore.LOGGER.debug("abilities2 passive teardown for {} did not start: {}",
                    runtime.ability().abilityId(), result.failureReason());
        }
    }

    private void tickActiveToggles(long gameTick) {
        Iterator<ToggleRuntime> iterator = activeToggles.values().iterator();
        while (iterator.hasNext()) {
            ToggleRuntime runtime = iterator.next();
            IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
            IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
            if (ownerData == null || casterData == null
                    || !ownerData.getEntity().isAlive() || ownerData.getEntity().isRemoved()
                    || !casterData.getEntity().isAlive() || casterData.getEntity().isRemoved()) {
                iterator.remove();
                clearReactionOwner(runtime.owner());
                continue;
            }

            if (!runtime.hasAuraBehavior() || gameTick < runtime.nextPulseTick()) {
                continue;
            }

            startAuraPulse(runtime, ownerData, casterData);
            runtime.scheduleNextPulse(gameTick);
        }
    }

    private void tickActiveDeliveries() {
        Iterator<DeliveryRuntime> iterator = activeDeliveries.values().iterator();
        while (iterator.hasNext()) {
            DeliveryRuntime runtime = iterator.next();
            Entity entity = findAnyEntity(runtime.deliveryEntityId());
            if (entity instanceof AbilityProjectileEntity projectile && !projectile.isRemoved()) {
                continue;
            }
            iterator.remove();
            clearReactionOwner(runtime.owner());
        }
    }

    public boolean handleProjectileImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result) {
        DeliveryRuntime runtime = activeDeliveries.get(projectile.getUUID());
        if (runtime == null) {
            return false;
        }

        boolean persistAfterImpact = result.getType() == HitResult.Type.BLOCK
                && runtime.onGroundTickActivationId() != null;
        IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
        IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
        if (ownerData == null || casterData == null) {
            closeDelivery(runtime.deliveryEntityId());
            return true;
        }

        if (runtime.onImpactActivationId() != null) {
            InvocationResult callbackResult = startDeliveryCallback(
                    runtime,
                    ownerData,
                    casterData,
                    runtime.onImpactActivationId(),
                    impactTargets(projectile, result)
            );
            if (!callbackResult.started()) {
                MKCore.LOGGER.debug("abilities2 delivery impact callback {} for {} did not start: {}",
                        runtime.onImpactActivationId(), runtime.ability().abilityId(), callbackResult.failureReason());
            }
        }

        if (!persistAfterImpact) {
            closeDelivery(runtime.deliveryEntityId());
            return true;
        }
        return false;
    }

    public boolean handleProjectileAirTick(AbilityProjectileEntity projectile, LivingEntity caster) {
        DeliveryRuntime runtime = activeDeliveries.get(projectile.getUUID());
        if (runtime == null || runtime.onAirTickActivationId() == null) {
            return false;
        }

        IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
        IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
        if (ownerData == null || casterData == null) {
            closeDelivery(runtime.deliveryEntityId());
            return true;
        }

        InvocationResult callbackResult = startDeliveryCallback(
                runtime,
                ownerData,
                casterData,
                runtime.onAirTickActivationId(),
                periodicDeliveryTargets(projectile)
        );
        if (!callbackResult.started()) {
            MKCore.LOGGER.debug("abilities2 delivery air callback {} for {} did not start: {}",
                    runtime.onAirTickActivationId(), runtime.ability().abilityId(), callbackResult.failureReason());
        }
        return false;
    }

    public boolean handleProjectileGroundTick(AbilityProjectileEntity projectile, LivingEntity caster) {
        DeliveryRuntime runtime = activeDeliveries.get(projectile.getUUID());
        if (runtime == null || runtime.onGroundTickActivationId() == null) {
            return false;
        }

        IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
        IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
        if (ownerData == null || casterData == null) {
            closeDelivery(runtime.deliveryEntityId());
            return true;
        }

        InvocationResult callbackResult = startDeliveryCallback(
                runtime,
                ownerData,
                casterData,
                runtime.onGroundTickActivationId(),
                periodicDeliveryTargets(projectile)
        );
        if (!callbackResult.started()) {
            MKCore.LOGGER.debug("abilities2 delivery ground callback {} for {} did not start: {}",
                    runtime.onGroundTickActivationId(), runtime.ability().abilityId(), callbackResult.failureReason());
        }
        return false;
    }

    private void startAuraPulse(ToggleRuntime runtime, IMKEntityData ownerData, IMKEntityData casterData) {
        ActivationBehavior.AuraBehavior auraBehavior = runtime.auraBehavior();
        if (auraBehavior == null) {
            return;
        }
        InvocationResult result = engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                runtime.ability(),
                runtime.enableActivationId(),
                runtime.owner().stableSourceId(),
                null,
                null,
                true,
                true,
                ActivationReason.AURA_PULSE,
                0,
                null,
                null,
                auraBehavior.pulseEntryPoint(),
                auraBehavior.pulseTargeting(),
                runtime.grantParameterOverrides(),
                runtime.owner(),
                false,
                false
        ));
        if (!result.started()) {
            MKCore.LOGGER.debug("abilities2 aura pulse for {} did not start: {}",
                    runtime.ability().abilityId(), result.failureReason());
        }
    }

    private @Nullable String resolveSingleActivationId(PatchedAbilityDefinition definition, ActivationKind kind) {
        String match = null;
        for (Map.Entry<String, AbilityActivationDefinition> entry : definition.definition().activations().entrySet()) {
            if (entry.getValue().kind() != kind) {
                continue;
            }
            if (match != null) {
                throw new IllegalStateException("multiple activations with kind " + kind);
            }
            match = entry.getKey();
        }
        return match;
    }

    private void clearReactionOwner(AbilityReactionOwner owner) {
        reactionBus.unregisterOwner(owner);
        installedReactionHandles.remove(owner);
        reactionOwnerRuntime.remove(owner);
    }

    private void closeDelivery(UUID deliveryEntityId) {
        DeliveryRuntime runtime = activeDeliveries.remove(deliveryEntityId);
        if (runtime != null) {
            clearReactionOwner(runtime.owner());
        }
    }

    private AbilityReactionOwner registerProjectileDelivery(AbilityInvocation invocation,
                                                            String deliveryId,
                                                            AbilityDeliveryDefinition definition,
                                                            AbilityProjectileEntity projectile) {
        closeDelivery(projectile.getUUID());

        AbilityReactionOwner owner = new AbilityReactionOwner(ReactionOwnerType.DELIVERY, projectile.getUUID(),
                projectile.getUUID(), invocation.abilityId());
        reactionOwnerRuntime.put(owner, new ReactionOwnerRuntime(
                invocation.ownerData().getEntity().getUUID(),
                invocation.casterData().getEntity().getUUID()
        ));
        activeDeliveries.put(projectile.getUUID(), new DeliveryRuntime(
                projectile.getUUID(),
                owner,
                new AbilityReference(invocation.abilityId(), invocation.abilityInstanceId()),
                invocation.grantParameterOverrides(),
                invocation.ownerData().getEntity().getUUID(),
                invocation.casterData().getEntity().getUUID(),
                deliveryId,
                AbilityEventProvenance.fromInvocation(invocation).asChildSource(projectile.getUUID()),
                definition.onImpactActivationId(),
                definition.onAirTickActivationId(),
                definition.onGroundTickActivationId()
        ));
        return owner;
    }

    private InvocationResult startDeliveryCallback(DeliveryRuntime runtime,
                                                   IMKEntityData ownerData,
                                                   IMKEntityData casterData,
                                                   String activationId,
                                                   AbilityResolvedTargets forcedTargets) {
        return engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                runtime.ability(),
                activationId,
                runtime.owner().stableSourceId(),
                forcedTargets,
                null,
                false,
                false,
                ActivationReason.DELIVERY_CALLBACK,
                runtime.callbackProvenance().chainDepth(),
                runtime.callbackProvenance().invocationId(),
                runtime.callbackProvenance().rootInvocationId(),
                null,
                RESOLVED_TARGETING,
                runtime.grantParameterOverrides(),
                runtime.owner(),
                false,
                false
        ));
    }

    private AbilityResolvedTargets impactTargets(AbilityProjectileEntity projectile, HitResult result) {
        UUID targetEntityId = result instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity livingTarget
                ? livingTarget.getUUID()
                : null;
        return new AbilityResolvedTargets(
                targetEntityId,
                targetEntityId != null ? List.of(targetEntityId) : List.of(),
                result.getLocation(),
                result,
                projectile.getUUID()
        );
    }

    private AbilityResolvedTargets periodicDeliveryTargets(AbilityProjectileEntity projectile) {
        return new AbilityResolvedTargets(
                null,
                List.of(),
                projectile.position(),
                null,
                projectile.getUUID()
        );
    }

    private @Nullable IMKEntityData resolveEntityData(UUID entityId) {
        LivingEntity entity = findEntity(entityId);
        return entity != null ? MKCore.getEntityDataOrThrow(entity) : null;
    }

    private void teardownToggle(ToggleRuntime runtime) {
        activeToggles.remove(runtime.key());
        clearReactionOwner(runtime.owner());
    }

    private void triggerReaction(RegisteredReaction reaction, AbilityEventSnapshot event) {
        IMKEntityData ownerData = resolveOwnerData(reaction.owner());
        IMKEntityData casterData = resolveCasterData(reaction.owner());
        if (ownerData == null || casterData == null) {
            return;
        }
        InvocationResult result = engine.activateReaction(
                reaction,
                ownerData,
                casterData,
                reaction.owner().stableSourceId(),
                event
        );
        if (!result.started()) {
            MKCore.LOGGER.debug("abilities2 reaction {} did not start: {}", reaction.handle(), result.failureReason());
        }
    }

    private void handleInvocationCompleted(AbilityInvocation invocation) {
    }

    private void handleInvocationInterrupted(AbilityInvocation invocation) {
    }

    private boolean evaluateRelation(UUID ownerEntityId, UUID participantEntityId, ParticipantRelation relation) {
        if (relation == ParticipantRelation.IS_SELF) {
            return ownerEntityId.equals(participantEntityId);
        }

        LivingEntity owner = findEntity(ownerEntityId);
        LivingEntity participant = findEntity(participantEntityId);
        if (owner == null || participant == null) {
            return false;
        }

        return switch (relation) {
            case IS_SELF -> ownerEntityId.equals(participantEntityId);
            case IS_ALLY -> !ownerEntityId.equals(participantEntityId) && owner.isAlliedTo(participant);
            case IS_ENEMY -> !ownerEntityId.equals(participantEntityId) && !owner.isAlliedTo(participant);
        };
    }

    private @Nullable UUID resolveOwnerEntityId(AbilityReactionOwner owner) {
        ReactionOwnerRuntime runtime = reactionOwnerRuntime.get(owner);
        if (runtime != null) {
            return runtime.ownerEntityId();
        }
        return switch (owner.type()) {
            case ENTITY_PASSIVE, ACTIVE_EFFECT, ITEM_SOURCE, TOGGLE_STATE -> owner.ownerId();
            case INVOCATION, DELIVERY -> null;
        };
    }

    private @Nullable IMKEntityData resolveOwnerData(AbilityReactionOwner owner) {
        ReactionOwnerRuntime runtime = reactionOwnerRuntime.get(owner);
        UUID entityId = runtime != null ? runtime.ownerEntityId() : resolveOwnerEntityId(owner);
        LivingEntity entity = entityId != null ? findEntity(entityId) : null;
        return entity != null ? MKCore.getEntityDataOrThrow(entity) : null;
    }

    private @Nullable IMKEntityData resolveCasterData(AbilityReactionOwner owner) {
        ReactionOwnerRuntime runtime = reactionOwnerRuntime.get(owner);
        UUID entityId = runtime != null ? runtime.casterEntityId() : resolveOwnerEntityId(owner);
        LivingEntity entity = entityId != null ? findEntity(entityId) : null;
        return entity != null ? MKCore.getEntityDataOrThrow(entity) : null;
    }

    private boolean matchesTag(ResourceLocation value, ResourceLocation tag) {
        if (value.equals(tag)) {
            return true;
        }
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(value);
        return definition != null && definition.definition().data().tags().contains(tag);
    }

    private void emitCooldownFinished(AbilityStateStore.CooldownFinishedEvent event) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("cooldown_scope", new AbilityValue.StringValue(event.scope().name().toLowerCase(Locale.ROOT)));
        payload.put("cooldown_key", new AbilityValue.StringValue(event.key()));
        emitExternalEvent(
                AbilityEventType.COOLDOWN_FINISHED,
                null,
                event.stableSourceId(),
                normalizeAbilityId(event.abilityId()),
                null,
                event.ownerEntityId(),
                event.ownerEntityId(),
                payload
        );
    }

    public void emitSpellCrit(MKDamageSource source, LivingEntity target, float damageAmount) {
        if (target.level().isClientSide()) {
            return;
        }
        ResolvedCombatSource resolved = resolveCombatSource(source);
        Map<String, AbilityValue> payload = damagePayload(damageAmount, resolved);
        emitExternalEvent(
                AbilityEventType.SPELL_CRIT,
                resolved.provenance(),
                resolved.fallbackSourceId(),
                resolved.fallbackSourceAbilityId(),
                null,
                resolved.actorEntityId(),
                target.getUUID(),
                payload
        );
    }

    public void emitEffectRemoved(IMKEntityData targetData, MKActiveEffect effect) {
        if (!targetData.isServerSide()) {
            return;
        }
        LivingEntity sourceEntity = effect.getSourceEntity();
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("effect_id", new AbilityValue.ResourceLocationValue(effect.getEffect().getId()));
        payload.put("stack_count", new AbilityValue.IntValue(effect.getStackCount()));
        emitExternalEvent(
                AbilityEventType.EFFECT_REMOVED,
                effect.getEventProvenance(),
                effect.getSourceId(),
                normalizeAbilityId(effect.getAbilityId()),
                null,
                sourceEntity != null ? sourceEntity.getUUID() : null,
                targetData.getEntity().getUUID(),
                payload
        );
    }

    private void emitDamageTaken(DamageSource source, LivingEntity target, float damageAmount) {
        ResolvedCombatSource resolved = resolveCombatSource(source);
        emitExternalEvent(
                AbilityEventType.DAMAGE_TAKEN,
                resolved.provenance(),
                resolved.fallbackSourceId(),
                resolved.fallbackSourceAbilityId(),
                null,
                resolved.actorEntityId(),
                target.getUUID(),
                damagePayload(damageAmount, resolved)
        );
    }

    private boolean shouldQueueDamageInterrupt(DamageSource source, float damageAmount) {
        if (damageAmount <= 0.0f) {
            return false;
        }
        if (source instanceof MKDamageSource mkDamageSource
                && mkDamageSource.getOrigination() == MKDamageSource.Origination.DAMAGE_TYPE) {
            return false;
        }
        return source instanceof MKDamageSource
                || source.getEntity() instanceof LivingEntity
                || source.getDirectEntity() instanceof Projectile;
    }

    private void emitKill(DamageSource source, LivingEntity target) {
        ResolvedCombatSource resolved = resolveCombatSource(source);
        emitExternalEvent(
                AbilityEventType.KILL,
                resolved.provenance(),
                resolved.fallbackSourceId(),
                resolved.fallbackSourceAbilityId(),
                null,
                resolved.actorEntityId(),
                target.getUUID(),
                killPayload(resolved)
        );
    }

    private void emitProjectileHit(Projectile projectile, @Nullable LivingEntity target) {
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity living ? living : null;
        UUID actorEntityId = owner != null ? owner.getUUID() : null;
        AbilityEventProvenance provenance = projectile instanceof AbilityProjectileEntity abilityProjectile
                ? abilityProjectile.getEventProvenance()
                : null;
        ResourceLocation sourceAbilityId = projectile instanceof AbilityProjectileEntity abilityProjectile
                ? normalizeAbilityId(abilityProjectile.getAbilityId())
                : null;
        emitExternalEvent(
                AbilityEventType.PROJECTILE_HIT,
                provenance,
                projectile.getUUID(),
                sourceAbilityId,
                null,
                actorEntityId,
                target != null ? target.getUUID() : null,
                Map.of()
        );
    }

    private void emitExternalEvent(AbilityEventType eventType,
                                   @Nullable AbilityEventProvenance provenance,
                                   @Nullable UUID sourceId,
                                   @Nullable ResourceLocation sourceAbilityId,
                                   @Nullable String sourceActivationId,
                                   @Nullable UUID actorEntityId,
                                   @Nullable UUID targetEntityId,
                                   Map<String, AbilityValue> payload) {
        reactionBus.emit(new AbilityEventSnapshot(
                eventType,
                provenance != null ? provenance.invocationId() : null,
                provenance != null ? provenance.rootInvocationId() : null,
                provenance != null ? provenance.chainDepth() : 0,
                provenance != null && provenance.sourceId() != null ? provenance.sourceId() : sourceId,
                provenance != null && provenance.sourceAbilityId() != null ? provenance.sourceAbilityId() : sourceAbilityId,
                provenance != null && provenance.sourceActivationId() != null ? provenance.sourceActivationId() : sourceActivationId,
                actorEntityId,
                targetEntityId,
                payload
        ));
    }

    private Map<String, AbilityValue> damagePayload(float damageAmount, ResolvedCombatSource resolved) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        payload.put("damage_amount", new AbilityValue.FloatValue(damageAmount));
        if (resolved.damageTypeId() != null) {
            payload.put("damage_type", new AbilityValue.ResourceLocationValue(resolved.damageTypeId()));
        }
        if (resolved.damageSchoolId() != null) {
            payload.put("damage_school", new AbilityValue.ResourceLocationValue(resolved.damageSchoolId()));
        }
        return payload;
    }

    private Map<String, AbilityValue> killPayload(ResolvedCombatSource resolved) {
        Map<String, AbilityValue> payload = new LinkedHashMap<>();
        if (resolved.damageTypeId() != null) {
            payload.put("damage_type", new AbilityValue.ResourceLocationValue(resolved.damageTypeId()));
        }
        if (resolved.damageSchoolId() != null) {
            payload.put("damage_school", new AbilityValue.ResourceLocationValue(resolved.damageSchoolId()));
        }
        return payload;
    }

    private ResolvedCombatSource resolveCombatSource(DamageSource source) {
        UUID actorEntityId = source.getEntity() instanceof LivingEntity living ? living.getUUID() : null;
        UUID fallbackSourceId = actorEntityId;
        ResourceLocation fallbackSourceAbilityId = null;
        ResourceLocation damageTypeId = null;
        ResourceLocation damageSchoolId = null;
        AbilityEventProvenance provenance = null;
        if (source instanceof MKDamageSource mkDamageSource) {
            provenance = mkDamageSource.getEventProvenance();
            damageTypeId = mkDamageSource.getMKDamageType().getId();
            damageSchoolId = damageTypeId;
            if (mkDamageSource instanceof MKDamageSource.AbilityDamage abilityDamage) {
                fallbackSourceAbilityId = normalizeAbilityId(abilityDamage.getAbilityId());
            }
        }
        return new ResolvedCombatSource(provenance, fallbackSourceId, fallbackSourceAbilityId, actorEntityId,
                damageTypeId, damageSchoolId);
    }

    private @Nullable ResourceLocation normalizeAbilityId(@Nullable ResourceLocation abilityId) {
        if (abilityId == null || abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            return null;
        }
        return abilityId;
    }

    private final class EngineReactionController implements SimpleAbilityEngine.ReactionController {
        @Override
        public void install(AbilityReactionOwner owner,
                            UUID ownerEntityId,
                            UUID casterEntityId,
                            String reactionId,
                            AbilityReactionDefinition definition) {
            reactionOwnerRuntime.put(owner, new ReactionOwnerRuntime(ownerEntityId, casterEntityId));
            Map<String, AbilityReactionHandle> byReactionId = installedReactionHandles.computeIfAbsent(owner,
                    ignored -> new LinkedHashMap<>());
            AbilityReactionHandle previous = byReactionId.remove(reactionId);
            if (previous != null) {
                reactionBus.unregister(previous);
            }
            byReactionId.put(reactionId, reactionBus.register(owner, definition));
        }

        @Override
        public void remove(AbilityReactionOwner owner, String reactionId) {
            Map<String, AbilityReactionHandle> byReactionId = installedReactionHandles.get(owner);
            if (byReactionId == null) {
                return;
            }
            AbilityReactionHandle handle = byReactionId.remove(reactionId);
            if (handle != null) {
                reactionBus.unregister(handle);
            }
            if (byReactionId.isEmpty()) {
                installedReactionHandles.remove(owner);
                reactionOwnerRuntime.remove(owner);
            }
        }

        @Override
        public void clearOwner(AbilityReactionOwner owner) {
            clearReactionOwner(owner);
        }
    }

    private final class EngineDeliveryController implements SimpleAbilityEngine.DeliveryController {
        @Override
        public AbilityReactionOwner registerProjectile(AbilityInvocation invocation,
                                                       String deliveryId,
                                                       AbilityDeliveryDefinition definition,
                                                       AbilityProjectileEntity projectile) {
            return registerProjectileDelivery(invocation, deliveryId, definition, projectile);
        }
    }

    private final class EngineLifecycleListener implements SimpleAbilityEngine.LifecycleListener {
        @Override
        public void onInvocationCompleted(AbilityInvocation invocation, int castTicksSpent) {
            handleInvocationCompleted(invocation);
        }

        @Override
        public void onInvocationInterrupted(AbilityInvocation invocation,
                                            FailureReason failureReason,
                                            int castTicksSpent) {
            handleInvocationInterrupted(invocation);
        }
    }

    private @Nullable Entity findAnyEntity(UUID entityId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private @Nullable LivingEntity findEntity(UUID entityId) {
        Entity entity = findAnyEntity(entityId);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private record ReactionOwnerRuntime(UUID ownerEntityId, UUID casterEntityId) {
    }

    private record ToggleKey(UUID ownerEntityId,
                             ResourceLocation abilityId,
                             @Nullable UUID grantId,
                             UUID stableSourceId) {
    }

    private record PassiveKey(UUID ownerEntityId,
                              AbilityGrantSource source,
                              ResourceLocation abilityId,
                              UUID grantId) {
    }

    private static final class ToggleRuntime {
        private final ToggleKey key;
        private final AbilityReactionOwner owner;
        private final AbilityReference ability;
        private final Map<String, AbilityValue> grantParameterOverrides;
        private final UUID ownerEntityId;
        private final UUID casterEntityId;
        private final String enableActivationId;
        private final @Nullable String disableActivationId;
        private final @Nullable ActivationBehavior.AuraBehavior auraBehavior;
        private long nextPulseTick;

        private ToggleRuntime(ToggleKey key,
                              AbilityReactionOwner owner,
                              AbilityReference ability,
                              Map<String, AbilityValue> grantParameterOverrides,
                              UUID ownerEntityId,
                              UUID casterEntityId,
                              String enableActivationId,
                              @Nullable String disableActivationId,
                              @Nullable ActivationBehavior.AuraBehavior auraBehavior) {
            this.key = key;
            this.owner = owner;
            this.ability = ability;
            this.grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(grantParameterOverrides));
            this.ownerEntityId = ownerEntityId;
            this.casterEntityId = casterEntityId;
            this.enableActivationId = enableActivationId;
            this.disableActivationId = disableActivationId;
            this.auraBehavior = auraBehavior;
            this.nextPulseTick = Long.MAX_VALUE;
        }

        private ToggleKey key() {
            return key;
        }

        private AbilityReactionOwner owner() {
            return owner;
        }

        private AbilityReference ability() {
            return ability;
        }

        private Map<String, AbilityValue> grantParameterOverrides() {
            return grantParameterOverrides;
        }

        private UUID ownerEntityId() {
            return ownerEntityId;
        }

        private UUID casterEntityId() {
            return casterEntityId;
        }

        private String enableActivationId() {
            return enableActivationId;
        }

        private @Nullable String disableActivationId() {
            return disableActivationId;
        }

        private @Nullable ActivationBehavior.AuraBehavior auraBehavior() {
            return auraBehavior;
        }

        private boolean hasAuraBehavior() {
            return auraBehavior != null;
        }

        private long nextPulseTick() {
            return nextPulseTick;
        }

        private void scheduleNextPulse(long currentGameTick) {
            nextPulseTick = auraBehavior != null ? currentGameTick + auraBehavior.pulseIntervalTicks() : Long.MAX_VALUE;
        }
    }

    private static final class PassiveRuntime {
        private final PassiveKey key;
        private final AbilityReactionOwner owner;
        private final AbilityReference ability;
        private final AbilityGrantSource source;
        private final Map<String, AbilityValue> grantParameterOverrides;
        private final UUID ownerEntityId;
        private final UUID casterEntityId;
        private final @Nullable String setupActivationId;
        private final @Nullable String teardownActivationId;

        private PassiveRuntime(PassiveKey key,
                               AbilityReactionOwner owner,
                               AbilityReference ability,
                               AbilityGrantSource source,
                               Map<String, AbilityValue> grantParameterOverrides,
                               UUID ownerEntityId,
                               UUID casterEntityId,
                               @Nullable String setupActivationId,
                               @Nullable String teardownActivationId) {
            this.key = key;
            this.owner = owner;
            this.ability = ability;
            this.source = source;
            this.grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(grantParameterOverrides));
            this.ownerEntityId = ownerEntityId;
            this.casterEntityId = casterEntityId;
            this.setupActivationId = setupActivationId;
            this.teardownActivationId = teardownActivationId;
        }

        private PassiveKey key() {
            return key;
        }

        private AbilityReactionOwner owner() {
            return owner;
        }

        private AbilityReference ability() {
            return ability;
        }

        private AbilityGrantSource source() {
            return source;
        }

        private Map<String, AbilityValue> grantParameterOverrides() {
            return grantParameterOverrides;
        }

        private UUID ownerEntityId() {
            return ownerEntityId;
        }

        private UUID casterEntityId() {
            return casterEntityId;
        }

        private @Nullable String setupActivationId() {
            return setupActivationId;
        }

        private @Nullable String teardownActivationId() {
            return teardownActivationId;
        }
    }

    private record DeliveryRuntime(UUID deliveryEntityId,
                                   AbilityReactionOwner owner,
                                   AbilityReference ability,
                                   Map<String, AbilityValue> grantParameterOverrides,
                                   UUID ownerEntityId,
                                   UUID casterEntityId,
                                   String deliveryId,
                                   AbilityEventProvenance callbackProvenance,
                                   @Nullable String onImpactActivationId,
                                   @Nullable String onAirTickActivationId,
                                   @Nullable String onGroundTickActivationId) {
        private DeliveryRuntime {
            Objects.requireNonNull(deliveryEntityId, "deliveryEntityId");
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(ability, "ability");
            grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(grantParameterOverrides));
            Objects.requireNonNull(ownerEntityId, "ownerEntityId");
            Objects.requireNonNull(casterEntityId, "casterEntityId");
            if (deliveryId == null || deliveryId.isBlank()) {
                throw new IllegalArgumentException("Delivery runtime deliveryId must not be blank");
            }
            Objects.requireNonNull(callbackProvenance, "callbackProvenance");
        }
    }

    private record ResolvedCombatSource(@Nullable AbilityEventProvenance provenance,
                                        @Nullable UUID fallbackSourceId,
                                        @Nullable ResourceLocation fallbackSourceAbilityId,
                                        @Nullable UUID actorEntityId,
                                        @Nullable ResourceLocation damageTypeId,
                                        @Nullable ResourceLocation damageSchoolId) {
    }
}
