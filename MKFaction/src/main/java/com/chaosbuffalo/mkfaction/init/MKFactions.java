package com.chaosbuffalo.mkfaction.init;


import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.FactionConstants;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MKFactions {

    public static final DeferredRegister<MKFaction> REGISTRY = DeferredRegister.create(
            MKFactionRegistry.FACTION_REGISTRY_KEY, MKFactionMod.MODID);

    public static final ResourceLocation UNDEAD_FACTION_NAME = MKFactionMod.id("undead");
    public static final ResourceLocation VILLAGER_FACTION_NAME = MKFactionMod.id("villagers");
    public static final ResourceLocation DOMESTICATED_ANIMALS_FACTION_NAME = MKFactionMod.id("domesticated_animals");
    public static final ResourceLocation WILD_ANIMALS_FACTION_NAME = MKFactionMod.id("wild_animals");
    public static final ResourceLocation HOSTILE_ANIMALS_FACTION_NAME = MKFactionMod.id("hostile_animals");
    public static final ResourceLocation ILLAGERS_FACTION_NAME = MKFactionMod.id("illagers");
    public static final ResourceLocation MONSTERS_FACTION_NAME = MKFactionMod.id("monsters");
    public static final ResourceLocation NEUTRAL_FACTION_NAME = MKFactionMod.id("neutral");

    public static DeferredHolder<MKFaction, MKFaction> UNDEAD_FACTION = REGISTRY.register("undead",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static DeferredHolder<MKFaction, MKFaction> VILLAGER_FACTION = REGISTRY.register("villagers",
            () -> new MKFaction(FactionConstants.FRIENDLY_THRESHOLD));

    public static DeferredHolder<MKFaction, MKFaction> DOMESTICATED_ANIMALS_FACTION = REGISTRY.register("domesticated_animals",
            () -> new MKFaction(FactionConstants.FRIENDLY_THRESHOLD));

    public static DeferredHolder<MKFaction, MKFaction> HOSTILE_ANIMALS_FACTION = REGISTRY.register("hostile_animals",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static DeferredHolder<MKFaction, MKFaction> WILD_ANIMALS_FACTION = REGISTRY.register("wild_animals",
            () -> new MKFaction(FactionConstants.TRUE_NEUTRAL));

    public static DeferredHolder<MKFaction, MKFaction> ILLAGERS_FACTION = REGISTRY.register("illagers",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static DeferredHolder<MKFaction, MKFaction> MONSTERS_FACTION = REGISTRY.register("monsters",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static DeferredHolder<MKFaction, MKFaction> NEUTRAL_FACTION = REGISTRY.register("neutral",
            () -> new MKFaction(FactionConstants.TRUE_NEUTRAL));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

}
