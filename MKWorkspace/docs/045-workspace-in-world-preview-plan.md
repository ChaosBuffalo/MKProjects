# Workspace In-World Preview Plan

## Goal

Give designers a one-click way to generate a sample assembled structure next to the workspace they are editing.

The preview should answer the authoring question "what will this workspace look like when the runtime generator assembles it?" without requiring designers to export the workspace, register the generated data, or create every runtime variant up front.

This feature should be editor-only and non-destructive:

- keep the workspace dev block anchor fixed,
- do not alter authored template pieces or authored variants,
- do not add preview fallback templates to real runtime generation pools,
- clear and replace only the prior sample preview area,
- use runtime-like assembly behavior wherever possible.

## User Experience

The Utilities page should expose a new button:

- `Generate Sample Structure`

It should also expose a checkbox:

- `Lock Seed`

When `Lock Seed` is enabled, every sample generation for that workspace uses the same persisted preview seed. When it is disabled, each button click uses a new seed and updates the persisted preview state with the seed that was just used.

When clicked:

1. The client sends a server packet for the current workspace anchor.
2. The server validates the workspace.
3. The server resolves the preview seed from the `Lock Seed` setting.
4. The previous sample preview for that workspace is cleared, if present.
5. The server computes the fixed preview placement adjacent to the workspace catalog.
6. A sample assembled structure is generated in world.
7. The player receives a concise success or validation failure message.

The generated sample should sit far enough away from the authoring grid that designers can walk around it without confusing preview blocks with editable workspace pieces. The sample should still be close enough that it is visibly associated with the workspace.

## Current Code Touchpoints

### Utilities UI

`WorkspaceUtilitiesPage` already hosts workspace-wide maintenance actions such as block swap, backups, stair generation, and variant helpers.

Add the preview button there so designers can find it alongside other non-form tools.

### Existing Workspace Generation

`GenerateWorkspacePacket` and `MKStructureWorkspaceService.generateWorkspace` currently regenerate the authoring workspace itself. That path rebuilds canonical template pieces through the planner and updates workspace state.

The in-world preview should not reuse that packet directly. It needs a separate packet and service method because the semantics are different:

- authoring regeneration changes workspace pieces,
- sample preview places disposable output beside the workspace.

### Runtime Export Model

`MKWorkspaceExportManifest` already separates template pieces from runtime pieces. Runtime validation intentionally ignores pieces marked as `workspace_piece_kind = template`.

The preview feature should preserve that runtime rule. Template fallback is useful for editor preview only and must be controlled by an explicit preview policy.

## Preview Pool Policy

Runtime generation should keep using authored runtime variants only.

Sample preview generation should use this pool policy:

1. For each runtime base name, use authored variants if any exist.
2. If no authored variants exist for a base name, use the authored template piece as a preview fallback.
3. Mark fallback selections as preview-only in diagnostics or internal metadata.
4. Never write fallback templates into exported runtime pool data.

This gives designers immediate visual feedback while preserving the production contract that real runtime generation excludes templates from variant pools.

## Placement Policy

The preview should be anchored from the workspace bounds, not from the player position.

Recommended first implementation:

- Compute the union of all workspace piece preview/export bounds.
- Place the sample on the opposite X/Z side of the workspace dev block from where the workspace work area is generated.
- Include a fixed walkway gap, for example 16 blocks.
- Align the sample base Y to the workspace anchor Y or the lowest workspace export bound Y.
- Store the generated sample bounding box so repeat clicks can clear the prior sample safely.

Future enhancement:

- Add a "Teleport To Sample" button after generation if the sample is large.

## Cleanup Policy

Preview cleanup must be deterministic and bounded.

Do not scan outward from the workspace looking for matching blocks. Instead:

- Track the last generated sample bounds for each workspace.
- Clear only those bounds plus a small safety margin.
- Clear block entities and temporary markers inside the bounds.
- Refuse to clear if stored bounds overlap the workspace authoring bounds.

Preview state should persist across world reloads. Add preview metadata to workspace capability data, including last sample origin, bounds, seed, lock state, and generated-at time.

Persistent bounds are required because designers will expect the next click after a reload to replace the old sample instead of leaving abandoned preview blocks.

## Server API Shape

Add a new packet:

- `GenerateWorkspaceSamplePacket(BlockPos anchor, boolean lockSeed)`

Packet handler behavior:

1. Require a creative server player.
2. Resolve the workspace from `IMKStructureWorkspaceData` by anchor.
3. Validate with `MKStructureWorkspaceService.validateWorkspace`.
4. Call a new service method, tentatively:
   - `generateSamplePreview(ServerLevel level, BlockPos anchor, boolean lockSeed)`
5. Report validation or generation errors through `MKWorkspaceValidationMessages`.

The service method should return a result object rather than just `Optional<MKStructureWorkspace>`:

```java
public record MKWorkspaceSamplePreviewResult(
        BlockPos origin,
        BoundingBox bounds,
        long seed,
        boolean seedLocked,
        int placedPieceCount,
        int templateFallbackCount,
        List<String> warnings
) {
}
```

The result should make it easy to tell the player whether the sample used real authored variants or preview fallback templates.

## Generation Strategy

### Phase 1: Deterministic Single Sample

Generate one assembled structure using the same planner/runtime layout concepts available to current runtime generation.

For the first pass:

- If `Lock Seed` is enabled, use the persisted preview seed for every generation. If no preview seed exists yet, create one and persist it before generating.
- If `Lock Seed` is disabled, create a fresh seed on each button click and persist the seed used for the latest sample.
- Resolve runtime start piece from existing runtime metadata.
- Build preview pools using the preview pool policy.
- Place the resolved pieces into world at the computed sample origin.
- Record the final bounds.

If runtime placement is too coupled to configured structure generation, add a small editor placement adapter that consumes the same exported piece metadata and places the selected templates directly.

### Phase 2: Runtime Parity

Hook the preview into the resolved runtime plan model described in `039-floor-preview-runtime-parity-plan.md`.

The sample preview should eventually share:

- the same floor layout solver inputs,
- the same locked seed handling,
- the same link pass,
- the same closed-opening decisions,
- the same placement constraints,
- the same failure diagnostics.

The long-term goal is that a locked floor preview, a generated sample, and real runtime generation all agree for the same workspace data and seed.

### Phase 3: Preview Controls

The first version should include:

- `Generate Sample Structure`
- `Lock Seed`

Keep the first version otherwise simple. Template fallback is automatic, placement is fixed, and temporary labels/signs are not part of v1.

## Template Fallback Details

A template fallback should behave as if there were a temporary variant with the same exported NBT as the template, but it should not create a new workspace piece.

Fallback should be automatic. Designers should not need to enable it before a workspace has authored variants. The success message should still report fallback usage so it is clear whether the sample is using production-ready authored variants or editor-only fallbacks.

Important constraints:

- Do not call `AddWorkspaceVariantsForAllPacket`.
- Do not mutate `workspace.pieces()`.
- Do not set the template's `workspace_piece_kind` to `instance`.
- Do not make `MKWorkspaceExportManifest.fromWorkspace` treat templates as runtime pieces globally.

Preferred implementation:

- Introduce a preview-only runtime pool builder.
- It can produce a transient list of pool entries from `MKWorkspacePieceDefinition`.
- It can wrap template pieces as preview candidates in memory.
- It should annotate how many fallback candidates were used.

This keeps the production export/runtime path strict while giving the editor a more forgiving visualization path.

## Validation And Failure Messages

The button should fail early with actionable messages.

Required checks:

- workspace exists at anchor,
- workspace planner validation passes,
- workspace has a runtime start piece or a preview-resolvable start template,
- every required runtime pool has either an authored variant or a preview template fallback,
- computed sample bounds do not overlap the authoring workspace,
- computed sample bounds fit inside build height,
- prior preview bounds are safe to clear.

Example messages:

- `Sample preview failed: workspace did not define a runtime start piece.`
- `Sample preview failed: no variant or template fallback found for pool mkultra:castle/rooms/main/default.`
- `Sample preview generated with 12 authored variants and 5 template fallbacks.`

## Data Model Additions

Add a small preview state record to the workspace capability model:

```java
public record MKWorkspaceSamplePreviewState(
        BlockPos origin,
        BoundingBox bounds,
        long seed,
        boolean seedLocked,
        long generatedAt,
        int placedPieceCount,
        int templateFallbackCount
) {
}
```

Attach it to the workspace by id or anchor.

Store it persistently through `IMKStructureWorkspaceData` beside the workspace record. The preview state is editor metadata, not runtime export metadata.

## Implementation Checklist

### Phase 1: UI And Packet

- Add `GenerateWorkspaceSamplePacket`.
- Register it in `MKWorkspacePacketHandler`.
- Add `Generate Sample Structure` to `WorkspaceUtilitiesPage`.
- Add a `Lock Seed` checkbox to `WorkspaceUtilitiesPage`.
- Gate the packet handler to creative server players.
- Add success/failure chat messages.

### Phase 2: Preview Service

- Add `MKWorkspaceSamplePreviewService` or a focused method on `MKStructureWorkspaceService`.
- Resolve workspace and validate it.
- Compute authoring workspace bounds.
- Compute the fixed sample origin on the opposite X/Z side of the workspace dev block from the work area.
- Resolve and persist seed state from the `Lock Seed` checkbox.
- Clear previous sample bounds.
- Store new preview state after successful generation.

### Phase 3: Preview Pool Builder

- Build transient runtime pools from authored variants.
- Add template fallback only for bases with no authored variant.
- Count and report fallback use.
- Keep this builder out of normal export validation.

### Phase 4: Runtime-Like Placement

- Reuse existing runtime layout and placement logic where possible.
- If direct reuse is blocked, add an editor adapter that consumes runtime/export metadata.
- Place the sample structure at the computed origin.
- Track all placed piece bounds and final union bounds.

### Phase 5: Safety And Tests

- Unit-test pool fallback selection.
- Unit-test sample origin/bounds calculations.
- Unit-test overlap rejection against workspace bounds.
- Add a manual test workspace with no variants to verify template fallback.
- Add a manual test workspace with authored variants to verify templates are excluded when variants exist.
- Verify repeated button clicks replace the old sample instead of accumulating samples.
- Verify locked seed repeats the same sample across repeated clicks and world reloads.
- Verify unlocked seed produces a new sample seed on each click.

## Resolved Product Decisions

- Provide a `Lock Seed` checkbox. Locked generation reuses the same persisted seed; unlocked generation creates a fresh seed per click.
- Persist preview state across world reloads.
- Use a fixed sample origin on the opposite X/Z side of the workspace block from the generated workspace work area.
- Make template fallback automatic.
- Do not add temporary labels or signs in the first implementation.

## Non-Goals

- Do not make templates valid production runtime variants.
- Do not export preview fallback pools.
- Do not mutate authored variants.
- Do not replace the existing workspace regeneration flow.
- Do not introduce a full preview settings UI before the button proves useful.
- Do not add temporary labels/signs in v1.
