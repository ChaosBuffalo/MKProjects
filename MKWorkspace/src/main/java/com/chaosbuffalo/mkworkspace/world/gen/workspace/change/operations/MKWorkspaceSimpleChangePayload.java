package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceSimpleChangePayload(String target, String secondary, String tertiary, int amount) {
    public static final Codec<MKWorkspaceSimpleChangePayload> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("target", "").forGetter(MKWorkspaceSimpleChangePayload::target),
                    Codec.STRING.optionalFieldOf("secondary", "").forGetter(MKWorkspaceSimpleChangePayload::secondary),
                    Codec.STRING.optionalFieldOf("tertiary", "").forGetter(MKWorkspaceSimpleChangePayload::tertiary),
                    Codec.INT.optionalFieldOf("amount", 0).forGetter(MKWorkspaceSimpleChangePayload::amount)
            ).apply(instance, MKWorkspaceSimpleChangePayload::new));

    public static MKWorkspaceSimpleChangePayload empty() {
        return new MKWorkspaceSimpleChangePayload("", "", "", 0);
    }
}
