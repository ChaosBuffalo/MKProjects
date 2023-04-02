package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.FactionConstants;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKFactionMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class Factions {

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

    @ObjectHolder("mkfaction:undead")
    public static MKFaction UNDEAD_FACTION;

    @ObjectHolder("mkfaction:villagers")
    public static MKFaction VILLAGER_FACTION;

    @ObjectHolder("mkfaction:domesticated_animals")
    public static MKFaction DOMESTICATED_ANIMALS_FACTION;

    @ObjectHolder("mkfaction:hostile_animals")
    public static MKFaction HOSTILE_ANIMALS_FACTION;

    @ObjectHolder("mkfaction:wild_animals")
    public static MKFaction WILD_ANIMALS_FACTION;

    @ObjectHolder("mkfaction:illagers")
    public static MKFaction ILLAGERS_FACTION;

    @ObjectHolder("mkfaction:monsters")
    public static MKFaction MONSTERS_FACTION;

    @ObjectHolder("mkfaction:neutral")
    public static MKFaction NEUTRAL_FACTION;


    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerFactions(RegistryEvent.Register<MKFaction> event) {
        event.getRegistry().register(new MKFaction(UNDEAD_FACTION_NAME, FactionConstants.ENEMY_THRESHOLD));
        event.getRegistry().register(new MKFaction(VILLAGER_FACTION_NAME,
                FactionConstants.FRIENDLY_THRESHOLD));
        event.getRegistry().register(new MKFaction(DOMESTICATED_ANIMALS_FACTION_NAME,
                FactionConstants.FRIENDLY_THRESHOLD));
        event.getRegistry().register(new MKFaction(HOSTILE_ANIMALS_FACTION_NAME, FactionConstants.ENEMY_THRESHOLD));
        event.getRegistry().register(new MKFaction(ILLAGERS_FACTION_NAME, FactionConstants.ENEMY_THRESHOLD));
        event.getRegistry().register(new MKFaction(MONSTERS_FACTION_NAME, FactionConstants.ENEMY_THRESHOLD));
        event.getRegistry().register(new MKFaction(NEUTRAL_FACTION_NAME, FactionConstants.TRUE_NEUTRAL));
        event.getRegistry().register(new MKFaction(WILD_ANIMALS_FACTION_NAME, FactionConstants.TRUE_NEUTRAL));
    }
}
