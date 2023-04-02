package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExecuteActiveAbilityPacket {

    private final AbilityGroupId group;
    private final int slot;

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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null)
                return;

            entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(cap ->
                    cap.getAbilityExecutor().executeHotBarAbility(group, slot));
        });
        ctx.setPacketHandled(true);
    }
}
