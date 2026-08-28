package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVariantAddition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.UUID;

public record MKWorkspaceDefinitionChangePayload(
        MKStructureWorkspace workspace,
        boolean generateAfterApply,
        boolean forceFullRegenerate,
        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
        List<MKWorkspaceVariantAddition> addedVariants,
        List<UUID> deletedVariantPieceIds,
        boolean workspaceSettingsDirty
) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<MKWorkspaceDefinitionChangePayload> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MKStructureWorkspace.CODEC.fieldOf("workspace")
                            .forGetter(MKWorkspaceDefinitionChangePayload::workspace),
                    Codec.BOOL.optionalFieldOf("generateAfterApply", false)
                            .forGetter(MKWorkspaceDefinitionChangePayload::generateAfterApply),
                    Codec.BOOL.optionalFieldOf("forceFullRegenerate", false)
                            .forGetter(MKWorkspaceDefinitionChangePayload::forceFullRegenerate),
                    MKWorkspaceTemplateRemapSuggestion.CODEC.listOf().optionalFieldOf("acceptedRemaps", List.of())
                            .forGetter(MKWorkspaceDefinitionChangePayload::acceptedRemaps),
                    MKWorkspaceVariantAddition.CODEC.listOf().optionalFieldOf("addedVariants", List.of())
                            .forGetter(MKWorkspaceDefinitionChangePayload::addedVariants),
                    UUID_CODEC.listOf().optionalFieldOf("deletedVariantPieceIds", List.of())
                            .forGetter(MKWorkspaceDefinitionChangePayload::deletedVariantPieceIds),
                    Codec.BOOL.optionalFieldOf("workspaceSettingsDirty", true)
                            .forGetter(MKWorkspaceDefinitionChangePayload::workspaceSettingsDirty)
            ).apply(instance, MKWorkspaceDefinitionChangePayload::new));

    public MKWorkspaceDefinitionChangePayload {
        workspace = withoutPieces(workspace);
        acceptedRemaps = List.copyOf(acceptedRemaps);
        addedVariants = List.copyOf(addedVariants);
        deletedVariantPieceIds = List.copyOf(deletedVariantPieceIds);
    }

    private static MKStructureWorkspace withoutPieces(MKStructureWorkspace workspace) {
        if (workspace.pieces().isEmpty()) {
            return workspace;
        }
        return new MKStructureWorkspace(workspace.id(), workspace.anchor(), workspace.namespace(),
                workspace.structureName(), workspace.topologyProfile(), workspace.dimensions(), workspace.palette(),
                workspace.stairConfig(), workspace.verticalAccessPlacement(), workspace.shellMargin(),
                workspace.verticalShellMargin(), workspace.exteriorAirMargin(), workspace.previewMargin(),
                workspace.verticalAccessSpec(), workspace.familyDefinitions(), workspace.openingProfiles(),
                workspace.linearRunFamilies(), workspace.insertFamilies(), workspace.createdAt(), workspace.updatedAt(),
                List.of(), workspace.layerStates());
    }
}
