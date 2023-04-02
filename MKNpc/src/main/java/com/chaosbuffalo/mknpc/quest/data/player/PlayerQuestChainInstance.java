package com.chaosbuffalo.mknpc.quest.data.player;

import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PlayerQuestChainInstance implements IMKSerializable<CompoundTag> {
    private Consumer<PlayerQuestChainInstance> dirtyNotifier;
    private UUID questId;
    private Component questName;
    private List<String> currentQuests;
    private boolean questComplete;
    private final LinkedHashMap<String, PlayerQuestData> questData = new LinkedHashMap<>();

    public PlayerQuestChainInstance(UUID questId){
        this.questId = questId;
        questComplete = false;
    }

    public LinkedHashMap<String, PlayerQuestData> getQuestData() {
        return questData;
    }

    public PlayerQuestChainInstance(CompoundTag nbt){
        deserialize(nbt);
    }

    public boolean isQuestComplete() {
        return questComplete;
    }

    public UUID getQuestId() {
        return questId;
    }

    public void setQuestName(Component questName) {
        this.questName = questName;
    }

    public void setupQuestChain(QuestChainInstance instance){
        setQuestName(instance.getDefinition().getQuestName());
    }

    public Component getQuestName() {
        return questName;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("questName", Component.Serializer.toJson(questName));
        tag.putUUID("questId", questId);
        ListTag currentQuestsNbt = new ListTag();
        for (String questName : currentQuests){
            currentQuestsNbt.add(StringTag.valueOf(questName));
        }
        tag.put("currentQuests", currentQuestsNbt);
        tag.putBoolean("questComplete", questComplete);
        ListTag quests = new ListTag();
        for (Map.Entry<String, PlayerQuestData> entry : questData.entrySet()){
            quests.add(entry.getValue().serializeNBT());
        }
        tag.put("quests", quests);
        return tag;
    }

    public void addQuestData(PlayerQuestData questData){
        this.questData.put(questData.getQuestName(), questData);
    }

    public PlayerQuestData getQuestData(String questName){
        return questData.get(questName);
    }

    public List<String> getCurrentQuests() {
        return currentQuests;
    }


    public void addCurrentQuest(String questName){
        this.currentQuests.add(questName);
    }

    public void setCurrentQuests(List<String> currentQuests) {
        this.currentQuests = currentQuests;
    }

    @Override
    public boolean deserialize(CompoundTag compoundNBT) {
        questName = Component.Serializer.fromJson(compoundNBT.getString("questName"));
        questId = compoundNBT.getUUID("questId");
        ListTag currentQuestsNbt = compoundNBT.getList("currentQuests", Tag.TAG_STRING);
        currentQuests = currentQuestsNbt.stream().map(Tag::getAsString).collect(Collectors.toList());
        ListTag questData = compoundNBT.getList("quests", Tag.TAG_COMPOUND);
        for (Tag questNbt : questData){
            PlayerQuestData newData = new PlayerQuestData((CompoundTag) questNbt);
            this.questData.put(newData.getQuestName(), newData);
        }
        questComplete = compoundNBT.getBoolean("questComplete");
        return true;
    }

    public void setQuestComplete(boolean questComplete) {
        this.questComplete = questComplete;
    }

    public void setDirtyNotifier(Consumer<PlayerQuestChainInstance> dirtyNotifier) {
        this.dirtyNotifier = dirtyNotifier;
    }

    public void notifyDirty(){
        dirtyNotifier.accept(this);
    }
}
