package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class DialogueOption extends ResourceLocationOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "dialogue");

    public DialogueOption() {
        super(NAME);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, ResourceLocation value) {
        entity.getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY).ifPresent(cap -> cap.setDialogueTree(value));
    }
}
