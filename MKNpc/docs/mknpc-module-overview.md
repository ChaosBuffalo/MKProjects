# MKNpc Module Overview

This document describes the major responsibilities and internal boundaries of the
`MKNpc` module. It is intended as a starting point for adding Javadocs: it names
the main subsystems, explains how data moves through them, and calls out the
classes that form useful documentation anchors.

## Module Role

`MKNpc` is the NPC, quest, and structure integration module for the MK mod set.
It depends on the adjacent MK modules for combat, abilities, factions, dialogue,
widgets, weapons, and targeting. Within that larger system, `MKNpc` owns:

- data-driven NPC definitions and NPC option application;
- MK-specific entity classes, AI goals, sensors, threat handling, and rendering;
- quest definitions, quest instances, objectives, rewards, requirements, and
  player quest state;
- world-level NPC data, structure instance tracking, notable NPC/chest/POI
  indexing, and quest generation against discovered structures;
- MK jigsaw structure generation, dungeon layout control, structure events, and
  structure metadata;
- authoring tools for structure workspaces, including in-world previews,
  variants, stairs, exports, backups, imports, and palette mutations;
- client UI and network packets for spawners, quests, and workspaces.

The module entry point is `com.chaosbuffalo.mknpc.MKNpc`. It registers the
module's deferred registries, datapack registries, client setup, commands, IMC
extensions, and global event handlers.

## Startup And Registration

`MKNpc` wires the module together in its constructor and `setupRegistries`.
The important registration groups are:

- `MKNpcAttachments`: entity, player, world, chunk, chest, and workspace
  attachments/capabilities.
- `MKNpcAttributes`: NPC-specific attributes such as aggro range.
- `MKNpcBlocks`, `MKNpcBlockEntityTypes`, and `NpcComponents`: spawner, POI,
  workspace-dev block state and data-component support.
- `MKNpcEntityTypes`: MK entity types such as skeletons, golems, piglins,
  blazes, fire elementals, flying skeletons, and skulls.
- `MKMemoryModuleTypes` and `MKSensorTypes`: Minecraft brain integration used
  by MK NPC AI.
- `MKNpcWorldGen`: custom structure type, structure piece type, and custom
  jigsaw pool element registrations.
- `NpcRegistries`: custom registries for NPC options, option entries, model
  styles, datapack NPC definitions, and model looks.
- `QuestRegistries`: custom registries for quest objective, reward, and
  requirement types plus datapack quest definitions.
- `NpcDialogueEffectTypes`, `NpcDialogueConditionTypes`, and
  `StructureEventManager`: dialogue and structure-event codec registration.
- `PacketHandler`: network payloads used by spawner and workspace screens.

During common setup, `MKNpc` registers NPC entity faction identities with
`MKFaction`. During IMC setup it publishes dialogue and player persona quest
extensions so other modules can interact with NPC dialogue and quest state.

## Data-Driven NPC Definitions

Package: `com.chaosbuffalo.mknpc.npc`

NPCs are defined by `NpcDefinition`, a datapack-serializable object registered in
`NpcRegistries.NPC_DEFINITIONS`. A definition contains either an entity type or a
parent definition reference, plus a map of `NpcDefinitionOption` instances.

The core behaviors are:

- parent definitions are resolved by `NpcDefinitionManager.resolveDefinitions`;
- child definitions inherit entity type and options from ancestors;
- options apply in `EARLY`, `MIDDLE`, and `LATE` order when an entity is spawned
  or restored;
- difficulty value is converted into health scaling and passed to option
  application;
- `NpcDefinition.createEntity` creates the Minecraft entity, attaches NPC data,
  applies options, and calls `MKEntity.postDefinitionApply` for MK entities.

`NpcOptionTypes` and `npc.options.*` implement the configurable parts of an NPC:
abilities, equipment, attributes, faction identity, render group, model look,
quests, loot, ghost rendering, boss stages, size, skill classes, battlecries, and
world-permanent state. Some options create `npc.option_entries.*` entries, which
allow world-persistent per-spawn overrides.

`NpcDefinitionManager` also syncs compact `NpcDefinitionClient` snapshots to
clients during datapack sync. Those client definitions support UI display without
exposing the full server-side definition object.

Useful Javadoc anchors:

- `NpcDefinition`
- `NpcDefinitionOption`
- `NpcOptionType`
- `NpcDefinitionManager`
- `NpcRegistries`
- `WorldPermanentSpawnConfiguration`

## Entity Runtime And AI

Package: `com.chaosbuffalo.mknpc.entity`

`MKEntity` is the base class for most custom NPCs. It extends `PathfinderMob`
and integrates with MKCore combat/ability data, MKFaction, MKChat dialogue,
TargetingAPI, pet ownership, ranged attacks, visual melee attacks, and entity
sync.

Important responsibilities in `MKEntity` include:

- owning an `EntitySyncController` and MKCore entity data attachment;
- installing default goals for return-to-spawn, movement, ability use, ranged
  attacks, blocking, melee attacks, target acquisition, and look-at-threat;
- exposing combat and non-combat movement modes;
- managing Minecraft brain memories for allies, enemies, threat, movement
  strategy, current ability, ability targets, spawn point, and projectiles;
- calculating threat from damage, healing, faction calls for help, and combat
  state;
- supporting boss-stage transitions by replacing stats/behavior instead of
  dying immediately;
- handling dialogue/trade interaction for non-hostile players;
- driving ghost visibility, ghost armor translucency, render scale, model look,
  visual melee windups, and local melee animation variants.

Concrete entity classes such as `MKSkeletonEntity`, `MKGolemEntity`,
`MKBlazeEntity`, `MKFireElementalEntity`, `MKZombifiedPiglinEntity`,
`MKFlyingSkeletonEntity`, and `MKFlyingSkullEntity` specialize base stats,
sounds, models, movement, or rendering assumptions.

AI support is split into:

- `entity.ai.goal`: goal wrappers for attacking, blocking, movement,
  return-to-spawn, swimming, ability use, and target selection.
- `entity.ai.sensor`: brain sensors that populate visible entities, threat
  targets, movement strategy, and ability-use decisions.
- `entity.ai.memory`: custom memory modules and threat map entries.
- `entity.ai.movement_strategy`: reusable movement strategies for following,
  flying, kiting, wandering, and stationary behavior.
- `entity.ai.controller.MovementStrategyController`: helper methods for
  swapping movement strategies based on current combat state.

Useful Javadoc anchors:

- `MKEntity`
- `MovementStrategy`
- `MovementStrategyController`
- `ThreatMapEntry`
- `UseAbilityGoal`
- `MKTargetGoal`
- `NpcMeleeAttackExecutor`
- `BossStage`

## Attachments And Persistent Data

Package: `com.chaosbuffalo.mknpc.capabilities`

The module uses NeoForge attachments/capability-style interfaces to hold runtime
and persistent state.

`EntityNpcDataHandler` stores NPC-specific entity state:

- associated `NpcDefinition`;
- spawn id, notable id, structure id, spawn position, and difficulty value;
- whether the entity was spawned by MKNpc;
- extra loot options and drop chances;
- pending/generated quest offerings;
- death receiver callback for spawners and structure events;
- deferred definition application when entities are deserialized.

`PlayerQuestingDataHandler` stores player quest state, but most of the actual
state is persona-specific through the MKCore persona extension
`PersonaQuestData`. It tracks active quest chains, completed quest ids, sync
updates, current quest steps, chain advancement, and reward grant timing.

`WorldNpcDataHandler` is the world-level index for NPC/quest/structure content.
It stores:

- world-permanent spawn configurations;
- structure entries keyed by structure instance UUID;
- structure-name to instance indexes;
- generated quest chain instances;
- notable NPC, notable chest, and point-of-interest entries;
- queued chest processing for chunks that are not ready yet;
- a `WorldStructureManager` for active structure lifecycle.

`WorldStructureManager` tracks structures that currently have players nearby. It
fires structure activation/deactivation callbacks, player enter/exit callbacks,
active tick callbacks, NPC death callbacks, and tracked-event entity death
callbacks.

Useful Javadoc anchors:

- `IEntityNpcData` and `EntityNpcDataHandler`
- `IPlayerQuestingData` and `PlayerQuestingDataHandler`
- `IWorldNpcData` and `WorldNpcDataHandler`
- `WorldStructureManager`
- `MKStructureEntry`
- `NotableNpcEntry`
- `NotableChestEntry`
- `PointOfInterestEntry`

## Blocks, Block Entities, And Spawning

Packages:

- `com.chaosbuffalo.mknpc.blocks`
- `com.chaosbuffalo.mknpc.block_entities`
- `com.chaosbuffalo.mknpc.spawn`

The runtime structure authoring and spawning blocks are:

- `MKSpawnerBlock` / `MKSpawnerBlockEntity`: stores a weighted `SpawnList`,
  respawn timer, movement mode, structure metadata, notable ids, and the
  currently spawned entity. It spawns NPCs when players are in range and despawns
  idle spawns when players leave the despawn range.
- `MKPoiBlock` / `MKPoiBlockEntity`: records a labeled point of interest in a
  generated structure, uploads it into world NPC data, then removes the marker
  block from the world.
- `MKWorkspaceDevBlock` / `MKWorkspaceDevBlockEntity`: anchors an editable
  structure workspace and stores the workspace id for the client workspace UI.

`SpawnList` and `SpawnOption` are the serializable weighted spawn definition
model used by spawners and spawner items/components.

Useful Javadoc anchors:

- `MKSpawnerBlockEntity`
- `SpawnList`
- `SpawnOption`
- `MKPoiBlockEntity`
- `MKWorkspaceDevBlockEntity`
- `SpawnerDataComponent`

## Quests

Package: `com.chaosbuffalo.mknpc.quest`

Quests are datapack-defined through `QuestDefinition`, registered under
`QuestRegistries.QUEST_DEFINITIONS`. A quest definition contains a named quest
chain, display name, repeatability, requirements, quest mode, start dialogue, and
optional additional notable NPC requirements.

The quest model has three layers:

- `QuestDefinition`: template-level chain, requirements, start dialogue, and
  structure requirements.
- `QuestChainInstance`: world-generated instance that binds a definition to
  concrete structure instances and quest data.
- `PlayerQuestChainInstance`: player/persona state that tracks current quest
  steps, objective state, completion, and sync.

Quest content is composed from:

- `quest.objectives.*`: kill, loot, talk, trade, ability-kill, notable-kill, and
  type-tag objectives.
- `quest.rewards.*`: XP, faction, loot, entitlement, talent-tree, and notable
  faction override rewards.
- `quest.requirements.*`: requirements checked before a player can start a
  quest.
- `quest.dialogue.conditions.*` and `quest.dialogue.effects.*`: integration
  points with MKChat dialogue trees.
- `DialogueBuilder` and `QuestBuilder`: builder helpers for generated or
  programmatic quest content.

Quest generation is structure-aware. `WorldNpcDataHandler.buildQuest` checks
that all required `QuestStructureLocation`s have discovered structure instances,
filters them by objective and additional notable requirements, picks nearby
compatible structures, builds a `QuestChainInstance`, generates dialogue, and
stores it in world data.

Quest progress is event-driven. `EntityHandler` listens for NPC deaths, chest
interactions, and dialogue gathering, then checks the player's active quest
chains for matching objectives.

Useful Javadoc anchors:

- `QuestDefinition`
- `Quest`
- `QuestChainInstance`
- `QuestObjective`
- `QuestReward`
- `QuestRequirement`
- `QuestStructureLocation`
- `PlayerQuestChainInstance`
- `NpcDialogueUtils`

## Dialogue Integration

Packages:

- `com.chaosbuffalo.mknpc.dialogue`
- `com.chaosbuffalo.mknpc.quest.dialogue`

MKNpc extends MKChat with custom dialogue effects and conditions. The base
dialogue extension is registered through IMC in `NPCDialogueExtension`, while
quest-specific dialogue conditions/effects connect dialogue nodes to quest
state.

Examples:

- `GrantEntitlementEffect` and `OpenLearnAbilitiesEffect` are general NPC
  dialogue effects.
- `StartQuestChainEffect`, `AdvanceQuestChainEffect`, and
  `ObjectiveCompleteEffect` mutate quest state.
- `CanStartQuestCondition`, `OnQuestCondition`, `ObjectivesCompleteCondition`,
  `PendingGenerationCondition`, and related conditions gate dialogue responses.

When a player opens dialogue with an NPC, `EntityHandler.onSetupDialogue` adds
quest-specific dialogue trees for any active player quest chain that has
dialogue for that NPC.

Useful Javadoc anchors:

- `NPCDialogueExtension`
- `NpcDialogueEffectTypes`
- `NpcDialogueConditionTypes`
- `NpcDialogueUtils`
- quest dialogue conditions/effects

## Worldgen And Structure Runtime

Packages:

- `com.chaosbuffalo.mknpc.world.gen`
- `com.chaosbuffalo.mknpc.world.gen.feature.structure`
- `com.chaosbuffalo.mknpc.world.gen.feature.structure.events`

MKNpc provides a custom structure type, `MKJigsawStructure`, that extends the
vanilla jigsaw structure pipeline with MK-specific layout control, piece
metadata, structure events, optional floor filling, and per-piece foundation
policies.

Key pieces:

- `MKJigsawStructure`: codec-backed structure definition and generation entry
  point. It delegates to vanilla jigsaw placement unless `dungeon_layout` is
  present.
- `MKJigsawPlacement`: custom jigsaw placer that classifies connectors, consults
  `MKDungeonLayoutController`, filters candidates by `MKJigsawPieceMetadata`,
  inserts main-path endings and branch caps, and propagates dungeon state through
  placed pieces.
- `MKSinglePoolElement`: custom jigsaw pool element that can receive structure
  instance metadata during placement.
- `MKJigsawPieceMetadata` and `MKJigsawPieceMetadataManager`: data-driven
  metadata used by custom layout selection and foundation generation.
- `MKDungeonLayoutSettings`, `MKDungeonLayoutController`,
  `MKDungeonPieceState`, `MKDungeonConnectorSettings`, and related enums/rules:
  the policy layer for vertical progression, main path length, branch behavior,
  connector roles, and piece acceptance.
- `MKStructure`: base class for runtime structure events and player
  enter/exit/activate/deactivate callbacks.
- `StructureEvent` and `StructureEventManager`: codec-backed event system for
  spawning NPC definitions and reacting to active structure state.

Two mixins support this flow:

- `StructureStartMixins` assigns and persists a UUID for each generated
  structure instance, then pushes the id into pool pieces.
- `PoolElementStructurePieceMixins` routes placement through `IMKPoolElement`
  when possible so structure name and instance id are available to custom pool
  elements.

`WorldStructureHandler` caches registered `MKStructure` instances on server
start. On level ticks, it detects players inside MK structures, indexes
structure data if needed, and visits active structures through
`WorldStructureManager`.

Useful Javadoc anchors:

- `MKJigsawStructure`
- `MKJigsawPlacement`
- `MKDungeonLayoutController`
- `MKDungeonLayoutSettings`
- `MKJigsawPieceMetadata`
- `MKSinglePoolElement`
- `MKStructure`
- `StructureEvent`
- `StructureEventManager`
- `StructureStartExtension`

## Structure Workspace Authoring

Packages:

- `com.chaosbuffalo.mknpc.world.gen.workspace`
- `com.chaosbuffalo.mknpc.world.gen.workspace.model`
- `com.chaosbuffalo.mknpc.world.gen.workspace.planner`
- `com.chaosbuffalo.mknpc.world.gen.workspace.scaffold`
- `com.chaosbuffalo.mknpc.world.gen.workspace.stairs`
- `com.chaosbuffalo.mknpc.world.gen.workspace.export`
- `com.chaosbuffalo.mknpc.world.gen.workspace.mutation`

The workspace subsystem is an in-world authoring tool for creating structure
pieces and exporting them into datapack resources. It is anchored by
`MKStructureWorkspace`, a codec-backed model stored in level data through
`IMKStructureWorkspaceData`.

The high-level workflow is:

1. A creative player places or uses a workspace-dev block.
2. `MKStructureWorkspaceService` creates or updates an `MKStructureWorkspace`
   using the requested dimensions, topology, families, openings, materials,
   margins, and vertical access settings.
3. A planner creates canonical `MKPlannedPiece` definitions for the workspace.
4. `MKWorkspaceScaffoldBuilder` lays the planned pieces into a preview grid,
   clears the area, builds scaffold geometry, places jigsaw connectors,
   structure blocks, signs, marker blocks, and records
   `MKWorkspacePieceDefinition` metadata.
5. Optional variant, stair, block-swap, palette-swap, relayout, margin-expansion,
   and identity-rename services mutate the existing workspace while writing
   backups before destructive operations.
6. `MKWorkspaceExportArchiveWriter` exports structure NBT, piece metadata,
   template pools, and manifests for use by worldgen.
7. `MKStructureWorkspaceImportService` and related manifest discovery code can
   rehydrate a workspace from exported resources.

Important model concepts:

- `MKStructureWorkspace`: root workspace metadata and validation.
- `MKWorkspaceTopologyProfile`: topology type and path settings. Current code
  supports tower and walled-keep style topology profiles.
- `MKWorkspaceRoomFamilyDefinition`: room/family definitions tied to topology
  slots and connector/opening rules.
- `MKHorizontalOpeningProfile`: reusable horizontal opening dimensions and path
  compatibility rules.
- `MKWorkspaceLinearRunFamilyDefinition`: hallway/linear-run family settings for
  main and branch paths.
- `MKWorkspaceMaterialPalette`, `MKWorkspacePaletteOverride`, and
  `MKWorkspacePaletteResolver`: material selection and per-family overrides.
- `MKWorkspaceVerticalAccessSpec`, `MKVerticalAccessPlacement`, and
  `MKWorkspaceStairAuthoringConfig`: vertical access, stairs, ladders, and shaft
  layout rules.
- `MKWorkspacePieceDefinition`: saved metadata for each generated or authored
  piece, including bounds, structure block position, connector definitions,
  marker positions, generated stair positions, and workspace tags.

Planning and generation concepts:

- `MKWorkspacePlanner` is the small interface for producing canonical planned
  pieces from a workspace.
- `MKWorkspacePlannerRegistry` chooses a planner for a workspace topology.
- `MKTowerWorkspacePlanner`, `MKTowerStackPlanner`, and
  `MKWalledKeepWorkspacePlanner` produce topology-specific piece plans.
- `MKWorkspaceTopologyPlanner` and schema classes describe slots, roles,
  regions, and links used by planners.
- `MKWorkspaceScaffoldBuilder` turns planned pieces into in-world editable
  geometry.
- `MKWorkspaceStairBuilder` adds or clears generated stairs for a piece.

Useful Javadoc anchors:

- `MKStructureWorkspace`
- `MKStructureWorkspaceService`
- `IMKStructureWorkspaceData`
- `MKWorkspacePlanner`
- `MKWorkspacePlannerRegistry`
- `MKWorkspaceScaffoldBuilder`
- `MKWorkspaceStairBuilder`
- `MKWorkspaceExportArchiveWriter`
- `MKWorkspaceExportManifest`
- `MKStructureWorkspaceMutationService`
- `MKWorkspacePieceRelayoutService`
- `MKWorkspaceBackupRestoreService`

## Client UI And Rendering

Packages:

- `com.chaosbuffalo.mknpc.client.gui`
- `com.chaosbuffalo.mknpc.client.render`

Client UI covers three main surfaces:

- `MKSpawnerScreen`: edit and finalize spawner spawn lists.
- `QuestPage`: display current quest chains and quest step state.
- `MKWorkspaceScreen` plus `client.gui.screens.workspace.*`: create, edit,
  validate, import, manage, generate, mutate, backup, and export structure
  workspaces.

Rendering covers model layers, skeletons, model styles, render groups, and
entity renderers:

- `RenderRegistry` registers entity renderers and model layers.
- `ModelLook`, `ModelStyle`, `LayerStyle`, and `ModelLookManager` describe and
  resolve data-driven model looks/styles.
- renderer classes such as `MKBipedRenderer`, `SkeletalRenderer`,
  `GolemRenderer`, `FireElementalRenderer`, and piglin/skull renderers bind
  concrete entity types to models and texture/style selection.
- client mixins adjust ghost and ghost-armor translucency for MK entities.

`NpcClientEventHandler` registers the quest menu key binding and renders quest
objective indicator particles in-world.

Useful Javadoc anchors:

- `MKWorkspaceScreen`
- `WorkspacePageBase`
- workspace page classes
- `QuestPage`
- `RenderRegistry`
- `ModelLook`
- `ModelStyle`
- `ModelLookManager`
- `MKBipedRenderer`

## Networking And Commands

Package: `com.chaosbuffalo.mknpc.network`

`PacketHandler` registers all custom payloads. Packet responsibilities are
mostly UI-to-server operations and server-to-client screen/data synchronization:

- spawner packets: open spawner UI, set spawn list, finalize spawner data;
- NPC definition sync: send client-safe NPC definition snapshots;
- workspace packets: open workspace screen, create/update workspaces, generate
  scaffold pieces, generate/clear stairs, add variants, export pieces, import
  manifests, swap blocks, and restore backups.

Package: `com.chaosbuffalo.mknpc.command`

Commands expose development and admin workflows:

- `/mksummon`: spawn an NPC from a datapack `NpcDefinition` at a difficulty
  value.
- `/mkquest`: quest inspection/manipulation support.
- `/mkstructure`: structure inspection/manipulation support.
- `/mkworkspace`: workspace listing, regeneration, backup restore, preview
  relayout, and block swapping.

Custom command argument types resolve NPC and quest definition ids from the
custom registries.

Useful Javadoc anchors:

- `PacketHandler`
- packet records/classes in `network.packets`
- `NpcCommands`
- `MKSummonCommand`
- `MKQuestCommand`
- `MKStructureCommands`
- `MKWorkspaceCommands`
- `NpcDefinitionIdArgument`
- `QuestDefinitionIdArgument`

## Data Generation And Resources

Package: `com.chaosbuffalo.mknpc.data`

`MKNpcGenerator` registers data providers for server and client generated
resources:

- datapack registry sets;
- biome, structure, and entity-type tags;
- default faction data;
- language entries;
- melee animation data;
- model look definitions;
- pack metadata.

Workspace exports also have source-side helpers:

- `MKWorkspaceExportManifestLoader` reads manifests from
  `src/main/resources/data/<namespace>/mk_workspace_exports`.
- `ExportedWorkspacePoolBootstrap` and related registry bootstrap code use
  exported workspace manifests to produce worldgen template pools and structures.

Useful Javadoc anchors:

- `MKNpcGenerator`
- `NpcRegistrySets`
- `MKWorkspaceExportManifestLoader`
- `ExportedWorkspacePoolBootstrap`
- `MKJigsawBuilder`
- `NpcDefinitionBuilder`
- `NpcGenUtils`

## Event Flow Summary

Important event handlers are concentrated in `com.chaosbuffalo.mknpc.event`.

`EntityHandler` handles:

- reapplying saved NPC definitions when entities join a level;
- reducing incoming MK damage to players based on world difficulty;
- queueing chest processing on chunk load;
- intercepting notable/quest chest interactions;
- adding bonus NPC XP;
- adding quest dialogue trees during dialogue gathering;
- notifying death receivers and active structures on NPC death;
- advancing kill objectives for the killing player and their team members;
- ticking entity NPC data;
- adding extra NPC loot drops;
- adding threat when healing enemies.

`WorldStructureHandler` handles:

- caching all registered `MKStructure` instances on server start;
- detecting players inside MK structures on server level ticks;
- indexing structure starts into world NPC data;
- ticking world NPC data and active structure state from the overworld tick.

`NpcClientEventHandler` handles:

- quest menu key binding;
- in-world objective indicator particles.

## Cross-System Flows

### Spawning An NPC From A Structure Spawner

1. A generated structure places an `MKSpawnerBlockEntity`.
2. The block entity receives structure name/id through custom pool element
   placement and uploads itself into `WorldNpcDataHandler`.
3. When a player is within spawn range, the spawner chooses a weighted
   `NpcDefinition`.
4. `NpcDefinition.createEntity` creates the entity, writes `IEntityNpcData`,
   applies definition options, and lets `MKEntity` finalize its derived stats.
5. The spawner writes structure id, spawn id, notable id, movement mode, and
   death receiver into the entity data.
6. `EntityHandler` and `WorldStructureManager` observe future ticks, drops,
   quest progress, and death events.

### Generating And Starting A Quest

1. An NPC definition option queues a `QuestOfferingEntry` on
   `EntityNpcDataHandler`.
2. The entity data tick attempts to build a quest through `ContentDB.getQuestDB`
   and `WorldNpcDataHandler.buildQuest`.
3. World data matches quest-required structure locations to discovered
   `MKStructureEntry` instances.
4. A `QuestChainInstance` is generated, stored in world data, and linked to
   dialogue for the offering NPC.
5. The player starts the quest through dialogue, which calls
   `PlayerQuestingDataHandler.startQuest`.
6. Event handlers update objective data from kills, chest interactions, dialogue,
   trades, or other objective-specific hooks.
7. Completing a quest grants rewards and advances or completes the quest chain.

### Authoring And Exporting A Workspace

1. The player opens a workspace from a workspace-dev block.
2. Client workspace pages send create/update/generate/mutate packets.
3. `MKStructureWorkspaceService` validates and persists workspace settings.
4. A planner emits canonical planned pieces.
5. `MKWorkspaceScaffoldBuilder` creates editable in-world scaffold pieces and
   stores `MKWorkspacePieceDefinition` metadata.
6. Optional mutators add variants, stairs, material swaps, or layout changes.
7. Export writes NBT structures, jigsaw metadata, template pools, and manifests
   for generated resources.
8. Data generation or runtime import can consume those manifests to create
   usable worldgen content.

## Suggested Javadoc Priorities

For the first Javadoc pass, document public contracts and lifecycle-sensitive
classes before documenting every helper:

1. Registry and entry points: `MKNpc`, `NpcRegistries`, `QuestRegistries`,
   `MKNpcWorldGen`, `PacketHandler`.
2. Data models and codecs: `NpcDefinition`, `NpcDefinitionOption`,
   `QuestDefinition`, `Quest`, `QuestObjective`, `QuestReward`,
   `MKStructureWorkspace`, `MKJigsawPieceMetadata`, `MKWorkspacePieceDefinition`.
3. Persistent data boundaries: `IEntityNpcData`, `IPlayerQuestingData`,
   `IWorldNpcData`, `IMKStructureWorkspaceData`, and their handlers.
4. Runtime lifecycle classes: `MKEntity`, `MKSpawnerBlockEntity`,
   `WorldStructureManager`, `MKStructure`, `StructureEvent`,
   `MKJigsawStructure`, `MKJigsawPlacement`.
5. Workspace service boundaries: `MKStructureWorkspaceService`,
   `MKWorkspacePlanner`, `MKWorkspaceScaffoldBuilder`,
   `MKWorkspaceStairBuilder`, export/import/mutation services.
6. Network packet handlers and commands, especially where server authority,
   creative-player restrictions, validation, or backups matter.

Good Javadocs in this module should emphasize:

- whether a class is server-only, client-only, or shared;
- whether a model is datapack codec-backed, NBT-backed, network-backed, or
  transient;
- what owns persistence and when state is synchronized;
- what lifecycle event calls a method;
- whether a method expects resolved registries, loaded chunks, generated
  structure metadata, or creative-player authority;
- which calls are safe to use from datapack reload, world tick, entity tick,
  network packet handling, or data generation.
