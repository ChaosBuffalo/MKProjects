package com.chaosbuffalo.mkultra.data_generators;

import com.chaosbuffalo.mkfaction.data.MKFactionDataProvider;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;

import javax.annotation.Nonnull;

public class MKUFactionProvider extends MKFactionDataProvider {

    public MKUFactionProvider(DataGenerator generator) {
        super(MKUltra.MODID, generator);
    }

    public void run(@Nonnull HashCache cache) {
        writeFaction(MKUFactions.GREEN_KNIGHTS_FACTION.get(), cache);
        writeFaction(MKUFactions.HYBOREAN_DEAD.get(), cache);
        writeFaction(MKUFactions.IMPERIAL_DEAD.get(), cache);
        writeFaction(MKUFactions.SEE_OF_SOLANG.get(), cache);
        writeFaction(MKUFactions.NETHER_MAGES.get(), cache);
    }
}
