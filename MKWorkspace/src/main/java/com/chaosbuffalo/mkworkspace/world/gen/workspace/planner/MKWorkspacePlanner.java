package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public interface MKWorkspacePlanner extends MKWorkspacePiecePlanner {
    ResourceLocation plannerId();

    MKWorkspaceTopologySchema schema();

    MKWorkspaceTopologyProfile createDefaultTopologyProfile();

    default List<MKWorkspaceRoomFamilyDefinition> createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return List.of();
    }

    default List<MKWorkspaceLinearRunFamilyDefinition> createDefaultLinearRunFamilyDefinitions(
            MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
        return List.of();
    }

    default List<String> validateTopology(MKStructureWorkspace workspace) {
        return List.of();
    }

    default Map<String, String> migrateImportedRuntimeTags(MKStructureWorkspace workspace,
                                                           Map<String, String> sourceTags) {
        return sourceTags;
    }

    default boolean allowsRuntimePoolChild(String runtimePoolPath, Map<String, String> childTags) {
        return true;
    }

    default boolean usesRuntimePathFilters(String runtimePoolPath) {
        return true;
    }
}
