package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.EntityDefaultFaction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class FactionDataMaps {

    public static final DataMapType<EntityType<?>, EntityDefaultFaction> ENTITY_DEFAULT_FACTION = AdvancedDataMapType.builder(
                    MKFactionMod.id("entity_default_factions"),
                    Registries.ENTITY_TYPE,
                    EntityDefaultFaction.CODEC)
            .build();

    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(ENTITY_DEFAULT_FACTION);
    }
}
