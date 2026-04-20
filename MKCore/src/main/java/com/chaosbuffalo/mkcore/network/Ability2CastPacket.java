package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class Ability2CastPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<Ability2CastPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability2_cast"));
    public static final StreamCodec<FriendlyByteBuf, Ability2CastPacket> STREAM_CODEC = StreamCodec.ofMember(
            Ability2CastPacket::toBytes, Ability2CastPacket::new
    );

    private final int entityId;
    private final CastAction action;
    private final ResourceLocation abilityId;
    private final int castTicks;

    public enum CastAction {
        START,
        STOP
    }

    public Ability2CastPacket(int entityId, CastAction action, ResourceLocation abilityId, int castTicks) {
        this.entityId = entityId;
        this.action = action;
        this.abilityId = abilityId;
        this.castTicks = castTicks;
    }

    public static Ability2CastPacket start(Entity entity, ResourceLocation abilityId, int castTicks) {
        return new Ability2CastPacket(entity.getId(), CastAction.START, abilityId, castTicks);
    }

    public static Ability2CastPacket stop(Entity entity, ResourceLocation abilityId) {
        return new Ability2CastPacket(entity.getId(), CastAction.STOP, abilityId, 0);
    }

    private Ability2CastPacket(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.action = buffer.readEnum(CastAction.class);
        this.abilityId = buffer.readResourceLocation();
        this.castTicks = action == CastAction.START ? buffer.readInt() : 0;
    }

    private void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeEnum(action);
        buffer.writeResourceLocation(abilityId);
        if (action == CastAction.START) {
            buffer.writeInt(castTicks);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(Ability2CastPacket packet, IPayloadContext context) {
        ClientHandler.handleClient(packet);
    }

    static final class ClientHandler {
        private ClientHandler() {
        }

        static void handleClient(Ability2CastPacket packet) {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            Entity entity = level.getEntity(packet.entityId);
            if (entity == null) {
                return;
            }

            if (packet.action == CastAction.START) {
                MKCore.getAbilityRuntimeService().startClientCast(entity, packet.abilityId, packet.castTicks);
            } else {
                MKCore.getAbilityRuntimeService().stopClientCast(entity, packet.abilityId);
            }
        }
    }
}
