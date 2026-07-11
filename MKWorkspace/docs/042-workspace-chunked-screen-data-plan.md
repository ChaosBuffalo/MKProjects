# Workspace Chunked Screen Data Plan

## Goal

Make the workspace editor scale beyond Minecraft's custom payload and NBT read limits by replacing monolithic workspace screen packets with a compact workspace snapshot plus chunked piece data.

The editor must keep enough client-side data to support topology fit checks, template grouping, variant counts, stair-generation affordances, palette checks, and detail pages. It should not require every full `MKWorkspacePieceDefinition` to be serialized into one `open_workspace_screen` packet.

After this work:

- Opening a workspace never hard-crashes because the workspace NBT is too large.
- The main workspace screen opens from a bounded-size metadata/index payload.
- Physical authored pieces remain visible to the user.
- Derived/reused pieces stay hidden unless a specific internal workflow needs them.
- Full piece definitions are fetched in bounded chunks or by targeted query.
- Planner/UI calculations use a compact piece index when full piece payloads are unnecessary.
- Large workspaces degrade by loading more data over multiple packets, not by losing planner functionality.

## Problem

`OpenWorkspaceScreenPacket` historically sent `MKStructureWorkspace.toTag()` directly. That includes the full `pieces` list, with each piece carrying identity, geometry, placement, sidecar positions, generated stair positions, connectors, and tags.

The observed crash was:

```text
Failed decoding custom payload mknpc:open_workspace_screen
net.minecraft.nbt.NbtAccounterException: Tried to read NBT tag that was too big;
tried to allocate: 2097121 + 48 bytes where max allowed: 2097152
```

The triggering operation reported:

- `total=78` physical template impacts
- `preserved=24`
- `new=50`
- `rebuilt=4`

That impact summary was logged once for confirm and once for apply. The user-facing count was 78 physical authored template slots, but the old packet also carried any non-hidden full workspace piece data in one NBT payload.

The current crash guard is intentionally temporary:

- update/preflight/apply packets send compact workspace config without pieces;
- screen-open packets filter derived/reused pieces;
- screen-open falls back to metadata-only if physical authored pieces still produce a large payload;
- client decode catches an oversized screen payload and opens without workspace data instead of crashing.

That prevents the hard crash, but metadata-only fallback is not acceptable as the final behavior for large workspaces because many planner UI paths need piece-derived data.

## Constraints

- Minecraft's NBT reader enforces a roughly 2 MB accounter limit for `FriendlyByteBuf.readNbt()`.
- NeoForge packet splitting does not remove the NBT accounter limit for one decoded NBT value.
- Piece payload size is not proportional to piece count alone. Tags, connectors, marker positions, generated stair positions, and sidecar data can dominate.
- The workspace editor should not expose derived/reused pieces to the user.
- The server remains the source of truth for persisted workspace pieces and destructive/safe mutation decisions.
- Client-side UI should be responsive and should not block the first screen on loading every full piece definition.

## Data Classes

### Workspace Header

A compact workspace snapshot sent when opening the screen.

Contains:

- workspace id
- anchor
- namespace
- structure name
- topology profile
- dimensions
- palette
- stair config
- vertical access config
- margins
- family definitions
- opening profiles
- linear run families
- insert families
- created/updated timestamps
- layer states
- import/backup manifest ids
- piece index summary metadata

Does not contain:

- full `MKWorkspacePieceDefinition` list
- marker positions
- generated stair positions
- derived/reused full piece records

### Workspace Piece Index

A compact client-visible index row for each physical authored piece.

Suggested fields:

- `pieceId`
- `pieceName`
- `roleId`
- `plannerId`
- `variantIndex`
- `baseName`
- `pieceKind`
- topology group/stack/section identifiers
- stable slot id when available
- world origin
- export bounds
- preview bounds
- structure block position
- sign position
- effective dimensions
- shell margin
- connector signature/summary
- generated stair count or `hasGeneratedStairs`
- authoring/derived flags
- selected UI tags used for grouping and fit checks

The index should omit:

- arbitrary full tag maps when not used by UI;
- marker position lists;
- generated stair position lists;
- full connector objects if a connector summary is enough;
- derived/reused rows for user-facing views.

### Workspace Piece Detail

The full `MKWorkspacePieceDefinition`, fetched only when needed.

Use cases:

- selected topology slot detail pages that need full connector data;
- operations that act on exact marker/generated stair positions;
- diagnostics or export/debug tools;
- workflows that cannot be represented by the compact index.

## Packet Design

### OpenWorkspaceScreenPacket

Replace the workspace NBT payload with:

- anchor
- `WorkspaceHeader`
- import manifest ids
- backup manifest files
- piece index metadata:
  - total physical authored pieces
  - index revision
  - chunk size
  - first index chunk if it fits comfortably
  - `hasMoreIndexChunks`

The packet must remain small enough to be safe regardless of workspace size. A target budget of 256 KB or less is preferable for the initial screen packet.

### RequestWorkspacePieceIndexChunkPacket

Client-to-server request:

- anchor
- index revision expected by client
- chunk offset or cursor
- max rows
- optional filter:
  - topology group
  - base name
  - role id
  - piece kind
  - include hidden derived pieces, default false and creative/debug-only

### WorkspacePieceIndexChunkPacket

Server-to-client response:

- anchor
- index revision
- offset/cursor
- total physical authored rows
- rows
- `hasMore`

Each chunk should be independently bounded by encoded byte size, not just row count. The planned target budget is 512 KB per chunk. If one row is unexpectedly large, the server should send fewer rows and log the row size.

### RequestWorkspacePieceDetailsPacket

Client-to-server request:

- anchor
- index revision expected by client
- piece ids or planner ids
- max response bytes

### WorkspacePieceDetailsPacket

Server-to-client response:

- anchor
- index revision
- full piece definitions
- omitted ids if the response would exceed the 512 KB chunk budget
- continuation cursor if needed

### WorkspacePieceIndexInvalidatedPacket

Server-to-client notice when the workspace changed while the screen is open:

- anchor
- new index revision
- reason

The client should clear stale index/detail caches and request the first index chunk again.

## Client Model

Add a client-side workspace view model separate from `MKStructureWorkspace`:

```text
ClientWorkspaceSession
  header
  pieceIndexRows
  pieceDetailsById
  indexRevision
  loading state
  missing detail requests
```

`MKWorkspaceScreen` should stop using `workspace.pieces()` as the primary UI data source. Instead:

- header/topology forms use the workspace header;
- overview/counts use the piece index;
- grouping uses the piece index;
- selected slot pages use index rows first and request details only if needed;
- utility buttons use index summary fields where possible;
- palette/safe-mutation checks should be server-owned wherever possible, with the client acting as a preview and display layer.

The full persisted `MKStructureWorkspace` remains a server model. The client should move toward a separate draft model for editor changes instead of constructing and shipping full workspace objects. Draft requests should carry only the user-edited settings/topology/remaps and should never include existing persisted pieces.

## Server Model

The server owns:

- persisted workspace pieces;
- index construction;
- index revision calculation;
- detail fetching;
- filtering hidden/derived pieces;
- byte-budgeted chunking.
- planner solving and authoritative fit checks wherever practical.

Index revision can initially be a hash of:

- workspace id
- updated timestamp
- piece count
- stable hash of physical authored piece ids/planner ids/variant indices
- layer state versions

It does not need to be cryptographic. It needs to detect stale client caches.

## UI Behavior

### Opening A Workspace

1. Server sends `OpenWorkspaceScreenPacket` with header and first safe index chunk.
2. Screen opens immediately.
3. If more index chunks exist, client requests them in the background.
4. Overview displays loaded counts and total counts separately if not fully loaded.
5. Controls that require unloaded detail show loading state or request details on demand.

### Selecting A Topology Slot

1. Client filters loaded index rows by slot/base/planner id.
2. If the index is not complete for that filter, request the relevant index chunk/filter.
3. Display rows from the index.
4. Request full piece details only for selected rows or operations that need them.

### Applying Workspace Changes

1. Client sends compact draft/config plus accepted remaps.
2. Server preflight computes changes from persisted workspace state.
3. Server returns compact preflight report.
4. On apply, server mutates persisted workspace and sends index invalidation or reopens with a fresh header.
5. Client refreshes index chunks.

## Fit And Topology Calculations

Client calculations should be audited and classified:

| Calculation | Current Data Need | Target Data Source |
| --- | --- | --- |
| Has existing workspace pieces | non-empty `pieces` | header/index total count |
| Manage overview piece count | `pieces().size()` | index total count |
| Authored template grouping | tags, planner id, base name, kind, variant | piece index |
| Variant counts per base/slot | base name, variant index, kind | piece index |
| Generated stair availability | generated stair list non-empty | index `hasGeneratedStairs` |
| Selected slot list | planner/topology tags, base name | piece index |
| Palette ambiguity check | piece palette-relevant role/tags | server preflight |
| Exact connector fit checks | connector details | server preflight or detail fetch only when the UI must display exact connector data |
| Marker/stair edit operations | exact sidecar positions | piece detail fetch |

Any calculation that only needs grouping/counting should not require full piece details.

The index should not carry full connector geometry by default. It should carry stable connector signatures and summaries sufficient for display, grouping, and detecting whether the client needs more information. Full connector geometry belongs in server-side fit/preflight logic or in targeted piece detail responses.

Derived/reused pieces should remain server-only unless a specific planner solve requires exposing a compact non-user-visible influence summary. The client should not receive or display derived/reused full piece definitions as part of normal workspace editing.

## Implementation Phases

### Phase 1: Introduce Compact Index Model

- Add `MKWorkspacePieceIndexRow`.
- Add codec tests for row serialization.
- Add a server-side builder from `MKWorkspacePieceDefinition`.
- Include only physical authored pieces by default.
- Preserve all fields currently used by workspace UI grouping/counting.
- Include connector signatures/summaries, not full connector geometry.

### Phase 2: Split Screen Payload

- Add `MKWorkspaceScreenSnapshot` or similar header model.
- Change `OpenWorkspaceScreenPacket` to send header plus first index chunk.
- Keep the existing metadata-only fallback as a final guard during migration.
- Add encoded-size logging for header and each chunk.

### Phase 3: Client Session Cache

- Add `ClientWorkspaceSession`.
- Move screen/page code away from direct `workspace.pieces()` usage.
- Provide query helpers:
  - `hasPieces()`
  - `pieceCount()`
  - `authoredPiecesByTopology()`
  - `variantsForBase(baseName)`
  - `piecesForTopologySlot(slotId)`
  - `hasGeneratedStairTargets()`

### Phase 4: Chunk Requests

- Add index chunk request/response packets.
- Add detail request/response packets.
- Add byte-budgeted server chunking with a 512 KB target per chunk.
- Add stale revision handling.
- Add client retry/refresh behavior after workspace mutation.

### Phase 5: Detail Fetch Migration

- Identify every UI path that truly needs full `MKWorkspacePieceDefinition`.
- Convert those paths to request details on demand.
- Add loading/empty/error states for detail-dependent pages.
- Ensure normal overview and topology editing remain usable from the index alone.
- Move fit checks and mutation safety decisions to server preflight where they are not purely presentational.

### Phase 5.5: Separate Draft Model

- Introduce a client-to-server workspace draft/settings model distinct from `MKStructureWorkspace`.
- Include only editable topology/settings/remap data.
- Stop using full persisted workspace serialization for draft update requests.
- Keep server-side conversion from draft settings to requested workspace state.

### Phase 6: Remove Monolithic Piece Payload Assumptions

- Remove any remaining screen-open full piece list path.
- Keep tests that fail if `OpenWorkspaceScreenPacket` can encode a large full workspace piece list.
- Keep packet size warnings and oversized decode tolerance as defensive guards.

## Test Plan

- Unit test index row serialization and builder behavior.
- Unit test derived/reused filtering.
- Unit test chunker byte budgets with:
  - many small pieces;
  - few very large-tag pieces;
  - large connector lists;
  - generated stair/marker heavy pieces.
- Packet round-trip tests for:
  - open screen header;
  - first index chunk;
  - continuation chunk;
  - detail fetch;
  - stale revision response.
- Client model tests for grouping/counting from index rows.
- Service regression test that adding a floor main room preserves existing authored templates and refreshes the index.
- Manual test in `build 3`:
  - open existing workspace;
  - add a main room;
  - confirm preflight;
  - apply;
  - verify no packet crash;
  - verify floor rooms appear;
  - verify existing variants remain present;
  - verify derived/reused pieces stay hidden.

## Logging

Log at info/debug level:

- workspace screen header encoded bytes;
- first index chunk row count and encoded bytes;
- subsequent chunk row count and encoded bytes;
- detail response row count and encoded bytes;
- skipped/omitted rows due to byte budget;
- index revision changes after workspace mutation.

Warn when:

- any packet exceeds 75% of the target byte budget;
- one index row is unusually large;
- one full piece detail cannot fit in a normal detail response;
- client requests a stale revision.

## Decisions

- Chunk target: 512 KB per index/detail chunk.
- Connector data: index rows carry connector signatures/summaries; full geometry stays server-side or in targeted detail fetches.
- Derived/reused pieces: keep hidden from the client during normal editing. Expose only compact server-authored influence summaries if planner solving proves it is needed.
- Fit/preflight work: move as much as practical to the server. Client-side logic should focus on display, local draft editing, and lightweight previews.
- Draft model: introduce a separate draft/settings model instead of using full `MKStructureWorkspace` as the client update request shape.

## Success Criteria

- A workspace with hundreds or thousands of authored pieces can open without one large NBT payload.
- The first screen opens from a bounded packet.
- Piece list loading is incremental and observable.
- Existing planner pages work from the index.
- Full details load only for selected or operation-specific pieces.
- Oversized payloads produce warnings and chunking, not crashes or silent loss of planner functionality.
