package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static SimpleChannel networkChannel;
    private static final String VERSION = "1.0";

    public static void setupHandler() {
        networkChannel = NetworkRegistry.newSimpleChannel(MKCore.makeRL("packet_handler"),
                () -> VERSION,
                s -> s.equals(VERSION),
                s -> s.equals(VERSION));
        registerMessages();
    }

    public static SimpleChannel getNetworkChannel() {
        return networkChannel;
    }

    public static void registerMessages() {
        int id = 1;
        networkChannel.registerMessage(id++, PlayerDataSyncPacket.class, PlayerDataSyncPacket::toBytes,
                PlayerDataSyncPacket::new, PlayerDataSyncPacket::handle);
        networkChannel.registerMessage(id++, ExecuteActiveAbilityPacket.class, ExecuteActiveAbilityPacket::toBytes,
                ExecuteActiveAbilityPacket::new, ExecuteActiveAbilityPacket::handle);
        networkChannel.registerMessage(id++, EntityCastPacket.class, EntityCastPacket::toBytes,
                EntityCastPacket::new, EntityCastPacket::handle);
        networkChannel.registerMessage(id++, ParticleEffectSpawnPacket.class, ParticleEffectSpawnPacket::toBytes,
                ParticleEffectSpawnPacket::new, ParticleEffectSpawnPacket::handle);
        networkChannel.registerMessage(id++, PlayerAbilitiesSyncPacket.class, PlayerAbilitiesSyncPacket::toBytes,
                PlayerAbilitiesSyncPacket::new, PlayerAbilitiesSyncPacket::handle);
        networkChannel.registerMessage(id++, CritMessagePacket.class, CritMessagePacket::toBytes,
                CritMessagePacket::new, CritMessagePacket::handle);
        networkChannel.registerMessage(id++, PlayerLeftClickEmptyPacket.class, PlayerLeftClickEmptyPacket::toBytes,
                PlayerLeftClickEmptyPacket::new, PlayerLeftClickEmptyPacket::handle);
        networkChannel.registerMessage(id++, TalentPointActionPacket.class, TalentPointActionPacket::toBytes,
                TalentPointActionPacket::new, TalentPointActionPacket::handle);
        networkChannel.registerMessage(id++, TalentDefinitionSyncPacket.class, TalentDefinitionSyncPacket::toBytes,
                TalentDefinitionSyncPacket::new, TalentDefinitionSyncPacket::handle);
        networkChannel.registerMessage(id++, PlayerSlotAbilityPacket.class, PlayerSlotAbilityPacket::toBytes,
                PlayerSlotAbilityPacket::new, PlayerSlotAbilityPacket::handle);
        networkChannel.registerMessage(id++, OpenLearnAbilitiesGuiPacket.class, OpenLearnAbilitiesGuiPacket::toBytes,
                OpenLearnAbilitiesGuiPacket::new, OpenLearnAbilitiesGuiPacket::handle);
        networkChannel.registerMessage(id++, PlayerLearnAbilityRequestPacket.class, PlayerLearnAbilityRequestPacket::toBytes,
                PlayerLearnAbilityRequestPacket::new, PlayerLearnAbilityRequestPacket::handle);
        networkChannel.registerMessage(id++, MKItemAttackPacket.class, MKItemAttackPacket::toBytes,
                MKItemAttackPacket::new, MKItemAttackPacket::handle);
        networkChannel.registerMessage(id++, ResetAttackSwingPacket.class, ResetAttackSwingPacket::toBytes,
                ResetAttackSwingPacket::new, ResetAttackSwingPacket::handle);
        networkChannel.registerMessage(id++, MKParticleEffectSpawnPacket.class, MKParticleEffectSpawnPacket::toBytes,
                MKParticleEffectSpawnPacket::new, MKParticleEffectSpawnPacket::handle);
        networkChannel.registerMessage(id++, ParticleAnimationsSyncPacket.class, ParticleAnimationsSyncPacket::toBytes,
                ParticleAnimationsSyncPacket::new, ParticleAnimationsSyncPacket::handle);
        networkChannel.registerMessage(id++, MKParticleEffectEditorSpawnPacket.class, MKParticleEffectSpawnPacket::toBytes,
                MKParticleEffectEditorSpawnPacket::new, MKParticleEffectEditorSpawnPacket::handle);
        networkChannel.registerMessage(id++, ParticleAnimationEditorSyncPacket.class, ParticleAnimationEditorSyncPacket::toBytes,
                ParticleAnimationEditorSyncPacket::new, ParticleAnimationEditorSyncPacket::handle);
        networkChannel.registerMessage(id++, WriteAnimationPacket.class, WriteAnimationPacket::toBytes,
                WriteAnimationPacket::new, WriteAnimationPacket::handle);
        networkChannel.registerMessage(id++, EntityDataSyncPacket.class, EntityDataSyncPacket::toBytes,
                EntityDataSyncPacket::new, EntityDataSyncPacket::handle);
        networkChannel.registerMessage(id++, ForgetAbilitiesRequestPacket.class, ForgetAbilitiesRequestPacket::toBytes,
                ForgetAbilitiesRequestPacket::new, ForgetAbilitiesRequestPacket::handle);
        networkChannel.registerMessage(id++, EntityEffectPacket.class, EntityEffectPacket::toBytes,
                EntityEffectPacket::new, EntityEffectPacket::handle);
    }

    public static <T> void sendMessageToServer(T msg) {
        networkChannel.sendToServer(msg);
    }

    public static <T> void sendMessage(T msg, ServerPlayer target) {
        PacketDistributor.PLAYER.with(() -> target)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToTracking(T msg, Entity entity) {
        PacketDistributor.TRACKING_ENTITY.with(() -> entity)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToTrackingAndSelf(T msg, Entity entity) {
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity)
                .send(PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToAll(T msg) {
        PacketDistributor.ALL.noArg().send(
                PacketHandler.getNetworkChannel().toVanillaPacket(msg, NetworkDirection.PLAY_TO_CLIENT));

    }
}
