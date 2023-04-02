package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TalentPointActionPacket {
    private final ResourceLocation talentTree;
    private final String line;
    private final int index;
    private final Action action;

    public enum Action {
        SPEND,
        REFUND
    }

    public TalentPointActionPacket(ResourceLocation tree, String line, int index, Action action) {
        talentTree = tree;
        this.line = line;
        this.index = index;
        this.action = action;
    }

    public TalentPointActionPacket(FriendlyByteBuf buffer) {
        talentTree = buffer.readResourceLocation();
        line = buffer.readUtf(1024);
        index = buffer.readVarInt();
        action = buffer.readEnum(Action.class);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(talentTree);
        buffer.writeUtf(line);
        buffer.writeVarInt(index);
        buffer.writeEnum(action);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null)
                return;

            entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap -> {
                if (action == Action.SPEND) {
                    cap.getTalents().spendTalentPoint(talentTree, line, index);
                } else if (action == Action.REFUND) {
                    cap.getTalents().refundTalentPoint(talentTree, line, index);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}
