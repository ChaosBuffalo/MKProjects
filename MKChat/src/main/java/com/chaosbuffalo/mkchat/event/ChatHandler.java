package com.chaosbuffalo.mkchat.event;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
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
            player.getServer().getPlayerList().broadcast(null,
                    player.getX(), player.getY(), player.getZ(), ChatConstants.CHAT_RADIUS,
                    player.getLevel().dimension(),
                    new ClientboundChatPacket(event.getComponent(), ChatType.CHAT, player.getUUID()));

            List<Mob> entities = player.getLevel().getEntitiesOfClass(Mob.class,
                    getChatBoundingBox(player, ChatConstants.NPC_CHAT_RADIUS),
                    x -> x.getSensing().hasLineOfSight(player) && INpcDialogue.get(x).map(INpcDialogue::hasDialogue).orElse(false));

            for (Mob entity : entities) {
                INpcDialogue.get(entity).ifPresent(cap -> cap.receiveMessage(player, event.getMessage()));
            }
        }
        event.setCanceled(true);
    }
}
