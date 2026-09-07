# Jigsaw Structures Summary

Here’s the working model after reviewing your code and the vanilla 1.21.1 source.

## What a jigsaw structure is

A jigsaw structure is a graph of structure templates. Each placed piece contains jigsaw blocks, and each jigsaw block points at a template pool for what can attach next. Generation starts from one pool, picks an initial template, then recursively attaches child pieces until it hits depth, collision, or no valid connectors.

In your code, [`MKJigsawStructure`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:39) is basically a wrapper around vanilla `JigsawStructure`. Its `findGenerationPoint` builds the start position and then delegates straight to `JigsawPlacement.addPieces` ([`findGenerationPoint`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:150), [`addPieces` call](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:154)).

## How vanilla constructs and places them

Vanilla flow is:

1. A `structure_set` decides where the structure is allowed to try spawning.
2. The structure definition picks a start chunk and a start Y.
3. `JigsawPlacement.addPieces` picks a random rotation and a random element from the start pool.
4. If `start_jigsaw_name` is set, vanilla looks for a jigsaw block with that `name` inside the start piece and anchors generation from there.
5. It creates the first `PoolElementStructurePiece`, then walks outward through its jigsaw blocks.
6. For each jigsaw block, it reads the target pool from the block NBT, shuffles candidate templates, tries rotations, checks `JigsawBlock.canAttach(...)`, computes the child offset/Y, rejects overlaps, and queues accepted children.
7. After the piece graph is built, each `PoolElementStructurePiece` places its template into the world.

Important vanilla controls:

- `size` is recursion depth. Vanilla supports `0..20`; your MK codec clamps it to `0..7` in [`MKJigsawStructure`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:47).
- `project_start_to_heightmap` snaps the initial piece to surface height.
- `max_distance_from_center` bounds the total footprint.
- `use_expansion_hack` helps small pieces reserve enough vertical room for future children.
- Pool `projection` matters:
  - `RIGID` keeps the piece’s authored vertical relationships.
  - `TERRAIN_MATCHING` uses surface height adjustment, which is why it is used for roads/paths.

`StructureTemplatePool` is just a weighted bag of elements plus a fallback pool. Empty entries are valid and are used to terminate branches. You already do that in the digger camp pool in [`NpcStructurePools`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/data/registries/NpcStructurePools.java:38).

## How your MK layer extends vanilla

`MKJigsawBuilder` is the convenience layer for building these structures in data bootstrap code ([`MKJigsawBuilder`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/data/MKJigsawBuilder.java:21)). It exposes the same practical knobs you care about: depth, start height, max distance, floor fill, and structure events.

Your custom behavior is mainly:

- `structure_events` on the structure codec, carried by [`MKStructure`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKStructure.java:19).
- `fill_floor` and `fill_state`, implemented in [`afterPlace`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:119), which backfills below placed pieces down to solid ground.
- `MKSinglePoolElement`, which subclasses `SinglePoolElement` and customizes liquid handling plus post-placement data marker handling ([`MKSinglePoolElement`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKSinglePoolElement.java:29), [`mkPlace`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKSinglePoolElement.java:94)).
- A mixin overwrites `PoolElementStructurePiece.place` so the placed piece knows which MK structure instance it belongs to and can feed that into marker processing ([`PoolElementStructurePieceMixins`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/mixins/PoolElementStructurePieceMixins.java:56)).

That last part is important for tooling: your templates are not just geometry. Data markers inside the template become POIs/spawners/event hooks associated with the generated structure instance.

## How MKUltra structures are authored today

The current pattern is consistent:

- Register one or more template pools in Java.
- Register a structure that points at one start pool.
- Generate the JSON under `data/.../worldgen/structure` and `.../template_pool`.
- Let the NBT jigsaw blocks inside the templates decide the next pool transitions.

Examples:

- [`DecayingChurchPools`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/world/gen/feature/structure/DecayingChurchPools.java:41) is a classic multi-pool dungeon graph: `base`, `hallways`, `rooms`, `stairs_down`, `stairs_landing`, all `RIGID`.
- [`ThemcromancersLairPools`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/world/gen/feature/structure/ThemcromancersLairPools.java:51) mixes `TERRAIN_MATCHING` road pools with `RIGID` compounds/gates/towers.
- [`FireShrinePools`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/world/gen/feature/structure/FireShrinePools.java) is a hub-and-spokes style setup with separate pools for center, towers, corners, platforms, and platform contents.
- [`UltraStructures`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/data/registries/UltraStructures.java:57) shows the per-structure tuning:
  - `HYBOREAN_CRYPT` starts at absolute `-19` ([line 93](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/data/registries/UltraStructures.java:93))
  - `THEMCROMANCERS_LAIR` expands to `100` blocks from center ([line 114](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/data/registries/UltraStructures.java:114))
  - `FIRE_SHRINE` enables floor fill with red nether bricks ([line 122](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/data/registries/UltraStructures.java:122))

The generated structure JSON reflects this directly. For example, [`fire_shrine.json`](E:/MinecraftDev/MKProjects/MKUltra/src/generated/resources/data/mkultra/worldgen/structure/fire_shrine.json:1) points at `mkultra:fire_shrine/centers`, uses surface projection, size `7`, and enables `fill_floor`.

## Construction model to keep in mind for tooling

The authored templates do most of the real work. The structure registration only answers:

- where can it spawn?
- what pool does it start from?
- what depth/height/footprint rules apply?
- what extra MK behaviors happen after placement?

The actual shape comes from:

- the template pool graph
- the jigsaw block NBT inside each template
- each pool element’s projection
- optional empty entries and fallback pools
- marker blocks embedded in templates

So if you want in-game tooling for authors, the most valuable things to expose are:

- per-template jigsaw connectors: `name`, pool, facing, priority
- the pool graph: “this connector can draw from these templates”
- per-piece projection and waterlogging mode
- estimated expansion envelope from `size` + `max_distance_from_center`
- marker block inventory for POIs/spawners/events

If you want, the next useful step would be for me to map one concrete structure end-to-end, for example `Fire Shrine` or `Decaying Church`, from start pool through its template connectors into the full graph.

---

# Fire Shrine Connector Analysis

Fire Shrine is the clearest example in the repo of a connector naming scheme that is trying to express interface roles, not literal piece identities.

It starts from the `CENTER` pool in [`UltraStructures.java`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/data/registries/UltraStructures.java:117) and the pool graph is registered in [`FireShrinePools.java`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/world/gen/feature/structure/FireShrinePools.java:18). The actual connector semantics live in the template NBTs under [fire_shrine](E:/MinecraftDev/MKProjects/MKUltra/src/main/resources/data/mkultra/structure/fire_shrine).

## Observed graph

From the actual jigsaw blocks in the NBTs, the intended assembly is:

- `center_1`
  - 2 horizontal connectors to `fire_shrine/towers`
  - 2 horizontal connectors to `fire_shrine/platforms`
- `tower_1`
  - 1 connector back toward `fire_shrine/centers`
  - 2 side connectors to `fire_shrine/corners_east` and `fire_shrine/corners_west`
- `corner_east` / `corner_west`
  - 1 connector back toward `fire_shrine/towers`
  - 1 vertical connector to `fire_shrine/pillars`
- `platform_1`
  - 1 connector back toward `fire_shrine/centers`
  - 1 vertical connector to `fire_shrine/platform_contents`
- `gazebo` / `lava_fountain`
  - 1 vertical connector back toward `fire_shrine/platforms`
- `pillar_1`
  - 1 vertical connector back toward `fire_shrine/corners`

So the authored shape is basically:

`center -> towers -> corners -> pillars`
and
`center -> platforms -> contents`

That is a strong, deliberate hub-and-branch design rather than a freeform dungeon.

## What the naming means

The most important pattern is this:

- outward/generative sockets use `name = mkultra:base`, `target = mkultra:attach`
- return/receiving sockets use `name = mkultra:attach`, `target = mkultra:base`

That gives you a two-role protocol:

- `base` means “I am a piece that wants to grow outward from here”
- `attach` means “I am the reciprocal mating point on the child piece”

That is much better than naming sockets after specific piece types. It means the connector name is describing contract role, while the `pool` field decides what family of pieces may satisfy that contract.

The practical consequence is that Fire Shrine’s naming is already close to an interface model:

- connector role is in `name/target`
- allowed implementation set is in `pool`
- spatial meaning is in orientation/facing
- composition pattern is in the pool graph

That is a good foundation for tooling.

## What else is encoded besides names

Fire Shrine also uses connector orientation very intentionally:

- Horizontal expansion points use `*_up` jigsaw orientations and usually `final_state = minecraft:air`
- Vertical decorative attachments use `up_*` or `down_*` and usually `final_state = minecraft:red_nether_bricks`

That implies two connector classes even though the naming stays generic:

- horizontal structural expansion
- vertical ornament/content mounting

In other words, the names alone are not the full schema. The real schema is:

`role + pool + orientation + final_state`

That matters for editor tooling. If you only visualize names, you’ll miss half the design intent.

## Conclusions

- Fire Shrine uses a role-based connector naming scheme, not a piece-specific one. That is the right direction.
- The true modular unit is not “piece A connects to piece B”; it is “a `base` socket may consume one child from pool X that exposes an `attach` socket”.
- The structure is strongly hierarchical. Pools encode tiers:
  - `centers` are roots
  - `towers/platforms` are first-order branches
  - `corners/platform_contents` are second-order branches
  - `pillars` are terminal ornaments
- The naming is intentionally minimal. Only two role names are used repeatedly, and the pool graph carries the specialization.
- This is much easier to validate mechanically than ad hoc per-piece names. A tool can detect:
  - sockets with `base -> attach`
  - reciprocal child sockets with `attach -> base`
  - whether the referenced pool actually exists
  - whether orientation class matches expected use

## Problems and inconsistencies I found

There are two notable issues.

- `pillar_1.nbt` points to `pool = mkultra:fire_shrine/corners`, but only `corners_east` and `corners_west` are registered in [`FireShrinePools.java`](E:/MinecraftDev/MKProjects/MKUltra/src/main/java/com/chaosbuffalo/mkultra/world/gen/feature/structure/FireShrinePools.java:64). I did not find a `fire_shrine/corners` pool anywhere in code or generated data. That means pillar connectors will resolve to a missing pool at generation time.
- `corner_east` uses `joint = aligned` on its vertical pillar connector, while `corner_west` uses `joint = rollable` on the analogous vertical connector. That looks inconsistent. It may be harmless here, but it is the kind of drift a connector validation tool should flag.

## What I’d take away for future tooling

Fire Shrine suggests the right abstraction for an in-game editor is not “edit jigsaw names”, but “edit connector contracts”.

A good tool would present each connector as:

- role: `base` or `attach`
- target role: reciprocal role
- pool: allowed child family
- direction: horizontal or vertical
- joint mode: `rollable` or `aligned`
- final state: cleanup block after generation
- probable tier in graph: root / branch / terminal

That would let you validate structures like Fire Shrine very quickly, and it would catch issues like the missing `fire_shrine/corners` pool automatically.

If you want, I can turn this Fire Shrine analysis into a concrete proposed connector schema for your tooling, including validation rules and an editor UI model.
