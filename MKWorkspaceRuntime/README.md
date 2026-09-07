# MKWorkspaceRuntime

MKWorkspaceRuntime is the shared, editor-free runtime support module for workspace-authored structures. It exists so MKNpc and MKWorkspace can share stable metadata, codecs, and layout behavior without creating a dependency from MKNpc to the workspace editor.

The module should stay passive and safe for normal gameplay installs. It should not contain UI, commands, packets, development blocks, authoring saved state, or editor workflows.

## Responsibilities

- Runtime-safe workspace model and codecs.
- `mk_workspace_exports` manifest DTOs, codecs, and source-file loading utilities.
- MKFloorLayoutSolver and the layout records, enums, and settings needed by live structure placement.
- Connector, jigsaw piece role, floor mask, and runtime metadata types shared by world generation and workspace export or rehydration.
- Small runtime helpers for floor, pool, and manifest naming conventions.

## Dependency Rules

MKWorkspaceRuntime must not depend on MKNpc or MKWorkspace. It may be depended on by both modules.

Code in this module should avoid references to editor concepts unless the type is pure data required to preserve or interpret generated runtime metadata.

## Relationship To MKNpc

MKNpc uses MKWorkspaceRuntime for gameplay-safe structure metadata, procedural floor layout solving, and generated manifest parsing. MKNpc remains responsible for placing and generating structures in the world, runtime gameplay integration, and consuming generated datapack resources.

## Relationship To MKWorkspace

MKWorkspace uses MKWorkspaceRuntime so its import, export, rehydration, and data-generation paths use the same metadata and layout semantics as gameplay.

Data-generation orchestration belongs in MKWorkspace or in the content module generating the resources. MKWorkspaceRuntime should only keep pure runtime-safe data structures and utilities that MKNpc also needs.
