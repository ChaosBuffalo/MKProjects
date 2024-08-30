package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.network.packets.SyncWeaponTypesPacket;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeaponTypeManager extends SimpleJsonResourceReloadListener {
    public static final List<IMKMeleeWeapon> MELEE_WEAPONS = new ArrayList<>();

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();


    public WeaponTypeManager() {
        super(GSON, "melee_weapon_types");
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
        NeoForge.EVENT_BUS.addListener(this::onDataPackSync);
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    public void onDataPackSync(OnDatapackSyncEvent event) {
        MKWeapons.LOGGER.debug("WeaponTypeManager.onDataPackSync");
        SyncWeaponTypesPacket updatePacket = new SyncWeaponTypesPacket(MeleeWeaponTypes.WEAPON_TYPES.values());
        if (event.getPlayer() != null) {
            // sync to single player
            MKWeapons.LOGGER.debug("Sending {} weapon definition update packet", event.getPlayer());
            PacketDistributor.sendToPlayer(event.getPlayer(), updatePacket);
        } else {
            // sync to playerlist
            PacketDistributor.sendToAllPlayers(updatePacket);
        }
    }

    private void parse(ResourceLocation loc, JsonObject json) {
        MKWeapons.LOGGER.debug("Parsing Weapon Type Json for {}", loc);
        IMeleeWeaponType weaponType = MeleeWeaponTypes.getWeaponType(loc);
        if (weaponType == null) {
            MKWeapons.LOGGER.warn("Failed to parse weapon type data for : {}", loc);
            return;
        }
        weaponType.deserialize(new Dynamic<>(JsonOps.INSTANCE, json));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        MKWeapons.LOGGER.debug("Loading melee weapon type definitions from Json");

        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKWeapons.LOGGER.debug("Found file: {}", resourcelocation);
            parse(entry.getKey(), entry.getValue().getAsJsonObject());
        }
    }

    public static void addMeleeWeapon(IMKMeleeWeapon weapon) {
        MELEE_WEAPONS.add(weapon);
    }
}
