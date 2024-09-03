package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public class ExecuteActiveAbilityPacket implements CustomPacketPayload {

    private final AbilityGroupId group;
    private final int slot;
    public static final CustomPacketPayload.Type<ExecuteActiveAbilityPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "execute_active_ability"));

    public static final StreamCodec<FriendlyByteBuf, ExecuteActiveAbilityPacket> STREAM_CODEC = StreamCodec.ofMember(
            ExecuteActiveAbilityPacket::toBytes, ExecuteActiveAbilityPacket::new
    );

    public ExecuteActiveAbilityPacket(AbilityGroupId group, int slot) {
        this.group = group;
        this.slot = slot;
    }

    public ExecuteActiveAbilityPacket(FriendlyByteBuf buffer) {
        group = buffer.readEnum(AbilityGroupId.class);
        slot = buffer.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(group);
        buffer.writeVarInt(slot);
    }

    public static void handle(final ExecuteActiveAbilityPacket packet, IPayloadContext context) {
        MKCore.getPlayer(context.player()).ifPresent(data -> data.getAbilityExecutor().executeLoadoutAbility(packet.group, packet.slot));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
