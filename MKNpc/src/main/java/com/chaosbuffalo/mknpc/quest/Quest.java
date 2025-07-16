package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestReward;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjective;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.chaosbuffalo.mknpc.quest.rewards.QuestReward;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class Quest {
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("questName").forGetter(i -> i.questName),
            QuestObjective.CODEC.listOf().fieldOf("objectives").forGetter(i -> i.objectives),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(i -> i.description),
            Codec.BOOL.fieldOf("autoComplete").forGetter(i -> i.autoComplete),
            QuestReward.CODEC.listOf().fieldOf("rewards").forGetter(i -> i.rewards)
    ).apply(builder, Quest::new));

    private boolean autoComplete;
    private final List<QuestObjective<?>> objectives;
    private final Map<String, QuestObjective<?>> objectiveIndex;
    private final List<QuestReward> rewards;
    private final List<QuestRequirement> requirements;
    private final String questName;
    private final Component description;
    public static final Component defaultDescription = Component.literal("Placeholder Quest Description");

    private Quest(String questName, List<QuestObjective<?>> objectives, Component description, boolean autoComplete, List<QuestReward> rewards) {
        this.questName = questName;
        this.objectives = objectives;
        this.description = description;
        this.autoComplete = autoComplete;
        this.rewards = rewards;
        objectiveIndex = new HashMap<>(objectives.size());
        this.objectives.forEach(o -> objectiveIndex.put(o.getObjectiveName(), o));
        this.requirements = List.of();
    }

    public Quest(String questName, Component description) {
        this.questName = questName;
        this.description = description;
        this.objectives = new ArrayList<>();
        this.objectiveIndex = new HashMap<>();
        this.rewards = new ArrayList<>();
        this.requirements = new ArrayList<>();
    }

    public Quest() {
        this("default", defaultDescription);
    }

    public String getQuestName() {
        return questName;
    }

    public Component getDescription() {
        return description;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    public boolean shouldAutoComplete() {
        return autoComplete;
    }

    public DialogueTree generateDialogueForNpc(QuestChainInstance questChain, ResourceLocation npcDefinitionName,
                                               UUID npcId, DialogueTree tree,
                                               Map<QuestStructureLocation, MKStructureEntry> questStructures,
                                               QuestDefinition definition) {
        QuestData questData = questChain.getQuestData(this);
        for (QuestObjective<?> obj : getObjectives()) {
            if (obj instanceof TalkToNpcObjective talkObj) {
                UUIDInstanceData instanceData = talkObj.getInstanceData(questData);
                if (instanceData.getUUID().equals(npcId)) {
                    tree = talkObj.generateDialogueForNpc(this, questChain, npcDefinitionName, npcId, tree, questStructures, definition);
                }
            }
        }
        return tree;
    }

    public void addObjective(QuestObjective<?> objective) {
        if (objectiveIndex.containsKey(objective.getObjectiveName())) {
            MKNpc.LOGGER.error("Failed to add objective {} to quest {}", objective.getObjectiveName(), getQuestName());
        } else {
            objectives.add(objective);
            objectiveIndex.put(objective.getObjectiveName(), objective);
        }
    }

    public void addReward(QuestReward reward) {
        rewards.add(reward);
    }

    public QuestObjective<?> getObjective(String name) {
        return objectiveIndex.get(name);
    }

    public List<QuestObjective<?>> getObjectives() {
        return objectives;
    }

    public Set<QuestStructureLocation> getStructuresNeeded() {
        return objectives.stream().map(QuestObjective::getStructure).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }
    
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return objectives.stream().allMatch(x -> x.isStructureRelevant(entry));
    }

    public PlayerQuestData generatePlayerQuestData(IWorldNpcData worldData, QuestData instanceData) {
        PlayerQuestData data = new PlayerQuestData(getQuestName(), getDescription());
        objectives.forEach(x -> {
            PlayerQuestObjectiveData obj = x.generatePlayerData(worldData, instanceData);
            obj.setComplete(false);
            data.putObjective(x.getObjectiveName(), obj);
        });
        rewards.forEach(x -> {
            PlayerQuestReward questReward = new PlayerQuestReward(x);
            data.addReward(questReward);
        });
        return data;
    }

    public boolean isComplete(PlayerQuestData data) {
        return objectives.stream().allMatch(x -> x.isComplete(data.getObjective(x.getObjectiveName())));
    }

    public void grantRewards(IPlayerQuestingData playerData) {
        for (QuestReward reward : rewards) {
            reward.grantReward(playerData.getPlayer());
        }
    }
}
