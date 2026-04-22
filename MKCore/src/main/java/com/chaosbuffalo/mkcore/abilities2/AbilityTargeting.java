package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetRelation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public final class AbilityTargeting {
    public static final String RELATION_KEY = "relation";

    private AbilityTargeting() {
    }

    public static AbilityTargetRelation relation(AbilityTargetResolverDefinition targeting) {
        Objects.requireNonNull(targeting, "targeting");
        String relationName = getString(targeting, RELATION_KEY);
        return relationName != null ? AbilityTargetRelation.fromSerializedName(relationName) : AbilityTargetRelation.ALL;
    }

    public static boolean supportsRelation(String targetingType) {
        return switch (targetingType) {
            case "resolved", "event_target", "event_actor" -> true;
            default -> false;
        };
    }

    public static AbilityTargetResolverDefinition resolved(AbilityTargetRelation relation) {
        return withRelation("resolved", relation);
    }

    public static AbilityTargetResolverDefinition eventTarget(AbilityTargetRelation relation) {
        return withRelation("event_target", relation);
    }

    public static AbilityTargetResolverDefinition eventActor(AbilityTargetRelation relation) {
        return withRelation("event_actor", relation);
    }

    public static TargetingContext toTargetingContext(AbilityTargetResolverDefinition targeting) {
        Objects.requireNonNull(targeting, "targeting");
        return switch (targeting.type()) {
            case "self" -> TargetingContexts.SELF;
            case "resolved", "event_target", "event_actor" -> switch (relation(targeting)) {
                case ALL -> TargetingContexts.ALL;
                case FRIENDLY -> TargetingContexts.FRIENDLY;
                case ENEMY -> TargetingContexts.ENEMY;
            };
            case "none" -> TargetingContexts.SELF;
            default -> TargetingContexts.SELF;
        };
    }

    public static boolean isValidTarget(AbilityTargetResolverDefinition targeting,
                                        LivingEntity caster,
                                        LivingEntity target) {
        Objects.requireNonNull(targeting, "targeting");
        Objects.requireNonNull(caster, "caster");
        Objects.requireNonNull(target, "target");
        return switch (targeting.type()) {
            case "self" -> Targeting.areEntitiesEqual(caster, target);
            case "resolved", "event_target", "event_actor" -> switch (relation(targeting)) {
                case ALL -> Targeting.allowAny(caster, target);
                case FRIENDLY -> Targeting.isValidFriendly(caster, target);
                case ENEMY -> Targeting.isValidEnemy(caster, target);
            };
            default -> false;
        };
    }

    private static AbilityTargetResolverDefinition withRelation(String type, AbilityTargetRelation relation) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(relation, "relation");
        return new AbilityTargetResolverDefinition(type, Map.of(RELATION_KEY, new JsonPrimitive(relation.serializedName())));
    }

    private static @Nullable String getString(AbilityTargetResolverDefinition targeting, String key) {
        JsonElement element = targeting.get(key);
        if (element == null) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Target resolver field '" + key + "' must be a string");
        }
        return element.getAsString();
    }
}
