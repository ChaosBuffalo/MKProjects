package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid= MKNpc.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity) {
            if (e.getObject() instanceof Player) {
                PlayerQuestingDataProvider provider = new PlayerQuestingDataProvider((Player) e.getObject());
                attachCap(NpcCapabilities.MK_QUEST_CAP_ID, provider, e);
            } else {
                EntityNpcDataProvider provider = new EntityNpcDataProvider((LivingEntity) e.getObject());
                attachCap(NpcCapabilities.MK_NPC_CAP_ID, provider, e);
            }
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachWorldCapability(AttachCapabilitiesEvent<Level> e) {
        WorldNpcDataProvider provider = new WorldNpcDataProvider(e.getObject());
        attachCap(NpcCapabilities.MK_WORLD_NPC_CAP_ID, provider, e);
    }

    @SubscribeEvent
    public static void attachChunkCapability(AttachCapabilitiesEvent<LevelChunk> e) {
        ChunkNpcDataProvider provider = new ChunkNpcDataProvider(e.getObject());
        attachCap(NpcCapabilities.MK_CHUNK_NPC_CAP_ID, provider, e);
    }

    @SubscribeEvent
    public static void attachChestCapability(AttachCapabilitiesEvent<BlockEntity> e) {
        if (e.getObject() instanceof ChestBlockEntity) {
            ChestNpcDataProvider provider = new ChestNpcDataProvider((ChestBlockEntity) e.getObject());
            attachCap(NpcCapabilities.MK_CHEST_CAP_ID, provider, e);
        }
    }

    private static void attachCap(ResourceLocation capId, NpcCapabilities.Provider<?, ?> provider, AttachCapabilitiesEvent<?> event) {
        event.addCapability(capId, provider);
        event.addListener(provider::invalidate);
    }
}
