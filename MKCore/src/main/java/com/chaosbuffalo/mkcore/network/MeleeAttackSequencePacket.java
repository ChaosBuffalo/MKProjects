package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.combat.IVisualMeleeAttackEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MeleeAttackSequencePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MeleeAttackSequencePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "melee_attack_sequence"));

    public static final StreamCodec<FriendlyByteBuf, MeleeAttackSequencePacket> STREAM_CODEC = StreamCodec.ofMember(
            MeleeAttackSequencePacket::toBytes, MeleeAttackSequencePacket::new
    );

    private final int attackerId;
    private final InteractionHand hand;
    private final int[] swingStartTicks;
    private final int[] swingDurationTicks;

    public MeleeAttackSequencePacket(int attackerId, InteractionHand hand, int[] swingStartTicks, int[] swingDurationTicks) {
        this.attackerId = attackerId;
        this.hand = hand;
        this.swingStartTicks = swingStartTicks;
        this.swingDurationTicks = swingDurationTicks;
    }

    public MeleeAttackSequencePacket(FriendlyByteBuf buf) {
        attackerId = buf.readInt();
        hand = buf.readEnum(InteractionHand.class);
        int swingCount = buf.readVarInt();
        swingStartTicks = new int[swingCount];
        for (int i = 0; i < swingCount; i++) {
            swingStartTicks[i] = buf.readVarInt();
        }
        swingDurationTicks = new int[swingCount];
        for (int i = 0; i < swingCount; i++) {
            swingDurationTicks[i] = buf.readVarInt();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(attackerId);
        buf.writeEnum(hand);
        buf.writeVarInt(swingStartTicks.length);
        for (int swingStartTick : swingStartTicks) {
            buf.writeVarInt(swingStartTick);
        }
        for (int swingDurationTick : swingDurationTicks) {
            buf.writeVarInt(swingDurationTick);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MeleeAttackSequencePacket packet, IPayloadContext context) {
        Player player = context.player();
        if (player == null) {
            return;
        }
        Entity attacker = player.level().getEntity(packet.attackerId);
        if (attacker instanceof Player attackerPlayer) {
            MKCore.getPlayer(attackerPlayer).ifPresent(data ->
                    data.getCombatExtension().startVisualMeleeAttackSequence(packet.hand, packet.swingStartTicks, packet.swingDurationTicks));
        } else if (attacker instanceof IVisualMeleeAttackEntity visualMeleeAttackEntity) {
            visualMeleeAttackEntity.startVisualMeleeAttackSequence(packet.hand, packet.swingStartTicks, packet.swingDurationTicks);
        }
    }
}
