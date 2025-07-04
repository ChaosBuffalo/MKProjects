package com.chaosbuffalo.mkwidgets.data.content;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class MKWidgetsGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.addProvider(new MKWidgetsLanguageProvider(event.getGenerator().getPackOutput(), "en_us"));
    }
}
