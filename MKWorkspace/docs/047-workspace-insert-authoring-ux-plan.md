# Workspace Insert Authoring UX Plan

## Goal

Give designers an in-world workflow for declaring and placing insert sockets inside workspace authorial templates.

The workflow should let a designer point at a block in an authored workspace piece, open a focused UI, and either:

- create a new insert family at that location, or
- place a socket for an existing insert family that fits there.

The placed socket is a real jigsaw block in the authorial template. It targets an insert family pool and preserves the replaced block as its `final_state`, so preview and runtime replacement restore the expected block when no insert is selected or when a jigsaw is resolved.

## Vocabulary

Use generic insert language across planners.

- `insert_family`: reusable workspace family that defines insert bounds, socket contract, and one or more variants.
- `insert_socket`: jigsaw block placed in a topology/template piece that targets an insert family pool.
- `insert_variant`: concrete authored content for an insert family.

Rename legacy/specialized `courtyard_socket` concepts to `insert_socket`. Courtyard content, fire shrine platform content, fire shrine pillar supports, floor link details, and future decorative content should all use the same socket vocabulary.

## Utility Item Flow

Add a workspace insert placement utility item.

Normal use:

- Raycast the block the designer clicked.
- The clicked block becomes the candidate socket location.
- The clicked block state is captured as the candidate `final_state`.
- The candidate jigsaw orientation faces the player.
- Open the insert socket UI if the block is inside a workspace authorial template.

Crouch use:

- Raycast the block the designer clicked.
- The candidate socket location is the adjacent block in the clicked face direction.
- The candidate `final_state` is the block currently at the adjacent socket location.
- If the adjacent block is air, use `minecraft:air` as `final_state`.
- Open the same insert socket UI.

If the item is used outside a workspace authorial template, show a generic error and do not mutate the world.

The item should not place or replace the jigsaw until the UI is confirmed. The initial item use should capture a placement context:

```text
workspace_id
authorial_piece_id
authorial_piece_bounds
socket_world_pos
socket_local_pos
socket_orientation
replaced_block_state
clicked_face
player_facing
```

Canceling the UI should leave the world unchanged.

## Jigsaw Orientation

Support both horizontal and vertical insert sockets.

Horizontal sockets:

- Attach through one of the four horizontal faces of the insert bounds.
- The jigsaw front points outward from the insert bounds toward the host template socket.
- The jigsaw top should usually be `UP`.

Vertical upward sockets:

- Attach through the bottom face of the insert bounds.
- The jigsaw front points downward or upward according to the jigsaw contract needed by Minecraft's jigsaw matching.
- The authoring UX should present this as an upward placement from the host piece: the insert sits above the socket.
- This is required for fire shrine style inserts where content is placed on top of a platform or corner branch.

Vertical downward sockets:

- Attach through the top face of the insert bounds.
- The authoring UX should present this as a downward placement from the host piece: the insert hangs below or fills downward from the socket.
- This should be supported in the first implementation alongside upward inserts so ceiling, hanging, and underside details use the same system.

## Exterior Face Constraint

The insert template jigsaw cannot be placed arbitrarily inside the insert bounds. It must be on an exterior face of the insert bounds so it can physically attach to the host template's socket.

Valid local positions:

- North face: `local_z == 0`
- South face: `local_z == length - 1`
- West face: `local_x == 0`
- East face: `local_x == width - 1`
- Bottom face: `local_y == 0`
- Top face: `local_y == height - 1`

For the first implementation:

- support north/south/east/west horizontal faces
- support bottom-face vertical upward inserts
- support top-face vertical downward inserts

The UI should not expose freeform offsets that can place the jigsaw in the middle of the insert bounds. Instead, expose:

```text
attachment_face
face_u_offset
face_v_offset
```

For horizontal faces:

- `attachment_face`: north, south, east, west
- `face_u_offset`: lateral offset along the face
- `face_v_offset`: vertical offset along the face
- one coordinate is locked by the selected face

For bottom-face upward inserts:

- `attachment_face`: bottom
- `face_u_offset`: local X offset
- `face_v_offset`: local Z offset
- `local_y` is locked to `0`

For top-face downward inserts:

- `attachment_face`: top
- `face_u_offset`: local X offset
- `face_v_offset`: local Z offset
- `local_y` is locked to `height - 1`

This keeps the designer's mental model simple: choose the face that attaches, then slide the jigsaw along that face.

## Create Insert Family UI

The create screen should include:

- insert family id/name
- width
- length
- height
- attachment face
- face offsets for the jigsaw position
- replacement block picker for the jigsaw inside the insert template
- top-down diagram of bounds and jigsaw position
- side/elevation diagram for vertical placement and height
- validation messages
- confirm/cancel actions

Width and length:

- must be odd
- minimum `1`
- maximum is computed from the selected host template, socket position, attachment face, and jigsaw face offset

Height:

- can be any positive integer
- maximum is computed from the selected host template and socket placement

The replacement block picker controls the `final_state` for the jigsaw placed inside the new insert family authorial template. It should default to `minecraft:air`. This is separate from the host socket's `final_state`, which is captured from the replaced block in the host template.

## Bounds Fitting

Fit validation must be based on the realized insert bounds, not only raw width/length/height.

Inputs:

```text
host authorial piece bounds
host socket local position
host socket orientation
insert width
insert length
insert height
insert attachment face
insert jigsaw local position on that face
```

The insert bounds should be projected into the host authorial piece's local coordinate space as if the insert were placed at the socket. The UI should allow only dimensions and offsets where the projected bounds fit inside the host authorial piece bounds.

This means the slider ranges are contextual:

- increasing width may be limited more on one side than the other depending on the socket position
- changing the jigsaw face offset changes how much insert bounds extend in each direction
- vertical upward inserts are limited by available space above the socket inside the host template
- horizontal inserts are limited by available lateral, vertical, and depth space from the selected face

Validation should explain the failing axis:

```text
Too wide for this socket.
Too long for this socket.
Too tall for this socket.
Jigsaw offset pushes bounds outside the authorial piece.
Attachment face is incompatible with this socket orientation.
```

## Existing Insert Family Picker

The initial UI should list existing workspace insert families.

Rows should show:

- family id/name
- dimensions
- attachment face or supported attachment faces
- supported socket orientation
- variant count
- fit status

The picker should filter by attachment face and socket orientation first, so designers see the families that are meaningful for the socket they are placing. Families that match the face/orientation filter but do not fit the current host template/socket context should remain visible but disabled in red with a concise reason.

Disabled reasons should use the same validation engine as the create screen. Do not maintain separate fit logic for create and existing-family placement.

## Host Socket Placement

When the user confirms placement, replace the target block in the host authorial piece with an insert socket jigsaw.

The host socket jigsaw should be configured with:

```text
name: workspace insert socket target for this host slot
target: insert family entry target
pool: selected insert family pool
final_state: captured replaced block state at the host socket position
joint: aligned unless the existing connector model requires otherwise
orientation: computed from socket orientation
```

Planner-owned export should continue to preserve authored `final_state` from this socket, while planner/runtime code remains free to own target names, pools, and other generated connector details when applicable.

## New Family Authorial Template

Creating a new insert family should create an authorial template with the selected bounds.

First-pass template content:

- empty bounds
- structure void or air according to current workspace template conventions
- one jigsaw block on the selected exterior face
- jigsaw `final_state` from the UI replacement block picker
- metadata identifying the template as an insert family authorial template

The new family creation flow should create only the family contract authorial template. Designers will manage variants separately.

The important distinction is:

- family/template defines the socket contract and bounds
- variants contain actual decorative content

For fire shrine-style inserts, `gazebo` and `lava_fountain` should be variants of the same platform content insert family rather than separate topology pieces. The family authorial template should remain the contract/bounds template.

## Data Model Notes

The insert family model should store enough data to validate socket placement without reading NBT every time:

```text
family_id
width
length
height
attachment_face
jigsaw_local_pos
jigsaw_orientation
default_jigsaw_final_state
supported_socket_orientations
```

If multi-face families are needed later, model that as either:

- multiple attachment contracts on one family, or
- separate insert families that share variants

The first implementation should keep one attachment contract per family.

## Migration

Rename `courtyard_socket` to `insert_socket`.

Migration tasks:

1. Rename constants, tags, UI labels, and docs references.
2. Keep a compatibility read path for old exported manifests if existing test data uses `courtyard_socket`.
3. Write new exports using `insert_socket` only.
4. Update walled keep courtyard content to use generic insert socket naming.
5. Update hub/spoke fire shrine import/export to use insert socket naming for pillar and platform content.

## Implementation Phases

1. Add the design model and validation helper for insert socket placement.
2. Rename `courtyard_socket` to `insert_socket` with compatibility reads.
3. Add the utility item and server-side workspace placement context lookup.
4. Add the insert socket selection screen with existing-family fit list.
5. Add the create-family screen with bounded odd width/length sliders, height slider, face selector, offset controls, and diagrams.
6. Implement host socket jigsaw placement on confirm.
7. Implement new insert family contract authorial template creation without automatically creating content variants.
8. Wire preview/export so inserted sockets and variants resolve correctly.
9. Add tests for fit validation, exterior-face constraints, vertical upward sockets, and compatibility migration.

## Tests

Add focused tests for:

- odd width/length enforcement
- arbitrary height enforcement and max-height clamping
- horizontal exterior-face jigsaw constraints
- bottom-face upward jigsaw constraints
- top-face downward jigsaw constraints
- invalid center-of-bounds jigsaw rejection
- existing family disabled when projected bounds do not fit
- existing family filtered by attachment face and socket orientation
- host socket `final_state` uses replaced block state
- adjacent crouch placement uses adjacent block state as `final_state`
- new insert family replacement block picker defaults to `minecraft:air`
- create flow creates the insert family contract template without content variants
- outside-workspace item use does not mutate the world
- legacy `courtyard_socket` data reads as `insert_socket`

## Decisions

- Top-face downward inserts should be supported in the first pass.
- The replacement block picker should default to `minecraft:air`.
- The create flow should create only the insert family contract authorial template; designers manage variants separately.
- Existing insert families should be filtered by attachment face and socket orientation before fit validation disables oversized or otherwise invalid choices.
