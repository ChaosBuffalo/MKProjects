package com.chaosbuffalo.mkworkspace;

import com.chaosbuffalo.mkworkspace.init.MKWorkspaceBlockEntityTypes;
import com.chaosbuffalo.mkworkspace.init.MKWorkspaceBlocks;
import com.chaosbuffalo.mkworkspace.command.MKWorkspaceCommands;
import com.chaosbuffalo.mkworkspace.init.MKWorkspaceAttachments;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKWorkspace.MODID)
public class MKWorkspace {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkworkspace";

    public MKWorkspace(IEventBus modEventBus, ModContainer modContainer) {
        MKWorkspaceAttachments.register(modEventBus);
        MKWorkspaceBlocks.register(modEventBus);
        MKWorkspaceBlockEntityTypes.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(MKWorkspaceCommands.register());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
