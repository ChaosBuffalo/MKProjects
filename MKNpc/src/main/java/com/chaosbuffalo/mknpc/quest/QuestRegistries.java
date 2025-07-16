package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjectiveType;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjectiveTypes;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirementType;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirementTypes;
import com.chaosbuffalo.mknpc.quest.rewards.QuestRewardType;
import com.chaosbuffalo.mknpc.quest.rewards.QuestRewardTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class QuestRegistries {
    public static final ResourceKey<Registry<QuestRewardType<?>>> QUEST_REWARD_TYPES_REGISTRY_NAME = ResourceKey.createRegistryKey(MKNpc.id("quest_reward_types"));
    public static final ResourceKey<Registry<QuestRequirementType<?>>> QUEST_REQUIREMENT_TYPES_REGISTRY_NAME = ResourceKey.createRegistryKey(MKNpc.id("quest_requirement_types"));
    public static final ResourceKey<Registry<QuestObjectiveType<?>>> QUEST_OBJECTIVE_TYPES_REGISTRY_NAME = ResourceKey.createRegistryKey(MKNpc.id("quest_objective_types"));
    public static final Registry<QuestRewardType<?>> QUEST_REWARDS = new RegistryBuilder<>(QUEST_REWARD_TYPES_REGISTRY_NAME)
            .create();
    public static final Registry<QuestRequirementType<?>> QUEST_REQUIREMENTS = new RegistryBuilder<>(QUEST_REQUIREMENT_TYPES_REGISTRY_NAME)
            .create();
    public static final Registry<QuestObjectiveType<?>> QUEST_OBJECTIVES = new RegistryBuilder<>(QUEST_OBJECTIVE_TYPES_REGISTRY_NAME)
            .create();

    public static final ResourceKey<Registry<QuestDefinition>> QUEST_DEFINITIONS = ResourceKey.createRegistryKey(MKNpc.id("mkquests"));


    public static void createRegistries(NewRegistryEvent event) {
        event.register(QUEST_REWARDS);
        event.register(QUEST_REQUIREMENTS);
        event.register(QUEST_OBJECTIVES);
    }

    public static void createDataRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(QUEST_DEFINITIONS, QuestDefinition.CODEC);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(QuestRegistries::createRegistries);
        modBus.addListener(QuestRegistries::createDataRegistries);
        QuestRewardTypes.REGISTRY.register(modBus);
        QuestRequirementTypes.REGISTRY.register(modBus);
        QuestObjectiveTypes.REGISTRY.register(modBus);
    }
}
