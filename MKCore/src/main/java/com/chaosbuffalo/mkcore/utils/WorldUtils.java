package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.GameConstants;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class WorldUtils {

    private static final Map<ResourceKey<Level>, Double> difficultyBonuses = new HashMap<>();
    private static final Vec3i CENTER = new Vec3i(0, 0, 0);
    private static final int DIFFICULTY_BAND_SIZE = 500;
    private static final double DIFFICULTY_SCORE_PER_BAND = 10.0;

    static {
        difficultyBonuses.put(Level.OVERWORLD, 0.0);
        difficultyBonuses.put(Level.NETHER, 25.0);
        difficultyBonuses.put(Level.END, 40.0);
    }

    public static void putDifficultyBonus(ResourceKey<Level> worldKey, double value) {
        difficultyBonuses.put(worldKey, value);
    }

    public static double getDifficultyForGlobalPos(GlobalPos pos) {
        double diffOffset = difficultyBonuses.getOrDefault(pos.dimension(), 0.0);
        int manhattenDist = pos.pos().distManhattan(CENTER);
        int divisions = manhattenDist / DIFFICULTY_BAND_SIZE;
        return Math.min(Math.max(GameConstants.MIN_DIFFICULTY, (divisions * DIFFICULTY_SCORE_PER_BAND) + diffOffset), GameConstants.MAX_DIFFICULTY);
    }
}
