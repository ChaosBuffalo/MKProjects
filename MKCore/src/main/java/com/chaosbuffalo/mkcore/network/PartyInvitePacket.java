package com.chaosbuffalo.mkcore.network;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.PartyInvitePopup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class PartyInvitePacket implements CustomPacketPayload {
    private final UUID invitingUUID;
    public static final CustomPacketPayload.Type<PartyInvitePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "party_invite"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PartyInvitePacket> STREAM_CODEC = StreamCodec.ofMember(
            PartyInvitePacket::toBytes, PartyInvitePacket::new
    );

    public void toBytes(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeUUID(invitingUUID);
    }

    public PartyInvitePacket(FriendlyByteBuf buffer){
        this.invitingUUID = buffer.readUUID();
    }

    public PartyInvitePacket(Player player) {
        this.invitingUUID = player.getUUID();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PartyInvitePacket packet, IPayloadContext context) {
        ClientHandler.handleClient(packet);
    }

    static class ClientHandler {
        public static void handleClient(PartyInvitePacket packet) {
            Player player = Minecraft.getInstance().player;
            if (player == null || Minecraft.getInstance().level == null)
                return;
            Player inviting = Minecraft.getInstance().level.getPlayerByUUID(packet.invitingUUID);
            if (inviting == null) {
                return;
            }
            Minecraft.getInstance().setScreen(new PartyInvitePopup(inviting));
        }
    }
}
