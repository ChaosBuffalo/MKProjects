package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.Optional;

public enum MKWorkspaceMutationSafety {
    SAFE_METADATA_UPDATE("safe_metadata_update"),
    SAFE_BLOCK_SUBSTITUTION("safe_block_substitution"),
    SAFE_EXPANSION("safe_expansion"),
    SAFE_RELAYOUT("safe_relayout"),
    CONDITIONALLY_SAFE_TOPOLOGY_PATCH("conditionally_safe_topology_patch"),
    DESTRUCTIVE_REGENERATE("destructive_regenerate");

    public static final Codec<MKWorkspaceMutationSafety> CODEC = Codec.STRING.comapFlatMap(
            value -> fromSerializedName(value)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown workspace mutation safety: " + value)),
            MKWorkspaceMutationSafety::getSerializedName
    );

    private final String serializedName;

    MKWorkspaceMutationSafety(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static Optional<MKWorkspaceMutationSafety> fromSerializedName(String serializedName) {
        return Arrays.stream(values())
                .filter(safety -> safety.serializedName.equals(serializedName))
                .findFirst();
    }
}
