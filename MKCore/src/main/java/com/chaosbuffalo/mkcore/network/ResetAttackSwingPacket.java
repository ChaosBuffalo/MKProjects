package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ResetAttackSwingPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResetAttackSwingPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("reset_attack_swing"));

    public static final StreamCodec<FriendlyByteBuf, ResetAttackSwingPacket> STREAM_CODEC = StreamCodec.ofMember(
            ResetAttackSwingPacket::toBytes, ResetAttackSwingPacket::new
    );

    private final InteractionHand hand;
    private final int ticksToSet;

    public ResetAttackSwingPacket(InteractionHand hand, int ticksToSet) {
        this.hand = hand;
        this.ticksToSet = ticksToSet;
    }

    public ResetAttackSwingPacket(FriendlyByteBuf buf) {
        hand = buf.readEnum(InteractionHand.class);
        ticksToSet = buf.readInt();
    }

    public static void handle(ResetAttackSwingPacket packet, IPayloadContext context) {
        ClientHandler.handleClient(packet, context.player());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(hand);
        buf.writeInt(ticksToSet);
    }

    static class ClientHandler {
        public static void handleClient(ResetAttackSwingPacket packet, Player entity) {
            // +2 to account for the client 2 tick lag before allowing attack
            MKCore.getPlayer(entity).ifPresent(cap ->
                    cap.getCombatExtension().setAttackStrengthTicks(packet.hand, packet.ticksToSet + 2));
            SoundUtils.clientPlaySoundAtPlayer(entity, CoreSounds.attack_cd_reset.value(), entity.getSoundSource(), 1.0f, 1.0f);
        }
    }
}
