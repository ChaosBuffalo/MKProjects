La# Workspace Module Split Notes

## Shared widgets

- Move generic editor widgets from `MKNpc` into `MKWidgets` before extracting `MKWorkspace`.
- Known candidates:
  - `MKIntegerSlider`
  - item stack picker widgets
  - any generic creative/block/item picker wrappers that are not workspace-specific
- Keep workspace-specific composition, labels, and callbacks in `MKWorkspace`; keep reusable primitive picker/grid/slider controls in `MKWidgets`.

## Planner/runtime boundary

- Prefer moving authoring and data-generation planners to `MKWorkspace` if the data-gen environment is expected to load `MKWorkspace`.
- Keep only code required by actual player-runtime worldgen in `MKNpc`.
- Current runtime structure placement directly depends on `MKFloorLayoutSolver`; that is runtime procedural layout code, not just data-generation scaffolding.
- If `MKFloorLayoutSolver` also moves to `MKWorkspace`, the generated data must fully materialize the floor layouts or provide another runtime-only layout format/solver in `MKNpc`.
- Current `MKNpc` data registries (`NpcStructurePools`, `NpcStructures`) read workspace export manifests during registry-set generation. Those manifest-to-runtime-data builders can move to `MKWorkspace` as part of the data-gen toolchain, with generated JSON/NBT resources emitted back into the runtime datapack/module.
