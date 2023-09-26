package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class DialogueUtils {

    public static void sendMessageToAllAround(LivingEntity speaker, Component message) {
        if (speaker.getLevel() instanceof ServerLevel serverLevel) {
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

    public static MutableComponent getSpeakerMessage(LivingEntity speaker, Component message) {
        // Generate a string that looks like: "<speaker_name> {message}", doesn't have the dialogue node formatting
        MutableComponent msg = Component.literal("<")
                .append(speaker.getDisplayName())
                .append("> ");
        msg.append(message);
        return msg;
    }

    public static String getItemNameProvider(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return String.format("{item:%s}", id.toString());
    }

    public static String getStackCountItemProvider(ItemStack item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item.getItem());
        return String.format("%d {item:%s}", item.getCount(), id.toString());
    }

    public static void throwParseException(String message) {
        MKChat.LOGGER.error(message);
        throw new DialogueDataParsingException(message);
    }
}
