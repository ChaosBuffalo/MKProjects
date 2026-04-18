package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ComparisonOp;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.EventParticipant;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.EventParticipantFilter;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.EventPayloadComparisonFilter;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.EventPayloadTagFilter;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.EventSourceTagFilter;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ParticipantRelation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityReactionDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;

public final class AbilityReactionBus {
    @FunctionalInterface
    public interface ParticipantRelationEvaluator {
        boolean test(UUID ownerEntityId, UUID participantEntityId, ParticipantRelation relation);
    }

    @FunctionalInterface
    public interface ReactionTriggerHandler {
        void trigger(RegisteredReaction reaction, AbilityEventSnapshot event);
    }

    private final Map<AbilityEventType, List<AbilityReactionHandle>> byType = new EnumMap<>(AbilityEventType.class);
    private final Map<AbilityReactionOwner, List<AbilityReactionHandle>> byOwner = new HashMap<>();
    private final Map<Long, RegisteredReaction> byHandle = new HashMap<>();
    private final ArrayDeque<AbilityEventSnapshot> eventQueue = new ArrayDeque<>();
    private final Map<Long, Long> internalCooldownExpiry = new HashMap<>();
    private final Map<Long, Set<UUID>> oncePerRootSeen = new HashMap<>();
    private final RandomSource random;
    private final LongSupplier currentTickSupplier;
    private final ReactionTriggerHandler triggerHandler;
    private final ParticipantRelationEvaluator participantRelationEvaluator;
    private final BiPredicate<ResourceLocation, ResourceLocation> tagMatcher;
    private final Function<AbilityReactionOwner, UUID> ownerEntityResolver;
    private boolean draining;
    private long nextHandle;

    public AbilityReactionBus(LongSupplier currentTickSupplier,
                              ReactionTriggerHandler triggerHandler,
                              ParticipantRelationEvaluator participantRelationEvaluator,
                              BiPredicate<ResourceLocation, ResourceLocation> tagMatcher) {
        this(RandomSource.create(), currentTickSupplier, triggerHandler, participantRelationEvaluator, tagMatcher,
                owner -> owner.ownerId());
    }

    public AbilityReactionBus(RandomSource random,
                              LongSupplier currentTickSupplier,
                              ReactionTriggerHandler triggerHandler,
                              ParticipantRelationEvaluator participantRelationEvaluator,
                              BiPredicate<ResourceLocation, ResourceLocation> tagMatcher,
                              Function<AbilityReactionOwner, UUID> ownerEntityResolver) {
        this.random = Objects.requireNonNull(random, "random");
        this.currentTickSupplier = Objects.requireNonNull(currentTickSupplier, "currentTickSupplier");
        this.triggerHandler = Objects.requireNonNull(triggerHandler, "triggerHandler");
        this.participantRelationEvaluator = Objects.requireNonNull(participantRelationEvaluator, "participantRelationEvaluator");
        this.tagMatcher = Objects.requireNonNull(tagMatcher, "tagMatcher");
        this.ownerEntityResolver = Objects.requireNonNull(ownerEntityResolver, "ownerEntityResolver");
        this.draining = false;
        this.nextHandle = 1L;
    }

    public AbilityReactionHandle register(AbilityReactionOwner owner, AbilityReactionDefinition definition) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(definition, "definition");

        long handleValue = nextHandle++;
        AbilityReactionHandle handle = new AbilityReactionHandle(handleValue);
        RegisteredReaction registered = new RegisteredReaction(handle, owner, definition);
        byHandle.put(handleValue, registered);
        byType.computeIfAbsent(definition.eventType(), ignored -> new ArrayList<>()).add(handle);
        byOwner.computeIfAbsent(owner, ignored -> new ArrayList<>()).add(handle);
        return handle;
    }

    public void unregister(AbilityReactionHandle handle) {
        Objects.requireNonNull(handle, "handle");
        RegisteredReaction reaction = byHandle.remove(handle.value());
        if (reaction == null) {
            return;
        }

        List<AbilityReactionHandle> typeHandles = byType.get(reaction.definition().eventType());
        if (typeHandles != null) {
            typeHandles.remove(handle);
            if (typeHandles.isEmpty()) {
                byType.remove(reaction.definition().eventType());
            }
        }

        List<AbilityReactionHandle> ownerHandles = byOwner.get(reaction.owner());
        if (ownerHandles != null) {
            ownerHandles.remove(handle);
            if (ownerHandles.isEmpty()) {
                byOwner.remove(reaction.owner());
            }
        }

        internalCooldownExpiry.remove(handle.value());
        oncePerRootSeen.remove(handle.value());
    }

    public void unregisterOwner(AbilityReactionOwner owner) {
        Objects.requireNonNull(owner, "owner");
        List<AbilityReactionHandle> handles = byOwner.get(owner);
        if (handles == null || handles.isEmpty()) {
            return;
        }
        List<AbilityReactionHandle> snapshot = List.copyOf(handles);
        snapshot.forEach(this::unregister);
    }

    public void emit(AbilityEventSnapshot event) {
        Objects.requireNonNull(event, "event");
        eventQueue.addLast(event);
        if (draining) {
            return;
        }

        draining = true;
        try {
            while (!eventQueue.isEmpty()) {
                drainEvent(eventQueue.removeFirst());
            }
        } finally {
            draining = false;
        }
    }

    private void drainEvent(AbilityEventSnapshot event) {
        List<AbilityReactionHandle> handles = byType.get(event.eventType());
        if (handles == null || handles.isEmpty()) {
            return;
        }

        List<AbilityReactionHandle> snapshot = List.copyOf(handles);
        for (AbilityReactionHandle handle : snapshot) {
            RegisteredReaction reaction = byHandle.get(handle.value());
            if (reaction == null) {
                continue;
            }
            if (!shouldTrigger(reaction, event)) {
                continue;
            }
            markTriggered(reaction, event);
            triggerHandler.trigger(reaction, event);
        }
    }

    private boolean shouldTrigger(RegisteredReaction reaction, AbilityEventSnapshot event) {
        AbilityReactionDefinition definition = reaction.definition();
        if (event.chainDepth() >= definition.maxChainDepth()) {
            return false;
        }
        if (definition.internalCooldownTicks() > 0) {
            long now = currentTickSupplier.getAsLong();
            long expiresAt = internalCooldownExpiry.getOrDefault(reaction.handle().value(), Long.MIN_VALUE);
            if (now < expiresAt) {
                return false;
            }
        }
        if (definition.oncePerRoot() && event.rootInvocationId() != null) {
            Set<UUID> seenRoots = oncePerRootSeen.get(reaction.handle().value());
            if (seenRoots != null && seenRoots.contains(event.rootInvocationId())) {
                return false;
            }
        }
        for (AbilityEventFilter filter : definition.filters()) {
            if (!matchesFilter(reaction.owner(), event, filter)) {
                return false;
            }
        }
        if (definition.chance() < 1.0f && random.nextFloat() > definition.chance()) {
            return false;
        }
        return true;
    }

    private void markTriggered(RegisteredReaction reaction, AbilityEventSnapshot event) {
        AbilityReactionDefinition definition = reaction.definition();
        if (definition.internalCooldownTicks() > 0) {
            long now = currentTickSupplier.getAsLong();
            internalCooldownExpiry.put(reaction.handle().value(), now + definition.internalCooldownTicks());
        }
        if (definition.oncePerRoot() && event.rootInvocationId() != null) {
            oncePerRootSeen.computeIfAbsent(reaction.handle().value(), ignored -> new HashSet<>())
                    .add(event.rootInvocationId());
        }
    }

    private boolean matchesFilter(AbilityReactionOwner owner, AbilityEventSnapshot event, AbilityEventFilter filter) {
        return switch (filter) {
            case EventParticipantFilter participantFilter -> matchesParticipantFilter(owner, event, participantFilter);
            case EventSourceTagFilter sourceTagFilter -> matchesSourceTagFilter(event, sourceTagFilter);
            case EventPayloadComparisonFilter comparisonFilter -> matchesPayloadComparisonFilter(event, comparisonFilter);
            case EventPayloadTagFilter payloadTagFilter -> matchesPayloadTagFilter(event, payloadTagFilter);
        };
    }

    private boolean matchesParticipantFilter(AbilityReactionOwner owner,
                                             AbilityEventSnapshot event,
                                             EventParticipantFilter filter) {
        UUID ownerEntityId = ownerEntityResolver.apply(owner);
        if (ownerEntityId == null) {
            throw new IllegalStateException("Reaction owner %s cannot evaluate participant filters without an entity"
                    .formatted(owner));
        }

        UUID participantId = switch (filter.participant()) {
            case ACTOR -> event.actorEntityId();
            case TARGET -> event.targetEntityId();
        };
        if (participantId == null) {
            return false;
        }
        return participantRelationEvaluator.test(ownerEntityId, participantId, filter.relation());
    }

    private boolean matchesSourceTagFilter(AbilityEventSnapshot event, EventSourceTagFilter filter) {
        ResourceLocation sourceAbilityId = event.sourceAbilityId();
        return sourceAbilityId != null && tagMatcher.test(sourceAbilityId, filter.tag());
    }

    private boolean matchesPayloadComparisonFilter(AbilityEventSnapshot event, EventPayloadComparisonFilter filter) {
        AbilityValue payloadValue = event.payload().get(filter.key());
        if (payloadValue == null) {
            return false;
        }
        AbilityValue expected = filter.value();
        return switch (filter.op()) {
            case EQ -> valuesEqual(payloadValue, expected);
            case NEQ -> !valuesEqual(payloadValue, expected);
            case GT -> compareNumeric(payloadValue, expected) > 0;
            case GTE -> compareNumeric(payloadValue, expected) >= 0;
            case LT -> compareNumeric(payloadValue, expected) < 0;
            case LTE -> compareNumeric(payloadValue, expected) <= 0;
        };
    }

    private boolean matchesPayloadTagFilter(AbilityEventSnapshot event, EventPayloadTagFilter filter) {
        AbilityValue payloadValue = event.payload().get(filter.key());
        if (!(payloadValue instanceof AbilityValue.ResourceLocationValue resourceLocationValue)) {
            return false;
        }
        return tagMatcher.test(resourceLocationValue.value(), filter.tag());
    }

    private boolean valuesEqual(AbilityValue left, AbilityValue right) {
        return left.kind() == right.kind() && left.equals(right);
    }

    private int compareNumeric(AbilityValue left, AbilityValue right) {
        if (left.kind() != right.kind()) {
            throw new IllegalStateException("Cannot compare payload values of different kinds: %s vs %s"
                    .formatted(left.kind(), right.kind()));
        }
        return switch (left.kind()) {
            case FLOAT -> Float.compare(left.asFloat("payload"), right.asFloat("payload"));
            case INT -> Integer.compare(left.asInt("payload"), right.asInt("payload"));
            case BOOL, ENTITY_REF, RESOURCE_LOCATION ->
                    throw new IllegalStateException("Cannot apply numeric comparison to " + left.kind());
        };
    }
}
