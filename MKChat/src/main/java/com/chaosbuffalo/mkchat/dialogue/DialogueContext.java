package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

public class DialogueContext {
    private final LivingEntity speaker;
    private final ServerPlayer player;
    private final DialogueObject dialogueObject;

    public DialogueContext(LivingEntity speaker, ServerPlayer player, DialogueObject dialogueObject) {
        this.speaker = speaker;
        this.player = player;
        this.dialogueObject = dialogueObject;
    }

    public DialogueObject getDialogueObject() {
        return dialogueObject;
    }

    public LivingEntity getSpeaker() {
        return speaker;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
