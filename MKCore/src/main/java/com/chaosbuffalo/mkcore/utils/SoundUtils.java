package com.chaosbuffalo.mkcore.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class SoundUtils {
    public static void playSoundAtEntity(Entity entity, SoundEvent event) {
        playSoundAtEntity(entity, event, entity.getSoundSource(), 1.0f, 1.0f);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundSource cat) {
        playSoundAtEntity(entity, event, cat, 1.0f, 1.0f);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundSource cat, float volume) {
        playSoundAtEntity(entity, event, cat, volume, 1.0F);
    }

    public static void clientPlaySoundAtPlayer(Player player, SoundEvent event, SoundSource cat, float volume, float pitch) {
        if (event == null) {
            return;
        }
        player.level().playSound(player, player.getX(), player.getY(), player.getZ(), event, cat, volume, pitch);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundSource cat, float volume, float pitch) {
        if (event == null) {
            return;
        }
        ClientHandler.playSoundAtEntity(entity, event, cat, volume, pitch);

    }

    public static class ClientHandler {
        public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundSource cat, float volume, float pitch) {
            entity.level().playSound(Minecraft.getInstance().player, entity.getX(), entity.getY(), entity.getZ(), event, cat, volume, pitch);
        }
    }

    public static void serverPlaySoundFromEntity(double x, double y, double z,
                                                 SoundEvent soundIn, SoundSource category, float volume, float pitch,
                                                 Entity source) {


        PlayLevelSoundEvent.AtEntity event = EventHooks
                .onPlaySoundAtEntity(source, BuiltInRegistries.SOUND_EVENT.getHolder(soundIn.getLocation()).orElseThrow(), category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        Holder<SoundEvent> pSound = event.getSound();
        SoundSource pSource = event.getSource();
        float pVolume = event.getNewVolume();
        float pPitch = event.getNewPitch();

        // TODO(1.21): Make sure this is the right way to broadcast a vanilla packet
        if (source.level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcastAndSend(source,
                    new ClientboundSoundPacket(pSound, pSource, x, y, z, pVolume, pPitch, source.level().random.nextLong()));
        }
    }

    public static void serverPlaySoundAtEntity(Entity source, SoundEvent soundIn, SoundSource category, float volume, float pitch) {
        serverPlaySoundFromEntity(source.getX(), source.getY(), source.getZ(), soundIn, category,
                volume, pitch, source);
    }

    public static void serverPlaySoundAtEntity(Entity source, SoundEvent soundIn, SoundSource category) {
        serverPlaySoundFromEntity(source.getX(), source.getY(), source.getZ(), soundIn, category,
                1.0f, 1.0f, source);
    }
}
