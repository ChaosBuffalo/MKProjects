package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NpcCapabilities {
    public static ResourceLocation MK_NPC_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "npc_data");
    public static ResourceLocation MK_WORLD_NPC_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "world_npc_data");
    public static ResourceLocation MK_CHUNK_NPC_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "chunk_npc_data");
    public static ResourceLocation MK_CHEST_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "chest_npc_data");
    public static ResourceLocation MK_QUEST_CAP_ID = new ResourceLocation(MKNpc.MODID,
            "player_quest_data");

    public static final Capability<IEntityNpcData> ENTITY_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IWorldNpcData> WORLD_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IChunkNpcData> CHUNK_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IChestNpcData> CHEST_NPC_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IPlayerQuestingData> PLAYER_QUEST_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IEntityNpcData.class);
        event.register(IWorldNpcData.class);
        event.register(IChestNpcData.class);
        event.register(IChunkNpcData.class);
        event.register(IPlayerQuestingData.class);
    }
}
