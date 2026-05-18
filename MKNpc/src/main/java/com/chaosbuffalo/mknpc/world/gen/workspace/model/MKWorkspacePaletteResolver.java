package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.Optional;

public final class MKWorkspacePaletteResolver {
    public MKWorkspaceMaterialPalette resolveCategory(MKStructureWorkspace workspace, MKTowerWorkspaceCategory category) {
        return workspace.categoryProfile(category)
                .flatMap(MKTowerWorkspaceCategoryProfile::paletteOverrideOpt)
                .map(override -> override.resolve(workspace.palette()))
                .orElse(workspace.palette());
    }

    public MKWorkspaceMaterialPalette resolveFamily(MKStructureWorkspace workspace, MKWorkspacePaletteFamily family) {
        if (family instanceof MKTowerWorkspaceFamilyDefinition towerFamily) {
            Optional<MKWorkspaceTowerStackSettings> stackSettings = workspace.towerStackSettingsForFamily(towerFamily);
            MKWorkspaceMaterialPalette stackParent = stackSettings.isPresent() ? workspace.palette() :
                    family.paletteCategoryOpt()
                            .map(category -> resolveCategory(workspace, category))
                            .orElse(workspace.palette());
            MKWorkspaceMaterialPalette parent = stackSettings
                    .flatMap(MKWorkspaceTowerStackSettings::paletteOverrideOpt)
                    .map(override -> override.resolve(stackParent))
                    .orElse(stackParent);
            return family.paletteOverrideOpt()
                    .map(override -> override.resolve(parent))
                    .orElse(parent);
        }
        MKWorkspaceMaterialPalette parent = family.paletteCategoryOpt()
                .map(category -> resolveCategory(workspace, category))
                .orElse(workspace.palette());
        return family.paletteOverrideOpt()
                .map(override -> override.resolve(parent))
                .orElse(parent);
    }

    public Optional<MKWorkspaceMaterialPalette> resolvePiece(MKStructureWorkspace workspace,
                                                             MKWorkspacePieceDefinition piece) {
        String linearRunId = piece.tags().get("workspace_linear_run_family_id");
        if (linearRunId != null && !linearRunId.isBlank()) {
            return workspace.linearRunFamilies().stream()
                    .filter(linearRun -> linearRun.linearRunId().equals(linearRunId))
                    .findFirst()
                    .map(linearRun -> resolveFamily(workspace, linearRun));
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null && !familyId.isBlank()) {
            return workspace.familyDefinitions().stream()
                    .filter(family -> family.baseName().equals(familyId))
                    .findFirst()
                    .map(family -> resolveFamily(workspace, family));
        }
        String categoryId = piece.tags().get("workspace_category");
        if (categoryId != null && !categoryId.isBlank()) {
            return Optional.of(resolveCategory(workspace, MKTowerWorkspaceCategory.fromSerializedName(categoryId)));
        }
        return Optional.empty();
    }
}
