package com.chaosbuffalo.mkcore.abilities2.runtime;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public record AbilityResolvedTargets(
        @Nullable UUID primaryEntityId,
        List<UUID> entityIds,
        @Nullable Vec3 point,
        @Nullable HitResult hitResult,
        @Nullable UUID deliveryId
) {
    public AbilityResolvedTargets {
        entityIds = normalize(primaryEntityId, entityIds);
    }

    private static List<UUID> normalize(@Nullable UUID primaryEntityId, List<UUID> entityIds) {
        LinkedHashSet<UUID> deduped = new LinkedHashSet<>(Objects.requireNonNull(entityIds, "entityIds"));
        if (primaryEntityId != null) {
            deduped.remove(primaryEntityId);
            ArrayList<UUID> ordered = new ArrayList<>(deduped.size() + 1);
            ordered.add(primaryEntityId);
            ordered.addAll(deduped);
            return List.copyOf(ordered);
        }
        return List.copyOf(deduped);
    }
}
