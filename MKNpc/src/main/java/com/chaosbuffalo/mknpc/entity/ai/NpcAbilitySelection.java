package com.chaosbuffalo.mknpc.entity.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityResolvedTargets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public record NpcAbilitySelection(
        ResourceLocation abilityId,
        @Nullable MKAbilityInfo legacyAbilityInfo,
        @Nullable String activationId,
        @Nullable AbilityTargetResolverDefinition targeting,
        AbilityTargetingDecision.MovementSuggestion movementSuggestion
) {
    public static NpcAbilitySelection legacy(MKAbilityInfo abilityInfo,
                                             AbilityTargetingDecision.MovementSuggestion movementSuggestion) {
        return new NpcAbilitySelection(abilityInfo.getId(), abilityInfo, null, null, movementSuggestion);
    }

    public static NpcAbilitySelection definition(ResourceLocation abilityId,
                                                 String activationId,
                                                 AbilityTargetResolverDefinition targeting,
                                                 AbilityTargetingDecision.MovementSuggestion movementSuggestion) {
        return new NpcAbilitySelection(abilityId, null, activationId, targeting, movementSuggestion);
    }

    public boolean isDefinitionBacked() {
        return legacyAbilityInfo == null;
    }

    public boolean usesExternalTarget() {
        return targeting != null && "resolved".equals(targeting.type());
    }

    public @Nullable AbilityResolvedTargets createForcedTargets(LivingEntity caster, @Nullable LivingEntity target) {
        if (targeting == null) {
            return null;
        }
        return switch (targeting.type()) {
            case "none" -> new AbilityResolvedTargets(null, List.of(), null, null, null);
            case "self" -> {
                UUID casterId = caster.getUUID();
                yield new AbilityResolvedTargets(casterId, List.of(casterId), null, null, null);
            }
            case "resolved" -> {
                if (target == null) {
                    yield null;
                }
                UUID targetId = target.getUUID();
                yield new AbilityResolvedTargets(targetId, List.of(targetId), null, null, null);
            }
            default -> null;
        };
    }
}
