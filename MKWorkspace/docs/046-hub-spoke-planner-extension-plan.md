# Hub Spoke Planner Extension Plan

## Status

Draft implementation plan.

## Summary

Add a new `MKWorkspaceExtensions` module that hosts workspace planners which are useful outside the core tower and walled keep authoring flows. The first planner in that module should be a generic `HubSpokePlanner`. The first authored workspace should be a simple default hub/spoke fixture; the existing MKUltra `fire_shrine` structure should be treated as a later migration target and regression fixture once the default planner workflow is solid.

The planner should not be named after fire shrine. Fire shrine is an instance of a grid-aligned hub topology:

```text
NW corner branch   N platform port   NE corner branch
             \          |          /
              W port -- hub -- E port
             /          |          \
SW corner branch   S platform port   SE corner branch
```

The same planner should be usable later for shrines, camps, square arenas, small villages, ritual sites, portal compounds, and boss courtyards. It should stay honest about Minecraft's square grid: first-class topology should be named ports, orthogonal runs, and bent corner branches rather than arbitrary radial angles.

## Goals

- Add a new `MKWorkspaceExtensions` module.
- Implement a reusable `HubSpokePlanner` in that module.
- Keep `MKWorkspace` focused on core authoring infrastructure and built-in planners.
- Keep MKUltra fire shrine as content data, not as the planner implementation.
- Build and validate a default hub/spoke workspace before migrating fire shrine.
- Later migrate the current fire shrine pieces into a workspace manifest that can be loaded through the workspace UI.
- Use authorial template collapse for rotationally equivalent pieces so designers do not need to author every directional instance.
- Provide a hub/spoke visualization and editor UI.
- Preserve runtime generation parity with the existing hand-authored fire shrine when the fire shrine migration begins.

## Non-Goals

- Making hub/spoke topology fully graph-arbitrary in the first implementation.
- Replacing the walled keep planner's perimeter or courtyard systems.
- Migrating every MKUltra structure to workspace authoring.
- Migrating fire shrine in the first implementation slice.
- Making fire shrine generation materially different during the first migration.
- Requiring every hub/spoke consumer to live in MKUltra.

## Module Plan

Add a new Gradle module:

```text
MKWorkspaceExtensions
```

Initial dependencies:

- `MKWorkspaceRuntime`
- `MKWorkspace`
- `MKWidgets`

`MKWorkspaceExtensions` should be a normal mod module, not just a Java library, because it needs client contributor registration and server-side planner registration. Its mod metadata should require `mkworkspace` and `mkworkspaceruntime`.

Update `settings.gradle`:

```text
include 'MKWorkspaceExtensions'
```

Update development runtime dependencies where useful:

- MKUltra workspace development runs can use `localRuntime project(":MKWorkspaceExtensions")`.
- Any workspace dev run that should expose extension planners should include the module at runtime.

The key dependency direction should be:

```text
MKWorkspaceRuntime <- MKWorkspace <- MKWorkspaceExtensions
                                  <- MKUltra content/manifests
```

MKWorkspace should not depend on MKWorkspaceExtensions.

`MKWorkspaceExtensions` should not depend on `MKNpc` just to define a planner. The hub/spoke planner should operate on workspace planner/runtime model types only. Runtime jigsaw structure registration and exported pool bootstrapping can stay in the consuming content module, such as MKUltra, or in a separate shared helper that is explicitly allowed to depend on MKNpc.

`MKWorkspaceExtensions` should not be required by runtime content modules such as MKUltra. Runtime content should continue to load without the workspace extension module unless a development or migration environment explicitly includes it. Workspace manifests that use `mkworkspace_extensions:hub_spoke` require the extension module only when authoring/importing that workspace.

## Registration Plan

`MKWorkspaceExtensions` should register the planner server side:

```text
MKWorkspacePlannerRegistry.registerShared(new HubSpokePlanner());
```

It should register the client contributor client side:

```text
WorkspacePlannerClientRegistry.register(... HubSpokePlannerClientContributor ...);
```

If the existing registries are sufficient, no new event bus is required. If load order becomes fragile, add a small extension registration event in `MKWorkspace` and have `MKWorkspaceExtensions` subscribe to it.

## Planner Identity

Planner id:

```text
mkworkspace_extensions:hub_spoke
```

Primary classes:

```text
com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner
com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.model.HubSpokePlannerSettings
com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace.HubSpokePlannerClientContributor
com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace.HubSpokeDraftEditor
com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace.HubSpokePlanPreview
```

Do not use `FireShrinePlanner` or fire-shrine-specific naming in planner APIs. Fire shrine-specific naming belongs in the MKUltra manifest and template ids.

## Hub Spoke Topology

The planner should model a central hub with grid-aligned named ports. A port can host a primary spoke segment and optional orthogonal corner branches. Decorative or content attachments should be modeled as insert families, not as pieces that contribute to topology flow. The first implementation should not expose arbitrary radial spoke counts, because Minecraft jigsaw rotations and collision boxes are cardinal and axis-aligned.

For this planner, a "spoke" means a graph edge from the hub through a cardinal port, not a geometric ray at any angle. Diagonal-looking corners should be represented as bent orthogonal branches that terminate visually in diagonal quadrants.

First-pass topology concepts:

- hub
- cardinal port
- primary spoke segment
- corner branch
- transition
- insert socket

First-pass layout presets:

```text
single_port
opposed_pair
three_way
cardinal_four
cardinal_with_corner_branches
multi_socket_cardinal
```

The first fire shrine profile should use:

```text
cardinal_with_corner_branches
```

Suggested schema slots:

```text
hub_spoke.hub
hub_spoke.port.primary
hub_spoke.spoke.primary
hub_spoke.corner_branch.primary
hub_spoke.transition.primary
hub_spoke.insert.primary
```

The planner should generate concrete slot instances from a small authorial schema:

```text
hub_spoke.spoke.north
hub_spoke.spoke.east
hub_spoke.spoke.south
hub_spoke.spoke.west
hub_spoke.corner_branch.north_west
hub_spoke.corner_branch.north_east
hub_spoke.corner_branch.south_west
hub_spoke.corner_branch.south_east
hub_spoke.transition.corner_left
hub_spoke.transition.corner_right
...
```

Concrete instances should inherit from compact authorial slots unless overridden.

## Simplest Default Profile

The simplest default hub/spoke profile should expose exactly three topology authorial pieces and zero insert families:

```text
center
spoke
corner
```

This profile is useful as the lowest-complexity authoring fixture. It proves that the planner can create a hub, rotate a shared spoke template onto cardinal ports, and rotate a shared corner template into corner branch positions without asking the designer to author every concrete direction.

Default settings:

```text
layoutPreset = cardinal_with_corner_branches
enabledPortIds = [north, east, south, west]
allowPerPortOverrides = false
collapseRotatedTemplates = true
insertFamilies = []
```

The generated concrete topology may still include many instances:

```text
hub_spoke.hub
hub_spoke.spoke.north
hub_spoke.spoke.east
hub_spoke.spoke.south
hub_spoke.spoke.west
hub_spoke.corner_branch.north_west
hub_spoke.corner_branch.north_east
hub_spoke.corner_branch.south_west
hub_spoke.corner_branch.south_east
```

But the designer only sees the three authorial archetypes unless they opt into per-port overrides.

## Default Generated Geometry

The default hub/spoke fixture should generate as a flat surface platform, not as enclosed rooms, tower shells, or walled keep-style building volumes. It should be closer to the walled keep courtyard content model: a simple authored surface that proves the workspace system can handle non-building topology.

The target visible footprint should read as an octagon:

```text
corner  spoke   corner
spoke   center  spoke
corner  spoke   corner
```

The pieces still use axis-aligned bounding boxes for jigsaw placement and collision. The octagonal shape comes from the authored block footprint inside each rectangular piece:

- `center`: flat square platform tile.
- `spoke`: flat rectangular platform extension, rotated to north/east/south/west.
- `corner`: flat chamfer or diagonal-corner tile, rotated to north_west/north_east/south_west/south_east.

Default fixture geometry constraints:

- no full room shell
- no interior volume requirement
- no roof
- no wall generation
- no vertical stack behavior
- zero insert families
- shallow floor or slab-like surface
- optional low curb/trim is acceptable if it stays part of the flat platform authoring surface

This geometry is intentionally minimal. It should test whether the planner and workspace UI can author a topology made from flat outdoor pieces before adding richer content such as fire shrine towers, platform decorations, supports, or insert sockets.

## Fire Shrine As First Profile

The existing fire shrine maps cleanly onto the generic planner:

| Current piece | Hub/spoke role |
| --- | --- |
| `center_1` | hub |
| `tower_1` | horizontal spoke segment |
| `platform_1` | horizontal spoke segment |
| `corner_east` | rotated transition |
| `corner_west` | rotated transition |

These pieces form the actual fire shrine topology. The remaining current pieces are better modeled as insert families:

| Current piece | Insert role |
| --- | --- |
| `pillar_1` | corner/support insert |
| `gazebo` | platform content insert |
| `lava_fountain` | platform content insert |

The first fire shrine workspace profile should reproduce the existing grid graph:

- one hub
- four cardinal ports
- east and west ports use tower spokes
- north and south ports use platform spokes
- four required corner branches occupy the diagonal quadrants
- corner branches are owned by the hub/spoke layout preset, not by a specific spoke family
- corner transition pieces expose an insert socket that can place `pillar_1`
- platform spoke pieces expose an insert socket that can place `gazebo` or `lava_fountain`

The useful mental model is:

```text
corner.nw     platform.north     corner.ne
     \              |              /
      tower.west -- hub -- tower.east
     /              |              \
corner.sw     platform.south     corner.se
```

These corner branches should not be treated as true diagonal jigsaw placements. They are grid-aligned branch chains whose final footprint reads as diagonal from above. The planner should guarantee all four corner branches for the `cardinal_with_corner_branches` preset regardless of which spoke family is assigned to a cardinal port.

`pillar_1`, `gazebo`, and `lava_fountain` should not appear as planner topology slots. They should appear in the workspace as insert families attached to insert sockets declared by topology pieces. They decorate the generated layout but do not create new topology flow, new branch depth, or new stable planner graph nodes.

The fire shrine profile should live as data in MKUltra:

```text
MKUltra/src/main/resources/data/mkultra/mk_workspace_exports/fire_shrine.json
```

The exported runtime structure should eventually use:

```text
mkultra:fire_shrine/start
```

## Authorial Template Collapse

The designer-facing template space should be smaller than the runtime graph. This is the same basic idea used by walled keep corner towers: a single authorial archetype can produce multiple concrete rotated instances.

First-pass archetypes:

```text
hub
spoke
corner_branch
corner_transition_left
corner_transition_right
```

Fire shrine profile archetypes:

```text
hub
spoke_tower
spoke_platform
corner_transition
```

Fire shrine insert family archetypes:

```text
corner_support_insert
platform_content_insert
```

The planner should support a `rotation` or `transform` setting on generated slot instances. The export/import path should mark rotated instances as derived from the shared authorial template where possible, using the existing rotated-template reuse mechanism instead of requiring separate physical authoring cells.

For fire shrine this means:

- north and south platform spokes can share one platform authorial template with rotation.
- east and west tower spokes can share one tower authorial template with rotation if their connector layout is symmetric under rotation.
- `corner_east` and `corner_west` should collapse to one `corner_transition` authorial template if their geometry and connector contracts can be represented as rotations.
- `pillar_1` should be a corner support insert family variant, not a topology archetype.
- `gazebo` and `lava_fountain` should be variants of a platform content insert family, not topology archetypes.

If an existing pair is not perfectly rotationally equivalent, keep it as two authorial archetypes for the first pass and add a validation note. Do not force collapse by losing connector correctness.

## Runtime Tags

The planner should emit generic hub/spoke tags:

```text
workspace_topology_slot_id
workspace_topology_role_id
workspace_hub_spoke_role
workspace_hub_spoke_layout_preset
workspace_hub_spoke_port_id
workspace_hub_spoke_branch_id
workspace_hub_spoke_archetype_slot_id
workspace_hub_spoke_transform
workspace_piece_kind
```

Stable slot identity should be based on the authorial archetype, not every concrete rotated instance, when the concrete instance is derived:

```text
hub_spoke_archetype:hub
hub_spoke_archetype:spoke
hub_spoke_archetype:corner
```

Concrete runtime instances can still carry their generated slot id:

```text
hub_spoke.transition.east.left
```

This distinction is important for catalog relayout and template preservation.

## Connector Model

The planner should use role-based connector contracts rather than fire-shrine-specific pool names.

Suggested connector roles:

```text
hub_spoke
spoke_back
spoke_forward
spoke_side_left
spoke_side_right
corner_branch_forward
corner_branch_back
insert_socket
```

For exported runtime pools, derive pool paths from topology role:

```text
hub_spoke/hub
hub_spoke/ports
hub_spoke/spokes/tower
hub_spoke/spokes/platform
hub_spoke/corner_branches
hub_spoke/transitions
```

The MKUltra fire shrine migration can map legacy `mkultra:base` and `mkultra:attach` jigsaw names into the generic connector roles during import or seed manifest creation.

Insert families should use the existing workspace insert-family export/import path rather than topology runtime pools. For fire shrine:

```text
corner_support_insert -> pillar_1
platform_content_insert -> gazebo, lava_fountain
```

## Planner Settings

Add `HubSpokePlannerSettings` as a planner settings payload stored in `MKWorkspaceTopologyProfile.plannerSettings`.

First-pass fields:

```text
HubSpokeLayoutPreset layoutPreset
List<String> enabledPortIds
List<HubSpokePortSettings> ports
HubSpokeCornerFamilySettings cornerFamilySettings
List<HubSpokeCornerBranchOverride> cornerBranchOverrides
boolean allowPerPortOverrides
List<HubSpokeSlotAssignment> spokeAssignments
List<HubSpokeInsertSocketSettings> insertSockets
boolean collapseRotatedTemplates
```

Fire shrine default:

```text
layoutPreset = cardinal_with_corner_branches
enabledPortIds = [north, east, south, west]
allowPerPortOverrides = true
collapseRotatedTemplates = true
insertSockets = [corner_support, platform_content]
```

The model should use named port ids, not numeric spoke indexes:

```text
north
east
south
west
```

This keeps the UI readable while still letting the planner map those names to rotation transforms.

Do not model arbitrary `spokeCount` as the primary control. Counts like 3, 5, 6, or 8 do not map cleanly to Minecraft's cardinal jigsaw rotations. When a non-four layout is needed, it should be represented by a named preset or explicit port/socket configuration.

## Corner Branch Configuration

Corner branches should be first-class topology slots owned by the hub/spoke layout preset. They should not depend on whether a particular spoke family declares left or right branch support. The planner's job is to guarantee that the selected preset emits the required corner branches, then lets the designer decide whether those corners all share one authorial family or whether individual corners become unique.

This matches the walled keep corner tower model: start with one shared corner archetype, then allow the designer to opt into per-corner uniqueness only when needed.

### Shared Corner Family

The layout preset defines all corner branches.

For `cardinal_with_corner_branches`, the planner always emits:

```text
north_west
north_east
south_west
south_east
```

Each corner branch derives from the shared `corner` authorial template and receives only a generated branch id and transform. This is the right default because it keeps the authoring surface at three topology pieces:

```text
center
spoke
corner
```

This mode guarantees all four corners while requiring the designer to build only one corner template.

### Unique Corner Overrides

Designer settings can promote any corner from the shared family to a unique family:

```text
cornerBranches:
  north_west: shared
  north_east: shared
  south_west: unique
  south_east: shared
```

In this mode, the planner still emits all four corners. The difference is only which authorial family supplies each concrete corner instance.

Candidate settings shape:

```text
cornerFamilySettings:
  sharedFamilyId: corner
  uniqueOverrides:
    south_west: corner_south_west
```

This supports asymmetry without expanding the default authoring space.

### Corner Attachment Routing

Corner branches still need a runtime jigsaw attachment route, but that route should be planner implementation detail, not the author's mental model. The first implementation can attach corner branches through whichever deterministic connector scheme is easiest to generate and validate:

- direct hub-to-corner connectors
- corner connectors emitted from adjacent spoke instances
- small generated transition pieces between spokes and corners

The authoring UI should show the result as four required corners, not as "the west spoke owns two corners" or "the north spoke owns two corners." Fire shrine was hand-authored through tower-side branching because that was easier to reason about manually; the planner does not need to preserve that ownership model.

### Recommendation

Start with shared corner family mode. Add unique corner overrides using the walled keep corner-tower pattern. Keep all four corners required for the `cardinal_with_corner_branches` preset.

`corner_east` and `corner_west` in the current fire shrine are symmetrical in shape; their side-specific connector setup exists to force them onto the correct side. The workspace version should represent that as one authorial `corner_transition` archetype when connector role remapping and rotation metadata can preserve the side intent.

## Visualization UI

Add `HubSpokePlanPreview`.

The preview should show:

- the hub at center
- cardinal ports around it
- primary spokes on enabled ports
- bent corner branches in diagonal quadrants
- transition sockets on spokes and corner branches
- insert sockets, when enabled
- disabled spokes or sockets as muted nodes
- derived/rotated nodes with a compact rotation marker

Interactions:

- click hub: open hub authorial slot
- click spoke: open spoke archetype or concrete override
- click transition: open transition archetype
- click insert socket: open insert family/slot

Settings panel:

- named layout preset
- enabled port controls
- corner branch controls
- enable/disable per-port override
- collapse rotated templates toggle
- insert socket controls
- palette scope rows for `hub_spoke`, `hub_spoke.hub`, and `hub_spoke.spoke`

## Import Discovery

The workspace import flow currently discovers MKNpc manifests only. The default hub/spoke fixture can use the existing development path at first. Before the later MKUltra fire shrine migration, add multi-source manifest discovery so MKUltra manifests can be loaded as workspaces.

Proposed service:

```text
MKWorkspaceImportSourceRegistry
```

Each source should define:

```text
moduleRoot
namespace
displayName
```

Initial sources:

- `mknpc`
- `mkultra`

`MKWorkspaceExtensions` does not need to own MKUltra content discovery, but the extension planner must be registered before importing a manifest that uses `mkworkspace_extensions:hub_spoke`.

## Runtime Pool Bootstrap

The exported workspace pool bootstrap currently lives in MKNpc. Move or duplicate the reusable part so MKUltra can register exported workspace pools.

Do not solve this by making `MKWorkspaceExtensions` depend on MKNpc. The extension module should register planner behavior, not own content-module runtime structure bootstrapping.

Possible refactors:

```text
MKNpc or a small jigsaw-integration module:
  ExportedWorkspacePoolBootstrap
```

Then both MKNpc and MKUltra can call it. Moving the current bootstrap directly into `MKWorkspaceRuntime` is only valid if the implementation no longer requires `MKSinglePoolElement` or other MKNpc classes, because `MKWorkspaceRuntime` should not grow a reverse dependency on MKNpc.

MKUltra structure registration can keep the old hand-authored fire shrine until the workspace-backed manifest validates. After validation, switch `UltraStructures.FIRE_SHRINE` to the exported start pool:

```text
mkultra:fire_shrine/start
```

## Implementation Phases

### Phase 1: Module Skeleton

- Add `MKWorkspaceExtensions` to `settings.gradle`.
- Add module build file and mod metadata.
- Add server and client entrypoints.
- Add runtime dependency from dev runs as needed.
- Verify the module loads beside `MKWorkspace`.

### Phase 2: Planner Skeleton

- Add `HubSpokePlanner`.
- Add `HubSpokePlannerSettings`.
- Register planner id `mkworkspace_extensions:hub_spoke`.
- Add schema declaration for hub, port, spoke, corner branch, transition, and insert socket roles.
- Add canonical piece generation for the `cardinal_with_corner_branches` preset.
- Add validation for duplicate stable identities and missing archetypes.

### Phase 3: Rotated Authorial Template Collapse

- Add archetype-to-instance mapping to the planner.
- Emit derived instance tags for rotated concrete slots.
- Reuse existing rotated-template metadata where possible.
- Add tests proving east/west and north/south generated instances can share one authorial template when geometry is compatible.
- Add escape hatch for non-collapsible asymmetric pieces.

### Phase 4: Default Hub Spoke Fixture

- Create a simple default workspace fixture using only `center`, `spoke`, and `corner`.
- Generate it as a flat octagonal platform, not a shell/interior structure.
- Use zero insert families.
- Validate that concrete generated slots are derived from those three authorial archetypes.
- Confirm the fixture can be loaded through the workspace UI.
- Export and re-import the fixture to validate manifest stability.

### Phase 5: UI

- Add `HubSpokePlannerClientContributor`.
- Add `HubSpokeDraftEditor`.
- Add `HubSpokePlanPreview`.
- Add settings rows and clickable preview navigation.
- Keep the UI general enough that the later fire shrine profile will not expose every rotated instance as a separate required authoring cell.

### Phase 6: Fire Shrine Migration

- Create `mkultra:fire_shrine` workspace export manifest using the hub/spoke planner id.
- Seed current topology templates as the first authorial template set and first runtime variant.
- Represent `pillar_1`, `gazebo`, and `lava_fountain` as insert family variants rather than topology pieces.
- Use spoke-family placement constraints so tower spokes qualify for east/west ports and platform spokes qualify for north/south ports.
- Use shared corner family settings so all four corner branches derive from one authorial corner transition by default.
- Allow optional unique corner family overrides following the walled keep corner tower pattern.
- Normalize or preserve legacy connector names.
- Validate import into a workspace.
- Export once through the workspace tool to confirm manifest and NBT paths are stable.

### Phase 7: Runtime Integration

- Keep MKWorkspaceExtensions optional for MKUltra runtime content.
- Generalize exported workspace pool bootstrap for MKUltra.
- Register fire shrine exported pools.
- Add or update `UltraStructurePools` and `UltraStructures` to support the workspace-backed start pool.
- Compare generated fire shrine against the current hand-authored version.

### Phase 8: Regression Tests

- Planner schema round trip.
- Planner settings codec round trip.
- Simplest default profile emits only center, spoke, and corner authorial topology pieces.
- Simplest default profile produces flat platform geometry with no shell, roof, vertical stack, or room interior requirements.
- Default fixture imports and exports without fire shrine content.
- Canonical fire shrine profile emits expected pieces.
- Fire shrine insert families include pillar, gazebo, and lava fountain variants.
- Stable authorial identities are unique.
- Rotated derived pieces preserve archetype identity.
- Runtime pools contain expected child base names.
- Fire shrine manifest imports from MKUltra source discovery.
- Workspace export manifest validates as a runtime structure.

## Resolved Decisions

- `MKWorkspaceExtensions` should not be required by MKUltra runtime content.
- The first implementation should not migrate current fire shrine. Build the default hub/spoke workspace first, then migrate fire shrine after the workflow is validated.
- `corner_east` and `corner_west` are symmetrical except for connector setup that forces correct side placement. Treat them as a collapse candidate.
- The first planner should support only `cardinal_with_corner_branches`. Add other presets later.
- Corner branches are owned by the layout preset, not by spoke families. Start with one shared corner family and add optional unique corner overrides like walled keep corner towers.
- Insert sockets are designer configured as part of authored templates. Planner settings may understand insert socket ids for preview and validation, but insert placement belongs to the template/family authoring flow.

## Recommended First Slice

Start with the module and simplest `cardinal_with_corner_branches` planner, not the full fire shrine migration.

1. Add `MKWorkspaceExtensions`.
2. Register `HubSpokePlanner`.
3. Generate a cardinal hub catalog with four ports, primary spokes, and corner branches using only center, spoke, and corner authorial topology pieces.
4. Add tests for planner validation and rotated derived slots.
5. Add a minimal preview UI.
6. Create and roundtrip the default hub/spoke fixture.
7. Defer `mkultra:fire_shrine` until the default fixture is stable.

This keeps the topology generic from the beginning while still preserving fire shrine as the concrete follow-up test that proves the extension system is actually extensible.
