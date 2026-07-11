# Floor Link Generation Plan

## Goal

Replace the current temporary yellow-wool floor link visualization with configurable runtime link generation for `MKFloorTopologyPlanner` floors.

The existing debug behavior should remain available, but it should not be the default when floor links are enabled. The default should be a generated hallway that uses the floor topology palette and can optionally decay or receive designer-authored insert templates along the solved route.

## Current Behavior

The link routing behavior is already split from normal floor topology placement:

- `MKFloorLayoutSolver` solves the main and branch floor layout.
- The same solver then adds optional loop links as `LINK_HALL` logical route segments.
- `MKJigsawPlacement` skips `LINK_HALL` segments during normal jigsaw piece placement.
- `MKJigsawStructure.afterPlace` resolves accepted links between already placed rooms.
- `MKJigsawStructure.carveLink` currently opens both endpoints and calls `carveCorridorCell`.
- `carveCorridorCell` currently places `Blocks.YELLOW_WOOL` as a temporary floor and ceiling marker and clears a simple 3-wide air volume.

This means the first implementation should not try to make loop links ordinary jigsaw children. Runtime post-placement corridor generation is the correct extension point.

## Non-Goals

- Do not remove debug link visualization.
- Do not make loop links participate in vanilla jigsaw pool expansion.
- Do not make link corridors satisfy main path or branch path topology requirements.
- Do not require designer-authored link templates for basic link generation.
- Do not add arbitrary pathfinding in this work. Use the routes already accepted by `MKFloorLayoutSolver`.

## Link Generation Modes

Add a link generation mode to floor topology link settings:

```json
"links": {
  "enabled": true,
  "generation_mode": "full_hallway"
}
```

Recommended enum:

- `full_hallway`
- `decaying_hallway`
- `debug`

Default:

- `full_hallway`

### Full Hallway

Build a complete corridor shell along the accepted route.

Expected block roles:

- Floor uses the resolved floor palette block.
- Side walls use the resolved wall palette block.
- Ceiling uses the resolved ceiling palette block.
- Interior uses air.

The generated corridor should be based on the endpoint opening profile:

- Width should be at least the larger endpoint opening width.
- Height should be at least the smaller compatible endpoint opening height, with a safe minimum of 2.
- Shell width includes both the walkable opening span and wall thickness.
- Shell height includes floor, interior air, and ceiling.

### Decaying Hallway

Build the same logical corridor volume as `full_hallway`, but use deterministic decay to omit some shell blocks.

Designer controls:

```json
"links": {
  "generation_mode": "decaying_hallway",
  "decay": 0.45,
  "endpoint_intact_radius": 3,
  "middle_decay_bonus": 0.25
}
```

Suggested defaults:

- `decay`: `0.0`
- `endpoint_intact_radius`: `3`
- `middle_decay_bonus`: `0.25`

Semantics:

- `decay = 0.0` behaves like `full_hallway`.
- `decay = 1.0` places no procedural shell and gives interior air carving the highest chance to fail, but still carves at least 70% of the tunnel volume.
- Values between `0.0` and `1.0` probabilistically omit shell blocks.
- Values between `0.0` and `1.0` can also probabilistically omit interior air carving so some natural blocks remain inside the route.
- Decay is deterministic from world seed, topology group, link endpoints, route index, and block position.

The endpoints should be the most intact parts of the generated corridor. The middle should exhibit the most decay.

Recommended decay profile:

```text
room opening -> intact shell -> chipped shell -> broken middle -> chipped shell -> intact shell -> room opening
```

### Debug

Preserve the current temporary visualization behavior.

Debug mode should:

- Open the endpoints.
- Carve the route interior.
- Place visible marker blocks, initially equivalent to the current yellow wool behavior.
- Avoid palette-driven shell generation.
- Avoid designer insert placement unless a later debug option explicitly asks for it.

## Palette Metadata

Runtime link generation needs floor, wall, and ceiling block states.

Current runtime metadata has `wall_block`, but the link builder needs the full resolved palette. Add metadata for:

- `floor_block`
- `wall_block`
- `ceiling_block`

The source should be the same palette resolution path already used for floor topology authoring:

- workspace palette
- topology/floor palette overrides
- room/profile palette overrides where applicable

For accepted links between two rooms with different resolved palettes, use the resolved topology group palette.

This keeps generated link corridors visually coherent with the floor topology that requested them instead of blending per-room palette details into a route that may cross multiple room types.

## Decay Model

Implement decay through a helper that decides whether to place a shell block.

Conceptual formula:

```text
removeChance = decay * verticalWeight * routePositionWeight * edgeWeight
placeShellBlock = deterministicNoise(pos, linkSeed) >= clamp(removeChance, 0, 1)
```

Interior air should use the same weighted decay framework in `decaying_hallway` mode.

For each block that would normally be carved to air, evaluate it as an air layer with its own vertical and edge weights. When air carving is skipped, leave the existing world block in place. This lets decayed links expose caves, stone, dirt, ore, water, or other natural generation instead of always forcing a clean tunnel volume.

The air weights should be tuned so even fully decayed links still carve at least 70% of the intended tunnel air volume. A final clamp may enforce that no individual air block has more than a 30% skip-carve chance, but the design should come from the same weighted decay model used for shell blocks.

### Vertical Bias

Decay should be biased toward the upper parts of the hallway. Floors and lower walls should survive more often than ceilings and upper walls.

Suggested vertical weights:

```text
floor center: 0.10
floor edge:   0.25
lower wall:   0.35
mid wall:     0.65
upper wall:   0.90
ceiling:      1.15
lower center air:     0.05
lower side air:       0.15
mid side air:         0.25
upper air:            0.35
ceiling-adjacent air: 0.45
```

Clamp the final removal chance to `0.0..1.0`.

For air layers, clamp or tune the final skip-carve chance so the route still carves at least 70% of the intended tunnel volume at `decay = 1.0`.

### Endpoint Protection

Endpoints should be more intact than the middle.

Suggested route position weight:

```text
distance from nearest endpoint:
0 cells: 0.10x
1 cell:  0.25x
2 cells: 0.50x
3 cells: 0.75x
4+ cells: fade toward 1.00x
middle:  1.00x + middle_decay_bonus
```

For short links, clamp the curve so the whole link does not collapse into the maximum-middle-decay case.

### Navigability

The first implementation should preserve a more reliable walking strip than the visual shell.

Recommended rule:

- Strongly protect the core walking volume from skipped air carving when `decay < 1.0`.
- Strongly protect the floor centerline when `decay < 1.0`.
- Let floor edges decay more than the centerline.
- Let upper and edge interior air fail to carve more often than the player-height centerline.
- At `decay = 1.0`, place no shell blocks and allow the highest natural-block preservation chance inside the route, capped so at least 70% of tunnel air still carves.

This keeps low and medium decay hallways traversable while allowing high-decay links to become partially obstructed or exposed to whatever underground terrain already exists.

## Designer Authored Inserts

Designer inserts should use existing workspace/template infrastructure as an authoring and export format, but not as jigsaw-placed children.

The runtime link route remains authoritative. Inserts are manually stamped into selected spans of the generated corridor after the base corridor has been generated.

Longer term, hallway inserts should be one consumer of a broader workspace insert family system rather than a one-off raw template field on floor links.

### Insert Contract

An insert is a corridor segment replacement.

The template volume includes the full hallway shell:

- local X: corridor shell width
- local Y: corridor shell height
- local Z: insert depth along the route

The insert replaces the generated shell and interior in its occupied span, except where the template contains `STRUCTURE_VOID`.

Placement semantics:

- `STRUCTURE_VOID`: preserve the generated hallway block already present at that position.
- Air: clear the block.
- Any other block: replace the generated hallway block.

This lets designers replace entire corridor sections with authored arches, supports, lights, rubble, collapsed ceilings, side details, or intact reinforcement segments while still using void to keep parts of the procedural shell.

### Insert Settings

Suggested settings:

```json
"links": {
  "insert_family": "crypt_link_supports",
  "insert_depth": 3,
  "insert_spacing": 7,
  "insert_probability": 0.65
}
```

Suggested defaults:

- `insert_family`: empty
- `insert_depth`: `1`
- `insert_spacing`: `0`
- `insert_probability`: `1.0`

Rules:

- If no insert family is configured, do not stamp inserts.
- If `insert_spacing <= 0`, do not stamp inserts.
- `insert_depth` reserves that many consecutive route cells.
- Inserts should be optional. Missing or invalid insert families/templates should log in dev mode and leave the base generated hallway intact.

### Insert Eligibility

The first implementation should keep insert placement conservative.

Eligible spans:

- Fully inside the accepted link route.
- At least `endpoint_intact_radius` cells away from both endpoints.
- Straight for the entire `insert_depth`.
- Not crossing a route bend.
- Not overlapping a placed room bounding box.
- Inside the current chunk bounds for the blocks being placed.

Later work can support bend inserts and endpoint-specific inserts as separate template categories.

### Insert Decay

Default behavior:

- Do not apply procedural decay to designer insert blocks.
- Use the route span's decay score to decide whether an insert should be placed at all.

Rationale:

- Designers authored the insert intentionally.
- A broken support, damaged arch, or rubble pile can be authored directly.
- If a route section is too decayed, skipping the insert is clearer than damaging authored blocks procedurally.
- Later work can support insert family variants selected by decay band, such as intact, damaged, and ruined.

Suggested setting:

```json
"links": {
  "insert_max_decay": 0.55
}
```

Placement rule:

```text
spanDecay = baseDecay * routePositionWeight
place insert only when spanDecay <= insert_max_decay
```

`insert_probability` should still apply after the decay threshold passes.

## Workspace Insert Families

Floor link inserts should become part of a general workspace insert family model.

The workspace model would gain a collection like:

```text
Workspace
  room families
  linear run families
  opening profiles
  insert families
```

An insert family is a designer-authored family with one fixed template shape, shared placement intent, validation rules, and export metadata.

Insert families should follow the same broad pattern as other authored workspace families:

- The family declares one fixed template shape.
- The shape has fixed bounds and a placement contract.
- The workspace generates one authoring template slot for that family.
- Designers must provide at least one concrete variant of that template.
- Designers may add more variants with the same shape.
- Runtime randomly selects from the available compatible variants.

The first implementation should use simple deterministic random selection among valid variants. Weights, tags, and richer variant filters can be added later without changing the family model.

Examples:

- `crypt_link_supports`
- `courtyard_statues`
- `floor_opening_closures`
- `wall_detail_alcoves`
- `lighting_sconces`
- `rubble_piles`

The important separation is:

- Insert family: authoring/export/tracking concept with one fixed template shape.
- Insert variant: one concrete authored structure asset for that family shape.
- Insert consumer: feature-specific placement rules for where and how a family may be used.

The workspace should expose insert families through a dedicated editor rather than relying on designers to manually tag ordinary pieces.

### Consumers

The same insert family system should support multiple workspace features.

Floor link corridor consumer:

- Selects inserts along accepted link routes.
- Requires straight route spans in v1.
- Uses corridor shell width, shell height, and configured insert depth.
- Stamps a selected compatible insert variant manually after base corridor generation.

Walled keep courtyard consumer:

- Selects inserts for courtyard sockets or authored placement points.
- Useful for randomized statues, fountains, trees, decorations, or encounter props.
- Can automate any jigsaw block or metadata configuration needed inside the authored insert pieces.
- Keeps all designer-authored courtyard insert variants tracked by the workspace.

Floor opening closure consumer:

- Replaces current purely procedural wall-block closure patches when an authored closure family is configured.
- Useful for boarded doors, collapsed stone, sealed gates, cracked masonry, secret walls, or rubble closures.
- Falls back to current procedural wall-block closure when no valid insert exists.

### Insert Family Data

A first family model should likely include:

```json
{
  "id": "crypt_link_supports",
  "kind": "floor_link_hallway",
  "width": 5,
  "height": 4,
  "depth": 3
}
```

Potential fields:

- stable family id
- consumer kind or allowed consumer tags
- fixed footprint width, height, and depth
- required opening or corridor profile tags
- palette behavior
- whether jigsaw blocks inside the insert should be rewritten, ignored, or preserved
- validation warnings for oversized or incompatible templates

Concrete insert variants should be tracked the same way other authored family variants are tracked. For example:

```text
Insert family: crypt_link_supports
Shape:
  kind: floor_link_hallway
  width: 5
  height: 4
  depth: 3
Variants:
  crypt_link_supports_0
  crypt_link_supports_1
  crypt_link_supports_broken
```

All variants in the family must fit the declared shape. If more than one valid variant exists, runtime chooses one deterministically from the available compatible variants.

The first implementation can keep this small and focused on link corridors, but it should name the concept as insert families so courtyard and closure inserts do not need separate ad hoc systems later.

### Dedicated Insert Family Editor

Insert families should be first-class workspace objects.

The workspace UI should include an Insert Families page where designers can:

- create, rename, and delete insert families
- choose the family consumer kind, such as `floor_link_hallway`, `courtyard_socket`, or `floor_opening_closure`
- configure the family's fixed width, height, and depth
- generate or manage the family authoring template slot
- add, remove, and inspect concrete authored variants for that family shape
- configure allowed opening profiles or corridor profiles
- configure palette behavior
- decide how jigsaw blocks inside insert templates are handled
- see validation warnings for missing templates, incompatible dimensions, invalid consumer kinds, or missing socket references

This keeps insert authoring explicit and discoverable. It also avoids hidden behavior where a normal workspace piece only becomes an insert because of low-level tags.

### Insert Socket Dev Item

Some insert consumers need configured sockets inside designer-authored templates. Courtyard statues are the clearest example: a designer may want to place one or more randomized insert sockets inside a courtyard template without hand-authoring jigsaw block NBT.

Add a workspace dev item for this workflow, tentatively:

- Insert Socket Wand
- Insert Socket Tool
- Workspace Insert Socket Placer

Behavior:

1. Designer uses the item on a block while inside an active workspace.
2. The item opens a workspace-aware menu.
3. The menu lists insert families compatible with the current workspace and selected consumer kind.
4. Designer chooses the insert family and optional socket settings.
5. The tool places or updates a correctly configured marker block, likely a jigsaw block if that fits the existing export path.
6. The workspace records enough metadata for export/runtime validation and placement.

The menu should allow:

- consumer kind selection
- insert family selection
- orientation from clicked face, with manual override
- optional socket id
- optional placement probability override
- optional required tags or profile constraints
- optional depth/profile constraints where relevant

The designer should not need to know the jigsaw `name`, `target`, `pool`, or custom NBT conventions. The tool should write those details consistently.

Conceptual jigsaw configuration:

```text
name:   mknpc:insert_socket/<consumer_kind>/<family_id>
target: mknpc:insert/<consumer_kind>
pool:   mknpc:<workspace>/<insert_family_pool>
```

The exact fields should follow the exporter/runtime implementation, but the authoring interaction should remain stable.

Important distinction:

- The marker may be represented as a jigsaw block for authoring/export convenience.
- The consumer does not have to use vanilla jigsaw expansion at runtime.

For floor link hallway inserts, the solved link route remains authoritative and the runtime link generator manually stamps selected inserts.

For courtyard sockets, the consumer may manually select and stamp inserts or may generate automated pool-backed jigsaw configuration if that better matches the existing courtyard generation path.

For floor opening closures, the socket may be implicit from the opening metadata rather than a placed marker. The same insert family model still applies.

The dev item should validate authoring-time mistakes:

- selected family does not exist
- selected family is not compatible with the consumer kind
- selected insert family shape is too large for the socket/profile
- selected insert variants do not match the family shape
- socket orientation is invalid
- required opening/corridor profile is missing
- referenced insert family has no valid templates

This tool should pair with the dedicated Insert Families editor: the editor manages the families and variants, while the dev item places configured sockets in authored structure templates.

## Runtime Order

Update `MKJigsawStructure.afterPlace` link processing to run in this order:

1. Resolve accepted solver links.
2. Close unconnected floor openings.
3. Apply piece foundations.
4. Generate accepted links.
5. For each accepted link:
   - Open both endpoints.
   - Generate the base corridor according to `generation_mode`.
   - Stamp configured inserts over eligible spans.

Link generation should remain last so closure and foundations cannot overwrite planned corridors.

## Workspace Invalidation

The new link generation settings must not accidentally force destructive workspace regeneration.

Current floor topology invalidation uses broad categories:

- Topology-shaping changes trigger `regenerate_floor_topology`.
- Hallway/link routing changes trigger `regenerate_hallway_routing`.
- Unclassified settings changes currently fall through to broad topology regeneration.

When adding link generation settings, explicitly classify them so visual/runtime rendering options do not hit the destructive fallback.

### Rendering-Only Settings

These settings should not change room envelopes, connector graph, template bindings, or accepted link routes:

- `generation_mode`
- `decay`
- vertical decay bias fields
- `endpoint_intact_radius`
- `middle_decay_bonus`
- `insert_family`
- `insert_depth`
- `insert_spacing`
- `insert_probability`
- `insert_max_decay`

Expected invalidation:

- No destructive floor topology regeneration.
- No hallway routing regeneration unless route-selection fields also changed.
- Mark only the layers needed for export/runtime metadata refresh.

Likely dirty layers:

- `RUNTIME_METADATA`
- possibly `SIDECAR_BLOCKS` if insert-family metadata or exported auxiliary records are stored there

These settings affect how accepted links are rendered at world generation time. They should preserve authored room template bindings and generated hallway piece definitions.

### Route-Selection Settings

These settings can change which links are accepted or where link routes run:

- `linksEnabled`
- `linkDensity`
- `maxLinksPerFloor`
- `maxLinksPerRoom`
- `maxLinkLength`

Expected invalidation:

- `regenerate_hallway_routing`
- preserve room template bindings
- dirty hallway routing, hallway pieces, sidecar blocks, and runtime metadata as needed

This matches current behavior in `MKWorkspaceFloorTopologyInvalidationAnalyzer`.

### Topology Settings

These settings should continue to require broad floor topology regeneration:

- main path min/max
- branch depth/cap settings
- main/branch hallway enabled flags
- main cap approach enabled
- sprawl
- locked layout seed
- floor room profile lists
- profile exits, dimensions, or weights

Expected invalidation:

- `regenerate_floor_topology`
- destructive regenerate warning remains appropriate

### Analyzer Changes

Add a new classification method in `MKWorkspaceFloorTopologyInvalidationAnalyzer`, for example:

```java
private boolean linkRenderingChanged(MKWorkspaceFloorTopologySettings previous,
                                     MKWorkspaceFloorTopologySettings updated) {
    return previous.linkGenerationMode() != updated.linkGenerationMode()
            || Float.compare(previous.linkDecay(), updated.linkDecay()) != 0
            || previous.endpointIntactRadius() != updated.endpointIntactRadius()
            || previous.insertDepth() != updated.insertDepth()
            || previous.insertSpacing() != updated.insertSpacing()
            || Float.compare(previous.insertProbability(), updated.insertProbability()) != 0
            || Float.compare(previous.insertMaxDecay(), updated.insertMaxDecay()) != 0
            || !Objects.equals(previous.insertFamily(), updated.insertFamily());
}
```

Then handle it before the destructive fallback:

```java
if (hallwayRoutingChanged(previous, updated)) {
    return hallwayRoutingReport(floorPlannerId);
}

if (linkRenderingChanged(previous, updated)) {
    return linkRenderingReport(floorPlannerId);
}

return broadTopologyReport(floorPlannerId);
```

`linkRenderingReport` should use a non-destructive safety level and communicate that only runtime/export link rendering metadata changed.

## Implementation Plan

1. Add `MKWorkspaceFloorLinkGenerationMode` enum with codec values `full_hallway`, `decaying_hallway`, and `debug`.
2. Extend `MKWorkspaceFloorTopologySettings.LinkSettings` with generation mode, decay fields, and insert fields.
3. Add `with...` helpers for new settings used by UI/editor code.
4. Extend exported floor runtime metadata with `floor_block` and `ceiling_block`, keeping existing `wall_block`.
5. Update metadata export/import code to populate and read the full palette.
6. Thread the relevant floor link settings into `MKJigsawStructure` link candidates or resolve them by topology group during `resolveSolverFloorLinks`.
7. Replace `TEMP_LINK_MARKER_STATE`-driven `carveCorridorCell` with a mode dispatcher:
   - `renderDebugLinkCell`
   - `renderFullHallwayCell`
   - `renderDecayingHallwayCell`
8. Add deterministic link/block noise helpers for decay.
9. Implement endpoint-protected, middle-biased decay weighting.
10. Implement width/height-aware corridor cell generation from endpoint dimensions.
11. Add insert family lookup and manual template stamping for straight eligible route spans.
12. Add dev logging for skipped inserts and link generation summaries.
13. Update floor preview UI to expose mode, decay, endpoint intact radius, insert template, insert depth, spacing, and probability.
14. Update `MKWorkspaceFloorTopologyInvalidationAnalyzer` so link rendering settings do not force destructive floor topology regeneration.
15. Add tests or debug scenarios for:
    - debug mode preserves yellow marker behavior
    - full hallway uses palette floor/wall/ceiling blocks
    - decay preserves endpoints more than the middle
    - upper shell blocks decay more often than lower shell blocks
    - `decay = 1.0` places no shell and still carves at least 70% of tunnel air
    - inserts replace generated shell
    - structure void in inserts preserves generated shell
    - insert depth reserves consecutive straight route cells
    - missing insert families/templates do not fail structure generation
    - rendering-only link setting changes do not request `regenerate_floor_topology`

## Resolved Decisions

- Link corridor palette comes from the resolved topology group palette.
- Insert families are first-class workspace objects managed through a dedicated insert-family editor.
- Insert sockets are placed with a workspace-aware dev item that writes correctly configured marker or jigsaw block data.
- Insert families use one fixed template shape with one or more concrete authored variants.
- V1 insert variant selection is simple deterministic random selection from compatible variants. Weights and richer selection rules can be added later.
- Endpoint-specific insert templates are out of scope for v1.
- Decaying hallway mode omits procedural shell blocks and can skip interior air carving in v1. It does not place rubble or alternate decay blocks yet.
