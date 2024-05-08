package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class MobStats extends EntityStats {
    private int manaTick;
    private int poiseRegenTick;

    public MobStats(IMKEntityData data) {
        super(data);
        manaTick = 0;
        poiseRegenTick = 0;
    }

    @Override
    protected void doManaRegen(float current, float max, float regenRate) {
        // if getManaRegenRate == 1, this is 1 mana per 3 seconds
        final float manaTickPeriod = 3.0f;
        final int ticksPerMana = (int) (manaTickPeriod / regenRate * GameConstants.TICKS_PER_SECOND);

        manaTick++;
        while (manaTick >= ticksPerMana) {
            if (current < max) {
                float newValue = current + 1;
                setMana(newValue, newValue >= max);
                current = getMana();
            }
            manaTick -= ticksPerMana;
        }
    }

    @Override
    protected void doPoiseRegen(float current, float max, float regenRate) {
        // if getPoiseRegenRate == 1, this is 1 poise per 1 seconds
        final float poiseTickPeriod = 1.0f;
        final int ticksPerPoise = (int) (poiseTickPeriod / regenRate * GameConstants.TICKS_PER_SECOND);

        poiseRegenTick++;
        while (poiseRegenTick >= ticksPerPoise) {
            if (current < max) {
                float newValue = current + 1;
                setPoise(newValue, newValue >= max);
                current = getPoise();
            }
            poiseRegenTick -= ticksPerPoise;
        }
    }
}
