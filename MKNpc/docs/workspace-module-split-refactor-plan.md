# Workspace Module Split Refactor Plan

## Goal

Split the structure Workspace editor out of `MKNpc` into a new `MKWorkspace` module so normal gameplay installs do not load editor UI, authoring workflow, workspace dev blocks, editor packets, editor commands, or authoring/data-generation planners.

After the split:

- `MKNpc` remains the runtime gameplay module.
- `MKWorkspace` becomes an optional authoring and data-generation module that depends on `MKNpc`.
- `MKWidgets` owns reusable UI controls that are useful outside the workspace editor.
- Procedural runtime floor layout remains in `MKNpc` because gameplay worldgen still solves layouts procedurally.
- Generated datapack resources remain consumable by `MKNpc` without requiring `MKWorkspace` in player installs.

## Agreed Direction

The dependency direction should be:

```text
MKWorkspace -> MKNpc -> MKWidgets
MKWorkspace -> MKWidgets
```

`MKNpc` should not depend on `MKWorkspace`. This keeps the editor optional.

Data generation can require `MKWorkspace`. A developer or build environment that generates structures/datapacks is expected to have `MKWorkspace` loaded. The generated JSON/NBT resources are then shipped with, or alongside, `MKNpc` for real gameplay.

## Current Workspace Surface

The workspace functionality currently spans several areas:

- `client/gui/screens/MKWorkspaceScreen.java`
- 46 files under `client/gui/screens/workspace`
- `world/gen/workspace` authoring model, services, planners, mutation, scaffold, stairs, export, import, backup, and capability code
- `MKWorkspaceCommands`
- workspace network packet/payload classes, currently including compact payload helpers and chunked piece loading
- `MKWorkspaceDevBlock` and `MKWorkspaceDevBlockEntity`
- workspace block/item/entity registration in `MKNpcBlocks` and `MKNpcBlockEntityTypes`
- workspace saved-data attachment in `MKNpcAttachments`
- workspace client registration in `MKNpc.clientSetup`
- workspace language entries in `MKNpcLanguageProvider`
- generated runtime manifests under `data/<namespace>/mk_workspace_exports`
- runtime structure code in `world/gen/feature/structure` that imports some workspace model/planner types

The split should not be implemented as a bulk package move. There are real runtime dependencies that need to be isolated first.

## Recent Implementation Notes

Work added after this plan was first drafted affects the migration shape but not the core module boundary.

New or expanded editor-only areas to move with `MKWorkspace`:

- catalog relayout implementation in `MKWorkspacePieceRelayoutService`
- stable template identity support in `MKWorkspaceStableSlotIdentity`
- relayout impact reporting in `MKWorkspaceRelayoutImpact`
- preflight effect logging in `MKWorkspacePreflightLogger`
- compact workspace packet helpers in `MKWorkspacePacketPayloads`
- chunked workspace piece loading packets:
  - `RequestWorkspacePieceChunkPacket`
  - `WorkspacePieceChunkPacket`
- workspace variant utility modes in `AddWorkspaceVariantsForAllPacket`
- tests covering stable slot identity, preflight logging, chunk/compact packet behavior, and relayout impact reporting

These additions strengthen the authoring/editor side of the split. They should move as part of `MKWorkspace`; they do not change the decision that `MKFloorLayoutSolver` remains in `MKNpc`.

## Module Ownership

### Keep In MKNpc

`MKNpc` owns gameplay runtime and generated-resource consumption.

Keep:

- `world/gen/feature/structure`
- `MKJigsawStructure`
- `MKJigsawPlacement`
- `MKJigsawPieceMetadata`
- `MKDungeonTopologyGroupRule`
- runtime structure settings/codecs
- structure types and pool element types in `MKNpcWorldGen`
- generated NBT structures used by runtime gameplay
- generated worldgen JSON used by runtime gameplay
- generated template pools and structure definitions
- runtime-only model types needed by structure codecs or placement
- `MKFloorLayoutSolver`

`MKFloorLayoutSolver` must stay in `MKNpc` because runtime worldgen currently uses it to solve procedural floor layouts from exported topology settings, root exits, and seed. This is gameplay behavior, not just authoring.

### Move To MKWorkspace

`MKWorkspace` owns authoring, editor workflow, data generation, and export tooling.

Move:

- `MKWorkspaceScreen`
- all pages/previews/draft adapters under `client/gui/screens/workspace`
- workspace client planner contributors
- workspace dev block and block entity
- workspace commands
- workspace editor packets
- workspace editor saved-data attachment/capability if only used by authoring sessions
- workspace import/backup/restore services
- workspace mutation services used only by editor workflows
- workspace scaffold services used only to build authoring layouts
- workspace stairs builders used only to scaffold authoring templates
- canonical workspace planners used to produce authoring pieces
- stable template identity and catalog relayout helpers
- relayout impact/preflight reporting models used by editor confirmation flows
- manifest/export archive writers
- manifest-to-runtime-datapack data-generation builders
- workspace language entries
- workspace blockstate/model/item resources for the dev block

### Move To MKWidgets

Move generic UI controls that are not workspace-specific.

Known candidates:

- `MKIntegerSlider`
- item stack picker widgets
- generic item/block picker wrappers
- generic modal picker composition when not tied to workspace state

`MKWorkspace` should keep screen-specific composition, labels, callbacks, validation messages, and layout decisions. `MKWidgets` should own reusable primitive controls.

## Planner Boundary

The planner split should be based on when the code runs.

### Runtime Planner Code

Keep in `MKNpc`:

- `MKFloorLayoutSolver`
- its result records/enums, unless extracted with it
- minimal model/settings types required by runtime floor solving

This solver is used during live worldgen to choose floor routes, branch paths, link halls, and layout extents. Since layouts remain procedural, it cannot move to `MKWorkspace` without replacing it with another runtime solver or fully materialized generated layouts.

### Data-Generation Planner Code

Move to `MKWorkspace`:

- `MKTowerWorkspacePlanner`
- `MKWalledKeepWorkspacePlanner`
- `MKFloorTopologyPlanner`
- `MKWorkspaceVerticalStackPlanner`
- `MKWalledKeepSizingCalculator`
- `MKWalledKeepSizingReport`
- `MKWorkspaceVerticalStackSizingReport`
- `MKWorkspacePlanner`
- `MKWorkspacePlannerRegistry`
- `MKWorkspacePiecePlanner`
- `MKPlannedPiece`
- `MKPlannedConnector`
- `MKWorkspaceStableSlotIdentity`
- topology schema classes used only by authoring/data generation

These are needed to produce canonical authoring templates and datapack resources, but they do not need to be present in a normal player runtime if the generated resources are already available.

### Runtime Manifest Data

`mk_workspace_exports` should remain as generated runtime metadata and as the stable interchange format between runtime structures and the optional authoring module.

This is intentional, not an intermediate compromise. The metadata gives `MKWorkspace` a way to rehydrate an editable workspace from structures that are already present in a runtime Minecraft install. A user should be able to add `MKWorkspace` to an existing mod list and recover workspace authoring state for any installed structure that ships `data/<namespace>/mk_workspace_exports/*.json`.

`MKNpc` should therefore keep:

- a compact runtime manifest codec/DTO for `mk_workspace_exports`
- passive resource loading for installed runtime manifests
- validation needed to consume runtime structure metadata safely
- no dependency on authoring planners, editor services, workspace saved state, or `MKWorkspace`

`MKWorkspace` should use that runtime manifest metadata for:

- workspace rehydration/import from installed mods and datapacks
- authoring backup/restore workflows
- data-generation inputs and outputs
- richer authoring snapshots layered on top of the runtime-safe manifest when needed

The runtime manifest codec must remain passive. It should parse and expose shipped metadata, but it should not call planner registries, rebuild canonical authoring catalogs, scaffold workspaces, or normalize runtime hints with editor-only planner code.

## Data Generation Boundary

Today, `NpcStructurePools` and `NpcStructures` read workspace export manifests during registry-set generation:

- `NpcStructurePools` loads manifests to bootstrap exported pools.
- `NpcStructures` loads manifests to derive floor topology rules, room footprints, hallway footprints, and ending pools.

This can move to `MKWorkspace` because data generation is an authoring/build toolchain.

Target behavior:

1. `MKWorkspace` runs data generation.
2. It reads workspace authoring/export manifests.
3. It emits final runtime JSON/NBT resources into the module that owns the generation task.
4. It also emits or preserves `data/<namespace>/mk_workspace_exports/*.json` rehydration metadata.
5. `MKNpc` ships and consumes the generated runtime resources and passive manifest metadata during gameplay.
6. A player does not need `MKWorkspace` installed unless they want authoring/rehydration tools.

Generated resources should not be hard-wired to either `MKNpc` or `MKWorkspace`. They should be written into the module that is generating and packaging that content. If `MKWorkspace` generates core `MKNpc` structures as part of the current build, that build wiring can copy or publish the generated output into the `MKNpc` artifact. If another content module generates structures, that module owns its generated resources and should still include `mk_workspace_exports` metadata for rehydration.

## Refactor Phases

### Phase 1: Create MKWorkspace Module

- Add `include 'MKWorkspace'` to `settings.gradle`.
- Add `MKWorkspace/build.gradle`.
- Set `MKWorkspace` dependencies on `MKNpc`, `MKWidgets`, and any existing shared modules needed by editor workflows.
- Add `mk_workspace_version` and metadata properties.
- Add `MKWorkspace` mod metadata template with dependency on `mknpc`.
- Use a distinct mod id, likely `mkworkspace`.
- Create `com.chaosbuffalo.mkworkspace.MKWorkspace`.

Acceptance:

- Empty `MKWorkspace` module compiles.
- `MKNpc` still compiles and runs without referencing `MKWorkspace`.

### Phase 2: Move Generic Widgets To MKWidgets

- Move `MKIntegerSlider` from `MKNpc` to `MKWidgets`.
- Move item stack picker widgets to `MKWidgets`.
- Move or wrap any generic creative picker controls that are still local to `MKNpc`.
- Update imports in existing `MKNpc` screens while the workspace UI still lives there.
- Keep workspace-specific picker rows and callbacks in workspace screens for now.

Acceptance:

- `MKNpc` compiles against the new `MKWidgets` widget paths.
- Existing workspace UI behavior is unchanged.
- `MKWidgets` does not depend on `MKNpc`.

### Phase 3: Isolate Runtime Model Types In MKNpc

Create a clear runtime package for types used by live structure placement and generated runtime data.

Candidate package:

```text
com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout
```

Move or rename runtime-owned workspace floor topology model types gradually. Retained runtime layout types include:

- floor topology settings used by `MKDungeonTopologyGroupRule`
- floor room profile/kind used by `MKFloorLayoutSolver`
- horizontal exit path kind
- family horizontal exit definition

Planned floor layout renames:

| Current class | Runtime class |
| --- | --- |
| `MKFloorLayoutSolver` | `MKFloorLayoutSolver` |
| `MKWorkspaceFloorTopologySettings` | `MKFloorTopologySettings` |
| `MKWorkspaceFloorRoomKind` | `MKFloorRoomKind` |
| `MKWorkspaceFloorRoomProfile` | `MKFloorRoomProfile` |
| `MKWorkspaceFloorLinkGenerationMode` | `MKFloorLinkGenerationMode` |
| `MKWorkspaceHallwayLeadInMode` | `MKHallwayLeadInMode` |
| `MKWorkspaceHorizontalExitPathKind` | `MKHorizontalExitPathKind` |
| `MKWorkspaceFamilyHorizontalExitDefinition` | `MKFamilyHorizontalExitDefinition` |

The editor-only topology preflight classes should not move into this runtime package:

- `MKWorkspaceFloorTopologyInvalidationAnalyzer`
- `MKWorkspaceFloorTopologyMutationPreflightService`

They belong with `MKWorkspace` authoring workflow unless a later pass finds a runtime dependency.

Avoid moving editor-only fields into the runtime package. If a current model type mixes runtime and authoring concerns, split it.

Acceptance:

- `world/gen/feature/structure` no longer imports broad `world.gen.workspace` packages except possibly during a temporary migration.
- `MKFloorLayoutSolver` depends only on runtime-safe settings/profile classes.

### Phase 4: Split Workspace Export Manifest

Separate manifest responsibilities:

- Runtime manifest parsing stays in `MKNpc`.
- Authoring snapshot/export/archive writing moves to `MKWorkspace`.

Recommended split:

- `MKNpc`: compact runtime export manifest DTO/codecs used to load generated runtime metadata and support later workspace rehydration.
- `MKNpc`: passive resource loader for `data/<namespace>/mk_workspace_exports/*.json`.
- `MKWorkspace`: full workspace snapshot/export/archive writer that knows about `MKStructureWorkspace`, planned pieces, backups, import, and editor state.
- `MKWorkspace`: rehydration service that consumes the runtime manifest DTOs exposed by `MKNpc`.

Acceptance:

- Runtime manifest loading does not depend on workspace planners.
- Authoring export can still write everything needed by data gen.
- Adding `MKWorkspace` to an existing runtime install can discover shipped `mk_workspace_exports` metadata.

### Phase 5: Move Data-Generation Builders To MKWorkspace

Move manifest-to-runtime generation code into `MKWorkspace`.

Candidates:

- `ExportedWorkspacePoolBootstrap`
- manifest loading from mod source for data gen, distinct from the passive runtime resource loader
- generated pool creation from workspace manifests
- generated structure rule derivation currently embedded in `NpcStructures`

`MKNpc` should retain static/runtime structure bootstrap code plus passive runtime manifest loading. It should not retain data-generation builders that require authoring planners.

Acceptance:

- Running data gen with `MKWorkspace` produces the same runtime resources currently generated by `MKNpc`.
- Running/compiling `MKNpc` does not require authoring planners.
- Generated output still includes `mk_workspace_exports` metadata for rehydration.

### Phase 6: Move Authoring Planners To MKWorkspace

Move canonical template planners and authoring schema types:

- planner registry
- planner interfaces
- tower planner
- walled keep planner
- floor topology planner
- vertical stack planner
- planned piece/connector records
- stable template identity helpers
- sizing reports/calculators
- topology schemas used by editor/catalog generation

Keep only `MKFloorLayoutSolver` and its runtime input/output model in `MKNpc`.

If data-generation code still needs planner-specific runtime path filtering, replace the current planner dependency with a small data-gen policy interface inside `MKWorkspace`.

Acceptance:

- `MKNpc` gameplay/runtime code does not import canonical authoring planners.
- `MKWorkspace` can still produce the same planned pieces and exports.

### Phase 7: Move Editor Backend Services To MKWorkspace

Move editor-only server workflows:

- workspace creation/update service methods
- workspace saved state/capability
- mutation services
- catalog relayout services and impact reporting
- preflight effect logging
- block swap service
- piece relayout service
- identity rename service
- margin expansion service
- hallway regeneration planner if editor-only
- template binding/block diff services
- import/backup/restore services
- scaffold builder
- stair builder

If any of these are needed by data gen but not gameplay, they still belong in `MKWorkspace`.

Acceptance:

- `MKNpcAttachments` no longer registers `structure_workspace_data`.
- `MKNpc` no longer has workspace authoring state attached to levels.

### Phase 8: Move Dev Block, Commands, Packets, UI

Move the visible editor surface:

- `MKWorkspaceDevBlock`
- `MKWorkspaceDevBlockEntity`
- workspace block/item/entity registration
- workspace commands
- workspace packets and packet handler
- compact workspace payload helpers
- workspace piece chunk request/response packets
- workspace screen and pages
- workspace client planner contributors
- workspace language provider entries
- workspace resources under `assets/<modid>`

Remove from `MKNpc`:

- `WorkspacePlannerClientRegistry.registerBuiltIns()` in client setup
- `MKWorkspaceCommands` registration from `NpcCommands`
- workspace packets from `PacketHandler`
- workspace block/item/entity registrations
- workspace lang keys

Acceptance:

- Installing only `MKNpc` exposes no workspace editor block, commands, UI, or packets.
- Installing `MKNpc` plus `MKWorkspace` restores editor functionality.

### Phase 9: Resource And Compatibility Cleanup

Decide whether workspace resources keep namespace `mknpc` or move to `mkworkspace`.

Recommendation:

- Editor-only assets use `mkworkspace`.
- Runtime generated structures/pools can continue using the target gameplay namespace, often `mknpc`.
- The workspace dev block can move cleanly from `mknpc:mk_workspace_dev` to `mkworkspace:mk_workspace_dev`.
- No world migration or compatibility alias is required for existing developer worlds.

Generated datapack resources should be treated as runtime content, not editor assets.

Acceptance:

- Runtime structures generated by prior data gen still resolve.
- Editor resources are owned by `MKWorkspace`.

## Key Risks

### Runtime Accidentally Depends On MKWorkspace

This is the main failure mode. Use dependency direction and import checks to prevent it.

Guardrail:

- `MKNpc` must not have a Gradle dependency on `MKWorkspace`.
- `MKNpc/src/main/java` must not import `com.chaosbuffalo.mkworkspace`.

### Manifest Code Pulls Authoring Planners Back Into MKNpc

Current `MKWorkspaceExportManifest` calls planner registry logic to normalize runtime hints. That should move out of runtime manifest loading.

Guardrail:

- Runtime manifest parsing should be passive.
- Data gen may normalize/build hints, but gameplay should not need planner registry.

### Runtime Floor Layout Needs More Than Expected

`MKFloorLayoutSolver` uses several floor topology model types. Those types need to be runtime-safe and decoupled from workspace authoring state.

Guardrail:

- Keep runtime settings/profile classes small and codec-friendly.
- Do not let them depend on workspace saved state, planner registry, UI, or mutation services.

### Data Gen Output Changes

Moving data-gen ownership can accidentally change generated pool ids, structure ids, or tags.

Guardrail:

- Snapshot generated resources before the move.
- Diff generated JSON/NBT after each phase.
- Add tests around key exported pool ids and structure ids.

## Verification Checklist

For `MKNpc` alone:

- `:MKNpc:compileJava`
- `:MKNpc:test`
- `:MKNpc:runData` if runtime data gen remains in `MKNpc` during intermediate phases
- Client/server launch without `MKWorkspace`
- No workspace editor command is registered
- No workspace dev block/item is registered
- Runtime structures still generate
- Procedural floor layouts still work

For `MKWorkspace` installed with `MKNpc`:

- `:MKWorkspace:compileJava`
- `:MKWorkspace:test`
- `:MKWorkspace:runData`
- Workspace dev block opens the editor
- Workspace create/generate/export/import flows work
- Workspace packets sync correctly
- Workspace commands work
- Generated datapack resources match previous output or expected intentional diffs

For dependency hygiene:

- `MKNpc` has no dependency on `MKWorkspace`.
- `MKWidgets` has no dependency on `MKNpc` or `MKWorkspace`.
- Runtime packages in `MKNpc` do not import editor packages.
- Editor packages in `MKWorkspace` may import runtime APIs from `MKNpc`.

## Suggested Implementation Order

1. Add empty `MKWorkspace` module.
2. Move generic widgets to `MKWidgets`.
3. Extract and rename runtime floor topology model used by `MKFloorLayoutSolver` into `world/gen/structure/runtime/layout`.
4. Make runtime manifest loading passive and planner-free in `MKNpc`.
5. Move data-gen manifest builders to `MKWorkspace`.
6. Move authoring planners to `MKWorkspace`.
7. Move workspace backend authoring services to `MKWorkspace`.
8. Move dev block, commands, packets, and UI to `MKWorkspace`.
9. Remove all remaining workspace editor registrations from `MKNpc`.
10. Run full compile/test/data-gen verification for both module combinations.

## Resolved Decisions

- Generated resources belong to the module that generates and packages them. `MKWorkspace` can generate resources for `MKNpc`, but the build should move or publish those resources explicitly rather than having runtime code depend on editor modules.
- Existing developer worlds do not need migration support for the workspace dev block. The block can move from `mknpc:mk_workspace_dev` to `mkworkspace:mk_workspace_dev`.
- Runtime floor topology classes should drop the `MKWorkspace` prefix while they are extracted.
- Runtime floor layout classes should live under `world/gen/structure/runtime/layout`.

## Acceptance Criteria

- A player can install `MKNpc` without loading workspace editor UI, workspace authoring commands, workspace dev blocks, workspace packets, or authoring planners.
- A builder/developer can install `MKNpc` plus `MKWorkspace` and retain the full workspace editor and data-generation workflow.
- Procedural runtime floor layout remains in `MKNpc` and behaves the same.
- Generated runtime structures and datapacks still load without `MKWorkspace`.
- `mk_workspace_exports` metadata remains shipped runtime data and can be used by `MKWorkspace` to rehydrate installed structures.
- Shared widgets are available from `MKWidgets` for other modules.
