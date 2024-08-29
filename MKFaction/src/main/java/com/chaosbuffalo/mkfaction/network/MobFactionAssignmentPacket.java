package com.chaosbuffalo.mkfaction.network;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MobFactionAssignmentPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MobFactionAssignmentPacket> TYPE = new CustomPacketPayload.Type<>(
            MKFactionMod.id("faction_assignment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MobFactionAssignmentPacket> STREAM_CODEC = StreamCodec.ofMember(
            MobFactionAssignmentPacket::toBytes, MobFactionAssignmentPacket::new
    );

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(factionName);
    }

    public static void handle(final MobFactionAssignmentPacket packet, IPayloadContext context) {
        ClientHandler.handle(packet);
    }

    public static class ClientHandler {
        public static void handle(MobFactionAssignmentPacket packet) {
            Level world = Minecraft.getInstance().level;
            if (world == null) {
                return;
            }

            Entity entity = world.getEntity(packet.entityId);
            if (entity != null) {
                IMobFaction.get(entity).ifPresent(mobFaction ->
                        mobFaction.setFactionName(packet.factionName));
            }
        }
    }
}
