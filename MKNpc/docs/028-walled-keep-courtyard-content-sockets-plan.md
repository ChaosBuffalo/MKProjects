# Walled Keep Courtyard Content Sockets Plan

## Problem

The default walled keep leaves meaningful open space between the central keep tower and the defensive wall. The current south walkway template also has branch connectors to either side, but those branches do not yet lead into an interior circulation route or any planned content.

We want to use that interior space for courtyard content that is guaranteed to fit inside the wall. The planner should decide where content can fit before runtime generation. Runtime jigsaw placement should not be responsible for discovering usable courtyard space through collision failures.

## Goals

- Add deterministic courtyard content sockets inside the walled keep.
- Branch the interior walkway around the central keep tower and use it as the access path to courtyard content.
- Add a dedicated keep entry approach template that occupies the south courtyard approach space between the gatehouse and center keep.
- Generate default small, medium, and large content authoring templates.
- Give each generated content template a short continuation of the walkway using the same walkway blocks.
- Fill the remaining content template area with structure void by default so designers can build into it.
- Let walled keep topology settings control the generated content template height.
- Restrict courtyard socket and content footprints to odd square dimensions for simple rotation-safe authoring.
- Filter socket pools so only content templates that fit the socket can generate.
- Keep authoring simple: designers should author content size classes, not concrete quadrant-specific variants.

## Non-Goals

- Do not rely on vanilla jigsaw rotation to fit content.
- Do not use runtime collision failure as the normal fitting mechanism.
- Do not require every socket to have the same footprint.
- Do not force designers to author content for every socket.
- Do not generate a closed jigsaw loop for the walkway. Use deterministic acyclic branches that look like a loop in-world.
- Do not support asymmetric courtyard content footprints in the first implementation.

## Proposed Shape

The keep planner should expose courtyard content sockets around the center tower:

```text
+-----------------------------------+
| [NW]        [N]        [NE]       |
|                                   |
|   +-----+===========+-----+       |
|   |     |           |     |       |
| [W] === |  CENTER   | === [E]     |
|   |     |  KEEP     |     |       |
|   +-----+===========+-----+       |
|                                   |
| [SW]      ENTRY       [SE]        |
+---------------G-------------------+
```

Legend:

```text
G          = gatehouse
CENTER     = central keep tower
ENTRY      = dedicated gatehouse-to-keep approach
=========== = interior walkway branches/ring
[NW] etc.  = courtyard content sockets
```

For the current default keep, start with seven candidate sockets:

```text
keep.courtyard.north_west
keep.courtyard.north
keep.courtyard.north_east
keep.courtyard.west
keep.courtyard.east
keep.courtyard.south_west
keep.courtyard.south_east
```

Do not create a south content socket by default. The south courtyard space should be owned by a dedicated keep entry approach topology family. That approach piece should run from the gatehouse toward the center keep and provide east/west connectors into the courtyard walkway paths.

Recommended entry approach slot:

```text
keep.entry_approach.main
```

## Walkway Graph

Use the existing `keep.walkway.*` idea as an interior access graph.

Recommended graph:

```text
gate.main
  -> keep.entry_approach.main
    -> keep.walkway.west
      -> keep.walkway.north_west_terminal
    -> keep.walkway.east
      -> keep.walkway.north_east_terminal
```

The visible result can read as a ring around the central tower, but the jigsaw graph remains two acyclic branches from the gatehouse. This avoids asking jigsaw placement to close a loop between already placed endpoints.

Each walkway segment should expose branch connectors for nearby courtyard sockets. Those connectors target socket-specific pools.

The entry approach template is a dedicated walled keep topological family rather than a generic walkway or content socket. Its default generated form should be a path from the gatehouse to the center keep with east and west connectors that seed the courtyard walkway branches.

## Socket Bounds

Add a planner-owned bounds calculation for the realized keep layout.

Inputs:

- realized perimeter wall segment counts and segment length
- gatehouse footprint
- center tower footprint
- corner tower footprint
- shell margin and exterior air margin where relevant
- reserved walkway footprint
- reserved gatehouse-to-center approach path
- minimum clearance around sockets and connectors

Output per socket:

```text
socket_id
max_square_size
socket_class
facing
connector_source_slot
enabled
```

The exact available size can vary per socket. The planner should compute the largest odd square footprint that fits each socket spot after all reservations are applied. Socket classes keep authoring understandable:

```text
small
medium
large
```

The planner should map the computed odd square size to the largest class that fits:

```text
>= largeSize  -> large
>= mediumSize -> medium
>= smallSize  -> small
otherwise     -> disabled
```

The default class sizes should be:

```text
small  = 5x5
medium = 7x7
large  = 9x9
```

These defaults fit the current default keep sizing while still leaving the planner free to downgrade or disable sockets in smaller/custom layouts. The thresholds should be settings-backed rather than hard-coded into export behavior.

Socket class matching should be strict. A `small` socket accepts only small content, a `medium` socket accepts only medium content, and a `large` socket accepts only large content. This is less flexible than inclusive matching, but it is easier for designers to understand and makes socket behavior predictable.

Odd square footprints are required for courtyard content. This keeps rotation safety simple:

- a rotated template has the same footprint
- centered connector placement remains stable on every edge
- walkway continuation stays centered after rotation
- designers do not need to reason about width/length swapping
- fitting is a class-size check instead of an asymmetric bounds check

## Topology Settings

Extend walled keep topology settings with courtyard content settings. The exact model can live on `MKWorkspaceTopologyProfile`, `MKWorkspaceTopologyPathSettings`, or a new walled-keep-specific settings record.

Recommended fields:

```text
courtyardContentEnabled
courtyardSocketGenerationEnabled
courtyardContentTemplateHeight
courtyardSocketClearance
courtyardWalkwayContinuationLength
courtyardSmallTemplateSize
courtyardMediumTemplateSize
courtyardLargeTemplateSize
```

Defaults should be conservative:

```text
courtyardContentEnabled = true
courtyardSocketGenerationEnabled = true
courtyardContentTemplateHeight = 7
courtyardSocketClearance = 1
courtyardWalkwayContinuationLength = 3
small = 5x5
medium = 7x7
large = 9x9
```

`courtyardContentTemplateHeight` controls the generated template interior height for all default small, medium, and large courtyard content templates. Later we can allow per-class height overrides if needed.

`courtyardSocketGenerationEnabled` is a global walled keep setting. Per-side or per-socket toggles are intentionally out of scope for the first implementation.

Template sizes should validate as odd square sizes. If a configured size is even, non-square, or smaller than the minimum content size, workspace validation should report an error instead of silently snapping it.

## Generated Content Templates

Generate one authoring template per size class:

```text
keep_courtyard_content_small_template
keep_courtyard_content_medium_template
keep_courtyard_content_large_template
```

Each default generated template should contain:

- a centered connector on one edge facing back toward the walkway
- a short continuation of the walkway from that connector into the template
- floor/wall/detail blocks for that walkway continuation using the resolved walkway palette
- structure void for the rest of the content footprint
- metadata declaring the content footprint and class

Conceptual overhead view:

```text
+-----------+
| v v v v v |
| v v v v v |
| v v # v v |
| v v # v v |
| v v J v v |
+-----------+
```

Legend:

```text
J = courtyard content connector
# = walkway continuation blocks
v = structure void
```

The continuation should be long enough for designers to see the intended access path and decorate around it, but short enough that the template remains mostly empty authoring space.

## Template Metadata

Add content tags to generated pieces:

```text
workspace_content_kind = courtyard
workspace_content_socket_class = small|medium|large
workspace_content_size = <odd-square-size>
workspace_content_width = <same-size>
workspace_content_length = <same-size>
workspace_content_height = <height>
workspace_content_requires_path = true
workspace_content_connector_edge = south
```

Socket pieces or runtime socket pools should carry:

```text
workspace_courtyard_socket_id
workspace_courtyard_socket_class
workspace_courtyard_socket_max_square_size
```

The connector edge should be canonical for authoring. Export can rotate derived runtime variants when needed, following the compact rotated template authoring approach.

## Rotation Safety

For the first implementation, courtyard content templates should use odd square footprints only. This makes content rotation-safe by construction as long as the connector is placed on the canonical centered edge and the walkway continuation is centered.

A symmetric template is straightforward:

```text
7x7 template
one centered connector on the canonical edge
```

This can rotate into north/east/south/west socket facings without changing its fitting footprint.

Asymmetric templates are intentionally out of scope. An asymmetric template could be rotate-safe in a future implementation, but only if the rotated footprint is checked against the destination socket:

```text
9x5 template authored with connector on the south edge
90 degree rotation behaves as a 5x9 footprint
```

That template is rotate-safe for a socket only when:

- the rotated width and length fit the socket bounds
- the connector remains centered or otherwise lands on an allowed connector offset
- the continuation path after rotation still points back to the walkway
- any declared blocked/reserved cells rotate with the footprint and remain inside the socket

If any of those conditions fail, the template should not be exported into that socket pool. For now, the authoring rule is strict: courtyard content templates must be odd squares.

## Export And Runtime Pools

Each socket should get a dedicated target pool:

```text
keep_slots/keep/courtyard/north_west
keep_slots/keep/courtyard/north
keep_slots/keep/courtyard/north_east
...
```

During export, include only content templates that fit:

```text
content_size <= socket_max_square_size
content_height <= courtyardContentTemplateHeight or socket_max_height
content_class == socket_class
```

The socket pool can include `minecraft:empty` as a fallback so content remains optional unless a future setting makes a socket required.

## Workspace Authoring

The generated workspace should only show the compact content templates:

```text
keep_courtyard_content_small_template
keep_courtyard_content_medium_template
keep_courtyard_content_large_template
```

It should not create one visible authoring template per socket. Socket-specific runtime variants can be generated at export with the correct connector pools and rotations.

Designers can duplicate or variant these base templates to add multiple small/medium/large courtyard content options.

## Implementation Steps

1. Add walled keep courtyard content settings and defaults.
2. Add a `KeepLayoutBounds` or similar planner helper that computes realized interior free rectangles.
3. Add socket records to the walled keep planner with id, bounds, class, facing, and enabled state.
4. Add the entry approach family and extend walkway planning so it branches into west/east/north access paths and exposes socket connectors.
5. Generate small, medium, and large courtyard content authoring templates.
6. Implement default content scaffold generation:
   - walkway continuation blocks from the connector
   - structure void in the remaining template area
   - height from topology settings
7. Add content metadata tags to generated templates and socket pools.
8. Filter runtime socket pools by footprint and class during export.
9. Preserve compact authoring on import.
10. Add tests for default socket count, socket bounds, generated template height, default void fill, walkway connector targets, and export pool filtering.

## Tests

Recommended focused tests:

- default walled keep creates seven candidate courtyard sockets and no south content socket
- default walled keep creates a dedicated entry approach piece from gatehouse to center keep
- entry approach piece exposes east and west connectors into the courtyard walkway branches
- each enabled socket has a positive odd max square size
- each enabled socket maps to the largest strict class that fits its computed max square size
- small/medium/large content templates use the configured height
- small/medium/large content templates validate as odd square footprints
- generated content templates include one walkway continuation and otherwise default to structure void
- walkway branches target socket pools
- export excludes content that exceeds a socket footprint
- export excludes content whose class does not exactly match the socket class
- compact import preserves only the authoring content templates, not all socket rotations
