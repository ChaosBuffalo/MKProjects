package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TalentPointActionPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TalentPointActionPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("talent_point_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TalentPointActionPacket> STREAM_CODEC = StreamCodec.ofMember(
            TalentPointActionPacket::toBytes, TalentPointActionPacket::new
    );

    private final ResourceLocation talentTree;
    private final String line;
    private final int index;
    private final Action action;

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(talentTree);
        buffer.writeUtf(line);
        buffer.writeVarInt(index);
        buffer.writeEnum(action);
    }

    public void handle(IPayloadContext context) {
        Player entity = context.player();
        MKCore.getPlayer(entity).ifPresent(cap -> {
            if (action == Action.SPEND) {
                cap.getTalents().spendTalentPoint(talentTree, line, index);
            } else if (action == Action.REFUND) {
                cap.getTalents().refundTalentPoint(talentTree, line, index);
            }
        });
    }

    public enum Action {
        SPEND,
        REFUND
    }
}
