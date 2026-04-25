# Workspace Rehydration And Load Plan

## Purpose

This plan defines how to rehydrate an in-game structure workspace from exported structure data, using `test_tower` as the first target.

The goal is to let a designer:

1. place a workspace dev block in a fresh development save
2. choose an existing exported workspace such as `mknpc:test_tower`
3. recreate the workspace capability entry and preview catalog at the new anchor
4. load the saved structure templates for every piece, including base templates and all variants, for continued editing

This builds directly on the current canonical export flow:

- structure NBT files
- workspace export manifest
- exported `mk_jigsaw_piece_meta`

This flow is authoring-focused.

It does not depend on automatic structure registration from manifests.

Runtime `Structure` and `StructureSet` registration remain manually owned by the implementing mod.

## Scope

### In Scope

- load an existing workspace export from data resources
- create a new in-world workspace capability entry from that export
- attach the new workspace to the interacting dev block anchor
- recreate preview piece placements from the current workspace layout rules
- load the saved structure templates into every preview piece:
  - base templates
  - all variants

### Out Of Scope

- importing arbitrary structure NBT that has no workspace manifest
- reconstructing a workspace from runtime template pools alone
- automatic migration from unsupported manifest schema versions
- non-workspace structure authoring flows
- scaffold-only rehydration mode

## Current Foundation

The current system already gives us the core inputs we need:

- exported manifest at:
  - `data/<namespace>/mk_workspace_exports/<structure_name>.json`
- structure templates at:
  - `data/<namespace>/structure/<structure_name>/<piece_name>.nbt`
- exported piece catalog, categories, dimensions, palette, tags, connectors, and runtime hints

For `test_tower`, the current manifest already contains:

- workspace settings
- full piece list
- base-name categories
- per-piece export bounds and metadata
- stair settings and generated stair metadata

That means rehydration should be manifest-driven, not inferred from structure NBT.

The runtime registration boundary does not affect rehydration.

Import only needs:

- the exported authoring manifest
- the referenced structure templates

It does not require the workspace to have been auto-registered as a runtime worldgen structure.

## Core Design Decision

The manifest is the canonical import source.

Rehydration should not try to reverse-engineer a workspace from NBT or from runtime worldgen assets.

The import contract should be:

- required:
  - manifest JSON
  - referenced structure NBT files present in resources
- optional:
  - exported piece metadata for validation/debugging

This keeps the import path aligned with the existing export-driven workflow.

## User Experience

### Dev Block Flow

The workspace dev block should support two entry paths:

1. create a new workspace
2. load an existing exported workspace

Recommended interaction model:

- initial screen from the dev block becomes a chooser:
  - `Create New Workspace`
  - `Load Existing Workspace`

If the block is already bound to a workspace:

- keep the current behavior of opening that workspace directly
- also expose a way to replace/unbind if needed later

### Load Existing Workspace Flow

Recommended UI steps:

1. open dev block
2. choose `Load Existing Workspace`
3. see a list of exported workspaces discovered from manifests
4. choose one entry, for example:
   - `mknpc:test_tower`
5. confirm
6. server creates workspace at this anchor, rebuilds the preview layout, and loads every saved piece template into the corresponding preview slot

## Proposed Architecture

## Phase 1: Manifest Discovery

### Goal

Allow the game to discover available exported workspaces that can be loaded.

### New Service

- `MKWorkspaceImportManifestDiscovery`

Responsibilities:

- search resource locations for `mk_workspace_exports`
- list available manifests by:
  - namespace
  - structure name
  - family type
- optionally expose a lightweight summary DTO for the UI

### Suggested DTO

```java
public record MKWorkspaceImportCandidate(
        ResourceLocation workspaceId,
        String familyType,
        int pieceCount,
        int categoryCount
) {}
```

### Notes

Use the existing codec-backed manifest model:

- `MKWorkspaceExportManifest`

Do not create a second JSON parser for import.

## Phase 2: Import Service

### Goal

Build a workspace capability entry from a manifest at a new anchor.

### New Service

- `MKStructureWorkspaceImportService`

### Responsibilities

1. load and decode the manifest
2. validate referenced structure files exist
3. create a new `MKStructureWorkspace`
4. recreate `MKWorkspacePieceDefinition` entries
5. assign fresh preview world placements from the current grid layout rules
6. generate the preview scaffold/catalog
7. load the saved structures into the preview area for every piece listed in the manifest
8. persist the workspace in the capability
9. bind the workspace id to the dev block entity

### Recommended API

```java
public record MKWorkspaceImportResult(UUID workspaceId, int pieceCount) {}

public Optional<MKWorkspaceImportResult> importWorkspaceAtAnchor(ServerLevel level,
                                                                 BlockPos anchor,
                                                                 ResourceLocation manifestId)
```

## Phase 3: Reconstruct Workspace Model

### Goal

Map export DTOs back into the live workspace model.

### Mapping Rules

#### Workspace

Map manifest fields back into:

- `MKStructureWorkspace`
- `MKWorkspaceDimensions`
- `MKWorkspaceMaterialPalette`
- `MKWorkspaceStairAuthoringConfig`

The imported workspace gets:

- a new runtime workspace UUID
- the new anchor position
- fresh timestamps

It should retain:

- namespace
- structure name
- family type
- dimensions
- palette
- stair settings
- shell margin
- preview margin
- family-specific tags/settings

#### Pieces

For each exported piece:

Map back into `MKWorkspacePieceDefinition`:

- piece name
- role
- variant index
- tags
- effective dimensions
- shell margin
- connector definitions
- generated stair positions if present
- generated stair metadata if present

Do not reuse exported absolute world positions.

Those should be recomputed for the new anchor.

## Phase 4: Preview Layout Rebuild

### Goal

Lay out the imported workspace as a fresh local preview catalog.

### Rules

Use the current workspace layout pipeline:

- `MKWorkspaceGridLayout`
- `MKWorkspaceScaffoldBuilder`

That means:

- imported workspaces should appear exactly like freshly generated workspaces
- preview cell spacing remains driven by the current layout rules
- imports are portable across saves because they do not depend on old absolute positions

### Important Constraint

The import path should not trust exported `worldOrigin`, `signPos`, `markerPositions`, or `structureBlockPos` as placement inputs.

Those are useful for validation and diagnostics only.

## Phase 5: Load Saved Templates Into Preview Pieces

### Goal

After reconstructing the workspace layout, populate the preview pieces with the saved authored structures.

### Recommended Strategy

Two-step flow:

1. generate the preview scaffold and structure blocks normally
2. for each piece, load the corresponding structure template into the exact export bounds of that preview slot

This is better than trying to place templates without scaffold generation because:

- structure block setup remains correct
- signs and markers still get generated
- metadata and preview layout stay consistent

This is the only supported rehydration mode.

On import, the system should always:

- generate the scaffold/catalog
- overlay the saved structure into each preview export area

That applies to:

- base template pieces
- all saved variants

## Phase 6: Dev Block And UI Integration

### Goal

Expose the import flow from the workspace dev block.

### UI Changes

Add a new top-level entry path in the workspace screen flow:

- `load_existing_workspace`

Recommended states:

- `workspace_home`
  - `Create New Workspace`
  - `Load Existing Workspace`
- `workspace_import_list`
  - list discovered manifest entries
- `workspace_import_confirm`
  - selected workspace

### Networking

Add packets:

- `OpenWorkspaceImportPickerPacket`
  - server to client
  - sends available manifest candidates
- `LoadWorkspaceFromManifestPacket`
  - client to server
  - includes:
    - anchor
    - manifest resource id

## Phase 7: Validation

### Manifest Validation

Validate:

- schema version supported
- namespace and structure name present
- family type supported
- piece catalog non-empty
- category and piece references consistent

### Asset Validation

Validate:

- every referenced `structure_id` can be resolved to a structure template
- piece names are unique
- required room categories for the family exist

### World Validation

Validate:

- no workspace already bound to that anchor unless replace is explicitly allowed
- placement area is safe to use

## Phase 8: `test_tower` First Vertical Slice

### Goal

Use `test_tower` as the first full implementation target.

### Expected First Demo

In a fresh development save:

1. place workspace dev block
2. choose `Load Existing Workspace`
3. select `mknpc:test_tower`
4. confirm
5. receive the full editable `test_tower` preview catalog at the new anchor, with all template and variant pieces loaded from saved structure data

That verifies:

- manifest discovery
- import mapping
- preview layout regeneration
- saved template overlay
- dev block binding

## Suggested Implementation Order

1. add manifest discovery service
2. add import DTO/summary model for the UI
3. add `MKStructureWorkspaceImportService`
4. implement saved-template overlay path
5. add dev block/UI entry point
6. add packets
7. validate with `test_tower`
8. add diagnostics and failure messaging

## Main Files Likely To Change

- `MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/MKStructureWorkspaceService.java`
- `MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/export/MKWorkspaceExportManifest.java`
- `MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/scaffold/MKWorkspaceScaffoldBuilder.java`
- `MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKStructureWorkspace.java`
- `MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/model/MKWorkspacePieceDefinition.java`
- `MKNpc/src/main/java/com/chaosbuffalo/mknpc/client/gui/screens/MKWorkspaceScreen.java`
- dev block / dev block entity classes
- packet registration and packet handlers

New likely classes:

- `MKWorkspaceImportManifestDiscovery`
- `MKStructureWorkspaceImportService`
- `LoadWorkspaceFromManifestPacket`

## Acceptance Criteria

- The workspace dev block offers a clear path to load an existing exported workspace.
- `mknpc:test_tower` can be rehydrated into a fresh development save from exported data.
- Imported workspaces use the current preview grid and structure block setup.
- All authored room contents are restored for base templates and all variants.
- Imported workspaces are editable and can be re-exported through the normal workspace flow.
