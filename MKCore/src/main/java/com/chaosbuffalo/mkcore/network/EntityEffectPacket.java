package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class EntityEffectPacket {
    private final int entityId;
    private final Action action;
    private final UUID sourceId;
    private final List<MKActiveEffect> effects = new ArrayList<>();

    public enum Action {
        SET,
        REMOVE,
        SET_ALL
    }

    // Server
    public EntityEffectPacket(IMKEntityData entityData, MKActiveEffect activeEffect, Action action) {
        this.entityId = entityData.getEntity().getId();
        this.action = action;
        sourceId = activeEffect.getSourceId();
        effects.add(activeEffect);
    }

    // Server
    public EntityEffectPacket(IMKEntityData entityData, UUID sourceId, Collection<MKActiveEffect> effectInstances) {
        this.entityId = entityData.getEntity().getId();
        this.sourceId = sourceId;
        action = Action.SET_ALL;
        effects.addAll(effectInstances);
    }

    // Client
    public EntityEffectPacket(FriendlyByteBuf buffer) {
        entityId = buffer.readVarInt();
        action = buffer.readEnum(Action.class);
        sourceId = buffer.readUUID();

        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation effectId = buffer.readResourceLocation();
            CompoundTag data = buffer.readNbt();
            if (data == null)
                continue;

            MKActiveEffect instance = MKActiveEffect.deserializeClient(effectId, sourceId, data);
            if (instance != null) {
                effects.add(instance);
            }
        }
    }

    // Server
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeEnum(action);
        buffer.writeUUID(sourceId);
        buffer.writeVarInt(effects.size());
        for (MKActiveEffect effect : effects) {
            buffer.writeResourceLocation(effect.getEffect().getId());
            buffer.writeNbt(effect.serializeClient());
        }
    }

    public static void handle(EntityEffectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(EntityEffectPacket packet) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return;

            Entity target = mc.level.getEntity(packet.entityId);
            if (target == null)
                return;

            MKCore.getEntityData(target).ifPresent(data -> {
                EntityEffectHandler handler = data.getEffects();
                switch (packet.action) {
                    case SET: {
                        packet.effects.forEach(instance -> handler.clientSetEffect(packet.sourceId, instance));
                        break;
                    }
                    case REMOVE: {
                        packet.effects.forEach(instance -> handler.clientRemoveEffect(packet.sourceId, instance));
                        break;
                    }
                    case SET_ALL: {
                        handler.clientSetAllEffects(packet.sourceId, packet.effects);
                        break;
                    }
                }
            });
        }
    }
}
