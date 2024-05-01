package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DialogueOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "dialogue");
    public static final Codec<DialogueOption> CODEC = ResourceLocation.CODEC.xmap(DialogueOption::new, DialogueOption::getValue);

    private final ResourceLocation dialogueId;

    public DialogueOption(ResourceLocation dialogueId) {
        super(NAME, ApplyOrder.MIDDLE);
        this.dialogueId = dialogueId;
    }

    public ResourceLocation getValue() {
        return dialogueId;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        entity.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY).ifPresent(cap -> cap.setDialogueTree(dialogueId));
    }
}
