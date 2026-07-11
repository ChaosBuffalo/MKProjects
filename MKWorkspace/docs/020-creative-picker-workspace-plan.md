# Creative Picker Workspace Plan

## Purpose

The current workspace material and block swap screens use `MKPlayerHotbar` as the only block source. This is useful for quick edits, but it forces the user to preload their hotbar before they can choose floor, wall, ceiling, stair, slab, ladder, source, or target blocks.

This plan replaces those inline hotbar pickers with navigation to a dedicated picker subpage. The first implementation should be a block picker backed by creative inventory contents. The design should also leave room for future item pickers and narrower creative subsets without rewriting the workspace screens again.

## Goals

- Let users choose blocks from creative-style categories instead of only the hotbar.
- Make better use of screen space by moving picking to a focused subpage.
- Support scrolling through large modded block catalogs.
- Reuse the same picker flow for workspace materials, block swap, stair detail materials, and future block-selecting tools.
- Keep the picker client-only; final persisted values remain `ResourceLocation` block ids or item ids in existing packets/models.
- Design the picker as a general creative picker framework with block picking as the first content type.

## Non-Goals

- Do not implement server-side creative inventory behavior.
- Do not give or remove items from the player's inventory.
- Do not require the player to be in creative mode.
- Do not support arbitrary non-block items in the first pass.
- Do not replace vanilla creative inventory screens.
- Do not change palette, block swap, or workspace serialization formats.

## Current State

Existing UI pieces:

- `MKWorkspaceScreen` builds material slots in `buildFormMaterialsState`.
- `MKWorkspaceScreen` builds block swap slots in `buildBlockSwapState`.
- `addPaletteSection` and `addBlockSwapPaletteSection` place `MKPlayerHotbar` plus `MKBlockSlot` targets inline.
- `MKPlayerHotbar` renders the first nine player inventory slots and starts `BlockItemDragState` for `BlockItem`s.
- `MKBlockSlot` accepts `BlockItemDragState`, stores a block `ResourceLocation`, and renders the block as an item stack.

The useful existing abstraction is `MKBlockSlot`: it is already a compact value display and drop target. The weak point is `MKPlayerHotbar`: it is a source widget tied to the player's current hotbar instead of a searchable catalog.

## UX Model

### Picker Entry

Every existing inline source picker should become a button or clickable value row:

- Floor block
- Wall block
- Ceiling block
- Stair block
- Slab block
- Ladder block
- Block swap source
- Block swap target
- Category/detail stair overrides
- Hallway palette overrides if exposed later

Each row should show:

- label
- current `MKBlockSlot` preview
- current id or shortened display name
- `Choose` button
- optional `Clear` button when air/empty is valid

Pressing `Choose` pushes a picker subpage.

### Picker Subpage

The picker subpage should consume most of the panel:

- top row: title and current target label, for example `Choose Floor Block`
- search field
- tab/category strip
- scrollable item grid
- bottom actions: `Cancel`, optionally `Clear`

Picking an entry should immediately return to the previous page and update the pending draft value.

Cancel should return without changing the value.

For block swap, selecting source or target should return to the block swap page with only that slot updated.

### Navigation Contract

The workspace screen needs a small "return target" model for picker calls:

- current screen state name
- field being edited
- current selected value
- callback to apply selected value

Recommended flow:

1. User presses `Choose` beside a field.
2. Screen stores a pending picker request.
3. Screen pushes `creative_picker`.
4. Picker shows values from the request.
5. On selection, picker calls the request callback and pops back.
6. Original page rebuilds from the updated draft fields.

This avoids trying to keep live widget references across state rebuilds.

## Picker Framework

Build the picker around three concepts:

### Creative Picker Value

Represents the selected thing independent of UI source.

Initial block implementation:

- value type: `block`
- id: `ResourceLocation`
- display stack: `ItemStack`
- display name: `Component`
- searchable text: id plus display name

Future item implementation:

- value type: `item`
- id: `ResourceLocation`
- optional components or metadata if we ever need exact `ItemStack` identity
- display stack: `ItemStack`

### Creative Picker Source

Provides tabs/categories and entries.

Suggested interface:

```java
public interface MKCreativePickerSource<T> {
    List<MKCreativePickerCategory<T>> categories(Minecraft minecraft);
    List<MKCreativePickerEntry<T>> entries(Minecraft minecraft, MKCreativePickerCategory<T> category, String query);
}
```

The first source should be `MKCreativeBlockPickerSource`.

### Creative Picker Widget/Page

Renders:

- categories
- search field
- scrollable grid
- selection callback

The widget should not know about workspace fields. It should only return a selected value.

## Creative Block Source

The block source should use vanilla creative tab contents rather than manually iterating every block registry entry.

Refresh creative tab contents with:

- `CreativeModeTabs.tryRebuildTabContents(enabledFeatures, hasPermissions, registryAccess)`

Then enumerate:

- `CreativeModeTabs.tabs()`
- `CreativeModeTab#getDisplayItems()`

Filter entries:

- keep only stacks where `stack.getItem() instanceof BlockItem`
- map the `BlockItem` to `BuiltInRegistries.BLOCK.getKey(blockItem.getBlock())`
- skip duplicate block ids within the same displayed result set

Permission handling:

- `hasPermissions` should match creative behavior as closely as possible:
  - `player.canUseGameMasterBlocks()`
- This means operator-only blocks can appear when the current player has permission.

Feature handling:

- use `player.connection.enabledFeatures()` when available
- use `player.level().registryAccess()` for holder lookup

Fallback:

- if player/connection/level is unavailable, show an empty picker with a message and a cancel button

## Categories

Initial categories should mirror creative tabs:

- Building Blocks
- Colored Blocks
- Natural Blocks
- Functional Blocks
- Redstone Blocks
- any modded creative tab with block items

The vanilla search tab can be handled in one of two ways:

1. Use our own `All` category made from all block entries.
2. Use `CreativeModeTabs.searchTab().getDisplayItems()`.

Recommended first pass:

- create an `All` category from deduplicated block entries across visible creative tabs
- then list visible creative tabs after it

Reason:

- Our search behavior is simpler if all entries are available in one list.
- Category tabs still preserve familiar creative grouping.

## Search

First pass search should be local and deterministic:

- lowercase display name
- lowercase registry id
- optional namespace search, for example `minecraft:` or `mknpc:`

Do not depend on vanilla `SessionSearchTrees` in the first pass. The vanilla search tree adds tag and tooltip matching, but it is more coupled to creative inventory internals. We can add it later if local matching is insufficient.

Search should update the displayed grid as the user types and reset scroll to the top.

## Scrolling And Layout

The picker grid should be a dedicated widget instead of hundreds of child widgets.

Recommended widget:

- `MKCreativeGridPicker<T> extends MKWidget`

Why not a child widget per item:

- modded creative catalogs can be large
- a single grid widget can virtualize visible rows
- scrolling and hover hit tests are easier to keep consistent

Grid behavior:

- slot size: 18
- gap: 2
- columns derived from available width
- rows derived from entry count
- scroll offset in rows or pixels
- mouse wheel scrolls the grid
- draw only visible rows
- left-click selects
- optional click-and-drag can set existing drag state later

The picker page should keep fixed regions:

- header height
- search height
- category strip height
- footer button height
- grid takes remaining height

This avoids squeezing the picker into the old palette section area.

## Rendering Details

Each grid cell should render:

- vanilla slot sprite or simple slot frame
- `GuiGraphics.renderItem`
- `GuiGraphics.renderItemDecorations`
- hover highlight
- selected highlight when the entry matches current value

Tooltip:

- on hover, show display name and registry id
- use existing MKWidgets post-render tooltip flow or `GuiGraphics.renderTooltip`

The picker should support blocks whose display item is not constructible from `new ItemStack(block)` by carrying the original creative `ItemStack` from the tab entry.

## Value Display Rows

Add a reusable row builder in `MKWorkspaceScreen`, or a reusable widget if it becomes common enough:

- label text
- `MKBlockSlot` preview
- id/display text
- choose button
- clear button when allowed

For now this can live in `MKWorkspaceScreen` because the row is workspace-specific. The picker itself should live in `MKWidgets` or a neutral client GUI package.

## Workspace Integration Points

### Materials Page

Replace inline `MKPlayerHotbar` and six target slots with rows:

- Floor
- Wall
- Ceiling
- Stair
- Slab
- Ladder

The page should no longer need `addPaletteSection`.

On Back:

- copy draft values from the row model/draft fields exactly as today

### Block Swap Page

Replace inline hotbar with rows:

- Source
- Target

Rows update local `ResourceLocation` values.

`Swap Blocks` continues to send `SwapWorkspaceBlockPacket(anchor, sourceBlock, targetBlock)`.

### Stair Detail Page

Replace the inline palette section at the bottom of category/detail editing with rows or buttons:

- Stair
- Slab
- Ladder

This page already has limited vertical space, so navigation to a picker subpage is especially useful.

## Suggested Class Layout

In `MKWidgets`:

- `client/gui/pickers/MKCreativePickerEntry.java`
- `client/gui/pickers/MKCreativePickerCategory.java`
- `client/gui/pickers/MKCreativePickerSource.java`
- `client/gui/pickers/MKCreativeBlockPickerSource.java`
- `client/gui/widgets/MKCreativeGridPicker.java`

In `MKNpc`:

- workspace picker request model inside `MKWorkspaceScreen`
- `buildCreativeBlockPickerState`
- helper methods for `chooseBlock(...)`
- replacement row helpers for material/block-swap/stair pages

If item picking is added later:

- `MKCreativeItemPickerSource`
- optional `MKItemSlot`
- same `MKCreativeGridPicker<T>`

## Data Flow

Block row state:

1. Row displays current `ResourceLocation`.
2. User presses `Choose`.
3. `MKWorkspaceScreen` stores:
   - picker title
   - current value
   - callback accepting `ResourceLocation`
   - return state
4. `creative_block_picker` state builds from request.
5. User clicks a block entry.
6. Callback updates draft field.
7. Screen pops back to return state.
8. Return state rebuilds and displays updated value.

No packet is sent until the existing page action is pressed:

- material draft submit/back flow
- block swap button
- future utility action buttons

## Future Picker Types

The framework should support these without changing workspace navigation:

- Block picker: returns a block id
- Item picker: returns an item id or `ItemStack` descriptor
- Tag picker: returns a `TagKey`
- Filtered block picker:
  - stairs only
  - slabs only
  - ladders/climbables only
  - solid full blocks only
  - jigsaw-safe blocks only
- Workspace palette picker:
  - returns a group of floor/wall/ceiling ids

Filters should be modeled as picker source configuration, not hardcoded in the grid widget.

Example:

```java
new MKCreativeBlockPickerSource(BlockPickFilter.STAIRS)
```

or:

```java
new MKCreativeBlockPickerSource(stack -> stack.getItem() instanceof BlockItem blockItem
        && blockItem.getBlock() instanceof StairBlock)
```

## Implementation Phases

### Phase 1: Picker Infrastructure

- Add generic picker entry/category/source records. Done.
- Add `MKCreativeBlockPickerSource`. Done.
- Add virtualized `MKCreativeGridPicker`. Done.
- Support selection by click. Done.
- Support mouse wheel scrolling. Done.
- Support hover tooltips. Done.
- Compile and smoke test in a simple screen state. Done.

### Phase 2: Workspace Picker Subpage

- Add a pending block picker request to `MKWorkspaceScreen`. Done.
- Add `buildCreativeBlockPickerState`. Done.
- Add `chooseBlock(title, currentValue, callback, returnState)`. Done as `openBlockPicker(...)`.
- Add cancel and clear behavior. Done.
- Preserve state stack behavior during picker return. Done; picker close now pops back to the opener without a full screen setup fallback.

### Phase 3: Replace Materials Page Inline Picker

- Remove `MKPlayerHotbar` from materials page. Done.
- Replace slots with field rows and `Choose` buttons. Done.
- Verify all six material values round trip through `formDraft`. Compile verified; manual validation still recommended.

### Phase 4: Replace Block Swap Inline Picker

- Remove `MKPlayerHotbar` from block swap page. Done.
- Replace source/target slots with field rows. Done.
- Verify selected blocks feed `SwapWorkspaceBlockPacket`. Compile verified; manual validation still recommended.

### Phase 5: Replace Stair Detail Inline Picker

- Replace category/detail stair block selector area. Done.
- Confirm page layout no longer reserves hotbar/palette area. Done.
- Verify category edits still apply selected stair/slab/ladder ids. Compile verified; manual validation still recommended.

### Phase 6: Cleanup And Reuse

- Remove unused `addPaletteSection` and `addBlockSwapPaletteSection` if no callers remain. Done.
- Keep `MKPlayerHotbar` if other screens still need it; otherwise leave it as a small reusable widget.
- Add translation keys for picker labels and messages. Future polish.

## Implementation Status

Implemented:

- `MKCreativePickerEntry`, `MKCreativePickerCategory`, `MKCreativePickerSource`, and `MKCreativeBlockPickerSource` in `MKWidgets`.
- `MKCreativeGridPicker` with virtualized rendering, scrolling, click selection, and hover tooltips.
- `creative_block_picker` state in `MKWorkspaceScreen`.
- Materials, block swap, and stair detail block selection now navigate to the picker subpage.
- Picker category changes refresh the grid in-place.
- Picker selection updates the opener row immediately and returns to the page that opened the picker.
- Cancel and clear return through the same picker close path.

Remaining polish:

- Add translation keys for picker strings.
- Manual in-game validation with large modded creative catalogs and operator-only blocks.

## Validation

Manual tests:

- Open Materials page.
- Choose floor from Building Blocks.
- Choose wall from a modded creative tab.
- Search by display name.
- Search by registry id.
- Scroll through a large category.
- Cancel without changing a value.
- Clear a value only where air/empty is valid.
- Apply materials and confirm mutation/regenerate behavior still uses selected ids.
- Open Block Swap.
- Choose source and target from picker.
- Run swap and confirm packet behavior unchanged.
- Confirm picker behaves when the player is not an operator.
- Confirm operator-only blocks appear only when allowed.

Automated tests are limited because this is GUI-heavy, but compile should be required:

- `.\gradlew.bat :MKWidgets:compileJava :MKNpc:compileJava --rerun-tasks`

## Risks

- Creative tab contents depend on client feature flags and registry access. If refreshed incorrectly, entries may be missing.
- Large modpacks can have many block items; the grid should virtualize rendering from the start.
- Some creative entries carry components. The block picker can store only the block id, but it should render the original stack for accurate visuals.
- Exact vanilla creative search behavior is broader than simple local search. Local search is acceptable for V1 but should be easy to replace.
- Existing `MKWorkspaceScreen` state caching may need careful invalidation when returning from picker pages.

## Recommended First Commit Scope

Keep the first implementation narrow:

- block picker source
- virtualized grid widget
- workspace block picker subpage
- materials page replacement only

Then follow with separate commits for:

- block swap replacement
- stair detail replacement
- cleanup

This keeps each step reviewable and makes layout regressions easier to isolate.
