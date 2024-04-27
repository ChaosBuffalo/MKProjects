package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.Lazy;

import java.util.function.Supplier;

public class DialogueObject {
    public static final String EMPTY_MSG = "";
    private String id;
    private String rawMessage;
    private DialogueTree dialogueTree;
    private Supplier<Component> compiledMessage;

    public DialogueObject(String id, String rawMessage) {
        this.id = id;
        setRawMessage(rawMessage);
    }

    public String getId() {
        return id;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String newRaw) {
        rawMessage = newRaw;
        buildMessageSupplier();
    }

    public void setDialogueTree(DialogueTree dialogueTree) {
        this.dialogueTree = dialogueTree;
    }

    public DialogueTree getDialogueTree() {
        return dialogueTree;
    }

    public Component getMessage() {
        return compiledMessage.get();
    }

    private void buildMessageSupplier() {
        compiledMessage = Lazy.of(() -> {
            if (getDialogueTree() == null) {
                throw new DialogueElementMissingException(
                        "Dialogue object '%s' was attempted to be compiled without a tree! Raw Message '%s'",
                        getId(), getRawMessage());
            }
            return DialogueManager.parseDialogueMessage(getRawMessage(), getDialogueTree());
        });
    }
}
