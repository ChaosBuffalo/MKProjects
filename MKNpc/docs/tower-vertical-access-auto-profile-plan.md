# Tower Vertical Access Auto Profile Plan

## Goal

Move flat run length out of the author-facing workflow and make it a deterministic dependent calculation.

Add a mixed stair + slab rise mode so the solver can find more usable room heights without forcing larger shafts or odd manual flat run choices.

This is a clean refactor. We do not need to preserve authored `flatRunLength` compatibility for existing test towers. Test towers can be regenerated after the model changes.

## Current State

Vertical access authoring currently exposes:

- stair mode
- rise type
- flat run length
- stair width
- block choices

`flatRunLength` is stored in `MKWorkspaceStairAuthoringConfig` and is used by `MKVerticalAccessProfile`.

The current profile model assumes a uniform rise pattern:

- `STAIR`: each rise event climbs one full block.
- `SLAB`: each rise event climbs one half block.
- `flatRunLength`: inserts a fixed number of flat steps after each rise event.

This makes valid heights very sparse. For default tower dimensions, the allowed flat run list can collapse to a single value, usually `0`.

## Desired Authoring Model

Users should author intent, not stair phase mechanics.

User-facing controls should become:

- shaft size
- stair width
- stair mode
- rise strategy
- block choices
- room/category heights

Flat run length should be removed from authored workspace data and computed by a deterministic solver.

## Rise Strategy

Replace or extend the current `MKWorkspaceStairRiseType` shape:

```java
STAIR
SLAB
MIXED
```

`MIXED` means the solver may use both full-block stair rises and half-block slab rises in one vertical access path.

The existing `STAIR` and `SLAB` strategies remain useful because they produce predictable visual styles. `MIXED` should become the default strategy because it offers the broadest set of valid heights.

## Resolved Profile

Introduce a resolved profile type separate from authoring config:

```java
MKResolvedVerticalAccessProfile
```

Potential fields:

- `MKWorkspaceStairMode mode`
- `MKWorkspaceStairRiseType riseStrategy`
- `int stairWidth`
- `int flatRunLength`
- `List<RiseStepKind> risePattern`
- `BoundaryCompatibility boundaryCompatibility`

For uniform `STAIR` and `SLAB`, `risePattern` can be implicit or represented as repeated single-kind steps.

For `MIXED`, `risePattern` should be explicit and deterministic.

Example:

```java
enum RiseStepKind {
    STAIR,
    SLAB_BOTTOM,
    SLAB_TOP,
    FLAT
}
```

The final naming can be adjusted during implementation. The important part is separating authored intent from the exact generated stair path.

## Deterministic Solver

Add a resolver API:

```java
Optional<MKResolvedVerticalAccessProfile> resolve(
        MKWorkspaceStairAuthoringConfig config,
        int shaftWidth,
        int shaftLength,
        int interiorHeight
);
```

The resolver must be deterministic:

- iterate candidates in stable order
- always select the first/best profile by a documented scoring rule
- avoid random choices
- resolve against a shared cross-piece boundary convention unless explicit adjacent boundary compatibility is added later

Suggested scoring order:

1. Prefer the strategy/search branch that yields the most valid height options for the current shaft and stair width.
2. Prefer exact boundary compatibility over bridgeable compatibility.
3. Prefer visually regular patterns over irregular patterns.
4. Prefer lower `flatRunLength` when two profiles are otherwise equivalent.
5. Prefer fewer mixed correction steps when two mixed profiles are otherwise equivalent.

For `STAIR`:

- Search flat run lengths from `0` to a bounded max.
- Accept profiles that pass current boundary compatibility.

For `SLAB`:

- Same as current slab logic, but resolved through the shared solver.

For `MIXED`:

- Search combinations of full-block stair rises and half-block slab rises that reach the target height.
- Try simple repeatable patterns first.
- Use slabs as phase correction when stair-only would fail.
- Prefer visual regularity, but allow irregular patterns when they make a height possible.

The first mixed implementation can be conservative:

- Generate mostly stair rises.
- Insert paired slab steps only when needed to shift phase.
- Prefer less noisy patterns, but do not reject irregularity solely because it is irregular.

## Cross-Category Shaft Continuity

Different categories may have different heights, and each height may need a different internal resolved profile.

That is acceptable only if stacked pieces remain walkable across category boundaries.

The first implementation should use a shared boundary convention:

- Every resolved profile starts from the same canonical bottom landing/phase.
- Every resolved profile ends at the same canonical top landing/phase, or at a boundary state that is explicitly bridgeable by the existing boundary bridge logic.
- A profile is valid only if it satisfies this boundary convention for its piece height.

This lets MAIN, ENTRY, TOP_CAP, BASEMENT, and BASEMENT_CAP resolve different internal patterns while still connecting through a consistent shaft interface.

Longer term, we can replace the canonical convention with explicit boundary compatibility:

```java
ResolvedBoundary bottomBoundary;
ResolvedBoundary topBoundary;
```

Then generation could validate:

```text
previous.topBoundary is compatible with next.bottomBoundary
```

For now, the canonical boundary convention is simpler and safer.

`BRIDGEABLE` boundaries should remain valid. The solver should prefer `EXACT`, but accept `BRIDGEABLE` when it makes a height possible and the generated bridge conforms to the canonical boundary convention.

## Height Selection

Replace "allowed heights for current flat run" with "allowed heights that have a resolved profile."

Current:

```java
getAllowedTowerHeights(stairConfig, shaftSize, minimumHeight, count)
```

Future:

```java
getAllowedTowerHeights(stairAuthoringConfigWithoutFlatRun, shaftSize, minimumHeight, count)
```

Internally this should scan candidate heights and call the resolver for each height.

The UI height slider should only land on heights where `resolve(...)` returns a profile.

## UI Changes

Remove normal flat run length controls from:

- main profile vertical access screen
- per-piece/family stair detail screens, if those screens are meant for normal authoring

Add or keep a debug-only display:

```text
Resolved run: 0
Resolved pattern: stair
```

This helps during development without making flat run a user responsibility.

Add `MIXED` to the rise strategy selector. Keep `STAIR`, `SLAB`, and `MIXED` in the same selector because they are solver strategies, not separate advanced/debug modes.

## Debug Metadata

Persist or display only the selected resolved profile. Do not persist rejected candidate summaries in normal workspace data.

First-pass selected-profile debug fields:

- `resolvedRiseStrategy`
- `resolvedFlatRunLength`
- `resolvedPattern`
- `boundaryStatus`
- `bridgeSteps`
- `topPhase`
- `cycleLength`
- `pathSteps`
- `interiorHeight`

These fields should be enough to debug unresolved heights, unexpected mixed patterns, boundary bridge use, and phase issues.

## Data Refactor

Remove authored `flatRunLength` from `MKWorkspaceStairAuthoringConfig`.

Clean refactor approach:

- Stop decoding `flatRunLength`.
- Stop writing `flatRunLength` in workspace data and export manifests.
- Regenerate the test tower and any local authored test workspaces after the refactor.
- If runtime/generated metadata needs a resolved run or pattern for debugging, write only the selected resolved profile as resolved metadata, not authored config.

The authoring config should contain only user intent:

- mode
- rise strategy
- stair width
- stair block
- slab block
- ladder block

## Stair Builder Changes

`MKWorkspaceStairBuilder` currently chooses between:

- `generateStairSpiral`
- `generateSlabSpiral`

Add a shared generation path based on resolved profile steps, or add:

- `generateMixedSpiral`

The simpler first step is likely:

1. Keep `generateStairSpiral` and `generateSlabSpiral`.
2. Add `generateMixedSpiral`.
3. Refactor common perimeter/landing/bridge code after behavior is stable.

`generateMixedSpiral` should consume the resolved rise pattern and place:

- stair blocks for full-block rise steps
- bottom/top slabs for half-block rise steps
- floor blocks or slab landings for flat steps

## Validation Changes

Validation should stop asking whether authored `flatRunLength` is allowed because `flatRunLength` should no longer be authored.

Instead, validation should ask whether every category height that needs vertical access can resolve a profile:

```text
main category height 7 cannot resolve a vertical access profile for shaft 3, stair width 1, mixed rise
```

Validation should also keep checking:

- shaft size is valid for footprint
- stair width is valid for shaft
- vertical access mode is not incompatible with required tower generation

## Testing Plan

Add tests for:

- Existing default workspace resolves to the same effective stair profile as before.
- Uniform `STAIR` resolver preserves current valid height behavior.
- Uniform `SLAB` resolver preserves current valid height behavior.
- `MIXED` finds at least one height that `STAIR` alone rejects for the same shaft.
- Height lists are deterministic and stable.
- Validation reports unresolved category heights.
- Workspace export/import uses the new clean authoring config shape.

## Suggested Implementation Phases

### Phase 1: Extract Resolver

- Create resolved profile type.
- Move current flat-run search into resolver.
- Keep current UI only temporarily if needed for comparison, but move implementation toward the new config shape.
- Prove no behavior change with tests.

### Phase 2: Auto Flat Run

- Remove flat run from normal authoring controls.
- Remove flat run from authored config.
- Height selection uses resolver.
- Stair generation asks resolver for the profile at generation time.

### Phase 3: Mixed Strategy

- Add `MIXED` rise strategy and make it the default.
- Add mixed resolver search.
- Add mixed stair generation.
- Add focused visual/debug metadata showing only the selected resolved profile.

### Phase 4: Cleanup

- Consolidate stair/slab/mixed generation paths if duplication becomes costly.
- Remove or hide debug-only resolved-profile text once the feature is stable.

## Open Questions

- None currently. The first implementation should use the canonical boundary convention and selected-profile debug metadata listed above.
