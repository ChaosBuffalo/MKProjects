# Mixed + Ladder Vertical Access Plan

## Goal

Add an opt-in vertical access mode that can use mixed stair/slab generation plus a minimal ladder assist when a normal stair or slab profile cannot produce a valid, walkable shaft for the selected dimensions.

This should be a new designer-selected mode. Existing stair, slab, and mixed modes must never introduce ladder assists implicitly.

## Design Shape

Add a new solver strategy:

- `MIXED_LADDER`

The mode should behave as follows:

1. Try the existing mixed stair/slab solver first.
2. If mixed mode resolves successfully, use that normal mixed profile with no ladders.
3. If mixed mode cannot resolve, search ladder-assisted candidates.
4. Select the valid candidate with the fewest ladder blocks.
5. Use existing deterministic tie-breakers after ladder count: broadest option set, visual regularity, then simpler/shorter path.

The ladder assist is a pressure-release valve for large shaft sizes or awkward dimensions where stair blocks cannot gain enough height without forcing an excessively tall band.

## Model Changes

Add a new rise type or solver strategy value:

```java
MIXED_LADDER
```

Extend resolved vertical access data with ladder-assist information:

- `ladderAssistHeight`
- `ladderAssistCount`
- `ladderAssistInsertionIndex`
- `ladderAssistFacing`
- `ladderAssistEntryPos`
- `ladderAssistExitPos`
- `ladderAssistSupportPositions`

The resolved profile should still expose one authoritative result used by generation, validation, export metadata, and debug display.

## Solver Rules

The `MIXED_LADDER` resolver should:

- Prefer a no-ladder mixed solution if one exists.
- Use ladders only when normal mixed resolution fails.
- Minimize total ladder blocks.
- Start with support for one ladder segment.
- Require a walkable landing before the ladder.
- Require a walkable landing after the ladder.
- Require the ladder and support blocks to fit inside the editable shaft area.
- Reject candidates that would collide with protected cap shell blocks.

Multiple ladder segments can be deferred until there is a concrete need.

## Ladder Placement Rules

Ladders must never appear unsupported.

Every ladder rung should be placed against a full wall palette block. For each rung:

- Place the ladder block at the climb position.
- Place a full support block directly behind the ladder.
- Use the workspace palette wall block.
- Ensure the support position is inside the valid editable area.

Generation should add a helper similar to:

```java
planLadderAssistColumn(
    planned,
    ladderBase,
    height,
    facing,
    supportBlock,
    ladderBlock,
    shaftBounds,
    generated
)
```

The helper should own both ladder placement and support-wall placement so unsupported ladders cannot be produced by mistake.

## Placement Constraints

For the first implementation:

- Ladder assist should be placed on a shaft perimeter position.
- Prefer straight path segments over turns.
- Do not place ladder assist inside final top-cap continuation. Top-cap continuation should keep its current behavior.
- Do not place ladder assist through bottom-cap protected shell.
- Keep entry and exit landings on generated walkable surfaces.

## Generation Changes

Add a mixed+ladder generation path in `MKWorkspaceStairBuilder`.

Expected flow:

1. Resolve the selected `MIXED_LADDER` profile.
2. Generate mixed stair/slab path steps normally until the ladder insertion point.
3. Place the ladder entry landing.
4. Place the ladder column and its support wall blocks.
5. Advance the vertical phase by the ladder assist height.
6. Place the ladder exit landing.
7. Continue mixed stair/slab generation after the ladder segment.
8. Record generated ladder and support positions in generated stair metadata.

The generated result should remain deterministic for the same workspace profile, dimensions, and seed-independent settings.

## Validation

Validation should reject a mixed+ladder profile when:

- No valid ladder-assisted solution exists.
- The ladder support block would be outside bounds.
- The ladder climb position would be outside bounds.
- Entry or exit landing cannot be placed.
- The ladder or support would collide with protected cap shell.
- The ladder would pass through non-editable space.

Validation errors should name the affected piece/category profile where possible, consistent with current stair validation messaging.

## UI

Add `Mixed + Ladder` to the same selector where designers choose stair/slab/mixed behavior.

Initial UI can avoid extra numeric controls. The solver should automatically minimize ladder usage.

Possible later controls:

- `Max ladder assist height`
- `Allow multiple ladder assists`

Avoid exposing a manual minimum ladder count. The minimum should be calculated by the solver.

## Debug Metadata

Resolved debug tags should include:

- `resolved_rise_strategy=mixed_ladder`
- `resolved_ladder_assist_height`
- `resolved_ladder_assist_count`
- `resolved_ladder_assist_index`
- `resolved_ladder_assist_facing`
- `resolved_ladder_entry`
- `resolved_ladder_exit`
- `resolved_ladder_support_count`

For now, debug display can show only the selected resolved profile, matching the current direction for stair debug metadata.

## Tests

Add focused tests for:

- `MIXED_LADDER` uses normal mixed resolution when mixed is already valid.
- `MIXED_LADDER` finds a valid minimum-ladder solution when mixed fails.
- Normal `MIXED` mode never uses ladders.
- Ladder generation emits support wall blocks behind every rung.
- Ladder generation rejects out-of-bounds support placement.
- Entry and exit landings are required.
- Cap shell protections are respected.
- Debug metadata records ladder-assist details.

## Decisions

- Keep `MKWorkspaceStairRiseType` named as-is and add the new mixed+ladder strategy there.
- Use the palette wall block as the canonical support block behind ladders.
- Keep top-cap continuation behavior unchanged. Ladder assist does not need special top-cap continuation support in the first implementation.
- Support a single ladder segment for the first implementation.

## Deferred Decisions

- Whether multiple ladder segments are worth supporting after the single-segment version proves useful.
