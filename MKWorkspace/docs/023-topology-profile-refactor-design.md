# Topology Profile Refactor Design

## Status

Draft design and implementation plan.

## Summary

The workspace structure authoring system has been moving toward a clearer split between topology policy and template-family configuration. Recent changes moved template-specific behavior, such as vertical void margins and six-face exit declarations, onto family definitions. This is the right direction.

This document proposes the next refactor: make the current tower layout one topology profile implementation instead of the shape assumed by the workspace model. A topology profile should describe the coherent structure graph: regions, repeated sequences, placement relationships, and required connections. Family definitions should describe authored templates that can satisfy slots in that graph.

The first non-tower target topology should be a walled keep:

- four small corner towers
- wall runs connecting those towers
- optional gatehouse or wall entry
- a larger central keep stack using the current tower-like vertical structure
- optional courtyard connectors between the central keep, gatehouse, walls, and corner towers

## Goals

- Support multiple structure topologies without cloning the entire workspace system.
- Keep family definitions responsible for template-local shape and connector intent.
- Move tower-specific ordering rules out of generic workspace state.
- Preserve the existing tower workflow while introducing a generalized topology layer.
- Make walled keeps possible with the same family/exit/scaffold/export system.
- Allow future topologies, such as crypt networks, courtyard compounds, villages, and multi-wing buildings.

## Non-Goals

- Replacing the jigsaw runtime system.
- Removing all tower terminology in one change.
- Making topology profiles fully data-driven in the first pass.
- Supporting arbitrary graph solving immediately.
- Migrating legacy workspace data. The system is still under construction, so compatibility can be limited to current development needs.

## Current Model

The current workspace model is centered around the tower topology:

- `MKTowerWorkspaceCategoryProfile`
- `MKTowerWorkspaceFamilyDefinition`
- `MKTowerWorkspaceFloorSettings`
- `MKTowerWorkspacePlanner`
- `MKTowerWorkspaceCategory`
- `MKWorkspacePieceRole`

The tower topology is effectively hardcoded as:

```text
basement_cap
basement_cap_approach?
basement_main * N
basement_entry
entry
floor_main * N
top_cap_approach?
top_cap
```

Families now own more template-specific details:

- room dimensions
- top and bottom void margins
- horizontal exits
- vertical exits
- palette overrides
- horizontal extrusion mode

This makes `MKTowerWorkspaceCategoryProfile` closer to topology policy, but it still mixes tower-specific concepts with category-level defaults.

## Design Principle

The system should have three distinct layers.

### 1. Topology Profile

Describes the structure's coherent layout:

- named regions
- slots within each region
- repeat rules
- adjacency rules
- vertical stack rules
- placement transforms
- required and optional connections
- budget constraints
- planner slot assignment rules

Examples:

- tower
- walled keep
- linear dungeon
- courtyard compound
- branching crypt

### 2. Family Definition

Describes authored template families:

- family id
- allowed region/slot usage
- dimensions
- exits on any face
- vertical access declarations
- void margins
- palette overrides
- local shell/extrusion behavior

Families should not define global structure shape. They define what a template can connect to and where it can be used.

### 3. Planner

Instantiates a topology profile:

- chooses slots
- applies repetition counts
- resolves explicit family assignments
- builds planned pieces
- builds connectors
- emits runtime tags
- creates hallway and transition pools

The planner should be topology-specific, but it should consume shared model types.

## Proposed Model

### Java-Driven Topology Schema

Topology planners should be responsible for declaring the valid vocabulary for each structure type. The topology profile should not let designers invent arbitrary region and slot ids. Instead, a planner exposes stable ids, valid slot kinds, valid links, required settings, optional settings, and validation rules for its topology.

This makes the Java planner the schema provider and interpreter:

- the planner declares which regions and slots exist for a topology type
- the planner declares which settings designers can edit for those regions and slots
- the topology profile stores designer choices against those planner-declared ids
- the planner interprets those choices when creating planned pieces

Candidate schema API:

```text
interface MKWorkspaceTopologyPlanner {
    String profileType();
    MKWorkspaceTopologySchema schema();
    List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace);
    List<String> validateTopology(MKStructureWorkspace workspace);
}
```

Candidate schema model:

```text
MKWorkspaceTopologySchema
  profileType: string
  regions: List<MKWorkspaceRegionSchema>
  slots: List<MKWorkspaceSlotSchema>
  links: List<MKWorkspaceLinkSchema>
  roles: List<MKWorkspaceTopologyRole>
  verticalAccessGroups: List<MKWorkspaceAccessGroupSchema>
  editableSettings: List<MKWorkspaceTopologySettingSchema>
```

For example, the walled keep planner can declare stable region ids such as:

```text
central_keep
corner_tower.default
nw_tower
ne_tower
sw_tower
se_tower
north_wall
east_wall
south_wall
west_wall
gatehouse
courtyard
```

And stable slot ids such as:

```text
central_keep.entry
central_keep.floor
central_keep.top_cap
corner_tower.default.floor
nw_tower.floor
north_wall.segment
south_wall.gatehouse_anchor
```

Designers can edit display names, explicit family assignments, repeat counts, dimensions, access settings, and override settings, but the planner owns the canonical ids and their meaning.

### `MKWorkspaceTopologyProfile`

Introduce a new profile root. This is the saved configuration instance for a planner-declared topology schema.

Candidate fields:

```text
profileId: string
profileType: tower | walled_keep | ...
regionSettings: Map<regionId, MKWorkspaceRegionProfile>
slotSettings: Map<slotId, MKWorkspaceSlotRule>
linkSettings: Map<linkId, MKWorkspaceTopologyLink>
verticalAccessGroups: List<MKWorkspaceVerticalAccessGroup>
settings: topology-specific settings payload
```

The first implementation can keep `settings` strongly typed in Java rather than fully dynamic. Region, slot, and link ids should resolve against the active planner's schema.

### `MKWorkspaceRegionProfile`

Represents a named structural region.

Candidate fields:

```text
regionId: string
regionKind: central_keep | corner_tower | wall_run | gatehouse | courtyard_connector | tower_stack
defaultWidth: int
defaultLength: int
fullHeight: int
minRepeats: int
maxRepeats: int
paletteOverride: optional
slotRules: List<MKWorkspaceSlotRule>
```

This generalizes the useful parts of `MKTowerWorkspaceCategoryProfile`. In the Java-driven model, the planner schema declares which region ids are valid, and this profile stores designer-editable configuration for those regions.

### `MKWorkspaceSlotRule`

Represents a logical slot that requires a family.

Candidate fields:

```text
slotId: string
regionId: string
slotKind: floor | cap | approach | wall_segment | wall_corner | gate | connector | landing
required: boolean
repeat: fixed | range | derived
assignedFamilyId: optional string
assignmentInheritance: none | topology_default | region_default | slot_archetype
requiredExits: Set<Direction>
forbiddenExits: Set<Direction>
```

Tower examples:

- `entry`
- `main_floor`
- `top_cap`
- `basement_floor`

Walled keep examples:

- `nw_tower_floor`
- `north_wall_segment`
- `gatehouse_entry`
- `central_keep_floor`

In the Java-driven model, slot ids are planner-declared stable ids. Designers assign families and editable settings for a slot, but they should not create new slot ids unless the topology type explicitly supports a custom extension point.

### Explicit Family Assignment

Topology profiles should use explicit family assignments rather than automatic family selection. A required slot is valid only when it resolves to a specific family id after inheritance.

The important distinction is between authored slots and generated slot instances.

Authored slot assignments:

```text
central_keep.entry = stone_keep_entry
central_keep.floor = stone_keep_floor
central_keep.top_cap = stone_keep_roof_cap
corner_tower.default.floor = small_corner_tower_floor
north_wall.segment = stone_wall_segment
```

Generated repeated instances inherit from their authored slot:

```text
north_wall.segment[0] -> stone_wall_segment
north_wall.segment[1] -> stone_wall_segment
north_wall.segment[2] -> stone_wall_segment
central_keep.floor[0] -> stone_keep_floor
central_keep.floor[1] -> stone_keep_floor
```

Instance-specific overrides should be supported when designers want variation:

```text
north_wall.segment[3] = cracked_stone_wall_segment
se_tower.floor = southeast_special_tower_floor
```

Assignment inheritance should be explicit and predictable:

```text
topology default assignment
region archetype assignment
slot archetype assignment
slot instance override
```

The planner may still validate that the assigned family satisfies role compatibility, dimensions, exits, void margin rules, and vertical access requirements. It should not silently choose a different compatible family.

This keeps the designer workflow clear: designers know exactly which topology slot they are building a piece for, while repeated slots and shared archetypes avoid making large structures noisy to configure.

### `MKWorkspaceTopologyLink`

Represents an intended connection between two regions or slots.

Candidate fields:

```text
fromSlot: string
fromDirection: Direction
toSlot: string
toDirection: Direction
linkKind: direct | hallway | wall_run | vertical_stack | courtyard_path
openingProfileId: optional
minLength: int
maxLength: int
```

This would eventually replace some of the implicit main/branch/cap behavior in `MKTowerWorkspacePlanner`.

### Linear Run Families

Hallways, wall runs, bridges, ramparts, and courtyard paths have the same topological role: they are linear connector regions between larger structural nodes.

The current `MKHallwayFamilyDefinition` should be replaced early in the refactor by a general linear-run family model. Current hallway behavior should migrate to `runKind = enclosed_corridor`, which keeps the existing hallway use case but removes the tower-specific hallway abstraction before walled keep work depends on it.

Candidate target model:

```text
runId: string
runKind: enclosed_corridor | open_walkway | solid_wall | parapet | custom
runTopology: directed_edge | branching_network
openingProfileId: string
length: int
width: int
height: int
slopeDelta: int
shellMode: enclosed | open | solid_wall | parapet | custom
traversalSurface: interior | top | both | none
projection: rigid | terrain_matching
allowedShapes: Set<straight | corner | t_junction | cross | terminator | plaza>
foundationPolicy: MKWorkspaceFoundationPolicy
allowOnMainPath: boolean
allowOnBranchPath: boolean
runTags: Set<string>
paletteOverride: optional
```

First-pass linear-run kinds:

- `enclosed_corridor`: the generalized form of the current hallway system
- `open_walkway`: exposed path, bridge, courtyard path, or village-style surface walkway
- `solid_wall`: structural wall mass that is not necessarily traversable
- `parapet`: defensive wall run with a traversable top surface

Projection rules:

- `rigid` is the default for enclosed corridors, solid walls, and parapets
- `terrain_matching` is only valid for `open_walkway`
- terrain-matched open walkways are useful for paths that should look built on top of the generated surface
- terrain matching should not be used for walls, parapets, or enclosed corridors because large terrain deltas can make those structures look disjoint

Current hallway/enclosed corridor examples:

```text
runKind: enclosed_corridor
runTopology: directed_edge
shellMode: enclosed
traversalSurface: interior
projection: rigid
allowedShapes: straight, terminator
foundationMode: none
runTags: interior, navigable
```

Walled keep solid wall examples:

```text
runKind: solid_wall
runTopology: directed_edge
shellMode: solid_wall
traversalSurface: none
projection: rigid
allowedShapes: straight
foundationMode: masked_extend_bottom_blocks
foundationMaskBlocks: stone_bricks, cracked_stone_bricks, cobblestone
runTags: exterior, defensive
```

Walled keep parapet examples:

```text
runKind: parapet
runTopology: directed_edge
shellMode: parapet
traversalSurface: top
projection: rigid
allowedShapes: straight
foundationMode: masked_extend_bottom_blocks
foundationMaskBlocks: stone_bricks, cracked_stone_bricks, cobblestone
runTags: exterior, defensive, wall_walk
```

Open walkway/courtyard examples:

```text
runKind: open_walkway
runTopology: branching_network
shellMode: open
traversalSurface: top
projection: terrain_matching
allowedShapes: straight, corner, terminator
foundationMode: none
runTags: courtyard, ground_path
```

#### Directed Edges and Branching Networks

Linear runs have two topology modes.

`directed_edge` fills a known topology edge between known endpoints. The planner already knows the source, target, direction, and length. This is the right mode for:

- perimeter walls
- parapets
- bridges between known anchors
- explicit room-to-room corridors

`branching_network` behaves more like vanilla village paths. The network may grow through authored pieces that introduce new exits, so designers must be able to control which shapes are allowed.

Branch-capable shapes:

```text
straight
corner
t_junction
cross
terminator
plaza
```

Each branching network should declare the enabled shape set and shape usage:

```text
branchingNetwork:
  allowedShapes: straight, corner, t_junction, terminator
  maxDepth: 6
  maxBranches: 12
  terminatorRequired: true

shapeUsage:
  straight:
    allowAsStart: true
    allowAsContinuation: true
    allowAsTerminator: false
  corner:
    allowAsStart: false
    allowAsContinuation: true
    allowAsTerminator: false
  t_junction:
    allowAsStart: false
    allowAsContinuation: true
    allowAsTerminator: false
  terminator:
    allowAsStart: false
    allowAsContinuation: false
    allowAsTerminator: true
```

Allowed shapes are both a generation constraint and an authoring contract:

- disabled shapes are never emitted by the planner
- disabled shapes do not require workspace templates
- enabled shapes require explicit family assignments for their planner-declared slots
- UI should only show family assignment rows for enabled shapes
- validation should fail if an enabled required shape has no assigned family

This keeps branching hallway and path networks legible. Designers can choose a simple corridor chain with only `straight`, `corner`, and `terminator`, or a village-like path network with `t_junction`, `cross`, and `plaza` enabled.

Walls and parapets should use `directed_edge` in the first pass. They should not support branching shapes because branching wall growth would make the walled keep footprint harder to reason about.

### Foundation Policy

Runtime structure placement currently supports a structure-wide `fill_floor` option with a single `fill_state`. `FIRE_SHRINE` uses this to ground the whole shrine footprint with red nether bricks. That works for simple platforms, but topology-driven workspaces need piece/family-level foundation policy.

Foundation policy should live on room families and linear-run families, with topology/region defaults only as inheritance conveniences.

Candidate model:

```text
foundationMode: none | uniform_state | extend_bottom_blocks | masked_extend_bottom_blocks
foundationState: optional BlockState
foundationMaskBlocks: Set<Block>
```

There should be no maximum depth. Foundation fill should continue downward until it hits non-air/non-liquid terrain or reaches the world minimum build height. A very tall grounded support is better than a rigid structure floating over a ravine.

Modes:

`none`:

- no foundation fill
- default for terrain-matched open walkways and pieces that should not modify terrain below themselves

`uniform_state`:

- fill eligible footprint columns downward with `foundationState`
- equivalent to the current `fill_floor` behavior, but scoped to a piece/family instead of the whole structure
- requires `foundationState`

`extend_bottom_blocks`:

- inspect eligible bottom-layer blocks and extend each block's placed state downward
- useful for keeps, towers, walls, and foundations whose bottom layer already represents the intended support material

`masked_extend_bottom_blocks`:

- inspect eligible bottom-layer blocks
- only extend blocks whose block id appears in `foundationMaskBlocks`
- fill downward using the actual placed block state from the template, not a replacement state
- requires a non-empty `foundationMaskBlocks`

Example:

```text
foundationMode: masked_extend_bottom_blocks
foundationMaskBlocks:
  - minecraft:stone_bricks
  - minecraft:cracked_stone_bricks
  - minecraft:cobblestone
```

This lets designers decide which authored blocks behave as supports. A bridge can extend only log posts, a ruined wall can extend only stone structural blocks, and an open walkway can avoid turning every deck plank into a column.

The runtime placement pass should apply foundation policy per placed piece, not once per whole structure footprint. The exported piece metadata or runtime tags should identify the resolved foundation mode and mask/state so `MKJigsawStructure.afterPlace` or a replacement helper can fill beneath each eligible piece bounding box.

Structure-wide `fill_floor` can remain for legacy/simple hand-authored structures, but workspace exports should prefer family-level foundation policy.

Topology links should request compatible linear runs through link kind and tags:

```text
linkKind: wall_run
requiredRunTags: exterior, defensive
```

```text
linkKind: hallway
requiredRunTags: interior, navigable
```

This avoids creating separate hardcoded concepts for every connector-like structure. The planner can resolve all linear links through one mechanism, then let run kind/shell mode determine how the scaffold/export layer builds the template shell.

### `MKWorkspaceVerticalAccessGroup`

Represents a named vertical shaft/stair system.

Candidate fields:

```text
accessId: string
regionId: string
shaftSize: int
placement: center | north | south | east | west
stairConfig: MKWorkspaceStairAuthoringConfig
appliesToSlots: Set<string>
```

This is needed because a walled keep may have:

- central keep vertical access
- four corner tower vertical access groups
- no vertical access in wall runs

The current workspace has one shared `MKWorkspaceVerticalAccessSpec`. That is sufficient for a tower, but too narrow for multi-region structures.

### Multiple Vertical Access Groups

The current system assumes a single vertical access configuration for the whole workspace. That works for a tower because every shaft-enabled room participates in the same vertical stack. A walled keep needs more than that:

- the central keep may have a large stair shaft
- each corner tower may have a smaller ladder or stair shaft
- wall runs usually have no vertical access
- gatehouses may have a short internal stair, a ladder, or no vertical access
- some decorative stacks may have up/down exits for jigsaw attachment but no generated stairs

Vertical access should therefore be modeled as named groups.

#### Group Identity

Every vertical exit should reference a vertical access group either directly or by slot assignment.

Candidate fields:

```text
accessId: string
displayName: string
regionScope: Set<string>
slotScope: Set<string>
shaftSize: int
placement: center | north | south | east | west
stairConfig: MKWorkspaceStairAuthoringConfig
generationMode: stairs | ladder | none | auto
phasePolicy: independent | align_to_group | align_to_profile
alignedToAccessId: optional string
```

Examples:

```text
accessId: central_keep
regionScope: central_keep
shaftSize: 5
placement: center
generationMode: auto
phasePolicy: independent
```

```text
accessId: nw_corner_tower
regionScope: nw_tower
shaftSize: 3
placement: center
generationMode: ladder
phasePolicy: independent
```

```text
accessId: wall_walk_ladder_south
regionScope: gatehouse
shaftSize: 2
placement: north
generationMode: ladder
phasePolicy: independent
```

#### Exit Assignment

Family exits need enough information to identify which vertical access group they belong to.

Candidate addition to six-face family exits:

```text
verticalAccessGroupId: optional string
```

For tower compatibility, this can default to:

```text
default
```

For walled keep:

```text
central_keep_floor:
  up -> central_keep
  down -> central_keep

nw_tower_floor:
  up -> nw_corner_tower
  down -> nw_corner_tower
```

If family exits do not store the group id directly, the slot rule must provide it:

```text
slotId: nw_tower_floor
verticalAccessGroupId: nw_corner_tower
requiredExits: up, down
```

The slot-based approach is probably better for reuse. A single `small_tower_floor` family can be used in all four corner towers, while the topology slot tells the planner which access group applies.

#### Planner Responsibilities

When planning a vertical stack, the planner should:

1. Resolve the slot's vertical access group.
2. Validate family vertical exits against that group.
3. Use the group's shaft size and placement when creating vertical connectors.
4. Tag planned pieces with the group id.
5. Generate only the connectors declared by the family/slot, such as top-only, bottom-only, or both.

Planned piece tags should include:

```text
workspace_vertical_access_group=<accessId>
supports_vertical_access=true
vertical_access_placement=<placement>
vertical_access_direction=up|down|both
```

The existing tags can remain, but `workspace_vertical_access_group` becomes the key that lets stair generation find the right settings.

#### Stair Generation

Stair generation should resolve settings from the planned piece's vertical access group:

```text
accessId = piece.tags["workspace_vertical_access_group"]
group = topologyProfile.verticalAccessGroups[accessId]
stairConfig = group.stairConfig
shaftSize = group.shaftSize
placement = group.placement
```

The current `workspace.verticalAccessSpec()` can remain as a default while tower is migrated.

Terminal behavior should be connector-driven:

- no `UP` connector means do not punch upward; behave like a top terminal
- no `DOWN` connector means do not punch downward; protect the bottom shell
- both connectors means generate a through-shaft/stair

This matches the recent one-sided vertical exit behavior and should become the standard rule.

#### Height Phase and Reusable Band Rules

The current tower system uses one shared stair profile to decide valid band heights. With multiple groups, each group may have different valid heights.

We need explicit phase policy.

`independent`:

- this access group validates only the slots it owns
- corner towers can have a different floor height from the central keep

`align_to_group`:

- this group must use heights compatible with another group
- useful when two shafts must connect to the same landing levels

`align_to_profile`:

- this group must obey topology-wide height bands
- useful when wall walks, towers, and central floors need shared Y levels

For walled keep, likely defaults:

```text
central_keep: independent
corner_towers: align_to_profile if wall walks connect at shared heights, otherwise independent
gatehouse: align_to_profile if it connects to wall walk
```

#### Region and Slot Height Implications

Multiple vertical access groups require region profiles to own height constraints. A region should define:

```text
floorHeightPolicy: fixed | range | derived_from_access_group | align_to_region
fullHeight: int
minHeight: int
maxHeight: int
alignedRegionId: optional string
```

Examples:

- central keep floors derive from `central_keep` access group
- corner tower floors derive from each tower's group
- wall runs align to corner tower wall-walk height
- gatehouse aligns to south wall

#### UI Implications

The topology settings UI needs an access group editor:

- add/remove access groups
- assign group id and label
- set shaft size
- set placement
- set stair mode/rise/materials
- set phase policy
- show which regions/slots use the group

The family editor should not usually ask designers to pick a vertical access group directly. Instead:

- family editor declares top/bottom exits
- topology slot editor assigns the vertical access group
- planner combines the two

This keeps template families reusable.

#### Export and Import

Topology export should include vertical access groups:

```json
{
  "vertical_access_groups": [
    {
      "access_id": "central_keep",
      "region_scope": ["central_keep"],
      "shaft_size": 5,
      "placement": "center",
      "generation_mode": "auto",
      "phase_policy": "independent",
      "stair_config": {}
    },
    {
      "access_id": "nw_corner_tower",
      "region_scope": ["nw_tower"],
      "shaft_size": 3,
      "placement": "center",
      "generation_mode": "ladder",
      "phase_policy": "independent",
      "stair_config": {}
    }
  ]
}
```

During migration, a tower workspace can export one group:

```text
accessId: default
regionScope: entry, main, basement, top_cap, basement_cap
```

#### Validation

Validation should check:

- access ids are unique
- every slot requiring vertical access resolves exactly one group
- every planned vertical connector has a group
- group shaft size fits all families used by scoped slots
- group placement fits all family room footprints
- group stair profile has valid reusable heights for all scoped repeated slots
- phase alignment rules are satisfiable
- top-only and bottom-only terminal pieces do not produce through-shaft generation
- families with vertical exits do not define vertical void margins

#### Walled Keep Example

Initial walled keep access groups:

```text
central_keep
nw_corner_tower
ne_corner_tower
sw_corner_tower
se_corner_tower
```

Optional later groups:

```text
gatehouse
north_wall_walk
south_wall_walk
east_wall_walk
west_wall_walk
```

The corner tower groups can all share the same settings object if we add templates or presets, but they should still be distinct groups because their slots and generated pieces are distinct.

## Family Definition Changes

Family definitions should gain topology usage metadata.

Candidate additions:

```text
familyTags: Set<string>
allowedRegionKinds: Set<string>
allowedSlotKinds: Set<string>
allowedSlotIds: Set<string>
foundationPolicy: MKWorkspaceFoundationPolicy
```

Examples:

```text
familyId: central_keep_floor
allowedRegionKinds: central_keep
allowedSlotKinds: floor
exits: up, down, north, south
```

```text
familyId: north_wall_segment
allowedRegionKinds: wall_run
allowedSlotKinds: wall_segment
exits: east, west
```

```text
familyId: nw_corner_tower_floor
allowedRegionKinds: corner_tower
allowedSlotKinds: floor
exits: up, down, east, south
```

This avoids overloading `MKWorkspacePieceRole` with every possible structure-specific role.

Slot compatibility metadata is validation metadata, not an automatic picker. It tells the editor and validator which topology slots a family is allowed to satisfy. The topology profile still stores the explicit family assignment for each required authored slot.

## Topology-Declared Roles

We should move away from Java enums such as `MKWorkspacePieceRole` as authoring concepts. Roles should be declared by the topology profile as data.

The important distinction is:

- topology roles describe what a slot means inside a specific topology
- runtime roles describe what the current jigsaw/runtime layer needs to generate correctly

Today `MKWorkspacePieceRole` does both. That works for a single tower topology, but it does not scale to walled keeps, gatehouses, wall walks, courtyards, side buildings, or future structure families.

### Proposed Role Model

Add topology role declarations:

```text
roleId: string
roleKind: floor | cap | wall | gate | connector | landing | decorative | custom
runtimeRoleHint: optional existing runtime role
terminal: boolean
startCandidate: boolean
mainPathParticipation: none | entry | continuation | ending
branchParticipation: none | branch | branch_cap
verticalBehavior: none | access | terminal_top | terminal_bottom
requiredExits: Set<Direction>
forbiddenExits: Set<Direction>
tags: Set<string>
```

Examples for tower:

```text
roleId: tower.entry
roleKind: floor
runtimeRoleHint: entry
startCandidate: true
mainPathParticipation: entry
verticalBehavior: access
requiredExits: up, down
```

```text
roleId: tower.top_cap
roleKind: cap
runtimeRoleHint: top_cap
terminal: true
verticalBehavior: terminal_top
requiredExits: down
```

Examples for walled keep:

```text
roleId: keep.central_floor
roleKind: floor
verticalBehavior: access
requiredExits: up, down
```

```text
roleId: keep.north_wall_segment
roleKind: wall
verticalBehavior: none
requiredExits: east, west
```

```text
roleId: keep.gatehouse
roleKind: gate
startCandidate: true
requiredExits: north, south, east, west
```

### Family Role Compatibility

Families should reference topology role ids or role tags instead of enum values:

```text
allowedRoleIds: Set<string>
allowedRoleKinds: Set<string>
familyTags: Set<string>
```

This lets a single authored family opt into multiple topology roles without Java changes.

Examples:

```text
familyId: stone_wall_straight
allowedRoleKinds: wall
requiredExits: east, west
```

```text
familyId: small_corner_tower_floor
allowedRoleIds: keep.nw_tower_floor, keep.ne_tower_floor, keep.sw_tower_floor, keep.se_tower_floor
```

### Runtime Compatibility

During migration, planned pieces can still carry an `MKWorkspacePieceRole` or runtime role hint. The planner should derive that from topology role declarations.

Long term, `MKWorkspacePieceRole` should become either:

- a narrow runtime-only enum used only at the final export/generation boundary, or
- a string-based runtime tag if the jigsaw/runtime layer can support that cleanly

The authoring model should not require adding enum constants for every new topology role.

## Walled Keep Topology

### Layout

Conceptual graph:

```text
nw_tower -- north_wall -- ne_tower
   |                       |
west_wall   central_keep   east_wall
   |                       |
sw_tower -- south_wall -- se_tower
```

Optional additions:

```text
south_wall -- gatehouse
gatehouse -- courtyard_path -- central_keep
corner_tower -- wall_stairs?
central_keep -- courtyard_balcony?
```

### Regions

```text
central_keep
nw_tower
ne_tower
sw_tower
se_tower
north_wall
east_wall
south_wall
west_wall
gatehouse
courtyard
```

### Repeat Rules

Central keep:

- basement cap
- basement floors range
- entry
- upper floors range
- top cap

Corner towers:

- base
- floors range
- cap

### Corner Tower Archetype and Overrides

The walled keep planner should default to one shared corner tower archetype instead of requiring four separate tower definitions.

Planner-declared corner tower instances:

```text
nw_tower
ne_tower
sw_tower
se_tower
```

Shared default configuration:

```text
corner_tower.default:
  base_slot
  floor_slot
  cap_slot
  dimensions
  repeat_range
  assigned_families
  vertical_access_mode
  vertical_access_settings
```

Each corner instance should inherit from the shared default unless the designer opts into a unique override:

```text
nw_tower:
  uses: corner_tower.default

ne_tower:
  uses: corner_tower.default

sw_tower:
  uses: corner_tower.default

se_tower:
  uses: corner_tower.default
  override:
    dimensions: larger
    repeat_range: 4-6
    assigned_families:
      floor: southeast_special_tower_floor
```

The planner still emits distinct region and slot ids for each instance, but the editable settings can be shared:

```text
nw_tower.floor
ne_tower.floor
sw_tower.floor
se_tower.floor
```

This gives designers a simple default while still supporting asymmetric keeps, special gate-adjacent towers, boss towers, taller corner towers, or decorative variants.

Family assignment should happen at the shared archetype level first:

```text
corner_tower.default.floor = small_corner_tower_floor
corner_tower.default.base = small_corner_tower_base
corner_tower.default.cap = small_corner_tower_cap
```

Per-corner family assignments should only be needed when an override is enabled:

```text
se_tower.floor = southeast_special_tower_floor
```

The planner should handle orientation. A single assigned corner tower family can be reused in all four corners if it is rotatable and its exits satisfy the rotated wall attachments.

Walls:

- straight wall segments repeated by side length
- optional gatehouse interruption
- tower attachment segments at endpoints

Courtyard:

- optional path segments
- optional direct connectors

### Vertical Access

Central keep can use the current tower-style shaft.

Corner towers can each have one of:

- no vertical access
- ladder
- small stair shaft
- decorative inaccessible stack

Wall runs usually have no vertical access, but walled keep wall runs should support parapet traversal from the first pass. Parapet runs must align to tower or gatehouse landing levels so the top of the wall is usable.

### Family Requirements

Central keep floor family:

- vertical exits as needed
- optional horizontal courtyard/gate exits

Corner tower floor family:

- up/down if tower has access
- two wall-facing exits depending on corner

Wall segment family:

- horizontal exits on both ends
- no vertical exits
- should be represented as a linear-run family rather than a room family

Gatehouse family:

- wall-run exits east/west
- entry exit south/outside
- courtyard exit north/inside

## Planner Architecture

Introduce a planner registry:

```text
MKWorkspacePlannerRegistry
  profileType -> MKWorkspaceTopologyPlanner
```

Candidate interface:

```text
interface MKWorkspaceTopologyPlanner {
    String profileType();
    MKWorkspaceTopologySchema schema();
    List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace);
    List<String> validateTopology(MKStructureWorkspace workspace);
}
```

`MKTowerWorkspacePlanner` becomes the `tower` implementation.

Add `MKWalledKeepWorkspacePlanner` as the first new topology implementation.

Planner responsibilities:

- declare the topology schema for its `profileType`
- provide stable region, slot, link, and role ids
- define which ids are required, optional, repeated, or overrideable
- expose editable setting definitions for the UI
- validate a saved topology profile against the schema
- interpret the profile settings while producing planned pieces

This keeps topology-specific knowledge in Java where it is useful, while keeping designer intent in exported profile data.

## Migration Strategy

### Phase 1: Add Topology Profile Shell

- Add `MKWorkspaceTopologyProfile`.
- Add `profileType` to workspace settings.
- Keep tower fields in place.
- Create a tower topology profile adapter that reads existing `MKTowerWorkspaceFloorSettings` and category profiles.
- Route planner selection through a registry.
- Add `MKWorkspaceTopologySchema` and have the tower planner expose its current tower regions, slots, links, and roles as stable planner-declared ids.

Result: no behavior change.

### Phase 2: Generalize Category Profiles Into Region Profiles

- Introduce `MKWorkspaceRegionProfile`.
- Map tower categories to region profiles:
  - `entry`
  - `main`
  - `basement`
  - `top_cap`
  - `basement_cap`
- Store region settings keyed by planner-declared region ids.
- Keep old Java class names temporarily if that reduces churn.
- Update export/import to write topology profile data.

Result: tower still works, but the model can describe non-tower regions.

### Phase 3: Replace Hallway Families With Linear Runs

- Introduce `MKWorkspaceLinearRunFamilyDefinition`.
- Migrate existing `MKHallwayFamilyDefinition` data into linear-run families with:
  - `runKind = enclosed_corridor`
  - `runTopology = directed_edge`
  - `shellMode = enclosed`
  - `traversalSurface = interior`
  - `projection = rigid`
  - `allowedShapes = straight, terminator`
  - `foundationMode = none`
- Update tower hallway generation to resolve `enclosed_corridor` linear runs instead of hallway families.
- Add branching-network support to the linear-run model, but keep migrated tower hallway families in `directed_edge` mode until a topology explicitly opts into corridor networks.
- Keep existing hallway export/import fields only long enough to normalize them into linear-run family settings.
- Add validation for first-pass linear-run projection rules.
- Add validation for allowed shape sets and required shape assignments.
- Add `MKWorkspaceFoundationPolicy` to room and linear-run family definitions.

Result: the tower still has hallway behavior, but the model uses the same linear-run abstraction needed by walled keeps.

### Phase 4: Add Slot Rules

- Introduce slot rules for tower roles.
- Move role/category compatibility checks into slot validation.
- Add family usage metadata.
- Add topology-declared roles.
- Move family compatibility from `MKWorkspacePieceRole` enum checks to role ids, role kinds, and family tags.
- Add explicit family assignment rules for authored slots, with inheritance from topology defaults, region archetypes, and slot archetypes.
- Keep `MKWorkspacePieceRole` only as a runtime compatibility hint during transition.

Result: family assignment becomes topology-driven and explicit.

### Phase 5: Multiple Vertical Access Groups

- Replace singular `verticalAccessSpec` in topology planning with named access groups.
- Keep workspace-level default vertical access for tower compatibility.
- Update stair generation to read vertical access group tags from planned pieces.

Result: central keep and corner towers can have different shaft sizes and stair settings.

### Phase 6: Walled Keep Planner

- Add a walled keep topology settings record.
- Generate region slots:
  - central keep stack
  - four corner tower stacks
  - four wall runs
  - optional gatehouse
  - optional courtyard connectors
- Resolve explicit family assignments through region/slot rules.
- Require every generated slot instance to resolve to an explicit family assignment after inheritance.
- Resolve wall runs, bridges, and courtyard connectors through generalized linear run families.
- Support first-pass linear-run kinds: `enclosed_corridor`, `open_walkway`, `solid_wall`, and `parapet`.
- Generate walled keep perimeter wall runs as parapets by default so they are traversable on top.
- Allow `terrain_matching` projection only for `open_walkway` runs.
- Use `directed_edge` topology for solid walls and parapets.
- Allow enclosed corridors and open walkways to opt into `branching_network` topology when the profile enables a controlled shape set.
- Apply family-level foundation policy to generated wall, tower, keep, and path pieces.
- Prefer `masked_extend_bottom_blocks` for solid walls and parapets so authored structural blocks extend downward without extending decorative blocks.
- Prefer `none` for terrain-matched open walkways.
- Emit planned pieces and connector pools.

Result: first non-tower topology.

### Phase 7: UI Refactor

- Replace tower-only pages with topology-aware pages.
- Keep a tower-specific editor for tower topology settings.
- Add a walled keep editor:
  - wall footprint
  - corner tower dimensions
  - shared corner tower defaults
  - optional per-corner tower overrides
  - central keep dimensions
  - wall segment length
  - gatehouse toggle
  - vertical access group settings
- Keep the family editor mostly shared.

Result: designers can author multiple topology types from the same workspace UI.

## Export and Import

Add topology settings to manifests.

Candidate structure:

```json
{
  "workspace_settings": {
    "profile_type": "walled_keep",
    "topology_profile": {
      "regions": [],
      "links": [],
      "vertical_access_groups": []
    },
    "family_definitions": [],
    "opening_profiles": [],
    "linear_run_families": []
  }
}
```

During transition, tower manifests can still export:

```json
{
  "profile_type": "tower",
  "floor_settings": {},
  "category_profiles": []
}
```

The importer can normalize this into an in-memory tower topology profile.

## Runtime Tags

Current tags include tower-specific concepts:

- `topology_role`
- `tower_piece_kind`
- `workspace_category`
- `workspace_family_id`
- vertical access tags
- runtime path tags
- foundation policy tags

For topology profiles, add generic tags:

```text
workspace_topology_type
workspace_region_id
workspace_slot_id
workspace_slot_kind
workspace_family_id
workspace_vertical_access_group
workspace_foundation_mode
workspace_foundation_state
workspace_foundation_mask_blocks
```

Keep existing tower tags while tower runtime code still depends on them.

## Validation Requirements

Generic validation:

- every required authored slot resolves to an explicit assigned family
- every generated slot instance resolves to an explicit assigned family after inheritance
- every assigned family is compatible with the slot role, dimensions, exits, and vertical access requirements
- every required topology link can be satisfied by exits on both sides
- vertical access groups reference valid regions/slots
- family dimensions fit region constraints
- terrain-matching projection is used only by `open_walkway` linear runs
- `directed_edge` linear runs do not enable branching-only shapes such as `t_junction`, `cross`, or `plaza`
- `branching_network` linear runs declare a non-empty allowed shape set
- every enabled required network shape resolves to an explicit assigned family
- disabled network shapes are not emitted by planners and are not required by validation
- `uniform_state` foundation policy defines `foundationState`
- `masked_extend_bottom_blocks` foundation policy defines a non-empty `foundationMaskBlocks` set
- foundation mask blocks are block ids, not full block states
- foundation fill policies are valid for the selected family/run kind
- `parapet` wall runs align to compatible tower or gatehouse landing levels
- repeated regions fit workspace budget
- wall and tower footprints do not overlap unexpectedly
- void margins are invalid on any family with vertical exits

Tower validation:

- same behavior as today
- expressed through tower topology settings

Walled keep validation:

- all four corner tower regions exist
- wall runs connect correct tower pairs
- perimeter wall runs use `parapet` linear runs by default
- corner towers use shared defaults unless an override is enabled
- wall segment count fits requested footprint
- gatehouse, if enabled, interrupts exactly one wall run
- central keep does not overlap walls or corner towers
- courtyard connectors have valid paths
- vertical access groups are assigned only to compatible tower/keep regions

## Testing Plan

### Model Tests

- topology profile codec roundtrip
- tower adapter produces equivalent slot rules to current tower settings
- family usage metadata validates correctly
- hallway family migration creates equivalent `enclosed_corridor` linear-run families
- linear-run family codec roundtrip
- directed-edge linear runs reject branching-only shapes
- branching-network linear runs require explicit assignments for enabled shapes
- disabled branching-network shapes do not create planner slots or validation requirements
- foundation policy codec roundtrip
- foundation policy validates required state or mask fields
- explicit slot family assignments validate correctly
- repeated slot instances inherit family assignments from authored slot definitions
- slot instance overrides replace inherited family assignments
- vertical access groups validate missing/duplicate ids

### Planner Tests

- current tower canonical pieces are unchanged after routing through topology planner registry
- tower with top-only/bottom-only vertical exits still behaves correctly
- walled keep creates four corner tower stacks
- walled keep applies shared corner tower settings by default
- walled keep can override one corner tower without changing the others
- walled keep creates four wall runs
- walled keep wall runs are parapets by default and align to traversal height
- walled keep wall/parapet pieces emit masked foundation policy tags
- open walkway linear runs may use terrain matching
- enclosed corridor, solid wall, and parapet linear runs reject terrain matching
- terrain-matched open walkway pieces default to no foundation fill
- walled keep gatehouse replaces or interrupts one wall run
- central keep stack uses its own vertical access group
- corner towers can independently enable or disable vertical access

### Export/Import Tests

- tower legacy-shaped manifest imports into tower topology profile
- new topology profile manifest roundtrips
- walled keep manifest roundtrips

### UI Tests

- topology type selection controls which topology settings page is shown
- family editor remains shared across topology types
- walled keep editor updates region/slot settings

## Risks

- The current `MKWorkspacePieceRole` enum may become too overloaded if used for every topology. Prefer new slot metadata over adding many enum values.
- Existing runtime generation may still assume tower-style progression. Keep compatibility tags until the runtime layer is generalized.
- Multiple vertical access groups will affect stair generation, scaffold placement, validation, and UI.
- A fully data-driven topology solver could become too broad. Start with explicit Java planners for tower and walled keep.

## Recommended First Implementation Slice

The safest first slice is not the walled keep planner. It is making tower topology explicit while preserving behavior.

1. Add `MKWorkspaceTopologyProfile` with `profileType = "tower"`.
2. Add a `MKTowerTopologySettings` record that wraps current floor settings and category/region policy.
3. Add `MKWorkspaceTopologyPlanner`, `MKWorkspaceTopologySchema`, and planner registry.
4. Have the tower planner declare stable ids for current tower regions, slots, links, and roles.
5. Add `MKWorkspaceLinearRunFamilyDefinition` and migrate current hallway families to `enclosed_corridor` linear runs.
6. Route existing tower workspaces through the registry to `MKTowerWorkspacePlanner`.
7. Add tests proving current tower canonical pieces are unchanged.
8. Export `profile_type` and `linear_run_families` while continuing to import existing hallway fields during transition.

After that, add region profiles and slot rules, then the walled keep planner.
