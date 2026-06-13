package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.List;

public record MKWorkspaceTemplateBlockDiffReport(
        int comparedBlockCount,
        int changedBlockCount,
        List<BlockPos> changedPositions
) {
    public static final Codec<MKWorkspaceTemplateBlockDiffReport> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("comparedBlockCount")
                            .forGetter(MKWorkspaceTemplateBlockDiffReport::comparedBlockCount),
                    Codec.INT.fieldOf("changedBlockCount")
                            .forGetter(MKWorkspaceTemplateBlockDiffReport::changedBlockCount),
                    MKWorkspaceCodecs.BLOCK_POS_CODEC.listOf().fieldOf("changedPositions")
                            .forGetter(MKWorkspaceTemplateBlockDiffReport::changedPositions)
            ).apply(instance, MKWorkspaceTemplateBlockDiffReport::new)
    );

    public MKWorkspaceTemplateBlockDiffReport {
        changedPositions = List.copyOf(changedPositions);
    }

    public boolean hasAuthoredChanges() {
        return changedBlockCount > 0;
    }
}
