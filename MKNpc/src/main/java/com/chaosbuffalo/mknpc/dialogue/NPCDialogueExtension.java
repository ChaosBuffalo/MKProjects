package com.chaosbuffalo.mknpc.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.IDialogueExtension;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.InterModComms;

import java.util.UUID;


public class NPCDialogueExtension implements IDialogueExtension {

    public static void sendExtension() {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(MKChat.MODID, MKChat.REGISTER_DIALOGUE_EXTENSION, NPCDialogueExtension::new);
    }

    private static Component notable(String name, DialogueTree tree) {
        NotableNpcEntry entry = ContentDB.getPrimaryData().getNotableNpc(UUID.fromString(name));
        if (entry != null) {
            return entry.getName();
        } else {
            return null;
        }
    }

    @Override
    public void registerDialogueExtension() {
        MKNpc.LOGGER.info("Registering MKNpc Dialogue Extension");

        ChatRegistries.putTextComponentProvider("notable", NPCDialogueExtension::notable);
    }
}
