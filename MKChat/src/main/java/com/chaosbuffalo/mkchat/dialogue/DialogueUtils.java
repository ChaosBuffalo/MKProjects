package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DialogueUtils {

    public static void sendMessageToAllAround(LivingEntity speaker, Component message) {
        if (speaker.level() instanceof ServerLevel serverLevel) {
            sendMessageToAllAround(serverLevel, speaker, message);
        }
    }

    public static void sendMessageToAllAround(ServerLevel serverLevel, LivingEntity source, Component message) {
        serverLevel.players().forEach(sp -> {
            if (sp.distanceToSqr(source) < ChatConstants.CHAT_RADIUS_SQ) {
                sp.sendSystemMessage(message);
            }
        });
    }

    public static Component formatSpeakerMessage(LivingEntity speaker, Component message) {
        // Generate a string that looks like: "<speaker_name> {message}", doesn't have the dialogue node formatting
        return ChatType.bind(ChatType.CHAT, speaker).decorate(message);
    }

    public static String getItemNameProvider(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return String.format("{item:%s}", id);
    }

    public static String getStackCountItemProvider(ItemStack item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.getItem());
        return String.format("%d {item:%s}", item.getCount(), id);
    }

}
