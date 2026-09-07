# Workspace Preserving Catalog Relayout Plan

## Goal

Make workspace topology catalog edits preserve authored work whenever the existing authored templates can be matched to the requested workspace model.

Today many planner edits are treated as broad topology regeneration. A simple edit such as adding one floor main-room profile can route to destructive regeneration, which rebuilds the workspace scaffold and risks invalidating unrelated authored rooms, hallway templates, insert templates, structure blocks, signs, jigsaws, and template bindings.

The desired behavior is:

- Keep the workspace dev block anchor fixed.
- Diff the existing authored template catalog against the requested canonical catalog.
- Move matched existing templates into their new correct workspace positions.
- Scaffold only newly introduced template slots.
- Clear only removed or truly orphaned template slots.
- Preserve authored blocks and piece identity for every matched template.
- Use destructive regeneration only when the system cannot compute a defensible preservation plan.

## Terminology

- **Workspace anchor**: The dev block position stored in `MKStructureWorkspace.anchor()`. This must not move during relayout.
- **Catalog**: The set of canonical authoring template pieces produced by a planner for a workspace draft.
- **Template slot**: A stable logical planner target, normally identified by `MKWorkspacePlannerId`, topology slot metadata, base name, room profile id, and variant index.
- **Matched piece**: An existing `MKWorkspacePieceDefinition` that corresponds to a requested planned piece.
- **New piece**: A requested planned piece with no matching existing piece.
- **Removed piece**: An existing piece whose template slot is no longer present in the requested catalog.
- **Orphaned piece**: An existing authored piece that cannot be confidently matched and should require user confirmation or remap.
- **Relayout**: Physically moving existing authored blocks and sidecars to their newly computed preview positions while preserving their piece identity.

## Current Behavior

`MKWorkspaceFloorTopologyInvalidationAnalyzer` treats changes to room profile lists as broad topology changes:

- `mainRoomProfiles`
- `branchRoomProfiles`
- `branchCapProfiles`
- `mainCapApproachProfiles`
- `mainCapProfiles`

Broad topology changes currently invalidate room envelopes, connector graph, hallway routing, hallway pieces, template bindings, sidecar blocks, and runtime metadata. The UI then treats the update as not covered by a safe live mutation and sends the user to the destructive regenerate confirmation page.

This behavior is safe in the sense that it avoids accidental partial mutation, but it is too conservative for authored-template catalog changes.

## Desired User Experience

### Add A Template Category

When the user adds a main room, branch room, branch cap, main cap approach, main cap, hallway, insert, keep corner, or similar authored template category:

1. The workspace screen marks the draft dirty.
2. On apply, the server builds a preservation plan.
3. Existing authored templates remain authored and keep their identities.
4. Existing pieces may move if the catalog grid needs to reflow.
5. New category rows appear in the proper position in the workspace catalog.
6. Only new category pieces are scaffolded from scratch.
7. The workspace screen reopens at the same anchor.

### Remove A Template Category

When the user removes an authored template category:

1. The server builds a preservation plan.
2. Pieces belonging to removed slots are listed in confirmation text.
3. Surviving pieces are moved into their proper compacted positions.
4. Only removed pieces are cleared.
5. Any ambiguous matches become orphan/remap prompts rather than silent deletion.

### Rename Or Re-Key A Template Category

Renames should not be treated blindly as delete plus add.

If the system sees one removed slot and one new slot with compatible dimensions, role, profile kind, or base name, it should offer a remap:

- `old planner id -> new planner id`
- preserve existing authored blocks
- update template metadata and structure block names

If the user rejects the remap, the operation becomes add plus remove.

### Resize A Template Category

Resizing should preserve work when possible, but shrink/crop edits are destructive for the affected physical pieces.

- Expansion is safe if all old authored/export bounds fit inside the new export bounds.
- Shrinking or cropping a template slot should be treated as clearing and rebuilding that affected physical piece.
- Connector/sidecar positions should be regenerated or shifted for the affected template slots.
- Unrelated templates should still be preserved and relaid out.

## Planner UI Actions

The new behavior should apply to any UI action that changes the authored template catalog or the placement of catalog entries.

| UI action | New behavior | Notes |
|---|---|---|
| Add floor room profile: `MAIN_ROOM`, `BRANCH_ROOM`, `BRANCH_CAP`, `MAIN_CAP_APPROACH`, `MAIN_CAP` | Catalog diff + relayout | Existing profile rows move if needed; only the new profile row scaffolds fresh. |
| Remove floor room profile for those same kinds | Catalog diff + relayout | Clear only templates belonging to the removed profile. |
| Reorder floor room profiles, if exposed later | Catalog diff + relayout | Match by stable profile id, not list index. |
| Rename or re-key a floor room profile | Remap-assisted relayout | Offer old profile to new profile remap before clearing anything. |
| Change floor room profile dimensions | Resize-aware relayout | Preserve when expanding; shrink/crop clears and rebuilds affected physical pieces only. |
| Change floor room exits | Affected-slot patch + relayout if bounds change | Preserve authored blocks; refresh connectors, jigsaws, and markers. |
| Toggle `mainCapApproachEnabled` | Catalog diff + relayout | Add or remove main-cap-approach rows only. |
| Toggle `mainHallwaysEnabled` or `branchHallwaysEnabled` | Catalog diff + relayout | Add or remove hallway rows only. |
| Enable or disable floor link inserts | Catalog diff + relayout | Add or remove insert-family template rows. |
| Change selected floor insert family | Remap-assisted relayout | Preserve if old and new insert slots can be mapped. |
| Add room family definition | Catalog diff + relayout | Add only that family slot. |
| Remove room family definition | Catalog diff + relayout | Clear only that family slot. |
| Change a family topology slot | Remap-assisted relayout | Usually old slot to new slot remap. |
| Rename a family base name | Remap-assisted relayout | Preserve content and update export naming if confirmed. |
| Add, remove, or change family exits | Affected-slot patch + relayout if bounds change | Refresh connector sidecars for affected family. |
| Add linear-run family | Catalog diff + relayout | Add only the new run/hallway slot. |
| Remove linear-run family | Catalog diff + relayout | Clear only removed run/hallway slots. |
| Change linear-run id, topology slot, or kind | Remap-assisted relayout | Preserve if identity can be mapped. |
| Change linear-run dimensions, opening, or supported shapes | Resize-aware relayout | Preserve when expanding; shrink/crop clears and rebuilds affected physical pieces only. |
| Add insert family | Catalog diff + relayout | Add only the new insert slot. |
| Remove insert family | Catalog diff + relayout | Clear only removed insert slots. |
| Change insert-family id, kind, or dimensions | Remap/resize-aware relayout | Preserve when expanding or remapping safely; shrink/crop clears and rebuilds affected physical pieces only. |
| Add opening profile | No relayout unless referenced | If referenced by generated slots, patch affected connectors. |
| Remove opening profile | Remap-assisted if referenced | If unreferenced, metadata-only. If referenced, prompt for replacement profile. |
| Edit opening profile dimensions or path-kind flags | Affected-slot patch + relayout if bounds change | Preserve blocks; refresh affected openings/connectors. |
| Change vertical stack floor counts | Catalog diff + relayout | Add or remove floor-section template groups. |
| Change vertical stack dimensions or heights | Resize-aware relayout | Preserve when expanding; shrink/crop clears and rebuilds affected physical pieces only. |
| Toggle walled-keep unique/shared corner towers | Catalog diff + relayout | Add/remove unique corner groups, preserve surviving center/shared/corner templates. |
| Walled-keep wall height, unit span, or passage width | Resize-aware relayout | Preserve perimeter and gatehouse templates when expanding; shrink/crop clears affected physical pieces only. |
| Walled-keep courtyard content slot settings | Catalog diff + relayout | Add, remove, or resize courtyard content slots without touching unrelated keep templates. |
| Switch planner type | Remap-assisted or destructive fallback | Try remap suggestions first; destructive regenerate only if no defensible match exists. |
| Reset topology defaults | Remap-assisted or destructive fallback | Treat as a large catalog diff with clear confirmation. |

The new behavior should not replace existing safe paths for pure metadata/material edits:

- workspace identity rename
- palette swap
- preview margin relayout
- link rendering refresh
- hallway routing regeneration
- layer locking
- block swap
- backup/restore

Those operations should continue using their narrower existing services unless they also change the authored catalog.

## Proposed Architecture

### 1. Catalog Diff Model

Add a model that compares existing workspace pieces to requested planned pieces.

Suggested types:

```java
public record MKWorkspaceCatalogRelayoutPlan(
        List<MatchedPiece> matchedPieces,
        List<NewPiece> newPieces,
        List<RemovedPiece> removedPieces,
        List<OrphanedPiece> orphanedPieces,
        List<MKWorkspaceTemplateRemapSuggestion> remapSuggestions,
        MKWorkspaceMutationSafety safety,
        List<String> warnings
) {}
```

Important matching inputs:

- `MKWorkspacePieceDefinition.plannerId()`
- `MKWorkspacePieceDefinition.pieceName()`
- `workspace_topology_slot_id`
- `workspace_base_name`
- `workspace_variant_index`
- floor stack id and floor role tags
- room profile id tags
- linear run family id tags
- insert family id tags
- keep slot id tags

Matching should prefer stable logical ids over list position.

### 2. Stable Template Slot Identity

The feature depends on deterministic identities. If any planned piece currently only has a generated name with an index, add explicit tags so it can survive reordering:

- `workspace_template_slot_id`
- `workspace_template_kind`
- `workspace_profile_id`
- `workspace_floor_stack_id`
- `workspace_floor_role`
- `workspace_family_id`
- `workspace_linear_run_id`
- `workspace_insert_family_id`
- `workspace_keep_slot_id`

The exact tag set can be smaller, but every authored template slot needs a stable key.

### 3. Relayout Service

Generalize `MKWorkspacePieceRelayoutService`.

The current service already proves the core mechanism:

- compute new `MKWorkspaceGridLayout` placements
- snapshot source blocks and block entities
- clear source locations
- place snapshots at destination locations
- shift `worldOrigin`, bounds, structure block position, sign position, marker positions
- preserve piece ids

Extend this from preview-margin-only to catalog diffs:

```java
public Optional<RelayoutResult> relayoutCatalog(
        ServerLevel level,
        MKStructureWorkspace existing,
        MKStructureWorkspace requested,
        List<MKPlannedPiece> requestedCanonicalPieces,
        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps
);
```

The service should:

1. Write a backup before mutation.
2. Build a catalog diff plan.
3. Validate no destination overlap.
4. Snapshot all matched source pieces.
5. Snapshot removed pieces for backup metadata if useful.
6. Clear matched source areas and removed source areas.
7. Place matched pieces at destination positions.
8. Scaffold new pieces with `MKWorkspaceScaffoldBuilder`.
9. Refresh sidecars/connectors for affected pieces.
10. Update the workspace model and capability.
11. Sync the dev block entity.

### 4. Sidecar Refresh

Moved pieces can shift sidecars by delta. Pieces whose connectors changed need sidecar regeneration.

Sidecars include:

- structure block
- sign
- connector markers
- jigsaw blocks
- generated stair positions
- connector definitions
- export bounds
- preview bounds

Relayout-only pieces can reuse shifted sidecars. Connector-changing pieces need connector/jigsaw/marker refresh while preserving interior authored blocks where possible.

### 5. Resize Handling

Add resize analysis before moving/scaffolding.

For each matched piece:

- Compare old export bounds to new export bounds in local coordinates.
- If new bounds contain old bounds, expansion is safe.
- If new bounds crop old bounds, mark the affected physical piece as destructive.
- Do not scan cropped regions to determine whether the crop is safe.
- The confirmation should list affected physical pieces that will be cleared and rebuilt.
- Surviving unrelated pieces should still be preserved and relaid out.

Resize-aware relayout should not silently delete authored blocks. A crop is allowed only when the user confirms that the affected physical pieces will be rebuilt.

### 6. Invalidation Analyzer

Replace the current broad classification for list/profile/catalog edits with a narrower classification.

Suggested safety values:

- `SAFE_RELAYOUT` for pure movement with no slot removal.
- `SAFE_EXPANSION` for added slots or expanded matched slots.
- `CONDITIONALLY_SAFE_TOPOLOGY_PATCH` for removals, remaps, connector changes, or confirmed shrink/crop of affected pieces.
- `DESTRUCTIVE_REGENERATE` only when matching/remap fails or locked layers make the patch impossible.

The analyzer should distinguish:

- route/rules changes that only affect runtime selection
- link rendering changes
- hallway routing changes
- catalog additions/removals/remaps
- true structural topology changes that cannot preserve authored templates

### 7. UI Confirmation

Replace the generic destructive message for catalog diffs.

The confirmation should show:

- number of matched templates preserved
- number of templates moved
- number of new templates scaffolded
- number of removed templates that will be cleared
- any orphaned templates
- any shrink/crop affected templates that will be rebuilt
- remap suggestions

Example:

```text
Preserve Workspace Catalog

32 templates will be preserved.
7 templates will move to new catalog positions.
1 new main-room template row will be scaffolded.
0 authored templates will be removed.
```

For removal:

```text
Preserve Workspace Catalog

29 templates will be preserved.
6 templates will move to new catalog positions.
2 templates from "main_room_tall" will be removed.
```

For remap:

```text
Possible remap:
main_room_large -> main_room_elite

Preserve authored content for this template?
```

### 8. Layer Locking

Layer locks should block only the affected operation.

If a removed piece would clear blocks and the relevant layer is locked, block the removal. If unrelated pieces are locked but not changing, do not block the entire relayout.

Suggested affected layers:

- `PREVIEW_LAYOUT` for movement
- `SCAFFOLD_BLOCKS` for new scaffold
- `SIDECAR_BLOCKS` for signs, markers, jigsaws, structure blocks
- `TEMPLATE_BINDINGS` for remap, add, remove
- `ROOM_ENVELOPES` for resize
- `CONNECTOR_GRAPH` for exit/connector changes
- `RUNTIME_METADATA` for all catalog patches

## Implementation Phases

### Phase 1: Stable Slot Identity

- Add stable catalog tags to planned pieces emitted by tower floor-plan planners.
- Add stable catalog tags to walled-keep planned pieces.
- Ensure exported/imported workspace manifests preserve these tags.
- Add tests for stable ids across add/remove/reorder cases.

### Phase 2: Catalog Diff Planner

- Add `MKWorkspaceCatalogDiffService`.
- Match existing pieces to requested planned pieces by stable slot identity.
- Detect additions, removals, orphans, and remap candidates.
- Produce a report usable by preflight and UI confirmation.

### Phase 3: Generalized Relayout Service

- Extend `MKWorkspacePieceRelayoutService` or add `MKWorkspaceCatalogRelayoutService`.
- Move matched pieces by snapshot/clear/place.
- Scaffold new planned pieces.
- Clear only removed pieces.
- Preserve existing piece ids for matched pieces.
- Update workspace piece metadata atomically after block moves.

### Phase 4: Resize And Sidecar Patching

- Add local-bound resize analysis.
- Implement connector/sidecar refresh for affected pieces.
- Add shrink/crop affected-piece confirmation data.

### Phase 5: Invalidation And Safe Apply Routing

- Update `MKWorkspaceFloorTopologyInvalidationAnalyzer`.
- Update `MKStructureWorkspaceService.createOrUpdateWorkspace` to route catalog patches before destructive regenerate.
- Add server-side locked-layer checks scoped to the catalog relayout plan.
- Keep existing palette, identity, margin, hallway-routing, and link-rendering paths intact.

### Phase 6: UI Confirmation

- Add a preserving catalog confirmation page or extend `WorkspaceGenerateConfirmPage`.
- Display preserved/moved/new/removed/orphan/remap counts for physical authoring pieces only.
- Let users accept remap suggestions individually or accept all safe remap suggestions at once.
- Use destructive confirmation only for true fallback cases.

### Phase 7: Tests

Add unit and game tests for:

- add main room profile preserves existing authored rooms
- remove main room profile clears only removed profile templates
- add branch room profile relayouts existing rows without identity loss
- rename profile with accepted remap preserves authored blocks
- profile shrink clears and rebuilds only affected physical pieces after confirmation
- walled-keep unique corner toggle preserves unaffected corner/center templates
- insert-family add/remove preserves unrelated room and hallway templates
- linear-run add/remove preserves unrelated templates
- locked removed template layer blocks removal
- locked unrelated layer does not block safe relayout

## Acceptance Criteria

- Adding an authored template category no longer forces destructive regeneration.
- Removing an authored template category clears only that category's pieces.
- Existing authored template blocks survive catalog reflow.
- Existing matched piece ids survive catalog reflow.
- The workspace anchor remains unchanged.
- New template rows appear in their correct organized grid positions.
- Remap suggestions are shown for likely rename/re-key operations.
- Destructive regenerate remains available only as an explicit fallback.

## Open Questions

Resolved decisions:

- Removed pieces should be cleared immediately after confirmation. The pre-mutation backup is the recovery path.
- The remap UI should support both per-remap approval and an `Accept All Safe Remaps` action.
- Structure block names should be updated immediately during relayout/remap, not deferred until export.
- The relayout confirmation should show only physical authoring pieces. Derived or reused logical pieces should remain hidden from the user-facing counts and lists.

- Shrink/crop should be treated as destructive for the affected physical pieces. The implementation should not spend server time scanning cropped regions to prove whether a shrink is safe.

## Shrink/Crop Policy

Shrink/crop analysis should be geometric, not block-content based.

For each matched physical piece:

1. Compare the old local export bounds to the new local export bounds.
2. If the new bounds fully contain the old bounds, preserve and move the piece.
3. If the new bounds crop the old bounds, mark that physical piece as `rebuild_required`.
4. Confirmation should list all `rebuild_required` physical authoring pieces.
5. After confirmation, clear and scaffold only those affected pieces.
6. Preserve and relayout every other matched physical piece.

This avoids expensive and ambiguous block scans. It also sets the correct user expectation: changing a template slot to a smaller shape is destructive for that template slot, even if some cropped blocks are currently air.
