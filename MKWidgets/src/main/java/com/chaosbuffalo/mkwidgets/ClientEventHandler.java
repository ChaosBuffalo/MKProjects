package com.chaosbuffalo.mkwidgets;

import com.chaosbuffalo.mkwidgets.client.gui.example.TestScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


/**
 * Client-only event hooks used by the standalone MKWidgets module.
 * <p>
 * The handler currently exists to expose the demo {@link TestScreen} through a key binding so the library can
 * be explored in a running client.
 */
@EventBusSubscriber(modid = MKWidgets.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    /**
     * Key binding that opens the built-in test UI.
     */
    public static final KeyMapping openTestUi = new KeyMapping("key.mkwidgets.test.desc",
            InputConstants.KEY_APOSTROPHE,
            "key.mkwidgets.category");

    /**
     * Opens the test screen when the configured key binding is consumed.
     *
     * @param event the NeoForge key input event
     */
    @SubscribeEvent
    public static void onEvent(InputEvent.Key event) {
        if (openTestUi.consumeClick()) {
            Minecraft.getInstance().setScreen(new TestScreen(
                    Component.literal("MK Widgets Test")));
        }
    }

    /**
     * Nested mod event subscriber responsible for registering client key mappings.
     */
    @EventBusSubscriber(modid = MKWidgets.MODID, value = Dist.CLIENT)
    public static class ModEvents {
        /**
         * Registers the demo screen key binding.
         *
         * @param event the key mapping registration event
         */
        @SubscribeEvent
        public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
            event.register(openTestUi);
        }
    }
}
