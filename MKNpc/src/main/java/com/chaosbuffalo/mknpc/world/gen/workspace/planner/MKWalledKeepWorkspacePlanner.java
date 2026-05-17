package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;

import java.util.List;
import java.util.Set;

public class MKWalledKeepWorkspacePlanner implements MKWorkspaceTopologyPlanner {
    @Override
    public String profileType() {
        return MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE;
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                profileType(),
                List.of(
                        new MKWorkspaceRegionSchema("keep.center_tower", "tower_stack", true),
                        new MKWorkspaceRegionSchema("keep.corner_towers", "tower_stack", true),
                        new MKWorkspaceRegionSchema("keep.wall_runs", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.parapets", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.walkways", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.gates", "entry", false),
                        new MKWorkspaceRegionSchema("keep.courtyard", "open_area", false)
                ),
                List.of(
                        new MKWorkspaceSlotSchema("keep.center.basement_cap", "keep.center_tower", "cap", "keep.center.basement_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("keep.center.basement_floor", "keep.center_tower", "floor", "keep.center.basement_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("keep.center.entry", "keep.center_tower", "entry", "keep.center.entry", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("keep.center.main_floor", "keep.center_tower", "floor", "keep.center.main_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("keep.center.top_cap", "keep.center_tower", "cap", "keep.center.top_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("keep.corner.shared", "keep.corner_towers", "tower_stack", "keep.corner.shared", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.north_west", "keep.corner_towers", "tower_stack", "keep.corner.north_west", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.north_east", "keep.corner_towers", "tower_stack", "keep.corner.north_east", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.south_east", "keep.corner_towers", "tower_stack", "keep.corner.south_east", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.south_west", "keep.corner_towers", "tower_stack", "keep.corner.south_west", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.wall.north", "keep.wall_runs", "solid_wall", "keep.wall.north", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.wall.east", "keep.wall_runs", "solid_wall", "keep.wall.east", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.wall.south", "keep.wall_runs", "solid_wall", "keep.wall.south", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.wall.west", "keep.wall_runs", "solid_wall", "keep.wall.west", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.north", "keep.parapets", "parapet", "keep.parapet.north", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.east", "keep.parapets", "parapet", "keep.parapet.east", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.south", "keep.parapets", "parapet", "keep.parapet.south", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.west", "keep.parapets", "parapet", "keep.parapet.west", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.north", "keep.walkways", "open_walkway", "keep.walkway.north", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.east", "keep.walkways", "open_walkway", "keep.walkway.east", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.south", "keep.walkways", "open_walkway", "keep.walkway.south", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.west", "keep.walkways", "open_walkway", "keep.walkway.west", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.gate.main", "keep.gates", "entry", "keep.gate.main", MKWorkspaceSlotSchema.Repeat.OPTIONAL)
                ),
                List.of(
                        new MKWorkspaceLinkSchema("keep.center.vertical", "keep.center.basement_cap", "keep.center.top_cap", "vertical_access_group:keep.center"),
                        new MKWorkspaceLinkSchema("keep.corner.north_west.vertical", "keep.corner.north_west", "keep.parapet.north", "vertical_access_group:keep.corner.north_west"),
                        new MKWorkspaceLinkSchema("keep.corner.north_east.vertical", "keep.corner.north_east", "keep.parapet.east", "vertical_access_group:keep.corner.north_east"),
                        new MKWorkspaceLinkSchema("keep.corner.south_east.vertical", "keep.corner.south_east", "keep.parapet.south", "vertical_access_group:keep.corner.south_east"),
                        new MKWorkspaceLinkSchema("keep.corner.south_west.vertical", "keep.corner.south_west", "keep.parapet.west", "vertical_access_group:keep.corner.south_west"),
                        new MKWorkspaceLinkSchema("keep.wall.north", "keep.corner.north_west", "keep.corner.north_east", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.wall.east", "keep.corner.north_east", "keep.corner.south_east", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.wall.south", "keep.corner.south_west", "keep.corner.south_east", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.wall.west", "keep.corner.north_west", "keep.corner.south_west", "linear_run")
                ),
                List.of(
                        new MKWorkspaceRoleSchema("keep.center.entry", "floor", "room", false, true, Set.of("vertical_access", "center_tower")),
                        new MKWorkspaceRoleSchema("keep.center.main_floor", "floor", "room", false, false, Set.of("vertical_access", "center_tower")),
                        new MKWorkspaceRoleSchema("keep.center.top_cap", "cap", "top_cap", true, false, Set.of("terminal_top", "center_tower")),
                        new MKWorkspaceRoleSchema("keep.corner.shared", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "shared_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.north_west", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.north_east", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.south_east", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.south_west", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.wall.north", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.wall.east", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.wall.south", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.wall.west", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.walkway.north", "linear_run", "walkway", false, false, Set.of("open_walkway", "terrain_matched_allowed")),
                        new MKWorkspaceRoleSchema("keep.parapet.north", "linear_run", "parapet", false, false, Set.of("parapet"))
                )
        );
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        throw new UnsupportedOperationException("Walled keep topology schema is available, but canonical keep generation is not implemented yet");
    }
}
