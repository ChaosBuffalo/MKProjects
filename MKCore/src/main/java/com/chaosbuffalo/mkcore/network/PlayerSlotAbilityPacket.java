package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PlayerSlotAbilityPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerSlotAbilityPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("player_slot_ability"));

    public static final StreamCodec<FriendlyByteBuf, PlayerSlotAbilityPacket> STREAM_CODEC = StreamCodec.ofMember(
            PlayerSlotAbilityPacket::toBytes, PlayerSlotAbilityPacket::new
    );

    private final AbilityGroupId group;
    private final ResourceLocation ability;
    private final int slotIndex;

    public PlayerSlotAbilityPacket(AbilityGroupId group, int slotIndex, ResourceLocation ability) {
        this.group = group;
        this.slotIndex = slotIndex;
        this.ability = ability;
    }

    public PlayerSlotAbilityPacket(FriendlyByteBuf buf) {
        ability = buf.readResourceLocation();
        group = buf.readEnum(AbilityGroupId.class);
        slotIndex = buf.readInt();
    }

    public static void handle(PlayerSlotAbilityPacket packet, IPayloadContext context) {
        Player entity = context.player();

        MKCore.getPlayer(entity).ifPresent(playerData ->
                playerData.getLoadout()
                        .getAbilityGroup(packet.group)
                        .setSlot(packet.slotIndex, packet.ability));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(ability);
        buf.writeEnum(group);
        buf.writeInt(slotIndex);
    }
}
