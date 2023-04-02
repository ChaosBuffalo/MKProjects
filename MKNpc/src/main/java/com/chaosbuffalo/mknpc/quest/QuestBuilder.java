package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialogueResponse;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.quest.dialogue.NpcDialogueUtils;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.ObjectivesCompleteCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.objectives.*;
import com.chaosbuffalo.mknpc.quest.rewards.QuestReward;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class QuestBuilder {
    private Quest quest;

    public QuestBuilder(String questName, MutableComponent description){
        this.quest = new Quest(questName, description);
    }

    public QuestBuilder autoComplete(boolean value){
        quest.setAutoComplete(value);
        return this;
    }

    public QuestBuilder objective(QuestObjective<?> objective){
        quest.addObjective(objective);
        return this;
    }

    public QuestBuilder reward(QuestReward reward){
        quest.addReward(reward);
        return this;
    }

    public QuestBuilder killNotable(String objectiveName, QuestNpc npc){
        KillNotableNpcObjective kill = new KillNotableNpcObjective(objectiveName, npc.location.structureName,
                npc.location.structureIndex, npc.npcDef);
        objective(kill);
        return this;
    }

    public QuestBuilder killNpc(String objectiveName, ResourceLocation npcDef, int count){
        KillNpcDefObjective kill = new KillNpcDefObjective(objectiveName, npcDef, count);
        objective(kill);
        return this;
    }

    public QuestBuilder killWithAbility(String objectiveName, MKAbility ability, int count){
        KillWithAbilityObjective kill = new KillWithAbilityObjective(objectiveName, ability, count);
        objective(kill);
        return this;
    }

    public QuestBuilder questLootFromNotable(String objectiveName, QuestNpc npc, double chance, int count, MutableComponent itemDescription){
        QuestLootNotableObjective obj = new QuestLootNotableObjective(objectiveName, npc.location.structureName,
                npc.location.structureIndex, npc.npcDef, chance, count, itemDescription);
        objective(obj);
        return this;
    }

    public QuestBuilder hailWithObjectives(String objectiveName, MutableComponent description,
                                           QuestNpc talkTo, String withComplete,
                                           String withoutComplete, List<String> objectives,
                                           @Nullable Consumer<TalkToNpcObjective> additionalLogic){
        TalkToNpcObjective talkObj = new TalkToNpcObjective(objectiveName,
                talkTo.location.structureName, talkTo.location.structureIndex, talkTo.npcDef, description);
        DialogueNode completeNode = new DialogueNode(String.format("%s_complete", objectiveName), withComplete);
        DialogueResponse completeResponse = new DialogueResponse(completeNode.getId());
        completeResponse.addCondition(new ObjectivesCompleteCondition(quest.getQuestName(), objectives.toArray(new String[0])));
        completeNode.addEffect(new ObjectiveCompleteEffect(talkObj.getObjectiveName(), quest.getQuestName()));
        DialogueNode withoutCompleteNode = new DialogueNode(String.format("%s_wo_complete", objectiveName), withoutComplete);
        DialogueResponse withoutResponse = new DialogueResponse(withoutCompleteNode.getId());
        talkObj.withHailResponse(completeNode, completeResponse);
        talkObj.withHailResponse(withoutCompleteNode, withoutResponse);
        if (additionalLogic != null){
            additionalLogic.accept(talkObj);
        }
        objective(talkObj);
        return this;
    }

    public QuestBuilder hailWithCondition(String objectiveName, MutableComponent description,
                                          QuestNpc talkTo, String withCondition, String withoutCondition,
                                          DialogueCondition withCond,
                                          @Nullable Consumer<TalkToNpcObjective> additionalLogic){
        TalkToNpcObjective talkObj = new TalkToNpcObjective(objectiveName,
                talkTo.location.structureName, talkTo.location.structureIndex, talkTo.npcDef, description);
        DialogueNode conditionNode = new DialogueNode(String.format("%s_w_cond", objectiveName), withCondition);
        DialogueResponse conditionResponse = new DialogueResponse(conditionNode.getId());
        conditionResponse.addCondition(withCond);
        conditionNode.addEffect(new ObjectiveCompleteEffect(talkObj.getObjectiveName(), quest.getQuestName()));
        DialogueNode withoutConditionNode = new DialogueNode(String.format("%s_wo_cond", objectiveName), withoutCondition);
        DialogueResponse withoutConditionResponse = new DialogueResponse(withoutConditionNode.getId());
        talkObj.withHailResponse(conditionNode, conditionResponse);
        talkObj.withHailResponse(withoutConditionNode, withoutConditionResponse);
        if (additionalLogic != null){
            additionalLogic.accept(talkObj);
        }
        objective(talkObj);
        return this;
    }

    public QuestBuilder simpleHail(String objectiveName, MutableComponent description,
                                   QuestNpc talkTo, String hailMessage,
                                   boolean immediateComplete, @Nullable Consumer<TalkToNpcObjective> additionalLogic){
        TalkToNpcObjective talkObj = new TalkToNpcObjective(
                objectiveName,
                talkTo.location.structureName, talkTo.location.structureIndex, talkTo.npcDef,
                description);
        DialogueNode hailNode = new DialogueNode(String.format("%s_hail", objectiveName), hailMessage);
        if (immediateComplete){
            hailNode.addEffect(new ObjectiveCompleteEffect(talkObj.getObjectiveName(), quest.getQuestName()));
        }
        talkObj.withHailResponse(hailNode, new DialogueResponse(hailNode.getId()));
        if (additionalLogic != null){
            additionalLogic.accept(talkObj);
        }
        objective(talkObj);
        return this;
    }

    public QuestBuilder lootChest(String objectiveName, MutableComponent description, QuestLocation location,
                                  String chestTag, ItemStack... items){
        LootChestObjective chestObj = new LootChestObjective(objectiveName, location.structureName,
                location.structureIndex, chestTag, description);
        for (ItemStack item : items){
            chestObj.addItemStack(item);
        }
        objective(chestObj);
        return this;
    }

    public Quest quest(){
        return quest;
    }

    public static class QuestLocation {
        ResourceLocation structureName;
        int structureIndex;

        public QuestLocation(ResourceLocation structureName, int structureIndex){
            this.structureIndex = structureIndex;
            this.structureName = structureName;
        }
    }

    public static class QuestNpc {
        QuestLocation location;
        ResourceLocation npcDef;

        public QuestNpc(QuestLocation location, ResourceLocation npcDef){
            this.location = location;
            this.npcDef = npcDef;
        }

        public String getDialogueLink(){
            return NpcDialogueUtils.getNotableNpcRaw(location.structureName, location.structureIndex, npcDef);
        }
    }
}
