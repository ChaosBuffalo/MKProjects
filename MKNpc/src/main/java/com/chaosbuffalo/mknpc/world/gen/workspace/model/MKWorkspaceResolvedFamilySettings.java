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
        MKWorkspaceTowerStackSettings stackSettings = workspace.towerStackSettingsForFamily(familyDefinition)
                .orElse(null);
        MKWorkspaceFoundationPolicy foundationPolicy = familyDefinition.foundationPolicyOverrideOpt()
                .orElseGet(() -> stackSettings == null ?
                        MKWorkspaceFoundationPolicy.none() : stackSettings.foundationPolicy());
        return new MKWorkspaceResolvedFamilySettings(
                familyDefinition,
                stackSettings == null ? familyDefinition.roomWidth() : stackSettings.width(),
                stackSettings == null ? familyDefinition.roomLength() : stackSettings.length(),
                stackSettings == null ? familyDefinition.roomHeight() : stackSettings.height(),
                familyDefinition.topVoidMargin(),
                familyDefinition.bottomVoidMargin(),
                foundationPolicy,
                palette
        );
    }
}
