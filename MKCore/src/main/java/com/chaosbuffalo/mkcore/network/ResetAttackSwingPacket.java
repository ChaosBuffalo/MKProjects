package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ResetAttackSwingPacket {

    private final int ticksToSet;

    public ResetAttackSwingPacket(int ticksToSet) {
        this.ticksToSet = ticksToSet;
    }

    public ResetAttackSwingPacket(FriendlyByteBuf buf) {
        ticksToSet = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(ticksToSet);
    }

    public static void handle(ResetAttackSwingPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(ResetAttackSwingPacket packet) {
            Player entity = Minecraft.getInstance().player;
            if (entity == null)
                return;
            // +2 to account for the client 2 tick lag before allowing attack
            MKCore.getPlayer(entity).ifPresent(cap ->
                    cap.getCombatExtension().setEntityTicksSinceLastSwing(packet.ticksToSet + 2));
            SoundUtils.clientPlaySoundAtPlayer(entity, CoreSounds.attack_cd_reset.get(), entity.getSoundSource(), 1.0f, 1.0f);
        }
    }
}
