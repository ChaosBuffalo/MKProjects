# Tower Workspace V2 Implementation Plan

## Purpose

This document defines the migration from tower workspace V1 to a more flexible V2 authoring and runtime export flow.

V2 keeps the core tower workspace approach:

- scaffold authoring in-world
- exported NBT templates
- exported runtime metadata and pool hints
- one shared vertical access shaft system for tower-connected rooms

V2 expands the model so designers can:

- configure room width and length per room category or piece family
- expose horizontal branches from any relevant room category
- configure horizontal openings per category
- author hallway families for those openings
- choose which branch-exit combinations to scaffold
- allow non-shaft rooms to use smaller heights while still respecting the current vertical height band
- use a more segmented workspace UI instead of one long flat form

## V2 Goals

1. Support per-category room width and length while keeping one shared vertical access shaft size and aligned shaft placement.
2. Support horizontal branches off entry, basement, tower floors, and future categories.
3. Replace the long V1 settings screen with segmented configuration pages and subviews.
4. Support configurable cardinal branch-exit combinations instead of forcing all pieces of a type to share one branch layout.
5. Support per-category horizontal opening sizes.
6. Support hallway template generation per opening type, including palette overrides, lengths, and limited slope.
7. Allow non-shaft rooms to be shorter than shaft rooms as long as they do not exceed the category's current height band cap.

## Current V1 Constraints

V1 is intentionally narrow and hard-codes several assumptions that now block the desired flexibility:

- workspace dimensions are mostly global
- the tower planner produces a fixed seven-piece canonical set
- connector placement assumes centered horizontal openings
- the UI exposes one long tower-specific form
- export/import schema assumes the V1 global-dimension model

These assumptions currently live primarily in:

- `MKWorkspaceDimensions`
- `MKStructureWorkspace`
- `MKTowerWorkspacePlanner`
- `MKWorkspaceScaffoldBuilder`
- `MKWorkspaceScreen`
- `CreateWorkspacePacket`
- `MKWorkspaceExportManifest`
- `MKStructureWorkspaceImportService`

## High-Level V2 Architecture

V2 should split the tower workspace definition into four layers.

### 1. Workspace-Level Shared Systems

These remain global to the workspace:

- namespace
- structure name
- shell margin
- exterior air margin
- preview margin
- material defaults
- shared vertical access shaft size
- shared vertical access placement policy
- shared vertical stair/ladder authoring profile

This preserves the design requirement that all tower-connected shaft rooms align to one shaft contract.

### 2. Category Profiles

Each category gets its own configuration object. Initial categories:

- `entry`
- `main`
- `basement`
- `boss`

Each category profile should define:

- default room width
- default room length
- room height policy
- whether vertical access is supported
- horizontal opening profile
- hallway family options
- branch variation policy

Later, this can grow into subcategories or family overrides without changing the top-level model again.

### 3. Piece Families

A category should no longer imply one canonical room shape.

A piece family should define:

- category
- topology role
- whether it exposes vertical access
- whether it is main-path, branch-only, boss-only, or terminal-capable
- supported exit masks
- optional family-specific footprint overrides
- optional family-specific height overrides

This is the level that should eventually drive both scaffold generation and runtime metadata grouping.

### 4. Opening And Hallway Families

Horizontal progression and branch openings should become explicit authoring units.

An opening profile should define:

- opening width
- opening height
- whether it is main-path or branch-path oriented
- which hallway families are compatible with it

A hallway family should define:

- compatible opening profile
- hallway interior width and height
- hallway length
- palette override or palette source
- allowed slope delta
- whether it is branch, main-path, or both

## Data Model Changes

## Replace V1 Global Room Dimensions

The V1 `MKWorkspaceDimensions` object currently mixes:

- room footprint
- shaft size
- vertical heights
- doorway size

V2 should split that into separate concepts:

- `MKWorkspaceVerticalAccessSpec`
- `MKTowerCategoryProfile`
- `MKHorizontalOpeningProfile`
- `MKHallwayFamilyDefinition`

The old `MKWorkspaceDimensions` can remain as a compatibility input object for schema V1 imports, but it should stop being the primary authored model.

## New Suggested Model Objects

### `MKWorkspaceVerticalAccessSpec`

Shared workspace-level fields:

- `shaftSize`
- `placement`
- `stairConfig`
- `heightBandPolicy`

### `MKTowerCategoryProfile`

Per category:

- `categoryId`
- `defaultRoomWidth`
- `defaultRoomLength`
- `defaultRoomHeight`
- `minRoomHeight`
- `maxRoomHeight`
- `supportsVerticalAccess`
- `mainOpeningProfileId`
- `branchOpeningProfileIds`
- `hallwayFamilyIds`

### `MKPieceFamilyDefinition`

Per family:

- `familyId`
- `categoryId`
- `baseName`
- `runtimeRole`
- `pieceRole`
- `supportsVerticalAccess`
- `allowOnMainPath`
- `allowOnBranchPath`
- `progressionDelta`
- `verticalLevelDelta`
- `terminal`
- `bossOnly`
- `footprintOverride`
- `heightOverride`
- `selectedExitMasks`

### `MKHorizontalOpeningProfile`

- `openingProfileId`
- `width`
- `height`
- `connectorRole`
- `incomingPoolBaseName`
- `targetPoolBaseName`

### `MKHallwayFamilyDefinition`

- `hallwayFamilyId`
- `openingProfileId`
- `length`
- `interiorWidth`
- `interiorHeight`
- `slopeDelta`
- `paletteOverride`
- `allowOnMainPath`
- `allowOnBranchPath`

## Validation Rules

V2 validation should be explicit and derived from the vertical access profile instead of relying on one global room height.

### Shared Shaft Rules

- shaft size remains global per workspace
- shaft size must be odd
- any room exposing vertical access must have width and length large enough to contain the shaft at the configured placement
- shaft rooms that are reused across floors must satisfy the stair profile reuse rules for that shaft size

### Height Band Rules

For rooms that expose vertical access:

- allowed room height must be derived from the shared shaft size and stair profile
- an ascending shaft room must be validated so its geometry still fits inside the intended height band
- a descending shaft room must be validated the same way relative to the lower bound of the band
- if the room is intended to advance the tower vertically, its top or bottom exit must land exactly where the runtime band math expects

For rooms that do not expose vertical access:

- room height may be smaller than the shaft-compatible height
- room height must not exceed the maximum allowed height of the current category band
- non-shaft rooms do not need to satisfy shaft reuse shape rules

### Horizontal Opening Rules

- centered openings still require odd widths when centered
- explicit side openings may use alternate anchor offsets but must remain fully inside wall faces and shell margins
- opening height must fit inside the room's usable interior height
- hallway slope must not push the far opening outside the current height band unless the piece is explicitly a vertical progression piece

### Branch Exit Rules

- exit masks are represented as cardinal combinations
- only selected masks should produce scaffold templates
- category/family validation must reject impossible masks for a given footprint, shell margin, and opening width

## Planner Refactor

The current planner returns a fixed list of canonical pieces. V2 needs a planner that can build planned pieces from profiles and families.

## V2 Planner Inputs

Planner inputs should include:

- workspace vertical access spec
- category profiles
- piece family definitions
- opening profiles
- hallway families
- selected exit masks per family

## Piece Identity

A planned piece identity should become:

- `category`
- `family`
- `exitMask`
- `variantIndex`
- `pieceKind` such as `template` or `instance`

This replaces the V1 assumption that one role maps to one canonical base piece.

## Exit Mask Model

Use a four-bit cardinal exit mask:

- north
- east
- south
- west

This is preferable to large enums because it:

- matches the design requirement directly
- scales cleanly
- simplifies validation and UI selection
- allows grouped generation logic

Example masks:

- `0001` one-exit room
- `0101` two opposite exits
- `0011` corner exits
- `0111` three exits
- `1111` four exits

## Planner Outputs

The planner should emit:

- room planned pieces
- hallway planned pieces
- optional transition pieces later if needed

Each planned piece must carry:

- explicit interior width, length, and height
- connector anchor definitions
- runtime metadata
- category and family tags
- opening profile references

## Scaffold Builder Refactor

The scaffold builder needs one major conceptual change: stop deriving all horizontal connectors from the room center.

## Connector Anchor Model

A planned connector should include:

- facing
- opening width
- opening height
- lateral anchor offset
- vertical anchor offset
- opening profile id
- target pool
- incoming pool

This allows:

- centered main openings
- branch openings on any cardinal side
- multiple horizontal openings on the same room
- hallways of different opening sizes

## Separate Builders

The scaffold flow should separate:

- room shell generation
- connector opening carving
- hallway shell generation
- structure block and signage placement

This will make it much easier to add hallway families and sloped pieces without overloading the current tower room builder.

## Vertical Access Geometry Integration

The current vertical access geometry code should remain the source of truth for shaft placement, but it must read from the new shared vertical access spec plus per-piece dimensions.

The key V2 rule is:

- shaft alignment is global
- room footprint is local

So shaft center calculation must be stable even when room width and length vary by category.

## Runtime Export And Import Changes

V2 changes the authored model enough that the export schema must be versioned.

## Schema Version

- bump manifest schema from `1` to `2`
- keep schema `1` import support
- convert schema `1` workspaces into V2 defaults during import or load

## Export Additions

Manifest V2 should include:

- workspace vertical access spec
- category profiles
- opening profiles
- hallway families
- piece family metadata
- exit mask metadata
- explicit connector anchors

## Import Behavior

When importing V1:

- map global room width and length into all default categories
- map global heights into entry/main/basement/boss defaults
- create one default opening profile from V1 doorway values
- create one default family per old canonical base name
- mark the imported workspace as using the V2 compatibility layout

## Runtime Metadata Expectations

The runtime metadata path already supports:

- progression deltas
- vertical level deltas
- main path vs branch path eligibility
- terminal and boss flags
- branch depth checks

V2 should continue using that system.

The main runtime additions are:

- richer pools for opening-specific hallway families
- branch-family pools keyed by exit mask or family
- optional future distinction between same-band sloped hallways and true vertical progression pieces

## UI Redesign

The V1 UI is functionally correct but too flat for V2.

## New Top-Level UI Sections

- `Overview`
- `Vertical Access`
- `Category Profiles`
- `Branch Variants`
- `Hallways`
- `Materials`
- `Workspace Pieces`
- `Export`

## Overview Page

Shows:

- namespace
- structure name
- workspace summary
- schema version
- counts by category and family

## Vertical Access Page

Contains:

- shaft size
- shaft placement
- stair mode
- rise type
- flat run length
- stair width
- derived allowed shaft-room heights
- derived height-band notes

This page should make the allowable shaft-room height calculation visible so designers understand why a value is legal or illegal.

## Category Profiles Page

Shows one row or card per category:

- entry
- main
- basement
- boss

Selecting a category opens a detail subpage with:

- room width
- room length
- default height
- min/max height
- supports vertical access
- main opening profile
- branch opening profiles

## Branch Variants Page

This page should be purpose-built for the exit-mask problem.

For each family:

- show a cardinal-direction selector
- let the designer enable or disable masks
- show a generated-template count preview

The page should support:

- generate selected masks only
- clear selected masks
- add variant for selected family and mask

## Hallways Page

This page should manage hallway families grouped by opening profile.

Each hallway family should expose:

- opening profile
- length
- slope delta
- palette source or override
- path type eligibility

## Workspace Pieces Page

The current category view should evolve into a grouped piece browser by:

- category
- family
- exit mask
- variant index

This page should be the main authoring/management surface once a workspace is generated.

## Implementation Phases

## Phase 1: Schema And Model Split

Deliverables:

- introduce shared vertical access spec
- introduce category profiles
- introduce opening profiles
- introduce hallway family definitions
- introduce piece family definitions
- add V2 validation
- keep V1 runtime behavior functionally intact

Primary files:

- `MKStructureWorkspace`
- `MKWorkspaceDimensions` or its replacement
- export manifest classes
- import service
- create workspace packet and related networking

Success criteria:

- workspace can serialize and deserialize in V2 form
- V1 manifests still import
- validation can distinguish shaft rooms from non-shaft rooms

## Phase 2: Planner And Piece Identity Refactor

Deliverables:

- replace fixed canonical planner output with family-driven planned pieces
- add exit mask model
- add explicit connector anchor metadata
- preserve current tower room generation as a subset configuration

Primary files:

- `MKTowerWorkspacePlanner`
- `MKPlannedPiece`
- `MKPlannedConnector`
- workspace service variant logic

Success criteria:

- planner can emit different footprints per category
- planner can emit multiple masks for one category/family
- variant identity includes family plus exit mask

## Phase 3: Scaffold Builder Generalization

Deliverables:

- connector carving uses explicit anchors
- room scaffolds support non-centered horizontal openings
- hallway scaffolds become first-class pieces
- hallway slope validation enforced

Primary files:

- `MKWorkspaceScaffoldBuilder`
- vertical access geometry helper
- piece definition export/import path

Success criteria:

- scaffolded rooms can expose any selected cardinal exits
- hallway templates generate for configured opening profiles
- sloped hallways stay inside band limits

## Phase 4: UI Segmentation

Deliverables:

- replace long form with sectioned state flow
- add category detail pages
- add branch mask editor
- add hallway family editor

Primary files:

- `MKWorkspaceScreen`
- related packets for profile edits if the UI is moved toward partial updates

Success criteria:

- no single page contains the full V1-style field dump
- category-specific editing is straightforward
- selected branch masks are easy to inspect and change

## Phase 5: Export, Import, And Runtime Integration

Deliverables:

- export schema version 2
- import conversion from schema 1
- runtime pool hint generation for hallway families and branch variants
- metadata generation for new piece families

Primary files:

- `MKWorkspaceExportManifest`
- `MKWorkspaceExportManifestWriter`
- `MKWorkspaceExportPieceMetadataWriter`
- `MKStructureWorkspaceImportService`

Success criteria:

- exported V2 workspaces contain enough data to reconstruct the authored layout model
- runtime datagen can consume the new pool and family information

## Phase 6: Migration And Cleanup

Deliverables:

- migrate existing test tower workspace content
- remove obsolete V1-only assumptions
- update docs

Success criteria:

- current test tower can be represented in V2 without loss
- no core logic depends on one workspace-global room footprint

## Specific Answers To The Clarified Questions

## 1. Ascending Shaft Room Height Calculation

Yes. V2 should explicitly compute whether an ascending shaft room fits inside its allowed band.

The rule should be:

- derive shaft geometry from the shared shaft size and stair profile
- compute the room's topmost occupied and opened positions
- verify the piece remains inside the intended vertical band unless it is intentionally a band-advancing piece

This should produce either:

- a set of allowed reusable room heights
- or a max legal room height for the current category/family

The calculation should be surfaced in validation and shown in the UI.

## 2. Independent Room Width And Length

Yes. V2 should allow individual room templates and room possibilities outside vertical access rooms to use different widths and lengths.

The only hard constraint is:

- any room that exposes the shared shaft must still be large enough for that one shared shaft size and placement

Rooms without vertical access should be free to use smaller or otherwise different footprints.

## Testing Plan

## Unit And Logic Tests

- category profile validation
- shaft-room height-band validation
- non-shaft height-cap validation
- exit mask validation
- hallway slope band validation
- V1 to V2 import conversion

## Scaffold Tests

- centered main openings still place correctly
- branch openings on each cardinal side place correctly
- multiple simultaneous branch openings carve correctly
- hallway pieces generate correct export bounds

## Export/Import Tests

- V2 workspace round-trip
- V1 manifest import into V2 model
- connector anchor metadata persistence
- hallway family metadata persistence

## UI Tests Or Manual QA Checklist

- create new workspace using V2 sections
- edit each category independently
- select branch masks and regenerate
- generate hallway families for different opening profiles
- export and re-import successfully

## Risks And Design Notes

- The biggest risk is trying to layer V2 flexibility on top of the V1 global-dimensions model. That will create brittle special cases quickly.
- The second biggest risk is treating hallway generation as implicit geometry instead of first-class authored pieces. Hallways need to be authored families if palette, slope, and opening compatibility matter.
- The planner and scaffold builder should be refactored before major UI work. Otherwise the UI will just expose choices the backend still cannot represent cleanly.

## Recommended Order Of Work

1. model and schema split
2. planner refactor
3. scaffold builder generalization
4. hallway support
5. UI segmentation
6. export/import finalization
7. migrate current tower workspace content

## Definition Of Done

Tower workspace V2 is complete when a designer can:

1. create a tower workspace with one shared vertical shaft contract
2. configure entry, main, basement, and boss categories with different footprints
3. configure per-category horizontal opening sizes
4. choose which branch-exit masks should be scaffolded
5. generate hallway templates for selected opening profiles
6. author non-shaft rooms that are shorter than shaft rooms without violating band limits
7. export the workspace to a V2 manifest
8. re-import that manifest and recover the authored structure layout correctly
