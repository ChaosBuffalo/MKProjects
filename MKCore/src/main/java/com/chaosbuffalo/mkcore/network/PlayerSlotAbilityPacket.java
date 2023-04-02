package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerSlotAbilityPacket {

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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(ability);
        buf.writeEnum(group);
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null) {
                return;
            }
            entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(playerData ->
                    playerData.getLoadout()
                            .getAbilityGroup(group)
                            .setSlot(slotIndex, ability));
        });
        ctx.setPacketHandled(true);
    }
}
