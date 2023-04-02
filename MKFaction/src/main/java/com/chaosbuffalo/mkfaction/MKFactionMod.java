package com.chaosbuffalo.mkfaction;

import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.capabilities.PlayerFactionHandler;
import com.chaosbuffalo.mkfaction.client.gui.FactionPage;
import com.chaosbuffalo.mkfaction.command.FactionCommand;
import com.chaosbuffalo.mkfaction.event.InputHandler;
import com.chaosbuffalo.mkfaction.faction.FactionDefaultManager;
import com.chaosbuffalo.mkfaction.faction.FactionManager;
import com.chaosbuffalo.mkfaction.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
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
    private FactionManager factionManager;
    private FactionDefaultManager factionDefaultManager;

    public MKFactionMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        MinecraftForge.EVENT_BUS.register(this);
        factionManager = new FactionManager();
        factionDefaultManager = new FactionDefaultManager();
    }


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.debug("MKFactionMod.setup");
        PacketHandler.setupHandler();
        TargetingHooks.registerHooks();
        FactionCommand.registerArgumentTypes();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        InputHandler.registerKeybinds();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event){
        FactionCommand.register(event.getDispatcher());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        PlayerFactionHandler.registerPersonaExtension();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FactionPage::registerPlayerPage);
    }
}
