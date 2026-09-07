# Tower Workspace Stair Generation Plan

## Status

This plan has largely been implemented and now serves as a description of the tower stair-generation architecture.

The current tower workspace system generates stairs directly into room pieces as an authoring pass.

## Purpose

The tower stair system exists to let designers generate functional draft stairs directly into the authored room template space using the workspace UI.

This is an authoring helper.

It is not runtime jigsaw stair generation logic.

## Core Decision

Generated stairs are written directly into the owning room piece.

This means:

- stairs become part of the exported room template
- designers can inspect and edit them in place before saving
- structure block export remains WYSIWYG
- stair generation stays bounded by the room piece export area

Generated stairs are not emitted as separate helper structures.

## Current Authoring Model

The pipeline is:

1. planner emits room pieces and connector topology
2. scaffold builder creates shells, openings, jigsaws, structure blocks, and signs
3. stair builder fills the reserved vertical shaft inside eligible pieces

This keeps topology planning separate from stair authoring assistance.

## Eligible Tower Pieces

Current tower stair generation applies to room pieces that advertise stair participation, including:

- `entry`
- `floor_main`
- `boss_approach`
- `boss_cap`
- `basement_entry`
- `basement_main`
- `basement_cap`

## Stair Builder Responsibilities

`MKWorkspaceStairBuilder` is responsible for:

- validating the selected piece is stair-capable
- computing the reserved shaft footprint
- choosing the resolved generation mode
- clearing previous generated stair geometry for that piece
- placing new stair geometry
- persisting generated stair metadata back to the piece definition

It only modifies the shaft footprint inside the owning piece.

## Supported Stair Profiles

The current system supports:

- `RUN_PROFILE`
  - rise type `STAIR`
  - rise type `SLAB`
  - configurable `flatRunLength`
  - configurable `stairWidth`
- `LADDER`
- `AUTO`
- `NONE`

Legacy `STAIR_STAIRS` and `SLAB_STAIRS` still deserialize and normalize into `RUN_PROFILE`.

## Stair Profile Inputs

The relevant inputs are:

- `Vertical Shaft Size`
- `Room Height`
- `Entrance Height`
- `Basement Height`
- `Rise Type`
- `Flat Run Length`
- `Stair Width`

Those values are constrained by the profile math and by room footprint limits.

## Validation And Reuse Model

The current system no longer relies on only simple cycle divisibility.

`MKTowerStairProfile` now classifies repeated-room handoff as:

- `EXACT`
- `BRIDGEABLE`
- `INVALID_OVERLAP`
- `INVALID_GAP`

The UI and server allow:

- exact handoff heights
- bridgeable handoff heights

They reject:

- overlap cases
- unsupported gap cases

For bridgeable cases, the stair builder emits handoff slab fill across the room boundary landing footprint.

## Geometry Rules

The generator currently handles:

- normal centerline stair bands
- widened stair bands for `stairWidth > 1`
- turn stair rows
- corner landing wedges
- lower landing fill
- upper boundary bridge fill
- post-pass corner gap filling

The current practical goal is:

- no overlaps between repeated room stairs
- no unsupported vertical faces that halt player movement
- room-to-room handoff remains walkable even when the reusable height is only bridgeable rather than perfectly exact

## UI

The workspace UI now exposes:

- stair configuration on the workspace form
- per-room-category stair actions
- per-piece stair-generated indicators
- `Generate All Stairs` in the workspace piece list

The form also now constrains dependent values:

- shaft size depends on room footprint
- stair width depends on shaft size
- entry and basement heights depend on room height
- flat run length depends on current profile compatibility

## Export And Runtime Relationship

Runtime tower generation uses only room pieces.

Stairs are already authored into those pieces before export.

That means:

- export manifest carries room data and stair-related workspace settings
- runtime pools do not need stair child templates
- datagen works from the room-only runtime topology

## What Was Removed

The current tower workflow no longer depends on:

- embedded stair child runtime pools
- dedicated stair piece templates in the runtime tower graph

## Connector Contract

The current tower runtime contract uses the same connector names as the workspace planner:

- the workspace planner emits room-only tower topology
- runtime structure registration uses the active connector set:
  - `main_forward`
  - `main_back`
  - `branch`
  - `connect_up`
  - `connect_down`
  - `boss_forward`
  - `boss_back`
- stairs are authored directly into room pieces
- no dedicated stair child templates are part of the tower workspace flow

Legacy `stair_insert_*` connector names are no longer part of the active tower path.

## Current Open Areas

The main remaining work is continued geometry tuning rather than architectural change.

Typical future refinements:

- additional stair profile presets
- more polished corner shaping for special wide cases
- optional profile presets for gentler climbs

## Main Files

- [MKTowerStairProfile.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKTowerStairProfile.java)
- [MKWorkspaceStairBuilder.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/stairs/MKWorkspaceStairBuilder.java)
- [MKTowerWorkspaceShaftGeometry.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/stairs/MKTowerWorkspaceShaftGeometry.java)
- [MKWorkspaceScreen.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/client/gui/screens/MKWorkspaceScreen.java)
- [MKStructureWorkspaceService.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/MKStructureWorkspaceService.java)
