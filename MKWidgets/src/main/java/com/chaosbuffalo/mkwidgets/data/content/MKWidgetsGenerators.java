package com.chaosbuffalo.mkwidgets.data.content;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data generation registration for the standalone MKWidgets module.
 */
@EventBusSubscriber
public class MKWidgetsGenerators {

    /**
     * Adds data providers used by this module during NeoForge data generation.
     *
     * @param event gather data event
     */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.addProvider(new MKWidgetsLanguageProvider(event.getGenerator().getPackOutput(), "en_us"));
    }
}
