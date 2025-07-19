package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DialogueOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("dialogue");
    public static final MapCodec<DialogueOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            DialogueTree.KEY_CODEC.fieldOf("dialogueId").forGetter(i -> i.dialogueId)
    ).apply(builder, DialogueOption::new));

    private final ResourceKey<DialogueTree> dialogueId;

    public DialogueOption(ResourceKey<DialogueTree> dialogueId) {
        super(NAME, ApplyOrder.MIDDLE);
        this.dialogueId = dialogueId;
    }

    public ResourceKey<DialogueTree> getValue() {
        return dialogueId;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        INpcDialogue.get(entity).ifPresent(cap -> cap.setDialogueTree(dialogueId));
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.DIALOGUE.get();
    }
}
