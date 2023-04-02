package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.TalentDefinitionSyncPacket;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TalentManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "player_talents";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, TalentTreeDefinition> talentTreeMap = new HashMap<>();
    private Collection<TalentTreeDefinition> defaultTrees;

    public TalentManager() {
        super(GSON, DEFINITION_FOLDER);
        this.defaultTrees = null;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> objectIn,
                         @Nonnull ResourceManager resourceManagerIn,
                         @Nonnull ProfilerFiller profilerIn) {

        MKCore.LOGGER.info("Loading Talent definitions from json");
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation location = entry.getKey();
            if (location.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            parse(entry.getKey(), entry.getValue().getAsJsonObject());
        }
        defaultTrees = null;
    }

    private boolean parse(ResourceLocation loc, JsonObject json) {
        MKCore.LOGGER.debug("Parsing Talent Tree Json for {}", loc);
        ResourceLocation treeId = new ResourceLocation(loc.getNamespace(), "talent_tree." + loc.getPath());

        TalentTreeDefinition talentTree = TalentTreeDefinition.deserialize(treeId, new Dynamic<>(JsonOps.INSTANCE, json));

        registerTalentTree(talentTree);
        return true;
    }

    public TalentTreeDefinition getTalentTree(ResourceLocation treeId) {
        return talentTreeMap.get(treeId);
    }

    public Collection<TalentTreeDefinition> getDefaultTrees() {
        if (defaultTrees == null) {
            defaultTrees = talentTreeMap.values().stream()
                    .filter(TalentTreeDefinition::isDefault)
                    .collect(Collectors.toList());
        }
        return defaultTrees;
    }

    public void registerTalentTree(TalentTreeDefinition tree) {
        talentTreeMap.put(tree.getTreeId(), tree);
    }

    public Collection<ResourceLocation> getTreeNames() {
        return Collections.unmodifiableCollection(talentTreeMap.keySet());
    }


    @SubscribeEvent
    public void onDataPackSync(OnDatapackSyncEvent event) {
        MKCore.LOGGER.debug("TalentManager.onDataPackSync");
        TalentDefinitionSyncPacket updatePacket = new TalentDefinitionSyncPacket(talentTreeMap.values());
        if (event.getPlayer() != null) {
            // sync to single player
            MKCore.LOGGER.debug("Sending {} talent definition update packet", event.getPlayer());
            PacketHandler.sendMessage(updatePacket, event.getPlayer());
        } else {
            // sync to playerlist
            PacketHandler.sendToAll(updatePacket);
        }
    }
}
