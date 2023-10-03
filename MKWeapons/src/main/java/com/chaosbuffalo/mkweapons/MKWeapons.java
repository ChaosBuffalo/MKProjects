package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkweapons.capabilities.IArrowData;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.event.MKWeaponsEventHandler;
import com.chaosbuffalo.mkweapons.extensions.MKWCuriosExtension;
import com.chaosbuffalo.mkweapons.init.MKWeaponEffects;
import com.chaosbuffalo.mkweapons.init.MKWeaponsCommands;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.init.MKWeaponsParticles;
import com.chaosbuffalo.mkweapons.items.effects.IWeaponEffectsExtension;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import com.chaosbuffalo.mkweapons.network.PacketHandler;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public MKWeapons() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::processIMC);
        modBus.addListener(this::enqueueIMC);
        modBus.addListener(WeaponsCapabilities::registerCapabilities);
        setupRegistries(modBus);
        weaponTypeManager = new WeaponTypeManager();
        lootTierManager = new LootTierManager();
    }

    private void setupRegistries(IEventBus modBus) {
        MKWeaponsParticles.register(modBus);
        MKWeaponsItems.register(modBus);
        MKWeaponsCommands.register(modBus);
        MKWeaponEffects.register(modBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        PacketHandler.setupHandler();
        MKWeaponsEventHandler.registerCombatTriggers();
    }


    private void enqueueIMC(final InterModEnqueueEvent event) {
        MKWCuriosExtension.sendExtension();
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

    public static LazyOptional<IArrowData> getArrowCapability(AbstractArrow entity) {
        return entity.getCapability(WeaponsCapabilities.ARROW_DATA_CAPABILITY);
    }
}
