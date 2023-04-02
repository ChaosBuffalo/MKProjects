package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.network.MKFactionDefinitionUpdatePacket;
import com.chaosbuffalo.mkfaction.network.PacketHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nonnull;
import java.util.Map;

public class FactionManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "factions";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public FactionManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn,
                         @Nonnull ResourceManager resourceManagerIn,
                         @Nonnull ProfilerFiller profilerIn) {
        MKFactionMod.LOGGER.debug("FactionManager reloading all files");
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation factionId = entry.getKey();
            if (factionId.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.

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
        MKFactionDefinitionUpdatePacket updatePacket = new MKFactionDefinitionUpdatePacket(MKFactionRegistry.FACTION_REGISTRY.getValues());
        if (event.getPlayer() != null) {
            // sync to single player
            MKFactionMod.LOGGER.debug("Sending {} faction definition update packet", event.getPlayer());
            event.getPlayer().connection.send(
                    PacketHandler.getNetworkChannel().toVanillaPacket(updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        } else {
            // sync to playerlist
            event.getPlayerList().broadcastAll(
                    PacketHandler.getNetworkChannel().toVanillaPacket(updatePacket, NetworkDirection.PLAY_TO_CLIENT));
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
