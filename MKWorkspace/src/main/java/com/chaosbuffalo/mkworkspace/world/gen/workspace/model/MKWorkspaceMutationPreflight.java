package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceMutationPreflight(
        MKWorkspaceInvalidationReport report,
        MKStructureWorkspace workspaceWithDirtyLayers
) {
    public static final Codec<MKWorkspaceMutationPreflight> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MKWorkspaceInvalidationReport.CODEC.fieldOf("report")
                            .forGetter(MKWorkspaceMutationPreflight::report),
                    MKStructureWorkspace.CODEC.fieldOf("workspaceWithDirtyLayers")
                            .forGetter(MKWorkspaceMutationPreflight::workspaceWithDirtyLayers)
            ).apply(instance, MKWorkspaceMutationPreflight::new)
    );
}
