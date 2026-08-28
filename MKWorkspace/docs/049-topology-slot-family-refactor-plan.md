# Topology Slot and Content Family Refactor Plan

## Status

Implemented as a compatibility-first refactor. Existing workspace codecs and planner-specific definition names remain
readable, while explicit piece metadata is now authoritative for content selection. The compatibility layer can be
removed after saved workspaces have naturally been rewritten with explicit metadata or a dedicated bulk migration is
introduced.

## Implemented Shape

The implementation deliberately uses a runtime-safe metadata header on each physical piece instead of immediately
replacing every planner-specific definition codec:

```text
workspace_content_slot_id
workspace_content_family_id
workspace_family_weight
workspace_family_enabled
workspace_template_purpose
workspace_content_variant_id
workspace_variant_weight
workspace_variant_enabled
```

`MKWorkspaceContentSelectionTags` is the compatibility boundary. It reads these explicit values first and supplies
legacy inference only for old workspace data. Planner generation writes explicit values for all new pieces, and
relayout/regeneration preserves author-owned values instead of regenerating them from planner family names.

`MKWorkspaceContentCandidateResolver` is the single selection implementation. Manifest generation resolves concrete
weighted candidates once; preview and MKNpc runtime pool registration consume those manifest entries. This provides
identical canonical fallback, scaffold exclusion, family enablement, variant enablement, and hierarchical weights in
both paths.

Authoring selection changes use `mkworkspace:content_selection`. Its typed payload supports variant promotion, moving
a variant between compatible families, family and variant weights, family and variant enablement, and explicit
template-purpose changes. Each request follows prepare/review/confirm/backup/apply, preserves physical piece identity,
and reports the affected slot, family, template, old/new values, fallback effects, and invalidated layers.

The current `MKWorkspaceInsertFamilyDefinition` name is retained for save compatibility, but its ID is treated as the
insert topology slot contract. Independently promoted content families bind to that slot through
`workspace_content_slot_id`; runtime pools are compiled by slot rather than by content-family identity.

## Summary

Refactor workspace content selection around three explicit levels:

```text
topology slot -> weighted content family -> weighted content variant
```

A topology slot is a planner-owned placement contract. A content family is an author-owned category of
content that can satisfy one topology slot. A variant is a concrete runtime realization of a family.

Every slot may also have an authoring scaffold. The scaffold visualizes bounds, sockets, connectors, or
other slot requirements, but it is not content and must never be selected by preview or runtime generation.

Every content family has one canonical template. The canonical is useful for authoring and for creating
variants. Selection follows these rules:

- if a family has one or more eligible variants, preview and runtime select only from those variants;
- if a family has no eligible variants, preview and runtime may fall back to its canonical template;
- slot scaffolds and other data-only templates are never selection candidates;
- family and variant weights are applied hierarchically before the final slot pool is compiled.

This behavior keeps work-in-progress structures functional before designers have populated every family
with variants, without allowing empty bounds/scaffold templates to appear in generated structures.

This document supersedes the portions of
[047-workspace-insert-authoring-ux-plan.md](047-workspace-insert-authoring-ux-plan.md) that describe an
insert family as both the socket contract and the variant container. The insert authoring workflow remains
valid, but its saved vocabulary changes from insert family to insert slot plus content families.

## Goals

- Give topology slots, content families, canonical templates, and variants distinct identities and jobs.
- Use the same content-selection model for rooms, linear runs, inserts, and future topology types.
- Let multiple independently managed families satisfy the same topology slot.
- Let designers promote an existing variant into a new family without losing authored blocks.
- Support stable, explicit weights at both the family and variant levels.
- Keep preview and runtime candidate eligibility in parity.
- Permit canonical runtime fallback while a family has no eligible variants.
- Ensure slot scaffolds and data-only templates can never enter runtime pools.
- Preserve authored content during migration and report every identity or template change through preflight.
- Make the final pool compiler generic rather than planner- or insert-specific.

## Non-Goals

- Replacing Minecraft's jigsaw placement system.
- Making unrelated topology layout and graph-solving behavior data-driven.
- Allowing a single saved family to bind to several unrelated slots in the first implementation.
- Treating list order or a numeric variant index as durable identity.
- Automatically inferring the designer's ideal long-term family grouping from arbitrary authored content.

## Terminology and Ownership

### Topology slot

A topology slot describes structural demand. The active planner owns its meaning and stable ID.

Examples:

```text
tower.primary.main_floor
keep.corner.north_east.top_cap
fire_shrine_platform_contents
keep.courtyard.path.straight
```

A slot owns or references:

- its stable slot ID;
- region and topology role;
- slot kind;
- fixed, optional, ranged, or derived occurrence behavior;
- its compatibility contract;
- connector, socket, and attachment requirements;
- an optional authoring scaffold;
- planner-specific constraints needed to validate candidate families.

Slot repeat behavior describes topology occurrences. It does not describe the number of content families
assigned to the slot.

### Slot scaffold

A slot scaffold is a physical authoring aid for the slot contract. It may show bounds, an empty shell,
jigsaws, sockets, masks, or signs. It is always data-only:

```text
template purpose: slot_scaffold
preview eligible: false
runtime eligible: false
```

For example, the current blank `fire_shrine_platform_contents` template should become the scaffold for
the `fire_shrine_platform_contents` slot. It should not remain a zero-content family.

If a designer intentionally wants an empty result to be selectable, that must be represented by a real
content family with a placeable canonical template, not by making the slot scaffold placeable.

### Content family

A content family is an author-managed content category bound to exactly one topology slot. It owns:

- a stable family ID and display name;
- the topology slot ID it fulfills;
- a positive family weight;
- enabled/disabled state;
- exactly one canonical template;
- zero or more variants;
- family-level settings and overrides that are legal for the slot kind.

Examples for `fire_shrine_platform_contents`:

```text
gazebo_family
lava_fountain_family
```

A family is not a topology occurrence and is not a synonym for a runtime pool. Several families can
contribute candidates to the pool compiled for one slot.

### Canonical template

The canonical template is the representative authored form of a family. It provides:

- the source from which new variants can be cloned;
- the family's basic geometry and connector contract;
- a useful preview target during early authoring;
- a runtime fallback while no eligible variants exist.

A canonical is selected only when the family has no eligible variants. As soon as one or more enabled,
valid variants exist, the canonical is excluded from both preview and runtime selection.

This is a fallback rule, not an additional weighted entry. A canonical and its variants are never mixed in
the same resolved candidate set.

### Content variant

A content variant is a concrete, placeable realization of its family. It owns:

- a stable variant ID;
- a physical piece/template ID;
- a positive variant weight;
- enabled/disabled state;
- instance-oriented authored content such as entities, loot, encounters, or decoration;
- optional variant-level metadata explicitly supported by the family kind.

Numeric `variantIndex` may remain as an export/display ordering field during migration, but it must stop
being the variant's durable identity.

## Target Example

The current fire shrine platform content should evolve into:

```text
topology slot: fire_shrine_platform_contents
  scaffold: fire_shrine_platform_contents_template [slot_scaffold]

  family: gazebo_family [family weight 3]
    canonical: fire_shrine_gazebo [canonical fallback]
    variant: gazebo_quiet [variant weight 1]
    variant: gazebo_occupied [variant weight 2]

  family: lava_fountain_family [family weight 1]
    canonical: fire_shrine_lava_fountain [canonical fallback]
    variant: fountain_guarded [variant weight 1]
```

Before any variants are created, runtime and preview can select the gazebo and fountain canonicals. After
the gazebo variants are created, the gazebo canonical is no longer selected, while the fountain canonical
continues to act as that family's fallback until `fountain_guarded` exists and is enabled.

The blank slot scaffold is never selected in either state.

## Proposed Runtime-Safe Model

The exact Java decomposition can be adjusted during implementation, but the persisted model should make
the boundaries explicit.

### Common family binding

Introduce a shared runtime-safe family header rather than duplicating identity and selection policy in
room, linear-run, and insert records:

```text
MKWorkspaceFamilyBinding
  familyId: stable string
  displayName: string
  topologySlotId: stable string
  weight: positive int
  enabled: boolean
  canonicalTemplateId: stable template/piece identity
```

Existing kind-specific definitions may embed this header:

```text
MKWorkspaceRoomFamilyDefinition
MKWorkspaceLinearRunFamilyDefinition
MKWorkspaceInsertContentFamilyDefinition
```

This avoids one oversized union of every room, corridor, and insert setting while giving generic systems a
common family identity and selection API.

### Template purpose

Make template intent explicit in persisted piece metadata:

```text
MKWorkspaceTemplatePurpose
  SLOT_SCAFFOLD
  FAMILY_CANONICAL
  FAMILY_VARIANT
  DERIVED_DATA_ONLY
```

Eligibility is derived centrally from purpose and family state. Callers must not infer it from
`variantIndex`, `workspace_piece_kind`, the presence of an insert tag, or a piece-name suffix.

`DERIVED_DATA_ONLY` covers generated masks, helper templates, or future metadata-bearing physical artifacts
that are neither slot scaffolds nor family content.

### Stable variant identity

Add stable variant identity and selection data to physical pieces or to a family-owned variant record:

```text
MKWorkspaceFamilyVariant
  variantId: stable string
  familyId: stable string
  pieceId: UUID/stable piece reference
  weight: positive int
  enabled: boolean
  displayOrder: int
```

Prefer family/variant IDs for matching during relayout, import, export, promotion, and deletion. Preserve
piece UUIDs when physical authored content is moved between families.

### Insert slot contract

The current `MKWorkspaceInsertFamilyDefinition` primarily describes the contract targeted by a socket:

- kind;
- width, height, and depth;
- attachment face and offsets;
- template jigsaw final state;
- the pool ID derived from `familyId`.

Replace or migrate it to an insert slot contract, for example:

```text
MKWorkspaceInsertSlotDefinition
  topologySlotId
  kind
  dimensions
  attachment contract
  scaffoldTemplateId
```

Host sockets target the slot pool. Insert content families bind to the slot. Pool IDs must be derived from
the topology slot ID, not from one content family ID.

### Slot, role, and settings lineage

Stop copying a slot ID into `workspace_topology_role_id`. The planner schema should resolve the actual role
for the slot and write both identities independently.

Replace ambiguous family ancestry fields with explicit concepts:

- `topologySlotId`: concrete slot binding;
- `slotArchetypeId`, when a generated slot inherits structural behavior from an archetype;
- `familySettingsSourceId`, when a generated family inherits settings from another family or shared family
  definition.

Do not use `sourceTopologySlotId` as both stable placement identity and family/template inheritance.

## Candidate Resolution

Create one runtime-safe resolver used by validation, preview, manifest generation, export, and tests.

Conceptual API:

```text
resolveSlot(slot, families, pieces, mode) -> ResolvedSlotPool
```

Resolution for each enabled family bound to the slot:

1. Validate that the family satisfies the slot contract.
2. Collect enabled, valid `FAMILY_VARIANT` pieces for the family.
3. If variants are available, use only those variants.
4. Otherwise, use the valid `FAMILY_CANONICAL` piece as the sole fallback candidate.
5. Never include `SLOT_SCAFFOLD` or `DERIVED_DATA_ONLY` pieces.
6. Omit an invalid/unavailable optional family with a diagnostic.
7. Fail a required slot if no eligible family candidates remain.

"No variants available" means that no variant is simultaneously present, enabled, valid, and compatible.
Disabled or invalid variants do not prevent canonical fallback.

Preview and runtime must call the same eligibility resolver. Preview may use a deterministic seed and emit
additional diagnostics, but it must not construct a different candidate set.

## Hierarchical Weight Semantics

Weights represent two decisions:

1. Choose a family according to family weights.
2. Choose an eligible candidate within that family according to variant weights.

When a canonical is used as fallback, it is the family's sole candidate and therefore has conditional
probability `1`.

For example:

```text
gazebo family weight: 3
  quiet variant weight: 1
  occupied variant weight: 2

fountain family weight: 1
  guarded variant weight: 1
```

The intended probabilities are:

```text
quiet gazebo     = 3/4 * 1/3 = 1/4
occupied gazebo  = 3/4 * 2/3 = 1/2
guarded fountain = 1/4 * 1/1 = 1/4
```

Do not implement final entry weight as only `familyWeight * variantWeight`; doing so makes a family's total
probability depend on the sum or number of its variants.

The manifest should preserve the hierarchy:

```text
ResolvedSlotPool
  topologySlotId
  families:
    familyId
    familyWeight
    candidates:
      templateId
      variantId or canonical fallback marker
      candidateWeight
```

If the final Minecraft template pool must be flat, compile the rational probabilities into proportional
positive integer weights, reduce them by their greatest common divisor, and validate that the result fits
the supported pool weight range. Keep this flattening at the export boundary so authoring and preview do
not lose the two-stage meaning.

All migrated weights default to `1`. Use an explicit `enabled` field; do not overload weight `0` to mean
disabled.

## Planner Responsibilities

Each planner should:

- declare stable topology slots and their actual roles;
- provide the compatibility contract for each slot kind;
- create or describe slot scaffolds;
- validate planner-specific family settings against slot contracts;
- produce topology occurrences and connectors that target slot pools.

Each planner should not:

- build its own family/variant fallback rules;
- infer families from piece names;
- add canonical and variant templates directly to runtime pools;
- special-case weight resolution;
- use a content family ID as the topology slot or role ID.

The generic resolver and exporter should own family eligibility, canonical fallback, and pool compilation.

## Authoring Workflows

### Create family for slot

The user selects a topology slot and creates a family. The operation:

- creates a stable family ID;
- binds it to the selected slot;
- creates or clones a canonical template compatible with the slot;
- assigns default family weight `1`;
- reports the new family and canonical physical template in preflight.

If the slot has a scaffold, cloning it may initialize the canonical geometry, but the cloned canonical must
be retagged as `FAMILY_CANONICAL`. The scaffold itself remains data-only.

### Create variant from canonical

The operation creates a stable variant ID, clones the family canonical, assigns default variant weight `1`,
and marks the new physical piece as `FAMILY_VARIANT`. Once applied, the canonical stops being a selection
candidate because an eligible variant now exists.

The preflight summary must explicitly state that the family's resolved pool changes from canonical fallback
to variant selection.

### Promote variant to family

Promotion is a first-class, preserving change operation. Given a source variant and target family details,
it should:

1. Validate that the new family can fulfill the same target slot.
2. Create the new stable family identity.
3. Remove the piece from its old family's variant membership.
4. Preserve the piece UUID, physical blocks, and catalog position when possible.
5. Retag/rebind the piece as the new family's canonical.
6. Assign the new family weight and reset canonical selection semantics.
7. Recompute both affected family candidate sets and the slot pool.
8. Invalidate template bindings, preview layout, runtime metadata, and export-derived state as required.

The operation must not silently clone or delete blocks. If the user requests a copy instead of a move, use a
separate "Create family from variant copy" operation with distinct effects.

### Move variant between families

Support moving a variant between compatible families bound to the same slot. Moving across slots requires
full compatibility validation and should initially be a separate advanced operation.

### Change weights and eligibility

Family weight, variant weight, and enabled state are persistent workspace changes. They must use the shared
change coordinator even when no blocks move because they materially change runtime output.

## Change-System and Backup Requirements

Every operation in this refactor must use the workflow documented in
[048-workspace-change-system.md](048-workspace-change-system.md):

```text
request -> prepare -> review -> confirm -> backup -> apply
```

Required operation coverage includes:

- create, delete, or rename a family;
- promote a variant to a family;
- create a family from a variant copy;
- move a variant between families;
- create, delete, rename, enable, or disable a variant;
- change family or variant weights;
- change template purpose;
- change slot contracts or family-slot bindings;
- migrate legacy slot/family data.

Prepared summaries must name:

- every affected topology slot;
- every created, deleted, or updated family;
- every canonical, variant, scaffold, or helper template affected;
- old and new family/slot ownership;
- old and new human-readable weights and enabled state;
- whether a canonical begins or stops acting as fallback;
- every physical template that moves, is cloned, or is preserved;
- all invalidated generated layers.

Existing-workspace mutations require a backup immediately before apply. Prepared operations must preserve
physical authored templates wherever the identity change does not require rebuilding them.

## Validation Rules

Add shared validation for these invariants:

- slot IDs are stable and unique within the planner/workspace scope;
- family IDs are stable and unique within the workspace;
- variant IDs are stable and unique within their family, preferably within the workspace as well;
- every family references an existing active or valid archetype slot;
- every family has exactly one canonical template;
- every variant references exactly one family;
- slot scaffolds are never family canonicals or variants;
- family and variant weights are positive;
- disabled families and variants are excluded from resolution;
- family canonical and variants satisfy the slot compatibility contract;
- a family with eligible variants resolves to variants only;
- a family without eligible variants resolves to its canonical when the canonical is valid;
- a required slot has at least one resolved runtime candidate;
- pool compilation contains no data-only template;
- topology role IDs resolve from schema roles instead of being copied from slot IDs.

Validation should distinguish authoring diagnostics:

- error: a required slot has no placeable family candidate;
- error: a selected piece violates the slot contract;
- warning: an enabled family is unavailable and omitted because both its variants and canonical are invalid;
- warning: a family is currently using canonical runtime fallback;
- informational: a family has no variants yet but remains functional through its canonical.

## Migration Strategy

### Versioning

Add an explicit workspace content-model version. Decode old data into a legacy representation, build a
migration plan against authoritative workspace pieces, and apply it only through the change coordinator.

Do not let codec defaults silently reinterpret old physical pieces without a reviewed migration summary.

### Existing room families

For each existing room family:

- retain `baseName` as the initial stable family ID where valid;
- retain `topologySlotId` as its slot binding;
- make variant zero the family canonical;
- convert pieces with `variantIndex > 0` to stable family variants;
- assign family and variant weights of `1`;
- preserve physical piece UUIDs and blocks;
- translate source/settings topology ancestry into explicit slot archetype or family settings lineage.

Room families with no variants become functional through canonical runtime fallback.

### Existing linear-run families

Apply the same family/canonical/variant model while retaining linear-run-specific compatibility settings.
Planner-generated data-only masks or helper pieces must receive `DERIVED_DATA_ONLY`, not family membership.

### Existing insert families

For each existing insert family:

1. Create an insert topology slot using the existing family ID as the initial slot ID.
2. Convert the blank contract/bounds template to `SLOT_SCAFFOLD`.
3. Convert each existing placeable variant into a separate family canonical bound to that slot.
4. Derive stable family IDs from piece/base names with collision-safe, reviewable remaps.
5. Assign each new family weight `1`.
6. Preserve the existing physical templates and use canonical fallback so runtime behavior remains available.
7. Retarget host sockets from the old family pool to the new slot pool if the serialized pool identity changes.

For the fire shrine example:

```text
legacy insert family: fire_shrine_platform_contents
  variant: fire_shrine_gazebo
  variant: fire_shrine_lava_fountain

migrated slot: fire_shrine_platform_contents
  scaffold: fire_shrine_platform_contents_template
  family: fire_shrine_gazebo_family
    canonical: fire_shrine_gazebo
  family: fire_shrine_lava_fountain_family
    canonical: fire_shrine_lava_fountain
```

This generic migration preserves the old equal-choice behavior while establishing the new boundaries.
Designers can subsequently group, split, rename, or add variants through normal preflighted operations.

### Compatibility window

During migration:

- codecs may read legacy insert-family and variant-index fields;
- all new saves write the new model only after migration is confirmed;
- export should reject an ambiguous mixture of legacy and new pool ownership;
- debug output should report legacy fallback reads;
- remove compatibility code after all checked-in workspaces and fixtures are migrated.

## Implementation Phases

### Phase 0: Characterization and acceptance fixtures

- Add golden fixtures for the current tower, walled keep, hub/spoke, and fire shrine workspaces.
- Record current canonical pieces, variants, runtime pools, preview candidates, and physical catalog identities.
- Add an explicit fire shrine fixture covering the blank scaffold, gazebo, and lava fountain.
- Characterize existing insert runtime exclusion and preview fallback behavior before changing it.

### Phase 1: Introduce explicit identities and template purposes

- Add shared family binding, stable variant identity, and template-purpose model types in
  `MKWorkspaceRuntime`.
- Add codecs with legacy read support but do not change pool output yet.
- Add helpers that resolve slot, family, canonical, and variant ownership without piece-name inference.
- Write new tags alongside legacy tags during the compatibility window.

### Phase 2: Implement the generic slot candidate resolver

- Implement eligibility, canonical fallback, diagnostics, and hierarchical weight resolution once.
- Add a structured resolved-slot-pool manifest model.
- Use it in preview behind comparison logging while retaining the old resolver as a temporary oracle.
- Fail tests when old and new candidate membership differs without an approved migration expectation.

### Phase 3: Separate insert slots from insert content families

- Introduce the insert slot contract model.
- Make insert sockets target slot pools.
- Convert blank insert templates to slot scaffolds.
- Add insert content families bound to insert slots.
- Migrate fire shrine and other checked-in insert workspaces through prepared change operations.
- Remove insert-specific canonical exclusion once template-purpose filtering is active.

### Phase 4: Migrate room and linear-run families

- Embed the shared family binding in room and linear-run definitions.
- Resolve actual topology roles independently from slots.
- Replace source/settings slot overloading with explicit archetype and settings lineage.
- Move room, corridor, wall, path, cap, and other non-insert pools to the generic resolver.

### Phase 5: Add family-management workflows

- Add family creation and deletion for a selected slot.
- Add create-variant-from-canonical.
- Add promote-variant-to-family and copy-variant-to-family.
- Add compatible move-variant-between-families.
- Add family/variant enabled controls and weight editing.
- Show canonical fallback and unavailable-family status in slot/family screens.
- Route every action through the shared preflight/change coordinator.

### Phase 6: Switch preview, export, and runtime consumers

- Make preview consume only resolved slot pools.
- Export structured hierarchical weights and compile flat jigsaw weights at the final boundary.
- Make runtime data consumers use the same resolved membership and weights.
- Assert that slot scaffolds and data-only templates are absent from all emitted runtime pools.
- Remove pool construction based on unweighted `childBaseNames` and insert-family IDs.

### Phase 7: Remove legacy inference

- Stop using `variantIndex` as identity.
- Stop deriving eligibility from `workspace_piece_kind` or insert tags.
- Stop copying slot IDs into topology role IDs.
- Remove obsolete insert-family pool construction and preview exceptions.
- Update documentation and extension examples to use slots, families, canonicals, and variants consistently.

## Test Plan

### Resolver truth table

Cover at least:

| Family state | Runtime candidates | Preview candidates |
|---|---|---|
| No variants, valid canonical | canonical | canonical |
| One valid enabled variant | variant only | variant only |
| Several valid enabled variants | variants only | variants only |
| Variants exist but all are disabled | canonical | canonical |
| Variants exist but all are incompatible | canonical plus diagnostic | canonical plus diagnostic |
| No variants, invalid canonical | none/error | none/error |
| Data-only scaffold present | scaffold excluded | scaffold excluded |
| Family disabled | family excluded | family excluded |

### Weight tests

- Equal defaults preserve equal-choice behavior.
- Family probability is independent of variant count.
- Variant weights affect only selection within their family.
- Canonical fallback receives the family's full conditional probability.
- Disabled candidates do not contribute weight.
- Flattened integer weights preserve the hierarchical ratio and remain deterministic.
- Overflow/range validation produces an actionable error.

### Identity and migration tests

- Codec round trips preserve stable slot, family, and variant IDs.
- Legacy room and linear-run data migrate without physical block loss.
- Legacy insert variants become separate family canonicals.
- Legacy blank insert templates become slot scaffolds.
- Piece UUIDs and catalog positions remain stable across preserving migrations.
- Collision-safe ID remaps are deterministic and fully shown in preflight.
- Reordering variants does not change variant identity.

### Change-system tests

- Promotion summaries name source family, target family, slot, piece, and pool effects.
- Creating the first variant reports that canonical fallback will stop.
- Deleting/disabling the last eligible variant reports that canonical fallback will begin.
- Weight-only changes require confirmation and backup.
- Locked invalidated layers block apply.
- Stale plans are rejected or replaced when family membership or physical templates change.
- Apply requires an active coordinator transaction.

### Preview/runtime parity tests

- Preview and runtime resolve identical candidate membership for every slot.
- Fire shrine preview/runtime never selects the blank platform scaffold.
- Fire shrine remains functional when gazebo and fountain have no variants.
- Once gazebo variants exist, gazebo canonical is excluded while fountain canonical may still fall back.
- Exported effective weights match preview sampling weights.

## UI Requirements

The topology slot page should present the hierarchy directly:

```text
fire_shrine_platform_contents
  Slot scaffold: fire_shrine_platform_contents_template (never placed)

  Gazebo Family                         Weight: 3
    Canonical: fire_shrine_gazebo       Runtime fallback: active
    Variants: 0

  Fountain Family                       Weight: 1
    Canonical: fire_shrine_lava_fountain
    Variants: 2                          Runtime fallback: inactive
```

Required actions:

- create family for slot;
- edit family identity, weight, and enabled state;
- open or teleport to canonical;
- create variant from canonical;
- edit variant identity, weight, and enabled state;
- promote or copy variant to a new family;
- move variant to another compatible family;
- inspect the resolved slot pool and effective probabilities.

Use explicit labels such as "Slot Scaffold", "Family Canonical", "Runtime Variant", and "Canonical
Fallback". Avoid the current generic "template" label where its purpose is important.

## Extension Contract

Adding a new topology type should require an extension to provide:

1. Planner-declared slot schema and actual role mapping.
2. Slot compatibility validation for its family kinds.
3. Optional slot scaffold planning.
4. Kind-specific family settings and codecs.
5. Planned-piece construction for family canonicals and variants.

The extension should receive, rather than reimplement:

- family and variant identity management;
- template-purpose eligibility;
- canonical fallback;
- hierarchical weighting;
- resolved pool manifests;
- family management UI foundations;
- preflight, backup, and transaction enforcement.

## Risks and Mitigations

### Large identity migration

The current system relies heavily on base names, variant indexes, planner IDs, and tags. Introduce stable IDs
before changing pool behavior, maintain a bounded compatibility window, and use physical-piece-preserving
preflight plans.

### Planner-specific assumptions

Tower and walled-keep code currently infer vertical-stack behavior from topology slot ancestry. Preserve
kind-specific compatibility adapters initially, then remove inheritance overloading only after parity tests
cover each planner.

### Accidental empty-template placement

Use an explicit template-purpose enum, central eligibility resolver, export assertions, and fire shrine
acceptance tests. Do not rely on naming or empty-block detection.

### Weight drift during flattening

Keep hierarchical weights in the manifest, compile only at the Minecraft pool boundary, reduce rational
weights deterministically, and validate range limits.

### In-progress families masking errors

Canonical fallback is intentional, but UI and validation should visibly report that fallback is active.
An invalid canonical is still an error; fallback must not bypass slot compatibility.

## Completion Criteria

The refactor is complete when:

- slot IDs, role IDs, family IDs, variant IDs, and physical piece IDs are independently represented;
- insert sockets target slot pools rather than content family pools;
- all supported family kinds use the same canonical/variant eligibility resolver;
- runtime and preview fall back to valid family canonicals only when no eligible variants exist;
- slot scaffolds and data-only templates cannot appear in runtime or preview pools;
- family and variant weights produce documented hierarchical probabilities;
- promote-variant-to-family preserves authored blocks through a reviewed, backed-up change operation;
- legacy fire shrine gazebo and fountain content are represented as separate families for one insert slot;
- all material UI actions use preflight, confirmation, backup, and transaction-scoped apply;
- legacy eligibility and identity inference paths have been removed;
- runtime, preview, export, migration, and change-system tests pass across existing planners.
