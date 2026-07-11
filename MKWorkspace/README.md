# MKWorkspace

MKWorkspace is the optional authoring module for workspace-based structure editing and data generation. It contains the tools a builder or developer installs when they want to create, inspect, rehydrate, mutate, export, or regenerate structures.

The module is intentionally separate from the normal gameplay path. A player should be able to run structures generated from workspaces without loading the workspace editor, editor packets, commands, development blocks, or UI.

## Responsibilities

- Workspace editor screens, pages, previews, and draft adapters.
- Workspace development blocks and block entities.
- Workspace commands and editor packets.
- Authoring-only saved state, capabilities, and services.
- Import, backup, restore, and rehydration workflows.
- Mutation, preflight, relayout, scaffold, and stair-builder workflows.
- Canonical workspace planners and data-generation toolchain code.
- Editor-only assets, translations, and workflow resources.

## Dependencies

MKWorkspace may depend on MKNpc, MKWorkspaceRuntime, and MKWidgets.

MKNpc must not depend on MKWorkspace. This keeps runtime gameplay isolated from the editor and lets the core gameplay module be ported or loaded without carrying the authoring surface.

Reusable UI widgets that are not specific to workspace authoring belong in MKWidgets rather than in MKWorkspace.

## Runtime Data

Generated runtime resources should remain consumable without MKWorkspace installed. That includes structure data, runtime pool data, and the `data/<namespace>/mk_workspace_exports/*.json` metadata used to describe the workspace source.

MKWorkspace uses the shared manifest and layout APIs from MKWorkspaceRuntime so it can rehydrate or edit structures that are already present in an installed mod list. The generated resources themselves should live with the module that generated or packages them, not inside MKWorkspace by default.

## What Does Not Belong Here

- Live gameplay structure placement.
- Runtime world generation behavior.
- Runtime-safe codecs or metadata needed by MKNpc without the editor.
- Generic widgets that could be reused by other modules.
