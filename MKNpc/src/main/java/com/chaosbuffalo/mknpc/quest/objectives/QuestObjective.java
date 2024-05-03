package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.ObjectiveInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class QuestObjective<T extends ObjectiveInstanceData> {
    public static final Codec<QuestObjective<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            QuestRegistries.QUEST_OBJECTIVES.getCodec().dispatch(QuestObjective::getType, QuestObjectiveType::codec));

    protected final String objectiveName;
    protected QuestStructureLocation location; // temporary

    public QuestObjective(String name) {
        objectiveName = name;
    }

    public QuestObjective(String name, QuestStructureLocation location) {
        objectiveName = name;
        this.location = location;
    }

    public abstract QuestObjectiveType<? extends QuestObjective<?>> getType();

    public abstract List<Component> getDescription();

    public String getObjectiveName() {
        return objectiveName;
    }

    // return true if it works or you dont care
    // return false only if this structure is needed but doesnt meet requirements
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return true;
    }

    public Optional<QuestStructureLocation> getStructure() {
        return Optional.ofNullable(location);
    }

    public abstract T generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures);

    public abstract T instanceDataFactory();

    public T loadInstanceData(CompoundTag nbt) {
        T data = instanceDataFactory();
        data.deserializeNBT(nbt);
        return data;
    }

    public abstract PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData);

    public T getInstanceData(QuestData data) {
        return (T) data.getObjective(getObjectiveName());
    }

    public void signalCompleted(PlayerQuestObjectiveData objectiveData) {
        objectiveData.setComplete(true);
    }

    public boolean isComplete(PlayerQuestObjectiveData playerData) {
        return playerData.isComplete();
    }
}
