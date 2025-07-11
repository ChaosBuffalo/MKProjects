package com.chaosbuffalo.mkiafcompat;

import com.chaosbuffalo.mkiafcompat.trace.IAFTraceHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKIAFCompat.MODID)
public class MKIAFCompat {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkiafcompat";

    public MKIAFCompat(IEventBus modBus) {
        modBus.addListener(this::enqueueIMC);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        IAFTraceHandler.registerTraceHandler();
    }


}