package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid= MKCore.MOD_ID)
public class WorldUtils {
    private static final Map<ResourceKey<Level>, Double> difficultyBonuses = new HashMap<>();
    private static final Vec3i CENTER = new Vec3i(0, 0, 0);


    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.DIMENSION, (registry) -> {
            difficultyBonuses.clear();
            difficultyBonuses.putAll(registry.getDataMap(CoreDataMaps.DIMENSION_DIFFICULTY_BONUSES));
        });
    }

    public static double getDifficultyForGlobalPos(GlobalPos pos) {
        double diffOffset = difficultyBonuses.getOrDefault(pos.dimension(), 0.0);
        int manhattenDist = pos.pos().distManhattan(CENTER);
        int divisions = manhattenDist / MKConfig.SERVER.worldDifficultyBandSize.get();
        return Math.min(Math.max(GameConstants.MIN_DIFFICULTY,
                (divisions * MKConfig.SERVER.difficultyBandIncrease.get()) + diffOffset), GameConstants.MAX_DIFFICULTY);
    }
}
