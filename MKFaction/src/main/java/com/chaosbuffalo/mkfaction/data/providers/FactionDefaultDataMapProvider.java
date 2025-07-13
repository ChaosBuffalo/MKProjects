package com.chaosbuffalo.mkfaction.data.providers;

import com.chaosbuffalo.mkfaction.faction.EntityDefaultFaction;
import com.chaosbuffalo.mkfaction.init.FactionDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;

import java.util.concurrent.CompletableFuture;

public abstract class FactionDefaultDataMapProvider extends DataMapProvider {
    protected FactionDefaultDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    protected void tagGroup(EntityDefaultFaction faction, TagKey<EntityType<?>> tag) {
        Builder<EntityDefaultFaction, EntityType<?>> byEntity = builder(FactionDataMaps.ENTITY_DEFAULT_FACTION);
        byEntity.add(tag, faction, false);
    }

    protected void entityGroup(EntityDefaultFaction faction, EntityType<?>... types) {
        var byEntity = builder(FactionDataMaps.ENTITY_DEFAULT_FACTION);
        for (var type : types) {
            byEntity.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), faction, false);
        }
    }

    protected void entityGroup(EntityDefaultFaction faction, String... types) {
        var byEntity = builder(FactionDataMaps.ENTITY_DEFAULT_FACTION);
        for (var type : types) {
            EntityType.byString(type).ifPresent(t -> {
                byEntity.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(t), faction, false);
            });
        }
    }

    protected void moddedEntityGroup(String modId, EntityDefaultFaction faction, String... types) {
        var modLoaded = new ModLoadedCondition(modId);
        var byEntity = builder(FactionDataMaps.ENTITY_DEFAULT_FACTION);
        for (var type : types) {
            ResourceLocation typeId = ResourceLocation.parse(type);
            byEntity.add(typeId, faction, false, modLoaded);
        }
    }
}
