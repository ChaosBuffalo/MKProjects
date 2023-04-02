package com.chaosbuffalo.mkfaction.network;

import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MobFactionAssignmentPacket {

    private final ResourceLocation factionName;
    private final int entityId;

    public MobFactionAssignmentPacket(IMobFaction mobFaction) {
        entityId = mobFaction.getEntity().getId();
        factionName = mobFaction.getFactionName();
    }

    public MobFactionAssignmentPacket(FriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        factionName = buffer.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(factionName);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handle(this));
        ctx.setPacketHandled(true);
    }

    public static class ClientHandler {
        public static void handle(MobFactionAssignmentPacket packet) {
            Level world = Minecraft.getInstance().level;
            if (world == null) {
                return;
            }

            Entity entity = world.getEntity(packet.entityId);
            if (entity != null) {
                entity.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY).ifPresent(mobFaction ->
                        mobFaction.setFactionName(packet.factionName));
            }
        }
    }
}