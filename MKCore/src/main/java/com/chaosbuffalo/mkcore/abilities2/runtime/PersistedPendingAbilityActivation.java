package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.codec.AbilityCodecs;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PersistedPendingAbilityActivation(
        PendingActivationType type,
        InvocationEntry invocation,
        boolean ignoreCosts,
        int remainingTicks,
        int castTicksSpent,
        @Nullable TargetsEntry currentTargets
) {
    public PersistedPendingAbilityActivation {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(invocation, "invocation");
        if (remainingTicks < 0) {
            throw new IllegalArgumentException("Persisted pending activation remainingTicks must be >= 0");
        }
        if (castTicksSpent < 0) {
            throw new IllegalArgumentException("Persisted pending activation castTicksSpent must be >= 0");
        }
        if (type == PendingActivationType.CHANNEL && currentTargets == null) {
            throw new IllegalArgumentException("Persisted pending channel activation requires currentTargets");
        }
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.put("invocation", invocation.serialize(provider));
        tag.putBoolean("ignore_costs", ignoreCosts);
        tag.putInt("remaining_ticks", remainingTicks);
        tag.putInt("cast_ticks_spent", castTicksSpent);
        if (currentTargets != null) {
            tag.put("current_targets", currentTargets.serialize());
        }
        return tag;
    }

    public static PersistedPendingAbilityActivation deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        PendingActivationType type = PendingActivationType.valueOf(tag.getString("type"));
        return new PersistedPendingAbilityActivation(
                type,
                InvocationEntry.deserialize(provider, tag.getCompound("invocation")),
                tag.getBoolean("ignore_costs"),
                tag.getInt("remaining_ticks"),
                tag.getInt("cast_ticks_spent"),
                tag.contains("current_targets", Tag.TAG_COMPOUND)
                        ? TargetsEntry.deserialize(tag.getCompound("current_targets"))
                        : null
        );
    }

    private static Tag encodeAbilityValue(HolderLookup.Provider provider, AbilityValue value) {
        return AbilityCodecs.ABILITY_VALUE_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), value)
                .getOrThrow();
    }

    private static AbilityValue decodeAbilityValue(HolderLookup.Provider provider, Tag valueTag) {
        return AbilityCodecs.ABILITY_VALUE_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), valueTag)
                .getOrThrow();
    }

    private static CompoundTag serializeAbilityValueMap(HolderLookup.Provider provider, Map<String, AbilityValue> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((key, value) -> tag.put(key, encodeAbilityValue(provider, value)));
        return tag;
    }

    private static Map<String, AbilityValue> deserializeAbilityValueMap(HolderLookup.Provider provider, CompoundTag tag) {
        Map<String, AbilityValue> values = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            values.put(key, decodeAbilityValue(provider, tag.get(key)));
        }
        return values;
    }

    private static void putOptionalResourceLocation(CompoundTag tag, String key, @Nullable ResourceLocation value) {
        if (value != null) {
            tag.putString(key, value.toString());
        }
    }

    private static @Nullable ResourceLocation getOptionalResourceLocation(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_STRING) ? ResourceLocation.parse(tag.getString(key)) : null;
    }

    private static void putOptionalUuid(CompoundTag tag, String key, @Nullable UUID value) {
        if (value != null) {
            tag.putUUID(key, value);
        }
    }

    private static @Nullable UUID getOptionalUuid(CompoundTag tag, String key) {
        return tag.hasUUID(key) ? tag.getUUID(key) : null;
    }

    private static void putUuidList(CompoundTag tag, String key, List<UUID> values) {
        ListTag list = new ListTag();
        values.forEach(value -> list.add(StringTag.valueOf(value.toString())));
        tag.put(key, list);
    }

    private static List<UUID> getUuidList(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        return list.stream()
                .map(Tag::getAsString)
                .map(UUID::fromString)
                .toList();
    }

    private static void putOptionalVec3(CompoundTag tag, String key, @Nullable Vec3 value) {
        if (value == null) {
            return;
        }
        CompoundTag vecTag = new CompoundTag();
        vecTag.putDouble("x", value.x());
        vecTag.putDouble("y", value.y());
        vecTag.putDouble("z", value.z());
        tag.put(key, vecTag);
    }

    private static @Nullable Vec3 getOptionalVec3(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag vecTag = tag.getCompound(key);
        return new Vec3(vecTag.getDouble("x"), vecTag.getDouble("y"), vecTag.getDouble("z"));
    }

    public enum PendingActivationType {
        CAST,
        CHANNEL
    }

    public record InvocationEntry(
            UUID invocationId,
            UUID rootInvocationId,
            int chainDepth,
            @Nullable UUID parentInvocationId,
            ResourceLocation abilityId,
            @Nullable UUID abilityInstanceId,
            String activationId,
            String entryPointId,
            ActivationReason reason,
            UUID ownerEntityId,
            UUID casterEntityId,
            UUID sourceId,
            TargetsEntry targets,
            @Nullable EventEntry eventSnapshot,
            @Nullable ReactionOwnerEntry reactionOwner,
            boolean clearReactionOwnerOnCompletion,
            boolean clearReactionOwnerOnInterruption,
            Map<String, AbilityValue> grantParameterOverrides,
            StatSnapshotEntry invocationStats,
            Map<String, AbilityValue> graphVars,
            boolean hasProducedGameplayEffect
    ) {
        public InvocationEntry {
            Objects.requireNonNull(invocationId, "invocationId");
            Objects.requireNonNull(rootInvocationId, "rootInvocationId");
            if (chainDepth < 0) {
                throw new IllegalArgumentException("Persisted invocation chainDepth must be >= 0");
            }
            Objects.requireNonNull(abilityId, "abilityId");
            if (activationId == null || activationId.isBlank()) {
                throw new IllegalArgumentException("Persisted invocation activationId must not be blank");
            }
            if (entryPointId == null || entryPointId.isBlank()) {
                throw new IllegalArgumentException("Persisted invocation entryPointId must not be blank");
            }
            Objects.requireNonNull(reason, "reason");
            Objects.requireNonNull(ownerEntityId, "ownerEntityId");
            Objects.requireNonNull(casterEntityId, "casterEntityId");
            Objects.requireNonNull(sourceId, "sourceId");
            Objects.requireNonNull(targets, "targets");
            grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(
                    grantParameterOverrides, "grantParameterOverrides")));
            Objects.requireNonNull(invocationStats, "invocationStats");
            graphVars = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(graphVars, "graphVars")));
        }

        public static InvocationEntry fromInvocation(AbilityInvocation invocation) {
            return new InvocationEntry(
                    invocation.invocationId(),
                    invocation.rootInvocationId(),
                    invocation.chainDepth(),
                    invocation.parentInvocationId(),
                    invocation.abilityId(),
                    invocation.abilityInstanceId(),
                    invocation.activationId(),
                    invocation.entryPointId(),
                    invocation.reason(),
                    invocation.ownerData().getEntity().getUUID(),
                    invocation.casterData().getEntity().getUUID(),
                    invocation.sourceId(),
                    TargetsEntry.fromTargets(invocation.targets()),
                    invocation.eventSnapshot() != null ? EventEntry.fromSnapshot(invocation.eventSnapshot()) : null,
                    invocation.reactionOwner() != null ? ReactionOwnerEntry.fromOwner(invocation.reactionOwner()) : null,
                    invocation.clearReactionOwnerOnCompletion(),
                    invocation.clearReactionOwnerOnInterruption(),
                    invocation.grantParameterOverrides(),
                    StatSnapshotEntry.fromSnapshot(invocation.invocationStats()),
                    invocation.graphVars(),
                    invocation.hasProducedGameplayEffect()
            );
        }

        private CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("invocation_id", invocationId);
            tag.putUUID("root_invocation_id", rootInvocationId);
            tag.putInt("chain_depth", chainDepth);
            putOptionalUuid(tag, "parent_invocation_id", parentInvocationId);
            tag.putString("ability_id", abilityId.toString());
            putOptionalUuid(tag, "ability_instance_id", abilityInstanceId);
            tag.putString("activation_id", activationId);
            tag.putString("entry_point_id", entryPointId);
            tag.putString("reason", reason.name());
            tag.putUUID("owner_entity_id", ownerEntityId);
            tag.putUUID("caster_entity_id", casterEntityId);
            tag.putUUID("source_id", sourceId);
            tag.put("targets", targets.serialize());
            if (eventSnapshot != null) {
                tag.put("event_snapshot", eventSnapshot.serialize(provider));
            }
            if (reactionOwner != null) {
                tag.put("reaction_owner", reactionOwner.serialize());
            }
            tag.putBoolean("clear_reaction_owner_on_completion", clearReactionOwnerOnCompletion);
            tag.putBoolean("clear_reaction_owner_on_interruption", clearReactionOwnerOnInterruption);
            if (!grantParameterOverrides.isEmpty()) {
                tag.put("grant_parameter_overrides", serializeAbilityValueMap(provider, grantParameterOverrides));
            }
            tag.put("invocation_stats", invocationStats.serialize());
            if (!graphVars.isEmpty()) {
                tag.put("graph_vars", serializeAbilityValueMap(provider, graphVars));
            }
            tag.putBoolean("has_produced_gameplay_effect", hasProducedGameplayEffect);
            return tag;
        }

        private static InvocationEntry deserialize(HolderLookup.Provider provider, CompoundTag tag) {
            return new InvocationEntry(
                    tag.getUUID("invocation_id"),
                    tag.getUUID("root_invocation_id"),
                    tag.getInt("chain_depth"),
                    getOptionalUuid(tag, "parent_invocation_id"),
                    ResourceLocation.parse(tag.getString("ability_id")),
                    getOptionalUuid(tag, "ability_instance_id"),
                    tag.getString("activation_id"),
                    tag.getString("entry_point_id"),
                    ActivationReason.valueOf(tag.getString("reason")),
                    tag.getUUID("owner_entity_id"),
                    tag.getUUID("caster_entity_id"),
                    tag.getUUID("source_id"),
                    TargetsEntry.deserialize(tag.getCompound("targets")),
                    tag.contains("event_snapshot", Tag.TAG_COMPOUND)
                            ? EventEntry.deserialize(provider, tag.getCompound("event_snapshot"))
                            : null,
                    tag.contains("reaction_owner", Tag.TAG_COMPOUND)
                            ? ReactionOwnerEntry.deserialize(tag.getCompound("reaction_owner"))
                            : null,
                    tag.getBoolean("clear_reaction_owner_on_completion"),
                    tag.getBoolean("clear_reaction_owner_on_interruption"),
                    deserializeAbilityValueMap(provider, tag.getCompound("grant_parameter_overrides")),
                    StatSnapshotEntry.deserialize(tag.getCompound("invocation_stats")),
                    deserializeAbilityValueMap(provider, tag.getCompound("graph_vars")),
                    tag.getBoolean("has_produced_gameplay_effect")
            );
        }
    }

    public record TargetsEntry(
            @Nullable UUID primaryEntityId,
            List<UUID> entityIds,
            @Nullable Vec3 point,
            @Nullable UUID deliveryId
    ) {
        public TargetsEntry {
            entityIds = List.copyOf(Objects.requireNonNull(entityIds, "entityIds"));
        }

        public static TargetsEntry fromTargets(AbilityResolvedTargets targets) {
            return new TargetsEntry(
                    targets.primaryEntityId(),
                    targets.entityIds(),
                    targets.point(),
                    targets.deliveryId()
            );
        }

        public AbilityResolvedTargets toTargets() {
            return new AbilityResolvedTargets(primaryEntityId, entityIds, point, null, deliveryId);
        }

        private CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            putOptionalUuid(tag, "primary_entity_id", primaryEntityId);
            putUuidList(tag, "entity_ids", entityIds);
            putOptionalVec3(tag, "point", point);
            putOptionalUuid(tag, "delivery_id", deliveryId);
            return tag;
        }

        private static TargetsEntry deserialize(CompoundTag tag) {
            return new TargetsEntry(
                    getOptionalUuid(tag, "primary_entity_id"),
                    getUuidList(tag, "entity_ids"),
                    getOptionalVec3(tag, "point"),
                    getOptionalUuid(tag, "delivery_id")
            );
        }
    }

    public record EventEntry(
            AbilityEventType eventType,
            @Nullable UUID invocationId,
            @Nullable UUID rootInvocationId,
            int chainDepth,
            @Nullable UUID sourceId,
            @Nullable ResourceLocation sourceAbilityId,
            @Nullable String sourceActivationId,
            @Nullable UUID actorEntityId,
            @Nullable UUID targetEntityId,
            Map<String, AbilityValue> payload
    ) {
        public EventEntry {
            Objects.requireNonNull(eventType, "eventType");
            if (chainDepth < 0) {
                throw new IllegalArgumentException("Persisted event chainDepth must be >= 0");
            }
            payload = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(payload, "payload")));
        }

        public static EventEntry fromSnapshot(AbilityEventSnapshot snapshot) {
            return new EventEntry(
                    snapshot.eventType(),
                    snapshot.invocationId(),
                    snapshot.rootInvocationId(),
                    snapshot.chainDepth(),
                    snapshot.sourceId(),
                    snapshot.sourceAbilityId(),
                    snapshot.sourceActivationId(),
                    snapshot.actorEntityId(),
                    snapshot.targetEntityId(),
                    snapshot.payload()
            );
        }

        public AbilityEventSnapshot toSnapshot() {
            return new AbilityEventSnapshot(
                    eventType,
                    invocationId,
                    rootInvocationId,
                    chainDepth,
                    sourceId,
                    sourceAbilityId,
                    sourceActivationId,
                    actorEntityId,
                    targetEntityId,
                    payload
            );
        }

        private CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putString("event_type", eventType.name());
            putOptionalUuid(tag, "invocation_id", invocationId);
            putOptionalUuid(tag, "root_invocation_id", rootInvocationId);
            tag.putInt("chain_depth", chainDepth);
            putOptionalUuid(tag, "source_id", sourceId);
            putOptionalResourceLocation(tag, "source_ability_id", sourceAbilityId);
            if (sourceActivationId != null) {
                tag.putString("source_activation_id", sourceActivationId);
            }
            putOptionalUuid(tag, "actor_entity_id", actorEntityId);
            putOptionalUuid(tag, "target_entity_id", targetEntityId);
            if (!payload.isEmpty()) {
                tag.put("payload", serializeAbilityValueMap(provider, payload));
            }
            return tag;
        }

        private static EventEntry deserialize(HolderLookup.Provider provider, CompoundTag tag) {
            return new EventEntry(
                    AbilityEventType.valueOf(tag.getString("event_type")),
                    getOptionalUuid(tag, "invocation_id"),
                    getOptionalUuid(tag, "root_invocation_id"),
                    tag.getInt("chain_depth"),
                    getOptionalUuid(tag, "source_id"),
                    getOptionalResourceLocation(tag, "source_ability_id"),
                    tag.contains("source_activation_id", Tag.TAG_STRING) ? tag.getString("source_activation_id") : null,
                    getOptionalUuid(tag, "actor_entity_id"),
                    getOptionalUuid(tag, "target_entity_id"),
                    deserializeAbilityValueMap(provider, tag.getCompound("payload"))
            );
        }
    }

    public record ReactionOwnerEntry(
            ReactionOwnerType type,
            UUID ownerId,
            @Nullable UUID stableSourceId,
            @Nullable ResourceLocation abilityId
    ) {
        public ReactionOwnerEntry {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(ownerId, "ownerId");
        }

        public static ReactionOwnerEntry fromOwner(AbilityReactionOwner owner) {
            return new ReactionOwnerEntry(owner.type(), owner.ownerId(), owner.stableSourceId(), owner.abilityId());
        }

        public AbilityReactionOwner toOwner() {
            return new AbilityReactionOwner(type, ownerId, stableSourceId, abilityId);
        }

        private CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", type.name());
            tag.putUUID("owner_id", ownerId);
            putOptionalUuid(tag, "stable_source_id", stableSourceId);
            putOptionalResourceLocation(tag, "ability_id", abilityId);
            return tag;
        }

        private static ReactionOwnerEntry deserialize(CompoundTag tag) {
            return new ReactionOwnerEntry(
                    ReactionOwnerType.valueOf(tag.getString("type")),
                    tag.getUUID("owner_id"),
                    getOptionalUuid(tag, "stable_source_id"),
                    getOptionalResourceLocation(tag, "ability_id")
            );
        }
    }

    public record StatSnapshotEntry(
            Map<ResourceLocation, Double> attributes,
            double castSpeed,
            double cooldownRate,
            double manaCostMultiplier,
            double critChance,
            double critMultiplier
    ) {
        public StatSnapshotEntry {
            attributes = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(attributes, "attributes")));
        }

        public static StatSnapshotEntry fromSnapshot(AbilityStatSnapshot snapshot) {
            Map<ResourceLocation, Double> attributes = new LinkedHashMap<>();
            snapshot.attributes().forEach((key, value) -> attributes.put(key, value));
            return new StatSnapshotEntry(
                    attributes,
                    snapshot.castSpeed(),
                    snapshot.cooldownRate(),
                    snapshot.manaCostMultiplier(),
                    snapshot.critChance(),
                    snapshot.critMultiplier()
            );
        }

        public AbilityStatSnapshot toSnapshot() {
            Object2DoubleOpenHashMap<ResourceLocation> attributeValues = new Object2DoubleOpenHashMap<>();
            attributes.forEach(attributeValues::put);
            return new AbilityStatSnapshot(
                    attributeValues,
                    castSpeed,
                    cooldownRate,
                    manaCostMultiplier,
                    critChance,
                    critMultiplier
            );
        }

        private CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            if (!attributes.isEmpty()) {
                CompoundTag attributesTag = new CompoundTag();
                attributes.forEach((key, value) -> attributesTag.putDouble(key.toString(), value));
                tag.put("attributes", attributesTag);
            }
            tag.putDouble("cast_speed", castSpeed);
            tag.putDouble("cooldown_rate", cooldownRate);
            tag.putDouble("mana_cost_multiplier", manaCostMultiplier);
            tag.putDouble("crit_chance", critChance);
            tag.putDouble("crit_multiplier", critMultiplier);
            return tag;
        }

        private static StatSnapshotEntry deserialize(CompoundTag tag) {
            Map<ResourceLocation, Double> attributes = new LinkedHashMap<>();
            CompoundTag attributesTag = tag.getCompound("attributes");
            for (String key : attributesTag.getAllKeys()) {
                attributes.put(ResourceLocation.parse(key), attributesTag.getDouble(key));
            }
            return new StatSnapshotEntry(
                    attributes,
                    tag.getDouble("cast_speed"),
                    tag.getDouble("cooldown_rate"),
                    tag.getDouble("mana_cost_multiplier"),
                    tag.getDouble("crit_chance"),
                    tag.getDouble("crit_multiplier")
            );
        }
    }
}
