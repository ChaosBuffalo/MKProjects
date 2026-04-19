package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ParticipantRelation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
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

    private record ResolvedCombatSource(@Nullable AbilityEventProvenance provenance,
                                        @Nullable UUID fallbackSourceId,
                                        @Nullable ResourceLocation fallbackSourceAbilityId,
                                        @Nullable UUID actorEntityId,
                                        @Nullable ResourceLocation damageTypeId,
                                        @Nullable ResourceLocation damageSchoolId) {
    }
}
