package com.chaosbuffalo.mkweapons;

import com.chaosbuffalo.mkweapons.capabilities.IArrowData;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.command.WeaponsCommands;
import com.chaosbuffalo.mkweapons.event.MKWeaponsEventHandler;
import com.chaosbuffalo.mkweapons.extensions.MKWCuriosExtension;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.LootTierManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.chaosbuffalo.mkweapons.items.effects.IWeaponEffectsExtension;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import com.chaosbuffalo.mkweapons.network.PacketHandler;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
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
public class MKWeapons
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkweapons";
    public static final String REGISTER_MK_WEAPONS_EXTENSION = "register_mk_weapons_extension";
    public WeaponTypeManager weaponTypeManager;
    public LootTierManager lootTierManager;

    public MKWeapons() {
        MinecraftForge.EVENT_BUS.register(this);
        LootSlotManager.setupLootSlots();
        RandomizationSlotManager.setupRandomizationSlots();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        weaponTypeManager = new WeaponTypeManager();
        lootTierManager = new LootTierManager();

    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        PacketHandler.setupHandler();
        MKWeaponsEventHandler.registerCombatTriggers();
        WeaponsCommands.registerArguments();

    }


    private void enqueueIMC(final InterModEnqueueEvent event) {
        MKWCuriosExtension.sendExtension();
    }

    private void processIMC(final InterModProcessEvent event)
    {
        LOGGER.info("MKWeapons.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.getMethod().equals(REGISTER_MK_WEAPONS_EXTENSION)) {
                LOGGER.info("IMC register weapon extensions from mod {} {}", m.getSenderModId(),
                        m.getMethod());
                IWeaponEffectsExtension ext = (IWeaponEffectsExtension) m.getMessageSupplier().get();
                ext.registerWeaponEffectsExtension();
            }
        });

    }

    private void clientSetup(final FMLClientSetupEvent event){
        MKWeaponsItems.registerItemProperties();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event){
        WeaponsCommands.register(event.getDispatcher());
    }

    public static LazyOptional<IArrowData> getArrowCapability(AbstractArrow entity){
        return entity.getCapability(WeaponsCapabilities.ARROW_DATA_CAPABILITY);
    }
}
