package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public final class MKWorkspaceChangePackets {
    private static final int EFFECTS_PER_CHUNK = 128;

    private MKWorkspaceChangePackets() {
    }

    public static void sendPreparedPlan(ServerPlayer player, MKWorkspaceChangeCoordinator.PreparedPlan plan) {
        List<MKWorkspaceChangeEffect> effects = plan.change().summary().effects();
        PacketDistributor.sendToPlayer(player, new WorkspaceChangePreflightPacket(
                plan.requestId(), plan.planId(), plan.change().anchor(), plan.expiresAt().toEpochMilli(),
                plan.change().summary(), effects.size()));
        if (effects.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new WorkspaceChangeEffectsPacket(
                    plan.requestId(), plan.planId(), 0, 0, List.of()));
            return;
        }
        for (int offset = 0; offset < effects.size(); offset += EFFECTS_PER_CHUNK) {
            int end = Math.min(offset + EFFECTS_PER_CHUNK, effects.size());
            PacketDistributor.sendToPlayer(player, new WorkspaceChangeEffectsPacket(
                    plan.requestId(), plan.planId(), offset, effects.size(), effects.subList(offset, end)));
        }
    }

    static void sendFailure(ServerPlayer player, UUID requestId, BlockPos anchor, String message) {
        sendResult(player, requestId, anchor, false, false, message);
    }

    static void sendResult(ServerPlayer player, UUID requestId, BlockPos anchor, boolean success,
                           boolean stale, String message) {
        PacketDistributor.sendToPlayer(player,
                new WorkspaceChangeResultPacket(requestId, anchor, success, stale, message));
    }
}
