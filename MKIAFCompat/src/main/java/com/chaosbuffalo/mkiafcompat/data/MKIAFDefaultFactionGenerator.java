package com.chaosbuffalo.mkiafcompat.data;

import com.chaosbuffalo.mkfaction.data.providers.FactionDefaultDataMapProvider;
import com.chaosbuffalo.mkfaction.faction.EntityDefaultFaction;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.iafenvoy.iceandfire.registry.IafEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class MKIAFDefaultFactionGenerator extends FactionDefaultDataMapProvider {

    protected MKIAFDefaultFactionGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        super.gather(provider);
        var monsterFaction = new EntityDefaultFaction(MKFactions.MONSTERS);
        entityGroup(monsterFaction,
              "iceandfire:sea_serpent"
        );

    }
}
