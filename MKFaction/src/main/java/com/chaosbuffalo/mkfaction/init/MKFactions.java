package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.FactionConstants;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;


public class MKFactions {

    public static final ResourceKey<MKFaction> UNDEAD_FACTION_NAME = key("undead");
    public static final ResourceKey<MKFaction> VILLAGER_FACTION_NAME = key("villagers");
    public static final ResourceKey<MKFaction> DOMESTICATED_ANIMALS_FACTION_NAME = key("domesticated_animals");
    public static final ResourceKey<MKFaction> WILD_ANIMALS_FACTION_NAME = key("wild_animals");
    public static final ResourceKey<MKFaction> HOSTILE_ANIMALS_FACTION_NAME = key("hostile_animals");
    public static final ResourceKey<MKFaction> ILLAGERS_FACTION_NAME = key("illagers");
    public static final ResourceKey<MKFaction> MONSTERS_FACTION_NAME = key("monsters");
    public static final ResourceKey<MKFaction> NEUTRAL_FACTION_NAME = key("neutral");


    private static ResourceKey<MKFaction> key(String name) {
        return ResourceKey.create(MKFactionRegistry.FACTION_REGISTRY_KEY, MKFactionMod.id(name));
    }

    private static MKFaction undead(BootstrapContext<MKFaction> context) {
        var faction = new MKFaction.Builder(FactionConstants.ENEMY_THRESHOLD);
        faction.addEnemy(VILLAGER_FACTION_NAME);

        faction.addFirstName("Ted");
        faction.addFirstName("James");
        faction.addFirstName("Jolipnik");

        faction.addLastName("Smith");
        faction.addLastName("Caliente");
        faction.addLastName("Elkins");

        return faction.build(context);
    }

    private static MKFaction villagers(BootstrapContext<MKFaction> context) {
        var faction = new MKFaction.Builder(FactionConstants.FRIENDLY_THRESHOLD);
        faction.addAlly(DOMESTICATED_ANIMALS_FACTION_NAME);
        faction.addEnemy(UNDEAD_FACTION_NAME);
        faction.addEnemy(HOSTILE_ANIMALS_FACTION_NAME);
        faction.addEnemy(MONSTERS_FACTION_NAME);
        faction.addEnemy(ILLAGERS_FACTION_NAME);

        faction.addFirstName("Ted");
        faction.addFirstName("James");
        faction.addFirstName("Jolipnik");

        faction.addLastName("Smith");
        faction.addLastName("Caliente");
        faction.addLastName("Elkins");

        return faction.build(context);
    }

    public static void bootstrap(BootstrapContext<MKFaction> context) {
        context.register(UNDEAD_FACTION_NAME, undead(context));
        context.register(VILLAGER_FACTION_NAME, villagers(context));

        context.register(DOMESTICATED_ANIMALS_FACTION_NAME, new MKFaction(FactionConstants.FRIENDLY_THRESHOLD));

        context.register(WILD_ANIMALS_FACTION_NAME, new MKFaction(FactionConstants.TRUE_NEUTRAL));

        context.register(HOSTILE_ANIMALS_FACTION_NAME, new MKFaction(FactionConstants.ENEMY_THRESHOLD));

        context.register(ILLAGERS_FACTION_NAME, new MKFaction(FactionConstants.ENEMY_THRESHOLD));

        context.register(MONSTERS_FACTION_NAME, new MKFaction(FactionConstants.ENEMY_THRESHOLD));

        context.register(NEUTRAL_FACTION_NAME, new MKFaction(FactionConstants.TRUE_NEUTRAL));
    }
}
