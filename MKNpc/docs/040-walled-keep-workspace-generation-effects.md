# Walled Keep Workspace Generation Effects

This document summarizes the current generation behavior for an existing generated walled keep workspace and evaluates whether each action should remain destructive. It is meant as an audit target for the workspace save workflow, not as a final design contract.

## Current Safe Skip List

`CreateWorkspacePacket` skips the follow-up full `generateWorkspace()` call when one of these service checks is true:

- `canApplyPreviewMarginRelayout`
- `canApplyPaletteSwap`
- `canApplyIdentityRename`
- `canApplyMarginExpansion`
- `canApplyHallwayRoutingRegeneration`
- `canApplyLinkRenderingRefresh`

The floor topology preflight can also block an update when the requested change invalidates a locked generated layer.

## Action Matrix

| Action / setting changed | Examples | Current generation effect | Current workflow correct? |
|---|---|---|---|
| Create new workspace | First save for a new anchor | Full workspace generation. | Yes. There are no existing authored templates to preserve yet. |
| Explicit regenerate | Regenerate command/button | Full workspace generation. | Yes. The user explicitly requested regeneration. |
| Preview margin only | Workspace grid preview spacing | Non-destructive relayout. Moves workspace cells, preserves authored templates, skips full generation. | Yes. This is workspace layout metadata/placement, not template content. |
| Palette only | Global palette or palette overrides that can be represented as block replacement | Non-destructive palette swap. Updates blocks, skips full generation. | Yes for representable block substitutions. Palette edits that alter authoring families or ambiguous role mapping should remain blocked or require stronger handling. |
| Identity only | Namespace / structure name | Non-destructive rename, skips full generation. | Yes. This should update identity/export naming without rebuilding content. |
| Margin expansion only | Increase shell margin / exterior air margin | Non-destructive expansion, skips full generation. Shrinking margins is not in this safe path. | Yes. Expansion is safe because it adds clearance; shrinking can collide with authored work and should not be treated as equivalent. |
| Floor hallway routing settings | Hallway lead-in mode/count, links enabled, link density, max links per floor/room, max link length | Conditional topology patch: regenerates hallway routing, hallway pieces, sidecar blocks, runtime metadata. Preserves room template bindings. Skips full generation. | Mostly yes. This is the right direction, but it should continue to be guarded by layer locks and tests around preserving room bindings. |
| Floor link rendering settings | Link mode full/decay/debug, decay, endpoint intact radius, middle decay bonus, insert family/depth/spacing/chance/max decay | Metadata/rendering refresh: dirties sidecar blocks and runtime metadata only. Preserves topology and templates. Skips full generation. | Yes. These settings do not change authored templates or floor topology. |
| Floor topology scalar settings | Min/max main path pieces, max branch pieces, main/branch hallways enabled, main cap approach enabled, sprawl, locked layout seed | Destructive floor topology regenerate. Invalidates planner topology, room envelopes, connector graph, hallway routing/pieces, template bindings, sidecar blocks, runtime metadata. Full generation after confirmation. | Partially. This is currently correct for path count and seed changes, but some toggles may eventually be expressible as conditional topology patches if we can preserve affected template bindings. |
| Floor room profile changes | Room profile width/length/height, add/remove profile, main exit direction/randomization, branch exits, link-candidate exits | Treated as floor topology change. Full generation after confirmation. | Partially. Size/count/profile identity changes are destructive. Exit/link-candidate flags might be patchable if they only affect connector metadata or routing eligibility, but they currently need more precise invalidation modeling. |
| Vertical stack / tower structure changes | Floor count, floor heights, tower footprint, vertical stack dimensions | Not covered by a safe live path. Full generation after confirmation. | Mostly yes. These affect template dimensions, vertical relationships, and jigsaw layout. Some pure labeling/default-height edits could be separated later, but geometry edits should remain destructive. |
| Walled keep macro settings | Courtyard sizing/path settings, perimeter kind, wall/gate sizing, tower placement/topology settings | Not covered by a safe live path. Full generation after confirmation. | Mixed. Major courtyard/perimeter topology changes should be destructive. Some wall/gate presentation settings could eventually become palette/block substitution or sidecar-only refreshes if their generated layers are isolated. |
| Room family shape/connectivity | Family size, exits, extrusion mode, support flags, void margins, foundation override | Not covered by a safe live path. Full generation after confirmation. | Mostly yes. These define template bounds and connector authoring assumptions. Palette-only family changes should stay in the palette-safe path. |
| Opening profile edits | Door/opening width/height/profile settings | Not covered by a safe live path. Full generation after confirmation. | Mostly yes. Opening dimensions alter template geometry and connector masks. A future metadata-only path may be possible for display names or non-geometric metadata. |
| Linear run family edits | Walkway/wall run family settings | Not covered by a safe live path. Full generation after confirmation. | Partially. Geometry, connector, and template-bound changes are destructive. Palette/material-only linear run changes should be handled by palette swap, and purely procedural sidecar changes may deserve a smaller invalidation path. |
| Insert family library edits | Manually editing insert families outside the floor link auto settings | Not currently a special safe path. Full generation after confirmation unless the only resulting floor setting diff is link rendering refresh. | Partially. Link insert configuration is correctly non-destructive. General insert family authoring needs its own invalidation rules because template bounds/variants may affect authored workspace content. |
| Layer locks | Lock generated layers | Does not generate by itself. Blocks any update whose preflight invalidates a locked layer. | Yes. Locks should remain authoritative over otherwise safe updates. |

## Follow-Up Candidates

- Split floor room profile changes into geometry changes, connector metadata changes, and link-candidate routing eligibility changes.
- Add explicit invalidation rules for walled keep courtyard/perimeter layers once those generated layers are independently tracked.
- Define insert-family invalidation separately for link inserts, procedural closure inserts, and future designer-authored insert families.
- Consider a generic "safe preflight operation" hook in `CreateWorkspacePacket` so new safe operations do not need to be manually added to the packet skip list.
