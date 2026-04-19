package com.chaosbuffalo.mkcore.abilities2.runtime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public record AbilityEventProvenance(
        @Nullable UUID invocationId,
        @Nullable UUID rootInvocationId,
        int chainDepth,
        @Nullable UUID sourceId,
        @Nullable ResourceLocation sourceAbilityId,
        @Nullable String sourceActivationId
) {
    public AbilityEventProvenance {
        if (chainDepth < 0) {
            throw new IllegalArgumentException("Ability event provenance chainDepth must be >= 0");
        }
        if (sourceActivationId != null && sourceActivationId.isBlank()) {
            throw new IllegalArgumentException("Ability event provenance sourceActivationId must not be blank");
        }
    }

    public static AbilityEventProvenance fromInvocation(AbilityInvocation invocation) {
        return new AbilityEventProvenance(
                invocation.invocationId(),
                invocation.rootInvocationId(),
                invocation.chainDepth(),
                invocation.sourceId(),
                invocation.abilityId(),
                invocation.activationId()
        );
    }

    public AbilityEventProvenance withSourceId(@Nullable UUID updatedSourceId) {
        return new AbilityEventProvenance(
                invocationId,
                rootInvocationId,
                chainDepth,
                updatedSourceId,
                sourceAbilityId,
                sourceActivationId
        );
    }

    public AbilityEventProvenance asChildSource(UUID childSourceId) {
        return new AbilityEventProvenance(
                invocationId,
                rootInvocationId,
                chainDepth + 1,
                childSourceId,
                sourceAbilityId,
                sourceActivationId
        );
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        if (invocationId != null) {
            tag.putUUID("invocation_id", invocationId);
        }
        if (rootInvocationId != null) {
            tag.putUUID("root_invocation_id", rootInvocationId);
        }
        tag.putInt("chain_depth", chainDepth);
        if (sourceId != null) {
            tag.putUUID("source_id", sourceId);
        }
        if (sourceAbilityId != null) {
            tag.putString("source_ability_id", sourceAbilityId.toString());
        }
        if (sourceActivationId != null) {
            tag.putString("source_activation_id", sourceActivationId);
        }
        return tag;
    }

    public static @Nullable AbilityEventProvenance deserialize(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        UUID invocationId = tag.contains("invocation_id") ? tag.getUUID("invocation_id") : null;
        UUID rootInvocationId = tag.contains("root_invocation_id") ? tag.getUUID("root_invocation_id") : null;
        int chainDepth = tag.contains("chain_depth") ? tag.getInt("chain_depth") : 0;
        UUID sourceId = tag.contains("source_id") ? tag.getUUID("source_id") : null;
        ResourceLocation sourceAbilityId = tag.contains("source_ability_id")
                ? ResourceLocation.tryParse(tag.getString("source_ability_id"))
                : null;
        String sourceActivationId = tag.contains("source_activation_id")
                ? tag.getString("source_activation_id")
                : null;
        return new AbilityEventProvenance(
                invocationId,
                rootInvocationId,
                chainDepth,
                sourceId,
                sourceAbilityId,
                sourceActivationId
        );
    }
}
