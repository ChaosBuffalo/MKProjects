package com.chaosbuffalo.targeting_api;


import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;


/**
 * Provides an extensible targeting utility API for defining how entities and players can interact with
 * each other. All relationships are abstracted into one of: Friendly, Enemy, Neutral, or Unhandled.
 * <p>
 * The library centers on relationship resolution between entities and reusable
 * {@link TargetingContext targeting contexts} that describe valid targets such
 * as self, friendly, enemy, neutral, players, or custom filtered groups.
 * <p>
 *
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

    }
}
