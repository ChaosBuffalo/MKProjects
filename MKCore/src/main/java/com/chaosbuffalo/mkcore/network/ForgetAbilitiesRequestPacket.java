package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ForgetAbilitiesRequestPacket {
    private final List<ResourceLocation> forgetting;

    public ForgetAbilitiesRequestPacket(List<ResourceLocation> forgetting) {
        this.forgetting = forgetting;
    }

    public ForgetAbilitiesRequestPacket(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        forgetting = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            forgetting.add(buffer.readResourceLocation());
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(forgetting.size());
        for (ResourceLocation loc : forgetting) {
            buffer.writeResourceLocation(loc);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null)
                return;

            MKCore.getPlayer(player).ifPresent(playerData -> {
                for (ResourceLocation toForget : forgetting) {
                    playerData.getAbilities().unlearnAbility(toForget, AbilitySource.TRAINED);
                }
            });


        });
        ctx.setPacketHandled(true);
    }
}
