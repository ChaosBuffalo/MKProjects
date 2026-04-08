package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PlayerMeleeAttackRequestPacket implements CustomPacketPayload {
    public static final Type<PlayerMeleeAttackRequestPacket> TYPE = new Type<>(MKCore.id("player_melee_attack_request"));
    public static final StreamCodec<FriendlyByteBuf, PlayerMeleeAttackRequestPacket> STREAM_CODEC = StreamCodec.ofMember(
            PlayerMeleeAttackRequestPacket::toBytes, PlayerMeleeAttackRequestPacket::new
    );

    private final int targetId;

    public PlayerMeleeAttackRequestPacket(int targetId) {
        this.targetId = targetId;
    }

    public PlayerMeleeAttackRequestPacket(FriendlyByteBuf buf) {
        targetId = buf.readInt();
    }

    private void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(targetId);
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
        MKCore.getPlayer(player).ifPresent(data -> data.getCombatExtension().handleServerMeleeAttackRequest(target));
    }
}
