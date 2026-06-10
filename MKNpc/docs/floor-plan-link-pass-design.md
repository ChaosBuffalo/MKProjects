# Floor Plan Link Pass Design

## Goal

Add an optional post-solve pass for floor plans that creates extra loop connectivity between rooms after the normal main/branch topology has already produced a valid layout.

The link pass is intentionally not a main-path or branch-path planner. It should ignore main/branch categorization and connect any compatible rooms it can, so generated floors feel less tree-like and more like organic dungeon layouts.

## Core Rule

Floor generation remains a two phase process:

1. The topology solver builds a valid floor graph.
   - Main path rules still guarantee narrative progression.
   - Branch rules still control optional expansion and caps.
   - Required main path failures still invalidate the floor.
2. The link pass opportunistically adds loop links.
   - Links may connect main rooms, branch rooms, caps, or mixed categories.
   - Links do not satisfy main min/max counts.
   - Links do not satisfy branch cap requirements.
   - Links do not repair an invalid required topology.

This keeps topology validity simple: first solve a valid dungeon graph, then add extra connectivity where geometry allows.

## Candidate Exits

The link pass should consider closed horizontal exits from placed room regions.

Eligible exits:

- Optional branch exits that were disabled by mask selection.
- Optional exits rejected during branch planning.
- Main-room outgoing directions that were not selected as the main exit by randomized-main-exit resolution.
- Closed exits on main rooms, branch rooms, branch caps, main cap approaches, and main caps when the room kind allows that authored connector.
- Link-only candidate exits authored on branch caps or other room profiles.

Excluded exits:

- Required south main entries.
- Required branch-cap entries.
- Planner-owned ingress entries.
- Vertical access connectors.
- Any connector whose opening profile cannot be resolved.
- Any connector already used by the solved topology.

## Link-Only Candidate Exits

Some rooms should be able to offer openings for loop links without allowing those openings to generate more topology.

Branch caps are the first important case. A branch cap is still terminal for branch generation, but it should be allowed to expose one or more optional link exits. Each of those exits has exactly two outcomes:

- The link pass accepts it and connects it to another compatible candidate.
- The link pass does not accept it and runtime palette patching closes the opening.

These exits must not:

- Start another branch chain.
- Continue the main path.
- Count as branch cap failure when no link is accepted.
- Affect main or branch path validity.

Recommended connector/path category:

- `LINK_CANDIDATE`: optional horizontal opening that is only consumed by the post-solve link pass.

Designer-facing behavior:

- Branch cap, branch room, main room, main cap approach, and main cap profiles can author link candidate exits.
- Link candidate exits appear separately from branch/main exits in the floor room exit UI.
- The user can enable multiple link candidate directions on any supported floor room profile.
- If no link uses a candidate, the generated room remains closed in that direction.

This should be supported for all floor room kinds in the first implementation so links can form between branch caps, branch rooms, main rooms, and main caps without increasing main/branch generation complexity.

## Pairing Rules

Two candidate exits may become a link when:

- They face each other.
- They are on the same horizontal axis.
- Their centers are aligned on the perpendicular axis for straight links, or a bounded one-dogleg route can be found.
- Their opening widths and heights are compatible.
- The corridor between them is clear of existing rooms and hallways.
- The corridor stays inside the hard structure radius.
- The corridor length is within configured link limits.

The first version should support exact straight links plus a bounded route solver for simple doglegs and simple straight sloped links. Later versions can expand the route solver rather than replacing simple pair matching.

Future route options:

- Tolerance alignment: allow small lateral offsets when the two opening spans overlap.
- Adapter/stub alignment: emit short straight stubs from each room, then connect the remaining offset.
- Dogleg links: use an L-shaped route with one corner.
- Sloped links: connect exits with different vertical offsets using ascending or descending runs.
- Compound links: combine doglegs and vertical changes.

The planner should not accept a complex link merely because two exits are near each other. It should ask whether the runtime corridor builder or available link-capable piece catalog can realize the route.

## Link Selection

The pass should produce a list of candidate links, then choose a subset based on designer controls.

Suggested controls:

- `Enable Links`: default `false`.
- `Link Density`: range `0.0..1.0`, default `1.0`.
- `Max Links Per Floor`: range `0..64`, default `10`.
- `Max Links Per Room`: range `0..3`, default `3`.
- `Max Link Length`: integer cap, default `32`.

Selection should prefer useful loops instead of redundant local clutter:

- Prefer links between different solved branches.
- Prefer links that reduce graph distance between rooms.
- Prefer shorter clear links when two options are otherwise equivalent.
- Avoid adding multiple links between the same connected components unless density is high.

## Layout Metadata

The solver should preserve enough data for the link pass to reason about closed exits.

Each link candidate endpoint should know:

- Source segment id.
- Source room profile id.
- Room kind.
- Candidate kind: closed branch, rejected branch, unused randomized-main direction, or link-only candidate.
- World direction.
- Local direction.
- Connector center in layout coordinates.
- Opening width and height.
- Opening profile id.
- Whether the exit was closed by mask, rejected, or unused after randomized main-exit selection.

Each accepted link should record:

- Endpoint A.
- Endpoint B.
- Corridor rectangle.
- Corridor length.
- Opening profile chosen for the link.
- Route segments for straight, one-dogleg, or simple sloped links.
- Total vertical delta.
- Link id, stable within the solved layout.

## Runtime And Export

Loop links are more complex than normal jigsaw branches because a link has two already-placed endpoints. There are two possible implementation strategies.

### Template-Based Link Corridors

A template-based implementation would coordinate three things:

- Endpoint A room variant opens its linked exit.
- Endpoint B room variant opens its linked exit.
- A neutral link hallway is generated between those two openings.

This should not reuse `MAIN_EXIT` or `BRANCH` semantics. The link path should be represented as a neutral internal category such as `LINK` or `CROSS_LINK`.

Branch linear run definitions could be reused as source material, but they should be exported or registered under link-specific pools with link-specific connector roles. Literal reuse of branch pools is risky because branch pieces may carry continuation semantics that mean "keep generating branch content." A link corridor should terminate between two solved endpoints.

The first template-based version could reuse branch hallway visuals by default, while still keeping link metadata distinct from branch topology. Dedicated link hallway template families can be added later if designers want different visuals for loop connectors.

Important runtime constraint:

Vanilla-style jigsaw placement is naturally one-ended. It can grow a corridor from one endpoint, but it does not automatically reserve and open the second already-placed room unless the planner/export layer coordinates both endpoint masks. The implementation must therefore treat an accepted link as a solved-layout artifact, not as an ordinary optional branch.

### Procedural Palette-Built Corridors

For links, procedural corridor construction is the preferred long-term fit over jigsaw hallway templates.

In this model:

1. Jigsaw/planner places the authored room templates.
2. The floor solver records accepted link routes between room openings.
3. Runtime post-processing builds the link corridor directly from palette materials:
   - clear interior volume
   - place floor blocks
   - place wall blocks
   - place ceiling blocks
   - open or patch both room shells
   - optionally add trim, supports, stairs, or ramps

Advantages:

- Arbitrary corridor length without template variants.
- Easier support for vertical offsets.
- Easier support for doglegs and compound routes.
- Lower jigsaw generation step pressure.
- No second-endpoint jigsaw coordination problem.
- Fewer link-specific templates for designers to maintain.

Tradeoff:

Procedural corridors give designers less authored visual control than templates. To compensate, the corridor builder should eventually expose style controls such as floor/wall/ceiling palette entries, trim bands, width, height, doorway framing, stair/ramp style, support frequency, and lighting interval.

The first implementation should not add explicit lighting to link corridors. Dark link corridors are acceptable for v1 and avoid adding another styling surface before the route behavior is proven.

The first version should use the same palette resolution path as the existing structure style generation. It should not introduce a separate corridor palette yet. Floor, wall, ceiling, and shell patching materials should come from the workspace palette in the same way other generated structural surfaces do.

Recommended direction:

- Keep rooms as authored templates.
- Keep main/branch progression able to use authored hallway templates.
- Use procedural palette-built corridors for opportunistic loop links.

The first implementation should try to solve more than exact straight links. It should still avoid fully arbitrary pathfinding, but it should support a small bounded route set:

- Straight links when endpoints are aligned.
- One-dogleg links when a single horizontal turn creates a clear route.
- Straight sloped links when endpoints are horizontally aligned but have a vertical delta the procedural builder can satisfy.
- One-dogleg sloped links when a dogleg route is clear and the vertical delta can be distributed across the route.

When both one-dogleg route orders are valid, prefer X-then-Z before Z-then-X. Sloped route segments may use stairs and slabs, but should not use full-block step changes because those would feel abrupt and unnatural to players.

The data model should represent accepted links as routes, not just rectangles, so additional route shapes can be added without replacing the feature.

## Mask Interaction

Current room masks represent accepted optional branch exits. Link pass needs additional mask state.

Recommended approach:

- Keep branch masks for topology-generated optional branches.
- Add link mask metadata for endpoint openings accepted by the link pass.
- Export variants from the combined branch/link open set.
- Palette patch every authored optional opening that is neither topology-active nor link-active.

This avoids overloading branch masks with non-branch topology.

## Preview UI

The floor plan preview should display accepted links as neutral loop corridors.

Suggested behavior:

- Main path remains green.
- Branch paths remain branch-colored.
- Link corridors use a distinct neutral/accent color.
- Hovering a link shows:
  - `Loop Link`
  - endpoint room labels
  - corridor length
  - opening profile
- Rejected link candidates can optionally be shown in debug/advanced mode.

The preview should run the same deterministic link selection as export/runtime planning for a locked seed.

## Validation

A failed link candidate should not invalidate the floor.

Only these conditions should produce warnings:

- Links enabled but no eligible candidates exist.
- Link density requested links, but all candidates failed due to collision or bounds.
- Accepted link count differs between preview and export for a locked seed.

Required main topology failures should still be reported separately and should not be hidden by the link pass.

## Implementation Plan

1. Extend floor layout solver output with room segment ids and closed/unused exit metadata.
2. Add a link candidate builder that scans eligible endpoint pairs after the normal solve.
3. Add bounded route generation for straight, one-dogleg, and simple sloped link candidates.
4. Add route fit validation against existing rooms, halls, vertical clearance, and hard radius bounds.
5. Add deterministic link selection using floor seed plus link settings.
6. Add accepted link route segments to the floor layout result.
7. Update the floor preview to render link corridors and link hover tooltips.
8. Add model settings for link enablement, density, max count, per-room cap, and max length.
9. Add export/runtime metadata for link-active endpoints.
10. Add a procedural corridor builder that uses the existing workspace palette resolution for floor, wall, ceiling, and shell patch materials.
11. Extend mask variant export so topology-active and link-active openings remain open, while all other optional openings are palette patched closed.
12. Add route data structures that can later represent more complex sloped and dogleg combinations.
13. Add solver/export/runtime tests for:
    - straight aligned links
    - one-dogleg links
    - simple sloped links
    - one-dogleg sloped links
    - collision rejection
    - hard radius rejection
    - mixed main/branch room links
    - endpoint mask coordination
    - palette-built corridor block placement

## Current Implementation Status

Implemented in the first pass:

- Floor topology settings now include link enablement, density, per-floor cap, per-room cap, and max link length.
- Floor room profiles can author `LINK_CANDIDATE` exits.
- Branch cap profiles default to north, east, and west link candidates.
- The floor plan UI can toggle link candidates and configure link settings.
- The preview/layout solver can build accepted loop links after the normal topology solve.
- Preview links support straight routes and one-dogleg routes, with X-then-Z preference.
- Link routes respect collision checks, per-room limits, per-floor limits, density, max length, and the hard 128-block horizontal radius.
- Link candidates export as closed patched openings by default, so designers do not get stray holes before runtime link corridor generation exists.

Not yet implemented:

- Runtime floor generation does not currently consume `MKFloorLayoutSolver` results. Live floor generation is still jigsaw-selection driven, so accepted preview links are not yet guaranteed to match placed world rooms.
- Procedural palette-built link corridors are still the next implementation stage.
- Link-active endpoint mask coordination is still pending. Export currently closes `LINK_CANDIDATE` openings unless a future runtime/export pass explicitly marks them as accepted.
- Sloped link routes are planned but not represented in the current 2D solver route output yet.

## Open Decisions

- None currently.
