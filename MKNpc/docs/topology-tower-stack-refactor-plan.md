# Topology Tower Stack Refactor Plan

## Purpose

Corner towers in the walled keep should be able to use the same piece set and generation patterns as the main keep tower and the standalone tower topology. The long-term shape is to make "tower stack" a reusable topology primitive that can be instantiated by any topology profile.

This should also improve the designer workflow:

- Topology profile pages configure a working structure at a high level.
- Family pages are for intentional overrides and special authored templates.
- Shared behavior such as tower stack generation, vertical access, cap handling, palette defaults, and floor counts is defined once and reused.

## Current State

The standalone tower profile has a full vertical stack pattern:

- entry
- main floors
- top cap approach
- top cap
- basement entry/floors
- basement cap approach
- basement cap

The walled keep center tower has explicit slots for:

- `keep.center.entry`
- `keep.center.main_floor`
- `keep.center.top_cap`
- `keep.center.basement_floor`
- `keep.center.basement_cap`

The walled keep corner towers currently have only one room slot per active corner template:

- `keep.corner.shared`
- `keep.corner.north_west`
- `keep.corner.north_east`
- `keep.corner.south_east`
- `keep.corner.south_west`

Those corner slots have vertical access and can now configure height and floor counts, but they do not yet generate a full stack with top and bottom caps.

## Target Model

Introduce a reusable tower stack primitive. A topology profile declares tower stack instances, and each stack expands into a standard set of stack slots.

Example stack ids:

- `tower.primary`
- `keep.center`
- `keep.corner.shared`
- `keep.corner.north_west`
- `keep.corner.north_east`
- `keep.corner.south_east`
- `keep.corner.south_west`

Each stack can expand into slots like:

- `{stack}.basement_cap`
- `{stack}.basement_cap_approach`
- `{stack}.basement_floor`
- `{stack}.entry`
- `{stack}.main_floor`
- `{stack}.top_cap_approach`
- `{stack}.top_cap`

The standalone tower planner should become a thin wrapper around one tower stack. The walled keep planner should invoke the same tower stack planning logic for the center stack and each active corner stack, then add keep-specific walls, walkways, gates, and perimeter connectors.

## Stack Settings

`MKWorkspaceTowerStackSettings` should become the source of truth for stack-level defaults. It currently stores:

- `stackId`
- `mainFloors`
- `basementFloors`
- `height`

It should grow to include:

- main floor count
- basement floor count
- top cap approach enabled
- basement cap approach enabled
- entry height
- main floor height
- basement floor height
- top cap height
- basement cap height
- width
- length
- default palette override
- default foundation policy
- top and bottom void defaults for eligible non-shaft pieces
- stack-scoped vertical access settings

## Stack-Scoped Vertical Access

Vertical access should be configurable per tower stack. The current workspace-global `MKWorkspaceVerticalAccessSpec` is too coarse for profiles like a walled keep.

Examples:

- Center keep tower uses a wide stair shaft.
- Shared corner towers use a compact ladder shaft.
- One unique corner tower uses a different shaft size or placement.

Stack-level vertical access settings should include:

- vertical access enabled
- vertical access group id
- shaft size
- placement
- stair mode
- stair rise type
- stair width
- stair/slab/ladder blocks, inherited from topology palette unless overridden

Vertical access shaft direction should be configurable per vertical-access slot using the existing exit mask widget. A stack or individual shaft piece can opt into only upward access, only downward access, or both by including or excluding `UP` and `DOWN` in that mask. For example, a tower profile that only grows upward and has no basement should be able to disable downward shaft access.

We should not introduce a second dedicated vertical direction control. The topology/stack page can expose higher-level defaults like whether the stack supports vertical access and what shaft/stair settings it uses, but the up/down direction mask remains the existing exit mask widget on the relevant template/family slot.

Resolution order should be:

1. Workspace/global default vertical access settings.
2. Topology stack vertical access settings.
3. Family override only where explicitly supported.

Validation and height snapping must become per-stack. A corner stack should not be forced into the center tower's shaft constraints, and the center stack should not be constrained by the smallest corner tower footprint. We should validate each stack independently and let designers handle whether the whole structure reads sensibly as a combined composition.

## Planner Refactor

Create a reusable tower stack planner/helper, likely `MKTowerStackPlanner`.

Responsibilities:

- Expand one tower stack into planned pieces.
- Apply main and basement floor counts.
- Apply top and bottom cap approach settings.
- Generate vertical connector pools for the stack.
- Emit correct runtime metadata for rooms, approaches, caps, and terminals.
- Resolve stack-local settings and family overrides.
- Support stack-local vertical access spec.

Then update planners:

- `MKTowerWorkspacePlanner` calls `MKTowerStackPlanner` for `tower.primary`.
- `MKWalledKeepWorkspacePlanner` calls `MKTowerStackPlanner` for `keep.center` and every active corner stack.
- Walled keep keeps responsibility for perimeter runs, walkways, gates, and corner-to-wall layout connectors.

## Corner Tower Slot Expansion

Corner towers should no longer be single room slots. Each active corner stack should have the full tower stack slot set.

For shared corners, default families should include:

- `keep_corner_shared_entry`
- `keep_corner_shared_main_floor`
- `keep_corner_shared_top_cap_approach`
- `keep_corner_shared_top_cap`
- `keep_corner_shared_basement_floor`
- `keep_corner_shared_basement_cap_approach`
- `keep_corner_shared_basement_cap`

For unique corners, the same set should exist per concrete corner stack.

The planner should continue to map each concrete corner position to either:

- the shared corner stack, or
- its unique corner stack.

The selected stack should then provide all required tower pieces for that corner position.

## Topology Profile Page

The topology profile page should be enough to configure a usable structure without opening individual family detail pages.

For the standalone tower, this page should expose:

- tower stack dimensions
- floor counts
- cap approach toggles
- vertical access settings
- palette defaults
- foundation defaults
- eligible cap void margins

For the walled keep, this page should expose:

- center tower stack settings
- shared/unique corner mode per corner
- each active corner stack's settings
- wall/perimeter run settings
- walkway/run settings
- gate settings
- topology-level palette defaults
- foundation defaults
- high-level sizing/counts

This page should configure topology slot, stack, and run defaults. It should not require designers to touch every generated family to get a sensible result.

## Family Pages as Overrides

Family pages should be for intentional deviations from topology defaults.

Examples:

- A specific top cap uses a different palette.
- A unique south-east corner main floor has a special height.
- One wall side uses a different foundation mask.
- A family has special exits or offsets.

This implies family definitions need a clearer inherited-vs-overridden model. If every family eagerly copies every default value, the UI becomes noisy and designers cannot tell what they intentionally changed.

Preferred behavior:

- Topology settings define defaults.
- Family definitions can override selected fields.
- Family UI displays inherited values by default.
- A designer can enable an override per field/group.
- A designer can reset an override back to inherited topology defaults.

## Resolution Layer

Introduce a resolver that produces final effective settings for planner/scaffold/export code.

Conceptually:

```java
ResolvedFamilySettings resolve(topologyProfile, slotId, familyOverride)
```

The resolver should answer:

- final width/length/height
- final vertical access spec
- final palette
- final foundation policy
- final void margins
- final horizontal exits/openings
- final role/slot metadata

Planners should use resolved settings instead of manually checking whether a value came from topology defaults or family overrides.

## Validation Updates

Topology-level validation should check:

- every active tower stack has required slot definitions or default templates
- every active shared/unique corner mode resolves cleanly
- floor counts fit vertical budget per stack
- per-stack vertical access settings are valid
- stack heights are valid for that stack's shaft configuration
- non-shaft void margins are not applied to vertical shaft pieces
- wall/walkway heights and void margins are valid
- topology slots reference compatible opening/run profiles

Validation should not require every stack in a topology profile to share a global vertical band height. The global structure can contain stacks with different heights, shaft sizes, and vertical access modes.

Family-level validation should focus on override legality:

- override values are in range
- override is allowed for that slot type
- exits and opening profiles are compatible
- vertical access exit masks are legal for the slot and do not contradict required cap/terminal behavior

## Implementation Phases

### Current Implementation Status

- Phase 2 is implemented: the standalone tower planner delegates room stack planning to `MKTowerStackPlanner`.
- Phase 3 is implemented: the walled keep center stack uses the reusable stack planner with scoped stack pools.
- Phase 4 is partially implemented: default shared corner towers now generate as full tower stacks, and the walled keep planner can plan active shared or unique corner stacks through the reusable stack planner. The remaining work is to finish topology-default editing, inherited family overrides, and stack-scoped vertical access settings.

### Phase 1: Model Reusable Tower Stack Slots

- Add a stack slot id helper that maps `{stackId}` plus stack role to concrete slot ids.
- Add a reusable tower stack schema builder.
- Expand `MKWorkspaceTowerStackSettings` with per-slot or per-stack-role heights, dimensions, cap approach toggles, and vertical access settings.
- Keep current tower and walled keep behavior working through compatibility helpers.

### Phase 2: Extract Tower Stack Planner

- Move reusable vertical stack planning out of `MKTowerWorkspacePlanner`.
- Make `MKTowerWorkspacePlanner` call the extracted helper for one stack.
- Preserve existing standalone tower tests.

### Phase 3: Convert Walled Keep Center Tower

- Update `MKWalledKeepWorkspacePlanner` to plan `keep.center` through the reusable stack planner.
- Keep perimeter/walkway/gate planning in the walled keep planner.
- Preserve current center keep output where possible.

### Phase 4: Expand Corner Towers

- Replace single corner tower slots with full stack slots.
- Generate default families for shared and unique corner stacks.
- Use per-corner stack settings for height, floor count, vertical access, and palette defaults.
- Ensure shared mode still maps all shared concrete corners to one shared template set.
- Ensure unique mode only shows and generates unique template sets for corners opted into unique mode.

### Phase 5: Topology Defaults UI

- Rework the topology defaults page around stack/run settings.
- Add stack-scoped vertical access controls.
- Add topology-level palette and foundation controls.
- Add reset-to-default behavior per stack/run section where useful.
- Make the page sufficient to configure a sensible default tower or walled keep.

### Phase 6: Family Override UI

- Add inherited/override state for family fields.
- Show inherited values clearly.
- Add controls to enable, edit, and reset overrides.
- Keep low-level family controls available for advanced customization.

### Phase 7: Resolver and Validation

- Introduce resolved settings objects.
- Update planners, scaffold generation, export, and validation to consume resolved settings.
- Split topology validation from family override validation.

### Phase 8: Tests

- Standalone tower still generates the same canonical pieces.
- Walled keep center tower uses the reusable stack planner.
- Shared corner towers generate full stack pieces including top and bottom caps.
- Unique corner towers can differ in height, floor count, palette, and vertical access settings.
- Stack-scoped vertical access affects height snapping and shaft generation independently per stack.
- Topology defaults flow into generated/resolved families.
- Family overrides can deviate from topology defaults.
- Resetting overrides returns to topology defaults.
- Wall top void margin still applies only to eligible non-shaft linear run pieces.

## Design Decisions

- Category profiles should be removed entirely as part of the topology profile refactor. Topology stack/run settings should become the primary configuration surface.
- Vertical access shaft direction is configured through the existing exit mask widget; no new dedicated up/down control is planned.
- Shared corner stack settings should only appear if at least one concrete corner uses shared mode. Unique corner stack settings should only appear for corners opted into unique mode.
- Vertical band height validation should be per-stack, not workspace-global.
- Cross-stack visual coherence is a designer responsibility. The system should validate that each stack can generate correctly, not force every tower/run in the topology to share one global height phase.
