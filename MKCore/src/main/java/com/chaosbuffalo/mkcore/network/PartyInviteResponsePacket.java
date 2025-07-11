package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.PartyInvitePopup;
import com.chaosbuffalo.mkcore.party.PartyManager;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class PartyInviteResponsePacket implements CustomPacketPayload {
    private final UUID invitingUUID;
    private final boolean accepted;

    public static final CustomPacketPayload.Type<PartyInviteResponsePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "party_invite_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PartyInviteResponsePacket> STREAM_CODEC = StreamCodec.ofMember(
            PartyInviteResponsePacket::toBytes, PartyInviteResponsePacket::new
    );

    public void toBytes(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeUUID(invitingUUID);
        registryFriendlyByteBuf.writeBoolean(accepted);
    }

    public PartyInviteResponsePacket(FriendlyByteBuf buffer){
        this.invitingUUID = buffer.readUUID();
        this.accepted = buffer.readBoolean();
    }

    public PartyInviteResponsePacket(Player invitingPlayer, boolean response) {
        this.invitingUUID = invitingPlayer.getUUID();
        this.accepted = response;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(final PartyInviteResponsePacket packet, IPayloadContext context) {
        packet.handle(context);
    }

    private void handle(IPayloadContext ctx) {

        Player respondingPlayer = ctx.player();
        MinecraftServer server = ctx.player().getServer();
        if (server != null) {
            Player inviting = ctx.player().level().getPlayerByUUID(invitingUUID);
            if (inviting != null) {
                if (accepted) {
                    ChatUtils.sendMessage(inviting, Component.translatable("mk.core.party.inviter.accept.text", respondingPlayer.getDisplayName()));
                    ChatUtils.sendMessage(respondingPlayer, Component.translatable("mk.core.party.invitee.accept.text", inviting.getDisplayName()));
                    PartyManager.handleInviteAccept(server, inviting, respondingPlayer);
                } else {
                    ChatUtils.sendMessage(inviting, Component.translatable("mk.core.party.inviter.decline.text", respondingPlayer.getDisplayName()));
                    ChatUtils.sendMessage(respondingPlayer, Component.translatable("mk.core.party.invitee.decline.text", inviting.getDisplayName()));
                }
            }
        }
    }
}
