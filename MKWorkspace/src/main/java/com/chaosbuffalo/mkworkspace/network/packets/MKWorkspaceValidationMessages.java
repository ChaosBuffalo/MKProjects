package com.chaosbuffalo.mkworkspace.network.packets;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

final class MKWorkspaceValidationMessages {
    private MKWorkspaceValidationMessages() {
    }

    static void displayValidationErrors(ServerPlayer player, List<String> errors) {
        if (errors.isEmpty()) {
            return;
        }
        player.displayClientMessage(Component.literal("Workspace validation failed:"), false);
        for (String error : errors) {
            player.displayClientMessage(Component.literal(" - " + error), false);
        }
    }

    static void displayFailure(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), false);
    }
}
