# Stair Profile And Height System

## Goal

Tower workspace stairs are now treated as a constrained authoring system, not just a block-pattern helper.

We want:

- reusable room templates with baked stairs
- repeated rooms like `floor_main` to remain phase-compatible
- support for different climb profiles:
  - stair rises
  - slab rises
  - configurable flat run segments
  - variable stair width
- UI controls that only expose values that can produce a usable staircase
- handoff validation that rejects overlapping stair phases and allows simple bridgeable gaps

## Current Model

Stairs are authored directly into room pieces.

Runtime tower topology uses room pieces only:

- `entry`
- `floor_main`
- `boss_approach`
- `boss_cap`
- `basement_entry`
- `basement_main`
- `basement_cap`

There are no runtime stair child templates in the current workflow.

The workspace stair builder fills only the reserved shaft footprint inside the selected room variant or base template.

## Stair Profile Inputs

The active stair profile is defined by:

- `vertical shaft size`
- `rise type`
- `flat run length`
- `stair width`

The high-level stair modes are:

- `AUTO`
- `RUN_PROFILE`
- `LADDER`
- `NONE`

Legacy modes still deserialize:

- `STAIR_STAIRS`
- `SLAB_STAIRS`

Those legacy modes are normalized into `RUN_PROFILE` presets.

## Rise Type

`RUN_PROFILE` also specifies a rise type:

- `STAIR`
  - uses stair blocks for rise steps
- `SLAB`
  - uses slabs for half-block rise steps

This matters because alignment is calculated in half-block space.

## Flat Run Length

`flatRunLength` defines how many flat path steps happen after each rise event.

Examples:

- `0`
  - rise every path step
- `1`
  - rise, flat, rise, flat
- `2`
  - rise, flat, flat, rise

This lets a wide tower climb more slowly while consuming more horizontal path length.

## Stair Width

`stairWidth` defines how thick the staircase band is inside the shaft.

The current generator:

- derives a centerline path around the shaft
- expands that path into a walkable band
- handles turns, widened turn rows, and landing fill separately

Allowed widths are constrained by shaft size. The UI and validation reject values where:

- `2 * stairWidth > shaftSize`

So for a shaft size of `5`, valid widths are:

- `1`
- `2`

## Shaft Size

The old `hallway width` field is now surfaced as `Vertical Shaft Size`.

Important behavior:

- shaft size is odd-only so the shaft stays centered cleanly
- shaft size is dependent on room width and room length
- the UI cycles only through valid odd shaft sizes that fit the current room footprint

Changing shaft size rebases dependent values so the form does not get trapped in a narrow leftover option set.

When shaft size changes, the UI now resets:

- `room height`
- `entry height`
- `basement height`
- `flat run length`

to the first valid values for the new shaft configuration.

## Derived Stair Profile

`MKTowerStairProfile` is the shared math model.

It derives:

- cycle length
- rise quantum in half-blocks
- total path steps for a piece height
- allowed room heights
- allowed entry heights
- allowed basement heights
- allowed flat run lengths
- boundary compatibility between repeated room pieces

## Boundary Compatibility

The system no longer treats height validity as only a simple modulo check.

For repeated rooms, the profile classifies boundary handoff as:

- `EXACT`
  - next piece starts adjacent cleanly
- `BRIDGEABLE`
  - there is a small non-overlapping handoff gap that can be patched with slabs
- `INVALID_OVERLAP`
  - the next piece would overlap an existing stair footprint
- `INVALID_GAP`
  - the handoff gap is too large for the supported bridge behavior

Allowed reusable heights are:

- `EXACT`
- `BRIDGEABLE`

Rejected heights are:

- `INVALID_OVERLAP`
- `INVALID_GAP`

This is the reason the UI now constrains height and flat-run options much more aggressively than earlier versions.

## Height Rules

`roomHeight` is the primary repeating-room height.

`entryHeight` and `basementHeight` are now separate values, but both must remain in phase with `roomHeight`.

Current UI behavior:

- `Room Height` cycles only through reusable heights for the current stair profile
- `Entrance Height` cycles only through heights that are valid and phase-aligned with the selected `Room Height`
- `Basement Height` does the same

Changing `roomHeight` snaps:

- `entranceHeight`
- `basementHeight`

to aligned values for the newly selected room height.

## Flat Run Constraints

`flatRunLength` is not a free integer choice anymore.

Allowed values depend on:

- shaft size
- rise type
- stair width
- selected room height

The UI now cycles only through flat-run values that remain compatible with the selected repeating room height.

## Generation Behavior

`MKWorkspaceStairBuilder` now generates directly into room shafts.

Current supported authored patterns:

- stair rise with optional flat run segments
- slab rise with optional flat run segments
- ladder fallback

For stair rise type:

- rise steps use stair blocks
- flat run steps use solid blocks
- turn steps and landing fill are handled separately to keep the path smooth

For slab rise type:

- rise uses alternating bottom/top slabs
- turn landings use slab fill matching the local step geometry

## Landing And Bridge Rules

The generator now includes extra geometry passes so wide stairs remain walkable:

- corner landing fill for normal turns
- lower landing fill at the start of a run
- upper bridge fill for bridgeable room-to-room handoff cases
- post-pass corner gap filling inside the shaft footprint

Important distinction:

- bridge filling is for piece-to-piece handoff
- corner landing filling is for within-piece turn geometry

This means a height can be reusable even if it is not perfectly symmetric, as long as the handoff is bridgeable and non-overlapping.

## UI Summary

The current workspace form order groups dependent sizing values near the top:

- `Room Width`
- `Room Length`
- `Vertical Shaft Size`
- `Room Height`
- `Entrance Height`
- `Basement Height`

The stair profile controls then constrain what remains selectable.

The workspace and category screens also support:

- per-piece stair generation
- per-piece stair clearing
- `Generate All Stairs`
- stair-generated status indicators on piece entries

## Validation

Server-side validation now checks:

- shaft size is valid for current room footprint
- stair width is valid for current shaft size
- room height is allowed for current stair profile
- entrance height is aligned to room height
- basement height is aligned to room height
- flat run length is valid for the selected room height and profile

## Current Practical Notes

- `roomHeight` determines the primary repeating phase
- `entryHeight` and `basementHeight` must align to that phase
- `vertical shaft size` controls shaft footprint, not any of the height fields
- the current system is designed for reusable baked room templates, not runtime stair synthesis

## Main Files

- [MKTowerStairProfile.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKTowerStairProfile.java)
- [MKWorkspaceStairAuthoringConfig.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKWorkspaceStairAuthoringConfig.java)
- [MKWorkspaceDimensions.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKWorkspaceDimensions.java)
- [MKStructureWorkspace.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKStructureWorkspace.java)
- [MKWorkspaceScreen.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/client/gui/screens/MKWorkspaceScreen.java)
- [MKWorkspaceStairBuilder.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/stairs/MKWorkspaceStairBuilder.java)
