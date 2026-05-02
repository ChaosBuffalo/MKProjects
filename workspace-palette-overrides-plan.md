# Workspace Palette Override Plan

## Purpose

Workspace generation currently has one base structure palette on `MKStructureWorkspace`, while hallway families store their own direct floor, wall, and ceiling block ids. Categories and room families cannot express material intent, so every non-hallway room inherits the same workspace palette unless planned-piece tags are manually populated.

The goal is to let categories, room families, and hallway families optionally override the base workspace palette, while keeping the base palette as the default source of truth.

## Goals

- Add optional palette overrides to tower category profiles.
- Add optional palette overrides to tower room family definitions.
- Convert hallway family material settings into optional palette overrides.
- Resolve palettes consistently through one family-aware precedence:
  1. family override
  2. category override
  3. workspace base palette
- Let each override be partial, so a category can override only walls while inheriting floor and ceiling.
- Preserve codec-based serialization for workspace saves, backup manifests, exports, and imports.
- Support non-destructive material mutation for existing live workspaces where geometry does not change.

## Non-Goals

- Do not introduce per-piece or per-variant palette editing in this pass.
- Do not change stair, slab, or ladder materials; those remain controlled by stair authoring config/detail overrides.
- Do not require existing authored blocks to be scaffold-owned before material substitution. Palette changes should still replace matching user-placed blocks inside the affected piece bounds, consistent with generic block swap behavior.
- Do not clean up old loose export files or old generated resources as part of this feature.

## Current State

Relevant model classes:

- `MKStructureWorkspace` stores the base `MKWorkspaceMaterialPalette`.
- `MKTowerWorkspaceCategoryProfile` stores category dimensions and path count constraints.
- `MKTowerWorkspaceFamilyDefinition` stores room family geometry, role, vertical access support, extrusion mode, and horizontal exits.
- `MKHallwayFamilyDefinition` stores hallway geometry and direct `floorBlock`, `wallBlock`, and `ceilingBlock` values.

Relevant generation flow:

- `MKTowerWorkspacePlanner` creates `MKPlannedPiece` records.
- Room pieces currently get no explicit palette tags.
- Hallway pieces currently get `workspace_palette_floor`, `workspace_palette_wall`, and `workspace_palette_ceiling` tags from `MKHallwayFamilyDefinition`.
- `MKWorkspaceScaffoldBuilder` resolves those planned-piece palette tags and falls back to `workspace.palette()`.
- `MKWorkspaceMarginExpansionService` has parallel palette-tag constants and resolves material state during shell expansion.
- Palette-only form edits currently route through `MKStructureWorkspaceMutationService.swapPalette`, which swaps old base floor/wall/ceiling blocks to the new base palette across all pieces.

The scaffold path already has the right low-level mechanism: generated pieces can carry resolved palette tags. What is missing is a first-class inherited palette model and mutation logic that can reason about old and new resolved palettes per affected piece.

## Data Model

### Partial Palette Override

Add a new model type:

```java
public class MKWorkspacePaletteOverride {
    Optional<ResourceLocation> floorBlock();
    Optional<ResourceLocation> wallBlock();
    Optional<ResourceLocation> ceilingBlock();
}
```

Codec fields:

- `floorBlock`
- `wallBlock`
- `ceilingBlock`

All fields are optional. An empty override means "inherit everything" and should usually serialize as absent at the parent object level.

Suggested helpers:

- `boolean isEmpty()`
- `MKWorkspaceMaterialPalette resolve(MKWorkspaceMaterialPalette fallback)`
- `MKWorkspacePaletteOverride fromResolvedDifference(MKWorkspaceMaterialPalette parent, MKWorkspaceMaterialPalette resolved)`

### Category Profiles

Add:

```java
Optional<MKWorkspacePaletteOverride> paletteOverride;
```

to `MKTowerWorkspaceCategoryProfile`.

Default category profiles should use `Optional.empty()`.

### Family Definitions

Add:

```java
Optional<MKWorkspacePaletteOverride> paletteOverride;
```

to `MKTowerWorkspaceFamilyDefinition`.

Family definitions inherit from their category when they have one. A family override is applied over the resolved category palette. Families without category membership inherit directly from the workspace base palette.

### Hallway Family Definitions

Hallway families should use the same palette override field and resolver path as every other family definition. If we keep `MKHallwayFamilyDefinition` as a separate Java class for now, it should still implement the same palette-bearing family contract used by room families.

If hallway families become entries in a unified family-definition model, they should have `pieceRole = HALLWAY` and no category, unless we later add an explicit hallway category concept. Their fallback chain is therefore family override -> workspace base palette.

Because this feature is still under active development, backward compatibility with the current required hallway `floorBlock/wallBlock/ceilingBlock` fields is optional. If we want a smoother transition, the hallway codec can accept legacy fields and convert them into a full override.

## Palette Resolution Service

Add a small resolver instead of duplicating inheritance rules:

```java
public final class MKWorkspacePaletteResolver {
    MKWorkspaceMaterialPalette resolveCategory(MKStructureWorkspace workspace,
                                               MKTowerWorkspaceCategory category);

    MKWorkspaceMaterialPalette resolveFamily(MKStructureWorkspace workspace,
                                             MKWorkspacePaletteFamily family);

    Optional<MKWorkspaceMaterialPalette> resolvePiece(MKStructureWorkspace workspace,
                                                      MKWorkspacePieceDefinition piece);
}
```

`MKWorkspacePaletteFamily` can be an interface or small adapter record with:

- family id
- optional category
- optional palette override

`resolvePiece` should use piece tags to locate the owning family id and optional category. Hallway pieces can still carry `workspace_hallway_family_id` for diagnostics or pool naming, but palette resolution should not need a separate hallway-specific method. It is useful for mutation and export validation, but scaffold generation should generally resolve from planned family data before creating tags.

Add shared constants for palette tags:

- `workspace_palette_floor`
- `workspace_palette_wall`
- `workspace_palette_ceiling`

Suggested location:

- `MKWorkspacePaletteTags`

This removes the duplicate string constants currently present in planner, scaffold, and margin expansion code.

## Planner And Scaffold Changes

### Planner

For every room family planned piece:

1. Resolve the family palette through `MKWorkspacePaletteResolver`.
2. Compare the resolved palette to the base workspace palette.
3. If any role differs from the base palette, write the resolved value into planned-piece palette tags.

For hallway pieces:

1. Resolve the hallway family palette through the same `resolveFamily(...)` path.
2. Write resolved palette tags only when a role differs from base, or always write them if simpler.

Writing only differing tags keeps piece metadata cleaner. Writing all resolved tags makes exported/runtime hints easier to inspect. The safer first pass is to write all resolved tags for any piece whose resolved palette is not equal to the base palette.

### Scaffold

Keep the current behavior:

- read palette tags from planned piece
- fall back to `workspace.palette()`
- resolve the `ResourceLocation` to a default block state

Change only the constants/import path and ensure all room pieces can now receive tags, not just hallways.

### Margin Expansion

Update `MKWorkspaceMarginExpansionService` to use the same palette tag constants and piece-level resolved palette logic when adding new shell blocks. New shell material should match the piece's resolved palette, not blindly use the base workspace palette.

## Export And Import

### Workspace Manifest

Extend export settings:

- `ExportCategoryProfile` gets optional `palette_override`.
- `ExportFamilyDefinition` gets optional `palette_override`.
- `ExportHallwayFamily` gets optional `palette_override`.

The base `palette` field remains unchanged.

All new fields must be serialized through codecs. Do not add direct Gson parsing or ad hoc JSON reads.

### Import

`MKStructureWorkspaceImportService` should map exported overrides back into:

- `MKTowerWorkspaceCategoryProfile`
- `MKTowerWorkspaceFamilyDefinition`
- `MKHallwayFamilyDefinition`

When importing a hallway without an override, it inherits the base workspace palette.

### Runtime Export

Exported piece NBT is written fresh from the live workspace, so palette overrides only affect newly generated or explicitly mutated live blocks. Runtime metadata should carry resolved palette tags for diagnostics, but structure generation in-world does not need palette resolution at runtime because the blocks are already captured in piece NBT.

## Non-Destructive Mutation Behavior

Changing palette override metadata is safe when piece topology and bounds are unchanged. The mutation should behave like a scoped palette swap.

### Resolved Palette Diff

For each existing piece:

1. Resolve the old effective palette.
2. Resolve the new effective palette.
3. Build replacements for changed roles:
   - old floor -> new floor
   - old wall -> new wall
   - old ceiling -> new ceiling
4. If no replacements are needed, skip the piece.
5. Run `MKWorkspaceBlockSwapService` inside that piece's export bounds, excluding connector blocks as today.

This handles all cases:

- base palette changes affect only pieces that inherit the changed roles
- category override changes affect families in that category unless a family overrides that role
- family override changes affect only pieces for that family
- hallway family override changes affect only pieces for that hallway family

### User-Placed Blocks

The replacement should include user-placed blocks that match the old resolved palette blocks inside the affected piece bounds. This is consistent with current block swap expectations.

### Backup

Before applying any scoped palette override mutation, write a backup archive using the existing backup manifest writer.

Suggested operation names:

- `palette-override-swap`
- `category-palette-swap`
- `family-palette-swap`
- `hallway-palette-swap`

### Metadata Update

After block replacement:

- update the live workspace model
- update piece tags for affected pieces to reflect the new resolved palette tags
- refresh signs only if the screen currently surfaces material summaries there later
- do not regenerate piece geometry
- do not export files automatically

## UI Plan

### Materials Page

Keep the base workspace palette editor on the Materials page.

Add entry points for override editing:

- `Category Palettes`
- `Family Palettes`
- `Hallway Palettes`

Each override editor should show inherited values and explicit values separately:

- Floor: inherited `minecraft:smooth_stone` or explicit selected block
- Wall: inherited `minecraft:stone_bricks` or explicit selected block
- Ceiling: inherited `minecraft:smooth_stone` or explicit selected block

Controls:

- `Choose` opens the existing block picker modal.
- `Use Inherited` clears that role from the override.
- `Clear Override` clears all three roles.

### Category Page

When editing a category, include a compact palette override section after geometry/path settings. It should not crowd the main category list.

### Family Page

When editing a family, include a compact palette override section after family geometry and exits. Show the resolved category palette as the inherited source.

### Hallway Page

Replace the current direct text fields for floor, wall, and ceiling block ids with the same override rows used elsewhere. Hallway inherited source is the base workspace palette.

## Validation

Model validation should verify:

- override block ids exist in `BuiltInRegistries.BLOCK`
- override fields are allowed to be empty
- family overrides do not require a category override
- hallway overrides do not require category membership

Validation should report the parent object in the error message:

- `category main palette wall block is not registered: ...`
- `family floor_main palette floor block is not registered: ...`
- `hallway family branch palette ceiling block is not registered: ...`

## Tests

Add unit tests for:

- empty override resolves to parent palette
- partial override preserves inherited roles
- category override applies to families in that category
- family override beats category override for only the roles it defines
- hallway family override uses the same family resolver and resolves against the base workspace palette when it has no category
- planner writes expected palette tags for category/family/hallway resolved palettes
- scoped palette mutation does not affect a family that overrides the changed base role
- export/import round trips override fields through codecs

Where Minecraft bootstrap makes direct `BlockState` tests awkward, keep tests at the `ResourceLocation` and tag level.

## Implementation Phases

### Phase 1: Model And Resolver

- Add `MKWorkspacePaletteOverride`.
- Add optional override fields to category profiles and the shared family definition contract.
- Add `MKWorkspacePaletteResolver`.
- Add shared `MKWorkspacePaletteTags` constants.
- Update constructors, defaults, normalization, and form draft copy logic.
- Compile.

### Phase 2: Generation

- Update `MKTowerWorkspacePlanner` to resolve and tag room family palettes.
- Update hallway planning to use the same family resolver instead of direct hallway block fields.
- Update `MKWorkspaceScaffoldBuilder` and `MKWorkspaceMarginExpansionService` to use shared constants and resolved tags.
- Add planner/tag tests.

### Phase 3: Export And Import

- Extend `MKWorkspaceExportManifest` category, family, and hallway records.
- Update `MKStructureWorkspaceImportService`.
- Update backup manifest behavior if it depends on export manifest records.
- Add codec round-trip tests.

### Phase 4: UI

- Add reusable palette override rows using the existing block picker modal.
- Replace hallway direct block id text fields.
- Add category and family override controls.
- Keep base palette editing unchanged.
- Compile `:MKWidgets:compileJava :MKNpc:compileJava`.

### Phase 5: Non-Destructive Mutation

- Add a scoped resolved-palette diff service.
- Route override-only edits through backed-up block substitution.
- Update live workspace metadata and affected piece tags after the swap.
- Keep geometry-affecting category/family/hallway edits on the existing regenerate/relayout paths.
- Add focused tests for resolved diff scoping.

## Risks

- Base palette changes become more subtle because some pieces may inherit a role while others override it. The UI should show the resolved value so users can predict which pieces will change.
- Hallway family migration touches both model and UI because hallway materials are currently required direct fields.
- Scoped mutation needs old and new resolved palettes for the same piece identity. If a family base name changes at the same time, treat the operation as destructive or require a separate rename-aware path.
- Piece tags must stay synchronized after non-destructive override changes, or later margin expansion and diagnostics may use stale material tags.

## Recommended First Commit Scope

Start with the model/resolver and planner tagging only:

- `MKWorkspacePaletteOverride`
- resolver and shared tag constants
- optional override fields on category profiles and the shared family contract
- planner/scaffold updates
- unit tests for resolution and planned tags

Then follow with separate commits for export/import, UI, and non-destructive mutation. This keeps serialization and live-world mutation risk isolated.
