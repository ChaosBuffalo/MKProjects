package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public record MKWorkspaceResolvedFamilySettings(
        MKTowerWorkspaceFamilyDefinition familyDefinition,
        int roomWidth,
        int roomLength,
        int roomHeight,
        int topVoidMargin,
        int bottomVoidMargin,
        MKWorkspaceFoundationPolicy foundationPolicy,
        MKWorkspaceMaterialPalette palette
) {
    public static MKWorkspaceResolvedFamilySettings from(MKStructureWorkspace workspace,
                                                         MKTowerWorkspaceFamilyDefinition familyDefinition) {
        MKWorkspaceMaterialPalette palette = new MKWorkspacePaletteResolver()
                .resolveFamily(workspace, familyDefinition);
        return new MKWorkspaceResolvedFamilySettings(
                familyDefinition,
                familyDefinition.roomWidth(),
                familyDefinition.roomLength(),
                familyDefinition.roomHeight(),
                familyDefinition.topVoidMargin(),
                familyDefinition.bottomVoidMargin(),
                familyDefinition.foundationPolicy(),
                palette
        );
    }
}
