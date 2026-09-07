package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;

public record MKWorkspaceVariantAddition(String basePieceName, @Nullable String sourcePieceName) {
    public static final Codec<MKWorkspaceVariantAddition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("basePieceName").forGetter(MKWorkspaceVariantAddition::basePieceName),
                    Codec.STRING.optionalFieldOf("sourcePieceName", "")
                            .forGetter(value -> value.sourcePieceName() == null ? "" : value.sourcePieceName())
            ).apply(instance, MKWorkspaceVariantAddition::new));

    public MKWorkspaceVariantAddition {
        basePieceName = basePieceName == null ? "" : basePieceName;
        sourcePieceName = sourcePieceName == null || sourcePieceName.isBlank() ? null : sourcePieceName;
    }
}
