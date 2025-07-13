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
    protected void gather(HolderLookup.Provider provider) {
        // #undead = #skeletons + #zombies + wither + phantom
        var undead = new EntityDefaultFaction(MKFactions.UNDEAD);
        tagGroup(undead, EntityTypeTags.UNDEAD);
        entityGroup(undead,
                EntityType.VEX,
                EntityType.GIANT
        );


        var domesticatedAnimals = new EntityDefaultFaction(MKFactions.DOMESTICATED_ANIMALS);
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


        var wildAnimals = new EntityDefaultFaction(MKFactions.WILD_ANIMALS);
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





        var hostileAnimals = new EntityDefaultFaction(MKFactions.HOSTILE_ANIMALS);
        entityGroup(hostileAnimals,
                EntityType.SPIDER,
                EntityType.CAVE_SPIDER,
                EntityType.POLAR_BEAR
        );


        var villagers = new EntityDefaultFaction(MKFactions.VILLAGERS);
        entityGroup(villagers,
                EntityType.VILLAGER,
                EntityType.IRON_GOLEM,
                EntityType.SNOW_GOLEM,
                EntityType.WANDERING_TRADER
        );


        var illagerFaction = new EntityDefaultFaction(MKFactions.ILLAGERS);
        // #illager = evoker + illusioner + pillager + vindicator
        tagGroup(illagerFaction, EntityTypeTags.ILLAGER);
        entityGroup(illagerFaction,
                EntityType.WITCH,
                EntityType.RAVAGER // only appears during illager raids
        );


        var monsterFaction = new EntityDefaultFaction(MKFactions.MONSTERS);
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

        // Mod Entries below

        final String MOD_ICEANDFIRE = "iceandfire";
        moddedEntityGroup(MOD_ICEANDFIRE, wildAnimals,
                "iceandfire:hippogryph",
                "iceandfire:pixie",
                "iceandfire:hippocampus",
                "iceandfire:amphithere"
        );
        moddedEntityGroup(MOD_ICEANDFIRE, monsterFaction,
                "iceandfire:sea_serpent",
                "iceandfire:fire_dragon",
                "iceandfire:ice_dragon",
                "iceandfire:lightning_dragon",
                "iceandfire:gorgon",
                "iceandfire:cyclops",
                "iceandfire:siren",
                "iceandfire:deathworm",
                "iceandfire:cockatrice",
                "iceandfire:stymphalian_bird",
                "iceandfire:troll",
                "iceandfire:myrmex_worker",
                "iceandfire:myrmex_soldier",
                "iceandfire:myrmex_sentinel",
                "iceandfire:myrmex_royal",
                "iceandfire:myrmex_queen",
                "iceandfire:myrmex_swarmer",
                "iceandfire:dread_thrall",
                "iceandfire:dread_ghoul",
                "iceandfire:dread_beast",
                "iceandfire:dread_scuttler",
                "iceandfire:dread_lich",
                "iceandfire:dread_knight",
                "iceandfire_dread_horse",
                "iceandfire:hydra",
                "iceandfire:ghost"
        );


        final String MOD_MOWZIES = "mowziesmobs";
        moddedEntityGroup(MOD_MOWZIES, monsterFaction,
                "mowziesmobs:foliaath",
                "mowziesmobs:baby_foliaath",
                "mowziesmobs:ferrous_wroughtnaut",
                "mowziesmobs:umvuthi",
                "mowziesmobs:frostmaw",
                "mowziesmobs:naga",
                "mowziesmobs:bluff"
        );
        moddedEntityGroup(MOD_MOWZIES, domesticatedAnimals,

                "mowziesmobs:umvuthana_follower_player",
                "mowziesmobs:umvuthana_crane_player"
        );
        moddedEntityGroup(MOD_MOWZIES, hostileAnimals,
                "mowziesmobs:umvuthana_follower_raptor",
                "mowziesmobs:umvuthana",
                "mowziesmobs:umvuthana_raptor",
                "mowzibesmobs:umvuthana_crane"
        );
        moddedEntityGroup(MOD_MOWZIES, wildAnimals,
                "mowziesmobs:grottol",
                "mowziesmobs:lantern",
                "mowziesmobs:sculptor"
        );

    }
}
