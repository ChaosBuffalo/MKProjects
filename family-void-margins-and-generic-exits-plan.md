# Family Void Margins and Generic Exits Plan

## Goal

Move top and bottom void margins out of tower category profiles and onto individual family definitions. Void margins are a property of a specific template family because they are only valid when that family does not need vertical access through the affected volume.

This also prepares the workspace model for a cleaner generic exit system where horizontal exits and vertical access points are declared through the same family-level mechanism.

## Current Problem

Category profiles currently describe top and bottom void margins. That is too broad because a category can contain multiple families with different access needs:

- A main-floor shaft room needs vertical access and cannot safely use vertical void margins.
- A decorative or side-room family in the same category may have no vertical access and should be able to reserve top or bottom void space.
- Cap pieces can also vary: a cap with vertical access continuation should not allow margins, while a non-shaft cap shell can.

The current model also splits exit declaration:

- Horizontal exits are explicitly declared per family.
- Vertical access is inferred from `pieceRole` and `supportsVerticalAccess`.

That makes it harder to reason about what connectors a template actually has.

## Desired Model

`MKTowerWorkspaceFamilyDefinition` should own the template-specific shape and connection intent:

- `roomWidth`
- `roomLength`
- `roomHeight`
- `topVoidMargin`
- `bottomVoidMargin`
- exits, eventually replacing `horizontalExits` and `supportsVerticalAccess`

`MKTowerWorkspaceCategoryProfile` should remain category-level policy:

- category
- default width and length
- full height or max height
- main path min and max
- branch cap limit
- palette override

Void margins should be removed from category profiles.

## Validation Rules

For the current intermediate model:

- If `supportsVerticalAccess == true`, `topVoidMargin` and `bottomVoidMargin` must both be `0`.
- If `supportsVerticalAccess == false`, `roomHeight - topVoidMargin - bottomVoidMargin` must remain at least `MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT`.
- Void margins must be non-negative.
- Horizontal exit vertical offsets must still fit inside the reduced usable shell height.

After generic exits exist:

- Replace `supportsVerticalAccess == true` with `family.hasVerticalExit()`.
- A family with any `UP` or `DOWN` vertical access exit cannot declare top or bottom void margins.

## Planner Changes

The planner should stop reading void margins from category profiles.

Instead, when creating planned room tags:

```text
if family.topVoidMargin > 0:
    tags["workspace_top_void_margin"] = family.topVoidMargin

if family.bottomVoidMargin > 0:
    tags["workspace_bottom_void_margin"] = family.bottomVoidMargin
```

The scaffold layer should keep its defensive guard: if a planned piece still carries vertical-access tags, treat void margins as zero.

## Scaffold Behavior

Keep the fixed-size template behavior:

```text
export height = roomHeight + vertical shell layers
generated shell height = export height - topVoidMargin - bottomVoidMargin
geometry origin Y = export origin Y + bottomVoidMargin
```

Margins should create empty/structure-void bands inside the template bounds, not expand the template bounds.

## UI Changes

Remove top and bottom void margin controls from the category profile page.

Add top and bottom void margin controls to the family detail page:

- Show them when vertical access is disabled.
- Disable or hide them when vertical access is enabled.
- Later, when generic exits replace `supportsVerticalAccess`, show them only when the family has no vertical exits.

For the generic exit follow-up, expand the existing horizontal exit visualization into a six-face exit editor:

- Extend `MKBranchExitMaskWidget` or replace it with a generic family exit widget.
- Support `north`, `south`, `east`, `west`, `up`, and `down` faces.
- Keep the current interaction model where a face can be selected for editing and toggled on or off.
- Represent top and bottom distinctly, either as top/bottom controls around the room diagram or as face tabs paired with the diagram.
- Use the same widget for horizontal exits and vertical access declarations so designers have one place to define the template's connector intent.
- Show vertical-specific fields for top/bottom faces, such as vertical exit kind, shaft/connector profile, cap link behavior, and terminal behavior.
- Disable or hide void margin controls whenever either top or bottom vertical exit is present.

## Export and Import

Update workspace and manifest codecs:

- Remove `top_void_margin` and `bottom_void_margin` from category profile export.
- Add `top_void_margin` and `bottom_void_margin` to family definition export.
- Add matching fields to `MKTowerWorkspaceFamilyDefinition` codec/NBT.

No legacy migration is needed while the system is still under construction.

## Generic Exit Follow-Up

After void margins live on families, introduce a generic family exit model.

Candidate model:

```text
direction: north | south | east | west | up | down
kind: main_entry | main_exit | branch | branch_cap_entry | vertical_up | vertical_down | cap_link
profile_id: connector/opening profile id
connection_mode: none | hallway | direct_room | vertical_access | terminal
side_offset: face-local horizontal offset
vertical_offset: face-local vertical offset
target_pool_override: optional
incoming_pool_override: optional
```

This would allow each family to explicitly declare every connector it wants, including top-only and bottom-only vertical access.

## Implementation Steps

1. Add `topVoidMargin` and `bottomVoidMargin` to `MKTowerWorkspaceFamilyDefinition`.
2. Remove void margin fields and validation from `MKTowerWorkspaceCategoryProfile`.
3. Move UI controls from `WorkspaceFormCategoriesPage` to `WorkspaceFormFamilyDetailPage`.
4. Update `MKTowerWorkspacePlanner` to write margin tags from family definitions.
5. Update scaffold and margin-relayout code to continue using fixed-size internal void bands.
6. Update workspace export/import manifest records.
7. Update tests for family codec roundtrip, planner tagging, validation, and fixed-size scaffold behavior.
8. Follow with the generic exit model once family-owned margins are stable.
9. Expand the family exit visualization/editor to handle all six faces, including top and bottom vertical access.

## Test Coverage

Add or update tests for:

- family codec roundtrip preserves top and bottom void margins
- category profile codec no longer includes void margins
- planner tags non-shaft families with their family margins
- planner does not tag shaft-enabled families with void margins
- validation rejects void margins on vertical-access families
- validation rejects margins that consume too much room height
- fixed-size scaffold export height does not grow when margins are present
- generic-exit follow-up: up-only, down-only, both, and no-vertical-exit families
- six-face exit widget can select and toggle top and bottom faces
