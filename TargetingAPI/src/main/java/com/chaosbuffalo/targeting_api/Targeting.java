package com.chaosbuffalo.targeting_api;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.OwnableEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Central utility class for resolving {@link Entity} relationships and validating
 * targets against a {@link TargetingContext}.
 * <p>
 * Relationship checks account for direct entity identity, team membership,
 * entity ownership, mounted controllers, and registered custom callbacks.
 */
public class Targeting {

    private static final List<TargetRelationCallback> relationCallbacks = new ArrayList<>();

    // Per-tick caches, cleared at the start of each client/server tick.
    // relationCache key: two entity IDs packed into a long (source << 32 | target);
    //   value: TargetRelation.ordinal(). -1 sentinel means no cached entry.
    // rootCache key: entity ID; value: the resolved root entity for that entity.
    private static final int RELATION_CACHE_MISS = -1;
    private static final int ROOT_ENTITY_MAX_DEPTH = 5;
    private static final Long2IntOpenHashMap relationCache = new Long2IntOpenHashMap();
    private static final Int2ObjectOpenHashMap<Entity> rootCache = new Int2ObjectOpenHashMap<>();
    private static final TargetRelation[] RELATION_VALUES = TargetRelation.values();

    static {
        relationCache.defaultReturnValue(RELATION_CACHE_MISS);
    }

    static void clearTickCaches() {
        relationCache.clear();
        rootCache.clear();
    }

    /**
     * Removes the cached relation for a specific entity pair.
     * <p>
     * Call this when a relationship between two known entities changes mid-tick.
     * The relation will be recomputed on the next check.
     *
     * @param source the acting entity
     * @param target the other entity
     */
    public static void invalidateRelation(Entity source, Entity target) {
        relationCache.remove(packRelationKey(source.getId(), target.getId()));
        relationCache.remove(packRelationKey(target.getId(), source.getId()));
    }

    /**
     * Clears the entire per-tick relation cache.
     * <p>
     * Call this when a change affects an entity whose full set of cached
     * pairs is unknown, such as a faction reassignment.
     */
    public static void invalidateAllRelations() {
        relationCache.clear();
    }

    private static long packRelationKey(int sourceId, int targetId) {
        return ((long) sourceId << 32) | (targetId & 0xFFFFFFFFL);
    }

    protected record TargetRelationCallback(BiFunction<Entity, Entity, TargetRelation> func, int priority) {}

    /**
     * Describes how one {@link Entity} relates to another for targeting purposes.
     */
    public enum TargetRelation {
        /**
         * The target should be treated as friendly to the source.
         */
        FRIEND,
        /**
         * The target should be treated as hostile to the source.
         */
        ENEMY,
        /**
         * The target is neither friendly nor hostile.
         */
        NEUTRAL,
        /**
         * No relation could be determined by the current checks.
         */
        UNHANDLED;

        public boolean isFriendly() { return this == FRIEND; }
        public boolean isHostile()  { return this == ENEMY; }
        public boolean isNeutral()  { return this == NEUTRAL; }
        public boolean isHandled()  { return this != UNHANDLED; }

        public TargetRelation opposite() {
            return switch (this) {
                case FRIEND -> ENEMY;
                case ENEMY  -> FRIEND;
                default     -> this;
            };
        }
    }

    private static final EnumSet<TargetRelation> FRIEND_SET  = EnumSet.of(TargetRelation.FRIEND);
    private static final EnumSet<TargetRelation> ENEMY_SET   = EnumSet.of(TargetRelation.ENEMY);
    private static final EnumSet<TargetRelation> NEUTRAL_SET = EnumSet.of(TargetRelation.NEUTRAL, TargetRelation.UNHANDLED);

    /**
     * Compares two entities by reference then by integer entity ID.
     *
     * @param first the first entity
     * @param second the second entity
     * @return {@code true} if both entities are non-null and refer to the same entity
     */
    public static boolean areEntitiesEqual(Entity first, Entity second) {
        if (first == null || second == null) return false;
        if (first == second) return true;
        return first.getId() == second.getId();
    }

    static TargetRelation defaultRelationCheck(Entity source, Entity target) {
        return target.getClassification(false).isFriendly() ?
                Targeting.TargetRelation.FRIEND :
                Targeting.TargetRelation.ENEMY;
    }

    /**
     * Resolves the relationship between two entities after following ownership
     * and controller chains to their effective root entities.
     *
     * @param source the acting entity
     * @param target the potential target
     * @return the resolved targeting relation
     */
    public static TargetRelation getTargetRelation(Entity source, Entity target) {
        Entity sourceRoot = getRootEntity(source);
        Entity targetRoot = getRootEntity(target);
        return getTargetRelationInternal(sourceRoot, targetRoot);
    }

    private static TargetRelation getTargetRelationInternal(Entity source, Entity target) {
        if (source == null || target == null) {
            return TargetRelation.NEUTRAL;
        }

        long key = packRelationKey(source.getId(), target.getId());
        int cached = relationCache.get(key);
        if (cached != RELATION_CACHE_MISS) {
            return RELATION_VALUES[cached];
        }

        TargetRelation result = resolveRelation(source, target);
        relationCache.put(key, result.ordinal());
        return result;
    }

    private static TargetRelation resolveRelation(Entity source, Entity target) {
        if (areEntitiesEqual(source, target)) {
            return TargetRelation.FRIEND;
        }

        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            return TargetRelation.UNHANDLED;
        }

        if (source.isAlliedTo(target)) {
            return TargetRelation.FRIEND;
        }

        for (TargetRelationCallback func : relationCallbacks) {
            TargetRelation result = func.func().apply(source, target);
            if (result != TargetRelation.UNHANDLED) {
                return result;
            }
        }
        return defaultRelationCheck(source, target);
    }

    /**
     * Registers a relation callback with the default priority of {@code 10}.
     * <p>
     * Callbacks are evaluated in ascending priority order until one returns a
     * relation other than {@link TargetRelation#UNHANDLED}.
     *
     * @param callback the callback used to resolve a source/target relation
     */
    public static void registerRelationCallback(BiFunction<Entity, Entity, TargetRelation> callback) {
        relationCallbacks.add(new TargetRelationCallback(callback, 10));
        relationCallbacks.sort(Comparator.comparingInt(TargetRelationCallback::priority));
    }

    /**
     * Registers a relation callback with an explicit priority.
     * <p>
     * Lower priority values run first.
     *
     * @param callback the callback used to resolve a source/target relation
     * @param priority the callback ordering value
     */
    public static void registerRelationCallback(BiFunction<Entity, Entity, TargetRelation> callback, int priority) {
        relationCallbacks.add(new TargetRelationCallback(callback, priority));
        relationCallbacks.sort(Comparator.comparingInt(TargetRelationCallback::priority));
    }

    /**
     * Tests whether a target is valid for the supplied targeting context.
     *
     * @param context the targeting rules to apply
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the target passes the context checks
     */
    public static boolean isValidTarget(TargetingContext context, Entity caster, Entity target) {
        return context.isValidTarget(caster, target);
    }

    private static Entity getRootEntity(Entity source) {
        int id = source.getId();
        Entity cached = rootCache.get(id);
        if (cached != null) {
            return cached;
        }
        Entity root = getRootEntity(source, ROOT_ENTITY_MAX_DEPTH);
        rootCache.put(id, root);
        return root;
    }

    private static Entity getRootEntity(Entity source, int depth) {
        if (depth == 0) {
            return source;
        }

        Entity controller = source.getControllingPassenger();
        if (controller != null) {
            return getRootEntity(controller, depth - 1);
        }

        if (source instanceof OwnableEntity owned) {
            Entity owner = owned.getOwner();
            if (owner != null) {
                // Owner is online, so use it for relationship checks
                return getRootEntity(owner, depth - 1);
            } else if (owned.getOwnerUUID() != null) {
                // Entity is owned, but the owner is offline
                // If the owner if offline then there's not much we can do.
                return source;
            }
        }

        if (source instanceof ITargetingOwner owned) {
            Entity owner = owned.getTargetingOwner();
            if (owner != null) {
                return getRootEntity(owner, depth - 1);
            }
        }

        return source;
    }

    static boolean validCheck(Entity caster, Entity target, EnumSet<TargetRelation> relations) {
        Entity casterRoot = getRootEntity(caster);
        Entity targetRoot = getRootEntity(target);

        TargetRelation relation = getTargetRelationInternal(casterRoot, targetRoot);
        return relations.contains(relation);
    }

    /**
     * Accepts all targets.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return always {@code true}
     */
    public static boolean allowAny(Entity caster, Entity target) {
        return true;
    }

    /**
     * Checks whether the target is friendly to the caster.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the resolved relation is friendly
     */
    public static boolean isValidFriendly(Entity caster, Entity target) {
        return validCheck(caster, target, FRIEND_SET);
    }

    /**
     * Checks whether the target is hostile to the caster.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the resolved relation is enemy
     */
    public static boolean isValidEnemy(Entity caster, Entity target) {
        return validCheck(caster, target, ENEMY_SET);
    }

    /**
     * Checks whether the target is neutral or unhandled relative to the caster.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the resolved relation is neutral or unhandled
     */
    public static boolean isValidNeutral(Entity caster, Entity target) {
        return validCheck(caster, target, NEUTRAL_SET);
    }
}
