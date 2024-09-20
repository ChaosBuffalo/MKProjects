package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.List;

@EventBusSubscriber(modid = MKChat.MODID)
public class ChatHandler {

    private static AABB getChatBoundingBox(ServerPlayer entity, double radius) {
        return new AABB(new BlockPos(entity.blockPosition())).inflate(radius, entity.getBbHeight(), radius);
    }

    @SubscribeEvent
    public static void handleServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        var decorated = DialogueUtils.formatSpeakerMessage(player, event.getMessage());
        DialogueUtils.sendMessageToAllAround(player, decorated);

        List<Mob> entities = player.level().getEntitiesOfClass(Mob.class,
                getChatBoundingBox(player, ChatConstants.NPC_CHAT_RADIUS),
                x -> x.getSensing().hasLineOfSight(player) && INpcDialogue.getOrThrow(x).hasDialogue());

        for (Mob entity : entities) {
            INpcDialogue.get(entity).ifPresent(cap -> cap.receiveMessage(player, event.getMessage().getString()));
        }
        event.setCanceled(true);
    }
}
