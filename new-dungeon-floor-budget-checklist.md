# New Dungeon Floor Budget Checklist

This checklist is for adding the new floor-budgeted dungeon generation system to the MK jigsaw pipeline for future dungeons.

The existing crypt should remain a legacy implementation and should not be retrofitted as part of this work.

Recommended model: Style A

- one main progression path from entrance to boss
- side branches may grow upward or downward physically
- progression floors are tracked separately from physical vertical level
- main-path transitions usually advance progression
- branch transitions usually do not

## Goals

- Keep existing `MKJigsawStructure` behavior unchanged for all current structures unless they opt in.
- Add a new dungeon-layout extension for future dungeons.
- Make floor count an explicit generation budget.
- Make progression-transition pieces the only way to advance progression floors.
- Support boss-floor gating and branch-depth limits.
- Support downward, upward, and mixed vertical growth.
- Keep the system data-driven enough to scale to future dungeon families.

## Phase 1: Core Data Model

### Task 1. Add dungeon layout settings type

- Create `MKDungeonLayoutSettings`
- Add fields for:
  - `minFloors`
  - `maxFloors`
  - `minPiecesPerFloor`
  - `maxPiecesPerFloor`
  - `maxBranchDepth`
  - `allowBranchesOnFinalFloor`
  - `verticalProgressionMode`
  - connector role names for:
    - `mainForward`
    - `mainBack`
    - `branch`
    - `stairsDown`
    - `stairsUp`
    - `bossForward`
    - `bossBack`
- Add a codec for this type

Definition of done:

- The type exists
- The codec round-trips cleanly
- No existing structures are affected

### Task 2. Add generation-time piece state type

- Create `MKDungeonPieceState`
- Include:
  - `progressionFloorIndex`
  - `verticalLevelIndex`
  - `piecesOnFloor`
  - `branchDepth`
  - `onMainPath`
  - `targetFloors`

Definition of done:

- The state object is immutable and simple to pass around
- It has no persistence or gameplay coupling

### Task 3. Add connector and piece role enums

- Create `MKConnectorRole`
- Create `MKJigsawPieceRole`
- Include at minimum:
  - connector roles:
    - `MAIN_FORWARD`
    - `MAIN_BACK`
    - `BRANCH`
    - `STAIRS_DOWN`
    - `STAIRS_UP`
    - `BOSS_FORWARD`
    - `BOSS_BACK`
    - `ROOM`
    - `TERMINAL`
    - `UNKNOWN`
  - piece roles:
    - `MAIN`
    - `BRANCH`
    - `ROOM`
    - `STAIRS_DOWN`
    - `BOSS_APPROACH`
    - `BOSS`
    - `TERMINAL`

Definition of done:

- Enums exist and are independent of any one dungeon family

### Task 4. Add connector and piece metadata containers

- Create `MKConnectorInfo`
- Create `MKJigsawPieceMetadata`
- `MKConnectorInfo` should include:
  - jigsaw `name`
  - jigsaw `target`
  - target pool key
  - resolved `MKConnectorRole`
- `MKJigsawPieceMetadata` should include:
  - piece role
  - `progressionDelta`
  - `verticalLevelDelta`
  - `allowOnMainPath`
  - `allowOnBranchPath`
  - `terminal`
  - `bossOnly`

Definition of done:

- Both types exist
- They are small and generation-focused
- The model supports upward, downward, and mixed vertical growth without coupling progression to world Y

## Phase 2: Structure and Builder Opt-In

### Task 5. Extend `MKJigsawStructure`

- Add optional field:
  - `Optional<MKDungeonLayoutSettings> dungeonLayout`
- Extend `MKJigsawStructure.CODEC` with optional `dungeon_layout`
- Keep behavior unchanged when `dungeon_layout` is absent

Definition of done:

- Existing structures still deserialize unchanged
- New structures can opt into `dungeon_layout`

### Task 6. Extend `MKJigsawBuilder`

- Add optional builder field for `dungeonLayout`
- Add method:
  - `setDungeonLayout(MKDungeonLayoutSettings settings)`
- Pass the setting through to `MKJigsawStructure` in `build()`

Definition of done:

- Builders can create old-style structures and new dungeon-layout structures

## Phase 3: Metadata Resolution

### Task 7. Add piece metadata manager

- Create `MKJigsawPieceMetadataManager`
- Resolve metadata by template `ResourceLocation`
- Use separate data files instead of changing `MKSinglePoolElement` codec for the first pass

Suggested data location:

- `data/<modid>/mk_jigsaw_piece_meta/...`

Definition of done:

- The manager can load metadata for a template id
- Missing metadata fails clearly or defaults in a controlled way

### Task 8. Add connector classifier

- Create `MKConnectorClassifier`
- Resolve `MKConnectorRole` from jigsaw block `name` and layout settings
- Do not rely on legacy crypt naming
- Only support the new naming scheme for new dungeons

Definition of done:

- Given a jigsaw block NBT and layout settings, the classifier returns a connector role

### Task 9. Define metadata JSON format

- Create a documented JSON schema for piece metadata
- Include example files for:
  - a same-floor main hall
  - a branch room
  - a stairs-down piece
  - a stairs-up piece
  - an upward branch transition piece
  - a downward branch transition piece
  - a boss-approach piece
  - a boss room

Definition of done:

- New dungeon authors can classify pieces without changing code

## Phase 4: Custom Placement Pipeline

### Task 10. Create `MKJigsawPlacement`

- Fork the relevant logic from vanilla `JigsawPlacement`
- Keep the fork as close to vanilla structure as practical
- Only activate it when `dungeon_layout` is present

Definition of done:

- The new class can generate structure pieces using the same spatial rules as vanilla
- Existing structures still use vanilla placement

### Task 11. Add MK-specific queued piece state

- Create an internal queued state record, for example `MKPieceState`
- Include:
  - `PoolElementStructurePiece piece`
  - free voxel shape
  - recursion depth
  - `MKDungeonPieceState dungeonState`

Definition of done:

- The placer can propagate dungeon state from parent to child pieces

### Task 12. Select target floors at generation start

- When generation begins, choose `targetFloors` from `minFloors..maxFloors`
- Initialize root piece state with:
  - `progressionFloorIndex = 0`
  - `verticalLevelIndex = 0`
  - `piecesOnFloor = 1`
  - `branchDepth = 0`
  - `onMainPath = true`
  - chosen `targetFloors`

Definition of done:

- Each generated dungeon instance has its own chosen floor budget

## Phase 5: Layout Policy Controller

### Task 13. Add `MKDungeonLayoutController`

- Create a controller class that owns the policy rules
- Add methods:
  - `chooseTargetFloors(RandomSource random)`
  - `canPlaceChild(...)`
  - `nextState(...)`

Definition of done:

- Policy logic is isolated from low-level placement code

### Task 14. Implement floor advancement rules

- Only pieces with `progressionDelta != 0` advance `progressionFloorIndex`
- Same-progression-floor pieces keep the same `progressionFloorIndex`
- `verticalLevelDelta` changes `verticalLevelIndex`
- `progressionDelta` and `verticalLevelDelta` both come from piece metadata

Definition of done:

- Progression advancement is explicit and not inferred from world Y
- Physical vertical movement is tracked independently from progression

### Task 15. Implement per-floor piece budget rules

- Track `piecesOnFloor`
- If current floor has reached its budget:
  - stop or heavily restrict additional main-path same-floor expansion
  - prefer or require a valid progression-advancing transition

Definition of done:

- Single floors cannot sprawl indefinitely

### Task 16. Implement branch depth rules

- Track `branchDepth`
- Branch-expanding pieces increment branch depth
- Reject child placements that exceed `maxBranchDepth`

Definition of done:

- Optional branch content is bounded

### Task 17. Implement final-floor and boss gating

- If `floorIndex == targetFloors - 1`:
  - disallow additional progression-advancing transitions
  - disallow branches if `allowBranchesOnFinalFloor == false`
  - allow boss-approach or boss-compatible pieces only where intended

Definition of done:

- The final floor behaves differently from earlier floors

## Phase 6: Inject Policy Into Expansion

### Task 18. Resolve connector role during child expansion

- During child candidate evaluation:
  - inspect parent jigsaw block
  - resolve `MKConnectorInfo`

Definition of done:

- Every expansion decision has connector-role context

### Task 19. Resolve child piece metadata during candidate evaluation

- For each child template candidate:
  - resolve `MKJigsawPieceMetadata`

Definition of done:

- The layout controller has enough information to decide whether the child is legal
- The layout controller can distinguish progression movement from physical vertical movement

### Task 20. Add policy gate before accepting a child

- Before accepting a candidate child:
  - call `layoutController.canPlaceChild(parentState, connectorInfo, childMetadata)`
- Reject invalid candidates before or as early as possible in the expensive placement path

Definition of done:

- Illegal child pieces are filtered by layout policy, not just by geometry

### Task 21. Compute child dungeon state on acceptance

- On accepted child placement:
  - call `layoutController.nextState(...)`
  - store returned state in queued child record

Definition of done:

- Child pieces inherit correct floor and branch state

## Phase 7: New Dungeon Pilot

### Task 22. Choose a new dungeon prototype

- Do not use the existing crypt
- Create or designate a fresh test dungeon family using the new connector naming scheme

Definition of done:

- One new dungeon family exists as the pilot target for the system

### Task 23. Define new connector naming convention for the pilot dungeon

- Use the new role-based naming:
  - `main_forward`
  - `main_back`
  - `branch`
  - `stairs_down`
  - `stairs_up`
  - `boss_forward`
  - `boss_back`
- Document the intended meaning for each connector role
- Document which transitions are:
  - main-path progression transitions
  - vertical branch transitions
  - same-progression-floor connectors

Definition of done:

- Authors have a clear naming contract for new dungeon templates

### Task 24. Author starter metadata for the pilot dungeon

- Create metadata JSON for:
  - same-floor main hall pieces
  - branch pieces
  - room pieces
  - downward main-path stair pieces
  - upward main-path stair pieces
  - upward branch-transition pieces
  - downward branch-transition pieces
  - boss approach pieces
  - boss room pieces

Definition of done:

- The pilot dungeon has enough metadata to exercise the full floor-budget system

### Task 25. Register a pilot structure using `dungeon_layout`

- Add a new structure registration in the relevant module
- Set:
  - floor range
  - per-floor piece range
  - branch depth
  - final-floor behavior

Definition of done:

- At least one structure uses the new system end to end

## Phase 8: Debugging and Validation

### Task 26. Add debug logging for generation state

- Log:
  - chosen `targetFloors`
  - accepted piece template id
  - connector role
  - `progressionFloorIndex`
  - `verticalLevelIndex`
  - `piecesOnFloor`
  - `branchDepth`
  - main-path vs branch-path

Definition of done:

- A generated dungeon can be inspected from logs without stepping through the debugger

### Task 27. Add policy rejection logging

- Log when a candidate child is rejected because of:
  - floor limit
  - per-floor piece budget
  - branch depth
  - final-floor restrictions
  - boss-only restrictions
  - illegal vertical transition for the configured progression mode

Definition of done:

- Policy failures are distinguishable from spatial/collision failures

### Task 28. Add sanity assertions and validation checks

- Validate:
  - no progression floor index exceeds `targetFloors - 1`
  - no progression-advancing transition appears after the final floor is reached
  - boss room pieces only appear where intended
  - branch depth never exceeds the configured limit
  - `verticalLevelIndex` changes match piece metadata

Definition of done:

- The new system fails loudly when invariants are violated

## Phase 9: Optional Follow-Up Work

These are not required for the first working version.

### Task 29. Expose floor info to piece placement

- Extend the existing piece-placement context so placed pieces can know:
  - `floorIndex`
  - `onMainPath`
  - piece role

Use case:

- floor-aware marker handling
- tier-scaled spawns
- floor-scaled loot

### Task 30. Extend marker resolution with floor/tier context

- Allow `MKSinglePoolElement.mkPlace(...)` marker handling to consume floor-aware generation context

Use case:

- stronger enemies on deeper floors
- boss-floor-only spawns and rewards

### Task 31. Add data generation support for piece metadata

- Add generators or helpers so new dungeon metadata files are easy to author and validate

## Minimum Viable Version

The minimum version that should be built first is:

- `MKDungeonLayoutSettings`
- `MKDungeonPieceState`
- `MKJigsawPlacement`
- `MKDungeonLayoutController`
- metadata manager
- connector classifier
- one new pilot dungeon
- floor count enforcement
- progression-driven floor transitions
- support for `stairs_down`, `stairs_up`, and mixed vertical side content
- branch-depth enforcement
- final-floor boss gating

Do not include marker-tier integration in the first milestone.

## Suggested Execution Order

1. Core data model
2. Structure and builder opt-in
3. Metadata resolution
4. `MKJigsawPlacement` fork
5. `MKDungeonLayoutController`
6. Policy checks in expansion loop
7. New pilot dungeon
8. Debug logging and validation
9. Optional floor-aware marker integration

## Non-Goals for This Work

- Do not retrofit the current crypt
- Do not redesign legacy connector naming
- Do not add runtime mirroring
- Do not push floor-aware marker logic into the first implementation milestone

## Deliverable for Completion

The work is complete when:

- a new dungeon can opt into `dungeon_layout`
- the generator chooses a target floor count
- only progression-transition pieces advance progression floors
- physical vertical movement can be upward, downward, or mixed
- floor count and branch depth are enforced during generation
- the final floor can be reserved for boss content
- at least one new dungeon family is generating successfully under the new system
