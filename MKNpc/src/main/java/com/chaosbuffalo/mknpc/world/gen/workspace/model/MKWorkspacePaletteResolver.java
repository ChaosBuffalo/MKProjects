package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.Optional;

public final class MKWorkspacePaletteResolver {
    public MKWorkspaceMaterialPalette resolveCategory(MKStructureWorkspace workspace, MKTowerWorkspaceCategory category) {
        return workspace.categoryProfile(category)
                .flatMap(MKTowerWorkspaceCategoryProfile::paletteOverride)
                .map(override -> override.resolve(workspace.palette()))
                .orElse(workspace.palette());
    }

    public MKWorkspaceMaterialPalette resolveFamily(MKStructureWorkspace workspace, MKWorkspacePaletteFamily family) {
        MKWorkspaceMaterialPalette parent = family.paletteCategory()
                .map(category -> resolveCategory(workspace, category))
                .orElse(workspace.palette());
        return family.paletteOverride()
                .map(override -> override.resolve(parent))
                .orElse(parent);
    }

    public Optional<MKWorkspaceMaterialPalette> resolvePiece(MKStructureWorkspace workspace,
                                                             MKWorkspacePieceDefinition piece) {
        String hallwayId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayId != null && !hallwayId.isBlank()) {
            return workspace.hallwayFamilies().stream()
                    .filter(hallway -> hallway.hallwayId().equals(hallwayId))
                    .findFirst()
                    .map(hallway -> resolveFamily(workspace, hallway));
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
