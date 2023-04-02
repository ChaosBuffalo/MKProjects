package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.util.SingleJsonFileReloadListener;
import com.google.gson.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;

public class FactionDefaultManager extends SingleJsonFileReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HashMap<ResourceLocation, ResourceLocation> factionDefaults = new HashMap<>();

    public FactionDefaultManager() {
        super(GSON, MKFactionMod.MODID, "categories");
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    public static Optional<ResourceLocation> getDefaultFaction(ResourceLocation entityType) {
        return Optional.ofNullable(factionDefaults.get(entityType));
    }

    public static Optional<ResourceLocation> getDefaultFaction(Entity entity) {
        return getDefaultFaction(ForgeRegistries.ENTITIES.getKey(entity.getType()));
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    @Override
    protected void apply(JsonObject objectIn, @Nullable ResourceManager resourceManagerIn,
                         @Nonnull ProfilerFiller profilerIn) {
        JsonArray arr = objectIn.getAsJsonArray("members");
        factionDefaults.clear();
        for (JsonElement ele : arr) {
            JsonObject obj = ele.getAsJsonObject();
            ResourceLocation factionName = new ResourceLocation(obj.get("name").getAsString());
            JsonArray members = obj.getAsJsonArray("defaultMembers");
            for (JsonElement memb : members) {
                ResourceLocation memberName = new ResourceLocation(memb.getAsString());
                factionDefaults.put(memberName, factionName);
            }
        }
    }
}
