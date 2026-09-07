# Walled Keep Perimeter Wall Plan

## Problem

The current walled keep defaults treat each perimeter side as one wall piece. That makes the generated footprint depend on a single wall section length. With the default keep, this creates two related problems:

- The wall span may be too short to clear the center keep/tower footprint, causing the back side to fail during jigsaw placement and leaving a U-shaped perimeter.
- The jigsaw graph is trying to close a loop through normal branch placement. Minecraft jigsaw placement is tree-like and rejects overlapping candidate bounding boxes; it does not solve closed loops between already placed endpoints.

The wall and gatehouse gap fix addressed connector-facing template margins, but it does not solve world-space gaps or rejected perimeter placements caused by an undersized span.

## Goals

- Let designers keep authoring a simple reusable wall segment template.
- Derive keep perimeter size from desired interior clearance and tower sizes, not from one wall piece length.
- Generate all four sides consistently with enough span around the center tower.
- Preserve exterior air margin for decoration.
- Keep wall interiors walkable and carved with air, not structure void.

## Proposed Approach

Add a perimeter wall span planner that expands each logical perimeter side into a chain of wall segment pieces.

Instead of:

```text
keep.perimeter.north -> keep_wall_north
```

the planner emits logical segment slots:

```text
keep.perimeter.north.0
keep.perimeter.north.1
keep.perimeter.north.2
```

All segments can reuse the same physical wall family/template by default. The segment index exists to make the generated pool graph deterministic, not to force designers to author many templates.

## Span Calculation

Compute the required side span from the actual keep layout settings:

```text
requiredSideSpan =
    cornerTowerSpan
  + courtyardClearance
  + centerTowerSpan
  + courtyardClearance
  + cornerTowerSpan
```

Then derive a segment count:

```text
usableWallSpan = requiredSideSpan - cornerAttachmentAllowance
segmentCount = ceil(usableWallSpan / preferredWallSegmentLength)
actualSideSpan = segmentCount * preferredWallSegmentLength + cornerAttachmentAllowance
```

For the first implementation, grow the keep footprint to fit whole repeated segments instead of creating one variable-length filler segment. It is acceptable for the generated keep to be slightly larger than the requested minimum span. This keeps authoring simple and avoids requiring odd custom wall pieces.

## Pool Graph

The perimeter should not rely on an open-ended loop where any wall can attach to any matching wall pool. Use directed logical pools so each segment knows the next intended slot.

Generation should grow clockwise from the gatehouse. This gives the perimeter one deterministic expansion order while avoiding the complexity of two independent arms that need to meet cleanly.

Example clockwise path from gatehouse:

```text
gate.main
  -> keep.perimeter.south.west.0
  -> keep.perimeter.south.west.1
  -> keep.corner.south_west
```

Then continue around the keep with deterministic segment chains:

```text
gate.main -> south west chain -> south_west corner
south_west corner -> west chain -> north_west corner
north_west corner -> north chain -> north_east corner
north_east corner -> east chain -> south_east corner
south_east corner -> south east chain -> gate-adjacent terminal segment
```

The final south-east chain should terminate adjacent to the gatehouse instead of targeting the already placed gatehouse as a jigsaw child. That keeps the graph acyclic while still allowing the computed span to bring the wall back to the gate visually.

This avoids asking jigsaw placement to close a cycle between two already placed endpoints. It also makes failures easier to diagnose because each segment targets one expected next pool.

## Template Authoring

Default authoring should remain simple:

- One defensive wall segment template can be reused for every segment.
- Optional override families can be added later for start, middle, end, corner-adjacent, or gate-adjacent wall segments.
- Logical segment names should not imply unique designer templates unless an override is present.

Use one default wall segment family:

```text
keep_wall_segment
```

North/east/south/west walls should collapse into this shared family by default. Direction-specific variation can be added later as an explicit override feature if we need it, but the baseline should make a simple keep authorable from one wall segment template.

## Defensive Wall Interior Blocks

The defensive wall interior should use air blocks for walkable space and connector passages.

Structure void should remain reserved for exterior decoration margins, where preserving the world or designer-added blocks is useful. Using structure void in the wall passage would allow terrain or other existing blocks to survive inside the wall and could make the wall non-walkable.

## Implementation Steps

1. Add perimeter span settings to the walled keep topology profile or path settings:
   - courtyard clearance from center tower to perimeter walls
   - preferred wall segment length
   - optional minimum segment count per side

2. Update `MKWalledKeepWorkspacePlanner` to expand each perimeter run family into segment pieces based on computed side span.

3. Add deterministic logical pool names for segment chains.

4. Keep the default wall authoring model backed by a single reusable defensive wall family.

5. Update scaffold/export metadata so generated segment pieces retain:
   - `workspace_linear_run_kind=defensive_wall`
   - `workspace_connector_stitch=wall_run`
   - logical segment index and side tags for debugging

6. Add planner tests that verify:
   - all four sides generate for default keep settings
   - segment count grows when center/corner tower sizes or courtyard clearance increase
   - segment pieces reuse the default wall family when no overrides exist
   - generated pools form directed chains instead of broad cyclic perimeter pools

7. Re-scaffold and re-export `test_keep` so NBT templates include the current defensive wall stitching and air-carved interiors.

## Decisions

- Grow the perimeter clockwise from the gatehouse.
- Use whole-segment expansion only; generated keeps may be slightly larger than the requested minimum span.
- Collapse default perimeter authoring to one shared `keep_wall_segment` family.
