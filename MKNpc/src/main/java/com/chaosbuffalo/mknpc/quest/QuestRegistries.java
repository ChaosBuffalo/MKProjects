package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjectiveType;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjectiveTypes;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirementType;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirementTypes;
import com.chaosbuffalo.mknpc.quest.rewards.QuestRewardType;
import com.chaosbuffalo.mknpc.quest.rewards.QuestRewardTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

public class QuestRegistries {
    public static final ResourceLocation QUEST_REWARD_TYPES_REGISTRY_NAME = new ResourceLocation(MKNpc.MODID, "quest_reward_types");
    public static final ResourceLocation QUEST_REQUIREMENT_TYPES_REGISTRY_NAME = new ResourceLocation(MKNpc.MODID, "quest_requirement_types");
    public static final ResourceLocation QUEST_OBJECTIVE_TYPES_REGISTRY_NAME = new ResourceLocation(MKNpc.MODID, "quest_objective_types");
    public static IForgeRegistry<QuestRewardType<?>> QUEST_REWARDS = null;
    public static IForgeRegistry<QuestRequirementType<?>> QUEST_REQUIREMENTS = null;
    public static IForgeRegistry<QuestObjectiveType<?>> QUEST_OBJECTIVES = null;


    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<QuestRewardType<?>>()
                .setName(QUEST_REWARD_TYPES_REGISTRY_NAME), r -> QUEST_REWARDS = r);
        event.create(new RegistryBuilder<QuestRequirementType<?>>()
                .setName(QUEST_REQUIREMENT_TYPES_REGISTRY_NAME), r -> QUEST_REQUIREMENTS = r);
        event.create(new RegistryBuilder<QuestObjectiveType<?>>()
                .setName(QUEST_OBJECTIVE_TYPES_REGISTRY_NAME), r -> QUEST_OBJECTIVES = r);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(QuestRegistries::createRegistries);
        QuestRewardTypes.REGISTRY.register(modBus);
        QuestRequirementTypes.REGISTRY.register(modBus);
        QuestObjectiveTypes.REGISTRY.register(modBus);
    }
}
