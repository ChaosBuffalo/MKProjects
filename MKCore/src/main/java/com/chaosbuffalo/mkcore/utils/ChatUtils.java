package com.chaosbuffalo.mkcore.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class ChatUtils {

    private static void sendPlayerChatMessage(Player playerEntity, Component message, boolean brackets) {
        if (brackets)
            message = ComponentUtils.wrapInSquareBrackets(message);
        playerEntity.displayClientMessage(message, false);
    }

    public static void sendMessageWithBrackets(Player playerEntity, String format, Object... args) {
        String message = String.format(format, args);
        sendMessageWithBrackets(playerEntity, message);
    }

    public static void sendMessageWithBrackets(Player playerEntity, String message) {
        sendMessageWithBrackets(playerEntity, new TextComponent(message));
    }

    public static void sendMessageWithBrackets(Player playerEntity, Component message) {
        sendPlayerChatMessage(playerEntity, message, true);
    }

    public static void sendMessage(Player playerEntity, String format, Object... args) {
        String message = String.format(format, args);
        sendMessage(playerEntity, message);
    }

    public static void sendMessage(Player playerEntity, String format) {
        sendMessage(playerEntity, new TextComponent(format));
    }

    public static void sendMessage(Player playerEntity, Component message) {
        sendPlayerChatMessage(playerEntity, message, false);
    }
}
