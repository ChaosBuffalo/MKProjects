package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MKChat.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatHandler {


    private static AABB getChatBoundingBox(ServerPlayer entity, double radius) {
        return new AABB(new BlockPos(entity.blockPosition())).inflate(radius, entity.getBbHeight(), radius);
    }

    @SubscribeEvent
    public static void handleServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player.getServer() != null) {

            DialogueUtils.sendMessageToAllAround(player, event.getMessage());

            List<Mob> entities = player.getLevel().getEntitiesOfClass(Mob.class,
                    getChatBoundingBox(player, ChatConstants.NPC_CHAT_RADIUS),
                    x -> x.getSensing().hasLineOfSight(player) && INpcDialogue.get(x).map(INpcDialogue::hasDialogue).orElse(false));

            for (Mob entity : entities) {
                INpcDialogue.get(entity).ifPresent(cap -> cap.receiveMessage(player, event.getMessage().getString()));
            }
        }
        event.setCanceled(true);
    }
}
