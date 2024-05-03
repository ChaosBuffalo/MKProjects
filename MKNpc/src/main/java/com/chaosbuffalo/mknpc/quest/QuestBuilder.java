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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class QuestBuilder {
    private Quest quest;

    public QuestBuilder(String questName, Component description) {
        this.quest = new Quest(questName, description);
    }

    public QuestBuilder autoComplete(boolean value) {
        quest.setAutoComplete(value);
        return this;
    }

    public QuestBuilder objective(QuestObjective<?> objective) {
        quest.addObjective(objective);
        return this;
    }

    public QuestBuilder reward(QuestReward reward) {
        quest.addReward(reward);
        return this;
    }

    public QuestBuilder killNotable(String objectiveName, QuestNpc npc) {
        KillNotableNpcObjective kill = new KillNotableNpcObjective(objectiveName, npc.location, npc.npcDef);
        objective(kill);
        return this;
    }

    public QuestBuilder killNpc(String objectiveName, ResourceLocation npcDef, int count) {
        KillNpcDefObjective kill = new KillNpcDefObjective(objectiveName, npcDef, count);
        objective(kill);
        return this;
    }

    public QuestBuilder killWithAbility(String objectiveName, MKAbility ability, int count) {
        KillWithAbilityObjective kill = new KillWithAbilityObjective(objectiveName, ability, count);
        objective(kill);
        return this;
    }

    public QuestBuilder questLootFromNotable(String objectiveName, QuestNpc npc, double chance, int count, Component itemDescription) {
        QuestLootNotableObjective obj = new QuestLootNotableObjective(objectiveName, npc.location, npc.npcDef, chance, count, itemDescription);
        objective(obj);
        return this;
    }

    public QuestBuilder hailWithObjectives(String objectiveName, Component description,
                                           QuestNpc talkTo, String withComplete,
                                           String withoutComplete, List<String> objectives,
                                           @Nullable Consumer<TalkToNpcObjective> additionalLogic) {
        TalkToNpcObjective talkObj = new TalkToNpcObjective(objectiveName,
                talkTo.location, talkTo.npcDef, description);
        DialogueNode completeNode = new DialogueNode(String.format("%s_complete", objectiveName), withComplete);
        DialogueResponse completeResponse = new DialogueResponse(completeNode.getId());
        completeResponse.addCondition(new ObjectivesCompleteCondition(quest.getQuestName(), objectives));
        completeNode.addEffect(new ObjectiveCompleteEffect(talkObj.getObjectiveName(), quest.getQuestName()));
        DialogueNode withoutCompleteNode = new DialogueNode(String.format("%s_wo_complete", objectiveName), withoutComplete);
        DialogueResponse withoutResponse = new DialogueResponse(withoutCompleteNode.getId());
        talkObj.withHailResponse(completeNode, completeResponse);
        talkObj.withHailResponse(withoutCompleteNode, withoutResponse);
        if (additionalLogic != null) {
            additionalLogic.accept(talkObj);
        }
        objective(talkObj);
        return this;
    }

    public QuestBuilder hailWithCondition(String objectiveName, Component description,
                                          QuestNpc talkTo, String withCondition, String withoutCondition,
                                          DialogueCondition withCond,
                                          @Nullable Consumer<TalkToNpcObjective> additionalLogic) {
        TalkToNpcObjective talkObj = new TalkToNpcObjective(objectiveName,
                talkTo.location, talkTo.npcDef, description);
        DialogueNode conditionNode = new DialogueNode(String.format("%s_w_cond", objectiveName), withCondition);
        DialogueResponse conditionResponse = new DialogueResponse(conditionNode.getId());
        conditionResponse.addCondition(withCond);
        conditionNode.addEffect(new ObjectiveCompleteEffect(talkObj.getObjectiveName(), quest.getQuestName()));
        DialogueNode withoutConditionNode = new DialogueNode(String.format("%s_wo_cond", objectiveName), withoutCondition);
        DialogueResponse withoutConditionResponse = new DialogueResponse(withoutConditionNode.getId());
        talkObj.withHailResponse(conditionNode, conditionResponse);
        talkObj.withHailResponse(withoutConditionNode, withoutConditionResponse);
        if (additionalLogic != null) {
            additionalLogic.accept(talkObj);
        }
        objective(talkObj);
        return this;
    }

    public QuestBuilder simpleHail(String objectiveName, Component description,
                                   QuestNpc talkTo, String hailMessage,
                                   boolean immediateComplete, @Nullable Consumer<TalkToNpcObjective> additionalLogic) {
        TalkToNpcObjective talkObj = new TalkToNpcObjective(objectiveName, talkTo.location, talkTo.npcDef, description);
        DialogueNode hailNode = new DialogueNode(String.format("%s_hail", objectiveName), hailMessage);
        if (immediateComplete) {
            hailNode.addEffect(new ObjectiveCompleteEffect(talkObj.getObjectiveName(), quest.getQuestName()));
        }
        talkObj.withHailResponse(hailNode, new DialogueResponse(hailNode.getId()));
        if (additionalLogic != null) {
            additionalLogic.accept(talkObj);
        }
        objective(talkObj);
        return this;
    }

    public QuestBuilder lootChest(String objectiveName, Component description, QuestStructureLocation location,
                                  String chestTag, ItemStack... items) {
        LootChestObjective chestObj = new LootChestObjective(objectiveName, location, chestTag, Arrays.asList(items), List.of(description));
        objective(chestObj);
        return this;
    }

    public Quest quest() {
        return quest;
    }

    public static class QuestNpc {
        public final QuestStructureLocation location;
        public final ResourceLocation npcDef;

        public QuestNpc(QuestStructureLocation location, ResourceLocation npcDef) {
            this.location = location;
            this.npcDef = npcDef;
        }

        public String getDialogueLink() {
            return NpcDialogueUtils.getNotableNpcRaw(location.getStructureId(), location.getIndex(), npcDef);
        }
    }
}
