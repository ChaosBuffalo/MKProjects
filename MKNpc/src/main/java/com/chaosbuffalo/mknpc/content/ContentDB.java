package com.chaosbuffalo.mknpc.content;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContentDB {

    static Map<ResourceKey<Level>, LazyOptional<IWorldNpcData>> levelCaps = new IdentityHashMap<>();
    static LazyOptional<IWorldNpcData> overworldData = LazyOptional.empty();


    @Nullable
    public static QuestChainInstance getQuestInstance(UUID questId) {
        return getQuestDB().getQuest(questId);
    }

    @Nonnull
    public static IWorldNpcData getQuestDB() {
        return getPrimaryData();
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
}
