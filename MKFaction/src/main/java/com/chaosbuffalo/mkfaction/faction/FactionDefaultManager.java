package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.init.FactionDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = MKFactionMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class FactionDefaultManager {
    private static final Map<EntityType<?>, Holder<MKFaction>> factionDefaults = new HashMap<>();

    private FactionDefaultManager() {

    }

    public static Optional<Holder<MKFaction>> getDefaultFaction(Entity entity) {
        return getDefaultFaction(entity.getType());
    }

    public static Optional<Holder<MKFaction>> getDefaultFaction(EntityType<?> entityType) {
        return Optional.ofNullable(factionDefaults.get(entityType));
    }

    @SubscribeEvent
    public static void onDataMapUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.ENTITY_TYPE, entityRegistry -> {
            Registry<MKFaction> factionRegistry = event.getRegistries().registryOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY);
            factionDefaults.clear();
            entityRegistry.holders().forEach(typeHolder -> {
                EntityType<?> type = typeHolder.value();

                EntityDefaultFaction defFaction = typeHolder.getData(FactionDataMaps.ENTITY_DEFAULT_FACTION);
                if (defFaction != null) {
                    factionRegistry.getHolder(defFaction.faction()).ifPresentOrElse(factionHolder -> {
                        if (type.getCategory().isFriendly() && factionHolder.value().getDefaultPlayerScore() < 0) {
                            MKFactionMod.LOGGER.warn("Friendly mob {} assigned to enemy faction", type);
                        }
                        factionDefaults.put(typeHolder.value(), factionHolder);
                    }, () -> MKFactionMod.LOGGER.warn("Mob {} has invalid default faction assigned", typeHolder.key()));
                } else {
                    if (type.getCategory() != MobCategory.MISC) {
                        if (!type.getCategory().isFriendly()) {
                            MKFactionMod.LOGGER.warn("Enemy mob {} not assigned to enemy faction", type);
                        } else {
                            MKFactionMod.LOGGER.warn("Friendly mob {} not assigned to any faction", type);
                        }
                    }
                }
            });

            MKFactionMod.LOGGER.info("Default Faction assignments reloaded");
        });
    }
}
