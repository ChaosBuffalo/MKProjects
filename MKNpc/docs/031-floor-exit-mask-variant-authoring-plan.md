# Floor Exit Mask Variant Authoring Plan

## Problem

Dense floor topology graphs can request too many branch exits from the same room. When the room templates expose every possible branch connector, jigsaw generation can choose combinations that immediately overlap nearby rooms or hallways. The result is a visually regular failure pattern: rooms collide, later branches are cut off, and the final layout looks arbitrary rather than intentionally pruned.

The floor planner should choose a connector mask that fits the reserved footprint before export/generation, instead of letting jigsaw collision decide which branches survive.

## Goals

- Let designers author a compact room template with all possible branch connector locations.
- Generate export-time variants for the useful exit masks.
- Use those variants to choose a room shape that matches the planner-approved exits.
- Close inactive connector openings automatically with palette patching.
- Preserve designer decoration and exterior margins outside the connector opening footprint.
- Keep main caps and branch caps terminal, with no optional branch exits.
- Add a designer-facing `sprawl` setting that controls how aggressively optional branch connectivity is kept.
- Preview a sampled generation outcome instead of only showing maximum possible connectivity.
- Let designers lock a preview sample so runtime mask selection follows the same seed-derived choices.

## Implementation Status

Implemented:

- Compact floor room authoring remains the workspace-facing model.
- Export creates runtime `_mask_*` variants for floor plan room templates.
- Runtime variants remove inactive optional branch connectors.
- Closed optional connector openings are patched during structure export using the resolved wall palette block.
- Mask variants carry export weights derived from the floor `sprawl` setting.
- Runtime pools are built from exported runtime pieces instead of authoring templates.
- Export also publishes mask-specific runtime pools under `.../masks/<mask>`, so planner/runtime code can target an exact selected branch mask when it has one.
- Floor topology pools are scoped per floor topology group so main and branch paths do not share stale global pools.
- The floor preview renders a sampled layout from the shared floor layout solver and exposes a preview-only `Reroll` control.
- The shared solver reports the branch mask that actually placed for each room, not just the mask configured on the room profile.
- Runtime piece metadata carries the exported `floor_exit_mask`, and dungeon placement state records the mask of the currently placed floor piece.
- Runtime jigsaw placement now redirects floor room pool lookups to exact `masks/<mask>` pools when those pools exist. `sprawl = 0` selects `none`; `sprawl = 1` selects the largest available mask; intermediate values use weighted mask selection.
- Branch caps and main caps are terminal categories with no optional branch exits.
- Floor topology settings can optionally persist a `locked_layout_seed`.
- The floor preview exposes `Lock`/`Unlock`. Locking stores the current preview seed on the floor topology setting; unlocking returns to session-only preview rerolls.
- Runtime mask selection uses the locked layout seed when present, deriving per-connector random sources from the locked seed, pool id, topology group state, and connector position.
- Tests cover scoped floor pools, an east/west main path mask case, mask-specific runtime pools, runtime mask metadata, accepted solver masks, runtime mask selection, and closed connector patch footprint geometry.

Still open:

- Runtime generation can now share a locked seed with the UI preview, but it still does not replay a precomputed full floor-layout reservation graph. The preview and runtime use the same seed-derived rules, but runtime placement remains incremental because jigsaw expansion order is runtime-owned.
- A later exact-replay mode could persist selected room/mask decisions from a solved `FloorLayoutResult` instead of only sharing a seed.

## Exit Mask Model

Floor room templates use a south-facing convention:

- `S` is the required inbound side for main rooms, branch rooms, approaches, and caps.
- `N`, `E`, and `W` are the possible outbound branch directions.
- Main rooms and main cap approaches may also have a main path exit on one of `N`, `E`, or `W`.
- Main caps and branch caps only expose their required inbound connector.

For optional branch fanout, the useful branch masks are:

| Mask | Branch Exits |
| --- | --- |
| `none` | no optional branch exits |
| `n` | north |
| `e` | east |
| `w` | west |
| `ne` | north, east |
| `nw` | north, west |
| `ew` | east, west |
| `new` | north, east, west |

The planner should reserve footprints first, then choose a valid mask according to the floor's sprawl setting. At high sprawl, it should tend toward larger valid masks. At low sprawl, it should keep only required topology and early or explicit branches.

## Compact Authoring

Designers author one logical template per room profile and category. That template may include all possible connectors for its category.

For a branch room, the authored template can contain:

- required `S` branch entry connector
- optional `N`, `E`, and `W` branch connector locations
- fully open passage cuts for those optional connectors

For a main room, the authored template can contain:

- required `S` main entry connector
- possible `N`, `E`, and `W` main exit connector locations
- possible branch connector locations where the room allows optional branch exits

The designer should not need to author separate files for `n`, `e`, `nw`, `new`, and so on.

## Export Variants

During export, each compact template should produce runtime variants keyed by connector mask.

Example branch room variants:

- `branch_room_none`
- `branch_room_n`
- `branch_room_e`
- `branch_room_w`
- `branch_room_ne`
- `branch_room_nw`
- `branch_room_ew`
- `branch_room_new`

Each variant keeps only the jigsaw connectors required by its selected mask. Inactive optional jigsaws are removed, and their shell openings are closed before the runtime structure template is written.

Runtime templates should be export artifacts only. The workspace remains compact and designer-facing data should continue to describe the logical room profile, not every generated variant.

## Palette Patching For Closed Openings

Palette patching is the first closure strategy.

When an optional connector is inactive for a variant, export should:

1. Remove the inactive jigsaw block and any associated connector metadata.
2. Resolve the connector opening profile for that path kind.
3. Compute the canonical opening footprint on the relevant wall face.
4. Fill only that opening footprint with palette-resolved shell or wall blocks.
5. Leave exterior decoration, interior decoration, structure void margins, and unrelated blocks untouched.

This keeps compact authoring simple while still producing closed runtime variants.

The patch should be based on the same palette resolution used by generated templates. If a palette override applies to the room profile, the closure block should come from the overridden palette.

## Opening Footprint Rules

The closure operation should patch the connector cutout, not the whole wall side.

Inputs:

- room dimensions
- shell thickness
- connector direction
- opening profile width and height
- side and vertical offsets
- resolved palette

The exporter should derive a rectangular wall-plane footprint from those values. The filled area should cover the air doorway or passage cut that would otherwise lead to nowhere.

For the first implementation, palette patching can use the primary wall/shell block for the room. More advanced closure decoration, such as copied trim or designer-authored closure patches, can be added later.

## Planner Selection

The floor planner should stop treating branch exits as independent jigsaw outcomes. Instead:

1. Build the desired graph from the root exits and room settings.
2. Reserve the root room footprint.
3. Reserve main path rooms and main cap content in deterministic order.
4. Evaluate branch exits from each room against the current reserved footprint.
5. Build the set of valid exit masks for the room.
6. Choose one valid mask using the floor's sprawl setting and layout RNG.
7. Assign the chosen exit mask for the room.
8. Route generation to the pool or element matching that mask.

If no optional branch exits fit, the room uses the `none` mask and remains valid.

This makes dense graphs degrade cleanly instead of relying on generation cutoff.

## Sprawl

`sprawl` is a normalized floor topology setting from `0.0` to `1.0`.

It controls how much optional connectivity the planner tries to preserve after satisfying required topology and fit validation.

Suggested behavior:

| Sprawl | Behavior |
| --- | --- |
| `0.0` | Generate only required topology plus the first/root-level optional branches needed to satisfy the designer's explicit floor exits. Avoid continued branch expansion. |
| `0.5` | Generate moderate optional branching. Prefer smaller valid masks but allow occasional larger fanout. |
| `1.0` | Maximize optional connectivity. Prefer the largest valid masks and longest allowed branch/main expansions. |

Sprawl must never override footprint validation. It only chooses among valid masks and valid continuation lengths.

Sprawl should influence:

- how many optional root branches are accepted
- which valid branch mask is selected for a room
- whether branch rooms continue toward another branch room or terminate at a branch cap
- how close main path length samples are to the configured max

Sprawl should not influence:

- required inbound connectors
- required main path contracts
- required caps
- collision safety
- jigsaw hard size limits

The planner should still be deterministic for a given settings snapshot and seed.

## Sample Layout Preview

The floor plan preview should default to showing a sampled layout result, not the maximum possible connectivity envelope.

The current maximum-connectivity preview is useful for worst-case debugging, but it gives designers a poor sense of likely output when the planner is intentionally stochastic and sprawl-controlled.

The preview should render a shared `FloorLayoutResult` produced by the same planner used for export/data generation. The UI should not independently reconstruct a different interpretation of the floor topology settings.

Preview behavior:

1. Use the current floor settings and a preview seed.
2. Run the shared floor layout solver.
3. Render the chosen room profiles, selected main path length, accepted branch masks, branch caps, main cap approach, and main cap.
4. Show rejected optional exits only when useful, such as with collision markers or hover details.
5. Keep the preview stable until settings change or the designer rerolls.

The preview should include small `Reroll` and `Lock`/`Unlock` buttons in the top right of the preview panel.

- `Reroll` changes only the session preview seed while the floor is unlocked.
- `Lock` writes the current preview seed to the floor topology setting as `locked_layout_seed`.
- `Unlock` clears `locked_layout_seed` and returns to session-only preview sampling.
- While locked, the preview should continue using the persisted seed and reroll should be disabled.

Unlocked preview seeds can live in UI/session state because they are inspection aids, not topology data. Locked seeds are topology data because they are exported into runtime topology rules and participate in mask selection.

Optional debug mode:

- A future toggle can show the maximum-connectivity/worst-case footprint view.
- That mode is for validation and tuning, not the default designer preview.

## Shared Layout Result

The floor planner should produce a result object that both export and UI can consume.

The result should include:

- root footprint
- chosen main path room count
- chosen room profile for each placed room
- chosen exit mask for each placed room
- placed hallway segments, if enabled
- placed main cap approach, if enabled
- placed main cap and branch caps
- rejected candidate exits with rejection reason
- total footprint extents
- whether generation fits hard limits

This result is the contract between topology planning, preview rendering, and runtime export generation.

## Preview And Runtime Seed Contract

The first seed-sharing implementation should be deterministic without requiring jigsaw placement to replay the entire UI solver result.

Rules:

1. Each floor topology setting may carry an optional `locked_layout_seed`.
2. Export copies that seed into the corresponding runtime `MKDungeonTopologyGroupRule`.
3. Runtime mask selection checks the topology group rule for a locked seed.
4. If no locked seed exists, runtime uses the normal jigsaw random source.
5. If a locked seed exists, runtime derives a local random source from:
   - locked layout seed
   - target base pool id
   - topology group
   - main path index
   - branch index
   - connector block position
6. The local random source is then passed through the same sprawl-weighted mask picker.

This makes a locked preview sample stable enough for designers to iterate against while keeping runtime compatible with vanilla jigsaw expansion order. The runtime still validates available mask pools and falls back to the original pool if no mask-specific pool exists.

## UI Behavior

The floor plan preview should show a sampled output layout by default. It may also show requested exits and accepted masks when hovering specific rooms.

Recommended display:

- active accepted exits use the normal branch color
- requested-but-rejected exits are shown as collision/rejection markers
- hover text explains which footprint caused the rejection
- terminal caps show their inbound connector only
- current `sprawl` value is visible near the preview controls
- `Reroll` and `Lock`/`Unlock` buttons are available in the top right of the preview panel

For room profile editing:

- branch caps and main caps should not expose optional branch toggles
- main rooms and main cap approaches can choose a main exit direction
- optional branch exits cannot occupy the same local direction as the selected main exit

## Data Impact

Workspace data should continue to store logical room profiles and their configured exits. Generated mask variants should not be written back into workspace JSON.

Export data needs a runtime naming convention for mask variants and a way for generated pools to target a specific mask. This can be represented as either:

- distinct runtime template ids per mask, or
- distinct pools per mask that contain the matching template variants

The first implementation can use distinct runtime template ids because it mirrors the existing compact-rotation export approach.

## Implementation Steps

1. Add an export-time mask variant abstraction for floor room profiles.
2. Generate all valid branch masks for room categories that allow optional branch exits.
3. Generate main-exit directional variants for main rooms and main cap approaches.
4. Remove inactive jigsaws from each runtime variant.
5. Apply palette patching to inactive connector openings.
6. Update floor topology pool generation to reference mask-specific runtime templates.
7. Add `sprawl` to floor topology settings.
8. Introduce a shared seeded floor layout solver and result model.
9. Teach the planner to assign accepted masks after footprint reservation using sprawl-weighted selection.
10. Update the floor plan preview to render a sampled `FloorLayoutResult`.
11. Add a preview-only `Reroll` button and seed state.
12. Add validation that caps cannot expose optional branch exits.
13. Add optional `locked_layout_seed` to floor topology settings.
14. Add preview `Lock`/`Unlock` controls backed by the workspace draft session.
15. Export locked seeds into runtime topology group rules.
16. Use locked seeds for deterministic runtime mask-pool selection when present.

## Non-Goals

- Do not require designers to author every mask variant manually.
- Do not use vanilla jigsaw rotation as the solution.
- Do not add donor patching or designer-authored closure patches in the first version.
- Do not remove exterior authoring margin around templates.
- Do not let jigsaw collision be the primary branch-pruning mechanism.
- Do not make the preview reroll mutate exported workspace data.
- Do not require locked previews for normal generation; unlocked floors should remain stochastic at runtime.

## Future Work

- Designer-authored closure patch regions for decorative blocked doors or collapsed exits.
- Weighted mask preferences, such as preferring side branches over forward branches.
- More than one branch-depth pass once footprint reservation is robust.
- Preview controls that show how changing room dimensions changes accepted masks.
- Debug toggle for maximum-connectivity footprint visualization.
- Exact layout replay by exporting selected room/mask decisions instead of only a locked seed.
