package com.chaosbuffalo.mkworkspace;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKWorkspace.MODID)
public class MKWorkspace {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkworkspace";

    public MKWorkspace(IEventBus modEventBus, ModContainer modContainer) {
    }
}
