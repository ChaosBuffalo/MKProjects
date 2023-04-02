package com.chaosbuffalo.mkchat;

import com.chaosbuffalo.mkchat.command.ChatCommands;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKChat.MODID)
public class MKChat {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mkchat";
    public static final String REGISTER_DIALOGUE_EXTENSION = "register_dialogue_extension";
    private DialogueManager dialogueManager;

    public MKChat() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        ChatEntityTypes.ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        dialogueManager = new DialogueManager();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("In MKChat command registration");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ChatCommands.register(event.getDispatcher());
    }

    private void setup(final FMLCommonSetupEvent event) {
        DialogueManager.dialogueSetup();
    }


    private void processIMC(final InterModProcessEvent event) {
        MKChat.LOGGER.info("MKChat.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_DIALOGUE_EXTENSION)) {
                MKChat.LOGGER.info("IMC register dialogue extension from mod {} {}", m.senderModId(),
                        m.method());
                IDialogueExtension ext = (IDialogueExtension) m.messageSupplier().get();
                ext.registerDialogueExtension();
            }
        });
    }
}
