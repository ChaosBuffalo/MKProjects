package com.chaosbuffalo.mkchat;

import com.chaosbuffalo.mkchat.command.ChatCommands;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mkchat.init.ChatAttachments;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MKChat.MODID)
public class MKChat {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean DEV_LOGGING = false;
    public static final String MODID = "mkchat";
    public static final String REGISTER_DIALOGUE_EXTENSION = "register_dialogue_extension";
    private final DialogueManager dialogueManager;


    public MKChat(IEventBus modBus) {
        modBus.addListener(this::setup);
        modBus.addListener(this::processIMC);
        ChatEntityTypes.ENTITY_TYPES.register(modBus);
        ChatRegistries.register(modBus);
        ChatAttachments.register(modBus);

        NeoForge.EVENT_BUS.register(this);
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
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_DIALOGUE_EXTENSION)) {
                MKChat.LOGGER.debug("IMC register dialogue extension from mod {} {}", m.senderModId(),
                        m.method());
                IDialogueExtension ext = (IDialogueExtension) m.messageSupplier().get();
                ext.registerDialogueExtension();
            }
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
