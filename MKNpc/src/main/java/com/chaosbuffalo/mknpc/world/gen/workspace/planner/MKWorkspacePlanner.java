package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface MKWorkspacePlanner extends MKWorkspacePiecePlanner {
    ResourceLocation plannerId();

    MKWorkspaceTopologySchema schema();

    default List<String> validateTopology(MKStructureWorkspace workspace) {
        return List.of();
    }
}
