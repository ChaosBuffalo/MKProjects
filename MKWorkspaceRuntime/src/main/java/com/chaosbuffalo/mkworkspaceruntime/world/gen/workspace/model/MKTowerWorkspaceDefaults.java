package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.List;

public final class MKTowerWorkspaceDefaults {
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    public static final String PRIMARY_STACK_ID = "tower.primary";

    private MKTowerWorkspaceDefaults() {
    }

    public static MKWorkspaceTopologyProfile topologyProfile() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        return new MKWorkspaceTopologyProfile(PLANNER_ID,
                List.of(new MKWorkspaceVerticalStackSettings(PRIMARY_STACK_ID,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_MAIN_FLOORS,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_FLOORS,
                        dimensions.roomHeight(), dimensions.roomWidth(), dimensions.roomLength(),
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED)),
                List.of(),
                MKWorkspaceTopologyPathSettings.defaults(),
                List.of(),
                TerrainAdjustment.BEARD_THIN);
    }

    public static List<MKWorkspaceRoomFamilyDefinition> roomFamilyDefinitions() {
        return roomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions());
    }

    public static List<MKWorkspaceRoomFamilyDefinition> roomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return List.of(
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("entry", MKWorkspaceVerticalStackSlot.ENTRY,
                        PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(new MKFamilyHorizontalExitDefinition(Direction.SOUTH,
                                MKHorizontalExitPathKind.INGRESS, "main_opening",
                                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION)),
                        0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("floor_main", MKWorkspaceVerticalStackSlot.MAIN_FLOOR,
                        PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("top_cap_approach",
                        MKWorkspaceVerticalStackSlot.TOP_CAP_APPROACH, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("top_cap", MKWorkspaceVerticalStackSlot.TOP_CAP,
                        PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_entry",
                        MKWorkspaceVerticalStackSlot.BASEMENT_ENTRY, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_main",
                        MKWorkspaceVerticalStackSlot.BASEMENT_FLOOR, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_cap_approach",
                        MKWorkspaceVerticalStackSlot.BASEMENT_CAP_APPROACH, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_cap",
                        MKWorkspaceVerticalStackSlot.BASEMENT_CAP, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null)
        );
    }

    public static List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilyDefinitions(
            MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
        return List.of(
                new MKWorkspaceLinearRunFamilyDefinition(
                        "main",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "main_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        true,
                        false,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        MKWorkspaceFoundationPolicy.none(),
                        null
                ),
                new MKWorkspaceLinearRunFamilyDefinition(
                        "branch",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "branch_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        false,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        MKWorkspaceFoundationPolicy.none(),
                        null
                )
        );
    }
}
