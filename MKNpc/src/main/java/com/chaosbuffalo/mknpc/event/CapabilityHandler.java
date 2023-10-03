package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mkcore.utils.CapabilityUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                var provider = CapabilityUtils.provider(NpcCapabilities.PLAYER_QUEST_DATA_CAPABILITY,
                        PlayerQuestingDataHandler::new,
                        player);

                event.addCapability(NpcCapabilities.MK_QUEST_CAP_ID, provider);
            } else {
                var provider = CapabilityUtils.provider(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY,
                        EntityNpcDataHandler::new,
                        livingEntity);

                event.addCapability(NpcCapabilities.MK_NPC_CAP_ID, provider);
            }
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachWorldCapability(AttachCapabilitiesEvent<Level> event) {
        var provider = CapabilityUtils.provider(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY,
                WorldNpcDataHandler::new,
                event.getObject());

        event.addCapability(NpcCapabilities.MK_WORLD_NPC_CAP_ID, provider);
    }

    @SubscribeEvent
    public static void attachChunkCapability(AttachCapabilitiesEvent<LevelChunk> event) {
        var provider = CapabilityUtils.provider(NpcCapabilities.CHUNK_NPC_DATA_CAPABILITY,
                ChunkNpcDataHandler::new,
                event.getObject());

        event.addCapability(NpcCapabilities.MK_CHUNK_NPC_CAP_ID, provider);
    }

    @SubscribeEvent
    public static void attachChestCapability(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof ChestBlockEntity chest) {
            var provider = CapabilityUtils.provider(NpcCapabilities.CHEST_NPC_DATA_CAPABILITY,
                    ChestNpcDataHandler::new,
                    chest);

            event.addCapability(NpcCapabilities.MK_CHEST_CAP_ID, provider);
        }
    }
}
