package com.chaosbuffalo.mkchat.init;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.NpcDialogueHandler;
import com.chaosbuffalo.mkchat.capabilities.PlayerDialogueHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ChatAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKChat.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerDialogueHandler>> PLAYER_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "player_dialogue_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Player player) {
                    return new PlayerDialogueHandler(player);
                }
                throw new IllegalArgumentException("Cannot construct player_dialogue_data attachment for non-player entity " + holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<NpcDialogueHandler>> ENTITY_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "npc_dialogue_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof LivingEntity entity && !(entity instanceof Player)) {
                    return new NpcDialogueHandler(entity);
                }
                throw new IllegalArgumentException("Cannot construct npc_dialogue_data attachment for non-living entity " + holder);
            }).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
