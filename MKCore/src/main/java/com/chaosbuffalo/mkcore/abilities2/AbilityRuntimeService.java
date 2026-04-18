package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ParticipantRelation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.runtime.*;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;

public class AbilityRuntimeService {
    private final AbilityDefinitionResolver definitionResolver;
    private final AbilityStateStore stateStore;
    private final AbilityPowerResolver powerResolver;
    private final AbilityReactionBus reactionBus;
    private final SimpleAbilityEngine engine;
    private final Map<AbilityReactionOwner, ReactionOwnerRuntime> reactionOwnerRuntime = new HashMap<>();
    private final Map<AbilityReactionOwner, Map<String, AbilityReactionHandle>> installedReactionHandles = new HashMap<>();
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
                new EngineReactionController()
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
    }

    private long currentGameTick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.overworld().getGameTime() : 0L;
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
        reactionBus.emit(new AbilityEventSnapshot(
                AbilityEventType.COOLDOWN_FINISHED,
                null,
                null,
                0,
                event.stableSourceId(),
                event.abilityId(),
                null,
                event.ownerEntityId(),
                event.ownerEntityId(),
                payload
        ));
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
            reactionBus.unregisterOwner(owner);
            installedReactionHandles.remove(owner);
            reactionOwnerRuntime.remove(owner);
        }
    }

    private @Nullable LivingEntity findEntity(UUID entityId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            var entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    private record ReactionOwnerRuntime(UUID ownerEntityId, UUID casterEntityId) {
    }
}
