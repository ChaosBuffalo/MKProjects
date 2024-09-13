package com.chaosbuffalo.mkfaction.network.packets;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.Optional;

public class MobFactionAssignmentPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MobFactionAssignmentPacket> TYPE = new CustomPacketPayload.Type<>(
            MKFactionMod.id("faction_assignment"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MobFactionAssignmentPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            i -> i.entityId,
            ByteBufCodecs.optional(
                    ByteBufCodecs.holderRegistry(MKFactionRegistry.FACTION_REGISTRY_KEY)
            ),
            i -> Optional.ofNullable(i.faction),
            MobFactionAssignmentPacket::new
    );

    @Nullable
    private final Holder<MKFaction> faction;
    private final int entityId;

    public MobFactionAssignmentPacket(int entityId, Optional<Holder<MKFaction>> mobFaction) {
        this.entityId = entityId;
        this.faction = mobFaction.orElse(null);
    }

    public MobFactionAssignmentPacket(IMobFaction mobFaction) {
        entityId = mobFaction.getEntity().getId();
        faction = mobFaction.getFaction();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
                        mobFaction.setFaction(packet.faction));
            }
        }
    }
}
