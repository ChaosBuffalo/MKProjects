# Compact Rotated Template Authoring Plan

## Problem

The walled keep currently creates authored workspace pieces for every directional variant of repeated wall segments and shared corner tower pieces. Many of these pieces are only rotations of the same authored design, but designers still have to review, edit, export, and import them as separate templates.

This is especially noisy for:

- defensive wall segments repeated around the perimeter
- shared corner tower stack pieces
- future repeated path or ring pieces that are directionally distinct in the graph but visually identical under rotation

Vanilla jigsaw rotation should not be used to solve this. The keep perimeter needs deterministic connector identity, side-specific pools, and controlled graph order. Runtime generation should continue to receive concrete structure files with correctly assigned jigsaw blocks.

## Goals

- Let designers author one canonical template for rotationally equivalent pieces.
- Keep designers unaware of derived directional variants during normal authoring.
- Preserve all logical runtime variants needed by jigsaw generation.
- Generate rotated runtime NBTs during export with correct jigsaw orientation, `name`, `target`, and `pool`.
- Preserve compact authoring when importing a workspace export.
- Keep support for explicit unique directional templates when designers opt into them.
- Avoid changing vanilla pool placement semantics.

## Non-Goals

- Do not rely on vanilla jigsaw random rotation.
- Do not remove runtime variant pieces from exported data packs.
- Do not require all keep pieces to be rotationally reusable.
- Do not compact templates whose dimensions, connector topology, or authored details are intentionally unique.

## Core Model

Separate authored templates from logical runtime pieces.

The planner should still emit every logical runtime slot:

```text
keep_wall_segment_west_0
keep_wall_segment_west_1
keep_wall_segment_north_0
keep_wall_segment_east_0
keep_corner_north_west_entry
keep_corner_north_east_entry
```

But equivalent pieces can point at one authoring source:

```text
keep_wall_segment_authoring
keep_corner_shared_entry_authoring
```

Each derived runtime piece stores:

```text
workspace_template_source_id = keep_wall_segment_authoring
workspace_template_rotation = clockwise_90
workspace_template_reuse_mode = rotate_export
```

The exact tag names can be adjusted, but they should stay separate from `workspace_base_name`. `workspace_base_name` already groups runtime variants for pools and metadata; authoring-source identity is a different concept.

## Rotation Metadata

Use a small rotation enum rather than raw degrees:

```text
none
clockwise_90
clockwise_180
clockwise_270
```

For wall segments, define a canonical authored orientation. A practical default is "path runs west to east" with connectors on west/east. North/south wall segments then derive from the same source through 90 or 270 degree rotation.

For shared corner towers, define a canonical authored corner, likely north-west. Other shared corners derive from it:

```text
north_west -> none
north_east -> clockwise_90
south_east -> clockwise_180
south_west -> clockwise_270
```

This only applies when the shared corner stack is actually shared. The walled keep planner should enforce that every shared corner tower uses the same horizontal dimensions and that those dimensions are square. If a corner is unique, it should remain independently authored and should not participate in the shared rotated template source.

## Shared Corner Dimension Invariant

The walled keep planner should make shared corner towers rotationally valid by construction.

When shared corner towers are enabled:

- all shared corner tower stack pieces use the same width
- all shared corner tower stack pieces use the same length
- width and length must be equal
- every shared corner stack slot uses the same horizontal dimensions for its corresponding stack piece kind

If workspace settings try to give shared corners non-square dimensions, the planner should normalize them to a square size before planned pieces are emitted. A conservative rule is:

```text
sharedCornerSize = max(configuredWidth, configuredLength)
```

Then apply `sharedCornerSize` as both width and length for all shared corner tower pieces.

Unique corner towers are outside this invariant. They may keep independent horizontal dimensions, but they should not be compacted through the shared corner authoring source unless they independently satisfy a future explicit reuse contract.

## Workspace Piece Kinds

Add a clear distinction between authoring sources and derived runtime pieces.

Recommended tags:

```text
workspace_piece_kind = template
workspace_authoring_piece = true
workspace_template_source_id = keep_wall_segment_authoring
workspace_template_rotation = none
```

For derived runtime pieces:

```text
workspace_piece_kind = instance
workspace_authoring_piece = false
workspace_template_source_id = keep_wall_segment_authoring
workspace_template_rotation = clockwise_90
```

The workspace model can continue to store all logical pieces, but UI/scaffold/import should know which pieces are editable authoring sources and which are derived runtime instances.

## Planner Changes

Add a reusable template-source assignment phase after logical pieces are planned.

For each planned piece:

1. Determine whether it belongs to a rotational reuse group.
2. Select or create the canonical authoring source id.
3. Assign the required rotation from canonical source to logical target.
4. Mark unique or incompatible pieces as non-reused.

Initial reuse groups:

- default walled keep defensive wall segment family
- shared walled keep corner tower stack pieces

Later reuse groups can be added for repeated walkways, symmetric gates, or other topology primitives.

## Scaffold Changes

The scaffold currently builds one workspace piece for every planned piece. Compact authoring needs two paths:

- Build normal authored pieces for canonical source templates.
- Represent derived runtime pieces without placing full editable duplicate templates.

Use Option A: store derived logical pieces in the workspace but do not place their blocks in the authoring grid.

Designers should only see and edit canonical authoring source templates. Derived wall segments and shared corner tower variants should not appear as editable template regions, signs, structure blocks, or placeholders in normal authoring.

The workspace model remains authoritative for runtime export:

- canonical source pieces have editable export bounds
- derived logical pieces keep tags, connectors, base names, variant indices, and rotation metadata
- derived logical pieces do not require placed block regions
- export materializes derived runtime NBTs from their source authoring template

If visual debugging is needed later, add it as an explicit developer/debug mode rather than part of the designer-facing workspace.

## Export Changes

The export writer should continue producing concrete runtime structure files for every non-template runtime piece.

For normal pieces:

1. Capture the piece NBT from its own export bounds.
2. Write it under `data/<namespace>/structure/<structure>/<piece>.nbt`.

For derived rotated pieces:

1. Resolve the source authoring piece by `workspace_template_source_id`.
2. Capture source NBT from the source export bounds.
3. Rotate the source template data into the target piece dimensions.
4. Remove or rewrite source jigsaw block entities.
5. Rebuild target jigsaw blocks from the target planned connector definitions.
6. Write the resulting NBT under the target piece name.
7. Write runtime metadata for the target piece as usual.

The manifest should still list the target runtime piece and structure id:

```text
mknpc:test_keep/keep_wall_segment_north_0_1
```

That keeps generated data pack output compatible with the existing runtime pool model.

## Jigsaw Rewrite Rules

Do not copy connector identity from the source template.

During rotated export, jigsaw blocks need to be rebuilt from the target logical piece:

- rotate or recompute the connector position
- set `JigsawBlock.ORIENTATION` from the target connector facing
- set `name` from the target connector incoming pool when present
- set `target` from the target connector target pool when present
- set `pool` from the target connector target pool
- preserve `final_state = minecraft:air`
- preserve aligned joint behavior

This should use the same identity rules as `MKWorkspaceScaffoldBuilder.placeConnector`, because that path already handles pool-specific connector identities.

## Block Rotation

Add a focused rotation utility for exported structure templates.

The utility should handle:

- block positions within the source template volume
- horizontal block states that expose rotation behavior
- block entities with position fields
- structure template size after rotation
- marker/sign/structure block exclusion or rewrite

The safest path is to operate through Minecraft's `StructureTemplate` and `StructurePlaceSettings` rotation support where possible. If that is not sufficient for saved NBT generation, add a small NBT-level rotation utility with tests around jigsaw blocks and representative directional blocks.

## Manifest Changes

Add authoring reuse metadata to the workspace export manifest.

Recommended new section:

```json
"authoring_templates": [
  {
    "source_id": "keep_wall_segment_authoring",
    "source_piece": "keep_wall_segment_template",
    "reuse_mode": "rotate_export"
  }
],
"piece_template_sources": [
  {
    "piece_name": "keep_wall_segment_north_0_1",
    "source_id": "keep_wall_segment_authoring",
    "rotation": "clockwise_90"
  }
]
```

This can also live on each `ExportPiece` if that is less invasive:

```json
"template_source_id": "keep_wall_segment_authoring",
"template_rotation": "clockwise_90"
```

Recommendation: put compact fields on each `ExportPiece` and optionally add a grouped summary later. Per-piece metadata is easier for import and supports partial reuse.

## Import Preservation

Import should reconstruct compact authoring, not expand every rotated runtime variant into editable templates.

Import flow:

1. Read the manifest.
2. Identify authoring source pieces.
3. Place only source authoring templates into the workspace authoring grid.
4. Recreate derived logical workspace pieces from manifest metadata without placing full duplicate block regions.
5. Preserve each derived piece's runtime tags, connectors, base name, variant index, and rotation metadata.
6. Rebuild structure blocks/signs only for editable source pieces, unless placeholders are added.

Runtime exported NBTs for derived pieces should not become new authoring sources on import unless the manifest lacks compact authoring metadata. This gives old exports a fallback path while preserving compact authoring for new exports.

## Compatibility

Old manifests without compact authoring metadata should import exactly as they do today.

New manifests should:

- keep all runtime pieces in `pieces`
- keep all runtime pools in `runtime_hints`
- include compact authoring source metadata
- include derived piece rotation metadata

If a source authoring template is missing during import, fail the import preflight rather than silently expanding from a runtime derivative.

## Implementation Steps

1. Add rotation and template-source tag constants.
2. Enforce square, dimension-matched shared corner tower settings in the walled keep planner.
3. Add template-source metadata to planned pieces for default wall segment reuse and shared corner tower reuse.
4. Extend the workspace model or piece tags so derived pieces can reference an authoring source and rotation.
5. Update scaffold generation to create editable source templates once and preserve derived logical pieces without duplicate editable block regions.
6. Add export-time rotated NBT generation for derived wall segments and shared corner tower stack pieces.
7. Rebuild jigsaws from target logical connectors during derived export.
8. Add manifest fields for per-piece authoring source and rotation.
9. Update import preflight and placement to preserve compact authoring from new manifests.
10. Add fallback behavior for old manifests and non-reusable pieces.

## Tests

Add focused tests before broad data regeneration.

Planner tests:

- default keep wall segments share one template source
- wall segment directions receive expected rotations
- unique or incompatible pieces are not compacted
- shared corner tower pieces share one template source when shared corners are enabled
- shared corner tower dimensions are normalized to one square horizontal size
- unique corner tower pieces do not use the shared corner source

Export tests:

- derived wall segments produce concrete structure ids under their target names
- derived shared corner tower pieces produce concrete structure ids under their target names
- rotated jigsaw orientation matches target facing
- rotated jigsaw `name`, `target`, and `pool` match target connector pools
- runtime pool membership still contains target base names

Import tests:

- compact manifest imports with one editable wall authoring template
- compact manifest imports with one editable shared corner tower authoring stack
- derived wall logical pieces survive import with tags/connectors intact
- derived shared corner tower logical pieces survive import with tags/connectors intact
- old manifests without compact metadata still import expanded
- missing source authoring template fails preflight

Integration tests:

- exported test keep still generates all perimeter sides
- generated wall interiors remain walkable
- corner tower to wall shell stitching remains intact after derived export

## Risks

- Rotating arbitrary block entity NBT can be error-prone. Cover wall segments and shared corner tower stack pieces together, with tests around jigsaw blocks and common directional block states before broader reuse.
- Compact logical pieces without placed regions may expose assumptions in UI or mutation services that every piece has editable blocks.
- Import/export schema changes need a clear compatibility path.
- Shared corner towers only work cleanly when horizontal dimensions are square and connector layout is rotationally symmetric.

## Initial Implementation Scope

Implement compact rotated authoring for both default wall segments and shared corner tower stack pieces in the first implementation.

These two cases should land together because they define the default walled keep authoring workflow. The first version should still keep the feature narrowly scoped to those reuse groups and avoid generalizing to arbitrary rotated pieces until the export/import path is stable.
