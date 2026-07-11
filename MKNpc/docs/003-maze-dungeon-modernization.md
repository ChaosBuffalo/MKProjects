# Modernizing Maze-Style Jigsaw Dungeons

Yes. The main lesson from Fire Shrine is that connector naming works best when it expresses interface role and topology, not piece identity. That matters even more for a maze dungeon, because once the graph gets dense, ad hoc naming collapses.

For a maze-style crypt, I would modernize it around three ideas: connector contracts, piece classes, and validation.

## 1. Move from piece-specific sockets to connector contracts

Fire Shrine is already close to this with `base` and `attach`. For a dungeon, I’d make the connector language a little richer, but still role-based.

Instead of naming sockets after exact target pieces, give them semantic contracts like:

- `hall`
- `room`
- `branch`
- `stairs_up`
- `stairs_down`
- `dead_end`
- `entrance`
- `boss`
- `loot`

Then keep the reciprocal side simple, for example:

- `out_hall` -> `in_hall`
- `out_room` -> `in_room`

or even more compact:

- `socket: hall`
- `target: hall`

The key is that the name should describe what kind of connection this is, not which template it expects.

That gives you freedom to swap room sets without rewriting every connector.

## 2. Separate topology pools from content pools

In Fire Shrine, the topology and decoration are partly mixed but still readable. For a maze dungeon, I’d split them more deliberately.

You want one layer that defines navigation shape:

- corridor straight
- corridor turn
- T junction
- X junction
- small room
- large room
- stair transition
- dead end

Then a second layer that defines content variants:

- crypt empty room
- crypt coffin room
- crypt altar room
- crypt flooded room
- crypt trap room

So instead of “this connector goes to a lava room”, it goes to a `room_small` pool, and that pool may choose from several content variants.

That gives you:

- cleaner graph structure
- easier balancing
- less connector duplication
- better control over encounter pacing

## 3. Introduce connector classes explicitly

Fire Shrine implicitly distinguishes horizontal growth and vertical attachments through orientation and final state. For the crypt, I’d make that explicit in tooling and conventions.

Useful connector classes:

- `horizontal_main`
- `horizontal_branch`
- `vertical_transition`
- `terminal`
- `special`

That helps you prevent bad assemblies like:

- stairs attaching to flat corridor sockets
- boss rooms spawning on branch-only connectors
- terminal loot rooms being treated as pass-through nodes

## 4. Standardize room interfaces

For maze dungeons, the biggest authoring win is standardized footprints and socket locations.

For example:

- `5x5 room_small`
- `7x7 room_medium`
- `3x7 corridor`
- fixed doorway height
- fixed connector Y
- fixed doorway width

Then each piece advertises which interface family it implements.

This is the real upgrade from Fire Shrine’s lessons: don’t just standardize names, standardize geometry. Once all “small room north socket” connectors align the same way, your pool system becomes much more reusable.

## 5. Add lightweight tags for structural semantics

I would keep pool names simple, but add metadata in tooling for things like:

- `piece_class = corridor | room | stair | terminal`
- `tier = entry | mid | deep | boss`
- `branch_budget = 0..n`
- `rarity`
- `requires_depth >= n`
- `caps_branch = true`

Vanilla jigsaw will not enforce all of this for you, but your tooling can validate and your data pipeline can organize pools around it.

This is how you stop a maze from becoming noisy randomness.

## 6. Build progression into the graph

Fire Shrine is mostly symmetrical. A crypt should feel like it deepens.

A modernized setup should distinguish:

- entry pieces
- common traversal pieces
- deep traversal pieces
- special destination pieces

You can do that with pools like:

- `crypt/entry`
- `crypt/halls_shallow`
- `crypt/halls_deep`
- `crypt/rooms_common`
- `crypt/rooms_deep`
- `crypt/specials`
- `crypt/terminals`

Then connector contracts decide compatibility, while pool choice controls pacing.

That lets you make “later” dungeon space feel more dangerous or important without hardcoding one linear path.

## 7. Use terminals deliberately

Maze dungeons need explicit branch stopping behavior.

Instead of relying only on empty pool entries, define terminal piece families:

- sealed tomb
- loot alcove
- collapsed hall
- shrine niche
- trap dead end

That gives branches meaning when they stop. Fire Shrine is clean because every branch has a purpose. Maze dungeons need the same discipline.

## 8. Validate the graph automatically

This is the biggest practical lesson from Fire Shrine. The missing `corners` reference is exactly the kind of thing a maze setup will multiply.

For a crypt, your tooling should check:

- referenced pools exist
- every connector contract has at least one satisfiable counterpart
- socket geometry lines up
- vertical connectors only connect to vertical-compatible pieces
- terminal pieces do not expose unresolvable outward sockets
- special rooms do not appear in incompatible pools
- handed pieces have both variants where required

Without this, a maze graph becomes fragile very quickly.

## 9. Prefer interface families over unique one-off pieces

A strong maze system should make it easy to add a new room by saying:

- this is a `room_medium`
- it has two `hall` connectors and one `branch` connector
- it belongs in `rooms_deep`

That is much better than wiring every room individually into the graph.

So the modernization is really: move from handcrafted adjacency toward reusable interface families.

## Concrete direction for the crypt

If I were refactoring the crypt setup, I’d aim for something like this:

- Topology pools:
  - `crypt/corridor_straight`
  - `crypt/corridor_turn`
  - `crypt/corridor_t`
  - `crypt/corridor_cross`
  - `crypt/room_small`
  - `crypt/room_medium`
  - `crypt/stairs_down`
  - `crypt/stairs_up`
  - `crypt/terminal`
- Content variants sit inside those pools.
- Connector contracts:
  - `hall`
  - `branch`
  - `vertical`
  - `terminal`
  - `special`
- Tooling metadata:
  - class, tier, handedness, depth band, terminal/pass-through

That gives you a system that is still jigsaw-native, but much more maintainable than a loose set of room-specific pool references.

The short version is: Fire Shrine teaches that clean naming comes from separating role from implementation. For the crypt, take that one step further and separate topology from content as well.

If you want, I can sketch a concrete connector taxonomy and pool layout for `CryptStructurePools` that would fit your current codebase.
