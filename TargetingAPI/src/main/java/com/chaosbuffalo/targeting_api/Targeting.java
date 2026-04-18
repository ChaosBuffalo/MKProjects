package com.chaosbuffalo.targeting_api;

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

    protected static class TargetRelationCallback {
        BiFunction<Entity, Entity, TargetRelation> func;
        int priority;

        TargetRelationCallback(BiFunction<Entity, Entity, TargetRelation> func, int priority) {
            this.func = func;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

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
        UNHANDLED
    }

    /**
     * Compares two entities by UUID.
     *
     * @param first the first entity
     * @param second the second entity
     * @return {@code true} if both entities are non-null and have the same UUID
     */
    public static boolean areEntitiesEqual(Entity first, Entity second) {
        return first != null && second != null && first.getUUID().compareTo(second.getUUID()) == 0;
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
        // can't be enemy with self
        //need to handle null
        if (source == null || target == null) {
            return TargetRelation.NEUTRAL;
        }

        if (areEntitiesEqual(source, target)) {
            return TargetRelation.FRIEND;
        }

        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            return TargetRelation.UNHANDLED;
        }
        // can't be enemy with entities on same team
        if (source.isAlliedTo(target)) {
            return TargetRelation.FRIEND;
        }

        if (!relationCallbacks.isEmpty()) {
            for (TargetRelationCallback func : relationCallbacks) {
                TargetRelation result = func.func.apply(source, target);
                if (result != TargetRelation.UNHANDLED) {
                    return result;
                }
            }
        } else {
            return defaultRelationCheck(source, target);
        }
        return TargetRelation.UNHANDLED;
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
        relationCallbacks.sort(Comparator.comparingInt(TargetRelationCallback::getPriority));
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
        relationCallbacks.sort(Comparator.comparingInt(TargetRelationCallback::getPriority));
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
        Entity controller = source.getControllingPassenger();
        if (controller != null) {
            return getRootEntity(controller);
        }

        if (source instanceof OwnableEntity owned) {
            Entity owner = owned.getOwner();
            if (owner != null) {
                // Owner is online, so use it for relationship checks
                return getRootEntity(owner);
            } else if (owned.getOwnerUUID() != null) {
                // Entity is owned, but the owner is offline
                // If the owner if offline then there's not much we can do.
                return source;
            }
        }

        if (source instanceof ITargetingOwner owned) {
            Entity owner = owned.getTargetingOwner();
            if (owner != null) {
                return getRootEntity(owner);
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
        return validCheck(caster, target, EnumSet.of(TargetRelation.FRIEND));
    }

    /**
     * Checks whether the target is hostile to the caster.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the resolved relation is enemy
     */
    public static boolean isValidEnemy(Entity caster, Entity target) {
        return validCheck(caster, target, EnumSet.of(TargetRelation.ENEMY));
    }

    /**
     * Checks whether the target is neutral or unhandled relative to the caster.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the resolved relation is neutral or unhandled
     */
    public static boolean isValidNeutral(Entity caster, Entity target) {
        return validCheck(caster, target, EnumSet.of(TargetRelation.NEUTRAL, TargetRelation.UNHANDLED));
    }
}
