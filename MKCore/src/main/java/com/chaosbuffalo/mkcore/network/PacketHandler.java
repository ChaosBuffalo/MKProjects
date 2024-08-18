package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.packets.EntityDataUpdatePacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid=MKCore.MOD_ID, bus= EventBusSubscriber.Bus.MOD)
public class PacketHandler {

    private static final String VERSION = "1.0";


    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                EntityDataUpdatePacket.TYPE,
                EntityDataUpdatePacket.STREAM_CODEC,
                EntityDataUpdatePacket::handlePacket
        );
    }


//    public static void registerMessages() {
//        int id = 1;
//        networkChannel.registerMessage(id++, ExecuteActiveAbilityPacket.class, ExecuteActiveAbilityPacket::toBytes,
//                ExecuteActiveAbilityPacket::new, ExecuteActiveAbilityPacket::handle);
//        networkChannel.registerMessage(id++, EntityCastPacket.class, EntityCastPacket::toBytes,
//                EntityCastPacket::new, EntityCastPacket::handle);
//        networkChannel.registerMessage(id++, ParticleEffectSpawnPacket.class, ParticleEffectSpawnPacket::toBytes,
//                ParticleEffectSpawnPacket::new, ParticleEffectSpawnPacket::handle);
//        networkChannel.registerMessage(id++, PlayerAbilitiesSyncPacket.class, PlayerAbilitiesSyncPacket::toBytes,
//                PlayerAbilitiesSyncPacket::new, PlayerAbilitiesSyncPacket::handle);
//        networkChannel.registerMessage(id++, CritMessagePacket.class, CritMessagePacket::toBytes,
//                CritMessagePacket::new, CritMessagePacket::handle);
//        networkChannel.registerMessage(id++, TalentPointActionPacket.class, TalentPointActionPacket::toBytes,
//                TalentPointActionPacket::new, TalentPointActionPacket::handle);
//        networkChannel.registerMessage(id++, TalentDefinitionSyncPacket.class, TalentDefinitionSyncPacket::toBytes,
//                TalentDefinitionSyncPacket::new, TalentDefinitionSyncPacket::handle);
//        networkChannel.registerMessage(id++, PlayerSlotAbilityPacket.class, PlayerSlotAbilityPacket::toBytes,
//                PlayerSlotAbilityPacket::new, PlayerSlotAbilityPacket::handle);
//        networkChannel.registerMessage(id++, OpenLearnAbilitiesGuiPacket.class, OpenLearnAbilitiesGuiPacket::toBytes,
//                OpenLearnAbilitiesGuiPacket::new, OpenLearnAbilitiesGuiPacket::handle);
//        networkChannel.registerMessage(id++, PlayerLearnAbilityRequestPacket.class, PlayerLearnAbilityRequestPacket::toBytes,
//                PlayerLearnAbilityRequestPacket::new, PlayerLearnAbilityRequestPacket::handle);
//        networkChannel.registerMessage(id++, ResetAttackSwingPacket.class, ResetAttackSwingPacket::toBytes,
//                ResetAttackSwingPacket::new, ResetAttackSwingPacket::handle);
//        networkChannel.registerMessage(id++, MKParticleEffectSpawnPacket.class, MKParticleEffectSpawnPacket::toBytes,
//                MKParticleEffectSpawnPacket::new, MKParticleEffectSpawnPacket::handle);
//        networkChannel.registerMessage(id++, ParticleAnimationsSyncPacket.class, ParticleAnimationsSyncPacket::toBytes,
//                ParticleAnimationsSyncPacket::new, ParticleAnimationsSyncPacket::handle);
//        networkChannel.registerMessage(id++, MKParticleEffectEditorSpawnPacket.class, MKParticleEffectSpawnPacket::toBytes,
//                MKParticleEffectEditorSpawnPacket::new, MKParticleEffectEditorSpawnPacket::handle);
//        networkChannel.registerMessage(id++, ParticleAnimationEditorSyncPacket.class, ParticleAnimationEditorSyncPacket::toBytes,
//                ParticleAnimationEditorSyncPacket::new, ParticleAnimationEditorSyncPacket::handle);
//        networkChannel.registerMessage(id++, WriteAnimationPacket.class, WriteAnimationPacket::toBytes,
//                WriteAnimationPacket::new, WriteAnimationPacket::handle);
//        networkChannel.registerMessage(id++, ForgetAbilitiesRequestPacket.class, ForgetAbilitiesRequestPacket::toBytes,
//                ForgetAbilitiesRequestPacket::new, ForgetAbilitiesRequestPacket::handle);
//        networkChannel.registerMessage(id++, EntityEffectPacket.class, EntityEffectPacket::toBytes,
//                EntityEffectPacket::new, EntityEffectPacket::handle);
//        networkChannel.messageBuilder(EntityDataUpdatePacket.class, id++)
//                .consumerMainThread(EntityDataUpdatePacket::handleMainThread)
//                .encoder(EntityDataUpdatePacket::toBytes)
//                .decoder(EntityDataUpdatePacket::new)
//                .add();
//    }

    public static void sendMessageToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static void sendMessage(CustomPacketPayload msg, ServerPlayer target) {
        PacketDistributor.sendToPlayer(target, msg);
    }

    public static  void sendToTracking(CustomPacketPayload msg, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
    }

    public static  void sendToTrackingAndSelf(CustomPacketPayload msg, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
    }

    public static void sendToAll(CustomPacketPayload msg) {
        PacketDistributor.sendToAllPlayers(msg);

    }
}
