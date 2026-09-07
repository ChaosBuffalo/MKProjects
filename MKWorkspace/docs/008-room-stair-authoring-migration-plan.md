# Room Stair Authoring Migration Plan

## Status

This migration is effectively complete for the tower workspace flow.

The current system now uses:

- room-only runtime topology
- room-local stair authoring
- per-piece stair generation and clearing in the workspace UI
- generalized stair profile validation

This document is retained as a record of the migration and the resulting end state.

## Original Goal

Move from the old runtime model:

- room pieces plus embedded stair jigsaw pieces
- runtime `stair_insert_*` / stair subpiece pools

to a simpler authoring/runtime model:

- runtime uses only room-to-room topology pieces
- stairs are generated directly into the owning room variant/template
- each room category page in the workspace UI can generate, clear, and regenerate stairs for the selected variant using the current shaft configuration

## Current End State

### Runtime

Tower worldgen uses room pieces only:

- `entry`
- `floor_main`
- `boss_approach`
- `boss_cap`
- `basement_entry`
- `basement_main`
- `basement_cap`

Vertical topology uses only room-to-room connectors:

- `connect_up`
- `connect_down`
- boss connectors

Runtime stair child pieces are no longer required.

### Workspace Authoring

Stair shaft geometry is derived from room metadata and current tower shaft configuration.

Stairs are generated directly into the selected room piece or variant.

The stair builder only touches blocks inside the reserved shaft footprint.

Designers can:

- choose stair profile settings at workspace scope
- override stair generation settings per piece generation action
- clear and regenerate stairs for one exact room variant
- bulk-generate stairs for all stair-capable pieces

### UI

Each room category page shows stair actions only for eligible pieces.

The UI now supports:

- per-piece `Generate Stairs`
- per-piece `Clear Stairs`
- workspace-level `Generate All Stairs`
- piece status indicating whether stairs have already been generated

Legacy stair-only categories are no longer part of the active runtime workflow.

## What Changed

### 1. Stair Subpieces Were Removed From Runtime Topology

The old stair child pools and stair insert connectors were removed from the runtime tower graph.

Worldgen now progresses room-to-room only.

### 2. Stair Generation Was Retargeted To Room Shaft Filling

The stair builder no longer targets dedicated stair piece templates.

Instead it writes directly into room pieces:

- `entry`
- `floor_main`
- `boss_approach`
- `boss_cap`
- `basement_entry`
- `basement_main`
- `basement_cap`

### 3. Planner And Export Were Simplified

The tower planner and export path now reflect the room-only runtime model.

The workspace export and datagen path no longer depend on runtime stair pools.

### 4. Stair Validation Became Profile-Driven

Room, entry, and basement heights are now constrained by the active stair profile.

The system validates:

- repeating room phase compatibility
- aligned entry and basement heights
- allowed flat run lengths
- allowed stair widths
- valid shaft sizes

### 5. Boundary Bridge Support Was Added

The system no longer requires every reusable room height to hand off perfectly.

Bridgeable handoffs are allowed when they:

- do not overlap the next piece’s stair footprint
- can be completed with a small slab bridge

That widened the set of usable heights while still rejecting bad overlap cases.

## Remaining Notes

This migration removed runtime stair child templates from the active tower workflow, but stair generation is still modular at authoring time because:

- it only modifies the shaft footprint
- it can be cleared and regenerated independently
- it supports different rise profiles and widths without regenerating the rest of the room

## Acceptance Criteria Met

- Towers generate upward and downward correctly using room pieces only.
- `boss_cap` and `basement_cap` participate in room-local stair generation.
- No runtime stair child templates are required for tower generation.
- Each eligible room category page can generate and clear stairs for the selected variant.
- Stair generation modifies only the room’s reserved shaft footprint.
- Stair style can be changed per variant without regenerating the rest of the room.

## Main Files

- [MKTowerWorkspacePlanner.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/planner/MKTowerWorkspacePlanner.java)
- [MKWorkspaceStairBuilder.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/stairs/MKWorkspaceStairBuilder.java)
- [MKWorkspaceExportManifest.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/export/MKWorkspaceExportManifest.java)
- [MKStructureWorkspaceService.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/world/gen/workspace/MKStructureWorkspaceService.java)
- [MKWorkspaceScreen.java](/E:/MinecraftDev/MKProjects/MKNpc/src/main/java/com/chaosbuffalo/mknpc/client/gui/screens/MKWorkspaceScreen.java)
