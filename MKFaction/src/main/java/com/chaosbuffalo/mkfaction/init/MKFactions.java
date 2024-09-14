package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.FactionConstants;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.MKFactionRegistry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;


public class MKFactions {

    public static final ResourceKey<MKFaction> UNDEAD = key("undead");
    public static final ResourceKey<MKFaction> VILLAGERS = key("villagers");
    public static final ResourceKey<MKFaction> DOMESTICATED_ANIMALS = key("domesticated_animals");
    public static final ResourceKey<MKFaction> WILD_ANIMALS = key("wild_animals");
    public static final ResourceKey<MKFaction> HOSTILE_ANIMALS = key("hostile_animals");
    public static final ResourceKey<MKFaction> ILLAGERS = key("illagers");
    public static final ResourceKey<MKFaction> MONSTERS = key("monsters");
    public static final ResourceKey<MKFaction> NEUTRAL = key("neutral");


    private static ResourceKey<MKFaction> key(String name) {
        return ResourceKey.create(MKFactionRegistry.FACTION_REGISTRY_KEY, MKFactionMod.id(name));
    }

    private static MKFaction undead(BootstrapContext<MKFaction> context) {
        var faction = new MKFaction.Builder(FactionConstants.ENEMY_THRESHOLD);
        faction.addEnemy(VILLAGERS);

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
        faction.addAlly(DOMESTICATED_ANIMALS);
        faction.addEnemy(UNDEAD);
        faction.addEnemy(HOSTILE_ANIMALS);
        faction.addEnemy(MONSTERS);
        faction.addEnemy(ILLAGERS);

        faction.addFirstName("Ted");
        faction.addFirstName("James");
        faction.addFirstName("Jolipnik");

        faction.addLastName("Smith");
        faction.addLastName("Caliente");
        faction.addLastName("Elkins");

        return faction.build(context);
    }

    public static void bootstrap(BootstrapContext<MKFaction> context) {
        context.register(UNDEAD, undead(context));
        context.register(VILLAGERS, villagers(context));

        context.register(DOMESTICATED_ANIMALS, new MKFaction(FactionConstants.FRIENDLY_THRESHOLD));

        context.register(WILD_ANIMALS, new MKFaction(FactionConstants.TRUE_NEUTRAL));

        context.register(HOSTILE_ANIMALS, new MKFaction(FactionConstants.ENEMY_THRESHOLD));

        context.register(ILLAGERS, new MKFaction(FactionConstants.ENEMY_THRESHOLD));

        context.register(MONSTERS, new MKFaction(FactionConstants.ENEMY_THRESHOLD));

        context.register(NEUTRAL, new MKFaction(FactionConstants.TRUE_NEUTRAL));
    }
}
