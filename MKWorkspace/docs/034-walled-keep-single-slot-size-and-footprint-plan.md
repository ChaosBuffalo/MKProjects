# Walled Keep Single Slot Size And Footprint Plan

## Problem

The walled keep courtyard currently exposes separate small, medium, and large courtyard content sizes. That gives designers more choices than they need for the default keep and makes the planner harder to reason about. At the same time, keep sizing settings can grow the planned footprint, but the workspace UI does not currently show whether the resulting jigsaw structure fits inside Minecraft's hard placement bounds.

The jigsaw structure has two relevant limits:

- `max_distance_from_center` is encoded as `1..128`.
- Terrain adaptation reduces the usable maximum. `BEARD_THIN` currently consumes `12`, so the effective cap is `116`.

The topology UI should make the current footprint obvious while editing, and the planner should reject or clamp invalid size combinations before designers discover them through runtime generation cutoffs.

## Goals

- Replace courtyard small, medium, and large template sizes with one odd square content slot size.
- Make this a clean data migration with no compatibility shim for old workspace JSON.
- Make keep sizing settings interdependent through one shared fit calculator.
- Guarantee that generated keep plans fit inside the effective jigsaw distance cap:
  - `128` when terrain adaptation contributes no padding.
  - `116` when using `BEARD_THIN`.
- Surface the current planned footprint prominently on the topology page.
- Restrict UI controls to values that can pass planner validation.
- Keep planner/export validation defensive so hand-edited JSON cannot bypass the limits.

## Non-Goals

- Do not support multiple courtyard content size classes after this migration.
- Do not preserve old workspace JSON. There is no production data, and test data will be regenerated.
- Do not rely on vanilla jigsaw rotation.
- Do not rely on collision failure or missing pools to enforce fit.
- Do not fail the whole keep when only courtyard content cannot fit. Disable courtyard and report why.
- Do not remove the exterior air margin; it remains a designer decoration space.

## Current State

`MKWalledKeepCourtyardSettings` currently stores:

```text
courtyardSmallTemplateSize
courtyardMediumTemplateSize
courtyardLargeTemplateSize
```

The current planner maps seven courtyard sockets to those size classes:

```text
north_west, north, north_east -> large
west, east                   -> small
south_west, south_east       -> medium
```

The current test keep worldgen structure uses:

```json
"terrain_adaptation": "beard_thin",
"max_distance_from_center": 116,
"size": 18
```

Because `BEARD_THIN` adds `12`, the effective hard limit is already saturated:

```text
116 + 12 = 128
```

With the current default keep sizing, the planned horizontal footprint is approximately `70` blocks from the start center, so the default keep fits. Larger keep, wall, margin, socket, or path settings can reduce that headroom.

## Data Model Migration

Replace the three courtyard size settings with a single field:

```text
courtyardContentTemplateSize
```

Recommended default:

```text
9
```

Validation:

- Size must be at least `3`.
- Size must be odd.
- Size must fit the current courtyard socket fit limit.
- Size must fit the current jigsaw footprint limit after all other keep dimensions are applied.

Clean-break migration:

- Remove `courtyard_small_template_size`, `courtyard_medium_template_size`, and `courtyard_large_template_size` from the codec.
- Require or default only `courtyard_content_template_size`.
- Do not add compatibility reads for the old fields.
- Regenerate the checked-in `test_keep` workspace/export/generated data after the code change.

Runtime tags:

- Keep `workspace_content_size`, `workspace_content_width`, and `workspace_content_length`.
- Remove or stop writing `workspace_content_socket_class` and `workspace_courtyard_socket_class`.
- Remove old class-based filtering rather than keeping legacy ignore paths.

Template generation:

- Generate one authoring content template, for example `keep_courtyard_content`.
- Generate runtime variants for each socket through export/template reuse:
  - `keep_courtyard_content_north_west`
  - `keep_courtyard_content_north`
  - `keep_courtyard_content_north_east`
  - `keep_courtyard_content_west`
  - `keep_courtyard_content_east`
  - `keep_courtyard_content_south_west`
  - `keep_courtyard_content_south_east`

## Shared Footprint Calculator

Add a reusable calculator owned by the workspace planner layer, not the UI. Suggested shape:

```text
MKWalledKeepFitReport
MKWalledKeepFootprint
MKWalledKeepSizingCalculator
```

The calculator should be pure and deterministic so both planner and UI can call it.

Inputs:

- Center tower width and length.
- Shared or unique corner tower width and length.
- Gatehouse width and length.
- Wall segment length and width.
- Wall segment count on each side.
- Entry approach length.
- Courtyard path logical span and cropped runtime span.
- Courtyard content slot size.
- Shell margin.
- Exterior air margin.
- Opening width.
- Terrain adaptation padding.
- Structure `max_distance_from_center`.
- Structure jigsaw `size` depth budget.

Outputs:

- Planned footprint width and length.
- Maximum distance from the start center on each axis:
  - `westDistance`
  - `eastDistance`
  - `northDistance`
  - `southDistance`
- Required `max_distance_from_center`.
- Effective hard cap.
- Remaining headroom.
- Planned jigsaw depth estimate.
- Validation status and reasons.
- Courtyard enablement status and disable reason, if only courtyard fails.

## Footprint Formula

Use conservative runtime collision bounds, not authoring canvas dimensions.

Effective hard cap:

```text
effectiveJigsawCap = 128 - terrainAdaptationPadding
```

Terrain padding:

```text
NONE = 0
BEARD_THIN = 12
BEARD_BOX = 12
BURY = 12
ENCAPSULATE = 12
```

Required distance:

```text
requiredDistance = max(westDistance, eastDistance, northDistance, southDistance)
```

Validation:

```text
requiredDistance <= configuredMaxDistanceFromCenter
configuredMaxDistanceFromCenter <= effectiveJigsawCap
```

For generated structures, choose the full effective cap for the selected terrain adaptation:

```text
exportedMaxDistanceFromCenter = effectiveJigsawCap
```

For `test_keep` with `BEARD_THIN`, this can never exceed `116`.

There is no practical benefit to writing a smaller value than the selected terrain mode allows. The workspace fit report should still calculate and show the actual required footprint and headroom, but the generated structure JSON should not artificially constrain jigsaw placement below the hard cap.

This means `max_distance_from_center` is a selected-mode capacity value, not a summary of the current keep footprint. The topology page is responsible for showing the useful footprint information:

```text
required radius: 70
configured cap: 116
headroom: 46
```

The structure JSON should provide the maximum legal room for generation. The planner decides whether a specific keep fits inside that room.

## Courtyard Fit Formula

The courtyard slot size must fit the realized interior span after wall segment expansion.

Use the existing realized span idea, but expose it through the shared calculator:

```text
realizedHorizontalSpan = max(requiredHorizontalSpan, frontWallSpan, backWallSpan)
realizedVerticalSpan = max(requiredVerticalSpan, sideWallSpan)

freeHorizontal = floor((realizedHorizontalSpan - centerWidth) / 2)
freeVertical = floor((realizedVerticalSpan - centerLength) / 2)

socketMax = largestOddAtMost(min(freeHorizontal, freeVertical) - courtyardSocketClearance)
```

With a single content size:

```text
courtyardContentTemplateSize <= socketMax
```

The UI should only offer odd socket sizes from `3..socketMax`, further clamped by the jigsaw footprint limit.

## Path Length Formula

Keep the courtyard path length calculation from the export cropping plan, but make it part of the shared sizing report:

```text
laneCenterInset = shellMargin + exteriorAirMargin + floor(openingWidth / 2)
pathLogicalSpan = snapToOdd(centerKeepSpan + (2 * laneCenterInset))
```

Runtime collision footprint should use cropped export bounds:

```text
crossLaneWidth = openingWidth + (2 * shellMargin) + (2 * exteriorAirMargin)
```

The authored path canvas may be square for designer simplicity. The runtime footprint used for jigsaw bounds must be the cropped exported rectangle.

## Interdependent Settings

The sizing calculator should drive these constraints:

- Increasing center tower size increases courtyard path span.
- Increasing center tower size reduces free courtyard socket space.
- Increasing corner tower size increases required perimeter span.
- Increasing wall segment length changes segment counts and realized wall spans.
- Increasing courtyard content slot size may force additional wall segments.
- Adding wall segments increases the total footprint and may hit the jigsaw cap.
- Increasing shell margin or exterior air margin increases path span and piece footprints.
- Increasing opening width moves connector centerlines and increases path/cross-lane width.

The planner should apply settings in this order:

1. Normalize odd tower and socket dimensions.
2. Calculate path logical spans from center keep and margins.
3. Calculate minimum perimeter spans.
4. Expand wall runs by whole wall segments only.
5. Calculate realized interior spans.
6. Validate courtyard content size against realized socket capacity.
7. Calculate full structure footprint and jigsaw depth estimate.
8. If courtyard alone exceeds limits, disable courtyard and recalculate footprint without courtyard.
9. If the base keep still exceeds limits, report an invalid keep plan.

## UI Plan

Add a prominent footprint indicator to `WorkspaceTopologyDefaultsPage`.

Suggested display:

```text
Footprint: 141 x 129
Jigsaw radius: 70 / 116
Headroom: 46
Courtyard slot: 9 / max 13
Status: Fits
```

When close to the limit:

```text
Jigsaw radius: 110 / 116
Headroom: 6
Status: Near limit
```

When invalid:

```text
Jigsaw radius: 124 / 116
Over limit by: 8
Status: Invalid
```

Behavior:

- Show the indicator near the top of the topology page, before detailed controls.
- Show directional extents as a small overhead diagram, not only a single max radius number.
- Include north, south, east, and west distances from the start center in the diagram.
- Use the same calculator as the planner.
- Recompute whenever tower size, wall segment length, socket size, margins, opening width, or terrain mode changes.
- Restrict socket size controls to odd values that fit the current report.
- If no socket size can fit, disable courtyard controls and show the disable reason.
- Show courtyard-disabled reasons in both UI and logs.

The indicator should not depend on exported data. It must be calculated from the current draft settings.

Suggested directional display:

```text
          N 70
       +---------+
W 65   |  KEEP   |   E 65
       +---------+
          S 59

Radius: 70 / 116
Headroom: 46
```

The exact UI does not need to be ASCII; the topology page should render a compact overhead indicator that makes directional imbalance obvious while editing.

## Planner Changes

Update `MKWalledKeepWorkspacePlanner`:

- Replace class-based socket definitions with socket ids only.
- Remove `preferredClassName` from `CourtyardSocketDefinition`.
- Replace `courtyardClassSizes` with a single content size lookup.
- Generate one source content piece.
- Generate runtime socket variants using the same source piece.
- Use `MKWalledKeepSizingCalculator` for:
  - perimeter segment counts,
  - courtyard socket fit,
  - path span,
  - entry approach length,
  - final footprint validation.
- Add planner diagnostics tags for footprint and cap:
  - `workspace_keep_footprint_width`
  - `workspace_keep_footprint_length`
  - `workspace_keep_required_jigsaw_radius`
  - `workspace_keep_jigsaw_radius_cap`
  - `workspace_keep_jigsaw_radius_headroom`

## Export And Data Generation

Update generated data flow:

- Runtime templates should still be written only during export.
- Export should keep using cropped runtime bounds for rotated/cropped path variants.
- Structure data generation should use the sizing report when possible.
- `test_keep` may remain configured at `116` for `BEARD_THIN`, but tests should assert the calculated default footprint is below that cap.

If structure JSON remains static:

- `NpcStructures` keeps a fixed `max_distance_from_center` and terrain adaptation for `test_keep`.
- Planner/UI must treat the static `max_distance_from_center` as the configured cap for every workspace using that structure id.
- Invalid plans should be caught before export/generation.
- Smaller structures still reserve the same jigsaw distance at runtime.
- Changing topology sizing does not require regenerating structure JSON unless pools/templates changed.

If structure JSON becomes workspace-derived:

- Data generation/export reads the workspace terrain adaptation setting.
- Write `max_distance_from_center = effectiveJigsawCap`.
- The terrain adaptation mode is also sourced from workspace/topology settings.
- Keep `test_keep` at or below `116` while using `BEARD_THIN`.
- Changing topology sizing requires regenerating structure JSON as part of the normal workspace export/data generation workflow.

Under this approach, deriving structure JSON from the workspace does not mean writing the exact required radius. It only means the selected terrain adaptation mode controls which full legal cap is written. For example:

```text
NONE         -> max_distance_from_center = 128
BEARD_THIN   -> max_distance_from_center = 116
BEARD_BOX    -> max_distance_from_center = 116
BURY         -> max_distance_from_center = 116
ENCAPSULATE  -> max_distance_from_center = 116
```

The exact current keep footprint remains a validation/reporting value, not the structure JSON cap.

Terrain adaptation controls:

- The topology UI should allow every valid Minecraft terrain adaptation mode currently supported by the structure codec:
  - `NONE`
  - `BEARD_THIN`
  - `BEARD_BOX`
  - `BURY`
  - `ENCAPSULATE`
- The footprint report should show the effective cap produced by the selected mode.
- `NONE` allows the full `128` jigsaw distance cap.
- `BEARD_THIN`, `BEARD_BOX`, `BURY`, and `ENCAPSULATE` currently reserve `12` blocks, giving an effective cap of `116`.

Exact extent calculation:

- The planner should calculate exact runtime extents rather than adding a safety margin.
- Exact extents should be calculated from the same planned piece graph, connector offsets, rotations, and exported runtime bounds used for template generation.
- Export-cropped runtime bounds should be available to the calculator before validation, or validation should run after the crop bounds are known.
- If there is any mismatch between the planner report and final exported NBT bounds, that should be treated as a bug in the calculator/export integration rather than covered with padding.
- Fit validation should report the true calculated footprint and compare it directly against the effective cap.

The expected exact calculation flow is:

1. Build the deterministic planned jigsaw graph.
2. Resolve each planned piece to its runtime template bounds.
3. Apply template reuse rotation and export crop bounds.
4. Walk connector-to-connector placement to assign each piece a runtime origin.
5. Union all runtime bounds into one footprint.
6. Measure north, south, east, and west distance from the start center.
7. Validate `max(north, south, east, west) <= effectiveJigsawCap`.

Because this calculation uses the same connector offsets and runtime bounds as export/generation, off-by-one drift should not be accepted as normal. Common sources of drift, such as inclusive bounding boxes or connector center alignment, should be encoded directly in the calculator and covered by tests.

## Tests

Add unit tests for:

- New settings encode/decode with only `courtyard_content_template_size`.
- Old small/medium/large setting fields are not supported.
- Default test keep footprint fits `BEARD_THIN` cap.
- Same default footprint fits `NONE` cap.
- Oversized courtyard disables courtyard but keeps base keep valid when possible.
- Oversized base keep reports invalid when it cannot fit even without courtyard.
- UI-facing fit report clamps socket sizes to odd values only.
- Footprint report changes when center tower size, wall segment length, exterior air margin, and opening width change.

Add generated-data checks for:

- No `workspace_content_socket_class` or `workspace_courtyard_socket_class` in newly generated courtyard content.
- Only one courtyard content source template is generated.
- Runtime variants still exist for each socket.

## Implementation Order

1. Add `MKWalledKeepSizingCalculator` and report records without changing behavior.
2. Add tests for current default footprint and current `BEARD_THIN` cap.
3. Migrate `MKWalledKeepCourtyardSettings` to one content size with no backward-compatible decoding.
4. Update planner socket/content generation to use the single size.
5. Replace planner-local span calculations with the shared calculator.
6. Add courtyard/base keep validation and diagnostics.
7. Add topology page footprint indicator and constrained socket controls.
8. Update docs/tests/generated data.
9. Regenerate `test_keep`, re-export/import as needed, and verify in-game generation.

## Open Decisions

- Whether structure JSON should remain static for `test_keep` or be derived from workspace sizing reports.

## Resolved Decisions

- The topology UI should expose all supported terrain adaptation modes: `NONE`, `BEARD_THIN`, `BEARD_BOX`, `BURY`, and `ENCAPSULATE`.
- The topology page should show a directional footprint diagram with north, south, east, and west extents from the start center.
- Generated structure JSON should always set `max_distance_from_center` to the full effective cap for the selected terrain adaptation: `128` for `NONE`, `116` for terrain modes with `12` blocks of padding.
- The planner should calculate exact runtime extents and should not use a safety margin to hide off-by-one errors.
