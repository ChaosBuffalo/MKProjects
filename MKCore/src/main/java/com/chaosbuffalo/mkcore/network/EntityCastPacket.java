package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class EntityCastPacket {

    private final int entityId;
    private ResourceLocation abilityId;
    private int castTicks;
    private final CastAction action;
    private CastInterruptReason interruptReason;
    @Nullable
    private AbilityClientState clientState;

    public enum CastAction {
        START,
        INTERRUPT
    }

    public EntityCastPacket(IMKEntityData entityData, ResourceLocation abilityId, int castTicks, @Nullable AbilityClientState clientState) {
        entityId = entityData.getEntity().getId();
        this.abilityId = abilityId;
        this.castTicks = castTicks;
        action = CastAction.START;
        this.clientState = clientState;
    }

    public EntityCastPacket(IMKEntityData entityData, CastAction action, CastInterruptReason reason) {
        entityId = entityData.getEntity().getId();
        this.action = action;
        interruptReason = reason;
        clientState = null;
    }

    public static EntityCastPacket start(IMKEntityData entityData, MKAbilityInfo abilityInfo, int castTicks, AbilityContext context) {
        return new EntityCastPacket(entityData, abilityInfo.getId(), castTicks, context.getClientState());
    }

    public static EntityCastPacket interrupt(IMKEntityData entityData, CastInterruptReason reason) {
        return new EntityCastPacket(entityData, CastAction.INTERRUPT, reason);
    }

    public EntityCastPacket(FriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        action = buffer.readEnum(CastAction.class);
        if (action == CastAction.START) {
            abilityId = buffer.readResourceLocation();
            castTicks = buffer.readInt();
            boolean hasClient = buffer.readBoolean();
            if (hasClient) {
                CompoundTag tag = buffer.readNbt();
                if (tag != null) {
                    clientState = AbilityClientState.CODEC.parse(NbtOps.INSTANCE, tag.get("client_state"))
                            .getOrThrow(false, MKCore.LOGGER::error);
                } else {
                    clientState = null;
                }

            }
        } else if (action == CastAction.INTERRUPT) {
            interruptReason = buffer.readEnum(CastInterruptReason.class);
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeEnum(action);
        if (action == CastAction.START) {
            buffer.writeResourceLocation(abilityId);
            buffer.writeInt(castTicks);
            buffer.writeBoolean(clientState != null);
            if (clientState != null) {
                CompoundTag tag = new CompoundTag();
                tag.put("client_state", AbilityClientState.CODEC.encodeStart(NbtOps.INSTANCE, clientState).getOrThrow(false, MKCore.LOGGER::error));
                buffer.writeNbt(tag);
            }
        } else if (action == CastAction.INTERRUPT) {
            buffer.writeEnum(interruptReason);
        }

    }

    public static void handle(EntityCastPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(EntityCastPacket packet) {
            Level world = Minecraft.getInstance().level;
            if (world == null)
                return;

            Entity entity = world.getEntity(packet.entityId);
            if (entity == null)
                return;

            MKCore.getEntityData(entity).ifPresent(entityData -> {
                if (packet.action == CastAction.START) {
                    entityData.getAbilityExecutor().startCastClient(packet.abilityId, packet.castTicks, packet.clientState);
                } else if (packet.action == CastAction.INTERRUPT) {
                    entityData.getAbilityExecutor().interruptCast(packet.interruptReason);
                }
            });
        }
    }
}
