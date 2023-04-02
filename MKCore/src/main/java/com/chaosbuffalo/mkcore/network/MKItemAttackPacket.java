package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.events.PostAttackEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MKItemAttackPacket {

    private final int entityId;

    public MKItemAttackPacket(Entity entity) {
        this.entityId = entity.getId();
    }

    public MKItemAttackPacket(FriendlyByteBuf buf) {
        entityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer entity = ctx.getSender();
            if (entity == null) {
                return;
            }
            Entity target = entity.getLevel().getEntity(entityId);
            AttributeInstance instance = entity.getAttribute(MKAttributes.ATTACK_REACH);
            if (instance == null)
                return;
            double reach = instance.getValue();
            if (target != null) {
                if (entity.distanceToSqr(target) <= reach * reach) {
                    entity.attack(target);
                    entity.resetAttackStrengthTicker();
                    MKCore.getEntityData(entity).ifPresent(cap -> cap.getCombatExtension().recordSwing());
                    MinecraftForge.EVENT_BUS.post(new PostAttackEvent(entity));
                }
            }
        });
        ctx.setPacketHandled(true);
    }

}
