package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.Optional;

public final class MKWorkspacePaletteResolver {
    public MKWorkspaceMaterialPalette resolveTopologyGroup(MKStructureWorkspace workspace, String topologyGroupId) {
        return workspace.palette();
    }

    public MKWorkspaceMaterialPalette resolveFamily(MKStructureWorkspace workspace, MKWorkspacePaletteFamily family) {
        if (family instanceof MKTowerWorkspaceFamilyDefinition towerFamily) {
            Optional<MKWorkspaceTowerStackSettings> stackSettings = workspace.towerStackSettingsForFamily(towerFamily);
            MKWorkspaceMaterialPalette stackParent = stackSettings.isPresent() ? workspace.palette() :
                    family.paletteTopologyGroupIdOpt()
                            .map(topologyGroupId -> resolveTopologyGroup(workspace, topologyGroupId))
                            .orElse(workspace.palette());
            MKWorkspaceMaterialPalette parent = stackSettings
                    .flatMap(MKWorkspaceTowerStackSettings::paletteOverrideOpt)
                    .map(override -> override.resolve(stackParent))
                    .orElse(stackParent);
            return family.paletteOverrideOpt()
                    .map(override -> override.resolve(parent))
                    .orElse(parent);
        }
        MKWorkspaceMaterialPalette parent = family.paletteTopologyGroupIdOpt()
                .map(topologyGroupId -> resolveTopologyGroup(workspace, topologyGroupId))
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
        String topologyGroupId = piece.tags().get("workspace_topology_group");
        if (topologyGroupId != null && !topologyGroupId.isBlank()) {
            return Optional.of(resolveTopologyGroup(workspace, topologyGroupId));
        }
        return Optional.empty();
    }
}
