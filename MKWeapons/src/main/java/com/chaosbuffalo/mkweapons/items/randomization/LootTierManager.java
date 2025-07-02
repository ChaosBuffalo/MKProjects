package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class LootTierManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "loot_tiers";
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<ResourceLocation, LootTier> LOOT_TIERS = new HashMap<>();

    public LootTierManager() {
        super(GSON, DEFINITION_FOLDER);
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        MKWeapons.LOGGER.debug("Loading loot tier definitions from Json");
        LOOT_TIERS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKWeapons.LOGGER.debug("Found loot tier file: {}", resourcelocation);

            DataResult<LootTier> dataRes = LootTier.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataRes.resultOrPartial(MKWeapons.LOGGER::error).ifPresent(tier -> {
                LOOT_TIERS.put(tier.getName(), tier);
            });
        }
    }

    public static LootTier getTierFromName(ResourceLocation name) {
        return LOOT_TIERS.get(name);
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this);
    }

}
