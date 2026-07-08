package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestWorkspacePreflightPacketTest {
    @Test
    void packetRoundTripsAcceptedRemaps() {
        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps = List.of(
                new MKWorkspaceTemplateRemapSuggestion(
                        MKWorkspacePlannerId.of("keep.main.floor_plan.room.old_room"),
                        MKWorkspacePlannerId.of("keep.main.floor_plan.room.new_room"),
                        100,
                        "same floor piece kind, dimensions, and connector signature")
        );
        RequestWorkspacePreflightPacket packet = new RequestWorkspacePreflightPacket(
                MKStructureWorkspace.createDraft(BlockPos.ZERO), acceptedRemaps);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.toBytes(buffer);

            RequestWorkspacePreflightPacket decoded = new RequestWorkspacePreflightPacket(buffer);

            assertEquals(acceptedRemaps, decoded.acceptedRemaps());
        } finally {
            buffer.release();
        }
    }
}
