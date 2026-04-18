package com.chaosbuffalo.targeting_api;


import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;


/**
 * NeoForge bootstrap class for the Targeting API mod.
 * <p>
 * This class exists to register the module with the mod loader and is not part
 * of the main consumer-facing targeting API.
 */
@Mod(TargetingAPI.MODID)
public class TargetingAPI {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "targeting_api";

    /**
     * Creates the mod entry point.
     *
     * @param modEventBus the module event bus provided by NeoForge
     * @param modContainer the owning mod container
     */
    public TargetingAPI(IEventBus modEventBus, ModContainer modContainer) {
        // do a line change in source to test ci
    }
}
