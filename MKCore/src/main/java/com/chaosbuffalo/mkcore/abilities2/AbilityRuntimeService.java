package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ParticipantRelation;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityActivationDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCostDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityCooldownDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDeliveryDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationBehavior;
import com.chaosbuffalo.mkcore.abilities2.definition.ActivationKind;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.DeliveryKind;
import com.chaosbuffalo.mkcore.abilities2.runtime.*;
import com.chaosbuffalo.mkcore.core.AbilityDisplayEntry;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.events.PersonaEvent;
import com.chaosbuffalo.mkcore.network.Ability2CastPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class AbilityRuntimeService {
    private static final AbilityTargetResolverDefinition RESOLVED_TARGETING =
            new AbilityTargetResolverDefinition("resolved");

    private final AbilityDefinitionResolver definitionResolver;
    private final MemoryAbilityStateStore stateStore;
    private final AbilityPowerResolver powerResolver;
    private final AbilityReactionBus reactionBus;
    private final SimpleAbilityEngine engine;
    private final Map<AbilityReactionOwner, ReactionOwnerRuntime> reactionOwnerRuntime = new HashMap<>();
    private final Map<AbilityReactionOwner, Map<String, AbilityReactionHandle>> installedReactionHandles = new HashMap<>();
    private final Map<ToggleKey, ToggleRuntime> activeToggles = new LinkedHashMap<>();
    private final Map<PassiveKey, PassiveRuntime> activePassives = new LinkedHashMap<>();
    private final Map<UUID, DeliveryRuntime> activeDeliveries = new LinkedHashMap<>();
    private final Map<Integer, ClientCastState> clientCasts = new HashMap<>();
    private final Map<UUID, Persona> pendingPersonaRestores = new HashMap<>();
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

    public @Nullable AiActivationExecution resolveAiExecution(ResourceLocation abilityId,
                                                              @Nullable String requestedActivationId) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(abilityId);
        if (definition == null) {
            return null;
        }

        if (requestedActivationId != null) {
            AbilityActivationDefinition activation = definition.definition().getActivation(requestedActivationId);
            if (activation == null || activation.kind() != ActivationKind.AI) {
                return null;
            }
            return new AiActivationExecution(requestedActivationId, activation);
        }

        try {
            String activationId = resolveSingleActivationId(definition, ActivationKind.AI);
            if (activationId == null) {
                return null;
            }
            AbilityActivationDefinition activation = definition.definition().getActivation(activationId);
            return activation != null ? new AiActivationExecution(activationId, activation) : null;
        } catch (IllegalStateException e) {
            MKCore.LOGGER.debug("abilities2 AI execution for {} is ambiguous: {}", abilityId, e.getMessage());
            return null;
        }
    }

    public @Nullable FailureReason previewAiAbility(IMKEntityData ownerData,
                                                    IMKEntityData casterData,
                                                    AbilityReference ability,
                                                    @Nullable String requestedActivationId,
                                                    AbilityResolvedTargets forcedTargets) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(forcedTargets, "forcedTargets");

        AiActivationExecution execution = resolveAiExecution(ability.abilityId(), requestedActivationId);
        if (execution == null) {
            return resolveAiFailureReason(ability.abilityId(), requestedActivationId);
        }

        return engine.previewActivationFailure(new ActivationRequest(
                ownerData,
                casterData,
                ability,
                execution.activationId(),
                null,
                forcedTargets,
                null,
                false,
                false
        ));
    }

    public InvocationResult activateAiAbility(IMKEntityData ownerData,
                                              IMKEntityData casterData,
                                              AbilityReference ability,
                                              @Nullable String requestedActivationId,
                                              AbilityResolvedTargets forcedTargets) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(ability, "ability");
        Objects.requireNonNull(forcedTargets, "forcedTargets");

        AiActivationExecution execution = resolveAiExecution(ability.abilityId(), requestedActivationId);
        if (execution == null) {
            return InvocationResult.failed(resolveAiFailureReason(ability.abilityId(), requestedActivationId));
        }

        return engine.activate(new ActivationRequest(
                ownerData,
                casterData,
                ability,
                execution.activationId(),
                null,
                forcedTargets,
                null,
                false,
                false
        ));
    }

    public PersistedAbilityRuntimeState capturePersonaRuntime(Persona persona) {
        Objects.requireNonNull(persona, "persona");
        UUID ownerEntityId = persona.getPlayerData().getEntity().getUUID();
        PersistedAbilityRuntimeState stateSnapshot = stateStore.snapshotOwner(ownerEntityId, currentGameTick());
        List<PersistedAbilityRuntimeState.ToggleEntry> toggles = activeToggles.values().stream()
                .filter(runtime -> runtime.ownerEntityId().equals(ownerEntityId))
                .map(runtime -> snapshotToggle(runtime))
                .toList();
        List<PersistedAbilityRuntimeState.DeliveryEntry> deliveries = activeDeliveries.values().stream()
                .filter(runtime -> runtime.ownerEntityId().equals(ownerEntityId))
                .map(this::snapshotDelivery)
                .filter(Objects::nonNull)
                .toList();
        return new PersistedAbilityRuntimeState(
                stateSnapshot.cooldowns(),
                stateSnapshot.gcds(),
                stateSnapshot.states(),
                toggles,
                deliveries,
                engine.snapshotOwnedActivations(ownerEntityId)
        );
    }

    public boolean hasPendingActivation(IMKEntityData casterData) {
        Objects.requireNonNull(casterData, "casterData");
        if (casterData.isClientSide()) {
            return getClientCastState(casterData) != null;
        }
        return engine.hasPendingActivation(casterData);
    }

    public void interruptPendingActivations(IMKEntityData casterData) {
        Objects.requireNonNull(casterData, "casterData");
        engine.interruptPendingActivations(casterData);
    }

    public void startClientCast(Entity entity, ResourceLocation abilityId, int castTicks, int elapsedTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(abilityId, "abilityId");
        if (castTicks <= 0) {
            clearClientCastState(entity.getId());
            return;
        }
        clearClientCastState(entity.getId());
        int clampedElapsedTicks = Math.max(0, Math.min(elapsedTicks, castTicks));
        clientCasts.put(entity.getId(), new ClientCastState(abilityId, castTicks, entity.tickCount - clampedElapsedTicks));
        if (entity instanceof LivingEntity livingEntity) {
            Ability2VisualAbility visualAbility = createVisualAbility(abilityId);
            SoundEvent sound = visualAbility != null ? visualAbility.getCastingSoundEvent() : null;
            if (sound != null) {
                ClientCastAudio.start(livingEntity, sound, castTicks, abilityId);
            }
        }
    }

    public void stopClientCast(Entity entity, ResourceLocation abilityId) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(abilityId, "abilityId");
        ClientCastState existing = clientCasts.get(entity.getId());
        if (existing != null && existing.abilityId().equals(abilityId)) {
            clearClientCastState(entity.getId());
        }
    }

    public float getClientCastProgress(IMKEntityData entityData, float partialTicks) {
        Objects.requireNonNull(entityData, "entityData");
        ClientCastState state = getClientCastState(entityData);
        if (state == null || state.castTicks() <= 0) {
            return 0.0f;
        }
        float elapsedTicks = (entityData.getEntity().tickCount - state.startTick()) + partialTicks;
        return Math.min(Math.max(elapsedTicks / state.castTicks(), 0.0f), 1.0f);
    }

    public int getClientCastTicks(IMKEntityData entityData) {
        Objects.requireNonNull(entityData, "entityData");
        ClientCastState state = getClientCastState(entityData);
        return state != null ? state.castTicks() : 0;
    }

    public @Nullable ResourceLocation getClientCastAbilityId(IMKEntityData entityData) {
        Objects.requireNonNull(entityData, "entityData");
        ClientCastState state = getClientCastState(entityData);
        return state != null ? state.abilityId() : null;
    }

    public boolean canExecuteLoadoutAbility(AbilityGroupId groupId, ResourceLocation abilityId) {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(abilityId, "abilityId");
        return resolveLoadoutExecution(groupId, abilityId) != null;
    }

    public boolean isLoadoutDefinition(AbilityGroupId groupId, ResourceLocation abilityId) {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(abilityId, "abilityId");

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(abilityId);
        return definition != null && matchesLoadoutGroup(groupId, definition.definition().data().slotFamily());
    }

    public boolean canClientExecuteLoadoutAbility(IMKEntityData ownerData,
                                                  IMKEntityData casterData,
                                                  AbilityGroupId groupId,
                                                  AbilityReference ability,
                                                  @Nullable UUID sourceId) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(ability, "ability");

        LoadoutExecution execution = resolveLoadoutExecution(groupId, ability.abilityId());
        if (execution == null || getLoadoutCooldownTicks(ownerData, ability, sourceId) > 0) {
            return false;
        }
        if (execution.kind() == LoadoutExecutionKind.DIRECT && isCasterBusyForDirectActivation(casterData)) {
            return false;
        }

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(ability.abilityId());
        if (definition == null) {
            return false;
        }
        AbilityActivationDefinition activation = definition.definition().getActivation(execution.activationId());
        if (activation == null) {
            return false;
        }
        if (getLoadoutGcdTicks(ownerData, activation.gcdGroup()) > 0) {
            return false;
        }

        AbilityActionContext context = createLoadoutPreviewContext(ownerData, casterData, ability, sourceId,
                execution.activationId(), definition, Map.of());
        return activation.costs().stream().allMatch(cost -> canAffordLoadoutPreview(cost, context));
    }

    public int getLoadoutGcdTicks(IMKEntityData ownerData,
                                  AbilityGroupId groupId,
                                  AbilityReference ability) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(ability, "ability");

        LoadoutExecution execution = resolveLoadoutExecution(groupId, ability.abilityId());
        if (execution == null) {
            return 0;
        }

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(ability.abilityId());
        if (definition == null) {
            return 0;
        }

        AbilityActivationDefinition activation = definition.definition().getActivation(execution.activationId());
        return activation != null ? getLoadoutGcdTicks(ownerData, activation.gcdGroup()) : 0;
    }

    public float getLoadoutGcdPercent(IMKEntityData ownerData,
                                      AbilityGroupId groupId,
                                      AbilityReference ability,
                                      float partialTicks) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(ability, "ability");

        LoadoutExecution execution = resolveLoadoutExecution(groupId, ability.abilityId());
        if (execution == null) {
            return 0.0f;
        }

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(ability.abilityId());
        if (definition == null) {
            return 0.0f;
        }

        AbilityActivationDefinition activation = definition.definition().getActivation(execution.activationId());
        return activation != null ? getLoadoutGcdPercent(ownerData, activation.gcdGroup(), partialTicks) : 0.0f;
    }

    public int getLoadoutCooldownTicks(IMKEntityData ownerData,
                                       AbilityReference ability,
                                       @Nullable UUID sourceId) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(ability, "ability");

        ResourceLocation timerId = getLoadoutCooldownTimerId(ability, sourceId);
        return timerId != null ? ownerData.getStats().getTimer(timerId) : 0;
    }

    public float getLoadoutCooldownPercent(IMKEntityData ownerData,
                                           AbilityReference ability,
                                           @Nullable UUID sourceId,
                                           float partialTicks) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(ability, "ability");

        ResourceLocation timerId = getLoadoutCooldownTimerId(ability, sourceId);
        return timerId != null ? ownerData.getStats().getTimerPercent(timerId, partialTicks) : 0.0f;
    }

    public InvocationResult executeLoadoutAbility(IMKEntityData ownerData,
                                                  IMKEntityData casterData,
                                                  AbilityGroupId groupId,
                                                  ResourceLocation abilityId) {
        return executeLoadoutAbility(
                ownerData,
                casterData,
                groupId,
                new AbilityReference(abilityId, null),
                null
        );
    }

    public InvocationResult executeLoadoutAbility(IMKEntityData ownerData,
                                                  IMKEntityData casterData,
                                                  AbilityGroupId groupId,
                                                  AbilityReference ability,
                                                  @Nullable UUID sourceId) {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(ability, "ability");

        LoadoutExecution execution = resolveLoadoutExecution(groupId, ability.abilityId());
        if (execution == null) {
            return definitionResolver.resolvePatched(ability.abilityId()) != null
                    ? InvocationResult.failed(FailureReason.ACTIVATION_NOT_EXTERNALLY_CALLABLE)
                    : InvocationResult.failed(FailureReason.UNKNOWN_ABILITY);
        }

        return switch (execution.kind()) {
            case DIRECT -> {
                InvocationResult result = engine.activate(new ActivationRequest(
                        ownerData,
                        casterData,
                        ability,
                        execution.activationId(),
                        sourceId,
                        null,
                        null,
                        false,
                        false
                ));
                if (result.started()) {
                    syncLoadoutGcdTimer(ownerData, ability, execution.activationId());
                    syncLoadoutCooldownTimer(ownerData, casterData, ability, sourceId, execution.activationId(), Map.of());
                }
                yield result;
            }
            case TOGGLE -> requestToggle(ownerData, casterData, ability, sourceId);
        };
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
        if (isCasterBusyForDirectActivation(casterData)) {
            return InvocationResult.failed(FailureReason.BUSY);
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
            syncLoadoutGcdTimer(ownerData, ability, enableActivationId);
            syncLoadoutCooldownTimer(ownerData, casterData, ability, stableSourceId, enableActivationId,
                    runtime.grantParameterOverrides());
            if (runtime.hasAuraBehavior() && runtime.auraBehavior().pulseOnEnable()) {
                startAuraPulse(runtime, ownerData, casterData);
            }
        }
        return result;
    }

    @SubscribeEvent
    public void onPersonaActivated(PersonaEvent.PersonaActivated event) {
        MKPlayerData playerData = event.getPlayerData();
        if (playerData.isClientSide()) {
            return;
        }
        AbilityRuntimePersonaExtension extension = getRuntimeExtension(event.getPersona());
        if (extension != null) {
            extension.setCaptureLiveRuntimeOnSerialize(false);
        }
        pendingPersonaRestores.put(playerData.getEntity().getUUID(), event.getPersona());
    }

    @SubscribeEvent
    public void onPersonaDeactivated(PersonaEvent.PersonaDeactivated event) {
        MKPlayerData playerData = event.getPlayerData();
        if (playerData.isClientSide()) {
            return;
        }
        persistAndClearPersonaRuntime(event.getPersona(), playerData);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MKPlayerData playerData = MKCore.getPlayerOrThrow(event.getEntity());
        if (playerData.isClientSide()) {
            return;
        }
        persistAndClearPersonaRuntime(playerData.getPersonaManager().getActivePersona(), playerData);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        MKPlayerData oldData = MKCore.getPlayerOrThrow(event.getOriginal());
        if (oldData.isClientSide()) {
            return;
        }
        clearLiveRuntime(oldData, true, true);
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
        processPendingPersonaRestores();
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
        engine.interruptPendingActivations(MKCore.getEntityDataOrThrow(event.getEntity()));
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

    private void processPendingPersonaRestores() {
        if (pendingPersonaRestores.isEmpty()) {
            return;
        }
        List<Persona> restores = List.copyOf(pendingPersonaRestores.values());
        pendingPersonaRestores.clear();
        for (Persona persona : restores) {
            restorePersonaRuntime(persona);
        }
    }

    private void persistAndClearPersonaRuntime(Persona persona, MKPlayerData playerData) {
        pendingPersonaRestores.remove(playerData.getEntity().getUUID());
        AbilityRuntimePersonaExtension extension = getRuntimeExtension(persona);
        if (extension != null) {
            extension.setSnapshot(capturePersonaRuntime(persona));
            extension.setCaptureLiveRuntimeOnSerialize(false);
        }
        clearLiveRuntime(playerData, true, true);
    }

    private void restorePersonaRuntime(Persona persona) {
        AbilityRuntimePersonaExtension extension = getRuntimeExtension(persona);
        if (extension == null) {
            return;
        }

        MKPlayerData playerData = persona.getPlayerData();
        clearLiveRuntime(playerData, false, false);

        PersistedAbilityRuntimeState snapshot = extension.getSnapshot();
        if (!snapshot.isEmpty()) {
            stateStore.restoreOwner(playerData.getEntity().getUUID(), snapshot, currentGameTick());
            engine.restoreOwnedActivations(playerData, snapshot.pendingActivations(),
                    this::resolveEntityData);
            syncRestoredDirectCastStates(playerData, snapshot.pendingActivations());
            syncRestoredDirectCastVisuals(playerData, snapshot.pendingActivations());
            snapshot.toggles().forEach(toggle -> restoreToggle(playerData, toggle));
            snapshot.deliveries().forEach(delivery -> restoreDelivery(playerData, delivery));
        }
        extension.setCaptureLiveRuntimeOnSerialize(true);
    }

    private void clearLiveRuntime(MKPlayerData playerData, boolean clearPassives, boolean gracefulToggleDisable) {
        pendingPersonaRestores.remove(playerData.getEntity().getUUID());
        engine.interruptOwnedActivations(playerData.getEntity().getUUID(), FailureReason.INTERRUPTED);
        if (clearPassives) {
            clearPassivesForOwner(playerData.getEntity().getUUID());
        }
        clearTogglesForOwner(playerData.getEntity().getUUID(), gracefulToggleDisable);
        closeDeliveriesForOwner(playerData.getEntity().getUUID(), true);
        stateStore.clearOwner(playerData.getEntity().getUUID());
    }

    private void clearPassivesForOwner(UUID ownerEntityId) {
        for (PassiveRuntime runtime : List.copyOf(activePassives.values())) {
            if (!runtime.ownerEntityId().equals(ownerEntityId)) {
                continue;
            }
            requestPassiveTeardown(runtime);
            if (activePassives.containsKey(runtime.key())) {
                activePassives.remove(runtime.key());
                clearReactionOwner(runtime.owner());
            }
        }
    }

    private void clearTogglesForOwner(UUID ownerEntityId, boolean gracefulDisable) {
        for (ToggleRuntime runtime : List.copyOf(activeToggles.values())) {
            if (!runtime.ownerEntityId().equals(ownerEntityId)) {
                continue;
            }
            if (gracefulDisable) {
                requestToggleDisable(runtime);
            }
            if (activeToggles.containsKey(runtime.key())) {
                teardownToggle(runtime);
            }
        }
    }

    private void closeDeliveriesForOwner(UUID ownerEntityId, boolean discardEntity) {
        for (DeliveryRuntime runtime : List.copyOf(activeDeliveries.values())) {
            if (!runtime.ownerEntityId().equals(ownerEntityId)) {
                continue;
            }
            if (discardEntity) {
                Entity entity = runtime.trackedEntityId() != null ? findAnyEntity(runtime.trackedEntityId()) : null;
                if (entity != null && !entity.isRemoved()) {
                    entity.discard();
                }
            }
            closeDelivery(runtime.instanceId());
        }
    }

    private void restoreToggle(MKPlayerData ownerData, PersistedAbilityRuntimeState.ToggleEntry entry) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(entry.abilityId());
        if (definition == null || !ownerData.getAbilities().knowsAbility(entry.abilityId())) {
            return;
        }

        String enableActivationId;
        String disableActivationId;
        try {
            enableActivationId = resolveSingleActivationId(definition, ActivationKind.TOGGLE_ENABLE);
            disableActivationId = resolveSingleActivationId(definition, ActivationKind.TOGGLE_DISABLE);
        } catch (IllegalStateException e) {
            MKCore.LOGGER.debug("abilities2 toggle restore for {} is ambiguous: {}", entry.abilityId(), e.getMessage());
            return;
        }
        if (enableActivationId == null) {
            return;
        }

        AbilityActivationDefinition enableActivation = definition.definition().getActivation(enableActivationId);
        if (enableActivation == null) {
            return;
        }

        IMKEntityData casterData = entry.casterEntityId() != null ? resolveEntityData(entry.casterEntityId()) : null;
        if (casterData == null) {
            casterData = ownerData;
        }

        ToggleRuntime runtime = new ToggleRuntime(
                new ToggleKey(ownerData.getEntity().getUUID(), entry.abilityId(), entry.grantId(), entry.stableSourceId()),
                new AbilityReactionOwner(ReactionOwnerType.TOGGLE_STATE, ownerData.getEntity().getUUID(),
                        entry.stableSourceId(), entry.abilityId()),
                new AbilityReference(entry.abilityId(), entry.grantId()),
                entry.grantParameterOverrides(),
                ownerData.getEntity().getUUID(),
                casterData.getEntity().getUUID(),
                enableActivationId,
                disableActivationId,
                enableActivation.behavior() instanceof ActivationBehavior.AuraBehavior auraBehavior ? auraBehavior : null
        );

        InvocationResult result = engine.activateInternal(new InternalActivationRequest(
                ownerData,
                casterData,
                runtime.ability(),
                enableActivationId,
                entry.stableSourceId(),
                null,
                null,
                true,
                true,
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
            runtime.restoreNextPulse(currentGameTick(), entry.nextPulseDelayTicks());
        } else {
            MKCore.LOGGER.debug("abilities2 toggle restore for {} did not start: {}",
                    entry.abilityId(), result.failureReason());
        }
    }

    private PersistedAbilityRuntimeState.ToggleEntry snapshotToggle(ToggleRuntime runtime) {
        long currentGameTick = currentGameTick();
        long nextPulseDelay = runtime.hasAuraBehavior()
                ? Math.max(0L, runtime.nextPulseTick() - currentGameTick)
                : 0L;
        int persistedDelay = nextPulseDelay > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) nextPulseDelay;
        return new PersistedAbilityRuntimeState.ToggleEntry(
                runtime.ability().abilityId(),
                runtime.ability().grantId(),
                runtime.owner().stableSourceId(),
                runtime.casterEntityId(),
                runtime.grantParameterOverrides(),
                persistedDelay
        );
    }

    private @Nullable PersistedAbilityRuntimeState.DeliveryEntry snapshotDelivery(DeliveryRuntime runtime) {
        if (runtime.trackedEntityId() != null || runtime.point() == null) {
            return null;
        }
        return new PersistedAbilityRuntimeState.DeliveryEntry(
                runtime.ability().abilityId(),
                runtime.ability().grantId(),
                runtime.owner().stableSourceId(),
                runtime.casterEntityId(),
                runtime.grantParameterOverrides(),
                runtime.deliveryId(),
                runtime.kind(),
                runtime.dimension(),
                runtime.point(),
                runtime.radius(),
                runtime.delayTicksRemaining(),
                runtime.durationTicksRemaining(),
                runtime.tickIntervalTicks(),
                runtime.ticksUntilNextGroundTick(),
                runtime.callbackProvenance().withSourceId(runtime.owner().stableSourceId())
        );
    }

    private void restoreDelivery(MKPlayerData ownerData, PersistedAbilityRuntimeState.DeliveryEntry entry) {
        if (entry.kind() == DeliveryKind.PROJECTILE) {
            return;
        }

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(entry.abilityId());
        if (definition == null) {
            return;
        }

        AbilityDeliveryDefinition delivery = definition.definition().getDelivery(entry.deliveryId());
        if (delivery == null || delivery.kind() != entry.kind()) {
            return;
        }

        ServerLevel level = resolveServerLevel(entry.dimension());
        if (level == null) {
            return;
        }

        IMKEntityData casterData = entry.casterEntityId() != null ? resolveEntityData(entry.casterEntityId()) : null;
        if (casterData == null) {
            casterData = ownerData;
        }
        if (!ownerData.getEntity().isAlive() || ownerData.getEntity().isRemoved()
                || !casterData.getEntity().isAlive() || casterData.getEntity().isRemoved()) {
            return;
        }

        closeDelivery(entry.stableSourceId());

        AbilityReactionOwner owner = new AbilityReactionOwner(ReactionOwnerType.DELIVERY, entry.stableSourceId(),
                entry.stableSourceId(), entry.abilityId());
        reactionOwnerRuntime.put(owner, new ReactionOwnerRuntime(
                ownerData.getEntity().getUUID(),
                casterData.getEntity().getUUID()
        ));

        DeliveryRuntime runtime = new DeliveryRuntime(
                entry.stableSourceId(),
                null,
                level.dimension(),
                owner,
                new AbilityReference(entry.abilityId(), entry.grantId()),
                entry.grantParameterOverrides(),
                ownerData.getEntity().getUUID(),
                casterData.getEntity().getUUID(),
                entry.deliveryId(),
                entry.kind(),
                entry.callbackProvenance().withSourceId(entry.stableSourceId()),
                entry.point(),
                entry.radius(),
                entry.delayTicksRemaining(),
                entry.durationTicksRemaining(),
                entry.tickIntervalTicks(),
                delivery.onImpactActivationId(),
                delivery.onAirTickActivationId(),
                delivery.onGroundTickActivationId()
        );
        runtime.ticksUntilNextGroundTick(entry.ticksUntilNextGroundTick());
        activeDeliveries.put(runtime.instanceId(), runtime);
    }

    private @Nullable AbilityRuntimePersonaExtension getRuntimeExtension(Persona persona) {
        return persona.getExtension(AbilityRuntimePersonaExtension.class);
    }

    private boolean isCasterBusyForDirectActivation(IMKEntityData casterData) {
        return casterData.getEntity().isBlocking()
                || casterData.getAbilityExecutor().isCasting()
                || hasPendingActivation(casterData);
    }

    private @Nullable ClientCastState getClientCastState(IMKEntityData entityData) {
        if (!entityData.isClientSide()) {
            return null;
        }
        ClientCastState state = clientCasts.get(entityData.getEntity().getId());
        if (state == null) {
            return null;
        }
        if ((entityData.getEntity().tickCount - state.startTick()) >= state.castTicks()) {
            clearClientCastState(entityData.getEntity().getId());
            return null;
        }
        return state;
    }

    private void clearClientCastState(int entityId) {
        clientCasts.remove(entityId);
        ClientCastAudio.stop(entityId);
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
            syncLoadoutGcdTimer(ownerData, runtime.ability(), runtime.disableActivationId());
            syncLoadoutCooldownTimer(ownerData, casterData, runtime.ability(), runtime.owner().stableSourceId(),
                    runtime.disableActivationId(), runtime.grantParameterOverrides());
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

    private @Nullable LoadoutExecution resolveLoadoutExecution(AbilityGroupId groupId, ResourceLocation abilityId) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(abilityId);
        if (definition == null || !matchesLoadoutGroup(groupId, definition.definition().data().slotFamily())) {
            return null;
        }

        try {
            String toggleEnableActivationId = resolveSingleActivationId(definition, ActivationKind.TOGGLE_ENABLE);
            if (toggleEnableActivationId != null) {
                return new LoadoutExecution(LoadoutExecutionKind.TOGGLE, toggleEnableActivationId);
            }

            String manualActivationId = resolveSingleActivationId(definition, ActivationKind.MANUAL);
            if (manualActivationId != null) {
                return new LoadoutExecution(LoadoutExecutionKind.DIRECT, manualActivationId);
            }
        } catch (IllegalStateException e) {
            MKCore.LOGGER.debug("abilities2 loadout execution for {} is ambiguous: {}", abilityId, e.getMessage());
            return null;
        }

        return null;
    }

    private FailureReason resolveAiFailureReason(ResourceLocation abilityId, @Nullable String requestedActivationId) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(abilityId);
        if (definition == null) {
            return FailureReason.UNKNOWN_ABILITY;
        }
        if (requestedActivationId != null) {
            AbilityActivationDefinition activation = definition.definition().getActivation(requestedActivationId);
            return activation == null ? FailureReason.UNKNOWN_ACTIVATION : FailureReason.ACTIVATION_NOT_EXTERNALLY_CALLABLE;
        }
        try {
            return resolveSingleActivationId(definition, ActivationKind.AI) == null
                    ? FailureReason.ACTIVATION_NOT_EXTERNALLY_CALLABLE
                    : FailureReason.ACTIVATION_NOT_EXTERNALLY_CALLABLE;
        } catch (IllegalStateException e) {
            return FailureReason.ACTIVATION_NOT_EXTERNALLY_CALLABLE;
        }
    }

    private boolean matchesLoadoutGroup(AbilityGroupId groupId, ResourceLocation slotFamily) {
        var abilityType = AbilityDisplayEntry.resolveAbilityType(slotFamily);
        return abilityType != null && groupId.fitsAbilityType(abilityType);
    }

    private @Nullable ResourceLocation getLoadoutCooldownTimerId(AbilityReference ability, @Nullable UUID sourceId) {
        ResourceLocation abilityId = ability.abilityId();
        StringBuilder path = new StringBuilder("timer.abilities2/")
                .append(abilityId.getNamespace())
                .append("/")
                .append(abilityId.getPath());
        UUID scopeId = sourceId != null ? sourceId : ability.grantId();
        if (scopeId != null) {
            path.append("/source/").append(scopeId);
        }
        return ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, path.toString());
    }

    private @Nullable ResourceLocation getLoadoutGcdTimerId(@Nullable ResourceLocation gcdGroup) {
        if (gcdGroup == null) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(
                MKCore.MOD_ID,
                "timer.abilities2_gcd/" + gcdGroup.getNamespace() + "/" + gcdGroup.getPath()
        );
    }

    private void syncLoadoutCooldownTimer(IMKEntityData ownerData,
                                          IMKEntityData casterData,
                                          AbilityReference ability,
                                          @Nullable UUID sourceId,
                                          String activationId,
                                          Map<String, AbilityValue> grantParameterOverrides) {
        ResourceLocation timerId = getLoadoutCooldownTimerId(ability, sourceId);
        if (timerId == null) {
            return;
        }

        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(ability.abilityId());
        if (definition == null) {
            return;
        }
        AbilityActivationDefinition activation = definition.definition().getActivation(activationId);
        if (activation == null || activation.cooldowns().isEmpty()) {
            return;
        }

        AbilityActionContext context = createLoadoutPreviewContext(ownerData, casterData, ability, sourceId,
                activationId, definition, grantParameterOverrides);
        int maxDuration = activation.cooldowns().stream()
                .mapToInt(cooldown -> previewCooldownDuration(cooldown, context))
                .max()
                .orElse(0);
        if (maxDuration > 0) {
            ownerData.getStats().setTimer(timerId, maxDuration);
        }
    }

    private void syncLoadoutGcdTimer(IMKEntityData ownerData,
                                     AbilityReference ability,
                                     String activationId) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(ability.abilityId());
        if (definition == null) {
            return;
        }

        AbilityActivationDefinition activation = definition.definition().getActivation(activationId);
        if (activation == null || activation.gcdGroup() == null) {
            return;
        }

        ResourceLocation timerId = getLoadoutGcdTimerId(activation.gcdGroup());
        if (timerId != null) {
            ownerData.getStats().setTimer(timerId, GameConstants.GLOBAL_COOLDOWN_TICKS);
        }
    }

    private AbilityActionContext createLoadoutPreviewContext(IMKEntityData ownerData,
                                                             IMKEntityData casterData,
                                                             AbilityReference ability,
                                                             @Nullable UUID sourceId,
                                                             String activationId,
                                                             PatchedAbilityDefinition definition,
                                                             Map<String, AbilityValue> grantParameterOverrides) {
        UUID previewInvocationId = UUID.randomUUID();
        AbilityInvocation previewInvocation = new AbilityInvocation(
                previewInvocationId,
                previewInvocationId,
                0,
                null,
                ability.abilityId(),
                ability.grantId(),
                activationId,
                activationId,
                ActivationReason.DIRECT_REQUEST,
                ownerData,
                casterData,
                sourceId != null ? sourceId : casterData.getEntity().getUUID(),
                new AbilityResolvedTargets(null, List.of(), null, null, null),
                null,
                null,
                false,
                false,
                definition,
                grantParameterOverrides,
                powerResolver.captureInvocationStats(casterData),
                Map.of(),
                casterData.getEntity().getRandom()
        );
        return new SimpleAbilityActionContext(previewInvocation, powerResolver);
    }

    private int previewCooldownDuration(AbilityCooldownDefinition cooldown, AbilityActionContext context) {
        int baseDuration = Math.max(0, (int) Math.round(powerResolver.resolve(cooldown.duration(), context)));
        double modifier = 2.0 - context.stats(StatCapturePolicy.ON_INVOCATION).cooldownRate();
        return Math.max(0, (int) (modifier * baseDuration));
    }

    private int getLoadoutGcdTicks(IMKEntityData ownerData, @Nullable ResourceLocation gcdGroup) {
        ResourceLocation timerId = getLoadoutGcdTimerId(gcdGroup);
        return timerId != null ? ownerData.getStats().getTimer(timerId) : 0;
    }

    private float getLoadoutGcdPercent(IMKEntityData ownerData,
                                       @Nullable ResourceLocation gcdGroup,
                                       float partialTicks) {
        ResourceLocation timerId = getLoadoutGcdTimerId(gcdGroup);
        return timerId != null ? ownerData.getStats().getTimerPercent(timerId, partialTicks) : 0.0f;
    }

    private boolean canAffordLoadoutPreview(AbilityCostDefinition cost, AbilityActionContext context) {
        double amount = previewCostAmount(cost, context);
        return switch (cost.kind()) {
            case MANA -> context.ownerData().getStats().getMana() >= amount;
            case HEALTH -> context.ownerData().getEntity().getHealth() > amount;
            case CUSTOM_RESOURCE -> false;
        };
    }

    private double previewCostAmount(AbilityCostDefinition cost, AbilityActionContext context) {
        double amount = powerResolver.resolve(cost.amount(), context);
        if (cost.kind() == com.chaosbuffalo.mkcore.abilities2.definition.CostKind.MANA) {
            amount *= context.stats(StatCapturePolicy.ON_INVOCATION).manaCostMultiplier();
        }
        return Math.max(0.0, amount);
    }

    private void tickActiveDeliveries() {
        for (UUID deliveryInstanceId : List.copyOf(activeDeliveries.keySet())) {
            DeliveryRuntime runtime = activeDeliveries.get(deliveryInstanceId);
            if (runtime == null) {
                continue;
            }
            if (runtime.kind() == DeliveryKind.PROJECTILE) {
                Entity entity = runtime.trackedEntityId() != null ? findAnyEntity(runtime.trackedEntityId()) : null;
                if (entity instanceof AbilityProjectileEntity projectile && !projectile.isRemoved()) {
                    continue;
                }
                closeDelivery(runtime.instanceId());
                continue;
            }

            IMKEntityData ownerData = resolveEntityData(runtime.ownerEntityId());
            IMKEntityData casterData = resolveEntityData(runtime.casterEntityId());
            ServerLevel level = resolveServerLevel(runtime.dimension());
            if (ownerData == null || casterData == null || level == null
                    || ownerData.getEntity().level().dimension() != runtime.dimension()
                    || casterData.getEntity().level().dimension() != runtime.dimension()
                    || !ownerData.getEntity().isAlive() || ownerData.getEntity().isRemoved()
                    || !casterData.getEntity().isAlive() || casterData.getEntity().isRemoved()) {
                closeDelivery(runtime.instanceId());
                continue;
            }

            tickVirtualDelivery(runtime, ownerData, casterData, level);
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
            closeDelivery(runtime.instanceId());
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
            closeDelivery(runtime.instanceId());
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
            closeDelivery(runtime.instanceId());
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
            closeDelivery(runtime.instanceId());
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

    private void closeDelivery(UUID deliveryInstanceId) {
        DeliveryRuntime runtime = activeDeliveries.remove(deliveryInstanceId);
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
                projectile.getUUID(),
                projectile.level().dimension(),
                owner,
                new AbilityReference(invocation.abilityId(), invocation.abilityInstanceId()),
                invocation.grantParameterOverrides(),
                invocation.ownerData().getEntity().getUUID(),
                invocation.casterData().getEntity().getUUID(),
                deliveryId,
                definition.kind(),
                AbilityEventProvenance.fromInvocation(invocation).asChildSource(projectile.getUUID()),
                null,
                0.0,
                0,
                0,
                0,
                definition.onImpactActivationId(),
                definition.onAirTickActivationId(),
                definition.onGroundTickActivationId()
        ));
        return owner;
    }

    private AbilityReactionOwner registerRuntimeDelivery(AbilityInvocation invocation,
                                                         String deliveryId,
                                                         AbilityDeliveryDefinition definition,
                                                         Vec3 point,
                                                         double radius,
                                                         int delayTicks,
                                                         int durationTicks,
                                                         int tickIntervalTicks) {
        UUID runtimeId = UUID.randomUUID();
        AbilityReactionOwner owner = new AbilityReactionOwner(ReactionOwnerType.DELIVERY, runtimeId,
                runtimeId, invocation.abilityId());
        reactionOwnerRuntime.put(owner, new ReactionOwnerRuntime(
                invocation.ownerData().getEntity().getUUID(),
                invocation.casterData().getEntity().getUUID()
        ));
        activeDeliveries.put(runtimeId, new DeliveryRuntime(
                runtimeId,
                null,
                invocation.casterData().getEntity().level().dimension(),
                owner,
                new AbilityReference(invocation.abilityId(), invocation.abilityInstanceId()),
                invocation.grantParameterOverrides(),
                invocation.ownerData().getEntity().getUUID(),
                invocation.casterData().getEntity().getUUID(),
                deliveryId,
                definition.kind(),
                AbilityEventProvenance.fromInvocation(invocation).asChildSource(runtimeId),
                point,
                radius,
                delayTicks,
                durationTicks,
                tickIntervalTicks,
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

    private void tickVirtualDelivery(DeliveryRuntime runtime,
                                     IMKEntityData ownerData,
                                     IMKEntityData casterData,
                                     ServerLevel level) {
        switch (runtime.kind()) {
            case DELAYED_GROUND_BURST -> tickDelayedGroundBurst(runtime, ownerData, casterData, level);
            case AREA_CLOUD -> tickAreaCloud(runtime, ownerData, casterData, level);
            case PROJECTILE -> {
            }
        }
    }

    private void tickDelayedGroundBurst(DeliveryRuntime runtime,
                                        IMKEntityData ownerData,
                                        IMKEntityData casterData,
                                        ServerLevel level) {
        if (runtime.delayTicksRemaining() > 0) {
            runtime.delayTicksRemaining(runtime.delayTicksRemaining() - 1);
            return;
        }

        if (runtime.onImpactActivationId() != null) {
            InvocationResult callbackResult = startDeliveryCallback(
                    runtime,
                    ownerData,
                    casterData,
                    runtime.onImpactActivationId(),
                    areaDeliveryTargets(runtime, level)
            );
            if (!callbackResult.started()) {
                MKCore.LOGGER.debug("abilities2 delayed burst callback {} for {} did not start: {}",
                        runtime.onImpactActivationId(), runtime.ability().abilityId(), callbackResult.failureReason());
            }
        }
        closeDelivery(runtime.instanceId());
    }

    private void tickAreaCloud(DeliveryRuntime runtime,
                               IMKEntityData ownerData,
                               IMKEntityData casterData,
                               ServerLevel level) {
        if (runtime.delayTicksRemaining() > 0) {
            runtime.delayTicksRemaining(runtime.delayTicksRemaining() - 1);
            if (runtime.delayTicksRemaining() > 0) {
                return;
            }
            if (runtime.onImpactActivationId() != null) {
                InvocationResult callbackResult = startDeliveryCallback(
                        runtime,
                        ownerData,
                        casterData,
                        runtime.onImpactActivationId(),
                        areaDeliveryTargets(runtime, level)
                );
                if (!callbackResult.started()) {
                    MKCore.LOGGER.debug("abilities2 area cloud impact callback {} for {} did not start: {}",
                            runtime.onImpactActivationId(), runtime.ability().abilityId(), callbackResult.failureReason());
                }
            }
        }

        if (runtime.durationTicksRemaining() <= 0) {
            closeDelivery(runtime.instanceId());
            return;
        }

        runtime.durationTicksRemaining(runtime.durationTicksRemaining() - 1);
        runtime.ticksUntilNextGroundTick(runtime.ticksUntilNextGroundTick() - 1);
        if (runtime.ticksUntilNextGroundTick() <= 0) {
            if (runtime.onGroundTickActivationId() != null) {
                InvocationResult callbackResult = startDeliveryCallback(
                        runtime,
                        ownerData,
                        casterData,
                        runtime.onGroundTickActivationId(),
                        areaDeliveryTargets(runtime, level)
                );
                if (!callbackResult.started()) {
                    MKCore.LOGGER.debug("abilities2 area cloud ground callback {} for {} did not start: {}",
                            runtime.onGroundTickActivationId(), runtime.ability().abilityId(), callbackResult.failureReason());
                }
            }
            runtime.ticksUntilNextGroundTick(runtime.tickIntervalTicks());
        }

        if (runtime.durationTicksRemaining() <= 0) {
            closeDelivery(runtime.instanceId());
        }
    }

    private AbilityResolvedTargets areaDeliveryTargets(DeliveryRuntime runtime, ServerLevel level) {
        Vec3 point = runtime.point() != null ? runtime.point() : Vec3.ZERO;
        if (runtime.radius() <= 0.0) {
            return new AbilityResolvedTargets(null, List.of(), point, null, runtime.instanceId());
        }

        double radiusSqr = runtime.radius() * runtime.radius();
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(point, point).inflate(runtime.radius()),
                entity -> EntitySelector.NO_SPECTATORS.test(entity) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(entity)
        );
        entities.removeIf(entity -> entity.position().distanceToSqr(point) > radiusSqr);
        entities.sort(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(point)));

        List<UUID> entityIds = entities.stream().map(Entity::getUUID).toList();
        UUID primaryEntityId = entityIds.isEmpty() ? null : entityIds.getFirst();
        return new AbilityResolvedTargets(primaryEntityId, entityIds, point, null, runtime.instanceId());
    }

    private @Nullable ServerLevel resolveServerLevel(ResourceKey<Level> dimension) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getLevel(dimension) : null;
    }

    private @Nullable Ability2VisualAbility createVisualAbility(ResourceLocation abilityId) {
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(abilityId);
        if (definition == null) {
            return null;
        }
        return new Ability2VisualAbility(abilityId, definition.definition().data().presentation());
    }

    private void syncRestoredDirectCastStates(MKPlayerData ownerData,
                                              List<PersistedPendingAbilityActivation> pendingActivations) {
        for (PersistedPendingAbilityActivation activation : pendingActivations) {
            if (activation.type() != PersistedPendingAbilityActivation.PendingActivationType.CAST
                    || activation.remainingTicks() <= 0
                    || activation.invocation().reason() != ActivationReason.DIRECT_REQUEST) {
                continue;
            }
            LivingEntity caster = resolveRestoredCastCaster(ownerData, activation.invocation());
            if (caster == null) {
                continue;
            }
            int totalCastTicks = activation.castTicksSpent() + activation.remainingTicks();
            PacketHandler.sendToTrackingAndSelf(
                    Ability2CastPacket.start(caster, activation.invocation().abilityId(), totalCastTicks,
                            activation.castTicksSpent()),
                    caster
            );
        }
    }

    private void syncRestoredDirectCastVisuals(MKPlayerData ownerData,
                                               List<PersistedPendingAbilityActivation> pendingActivations) {
        for (PersistedPendingAbilityActivation activation : pendingActivations) {
            if (activation.type() != PersistedPendingAbilityActivation.PendingActivationType.CAST
                    || activation.remainingTicks() <= 0
                    || activation.invocation().reason() != ActivationReason.DIRECT_REQUEST) {
                continue;
            }
            Ability2VisualAbility visualAbility = createVisualAbility(activation.invocation().abilityId());
            if (visualAbility == null) {
                continue;
            }
            ownerData.getAnimationModule().restoreCast(visualAbility,
                    activation.castTicksSpent() + activation.remainingTicks(),
                    activation.castTicksSpent());
        }
    }

    private @Nullable LivingEntity resolveRestoredCastCaster(MKPlayerData ownerData,
                                                             PersistedPendingAbilityActivation.InvocationEntry invocation) {
        if (invocation.casterEntityId().equals(invocation.ownerEntityId())) {
            return ownerData.getEntity();
        }
        IMKEntityData casterData = resolveEntityData(invocation.casterEntityId());
        return casterData != null ? casterData.getEntity() : null;
    }

    private void syncDirectCastVisualStart(AbilityInvocation invocation, int castTicks) {
        if (castTicks <= 0 || invocation.reason() != ActivationReason.DIRECT_REQUEST) {
            return;
        }
        Ability2VisualAbility visualAbility = createVisualAbility(invocation.abilityId());
        if (visualAbility == null) {
            return;
        }
        invocation.casterData().getAnimationModule().startCast(visualAbility, castTicks);
    }

    private void syncDirectCastVisualCompletion(AbilityInvocation invocation) {
        if (invocation.reason() != ActivationReason.DIRECT_REQUEST) {
            return;
        }
        Ability2VisualAbility visualAbility = createVisualAbility(invocation.abilityId());
        if (visualAbility == null) {
            return;
        }
        invocation.casterData().getAnimationModule().endCast(visualAbility);
        SoundEvent completeSound = visualAbility.getSpellCompleteSoundEvent();
        if (completeSound != null && !invocation.casterData().isClientSide()) {
            SoundUtils.serverPlaySoundAtEntity(invocation.casterData().getEntity(), completeSound,
                    invocation.casterData().getEntity().getSoundSource());
        }
    }

    private void syncDirectCastVisualInterrupt(AbilityInvocation invocation) {
        if (invocation.reason() != ActivationReason.DIRECT_REQUEST) {
            return;
        }
        Ability2VisualAbility visualAbility = createVisualAbility(invocation.abilityId());
        if (visualAbility == null) {
            return;
        }
        invocation.casterData().getAnimationModule().interruptCast(visualAbility, CastInterruptReason.Other);
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

        @Override
        public AbilityReactionOwner registerRuntime(AbilityInvocation invocation,
                                                    String deliveryId,
                                                    AbilityDeliveryDefinition definition,
                                                    Vec3 point,
                                                    double radius,
                                                    int delayTicks,
                                                    int durationTicks,
                                                    int tickIntervalTicks) {
            return registerRuntimeDelivery(invocation, deliveryId, definition, point, radius, delayTicks,
                    durationTicks, tickIntervalTicks);
        }
    }

    private final class EngineLifecycleListener implements SimpleAbilityEngine.LifecycleListener {
        @Override
        public void onInvocationStarted(AbilityInvocation invocation, int castTicks) {
            syncCastState(invocation, castTicks, true);
            syncDirectCastVisualStart(invocation, castTicks);
        }

        @Override
        public void onInvocationCompleted(AbilityInvocation invocation, int castTicksSpent) {
            syncCastState(invocation, 0, false);
            syncDirectCastVisualCompletion(invocation);
            handleInvocationCompleted(invocation);
        }

        @Override
        public void onInvocationInterrupted(AbilityInvocation invocation,
                                            FailureReason failureReason,
                                            int castTicksSpent) {
            syncCastState(invocation, 0, false);
            syncDirectCastVisualInterrupt(invocation);
            handleInvocationInterrupted(invocation);
        }
    }

    private void syncCastState(AbilityInvocation invocation, int castTicks, boolean active) {
        if (invocation.reason() != ActivationReason.DIRECT_REQUEST || invocation.casterData().isClientSide()) {
            return;
        }
        if (castTicks <= 0 && active) {
            return;
        }
        Entity caster = invocation.casterData().getEntity();
        if (active) {
            PacketHandler.sendToTrackingAndSelf(
                    Ability2CastPacket.start(caster, invocation.abilityId(), castTicks),
                    caster
            );
        } else {
            PacketHandler.sendToTrackingAndSelf(
                    Ability2CastPacket.stop(caster, invocation.abilityId()),
                    caster
            );
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

    private static final class ClientCastAudio {
        private static final Map<Integer, com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting> ACTIVE_SOUNDS =
                new HashMap<>();

        private ClientCastAudio() {
        }

        private static void start(LivingEntity entity, SoundEvent sound, int castTicks, ResourceLocation abilityId) {
            stop(entity.getId());
            com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting movingSound =
                    new com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting(entity, sound, castTicks, abilityId);
            ACTIVE_SOUNDS.put(entity.getId(), movingSound);
            net.minecraft.client.Minecraft.getInstance().getSoundManager().play(movingSound);
        }

        private static void stop(int entityId) {
            com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting sound = ACTIVE_SOUNDS.remove(entityId);
            if (sound != null) {
                net.minecraft.client.Minecraft.getInstance().getSoundManager().stop(sound);
            }
        }
    }

    private record ReactionOwnerRuntime(UUID ownerEntityId, UUID casterEntityId) {
    }

    private enum LoadoutExecutionKind {
        DIRECT,
        TOGGLE
    }

    private record LoadoutExecution(LoadoutExecutionKind kind, String activationId) {
    }

    public record AiActivationExecution(String activationId, AbilityActivationDefinition activation) {
    }

    private record ToggleKey(UUID ownerEntityId,
                             ResourceLocation abilityId,
                             @Nullable UUID grantId,
                             UUID stableSourceId) {
    }

    private record ClientCastState(ResourceLocation abilityId, int castTicks, int startTick) {
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

        private void restoreNextPulse(long currentGameTick, int delayTicks) {
            nextPulseTick = auraBehavior != null ? currentGameTick + Math.max(0, delayTicks) : Long.MAX_VALUE;
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

    private static final class DeliveryRuntime {
        private final UUID instanceId;
        private final @Nullable UUID trackedEntityId;
        private final ResourceKey<Level> dimension;
        private final AbilityReactionOwner owner;
        private final AbilityReference ability;
        private final Map<String, AbilityValue> grantParameterOverrides;
        private final UUID ownerEntityId;
        private final UUID casterEntityId;
        private final String deliveryId;
        private final DeliveryKind kind;
        private final AbilityEventProvenance callbackProvenance;
        private final @Nullable Vec3 point;
        private final double radius;
        private final int tickIntervalTicks;
        private final @Nullable String onImpactActivationId;
        private final @Nullable String onAirTickActivationId;
        private final @Nullable String onGroundTickActivationId;
        private int delayTicksRemaining;
        private int durationTicksRemaining;
        private int ticksUntilNextGroundTick;

        private DeliveryRuntime(UUID instanceId,
                                @Nullable UUID trackedEntityId,
                                ResourceKey<Level> dimension,
                                AbilityReactionOwner owner,
                                AbilityReference ability,
                                Map<String, AbilityValue> grantParameterOverrides,
                                UUID ownerEntityId,
                                UUID casterEntityId,
                                String deliveryId,
                                DeliveryKind kind,
                                AbilityEventProvenance callbackProvenance,
                                @Nullable Vec3 point,
                                double radius,
                                int delayTicksRemaining,
                                int durationTicksRemaining,
                                int tickIntervalTicks,
                                @Nullable String onImpactActivationId,
                                @Nullable String onAirTickActivationId,
                                @Nullable String onGroundTickActivationId) {
            this.instanceId = Objects.requireNonNull(instanceId, "instanceId");
            this.trackedEntityId = trackedEntityId;
            this.dimension = Objects.requireNonNull(dimension, "dimension");
            this.owner = Objects.requireNonNull(owner, "owner");
            this.ability = Objects.requireNonNull(ability, "ability");
            this.grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(grantParameterOverrides));
            this.ownerEntityId = Objects.requireNonNull(ownerEntityId, "ownerEntityId");
            this.casterEntityId = Objects.requireNonNull(casterEntityId, "casterEntityId");
            if (deliveryId == null || deliveryId.isBlank()) {
                throw new IllegalArgumentException("Delivery runtime deliveryId must not be blank");
            }
            this.deliveryId = deliveryId;
            this.kind = Objects.requireNonNull(kind, "kind");
            this.callbackProvenance = Objects.requireNonNull(callbackProvenance, "callbackProvenance");
            this.point = point;
            this.radius = Math.max(0.0, radius);
            this.delayTicksRemaining = Math.max(0, delayTicksRemaining);
            this.durationTicksRemaining = Math.max(0, durationTicksRemaining);
            this.tickIntervalTicks = Math.max(0, tickIntervalTicks);
            this.ticksUntilNextGroundTick = Math.max(0, tickIntervalTicks);
            this.onImpactActivationId = onImpactActivationId;
            this.onAirTickActivationId = onAirTickActivationId;
            this.onGroundTickActivationId = onGroundTickActivationId;
        }

        private UUID instanceId() {
            return instanceId;
        }

        private @Nullable UUID trackedEntityId() {
            return trackedEntityId;
        }

        private ResourceKey<Level> dimension() {
            return dimension;
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

        private String deliveryId() {
            return deliveryId;
        }

        private DeliveryKind kind() {
            return kind;
        }

        private AbilityEventProvenance callbackProvenance() {
            return callbackProvenance;
        }

        private @Nullable Vec3 point() {
            return point;
        }

        private double radius() {
            return radius;
        }

        private int tickIntervalTicks() {
            return tickIntervalTicks;
        }

        private int delayTicksRemaining() {
            return delayTicksRemaining;
        }

        private void delayTicksRemaining(int delayTicksRemaining) {
            this.delayTicksRemaining = Math.max(0, delayTicksRemaining);
        }

        private int durationTicksRemaining() {
            return durationTicksRemaining;
        }

        private void durationTicksRemaining(int durationTicksRemaining) {
            this.durationTicksRemaining = Math.max(0, durationTicksRemaining);
        }

        private int ticksUntilNextGroundTick() {
            return ticksUntilNextGroundTick;
        }

        private void ticksUntilNextGroundTick(int ticksUntilNextGroundTick) {
            this.ticksUntilNextGroundTick = ticksUntilNextGroundTick;
        }

        private @Nullable String onImpactActivationId() {
            return onImpactActivationId;
        }

        private @Nullable String onAirTickActivationId() {
            return onAirTickActivationId;
        }

        private @Nullable String onGroundTickActivationId() {
            return onGroundTickActivationId;
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
