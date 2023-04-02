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
import com.chaosbuffalo.mknpc.quest.objectives.StructureInstanceObjective;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.chaosbuffalo.mknpc.quest.rewards.QuestReward;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Quest {

    private boolean autoComplete;
    private final List<QuestObjective<?>> objectives;
    private final Map<String, QuestObjective<?>> objectiveIndex;
    private final List<QuestReward> rewards;
    private final List<QuestRequirement> requirements;
    private String questName;
    private MutableComponent description;
    public static final MutableComponent defaultDescription = new TextComponent("Placeholder Quest Description");

    public Quest(String questName, MutableComponent description){
        this.questName = questName;
        this.objectives = new ArrayList<>();
        this.objectiveIndex = new HashMap<>();
        this.rewards = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.description = description;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    public boolean shouldAutoComplete(){
        return autoComplete;
    }

    public Quest(){
        this("default", defaultDescription);
    }

    public String getQuestName() {
        return questName;
    }

    public DialogueTree generateDialogueForNpc(QuestChainInstance questChain, ResourceLocation npcDefinitionName,
                                       UUID npcId, DialogueTree tree,
                                       Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                       QuestDefinition definition){
        QuestData questData = questChain.getQuestChainData().getQuestData(getQuestName());
        for (QuestObjective<?> obj : getObjectives()) {
            if (obj instanceof TalkToNpcObjective) {
                TalkToNpcObjective talkObj = (TalkToNpcObjective) obj;
                UUIDInstanceData instanceData = talkObj.getInstanceData(questData);
                if (instanceData.getUuid().equals(npcId)){
                    tree = talkObj.generateDialogueForNpc(this, questChain, npcDefinitionName, npcId, tree, questStructures, definition);
                }
            }
        }
        return tree;
    }

    public void addObjective(QuestObjective<?> objective){
        if (objectiveIndex.containsKey(objective.getObjectiveName())){
            MKNpc.LOGGER.error("Failed to add objective {} to quest {}", objective.getObjectiveName(), getQuestName());
        } else {
            objectives.add(objective);
            objectiveIndex.put(objective.getObjectiveName(), objective);
        }
    }

    public void addReward(QuestReward reward){
        rewards.add(reward);
    }

    public QuestObjective<?> getObjective(String name){
        return objectiveIndex.get(name);
    }

    public MutableComponent getDescription() {
        return description;
    }

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("questName"), ops.createString(questName));
        builder.put(ops.createString("objectives"), ops.createList(objectives.stream().map(x -> x.serialize(ops))));
        builder.put(ops.createString("description"), ops.createString(Component.Serializer.toJson(description)));
        builder.put(ops.createString("autoComplete"), ops.createBoolean(autoComplete));
        builder.put(ops.createString("rewards"), ops.createList(rewards.stream().map(x -> x.serialize(ops))));
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        questName = dynamic.get("questName").asString("default");
        autoComplete = dynamic.get("autoComplete").asBoolean(false);
        description = Component.Serializer.fromJson(
                dynamic.get("description").asString(Component.Serializer.toJson(defaultDescription)));
        List<Optional<QuestObjective<?>>> objectives = dynamic.get("objectives").asList(x -> {
            ResourceLocation type = QuestObjective.getType(x);
            Supplier<QuestObjective<?>> sup = QuestDefinitionManager.getObjectiveDeserializer(type);
            if (sup != null){
                QuestObjective<?> obj = sup.get();
                obj.deserialize(x);
                return Optional.of(obj);
            }
            return Optional.empty();
        });
        for (Optional<QuestObjective<?>> objOpt : objectives){
            objOpt.ifPresent(this::addObjective);
        }

        List<Optional<QuestReward>> rewards = dynamic.get("rewards").asList(x -> {
            ResourceLocation type = QuestReward.getType(x);
            Supplier<QuestReward> sup = QuestDefinitionManager.getRewardDeserializer(type);
            if (sup != null){
                QuestReward reward = sup.get();
                reward.deserialize(x);
                return Optional.of(reward);
            }
            return Optional.empty();
        });
        for (Optional<QuestReward> rewardOpt : rewards){
            rewardOpt.ifPresent(this::addReward);
        }
    }

    public List<QuestObjective<?>> getObjectives() {
        return objectives;
    }

    public List<Pair<ResourceLocation, Integer>> getStructuresNeeded(){
        return objectives.stream().filter(x -> x instanceof StructureInstanceObjective)
                .map(x -> (StructureInstanceObjective<?>) x)
                .map(x -> new Pair<>(x.getStructureName(), x.getStructureIndex() + 1))
                .collect(Collectors.toList());
    }


    public boolean isStructureRelevant(MKStructureEntry entry){
        return objectives.stream().allMatch(x -> x.isStructureRelevant(entry));
    }

    public PlayerQuestData generatePlayerQuestData(IWorldNpcData worldData, QuestData instanceData){
        PlayerQuestData data = new PlayerQuestData(getQuestName(), getDescription());
        objectives.forEach(x -> {
            PlayerQuestObjectiveData obj = x.generatePlayerData(worldData, instanceData);
            obj.putBool("isComplete", false);
            data.putObjective(x.getObjectiveName(), obj);
        });
        rewards.forEach(x -> {
            PlayerQuestReward questReward = new PlayerQuestReward(x);
            data.addReward(questReward);
        });
        return data;
    }

    public boolean isComplete(PlayerQuestData data){
        return objectives.stream().allMatch(x -> x.isComplete(data.getObjective(x.getObjectiveName())));
    }

    public void grantRewards(IPlayerQuestingData playerData){
        for (QuestReward reward : rewards){
            reward.grantReward(playerData.getPlayer());
        }

    }
}
