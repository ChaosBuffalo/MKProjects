package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public record MKWorkspaceResolvedFamilySettings(
        MKTowerWorkspaceFamilyDefinition familyDefinition,
        int roomWidth,
        int roomLength,
        int roomHeight,
        int topVoidMargin,
        int bottomVoidMargin,
        MKWorkspaceFoundationPolicy foundationPolicy,
        MKWorkspaceMaterialPalette palette,
        MKWorkspaceTopologySlotMetadata slotMetadata,
        MKWorkspaceVerticalAccessSpec verticalAccessSpec
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
                resolveDimension(stackSettings == null ? 0 : stackSettings.width(), familyDefinition.roomWidth()),
                resolveDimension(stackSettings == null ? 0 : stackSettings.length(), familyDefinition.roomLength()),
                resolveDimension(stackSettings == null ? 0 :
                        stackSettings.heightForTopologySlot(familyDefinition.topologySlotId()),
                        familyDefinition.roomHeight()),
                familyDefinition.topVoidMargin(),
                familyDefinition.bottomVoidMargin(),
                foundationPolicy,
                palette,
                MKWorkspaceTopologySlotMetadata.fromFamily(familyDefinition),
                stackSettings == null ? workspace.verticalAccessSpec() :
                        new MKWorkspaceVerticalAccessSpec(stackSettings.shaftSize(),
                                stackSettings.verticalAccessPlacement(), stackSettings.stairConfig())
        );
    }

    private static int resolveDimension(int inheritedValue, int overrideValue) {
        if (overrideValue > 0) {
            return overrideValue;
        }
        return inheritedValue;
    }
}
