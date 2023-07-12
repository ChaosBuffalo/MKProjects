package com.chaosbuffalo.mkfaction;

import com.chaosbuffalo.mkfaction.capabilities.PlayerFactionHandler;
import com.chaosbuffalo.mkfaction.client.gui.FactionPage;
import com.chaosbuffalo.mkfaction.command.FactionCommand;
import com.chaosbuffalo.mkfaction.faction.FactionDefaultManager;
import com.chaosbuffalo.mkfaction.faction.FactionManager;
import com.chaosbuffalo.mkfaction.init.FactionCommands;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mkfaction.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKFactionMod.MODID)
public class MKFactionMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkfaction";
    private final FactionManager factionManager;
    private final FactionDefaultManager factionDefaultManager;

    public MKFactionMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::enqueueIMC);

        MinecraftForge.EVENT_BUS.register(this);
        MKFactions.register(modBus);

        factionManager = new FactionManager();
        factionDefaultManager = new FactionDefaultManager();
        FactionCommands.register(modBus);
    }


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.debug("MKFactionMod.setup");
        PacketHandler.setupHandler();
        TargetingHooks.registerHooks();
    }


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        FactionCommand.register(event.getDispatcher());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        PlayerFactionHandler.registerPersonaExtension();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(FactionPage::registerPlayerPage);
    }
}
