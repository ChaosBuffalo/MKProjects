# MK Jigsaw Generation Bounds Notes

## Question

Do MK jigsaw structures still need to obey vanilla's `max_distance_from_center` and `size` limits?

## Current State

`MKJigsawStructure` still mirrors vanilla jigsaw bounds in its codec:

- `size`: `0..20`
- `max_distance_from_center`: `1..128`
- `max_distance_from_center + terrainAdaptationPadding <= 128`

When `dungeon_layout` is present, generation uses `MKJigsawPlacement`. When `dungeon_layout` is absent, generation still delegates to vanilla `JigsawPlacement`.

## Max Depth

The vanilla `size` value acts as a placement recursion depth.

For MK dungeon-layout structures, this is now mostly a legacy guardrail because generation is also bounded by semantic layout rules:

- target floors
- max pieces per floor
- max branch depth
- per-category main path targets
- branch cap limits
- available pools and terminal pieces

We should still keep an upper bound to protect worldgen from malformed pools or bad metadata, but MK layout structures do not need to keep vanilla's exact `0..20` depth range.

## Max Distance From Center

The `128` horizontal distance limit should be treated more carefully.

The concern is not that a Java `AABB` or `BoundingBox` cannot be larger. The concern is vanilla's structure-reference system. Structure starts are discovered and referenced by chunks, and vanilla jigsaw generation is designed around a bounded horizontal footprint. If one structure start can affect chunks farther away than vanilla's reference radius, distant chunks may not know they need to consider that structure start.

That can lead to inconsistent placement behavior, especially for structures that spread too far in X/Z.

## Horizontal vs Vertical

The current vanilla field is a single cubic distance:

```text
max_distance_from_center
```

MK towers do not naturally fit that model. Vertical height consumes the same budget as horizontal spread even though chunk structure references are primarily an X/Z concern.

For towers:

- Horizontal spread should remain bounded conservatively, likely near vanilla's `128` assumption unless we intentionally change structure-reference behavior.
- Vertical growth can be allowed to exceed `128` more safely, as long as it respects build-height and dimension padding.
- A single cubic `max_distance_from_center` is too blunt for tall tower generation.

## Recommended Direction

Keep vanilla-compatible behavior for non-layout structures that still call vanilla `JigsawPlacement`.

For MK dungeon-layout structures, introduce MK-specific bounds:

- `max_horizontal_distance_from_center`
- `max_vertical_distance_from_start`

The horizontal bound should default to the current `max_distance_from_center` behavior and should remain conservative.

The vertical bound should be derived from the authored tower plan when possible:

- category heights
- number of generated floors
- optional cap approaches
- dimension padding
- world min/max build height

An explicit vertical override could still be useful for debugging or unusual structures.

## Compatibility

Keep the existing `max_distance_from_center` field for old data and vanilla-style structures.

Possible migration behavior:

- If only `max_distance_from_center` exists, use it for horizontal and vertical bounds.
- If MK-specific bounds exist, use them for `dungeon_layout` generation.
- Continue to enforce vanilla `128` validation only for non-layout/vanilla placement paths.

## Open Questions

- Should horizontal distance remain hard-capped at `128` for all MK layout structures?
- Should vertical distance be computed automatically from `MKDungeonLayoutSettings`, or stored explicitly in generated structure JSON?
- Do we need a separate maximum total piece count guardrail in addition to depth and layout rules?
- Should oversized horizontal structures be split into multiple linked structure starts instead of relaxing the reference-radius assumption?
