package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.data.QuestChainData;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjective;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class QuestChainInstance implements INBTSerializable<CompoundTag> {

    public static class QuestChainBuildResult {
        public QuestChainInstance instance;
        public Map<ResourceLocation, List<MKStructureEntry>> questStructures;

        public QuestChainBuildResult(QuestChainInstance instance,
                                     Map<ResourceLocation, List<MKStructureEntry>> structuresIn){
            this.instance = instance;
            questStructures = structuresIn;
        }

    }

    private UUID questId;
    private QuestDefinition definition;
    private QuestChainData questChainData;
    private Map<UUID, DialogueTree> dialogueTrees = new HashMap<>();
    private UUID questSourceNpc;

    public QuestChainInstance(QuestDefinition definition, Map<ResourceLocation, List<MKStructureEntry>> questStructures){
        questId = UUID.randomUUID();
        this.definition = definition;
        questChainData = new QuestChainData(definition, questStructures);
    }

    public void generateDialogue(Map<ResourceLocation, List<MKStructureEntry>> questStructures){
        questChainData.generateDialogue(this, getDialogueTreeName(), definition, questStructures,
                getSpeakingRoles(), dialogueTrees);
    }

    public void setQuestSourceNpc(UUID questSourceNpc) {
        this.questSourceNpc = questSourceNpc;
    }


    public Map<ResourceLocation, UUID> getSpeakingRoles(){
        Map<ResourceLocation, UUID> speakingRoles = new HashMap<>();
        for (Quest quest : definition.getQuestChain()){
            QuestData questData = questChainData.getQuestData(quest.getQuestName());
            for (QuestObjective<?> obj : quest.getObjectives()){
                if (obj instanceof TalkToNpcObjective){
                    TalkToNpcObjective talkObj = (TalkToNpcObjective) obj;
                    UUIDInstanceData instanceData = talkObj.getInstanceData(questData);
                    if (speakingRoles.containsKey(talkObj.getNpcDefinition()) && !speakingRoles.get(talkObj.getNpcDefinition()).equals(instanceData.getUuid())){
                        MKNpc.LOGGER.warn("Error: quest chain has 2 different npc definition with speaking roles {}", talkObj.getNpcDefinition());
                    }
                    speakingRoles.put(talkObj.getNpcDefinition(), instanceData.getUuid());
                }
            }
        }
        return speakingRoles;
    }

    public QuestChainInstance(CompoundTag nbt){
        deserializeNBT(nbt);
    }

    public UUID getQuestId() {
        return questId;
    }

    public List<String> getStartingQuestNames(){
        return definition.getFirstQuests().stream().map(Quest::getQuestName).collect(Collectors.toList());
    }


    public Optional<DialogueTree> getTreeForEntity(Entity entity){
        return MKNpc.getNpcData(entity).map(x -> {
            UUID entityId = x.getNotableUUID();
            return Optional.ofNullable(dialogueTrees.get(entityId));
        }).orElse(Optional.empty());
    }

    public Optional<Quest> getNextQuest(String currentQuest){
        Quest current = definition.getQuest(currentQuest);
        int currentIndex = definition.getQuestChain().indexOf(current);
        if (definition.getQuestChain().size() > currentIndex + 1){
            return Optional.of(definition.getQuestChain().get(currentIndex + 1));
        } else {
            return Optional.empty();
        }
    }

    public QuestChainData getQuestChainData() {
        return questChainData;
    }

    public QuestDefinition getDefinition() {
        return definition;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("questId", questId);
        nbt.putString("definitionId", definition.getName().toString());
        nbt.put("questData", questChainData.serializeNBT());
        if (questSourceNpc != null){
            nbt.putUUID("questSource", questSourceNpc);
        }
        CompoundTag dialogueNbt = new CompoundTag();
        for (Map.Entry<UUID, DialogueTree> entry : dialogueTrees.entrySet()){
            dialogueNbt.put(entry.getKey().toString(), entry.getValue().serialize(NbtOps.INSTANCE));
        }
//        nbt.put("dialogueTree", dialogueTree.serialize(NBTDynamicOps.INSTANCE));
        nbt.put("dialogueTrees", dialogueNbt);
        return nbt;
    }

    protected ResourceLocation getDialogueTreeName(){
        return new ResourceLocation(MKNpc.MODID, String.format("quest_dialogue.%s", questId.toString()));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        questId = nbt.getUUID("questId");
        definition = QuestDefinitionManager.getDefinition(new ResourceLocation(nbt.getString("definitionId")));
        questChainData = new QuestChainData(definition, nbt.getCompound("questData"));
        if (nbt.contains("questSource")){
            questSourceNpc = nbt.getUUID("questSource");
        }
        CompoundTag dialogueNbt = nbt.getCompound("dialogueTrees");
        dialogueTrees.clear();
        for (String key : dialogueNbt.getAllKeys()){
            UUID npcId = UUID.fromString(key);
            DialogueTree newTree = new DialogueTree(getDialogueTreeName());
            newTree.deserialize(new Dynamic<>(NbtOps.INSTANCE, dialogueNbt.get(key)));
            dialogueTrees.put(npcId, newTree);
        }
//        dialogueTree = new DialogueTree(getDialogueTreeName());
//        dialogueTree.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt.get("dialogueTree")));
    }

    public void signalQuestProgress(IWorldNpcData worldData, IPlayerQuestingData questingData, Quest currentQuest, PlayerQuestChainInstance playerInstance, boolean manualAdvance){
        PlayerQuestData playerData = playerInstance.getQuestData(currentQuest.getQuestName());
        questingData.questProgression(worldData, worldData.getQuest(playerInstance.getQuestId()));
        if (currentQuest.isComplete(playerData) || manualAdvance){
            if (currentQuest.shouldAutoComplete() || manualAdvance){
                questingData.advanceQuestChain(worldData, worldData.getQuest(playerInstance.getQuestId()), currentQuest);
            }
        }
    }

    public void signalObjectiveComplete(String objectiveName, IWorldNpcData worldData, IPlayerQuestingData questingData,
                                        Quest currentQuest, PlayerQuestChainInstance playerInstance){
        for (QuestObjective<?> obj : currentQuest.getObjectives()){
            if (obj.getObjectiveName().equals(objectiveName)){
                obj.signalCompleted(playerInstance.getQuestData(currentQuest.getQuestName()).getObjective(objectiveName));
            }
        }
        signalQuestProgress(worldData, questingData, currentQuest, playerInstance, false);
    }
}
