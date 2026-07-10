package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceTemplateRemapSuggestion(
        MKWorkspacePlannerId orphanedPlannerId,
        MKWorkspacePlannerId targetPlannerId,
        int score,
        String reason
) {
    public static final Codec<MKWorkspaceTemplateRemapSuggestion> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MKWorkspacePlannerId.CODEC.fieldOf("orphanedPlannerId")
                            .forGetter(MKWorkspaceTemplateRemapSuggestion::orphanedPlannerId),
                    MKWorkspacePlannerId.CODEC.fieldOf("targetPlannerId")
                            .forGetter(MKWorkspaceTemplateRemapSuggestion::targetPlannerId),
                    Codec.INT.fieldOf("score").forGetter(MKWorkspaceTemplateRemapSuggestion::score),
                    Codec.STRING.fieldOf("reason").forGetter(MKWorkspaceTemplateRemapSuggestion::reason)
            ).apply(instance, MKWorkspaceTemplateRemapSuggestion::new)
    );

    public MKWorkspaceTemplateRemapSuggestion {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("remap suggestion score must be between 0 and 100");
        }
        if (reason == null || reason.isBlank()) {
            reason = "compatible template target";
        }
    }
}
