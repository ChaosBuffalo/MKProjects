package com.chaosbuffalo.mkultra.data.generators;


import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.generators.npc.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;

import java.util.concurrent.CompletableFuture;

public class MKUNpcProvider extends NpcDefinitionProvider {

    public MKUNpcProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(generator, lookupProvider, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                GreenKnightNpcs.writeDefinitions(this, cache),
                HyboreanNpcs.writeDefinitions(this, cache),
                IntroCastleNpcs.writeDefinitions(this, cache),
                ClericNpcs.writeDefinitions(this, cache),
                NecrotideNpcs.writeDefinitions(this, cache),
                SeawovenNpcs.writeDefinitions(this, cache),
                DecayingChurchNpcs.writeDefinitions(this, cache),
                ThemcromancerNpcs.writeDefinitions(this, cache)
        );
    }


    @Override
    public String getName() {
        return "MKU NPC GEN";
    }
}
