package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PlayerMeleeAttackRequestPacket implements CustomPacketPayload {
    public static final Type<PlayerMeleeAttackRequestPacket> TYPE = new Type<>(MKCore.id("player_melee_attack_request"));
    public static final StreamCodec<FriendlyByteBuf, PlayerMeleeAttackRequestPacket> STREAM_CODEC = StreamCodec.ofMember(
            PlayerMeleeAttackRequestPacket::toBytes, PlayerMeleeAttackRequestPacket::new
    );

    private final int targetId;
    private final List<InteractionHand> hands;

    public PlayerMeleeAttackRequestPacket(int targetId, List<InteractionHand> hands) {
        this.targetId = targetId;
        this.hands = List.copyOf(hands);
    }

    public PlayerMeleeAttackRequestPacket(FriendlyByteBuf buf) {
        targetId = buf.readInt();
        int handCount = buf.readVarInt();
        List<InteractionHand> decodedHands = new ArrayList<>(handCount);
        for (int i = 0; i < handCount; i++) {
            decodedHands.add(buf.readEnum(InteractionHand.class));
        }
        hands = List.copyOf(decodedHands);
    }

    private void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(targetId);
        buf.writeVarInt(hands.size());
        for (InteractionHand hand : hands) {
            buf.writeEnum(hand);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerMeleeAttackRequestPacket packet, IPayloadContext context) {
        Player player = context.player();
        if (player == null) {
            return;
        }
        Entity target = player.level().getEntity(packet.targetId);
        if (target == null) {
            return;
        }
        MKCore.getPlayer(player).ifPresent(data -> data.getCombatExtension().handleServerMeleeAttackRequest(target, packet.hands));
    }
}
