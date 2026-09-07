# Tiered Crypt Progression Sketch

Here is a concrete model for a tiered crypt that still fits jigsaw well.

## Progression Shape

Use a controlled backbone plus optional branches:

1. `entry`
2. `shallow`
3. `mid`
4. `deep`
5. `boss_approach`
6. `boss`

Branches can hang off `shallow`, `mid`, and `deep`, but only the backbone advances to the next tier.

So the dungeon graph becomes:

- main path: `entry -> shallow -> mid -> deep -> boss_approach -> boss`
- side paths: branch off each tier into terminals, loot rooms, miniboss rooms, shrines, traps

## Pool Layout

I would split pools into topology pools first:

- `crypt/entry`
- `crypt/main_shallow_halls`
- `crypt/main_mid_halls`
- `crypt/main_deep_halls`
- `crypt/branch_shallow`
- `crypt/branch_mid`
- `crypt/branch_deep`
- `crypt/room_shallow`
- `crypt/room_mid`
- `crypt/room_deep`
- `crypt/stairs_down`
- `crypt/boss_approach`
- `crypt/boss`
- `crypt/terminals`
- `crypt/specials_shallow`
- `crypt/specials_mid`
- `crypt/specials_deep`

Then content variants live inside those pools.

Examples:

- `room_shallow`
  - empty tomb
  - broken coffin room
  - weak ambush room
- `room_mid`
  - flooded burial room
  - priest chamber
  - trap crypt
- `room_deep`
  - cursed altar
  - elite guard room
  - ritual hall
- `boss`
  - necromancer sanctum
  - lich tomb
  - guardian vault

## Connector Contracts

Keep connector names role-based.

Suggested contracts:

- `main_forward`
- `main_back`
- `branch`
- `room`
- `stairs_down`
- `stairs_up`
- `special`
- `terminal`
- `boss_forward`
- `boss_back`

Reciprocal pairings:

- `main_forward <-> main_back`
- `boss_forward <-> boss_back`
- `branch <-> branch`
- `room <-> room`
- `stairs_down <-> stairs_up`
- `special <-> special`
- `terminal <-> terminal`

That is already enough to separate backbone progression from side content.

## Backbone Rules

Mainline pieces should expose:

- one `main_back`
- one `main_forward`
- optional `branch` sockets
- optional `room` sockets

Tier rules:

- `entry` can only connect forward into `main_shallow_halls`
- `main_shallow_halls` forward can connect to:
  - more `main_shallow_halls`
  - `stairs_down`
  - `boss_approach` only if depth gate is met
- `stairs_down` moves to the next tier pool:
  - shallow -> mid
  - mid -> deep
- `main_deep_halls` eventually connect to `boss_approach`
- `boss_approach` connects only to `boss`

That gives you pacing without needing runtime graph analysis.

## Side Branch Rules

Branch sockets should never advance the main path.

Examples:

- shallow branch sockets connect to:
  - `branch_shallow`
  - `room_shallow`
  - `specials_shallow`
  - `terminals`
- mid branch sockets connect to:
  - `branch_mid`
  - `room_mid`
  - `specials_mid`
  - `terminals`
- deep branch sockets connect to:
  - `branch_deep`
  - `room_deep`
  - `specials_deep`
  - `terminals`

This keeps optional content tier-appropriate.

## Difficulty Scaling

Drive difficulty by tier bands.

Example tier table:

- Tier 0: `entry`
  - weak undead
  - sparse traps
  - low loot
- Tier 1: `shallow`
  - standard undead
  - occasional elites
  - basic trap rooms
- Tier 2: `mid`
  - more elites
  - hazard rooms
  - stronger loot
- Tier 3: `deep`
  - high elite density
  - miniboss chance
  - strong loot
- Tier 4: `boss_approach`
  - scripted gauntlet
  - elite packs
  - checkpoint/healing/shrine possibility
- Tier 5: `boss`
  - dedicated boss spawn
  - boss arena logic
  - end reward

## Template Authoring Pattern

A good hallway backbone piece might have:

- `main_back` on one end
- `main_forward` on the other
- one or two `branch` sockets on the sides

A room piece might have:

- one `room` or `branch` return socket
- optional extra `branch` sockets if it is pass-through
- or `terminal` only if it is intended to dead-end

A boss approach piece might have:

- `boss_back` toward deep halls
- `boss_forward` toward the boss room
- no ordinary branch sockets

The boss room should have:

- one `boss_back`
- no outward progression sockets

## Example Mini Graph

A generated run could look like:

1. `entry_stair`
2. `hall_shallow_straight`
3. `hall_shallow_t`
4. branch to `room_shallow_coffins`
5. continue to `stairs_down_1`
6. `hall_mid_turn`
7. `room_mid_flooded`
8. `stairs_down_2`
9. `hall_deep_cross`
10. branch to `special_deep_altar`
11. `boss_approach_gauntlet`
12. `boss_room_lich`

That is still random, but the power curve is controlled.

## Spawner/Loot Marker Strategy

Do not bake exact mobs into every room template. Use neutral markers and resolve by tier.

Examples:

- `crypt_spawn_melee`
- `crypt_spawn_ranged`
- `crypt_spawn_elite`
- `crypt_loot_minor`
- `crypt_loot_major`
- `crypt_boss_spawn`

Then map marker + tier to content table.

For example:

- `crypt_spawn_melee` in shallow -> skeletons/zombies
- same marker in deep -> elite deathguards
- `crypt_loot_major` in mid -> rare chest
- same marker in boss room -> boss reward cache

That keeps templates reusable across tiers if needed.

## How to Guarantee Boss Placement

Use one of these:

- simplest: only `main_deep_halls` can connect to `boss_approach`, and `boss_approach` only connects to `boss`
- stricter: have a dedicated `deep_gate` piece that must appear before `boss_approach`
- strongest: only allow boss approach after one or two required stair transitions

I would use the stair-transition rule. It matches player intuition.

## Recommended First Pass

If you want something practical without a major engine rewrite:

- Split crypt pools into `shallow`, `mid`, `deep`, `boss_approach`, `boss`
- Standardize connector roles: `main_forward`, `main_back`, `branch`, `terminal`
- Make stair pieces be the only legal transition between tiers
- Reserve boss rooms for a dedicated final chain
- Drive mobs/loot/traps from tiered pools or marker resolution by tier

That gets you most of the value quickly.

If you want, I can turn this into a concrete proposed set of Java pool registrations and a naming convention document for the crypt pieces.
