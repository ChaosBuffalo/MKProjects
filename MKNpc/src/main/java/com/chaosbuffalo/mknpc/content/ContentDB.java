package com.chaosbuffalo.mknpc.content;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = MKNpc.MODID)
public class ContentDB {

    static Map<ResourceKey<Level>, IWorldNpcData> levelCaps = new IdentityHashMap<>();
    @Nullable
    static IWorldNpcData overworldData = null;

    @Nullable
    public static QuestChainInstance getQuestInstance(UUID questId) {
        return getQuestDB().getQuest(questId);
    }

    @Nonnull
    public static IWorldNpcData getQuestDB() {
        return getPrimaryData();
    }

    @Nonnull
    public static Optional<IWorldNpcData> tryGetLevelData(Level level) {
        IWorldNpcData cap = levelCaps.get(level.dimension());
        if (cap == null) {
            cap = IWorldNpcData.get(level);
            if (cap != null) {
                levelCaps.put(level.dimension(), cap);
            }
        }
        return Optional.ofNullable(cap);
    }

    @Nonnull
    public static IWorldNpcData getLevelData(Level level) {
        return tryGetLevelData(level).orElseThrow(IllegalStateException::new);
    }

    @Nonnull
    public static Optional<IWorldNpcData> tryGetPrimaryData() {
        return Optional.ofNullable(overworldData);
    }

    @Nonnull
    public static IWorldNpcData getPrimaryData() {
        return tryGetPrimaryData().orElseThrow(IllegalStateException::new);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            IWorldNpcData worldData = IWorldNpcData.get(serverLevel);

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
                overworldData = null;
            }
        }
    }
}
