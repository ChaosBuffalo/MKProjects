package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkfaction.data.MKFactionDataProvider;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class MKUFactionProvider extends MKFactionDataProvider {

    public MKUFactionProvider(DataGenerator generator) {
        super(generator, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pCachedOutput) {
        return CompletableFuture.allOf(
                writeFaction(MKUFactions.GREEN_KNIGHTS_FACTION.get(), pCachedOutput),
                writeFaction(MKUFactions.HYBOREAN_DEAD.get(), pCachedOutput),
                writeFaction(MKUFactions.IMPERIAL_DEAD.get(), pCachedOutput),
                writeFaction(MKUFactions.SEE_OF_SOLANG.get(), pCachedOutput),
                writeFaction(MKUFactions.NETHER_MAGES.get(), pCachedOutput),
                writeFaction(MKUFactions.NECROTIDE_CULTISTS.get(), pCachedOutput),
                writeFaction(MKUFactions.GHOSTS_OF_HYBORIA.get(), pCachedOutput)
        );
    }
}
