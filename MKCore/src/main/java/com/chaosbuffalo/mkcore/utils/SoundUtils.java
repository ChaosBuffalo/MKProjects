package com.chaosbuffalo.mkcore.utils;

import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

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
        player.level.playSound(player, player.getX(), player.getY(), player.getZ(), event, cat, volume, pitch);
    }

    public static void playSoundAtEntity(Entity entity, SoundEvent event, SoundSource cat, float volume, float pitch) {
        if (event == null) {
            return;
        }
        entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), event, cat, volume, pitch);
    }

    public static void serverPlaySoundFromEntity(double x, double y, double z,
                                                 SoundEvent soundIn, SoundSource category, float volume, float pitch,
                                                 Entity source) {
        net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory
                .onPlaySoundAtEntity(source, soundIn, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        soundIn = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> source).send(
                new ClientboundSoundPacket(soundIn, category, x, y, z, volume, pitch));
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
