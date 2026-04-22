# Tower Workspace V1 Implementation Checklist

## Purpose

This checklist breaks the structure workspace authoring spec into a concrete first milestone focused only on the `TOWER` family.

The goal of this milestone is to prove the full authoring loop in `MKNpc`:

- place a dev block
- open an `MKWidgets` UI
- create a tower workspace definition
- generate canonical scaffold pieces in a development world
- persist workspace metadata in a new capability
- allow designers to save those pieces using preconfigured structure blocks

This is not runtime dungeon generation work. This is an authoring tool milestone.

## Deliverable

The Tower V1 milestone is complete when a designer can:

1. place a tower workspace dev block in a world
2. open the workspace UI from the block
3. enter:
   - namespace
   - structure name
   - tower dimensions
   - shell margin
   - preview margin
   - floor / wall / ceiling blocks
4. click generate
5. receive a flat grid of canonical tower scaffold pieces in-world
6. inspect signs and connector markers outside the export area
7. use the structure blocks to save the generated pieces
8. leave and reload the world and still have the workspace metadata persisted

## Out of Scope

Do not include in Tower V1:

- additional variants per role
- dungeon or labyrinth planners
- POIs
- spawners
- loot markers
- runtime dungeon integration
- automatic template export
- decorative passes
- branch logic
- multi-style shell presets

## Canonical Tower Roles

Tower V1 generates exactly one piece for each of these roles:

- `ENTRY`
- `FLOOR_MAIN`
- `STAIRS_UP`
- `STAIRS_DOWN`
- `BOSS_CAP`

These pieces form the initial authoring set for a tower family.

## Naming Contract

### Structure Names

Generated structure block names should be:

- `<namespace>:<structure_name>/entry`
- `<namespace>:<structure_name>/floor_main`
- `<namespace>:<structure_name>/stairs_up`
- `<namespace>:<structure_name>/stairs_down`
- `<namespace>:<structure_name>/boss_cap`

### Connector Names

Use the role-based connector naming scheme:

- `main_forward`
- `main_back`
- `stairs_up`
- `stairs_down`
- `boss_forward`
- `boss_back`

## Validation Rules

### Input Dimensions

Tower V1 should validate:

- `roomWidth` odd
- `roomLength` odd
- `roomHeight >= 3`
- `hallwayWidth` odd and `>= 1`
- `hallwayHeight >= 2`
- `doorwayWidth` odd and `>= 1`
- `doorwayHeight >= 2`
- `shellMargin >= 1`
- `previewMargin >= 1`

### Relationship Rules

- `doorwayWidth <= hallwayWidth <= roomWidth`
- `doorwayHeight <= hallwayHeight <= roomHeight`

### Export Bound Rules

The export area must include:

- shell geometry
- interior geometry
- jigsaw blocks

The export area must exclude:

- signs
- connector marker blocks
- structure block placement bay

### Size Computation Rules

All UI dimensions describe logical interior size.

The scaffold builder computes export bounds using:

- `exportWidth = interiorWidth + (2 * shellMargin)`
- `exportLength = interiorLength + (2 * shellMargin)`
- `exportHeight = interiorHeight + (2 * shellMargin)`

This rule must apply consistently to rooms, stairs, and narrow halls.

## Implementation Checklist

## Phase 1: New Capability

### Task 1. Add new workspace capability types

Create:

- `IMKStructureWorkspaceData`
- `MKStructureWorkspaceDataHandler`
- new attachment registration in `MKNpcAttachments`

Definition of done:

- level-scoped capability exists
- capability can be retrieved from `ServerLevel`
- empty capability survives world load/save cycle

### Task 2. Add workspace core data model

Create:

- `MKStructureWorkspace`
- `MKWorkspaceDimensions`
- `MKWorkspaceMaterialPalette`
- `MKWorkspacePieceDefinition`
- `MKWorkspaceConnectorDefinition`
- `MKWorkspacePieceRole`
- `MKStructureFamilyType`

Tower V1 only needs `TOWER` wired, but the family enum should exist.

Definition of done:

- all records/classes serialize cleanly to NBT
- all validation rules can be enforced from model helpers or service layer

### Task 3. Add capability indexes and CRUD helpers

Implement:

- lookup by workspace id
- lookup by anchor block pos
- create/update/delete workspace
- add/update generated piece metadata

Definition of done:

- one dev block anchor maps to at most one workspace
- workspace data persists across reload

## Phase 2: Dev Block

### Task 4. Add tower workspace dev block

Create:

- `MKWorkspaceDevBlock`
- `MKWorkspaceDevBlockEntity`

Responsibilities:

- use block position as workspace anchor
- store optional `workspaceId`
- open UI on interaction

Definition of done:

- block exists and registers correctly
- block entity persists workspace id

### Task 5. Add client/server open-screen flow for dev block

Add:

- block interaction handler
- packet to open the authoring screen
- logic to send either draft state or existing workspace state

Definition of done:

- right-clicking block opens screen
- existing workspace reloads into screen fields

## Phase 3: Planner and Scaffold Contracts

### Task 6. Add planner abstractions

Create:

- `MKWorkspacePlanner`
- `MKPlannedPiece`

Definition of done:

- planner outputs logical piece plans independent of world placement

### Task 7. Implement `MKTowerWorkspacePlanner`

Emit canonical pieces for:

- entry
- floor_main
- stairs_up
- stairs_down
- boss_cap

Each planned piece must define:

- canonical role
- piece name
- logical dimensions
- connector roles
- intended facing contract

Definition of done:

- identical workspace inputs produce identical planned pieces

### Task 8. Add scaffold builder abstractions

Create:

- `MKWorkspaceScaffoldBuilder`
- `MKWorkspaceGridLayout`

Definition of done:

- builder accepts planned pieces and produces world placements
- planner and scaffold builder remain separate

## Phase 4: Tower Scaffold Generation

### Task 9. Implement preview grid layout

Place all tower pieces in a flat grid extending from the anchor.

Rules:

- horizontal layout only
- fixed columns are acceptable for V1
- use preview margin between cells
- normalize cell size using the largest export footprint

Definition of done:

- no generated piece overlaps another
- signs and structure blocks fit outside export bounds

### Task 10. Build shell geometry

For each piece:

- compute export bounds from interior dimensions + shell margin
- place floor
- place walls
- place ceiling
- carve intended connector openings

Definition of done:

- narrow dimensions like `1x2` openings produce correct enclosed geometry
- shell margin greater than `1` produces thicker outer layers

### Task 11. Place jigsaw blocks

For each connector:

- place jigsaw block centered on the opening axis
- support floor-level connector placement for narrow passages
- set `name`
- set `target`
- set `pool`

Definition of done:

- connectors are centered even for `1`-wide passages
- saved pieces have the expected naming contract

### Task 12. Place structure blocks outside export bounds

For each piece:

- place one structure block outside export area
- configure save mode
- prefill structure name
- compute save offset/size correctly

Definition of done:

- structure block saves exactly the intended piece
- signs, markers, and structure block bay are excluded from export bounds

### Task 13. Place sign and connector markers

For each piece:

- place sign outside export bounds
- place optional connector markers outside export bounds

Recommended sign contents:

- namespace
- structure name
- piece role
- piece name

Definition of done:

- designers can read the piece purpose from the preview area
- markers do not contaminate exported templates

### Task 14. Persist generated piece placements

After scaffold placement, save:

- world origin
- export bounds
- structure block position
- sign position
- connector marker positions

Definition of done:

- re-opening the workspace shows consistent existing placement metadata

## Phase 5: MKWidgets UI

### Task 15. Add `MKWorkspaceScreen`

Create a new `MKScreen`-based UI in `MKNpc`.

Suggested states:

- `tower_form`
- `tower_review`
- `tower_manage`

Definition of done:

- screen opens from dev block
- screen uses `MKWidgets`, not a vanilla container GUI

### Task 16. Add tower form inputs

Form fields:

- namespace
- structure name
- room width
- room length
- room height
- hallway width
- hallway height
- doorway width
- doorway height
- shell margin
- preview margin
- floor block
- wall block
- ceiling block

Definition of done:

- inputs display existing workspace values when reopened
- invalid values are blocked or clearly reported

### Task 17. Add form validation feedback

UI should validate at least:

- odd width constraints
- minimum dimension constraints
- resource location validity

Definition of done:

- user gets immediate useful feedback
- server still performs authoritative validation

### Task 18. Add generate action

UI action:

- `Generate Canonical Tower Pieces`

Behavior:

- sends workspace definition to server
- server validates
- server persists workspace
- server runs planner and scaffold builder

Definition of done:

- clicking generate from UI creates in-world scaffolds

## Phase 6: Networking

### Task 19. Add open-screen packet

Create:

- `OpenWorkspaceScreenPacket`

Definition of done:

- server can push current workspace state to client

### Task 20. Add create/update workspace packet

Create:

- `CreateWorkspacePacket`

Payload:

- anchor position
- namespace
- structure name
- family type
- dimensions
- shell margin
- preview margin
- material palette

Definition of done:

- server can create or update tower workspace definitions

### Task 21. Add generate packet

Create:

- `GenerateWorkspacePacket`

Definition of done:

- server can trigger planner + scaffold builder from UI

## Phase 7: Commands and Debugging

### Task 22. Add tower workspace command root

Suggested commands:

- `/mkworkspace list`
- `/mkworkspace open`
- `/mkworkspace regenerate`
- `/mkworkspace debug`

Definition of done:

- developer can inspect or recover workspace state without relying only on the UI

### Task 23. Add debug logging

Log:

- workspace id
- anchor
- computed export dimensions
- structure names
- planned connectors
- final scaffold positions

Definition of done:

- generation problems can be diagnosed from logs

## Phase 8: Verification

### Task 24. Verify narrow hallway case

Test at minimum:

- hallway width `1`
- hallway height `2`
- doorway width `1`
- shell margin `1`

Expected result:

- scaffold export geometry reflects a `3 x 4` enclosed section
- connector is centered
- marker and sign remain outside export area

### Task 25. Verify thick shell case

Test:

- shell margin `2` or `3`

Expected result:

- export bounds grow correctly
- wall thickness is visibly larger
- structure block save area still excludes authoring aids

### Task 26. Verify persistence

Test:

- create workspace
- generate scaffolds
- save and reload world
- reopen dev block UI

Expected result:

- workspace definition persists
- generated piece placement metadata persists

### Task 27. Verify structure block naming

Expected examples:

- `namespace:structure_name/entry`
- `namespace:structure_name/floor_main`
- `namespace:structure_name/stairs_up`
- `namespace:structure_name/stairs_down`
- `namespace:structure_name/boss_cap`

## Suggested Class List

### Capability

- `IMKStructureWorkspaceData`
- `MKStructureWorkspaceDataHandler`
- registration in `MKNpcAttachments`

### Model

- `MKStructureWorkspace`
- `MKWorkspaceDimensions`
- `MKWorkspaceMaterialPalette`
- `MKWorkspacePieceDefinition`
- `MKWorkspaceConnectorDefinition`
- `MKWorkspacePieceRole`
- `MKStructureFamilyType`

### Block

- `MKWorkspaceDevBlock`
- `MKWorkspaceDevBlockEntity`

### Planner

- `MKWorkspacePlanner`
- `MKPlannedPiece`
- `MKTowerWorkspacePlanner`

### Scaffold

- `MKWorkspaceScaffoldBuilder`
- `MKWorkspaceGridLayout`

### UI

- `MKWorkspaceScreen`

### Networking

- `OpenWorkspaceScreenPacket`
- `CreateWorkspacePacket`
- `GenerateWorkspacePacket`

### Commands

- `MKWorkspaceCommands`

## Milestone Recommendation

If implementation time needs to stay tight, the best sub-milestone is:

1. capability
2. dev block
3. tower planner
4. scaffold builder
5. structure blocks + signs + markers
6. simple UI with generate action

That gives a complete authoring loop before any extra features are added.

