package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class LootRandomizer {

    public LootConstructor getConstructorForTierAndSlot(LootTier tier, LootSlot slot){

        return null;
    }

    public List<LootConstructor> generateLootForEntity(LivingEntity entity, double[] chances, LootTier tier){
        return new ArrayList<>();
    }
}
