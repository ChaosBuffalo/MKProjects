# Topology UI Contributors Plan

## Goal

Make `WorkspaceTopologyDefaultsPage` a generic host for topology default editing instead of a page that special-cases every planner type.

The immediate driver is walled keep topology. It currently embeds walled-keep sizing, wall controls, corner toggles, and tower-stack controls directly in `WorkspaceTopologyDefaultsPage`. That does not scale as we add more topology planners, and it makes composite planners awkward because their child topology editors have to be hand-wired into the parent page.

The target shape is:

- standalone tower topology uses the same tower-stack editor as embedded tower stacks
- walled keep owns only walled-keep-specific controls and embeds tower-stack editors through tabs
- future composite planners can contribute child topology sections without adding new branches to `WorkspaceTopologyDefaultsPage`
- topology profile type is replaced by a namespaced planner id everywhere; do not add a temporary bridge mode

## Current Problems

- `WorkspaceTopologyDefaultsPage` branches on topology type and directly knows walled keep details.
- The page currently treats "not tower" as the walled keep path, which will become incorrect as soon as another topology profile exists.
- Tower stack controls are embedded methods on the defaults page, even though tower stacks are a reusable topology concept.
- Walled keep center/corner tower settings are displayed as one long list, which makes the page hard to scan.
- Preview widgets are topology-specific but currently have no shared place to be owned by the topology planner.

## Proposed Architecture

Introduce a small UI contribution interface:

```java
public interface WorkspaceTopologyUiContributor {
    ResourceLocation plannerId();

    void addDefaultsSections(
            MKWorkspaceScreen screen,
            MKStackLayoutVertical content,
            WorkspaceDraftSession editor
    );
}
```

`WorkspaceTopologyDefaultsPage` becomes responsible for:

- page title and generic help text
- active topology label
- scroll container and content stack
- invoking the registered contributor for the active planner id

It should not know about:

- walled keep sizing
- wall/gate/courtyard controls
- tower-stack details
- tower-stack tabs
- tower topology path depth defaults
- future planner-specific previews

The page should ask a client-side registry for the contributor:

```java
ResourceLocation plannerId = editor.topologyPlannerId();
WorkspaceTopologyUiContributor contributor =
        WorkspacePlannerUiRegistry.getTopologyUi(plannerId);
contributor.addDefaultsSections(screen, content, editor);
```

If the workspace model does not yet expose a `ResourceLocation` planner id, add one and replace the existing topology profile type usage. This migration does not need compatibility bridging for existing workspace JSON.

## Client UI Registry

Use a client-side registry inspired by `MKCore`'s `PlayerPageRegistry`. This keeps planner UI extension on the same pattern already used by the player quest page in MKNpc.

```java
public final class WorkspacePlannerUiRegistry {
    public interface PlannerUiDefinition {
        ResourceLocation getPlannerId();

        Component getDisplayName();

        WorkspaceTopologyUiContributor createTopologyUi();
    }

    public static void register(PlannerUiDefinition definition) {
        // client setup registration
    }

    public static WorkspaceTopologyUiContributor getTopologyUi(ResourceLocation plannerId) {
        // exact lookup with fallback
    }
}
```

This registry should live in client code and should be called during `FMLClientSetupEvent` with `event.enqueueWork(...)`, matching `QuestPage.registerPlayerPage()`.

Internal MKNpc planner UI registration should follow MKCore's internal player page registration pattern. MKCore does not use IMC for its built-in player pages; `MKCoreClient.clientSetup(...)` calls `PlayerPageRegistry.init()`, and `PlayerPageRegistry.init()` directly registers built-in pages.

Apply that same shape here. Internal MKNpc planner UI registration should happen from MKNpc client setup:

```java
private void clientSetup(final FMLClientSetupEvent event) {
    event.enqueueWork(WorkspacePlannerUiRegistry::init);
}
```

Internal registrations can mirror `PlayerPageRegistry.registerInternal(...)`:

```java
private static void registerInternal(ResourceLocation id,
                                     Component displayName,
                                     Supplier<WorkspaceTopologyUiContributor> factory) {
    register(new PlannerUiDefinition() {
        @Override
        public ResourceLocation getPlannerId() {
            return id;
        }

        @Override
        public Component getDisplayName() {
            return displayName;
        }

        @Override
        public WorkspaceTopologyUiContributor createTopologyUi() {
            return factory.get();
        }
    });
}
```

External modules with a compile dependency on MKNpc can register planner UI from their own client setup:

```java
event.enqueueWork(() ->
        WorkspacePlannerUiRegistry.register(new MyPlannerUiDefinition()));
```

The registry should be keyed by `ResourceLocation`, not a plain topology string, so planner ids are namespaced across modules and mods.

Registry behavior:

- reject or log duplicate planner UI ids
- return a fallback contributor when no UI is registered for a planner id
- avoid loading UI classes on dedicated servers
- use exact lookup by planner id for defaults-page rendering
- add ordering only if we later need a planner selection list

Do not use IMC for client planner UI registration unless we later need UI contributions from mods without direct compile-time access to MKNpc APIs.

## Common Planner Registry

Client UI registration should be separate from common planner registration.

Common planner registration is responsible for generation, datagen, defaults, validation, sizing reports, and serialization. Client planner UI registration is responsible only for screens, panels, and preview widgets.

This split matters because UI contributors reference client-only classes. Server, common, and datagen code should never need to load those classes.

Possible common descriptor:

```java
public interface MKWorkspacePlannerDescriptor {
    ResourceLocation id();
    MKWorkspaceTopologyProfile defaultProfile(MKWorkspaceDimensions dimensions);
    WorkspacePlanner planner();
    List<String> validate(MKStructureWorkspace workspace);
}
```

Possible client descriptor:

```java
public interface MKWorkspacePlannerClientDescriptor {
    ResourceLocation plannerId();
    Component displayName();
    WorkspaceTopologyUiContributor createTopologyUi();
}
```

The common registry should use direct internal registration for MKNpc's built-in planner types, matching MKCore's internal player page registration pattern. External planner registration can use IMC when we introduce real cross-mod planner extension points.

The client UI registry should stay client-only and use direct client setup registration, matching `PlayerPageRegistry`. Do not use IMC for client UI registration by default.

For internal common planner registration, prefer the same direct `init()` style as MKCore's internal page registration. IMC should be for cross-mod extension, not for registering MKNpc's own built-in planner types.

## Tower Stack Editor

Extract the current tower-stack controls into a reusable panel/helper, for example:

```java
public final class TowerStackTopologyPanel {
    public void addStackEditor(
            MKWorkspaceScreen screen,
            MKStackLayoutVertical content,
            WorkspaceDraftSession editor,
            String stackId,
            String labelPrefix
    );
}
```

This panel should include:

- tower footprint width/length/height
- min/max main floors
- min/max basement floors
- basement entry toggle
- shaft size
- stair placement, mode, rise type, stair width
- top cap approach toggle
- basement cap approach toggle
- foundation controls
- palette overrides

This panel should be used by both:

- `TowerTopologyUiContributor`
- `WalledKeepTopologyUiContributor`

## Tower Stack Side Preview

Add a tower-specific preview widget, separate from the walled keep top-down footprint widget:

```java
public final class MKTowerStackSidePreview extends MKWidget {
    // renders a vertical side view of one tower stack
}
```

The widget should show:

- basement cap
- optional basement cap approach
- repeated basement floors, labeled as fixed count or min-max range
- optional basement entry
- entry floor
- repeated main floors, labeled as fixed count or min-max range
- optional top cap approach
- top cap
- vertical extent and budget fit status

The preview must use planner/report data rather than duplicating layout math in rendering code.

Add a planner-side report object:

```java
public record MKTowerStackSizingReport(...) {
}
```

`MKTowerStackSizingReport` and its calculator should live in the `planner` package. The report is derived behavior used by validation and UI preview; it is not serialized workspace data. Keep passive settings and enums in `model`, and keep calculations that understand generation constraints in `planner`.

The report should derive:

- effective min/max upward pieces
- effective min/max downward pieces
- whether basement entry is generated
- whether cap approach pieces are generated
- maximum vertical span
- available vertical budget
- fit status and reason

## Walled Keep Tower Tabs

The walled keep contributor should use tabs for embedded tower stacks.

Initial tabs:

- `Center Tower`
- `Corner Towers`

Tabs can be plain text buttons to start. The selected tab should live in `WorkspaceDraftSession`, not local page state, because controls call `screen.flagNeedSetup()` and rebuild the page.

Suggested draft-session API:

```java
String walledKeepTowerStackTab();
void walledKeepTowerStackTab(String stackId);
```

The value should be transient UI state only. Do not serialize it to workspace JSON.

Recommended stack ids:

- `keep.center`
- `keep.corner.shared`

If individual corner stacks are enabled, the contributor should expand the tab list to:

- `Center`
- `NW Corner`
- `NE Corner`
- `SE Corner`
- `SW Corner`

Shared corner settings should be shown only while at least one corner still uses the shared corner stack. If every corner has become individual, hide the shared `Corner Towers` tab.

## Contributor Responsibilities

### TowerTopologyUiContributor

Renders:

- `MKTowerStackSidePreview`
- `TowerStackTopologyPanel` for `tower.primary`
- tower topology path depth defaults

The existing path depth controls are tower topology controls, not global workspace controls. They include main path min/max and branch cap limits for topology groups. These settings should move out of `WorkspaceTopologyDefaultsPage` and into `TowerTopologyUiContributor`.

### WalledKeepTopologyUiContributor

Renders:

- `MKWalledKeepFootprintPreview`
- terrain adaptation and footprint status
- courtyard socket controls
- corner shared/unique toggles
- perimeter kind
- wall unit span and recommended span
- wall passage width
- wall height and wall top void margin
- reset for wall/gate sizing
- tower-stack tabs
- active tab's `MKTowerStackSidePreview`
- active tab's `TowerStackTopologyPanel`

The walled keep contributor should not render global path depth defaults. Walled keep perimeter, courtyard, sockets, and wall layout use jigsaw branches as implementation details, but those are not the same designer-facing tower dungeon progression settings.

If embedded tower stacks later need path/progression controls, show those controls inside the relevant tower tab or reusable tower-stack editor, not as global walled keep settings.

### Default Contributor

Renders a small fallback message for unknown topology profiles. It should avoid mutating settings.

## Implementation Phases

### Phase 1: Extract Tower Stack Panel

- Move tower-stack row methods out of `WorkspaceTopologyDefaultsPage`.
- Move path depth controls out of `WorkspaceTopologyDefaultsPage` and into tower-owned UI.
- Keep behavior identical.
- Use the new panel from the existing page.
- Verify tower and walled keep tests still pass.

### Phase 2: Add Contributor Interface And Registry

- Replace topology profile type usage with namespaced planner ids throughout the workspace model and UI. Do not add compatibility bridging for existing workspace JSON.
- Add `WorkspaceTopologyUiContributor`.
- Add client-side `WorkspacePlannerUiRegistry`.
- Register internal MKNpc planner UI contributors from MKNpc client setup.
- Create `TowerTopologyUiContributor`.
- Create `WalledKeepTopologyUiContributor`.
- Move walled keep-specific methods into the walled keep contributor.
- Update `WorkspaceTopologyDefaultsPage` to call the registry by planner id.
- Add fallback UI contributor for missing planner UI registrations.

### Phase 3: Add Walled Keep Tower Tabs

- Add transient selected-tab state to `WorkspaceDraftSession`.
- Render center/shared-corner tower tab text buttons in `WalledKeepTopologyUiContributor`.
- Render unique corner tabs only when individual corner stack settings are enabled.
- Hide the shared corner tower tab when every corner is individual.
- Render only the active tower-stack panel.
- Keep reset scoped to the active stack.

### Phase 4: Add Tower Side Preview

- Add `MKTowerStackSizingReport`.
- Add calculator/report generation from `MKWorkspaceTowerStackSettings`.
- Place the report and calculator in the `planner` package.
- Add `MKTowerStackSidePreview`.
- Render it in both tower and walled keep contributors.

### Phase 5: Planner Descriptor Integration

- Introduce a common `WorkspacePlannerRegistry` and `MKWorkspacePlannerDescriptor` if planner-type registration starts spreading through generation/datagen code.
- Use the same namespaced planner id for common planner descriptors and client UI descriptors.
- Consider IMC for common planner descriptor registration if external mods need to register planners without direct initialization hooks.
- Keep client UI registration client-only and modeled after `PlayerPageRegistry`.

## Testing

Add or update tests for:

- `WorkspaceTopologyDefaultsPage` no longer directly references walled keep calculator/widgets.
- `WorkspaceTopologyDefaultsPage` no longer directly renders tower path depth defaults.
- tower topology contributor renders path depth defaults.
- walled keep topology contributor does not render global path depth defaults.
- client UI registry maps tower and walled keep planner ids to the expected contributors.
- duplicate client UI planner id registration logs or rejects predictably.
- missing planner UI id returns the fallback contributor.
- selected walled keep tower tab survives `flagNeedSetup()` rebuild through `WorkspaceDraftSession`.
- unique corner tabs appear only when their individual corner stacks are enabled.
- shared corner tab disappears when all corner stacks are individual.
- workspace model uses namespaced planner ids instead of profile type strings.
- tower stack panel mutates the same settings as the old inline UI.
- tower side preview report handles:
  - fixed main/basement floors
  - ranged main/basement floors
  - zero basement floors
  - basement entry disabled
  - top/basement cap approaches enabled and disabled

## Open Decisions

No remaining decisions are currently open for this plan.
