# Walled Keep Courtyard Grid Plan

## Problem

The current courtyard socket implementation branches courtyard content off long interior walkway chains. This can produce two bad outcomes:

- The interior walkway branch consumes too much jigsaw generation depth before the exterior wall finishes.
- The courtyard sockets are effectively arranged along a line instead of wrapping around the central keep.

This makes generation order sensitive. Depending on connector order, the gatehouse, wall, or courtyard pieces can collide or be cut off before the intended loop completes.

## Goal

Replace the long courtyard walkway chain with an explicit 3x3 interior courtyard layout around the central keep.

Use this logical layout:

```text
7 8 9
4 5 6
1 2 3
```

Slot assignments:

- Slot 5: central keep tower
- Slot 2: south entry walkway from gatehouse to keep
- Slots 4 and 6: small courtyard content sockets reached from the ring path
- Slots 1 and 3: medium courtyard content sockets reached from the ring path
- Slots 7, 8, and 9: large courtyard content sockets reached from the ring path

The courtyard path should circle the central keep through dedicated ring path templates. Courtyard content should branch outward from those path pieces as terminal sockets. It should not be generated as one long east or west walkway run, and content templates should not need to carry the path topology themselves.

## Design

### Courtyard Grid Plan

Add a planner model that computes a fixed 3x3 courtyard grid from the existing walled keep settings.

The grid should account for:

- Central keep footprint
- Interior wall span
- Gatehouse and south entry approach
- Courtyard socket size setting
- Courtyard socket template height setting

The planner should validate that the selected socket sizes fit inside the generated wall perimeter. If they do not fit, courtyard generation should be disabled for that keep plan instead of failing the whole keep. The center keep, gatehouse, and exterior wall should still generate.

When courtyard generation is disabled by fit validation, the planner should report the reason both in the workspace UI and in logs. The report should include the requested socket sizing and the available interior span so designers can adjust settings without guessing.

### Courtyard Slot Families

Replace the current distributed east/west walkway chain expansion with grid slot families.

Suggested path slots:

- `keep.courtyard.path.south_west`
- `keep.courtyard.path.south_east`
- `keep.courtyard.path.west`
- `keep.courtyard.path.east`
- `keep.courtyard.path.north_west`
- `keep.courtyard.path.north`
- `keep.courtyard.path.north_east`

Suggested content slots:

- `keep.courtyard.medium.south_west`
- `keep.courtyard.medium.south_east`
- `keep.courtyard.small.west`
- `keep.courtyard.small.east`
- `keep.courtyard.large.north_west`
- `keep.courtyard.large.north`
- `keep.courtyard.large.north_east`

The south entry path remains a dedicated keep entry family.

### Ring Path And Content Sockets

Each courtyard grid cell outside the center keep should be represented by a deterministic path piece plus, where appropriate, one outward content socket.

The path piece owns:

- The local ring path geometry.
- Connectors to neighboring ring path pieces.
- One outward content connector for the cell's content socket.

The content piece owns:

- The designer-authored courtyard content footprint.
- A single entrance connector back to the ring path.
- Mostly void default space, with no responsibility for continuing the ring path.

Examples:

- The southwest path cell connects east to the south entry path, north to the west path cell, and outward to `keep.courtyard.medium.south_west`.
- The southeast path cell connects west to the south entry path, north to the east path cell, and outward to `keep.courtyard.medium.south_east`.
- The west path cell connects south to the southwest path cell, north to the northwest path cell, and outward to `keep.courtyard.small.west`.
- The east path cell connects south to the southeast path cell, north to the northeast path cell, and outward to `keep.courtyard.small.east`.
- The north path cells complete the crossing and expose large content sockets.

The default generated content templates should remain mostly void and include:

- A single entrance area using the same walkway blocks.
- One jigsaw connector back to the path piece.
- The content footprint reserved by the socket size.

### Compact Authoring And Export Variants

Designers should not need to author every directional courtyard variant.

The authored template set should stay compact when the geometry is rotate-safe:

- `keep_courtyard_path_t_template`
- `keep_courtyard_path_corner_t_template`
- `keep_courtyard_content_small_template`
- `keep_courtyard_content_medium_template`
- `keep_courtyard_content_large_template`

On export, the workspace exporter should create the logical variants with the correct rotated geometry, jigsaw connector sides, and target pools. This follows the same direction as the wall segment and shared corner tower variant export feature.

Examples of exported variants:

- `keep_courtyard_path_corner_t_south_west`
- `keep_courtyard_path_t_west`
- `keep_courtyard_path_t_north`
- `keep_courtyard_content_small_west`
- `keep_courtyard_content_medium_south_west`
- `keep_courtyard_content_large_north`

Rotate-safe courtyard templates should require:

- Square footprints.
- Odd dimensions so connector centerlines rotate around a stable center block.
- Connector positions derived from the opening profile and lane inset.
- No directional decorative assumptions unless the designer accepts the rotated result.

### Generation Shape

The south entry approach should branch into two short paths:

- West branch to the southwest ring path cell.
- East branch to the southeast ring path cell.

From there, each side climbs around the central keep using local path pieces. Content sockets branch outward from those path pieces and should be terminal branches.

Default path shape assignment:

- Slot 1: corner-T path, connecting entry/south path, west-side path, and medium southwest content.
- Slot 3: corner-T path, connecting entry/south path, east-side path, and medium southeast content.
- Slot 4: T path, connecting southwest path, northwest path, and small west content.
- Slot 6: T path, connecting southeast path, northeast path, and small east content.
- Slot 7: corner-T path, connecting west-side path, north crossing, and large northwest content.
- Slot 8: T path, connecting the north crossing and large north content.
- Slot 9: corner-T path, connecting east-side path, north crossing, and large northeast content.

The final north crossing needs one clear ownership rule so both sides do not try to place the same target:

- The side that owns the back wall segment opposite the gatehouse also owns placement of slot 8.
- Slot 8 is a full large content socket reached from the north ring path, not a specialized crossing or cap piece.

This keeps jigsaw depth shallow and avoids depending on a long branch to discover the full courtyard loop.

### Connector Offsets

Connector offsets define where each local jigsaw connector is placed inside a courtyard path or content template footprint.

The courtyard path should be biased toward the central keep instead of running through the geometric center of each grid cell. This makes the path behave like a ring around the keep and leaves the outward side of each cell available for decoration or content.

Implementation rules:

- Derive connector offsets from the central keep footprint, socket footprint, exterior margin, and opening profile.
- Use `laneInset = exteriorMargin + floor(openingWidth / 2)` as the default lane center offset.
- Align each connector to the local walkway lane that circles the central keep.
- Keep neighboring grid connectors aligned so slot-to-slot placement produces a contiguous path.
- Do not center every connector in its grid cell unless that is also the keep-facing ring lane for that side.

For example, the west-side path cells should place their north/south path lane near the east edge of the cell, because that edge faces the central keep. The east-side path cells should mirror this by placing their north/south path lane near the west edge. The north-side path cells should place their east/west crossing near the south edge, because that edge faces the central keep.

### Exterior Wall Independence

Exterior wall generation should stay independent from courtyard generation.

The perimeter wall, gatehouse, center keep, and courtyard grid should be shallow sibling branches from the planned keep root where possible. Courtyard content should not sit upstream of exterior wall generation, because that allows interior path depth to starve wall completion.

The ring path should also avoid unnecessary depth. Content sockets should be terminal outward branches from local path pieces so a decorative content branch cannot prevent later wall or path pieces from generating.

## Implementation Steps

1. Add a `CourtyardGridPlan` model that describes the seven content cells plus the existing center and south entry cells.
2. Update courtyard slot collection so grid path and content slots are available when courtyard generation is enabled.
3. Replace east/west courtyard walkway chain piece creation with grid ring path piece creation.
4. Update entry approach connectors to target only the two south ring path cells plus gatehouse and center keep.
5. Add local path connectors on each ring path piece according to its 3x3 position.
6. Add terminal outward content connectors from the relevant ring path pieces.
7. Generate compact default authoring templates for path T, path corner-T, and small/medium/large content sockets.
8. Extend export-time rotated variant generation for courtyard path and content templates.
9. Update export/template-pool tests to assert the grid shape, compact authoring expansion, and duplicate connector prevention.
10. Regenerate and re-export the test keep workspace after the planner changes.

## Open Decisions

None.
