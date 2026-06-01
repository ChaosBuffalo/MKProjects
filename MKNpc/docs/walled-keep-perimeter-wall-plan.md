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

For the first implementation, prefer growing the keep footprint to fit whole repeated segments instead of creating one variable-length filler segment. That keeps authoring simple and avoids requiring odd custom wall pieces.

## Pool Graph

The perimeter should not rely on an open-ended loop where any wall can attach to any matching wall pool. Use directed logical pools so each segment knows the next intended slot.

Example south side from gatehouse:

```text
gate.main
  -> keep.perimeter.south.west.0
  -> keep.perimeter.south.west.1
  -> keep.corner.south_west

gate.main
  -> keep.perimeter.south.east.0
  -> keep.perimeter.south.east.1
  -> keep.corner.south_east
```

Then continue around the keep with deterministic segment chains:

```text
south_west corner -> west chain -> north_west corner
north_west corner -> north chain -> north_east corner
north_east corner -> east chain -> south_east corner
```

This avoids asking jigsaw placement to close a cycle between two already placed endpoints. It also makes failures easier to diagnose because each segment targets one expected next pool.

## Template Authoring

Default authoring should remain simple:

- One defensive wall segment template can be reused for every segment.
- Optional override families can be added later for start, middle, end, corner-adjacent, or gate-adjacent wall segments.
- Logical segment names should not imply unique designer templates unless an override is present.

Suggested family model:

```text
keep_wall_segment
keep_wall_segment_start        optional
keep_wall_segment_middle       optional
keep_wall_segment_end          optional
keep_wall_gate_adjacent        optional
keep_wall_corner_adjacent      optional
```

The planner can select the most specific available family, falling back to `keep_wall_segment`.

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

## Open Questions

- Should the default graph grow clockwise from the gatehouse, or as two arms from the gatehouse that meet only through deterministic corner paths?
- Do we want exact footprint dimensions with a filler segment, or whole-segment expansion only?
- Should north/east/south/west walls remain separate default families for palette variation, or should they collapse to one shared `keep_wall_segment` family with side tags?
