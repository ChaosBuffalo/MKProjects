package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.IWeaponData;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.network.PacketHandler;
import com.chaosbuffalo.mkweapons.network.SyncWeaponTypesPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeaponTypeManager extends SimpleJsonResourceReloadListener {
    private MinecraftServer server;
    public static final List<IMKMeleeWeapon> MELEE_WEAPONS = new ArrayList<>();
    private boolean serverStarted = false;

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();


    public WeaponTypeManager() {
        super(GSON, "melee_weapon_types");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void subscribeEvent(AddReloadListenerEvent event){
        event.addListener(this);
    }


    public static void addMeleeWeapon(IMKMeleeWeapon weapon){
        MELEE_WEAPONS.add(weapon);
    }


    public void syncToPlayers() {
        SyncWeaponTypesPacket updatePacket = new SyncWeaponTypesPacket(MeleeWeaponTypes.WEAPON_TYPES.values());
        PacketHandler.sendToAll(updatePacket);
    }

    @SubscribeEvent
    public void serverStart(ServerAboutToStartEvent event) {
        server = event.getServer();
        serverStarted = true;
    }


    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MKWeapons.LOGGER.debug("Player logged in weapon type manager");
        if (event.getPlayer() instanceof ServerPlayer) {
            SyncWeaponTypesPacket updatePacket = new SyncWeaponTypesPacket(MeleeWeaponTypes.WEAPON_TYPES.values());
            MKWeapons.LOGGER.debug("Sending {} update packet", event.getPlayer());
            PacketHandler.sendMessage(updatePacket, (ServerPlayer) event.getPlayer());
        }
    }

    public static void handleMKWeaponReloadForPlayerPre(Player player){
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof IMKMeleeWeapon){
            player.getAttributes().removeAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        }
    }

    public static void refreshAllWeapons(){
        for (IMKMeleeWeapon weapon : MELEE_WEAPONS){
            weapon.reload();
        }
    }

    public static void handleMKWeaponReloadForPlayerPost(Player player){
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof IMKMeleeWeapon){
            player.getAttributes().addTransientAttributeModifiers(mainHand.getAttributeModifiers(EquipmentSlot.MAINHAND));
        }
        for (ItemStack item : player.getInventory().items){
            if (!item.isEmpty()){
                item.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY)
                        .ifPresent(IWeaponData::markCacheDirty);
            }
        }
        for (ItemStack item : player.getInventory().offhand){
            if (!item.isEmpty()){
                item.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY)
                        .ifPresent(IWeaponData::markCacheDirty);
            }
        }
    }

    private boolean parse(ResourceLocation loc, JsonObject json) {
        MKWeapons.LOGGER.debug("Parsing Weapon Type Json for {}", loc);
        IMeleeWeaponType weaponType = MeleeWeaponTypes.getWeaponType(loc);
        if (weaponType == null) {
            MKWeapons.LOGGER.warn("Failed to parse weapon type data for : {}", loc);
            return false;
        }
        weaponType.deserialize(new Dynamic<>(JsonOps.INSTANCE, json));
        return true;
    }

    @SubscribeEvent
    public void serverStop(ServerStoppingEvent event) {
        serverStarted = false;
        server = null;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        MKWeapons.LOGGER.debug("Loading melee weapon type definitions from Json");
        boolean wasChanged = false;
        if (serverStarted){
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer entity : players){
                handleMKWeaponReloadForPlayerPre(entity);
            }
        }
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKWeapons.LOGGER.debug("Found file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            if (parse(entry.getKey(), entry.getValue().getAsJsonObject())) {
                wasChanged = true;
            }
        }
        refreshAllWeapons();
        if (serverStarted){
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer entity : players){
                handleMKWeaponReloadForPlayerPost(entity);
            }
        }

        if (serverStarted && wasChanged) {
            syncToPlayers();
        }
    }
}
