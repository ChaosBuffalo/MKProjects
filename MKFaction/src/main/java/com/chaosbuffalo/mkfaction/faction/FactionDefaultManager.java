package com.chaosbuffalo.mkfaction.faction;

import com.chaosbuffalo.mkcore.utils.SingleJsonFileReloadListener;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;

public class FactionDefaultManager extends SingleJsonFileReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HashMap<ResourceLocation, ResourceLocation> factionDefaults = new HashMap<>();

    public FactionDefaultManager() {
        super(GSON, MKFactionMod.MODID, "categories");
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    public static Optional<ResourceLocation> getDefaultFaction(ResourceLocation entityType) {
        return Optional.ofNullable(factionDefaults.get(entityType));
    }

    public static Optional<Holder<MKFaction>> getDefaultFaction(Entity entity) {
        return getDefaultFaction(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))
                .flatMap(factionId -> MKFactionRegistry.getFactionHolder(entity.registryAccess(), factionId));
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
            ResourceLocation factionName = ResourceLocation.parse(obj.get("name").getAsString());
            JsonArray members = obj.getAsJsonArray("defaultMembers");
            for (JsonElement memb : members) {
                ResourceLocation memberName = ResourceLocation.parse(memb.getAsString());
                factionDefaults.put(memberName, factionName);
            }
        }
    }
}
