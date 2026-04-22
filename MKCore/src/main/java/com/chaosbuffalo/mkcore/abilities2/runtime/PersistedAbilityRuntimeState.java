package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.codec.AbilityCodecs;
import com.chaosbuffalo.mkcore.abilities2.definition.DeliveryKind;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record PersistedAbilityRuntimeState(
        List<CooldownEntry> cooldowns,
        List<GcdEntry> gcds,
        List<StateEntry> states,
        List<ToggleEntry> toggles,
        List<DeliveryEntry> deliveries,
        List<PersistedPendingAbilityActivation> pendingActivations
) {
    public static final PersistedAbilityRuntimeState EMPTY =
            new PersistedAbilityRuntimeState(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    private static final String COOLDOWNS_TAG = "cooldowns";
    private static final String GCDS_TAG = "gcds";
    private static final String STATES_TAG = "states";
    private static final String TOGGLES_TAG = "toggles";
    private static final String DELIVERIES_TAG = "deliveries";
    private static final String PENDING_ACTIVATIONS_TAG = "pending_activations";

    public PersistedAbilityRuntimeState {
        cooldowns = List.copyOf(Objects.requireNonNull(cooldowns, "cooldowns"));
        gcds = List.copyOf(Objects.requireNonNull(gcds, "gcds"));
        states = List.copyOf(Objects.requireNonNull(states, "states"));
        toggles = List.copyOf(Objects.requireNonNull(toggles, "toggles"));
        deliveries = List.copyOf(Objects.requireNonNull(deliveries, "deliveries"));
        pendingActivations = List.copyOf(Objects.requireNonNull(pendingActivations, "pendingActivations"));
    }

    public boolean isEmpty() {
        return cooldowns.isEmpty() && gcds.isEmpty() && states.isEmpty() && toggles.isEmpty()
                && deliveries.isEmpty()
                && pendingActivations.isEmpty();
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!cooldowns.isEmpty()) {
            ListTag list = new ListTag();
            cooldowns.forEach(entry -> list.add(entry.serialize()));
            tag.put(COOLDOWNS_TAG, list);
        }
        if (!gcds.isEmpty()) {
            ListTag list = new ListTag();
            gcds.forEach(entry -> list.add(entry.serialize()));
            tag.put(GCDS_TAG, list);
        }
        if (!states.isEmpty()) {
            ListTag list = new ListTag();
            states.forEach(entry -> list.add(entry.serialize(provider)));
            tag.put(STATES_TAG, list);
        }
        if (!toggles.isEmpty()) {
            ListTag list = new ListTag();
            toggles.forEach(entry -> list.add(entry.serialize(provider)));
            tag.put(TOGGLES_TAG, list);
        }
        if (!deliveries.isEmpty()) {
            ListTag list = new ListTag();
            deliveries.forEach(entry -> list.add(entry.serialize(provider)));
            tag.put(DELIVERIES_TAG, list);
        }
        if (!pendingActivations.isEmpty()) {
            ListTag list = new ListTag();
            pendingActivations.forEach(entry -> list.add(entry.serialize(provider)));
            tag.put(PENDING_ACTIVATIONS_TAG, list);
        }
        return tag;
    }

    public static PersistedAbilityRuntimeState deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.isEmpty()) {
            return EMPTY;
        }

        List<CooldownEntry> cooldowns = readCompoundList(tag, COOLDOWNS_TAG).stream()
                .map(CooldownEntry::deserialize)
                .toList();
        List<GcdEntry> gcds = readCompoundList(tag, GCDS_TAG).stream()
                .map(GcdEntry::deserialize)
                .toList();
        List<StateEntry> states = readCompoundList(tag, STATES_TAG).stream()
                .map(entry -> StateEntry.deserialize(provider, entry))
                .toList();
        List<ToggleEntry> toggles = readCompoundList(tag, TOGGLES_TAG).stream()
                .map(entry -> ToggleEntry.deserialize(provider, entry))
                .toList();
        List<DeliveryEntry> deliveries = readCompoundList(tag, DELIVERIES_TAG).stream()
                .map(entry -> DeliveryEntry.deserialize(provider, entry))
                .toList();
        List<PersistedPendingAbilityActivation> pendingActivations = readCompoundList(tag, PENDING_ACTIVATIONS_TAG).stream()
                .map(entry -> PersistedPendingAbilityActivation.deserialize(provider, entry))
                .toList();
        return new PersistedAbilityRuntimeState(cooldowns, gcds, states, toggles, deliveries, pendingActivations);
    }

    private static List<CompoundTag> readCompoundList(CompoundTag root, String key) {
        if (!root.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = root.getList(key, Tag.TAG_COMPOUND);
        return list.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .toList();
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

    private static Tag encodeAbilityValue(HolderLookup.Provider provider, AbilityValue value) {
        return AbilityCodecs.ABILITY_VALUE_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), value)
                .getOrThrow();
    }

    private static AbilityValue decodeAbilityValue(HolderLookup.Provider provider, Tag valueTag) {
        return AbilityCodecs.ABILITY_VALUE_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), valueTag)
                .getOrThrow();
    }

    public record CooldownEntry(
            StateScope scope,
            String key,
            int remainingTicks,
            @Nullable UUID stableSourceId,
            @Nullable ResourceLocation abilityId
    ) {
        public CooldownEntry {
            Objects.requireNonNull(scope, "scope");
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Cooldown key must not be blank");
            }
            if (remainingTicks < 0) {
                throw new IllegalArgumentException("Cooldown remainingTicks must be >= 0");
            }
        }

        private CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("scope", scope.name());
            tag.putString("key", key);
            tag.putInt("remaining_ticks", remainingTicks);
            putOptionalUuid(tag, "stable_source_id", stableSourceId);
            putOptionalResourceLocation(tag, "ability_id", abilityId);
            return tag;
        }

        private static CooldownEntry deserialize(CompoundTag tag) {
            return new CooldownEntry(
                    StateScope.valueOf(tag.getString("scope")),
                    tag.getString("key"),
                    tag.getInt("remaining_ticks"),
                    getOptionalUuid(tag, "stable_source_id"),
                    getOptionalResourceLocation(tag, "ability_id")
            );
        }
    }

    public record GcdEntry(ResourceLocation gcdGroup, int remainingTicks) {
        public GcdEntry {
            Objects.requireNonNull(gcdGroup, "gcdGroup");
            if (remainingTicks < 0) {
                throw new IllegalArgumentException("GCD remainingTicks must be >= 0");
            }
        }

        private CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("gcd_group", gcdGroup.toString());
            tag.putInt("remaining_ticks", remainingTicks);
            return tag;
        }

        private static GcdEntry deserialize(CompoundTag tag) {
            return new GcdEntry(
                    ResourceLocation.parse(tag.getString("gcd_group")),
                    tag.getInt("remaining_ticks")
            );
        }
    }

    public record StateEntry(
            StateScope scope,
            String stateKey,
            @Nullable UUID stableSourceId,
            @Nullable ResourceLocation abilityId,
            AbilityValue value
    ) {
        public StateEntry {
            Objects.requireNonNull(scope, "scope");
            if (stateKey == null || stateKey.isBlank()) {
                throw new IllegalArgumentException("State key must not be blank");
            }
            Objects.requireNonNull(value, "value");
        }

        private CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putString("scope", scope.name());
            tag.putString("state_key", stateKey);
            putOptionalUuid(tag, "stable_source_id", stableSourceId);
            putOptionalResourceLocation(tag, "ability_id", abilityId);
            tag.put("value", encodeAbilityValue(provider, value));
            return tag;
        }

        private static StateEntry deserialize(HolderLookup.Provider provider, CompoundTag tag) {
            return new StateEntry(
                    StateScope.valueOf(tag.getString("scope")),
                    tag.getString("state_key"),
                    getOptionalUuid(tag, "stable_source_id"),
                    getOptionalResourceLocation(tag, "ability_id"),
                    decodeAbilityValue(provider, tag.get("value"))
            );
        }
    }

    public record ToggleEntry(
            ResourceLocation abilityId,
            @Nullable UUID grantId,
            UUID stableSourceId,
            @Nullable UUID casterEntityId,
            Map<String, AbilityValue> grantParameterOverrides,
            int nextPulseDelayTicks
    ) {
        public ToggleEntry {
            Objects.requireNonNull(abilityId, "abilityId");
            Objects.requireNonNull(stableSourceId, "stableSourceId");
            grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(
                    grantParameterOverrides, "grantParameterOverrides")));
            if (nextPulseDelayTicks < 0) {
                throw new IllegalArgumentException("Toggle nextPulseDelayTicks must be >= 0");
            }
        }

        private CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putString("ability_id", abilityId.toString());
            putOptionalUuid(tag, "grant_id", grantId);
            tag.putUUID("stable_source_id", stableSourceId);
            putOptionalUuid(tag, "caster_entity_id", casterEntityId);
            tag.putInt("next_pulse_delay_ticks", nextPulseDelayTicks);
            if (!grantParameterOverrides.isEmpty()) {
                CompoundTag overridesTag = new CompoundTag();
                grantParameterOverrides.forEach((key, value) -> overridesTag.put(key, encodeAbilityValue(provider, value)));
                tag.put("grant_parameter_overrides", overridesTag);
            }
            return tag;
        }

        private static ToggleEntry deserialize(HolderLookup.Provider provider, CompoundTag tag) {
            Map<String, AbilityValue> overrides = new LinkedHashMap<>();
            CompoundTag overridesTag = tag.getCompound("grant_parameter_overrides");
            for (String key : overridesTag.getAllKeys()) {
                overrides.put(key, decodeAbilityValue(provider, overridesTag.get(key)));
            }
            return new ToggleEntry(
                    ResourceLocation.parse(tag.getString("ability_id")),
                    getOptionalUuid(tag, "grant_id"),
                    tag.getUUID("stable_source_id"),
                    getOptionalUuid(tag, "caster_entity_id"),
                    overrides,
                    tag.getInt("next_pulse_delay_ticks")
            );
        }
    }

    public record DeliveryEntry(
            ResourceLocation abilityId,
            @Nullable UUID grantId,
            UUID stableSourceId,
            @Nullable UUID casterEntityId,
            Map<String, AbilityValue> grantParameterOverrides,
            String deliveryId,
            DeliveryKind kind,
            ResourceKey<Level> dimension,
            Vec3 point,
            @Nullable Vec3 velocity,
            double radius,
            int delayTicksRemaining,
            int durationTicksRemaining,
            int tickIntervalTicks,
            int ticksUntilNextGroundTick,
            boolean projectileInGround,
            int projectileTicksInGround,
            int projectileTicksInAir,
            AbilityEventProvenance callbackProvenance
    ) {
        public DeliveryEntry {
            Objects.requireNonNull(abilityId, "abilityId");
            Objects.requireNonNull(stableSourceId, "stableSourceId");
            grantParameterOverrides = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(
                    grantParameterOverrides, "grantParameterOverrides")));
            if (deliveryId == null || deliveryId.isBlank()) {
                throw new IllegalArgumentException("Delivery entry deliveryId must not be blank");
            }
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(point, "point");
            if (kind == DeliveryKind.PROJECTILE && velocity == null) {
                throw new IllegalArgumentException("Projectile delivery entries require a velocity");
            }
            if (radius < 0.0) {
                throw new IllegalArgumentException("Delivery entry radius must be >= 0");
            }
            if (delayTicksRemaining < 0 || durationTicksRemaining < 0 || tickIntervalTicks < 0
                    || ticksUntilNextGroundTick < 0 || projectileTicksInGround < 0 || projectileTicksInAir < 0) {
                throw new IllegalArgumentException("Delivery entry tick values must be >= 0");
            }
            Objects.requireNonNull(callbackProvenance, "callbackProvenance");
        }

        private CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putString("ability_id", abilityId.toString());
            putOptionalUuid(tag, "grant_id", grantId);
            tag.putUUID("stable_source_id", stableSourceId);
            putOptionalUuid(tag, "caster_entity_id", casterEntityId);
            if (!grantParameterOverrides.isEmpty()) {
                CompoundTag overridesTag = new CompoundTag();
                grantParameterOverrides.forEach((key, value) -> overridesTag.put(key, encodeAbilityValue(provider, value)));
                tag.put("grant_parameter_overrides", overridesTag);
            }
            tag.putString("delivery_id", deliveryId);
            tag.putString("kind", kind.name());
            tag.putString("dimension", dimension.location().toString());
            tag.putDouble("x", point.x);
            tag.putDouble("y", point.y);
            tag.putDouble("z", point.z);
            putOptionalVec3(tag, "velocity", velocity);
            tag.putDouble("radius", radius);
            tag.putInt("delay_ticks_remaining", delayTicksRemaining);
            tag.putInt("duration_ticks_remaining", durationTicksRemaining);
            tag.putInt("tick_interval_ticks", tickIntervalTicks);
            tag.putInt("ticks_until_next_ground_tick", ticksUntilNextGroundTick);
            tag.putBoolean("projectile_in_ground", projectileInGround);
            tag.putInt("projectile_ticks_in_ground", projectileTicksInGround);
            tag.putInt("projectile_ticks_in_air", projectileTicksInAir);
            tag.put("callback_provenance", callbackProvenance.serialize());
            return tag;
        }

        private static DeliveryEntry deserialize(HolderLookup.Provider provider, CompoundTag tag) {
            Map<String, AbilityValue> overrides = new LinkedHashMap<>();
            CompoundTag overridesTag = tag.getCompound("grant_parameter_overrides");
            for (String key : overridesTag.getAllKeys()) {
                overrides.put(key, decodeAbilityValue(provider, overridesTag.get(key)));
            }
            return new DeliveryEntry(
                    ResourceLocation.parse(tag.getString("ability_id")),
                    getOptionalUuid(tag, "grant_id"),
                    tag.getUUID("stable_source_id"),
                    getOptionalUuid(tag, "caster_entity_id"),
                    overrides,
                    tag.getString("delivery_id"),
                    DeliveryKind.valueOf(tag.getString("kind")),
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension"))),
                    new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")),
                    getOptionalVec3(tag, "velocity"),
                    tag.getDouble("radius"),
                    tag.getInt("delay_ticks_remaining"),
                    tag.getInt("duration_ticks_remaining"),
                    tag.getInt("tick_interval_ticks"),
                    tag.getInt("ticks_until_next_ground_tick"),
                    tag.getBoolean("projectile_in_ground"),
                    tag.getInt("projectile_ticks_in_ground"),
                    tag.getInt("projectile_ticks_in_air"),
                    Objects.requireNonNull(AbilityEventProvenance.deserialize(tag.getCompound("callback_provenance")))
            );
        }
    }
}
