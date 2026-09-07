package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;
import java.util.UUID;

public record MKWorkspaceContentSelectionChangePayload(
        Kind kind,
        UUID pieceId,
        String targetFamilyId,
        int weight,
        boolean enabled,
        MKWorkspaceTemplatePurpose purpose
) {
    public enum Kind {
        PROMOTE_VARIANT, MOVE_VARIANT, SET_FAMILY_WEIGHT, SET_FAMILY_ENABLED,
        SET_VARIANT_WEIGHT, SET_VARIANT_ENABLED, SET_TEMPLATE_PURPOSE
    }

    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    private static final Codec<Kind> KIND_CODEC = Codec.STRING.xmap(
            value -> Kind.valueOf(value.toUpperCase(Locale.ROOT)),
            value -> value.name().toLowerCase(Locale.ROOT));
    public static final Codec<MKWorkspaceContentSelectionChangePayload> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    KIND_CODEC.fieldOf("kind").forGetter(MKWorkspaceContentSelectionChangePayload::kind),
                    UUID_CODEC.fieldOf("pieceId").forGetter(MKWorkspaceContentSelectionChangePayload::pieceId),
                    Codec.STRING.optionalFieldOf("targetFamilyId", "")
                            .forGetter(MKWorkspaceContentSelectionChangePayload::targetFamilyId),
                    Codec.INT.optionalFieldOf("weight", 1)
                            .forGetter(MKWorkspaceContentSelectionChangePayload::weight),
                    Codec.BOOL.optionalFieldOf("enabled", true)
                            .forGetter(MKWorkspaceContentSelectionChangePayload::enabled),
                    MKWorkspaceTemplatePurpose.CODEC.optionalFieldOf("purpose",
                                    MKWorkspaceTemplatePurpose.FAMILY_VARIANT)
                            .forGetter(MKWorkspaceContentSelectionChangePayload::purpose)
            ).apply(instance, MKWorkspaceContentSelectionChangePayload::new));
}
