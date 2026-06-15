package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.Optional;

public final class MKWorkspacePaletteResolver {
    public MKWorkspaceMaterialPalette resolveTopologyGroup(MKStructureWorkspace workspace, String topologyGroupId) {
        MKWorkspaceMaterialPalette resolved = workspace.palette();
        for (String ancestorId : MKWorkspaceTopologyGroupSettings.hierarchy(topologyGroupId)) {
            Optional<MKWorkspacePaletteOverride> override = workspace.topologyProfile()
                    .topologyGroupPaletteOverride(ancestorId);
            if (override.isPresent()) {
                resolved = override.get().resolve(resolved);
            }
        }
        return resolved;
    }

    public MKWorkspaceMaterialPalette resolveTowerStack(MKStructureWorkspace workspace, String stackId) {
        MKWorkspaceMaterialPalette groupPalette = resolveTopologyGroup(workspace, stackId);
        return workspace.topologyProfile().verticalStackSettings(stackId)
                .flatMap(MKWorkspaceVerticalStackSettings::paletteOverrideOpt)
                .map(override -> override.resolve(groupPalette))
                .orElse(groupPalette);
    }

    public MKWorkspaceMaterialPalette resolveFloorTopology(MKStructureWorkspace workspace, String stackId,
                                                           String floorRole) {
        MKWorkspaceMaterialPalette stackPalette = resolveTowerStack(workspace, stackId);
        MKWorkspaceMaterialPalette floorGroupPalette = workspace.topologyProfile()
                .topologyGroupPaletteOverride(MKWorkspaceFloorTopologySettings.key(stackId, floorRole))
                .map(override -> override.resolve(stackPalette))
                .orElse(stackPalette);
        return workspace.topologyProfile().floorTopologySettings(stackId, floorRole)
                .flatMap(MKWorkspaceFloorTopologySettings::paletteOverride)
                .map(override -> override.resolve(floorGroupPalette))
                .orElse(floorGroupPalette);
    }

    public MKWorkspaceMaterialPalette resolveFloorTopologyForFamily(MKStructureWorkspace workspace,
                                                                    MKWorkspaceRoomFamilyDefinition family) {
        Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(family.topologySlotId());
        if (slot.isEmpty()) {
            return resolveFamily(workspace, family);
        }
        String stackId = workspace.verticalStackSettingsForFamily(family)
                .map(MKWorkspaceVerticalStackSettings::stackId)
                .orElseGet(() -> MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(family.topologySlotId()).orElse(""));
        if (stackId.isBlank()) {
            return resolveFamily(workspace, family);
        }
        MKWorkspaceMaterialPalette floorPalette = resolveFloorTopology(workspace, stackId, slot.get().suffix());
        return family.paletteOverrideOpt()
                .map(override -> override.resolve(floorPalette))
                .orElse(floorPalette);
    }

    public MKWorkspaceMaterialPalette resolveFamily(MKStructureWorkspace workspace, MKWorkspacePaletteFamily family) {
        if (family instanceof MKWorkspaceRoomFamilyDefinition towerFamily) {
            Optional<MKWorkspaceVerticalStackSettings> stackSettings = workspace.verticalStackSettingsForFamily(towerFamily);
            MKWorkspaceMaterialPalette parent = stackSettings
                    .map(settings -> resolveTowerStack(workspace, settings.stackId()))
                    .orElseGet(() -> family.paletteTopologyGroupIdOpt()
                            .map(topologyGroupId -> resolveTopologyGroup(workspace, topologyGroupId))
                            .orElse(workspace.palette()));
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
        String floorStackId = piece.tags().get("workspace_floor_topology_stack_id");
        String floorRole = piece.tags().get("workspace_floor_topology_floor_role");
        if (floorStackId != null && !floorStackId.isBlank() && floorRole != null && !floorRole.isBlank()) {
            MKWorkspaceMaterialPalette floorPalette = resolveFloorTopology(workspace, floorStackId, floorRole);
            String linearRunId = piece.tags().get("workspace_linear_run_family_id");
            if (linearRunId != null && !linearRunId.isBlank()) {
                return workspace.linearRunFamilies().stream()
                        .filter(linearRun -> linearRun.linearRunId().equals(linearRunId))
                        .findFirst()
                        .map(linearRun -> linearRun.paletteOverrideOpt()
                                .map(override -> override.resolve(floorPalette))
                                .orElse(floorPalette));
            }
            String profileId = piece.tags().get("workspace_floor_room_profile_id");
            String roomKind = piece.tags().get("workspace_floor_room_kind");
            if (profileId != null && !profileId.isBlank() && roomKind != null && !roomKind.isBlank()) {
                return workspace.topologyProfile().floorTopologySettings(floorStackId, floorRole)
                        .flatMap(settings -> floorRoomProfile(settings, profileId, roomKind))
                        .map(profile -> profile.paletteOverride()
                                .map(override -> override.resolve(floorPalette))
                                .orElse(floorPalette));
            }
            return Optional.of(floorPalette);
        }
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

    private Optional<MKWorkspaceFloorRoomProfile> floorRoomProfile(MKWorkspaceFloorTopologySettings settings,
                                                                   String profileId, String roomKind) {
        return floorRoomProfiles(settings, roomKind).stream()
                .filter(profile -> profile.id().equals(profileId))
                .findFirst();
    }

    private java.util.List<MKWorkspaceFloorRoomProfile> floorRoomProfiles(MKWorkspaceFloorTopologySettings settings,
                                                                          String roomKind) {
        return switch (roomKind) {
            case "main_room" -> settings.mainRoomProfiles();
            case "branch_room" -> settings.branchRoomProfiles();
            case "branch_cap" -> settings.branchCapProfiles();
            case "main_cap_approach" -> settings.mainCapApproachProfiles();
            case "main_cap" -> settings.mainCapProfiles();
            default -> java.util.List.of();
        };
    }
}
