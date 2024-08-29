package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.network.MKFactionDefinitionUpdatePacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Map;

public class FactionManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "factions";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public FactionManager() {
        super(GSON, DEFINITION_FOLDER);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn,
                         @Nonnull ResourceManager resourceManagerIn,
                         @Nonnull ProfilerFiller profilerIn) {
        MKFactionMod.LOGGER.debug("FactionManager reloading all files");
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation factionId = entry.getKey();

            MKFactionMod.LOGGER.debug("Found file: {}", factionId);
            parseFaction(factionId, entry.getValue().getAsJsonObject());
        }
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    @SubscribeEvent
    public void onDataPackSync(OnDatapackSyncEvent event) {
        MKFactionMod.LOGGER.debug("FactionManager.onDataPackSync");
        MKFactionDefinitionUpdatePacket updatePacket = new MKFactionDefinitionUpdatePacket(MKFactionRegistry.FACTION_REGISTRY);
        if (event.getPlayer() != null) {
            // sync to single player
            MKFactionMod.LOGGER.debug("Sending {} faction definition update packet", event.getPlayer());
            PacketDistributor.sendToPlayer(event.getPlayer(), updatePacket);
        } else {
            // sync to playerlist
            PacketDistributor.sendToAllPlayers(updatePacket);
        }
    }

    private void parseFaction(ResourceLocation factionId, JsonObject json) {
        MKFactionMod.LOGGER.debug("Parsing Faction Json for {}", factionId);
        MKFaction faction = MKFactionRegistry.getFaction(factionId);
        if (faction == null) {
            MKFactionMod.LOGGER.warn("Failed to parse faction data for : {}", factionId);
            return;
        }
        faction.deserialize(new Dynamic<>(JsonOps.INSTANCE, json));
        MKFactionMod.LOGGER.info("Updated Faction: {} default score: {}", factionId, faction.getDefaultPlayerScore());
    }
}
