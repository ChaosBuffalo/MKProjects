package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceRelayoutImpact(
        String outcome,
        String pieceName,
        String baseName,
        int variantIndex,
        String plannerId,
        String stableSlotKey,
        String reason
) {
    public static final Codec<MKWorkspaceRelayoutImpact> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("outcome").forGetter(MKWorkspaceRelayoutImpact::outcome),
                    Codec.STRING.fieldOf("pieceName").forGetter(MKWorkspaceRelayoutImpact::pieceName),
                    Codec.STRING.fieldOf("baseName").forGetter(MKWorkspaceRelayoutImpact::baseName),
                    Codec.INT.fieldOf("variantIndex").forGetter(MKWorkspaceRelayoutImpact::variantIndex),
                    Codec.STRING.fieldOf("plannerId").forGetter(MKWorkspaceRelayoutImpact::plannerId),
                    Codec.STRING.fieldOf("stableSlotKey").forGetter(MKWorkspaceRelayoutImpact::stableSlotKey),
                    Codec.STRING.fieldOf("reason").forGetter(MKWorkspaceRelayoutImpact::reason)
            ).apply(instance, MKWorkspaceRelayoutImpact::new)
    );

    public MKWorkspaceRelayoutImpact {
        outcome = outcome == null ? "" : outcome;
        pieceName = pieceName == null ? "" : pieceName;
        baseName = baseName == null ? "" : baseName;
        plannerId = plannerId == null ? "" : plannerId;
        stableSlotKey = stableSlotKey == null ? "" : stableSlotKey;
        reason = reason == null ? "" : reason;
    }
}
