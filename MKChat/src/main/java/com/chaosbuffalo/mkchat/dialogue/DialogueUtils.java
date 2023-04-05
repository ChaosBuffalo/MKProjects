package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class DialogueUtils {
    private static final double CHAT_RADIUS = 5.0;

    public static void sendMessageToAllAround(MinecraftServer server, LivingEntity source,
                                              Component message) {

        server.getPlayerList().getPlayers().forEach(sp -> {
            if (sp.distanceToSqr(source) < ChatConstants.CHAT_RADIUS_SQ) {
                sp.sendSystemMessage(message);
            }
        });
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
