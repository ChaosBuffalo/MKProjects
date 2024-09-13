package com.chaosbuffalo.mkfaction.data.content;

import com.chaosbuffalo.mkfaction.data.providers.FactionDefaultDataMapProvider;
import com.chaosbuffalo.mkfaction.faction.EntityDefaultFaction;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class MKFactionsDefaultFactionGenerator extends FactionDefaultDataMapProvider {
    public MKFactionsDefaultFactionGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        // #undead = #skeletons + #zombies + wither + phantom
        var undead = new EntityDefaultFaction(MKFactions.UNDEAD_FACTION_NAME);
        tagGroup(undead, EntityTypeTags.UNDEAD);
        entityGroup(undead,
                EntityType.VEX,
                EntityType.GIANT
        );


        var domesticatedAnimals = new EntityDefaultFaction(MKFactions.DOMESTICATED_ANIMALS_FACTION_NAME);
        entityGroup(domesticatedAnimals,
                EntityType.BEE,
                EntityType.CAT,
                EntityType.CHICKEN,
                EntityType.COW,
                EntityType.MOOSHROOM,
                EntityType.DONKEY,
                EntityType.HORSE,
                EntityType.LLAMA,
                EntityType.MULE,
                EntityType.PARROT,
                EntityType.PIG,
                EntityType.SHEEP,
                EntityType.TRADER_LLAMA,
                EntityType.CAMEL
        );


        var wildAnimals = new EntityDefaultFaction(MKFactions.WILD_ANIMALS_FACTION_NAME);
        entityGroup(wildAnimals,
                EntityType.BAT,
                EntityType.COD,
                EntityType.DOLPHIN,
                EntityType.FOX,
                EntityType.GOAT,
                EntityType.FROG,
                EntityType.OCELOT,
                EntityType.PANDA,
                EntityType.PUFFERFISH,
                EntityType.RABBIT,
                EntityType.SALMON,
                EntityType.SQUID,
                EntityType.GLOW_SQUID,
                EntityType.TROPICAL_FISH,
                EntityType.TURTLE,
                EntityType.WOLF,
                EntityType.STRIDER,
                EntityType.TADPOLE,
                EntityType.ALLAY,
                EntityType.ARMADILLO,
                EntityType.AXOLOTL,
                EntityType.SNIFFER
        );


        var hostileAnimals = new EntityDefaultFaction(MKFactions.HOSTILE_ANIMALS_FACTION_NAME);
        entityGroup(hostileAnimals,
                EntityType.SPIDER,
                EntityType.CAVE_SPIDER,
                EntityType.POLAR_BEAR
        );


        var villagers = new EntityDefaultFaction(MKFactions.VILLAGER_FACTION_NAME);
        entityGroup(villagers,
                EntityType.VILLAGER,
                EntityType.IRON_GOLEM,
                EntityType.SNOW_GOLEM,
                EntityType.WANDERING_TRADER
        );


        var illagerFaction = new EntityDefaultFaction(MKFactions.ILLAGERS_FACTION_NAME);
        // #illager = evoker + illusioner + pillager + vindicator
        tagGroup(illagerFaction, EntityTypeTags.ILLAGER);
        entityGroup(illagerFaction,
                EntityType.WITCH,
                EntityType.RAVAGER // only appears during illager raids
        );


        var monsterFaction = new EntityDefaultFaction(MKFactions.MONSTERS_FACTION_NAME);
        entityGroup(monsterFaction,
                "minecraft:blaze",
                "minecraft:creeper",
                "minecraft:elder_guardian",
                "minecraft:ender_dragon",
                "minecraft:enderman",
                "minecraft:endermite",
                "minecraft:ghast",
                "minecraft:guardian",
                "minecraft:magma_cube",
                "minecraft:shulker",
                "minecraft:silverfish",
                "minecraft:slime",
                "minecraft:phantom"
        );
        entityGroup(monsterFaction,
                EntityType.HOGLIN,
                EntityType.WARDEN,
                EntityType.PIGLIN,
                EntityType.PIGLIN_BRUTE,
                EntityType.BREEZE
        );
    }
}
