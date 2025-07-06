package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TalentPointActionPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TalentPointActionPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("talent_point_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TalentPointActionPacket> STREAM_CODEC = StreamCodec.ofMember(
            TalentPointActionPacket::toBytes, TalentPointActionPacket::new
    );

    private final ResourceKey<TalentTreeDefinition> talentTree;
    private final String line;
    private final int index;
    private final Action action;

    public TalentPointActionPacket(ResourceKey<TalentTreeDefinition> tree, String line, int index, Action action) {
        talentTree = tree;
        this.line = line;
        this.index = index;
        this.action = action;
    }

    public TalentPointActionPacket(RegistryFriendlyByteBuf buffer) {
        talentTree = buffer.readResourceKey(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY);
        line = buffer.readUtf(1024);
        index = buffer.readVarInt();
        action = buffer.readEnum(Action.class);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(talentTree);
        buffer.writeUtf(line);
        buffer.writeVarInt(index);
        buffer.writeEnum(action);
    }

    public void handle(IPayloadContext context) {
        var playerData = MKCore.getPlayerOrThrow(context.player());
        switch (action) {
            case SPEND -> playerData.getTalents().spendTalentPoint(talentTree, line, index);
            case REFUND -> playerData.getTalents().refundTalentPoint(talentTree, line, index);
            case null, default -> {
            }
        }
    }

    public enum Action {
        SPEND,
        REFUND
    }
}
