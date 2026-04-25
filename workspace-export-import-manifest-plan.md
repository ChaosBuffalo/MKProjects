# Workspace Export/Import Manifest Plan

## Purpose

This plan extends the current workspace authoring flow so that `Export All Structure Pieces` produces not only structure NBT files, but also a machine-readable manifest describing the authored structure set, topology, variants, and workspace metadata.

The immediate goal is:

- export enough data to construct runtime structure registration inputs during data generation

The longer-term goal is:

- allow a designer to take the exported structure NBT files plus the manifest and reconstruct a structure workspace in a different development save for continued iteration

## Current State

The current tower workspace flow now saves:

- structure NBT through configured structure blocks
- a workspace export manifest
- exported `mk_jigsaw_piece_meta`

Relevant code:

- [MKWorkspaceScreen.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/client/gui/screens/MKWorkspaceScreen.java:353)
- [ExportWorkspacePiecesPacket.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/network/packets/ExportWorkspacePiecesPacket.java:43)
- [MKStructureWorkspaceService.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/MKStructureWorkspaceService.java:168)

The remaining problem is not lack of export data, but keeping downstream runtime/datagen usage aligned with the exported authoring data.

Without a clear boundary we can still drift in places like:

- reconstruct topology intent from a set of saved NBTs
- distinguish base roles from instance variants cleanly
- derive runtime pool and metadata JSON automatically
- recreate a workspace layout in another save

## Design Principles

### Keep NBT and Authoring Metadata Separate

Do not try to encode the full authoring model into structure NBT.

Instead, export:

- structure NBT for the piece geometry
- one sidecar JSON manifest for workspace-level metadata

This keeps runtime templates clean and makes the authoring metadata versionable and migratable.

### Export a Canonical, Stable Manifest

The manifest should describe:

- workspace identity
- dimensions and palette
- canonical topology roles
- piece list and variants
- connector and pool intent
- export metadata needed for regeneration/import

It should not depend on transient world positions except where explicitly needed for workspace rehydration.

### Version the Schema

The manifest must include a schema version from day one. We will evolve this format.

## Proposed Artifact

On `Export All Structure Pieces`, write:

- all structure NBT files as today
- one JSON manifest at a predictable path, for example:
  - `data/<namespace>/mk_workspace_exports/<structure_name>.json`

Alternative:

- write under a dedicated authoring export directory outside normal runtime resources
  - `workspace_exports/<namespace>/<structure_name>.json`

Recommended V1 choice:

- write into the mod resources tree under `data/<namespace>/mk_workspace_exports/`

Reason:

- data generation can read it easily
- it lives next to authored content
- it can be committed with the structure files

## Manifest Scope

The manifest should include enough information for two consumers:

1. data generation importer
2. workspace rehydration importer

### Workspace-Level Fields

- `schema_version`
- `namespace`
- `structure_name`
- `family_type`
- `workspace_id` optional, for traceability only
- `exported_at`
- `generator_version` optional

### Dimension and Palette Fields

- full `MKWorkspaceDimensions`
- full `MKWorkspaceMaterialPalette`
- stair authoring defaults if present
- shell margin / preview margin / exterior air margin
- family-specific settings such as tower stair placement

These are required for future workspace reconstruction.

### Piece Catalog

For each piece:

- `piece_name`
- `base_name`
- `variant_index`
- `workspace_piece_kind`
  - `template`
  - `instance`
- `role`
- `tags`
- exported structure id
  - `<namespace>:<structure_name>/<piece_name>`
- dimensions
  - interior size
  - export size
- bounds metadata
  - export bounds size only, not absolute world position for V1 import
- stair generation metadata if present
- connector list

### Connector Fields

For each connector:

- `role`
- `facing`
- `relative_pos`
- `opening_width`
- `opening_height`
- `jigsaw_name`
- `jigsaw_target`
- `target_pool`

This captures the intended jigsaw contract without re-reading NBT.

### Topology/Generation Fields

The manifest should also contain a higher-level view for data gen:

- canonical family topology
- list of base categories
- per-category available variants
- optional parent/child expectations inferred from connector pools
- optional metadata role classification for MK jigsaw generation

For `tower`, this would include enough to derive:

- start pool
- per-base-name template pools
- piece metadata JSON
- optional structure set hints later

## Proposed Java Model

Add dedicated export DTOs instead of reusing the runtime workspace model directly.

Suggested classes:

- `MKWorkspaceExportManifest`
- `MKWorkspaceExportPiece`
- `MKWorkspaceExportConnector`
- `MKWorkspaceExportRuntimeHints`

Reasons:

- export schema can evolve independently
- avoids leaking transient in-world fields that are not suitable for file export
- allows explicit backward-compatible serialization rules

## Phase Plan

## Phase 1: Manifest Export Foundation

### Task 1. Add export manifest model

Create JSON-serializable records/classes for the manifest.

Definition of done:

- manifest can be created from `MKStructureWorkspace`
- schema version is embedded
- JSON serialization is deterministic

### Task 2. Add export path abstraction

Create a small service that resolves where exported manifests go.

Suggested class:

- `MKWorkspaceExportPathResolver`

Responsibilities:

- map namespace/structure name to output path
- keep path logic out of packet/service code
- support future alternate export targets

Definition of done:

- one call returns a stable output path for the manifest

### Task 3. Extend workspace export service

Update [MKStructureWorkspaceService.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/MKStructureWorkspaceService.java:168) so `exportWorkspacePieces(...)` becomes a richer export operation.

Recommended API change:

```java
public record MKWorkspaceExportResult(int savedPieceCount, Path manifestPath) {}

public Optional<MKWorkspaceExportResult> exportWorkspaceArtifacts(ServerLevel level, BlockPos anchor)
```

Flow:

1. load workspace by anchor
2. save all structure NBTs as today
3. build manifest from workspace capability data
4. write manifest JSON
5. return both piece count and manifest path

Definition of done:

- `Export All` saves pieces and writes manifest
- player receives a message confirming both

### Task 4. Update packet/UI messaging

Update:

- `ExportWorkspacePiecesPacket`
- workspace UI success message

Add a clearer message like:

- `Exported 10 pieces and wrote workspace manifest mknpc:test_tower`

Definition of done:

- export feedback reflects both artifacts

## Phase 2: Make the Manifest Useful for Data Gen

### Task 5. Define runtime import hints in the manifest

Add explicit fields that remove guesswork for data generation:

- start piece base name
- pool grouping base name for each piece
- piece role mapping
- progression metadata
- vertical delta metadata
- boss/terminal hints

Do not force data gen to infer everything from tags if we already know it in the workspace model.

Definition of done:

- tower exports contain enough information to generate pools and MK piece metadata without hand-written JSON

### Task 6. Add a datagen-side importer

Create a loader used during `runData` that reads exported manifests and generates:

- template pools
- `mk_jigsaw_piece_meta`
- tags or helper registries if appropriate

Suggested classes:

- `MKWorkspaceExportManifestLoader`
- `MKWorkspaceExportDatagenAdapter`

Definition of done:

- `test_tower` can drive template pools and MK piece metadata from manifest import instead of manually duplicated pool data

### Task 7. Decide generated-vs-handwritten boundary

This is the key architectural decision.

Options:

1. manifest generates only piece metadata and pool JSON
2. manifest also generates structure and structure set JSON
3. manifest generates helper Java source inputs

Recommended V1:

- generate pool JSON and MK piece metadata
- keep high-level structure/structure set registration explicit for now

Reason:

- avoids over-automating world placement policy too early
- still removes the most repetitive and error-prone manual work

Current implementation note:

- exported-workspace pool bootstrap can be shared across mods
- each mod should still filter to its own namespace
- each mod should still manually register its `Structure` and `StructureSet`
- `MKNpc` uses this model for `test_tower`

## Phase 3: Support Workspace Rehydration in Another Save

### Task 8. Define import contract

The import feature should accept:

- manifest JSON
- referenced structure NBT files already present in the dev resources path

Input assumptions:

- same mod codebase/version family
- manifest schema version is supported

Definition of done:

- we know exactly what files are required and what validation to run before import

### Task 9. Add workspace import service

Create a new service that reconstructs a workspace capability entry from the manifest.

Suggested class:

- `MKStructureWorkspaceImportService`

Responsibilities:

- validate manifest
- create `MKStructureWorkspace`
- recreate `MKWorkspacePieceDefinition` entries
- assign preview layout positions from the current workspace grid rules
- place scaffold/template previews in the target world
- preload structure blocks to the referenced structure ids

Important:

- import should not require original absolute world positions
- it should derive a fresh layout from the current anchor and workspace dimensions

Definition of done:

- importing into a new save recreates an editable workspace catalog from the manifest and existing structure files

### Task 10. Distinguish rehydrated pieces from fresh scaffold

When importing, designers may want:

1. empty scaffold regeneration
2. full template placement from saved NBT

Support both modes:

- `Scaffold Only`
- `Load Saved Templates`

Recommended default:

- `Load Saved Templates`

Definition of done:

- import flow supports continued authoring, not just metadata recreation

## Phase 4: Hardening and Migration

### Task 11. Add manifest validation and diagnostics

Validate:

- duplicate piece names
- missing base piece mappings
- missing structure files
- invalid pool names
- unsupported schema versions
- inconsistent connector contracts

Definition of done:

- import/export failures report actionable reasons

### Task 12. Add schema migration hooks

Create a small migration layer so older manifests can be upgraded.

Suggested approach:

- `schema_version`
- migrators from `v1 -> v2`, etc.

Definition of done:

- format can evolve without discarding authored work

## Suggested Manifest Shape

This is illustrative, not final:

```json
{
  "schema_version": 1,
  "namespace": "mknpc",
  "structure_name": "test_tower",
  "family_type": "tower",
  "dimensions": {},
  "palette": {},
  "runtime_hints": {
    "start_base_name": "entry",
    "categories": [
      {
        "base_name": "entry",
        "role": "entry",
        "variants": ["entry_1"]
      }
    ]
  },
  "pieces": [
    {
      "piece_name": "entry_1",
      "base_name": "entry",
      "variant_index": 1,
      "role": "entry",
      "workspace_piece_kind": "instance",
      "structure_id": "mknpc:test_tower/entry_1",
      "interior_size": { "x": 9, "y": 5, "z": 9 },
      "export_size": { "x": 15, "y": 7, "z": 15 },
      "tags": {},
      "connectors": [
        {
          "role": "connect_up",
          "facing": "up",
          "relative_pos": { "x": 7, "y": 6, "z": 7 },
          "jigsaw_name": "mknpc:connect_up",
          "jigsaw_target": "mknpc:connect_down",
          "target_pool": "mknpc:test_tower/connect_up"
        }
      ]
    }
  ]
}
```

## Recommended Implementation Order

1. Add manifest DTOs and writer.
2. Extend export service to write the manifest beside NBT exports.
3. Surface manifest path/result in the export packet/UI.
4. Add runtime-hint fields for tower and verify `test_tower` export.
5. Build datagen importer for pools and piece metadata.
6. Add workspace import/rehydration flow for a new save.
7. Add schema validation and migrations.

## Main Risks

### Risk 1. Leaking transient world positions into a portable format

Mitigation:

- export structural metadata, not absolute preview placement
- derive layout fresh on import

### Risk 2. Mixing runtime generation concerns with authoring concerns

Mitigation:

- keep manifest authoring-first
- add `runtime_hints` as a contained section rather than making the entire schema runtime-specific

### Risk 3. Manual runtime registrations drifting from authored exports

Mitigation:

- make manifest the source for generated pools and piece metadata during datagen
- keep the explicit manual/runtime boundary documented so structure placement policy remains intentional

### Risk 4. Future family types needing different metadata

Mitigation:

- keep shared core fields stable
- allow family-specific sections, for example `tower_settings`

## First Concrete Milestone

The first useful milestone is:

- clicking `Export All Structure Pieces` writes a manifest JSON for the workspace next to the authored assets
- the manifest contains enough information to regenerate `mk_jigsaw_piece_meta` and template pool JSON for `tower`

That milestone gives immediate value without waiting for full import/rehydration support.
