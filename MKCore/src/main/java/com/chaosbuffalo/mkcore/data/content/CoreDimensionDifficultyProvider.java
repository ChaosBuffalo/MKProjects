package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.init.CoreDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public class CoreDimensionDifficultyProvider extends DataMapProvider {

    protected CoreDimensionDifficultyProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        super.gather(provider);
        builder(CoreDataMaps.DIMENSION_DIFFICULTY_BONUSES)
                .add(Level.OVERWORLD, 0.0,false)
                .add(Level.NETHER, 25.0,false)
                .add(Level.END, 40.0,false)
                .build();

    }

    @Override
    public String getName() {
        return "MK Core Difficulty DataMap";
    }
}
