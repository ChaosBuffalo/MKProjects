# Tower Workspace V1 Implementation Checklist

## Status

Tower workspace V1 has been implemented and then extended significantly beyond the original shell-only milestone.

This document now serves as a compact summary of what the tower workspace flow is expected to support in its current form.

## Current Deliverable

The tower workspace flow is complete when a designer can:

1. place a tower workspace dev block
2. open the workspace UI
3. create or edit a tower workspace definition
4. choose either:
   - `Create New Workspace`
   - `Load Existing Workspace`
5. configure:
   - namespace
   - structure name
   - room width
   - room length
   - vertical shaft size
   - room height
   - entrance height
   - basement height
   - stair profile settings
   - shell margin
   - preview margin
   - floor / wall / ceiling blocks
6. generate canonical tower scaffold pieces in-world
7. generate stairs into eligible room pieces
8. export the workspace pieces and manifest
9. rehydrate an exported workspace from manifest data
10. use that exported data for datagen/runtime tower generation within a manually authored mod registration flow

## Current Canonical Tower Roles

Tower workspace generation now centers on room pieces:

- `ENTRY`
- `FLOOR_MAIN`
- `BOSS_APPROACH`
- `BOSS_CAP`
- `BASEMENT_ENTRY`
- `BASEMENT_MAIN`
- `BASEMENT_CAP`

The old dedicated stair runtime roles are no longer part of the active tower runtime model.

`MKWorkspacePieceRole` is also now reduced to this same tower-specific set.

## Current Connector Naming

The active tower connector naming scheme is:

- `main_forward`
- `main_back`
- `branch`
- `connect_up`
- `connect_down`
- `boss_forward`
- `boss_back`

`MKConnectorRole` is now shared between workspace and runtime code, and tower authoring uses only those connector roles.

## Current Validation Rules

### Dimension Rules

- `roomWidth` odd
- `roomLength` odd
- `verticalShaftSize` odd
- `doorwayWidth` odd
- `roomHeight >= 3`
- `entranceHeight >= 3`
- `basementHeight >= 3`
- `doorwayHeight >= 2`
- `shellMargin >= 1`
- `previewMargin >= 2`

### Relationship Rules

- `doorwayWidth <= verticalShaftSize <= min(roomWidth, roomLength)`
- `entranceHeight` must be in phase with `roomHeight`
- `basementHeight` must be in phase with `roomHeight`
- `stairWidth` must satisfy `2 * stairWidth <= verticalShaftSize`

### Stair Profile Rules

The workspace UI and server validation now also constrain:

- allowed `roomHeight` values
- allowed `entranceHeight` values
- allowed `basementHeight` values
- allowed `flatRunLength` values
- allowed `stairWidth` values

based on the active stair profile and shaft geometry.

## Current Export Rules

The export area must include:

- room shell geometry
- interior geometry
- jigsaw blocks
- stairs authored into the room piece

The export area must exclude:

- signs
- connector marker blocks
- structure block placement bay

The workspace export also now emits:

- structure NBT
- workspace export manifest
- `mk_jigsaw_piece_meta`

The workspace export manifest also now carries:

- explicit runtime piece metadata derived from authored workspace tags
- connector `incoming_pool` membership used to derive runtime pools from actual connectors

## Current Stair System Checklist

### Room-Local Stair Authoring

- stairs generate directly into room pieces
- runtime tower generation uses room topology only
- no runtime stair child pieces are required

### UI

- room category pages expose stair actions only where applicable
- piece rows show whether stairs have already been generated
- workspace list includes `Generate All Stairs`

### Geometry

- wide stairs support turn landing fill
- bridgeable room-to-room handoff gaps can be slab-filled
- overlapping repeated-room stair patterns are rejected by validation

### Caps

- `boss_cap` stair generation continues only to the landing, not through the whole room
- `basement_cap` stair generation continues down to the room floor without punching through the bottom shell

## Core Implemented Systems

### Capability And Workspace Model

- workspace capability
- persistent workspace definitions
- persistent piece metadata

### Planner And Scaffold

- tower planner
- scaffold builder
- connector markers outside export bounds
- room-only tower topology

### Stair Generation

- generalized stair profiles
- room-local stair generation
- clear/regenerate support
- stair metadata tracking

### Export / Datagen

- workspace manifest export
- metadata export
- export-driven datagen path for runtime tower assets
- namespace-scoped exported-pool bootstrap is reusable by other mods
- `Structure` and `StructureSet` registration remain manually authored per mod

## Current Registration Boundary

The current implementation intentionally keeps a manual/runtime boundary:

- exported manifests can drive runtime pool and metadata generation
- `MKNpc` only registers exported workspaces in the `mknpc` namespace
- `MKNpc` still manually registers `test_tower` as a structure and structure set
- other mods may reuse the exported-workspace pool bootstrap path for their own namespace

This keeps biome tags, structure placement spacing, salt, and related worldgen policy explicit in each mod.

## Main Files

- [MKStructureWorkspace.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKStructureWorkspace.java)
- [MKWorkspaceDimensions.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKWorkspaceDimensions.java)
- [MKTowerWorkspacePlanner.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/planner/MKTowerWorkspacePlanner.java)
- [MKWorkspaceScaffoldBuilder.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/scaffold/MKWorkspaceScaffoldBuilder.java)
- [MKWorkspaceStairBuilder.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/stairs/MKWorkspaceStairBuilder.java)
- [MKWorkspaceScreen.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/client/gui/screens/MKWorkspaceScreen.java)
- [MKWorkspaceExportManifest.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/export/MKWorkspaceExportManifest.java)
