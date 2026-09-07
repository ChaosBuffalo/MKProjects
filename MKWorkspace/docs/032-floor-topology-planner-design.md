# Floor Topology Planner Design

## Problem

Tower and walled keep workspaces currently expose vertical tower-stack controls through a reusable stack editor, but the horizontal path-depth controls still live as coarse topology-group settings. Those settings describe behavior such as main path length and branch cap depth, but they are not really whole-workspace settings. They describe what happens when a single tower floor has horizontal exits.

This becomes confusing for embedded tower stacks in the walled keep planner. The center tower and corner towers can each have their own stack settings, but the horizontal progression controls are not exposed in the subtower editor. Copying the current global sliders into the walled keep page would fix the missing controls while preserving the wrong model.

The deeper model is:

- A tower stack creates vertical floor roots.
- A floor topology expands horizontally from exits on each floor root.
- Hallways, rooms, approaches, and caps are horizontal topology pieces that should be configurable and authorable.

## Goals

- Introduce a reusable floor topology layer for horizontal dungeon-style generation.
- Use existing tower-stack floor pieces as the floor roots.
- Let standalone tower and walled keep tower stacks share the same floor topology controls.
- Let designers author templates for important horizontal categories without being forced to understand every generated directional variant.
- Support hallways, rooms, branch caps, main approaches, and main caps as explicit topological categories.
- Make floor topology settings visible inside the selected floor region of the tower stack side preview widget.
- Keep parent planners responsible for required exits and stack composition.

## Non-Goals

- Do not replace the tower stack planner. The floor topology planner is a child layer.
- Do not make rooms fully recursive junctions in the first version.
- Do not require a separate `floor_entry` authored template family. Existing tower stack pieces are the floor roots.
- Do not expose every possible hallway shape as a required designer template by default.
- Do not rely on vanilla jigsaw rotation for compact authoring.

## Terminology

### Tower Stack

The vertical topology primitive. It decides which layers exist, how many main and basement floors are generated, where caps and approaches appear, and how vertical access is configured.

### Floor Root

The tower-stack piece generated for a specific active layer. Examples:

- `entry`
- `main_floor`
- `basement_floor`
- `top_cap`
- `basement_cap`

The floor root is the anchor for horizontal generation. It can contain required topological exits, authored optional exits, vertical shaft access, and the first jigsaws that seed the floor topology.

### Floor Topology

The reusable horizontal topology layer that expands from a floor root. It decides how main paths, branch paths, rooms, approaches, and caps are generated.

### Main Path

The primary horizontal progression from a floor root. It has min and max depth controls and may optionally end through a main approach before placing a main cap.

### Branch Path

A secondary horizontal path spawned from a floor root, hallway, junction, or future room connector. It has a branch-depth budget and terminates in a branch cap.

### Room

A content-bearing horizontal destination with configurable footprint. For the first version, rooms should behave as terminal or near-terminal content, not arbitrary recursive path nodes.

### Room Connector

A transition between hallway/path pieces and a room. This gives designers control over doors, thresholds, arches, locks, traps, lighting, or other connection details.

### Main Approach

An optional transition immediately before the main cap. This mirrors vertical cap approach behavior in the tower stack but runs horizontally.

### Main Cap

The terminal piece for the main path. It should be separate from branch caps because the end of the main path is usually more important content.

### Branch Cap

The terminal piece for a branch path. This can be a dead end, small room facade, collapsed passage, storage nook, or other low-commitment ending.

## Topological Categories

The first reusable floor topology should support these categories:

| Category | Purpose | Default Authoring |
| --- | --- | --- |
| Floor Root | Existing tower-stack floor piece that starts horizontal generation | Authored as tower stack piece |
| Hall Segment | Continues main or branch path | Compact single authoring template with export variants |
| Junction | Allows side branches from a path | Compact authoring template with export variants |
| Room Connector | Connects path topology to a room | Compact authoring template with export variants |
| Room | Content footprint reached from path topology | Configurable size, likely one compact template per room profile |
| Branch Cap | Terminates branch paths | Compact authoring template with export variants |
| Main Approach | Optional transition before main cap | Compact authoring template with export variants |
| Main Cap | Terminates main paths | Compact authoring template with export variants |

The default designer-facing set should stay small:

- floor root
- hall segment
- junction
- room connector
- room
- branch cap
- main approach
- main cap

Additional shapes such as turns, cross junctions, locked-room connectors, or special caps can be opt-in variants later.

## Model Shape

The current `MKWorkspaceTopologyPathSettings` is keyed by broad topology group ids such as `entry`, `main`, `basement`, `top_cap`, and `basement_cap`. That is too coarse once a workspace contains multiple tower stacks.

The new settings should be scoped to a tower stack and floor role.

Suggested first-version model:

```text
MKWorkspaceFloorTopologySettings
  stackId
  floorRole
  minMainPathPieces
  maxMainPathPieces
  maxBranchPiecesBeforeCap
  hallwayLeadInMode
  manualHallwayLeadInPieces
  mainApproachEnabled
  roomGenerationEnabled
  roomProfiles
```

Example bindings:

```text
tower.primary.main_floor
keep.center.main_floor
keep.center.basement_floor
keep.corner.shared.main_floor
keep.corner.north_west.main_floor
```

This direct scoped model is easier to implement than reusable presets. Reusable floor-topology presets can be added later if designers need to share one horizontal profile across many stacks.

## Room Profiles

Rooms should support an arbitrary number of configurable profiles for both main path rooms and branch rooms. A room profile describes content placed inside the horizontal floor topology. It does not describe the main approach, main cap, or branch cap; those remain separate topological categories.

Room profiles should be stored directly on the floor topology setting. This encourages designers to make floor-specific content instead of pointing every floor at one shared room catalog. If reusable room catalogs become useful later, they can be added as an explicit opt-in preset feature rather than the default authoring model.

Suggested first-version profile fields:

```text
id
label
kind = MAIN_ROOM | BRANCH_ROOM
width
length
height
weight
paletteOverride
```

The floor topology should create one starter main room profile and one starter branch room profile when a designer opts a floor into horizontal dungeon generation:

```text
id = main_room
label = Main Room
kind = MAIN_ROOM
width = selected floor root width
length = selected floor root length
height = selected floor band height
weight = 1
paletteOverride = inherit

id = branch_room
label = Branch Room
kind = BRANCH_ROOM
width = selected floor root width
length = selected floor root length
height = selected floor band height
weight = 1
paletteOverride = inherit
```

Room footprints should use odd dimensions where practical so connector centers line up cleanly. The UI should validate room sizes against the selected floor root, the hallway lead-in recommendation, parent reservations, and known jigsaw placement limits.

Room height is configurable but must stay inside the owning vertical band:

```text
minimumHeight = maximum required horizontal opening height for the room's connector schema
maximumHeight = owning floor band height
```

If the floor band height changes, room heights should snap down or report validation warnings when they exceed the new maximum. A floor topology room should not expose vertical access settings. For now, a floor topology occupies exactly one vertical band; vertical movement remains owned by the tower stack.

Room connector rules should be enforced by role schema:

| Role | Required Connectors | Optional Connectors | Forbidden Connectors |
| --- | --- | --- | --- |
| Main Room | `main_in`, `main_out` | `branch_out` | `branch_in`, vertical access |
| Branch Room | `branch_in` | `branch_out` | `main_in`, `main_out`, vertical access |
| Main Approach | `main_in`, `main_out` | `branch_out` | `branch_in`, vertical access |
| Main Cap | `main_in` | `branch_out` | `main_out`, `branch_in`, vertical access |
| Branch Cap | `branch_in` | none for first version | `main_in`, `main_out`, `branch_out`, vertical access |

The room editor should reuse the same horizontal exit widget used for floor-root rooms, but in a schema-filtered mode. Only connectors valid for the selected room role should be shown or editable. Vertical access options should not appear for floor topology rooms, approaches, or caps.

Continuation is controlled by authored exits:

- A main room with `main_out` can continue the main path.
- A branch room with `branch_out` can continue or split a branch route.
- A main cap cannot continue the main path because `main_out` is forbidden.
- A branch cap cannot continue in the first version.

Rooms can later gain richer behavior:

- locked room connectors
- treasure/combat/puzzle tags
- biome or faction weighting
- optional exits
- nested room clusters

Those should not be required for the first planner.

## Generation Shape

At runtime, the tower stack planner emits floor roots. The floor topology planner receives the floor root exits and expands horizontal content.

High-level flow:

```text
tower stack planner
  -> create vertical floor root pieces
  -> for each active floor root:
       floor topology planner
         -> classify exits by path kind
         -> create main path pools and depth metadata
         -> create branch path pools and cap metadata
         -> create room connector and room pools
         -> create optional main approach and main cap pools
```

Main path:

```text
floor root
  -> main hall or junction sequence
  -> optional main room connectors
  -> main room
  -> optional continuation
  -> optional main approach
  -> main cap
```

Branch path:

```text
floor root or junction
  -> branch hall sequence
  -> optional room connector
  -> room
  -> optional continuation
  -> branch cap
```

Branches should generally behave as a hallway leading to a room, with optional continuation, and finally a special terminal room represented by the branch cap. The branch cap mirrors the main cap concept: it is not just a generic dead end, it is the guaranteed terminal content for that branch route.

The planner should keep branch-depth and main-depth budgets separate. A branch cap should not satisfy a main path ending, and a main cap should not be used as a generic branch ending.

Main and branch routes should share the same hallway and junction template categories. The distinction between main and branch is primarily narrative and budgeting:

- the main path lets the designer guarantee a floor-level gameplay progression
- the main path can end with an optional approach and guaranteed main cap encounter
- branches provide side exploration and side rooms
- branches eventually terminate in branch-cap content

By default, canonical tower floors should not have horizontal exits. A default tower is just a vertical sequence of authored rooms. Designers opt into dungeon-style behavior by adding horizontal exits to a floor root and configuring that floor's topology.

## Footprint Fitting

Floor topology footprint fitting should answer this question before runtime generation:

```text
Given this floor root, exits, path budgets, hallway dimensions, room profiles, and jigsaw cap,
what is the maximum horizontal space this floor topology may occupy?
```

The first version does not need to predict the exact random jigsaw result. It should calculate a conservative envelope for each enabled exit direction and validate that the configured topology has room to generate without immediately colliding with known parent-planner reservations.

For a standalone tower, fitting can mostly be local:

- respect Minecraft's structure placement radius
- account for the floor root footprint
- account for maximum main path depth
- account for maximum branch path depth
- account for largest enabled room and cap footprints
- report directional extents from the tower center

For a composed planner like the walled keep, fitting needs parent context:

- center tower floors must fit inside the keep wall interior unless explicitly allowed to generate outside it
- corner tower floors must avoid perimeter wall and courtyard reservations
- floor topology extents should be included in the walled keep footprint preview and jigsaw cap calculation
- if a floor topology cannot fit, the planner should disable that floor's horizontal expansion and report it instead of failing the entire keep
- parent planner fit failures should be reflected on the owning tower-stack floor region in the UI

The planner should calculate two levels of fit:

1. Hard placement fit: the maximum possible extent must fit inside the legal jigsaw radius for the structure.
2. Parent reservation fit: the maximum possible extent must not overlap parent-owned reserved space such as walls, gatehouses, central tower footprint, courtyard paths, or other deterministic slots.

For simple directional exits, the envelope can be calculated from component spans:

```text
pathEnvelope =
  floorRootHalfExtent
  + connectorClearance
  + hallwayLeadInPieces * hallSpan
  + maxPathPieces * hallSpan
  + optionalApproachSpan
  + max(terminalCapSpan, largestRoomSpan)
```

Branches add side envelopes from any junction that can spawn them:

```text
branchSideEnvelope =
  junctionSideConnectorClearance
  + maxBranchPiecesBeforeCap * hallSpan
  + largestBranchRoomSpan
  + branchCapSpan
```

The exact implementation should use oriented rectangles rather than scalar radii wherever possible. That lets the UI show north/south/east/west extents and lets composed planners test overlap against reserved rectangles.

Footprint fitting should remain deterministic and settings-driven. If planner fitting and exported template bounds disagree, that is a bug in planner/export integration, not something to hide with arbitrary padding.

### Recommended Hallway Lead-In

Floor topology should calculate a recommended minimum hallway lead-in before wide rooms or caps can begin. This prevents a room placed immediately after a floor-root exit from colliding with the floor root or with content from adjacent exits.

The recommendation should be based on clearance rather than aesthetics. It should consider:

- floor root width and length
- exit direction
- hallway width and span
- connector footprint
- largest enabled room footprint
- largest enabled main and branch cap footprints
- adjacent enabled exits
- parent planner reservations
- hard jigsaw placement radius

Suggested setting shape:

```text
hallwayLeadInMode = AUTO | MANUAL
manualHallwayLeadInPieces
```

In `AUTO`, the planner uses the recommended lead-in from the sizing report. In `MANUAL`, the designer can choose a shorter or longer lead-in for aesthetic reasons. Manual values below the recommendation should remain selectable but should produce a visible warning unless they create a hard placement failure.

The sizing report should expose:

```text
recommendedHallwayLeadInPieces
effectiveHallwayLeadInPieces
hallwayLeadInWarnings
```

The topology UI should show the recommendation near the hallway lead-in control and visualize the lead-in as the clear spacer between the floor root and the first room-capable placement.

## Exits

The parent tower stack remains responsible for required topological exits.

Examples:

- The entry floor may require a fixed south main entry.
- A cap approach may require a fixed vertical connector.
- A walled keep subtower may require a specific exterior or wall-facing exit.

Required exits should be visible in the floor-root editor but locked against mutation. The floor topology settings should decide what those exits generate into, not whether the required exit exists.

Optional authored exits remain designer-configurable through the existing exit mask style controls.

## Template Authoring

Designers should not need to author every direction variant.

Use compact authoring for default floor topology pieces:

- Author one canonical hall segment.
- Author one canonical junction.
- Author one canonical room connector.
- Author one canonical branch cap.
- Author one canonical main approach.
- Author one canonical main cap.

Export should create runtime variants with rotated blocks and rewritten jigsaw metadata. This should reuse the compact rotated authoring machinery already planned and implemented for repeated wall and corner tower pieces.

The authoring workspace should show the compact logical pieces. Runtime-only directional variants should be written during export, not exposed as extra designer templates.

## UI Placement

The floor topology editor should live inside the tower stack side preview widget.

When a floor region is selected, the region options should include:

- floor root height and count controls, where applicable
- required and optional horizontal exits
- floor topology summary
- main path min and max controls
- branch cap depth
- main approach toggle
- room generation toggle
- room profile list or entry point to a room profile subpage

This placement should be shared by:

- standalone tower topology page
- walled keep center tower tab
- walled keep shared corner tower tab
- walled keep unique corner tower tabs

The old global path-depth section on the standalone tower page should be removed once scoped floor topology settings are implemented.

## Visualization

The floor topology needs a small horizontal preview that complements the tower stack side preview.

The preview should render a generation envelope, not an exact promised layout. It should answer:

```text
Given the currently enabled templates and settings,
what is the largest shape this floor topology is allowed to create?
```

The first preview can be schematic:

- floor root in the center
- main path direction and min/max depth
- branch depth rings or branch stubs
- hallway lead-in before wide content starts
- optional main approach and main cap
- room connector and room footprints
- required exits shown with locked styling
- optional exits shown with editable styling

The preview should derive possible shape from template capability, not only numeric settings. If the enabled templates only contain a one-exit hall piece, the preview should show a linear route even when branch depth is configured. If a junction template can emit side exits, the preview can show side branch envelopes. If rooms are enabled but no room connector template exists, the room envelope should appear disabled or invalid.

Suggested visual semantics:

- solid rectangles for floor roots and guaranteed terminal categories
- outlined regions for maximum possible generation envelope
- dashed branches for possible branch paths
- faded regions for disabled or missing template categories
- warning colors for configured paths that cannot generate because a required pool or template category is missing

The visual style should match the existing walled keep footprint preview where practical: a top-down schematic with hoverable colored segments representing hallways, rooms, approaches, and caps. Each segment should expose a tooltip with its category, size, path role, and fit status. Main and branch connector markers should be visually distinct, and in/out direction should be visible from marker placement and tooltip text.

Suggested connector marker categories:

```text
main_in
main_out
branch_in
branch_out
required locked connector
optional authored connector
invalid or missing connector
```

The preview should also display a compact capability summary:

```text
Main path: 2-5 halls
Lead-in: auto 3
Branches: up to 2 deep
Rooms: 3 profiles, max 13x13
Caps: main enabled, branch enabled
Junctions: side branches possible
```

The UI should render from a planner-owned report rather than duplicating planner logic. Suggested report shape:

```text
MKFloorTopologyPreviewReport
  rootBounds
  enabledExits
  templateCapabilities
  recommendedHallwayLeadInPieces
  effectiveHallwayLeadInPieces
  mainPathEnvelopeByExit
  branchEnvelopeByExit
  roomEnvelopeByExit
  capEnvelopeByExit
  warnings
```

Inside `MKTowerStackSidePreview`, the floor layout preview should appear below the existing selected-region controls. It should take the full widget width and as much vertical space as required to remain legible. The side stack diagram should stay the compact vertical summary at the top; the floor layout preview is a separate lower panel that focuses on horizontal generation.

If parent planner fitting disables horizontal generation for a floor, the selected floor region should show a visible `X` marker. Hovering the region or the marker should explain the fit failure, such as:

```text
Floor topology disabled: branch room envelope overlaps courtyard path reservation.
```

The disabled marker should be a small red `X` shape centered in the affected floor region on the tower stack side preview diagram. It does not need a complex icon; the important behavior is that the marker is visible at a glance and exposes the validation reason on hover.

## Planner Integration

Add a reusable floor topology planner/helper. Suggested shape:

```text
MKFloorTopologyPlanner
MKFloorTopologyDefinition
MKFloorTopologySettings
MKFloorTopologySizingReport
MKFloorTopologyPreviewReport
```

Responsibilities:

- Resolve floor topology settings for one stack/floor role.
- Create planned pieces for horizontal categories.
- Create runtime pools for main path, branch path, room connectors, rooms, approaches, and caps.
- Emit sizing, preview, and validation diagnostics for UI.
- Respect required exits supplied by the parent tower stack.

The tower stack planner should call this helper for each floor root that can have horizontal generation.

The walled keep planner should not special-case these controls. It should configure tower stack instances, and each stack instance should resolve its floor topology settings.

## Migration

There is no need to preserve compatibility with existing test workspace JSON.

Migration can be clean:

1. Add scoped floor topology settings to the topology profile.
2. Remove or deprecate global `MKWorkspaceTopologyPathSettings`.
3. Update tower and walled keep defaults to create scoped settings for active stack floor roles.
4. Regenerate test workspaces.
5. Re-export data and runtime templates.

If temporary compatibility is useful during development, it should be removed before finalizing the feature.

## Validation

Validation should check:

- `minMainPathPieces <= maxMainPathPieces`
- branch cap depth is within supported budget
- room dimensions are positive and odd where required
- room footprints fit the selected floor role constraints
- room heights stay between connector-driven minimum height and owning floor band height
- room connector schemas reject invalid main, branch, or vertical access connectors
- required exits have compatible path settings
- every enabled category has at least one authored or generated template
- compact authoring sources can produce all required runtime variants
- generated pools for main, branch, room, approach, and cap categories are non-empty when referenced
- configured template capabilities match the requested generation shape
- manual hallway lead-in values below the recommendation are reported
- floor topology envelopes fit hard placement and parent-reservation limits
- parent planner fit failures are attached to the affected floor root report so the UI can mark that region

Validation failures should be shown in the topology UI and logged during planner/export operations.

## Implementation Phases

### Phase 1: Data Model

- Add scoped floor topology settings.
- Bind settings by stack id and floor role.
- Add room profiles directly to each floor topology setting.
- Add one starter main room profile and one starter branch room profile when a floor opts into horizontal generation.
- Add hallway lead-in mode and manual lead-in settings.
- Add defaults for tower primary, keep center, shared corner, and unique corner stacks.
- Remove global path-depth UI from the tower contributor.

### Phase 2: UI

- Add floor topology controls to the selected region area of `MKTowerStackSidePreview`.
- Keep required exits visible but locked.
- Add a compact horizontal generation-envelope preview.
- Show recommended and effective hallway lead-in values.
- Warn when manual lead-in is below the recommendation.
- Show an `X` marker on floor regions whose horizontal topology is disabled by parent planner fit validation.
- Render the floor layout preview below the selected-region controls at full widget width.
- Reuse the same panel for standalone tower and walled keep subtower tabs.

### Phase 3: Planner

- Add `MKFloorTopologyPlanner`.
- Move main path and branch cap pool generation out of tower-stack-specific code where possible.
- Add sizing and preview reports for envelope calculation, template capability, and hallway lead-in recommendation.
- Keep tower stack responsible for floor roots and vertical sequencing.
- Update tower and walled keep planners to invoke floor topology planning per active floor root.

### Phase 4: Templates And Export

- Add default compact authoring pieces for hall, junction, connector, room, branch cap, main approach, and main cap.
- Generate runtime directional variants during export.
- Ensure jigsaw metadata is rewritten for each runtime variant.
- Regenerate and re-export test workspaces.

### Phase 5: Tests

- Add planner tests for scoped floor topology settings.
- Verify standalone tower and walled keep subtowers resolve independent floor topology settings.
- Verify branch pools and main path pools are non-empty.
- Verify disabling room generation does not break main or branch caps.
- Verify required exits remain locked in UI-facing reports.
- Verify compact authoring creates expected runtime variants.
- Verify recommended hallway lead-in increases when room or cap footprints widen.
- Verify preview reports do not show branches when no enabled template category can emit branch exits.
- Verify parent reservation failures disable only the affected horizontal floor topology where possible.
- Verify floor regions with parent fit failures expose a UI-facing disabled marker and reason.
- Verify room connector schemas allow only valid main/branch connectors and never expose vertical access options.
- Verify room height validation uses connector minimum height and owning floor band maximum height.

## Settled Decisions

- Room profiles live directly on each floor topology setting. Reusable room catalogs can be added later as an explicit opt-in feature.
- Floor topology supports arbitrary main room profiles and branch room profiles.
- When horizontal generation is enabled for a floor, create one starter main room and one starter branch room sized to the selected floor root.
- Main approach, main cap, and branch cap remain separate topological categories.
- Branches are hallway-led side routes that can place a room, optionally continue, and eventually terminate in branch-cap content.
- Branch continuation after a room is controlled by authored `branch_out` exits on the branch room template.
- Main path continuation after a main room is controlled by authored `main_out` exits on the main room template.
- Main rooms require `main_in` and `main_out`, and may optionally expose `branch_out`.
- Branch rooms require `branch_in`, and may optionally expose `branch_out`.
- Main approach requires `main_in` and `main_out`, and may optionally expose `branch_out`.
- Main cap requires `main_in`, may optionally expose `branch_out`, and may not expose `main_out`.
- Floor topology rooms, approaches, and caps never expose vertical access options in the first version.
- Room height is configurable between connector-driven minimum height and the owning floor band height.
- Main and branch routes share hallway and junction template categories. Their distinction is narrative and budgetary, not a separate authored hallway set.
- Canonical tower floors have no horizontal exits by default. Designers opt into dungeon-style horizontal expansion by adding floor-root exits.
- Floor topology fitting calculates deterministic maximum envelopes, including recommended hallway lead-in, hard placement fit, and parent reservation fit.
- The preview renders a generation envelope based on both settings and enabled template capabilities, not an exact random result.
- The generation-envelope preview should use a walled-keep-style top-down schematic with hoverable hallway, room, approach, and cap segments.
- Main and branch in/out connectors should be visually distinct in the floor preview.
- Parent planner fit failures are shown on the affected tower-stack floor region with an `X` marker and a reason.
- The floor-region fit failure marker is a simple red `X` centered on the affected region.
- The floor layout preview appears below the selected-region controls in the tower stack side preview widget and uses the full widget width.

## Open Decisions

- None for the current design pass.
