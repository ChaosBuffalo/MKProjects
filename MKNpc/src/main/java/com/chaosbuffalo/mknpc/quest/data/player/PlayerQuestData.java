package com.chaosbuffalo.mknpc.quest.data.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class PlayerQuestData implements INBTSerializable<CompoundTag> {

    private final LinkedHashMap<String, PlayerQuestObjectiveData> objectives = new LinkedHashMap<>();
    private String questName;
    private MutableComponent description;
    private final List<PlayerQuestReward> playerQuestRewards = new ArrayList<>();

    public PlayerQuestData(String questName, MutableComponent description){
        this.questName = questName;
        this.description = description;
    }

    public PlayerQuestData(CompoundTag nbt){
        deserializeNBT(nbt);
    }

    public void putObjective(String objectiveName, PlayerQuestObjectiveData data){
        objectives.put(objectiveName, data);
    }

    public List<PlayerQuestReward> getQuestRewards() {
        return playerQuestRewards;
    }

    public void addReward(PlayerQuestReward questReward){
        playerQuestRewards.add(questReward);
    }

    public boolean isComplete(){
        return objectives.values().stream().allMatch(PlayerQuestObjectiveData::isComplete);
    }

    public MutableComponent getDescription() {
        return description;
    }

    public Collection<PlayerQuestObjectiveData> getObjectives(){
        return objectives.values();
    }

    public PlayerQuestObjectiveData getObjective(String objectiveName){
        return objectives.get(objectiveName);
    }


    public String getQuestName() {
        return questName;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag objectiveNbt = new ListTag();
        for (Map.Entry<String, PlayerQuestObjectiveData> entry : objectives.entrySet()){
            objectiveNbt.add(entry.getValue().serializeNBT());
        }
        nbt.put("objectives", objectiveNbt);
        nbt.putString("questName", questName);
        nbt.putString("description", Component.Serializer.toJson(description));
        ListTag rewardNbt = new ListTag();
        for (PlayerQuestReward reward : playerQuestRewards){
            rewardNbt.add(reward.serializeNBT());
        }
        nbt.put("rewards", rewardNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag objectiveNbt = nbt.getList("objectives", Tag.TAG_COMPOUND);
        for (Tag objNbt : objectiveNbt){
            PlayerQuestObjectiveData objective = new PlayerQuestObjectiveData((CompoundTag) objNbt);
            objectives.put(objective.getObjectiveName(), objective);
        }
        questName = nbt.getString("questName");
        description = Component.Serializer.fromJson(nbt.getString("description"));
        ListTag rewardNbts = nbt.getList("rewards", Tag.TAG_COMPOUND);
        for (Tag rewardNbt : rewardNbts){
            addReward(new PlayerQuestReward((CompoundTag) rewardNbt));
        }
    }
}
