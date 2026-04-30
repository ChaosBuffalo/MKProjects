# Non-Destructive Workspace Settings Plan

## Problem

The current MKNpc workspace form treats most setting changes as a full generate operation. On submit, the client sends `CreateWorkspacePacket(..., true)`, and the server calls `generateTowerWorkspace`. That rebuild path uses `MKWorkspaceScaffoldBuilder.build`, which clears the previous preview area and creates fresh scaffold pieces. This is correct for initial generation, but it destroys authored in-world edits for many changes that could be handled more narrowly.

The goal is to classify workspace setting changes by whether they can preserve existing piece data, then introduce mutation operations that update only the affected blocks, block entities, and workspace metadata.

## Current Shape

Important existing pieces:

- `MKStructureWorkspace` stores workspace settings, piece definitions, palette, shell margin, exterior air margin, preview margin, family definitions, opening profiles, hallway families, and generated pieces.
- `MKWorkspacePieceDefinition` stores per-piece effective dimensions, shell margin, connectors, world/export/preview bounds, structure block position, sign position, marker positions, generated stair positions, and tags.
- `MKWorkspaceScaffoldBuilder` already knows how to calculate layout, bounds, shell blocks, connector positions, signs, markers, and structure block save settings.
- Import already proves a useful pattern: generate scaffold metadata, then overlay saved structure templates into the generated export area.
- Variant cloning already proves another useful pattern: copy an existing export area into a new export area, recreate connectors, markers, signs, and structure block metadata.

## Safety Levels

### Safe Metadata Update

These can be applied by changing workspace and piece metadata, plus affected block entities, without touching authored structure blocks.

- Workspace display/settings timestamps.
- Sign text refresh.
- Structure block save names and sizes when bounds are unchanged.
- Connector jigsaw pools, names, targets, final states, and metadata when connector positions are unchanged.
- Runtime/export tags when the physical topology does not change.

These should not clear or rewrite export bounds.

### Safe Block Substitution

These rewrite selected block occurrences but keep geometry and block positions stable.

- Palette swap from old floor/wall/ceiling blocks to new floor/wall/ceiling blocks.
- Generic block swaps for any selected source block ids, even when they are not part of the workspace palette.
- Stair material swap across generated stair positions.
- Marker block style updates outside export bounds.

Block substitution should be treated as an intentional material migration across the selected authored scope. That means matching user-placed blocks should be replaced too. Ownership metadata is still useful, but as an optional narrower mode for "generated scaffold only" edits, not as the default swap behavior.

Substitution must preserve compatible block state. For example, swapping `minecraft:stone_brick_stairs` to an oak stair block should keep the same facing, half, waterlogged, and stair shape values whenever the target stair block exposes matching properties.

### Safe Expansion

These can preserve authored blocks if all existing positions remain valid and new space is added outside them.

- Increasing `shellMargin` for a piece when the interior origin is kept stable and the new shell grows outward.
- Increasing `exteriorAirMargin` by adding more structure void around the existing geometry.
- Increasing preview spacing by moving whole preview cells or by leaving current placements unchanged and storing the new margin for future additions.

Expansion needs collision checks before writing. It should add new blocks around existing content, update bounds, update structure block sizes/offsets, and move markers/signs if needed.

### Safe Relayout

These preserve content by moving whole preview cells rather than regenerating pieces.

- Changing preview margin.
- Changing grid spacing or column count later.
- Reordering categories visually.
- Moving the workspace anchor, if we support it later.

The operation should copy each existing preview/export region to its new destination, then update all absolute positions in `MKWorkspacePieceDefinition`. It must handle overlap by staging copies or computing a safe move order.

### Conditionally Safe Topology Patch

These can preserve most authored data only when the changed connector or generated feature occupies a bounded area that can be surgically patched.

- Adding a new horizontal exit to an existing room family.
- Removing an existing horizontal exit.
- Changing an opening profile width/height if the new opening still fits the wall.
- Changing connector pool routing when the connector block remains in place.
- Changing vertical access placement if the old shaft can be cleared and a new shaft can be created without colliding with authored details.

These need preflight reports that show which piece families and exact regions will be rewritten. They should not be silent form-submit behavior.

### Destructive Regenerate

These should still require explicit confirmation, export/backup guidance, or a rehydrate-from-saved-template flow.

- Shrinking interior room width, room length, or height when authored blocks would fall outside the new bounds.
- Decreasing `shellMargin` or `exteriorAirMargin` when blocks would be removed.
- Changing hallway length, slope, or height in a way that changes export bounds or connector offsets.
- Changing family base names or deleting families that have existing templates/variants.
- Changing role semantics enough that piece identity, runtime classification, or required connector contracts change.
- Any operation where old and new piece counts do not map one-to-one.

## Concrete Non-Destructive Operations

### Generic Block Swap

Inputs:

- one or more source block ids
- target block id per source block id
- selected scope: all pieces, one category, one piece, or an explicit bounded region
- state preservation mode: preserve compatible properties by default

Default behavior:

1. Iterate each selected export bounds.
2. For each block, if its block id matches a configured source id, replace it with the configured target block.
3. Include designer-placed blocks that match the source ids.
4. Preserve compatible block-state properties from the original state.
5. Skip jigsaw blocks, structure blocks, signs, and marker positions unless the user explicitly includes sidecar blocks.
6. Report replacement counts per source/target pair and per piece.

Block-state preservation:

1. Start from the target block's default state.
2. For every property on the original state, find a property with the same name on the target state.
3. If the target property accepts the original serialized value, apply it.
4. If the value is not accepted, leave the target default for that property and report it as a dropped property.
5. Preserve block entity data only when the source and target block entity types are compatible; otherwise drop it and report the loss.

This gives expected behavior for common same-family swaps:

- stair to stair: preserve `facing`, `half`, `shape`, and `waterlogged`
- slab to slab: preserve `type` and `waterlogged`
- fence to fence: preserve directional connections and `waterlogged`
- wall to wall: preserve side heights, `up`, and `waterlogged`
- door to door: preserve `facing`, `half`, `hinge`, `open`, and `powered`
- trapdoor to trapdoor: preserve `facing`, `half`, `open`, `powered`, and `waterlogged`
- log to log: preserve `axis`

Palette swap becomes a preset over the generic block swapper:

- old floor block -> new floor block
- old wall block -> new wall block
- old ceiling block -> new ceiling block

When a palette swap is applied, update workspace palette and piece tags that carry palette overrides.

For future workspaces:

- Track scaffold ownership per block position or compact region. This enables an alternate "generated only" substitution mode, but the normal block swap should continue to replace all matching source blocks inside the selected export bounds.

### Stair Material Swap

Inputs:

- old stair config
- new stair config
- selected generated stair pieces

Rules:

- If only stair/slab/ladder block ids change and mode/rise geometry does not change, rewrite `generatedStairPositions` in place.
- If mode, rise type, stair width, shaft size, or resolved profile changes, treat as a generated-stair patch and clear/regenerate only the shaft footprint.

This is already close to existing `MKWorkspaceStairBuilder` behavior.

### Shell Margin Expansion

Inputs:

- old shell margin
- new shell margin

Safe only when `new > old`.

Per piece:

1. Compute old and new build contexts.
2. Keep the existing interior origin stable.
3. Add shell blocks only in the new outer ring.
4. Extend connector tunnels through the added thickness.
5. Move horizontal connector jigsaw blocks outward to the new export boundary, preserving their relative role/pool metadata.
6. Update `exportBounds`, `previewBounds`, `worldOrigin`, `structureBlockPos`, structure block size/offset, connector relative positions, markers, and sign.

Vertical connectors should usually keep the same x/z center and move only if export origin changes. Horizontal connectors must move to the new outer face.

Shrinking shell margin is destructive unless a preflight proves every removed block is scaffold-owned and empty of designer content.

### Exterior Air Margin Expansion

Safe when `new > old`.

This adds structure void around the existing geometry and updates export bounds. Authored geometry should not move. It is less risky than shell margin expansion because it should not modify the shell itself, only the exported padding area and structure block size.

Shrinking is only safe if the removed ring contains only structure void or air.

### Identity Rename

Changing namespace or structure name can be non-destructive but must update several contracts:

- structure block save name for every piece
- jigsaw target/incoming pool resource ids that include the old namespace/structure path
- workspace manifest/export identity
- signs
- piece connector metadata

The in-world authored blocks do not need to be cleared. The caveat is saved NBT files under the old name remain old assets until the user exports again.

Rename policy should be conservative:

- update the live workspace identity and in-world metadata immediately
- do not delete or move old exported structure files automatically
- on next export, write new assets to the new namespace/structure path
- write a backup manifest before applying the rename so the old identity and piece list remain recoverable

The export flow should not copy existing old structure NBT files to the new path. Rename updates only the live workspace and in-world metadata. The user exports the modified structure later when they want the new files written.

### Preview Relayout

Relayout should be a distinct operation from regeneration.

Algorithm:

1. Build old placement map from existing `MKWorkspacePieceDefinition`.
2. Build new placement map with `MKWorkspaceGridLayout`.
3. Preflight destination bounds for collisions outside old workspace-owned bounds.
4. Copy each old preview cell or export-plus-sidecar region to its new position.
5. Clear old regions after copy.
6. Remap every absolute position in each piece definition.
7. Reconfigure structure blocks, signs, markers, and connector metadata.

For overlapping moves, copy to temporary staging bounds or use a two-pass save-template/load-template approach.

Relayout must preserve exact authored block states. It should copy the existing block state at each source position and write that exact state to the destination. It should not reconstruct the piece from palette, shell, connector, or planner settings. Block entities should be copied with full metadata and their `x`, `y`, and `z` fields rewritten to the destination position. After the raw copy, only workspace-owned sidecar metadata should be refreshed:

- structure block save name, relative structure position, and size
- sign text
- connector/jigsaw pool metadata if the piece origin changed
- marker positions
- piece definition absolute positions and bounds

This keeps designer-authored stair facings, slab types, waterlogged flags, wall connections, custom block states, and non-workspace block entities intact during preview-margin relayouts.

## Required Support Code

### Change Classifier

Add a service that compares old and new workspaces before applying form changes.

Suggested class:

```java
public final class MKWorkspaceSettingsChangeClassifier {
    public MKWorkspaceChangePlan classify(MKStructureWorkspace oldWorkspace,
                                          MKStructureWorkspace newWorkspace);
}
```

The result should list:

- metadata updates
- block substitutions
- expansions
- relayouts
- patch operations
- destructive changes
- validation errors and warnings

### Mutation Service

Add a server-side service that applies classified plans.

Suggested class:

```java
public final class MKStructureWorkspaceMutationService {
    public MKWorkspaceMutationResult apply(ServerLevel level,
                                           MKStructureWorkspace oldWorkspace,
                                           MKStructureWorkspace newWorkspace,
                                           MKWorkspaceChangePlan plan);
}
```

This should be separate from `MKStructureWorkspaceService.generateTowerWorkspace` so generation remains the explicit scaffold creation path.

### Backup Manifests

Before applying any non-destructive mutation, write a backup manifest beside the normal workspace exports.

Recommended path:

- `generated/<namespace>/mk_workspace_exports/backups/<structure_name>/<timestamp>-before-<operation>.json`

The backup should capture the pre-mutation workspace model, including piece placement, bounds, tags, connectors, generated stair positions, and settings. For operations that also affect exported structure NBT files later, the backup manifest is not a full asset backup by itself, but it provides the recovery map needed to understand what changed.

### Backup Restore

Backups should be discoverable and restorable from the workspace dev block.

Discovery should scan:

- `generated/<namespace>/mk_workspace_exports/backups/<structure_name>/*.json`

The UI should expose:

- backup timestamp
- operation name
- namespace and structure name
- schema version
- piece count
- whether matching structure NBT assets are present

Restore modes:

- `Restore Settings And Layout`: restores the workspace model, piece definitions, bounds, connectors, signs, markers, and structure block metadata from the backup manifest. This does not rewrite authored piece blocks unless a relayout is required by the target anchor.
- `Restore And Rehydrate Templates`: restores the workspace model and then reloads saved structure NBT into each piece export area, using the same manifest-driven overlay approach as workspace import.

Recommended default:

- If restoring at the same anchor and existing piece bounds still match, use `Restore Settings And Layout`.
- If restoring into a fresh anchor or if current piece bounds do not match the backup, use `Restore And Rehydrate Templates` when all referenced structure NBT files exist.

Safety rules:

1. Write a new backup of the current live workspace before restoring an older backup.
2. Validate backup schema version before restore.
3. Validate piece names are unique and match the backup categories.
4. Validate destination area is clear or is wholly owned by the current workspace.
5. Do not delete exported structure NBT files during restore.
6. After restore, keep the live workspace as the editable source of truth; the user still exports when they want resources updated.

Likely implementation pieces:

- `MKWorkspaceBackupManifestDiscovery`
- `MKStructureWorkspaceBackupService`
- `RestoreWorkspaceBackupPacket`
- a dev-block UI state for backup selection and restore confirmation

Backup restore should reuse as much of the existing import path as possible. The main difference is that backup manifests live under `mk_workspace_exports/backups`, and same-anchor restore may apply metadata directly instead of rebuilding from external structure NBT.

### Ownership Metadata

Future scaffold builds should record enough ownership data to make edits safe:

- shell/floor/wall/ceiling generated regions
- exterior air/structure void regions
- connector tunnel regions
- generated stair regions
- markers/sign/structure block sidecar positions

This can start as compact rectangular regions per piece rather than a block-by-block mask. Generic swaps should still default to authored-scope replacement, but ownership data allows a narrower generated-only mode when needed.

### UI Flow

Replace the current single submit behavior with separate actions:

- Save Settings
- Apply Safe Changes
- Preview Impact
- Regenerate Workspace

When a change plan contains destructive operations, the UI should show that regeneration is required instead of silently doing it.

## First Milestone

The first useful milestone should avoid topology changes:

1. Add a classifier for generic block swaps, palette presets, identity, stair material, shell margin, exterior air margin, and preview margin changes.
2. Add metadata-only update support for signs, structure block names, and connector pool ids.
3. Add generic block swap with compatible block-state preservation.
4. Add generated stair material rewrite or reuse existing stair regeneration for selected pieces.
5. Write a backup manifest before every mutation.
6. Add backup discovery and restore selection.
7. Add shell margin expansion only.
8. Keep all shrink/topology/family-count changes as explicit destructive-regenerate changes.

This gives immediate value while keeping the high-risk topology surgery out of the first pass.

## Implementation Progress

Implemented:

- Backup manifest path support under `mk_workspace_exports/backups/<structure_name>/`.
- Backup manifest writer that serializes the same schema as normal workspace exports before a mutation.
- Backup manifest discovery for the nearest live workspace.
- Development command: `/mkworkspace backups list` shows recent backup manifests.
- Backup restore service for same-anchor live metadata/layout restore.
- Development commands: `/mkworkspace backups restorelatest` and `/mkworkspace backups restore <fileName>`.
- Generic block-state mapper that transfers compatible property values by property name and serialized value.
- Generic block swap service that scans a selected bounds, replaces configured source block ids, preserves compatible block state, preserves compatible block entity data, and reports replacement/dropped-property counts.
- Workspace mutation service that writes a backup before applying generic block swaps across all existing pieces.
- Development command: `/mkworkspace swapblock <source> <target>` applies a backed-up swap to the nearest workspace.
- Preview-margin relayout service that snapshots each piece export area plus sidecar blocks, moves exact block states and block entities, remaps all absolute piece positions, and updates live workspace metadata.
- Development command: `/mkworkspace setpreviewmargin <value>` applies the backed-up relayout to the nearest workspace.
- `CreateWorkspacePacket` now detects preview-margin-only edits and skips the automatic regenerate so the form path can preserve data for that specific safe change.
- `CreateWorkspacePacket` now detects palette-only edits, applies a backed-up palette swap through the generic block swapper, updates live workspace palette metadata, and skips automatic regeneration.
- Workspace screen now exposes a generic block swap entry point backed by `SwapWorkspaceBlockPacket`.
- Workspace screen now lists discovered backup manifests and can restore a selected backup through `RestoreWorkspaceBackupPacket`.
- Metadata-only identity rename updates live workspace identity, structure block names, signs, jigsaw names/targets, and connector pool metadata with a backup manifest.
- Shell margin and exterior air margin expansions now run through a backed-up expansion service with destination collision checks. Existing block states are mapped by interior-origin delta, new outer shell/structure-void regions are filled, and structure blocks, signs, markers, connector positions, and jigsaws are refreshed against the new bounds.
- Decreasing margins, topology changes, family-count changes, and other unsafe form edits remain destructive-regenerate changes.

## Open Questions

- None currently.
