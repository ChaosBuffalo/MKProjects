# Agent Notes For MKWorkspaceRuntime

Scope: this file applies to the `MKWorkspaceRuntime` module subtree.

## Module Role

MKWorkspaceRuntime is the shared, editor-free runtime support module for workspace-authored structures. It lets MKNpc and MKWorkspace use the same metadata, codecs, and layout behavior without making MKNpc depend on the editor.

This module must not depend on MKNpc or MKWorkspace. It may be depended on by both.

## Put Here

- Runtime-safe workspace model and codecs.
- `mk_workspace_exports` manifest DTOs, codecs, and source-file loading utilities.
- MKFloorLayoutSolver and layout records, enums, and settings used by live structure placement.
- Connector, jigsaw piece role, floor mask, and runtime metadata types shared by world generation and workspace export or rehydration.
- Small pure helpers for floor, pool, and manifest naming conventions.

## Do Not Put Here

- Screens, widgets, commands, packets, development blocks, or editor-only assets.
- Authoring saved state, mutation workflows, scaffold builders, backup/restore orchestration, or planner registries.
- MKNpc gameplay integration that performs placement or owns generated datapack consumption.
- Generic UI controls. Put those in MKWidgets.

## Editing Guidance

- Keep this module passive. Prefer immutable data, codecs, and deterministic helpers.
- Do not introduce references to Minecraft client UI, Forge event handlers, commands, packets, or authoring services.
- If a change needs MKNpc types, either keep the code in MKNpc or introduce a smaller runtime-neutral type here and adapt at the module boundary.
- If a change needs editor workflow state, keep it in MKWorkspace.
- When changing the split, update this file and `README.md` if the responsibility boundary changes.

## Useful Checks

- Compile this module after code changes: `.\gradlew.bat :MKWorkspaceRuntime:compileJava`
- For shared API changes, also run: `.\gradlew.bat :MKWorkspaceRuntime:compileJava :MKNpc:compileJava :MKWorkspace:compileJava`
- Before committing, inspect staged files so unrelated generated/resource churn is not included.
