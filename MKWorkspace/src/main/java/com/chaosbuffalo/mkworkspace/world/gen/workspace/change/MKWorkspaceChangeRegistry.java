package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class MKWorkspaceChangeRegistry {
    private static final MKWorkspaceChangeRegistry SHARED = new MKWorkspaceChangeRegistry();

    private final Map<ResourceLocation, MKWorkspaceChangeOperation<?>> operations = new LinkedHashMap<>();

    public static MKWorkspaceChangeRegistry shared() {
        return SHARED;
    }

    public synchronized <T> void register(MKWorkspaceChangeOperation<T> operation) {
        MKWorkspaceChangeOperation<?> previous = operations.putIfAbsent(operation.id(), operation);
        if (previous != null) {
            throw new IllegalStateException("Duplicate workspace change operation: " + operation.id());
        }
    }

    public synchronized Optional<MKWorkspaceChangeOperation<?>> get(ResourceLocation id) {
        return Optional.ofNullable(operations.get(id));
    }

    public MKWorkspacePreparedChange prepare(ServerPlayer player, MKWorkspaceChangeRequest request) throws Exception {
        MKWorkspaceChangeOperation<?> operation = get(request.operationId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown workspace change operation: " +
                        request.operationId()));
        return prepareTyped(player, request, operation);
    }

    @SuppressWarnings("unchecked")
    private <T> MKWorkspacePreparedChange prepareTyped(ServerPlayer player, MKWorkspaceChangeRequest request,
                                                       MKWorkspaceChangeOperation<?> rawOperation) throws Exception {
        MKWorkspaceChangeOperation<T> operation = (MKWorkspaceChangeOperation<T>) rawOperation;
        DataResult<T> decoded = operation.codec().parse(NbtOps.INSTANCE, request.payload());
        T payload = decoded.resultOrPartial(error -> MKWorkspace.LOGGER.error(
                        "Failed to decode workspace change {}: {}", request.operationId(), error))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid payload for workspace change " + request.operationId()));
        return operation.prepare(player, request.anchor(), request, payload);
    }
}
