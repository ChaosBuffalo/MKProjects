package com.chaosbuffalo.mknpc.content;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.*;
import com.chaosbuffalo.mknpc.content.databases.ILevelOptionDatabase;
import com.chaosbuffalo.mknpc.content.databases.IQuestDatabase;
import com.chaosbuffalo.mknpc.content.databases.IStructureDatabase;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContentDB {

    static Map<ResourceKey<Level>, LazyOptional<IWorldNpcData>> levelCaps = new HashMap<>();
    static LazyOptional<IWorldNpcData> overworldData = LazyOptional.empty();


    @Nullable
    public static QuestChainInstance getQuestInstance(UUID questId) {
        return getQuests().getQuest(questId);
    }

    @Nonnull
    public static LazyOptional<IWorldNpcData> tryGetLevelData(Level level) {
        LazyOptional<IWorldNpcData> cap = levelCaps.get(level.dimension());
        if (cap == null) {
            cap = level.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY);
            if (cap.isPresent()) {
                levelCaps.put(level.dimension(), cap);
            }
        }
        return cap;
    }

    public static IQuestDatabase getQuests() {
        return getPrimaryData();
    }

    public static IStructureDatabase getStructures() {
        return getPrimaryData();
    }

    public static ILevelOptionDatabase getLevelOptions(Level level) {
        return getLevelData(level);
    }

    @Nonnull
    public static IWorldNpcData getLevelData(Level level) {
        return tryGetLevelData(level).orElseThrow(IllegalStateException::new);
    }

    @Nonnull
    public static LazyOptional<IWorldNpcData> tryGetPrimaryData() {
        return overworldData;
    }

    @Nonnull
    public static IWorldNpcData getPrimaryData() {
        return tryGetPrimaryData().orElseThrow(IllegalStateException::new);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            LazyOptional<IWorldNpcData> worldData = serverLevel.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY);

            levelCaps.put(serverLevel.dimension(), worldData);
            if (serverLevel.dimension() == Level.OVERWORLD) {
                overworldData = worldData;
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {

            levelCaps.remove(serverLevel.dimension());
            if (serverLevel.dimension() == Level.OVERWORLD) {
                overworldData = LazyOptional.empty();
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            QuestObjectDB.tick();
        }
    }
}
