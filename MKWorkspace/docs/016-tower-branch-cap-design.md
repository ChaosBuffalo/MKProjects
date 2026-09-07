# Tower Branch Cap Design

## Goal

Add optional branch cap support to tower workspaces.

Branch caps are authored room pieces that can end branch generation cleanly. Branch caps are grouped by horizontal opening profile and are always available during branch generation when a matching branch connector is being resolved.

This feature should not make branch caps mandatory for existing workspaces. If a workspace does not define branch cap pieces, branch generation should continue to use the current `maxBranchDepth` and normal pool exhaustion behavior.

## Current Branch Model

Tower workspace branch generation is currently driven by connector role and opening profile:

- A family horizontal exit with `MKWorkspaceHorizontalExitPathKind.BRANCH` creates a `MKConnectorRole.BRANCH` connector.
- Branch hallway pools are named `hallways/branch/<openingProfileId>`.
- Branch direct-room pools are named `rooms/branch/<openingProfileId>`.
- Exported runtime pools include pieces whose runtime metadata allows branch placement.
- `MKDungeonLayoutController` tracks `branchDepth` and rejects candidates when the next state exceeds `MKDungeonLayoutSettings.maxBranchDepth()`.

Category is already exported in piece metadata, but branch pool selection does not currently use category. Branch choices are effectively scoped by opening profile, not by `MKTowerWorkspaceCategory`.

## Definitions

### Branch Continuation

A normal branch-capable room or hallway piece.

These are the existing pieces selected from branch pools when their metadata allows branch placement.

### Branch Cap

A branch-capable room piece that is intended to terminate a branch.

A branch cap should be terminal for branch growth. It should not create further branch connectors unless we explicitly decide to support cap variants with exits later.

### Branch Cap Pool

A generated runtime pool containing branch cap pieces for a specific opening profile.

Initial pool shape:

```text
branch_caps/<openingProfileId>
```

This mirrors the current branch pool scoping by opening profile.

## Desired Behavior

Branch caps should be optional and always eligible during branch generation.

| Workspace State | Behavior |
| --- | --- |
| No branch caps for opening profile | Existing branch behavior. |
| Branch caps exist for opening profile | Branch cap candidates are added when resolving matching branch connectors. |
| Branch depth below max | Normal branch pieces and branch caps can both be selected. |
| Branch depth reaches the category cap limit | Non-cap branch candidates are rejected and matching branch cap candidates remain eligible. |
| Branch depth exceeds the global max | Existing `maxBranchDepth` still provides the hard cutoff. |

There is no branch minimum and no branch cap target. Branch caps are part of the branch candidate set until the category limit makes them mandatory.

## Category Cap Limit

Each `MKTowerWorkspaceCategoryProfile` exposes:

```java
maxBranchPiecesBeforeCap
```

This is exported to `MKDungeonCategoryRule.maxBranchPiecesBeforeCap`.

Runtime placement should only force a cap when all of these are true:

- The connector being resolved is a branch connector.
- A matching branch cap pool exists for the connector opening profile.
- The parent piece branch depth is greater than or equal to the current category's `maxBranchPiecesBeforeCap`.
- The candidate being considered is not marked `branchCap`.

When those conditions are met, the layout controller rejects normal branch candidates with `branch_cap_required`. Branch cap candidates remain valid as long as they pass the normal structural checks.

The limit is per category profile, defaults to `10`, and is authorable from the category profile UI. The UI control allows integer values from `0` through `10`.

## Authoring Model

Add a new horizontal exit path kind:

```java
MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY
```

A family is considered a branch cap when it has a horizontal exit with `BRANCH_CAP_ENTRY`.

Expected authoring rules:

- A branch cap entry must place a connector, so it should not use `NO_CONNECTION`.
- A branch cap entry should use a normal opening profile.
- A branch cap family should be branch-path compatible.
- A branch cap family should not also define main path entry, main path exit, or main path ending entry behavior.
- A branch cap family should not define additional branch exits in the first pass.

The planner should treat the `BRANCH_CAP_ENTRY` connector as a `MKConnectorRole.BRANCH` connector whose incoming pool is:

```text
branch_caps/<openingProfileId>
```

The target pool can be `minecraft:empty`, matching the main ending pattern where the special entry connector advertises the incoming pool that selects the cap.

## Runtime Metadata

Add branch cap metadata beside the existing main path ending metadata:

```java
MKJigsawPieceMetadata.branchCap
MKWorkspaceRuntimePieceInfo.branchCap
ExportRuntimePieceMetadata.branchCap
```

Branch cap pieces should export with:

- `allow_on_main_path = false`
- `allow_on_branch_path = true`
- `terminal = true`
- `branch_cap = true`

The exact terminal flag can be enforced by validation or derived by the planner. Deriving it is safer for authoring because a branch cap should always stop branch expansion.

## Planner Changes

Update `MKTowerWorkspacePlanner`:

- Add `branchCapPoolName(String openingProfileId)`.
- Map `BRANCH_CAP_ENTRY` to `MKConnectorRole.BRANCH`.
- Emit the special branch cap connector with incoming pool `branch_caps/<openingProfileId>`.
- Mark `roomRuntimeInfo` as `branchCap = true` when the family has a branch cap entry.
- Ensure branch cap families are branch-only and terminal in runtime tags.

The first pass should not create category-specific branch cap pools.

## Export Changes

Runtime pool export currently builds pools by scanning connector incoming pools and adding compatible pieces by base name.

Required changes:

- Treat `branch_caps/<openingProfileId>` as a branch runtime pool.
- Include only pieces marked `branchCap` in branch cap pools.
- Continue to include normal branch pieces in `rooms/branch/<openingProfileId>` and `hallways/branch/<openingProfileId>`.
- Export `branch_cap` in piece metadata.

This keeps branch caps separate from normal branch room pools, which lets runtime placement intentionally inject cap candidates without making every branch room pool contain caps by default.

## Runtime Placement Changes

Update `MKJigsawPlacement.Placer`:

- When resolving a branch connector, ask the layout controller or a helper for a matching branch cap pool.
- Add shuffled candidates from `branch_caps/<openingProfileId>` to the candidate list.
- Add caps alongside existing branch candidates whenever the pool exists.
- Tell the layout controller whether branch caps are available so it can enforce the current category's `maxBranchPiecesBeforeCap`.

The matching opening profile is not currently stored directly on `MKConnectorInfo`, so the simplest implementation may add the branch cap candidates through pool naming at planner/export time rather than through connector metadata. If runtime cannot infer the opening profile from connector info, we should add an explicit connector metadata field instead of parsing pool names.

## Validation

Workspace validation should catch:

- `BRANCH_CAP_ENTRY` with `NO_CONNECTION`.
- More than one `BRANCH_CAP_ENTRY` on a family.
- A family that mixes `BRANCH_CAP_ENTRY` with main path entry, main path exit, or main path ending entry.
- A family that mixes `BRANCH_CAP_ENTRY` with normal branch exits in the first pass.
- A branch cap entry referencing a missing opening profile.

Runtime layout validation should continue to allow workspaces with no branch caps.

## UI Changes

Update the horizontal exit path kind selector:

- Add `BRANCH_CAP_ENTRY` to the cycle.
- Give it a short label distinct from normal branch, for example `C` or `Cap`.
- Use branch coloring unless a dedicated cap color is added.

Update the category profile controls:

- Add a `Branch Cap Max` integer slider.
- Minimum value: `0`.
- Maximum value: `10`.
- Persist the value on `MKTowerWorkspaceCategoryProfile`.

## Tests

Add focused tests for:

- Planner creates `branch_caps/<openingProfileId>` incoming pool for branch cap families.
- Export metadata writes `branch_cap = true`.
- Export runtime pools include branch cap pieces in branch cap pools.
- Normal branch generation remains valid when no branch caps exist.
- Normal branch pieces remain valid at the category cap limit when no matching branch caps exist.
- Normal branch pieces are rejected at the category cap limit when matching branch caps exist.
- Category profile export preserves `maxBranchPiecesBeforeCap`.
- Validation rejects branch cap entries with `NO_CONNECTION`.
- Validation rejects mixed branch cap/main path families.

## Deferred Work

- Category-specific branch caps.
- Author-controlled pool weights.
- Branch cap probability controls.
- Cap variants that intentionally contain secondary branch exits.
