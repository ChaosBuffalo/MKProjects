package com.chaosbuffalo.mknpc.npc.entries;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueResponse;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestDefinitionManager;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.CanStartQuestCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.IReceivesChainId;
import com.chaosbuffalo.mknpc.quest.generation.QuestChainBuildResult;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class QuestOfferingEntry implements INBTSerializable<CompoundTag> {
    private ResourceLocation questDef;
    @Nullable
    private UUID questId;
    @Nullable
    private DialogueTree tree;

    public QuestOfferingEntry(ResourceLocation questDef) {
        this.questDef = questDef;
        this.questId = null;
    }

    @Nullable
    public DialogueTree getTree() {
        return tree;
    }

    public ResourceLocation getQuestDef() {
        return questDef;
    }

    public QuestOfferingEntry(CompoundTag nbt) {
        deserializeNBT(nbt);
    }

    @Nullable
    public UUID getQuestId() {
        return questId;
    }

    private ResourceLocation makeTreeId(UUID questId) {
        return new ResourceLocation(MKNpc.MODID, String.format("give_quest.%s", questId));
    }

    private DialogueTree specializeTree(QuestDefinition definition, QuestChainBuildResult buildResult) {
        UUID chainId = buildResult.instance.getQuestId();
        DialogueTree specializedTree = definition.getStartQuestTree().copy(makeTreeId(chainId));
        for (DialogueNode node : specializedTree.getNodes().values()) {
            for (DialogueEffect effect : node.getEffects()) {
                if (effect instanceof IReceivesChainId advEffect) {
                    advEffect.setChainId(chainId);
                }
            }
            TalkToNpcObjective.handleQuestRawMessageManipulation(node, buildResult.questStructures, buildResult.instance);
        }
        for (DialoguePrompt prompt : specializedTree.getPrompts().values()) {
            for (DialogueResponse resp : prompt.getResponses()) {
                for (DialogueCondition condition : resp.getConditions()) {
                    if (condition instanceof IReceivesChainId chainedCondition) {
                        chainedCondition.setChainId(chainId);
                    }
                }
            }
        }
        return specializedTree;
    }

    public void setupDialogue(QuestChainBuildResult buildResult) {
        QuestDefinition definition = QuestDefinitionManager.getDefinition(questDef);
        UUID questId = buildResult.instance.getQuestId();

        DialogueTree startTree = specializeTree(definition, buildResult);
        DialoguePrompt hailPrompt = startTree.getHailPrompt();
        if (hailPrompt != null) {
            hailPrompt.getResponses().stream().filter(x -> x.getConditions().stream()
                            .anyMatch(cond -> cond instanceof CanStartQuestCondition))
                    .forEach(resp -> {
                        for (QuestRequirement req : definition.getRequirements()) {
                            resp.addCondition(req.getDialogueCondition());
                        }
                    });
        }


        MKNpc.LOGGER.debug("Generated Start Quest Dialogue for {} id {}", questDef, questId);
        this.tree = startTree;
    }

    public void setQuestId(@Nullable UUID questId) {
        this.questId = questId;
        if (questId == null) {
            MKNpc.LOGGER.debug("Set quest id called in quest generation with null id {}", questDef);
            return;
        }


    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("questDef", questDef.toString());
        if (questId != null) {
            nbt.putUUID("questId", questId);
        }
        if (tree != null) {
            nbt.put("dialogue", tree.serialize(NbtOps.INSTANCE));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        questDef = new ResourceLocation(nbt.getString("questDef"));
        if (nbt.contains("questId")) {
            questId = nbt.getUUID("questId");
        }
        if (nbt.contains("dialogue")) {
            tree = DialogueTree.deserialize(new Dynamic<>(NbtOps.INSTANCE, nbt.get("dialogue")));
        }
    }
}
