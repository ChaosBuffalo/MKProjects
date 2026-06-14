# Workspace Draft Session Refactor Plan

## Goal

Reduce `WorkspaceDraftSession` back to a generic draft owner and move planner-specific editing behavior into planner-scoped editor objects.

The immediate issue is that `WorkspaceDraftSession` now contains many methods that only make sense for a specific planner or planner layer: walled keep courtyard controls, keep corner modes, tower stack sizing, floor-plan topology settings, palette inheritance helpers, preview selections, and navigation-oriented UI state. This makes every new planner feature add more surface area to one central class, even when that feature should be owned by a planner UI contributor.

The target shape is:

- `WorkspaceDraftSession` owns draft lifecycle, shared workspace fields, and save/generate actions.
- Planner-specific editors own planner-specific reads and mutations.
- UI contributors request the editor that matches their planner or layer.
- Existing pages migrate incrementally without changing workspace serialization in the same patch.

## Current Problems

- `WorkspaceDraftSession` has become a broad API for every workspace concept, currently thousands of lines long.
- Walled keep UI code calls global session methods for settings such as courtyard path margin, corner mode, perimeter sizing, tower tabs, and courtyard socket size.
- Tower stack and floor-plan editing are reusable sub-planner concepts, but their accessors are mixed into the same session API as global workspace fields.
- Adding one planner setting requires editing a central class, which increases merge conflicts and makes ownership unclear.
- It is hard to tell whether a session method is safe for all topology modes or only valid for one planner.
- Planner UI contributors are improving ownership of layout and preview rendering, but they still depend on a global editor API.

## Non-Goals

- Do not redesign workspace JSON or persisted topology models as part of the first refactor.
- Do not remove every forwarding method in the first pass if doing so makes the patch too large.
- Do not change generation behavior while moving editor methods.
- Do not solve layer locking or scoped regeneration here; the editor split should make that later work easier.
- Do not introduce reflection or generic string-based mutation APIs for planner settings.

## Proposed Architecture

Keep `WorkspaceDraftSession` as the root draft coordinator:

```java
public class WorkspaceDraftSession {
    public Draft draft();
    public MKStructureWorkspace buildWorkspaceDraft();
    public void applyDraftToWorkspace();
    public void createWorkspace();
    public ResourceLocation topologyPlannerId();

    public WalledKeepDraftEditor walledKeepEditor();
    public TowerStackDraftEditor towerStackEditor(String stackId);
    public FloorPlanDraftEditor floorPlanEditor(String stackId, String floorRole);
    public LinearRunDraftEditor linearRunEditor(String topologySlotId);
}
```

Planner-scoped editors should hold a reference to the session and mutate the same draft object through narrow, named methods:

```java
public final class WalledKeepDraftEditor {
    private final WorkspaceDraftSession session;

    public TerrainAdjustment terrainAdjustment();
    public void terrainAdjustment(TerrainAdjustment value);

    public int courtyardPathInnerMargin();
    public void courtyardPathInnerMargin(int value);

    public int courtyardContentTemplateSize();
    public void courtyardContentTemplateSize(int value);
}
```

The important ownership rule is simple:

- If a method makes sense for every workspace, it stays on `WorkspaceDraftSession`.
- If a method requires a planner id, stack id, floor role, topology slot, or keep-specific convention, it moves to a scoped editor.
- If a method is only for UI state inside a planner page, prefer a planner UI state object instead of the root session.

## Editor Types

### WalledKeepDraftEditor

Owns walled keep topology settings and mutations:

- terrain adjustment
- courtyard settings
- courtyard content socket size
- courtyard path inner margin
- perimeter run kind and dimensions
- wall passage and wall height controls
- corner mode controls
- active walled keep tower tab, if that state is still needed outside the contributor

`WalledKeepTopologyUiContributor` should depend on this editor for keep-specific controls.

### TowerStackDraftEditor

Owns reusable tower stack mutations for both standalone tower planners and embedded keep towers:

- stack width and length
- floor counts and enabled floor roles
- root floor role selection
- vertical access settings
- stack palette override
- per-floor palette editor creation
- horizontal exit masks that belong to tower stack structure

This editor should be usable from:

- `TowerTopologyUiContributor`
- `WalledKeepTopologyUiContributor`
- tower planner node pages

### FloorPlanDraftEditor

Owns per-floor topology and room settings:

- floor topology path constraints
- hallway lead-in mode
- floor room profiles
- root room settings
- floor palette override
- room exit masks that belong to the floor planner
- preview seed for the selected floor plan

This editor should be used by `WorkspaceFloorPlanPage` and any floor-plan contributor panels.

### LinearRunDraftEditor

Owns linear-run family editing when the user is editing path-like generated pieces directly:

- run kind
- projection
- piece shape
- length and interior width
- opening profile reference
- palette override

This can be extracted later if walled keep perimeter/path editing remains closely coupled to the current linear-run arrays.

## Session Responsibilities To Keep

The root session should continue to own:

- draft initialization from `screen.workspace()`
- namespace and structure name
- global workspace dimensions
- global material palette
- shell, exterior air, and preview margins
- opening profile list management
- family definition list management when edited through generic forms
- linear-run family list management when edited through generic forms
- `buildWorkspaceDraft()`
- validation and save/generate packet actions
- generic selected indexes for existing form pages, until those pages are refactored

The session can expose package-private mutation helpers for editors where useful, but the public UI API should become smaller over time.

## Migration Plan

### Phase 1: Add Editors Without Behavior Changes

- Add `WalledKeepDraftEditor`, `TowerStackDraftEditor`, and `FloorPlanDraftEditor` in the workspace client UI package.
- Add factory methods on `WorkspaceDraftSession`.
- Editors delegate to existing session methods at first where that keeps the patch small.
- Add no new behavior.

Commit checkpoint:

- compile succeeds
- no UI call sites changed except optional smoke usage

### Phase 2: Move Walled Keep Methods First

Move keep-specific methods from `WorkspaceDraftSession` into `WalledKeepDraftEditor`.

Start with low-risk methods:

- `courtyardSettings()`
- `courtyardContentTemplateSize()`
- `courtyardContentTemplateSize(int)`
- `courtyardPathInnerMargin()`
- `courtyardPathInnerMargin(int)`
- terrain adjustment if no other page needs it directly

Then move larger groups:

- perimeter settings
- corner mode settings
- walled keep tower tab state

Update `WalledKeepTopologyUiContributor` to take:

```java
WalledKeepDraftEditor keepEditor = editor.walledKeepEditor();
```

Commit checkpoint:

- `:MKNpc:compileJava`
- focused workspace UI/planner tests that cover walled keep generation

### Phase 3: Extract Tower Stack Editing

Move reusable tower stack methods into `TowerStackDraftEditor`.

The editor should always be constructed with a stack id:

```java
TowerStackDraftEditor center = session.towerStackEditor("keep.center");
TowerStackDraftEditor primary = session.towerStackEditor("tower.primary");
```

The tower stack editor should not care whether the stack is standalone or embedded. Parent planners decide which controls to show. For example, walled keep can expose stack width and length while standalone tower can expose width, length, and stack composition controls.

Commit checkpoint:

- `TowerTopologyUiContributor` and `WalledKeepTopologyUiContributor` both use the same editor type
- standalone tower workspace still shows tower preview controls
- walled keep parent page still exposes embedded tower footprint controls

### Phase 4: Extract Floor Plan Editing

Move floor-plan-specific settings into `FloorPlanDraftEditor`.

This should include the floor preview seed and floor topology settings so preview pages do not need to know the root session structure.

Commit checkpoint:

- floor-plan page uses `session.floorPlanEditor(stackId, floorRole)`
- tower-to-floor navigation still returns to the tower page that opened it
- floor room exit mask and palette controls still affect generated floor plans

### Phase 5: Remove Forwarding Methods

Once contributors and pages use scoped editors, remove public forwarding methods from `WorkspaceDraftSession`.

Keep only package-private helpers where they reduce duplication and do not expose planner-specific API to unrelated callers.

Commit checkpoint:

- search for moved method names on `WorkspaceDraftSession`
- compile and focused workspace tests

## Suggested Package Layout

Keep the first pass in the existing client workspace package to avoid package churn:

```text
com.chaosbuffalo.mknpc.client.gui.screens.workspace
  WorkspaceDraftSession
  WalledKeepDraftEditor
  TowerStackDraftEditor
  FloorPlanDraftEditor
  LinearRunDraftEditor
```

If the editor set grows, move them later into:

```text
com.chaosbuffalo.mknpc.client.gui.screens.workspace.editor
```

Do not combine package movement with the initial extraction unless the first patch is already small.

## API Guidelines

- Prefer typed methods over generic setting keys.
- Return immutable values or record copies when exposing model fragments.
- Keep mutation methods narrow and named after the setting they change.
- Avoid making editors cache model fragments unless there is a clear invalidation rule.
- Editors should call back into the session to mark the screen dirty only when the current UI pattern requires it; prefer keeping `screen.flagNeedSetup()` in the UI callback for now.
- Avoid planner-specific constants in the root session where possible.
- Use value types for ids as they become available, but do not block this refactor on new id classes.

## Testing Strategy

This refactor is primarily ownership-preserving, so tests should focus on behavior staying identical:

- compile after every phase
- existing workspace planner tests for walled keep, tower, and floor plans
- focused tests around settings that are moved, especially settings that create new record instances
- manual UI smoke checks for:
  - walled keep defaults page
  - standalone tower defaults page
  - tower drill-down page
  - floor-plan drill-down page
  - topology defaults workspace creation page

For pure method moves, avoid adding brittle tests that only encode current UI layout. Add tests when a moved method has mutation logic that can drop fields, such as record reconstruction for courtyard settings or tower stack settings.

## Risks

- A scoped editor can become another oversized session if it is not kept to a single planner/layer.
- Moving methods that reconstruct records can accidentally lose newly added fields.
- Some pages may still need generic form-selection state; forcing those into editors too early may create awkward dependencies.
- Walled keep embeds tower stacks, so ownership boundaries between parent planner controls and child tower controls must remain explicit.
- Existing UI contributors may temporarily mix session and scoped editor calls during migration; that is acceptable only as an intermediate state.

## Definition Of Done

The refactor is complete when:

- `WorkspaceDraftSession` no longer exposes public walled keep, tower stack, or floor-plan-specific setting methods.
- Planner UI contributors receive or create scoped editors for planner-specific controls.
- The root session still owns draft lifecycle and generic workspace fields.
- Adding a new walled keep setting no longer requires adding a public method to `WorkspaceDraftSession`.
- Adding a new tower or floor-plan setting can be done in the relevant editor and contributor only.
- Compile and focused workspace tests pass after each migration phase.
