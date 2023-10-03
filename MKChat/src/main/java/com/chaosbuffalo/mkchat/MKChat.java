package com.chaosbuffalo.mkchat;

import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.command.ChatCommands;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
    private final DialogueManager dialogueManager;

    public MKChat() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::processIMC);
        modBus.addListener(ChatCapabilities::registerCapabilities);
        ChatEntityTypes.ENTITY_TYPES.register(modBus);

        MinecraftForge.EVENT_BUS.register(this);
        dialogueManager = new DialogueManager();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ChatCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(dialogueManager);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ChatRegistries.setup();
    }

    private void processIMC(final InterModProcessEvent event) {
        MKChat.LOGGER.debug("MKChat.processIMC");
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_DIALOGUE_EXTENSION)) {
                MKChat.LOGGER.debug("IMC register dialogue extension from mod {} {}", m.senderModId(),
                        m.method());
                IDialogueExtension ext = (IDialogueExtension) m.messageSupplier().get();
                ext.registerDialogueExtension();
            }
        });
    }
}
