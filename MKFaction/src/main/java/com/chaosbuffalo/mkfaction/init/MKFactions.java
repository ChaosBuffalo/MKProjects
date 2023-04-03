package com.chaosbuffalo.mkfaction.init;


import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.FactionConstants;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class MKFactions {

    public static final DeferredRegister<MKFaction> REGISTRY = DeferredRegister.create(
            MKFactionRegistry.FACTION_REGISTRY_NAME, MKFactionMod.MODID);

    public static final ResourceLocation UNDEAD_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "undead");
    public static final ResourceLocation VILLAGER_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "villagers");
    public static final ResourceLocation DOMESTICATED_ANIMALS_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "domesticated_animals");
    public static final ResourceLocation WILD_ANIMALS_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "wild_animals");
    public static final ResourceLocation HOSTILE_ANIMALS_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "hostile_animals");
    public static final ResourceLocation ILLAGERS_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "illagers");
    public static final ResourceLocation MONSTERS_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "monsters");
    public static final ResourceLocation NEUTRAL_FACTION_NAME = new ResourceLocation(MKFactionMod.MODID,
            "neutral");

    public static RegistryObject<MKFaction> UNDEAD_FACTION = REGISTRY.register("undead",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static RegistryObject<MKFaction> VILLAGER_FACTION = REGISTRY.register("villagers",
            () -> new MKFaction(FactionConstants.FRIENDLY_THRESHOLD));

    public static RegistryObject<MKFaction> DOMESTICATED_ANIMALS_FACTION = REGISTRY.register("domesticated_animals",
            () -> new MKFaction(FactionConstants.FRIENDLY_THRESHOLD));

    public static RegistryObject<MKFaction> HOSTILE_ANIMALS_FACTION = REGISTRY.register("hostile_animals",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static RegistryObject<MKFaction> WILD_ANIMALS_FACTION = REGISTRY.register("wild_animals",
            () -> new MKFaction(FactionConstants.TRUE_NEUTRAL));

    public static RegistryObject<MKFaction> ILLAGERS_FACTION = REGISTRY.register("illagers",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static RegistryObject<MKFaction> MONSTERS_FACTION = REGISTRY.register("monsters",
            () -> new MKFaction(FactionConstants.ENEMY_THRESHOLD));

    public static RegistryObject<MKFaction> NEUTRAL_FACTION = REGISTRY.register("neutral",
            () -> new MKFaction(FactionConstants.TRUE_NEUTRAL));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

}
