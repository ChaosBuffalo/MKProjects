package com.chaosbuffalo.mkcore;

public class GameConstants {
    public static final int DEFAULT_PASSIVE_ABILITIES = 0;
    public static final int MAX_PASSIVE_ABILITIES = 3;
    public static final int DEFAULT_ULTIMATE_ABILITIES = 0;
    public static final int MAX_ULTIMATE_ABILITIES = 2;
    public static final int DEFAULT_BASIC_ABILITIES = 0;
    public static final int MAX_BASIC_ABILITIES = 5;
    public static final int DEFAULT_ITEM_ABILITIES = 1;
    public static final int MAX_ITEM_ABILITIES = 1;

    public static final int ACTION_BAR_SIZE = MAX_BASIC_ABILITIES + MAX_ULTIMATE_ABILITIES;

    public static final int TICKS_PER_SECOND = 20;
    public static final float FTICKS_PER_SECOND = 20.0f;

    public static final int GLOBAL_COOLDOWN_TICKS = 1 * TICKS_PER_SECOND;
    public static final int DEFAULT_ABILITY_POOL_SIZE = 8;
    public static final int MAX_ABILITY_POOL_SIZE = 20;
    public static final double SKILL_MAX = 200.0;
    public static final double NATURAL_SKILL_MAX = 100.0;
    public static final double SKILL_POINTS_PER_LEVEL = 20.0;

    public static final double MAX_DIFFICULTY = 200.0;
    public static final double MIN_DIFFICULTY = 0.0;
}
