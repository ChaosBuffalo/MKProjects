# Structure Workspace Authoring Tool Spec

## Purpose

This document defines the first implementation milestone for a structure authoring tool in `MKNpc`.

The tool generates in-world scaffold templates that already conform to the graph structure and connector topology expected by the MK jigsaw dungeon system. Designers then customize those pieces in a development world and save them using preconfigured structure blocks.

This tool is for authoring templates, not for runtime dungeon generation.

## V1 Scope

V1 includes:

- a new world capability for structure workspace authoring data
- a dedicated dev block that defines the workspace anchor and opens the UI
- an `MKWidgets` screen for creating and managing workspaces
- server-side scaffold generation
- canonical piece generation for `TOWER`
- one canonical variant per role
- shell geometry
- jigsaw blocks
- structure blocks
- signs
- optional connector marker blocks outside the export area
- stair authoring directly into eligible tower room pieces
- workspace export manifest writing
- workspace rehydration from exported manifests

V1 does not include:

- POIs
- spawners
- loot markers
- encounter setup
- automatic decorative passes
- automatic branch-heavy graph authoring
- automatic structure registration from manifests
- `DUNGEON` workspace generation
- `LABYRINTH` workspace generation

Current implementation note:

- `MKNpc` currently supports tower workspaces only
- `MKWorkspacePieceRole` is intentionally reduced to the active tower role set
- broader future-family role taxonomies are not part of the active code path

## Core Authoring Model

The tool creates a logical workspace and then materializes that workspace in-world as a flat preview catalog laid out horizontally in a grid.

Each generated preview piece contains:

- a shell of floor, wall, and ceiling blocks
- correctly placed jigsaw blocks using the agreed naming contract
- one structure block configured for saving the piece
- one sign outside the structure bounds naming the piece
- optional topology marker blocks outside the export area

Designers modify the generated shell and save the final piece from the structure block.

## Key Terminology

### Logical Interior Size

This is the usable authored space requested by the designer.

Examples:

- a hallway with interior size `1 x 2` means:
  - walkable width: `1`
  - walkable height: `2`
- a room with interior size `9 x 9 x 5` means:
  - usable internal floor area: `9 x 9`
  - usable internal height: `5`

### Shell Margin

The scaffold generator adds horizontal shell thickness around the logical interior.

Current tower behavior:

- shell margin expands width and length
- vertical shell thickness for authored room pieces is effectively fixed by the scaffold contract

This means shell margin is mainly a wall-thickness control, not a generic full 3D padding value.

### Export Area

The export area is the structure block save region. It must include:

- the full scaffold shell
- the internal walkable area
- the jigsaw blocks used as part of the piece

It must not include:

- signs
- connector marker blocks
- structure block housing area if it is placed outside the piece

## High-Level Architecture

The implementation is split into six parts:

1. workspace capability
2. dev block and block entity
3. client UI
4. topology planners
5. scaffold builder
6. networking and commands

## New Capability

Create a new level-scoped capability for structure authoring data. Do not reuse `IWorldNpcData`.

### Suggested Interface

```java
public interface IMKStructureWorkspaceData extends INBTSerializable<CompoundTag> {
    Optional<MKStructureWorkspace> getWorkspace(UUID id);
    Optional<MKStructureWorkspace> getWorkspaceByAnchor(BlockPos anchor);
    Collection<MKStructureWorkspace> getAllWorkspaces();

    UUID createWorkspace(MKStructureWorkspace workspace);
    void updateWorkspace(MKStructureWorkspace workspace);
    void deleteWorkspace(UUID id);

    void addPiece(UUID workspaceId, MKWorkspacePieceDefinition piece);
    void updatePiece(UUID workspaceId, MKWorkspacePieceDefinition piece);
}
```

### Required Indexes

- `Map<UUID, MKStructureWorkspace> byId`
- `Map<BlockPos, UUID> byAnchor`
- optional `Map<String, UUID>` keyed by namespace/name combination

### Persistence

- serialize to level NBT
- capability is authoritative for workspace definitions and piece placements
- dev block entity may cache current workspace id, but the capability is the source of truth

## Dev Block

Create a dedicated authoring block in `MKNpc`.

Responsibilities:

- defines the workspace anchor by placement position
- opens the UI on interaction
- stores the current workspace id once a workspace is created
- provides a physical authoring station for designers
- offers both `Create New Workspace` and `Load Existing Workspace`

### Block Entity Data

- `@Nullable UUID workspaceId`

The block position is the workspace anchor. All preview placement is derived relative to this anchor.

## Client UI

Use `MKWidgets` and a dedicated `MKScreen`.

### UI States

- `home`
- `import`
- `workspace_form`
- `manage_workspace`
- room category detail / stair-authoring views

### Required Inputs

- namespace
- structure name
- room width
- room length
- vertical shaft size
- entrance height
- room height
- basement height
- doorway width
- doorway height
- shell margin
- floor block
- wall block
- ceiling block
- piece spacing margin in preview grid

Current implementation note:

- the workspace UI is tower-specific
- `family type` is not a user-facing V1 choice in `MKNpc`
- imported workspace selection is available from the dev block home screen

### Future Inputs

- max row width or preview column count
- add variant count per role
- regenerate selected role

## Dimension and Validation Rules

All horizontal authored dimensions that need centered connectors must be odd.

### Required Validation

- `room_width` odd
- `room_length` odd
- `vertical_shaft_size` odd
- `doorway_width` odd
- `room_height >= 3`
- `entrance_height >= 3`
- `basement_height >= 3`
- `doorway_height >= 2`
- `shell_margin >= 1`
- `preview_margin >= 2`

### Allowed Narrow Cases

Claustrophobic authored spaces are valid.

Examples:

- shaft size `1`
- doorway width `1`
- doorway height `2`

These are valid as long as shell margin rules are respected.

### Relationship Rules

- `doorway_width <= vertical_shaft_size <= min(room_width, room_length)`
- `entrance_height` must stay in phase with `room_height`
- `basement_height` must stay in phase with `room_height`
- `doorway_height <= entrance_height`
- all relevant centered-width values must be odd

## Interior Size Versus Exported Piece Size

This is critical and must be explicit in implementation.

The dimensions entered in the UI describe the desired usable authored space, not the total exported template size.

The scaffold builder computes total exported bounds as:

- exported width = interior width + `2 * shellMargin`
- exported length = interior length + `2 * shellMargin`

In the current tower implementation, height is not expanded by shell margin in the same way. Horizontal shell thickness is configurable; vertical shell behavior is handled separately by the scaffold/stair builder contract.

For narrow passages this means:

- desired interior width `1`
- shell margin `1`
- total enclosed width = `3`

The same horizontal rule applies to rooms unless a specific family planner overrides it.

## Connector Placement Rules

Connectors must be centered on the logical opening.

This includes extremely narrow passages like `1 x 2`.

### Important Runtime Assumption

A jigsaw block may sit in the floor/centerline of a narrow passage in the authored template. During runtime placement it will be removed/replaced as part of normal jigsaw behavior, so the final corridor remains open.

Because of that:

- scaffold generation should optimize for correct saved topology
- it does not need to preserve perfect preview walkability in every narrow case

### Connector Marker Blocks

The scaffold builder should optionally place marker blocks near connectors to make topology readable in-world.

Rules:

- marker blocks must be outside the export area
- marker blocks must not interfere with save bounds
- marker blocks should be near the associated face and easy to identify visually

These markers are only authoring aids.

## Naming Contract

Use role-based connector names.

### Connector Roles

- `main_forward`
- `main_back`
- `branch`
- `connect_up`
- `connect_down`
- `boss_forward`
- `boss_back`

### Structure Name Format

Each generated scaffold piece gets a structure block save name:

- `<namespace>:<structure_name>/<piece_name>`

Examples:

- `mkdev:ashen_tower/entry`
- `mkdev:ashen_tower/floor_main`
- `mkdev:ashen_tower/boss_approach`
- `mkdev:ashen_tower/boss_cap`

## Core Data Model

### `MKStructureWorkspace`

- `UUID id`
- `BlockPos anchor`
- `String namespace`
- `String structureName`
- `MKStructureFamilyType familyType`
- `MKWorkspaceDimensions dimensions`
- `MKWorkspaceMaterialPalette palette`
- `MKWorkspaceStairAuthoringConfig stairConfig`
- `MKTowerStairPlacement towerStairPlacement`
- `int shellMargin`
- `int exteriorAirMargin`
- `int previewMargin`
- `List<MKWorkspacePieceDefinition> pieces`
- `long createdAt`
- `long updatedAt`

### `MKWorkspaceDimensions`

- `int roomWidth`
- `int roomLength`
- `int verticalShaftSize`
- `int entranceHeight`
- `int roomHeight`
- `int basementHeight`
- `int doorwayWidth`
- `int doorwayHeight`

These values describe the logical interior target sizes.

### `MKWorkspaceMaterialPalette`

- `BlockState floorBlock`
- `BlockState wallBlock`
- `BlockState ceilingBlock`

### `MKWorkspacePieceDefinition`

- `UUID pieceId`
- `UUID workspaceId`
- `String pieceName`
- `MKWorkspacePieceRole role`
- `int variantIndex`
- `MKWorkspaceDimensions effectiveDimensions`
- `int shellMargin`
- `List<MKWorkspaceConnectorDefinition> connectors`
- `BlockPos worldOrigin`
- `BoundingBox exportBounds`
- `BoundingBox previewBounds`
- `BlockPos structureBlockPos`
- `BlockPos signPos`
- `List<BlockPos> markerPositions`
- `List<BlockPos> generatedStairPositions`
- `Map<String, String> tags`

Current implementation note:

- `MKWorkspacePieceRole` currently contains only:
  - `ENTRY`
  - `FLOOR_MAIN`
  - `BOSS_APPROACH`
  - `BOSS_CAP`
  - `BASEMENT_ENTRY`
  - `BASEMENT_MAIN`
  - `BASEMENT_CAP`

### `MKWorkspaceConnectorDefinition`

- `MKConnectorRole role`
- `Direction facing`
- `BlockPos relativePos`
- `int openingWidth`
- `int openingHeight`
- `String jigsawName`
- `String jigsawTarget`
- `ResourceLocation targetPool`
- `ResourceLocation incomingPool`

Current implementation note:

- `MKConnectorRole` is shared by workspace and runtime code
- active tower connector roles are:
  - `MAIN_FORWARD`
  - `MAIN_BACK`
  - `BRANCH`
  - `CONNECT_UP`
  - `CONNECT_DOWN`
  - `BOSS_FORWARD`
  - `BOSS_BACK`
- `ROOM`, `TERMINAL`, and `UNKNOWN` exist only as runtime classifier outcomes, not authored tower connector roles

## Family Types and Canonical Roles

### `TOWER`

Canonical roles:

- `ENTRY`
- `FLOOR_MAIN`
- `BOSS_APPROACH`
- `BOSS_CAP`
- `BASEMENT_ENTRY`
- `BASEMENT_MAIN`
- `BASEMENT_CAP`

Characteristics:

- one repeated floor footprint
- surface entrance
- variable floor count later at runtime
- room-to-room vertical topology
- stairs authored directly into room pieces
- cap room is final boss room

### Future Families

`DUNGEON` and `LABYRINTH` remain future planner additions.

The shared workspace model should continue to leave room for them, but `MKNpc` currently implements tower generation only.

That means future-family examples in this document are architectural intent, not active enum surface in the current code.

## Planner Contract

Planners define logical topology only.

```java
public interface MKWorkspacePlanner {
    List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace);
}
```

### Responsibilities

- choose canonical role set for the family
- define connector topology
- define intended connector names and targets
- emit deterministic canonical pieces

### Non-Responsibilities

- world placement
- shell block placement
- signs
- structure block setup

## Scaffold Builder Contract

The scaffold builder turns planned pieces into in-world preview scaffolds.

Responsibilities:

- assign world placements using a flat grid
- compute exported bounds from interior size + shell margin
- build shells
- carve openings
- place jigsaw blocks
- place structure blocks outside export bounds
- place signs outside export bounds
- place optional connector marker blocks outside export bounds
- record world placement metadata back into the capability

Non-responsibilities:

- topology planning
- gameplay markers
- runtime generation logic

## Preview Grid Layout Rules

All family types use the same preview layout model in V1.

Rules:

- pieces are placed on a flat horizontal grid
- a preview cell is larger than the export area to make room for:
  - sign
  - structure block
  - connector marker blocks
  - visual breathing room
- the preview margin is configurable

Recommended V1:

- fixed column count such as `4`
- expand positive `X` and positive `Z` from the anchor
- use the largest piece footprint in the workspace to normalize cell size

## Structure Block Placement Rules

Each piece gets one structure block in `SAVE` mode.

Rules:

- structure block must be outside the export area
- save region must match the piece export area exactly
- structure name must be prefilled
- structure block must be easy to access without entering the export area if possible

The save area must include:

- shell
- interior
- connector jigsaw blocks

The save area must exclude:

- sign
- connector marker blocks
- structure block placement bay

## Sign and Marker Rules

### Sign

Place a sign near each preview piece outside the export area.

Recommended content:

- namespace
- structure name
- piece name
- role

### Connector Marker Blocks

Marker blocks are optional but recommended.

Rules:

- must be outside export area
- should indicate connector role or facing
- should be cheap to place and easy to remove

## Networking

The server is authoritative.

### Packets

- `OpenWorkspaceScreenPacket`
  - server to client
  - sends current workspace or draft state
- `CreateWorkspacePacket`
  - client to server
  - create or update workspace definition
- `GenerateWorkspacePacket`
  - client to server
  - trigger canonical piece generation
- `LoadWorkspaceFromManifestPacket`
  - client to server
  - load an exported workspace at the current anchor

### Server Validation

- player is near the dev block
- dev block exists
- block entity is valid
- dimensions are valid
- namespace and structure name are valid
- placement area is safe to generate into

## Commands

The dev block is the primary interface, but commands are still useful.

Suggested root:

- `/mkworkspace list`
- `/mkworkspace open`
- `/mkworkspace regenerate`
- `/mkworkspace debug`

## Runtime Registration Boundary

Exported workspaces are not themselves runtime registrations.

Current intended boundary:

- exported manifests drive runtime pool and metadata generation
- individual mod implementations still manually register their `Structure` and `StructureSet`
- `MKNpc` only registers exported workspaces in the `mknpc` namespace
- other mods may reuse the same exported-workspace pool bootstrap pattern for their own namespace

This keeps biome tags, placement spacing, salt, and other worldgen policy decisions mod-owned.

## Recommended V1 Delivery Order

1. add new workspace capability
2. add dev block and block entity
3. add workspace model and validators
4. implement planner interface
5. implement `TOWER` planner first
6. implement scaffold builder and preview grid placement
7. add `MKWidgets` screen and networking
8. add commands and debug helpers
9. add later family planners if needed

## Recommended First Milestone

V1 is tower-first and tower-only in `MKNpc`.

Why:

- simplest topology
- repeated footprint
- clear stairs and cap-room flow
- proves odd-dimension centering
- proves shell-margin computation
- proves narrow-connector support
- proves save-bound exclusion for signs and markers

Once that pipeline is solid, `DUNGEON` and `LABYRINTH` can be added as planner additions instead of system redesigns.
