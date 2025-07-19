package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjective;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class QuestChainInstance implements INBTSerializable<CompoundTag> {

    private UUID questId;
    private QuestDefinition definition;
    private final Map<String, QuestData> questData = new HashMap<>();
    private final Map<UUID, DialogueTree> dialogueTrees = new HashMap<>();
    private UUID questSourceNpc;
    private final Level level;

    public QuestChainInstance(QuestDefinition definition, Map<QuestStructureLocation, MKStructureEntry> questStructures, Level level) {
        questId = UUID.randomUUID();
        this.definition = definition;
        this.level = level;
        for (Quest quest : definition.getQuestChain()) {
            QuestData qData = new QuestData(quest);
            for (QuestObjective<?> objective : quest.getObjectives()) {
                qData.putObjective(objective.getObjectiveName(), objective.generateInstanceData(questStructures, level));
            }
            questData.put(quest.getQuestName(), qData);
        }
    }

    public QuestChainInstance(HolderLookup.Provider provider, CompoundTag nbt, Level level) {
        this.level = level;
        deserializeNBT(provider, nbt);
    }

    public void generateDialogue(Map<QuestStructureLocation, MKStructureEntry> questStructures) {
        ResourceLocation dialogueName = getDialogueTreeName();
        Map<ResourceKey<NpcDefinition>, UUID> speakingRoles = getSpeakingRoles();

        for (Map.Entry<ResourceKey<NpcDefinition>, UUID> entry : speakingRoles.entrySet()) {
            DialogueTree tree = new DialogueTree(dialogueName);
            DialoguePrompt hailPrompt = new DialoguePrompt("hail");
            tree.addPrompt(hailPrompt);
            tree.setHailPrompt(hailPrompt);

            for (Quest quest : definition.getQuestChain()) {
                tree = quest.generateDialogueForNpc(this, entry.getKey(), entry.getValue(), tree, questStructures, definition);
            }
            dialogueTrees.put(entry.getValue(), tree);
        }
    }

    public void setQuestSourceNpc(UUID questSourceNpc) {
        this.questSourceNpc = questSourceNpc;
    }

    public Map<ResourceKey<NpcDefinition>, UUID> getSpeakingRoles() {
        Map<ResourceKey<NpcDefinition>, UUID> speakingRoles = new HashMap<>();
        for (Quest quest : definition.getQuestChain()) {
            QuestData questData = getQuestData(quest);
            for (QuestObjective<?> obj : quest.getObjectives()) {
                if (obj instanceof TalkToNpcObjective talkObj) {
                    UUIDInstanceData instanceData = talkObj.getInstanceData(questData);
                    if (speakingRoles.containsKey(talkObj.getNpcDefinition()) && !speakingRoles.get(talkObj.getNpcDefinition()).equals(instanceData.getUUID())) {
                        MKNpc.LOGGER.warn("Error: quest chain has 2 different npc definition with speaking roles {}", talkObj.getNpcDefinition());
                    }
                    speakingRoles.put(talkObj.getNpcDefinition(), instanceData.getUUID());
                }
            }
        }
        return speakingRoles;
    }

    public UUID getQuestId() {
        return questId;
    }

    public List<String> getStartingQuestNames() {
        return definition.getFirstQuests().stream().map(Quest::getQuestName).collect(Collectors.toList());
    }


    public Optional<DialogueTree> getTreeForEntity(Entity entity) {
        return MKNpc.getNpcData(entity).map(x -> {
            UUID entityId = x.getNotableUUID();
            return Optional.ofNullable(dialogueTrees.get(entityId));
        }).orElse(Optional.empty());
    }

    public Optional<Quest> getNextQuest(String currentQuest) {
        Quest current = definition.getQuest(currentQuest);
        int currentIndex = definition.getQuestChain().indexOf(current);
        if (definition.getQuestChain().size() > currentIndex + 1) {
            return Optional.of(definition.getQuestChain().get(currentIndex + 1));
        } else {
            return Optional.empty();
        }
    }

    public Level getLevel() {
        return level;
    }

    public QuestData getQuestData(Quest quest) {
        return questData.get(quest.getQuestName());
    }

    public QuestDefinition getDefinition() {
        return definition;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("questId", questId);
        nbt.putString("definitionId", definition.getName().toString());
        nbt.put("questData", serializeQuestParameters(provider));
        if (questSourceNpc != null) {
            nbt.putUUID("questSource", questSourceNpc);
        }
        CompoundTag dialogueNbt = new CompoundTag();
        for (Map.Entry<UUID, DialogueTree> entry : dialogueTrees.entrySet()) {
            dialogueNbt.put(entry.getKey().toString(), entry.getValue().serialize(NbtOps.INSTANCE));
        }
        nbt.put("dialogueTrees", dialogueNbt);
        return nbt;
    }

    private CompoundTag serializeQuestParameters(HolderLookup.Provider provider) {
        CompoundTag questNbt = new CompoundTag();
        for (Map.Entry<String, QuestData> entry : questData.entrySet()) {
            if (entry.getKey() == null) {
                MKNpc.LOGGER.error("QUEST WITH NULL NAME BEING SERIALIZED: {}", getDefinition().getName().toString());
                continue;
            }
            questNbt.put(entry.getKey(), entry.getValue().serializeNBT(provider));
        }
        return questNbt;
    }

    protected ResourceLocation getDialogueTreeName() {
        return MKNpc.id(String.format("quest_dialogue.%s", questId.toString()));
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        questId = nbt.getUUID("questId");
        var questKey = ResourceKey.create(QuestRegistries.QUEST_DEFINITIONS, ResourceLocation.parse(nbt.getString("definitionId")));
        definition = provider.lookupOrThrow(QuestRegistries.QUEST_DEFINITIONS).getOrThrow(questKey).value();
        deserializeQuestParameters(provider, nbt.getCompound("questData"));
        if (nbt.contains("questSource")) {
            questSourceNpc = nbt.getUUID("questSource");
        }
        CompoundTag dialogueNbt = nbt.getCompound("dialogueTrees");
        dialogueTrees.clear();
        for (String key : dialogueNbt.getAllKeys()) {
            UUID npcId = UUID.fromString(key);
            DialogueTree newTree = DialogueTree.deserialize(new Dynamic<>(NbtOps.INSTANCE, dialogueNbt.get(key)));
            dialogueTrees.put(npcId, newTree);
        }
    }

    private void deserializeQuestParameters(HolderLookup.Provider provider, CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            Quest source = definition.getQuest(key);
            if (source != null) {
                QuestData data = new QuestData(source);
                data.deserializeNBT(provider, tag.getCompound(key), source);
                questData.put(source.getQuestName(), data);
            }
        }
    }

    public void signalQuestProgress(IWorldNpcData worldData, IPlayerQuestingData questingData, Quest currentQuest, PlayerQuestChainInstance playerInstance, boolean manualAdvance) {
        PlayerQuestData playerData = playerInstance.getQuestData(currentQuest.getQuestName());
        QuestChainInstance questInstance = ContentDB.getQuestInstance(playerInstance.getQuestId());
        questingData.questProgression(worldData, questInstance);
        if (currentQuest.isComplete(playerData) || manualAdvance) {
            if (currentQuest.shouldAutoComplete() || manualAdvance) {
                questingData.advanceQuestChain(worldData, questInstance, currentQuest);
            }
        }
    }

    public void signalObjectiveComplete(String objectiveName, IWorldNpcData worldData, IPlayerQuestingData questingData,
                                        Quest currentQuest, PlayerQuestChainInstance playerInstance) {
        for (QuestObjective<?> obj : currentQuest.getObjectives()) {
            if (obj.getObjectiveName().equals(objectiveName)) {
                obj.signalCompleted(playerInstance.getQuestData(currentQuest.getQuestName()).getObjective(objectiveName));
            }
        }
        signalQuestProgress(worldData, questingData, currentQuest, playerInstance, false);
    }
}
