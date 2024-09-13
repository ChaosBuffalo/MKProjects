package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.ObjectiveInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class QuestObjective<T extends ObjectiveInstanceData> {
    public static final Codec<QuestObjective<?>> CODEC = Codec.lazyInitialized(() ->
            QuestRegistries.QUEST_OBJECTIVES.byNameCodec().dispatch(QuestObjective::getType, QuestObjectiveType::codec));

    protected final String objectiveName;

    @Nullable
    protected final QuestStructureLocation location; // temporary

    public QuestObjective(String name) {
        this(name, null);
    }

    public QuestObjective(String name, QuestStructureLocation location) {
        objectiveName = name;
        this.location = location;
    }

    @Nullable
    public QuestStructureLocation getLocation() {
        return location;
    }

    public abstract QuestObjectiveType<? extends QuestObjective<?>> getType();

    public abstract List<Component> getDescription(IWorldNpcData worldData);

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

    public abstract T generateInstanceData(Map<QuestStructureLocation, MKStructureEntry> questStructures, Level level);

    public abstract T instanceDataFactory();

    public T loadInstanceData(HolderLookup.Provider provider, CompoundTag nbt) {
        T data = instanceDataFactory();
        data.deserializeNBT(provider, nbt);
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
