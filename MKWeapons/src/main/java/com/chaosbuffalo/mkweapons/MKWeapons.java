package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkweapons.capabilities.WeaponsAttachments;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.event.MKWeaponsEventHandler;
import com.chaosbuffalo.mkweapons.init.MKWeaponEffects;
import com.chaosbuffalo.mkweapons.init.MKWeaponsCommands;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import com.chaosbuffalo.mkweapons.items.effects.IWeaponEffectsExtension;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import com.chaosbuffalo.mkweapons.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKWeapons.MODID)
public class MKWeapons {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkweapons";
    public static final String REGISTER_MK_WEAPONS_EXTENSION = "register_mk_weapons_extension";
    public final WeaponTypeManager weaponTypeManager;
    public final LootTierManager lootTierManager;

    public MKWeapons(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::processIMC);
        modBus.addListener(PacketHandler::register);
        setupRegistries(modBus);
        weaponTypeManager = new WeaponTypeManager();
        lootTierManager = new LootTierManager();
    }

    private void setupRegistries(IEventBus modBus) {
        MKWeaponsParticles.register(modBus);
        MKWeaponsItems.register(modBus);
        MKWeaponsCommands.register(modBus);
        MKWeaponEffects.register(modBus);
        WeaponsAttachments.register(modBus);
        WeaponsComponents.register(modBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        MKWeaponsEventHandler.registerCombatTriggers();
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("MKWeapons.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_MK_WEAPONS_EXTENSION)) {
                LOGGER.info("IMC register weapon extensions from mod {} {}", m.senderModId(),
                        m.method());
                IWeaponEffectsExtension ext = (IWeaponEffectsExtension) m.messageSupplier().get();
                ext.registerWeaponEffectsExtension();
            }
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(MKWeaponsItems::registerItemProperties);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MKWeaponsCommands.registerCommands(event.getDispatcher());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
