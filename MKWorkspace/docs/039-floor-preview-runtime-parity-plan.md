# Floor Preview vs Runtime Parity Plan

## Goal

The floor plan preview and locked runtime generation should be two views of the same deterministic plan. Given the same workspace settings, root exits, exported template set, and locked seed, the preview should show the same room graph, hallway graph, branch rejections, link routes, and closed openings that runtime generation produces in world.

The preview should not approximate runtime by reimplementing geometry assumptions. It should consume the same resolved runtime plan model that locked generation consumes.

## Current Problem

The room graph is mostly shared through `MKFloorLayoutSolver`, but runtime and preview still diverge after solving:

- Preview draws `MKFloorLayoutSolver.FloorLayoutResult.segments()`, including `LINK_HALL` segments.
- Runtime places room, hall, and cap segments from the solver, but currently ignores solver-accepted links for carving.
- Runtime recomputes links later from placed-piece metadata in `MKJigsawStructure.applyFloorLinks`.
- Runtime closes unconnected floor openings as a later post-pass, which can close endpoints that a link pass just carved.
- Preview uses logical profile dimensions directly, while runtime ultimately uses exported NBT bounds and jigsaw positions.

For generated floor pieces, dimensions should not diverge. If they do, that is an export/scaffold validation bug. The parity work should make that contract explicit and testable.

## Required Contracts

### Deterministic Solve Contract

- Preview and runtime must call the same solver with the same seed.
- The preview seed and runtime locked layout seed must resolve to the same value when the floor is locked.
- Solver output must be stable for:
  - room count selection,
  - branch acceptance/rejection,
  - random main exit selection,
  - selected room profile ordering,
  - accepted link routes.

### Geometry Contract

Every generated runtime floor piece must have an explicit footprint descriptor:

- exported width and length,
- authored forward direction,
- runtime rotation,
- logical origin offset after export/crop,
- connector positions on lateral faces,
- opening width and height,
- link candidate positions,
- closable opening positions.

For generated hallway pieces:

- desired hallway length must equal exported major footprint span,
- entry and exit connectors must be centered on opposite end faces,
- connector-to-connector placement must match the solver's logical adjacency model,
- the unrotated authoring direction must be explicit.

For generated room/cap pieces:

- desired room width/length must equal exported footprint width/length,
- unrotated room forward direction must be north,
- local north/east/south/west exits must rotate the same way in preview and runtime.

### Link Contract

Solver accepted links must be authoritative.

Runtime should not independently recompute a different set of links from placed metadata. Runtime may use metadata to resolve exact endpoint world positions, but the chosen pairs and routes should come from the locked solver result.

### Closure Contract

Closed opening patching must be aware of accepted links.

An opening should be closed only if:

- it is a closable branch opening,
- no generated child piece connects through it,
- no accepted solver link uses it.

## Proposed Runtime Model

Introduce a shared resolved model, tentatively `MKResolvedFloorPlan`.

Inputs:

- `MKWorkspaceFloorTopologySettings`
- root width and length
- root exits
- effective hallway lead-in
- locked layout seed
- runtime/export footprint descriptors

Output:

- logical segments with segment index and parent segment index,
- selected segment kind,
- selected room profile,
- selected mask,
- selected main exit direction,
- selected pool/template constraints,
- rotation,
- expected footprint rect,
- expected connector endpoints,
- rejected exits,
- accepted links,
- accepted link routes,
- closable openings.

Preview renders `MKResolvedFloorPlan`.

Runtime locked generation places `MKResolvedFloorPlan` segments and carves `MKResolvedFloorPlan` links.

Export/datagen validates generated templates against the footprint descriptors used by the resolved model.

## Implementation Plan

### Phase 1: Make Links Solver-Authoritative

- Keep `plan.acceptedLinks()` from `placeLockedFloorPlan`.
- Track placed pieces by solver `segmentIndex`.
- Resolve accepted link endpoints from the placed segment metadata.
- Carve exactly the solver-accepted links.
- Remove or bypass independent link recomputation for locked floor plans.
- Return or store the set of link-carved endpoints so closure can skip them.

### Phase 2: Make Closure Link-Aware

- Run closure with knowledge of:
  - placed child connections,
  - accepted link endpoints.
- Do not close branch openings used by accepted links.
- Prefer ordering that is easy to reason about:
  - close rejected/unused openings,
  - carve accepted links after closure,
  - or have a single post-processing pass that performs both from one plan.

The single pass is preferred for true parity.

### Phase 3: Add Footprint Descriptors

- Add a runtime footprint descriptor for generated floor rooms, caps, and linear runs.
- Build descriptors from the same workspace settings used to scaffold/export pieces.
- Include connector endpoints and crop/origin offsets.
- Keep descriptors in memory for preview.
- Export descriptor fields into metadata for runtime validation and debugging.

### Phase 4: Preview Uses the Resolved Runtime Model

- Replace direct preview drawing from raw profile dimensions with drawing from `MKResolvedFloorPlan`.
- Show actual resolved footprint rectangles.
- Show accepted links from the same list runtime will carve.
- Show rejected exits and closed openings from the same model.

### Phase 5: Runtime Validation and Logs

During locked generation, log or assert when runtime placement differs from the resolved model:

- selected template footprint differs from expected footprint,
- connector position differs from descriptor,
- placed bounding box does not match expected logical rect after rotation,
- accepted link endpoint cannot be resolved,
- accepted link route intersects placed pieces unexpectedly.

Developer logging should include segment index, segment label, expected rect, actual bounding box, selected template, mask, and rotation.

### Phase 6: Tests

Add tests for:

- preview seed equals runtime locked seed behavior,
- generated hallway footprint matches requested hallway length,
- generated room footprint matches profile width/length,
- room rotation maps local exits to expected world directions,
- hallway rotation maps authored direction to expected world directions,
- locked solver accepted links are the only links carved at runtime,
- closure skips link endpoints,
- exported metadata round-trips required footprint and connector data.

## Expected Re-Export Requirements

Changes that only make runtime consume solver links may not require re-export.

Changes that add footprint descriptors or closure/link endpoint metadata will require re-exporting affected workspaces and regenerating data.

## Related Documents

- `032-floor-topology-planner-design.md`
- `035-floor-plan-link-pass-design.md`
- `031-floor-exit-mask-variant-authoring-plan.md`
- `030-walled-keep-courtyard-export-cropping-plan.md`
