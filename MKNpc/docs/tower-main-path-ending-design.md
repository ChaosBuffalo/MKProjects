# Tower Main Path Ending Design

## Goal

Add optional category-scoped main path ending support to tower workspaces.

The feature should let a workspace author define special room families that can close the horizontal main path for a tower category, without making that behavior mandatory for existing workspaces. If a category does not define any main path ending families, generation should continue to use the current main path behavior.

In this document, "per floor" means per `MKTowerWorkspaceCategory`, not per numeric floor index.

## Current Model

Tower workspace generation currently separates authoring from runtime generation:

- Workspace families define authored room/hallway templates.
- Horizontal exits define authored openings and connector behavior.
- Planner output creates `MKPlannedPiece` instances with jigsaw connectors and runtime tags.
- Export builds runtime pools from saved piece connector metadata.
- `MKDungeonLayoutController` accepts or rejects candidate children using `MKDungeonPieceState` and `MKJigsawPieceMetadata`.

The existing runtime controller already tracks:

- `progressionFloorIndex`
- `verticalLevelIndex`
- `piecesOnFloor`
- `branchDepth`
- `onMainPath`
- `targetFloors`

It does not currently track "main path pieces within this tower category".

## Definitions

### Main Path Continuation

A normal main path family piece that can continue horizontal main path generation within a category.

This is the existing behavior for main path compatible rooms and hallways.

### Main Path Ending

A special main path compatible family piece that satisfies and closes the horizontal main path for its category.

A main path ending is not the same as a terminal tower piece. It ends the horizontal main path for the current category. It may contain vertical connectors, but it does not have to. Vertical progression can be handled by another piece in the generation path.

### Category

The existing `MKTowerWorkspaceCategory` value attached to a family:

- `ENTRY`
- `MAIN`
- `TOP_CAP`
- `BASEMENT`
- `BASEMENT_CAP`

Main path endings are grouped and selected by this category.

## Desired Behavior

For each category, generation should be able to answer two capability questions:

```java
hasMainPathContinuations(category)
hasMainPathEndings(category)
```

The category behavior should be:

| Continuations | Endings | Behavior |
| --- | --- | --- |
| yes | yes | Generate continuations until the category main path target is reached, then route to an ending. |
| yes | no | Use current main path generation behavior. |
| no | yes | Route a main path exit directly to an ending. The ending may be the only horizontal main path piece for that category. |
| no | no | Use current fallback behavior for the connector, such as normal target pool resolution or `minecraft:empty`. |

This makes the feature fully optional and allows compact categories that only define an ending piece.

## Authoring Model

Main path ending status is inferred from the family's horizontal exit definitions.

Add a new `MKWorkspaceHorizontalExitPathKind` value:

```java
MAIN_ENDING_ENTRY
```

A family is a main path ending candidate when it defines at least one horizontal exit with:

```java
exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY
```

This keeps ending behavior in the same topology model as `MAIN_ENTRY`, `MAIN_EXIT`, and `BRANCH`. It avoids adding a separate family-level boolean and makes the authored opening itself describe how the piece participates in the main path.

For the first implementation, do not support a corresponding main ending exit. `MAIN_ENDING_ENTRY` is inbound-only. It means "this family can receive the main path and close the category's horizontal main path."

### Family Semantics

A main path ending family:

- belongs to exactly one `MKTowerWorkspaceCategory`,
- defines at least one `MAIN_ENDING_ENTRY` horizontal exit,
- should not define a `MAIN_EXIT` in the first implementation,
- may expose branch exits,
- may expose vertical connectors, but is not required to,
- may also be terminal for cap-style categories if appropriate.

`MAIN_ENDING_ENTRY` should map to the same connector role directionality as an inbound main path connector. In practice, it behaves like `MAIN_ENTRY` for jigsaw role pairing, but it contributes the family to the category's ending pool instead of the normal main continuation pool.

## Pool Model

Export should build category-scoped ending pools:

```text
<namespace>:<structure>/main_endings/<category>
```

Examples:

```text
mknpc:test_tower/main_endings/entry
mknpc:test_tower/main_endings/main
mknpc:test_tower/main_endings/basement
mknpc:test_tower/main_endings/top_cap
mknpc:test_tower/main_endings/basement_cap
```

These pools contain only authored variants whose family has a `MAIN_ENDING_ENTRY` exit and whose category matches the pool category.

Normal main path pieces continue to use existing room/hallway pools.

## Routing Model

The planner/export layer should route inbound ending openings into category ending pools.

`MAIN_ENDING_ENTRY` is not a connection mode. It is a path kind. The connection mode can remain `HALLWAY`, `DIRECT_ROOM`, or `NO_CONNECTION` for the physical connector behavior, while the path kind defines the topology role.

For runtime pool construction, a family with `MAIN_ENDING_ENTRY` contributes its base name to:

```text
main_endings/<category>
```

All main path exits should be eligible to route to the category ending pool once the controller has determined that the category can end. This should not require an authored opt-in on each exit beyond it being a main path exit.

For now, this can be handled by planner/export logic rather than a new connection mode. The controller decides when ending candidates are valid; the pool gives it the category-specific ending candidates.

## Selection Rules

The runtime controller should not blindly accept ending pieces. It should apply category-aware rules.

At generation time, determine the category for the candidate child from metadata. The parent state should know the active category path context.

Recommended state additions:

```java
MKTowerWorkspaceCategory category
int mainPathPiecesInCategory
int mainPathTargetForCategory
```

The target can be chosen when entering a category:

```java
mainPathTargetForCategory = randomBetween(categoryMinMainPathPieces, categoryMaxMainPathPieces);
```

These min/max values should live on the category profile. They are only meaningful when the category has normal main path continuation pieces. If a category has ending pieces but no continuation pieces, direct-to-ending behavior is inferred and the UI should not show category path min/max controls for that category.

Candidate filtering:

```java
if (!parentState.onMainPath()) {
    reject mainPathEnding;
}

if (!hasMainPathContinuations(category) && hasMainPathEndings(category)) {
    allow mainPathEnding;
}

if (hasMainPathContinuations(category) && !hasMainPathEndings(category)) {
    allow normal current behavior;
}

if (hasMainPathContinuations(category) && hasMainPathEndings(category)) {
    if (parentState.mainPathPiecesInCategory() < parentState.mainPathTargetForCategory()) {
        allow continuations;
        reject endings;
    } else {
        reject continuations;
        allow endings;
    }
}
```

The key fallback rule:

Do not reject normal main path continuation candidates just because a target was reached unless a compatible ending is available for that category.

Direct-to-ending behavior is valid:

```java
if (!hasMainPathContinuations(category) && hasMainPathEndings(category)) {
    allow mainPathEnding;
    reject normalMainPathContinuation;
}
```

This lets a category with no normal main path pieces still connect a main path exit directly to an ending piece.

Main path exits remain eligible to route to endings once the category target is reached. The ending decision is made by category/path state, not by a special per-exit authoring flag.

## Metadata Changes

Extend `MKWorkspaceRuntimePieceInfo` or exported piece metadata with:

```java
MKTowerWorkspaceCategory category
boolean mainPathEnding // derived from MAIN_ENDING_ENTRY
```

Extend `MKJigsawPieceMetadata` similarly:

```java
MKTowerWorkspaceCategory category
boolean mainPathEnding // derived from MAIN_ENDING_ENTRY
```

If we do not want structure generation code to depend on workspace categories directly, use serialized strings:

```java
String progressionCategory
boolean mainPathEnding
```

The workspace planner already writes category tags. The ending flag can be derived from the serialized horizontal exit summary or from a helper on `MKTowerWorkspaceFamilyDefinition`.

Suggested helper:

```java
public boolean mainPathEnding() {
    return horizontalExits.stream()
            .anyMatch(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY);
}
```

## Workspace Validation

Validation should be opt-in and non-blocking for old workspaces.

Errors:

- A family defines more than one `MAIN_ENDING_ENTRY`.
- A family defines `MAIN_ENDING_ENTRY` and `MAIN_EXIT` in the first implementation.
- A `MAIN_ENDING_ENTRY` exit uses a vertical direction.
- A main path ending pool target is used for a category with no compatible `MAIN_ENDING_ENTRY` families.

Warnings or soft validation:

- A category has ending families but no normal main path continuation families.
  - This is valid if the category is intended to route directly to an ending.
- A main path ending has no vertical connector in a category that normally needs vertical progression.
  - This is valid if another piece in the category/generation path provides the vertical transition.
- A main path ending has branch exits.
  - This is valid.

## UI Changes

Family editor:

- Show category in the family summary.
- Surface whether the family contributes to:
  - normal main path pool,
  - main path ending pool,
  - branch pool.

Horizontal exit editor:

- Add `Main Ending Entry` as a path kind choice.
- Do not add a main ending exit path kind in the first implementation.
- Show the resolved pool:

```text
main_endings/<category>
```

Category/profile editor:

- Add minimum main path pieces per category.
- Add maximum main path pieces per category.
- Hide these controls when the category has no normal main path continuation families and has at least one main path ending family. In that case, direct-to-ending behavior is inferred.

## Export Changes

Export should add runtime pools for each category that has at least one main path ending:

```json
{
  "base_name": "main_endings/main",
  "pool_id": "mknpc:test_tower/main_endings/main",
  "child_base_names": [
    "main_dead_end_room",
    "main_treasure_end"
  ]
}
```

The export manifest should remain valid if this list is empty.

Workspace-exported pools currently do not have authored weights. Exported variants are registered into generated template pools with weight `1`, and `ExportRuntimePool` stores only `child_base_names`. Main ending pools should follow the same rule for now.

## Runtime Generation Changes

`MKDungeonLayoutController` should gain category-aware ending checks.

Required inputs:

- child category,
- child `mainPathEnding` flag,
- whether the category has continuation candidates,
- whether the category has ending candidates,
- current category main path count/target.

This probably means `MKDungeonLayoutSettings` needs a compact category capability map, derived from the exported manifest or static data bootstrap.

Example conceptual API:

```java
settings.categoryRules().hasMainPathContinuation(category);
settings.categoryRules().hasMainPathEnding(category);
settings.categoryRules().mainPathTarget(category, random);
```

## Decisions

1. All normal main path exits are eligible to route to ending pools once the controller decides the category can end.
2. Category profiles should get main path min/max controls.
3. Direct-to-ending categories are inferred from `!hasContinuations && hasEndings`.
4. Main path ending pieces may have branch exits.
5. Ending pools do not need weights in the first implementation because workspace-exported pools do not currently expose authored weights.

## Recommended First Implementation

1. Add `MAIN_ENDING_ENTRY` to `MKWorkspaceHorizontalExitPathKind`.
2. Add `MKTowerWorkspaceFamilyDefinition.mainPathEnding()` derived from `MAIN_ENDING_ENTRY`.
3. Carry `workspace_main_path_ending` into planned piece tags.
4. Carry `main_path_ending` and category into exported runtime metadata.
5. Add exported runtime pools at `main_endings/<category>`.
6. Add validation for `MAIN_ENDING_ENTRY` shape rules, especially no `MAIN_EXIT` support for now.
7. Add controller checks so ending pieces only appear on the main path and only when the category should end.
8. Keep fallback behavior unchanged when no ending exists for a category.

This keeps ending semantics inside the existing horizontal exit topology model, gives stable export output, and remains backward compatible.
