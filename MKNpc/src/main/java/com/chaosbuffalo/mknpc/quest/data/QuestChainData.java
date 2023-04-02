package com.chaosbuffalo.mknpc.quest.data;

import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjective;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestChainData implements IQuestInstanceData{

    private Map<String, QuestData> questData;


    public QuestChainData(QuestDefinition definition, Map<ResourceLocation, List<MKStructureEntry>> questStructures){
        questData = new HashMap<>();
        for (Quest quest : definition.getQuestChain()){
            QuestData qData = new QuestData(quest.getQuestName());
            for (QuestObjective<?> objective : quest.getObjectives()){
                objective.createDataForQuest(qData, questStructures);
            }
            questData.put(quest.getQuestName(), qData);
        }

    }

    public void generateDialogue(QuestChainInstance questChain,
                                 ResourceLocation dialogueName,
                                 QuestDefinition definition,
                                 Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                 Map<ResourceLocation, UUID> speakingRoles,
                                 Map<UUID, DialogueTree> npcTrees){

        for (Map.Entry<ResourceLocation, UUID> entry : speakingRoles.entrySet()){
            DialogueTree tree = new DialogueTree(dialogueName);
            DialoguePrompt hailPrompt = new DialoguePrompt("hail");
            tree.addPrompt(hailPrompt);
            tree.setHailPrompt(hailPrompt);

            for (Quest quest : definition.getQuestChain()){
                tree = quest.generateDialogueForNpc(questChain, entry.getKey(), entry.getValue(), tree, questStructures, definition);
            }
            npcTrees.put(entry.getValue(), tree);
        }
    }

    public QuestData getQuestData(String questName){
        return questData.get(questName);
    }

    public QuestChainData(QuestDefinition definition, CompoundTag nbt){
        questData = new HashMap<>();
        deserializeNBT(nbt, definition);
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag questNbt = new CompoundTag();
        for (Map.Entry<String, QuestData> entry : questData.entrySet()){
            questNbt.put(entry.getKey(), entry.getValue().serializeNBT());
        }
        nbt.put("questData", questNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, QuestDefinition definition) {
        CompoundTag questNbt = nbt.getCompound("questData");
        for (String key : questNbt.getAllKeys()){
            Quest source = definition.getQuest(key);
            if (source != null){
                QuestData data = new QuestData(source.getQuestName());
                data.deserializeNBT(questNbt.getCompound(key), source);
                questData.put(source.getQuestName(), data);
            }
        }
    }
}
