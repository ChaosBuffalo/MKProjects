# Workspace Planner Preview and Locking Plan

## Goal

Make planner previews the primary workspace overview, and make structural regeneration explicit, scoped, and safe.

The workspace UI should show the user the generated structure model first: floor plans, room graphs, hallway routes, keep/courtyard layouts, tower stacks, wall runs, slot assignments, and template placement state. Topology settings should become inspector controls for the selected planner layer instead of the place where the preview is hidden.

Changing settings should not imply a full workspace rebuild by default. Each setting should declare which generated layer it invalidates, and the UI should show whether applying the change can preserve designer-authored templates and in-world edits.

## Implementation Status

The first implementation pass has landed the eight initial work slices:

- planner id value type and planner id persistence on planned pieces, workspace pieces, tags, and export manifests,
- generated layer state, dirty/refresh tracking, and per-layer lock/unlock operations,
- floor topology invalidation reports and serialized mutation preflight reports,
- main workspace planner overview, layer status, lock controls, and impact report display,
- locked-layer edit gating for workspace updates,
- hallway-only regeneration for `regenerate_hallway_routing` preflight operations,
- real template-bounds block diff snapshots for authored-template detection, with managed sidecar positions ignored,
- focused workflow tests for planner ids, serialized reports, layer locking, hallway regeneration planning, and template block diffs.

The remaining design sections are still useful as the broader direction. The next implementation work should deepen preview/runtime parity, remapping of orphaned templates, and additional scoped regeneration operations beyond floor-plan hallways.

## Current Problem

Planner previews are currently treated as topology settings helpers. That hides the most useful overview of the workspace structure behind configuration pages, and it encourages a destructive mental model:

- the user creates a workspace,
- the scaffold is generated,
- the user starts authoring templates,
- later structural adjustments feel risky because the next generate operation may overwrite or discard authored work.

This is especially visible for floor plans. Settings such as hallway path distance, link routing, branch constraints, or preview display modes are planner-level concerns. Some of them affect only hallway pieces or preview rendering, while others affect room identity and template layout. The UI currently does not make that distinction clear.

The desired model is closer to an editor with generated layers:

- stable authored template content is protected by default,
- generated scaffold layers can be regenerated independently,
- topology-changing edits are possible but clearly marked,
- the user can preview the impact before committing.

## Design Principles

- Planner previews are workspace navigation, not secondary diagnostics.
- Locked workspaces protect authored content by default.
- Unlocking structural controls is an explicit action.
- Regeneration operates on named generated layers instead of the whole workspace whenever possible.
- Each setting declares its invalidation scope.
- The UI reports what will change before applying a mutation.
- Persistent ids are required for rooms, connectors, hallway segments, slots, tower stacks, wall segments, and authored template bindings.
- Preview and runtime should consume the same resolved planner model where possible.

## Workspace Modes

Lock state should be stored per planner layer, not only per workspace.

The practical reason is that authoring happens unevenly. A user may be done with room envelopes and template placement, still experimenting with hallway routing, and not yet ready to finalize export metadata. A single workspace-wide lock would force them to choose between too much safety and too little flexibility.

Per-layer lock state lets the workspace communicate that previous work areas are safe while still allowing active generation layers to evolve.

Example:

| Layer | State |
| --- | --- |
| `ROOM_ENVELOPES` | locked |
| `TEMPLATE_BINDINGS` | locked |
| `HALLWAY_ROUTING` | unlocked |
| `HALLWAY_PIECES` | unlocked |
| `PREVIEW_LAYOUT` | unlocked |
| `RUNTIME_METADATA` | dirty |

The workspace can still expose a high-level "locked" status when all structural layers are locked, but mutation checks should use the layer-level state.

### Draft Mode

Draft mode is for initial generation before meaningful authoring has started.

Allowed behavior:

- topology settings are editable,
- full regeneration is available,
- destructive impact warnings can be lighter,
- planner previews update aggressively,
- authored template preservation is best-effort.

Draft mode is appropriate for early experimentation.

### Locked Mode

Locked mode is the default after the user begins authoring templates or explicitly locks the workspace.

Allowed behavior:

- planner previews remain fully visible,
- non-structural settings remain editable,
- safe metadata and display updates apply immediately,
- scoped regeneration is available when the invalidated layer is narrow,
- destructive topology edits require unlocking or a scoped preflight.

Locked mode should make it hard to accidentally destroy authored rooms, variants, and template edits.

### Unlocked Structural Edit Mode

Unlocked structural edit mode is a temporary state for topology changes.

The UI should show:

- which settings are being changed,
- which generated layers will be invalidated,
- which pieces can be preserved,
- which authored templates will become orphaned or need remapping,
- whether the operation can be applied as a patch or requires full regeneration.

The user should be able to cancel and return to the locked workspace without changing generated content.

## Primary Workspace UI

The main workspace page should be centered on planner previews.

Suggested layout:

- left or top planner navigation for available preview layers,
- central preview canvas,
- right inspector for selected planner layer, selected segment, or selected template binding,
- footer/status area for lock state, pending changes, and regeneration impact.

Planner navigation should be driven by the active planner descriptor. Examples:

- Overview
- Floor Plan
- Room Graph
- Hallway Routing
- Link Routes
- Template Placement
- Tower Stack
- Keep Perimeter
- Courtyard Slots
- Export Layout

The topology defaults page can remain as a detailed configuration surface during migration, but it should no longer be the only place where users see the planner overview.

Long term, the planner page should replace topology defaults as the primary place where these controls live. The migration path should be:

1. move previews into the main planner page,
2. expose selected-layer controls in the planner inspector,
3. keep topology defaults as a compatibility/detail page,
4. remove or collapse topology defaults once the planner page exposes all required controls.

## Preview Layer Model

Introduce a common preview layer concept for workspace planners.

Possible interface shape:

```java
public interface WorkspacePlannerPreviewLayer {
    ResourceLocation id();

    Component displayName();

    WorkspacePreviewLayerKind kind();

    List<WorkspacePlannerSettingRef> controlledSettings();

    WorkspacePreviewSnapshot resolvePreview(WorkspaceDraftSession session);
}
```

Layer kind examples:

- `OVERVIEW`
- `TOPOLOGY`
- `ROUTING`
- `PLACEMENT`
- `EXPORT`
- `VALIDATION`

The layer should not own generation logic. It should render or summarize a resolved planner model that generation can also consume.

## Generated Layer Dependency Model

The workspace should treat generated output as a dependency graph.

Candidate layers:

| Layer | Description |
| --- | --- |
| `WORKSPACE_IDENTITY` | workspace id, planner id, root naming, family names |
| `PLANNER_TOPOLOGY` | high-level graph: rooms, slots, towers, walls, courtyard regions |
| `ROOM_ENVELOPES` | room/cap footprints, bounds, masks, floor dimensions |
| `CONNECTOR_GRAPH` | exits, doors, links, jigsaw connector identities |
| `HALLWAY_ROUTING` | floor hallway route selection and logical routes |
| `HALLWAY_PIECES` | scaffold/export pieces for generated hallway runs |
| `TEMPLATE_BINDINGS` | mapping from stable planner ids to template definitions |
| `SCAFFOLD_BLOCKS` | generated shell/floor/ceiling/opening blocks |
| `SIDECAR_BLOCKS` | signs, markers, jigsaws, structure blocks |
| `PREVIEW_LAYOUT` | visual workspace placement and preview spacing |
| `RUNTIME_METADATA` | exported manifests, piece metadata, template pool metadata |

Each setting change produces an invalidation report:

```java
public record WorkspaceInvalidationReport(
        List<WorkspaceGeneratedLayer> invalidatedLayers,
        List<WorkspacePieceRef> affectedPieces,
        List<WorkspaceTemplateBindingRef> preservedBindings,
        List<WorkspaceTemplateBindingRef> orphanedBindings,
        WorkspaceMutationSafety safety,
        Component summary
) {
}
```

The full report should be serializable for client display. The client should be able to render the same report that the server validates, including affected layers, affected pieces, preserved bindings, orphaned bindings, safety level, warnings, and recommended operation.

The server remains authoritative. The client may compute a preview report for responsiveness, but apply operations should use a server-generated or server-confirmed report.

Safety levels can reuse the existing non-destructive settings plan:

- safe metadata update,
- safe block substitution,
- safe expansion,
- safe relayout,
- conditionally safe topology patch,
- destructive regenerate.

## Setting Invalidation Examples

| Setting | Expected Invalidation |
| --- | --- |
| Preview colors, labels, zoom, selected layer | UI only |
| Preview margin or visual grid spacing | `PREVIEW_LAYOUT` |
| Palette material swap | `SCAFFOLD_BLOCKS` for selected generated blocks |
| Connector pool/name metadata | `CONNECTOR_GRAPH`, `SIDECAR_BLOCKS`, `RUNTIME_METADATA` |
| Hallway path distance | `HALLWAY_ROUTING`, `HALLWAY_PIECES`, affected connectors if endpoints move |
| Hallway material only | selected hallway `SCAFFOLD_BLOCKS` |
| Door/opening width | `CONNECTOR_GRAPH`, local room entrance patches, hallway endpoints |
| Room count | `PLANNER_TOPOLOGY`, `ROOM_ENVELOPES`, `CONNECTOR_GRAPH`, `HALLWAY_ROUTING`, `TEMPLATE_BINDINGS` remap |
| Floor dimensions | `ROOM_ENVELOPES`, `CONNECTOR_GRAPH`, `HALLWAY_ROUTING`, `HALLWAY_PIECES`, affected room scaffold |
| Tower stack floor count | tower topology, stack piece bindings, vertical connectors |
| Courtyard path options | courtyard routing/path pieces only when content slots remain stable |
| Keep wall length | perimeter topology, wall pieces, corner/gate adjacency |

The important rule is that each setting must describe its blast radius before it can be edited in locked mode.

## Persistent Identity Requirements

Scoped regeneration depends on stable ids.

Required ids:

- workspace id,
- planner id,
- generated layer id,
- floor id,
- room segment id,
- hallway segment id,
- connector id,
- link route id,
- tower stack id,
- tower floor id,
- wall segment id,
- courtyard slot id,
- template binding id.

Ids should be stable across edits when the conceptual object still exists. For example, changing hallway path distance should preserve room ids and connector ids where endpoints are still semantically the same. Regenerating the hallway route can replace hallway segment ids if the route topology changes, but the invalidation report should make that explicit.

Template definitions should bind to stable planner ids rather than only to generated names. Generated names can remain derived output, but they should not be the only identity used for preservation.

### Stable Id Strategy

Stable ids should be derived from the planner hierarchy.

Each planner already owns a conceptual part of the generated structure: keep, courtyard, tower stack, floor plan, room graph, hallway routing, perimeter wall, slot layout, and so on. That ownership tree is the most natural source of stable names. A child planner should receive its parent planner path, then append its own semantic ids for the things it owns.

The stable naming contract should be:

- parent planners allocate stable child planner paths,
- child planners allocate stable ids for their own generated objects,
- generated export names are derived from stable planner ids,
- settings changes preserve ids when the same conceptual object still exists,
- removed conceptual objects produce orphaned bindings instead of silent deletion.

Stable ids are still worth treating carefully because planner output is often positional, generated, and dependent on earlier random or constraint decisions.

Problem cases:

- Index-based ids shift when an earlier room, wall segment, or slot is inserted or removed.
- Coordinate-based ids shift when a parent footprint moves even though the conceptual room or slot is the same.
- Name-based ids shift when generated names include role, mask, direction, or sequence data that can change after a setting edit.
- Random-order ids shift when the solver accepts branches or links in a different order after a small parameter change.
- Composite planners need ids that are stable both inside the child planner and from the parent planner path that owns the child.

The recommended shape is dot-delimited hierarchical semantic ids directly from the planner tree.

Examples:

```text
keep.main.courtyard.slot.north_east
keep.main.perimeter.wall.east.segment.03
keep.main.tower.corner.north_west.floor.main_02
keep.main.tower.center.floor.basement_01.floor_plan.room.main_00
keep.main.tower.center.floor.basement_01.floor_plan.hallway.main_to_branch_02
```

Dot notation is enough detail if each segment is assigned by the planner that owns that level of the hierarchy. The id does not need to encode every physical detail. It needs to encode enough semantic ownership to preserve identity across ordinary edits and to derive export/template names deterministically.

Java code should use a small value type rather than passing planner ids as raw strings everywhere.

Tentative shape:

```java
public record WorkspacePlannerId(String value) {
    public static WorkspacePlannerId of(String value) {
        // validate and normalize
    }

    public WorkspacePlannerId child(String segment) {
        // validate segment and append with "."
    }

    public WorkspacePlannerId parent() {
        // return parent path
    }

    public boolean startsWith(WorkspacePlannerId parent) {
        // hierarchy check
    }

    public String toExportName() {
        // convert to generated template/export-safe name
    }
}
```

The serialized form should remain the plain dot string. The value type is for Java-side validation, safe composition, hierarchy operations, and consistent conversion to export paths, template names, resource ids, labels, and packet fields.

These ids should be assigned from planner concepts, not from final export filenames. When a layer is regenerated, the planner should reuse ids by walking the same planner hierarchy and matching stable semantic anchors:

- planner path,
- role,
- floor or vertical level,
- side/corner/slot direction,
- parent id,
- connector role,
- compatible dimensions.

When a conceptual object cannot be matched, create a new id and mark old bindings as orphaned instead of deleting them.

This does not mean every id must survive every topology edit. It means the system must be explicit about which ids survived, which ids were replaced, and which authored bindings no longer have a target.

## Authored Template Detection

Authored template detection is needed to decide whether a mutation is allowed to overwrite or regenerate a piece without risking user work.

The comparison is against what the scaffold generator would have produced for that template bounds under the current or previous layer settings. A piece can be considered "authored" when its export/template contents contain user changes beyond generated scaffold.

This is feasible because the workspace generator is deterministic enough to produce an expected block state for scaffold-owned regions. For an authoring check, the server can regenerate the expected scaffold into an in-memory representation for the same bounds, then compare it against the actual template/export bounds.

Useful signals:

- block diffs show non-scaffold blocks or changed scaffold blocks inside export bounds,
- the exported `.nbt` differs from the generated expected scaffold,
- a template binding has variants or designer-assigned metadata,
- workspace metadata records an explicit authoring action,
- the structure block has been saved after scaffold generation.

This matters because not every generated piece needs the same protection.

For example, hallway pieces that are still pure generated scaffold can be regenerated in-place without much ceremony. A room template that has been decorated by a designer should be preserved, remapped, or orphaned rather than silently regenerated.

The first version should use block comparison as the main authored-content check where the generator can produce an expected scaffold for the same bounds.

Comparison rules:

- compare only blocks inside the relevant template/export bounds,
- treat managed sidecar blocks as generated-owned,
- freely rewrite structure blocks, signs, jigsaws, markers, and other managed blocks during metadata or regeneration operations,
- compare block ids and relevant block-state properties,
- compare block entity data only for block entity types that matter to authored content,
- report changed positions and counts, but summarize them for the client impact report.

There are still edge cases. A designer may intentionally keep the same block palette as the generated scaffold, which means a pure block diff might not prove intent. For safety, metadata can supplement the diff:

- mark templates as authored after explicit save/export actions,
- store a content hash for expected generated scaffold and last saved content,
- treat unknown or changed templates as authored when the diff cannot be computed,
- allow force-regeneration only through an explicit impact report.

This gives the UI an honest report: "this hallway is unchanged generated scaffold" versus "this room differs from generated scaffold in 142 positions."

## Mutation Flow

The locked workspace edit flow should be:

1. User changes a setting in the inspector.
2. Client records a pending draft value.
3. Planner computes an invalidation report.
4. UI displays affected layers and pieces.
5. User applies the change.
6. Server validates the same report inputs.
7. Server applies the narrowest supported mutation.
8. Workspace metadata records the new settings and updated layer versions.

If client and server reports disagree, the server report is authoritative and the UI should require confirmation before applying any destructive operation.

## Layer Versioning

Store layer versions in workspace metadata.

Example:

```java
public record WorkspaceGeneratedLayerState(
        WorkspaceGeneratedLayer layer,
        int version,
        long sourceSettingsHash,
        Instant updatedAt,
        boolean dirty
) {
}
```

This gives the UI a clear status model:

- clean,
- dirty preview only,
- dirty needs patch,
- dirty requires regenerate,
- invalid due to missing template binding.

Layer versions also help export and runtime validation report whether data was generated from the current settings.

## Planner Preview as Canonical Model

For floor plans, align this work with the resolved floor plan direction:

- preview renders the same resolved model runtime generation uses,
- hallway routes and accepted links come from solver output,
- closure and connector state are visible in the preview,
- generated piece descriptors include footprint and connector data.

For composite planners, previews should be hierarchical:

- walled keep overview contains perimeter, gate, courtyard, and tower-stack child layers,
- tower-stack child previews can be opened directly,
- floor-plan child previews can be opened from a tower floor or keep slot,
- selecting a preview element opens the relevant inspector.

This keeps `WorkspaceTopologyDefaultsPage` from becoming the owner of every planner-specific visual.

## UI Impact Report

When a locked setting change is pending, show a short impact report.

Example for hallway path distance:

```text
This change affects hallway routing.

Will regenerate:
- 4 hallway route segments
- 4 hallway scaffold pieces
- 8 hallway connector sidecar blocks

Will preserve:
- 6 room templates
- 6 room scaffold envelopes
- all authored room content
```

Example for room count:

```text
This change affects floor topology.

Will regenerate:
- floor room graph
- room envelopes
- hallway routing
- hallway scaffold pieces

Needs remapping:
- 3 authored room templates may no longer have matching room ids
```

The UI should avoid vague "regenerate workspace" warnings when it can report concrete affected layers.

## Server Operations

Introduce explicit workspace mutation operations instead of routing every meaningful setting change through create/regenerate.

Candidate operations:

- `UpdateWorkspacePreviewSettings`
- `UpdateWorkspaceMetadata`
- `PatchWorkspaceConnectorMetadata`
- `RegenerateWorkspaceLayer`
- `RegenerateWorkspaceHallwayRouting`
- `RegenerateWorkspaceHallwayPieces`
- `RelayoutWorkspacePreview`
- `ApplyWorkspacePaletteSwap`
- `UnlockWorkspaceStructure`
- `LockWorkspaceStructure`

Each operation should:

- validate the current workspace generation version,
- compute or receive a server-side invalidation report,
- refuse unsafe edits in locked mode unless explicitly confirmed,
- update only the affected workspace metadata and blocks,
- record an audit/debug summary for the user and logs.

## File and Code Areas Likely Affected

Likely client areas:

- `WorkspaceDraftSession`
- `WorkspaceTopologyDefaultsPage`
- planner-specific topology panels
- preview widgets and preview render models
- workspace screen navigation
- packets for setting changes and regeneration actions

Likely common/server areas:

- `MKStructureWorkspace`
- workspace settings models
- workspace planner descriptors
- scaffold builder
- export manifest and metadata generation
- floor layout solver integration
- template binding and generated piece definitions

The first implementation should avoid a broad rewrite. Start by adding the invalidation vocabulary and a single high-value scoped regeneration path.

## Implementation Plan

### Phase 1: Define Lock State and Invalidation Vocabulary

- Add per-planner-layer structural lock state.
- Add generated layer enum or registry.
- Add mutation safety enum.
- Add invalidation report data model.
- Add planner setting metadata that can produce invalidated layers.
- Show read-only impact reports in the UI for selected settings.

This phase can be mostly additive and does not need to implement scoped regeneration yet.

### Phase 2: Promote Planner Preview to Main Workspace Surface

- Add a planner preview workspace page or tab as the default workspace landing view.
- Move existing topology previews into named preview layers.
- Keep topology defaults available as an inspector/detail panel.
- Add selection from preview element to inspector section where practical.
- Preserve existing topology page behavior during migration.

### Phase 3: Add Locked Editing Flow

- Lock authored planner layers after generation or after first export/template authoring action.
- Make destructive topology controls read-only in locked mode.
- Add an unlock action that clearly enters structural edit mode.
- Track pending draft changes separately from applied workspace settings.
- Require impact confirmation for conditionally safe or destructive changes.

### Phase 4: Implement First Scoped Regeneration Path

Use floor hallway routing as the first proof of the model.

Target behavior:

- changing hallway path distance invalidates hallway routing and hallway pieces,
- room ids and room template bindings are preserved,
- affected hallway scaffold/export regions are regenerated in-place,
- connector sidecar blocks are updated if needed,
- preview and runtime metadata are refreshed.

This is the best initial case because it has obvious user value and a narrow expected blast radius.

In-place regeneration means the hallway piece identity and template binding survive when the conceptual hallway segment survives. The operation clears and rewrites the affected hallway export bounds, updates sidecar blocks and metadata, and keeps the same planner id when the regenerated segment still represents the same route.

If the route topology changes enough that a hallway segment can no longer be matched, the old hallway binding should be orphaned and the new segment should receive a new id. That should be the exception for hallway path distance changes, not the default.

### Phase 5: Expand Patch Operations

After hallway routing works, add more mutation types:

- preview relayout,
- connector metadata updates,
- palette/material swaps,
- safe shell/exterior margin expansion,
- courtyard path regeneration,
- tower-stack child layer regeneration where floor identities remain stable.

Each operation should add tests and a clear invalidation report.

### Phase 6: Template Remapping and Orphan Handling

For topology edits that change room/slot identity:

- preserve template bindings by stable id where possible,
- offer remapping by compatible role, size, floor, mask, and connector shape,
- mark unmatched authored templates as orphaned instead of deleting them,
- provide a recovery/import path to attach orphaned templates to new planner ids.

Do not block all topology evolution just because some templates cannot be mapped automatically. Make the loss explicit and recoverable.

## Tests

Add tests for:

- setting-to-layer invalidation reports,
- locked mode refusing destructive changes without unlock/confirmation,
- hallway path distance preserving room ids and template bindings,
- hallway path distance regenerating only hallway pieces,
- preview relayout preserving export/template content,
- server-side invalidation matching client assumptions,
- stale layer version detection,
- orphaned template binding creation when topology ids disappear,
- resolved preview model matching generated runtime metadata for floor plans.

## Migration Scope

No legacy workspace migration is required for the initial implementation.

Workspaces are still early in development and there is no committed legacy data model that needs to be preserved. Existing development workspaces can be regenerated from the current planner model.

This simplifies the first implementation:

- require hierarchical planner ids for new generated workspace data,
- require layer lock state for new generated workspace data,
- require layer version state for new generated workspace data,
- regenerate old development workspaces instead of deriving compatibility ids,
- do not build fallback migration paths until there is real user data to preserve.

## Open Questions

Resolved decisions:

- Lock state is per planner layer.
- Stable ids are derived from the planner hierarchy.
- Hierarchical planner ids use dot notation, such as `keep.main.tower.center.floor.basement_01.floor_plan.room.main_00`.
- Java code should wrap planner ids in a small value type while serializing them as plain dot strings.
- Authored-content detection should primarily compare actual template bounds against generated expected scaffold.
- Managed sidecar blocks are generated-owned and can be rewritten freely.
- The full invalidation report should be serializable for client display.
- Hallway pieces should regenerate in-place when the conceptual hallway segment survives.
- The main planner page should eventually replace topology defaults as the primary control surface.
- Legacy workspace migration is out of scope for the initial implementation.
