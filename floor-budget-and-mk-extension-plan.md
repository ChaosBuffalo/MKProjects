# Floor Budget Control and MK Jigsaw Extension Plan

## Controlling Total Floors and Vertical Progression

Yes. The cleanest way is to stop thinking in raw block height and start treating “floor count” as an explicit generation budget.

The best model is:

- assign each piece a `progressionDelta`
- assign each piece a `verticalLevelDelta`
- track `progressionFloorIndex`
- track `verticalLevelIndex`
- enforce progression-floor limits explicitly
- gate which pools may be used based on progression floor index

So instead of hoping the geometry produces the right number of levels, generation knows “this run is supposed to have 4 floors” and only allows pieces that fit that plan.

For the recommended Style A model:

- the dungeon has one main progression path from entrance to boss
- side branches may go up or down physically
- main-path transitions usually advance progression
- branch transitions usually do not advance progression

### Recommended approach

Pick a target floor count when the structure starts:

- `target_floors = 3..6` for example

Then each vertical transition piece contributes:

- same-floor hall/room:
  - `progressionDelta = 0`
  - `verticalLevelDelta = 0`
- main-path stairs down:
  - `progressionDelta = +1`
  - `verticalLevelDelta = -1`
- main-path stairs up:
  - `progressionDelta = +1`
  - `verticalLevelDelta = +1`
- upward branch transition:
  - `progressionDelta = 0`
  - `verticalLevelDelta = +1`
- downward branch transition:
  - `progressionDelta = 0`
  - `verticalLevelDelta = -1`

During generation, track for each piece:

- `progressionFloorIndex`
- `verticalLevelIndex`
- `remaining_floors = target_floors - progressionFloorIndex`

Then use that to constrain child pools.

Example:

- if `remaining_floors > 1`
  - allow `stairs_down`
  - allow same-floor branches
- if `remaining_floors == 1`
  - allow final descent to boss approach
  - disallow more deep branching stairs
- if `remaining_floors == 0`
  - disallow any further progression-advancing transitions
  - allow only boss/terminal/content pieces

That gives you deterministic floor count even though the layout within each floor remains random.

### Why this is better than block-based control

If you only control total vertical height:

- different room heights distort floor count
- some floors become sparse or dense
- vertical transitions can accidentally consume too much or too little budget

If you control floor count directly:

- room height becomes a presentation detail
- stairs become the explicit floor boundary
- progression is much easier to reason about
- upward, downward, and mixed-growth dungeons all fit the same model

### Structure design pattern

Use two kinds of pieces:

- same-progression-floor pieces
  - halls
  - rooms
  - branches
  - terminals
- progression-transition pieces
  - stairs down on the main path
  - stairs up on the main path
  - shafts
  - boss ascent/descent
- vertical branch-transition pieces
  - optional upper shrine stairs
  - optional lower catacomb stairs

Only progression-transition pieces increment `progressionFloorIndex`.

That means a “floor” is really a progression layer, not just a Y interval.

### Pool scheme

You could organize pools like this:

- `crypt/floor_0_entry`
- `crypt/floor_n_main`
- `crypt/floor_n_branches`
- `crypt/floor_n_rooms`
- `crypt/floor_transition_down`
- `crypt/floor_final_approach`
- `crypt/boss`

Or more scalable:

- `crypt/main_same_floor`
- `crypt/branch_same_floor`
- `crypt/room_same_floor`
- `crypt/transition_down`
- `crypt/final_transition`
- `crypt/boss`

Then the runtime/tooling layer decides whether a transition is still legal based on `remaining_floors` and whether it advances progression.

### How to prevent infinite sprawl on one floor

You need a second budget besides floor count:

- `branch_budget`
- or `pieces_remaining_on_progression_floor`

Otherwise one floor can become huge before the generator ever descends.

A good combo is:

- `target_floors`
- `max_pieces_per_floor`
- `max_branch_depth_per_floor`

Example:

- 5 target floors
- 6 to 10 pieces per floor
- 2 branch depth max

Then once a floor has consumed enough pieces, the generator starts strongly favoring or requiring a downward transition.
Then once a progression floor has consumed enough pieces, the generator starts strongly favoring or requiring a progression-advancing transition.

### Practical control knobs

I’d give the system these explicit knobs:

- `target_floors_min`
- `target_floors_max`
- `target_pieces_per_floor_min`
- `target_pieces_per_floor_max`
- `boss_floor_depth`
- `allow_branches_on_final_floor`
- `max_branch_depth`

That lets you shape the dungeon without hardcoding a single layout.

### Simplest implementation path

If you do not want a major rewrite yet, the simplest approximation is:

1. Split pools by floor band:
   - `level_0`
   - `level_1`
   - `level_2`
   - etc.
2. Only stair pieces connect `level_n` to `level_n+1`.
3. Make the final stair connect only to `boss_approach`.
4. Limit stair count to the chosen target.

That is basically “floor count by controlled stair transitions”.

### Best long-term implementation

Long-term, the ideal system would attach per-piece generation metadata:

- `floor_index`
- `piece_index_on_floor`
- `remaining_floors`

Then markers, spawns, and pool eligibility can all use the same state.

That would let you say things like:

- “no miniboss before floor 3”
- “boss approach only on last floor”
- “loot quality scales by floor index”

### Recommendation

Use floor transitions as the only source of floor advancement and make target floor count an explicit generation budget.

In short:

- choose a target floor count at structure start
- track progression floor index per piece
- track physical vertical level separately per piece
- only progression-transition pieces advance floors
- limit piece count per floor so the dungeon does not sprawl forever
- reserve the last floor for boss approach and boss content

That is the most controllable and debuggable way to get consistent total floors.

If you want, I can sketch a concrete state machine for this, including what each pool is allowed to connect to at each floor.

---

## Extending `MKJigsawStructure` and Related Classes

The least fragile way to do this is to keep vanilla jigsaw for spatial assembly, but add an MK generation policy layer that tracks dungeon progression state while pieces are being expanded.

You do not want to force this into `MKJigsawStructure` alone. The real extension point is the placement pipeline.

For the recommended Style A model:

- progression from entrance to boss is monotonic
- the main path advances progression floors
- side branches may go upward or downward physically without advancing progression

### High-level shape

I would add four things:

1. a structure-level config object for dungeon progression rules
2. a piece-level generation state object
3. an MK-specific jigsaw placer derived from vanilla logic
4. optional pool element metadata for floor transitions and piece roles

So the flow becomes:

- `MKJigsawStructure` starts generation
- `MKJigsawPlacement` expands pieces
- each placed piece carries progression and vertical state, plus branch limits
- candidate children are filtered by dungeon rules before being accepted

### 1. Extend `MKJigsawStructure` with dungeon policy

Right now `MKJigsawStructure` mostly mirrors vanilla plus `fill_floor` and structure events.

Add an optional field like:

```java
Optional<MKDungeonLayoutSettings> dungeonLayout
```

Example shape:

```java
public record MKDungeonLayoutSettings(
        int minFloors,
        int maxFloors,
        int minPiecesPerFloor,
        int maxPiecesPerFloor,
        int maxBranchDepth,
        boolean allowBranchesOnFinalFloor,
        MKVerticalProgressionMode verticalProgressionMode,
        ResourceLocation mainConnectorName,
        ResourceLocation branchConnectorName,
        ResourceLocation stairsDownConnectorName,
        ResourceLocation stairsUpConnectorName,
        ResourceLocation bossForwardConnectorName,
        ResourceLocation bossBackConnectorName
) {}
```

Then `MKJigsawStructure.CODEC` gets one more optional field:

```java
MKDungeonLayoutSettings.CODEC.optionalFieldOf("dungeon_layout")
```

If absent, behavior stays as it is now.

### 2. Add per-piece generation state

You need state that vanilla does not track as first-class gameplay data.

Add a small record/class like:

```java
public record MKDungeonPieceState(
        int progressionFloorIndex,
        int verticalLevelIndex,
        int piecesOnFloor,
        int branchDepth,
        boolean onMainPath,
        boolean bossPathUnlocked,
        int targetFloors
) {}
```

This state is not world-persistent structure gameplay state like your `MKStructure` events. It is generation-time state used only while assembling pieces.

Each queued child candidate in the custom placer gets one of these.

### 3. Add `MKJigsawPlacement`

This is the main change.

Right now [`MKJigsawStructure.findGenerationPoint`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:150) calls vanilla `JigsawPlacement.addPieces(...)`.

Instead, if `dungeonLayout` is present, call your own:

```java
MKJigsawPlacement.addPieces(...)
```

That class should be a fork of the relevant vanilla `JigsawPlacement` logic, because you need to intercept candidate selection before placement.

#### What changes in the placer

Vanilla currently decides children based on:

- connector compatibility
- rotation
- overlap
- max depth
- bounds

Your version should also evaluate:

- current `progressionFloorIndex`
- current `verticalLevelIndex`
- `piecesOnFloor`
- `branchDepth`
- whether this connector is main/branch/stairs/boss
- whether a transition is still allowed
- whether final floor has been reached

So inside the child acceptance loop, add a policy check like:

```java
if (!layoutController.canPlaceChild(parentState, connectorInfo, childMetadata)) {
    continue;
}
```

And when a child is accepted:

```java
MKDungeonPieceState childState = layoutController.nextState(parentState, connectorInfo, childMetadata);
```

That state gets stored with the queued child piece.

### 4. Add connector and piece metadata

You need a lightweight way to classify pieces and connectors without parsing pool names heuristically.

#### Piece metadata
Add optional metadata to `MKSinglePoolElement` or to a parallel registry keyed by template `ResourceLocation`.

Example:

```java
public record MKJigsawPieceMetadata(
        PieceRole role,
        int progressionDelta,
        int verticalLevelDelta,
        boolean allowOnMainPath,
        boolean allowOnBranchPath,
        boolean terminal,
        boolean bossOnly
) {}
```

Where `PieceRole` might be:

```java
MAIN
BRANCH
ROOM
STAIRS_DOWN
BOSS_APPROACH
BOSS
TERMINAL
```

You can attach this:

- directly in the pool element codec, or
- in a separate data file keyed by template id

I would prefer separate data initially so templates and pool element codec stay simpler.

#### Connector metadata
You also want to classify connector intent. Since your naming is already good, a first pass can map by `name`/`target` and maybe pool.

For example:

- `main_forward`
- `main_back`
- `branch`
- `stairs_down`
- `stairs_up`
- `boss_forward`
- `boss_back`

A helper can inspect the jigsaw block NBT and return:

```java
public record MKConnectorInfo(
        ResourceLocation name,
        ResourceLocation target,
        ResourceKey<StructureTemplatePool> pool,
        ConnectorRole role
) {}
```

### 5. Layout controller

Put the real policy in one class so it is testable and not buried in the placer.

Example:

```java
public class MKDungeonLayoutController {
    private final MKDungeonLayoutSettings settings;

    public int chooseTargetFloors(RandomSource random) { ... }

    public boolean canPlaceChild(
            MKDungeonPieceState parentState,
            MKConnectorInfo connector,
            MKJigsawPieceMetadata childMeta
    ) { ... }

    public MKDungeonPieceState nextState(
            MKDungeonPieceState parentState,
            MKConnectorInfo connector,
            MKJigsawPieceMetadata childMeta
    ) { ... }
}
```

#### Example rules
This controller would implement the recommendation from earlier:

- only pieces with `progressionDelta != 0` advance `progressionFloorIndex`
- same-floor rooms/halls keep the same `progressionFloorIndex`
- `verticalLevelIndex` changes according to `verticalLevelDelta`
- if `piecesOnFloor >= targetPiecesThisFloor`, disallow most same-floor main growth and prefer stairs
- if `progressionFloorIndex == targetFloors - 1`, disallow further progression-advancing transitions
- if final floor reached, only allow boss approach/boss/terminal content
- branch pieces increase `branchDepth`
- if `branchDepth > maxBranchDepth`, disallow more branch growth
- Style A recommendation:
  - main-path transitions usually have `progressionDelta = +1`
  - branch transitions usually have `progressionDelta = 0`

This is the core of the whole system.

### 6. Store generation state with queued pieces

Vanilla `JigsawPlacement.PieceState` only tracks:

- piece
- free shape
- depth

Your MK version should track:

- piece
- free shape
- depth
- `MKDungeonPieceState`

So an MK equivalent becomes:

```java
record MKPieceState(
        PoolElementStructurePiece piece,
        MutableObject<VoxelShape> free,
        int depth,
        MKDungeonPieceState dungeonState
) {}
```

That lets each child inherit the correct floor state.

### 7. Optional: annotate placed pieces for post-processing

If you want markers/spawners/loot to react to floor tier, you should carry some of this state into placed pieces too.

You already have a mixin in [`PoolElementStructurePieceMixins`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/mixins/PoolElementStructurePieceMixins.java:23) that injects structure identity into piece placement.

Extend that pattern so a placed piece can also know:

- `floorIndex`
- `onMainPath`
- `pieceRole`

Then `MKSinglePoolElement.mkPlace(...)` could pass that into marker handling.

That would let your marker system resolve:

- floor-scaled spawns
- floor-scaled loot
- boss-room-only markers

without hardcoding it into the template.

### 8. Builder support

Extend [`MKJigsawBuilder`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/data/MKJigsawBuilder.java:21) with something like:

```java
public MKJigsawBuilder setDungeonLayout(MKDungeonLayoutSettings settings)
```

Then structures like the crypt can opt into the system declaratively in `UltraStructures`.

### 9. Data-driven piece metadata

For maintainability, I would not hardcode per-template rules in Java forever.

Add generated resources like:

- `data/mkultra/mk_jigsaw_piece_meta/crypt/hall_straight_1.json`
- `data/mkultra/mk_jigsaw_piece_meta/crypt/stairs_down_1.json`
- `data/mkultra/mk_jigsaw_piece_meta/crypt/boss_room_1.json`

Example:

```json
{
  "role": "stairs_down",
  "progression_delta": 1,
  "vertical_level_delta": -1,
  "allow_on_main_path": true,
  "allow_on_branch_path": false,
  "terminal": false,
  "boss_only": false
}
```

That gives your tools something clean to edit later.

### 10. Minimal first implementation

If you want the smallest viable extension:

- add `MKDungeonLayoutSettings` to `MKJigsawStructure`
- add `MKJigsawPlacement` fork
- classify connectors by jigsaw `name`
- classify pieces by separate metadata files
- track only:
  - `progressionFloorIndex`
  - `verticalLevelIndex`
  - `piecesOnFloor`
  - `branchDepth`
  - `targetFloors`

That is enough to get:
- fixed floor counts
- stair-driven progression transitions
- support for upward, downward, or mixed vertical side content
- capped branch growth
- reserved final floor / boss chain

### What this would look like in your codebase

Likely new classes:

- `MKNpc/.../structure/MKDungeonLayoutSettings.java`
- `MKNpc/.../structure/MKDungeonPieceState.java`
- `MKNpc/.../structure/MKJigsawPlacement.java`
- `MKNpc/.../structure/MKDungeonLayoutController.java`
- `MKNpc/.../structure/MKJigsawPieceMetadata.java`
- `MKNpc/.../structure/MKConnectorInfo.java`

Likely changed classes:

- [`MKJigsawStructure`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKJigsawStructure.java:39)
- [`MKJigsawBuilder`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/data/MKJigsawBuilder.java:21)
- [`MKSinglePoolElement`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/feature/structure/MKSinglePoolElement.java:29)
- maybe [`PoolElementStructurePieceMixins`](E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/mixins/PoolElementStructurePieceMixins.java:23) if you want floor-aware marker placement

### Recommendation

Do not try to make vanilla `JigsawPlacement` infer floors from Y alone. Make floor count a first-class generation budget and enforce it in a custom MK placer.

The design target should be:

- structure config chooses floor budget
- placer tracks progression state and physical vertical state separately
- connectors define topology role
- piece metadata defines progression and vertical transition behavior
- markers/spawns can optionally consume floor/tier info later

If you want, I can turn this into a concrete implementation plan with method signatures and the exact spots in a forked `JigsawPlacement` where the new checks should go.
