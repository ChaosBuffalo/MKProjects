package com.chaosbuffalo.mknpc.data.content;

import com.chaosbuffalo.mkfaction.data.providers.FactionDefaultDataMapProvider;
import com.chaosbuffalo.mkfaction.faction.EntityDefaultFaction;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class MKNpcDefaultFactionProvider extends FactionDefaultDataMapProvider {
    public MKNpcDefaultFactionProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        entityGroup(new EntityDefaultFaction(MKFactions.MONSTERS), MKNpcEntityTypes.FIRE_ELEMENTAL_TYPE.get());
    }
}
