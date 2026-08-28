# Workspace Change System

## Purpose

The workspace change system is the single entry point for persistent authored workspace mutations.
It separates a player's intent from the exact server-side operation that will be applied.

Every persistent change follows the same contract:

1. The client or a command sends a typed change request.
2. The server prepares the exact change and a complete user-visible summary.
3. The user reviews and confirms that prepared plan.
4. The server prepares the request again and rejects stale plans.
5. The server writes one backup of the current workspace when a workspace already exists.
6. The prepared mutation runs inside a workspace change transaction.
7. The client receives the result and refreshes the workspace screen.

This system applies to definition and topology edits, generation, variants, stairs, sockets, imports,
restores, deletion, relayout, and block substitution. Sample previews and generated-layer lock toggles
are intentionally exempt because they are disposable preview state and workflow controls rather than
authored workspace mutations.

The main implementation is in
[the change package](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/).

## Core Invariants

- A request expresses intent. It is never an apply packet.
- The server owns validation, strategy selection, effects, state guards, and the apply callback.
- Confirmation carries only a server-issued plan ID.
- A plan is bound to the player, request, anchor, workspace identity, and prepared state.
- Every affected piece, template, variant, socket, stair, or setting must appear in the summary.
- User-fixable validation failures are blockers. A summary containing blockers cannot be confirmed.
- Generated-layer locks must be checked for every invalidated layer.
- Existing-workspace mutations require a backup immediately before apply.
- Backup failure stops the mutation.
- Persistent mutation services must run inside the coordinator transaction.
- If the current state no longer matches the prepared state, the user must review a new plan and
  confirm again.

Do not add a mutation-specific request/apply packet or a second confirmation workflow.

## Lifecycle

~~~text
client UI or command
        |
        v
MKWorkspaceChangeRequest
        |
        v
RequestWorkspaceChangePacket
        |
        v
MKWorkspaceChangeRegistry
        |
        v
MKWorkspaceChangeOperation.prepare(...)
        |
        +--> validate and collect blockers
        +--> determine invalidated layers and lock conflicts
        +--> enumerate all field changes and effects
        +--> capture workspace fingerprint and operation state guard
        +--> close over the exact apply inputs
        |
        v
MKWorkspacePreparedChange
        |
        v
shared confirmation screen
        |
        v
ConfirmWorkspaceChangePacket(planId only)
        |
        v
prepare the original request again
        |
        +--> changed state: issue replacement plan; require second confirmation
        |
        v
write one backup when required
        |
        v
enter transaction and invoke prepared mutation
        |
        v
result packet and workspace screen refresh
~~~

Prepared plans currently expire after five minutes. The coordinator also rejects a plan submitted by
a player other than the player for whom it was prepared.

The relevant coordinator behavior is implemented by
[MKWorkspaceChangeCoordinator](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspaceChangeCoordinator.java).

## Core Types

### MKWorkspaceChangeRequest

[MKWorkspaceChangeRequest](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspaceChangeRequest.java)
is serialized client intent:

- request ID: correlates the request, preflight chunks, and result;
- operation ID: selects a registered operation;
- anchor: identifies the workspace location;
- payload: a defensive copy of codec-encoded NBT.

The request should contain only the information needed to describe intent. It must not contain a
client-selected mutation strategy or a client-provided effect report.

### MKWorkspaceChangeOperation

[MKWorkspaceChangeOperation](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspaceChangeOperation.java)
is the sole extension point for new persistent mutation kinds. An operation provides:

- a unique resource location;
- a payload codec;
- a prepare method that builds an MKWorkspacePreparedChange.

Preparation runs on the server. It may inspect the workspace, world blocks, manifests, registries,
planner data, and other authoritative state.

### MKWorkspacePreparedChange

[MKWorkspacePreparedChange](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspacePreparedChange.java)
is server-only and contains:

- the original request;
- the authoritative anchor and workspace UUID;
- a workspace fingerprint;
- an operation-specific state guard;
- the complete summary;
- a mutation callback closing over the exact prepared inputs.

The coordinator recreates this object from the original request at confirmation time. It compares the
anchor, workspace UUID, fingerprint, state guard, and complete summary. Any difference produces a new
plan rather than applying the old one.

Use the workspace fingerprint for authored metadata. Use the operation state guard for relevant state
outside that metadata. Existing examples include:

- block swaps fingerprinting world block states in authored piece bounds;
- restores including archive path, modification time, and manifest hash;
- imports including the loaded manifest hash;
- socket placement including the candidate block state.

### MKWorkspaceChangeSummary

[MKWorkspaceChangeSummary](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspaceChangeSummary.java)
is the operation-neutral report shown to the user. It contains:

- operation ID, title, and prose summary;
- mutation safety classification;
- backup requirement;
- invalidated generated layers;
- warnings and blockers;
- field-level before/after values;
- typed effects.

The shared formatter is used by the UI, clipboard output, and server logs. Effects are transferred in
chunks of 128, but the confirmation button remains disabled until every effect has arrived. The
displayed effect list is not truncated.

Definition field changes are semantic and human-readable. Scalar model objects are decomposed into
named properties such as room width or palette wall block. Keyed collections are diffed by family or
profile ID and report additions, removals, reordering, and changed definitions individually. Do not
feed arbitrary model objects through String.valueOf: classes without a semantic toString produce JVM
identity text, and separately decoded but equivalent instances can otherwise look changed.

### MKWorkspaceChangeEffect

An effect identifies one concrete subject and what will happen to it. Its important fields are:

- action: create, update, move, expand, rebuild, remove, patch, restore, or preserve;
- subject: workspace, setting, piece, template, variant, stair, insert, socket, area, or backup;
- stable subject ID and display name;
- base template name and variant index where applicable;
- whether the effect is physical and whether it is derived;
- a concise reason or description.

Prefer one accurate effect per affected subject. A restore, for example, reports both current pieces
that will be removed and every piece restored from the archive.

## Initiating a Change

### Built-in request helpers

[MKWorkspaceChangeRequests](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspaceChangeRequests.java)
contains helpers for the simple built-in operations. The block swap page uses this pattern:

~~~java
screen.requestWorkspaceChange(MKWorkspaceChangeRequests.swapBlocks(
        screen.anchor(),
        sourceBlockId,
        targetBlockId
));
~~~

MKWorkspaceScreen.requestWorkspaceChange records the request ID, opens the shared confirmation page,
and sends RequestWorkspaceChangePacket. UI code must not send a second mutation packet after
confirmation.

Targeted simple operations use the corresponding helper:

~~~java
screen.requestWorkspaceChange(MKWorkspaceChangeRequests.target(
        MKWorkspaceSimpleChangeOperation.Kind.CLEAR_STAIRS,
        screen.anchor(),
        pieceName
));
~~~

Use a dedicated typed payload instead of expanding MKWorkspaceSimpleChangePayload when a new operation
has structured data or nontrivial validation.

### Typed requests

Definition editing uses a typed payload because it carries a draft definition, remaps, variant edits,
and apply options. The important construction pattern is:

~~~java
MKWorkspaceDefinitionChangePayload payload =
        new MKWorkspaceDefinitionChangePayload(
                draft,
                generateAfterApply,
                forceFullRegenerate,
                acceptedRemaps,
                pendingAddedVariants,
                pendingDeletedVariantPieceIds,
                workspaceSettingsDirty
        );

MKWorkspaceChangeRequest request = new MKWorkspaceChangeRequest(
        UUID.randomUUID(),
        MKWorkspaceDefinitionChangeOperation.ID,
        draft.anchor(),
        MKWorkspaceChangePayloads.encode(
                MKWorkspaceDefinitionChangePayload.CODEC,
                payload,
                "workspace definition change"
        )
);

screen.requestWorkspaceChange(request);
~~~

MKWorkspaceDefinitionChangePayload intentionally strips loaded pieces during network encoding. Pieces
are authoritative server state and should not be echoed back as client-owned mutation input.

### Commands

Commands prepare the same request rather than bypassing the workflow. MKWorkspaceCommands opens the
workspace screen and sends the coordinator's prepared plan to the player:

~~~java
MKWorkspaceChangeRequest request =
        MKWorkspaceChangeRequests.swapBlocks(anchor, source, target);

service.openWorkspaceScreen(player, request.anchor());
MKWorkspaceChangePackets.sendPreparedPlan(
        player,
        MKWorkspaceChangeCoordinator.shared().prepare(player, request)
);
~~~

This keeps command and GUI behavior identical: both require review, confirmation, backup, and
transaction-scoped apply.

## Adding a New Operation

### 1. Define an immutable payload and codec

Use a record and a Mojang Codec. The insert socket payload is a representative existing example:

~~~java
public record MKWorkspaceInsertSocketChangePayload(
        UUID hostPieceId,
        BlockPos socketWorldPos,
        Direction socketFacing,
        boolean createFamily,
        String familyId,
        int width,
        int height,
        int depth,
        int faceUOffset,
        int faceVOffset,
        String hostFinalState,
        String templateJigsawFinalState
) {
    public static final Codec<MKWorkspaceInsertSocketChangePayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUID_CODEC.fieldOf("hostPieceId")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::hostPieceId),
                    BlockPos.CODEC.fieldOf("socketWorldPos")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::socketWorldPos),
                    Direction.CODEC.fieldOf("socketFacing")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::socketFacing),
                    Codec.BOOL.fieldOf("createFamily")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::createFamily),
                    Codec.STRING.fieldOf("familyId")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::familyId),
                    Codec.INT.optionalFieldOf("width", 1)
                            .forGetter(MKWorkspaceInsertSocketChangePayload::width),
                    Codec.INT.optionalFieldOf("height", 1)
                            .forGetter(MKWorkspaceInsertSocketChangePayload::height),
                    Codec.INT.optionalFieldOf("depth", 1)
                            .forGetter(MKWorkspaceInsertSocketChangePayload::depth),
                    Codec.INT.optionalFieldOf("faceUOffset", 0)
                            .forGetter(MKWorkspaceInsertSocketChangePayload::faceUOffset),
                    Codec.INT.optionalFieldOf("faceVOffset", 0)
                            .forGetter(MKWorkspaceInsertSocketChangePayload::faceVOffset),
                    Codec.STRING.optionalFieldOf("hostFinalState", "minecraft:air")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::hostFinalState),
                    Codec.STRING.optionalFieldOf(
                                    "templateJigsawFinalState",
                                    "minecraft:air"
                            )
                            .forGetter(
                                    MKWorkspaceInsertSocketChangePayload::templateJigsawFinalState
                            )
            ).apply(instance, MKWorkspaceInsertSocketChangePayload::new));
}
~~~

See
[MKWorkspaceInsertSocketChangePayload](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/operations/MKWorkspaceInsertSocketChangePayload.java)
for the complete codec.

Use optional codec fields only when a safe backward-compatible default exists. Reject malformed IDs,
unknown registry entries, or impossible values during prepare.

### 2. Implement MKWorkspaceChangeOperation

The operation should perform all of the following in prepare:

1. Verify that the payload anchor agrees with the request anchor when the payload has an anchor.
2. Load the current workspace and other authoritative server state.
3. Run the same validation that apply will rely on.
4. Record expected user-fixable errors as blockers.
5. Determine all invalidated generated layers.
6. Add blockers for locked invalidated layers.
7. Add field changes and every affected subject to the summary.
8. Set backupRequired when an existing workspace will be changed.
9. Capture the workspace UUID and fingerprint.
10. Add an operation state guard for relevant non-workspace state.
11. Return a mutation callback that uses the exact values prepared above.

The insert socket operation demonstrates this structure:

~~~java
public final class MKWorkspaceInsertSocketChangeOperation
        implements MKWorkspaceChangeOperation<MKWorkspaceInsertSocketChangePayload> {
    public static final ResourceLocation ID = MKWorkspace.id("insert_socket");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<MKWorkspaceInsertSocketChangePayload> codec() {
        return MKWorkspaceInsertSocketChangePayload.CODEC;
    }

    @Override
    public MKWorkspacePreparedChange prepare(
            ServerPlayer player,
            BlockPos anchor,
            MKWorkspaceChangeRequest request,
            MKWorkspaceInsertSocketChangePayload payload
    ) {
        MKStructureWorkspace workspace = IMKStructureWorkspaceData
                .get(player.serverLevel())
                .getWorkspaceByAnchor(anchor)
                .orElseThrow();

        MKWorkspaceInsertAuthoringService service =
                new MKWorkspaceInsertAuthoringService();

        List<String> blockers = service.validateCreateSocketFamily(
                player.serverLevel(),
                createRequest
        );

        List<MKWorkspaceGeneratedLayer> invalidated = List.of(
                MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT,
                MKWorkspaceGeneratedLayer.RUNTIME_METADATA
        );

        // Add lock blockers and complete socket/insert/template effects here.
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(
                ID,
                "Confirm Insert Socket Change",
                "Create an insert family and place its socket.",
                MKWorkspaceMutationSafety.SAFE_RELAYOUT,
                true,
                invalidated,
                List.of(),
                blockers,
                List.of(),
                effects
        );

        return new MKWorkspacePreparedChange(
                request,
                anchor,
                workspace.id(),
                MKWorkspaceChangeCoordinator.fingerprint(workspace),
                socketStateGuard,
                summary,
                applyPlayer -> {
                    MKWorkspaceInsertAuthoringService.Result result =
                            service.createSocketFamily(
                                    applyPlayer.serverLevel(),
                                    createRequest
                            );
                    return result.context().isPresent()
                            ? MKWorkspaceChangeApplyResult.success(
                                    anchor,
                                    "Insert socket family created."
                            )
                            : MKWorkspaceChangeApplyResult.failure(
                                    anchor,
                                    String.join("; ", result.errors())
                            );
                }
        );
    }
}
~~~

This is an abridged excerpt. The
[complete operation](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/operations/MKWorkspaceInsertSocketChangeOperation.java)
also reports the host socket, insert family, and new authorial template and checks every relevant
generated-layer lock.

Expected validation failures belong in blockers so the user can understand why the operation is
disabled. Throw an exception for malformed requests or conditions where no meaningful report can be
prepared, such as an unknown operation payload or an anchor that cannot identify the required
workspace.

### 3. Share validation with apply

Do not create a client-only or preflight-only approximation of apply validation. The insert service
exposes validateCreateSocketFamily and validatePlaceExistingSocketFamily; prepare calls those methods,
and the mutation service calls them again before changing state.

The second call protects the apply boundary. Coordinator revalidation protects the user-consent
boundary. Both are required.

### 4. Register the operation

Register operations during mod initialization, before requests can arrive:

~~~java
MKWorkspaceChangeRegistry.shared().register(new MyWorkspaceChangeOperation());
~~~

Core registrations live in
[MKWorkspaceCoreChangeOperations](../src/main/java/com/chaosbuffalo/mkworkspace/world/gen/workspace/change/MKWorkspaceCoreChangeOperations.java).
Operation IDs must be unique; duplicate registration throws an IllegalStateException.

Extensions should register their own operation from their initialization path rather than modifying
the core registry class.

### 5. Add a request helper when useful

If several callers will initiate the operation, add a focused helper that:

- generates a new request UUID;
- uses the operation ID and anchor;
- encodes the typed payload through MKWorkspaceChangePayloads;
- returns MKWorkspaceChangeRequest.

Callers should only construct or request a change. They should not know how it is applied.

## Definition Changes and Planner Extensions

Workspace form edits flow through MKWorkspaceDefinitionChangeOperation. Core code compares the
existing and requested definitions, builds the invalidation report, and selects a prepared update
strategy.

If core classification does not understand definition drift, the default is full regeneration. Full
regeneration:

- is classified as destructive;
- reports every existing authored piece as rebuilt;
- invalidates every generated layer;
- is blocked by any locked generated layer.

A planner may safely claim planner-specific definition drift by overriding
MKWorkspacePlanner.prepareDefinitionChange:

~~~java
@Override
public Optional<MKWorkspacePlannerChangePlan> prepareDefinitionChange(
        ServerPlayer player,
        MKStructureWorkspace existing,
        MKStructureWorkspace requested,
        MKWorkspaceMutationPreflight corePreflight
) {
    if (!isMyPlannerSpecificChange(existing, requested)) {
        return Optional.empty();
    }

    return Optional.of(new MKWorkspacePlannerChangePlan(
            MY_CHANGE_ID,
            "Relayout the affected planner-owned templates.",
            MKWorkspaceMutationSafety.SAFE_RELAYOUT,
            invalidatedLayers,
            warnings,
            completeEffects,
            plannerStateGuard,
            applyPlayer -> applyPreparedPlannerMutation(
                    applyPlayer,
                    existing,
                    requested,
                    preparedPlan
            )
    ));
}
~~~

A planner plan must fully own the claimed mutation. It must provide the exact mutation callback,
invalidated layers, effects, warnings, and any planner-specific state guard. Return Optional.empty
unless the planner can account for all changed fields. Partial claims are unsafe because they would
prevent the full-regeneration fallback.

Variant additions and deletions continue through the core definition-change path rather than the
planner hook.

## Backups and Transaction Enforcement

For an existing workspace, backupRequired must be true. On confirmation, the coordinator writes the
archive before it enters the mutation callback. The current path shape is:

~~~text
<world>/
  generated/
    mkworkspace/
      backups/
        <dimension namespace>/
          <dimension path>/
            <anchor x>_<anchor y>_<anchor z>/
              <workspace UUID>/
                <timestamp>-before-<operation>.zip
~~~

The stable path is independent of workspace namespace and structure name, so renaming a workspace
does not hide its history. Discovery also supports legacy backup directories. Backup metadata stores
the operation, dimension, anchor, workspace UUID and name, and creation time. This permits discovery
and restoration after workspace deletion.

Operations that create a workspace at an empty anchor, import into an empty anchor, or restore a
deleted workspace set backupRequired to false because no prior workspace exists. They still execute
inside a transaction.

Persistent mutation services call:

~~~java
MKWorkspaceBackupManifestWriter.requireTransaction("operation-name");
~~~

This makes direct service execution fail outside MKWorkspaceChangeCoordinator. If a nested service
asks to write a backup while a transaction is active, the writer returns the coordinator's backup
instead of writing another archive. If the transaction did not authorize a backup, such a nested
request fails.

Do not call writeBeforeMutation from a new operation. Declare backupRequired in the summary and let
the coordinator own backup timing and failure handling.

## Failure and Stale-State Behavior

- Malformed request or preparation exception: return a preflight failure; do not create a plan.
- Blockers: show the complete report but disable confirmation.
- Expired or unknown plan ID: reject confirmation.
- Different player: reject confirmation.
- Changed workspace fingerprint, operation state guard, or summary: issue a replacement plan and
  require another confirmation.
- Backup write failure: report failure and do not invoke the mutation.
- Apply failure result: keep the backup and report the operation failure.
- Apply exception: log and report failure; the pre-change backup remains available for recovery.

When an operation depends on world state or a file, its state guard must change whenever that state
could change the mutation or the report. Summary equality alone is not a substitute for a guard when
two distinct external states can produce the same text.

## Testing

At minimum, a new operation should add:

- payload codec round-trip coverage;
- preparation tests for valid input, blockers, and locked layers;
- summary tests proving every affected piece/template/variant is present;
- state-guard or stale-plan coverage for external dependencies;
- transaction enforcement coverage for any new mutating service;
- apply success and failure tests at the lowest practical service boundary.

Shared regression coverage currently includes registry duplicate rejection, payload codecs,
untruncated summary formatting, full-regeneration invalidation, stable backup paths, and transaction
scope behavior.

Run:

~~~powershell
.\gradlew.bat :MKWorkspace:test :MKWorkspaceExtensions:compileJava
~~~

For changes that affect public runtime types or planner integration, also run:

~~~powershell
.\gradlew.bat :MKWorkspaceRuntime:compileJava :MKNpc:compileJava :MKWorkspace:compileJava :MKWorkspaceExtensions:compileJava
~~~

## Extension Checklist

Before merging a persistent workspace mutation:

- [ ] The operation has a unique registered ID and a typed codec.
- [ ] Every caller creates an MKWorkspaceChangeRequest.
- [ ] UI and commands use the shared confirmation workflow.
- [ ] Prepare uses authoritative server state.
- [ ] Apply strategy is selected by the server.
- [ ] Expected validation failures are visible as blockers.
- [ ] Every affected setting, piece, template, variant, socket, insert, or stair is reported.
- [ ] Field values are semantic descriptions, not default Java object strings or whole-list dumps.
- [ ] Invalidated layers and locked-layer blockers are complete.
- [ ] Existing-workspace mutations require a backup.
- [ ] External dependencies are covered by an operation state guard.
- [ ] The mutation callback closes over the prepared inputs.
- [ ] The mutating service requires a coordinator transaction.
- [ ] No mutation-specific confirm/apply packet was added.
- [ ] Codec, summary, lock, transaction, stale-state, and apply behavior are tested.

## Anti-Patterns

Do not:

- send a packet that directly invokes a persistent mutation service;
- let the client choose safe relayout versus destructive regeneration;
- recompute a different mutation after confirmation;
- display only counts while omitting affected piece or template names;
- truncate the effect list;
- create backups inside each nested mutation service;
- use workspace display names as backup identity;
- silently ignore locked invalidated layers;
- treat warnings as blockers or blockers as warnings;
- register a planner-specific partial mutation that does not cover all changed fields.

When in doubt, add a new MKWorkspaceChangeOperation and make its prepared summary conservative and
complete. An explicit full-regeneration fallback is safer than an unreported partial mutation.
