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

    private static MKFaction.Builder undead() {
        var faction = new MKFaction.Builder(FactionConstants.ENEMY_THRESHOLD);
        faction.addEnemy(VILLAGERS);

        faction.addFirstName("Ted");
        faction.addFirstName("James");
        faction.addFirstName("Jolipnik");

        faction.addLastName("Smith");
        faction.addLastName("Caliente");
        faction.addLastName("Elkins");

        return faction;
    }

    private static MKFaction.Builder villagers() {
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

        return faction;
    }

    public static void bootstrap(BootstrapContext<MKFaction> context) {
        context.register(UNDEAD, undead()
                .build(context, UNDEAD));
        context.register(VILLAGERS, villagers()
                .build(context, VILLAGERS));

        context.register(DOMESTICATED_ANIMALS, new MKFaction.Builder(FactionConstants.TRUE_NEUTRAL)
                .build(context, DOMESTICATED_ANIMALS));

        context.register(WILD_ANIMALS, new MKFaction.Builder(FactionConstants.TRUE_NEUTRAL)
                .build(context, WILD_ANIMALS));

        context.register(HOSTILE_ANIMALS, new MKFaction.Builder(FactionConstants.ENEMY_THRESHOLD)
                .build(context, HOSTILE_ANIMALS));

        context.register(ILLAGERS, new MKFaction.Builder(FactionConstants.ENEMY_THRESHOLD)
                .build(context, ILLAGERS));

        context.register(MONSTERS, new MKFaction.Builder(FactionConstants.ENEMY_THRESHOLD)
                .build(context, MONSTERS));

        context.register(NEUTRAL, new MKFaction.Builder(FactionConstants.TRUE_NEUTRAL)
                .build(context, NEUTRAL));
    }
}
