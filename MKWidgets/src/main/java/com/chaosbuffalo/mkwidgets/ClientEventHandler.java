package com.chaosbuffalo.mkwidgets;

import com.chaosbuffalo.mkwidgets.client.gui.example.TestScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MKWidgets.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    public static final KeyMapping openTestUi = new KeyMapping("key.mkwidgets.test.desc",
            GLFW.GLFW_KEY_APOSTROPHE,
            "key.mkwidgets.category");

    @SubscribeEvent
    public static void onEvent(InputEvent.KeyInputEvent event){
        if (openTestUi.consumeClick()){
            Minecraft.getInstance().setScreen(new TestScreen(
                    new TextComponent("MK Widgets Test")));
        }
    }

    protected static void clientSetup(){
        ClientRegistry.registerKeyBinding(openTestUi);
    }
}
