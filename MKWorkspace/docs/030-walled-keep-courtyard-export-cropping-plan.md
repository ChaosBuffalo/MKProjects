# Walled Keep Courtyard Export Cropping Plan

## Goal

Courtyard path templates should remain compact and easy to author, while exported runtime pieces should have tight collision bounds. Designers should be able to build one square `T` path template and one square `corner T` path template, then let export create the rotated and cropped runtime variants.

This avoids the jigsaw collision problem caused by large square runtime path templates. `structure_void` can prevent visual overwrites, but it does not reduce the jigsaw bounding box. Runtime pieces must therefore be physically cropped during export.

## Problem

The current courtyard path plan wants path pieces that can route around the default `17x17` center keep. If we make those authored path pieces large enough to span the keep side and keep them square at runtime, the whole square template footprint becomes occupied for jigsaw placement.

With default settings:

- Center keep footprint is `17x17`.
- Shell margin is `1`.
- Exterior air margin is `2`.
- Connector/opening width is `3`.
- Current courtyard path pieces are `9x9`.
- A one-piece-per-side path ring would need a much larger logical span, `25` blocks connector-center to connector-center for each side run when preserving shell margin, exterior air margin, and opening centerline alignment.

That square runtime footprint is too expensive. It collides with nearby path/content pieces and consumes the room intended for courtyard slots.

## Logical Path Length Calculation

The path length should be calculated from the center keep footprint and workspace margins. It should not inherit the old `keep.walkway.west` or `keep.walkway.east` family length.

Use two different sizes:

- Logical path span: the connector-center distance needed for the path graph to route around the center keep.
- Runtime collision footprint: the cropped exported template bounds after rotation.

The logical side-run span should be:

```text
laneCenterInset = shellMargin + exteriorAirMargin + floor(openingWidth / 2)
sideRunSpan = centerKeepInteriorSpan + (2 * laneCenterInset)
sideRunSpan = snapToOdd(sideRunSpan)
```

Where:

- `centerKeepInteriorSpan` is `keep.center` width for east-west path runs and `keep.center` length for north-south path runs.
- `shellMargin` keeps the path outside the tower shell.
- `exteriorAirMargin` preserves the designer decoration margin around the center keep.
- `floor(openingWidth / 2)` aligns the calculation to the walkway connector centerline.
- `snapToOdd` preserves centered connector placement.

For the default walled keep:

```text
centerKeepInteriorSpan = 17
shellMargin = 1
exteriorAirMargin = 2
openingWidth = 3

laneCenterInset = 1 + 2 + 1 = 4
sideRunSpan = 17 + (2 * 4) = 25
```

So the default side path pieces need a logical through span of `25`, not `9`, to loop around the default center keep correctly.

The minimum cross-lane runtime width is a separate calculation:

```text
crossLaneWidth = openingWidth + (2 * shellMargin) + (2 * exteriorAirMargin)
```

For the default values:

```text
crossLaneWidth = 3 + 2 + 4 = 9
```

That means a straight or T-style side path may need an authored logical canvas of at least `25x25`, but the cropped runtime piece should be closer to a long narrow footprint, roughly `25x9` before any extra connector/decorative padding is included. The exported NBT bounds may be slightly larger depending on how the crop includes edge jigsaws and required air, but it should not remain a full `25x25` square.

Corner-T pieces should use the same `laneCenterInset` so their incoming and outgoing connectors land on the same ring centerlines as the side runs. The corner piece does not need to consume a full square collision footprint either; after rotation, export should crop it to the smallest rectangle that contains the corner walkway, side branch, jigsaws, and required air.

This calculation should be part of planner output or export metadata so the exported runtime path variants are derived from the actual keep dimensions. If the center keep grows, the path side-run span grows. If the opening width or margins change, the lane centerline moves and the path span updates with it.

## Design

Keep designer-facing authoring compact:

- Author one square `keep_courtyard_path_t` source template.
- Author one square `keep_courtyard_path_corner_t` source template.
- Designers do not create directional variants.
- Designers do not manually crop variants.

At export time:

1. Load the authored source template.
2. Apply the planned rotation for the runtime variant.
3. Compute the meaningful export bounds for that rotated variant.
4. Crop the exported NBT to those bounds.
5. Rebase all jigsaw connector positions to the cropped origin.
6. Emit pool metadata for the cropped runtime piece.

The planner can still create designer-invisible runtime pieces using template reuse metadata. Export becomes responsible for converting each reused runtime piece into a tight physical template.

## Crop Bounds

The crop should include:

- All non-void walkway floor blocks.
- All jigsaw blocks and connector markers.
- Any required air blocks used to guarantee passable walkway space.

The crop should exclude:

- Empty quadrants or unused halves of the square source template.
- Structure void regions that exist only to make the authoring template readable.
- Authoring-only padding that would increase runtime collision bounds.

Crop bounds should be detected from non-structure-void blocks after rotation. The detected bounds must then be normalized for jigsaw practicality:

- Exported runtime width and length should remain odd.
- Jigsaw connectors should remain centered on their connection edge when possible.
- If a detected crop would make a connector off-center by one block, expand the crop instead of shifting the connector into an awkward offset.
- Do not preserve extra decoration margin beyond what the detected non-structure-void bounds require.

For courtyard path templates, the likely runtime shapes are:

- Straight/T path variants crop to a narrow rectangle around the through path plus the side branch.
- Corner-T path variants crop to the L-shaped/corner routing area, represented as the tight rectangular bounds around the actual occupied L plus connector area.

NBT templates still export as rectangular bounding boxes, so an L-shaped piece will use the smallest rectangle that contains the L. The important win is that this rectangle is much smaller than the full square authoring canvas.

## Connector Rebasing

Connector rebasing is the highest-risk part of this feature.

After rotation and crop:

- Connector facing must be rotated.
- Connector local block position must be shifted by the crop minimum.
- Connector lateral and forward offsets must still describe the cropped template correctly.
- Any metadata used by scaffold or export to place walkway lanes must refer to the rotated/cropped orientation.

Example:

If a connector is at local position `(10, 1, 0)` after rotation and the crop removes `minX = 6`, then the exported connector position becomes `(4, 1, 0)`.

If connector positions are correct but offsets are not, generation may connect pieces at the wrong edge. If offsets are correct but NBT jigsaw positions are not rebased, generation may fail or place visibly offset pieces.

## Rotation Order

Cropping must happen after rotation.

The source template may be square and symmetric in dimensions, but the meaningful occupied area is directional after rotation. Cropping before rotation would either:

- Produce the wrong tight bounds for some variants, or
- Require separate crop rules for every rotation before the final orientation is known.

The intended pipeline is:

```text
source authoring template -> rotate -> detect crop bounds -> crop NBT -> rebase connectors -> write runtime template
```

## Planner Impact

The planner should continue treating courtyard path pieces as logical pieces with planned rotations and target slots.

The planner should not rely on a large square runtime footprint to make the path loop around the center tower. Instead, it should provide enough metadata for export/scaffold to derive:

- Path shape: `t` or `corner_t`.
- Runtime rotation.
- Path lane width.
- Path lane inset or connector-driven lane placement.
- Source template reuse id.

The planner may eventually need to distinguish logical routing span from exported collision footprint. The collision footprint should come from export bounds, not from the original square authoring canvas.

## Scaffold Impact

The scaffold builder can keep generating square authoring templates for designers, but the generated blocks should make crop detection straightforward:

- Walkway floor blocks mark the intended occupied lane.
- Structure void marks unused authoring space.
- Jigsaw blocks/connectors are placed where runtime connections should survive cropping.

If crop detection is block-based, the scaffold must not fill unused authoring area with ordinary air that should be preserved as runtime collision space. Use structure void for unused regions that should not force the crop larger.

## UI Impact

The workspace UI should continue showing the compact authoring sources to designers. Runtime variants should remain implementation details.

Useful UI/debug fields:

- Source template id.
- Runtime variant id.
- Rotation.
- Cropped export size.
- Crop offset from the source template origin.

These are mainly useful in diagnostics or advanced piece details, not primary designer controls.

Runtime cropped templates should only be written during export. Workspace authoring and preview should remain based on the compact source templates and logical derived pieces, with optional diagnostics showing what export will crop to.

## Tests

Add focused tests for export cropping:

- A rotated `T` path variant exports with a smaller bounding box than the source square.
- A rotated `corner T` path variant exports with a smaller bounding box than the source square.
- Jigsaw connector positions are rebased after crop.
- Connector facings are rotated correctly.
- Pool entries reference the cropped runtime template id, not the source authoring template id.
- Cropped variants still generate without collision in the default walled keep plan.

Add regression coverage for the current failure mode:

- A default walled keep with courtyard enabled should not export oversized square path runtime templates.
- Runtime path template bounds should leave enough room for the default small, medium, and large courtyard content sockets.

## Generalization

This feature should be implemented as a generalized compact-authoring export capability, not as courtyard-path-only behavior.

Courtyard paths are the motivating case, but the underlying problem applies anywhere designers should author a simple source template while generation needs multiple rotated or cropped runtime variants. The crop pipeline should therefore be driven by template reuse/export metadata that can apply to any derived runtime template.

The generalized behavior should be:

- Source authoring templates remain designer-facing.
- Derived runtime templates can request export-time rotation and crop.
- Crop bounds are detected from non-structure-void blocks after rotation.
- Crop normalization preserves odd dimensions and centered jigsaw placement.
- Connectors and jigsaw positions are rebased to the cropped origin.
- Cropped runtime NBT is emitted only during export.

Courtyard paths should be the first implementation target and regression case, because they have an immediate collision problem and a clear success condition. The implementation should avoid hard-coding the crop system to courtyard tags unless a courtyard-specific hook is needed to calculate logical path spans.
