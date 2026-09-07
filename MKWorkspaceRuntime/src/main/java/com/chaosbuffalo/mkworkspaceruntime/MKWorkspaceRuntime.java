package com.chaosbuffalo.mkworkspaceruntime;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKWorkspaceRuntime.MODID)
public class MKWorkspaceRuntime {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkworkspaceruntime";

    public MKWorkspaceRuntime(IEventBus modEventBus, ModContainer modContainer) {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
