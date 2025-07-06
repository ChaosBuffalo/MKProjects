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

@EventBusSubscriber(modid = MKCore.MOD_ID)
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
        registrar.playToClient(
                CritMessagePacket.TYPE,
                CritMessagePacket.STREAM_CODEC,
                CritMessagePacket::handle
        );
        registrar.playToClient(
                EntityCastPacket.TYPE,
                EntityCastPacket.STREAM_CODEC,
                EntityCastPacket::handle
        );
        registrar.playToClient(
                EntityEffectPacket.TYPE,
                EntityEffectPacket.STREAM_CODEC,
                EntityEffectPacket::handle
        );
        registrar.playToServer(
                ExecuteActiveAbilityPacket.TYPE,
                ExecuteActiveAbilityPacket.STREAM_CODEC,
                ExecuteActiveAbilityPacket::handle
        );
        registrar.playToServer(
                ForgetAbilitiesRequestPacket.TYPE,
                ForgetAbilitiesRequestPacket.STREAM_CODEC,
                ForgetAbilitiesRequestPacket::handle
        );
        registrar.playToClient(
                MKParticleEffectSpawnPacket.TYPE,
                MKParticleEffectSpawnPacket.STREAM_CODEC,
                MKParticleEffectSpawnPacket::handle
        );
        registrar.playToServer(
                MKParticleEffectEditorSpawnPacket.TYPE,
                MKParticleEffectEditorSpawnPacket.STREAM_CODEC,
                MKParticleEffectEditorSpawnPacket::handle
        );
        registrar.playToClient(
                OpenLearnAbilitiesGuiPacket.TYPE,
                OpenLearnAbilitiesGuiPacket.STREAM_CODEC,
                OpenLearnAbilitiesGuiPacket::handle
        );
        registrar.playToServer(
                ParticleAnimationEditorSyncPacket.TYPE,
                ParticleAnimationEditorSyncPacket.STREAM_CODEC,
                ParticleAnimationEditorSyncPacket::handle
        );
        registrar.playToClient(
                ParticleAnimationsSyncPacket.TYPE,
                ParticleAnimationsSyncPacket.STREAM_CODEC,
                ParticleAnimationsSyncPacket::handle
        );
        registrar.playToClient(
                ParticleEffectSpawnPacket.TYPE,
                ParticleEffectSpawnPacket.STREAM_CODEC,
                ParticleEffectSpawnPacket::handle
        );
        registrar.playToClient(
                PlayerAbilitiesSyncPacket.TYPE,
                PlayerAbilitiesSyncPacket.STREAM_CODEC,
                PlayerAbilitiesSyncPacket::handle
        );
        registrar.playToServer(
                PlayerLearnAbilityRequestPacket.TYPE,
                PlayerLearnAbilityRequestPacket.STREAM_CODEC,
                PlayerLearnAbilityRequestPacket::handle
        );
        registrar.playToServer(
                PlayerSlotAbilityPacket.TYPE,
                PlayerSlotAbilityPacket.STREAM_CODEC,
                PlayerSlotAbilityPacket::handle
        );
        registrar.playToClient(
                ResetAttackSwingPacket.TYPE,
                ResetAttackSwingPacket.STREAM_CODEC,
                ResetAttackSwingPacket::handle
        );
        registrar.playToServer(
                TalentPointActionPacket.TYPE,
                TalentPointActionPacket.STREAM_CODEC,
                TalentPointActionPacket::handle
        );
        registrar.playToServer(
                WriteAnimationPacket.TYPE,
                WriteAnimationPacket.STREAM_CODEC,
                WriteAnimationPacket::handle
        );
        registrar.playToClient(
                PartyInvitePacket.TYPE,
                PartyInvitePacket.STREAM_CODEC,
                PartyInvitePacket::handle
        );
        registrar.playToServer(
                PartyInviteResponsePacket.TYPE,
                PartyInviteResponsePacket.STREAM_CODEC,
                PartyInviteResponsePacket::handle
        );
    }

    public static void sendMessageToServer(CustomPacketPayload msg) {
        PacketDistributor.sendToServer(msg);
    }

    public static void sendMessage(CustomPacketPayload msg, ServerPlayer target) {
        PacketDistributor.sendToPlayer(target, msg);
    }

    public static void sendToTracking(CustomPacketPayload msg, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
    }

    public static void sendToTrackingAndSelf(CustomPacketPayload msg, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
    }

    public static void sendToAll(CustomPacketPayload msg) {
        PacketDistributor.sendToAllPlayers(msg);

    }
}
