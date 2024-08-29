package com.chaosbuffalo.mkfaction;

import com.chaosbuffalo.mkfaction.capabilities.PlayerFactionHandler;
import com.chaosbuffalo.mkfaction.client.gui.FactionPage;
import com.chaosbuffalo.mkfaction.command.FactionCommand;
import com.chaosbuffalo.mkfaction.faction.FactionDefaultManager;
import com.chaosbuffalo.mkfaction.faction.FactionManager;
import com.chaosbuffalo.mkfaction.init.FactionAttachments;
import com.chaosbuffalo.mkfaction.init.FactionCommands;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mkfaction.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKFactionMod.MODID)
public class MKFactionMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkfaction";
    private final FactionManager factionManager;
    private final FactionDefaultManager factionDefaultManager;

    public MKFactionMod(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::enqueueIMC);
        modBus.addListener(PacketHandler::register);

        NeoForge.EVENT_BUS.register(this);
        MKFactions.register(modBus);

        factionManager = new FactionManager();
        factionDefaultManager = new FactionDefaultManager();
        FactionAttachments.register(modBus);
        FactionCommands.register(modBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.debug("MKFactionMod.setup");
        event.enqueueWork(TargetingHooks::registerHooks);
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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
