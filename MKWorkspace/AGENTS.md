# Agent Notes For MKWorkspace

Scope: this file applies to the `MKWorkspace` module subtree.

## Module Role

MKWorkspace is the optional authoring and editor module for workspace-based structure development. Keep editor workflow code here when it is not required for normal gameplay.

This module may depend on MKNpc, MKWorkspaceRuntime, and MKWidgets. MKNpc must not depend on MKWorkspace.

## Put Here

- Workspace editor screens, pages, preview views, and draft adapters.
- Workspace development blocks, block entities, commands, and editor packets.
- Authoring-only saved state, capabilities, services, import, backup, restore, and rehydration workflows.
- Planner orchestration, preflight, relayout, mutation, scaffold, and stair-builder workflows used by authoring or data generation.
- Workspace-specific assets, translations, and UI resources.

## Do Not Put Here

- Live gameplay structure placement or world generation logic needed without the editor.
- Runtime-safe manifest DTOs, codecs, layout records, or solver code shared with MKNpc. Put those in MKWorkspaceRuntime.
- Generic reusable UI controls. Put those in MKWidgets.
- Generated resources that belong to another content module. The toolchain can create them, but the packaged output should live with the module that owns the content.

## Editing Guidance

- Preserve the dependency boundary: do not add imports from MKNpc to MKWorkspace to make an editor feature work.
- If code must be shared by MKWorkspace and MKNpc, first decide whether it is pure runtime data or behavior. Runtime-safe code belongs in MKWorkspaceRuntime; generic UI belongs in MKWidgets.
- Keep UI and workflow changes out of MKNpc unless they are strictly needed for runtime gameplay.
- When changing the split, update this file and `README.md` if the responsibility boundary changes.

## Useful Checks

- Compile this module after code changes: `.\gradlew.bat :MKWorkspace:compileJava`
- For broader boundary changes, also run: `.\gradlew.bat :MKWorkspaceRuntime:compileJava :MKNpc:compileJava :MKWorkspace:compileJava`
- Before committing, inspect staged files so unrelated generated/resource churn is not included.
